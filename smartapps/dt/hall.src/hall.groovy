definition(
name: "hall",
namespace: "dt",
author: "dt",
description: "smart habitat",
category: "Convenience",
iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
  section("Bathroom Door"){
    input "bathDoor", "capability.contactSensor"
  }
  section("Hallway East"){
    input "eastHall", "capability.motionSensor"
  }
  section("Hallway West"){
    input "westHall", "capability.motionSensor"
  }
  section("Bathroom Motion"){
    input "bathroomMotion", "capability.motionSensor"
  }
  section("Hall off delay"){
    input "hallDelay", "number", title: "Minutes?"
  }
  section("Bathroom off delay"){
    input "bathDelay", "number", title: "Minutes?"
  }
  section("Bright lights between what times?") {
    input "fromTime", "time", title: "From", required: true
    input "toTime", "time", title: "To", required: true
  }
  section("Alarm") {
    input "alarmFrom", "time", title: "From", required: true
    input "alarmTo", "time", title: "To", required: true
  }
  section("Motion Controlled Bulbs"){
    input "bulbs", "capability.switchLevel", multiple: true
  }
  section("Turn off when sleeping"){
    input "annoyingBulbs", "capability.switchLevel", multiple: true
  }
  section("Bulb Temperature"){
    input "bulbTemp", "capability.colorTemperature", multiple: true
  }
  section("Bathroom Bulbs"){
    input "bathroomBulbs", "capability.switchLevel", multiple: true
  }
  section("Bathroom Switch"){
    input "bathroomSwitch", "capability.switch"
  }
  section("Hallway Bulb (rgb)"){
    input "hallBulbColor", "capability.colorControl"
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
}

def init() {
  subscribe(bathDoor, "contact", onMotion)
  subscribe(eastHall, "motion", onMotion)
  subscribe(westHall, "motion", onMotion)
  subscribe(bathroomMotion, "motion", onMotion)
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
    hallBulbColor.setHue(240)
    hallBulbColor.setSaturation(100)
    bathroomColor.setHue(240)
    bathroomColor.setSaturation(100)
  } else if (bathroomMotion.currentState("motion").value == "active" && bathContact == "closed") {
    log.debug "bathroom occupied!"
    hallBulbColor.setHue(97)
    hallBulbColor.setSaturation(99)
  } else if(!sleepTime) {
    def currentTemp = bulbTemp.currentValue("colorTemperature")
    log.debug "Current temp: $currentTemp"
    def temp = getTemp()
    if (currentTemp[0] != temp) {
      bulbTemp.setColorTemperature(temp)
    }
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
      if (sleepTime) {
        bathroomBulbs.setLevel(10)
      } else {
        setColors()
        bulbs.setLevel(level)
      }
    }
    check()
  }
}

def check() {
  log.debug "check()"
  def east = eastHall.currentState("motion")
  def west = westHall.currentState("motion")
  def bath = bathroomMotion.currentState("motion")
  if (east.value == "inactive" && west.value == "inactive") {
    if (bath.value == "inactive") {
      def elapsedEast = now() - east.rawDateCreated.time
      def elapsedWest = now() - west.rawDateCreated.time
      def elapsedBath = now() - bath.rawDateCreated.time
      def hallThreshold = 1000 * hallDelay * 60 - 1000
      def bathThreshold = 1000 * bathDelay * 60 - 1000
      if (elapsedEast >= hallThreshold && elapsedWest >= hallThreshold && elapsedBath >= bathThreshold) {
        bulbs.off()
        return
      }
    } 
  } 
  runIn(60, check)
}