metadata {
	definition (
    name: "Insteon switch", 
    namespace: "dt", 
    author: "dt") {
		  capability "Actuator"
		  capability "Switch"
		  capability "Sensor"
	}

	// simulator metadata
	simulator {
	}

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

def localMessage(String host, String path)
{
  //log.debug "Sending request to ${host}${path}"
    
  def theAction = new physicalgraph.device.HubAction("""GET ${path} HTTP/1.1\r\n Accept: */*\r\nHOST: ${host}\r\n\r\n""", 
						     physicalgraph.device.Protocol.LAN, 
						     "${host}", 
						     [callback: calledBackHandler])

  sendHubCommand(theAction)
}

void calledBackHandler(physicalgraph.device.HubResponse hubResponse)
{
  //log.debug "hub response: ${hubResponse.body}"
  def data = new groovy.json.JsonSlurper().parseText(hubResponse.body)  
  //log.debug "Reponse ${data.level}"
  if (data.level == 100) {
    sendEvent(name: "switch", value: "on")
  } else {
    sendEvent(name: "switch", value: "off")
  }
}

def parse(String description) {
}

def installed() {
  init()
}

def updated() {
  init()
}

def poll() {
  localMessage("192.168.1.32:4201", "/bathroom/light?token=aADDs32d9e34DS2jad23LK")
}

def init() {
  runEvery1Minute(poll)
}

def on() {
  try {
    localMessage("192.168.1.32:4201", "/bathroom/light?token=aADDs32d9e34DS2jad23LK&level=100")
  } catch (e) {
    log.error "something went wrong: $e"
  }
}

def off() {
  localMessage("192.168.1.32:4201", "/bathroom/light?token=aADDs32d9e34DS2jad23LK&level=0")
}