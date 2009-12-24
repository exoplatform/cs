function UICombobox() {
}

UICombobox.prototype.init = function(textbox) {
	if(typeof(textbox) == "string") textbox = document.getElementById(textbox) ;
	var UICombobox = eXo.mail.UICombobox ;
	var onfocus = textbox.getAttribute("onfocus") ;
	var onclick = textbox.getAttribute("onclick") ;
	var onblur = textbox.getAttribute("onblur") ;
	if(!onfocus) textbox.onfocus = UICombobox.show ;
	if(!onclick) textbox.onclick = UICombobox.show ;
	if(!onblur)  textbox.onblur = UICombobox.correct ;
} ;

UICombobox.prototype.show = function(evt) {
	var UICombobox = eXo.mail.UICombobox ;	
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	UICombobox.defaultValue = this.value ;
	var src = _e.target || _e.srcElement ;
	if(UICombobox.list) UICombobox.list.style.display = "none" ;
	UICombobox.list = eXo.core.DOMUtil.findPreviousElementByTagName(src, "div") ;
	UICombobox.items = eXo.core.DOMUtil.findDescendantsByTagName(UICombobox.list, "a") ;		
	var len = UICombobox.items.length ;
	
	for(var i = 0 ; i < len ; i ++ ) {
		UICombobox.items[i].onclick = UICombobox.getValue ; 
	}
	if (len <= 0) return ;
	UICombobox.list.onmousedown = UICombobox.cancelBubbe ;
	UICombobox.list.style.width = (this.offsetWidth - 2) + "px" ;	
	UICombobox.list.style.overflowX = "hidden" ;
	UICombobox.list.style.display = "block" ;
	var top = eXo.core.Browser.findPosYInContainer(this, UICombobox.list.offsetParent) + this.offsetHeight ;
	var left = eXo.core.Browser.findPosXInContainer(this, UICombobox.list.offsetParent) ;
	UICombobox.list.style.top = top + "px" ;	
	UICombobox.list.style.left = left + "px" ;
  UICombobox.fixForIE6() ;
	document.onmousedown = eXo.mail.UICombobox.hide ;
} ;
UICombobox.prototype.fixForIE6 = function() {
  if(!eXo.core.Browser.isIE6()) return ;
  if(eXo.core.DOMUtil.getChildrenByTagName(eXo.mail.UICombobox.list,"iframe").length > 0) return ;
	var iframe = document.createElement("iframe") ;
  iframe.frameBorder = 0 ;
  iframe.style.position = "absolute" ;
  iframe.style.top = "0px" ;
  iframe.style.left = "0px" ;  
  iframe.style.width = "100%" ;
  iframe.style.zIndex = -1 ;
  iframe.style.height = eXo.mail.UICombobox.list.firstChild.offsetHeight + "px" ;
  eXo.mail.UICombobox.list.appendChild(iframe) ;
} ;
UICombobox.prototype.cancelBubbe = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
} ;

UICombobox.prototype.hide = function() {
	eXo.mail.UICombobox.list.style.display = "none" ;
} ;

UICombobox.prototype.getValue = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var UICombobox = eXo.mail.UICombobox ;
	var val = this.getAttribute("value") ;
	var textbox = eXo.core.DOMUtil.findNextElementByTagName(UICombobox.list,"input") ;
	textbox.value = val ;
	var len = UICombobox.items.length ;
	var icon = null ;
	var selectedIcon = null ;
	for(var i = 0 ; i < len ; i ++ ) {
		icon = eXo.core.DOMUtil.findFirstDescendantByClass(UICombobox.items[i],"div", "UIComboboxIcon") ;
		icon.className = "UIComboboxIcon" ;
	}
	selectedIcon = eXo.core.DOMUtil.findFirstDescendantByClass(this,"div", "UIComboboxIcon") ;
	eXo.core.DOMUtil.addClass(selectedIcon, "UIComboboxSelectedIcon") ;
	UICombobox.list.style.display = "none" ;
	UICombobox.synchronize(textbox) ;
} ;

// For validating

UICombobox.prototype.correct = function() {
	var value = this.value ;
	this.value = eXo.mail.UICombobox.setValue(value) ;
} ;

UICombobox.prototype.setValue = function(value) {
	var value = String(value).trim().toLowerCase() ;
	var UICombobox = eXo.mail.UICombobox ;
	var time = UICombobox.digitToTime(value) ;
	var hour = Number(time.hour) ;
	var min = Number(time.minutes) ;
	if (min > 60) min = "00" ;
	else min = time.minutes ;
	var timeFormat = UICombobox.getTimeFormat() ;
	if (timeFormat.am) {
		var am = String(timeFormat.am).toLowerCase() ;	
		var pm = String(timeFormat.pm).toLowerCase() ;
		if (!time) {
			return UICombobox.defaultValue ;
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

UICombobox.prototype.getTimeFormat= function() {
	var items = eXo.mail.UICombobox.items ;
	if (items.length <= 0) return {am:"AM", pm:"PM"} ;
	var first = eXo.core.DOMUtil.findFirstDescendantByClass(items[0], "div", "UIComboboxLabel").innerHTML ;
	var last =  eXo.core.DOMUtil.findFirstDescendantByClass(items[items.length - 1], "div", "UIComboboxLabel").innerHTML ;
	var am = first.match(/[A-Z]+/) ;
	var pm = last.match(/[A-Z]+/) ;
	return {am:am, pm:pm} ;
} ;

UICombobox.prototype.digitToTime = function(stringNo) {
	stringNo = new String(eXo.mail.UICombobox.getDigit(stringNo)) ;
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
			return eXo.mail.UICombobox.digitToTime(newString) ;
	}
} ;

UICombobox.prototype.getDigit = function(stringNo) {
	var parsedNo = "";
	for(var n=0; n<stringNo.length; n++) {
		var i = stringNo.substring(n,n+1);
		if(i=="1"||i=="2"||i=="3"||i=="4"||i=="5"||i=="6"||i=="7"||i=="8"||i=="9"||i=="0")
			parsedNo += i;
	}
	return parsedNo.toString() ;
} ;

UICombobox.prototype.synchronize = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
	var UICombobox = eXo.mail.UICombobox ;
	var value = obj.value ;
	obj.value = UICombobox.setValue(value) ;
	var uiTabContentContainer = DOMUtil.findAncestorByClass(obj, "UITabContentContainer") ;
	if (!uiTabContentContainer) return ;
	var UIComboboxInputs = DOMUtil.findDescendantsByClass(uiTabContentContainer, "input","UIComboboxInput") ;
	var len = UIComboboxInputs.length ;
	var name = obj.name.toLowerCase() ;
	var inputname = null ;
	var ifrom = null ;
	var ito = null ;
	var from = (name.indexOf("from") >=0) ;
	var to = (name.indexOf("to") >=0) ;
	for(var i = 0 ; i < len ; i ++) {
		inputname = UIComboboxInputs[i].name.toLowerCase() ;
		ifrom = (inputname.indexOf("from") >=0) ;
		ito = (inputname.indexOf("to") >=0) ;
		if((from && ifrom) || (to && ito)) 
			UIComboboxInputs[i].value = obj.value ;
	}
	var onfocus = obj.getAttribute("onfocus") ;
	var onclick = obj.getAttribute("onclick") ;
	if(!onfocus) obj.onfocus = UICombobox.show ;
	if(!onclick) obj.onclick = UICombobox.show ;
}
eXo.mail.UICombobox = new UICombobox() ;