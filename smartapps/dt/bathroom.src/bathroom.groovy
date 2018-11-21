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
  section("Neighbor Motion Sensor"){
    input "neighborMotion", "capability.motionSensor", required: false
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
  subscribe(neighborMotion, "motion", onNeighborMotion)
  subscribe(killSwitch, "switch", onKill)
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

def onNeighborMotion(evt) {
  def automationOn = killSwitch.currentValue("switch") != "on"
  log.trace "room onNeighborMotion() ${evt.value} ${automationOn}" 
  if (automationOn) {
    if (evt.value == "active") { 
		lightsOn()
	} 
  	runIn(60, check)
  }
}

def lightsOn() {
	log.debug("motion, light on")
	def level = getLevel()
	bulbs.setLevel(level)
	def temp = getTemp()
	tBulbs.setColorTemperature(temp)
}

def onMotion(evt) {
  def automationOn = killSwitch.currentValue("switch") != "on"
  log.trace "room onMotion() ${evt.value} ${automationOn}" 
  if (automationOn) {
    if (evt.value == "active") { 
		lightsOn()
	} else if (evt.value == "inactive") {
		check()
    }
  }
}

def check() {
  def motionData = motion.currentState("motion")
  log.trace "room check() " + motionData.value
  if (motionData.value == "inactive") {
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