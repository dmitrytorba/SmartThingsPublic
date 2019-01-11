metadata {
	definition (
	name: "Virtual Switch",
	namespace: "dt",
	author: "dt") {
	  capability "Switch"
	}

	// simulator metadata
	simulator {
	}

	command "toggle"
	// UI tile definitions
	tiles {
	  standardTile("switch", "device.switch", width: 2, height: 2, canChangeIcon: true) {
	    state "off", label: '${currentValue}', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff"
	    state "on", label: '${currentValue}', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC"
	  }
	  main "switch"
	  details "switch"
	}
}

def parse(String description) {
  log.trace "virtual switch parse(): " + description
}

def on() {
  if (device.currentValue("switch") == "off") {
  	log.trace "virtual on"
  	sendEvent(name: "switch", value: "on")	
  }
}

def off() {
  if (device.currentValue("switch") == "on") {
  	log.trace "virtual off"
  	sendEvent(name: "switch", value: "off")
  }
  state.val = "off"
}

def toggle() {
  log.trace "virtual toggle"
  def isOn = device.currentValue("switch") == "on"
  if (isOn) {
    off()
  } else {
    on()
  }
}