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
  section("Control Bulb"){
    input "controlBulb", "capability.colorTemperature"
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
  subscribe(sleepSwitch, "switch", onSleep)
  subscribe(controlBulb, "colorTemperature", onControlTemp)
  subscribe(controlBulb, "level", onControlLevel)
  subscribe(bulbs, "colorTemperature", onOverrideTemp)
  subscribe(bulbs, "level", onOverrideLevel)
}

def pendingOff() {
    state.pending = false
}

def onOverrideLevel(evt) {
  def sw = bulbs.currentValue("switch")
  log.debug "onOverrideLevel: ${evt.value} ${evt.displayName} ${evt.isStateChange()} ${sw}" 
  if (state.pending) {
    runIn(1, pendingOff)
  } else {
    killSwitch.on()
  }
}

def onOverrideTemp(evt) {
  def sw = bulbs.currentValue("switch")
	log.debug "onOverrideTemp: ${evt.value} ${evt.displayName} ${evt.isStateChange()} ${sw}" 
  if (state.pending) {
    runIn(1, pendingOff)
  } else {
    killSwitch.on()
  }
}

def onControlLevel(evt) {
	state.pending = true
  bulbs.setLevel(getLevel())
}

def onControlTemp(evt) {
	state.pending = true
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

def lightsOn() {
  def currentLevel = bulbs.currentValue("level")[0]
  def level = getLevel()
  log.debug "current: " + currentLevel
  log.debug "new: " + level
  if (Math.abs(currentLevel - level) < 5) {
    state.pending = true
    bulbs.setLevel(level)
  }
  def currentTemp = bulbs.currentValue("colorTemperature")[0]
  def temp = getTemp()
  log.debug "current: " + currentTemp
  log.debug "new: " + temp 
  if (Math.abs(currentTemp - temp) < 50) {
    state.pending = true
    tBulbs.setColorTemperature(temp)
  }
}

def onMotion(evt) {
  def automationOn = killSwitch.currentValue("switch") != "on"
  def sleepMode = sleepSwitch.currentValue("switch") == "on"
  log.trace "room onMotion() ${evt.value} ${automationOn} ${sleepMode}" 
  if (automationOn && !sleepMode) {
    if (evt.value == "active") { 
      log.debug("motion, light on")
      lightsOn()    
    } else if (evt.value == "inactive") {
      check()
    }
  }
}

def check() {
  log.trace "room check()"
  def motionData = motion.currentState("motion")
  if (motionData.value == "inactive") {
    def elapsed = now() - motionData.rawDateCreated.time
    def threshold = 1000 * delay * 60 - 1000
    if (elapsed >= threshold) {
      log.trace "room elapsed: " + elapsed
      state.pending = true
      bulbs.off()
      return
    }
  }
  runIn(60, check)
}