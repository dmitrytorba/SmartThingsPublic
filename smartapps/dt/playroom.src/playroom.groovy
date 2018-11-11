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
  section("Bright lights between what times?") {
    input "fromTime", "time", title: "From", required: true
    input "toTime", "time", title: "To", required: true
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
}

def init() {
  subscribe(playroomMotion, "motion", onMotion)
  subscribe(killSwitch, "switch", onKill)
  subscribe(sleepSwitch, "switch", onSleep)
  subscribe(sleepButton, "button", onSleepButton)
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
  def between = timeOfDayIsBetween(fromTime, toTime, new Date(), location.timeZone)
  if (!between) {
    return 20
  } else {
    return 100
  }
}

def getTemp() {
  def between = timeOfDayIsBetween(fromTime, toTime, new Date(), location.timeZone)
  if (!between) {
    return 2700
  } else {
    return 6500
  }
}

def onMotion(evt) {
  def automationOn = killSwitch.currentValue("switch") != "on"
  def sleepMode = sleepSwitch.currentValue("switch") == "on"
  if (automationOn && !sleepMode) {
    if (evt.value == "active") { 
      log.debug("playroom motion, light on")
      def level = getLevel()
      bulbs.setLevel(level)
      def temp = getTemp()
      bulbTemp.setColorTemperature(temp)
    } else if (evt.value == "inactive") {
      check()
    }
  }
}

def check() {
  def motion = playroomMotion.currentState("motion")
  if (motion.value == "inactive") {
    def elapsed = now() - motion.rawDateCreated.time
    def threshold = 1000 * delay * 60 - 1000
    if (elapsed >= Threshold) {
      bulbs.off()
      return
    }
  }
  runIn(60, check)
}