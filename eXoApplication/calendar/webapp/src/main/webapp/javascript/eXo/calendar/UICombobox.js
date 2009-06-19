function UICombobox() {
}

UICombobox.prototype.init = function(contId,id) {
	var cont = document.getElementById(contId);
	var textbox = eXo.core.DOMUtil.findDescendantById(cont,id) ;
	var UICombobox = eXo.calendar.UICombobox ;
	var onfocus = textbox.getAttribute("onfocus") ;
	var onclick = textbox.getAttribute("onclick") ;
	var onblur = textbox.getAttribute("onblur") ;
	if(!onfocus) textbox.onfocus = UICombobox.show ;
	if(!onclick) textbox.onclick = UICombobox.show ;
	if(!onblur)  textbox.onblur = UICombobox.correct ;
} ;

UICombobox.prototype.show = function(evt) {
	var uiCombo = eXo.calendar.UICombobox;
	uiCombo.items = eXo.core.DOMUtil.findDescendantsByClass(this.parentNode,"div","UIComboboxItem");
	if(uiCombo.list) uiCombo.list.style.display = "none";
	uiCombo.list = eXo.core.DOMUtil.findFirstDescendantByClass(this.parentNode,"div","UIComboboxContainer");
	uiCombo.list.parentNode.style.position = "absolute";
	uiCombo.fixForIE6(this);
	uiCombo.list.style.display = "block";	
	uiCombo.list.style.top = this.offsetHeight + "px";
	uiCombo.list.style.width = this.offsetWidth + "px";
	uiCombo.setSelectedItem(this);
	uiCombo.list.onmousedown = eXo.core.EventManager.cancelEvent;
	document.onmousedown = uiCombo.hide;
} ;

UICombobox.prototype.getSelectedItem = function(textbox){
	var val = textbox.value;
	var data = eval(textbox.getAttribute("options"));
	var len = data.length;
	for(var i = 0; i<len; i++) {
		if(val == data[i]) return i;
	}
	return false;
};

UICombobox.prototype.setSelectedItem = function(textbox){
	if(this.lastSelectedItem) eXo.core.DOMUtil.replaceClass(this.lastSelectedItem,"UIComboboxSelectedItem","");
	var selectedIndex = this.getSelectedItem(textbox);
	if(selectedIndex) {
		eXo.core.DOMUtil.addClass(this.items[selectedIndex],"UIComboboxSelectedItem");
		this.lastSelectedItem = this.items[selectedIndex];
		var y = eXo.core.Browser.findPosYInContainer(this.lastSelectedItem,this.list);
		this.list.firstChild.scrollTop = y ; 
	}
};

UICombobox.prototype.fixForIE6 = function(obj) {
  if(!eXo.core.Browser.isIE6()) return ;
  if(eXo.core.DOMUtil.getChildrenByTagName(this.list,"iframe").length > 0) return ;
	var iframe = document.createElement("iframe") ;
  iframe.frameBorder = 0 ;
	iframe.style.width = obj.offsetWidth+ "px" ;
  this.list.appendChild(iframe) ;
} ;

UICombobox.prototype.cancelBubbe = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
} ;

UICombobox.prototype.complete = function(obj,evt) {
	if(evt.keyCode == 16 ) return;
	if(evt.keyCode == 13 ) {
		obj.blur();
		this.correct(evt);
		this.hide();
		return;
	}
	var sVal = obj.value.toLowerCase();
	if(evt.keyCode == 8 )	sVal = sVal.substring( 0, sVal.length - 1 )
	if( sVal.length < 1 ) return ;
	var data = eval(obj.getAttribute("options").trim());
	var len = data.length;
	var tmp = null;
	for( var i = 0; i < data.length; i++ )	{
		tmp = data[i].trim();
		var idx = tmp.toLowerCase().indexOf( sVal, 0);
		if( idx == 0 && tmp.length > sVal.length )	{
			obj.value = data[i];
			if( obj.createTextRange )	{
				hRange = obj.createTextRange();
				hRange.findText( data[i].substr( sVal.length ) );
				hRange.select();
			}
			else	{
				obj.setSelectionRange( sVal.length, tmp.length );
			}
			break;
		}
	}
	this.setSelectedItem(obj);
} ;

UICombobox.prototype.hide = function() {
	eXo.calendar.UICombobox.list.style.display = "none" ;
	document.onmousedown = null ;
} ;

UICombobox.prototype.getValue = function(obj) {
	var UICombobox = eXo.calendar.UICombobox ;
	var val = obj.getAttribute("value") ;
	var textbox = eXo.core.DOMUtil.findNextElementByTagName(UICombobox.list.parentNode,"input") ;
	textbox.value = val ;
	this.setSelectedItem(textbox);
	UICombobox.list.style.display = "none" ;
	UICombobox.synchronize(textbox) ;
} ;

// For validating

UICombobox.prototype.correct = function() {
	var value = this.value ;
	this.value = eXo.calendar.UICombobox.setValue(value) ;
} ;

UICombobox.prototype.setValue = function(value) {
	var value = String(value).trim().toLowerCase() ;
	var UICombobox = eXo.calendar.UICombobox ;
	var time = UICombobox.digitToTime(value) ;
	var hour = Number(time.hour) ;
	var min = Number(time.minutes) ;
	if (min > 59) min = "00" ;
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
	var items = eXo.calendar.UICombobox.items ;
	if (items.length <= 0) return {am:"AM", pm:"PM"} ;
	var first = eXo.core.DOMUtil.findFirstDescendantByClass(items[0], "div", "UIComboboxLabel").innerHTML ;
	var last =  eXo.core.DOMUtil.findFirstDescendantByClass(items[items.length - 1], "div", "UIComboboxLabel").innerHTML ;
	var am = first.match(/[A-Z]+/) ;
	var pm = last.match(/[A-Z]+/) ;
	return {am:am, pm:pm} ;
} ;

UICombobox.prototype.digitToTime = function(stringNo) {
	stringNo = new String(eXo.calendar.UICombobox.getDigit(stringNo)) ;
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
			return eXo.calendar.UICombobox.digitToTime(newString) ;
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
	var UICombobox = eXo.calendar.UICombobox ;
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
eXo.calendar.UICombobox = new UICombobox() ;