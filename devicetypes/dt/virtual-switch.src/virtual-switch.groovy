metadata {
	definition (
	name: "Virtual Switch",
	namespace: "dt",
	author: "dt") {
	  capability "Actuator"
	  capability "Switch"
	  capability "Sensor"
	}

	// simulator metadata
	simulator {
	}

	command "toggle"
	// UI tile definitions
	tiles {
	  standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
	    state "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
	    state "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "off"
	  }
	  main "button"
	  details "button"
	}
}

def parse(String description) {
}

def on() {
  log.trace "virtual on"
  sendEvent(name: "switch", value: "on")
}

def off() {
  log.trace "virtual off"
  sendEvent(name: "switch", value: "off")
}

def toggle() {
  def isOn = device.currentValue("switch") == "on"
  if (isOn) {
    off()
  } else {
    on()
  }
}