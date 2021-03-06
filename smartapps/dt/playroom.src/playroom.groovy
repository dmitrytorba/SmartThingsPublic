definition(
name: "playroom",
namespace: "dt",
author: "Dmitry",
description: "smart habitat",
category: "Convenience",
iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
  section("Playroom Motion"){
    input "playroomMotion", "capability.motionSensor"
  }
  section("Turn off delay"){
    input "delay", "number", title: "Minutes?"
  }
  section("Bulbs"){
    input "bulbs", "capability.switchLevel", multiple: true
  }
  section("Bulb Temperature"){
    input "bulbTemp", "capability.colorTemperature", multiple: true
  }
  section("Kill Switch"){
    input "killSwitch", "capability.switch"
  }
  section("Sleep Switch"){
    input "sleepSwitch", "capability.switch"
  }
  section("Sleep Button"){
    input "sleepButton", "capability.button"
  }
  section("Control Bulb"){
    input "controlBulb", "capability.colorTemperature"
  }
}

def init() {
  subscribe(playroomMotion, "motion", onMotion)
  //subscribe(killSwitch, "switch", onKill)
  subscribe(sleepSwitch, "switch", onSleep)
  subscribe(sleepButton, "button", onSleepButton)
  subscribe(controlBulb, "colorTemperature", onControlTemp)
  subscribe(controlBulb, "level", onControlLevel)
  subscribe(bulbs, "colorTemperature", onOverrideTemp)
  subscribe(bulbs, "level", onOverrideLevel)
}

def onOverrideLevel(evt) {
  log.debug "onOverrideLevel " + evt.value
  if (state.pending) {
    state.pending = false
  } else {
    killSwitch.on()
  }

}

def onOverrideTemp(evt) {
  if (state.pending) {
    state.pending = false
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
  bulbTemp.setColorTemperature(getTemp())
}   

def onSleepButton(evt) {
  log.trace "onSleepButton"
  switch(evt.value) {
    case "pushed":
      log.trace "button pushed"
      sleepSwitch.toggle()
      break
    case "double":
      killSwitch.toggle()
      break
    case "held":
      break
  }
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
    bulbTemp.setColorTemperature(temp)
  }
}

def onMotion(evt) {
  def automationOn = killSwitch.currentValue("switch") != "on"
  def sleepMode = sleepSwitch.currentValue("switch") == "on"
  log.trace "playroom onMotion() ${evt.value} ${automationOn} ${sleepMode}" 
  if (automationOn && !sleepMode) {
    if (evt.value == "active") { 
      log.debug("playroom motion, light on")
      lightsOn()    
    } else if (evt.value == "inactive") {
      check()
    }
  }
}

def check() {
  log.trace "playroom check()"
  def motion = playroomMotion.currentState("motion")
  if (motion.value == "inactive") {
    def elapsed = now() - motion.rawDateCreated.time
    def threshold = 1000 * delay * 60 - 1000
    if (elapsed >= threshold) {
      log.trace "playroom elapsed: " + elapsed
      state.pending = true
      bulbs.off()
      return
    }
  }
  runIn(60, check)
}