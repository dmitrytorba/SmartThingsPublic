definition(
    name: "livingroom",
    namespace: "dt",
    author: "dt",
    description: "room controler",
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
}

def installed() {
	log.debug "Installed with settings: ${settings}"
	init()
}

def updated() {
	log.debug "Updated with settings: ${settings}"
	unsubscribe()
	init()
}

def init() {
  subscribe(motion, "motion", onMotion)
  subscribe(killSwitch, "switch", onKill)
  subscribe(sleepSwitch, "switch", onSleep)

  runEvery1Minute(minuteUpdate)
}

def minuteUpdate() {
  bulbs.setLevel(getLevel())
  tBulbs.setColorTemperature(getTemp())
}   

def onSleep(evt) {
  if (evt.value == "on") {
    bulbs.off()
  } else if (evt.value == "off") {
    bulbs.on()
  }
}

def onKill(evt) {
  if (evt.value == "on") {
    bulbs.on()
    bulbs.setLevel(100)
  } else if (evt.value == "off") {
    bulbs.off()
  }
}
def getLevel() {
  return controlBulb.currentValue("level")
}

def getTemp() {
  return controlBulb.currentValue("colorTemperature")
}

def onMotion(evt) {
  def automationOn = killSwitch.currentValue("switch") != "on"
  def sleepMode = sleepSwitch.currentValue("switch") == "on"
  log.trace "room onMotion() ${evt.value} ${automationOn} ${sleepMode}" 
  if (automationOn && !sleepMode) {
    if (evt.value == "active") { 
      log.debug("motion, light on")
      def level = getLevel()
      bulbs.setLevel(level)
      def temp = getTemp()
      tBulbs.setColorTemperature(temp)
    } else if (evt.value == "inactive") {
      check()
    }
  }
}

def check() {
  log.trace "room check()"
  def motionData = motion.currentState("motion")
  if (motion.value == "inactive") {
    def elapsed = now() - motionData.rawDateCreated.time
    def threshold = 1000 * delay * 60 - 1000
    if (elapsed >= threshold) {
      log.trace "room elapsed: " + elapsed
      bulbs.off()
      return
    }
  }
  runIn(60, check)
}