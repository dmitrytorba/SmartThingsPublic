definition(
    name: "bathroom",
    namespace: "dt",
    author: "dt",
    description: "smart habitat",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
  section("Motion Sensor"){
    input "motion", "capability.motionSensor", required: false
  }
  section("Turn off delay"){
    input "delay", "number", title: "Minutes?"
  }
  section("Dimmable Bulbs"){
    input "bulbs", "capability.switchLevel", multiple: true
  }
  section("Temperature Bulbs"){
    input "tBulbs", "capability.colorTemperature", multiple: true
  }
  section("Kill Switch"){
    input "killSwitch", "capability.switch"
  }
  section("Sleep Switch"){
    input "sleepSwitch", "capability.switch"
  }
  section("Control Bulb"){
    input "controlBulb", "capability.colorTemperature"
  }      
  section("Bathroom Door"){
    input "bathDoor", "capability.contactSensor"
  }
  section("Alarm") {
    input "alarmFrom", "time", title: "From", required: true
    input "alarmTo", "time", title: "To", required: true
  }
  section("Turn off when sleeping"){
    input "annoyingBulbs", "capability.switchLevel", multiple: true
  }
  section("RGB Bulb")
    input "rgbBulbs", "capability.colorControl"
  }
}

def init() {
  subscribe(bathDoor, "contact", onMotion)
  subscribe(motion, "motion", onMotion)
  subscribe(sleepSwitch, "switch", onSleep)
  subscribe(killSwitch, "switch", onKill)
}

def onKill(evt) {
  if (evt.value == "on") {
    bulbs.on()
    bulbTemp.setColorTemperature(6500)
  } else if (evt.value == "off") {
    bulbs.off()
  }
}

def onSleep(evt) {
  if (evt.value == "on") {
    annoyingBulbs.off()
  } else if (evt.value == "off") {
    annoyingBulbs.on()
  }
}

def installed() {
  log.debug("installing...")
  init()
}

def updated() {
  log.debug("updating...")
  unsubscribe()
  init()
  log.debug("done updating!")
}

def getLevel() {
  return controlBulb.currentValue("level")
}

def getTemp() {
  return controlBulb.currentValue("colorTemperature")
}

def isAlarm(evt) {
  def df = new java.text.SimpleDateFormat("EEEE")
  df.setTimeZone(location.timeZone)
  def day = df.format(new Date())
  def weekdays = ["Monday", "Tuesday", "Wendsday", "Thursday", "Friday"]
  if (weekdays.contains(day)) {
    def isAlarmTime = timeOfDayIsBetween(alarmFrom, alarmTo, new Date(), location.timeZone)
    return isAlarmTime
  }
  return false
}

def setColors() {
  def bathContact = bathDoor.currentState("contact").value
  def sleepTime = sleepSwitch.currentValue("switch") == "on"
  if (isAlarm()) {
    log.debug "alarm!"
    rgbBulbs.setHue(240)
    rgbBulbs.setSaturation(100)
  } else if (bathroomMotion.currentState("motion").value == "active" && bathContact == "closed") {
    log.debug "bathroom occupied!"
    rgbBulbs.setHue(97)
    rgbBulbs.setSaturation(99)
  } else if(!sleepTime) {
    tBulbs.setColorTemperature(getTemp())
  }
}

def onMotion(evt) {
  def sleepTime = sleepSwitch.currentValue("switch") == "on"
  def automationOn = killSwitch.currentValue("switch") != "on"
  if (automationOn) {
    log.debug "onMotion() event: $evt.value"
    if (evt.value == "closed" || 
        evt.value == "active" ||
        evt.value == "open" ) {
      def level = getLevel()
      if (!sleepTime) {
        setColors()
        bulbs.setLevel(level)
      }
    }
    runIn(60, check)
  }
}

def check() {
  log.trace "playroom check()"
  def motionData = motion.currentState("motion")
  if (motionData.value == "inactive") {
    def elapsed = now() - motionData.rawDateCreated.time
    def threshold = 1000 * delay * 60 - 1000
    if (elapsed >= threshold) {
      log.trace "hall elapsed: " + elapsed
      bulbs.off()
      return
    }
  }
  runIn(60, check)
}
