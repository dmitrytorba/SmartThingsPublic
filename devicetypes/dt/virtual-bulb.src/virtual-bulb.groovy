metadata {
	definition (
    name: "Virtual Bulb", 
    namespace: "dt", 
    author: "dt") {
	   capability "Actuator"
	   capability "Switch"
	   capability "Sensor"
	   capability "Switch Level"
	   capability "Color Temperature"
	}

	// simulator metadata
	simulator {
	}
	
	attribute "bulbTemp", "string"
        attribute "disable", "string"
        command "todisable"
        command "toenable"
        command "toggleEnable"
        command "tuneup"
        command "tunedown"

	// UI tile definitions
	tiles {
	  standardTile("button", "device.switch", width: 2, height: 2, canChangeIcon: true) {
	    state "off", label: 'Off', action: "switch.on", icon: "st.switches.switch.off", backgroundColor: "#ffffff", nextState: "on"
	    state "on", label: 'On', action: "switch.off", icon: "st.switches.switch.on", backgroundColor: "#00A0DC", nextState: "off"
	  }
	  controlTile("levelSliderControl", "device.level", "slider", height: 1, width: 2, inactiveLabel: false) {
	    state "level", action:"switch level.setLevel"
	  }
	  controlTile("colorSliderControl",
		      "device.colorTemperature",
		      "slider",
	              height: 1,
	              width: 2,
	              inactiveLabel: false,
	              range: "(2700..6500)") {
	    state "colorTemperature", action:"color temperature.setColorTemperature"
	  }
	  valueTile("kelvin", "device.colorTemperature") {
	    state "colorTemperature", label:'${currentValue}k',
	  backgroundColors:[
	    [value: 2900, color: "#FFA757"],
	    [value: 3300, color: "#FFB371"],
	    [value: 3700, color: "#FFC392"],
	    [value: 4100, color: "#FFCEA6"],
	    [value: 4500, color: "#FFD7B7"],
	    [value: 4900, color: "#FFE0C7"],
	    [value: 5300, color: "#FFE8D5"],
	    [value: 6600, color: "#FFEFE1"]
	  ]
	  }
	  valueTile("bulbTemp", "device.bulbTemp", inactiveLabel: false, decoration: "flat") {
	    state "bulbTemp", label: '${currentValue}'
	  }
	  valueTile("level", "device.level", inactiveLabel: false, decoration: "flat") {
	    state "level", label: 'Level ${currentValue}%'
	  }
	  standardTile("disable", "device.disable", width: 1, height: 1, canChangeIcon: true) {
	    state "off", label: 'enabled', action: "todisable", icon: "st.switches.light.on", backgroundColor: "#79b821"
	    state "on", label: 'disabled', action: "toenable", icon: "st.switches.light.off", backgroundColor: "#ff0000"
	  }  
	  main(["switch"])
	  details(["switch", "bulbTemp", "refresh", "levelSliderControl", "level", "colorSliderControl", "kelvin"])
	}
}

def on() {
  sendEvent(name: "switch", value: "on")
}

def off() {
  sendEvent(name: "switch", value: "off")
}

def parse(String description) {
}

def todisable() {
  state.switchDisable = true
  sendEvent(name: "disable", value: "on")
}

def toenable() {
  state.switchDisable = false;
  sendEvent(name: "disable", value: "off")
}

def toggleEnable() {
  if(state.switchDisable){
    toenable()
  }else{
    todisable()
  }
}

def tuneup(){
  def level = device.currentValue("level")
  log.info("switchLevel: $level")
  if(level < 100){
    setLevel(level+20)
  }
}

def tunedown(){
  def level = device.currentValue("level")
  log.info("switchLevel: $level")
  if(level >20){
    setLevel(level-20)
  }
}
def setLevel(val) {
  if(state.switchDisable )
    return
    log.info "setLevel $val"
    
    // make sure we don't drive switches past allowed values (command will hang device waiting for it to
    // execute. Never commes back)
    if (val < 0){
      val = 0
    }

    if( val > 100){
      val = 100
    }

    if (val == 0){ 
      sendEvent(name:"level",value:val)
      off()
    } else {
      on()
      sendEvent(name:"level",value:val)
      sendEvent(name:"switch.setLevel",value:val)
    }
}

def setColorTemperature(value) {
  log.trace "setColorTemperature($value)"

  def degrees = Math.max(2700, value)
  if(value > 2700){
    degrees = Math.min(6500, value)
  }
  def bTemp = getBulbTemp(value)
  
  log.trace degrees
  
  sendEvent(name: "colorTemperature", value: degrees)
  sendEvent( name: "bulbTemp", value: bTemp)

}

private getBulbTemp(value) {
  def s = "Soft White"
  
  if (value < 2900) {
    return s
  } else if (value < 3350) {
    s = "Warm White"
    return s
  }
  else if (value < 3900) {
    s = "Cool White"
    return s
  }
  else if (value < 4800) {
    s = "Bright White"
    return s
  }
  else if (value < 5800) {
    s = "Natural"
    return s
  }
  else {
    s = "Daylight"
    return s
  }
}