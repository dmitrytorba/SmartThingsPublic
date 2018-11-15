definition(
    name: "color",
    namespace: "dt",
    author: "dt",
    description: "Dynamic light control",
    category: "My Apps",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
	section("Bright lights between what times?") {
    	input "fromTime", "time", title: "From", required: true
    	input "toTime", "time", title: "To", required: true
  	}
	section("Temperature Bulbs"){
    	input "tBulbs", "capability.colorTemperature", multiple: true
  	}
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
  	runEvery1Minute(main)
}

def main() {
	tBulbs.setLevel(getLevel())
	tBulbs.setColorTemperature(getTemp())
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

