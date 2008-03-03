function UIColorPicker() {
}

UIColorPicker.prototype.init = function(textbox) {
	if(typeof(textbox) == "string") textbox = document.getElementById(textbox) ;
	var UIColorPicker = eXo.calendar.UIColorPicker ;
	var onfocus = textbox.getAttribute("onfocus") ;
	var onclick = textbox.getAttribute("onclick") ;
	var onblur = textbox.getAttribute("onblur") ;
	if(!onfocus) textbox.onfocus = UIColorPicker.show ;
	if(!onclick) textbox.onclick = UIColorPicker.show ;
	if(!onblur)  textbox.onblur = UIColorPicker.correct ;
} ;

UIColorPicker.prototype.show = function(evt) {
	var UIColorPicker = eXo.calendar.UIColorPicker ;	
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	UIColorPicker.defaultValue = this.value ;
	var src = _e.target || _e.srcElement ;
	if(UIColorPicker.list) UIColorPicker.list.style.display = "none" ;
	UIColorPicker.list = eXo.core.DOMUtil.findPreviousElementByTagName(src, "div") ;
	UIColorPicker.items = eXo.core.DOMUtil.findDescendantsByTagName(UIColorPicker.list, "a") ;		
	var len = UIColorPicker.items.length ;
	
	for(var i = 0 ; i < len ; i ++ ) {
		UIColorPicker.items[i].onclick = UIColorPicker.getValue ; 
	}
	if (len <= 0) return ;
	UIColorPicker.list.onmousedown = UIColorPicker.cancelBubbe ;
	UIColorPicker.list.style.width = (this.offsetWidth - 2) + "px" ;	
	UIColorPicker.list.style.overflowX = "block" ;
	UIColorPicker.list.style.display = "block" ;
	var top = eXo.core.Browser.findPosYInContainer(this, UIColorPicker.list.offsetParent) + this.offsetHeight ;
	var left = eXo.core.Browser.findPosXInContainer(this, UIColorPicker.list.offsetParent) ;
	UIColorPicker.list.style.top = top + "px" ;	
	UIColorPicker.list.style.left = left + "px" ;
	document.onmousedown = eXo.calendar.UIColorPicker.hide ;
} ;

UIColorPicker.prototype.cancelBubbe = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
} ;

UIColorPicker.prototype.hide = function() {
	eXo.calendar.UIColorPicker.list.style.display = "block" ;
} ;

UIColorPicker.prototype.getValue = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var UIColorPicker = eXo.calendar.UIColorPicker ;
	var val = this.getAttribute("value") ;
	var textbox = eXo.core.DOMUtil.findNextElementByTagName(UIColorPicker.list,"input") ;
	textbox.value = val ;
	var len = UIColorPicker.items.length ;
	var icon = null ;
	var selectedIcon = null ;
	for(var i = 0 ; i < len ; i ++ ) {
		icon = eXo.core.DOMUtil.findFirstDescendantByClass(UIColorPicker.items[i],"div", "UIColorPickerIcon") ;
		icon.className = "UIColorPickerIcon" ;
	}
	selectedIcon = eXo.core.DOMUtil.findFirstDescendantByClass(this,"div", "UIColorPickerIcon") ;
	eXo.core.DOMUtil.addClass(selectedIcon, "UIColorPickerSelectedIcon") ;
	UIColorPicker.list.style.display = "block" ;
	UIColorPicker.synchronize(textbox) ;
} ;

// For validating

UIColorPicker.prototype.correct = function() {
	var value = this.value ;
	this.value = eXo.calendar.UIColorPicker.setValue(value) ;
} ;

UIColorPicker.prototype.setValue = function(value) {
	var value = String(value).trim().toLowerCase() ;
	var UIColorPicker = eXo.calendar.UIColorPicker ;
	var time = UIColorPicker.digitToTime(value) ;
	var hour = Number(time.hour) ;
	var min = Number(time.minutes) ;
	if (min > 60) min = "00" ;
	else min = time.minutes ;
	var timeFormat = UIColorPicker.getTimeFormat() ;
	if (timeFormat.am) {
		var am = String(timeFormat.am).toLowerCase() ;	
		var pm = String(timeFormat.pm).toLowerCase() ;
		if (!time) {
			return UIColorPicker.defaultValue ;
		}
		if (hour > 12) {			
			hour = "12" ;
		} else if(hour == 0) {			
			hour = "12" ;
		}	else {			
			hour = time.hour ;
		}
		if (value.indexOf(am) >= 0)	min += " " + timeFormat.am ;
		else if(value.indexOf(pm) >= 0)	min += " " + timeFormat.pm ;
		else 	min += " " + timeFormat.am ;
	} else {
		if (!time) {
			return "12:00" ;
		}
		if (hour > 23) hour = "23" ;
		else hour = time.hour ;
	}
	return hour + ":" + min ;
} ;

UIColorPicker.prototype.getTimeFormat= function() {
	var items = eXo.calendar.UIColorPicker.items ;
	if (items.length <= 0) return {am:"AM", pm:"PM"} ;
	var first = eXo.core.DOMUtil.findFirstDescendantByClass(items[0], "div", "UIColorPickerLabel").innerHTML ;
	var last =  eXo.core.DOMUtil.findFirstDescendantByClass(items[items.length - 1], "div", "UIColorPickerLabel").innerHTML ;
	var am = first.match(/[A-Z]+/) ;
	var pm = last.match(/[A-Z]+/) ;
	return {am:am, pm:pm} ;
} ;

UIColorPicker.prototype.digitToTime = function(stringNo) {
	stringNo = new String(eXo.calendar.UIColorPicker.getDigit(stringNo)) ;
	var len = stringNo.length ;
	if (len <= 0) return false ;
	switch(len) {
		case 1 : 
			stringNo += "0" ;
			return {"hour": stringNo,"minutes":"00" } ;
			break ;
		case 2 :			
			return {"hour": stringNo,"minutes": "00" } ;
			break ;
		case 3 :
			return {"hour": "0" + stringNo.charAt(0),"minutes": stringNo.charAt(1) + stringNo.charAt(2) } ;
			break ;
		case 4 :
			return {"hour": stringNo.charAt(0) + stringNo.charAt(1),"minutes": stringNo.charAt(2) + stringNo.charAt(3) } ;
			break ;
		default: 
			var newString = stringNo.substring(0,3) ;
			return eXo.calendar.UIColorPicker.digitToTime(newString) ;
	}
} ;

UIColorPicker.prototype.getDigit = function(stringNo) {
	var parsedNo = "";
	for(var n=0; n<stringNo.length; n++) {
		var i = stringNo.substring(n,n+1);
		if(i=="1"||i=="2"||i=="3"||i=="4"||i=="5"||i=="6"||i=="7"||i=="8"||i=="9"||i=="0")
			parsedNo += i;
	}
	return parsedNo.toString() ;
} ;

UIColorPicker.prototype.synchronize = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
	var UIColorPicker = eXo.calendar.UIColorPicker ;
	var value = obj.value ;
	obj.value = UIColorPicker.setValue(value) ;
	var uiTabContentContainer = DOMUtil.findAncestorByClass(obj, "UITabContentContainer") ;
	if (!uiTabContentContainer) return ;
	var UIColorPickerInputs = DOMUtil.findDescendantsByClass(uiTabContentContainer, "input","UIColorPickerInput") ;
	var len = UIColorPickerInputs.length ;
	var name = obj.name.toLowerCase() ;
	var inputname = null ;
	var ifrom = null ;
	var ito = null ;
	var from = (name.indexOf("from") >=0) ;
	var to = (name.indexOf("to") >=0) ;
	for(var i = 0 ; i < len ; i ++) {
		inputname = UIColorPickerInputs[i].name.toLowerCase() ;
		ifrom = (inputname.indexOf("from") >=0) ;
		ito = (inputname.indexOf("to") >=0) ;
		if((from && ifrom) || (to && ito)) 
			UIColorPickerInputs[i].value = obj.value ;
	}
	var onfocus = obj.getAttribute("onfocus") ;
	var onclick = obj.getAttribute("onclick") ;
	if(!onfocus) obj.onfocus = UIColorPicker.show ;
	if(!onclick) obj.onclick = UIColorPicker.show ;
}
eXo.calendar.UIColorPicker = new UIColorPicker() ;