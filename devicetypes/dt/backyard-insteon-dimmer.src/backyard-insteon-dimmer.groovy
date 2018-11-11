metadata {
	definition (
    name: "Backyard Insteon Dimmer", 
    namespace: "dt", 
    author: "dt") {
		  capability "Actuator"
		  capability "Sensor"
		  capability "Switch"
		  capability "Switch Level"
	}


	simulator {
		// TODO: define status and reply messages here
	}

	tiles {
		// TODO: define your main and details tiles here
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
  def data = new groovy.json.JsonSlurper().parseText(hubResponse.body)  
  //log.debug "Reponse ${data.level}"
  if (data.level == 100) {
    sendEvent(name: "switch", value: "on")
  } else {
    sendEvent(name: "switch", value: "off")
  }
}

def installed() {
  init()
}

def updated() {
  init()
}

def poll() {
  localMessage("192.168.1.32:4201", "/backyard/light?token=aADDs32d9e34DS2jad23LK")
}

def init() {
  runEvery1Minute(poll)
}

// parse events into attributes
def parse(String description) {
  log.debug "Parsing '${description}'"
  // TODO: handle 'switch' attribute
  // TODO: handle 'level' attribute
  
}

// handle commands
def on() {
  localMessage("192.168.1.32:4201", "/backyard/light?token=aADDs32d9e34DS2jad23LK&level=100")
}

def off() {
  localMessage("192.168.1.32:4201", "/backyard/light?token=aADDs32d9e34DS2jad23LK&level=0")
}

def setLevel() {
  log.debug "Executing 'setLevel'"
  // TODO: handle 'setLevel' command
}