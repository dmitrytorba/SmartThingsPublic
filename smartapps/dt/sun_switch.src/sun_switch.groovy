definition(
	name: "sun switch",
	namespace: "dt",
	author: "dt",
	description: "smart habitat",
	category: "Convenience",
	iconUrl: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch.png",
	iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Meta/temp_thermo-switch@2x.png"
)

preferences {
	section("Sun"){
    	input "sun", "capability.switch"
  	}
}

def init() {
  subscribe(location, "sunriseTime", sunriseTimeHandler)
  subscribe(location, "sunsetTime", sunsetTimeHandler)
  // TODO init sun switch 
}

def sunsetTimeHandler(evt) {
  sun.off()
}

def sunriseTimeHandler(evt) {
  sun.on()
}