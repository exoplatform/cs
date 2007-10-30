eXo.require('eXo.webui.UIContextMenu') ;

function UICalendarPortlet() {
		
}

/* for general calendar */
UICalendarPortlet.prototype.setting = function() {
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	UICalendarPortlet.interval = (arguments.length > 0) ? parseInt(arguments[0]) : 30 ;

} ;
UICalendarPortlet.prototype.hide = function() {
	var ln = eXo.core.DOMUtil.hideElementList.length ;
	if (ln > 0) {
		for (var i = 0; i < ln; i++) {
			eXo.core.DOMUtil.hideElementList[i].style.display = "none" ;
		}
	}
} ;
UICalendarPortlet.prototype.autoHide = function(evt) {
	var _e = window.event || evt ;
	var eventType = _e.type ;	
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	if (eventType == 'mouseout') {
		UICalendarPortlet.timeout = setTimeout("eXo.calendar.UICalendarPortlet.menuElement.style.display='none'", 5000) ;		
	} else {
		if (UICalendarPortlet.timeout) clearTimeout(UICalendarPortlet.timeout) ;		
	}
} ;
UICalendarPortlet.prototype.showHide = function(obj) {
	if (obj.style.display != "block") {
		eXo.calendar.UICalendarPortlet.hide() ;
		obj.style.display = "block" ;
		obj.onmouseover = eXo.calendar.UICalendarPortlet.autoHide ;
		obj.onmouseout = eXo.calendar.UICalendarPortlet.autoHide ;
		eXo.core.DOMUtil.listHideElements(obj) ;
	} else {
		obj.style.display = "none" ;
	}
} ;
UICalendarPortlet.prototype.showMainMenu = function(obj, evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var oldmenu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu") ;
	eXo.calendar.UICalendarPortlet.swapMenu(oldmenu, obj) ;
}
UICalendarPortlet.prototype.show = function(obj, evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var DOMUtil = eXo.core.DOMUtil ;
	var uiCalendarPortlet =	document.getElementById("UICalendarPortlet") ;
	var contentContainer = DOMUtil.findFirstDescendantByClass(uiCalendarPortlet, "div", "ContentContainer") ;
	var	uiPopupCategory = DOMUtil.findNextElementByTagName(contentContainer,  "div") ;
	var newPos = {
		"x" : eXo.core.Browser.findPosX(obj) ,
		"y" : eXo.core.Browser.findPosY(obj) + obj.offsetHeight - contentContainer.scrollTop
	}
	if (DOMUtil.findAncestorByClass(obj, "CalendarItem")) uiPopupCategory = DOMUtil.findNextElementByTagName(uiPopupCategory,  "div") ;
	if (!uiPopupCategory) return ;
	var value = "" ;
	var calType = obj.getAttribute("calType") ;
	var calName = obj.getAttribute("calName") ;
	var calColor = obj.getAttribute("calColor") ;
	value = "objectId=" + obj.id ;
	if (calType) {		
		value += "&calType=" + calType ;
	}
	if (calName) {		
		value += "&calName=" + calName ;
	}
	if (calColor) {		
		value += "&calColor=" + calColor ;
	}	
	var items = DOMUtil.findDescendantsByTagName(uiPopupCategory, "a") ;
	for(var i = 0 ; i < items.length ; i ++) {
		if (DOMUtil.hasClass(items[i].firstChild, "SelectedColorCell")) {
			items[i].firstChild.className = items[i].firstChild.className.toString().replace(/SelectedColorCell/,"") ;
		}		
		if(DOMUtil.hasClass(items[i], calColor)) {				
			var selectedCell = items[i].firstChild ;
			DOMUtil.addClass(selectedCell, "SelectedColorCell") ;
		}
		if(items[i].href.indexOf("ChangeColor") != -1) {
			value = value.replace(/calColor\s*=\s*\w*/,"calColor=" + items[i].className.split(" ")[0]) ;
		}
		items[i].href = String(items[i].href).replace(/objectId\s*=.*(?='|")/, value) ;
	}	
	
	eXo.calendar.UICalendarPortlet.swapMenu(uiPopupCategory, obj, newPos) ;
	if (calType && (calType != "0") ) {
		var actions = DOMUtil.findDescendantsByTagName(eXo.calendar.UICalendarPortlet.menuElement, "a") ;
		for(var j = 0 ; j < actions.length ; j ++) {
			if (
				(actions[j].href.indexOf("EditCalendar") >= 0) ||
				(actions[j].href.indexOf("ShareCalendar") >= 0) ||
				(actions[j].href.indexOf("ChangeColorCalendar") >= 0)) {
				actions[j].style.display = "none" ;
			}
		}
	}
} ;

//UICalendarPortlet.prototype.showAction = function(obj, evt) {
//	eXo.webui.UIPopupSelectCategory.show(obj, evt) ;
//	if (this.viewer && document.all) {
//		//this.viewer.style.visibility = "hidden" ;
//		//var uiPopupCategory = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIPopupCategory") ;
//		//if (uiPopupCategory.style.display == "none") this.viewer.style.visibility = "visible" ;
//		//var board = eXo.core.DOMUtil.findFirstDescendantByClass(this.viewer, "div", "EventBoard") ;
//		//board.style.position = "static" ;
//	}
////	var uiPopupCategory = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIPopupCategory") ;
//} ;
UICalendarPortlet.prototype.setCookie = function(name,value,expiredays) {
	var exdate = new Date() ;
	exdate.setDate(exdate.getDate() + expiredays) ;
	document.cookie = name + "=" + escape(value) + ((expiredays==null) ? "" : ";expires="+exdate.toGMTString()) ;
}

UICalendarPortlet.prototype.getCookie = function(name) {
if (document.cookie.length > 0) {
	var start = document.cookie.indexOf(name + "=")
	if (start != -1) {
		start = start + name.length + 1 ;
    end = document.cookie.indexOf(";",start) ;
    if (end == -1) end = document.cookie.length ;
    	return unescape(document.cookie.substring(start,end)) ;
  } 
}
return "" ;
}
UICalendarPortlet.prototype.checkLayout = function() {
	try{
		var	layout = this.getCookie("layoutmode") ;
		var	display = this.getCookie("displaymode") ;
		var	layout0 = document.getElementById("UIMiniCalendar") ;
		var	layout1 = document.getElementById("UICalendars").parentNode ;
	}catch(e) {
		return ;
	}
	var	layout = this.getCookie("layoutmode") ;
	var	display = this.getCookie("displaymode") ;
	var	layout0 = document.getElementById("UIMiniCalendar") ;
	var	layout1 = document.getElementById("UICalendars").parentNode ;
	switch(layout) {
		case 0 :			
				layout0.style.display = display ;
				layout1.style.display = display ;
			break ;
		case 1 : 			
			layout0.style.display = display ;
			break ;
		case 2 :			
			layout1.style.display = display ;
			break ;
	}
}
UICalendarPortlet.prototype.switchLayout = function(layout) {
	var UICalendarPortlet =	eXo.calendar.UICalendarPortlet
	layout = parseInt(layout) ;
	var	layout0 = document.getElementById("UIMiniCalendar") ;
	var	layout1 = document.getElementById("UICalendars").parentNode ;
	UICalendarPortlet.setCookie("layoutmode",layout,7) ;
	switch(layout) {
		case 0 :
			if ((layout0.style.display == "none") && (layout1.style.display == "none")) {				
				layout0.style.display = "block" ;			
				layout1.style.display = "block" ;
				UICalendarPortlet.setCookie("displaymode","block",7) ;
			} else {
				layout0.style.display = "none" ;
				layout1.style.display = "none" ;
				eXo.calendar.UICalendarPortlet.setCookie("displaymode","none",7) ;
			}
			break ;
		case 1 : 
			if (layout0.style.display == "none") {
				layout0.style.display = "block" ;
				UICalendarPortlet.setCookie("displaymode","block",7) ;
			}
			else {
				layout0.style.display = "none" ;
				UICalendarPortlet.setCookie("displaymode","none",7) ;	
			}
			break ;
		case 2 :
			if (layout1.style.display == "none") {
				layout1.style.display = "block" ;
				UICalendarPortlet.setCookie("displaymode","block",7) ;	
			}
			else {				
				layout1.style.display = "none" ;
				UICalendarPortlet.setCookie("displaymode","none",7) ;	
			}
			break ;
	}
}	
/* for event */

UICalendarPortlet.prototype.init = function() {
	try{
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var uiDayViewGrid = document.getElementById("UIDayViewGrid") ;
	if (!uiDayViewGrid) return false ;
	UICalendarPortlet.viewer = eXo.core.DOMUtil.findFirstDescendantByClass(uiDayViewGrid, "div", "EventBoardContainer") ;
	UICalendarPortlet.step = 60 ;
	//UICalendarPortlet.viewer.onmousedown = eXo.calendar.UISelection.init ;
	}catch(e) {
		window.status = " !!! Error : " + e.message ;
		return false ;
	}
	return true ;
} ;

UICalendarPortlet.prototype.getElements = function(viewer) {
	var elements = eXo.core.DOMUtil.findDescendantsByClass(viewer, "div", "EventContainerBorder") ;
	var len = elements.length ;
	var elems = new Array() ;
	for(var i = 0 ; i < len ; i ++) {
		if (elements[i].style.display != "none") elems.push(elements[i]) ;
	}
	return elems ;
} ;

UICalendarPortlet.prototype.setSize = function(obj) {
	var start = parseInt(obj.getAttribute("startTime")) ;
	var end = parseInt(obj.getAttribute("endTime")) ;	
	height = Math.abs(start - end);
	var top = start ;
	obj.style.height = (height - 2) + "px" ;
	obj.style.top = top + "px" ;
	var eventContainer = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "EventContainer") ;
	eventContainer.style.height = (height - 19) + "px" ;
} ;

UICalendarPortlet.prototype.setWidth = function(element, width) {
	element.style.width = width + "%" ;
} ;

UICalendarPortlet.prototype.getInterval = function(el) {
	var bottom = new Array() ;
	var interval = new Array() ;
	if (el.length <= 0) return ;
	for(var i = 0 ; i < el.length ; i ++ ) {
		bottom.push(el[i].offsetTop + el[i].offsetHeight) ;
		if (bottom[i-1] && (el[i].offsetTop > bottom[i-1])) interval.push(i) ;
	}
	interval.unshift(0) ;
	interval.push(el.length) ;
	return interval ;
} ;

UICalendarPortlet.prototype.adjustWidth = function(el) {
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var inter = UICalendarPortlet.getInterval(el) ;
	var totalWidth = (arguments.length > 1) ? arguments[1] : 100 ;
	var offsetLeft = (arguments.length > 2) ? arguments[2] : 0 ;
	if (el.length <= 0) return ;
	for(var i = 0 ; i < inter.length ; i ++) {
		var width = "" ;
		var len = (inter[i+1] - inter[i]) ;
		if(isNaN(len)) continue ;
		var n = 0 ;
		var maxElement = el[inter[i]].offsetTop + el[inter[i]].offsetHeight ;

		for(var j = inter[i]; j < inter[i+1] ; j++) {			
			width = parseFloat(totalWidth/len) ;
			UICalendarPortlet.setWidth(el[j], width) ;
			if (el[j-1]&&(len > 1)) el[j].style.left = offsetLeft + parseFloat(el[j-1].style.width)*n +  "%" ;
			n++ ;
		}
		
	}
} ;

UICalendarPortlet.prototype.showEvent = function() {
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;	
	if (!UICalendarPortlet.init()) return ;
	
	var el = UICalendarPortlet.getElements(UICalendarPortlet.viewer) ;
	el = UICalendarPortlet.sortByAttribute(el, "startTime") ;
	if (el.length <= 0) return ;
	var marker = null ;
	for(var i = 0 ; i < el.length ; i ++ ) {		
		UICalendarPortlet.setSize(el[i]) ;
		el[i].onmousedown = UICalendarPortlet.initDND ;
		marker = eXo.core.DOMUtil.findFirstChildByClass(el[i], "div", "ResizeEventContainer") ;
		marker.onmousedown = eXo.calendar.UIResizeEvent.init ;
	}
	UICalendarPortlet.adjustWidth(el) ;
} ;

UICalendarPortlet.prototype.sortByAttribute = function(obj, attribute) {
	var len = obj.length ;
	var tmp = null ;
	var attribute1 = null ;
	var attribute2 = null ;
	for(var i = 0 ; i < len ; i ++){
		for(var j = i + 1 ; j < len ; j ++){
			attribute1 = parseInt(obj[i].getAttribute(attribute)) ;
			attribute2 = parseInt(obj[j].getAttribute(attribute)) ;
			if(attribute2 < attribute1) {
				tmp = obj[i] ;
				obj[i] = obj[j] ;
				obj[j] = tmp ;
			}
		}
	}
	return obj ;
}
/* for resizing event box */
function UIResizeEvent() {
	
}

UIResizeEvent.prototype.init = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;
	var outerElement = eXo.core.DOMUtil.findAncestorByClass(this,'EventBoxes') ;
	var innerElement = eXo.core.DOMUtil.findPreviousElementByTagName(this, "div") ;
	var container = eXo.core.DOMUtil.findAncestorByClass(outerElement, 'EventDayContainer') ;
	var minHeight = 15 ;
	var interval = eXo.calendar.UICalendarPortlet.interval ;
	UIResizeEvent.start(_e, innerElement, outerElement, container, minHeight, interval) ;
	UIResizeEvent.callback = UIResizeEvent.resizeCallback ;
} ;

UIResizeEvent.prototype.start = function(evt, innerElement, outerElement, container, minHeight, interval) {
	var _e = window.event || evt ; 
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;
	UIResizeEvent.innerElement = innerElement ;
	UIResizeEvent.outerElement = outerElement ;
	UIResizeEvent.container = container ;
	UIResizeEvent.posY = _e.clientY ;
	UIResizeEvent.minHeight = (minHeight) ? parseInt(minHeight) : 0 ;
	UIResizeEvent.interval = (interval) ? parseInt(interval) : 5 ;
	UIResizeEvent.innerElementHeight =  UIResizeEvent.innerElement.offsetHeight + 2;
	UIResizeEvent.outerElementHeight =  UIResizeEvent.outerElement.offsetHeight ;
	UIResizeEvent.container.onmousemove = UIResizeEvent.execute ;
	UIResizeEvent.container.onmouseup = UIResizeEvent.end ;
} ;

UIResizeEvent.prototype.execute = function(evt) {
	var _e = window.event || evt ;
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;
	var delta = _e.clientY - UIResizeEvent.posY ;
	var innerElementHeight = UIResizeEvent.innerElementHeight + delta ;
	var outerElementHeight = UIResizeEvent.outerElementHeight + delta ;
	if (height <= UIResizeEvent.minHeight) return ;
	if (delta%UIResizeEvent.interval == 0){
		UIResizeEvent.innerElement.style.height = innerElementHeight + "px" ;
		UIResizeEvent.outerElement.style.height = outerElementHeight + "px" ;
	}	
} ;

UIResizeEvent.prototype.end = function(evt) {
	var _e = window.event || evt ;
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;
	if (typeof(UIResizeEvent.callback) == "function") UIResizeEvent.callback() ;
	UIResizeEvent.innerElement = null ;
	UIResizeEvent.outerElement = null ;
	UIResizeEvent.posY = null ;
	UIResizeEvent.minHeight = null ;
	UIResizeEvent.interval = null ;
	UIResizeEvent.innerElementHeight =  null;
	UIResizeEvent.outerElementHeight =  null ;
	UIResizeEvent.container.onmousemove = null ;
	UIResizeEvent.container.onmouseup = null ;
	UIResizeEvent.container = null ;
} ;
UIResizeEvent.prototype.resizeCallback = function(evt) {
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;
	var eventBox = UIResizeEvent.outerElement ;
	var start =  parseInt(eventBox.getAttribute("startTime")) ;
	var calType = eventBox.getAttribute("calType") ;
	var end =  start + eventBox.offsetHeight - 2 ;
	if (eventBox.offsetHeight != UIResizeEvent.outerElementHeight) {
		var actionLink = eXo.calendar.UICalendarPortlet.adjustTime(start, end, eventBox) ;
		if(calType) actionLink = actionLink.replace(/'\s*\)/,"&calType=" + calType + "')") ;		
		eval(actionLink) ;
	}	
}
//UICalendarPortlet.prototype.initResize = function(evt) {	
//	var _e = window.event || evt ;
//	_e.cancelBubble = true ;
//	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
//	UICalendarPortlet.resizeObject = this ;
//	UICalendarPortlet.posY = _e.clientY ;
//	var eventDayContainer = eXo.core.DOMUtil.findAncestorByClass(UICalendarPortlet.resizeObject, 'EventDayContainer') ;
//	UICalendarPortlet.eventBox = eXo.core.DOMUtil.findAncestorByClass(UICalendarPortlet.resizeObject,'EventContainerBorder') ;
//	UICalendarPortlet.eventContainer = eXo.core.DOMUtil.findPreviousElementByTagName(UICalendarPortlet.resizeObject, "div") ;
//	UICalendarPortlet.posY = _e.clientY ;
//	UICalendarPortlet.beforeHeight = UICalendarPortlet.eventBox.offsetHeight ;
//	UICalendarPortlet.eventContainerHeight = UICalendarPortlet.eventContainer.offsetHeight + 2 ;
//	eventDayContainer.onmousemove = UICalendarPortlet.adjustHeight ;
//	eventDayContainer.onmouseup = UICalendarPortlet.resizeCallBack ;
//} ;
//
//UICalendarPortlet.prototype.adjustHeight = function(evt) {
//	var _e = window.event || evt ;
//	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
//	var delta = _e.clientY - UICalendarPortlet.posY ;
//	var height = UICalendarPortlet.beforeHeight + delta ;
//	var containerHeight = UICalendarPortlet.eventContainerHeight + delta ;
//	if (height <= (eXo.calendar.UICalendarPortlet.step/2)) return ;
//	if (delta%UICalendarPortlet.interval == 0){
//		UICalendarPortlet.eventBox.style.height = height + "px" ;
//		UICalendarPortlet.eventContainer.style.height = containerHeight + "px" ;
//	}	
//} ;
//
//UICalendarPortlet.prototype.resizeCallBack = function() {
//	var eventBox = eXo.calendar.UICalendarPortlet.eventBox ;
//	var start =  parseInt(eventBox.getAttribute("startTime")) ;
//	var end =  start + eventBox.offsetHeight - 2 ;
//	if (eventBox.offsetHeight != eXo.calendar.UICalendarPortlet.beforeHeight) {
//		var actionLink = eXo.calendar.UICalendarPortlet.adjustTime(start, end, eventBox) ;	
//		eval(actionLink) ;
//	}	
//} ;

/* for drag and drop */

UICalendarPortlet.prototype.initDND = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	UICalendarPortlet.dragObject = this ;
	var eventDayContainer = eXo.core.DOMUtil.findAncestorByClass(UICalendarPortlet.dragObject, "EventDayContainer") ;
		UICalendarPortlet.dragObject.style.zIndex = 1000 ;
	UICalendarPortlet.eventY = _e.clientY ;
	UICalendarPortlet.eventTop = UICalendarPortlet.dragObject.offsetTop ;
	eventDayContainer.onmousemove = UICalendarPortlet.dragStart ;
	eventDayContainer.onmouseup = UICalendarPortlet.dragEnd ;
}
UICalendarPortlet.prototype.dragStart = function(evt) {
	var _e = window.event || evt ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var delta = _e.clientY - UICalendarPortlet.eventY ;
	if (delta%UICalendarPortlet.interval == 0) {
		var top = UICalendarPortlet.eventTop + delta ;
		UICalendarPortlet.dragObject.style.top = top + "px" ;
	}
} ;
UICalendarPortlet.prototype.dragEnd = function() {
	this.onmousemove = null ;
	var dragObject = eXo.calendar.UICalendarPortlet.dragObject ;
	var calType = dragObject.getAttribute("calType") ;
	var start = parseInt(dragObject.getAttribute("startTime")) ;
	var end = parseInt(dragObject.getAttribute("endTime")) ;
	var delta = end - start  ;
	var currentStart = dragObject.offsetTop ;
	var currentEnd = currentStart + delta ;
	var eventDayContainer = eXo.core.DOMUtil.findAncestorByClass(dragObject, "EventDayContainer") ;
	eventDayContainer.onmousemove = null ;
	eventDayContainer.onmouseup = null ;
	if(dragObject.offsetTop != eXo.calendar.UICalendarPortlet.eventTop) {		
		var actionLink =	eXo.calendar.UICalendarPortlet.adjustTime(currentStart, currentEnd, dragObject) ;
		if (calType) actionLink = actionLink.replace(/'\s*\)/, "&calType=" + calType + "')") ;
		eval(actionLink) ;
	}
	dragObject = null ;
} ;

/* for adjusting time */

UICalendarPortlet.prototype.adjustTime = function(currentStart, currentEnd, obj) {
	var actionLink = obj.getAttribute("actionLink") ;	
	var pattern = /startTime.*endTime/g ;
	var params = "startTime=" + currentStart + "&finishTime=" + currentEnd ;
	actionLink = actionLink.replace(pattern, params).replace("javascript:","") ;
	return actionLink ;
	//eval(actionLink) ;
} ;

/* for showing context menu */

UICalendarPortlet.prototype.showContextMenu = function() {	
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var config = {
		'preventDefault':false, 
		'preventForms':false
	} ;	
	UIContextMenu.init(config) ;
	UIContextMenu.attach("CalendarContentNomal","UIMonthViewRightMenu") ;
	UIContextMenu.attach("EventOnDayContent","UIMonthViewEventRightMenu") ;
	UIContextMenu.attach("TimeRule","UIDayViewRightMenu") ;
	UIContextMenu.attach("EventBoxes","UIDayViewEventRightMenu") ;
	UIContextMenu.attach(["EventWeekContent","EventAlldayContainer"],"UIWeekViewRightMenu") ;
	//UIContextMenu.attach(["EventContainerBoder","EventContainerBar","EventContainer","ResizeEventContainer"],"UIWeekViewEventRightMenu") ;
} ;

UICalendarPortlet.prototype.dayViewCallback = function(evt){
	var _e = window.event || evt ;
	var src = _e.srcElement || _e.target ;
	var startTime = "" ;
	var map = null ;
	if (src.nodeName == "TD") {		
		src = eXo.core.DOMUtil.findAncestorByTagName(src, "tr") ;
		startTime = src.getAttribute("startTime") ;
		map = {
			"startTime\s*=\s*.*(?=&|'|\")":"startTime="+startTime
		} ;
	}
	else{		
		src = eXo.core.DOMUtil.findAncestorByClass(src, "EventBoxes") ;
		var eventId = src.getAttribute("eventid") ;
		var calendarId = src.getAttribute("calid") ;
		var calType = src.getAttribute("calType") ;
		map = {
			"objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"objectId="+eventId ,
			"calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calendarId="+calendarId
		} ;
		if (calType) {
			map = {
			"objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"objectId=" + eventId ,
			"calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calendarId=" + calendarId,
			"calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calType=" + calType
		} ;
		}
	}
	eXo.webui.UIContextMenu.changeAction(eXo.webui.UIContextMenu.menuElement, map) ;
}
UICalendarPortlet.prototype.weekViewCallback = function(evt) {
	var _e = window.event || evt ;
	var src = _e.srcElement || _e.target ;
	var DOMUtil = eXo.core.DOMUtil ;
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var items = DOMUtil.findDescendantsByTagName(UIContextMenu.menuElement,"a") ;
	if (DOMUtil.hasClass(src,"EventContainerBoder") || DOMUtil.hasClass(src,"EventContainerBar") || DOMUtil.hasClass(src,"EventContainer") || DOMUtil.hasClass(src,"ResizeEventContainer")) {
		var obj = (DOMUtil.findAncestorByClass(src, "EventContainerBoder"))? DOMUtil.findAncestorByClass(src, "EventContainerBoder") : src ;
		var eventId = obj.getAttribute("eventId") ;
		var calendarId = obj.getAttribute("calId") ;
		map = {
			"objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"objectId="+eventId,
			"calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calendarId="+calendarId
		} ;
		obj = DOMUtil.findAncestorByTagName(src, "td").getAttribute("startTime") ;		
		for(var i = 0 ; i < items.length ; i ++ ) {
			if (items[i].style.display == "none") {
				items[i].style.display = "block" ;
				items[i].href = eXo.webui.UIContextMenu.replaceall(String(items[i].href),map) ;
			} else {				
				items[i].href = String(items[i].href).replace(/startTime\s*=\s*.*(?=&|'|\")/,"startTime="+obj) ;
			}
		}		
	} else {
		var obj = (DOMUtil.findAncestorByTagName(src, "td"))? DOMUtil.findAncestorByTagName(src, "td") : src ;
		var map = obj.getAttribute("startTime") ;
		for(var i = 0 ; i < items.length ; i ++ ) {
			if (items[i].style.display == "block") {
				items[i].style.display = "none" ;
			} else {				
				items[i].href = String(items[i].href).replace(/startTime\s*=\s*.*(?=&|'|\")/,"startTime="+map) ;
			}
			
		}
	}
}
UICalendarPortlet.prototype.monthViewCallback = function(evt){
	var _e = window.event || evt ;
	var src = _e.srcElement || _e.target ;
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var DOMUtil = eXo.core.DOMUtil ;
	var objectValue = "" ;
	var links = eXo.core.DOMUtil.findDescendantsByTagName(UIContextMenu.menuElement, "a") ;
	if (!DOMUtil.findAncestorByClass(src, "EventBoxes")) {
		if (objectValue = DOMUtil.findAncestorByTagName(src,"td").getAttribute("currentDate")){
			UIContextMenu.changeAction(UIContextMenu.menuElement,objectValue) ;
		}
	} else if (objvalue = DOMUtil.findAncestorByClass(src, "EventBoxes")) {
		var eventId = objvalue.getAttribute("eventId") ;
		var calendarId = objvalue.getAttribute("calId") ;
		var map = {
			"objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"objectId="+eventId,
			"calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calendarId="+calendarId
		} ;
		UIContextMenu.changeAction(UIContextMenu.menuElement, map) ;
	} else {
		return ;
	}	
}

UICalendarPortlet.prototype.initFilter = function(obj, type){
	if (type == 1) {
		var checkbox = eXo.core.DOMUtil.findFirstChildByClass(obj, "input", "checkbox") ;
		eXo.calendar.UICalendarPortlet.filterByCalendar(checkbox.name,checkbox.checked) ;
	} else if (type == 2) {
		
	} else {
		return ;
	}
} ;

UICalendarPortlet.prototype.filterByGroup = function(obj) { 
	var DOMUtil = eXo.core.DOMUtil ;
	var uiVtab = DOMUtil.findAncestorByClass(obj, "CalendarSelectedFlatStyle") ;
	var uiVTabContent = DOMUtil.findNextElementByTagName(uiVtab, "div") ;
	var checkboxes = uiVTabContent.getElementsByTagName("input") ;
	var checked = obj.checked ;
	var len = checkboxes.length ;
	for(var i = 0 ; i < len ; i ++) {
		checkboxes[i].checked = checked ;
		eXo.calendar.UICalendarPortlet.filterByCalendar(checkboxes[i].name, checked) ;
	}
} ;

UICalendarPortlet.prototype.checkSpaceAvailable = function() {
	var uiMonthViewGrid = document.getElementById("UIMonthViewGrid") ;
	if (!uiMonthViewGrid) return ;
	var DOMUtil = eXo.core.DOMUtil ;
	var tds = DOMUtil.findDescendantsByTagName(uiMonthViewGrid, "td") ;
	var eventBoxes = null ;
	for(var i = 0 ; i < tds.length ; i ++) {
		eventBoxes = DOMUtil.findDescendantsByClass(tds[i], "div", "EventBoxes") ;
		if (!eventBoxes || (eventBoxes.length <= 3)) continue ;
		var n = 0 ;
		var height = 0 ;
		var len = eventBoxes.length ;
		for(var j = 0 ; j < len ; j ++) {
			if (eventBoxes[j].style.display != "none") {
				n++ ;
				height += eventBoxes[j].offsetHeight ;
				if (height >= 50) break ;
			}	
		}
		var newMore = len - n ;
		var more = tds[i].getElementsByTagName("a")[0] ;
		if ((n < 3) && (n > 0)) {
			more.innerHTML = String(more.innerHTML).replace(/\+\s+\d*/,"+ " + newMore) ;
			more.style.display = "block" ;
		} else if (n == 0){
			more.style.display = "none" ;
		} else {
			more.innerHTML = String(more.innerHTML).replace(/\+\s+\d*/,"+ " + newMore) ;
			more.style.display = "block" ;
		}
		eventBoxes = null ;
	}
} ;

UICalendarPortlet.prototype.filterByCalendar = function(calendarId, status) {
	var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer") ;
	if (!uiCalendarViewContainer) return ;
	var className = "EventBoxes" ;
	var events = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className) ;
	if (!events) return ;
	var len = events.length ;
	for(var i = 0 ; i < len ; i ++){
		if (events[i].getAttribute("calid") == calendarId) {
			if (status) 					
				events[i].style.display = "block" ;			
			else 
				events[i].style.display = "none" ;				
		}
	}
	if (document.getElementById("UIMonthViewGrid")) eXo.calendar.UICalendarPortlet.checkSpaceAvailable() ;
	if (document.getElementById("UIDayViewGrid")) eXo.calendar.UICalendarPortlet.showEvent() ;

} ;
UICalendarPortlet.prototype.initFilterByCategory = function(obj) {
	var selectbox = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "select", "selectbox") ;
	selectbox.onchange = eXo.calendar.UICalendarPortlet.filterByCategory ;
} ;
UICalendarPortlet.prototype.filterByCategory = function() {
	var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer") ;
	if (!uiCalendarViewContainer) return ;
	var category = this.options[this.selectedIndex].value ;
	var className = "EventBoxes" ;
	var events = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className) ;
	if (!events) return ;
	var len = events.length ;
	for(var i = 0 ; i < len ; i ++){
		if (category == events[i].getAttribute("eventCat")) {
			events[i].style.display = "block" ;
		}
		else if (category == "") {
			events[i].style.display = "block" ;
		}
		else events[i].style.display = "none" ;
	}
	eXo.calendar.UICalendarPortlet.checkSpaceAvailable() ;	
} ;
UICalendarPortlet.prototype.showView = function(obj, evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;	
	var oldmenu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu") ;	
	eXo.calendar.UICalendarPortlet.swapMenu(oldmenu, obj) ;
}
UICalendarPortlet.prototype.swapMenu = function(oldmenu, clickobj) {
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var Browser = eXo.core.Browser ;
	var menuX = Browser.findPosX(clickobj) ;
	var menuY = Browser.findPosY(clickobj) + clickobj.offsetHeight ;
	if (arguments.length > 2) { // Customize position of menu with an object that have 2 properties x, y 
		menuX = arguments[2].x ;
		menuY = arguments[2].y ;
	}	
	if(document.getElementById("tmpMenuElement")) document.getElementById("UIPortalApplication").removeChild(document.getElementById("tmpMenuElement")) ;
	var tmpMenuElement = oldmenu.cloneNode(true) ;
	tmpMenuElement.setAttribute("id","tmpMenuElement") ;
	UICalendarPortlet.menuElement = tmpMenuElement ;
	document.getElementById("UIPortalApplication").appendChild(tmpMenuElement) ;
	
	
	UICalendarPortlet.menuElement.style.top = menuY + "px" ;
	UICalendarPortlet.menuElement.style.left = menuX + "px" ;	
	UICalendarPortlet.showHide(UICalendarPortlet.menuElement) ;
	//UICalendarPortlet.menuElement = null ;
}

/* for selection creation */

function UISelection() {
	
} ;

UISelection.prototype.init = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var UISelection = eXo.calendar.UISelection ;
	var Container = this ;
	UISelection.relativeObject = eXo.core.DOMUtil.findAncestorByClass(Container, "EventDayContainer") ;
	var selection = document.getElementById("selection") ;
	if (selection) selection.parentNode.removeChild(selection) ;
	UISelection.selection = document.createElement("div") ;
	UISelection.selection.className = "selection" ;
	UISelection.selection.setAttribute("id", "selection") ;
	UISelection.selectionY = eXo.core.Browser.findMouseRelativeY(UISelection.relativeObject, _e) + UISelection.relativeObject.scrollTop;
	UISelection.selection.innerHTML = "<span></span>" ;
	Container.appendChild(UISelection.selection) ;
	Container.onmousemove = UISelection.resize ;
	Container.onmouseup = UISelection.clear ;
} ;

UISelection.prototype.resize = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;	
	var UISelection = eXo.calendar.UISelection ;
	var delta = UISelection.selectionY - eXo.core.Browser.findMouseRelativeY(UISelection.relativeObject, _e) ;
	if (delta < 0) {
		UISelection.selection.style.top = UISelection.selectionY - UISelection.relativeObject.scrollTop + "px" ;
		UISelection.selection.style.height = Math.abs(delta) + "px" ;
	} else {
		UISelection.selection.style.bottom = UISelection.selectionY + "px" ;
		UISelection.selection.style.top = eXo.core.Browser.findMouseRelativeY(UISelection.relativeObject, _e) - UISelection.relativeObject.scrollTop + "px" ;
		UISelection.selection.style.height = Math.abs(delta) + "px" ;
	}
	window.status = UISelection.selection.offsetTop ;
} ;
UISelection.prototype.clear = function() {	
	this.onmousemove = null ;
} ;

eXo.calendar.UICalendarPortlet = new UICalendarPortlet() ;
eXo.calendar.UIResizeEvent = new UIResizeEvent() ;
eXo.calendar.UISelection = new UISelection() ;