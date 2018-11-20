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
    	input "tBulbs", "capability.colorTemperature", multiple: false
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
	state.temp = 6500
  	runEvery1Hour(hourUpdate)
}

def hourUpdate() {
	def current = tBulbs.currentValue("colorTemperature")
	log.debug "color hourUpdate() " + state.temp + " " + current
	if (current != state.temp) {
		log.debug "manual color override: " + current
		state.temp = current
	}
	if (state.temp > 2700) {
  		state.temp = state.temp - 350 	
		log.debug "updated: " + state.temp
		tBulbs.setColorTemperature(getTemp())
  	} 
	tBulbs.setLevel(getLevel())
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
  	state.temp = 6500
    return 2700
  } else {
    return state.temp
  }
}

