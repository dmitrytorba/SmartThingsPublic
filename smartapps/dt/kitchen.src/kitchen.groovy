definition(
name: "kitchen",
namespace: "dt",
author: "Dmitry",
description: "smart habitat",
category: "Convenience",
iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
  section("Kitchen Motion"){
    input "kitchenMotion", "capability.motionSensor"
  }
  section("Turn off delay"){
    input "delay", "number", title: "Minutes?"
  }
  section("Chanderlier Flood"){
    input "flood", "capability.switchLevel", multiple: true
  }
  section("Chandelier Dimmable"){
    input "bulb", "capability.switchLevel", multiple: true
  }
  section("Kill Switch"){
    input "killSwitch", "capability.switch"
  }
  section("Sleep Switch"){
    input "sleepSwitch", "capability.switch"
  }
  section("Sun"){
    input "sun", "capability.switch"
  }
}

def init() {
  subscribe(kitchenMotion, "motion", onMotion)
  subscribe(killSwitch, "switch", onKill)
  subscribe(sleepSwitch, "switch", onSleep)
  subscribe(sun, "switch", onSun)
}

def onSun(evt) {
  if (evt.value == "on") {
    bulb.off() 
  } else if (evt.value == "off") {
    bulb.setLevel(getLevel())
  }
}

def onSleep(evt) {
  bulb.setLevel(getLevel())
}

def onKill(evt) {
  if (evt.value == "on") {
    bulb.setLevel(100)
  } else if (evt.value == "off") {
    bulb.off()
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
  def isSleeping = sleepSwitch.currentValue("switch") == "on"
  if (isSleeping) {
    return 10 
  } else {
    return 100
  }
}

def onMotion(evt) {
  def automationOn = killSwitch.currentValue("switch") != "on"
  def sunOn = sun.currentValue("switch") == "on"
  if (automationOn && !sunOn) {
    if (evt.value == "active") {
      def level = getLevel()
      bulb.setLevel(level)
      log.debug("kitchen level: " +  level)
    }
    runIn(60, check)
  }
}

def check() {
  def automationOn = killSwitch.currentValue("switch") != "on"
  def sunOn = sun.currentValue("switch") == "on"
  if (automationOn && !sunOn) {
    log.debug("kitchen check()")
    def motion = kitchenMotion.currentState("motion")
    if (motion.value == "inactive") {
      def elapsed = now() - motion.rawDateCreated.time
      def threshold = 1000 * delay * 60 - 1000
      if (elapsed >= Threshold) {
	     log.debug("kitchen bulb off")
	     bulb.setLevel(0)
	     return
      }
    } 
    runIn(60, check)
  }
}