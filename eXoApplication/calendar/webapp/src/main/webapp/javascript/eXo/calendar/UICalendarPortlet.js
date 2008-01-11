eXo.require('eXo.cs.UIContextMenu','/csResources/javascript/') ;
function UICalendarPortlet() {
}

/* utility method */

UICalendarPortlet.prototype.clearSessionTimeout = function() {
	var str = "top.location.reload() ;" ;	
	setTimeout(str, 600000) ;
} ;

UICalendarPortlet.prototype.setStyle = function(object,styles) {	
	for(var value in styles) {
		object.style[value] = styles[value] ;
	}
} ;

UICalendarPortlet.prototype.timeToMin = function(milliseconds) {
	if (typeof(milliseconds) == "string") milliseconds = parseInt(milliseconds) ;
	var d = new Date(milliseconds) ;
	var hour = d.getHours();
	var min = d.getMinutes();
  var min = hour*60 + min ;
	return min ;
}	;

UICalendarPortlet.prototype.minToTime = function(min,timeFormat) {
	var minutes = min%60 ;
	var hour = (min - minutes)/ 60 ;
	if(this.timeFormat != "hh:mm a") return hour + ":" + minutes ;
	if (hour < 10) hour = "0" + hour ;
	if (minutes < 10) minutes = "0" + minutes ;
	var time = hour + ":" + minutes ;
	if(!timeFormat) return time ;
	if(hour < 12) time += " " + timeFormat.am ;
	else time += " " + timeFormat.pm ;
 return time ;
} ;

UICalendarPortlet.prototype.getBeginDay = function(millis) {
	var d = new Date(millis) ;
	var date = d.getDate() ;
	var month = d.getMonth() + 1 ;
	var year = d.getFullYear() ;
	var strDate = month + "/" + date + "/" + year + " 00:00:00 AM" ;
	return Date.parse(strDate) ;
} ;

UICalendarPortlet.prototype.dateDiff = function(start,end) {
	var start = this.getBeginDay(start) ;
	var end = this.getBeginDay(end) ;
	var msDiff = end - start ;
	var dateDiff = msDiff/(24*60*60*1000) ;
	return dateDiff ;
} ;

UICalendarPortlet.prototype.toSettingTime = function(time, settingTimeZone, severTimeZone) {
	var GMT = time - (3600000*serverTimeZone) ;
	var settingTime = GMT + (3600000*settingTimeZone) ;
	return settingTime ;
}

UICalendarPortlet.prototype.getYear = function(date) {
	x = date.getYear();
	var y = x % 100;
	y += (y < 38) ? 2000 : 1900;
	return y;
}

UICalendarPortlet.prototype.getDay = function(milliseconds) {
	var d = new Date(milliseconds) ;
	var day = d.getDay() ;
	return day ;
} ;

UICalendarPortlet.prototype.isBeginDate = function(milliseconds) {
	var d = new Date(milliseconds) ;
	var hour = d.getHours() ;
	var min = d.getMinutes() ;
	if( (hour == 0) && (hour == min)) return true ;
	return false ;
} ;

UICalendarPortlet.prototype.isBeginWeek = function(milliseconds) {
	var d = new Date(milliseconds) ;
	var day = d.getDay() ;
	var hour = d.getHours() ;
	var min = d.getMinutes() ;
	if((day == 0) && (hour == 0) && (min == 0)) return true ;
	return false ;
} ;

UICalendarPortlet.prototype.getWeekNumber = function(now) {
	var today = new Date(now) ;
	var Year = this.getYear(today) ;
	var Month = today.getMonth() ;
	var Day = today.getDate() ;
	var now = Date.UTC(Year,Month,Day+1,0,0,0);
	var Firstday = new Date() ;
	Firstday.setYear(Year) ;
	Firstday.setMonth(0) ;
	Firstday.setDate(1) ;
	var then = Date.UTC(Year,0,1,0,0,0) ;
	var Compensation = Firstday.getDay() ;
	if (Compensation > 3) Compensation -= 4;
	else Compensation += 3 ;
	var NumberOfWeek =  Math.round((((now-then)/86400000) + Compensation)/7) ;
	return NumberOfWeek ;
} ;

/* common method */

UICalendarPortlet.prototype.setting = function() {
	// paras 1: time interval, paras 2: working time, paras 3: time format type, paras 4: portletid
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	this.interval = ((arguments.length > 0) && (isNaN(parseInt(arguments[0])) == false )) ? parseInt(arguments[0]) : parseInt(15) ;
	var workingStart =  ((arguments.length > 1) && (isNaN(parseInt(arguments[1])) == false ) && (arguments[1] != "null")) ? arguments[1] : "" ;
	workingStart = Date.parse("1/1/2007 " + workingStart) ;
	this.workingStart = UICalendarPortlet.timeToMin(workingStart) ;
	this.timeFormat = (arguments.length > 2) ? (new String(arguments[2])).trim() : null ;
	this.portletName = arguments[3] ;
} ;

UICalendarPortlet.prototype.setFocus = function(obj, events, container) {
	events = this.getBlockElements(events) ;
	var len = events.length ;
	var scrollTop = (events[0]) ? events[0].offsetTop : 0 ;	
	var lastUpdatedId = obj.getAttribute("lastUpdatedId") ;
	if(lastUpdatedId && (lastUpdatedId != "null")) {
		for(var i = 0 ; i < len ; i ++) {
			if (events[i].getAttribute("eventId") == lastUpdatedId) {
				scrollTop = events[i].offsetTop ;
				break ;
			}
		}		
	} else {
		scrollTop = (this.workingStart)? (this.workingStart + 15) : scrollTop ;
	}
	container.scrollTop = scrollTop - 15 ;
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
} ;

UICalendarPortlet.prototype.show = function(obj, evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var DOMUtil = eXo.core.DOMUtil ;
	var uiCalendarPortlet =	document.getElementById(this.portletName) ;
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

UICalendarPortlet.prototype.checkLayout = function() {
	try{
		var Browser = eXo.core.Browser ;
		var	display = Browser.getCookie("displaymode") ;
		var	display0 = Browser.getCookie("displaymode0") ;
		var	display1 = Browser.getCookie("displaymode1") ;
		var	layout0 = document.getElementById("UIMiniCalendar") ;
		var	layout1 = document.getElementById("UICalendars").parentNode ;
		var	layout3 = document.getElementById("UICalendarContainer") ;
		var workingarea = eXo.core.DOMUtil.findNextElementByTagName(layout3, "div") ;
	}catch(e) {
		return ;
	}
	layout3.style.display = display ;
	if (display == "none") workingarea.style.marginLeft = "0px"	;
	layout0.style.display = display0 ;
	layout1.style.display = display1 ;
} ;

UICalendarPortlet.prototype.switchLayout = function(layout) {	
	var Browser = eXo.core.Browser ;
	layout = parseInt(layout) ;
	var	layout0 = document.getElementById("UIMiniCalendar") ;
	var	layout1 = document.getElementById("UICalendars").parentNode ;
	var	layout3 = document.getElementById("UICalendarContainer") ;
	var workingarea = eXo.core.DOMUtil.findNextElementByTagName(layout3, "div") ;
		
	switch(layout) {
		case 0 :
			if (layout3.style.display == "none") {
				layout0.style.display = "block" ;				
				layout1.style.display = "block" ;				
				layout3.style.display = "block" ;												
				workingarea.style.marginLeft = "243px"	;
				Browser.setCookie("displaymode","block",7) ;
				Browser.setCookie("displaymode0","block",7) ;
				Browser.setCookie("displaymode1","block",7) ;
			} else {
				layout0.style.display = "none" ;
				layout1.style.display = "none" ;
				layout3.style.display = "none" ;
				workingarea.style.marginLeft = "0px"	;
				Browser.setCookie("displaymode","none",7) ;
				Browser.setCookie("displaymode0","none",7) ;
				Browser.setCookie("displaymode1","none",7) ;
			}
			break ;
		case 1 :
			if (layout0.style.display == "none") {
				layout0.style.display = "block" ;
				layout3.style.display = "block" ;
				workingarea.style.marginLeft = "243px"	;			
				Browser.setCookie("displaymode","block",7) ;
				Browser.setCookie("displaymode0","block",7) ;
			}
			else {
				layout0.style.display = "none" ;
				if(layout1.style.display == "none") {
					Browser.setCookie("displaymode","none",7) ;
					workingarea.style.marginLeft = "0px"	;
					layout3.style.display = "none" ;
				}
				Browser.setCookie("displaymode0","none",7) ;	
			}
			break ;
		case 2 :
			if (layout1.style.display == "none") {
				layout1.style.display = "block" ;
				layout3.style.display = "block" ;
				workingarea.style.marginLeft = "243px"	;
				Browser.setCookie("displaymode","block",7) ;
				Browser.setCookie("displaymode1","block",7) ;

			}
			else {				
				layout1.style.display = "none" ;
				if(layout0.style.display == "none") {
					Browser.setCookie("displaymode","none",7) ;
					workingarea.style.marginLeft = "0px"	;
					layout3.style.display = "none" ;
				}
				Browser.setCookie("displaymode1","none",7) ;	
			}
			break ;
	}
}	;
/* for event */

UICalendarPortlet.prototype.init = function() {
	try{
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var uiDayViewGrid = document.getElementById("UIDayViewGrid") ;
	if (!uiDayViewGrid) return false ;
	UICalendarPortlet.viewer = eXo.core.DOMUtil.findFirstDescendantByClass(uiDayViewGrid, "div", "EventBoardContainer") ;
	UICalendarPortlet.step = 60 ;
	}catch(e) {
		window.status = " !!! Error : " + e.message ;
		return false ;
	}
	return true ;
} ;

UICalendarPortlet.prototype.getElements = function(viewer) {
	var className = (arguments.length > 1)? arguments[1] : "EventContainerBorder" ;
	var elements = eXo.core.DOMUtil.findDescendantsByClass(viewer, "div", className) ;
	var len = elements.length ;
	var elems = new Array() ;
	for(var i = 0 ; i < len ; i ++) {
		if (elements[i].style.display != "none") {
			elements[i].style.left = "0%" ;
			elements[i].style.zIndex = 1 ;
			elems.push(elements[i]) ;
		}
	}
	return elems ;
} ;
UICalendarPortlet.prototype.isShow = function(obj) {
	if(obj.style.display != "none") return true ;
	return false ;
}
UICalendarPortlet.prototype.getBlockElements = function(elements) {
	var el = new Array() ;
	var len = elements.length ;
	for(var i = 0 ; i < len ; i ++) {
		if(this.isShow(elements[i])) el.push(elements[i]);
	}
	return el ;
}

UICalendarPortlet.prototype.setSize = function(obj) {
	var start = parseInt(obj.getAttribute("startTime")) ;
	var end = parseInt(obj.getAttribute("endTime")) ;
	var eventContainer = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "EventContainer") ;
	if (end == 0) end = 1440 ;
	end = (end !=0) ? end : 1440 ;
	height = Math.abs(start - end) ;
	if(height < 30) height = 30 ;
	var styles = {
		"top" : start + "px",
		"height" : (height - 2) + "px"
	} ;
	eXo.calendar.UICalendarPortlet.setStyle(obj, styles) ;
	eventContainer.style.height = (height - 22) + "px" ;
} ;

UICalendarPortlet.prototype.setWidth = function(element, width) {
	element.style.width = width + "%" ;
} ;

UICalendarPortlet.prototype.getInterval = function(el) {
	var bottom = new Array() ;
	var interval = new Array() ;
	if (!el || (el.length <= 0)) return ;
	for(var i = 0 ; i < el.length ; i ++ ) {
		bottom.push(el[i].offsetTop + el[i].offsetHeight) ;
		if (bottom[i-1] && (el[i].offsetTop > bottom[i-1])) {
			interval.push(i) ;
		}
	}
	
	interval.unshift(0) ;
	interval.push(el.length) ;
	return interval ;
} ;

UICalendarPortlet.prototype.adjustWidth = function(el,totalWidth) {
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var inter = UICalendarPortlet.getInterval(el) ;
	if (el.length <= 0) return ;
	var width = "" ;
	for(var i = 0 ; i < inter.length ; i ++) {
		//var totalWidth = (arguments.length > 1) ? arguments[1] : parseFloat(this.viewer.offsetWidth) ;
		var offsetLeft = parseFloat(0) ;
		//var left = parseFloat(0) ;
		if(arguments.length > 2) {
			offsetLeft = parseFloat(arguments[2]) ;
			//left = arguments[2] ;
		} 
		var len = (inter[i+1] - inter[i]) ;
		if(isNaN(len)) continue ;
		var mark = null ;
		if (i > 0){
			for(var l = 0 ; l < inter[i] ; l ++) {
				if((el[inter[i]].offsetTop > el[l].offsetTop) && (el[inter[i]].offsetTop < (el[l].offsetTop + el[l].offsetHeight))) {
					mark = l ;					
				}
			}			
			if (mark != null) {
				offsetLeft = parseFloat(el[mark].style.left) + parseFloat(el[mark].style.width) ;
			}
		}
		var n = 0 ;
		for(var j = inter[i]; j < inter[i+1] ; j++) {
			if(mark != null) {				
				width = parseFloat((totalWidth + offsetLeft - parseFloat(el[mark].style.left) - parseFloat(el[mark].style.width))/len) ;
			} else {
				width = parseFloat(totalWidth/len) ;
			}
			el[j].style.width = width + "px" ;
			if (el[j-1]&&(len > 1)) el[j].style.left = offsetLeft + parseFloat(el[j-1].style.width)*n +  "px" ;
			else {
				el[j].style.left = offsetLeft +  "px" ;
			}
			n++ ;
		}
	}	
//	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
//	var inter = UICalendarPortlet.getInterval(el) ;
//	if (el.length <= 0) return ;
//	var width = "" ;
//	for(var i = 0 ; i < inter.length ; i ++) {
//		var totalWidth = (arguments.length > 1) ? arguments[1] : parseFloat(100) ;
//		var offsetLeft = parseFloat(0) ;
//		var left = parseFloat(0) ;
//		if(arguments.length > 2) {
//			offsetLeft = parseFloat(arguments[2]) ;
//			left = arguments[2] ;
//		} 
//		var len = (inter[i+1] - inter[i]) ;
//		if(isNaN(len)) continue ;
//		var mark = null ;
//		if (i > 0){
//			for(var l = 0 ; l < inter[i] ; l ++) {
//				if((el[inter[i]].offsetTop > el[l].offsetTop) && (el[inter[i]].offsetTop < (el[l].offsetTop + el[l].offsetHeight))) {
//					mark = l ;					
//				}
//			}			
//			if (mark != null) {
//				offsetLeft = parseFloat(el[mark].style.left) + parseFloat(el[mark].style.width) ;
//			}
//		}
//		var n = 0 ;
//		for(var j = inter[i]; j < inter[i+1] ; j++) {
//			if(mark != null) {				
//				width = parseFloat((totalWidth + left - parseFloat(el[mark].style.left) - parseFloat(el[mark].style.width))/len) ;
//			} else {
//				width = parseFloat(totalWidth/len) ;
//			}
//			UICalendarPortlet.setWidth(el[j], width) ;
//			if (el[j-1]&&(len > 1)) el[j].style.left = offsetLeft + parseFloat(el[j-1].style.width)*n +  "%" ;
//			else {
//				el[j].style.left = offsetLeft +  "%" ;
//			}
//			n++ ;
//		}
//	}
} ;

UICalendarPortlet.prototype.showEvent = function() {
	this.init() ;
	var EventDayContainer = eXo.core.DOMUtil.findAncestorByClass(this.viewer,"EventDayContainer") ;
	EventDayContainer.scrollTop = (this.workingStart) ? this.workingStart : 0 ;
	if (!this.init()) return ;
	var el = this.getElements(this.viewer) ;
	el = this.sortByAttribute(el, "startTime") ;
	if (el.length <= 0) return ;
	var marker = null ;
	for(var i = 0 ; i < el.length ; i ++ ) {		
		this.setSize(el[i]) ;
		el[i].onmousedown = eXo.calendar.UICalendarPortlet.initDND ;
		marker = eXo.core.DOMUtil.findFirstChildByClass(el[i], "div", "ResizeEventContainer") ;
		marker.onmousedown = eXo.calendar.UIResizeEvent.init ;
	}
	this.items = el ;
	this.adjustWidth(this.items, this.viewer.offsetWidth) ;
	this.setFocus(this.viewer, el, EventDayContainer) ;
} ;

UICalendarPortlet.prototype.browserResizeCallback = function() {
	if (!eXo.calendar.UICalendarPortlet.items) return ;
	eXo.calendar.UICalendarPortlet.adjustWidth(eXo.calendar.UICalendarPortlet.items) ;
}

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
} ;
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
	var minHeight = 30 ;
	var interval = eXo.calendar.UICalendarPortlet.interval ;
	UIResizeEvent.start(_e, innerElement, outerElement, container, minHeight, interval) ;
	UIResizeEvent.callback = UIResizeEvent.resizeCallback ;
} ;

UIResizeEvent.prototype.start = function(evt, innerElement, outerElement, container, minHeight, interval) {
	var _e = window.event || evt ; 
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;
	this.innerElement = innerElement ;
	this.outerElement = outerElement ;
	this.container = container ;
	eXo.calendar.UICalendarPortlet.resetZIndex(this.outerElement) ;
	this.minHeight = (minHeight) ? parseInt(minHeight) : 15 ;
	this.interval = (interval != "undefined") ? parseInt(interval) : 15 ;
	this.container.onmousemove = UIResizeEvent.execute ;
	this.container.onmouseup = UIResizeEvent.end ;	
	this.beforeHeight =  this.outerElement.offsetHeight ;	
	//this.posY = _e.clientY ;
} ;

UIResizeEvent.prototype.execute = function(evt) {
	var _e = window.event || evt ;
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;	
	var height = UIResizeEvent.outerElement.offsetHeight ;
	var mouseY = eXo.core.Browser.findMouseRelativeY(UIResizeEvent.container,_e) + UIResizeEvent.container.scrollTop ;
	var posY = UIResizeEvent.outerElement.offsetTop ;
	var delta = posY + height - mouseY ;
	if (height <= UIResizeEvent.minHeight) {
		if (mouseY >= (posY + height)) {
			UIResizeEvent.outerElement.style.height = UIResizeEvent.minHeight + "px" ;
			UIResizeEvent.innerElement.style.height = (UIResizeEvent.minHeight - 15) + "px" ;		
			window.status = UIResizeEvent.minHeight ;
		}
	} else {		
		if (delta >= UIResizeEvent.interval) {
			UIResizeEvent.outerElement.style.height = parseInt(UIResizeEvent.outerElement.style.height) - UIResizeEvent.interval + "px" ;
			UIResizeEvent.innerElement.style.height = parseInt(UIResizeEvent.innerElement.style.height) - UIResizeEvent.interval + "px" ;
		}
		if (mouseY >= (posY + height)) {
			UIResizeEvent.outerElement.style.height = parseInt(UIResizeEvent.outerElement.style.height) + UIResizeEvent.interval + "px" ;
			UIResizeEvent.innerElement.style.height = parseInt(UIResizeEvent.innerElement.style.height) + UIResizeEvent.interval + "px" ;		
		}
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
	var end =  start + eventBox.offsetHeight ;
	if (eventBox.offsetHeight != UIResizeEvent.beforeHeight) {
		var actionLink = eXo.calendar.UICalendarPortlet.adjustTime(start, end, eventBox) ;
		if(calType) actionLink = actionLink.replace(/'\s*\)/,"&calType=" + calType + "')") ;
		eval(actionLink) ;
	}	
} ;

/* for drag and drop */

UICalendarPortlet.prototype.resetZIndex = function(obj) {
	try{		
	var maxZIndex = parseInt(obj.style.zIndex) ;
	var items = eXo.core.DOMUtil.getChildrenByTagName(obj.parentNode, "div") ;
	var len = items.length ;
	for(var i = 0 ; i < len ; i ++) {
		if (!items[i].style.zIndex) items[i].style.zIndex = 1 ;
		if(parseInt(items[i].style.zIndex) > maxZIndex) {
			maxZIndex = parseInt(items[i].style.zIndex) ;
		}
	}
	obj.style.zIndex = maxZIndex + 1 ;
	} catch(e) {
		//alert(e.message) ;
	}
} ;

UICalendarPortlet.prototype.initDND = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	UICalendarPortlet.dragObject = this ;
	UICalendarPortlet.resetZIndex(UICalendarPortlet.dragObject) ;
	UICalendarPortlet.dragContainer = eXo.core.DOMUtil.findAncestorByClass(UICalendarPortlet.dragObject, "EventDayContainer") ;
	UICalendarPortlet.resetZIndex(UICalendarPortlet.dragObject) ;
	UICalendarPortlet.eventY = _e.clientY ;
	UICalendarPortlet.eventTop = UICalendarPortlet.dragObject.offsetTop ;
	UICalendarPortlet.dragContainer.onmousemove = UICalendarPortlet.dragStart ;
	UICalendarPortlet.dragContainer.onmouseup = UICalendarPortlet.dragEnd ;
} ;
UICalendarPortlet.prototype.dragStart = function(evt) {
	var _e = window.event || evt ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var delta = null ;
	var mouseY = eXo.core.Browser.findMouseRelativeY(UICalendarPortlet.dragContainer,_e) + UICalendarPortlet.dragContainer.scrollTop ;
	var posY = UICalendarPortlet.dragObject.offsetTop ;
	var height =  UICalendarPortlet.dragObject.offsetHeight ;
	if ( mouseY <= posY) {
		UICalendarPortlet.dragObject.style.top = parseInt(UICalendarPortlet.dragObject.style.top) - UICalendarPortlet.interval + "px" ;
	} else if(mouseY >= (posY + height)) {
		UICalendarPortlet.dragObject.style.top = parseInt(UICalendarPortlet.dragObject.style.top) + UICalendarPortlet.interval + "px" ;
	} else {
		delta = _e.clientY - UICalendarPortlet.eventY ;
		if (delta%UICalendarPortlet.interval == 0) {
			var top = UICalendarPortlet.eventTop + delta ;		
			UICalendarPortlet.dragObject.style.top = top + "px" ;
		}
	}	
} ;

UICalendarPortlet.prototype.dragEnd = function() {
	this.onmousemove = null ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var dragObject = UICalendarPortlet.dragObject ;
	var calType = dragObject.getAttribute("calType") ;
	var start = parseInt(dragObject.getAttribute("startTime")) ;
	var end = parseInt(dragObject.getAttribute("endTime")) ;
	if (end == 0) end = 1440 ;
	var delta = end - start  ;
//	var currentStart = (UICalendarPortlet.workingStart) ? dragObject.offsetTop + UICalendarPortlet.workingStart : dragObject.offsetTop ;
	var currentStart = dragObject.offsetTop ;
	var currentEnd = currentStart + delta ;
	var eventDayContainer = eXo.core.DOMUtil.findAncestorByClass(dragObject, "EventDayContainer") ;
	eventDayContainer.onmousemove = null ;
	eventDayContainer.onmouseup = null ;
	if(dragObject.offsetTop != UICalendarPortlet.eventTop) {		
		var actionLink =	UICalendarPortlet.adjustTime(currentStart, currentEnd, dragObject) ;
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
} ;

/* for showing context menu */

UICalendarPortlet.prototype.showContextMenu = function(compid) {	
	var UIContextMenu = eXo.webui.UIContextMenu ;
	this.portletName = compid ;
	UIContextMenu.portletName = this.portletName ;
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
	UIContextMenu.attach("UIListViewRow","UIListViewEventRightMenu") ;
} ;

UICalendarPortlet.prototype.listViewCallack = function(evt){
	var _e = window.event || evt ;
	var src = _e.srcElement || _e.target ;
	if(!eXo.core.DOMUtil.hasClass(src, "UIListViewRow")) src = eXo.core.DOMUtil.findAncestorByClass(src, "UIListViewRow") ;
	var eventId = src.getAttribute("eventid") ;
	var calendarId = src.getAttribute("calid") ;
	var calType = src.getAttribute("calType") ;
	map = {
		"objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"objectId=" + eventId ,
		"calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calendarId=" + calendarId,
		"calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calType=" + calType
	} ;	
	eXo.webui.UIContextMenu.changeAction(eXo.webui.UIContextMenu.menuElement, map) ;
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
} ;
;
UICalendarPortlet.prototype.weekViewCallback = function(evt) {
	var _e = window.event || evt ;
	var src = _e.srcElement || _e.target ;
	var DOMUtil = eXo.core.DOMUtil ;
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var map = null ;
	var obj = null ;
	var items = DOMUtil.findDescendantsByTagName(UIContextMenu.menuElement,"a") ;
	if (DOMUtil.findAncestorByClass(src, "WeekViewEventBoxes") || DOMUtil.hasClass(src, "WeekViewEventBoxes")) {
		var obj = (DOMUtil.findAncestorByClass(src, "WeekViewEventBoxes"))? DOMUtil.findAncestorByClass(src, "WeekViewEventBoxes") : src ;
		var eventId = obj.getAttribute("eventid") ;
		var calendarId = obj.getAttribute("calid") ;
		var calType = obj.getAttribute("calType") ;
		map = {
			"objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"objectId="+eventId ,
			"calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calendarId="+calendarId
		} ;
		if (calType) {
			map = {
				"objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"objectId=" + eventId ,
				"calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calendarId=" + calendarId ,
				"calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calType=" + calType
			} ;
		}
		obj = DOMUtil.findAncestorByTagName(src, "td").getAttribute("startTime") ;		
		for(var i = 0 ; i < items.length ; i ++ ) {
			if (items[i].className == "EventActionMenu") {
				items[i].style.display = "block" ;
				items[i].href = UIContextMenu.replaceall(String(items[i].href),map) ;
			} else {				
				items[i].href = String(items[i].href).replace(/startTime\s*=\s*.*(?=&|'|\")/,"startTime="+obj) ;
			}
		}		
	} else {
		obj = (DOMUtil.findAncestorByTagName(src, "td"))? DOMUtil.findAncestorByTagName(src, "td") : src ;
		map = obj.getAttribute("startTime") ;
		for(var i = 0 ; i < items.length ; i ++ ) {
			if (items[i].style.display == "block") {
				items[i].style.display = "none" ;
			} else {				
				items[i].href = String(items[i].href).replace(/startTime\s*=\s*.*(?=&|'|\")/,"startTime="+map) ;
			}
			
		}
	}
} ;

UICalendarPortlet.prototype.monthViewCallback = function(evt){
	var _e = window.event || evt ;
	var src = _e.srcElement || _e.target ;
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var DOMUtil = eXo.core.DOMUtil ;
	var objectValue = "" ;
	var links = eXo.core.DOMUtil.findDescendantsByTagName(UIContextMenu.menuElement, "a") ;
	if (!DOMUtil.findAncestorByClass(src, "EventBoxes")) {
		if (objectValue = DOMUtil.findAncestorByTagName(src,"td").getAttribute("startTime")){
			var map = {
			   "startTime\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"startTime=" + objectValue
			} ;
   		UIContextMenu.changeAction(UIContextMenu.menuElement,map) ;
		}
	} else if (objvalue = DOMUtil.findAncestorByClass(src, "DayContentContainer")) {
		var eventId = objvalue.getAttribute("eventId") ;
		var calendarId = objvalue.getAttribute("calId") ;
		var calType = objvalue.getAttribute("calType") ;
		var map = {
			"objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"objectId="+eventId,
			"calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calendarId="+calendarId,
			"calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calType=" + calType
		} ;
		UIContextMenu.changeAction(UIContextMenu.menuElement, map) ;
	} else {
		return ;
	}	
} ;
/* BOF filter */
UICalendarPortlet.prototype.getEventsByCalendar = function(events, calid) {
	var calendarid = null ;
	var len = events.length ;
	var event = new Array() ;
	for(var i = 0; i < len ; i ++) {
		calendarid = events[i].getAttribute("calid") ;
		if (calendarid == calid) event.push(events[i]) ;
	}
	return event ;
} ;

UICalendarPortlet.prototype.getEventsForFilter = function(events) {
	var form = this.filterForm ;
	var checkbox = eXo.core.DOMUtil.findDescendantsByClass(form, "input", "checkbox") ;
	var el = new Array() ;
	var len = checkbox.length ;
	var calid = null ;
	for(var i = 0; i < len ; i ++) {
		if(checkbox[i].checked) {
			calid = checkbox[i].name ;
			el.pushAll(this.getEventsByCalendar(events,calid)) ;
		}		
	}
	return el ;
} ;

UICalendarPortlet.prototype.filterByGroup = function() { 
	var DOMUtil = eXo.core.DOMUtil ;
	var uiVtab = DOMUtil.findAncestorByClass(this, "UIVTab") ;
	var checkboxes = DOMUtil.findDescendantsByClass(uiVtab, "input","checkbox") ;	
	var checked = this.checked ;
	var len = checkboxes.length ;
	for(var i = 0 ; i < len ; i ++) {
		eXo.calendar.UICalendarPortlet.runFilterByCalendar(checkboxes[i].name, checked) ;
		if (checkboxes[i].checked == checked) continue ;
		checkboxes[i].checked = checked ;
	}
} ;

UICalendarPortlet.prototype.runFilterByCalendar = function(calid, checked) {
	var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer") ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	if (!uiCalendarViewContainer) return ;
	var className = "EventBoxes" ;
	if (document.getElementById("UIWeekViewGrid")) className = "WeekViewEventBoxes" ; // TODO : review event box gettting
	var events = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className) ;
	if (!events) return ;
	var len = events.length ;
	for(var i = 0 ; i < len ; i ++){
		if (events[i].getAttribute("calId") == calid) {
			if (checked) {
				events[i].style.display = "block" ;
			}
			else {
				events[i].style.display = "none" ;
			}
		}
	}
	try {	//TODO: review order javascript file loading
		if (document.getElementById("UIDayViewGrid")) UICalendarPortlet.showEvent() ;
		if (document.getElementById("UIWeekViewGrid")) eXo.calendar.UIWeekView.init() ;
	}
	catch(e) {} ;

} ;

UICalendarPortlet.prototype.filterByCalendar = function() {
	var calid = this.name ;
	var checked = this.checked ;
	var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer") ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	if (!uiCalendarViewContainer) return ;
	var className = "EventBoxes" ;
	if (document.getElementById("UIWeekViewGrid")) className = "WeekViewEventBoxes" ; // TODO : review event box gettting
	var events = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className) ;
	if (!events) return ;
	var len = events.length ;
	for(var i = 0 ; i < len ; i ++){
		if (events[i].getAttribute("calId") == calid) {
			if (checked) {
				events[i].style.display = "block" ;
			}
			else {
				events[i].style.display = "none" ;
			}
		}
	}
	try {	//TODO: review order javascript file loading
		if (document.getElementById("UIDayViewGrid")) UICalendarPortlet.showEvent() ;
		if (document.getElementById("UIWeekViewGrid")) eXo.calendar.UIWeekView.init() ;
	}
	catch(e) {} ;

} ;

//UICalendarPortlet.prototype.initFilterByCategory = function(obj) {
//	var selectbox = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "select", "selectbox") ;
//	var onchange = selectbox.getAttribute("onchange") ;
//	if (!onchange) selectbox.onchange = new Function("eXo.calendar.UICalendarPortlet.filterByCategory(this)") ;
//} ;

UICalendarPortlet.prototype.filterByCategory = function() {
	var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer") ;
	if (!uiCalendarViewContainer) return ;
	var category = this.options[this.selectedIndex].value ;
	var className = "EventBoxes" ;
	if (document.getElementById("UIWeekViewGrid")) className = "WeekViewEventBoxes" ; // TODO : review event box gettting
	var allEvents = eXo.core.DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", className) ;
	var events = eXo.calendar.UICalendarPortlet.getEventsForFilter(allEvents) ;
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
	if (document.getElementById("UIDayViewGrid")) eXo.calendar.UICalendarPortlet.showEvent() ;
	if (document.getElementById("UIWeekViewGrid")) eXo.calendar.UIWeekView.init() ;
} ;

UICalendarPortlet.prototype.getFilterForm = function(form) {
	if(typeof(form) == "string") form = document.getElementById(form) ;
	this.filterForm = form ;
	var CalendarGroup = eXo.core.DOMUtil.findDescendantsByClass(form, "input","CalendarGroup") ;	
	var CalendarItem = eXo.core.DOMUtil.findDescendantsByClass(form, "input","checkbox") ;
	var uvTab = null ;
	var len = CalendarGroup.length ;
	var clen = CalendarItem.length ;
	for(var i = 0 ; i < len ; i ++) {
		CalendarGroup[i].onclick = eXo.calendar.UICalendarPortlet.filterByGroup ;
	}
	for(var j = 0 ; j < clen ; j ++) {
		CalendarItem[j].onclick = eXo.calendar.UICalendarPortlet.filterByCalendar ;
	}
} ;

UICalendarPortlet.prototype.getFilterSelect = function(form) {
	if(typeof(form) == "string") form = document.getElementById(form) ;
	var eventCategory = eXo.core.DOMUtil.findFirstDescendantByClass(form, "div", "EventCategory") ;
	select = eXo.core.DOMUtil.findDescendantsByTagName(eventCategory, "select")[0] ;
	var onchange = select.getAttribute("onchange") ;
	if (!onchange) select.onchange = eXo.calendar.UICalendarPortlet.filterByCategory ;
	this.filterSelect = select ;
} ;

UICalendarPortlet.prototype.checkFilter = function() {
	this.checkCalendarFilter() ;
} ;

UICalendarPortlet.prototype.checkCalendarFilter = function() {
	if(!this.filterForm) return ;
	var checkbox = eXo.core.DOMUtil.findDescendantsByClass(this.filterForm, "input", "checkbox") ;
	var len = checkbox.length ;
	for(var i = 0 ; i < len ; i ++) {		
		this.runFilterByCalendar(checkbox[i].name, checkbox[i].checked) ;
	}
} ;

UICalendarPortlet.prototype.checkCategoryFilter = function() {
	if(this.filterSelect) eXo.calendar.UICalendarPortlet.filterByCategory(this.filterSelect) ;	
} ;

/* EOF filter */
UICalendarPortlet.prototype.showView = function(obj, evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;	
	var oldmenu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu") ;	
	eXo.calendar.UICalendarPortlet.swapMenu(oldmenu, obj) ;
} ;

UICalendarPortlet.prototype.swapMenu = function(oldmenu, clickobj) {
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var Browser = eXo.core.Browser ;
	var menuX = Browser.findPosX(clickobj) ;
	var menuY = Browser.findPosY(clickobj) + clickobj.offsetHeight ;
	if (arguments.length > 2) { // Customize position of menu with an object that have 2 properties x, y 
		menuX = arguments[2].x ;
		menuY = arguments[2].y ;
	}
	if(document.getElementById("tmpMenuElement")) document.getElementById(UICalendarPortlet.portletName).removeChild(document.getElementById("tmpMenuElement")) ;
	var tmpMenuElement = oldmenu.cloneNode(true) ;
	tmpMenuElement.setAttribute("id","tmpMenuElement") ;
	UICalendarPortlet.menuElement = tmpMenuElement ;
	document.getElementById(UICalendarPortlet.portletName).appendChild(tmpMenuElement) ;
	
	
	UICalendarPortlet.menuElement.style.top = menuY + "px" ;
	UICalendarPortlet.menuElement.style.left = menuX + "px" ;	
	UICalendarPortlet.showHide(UICalendarPortlet.menuElement) ;
	//UICalendarPortlet.menuElement = null ;
} ;
UICalendarPortlet.prototype.isAllday = function(form) {
	try{
		if (typeof(form) == "string") form = document.getElementById(form) ;		
		if (form.tagName.toLowerCase() != "form") {
			form = eXo.core.DOMUtil.findDescendantsByTagName(form, "form") ;
		}
		for(var i = 0 ; i < form.elements.length ; i ++) {
			if(form.elements[i].getAttribute("name") == "allDay") {
				eXo.calendar.UICalendarPortlet.showHideTime(form.elements[i]) ;
				break ;
			}
		}
	}catch(e){
		
	}
} ;
UICalendarPortlet.prototype.showHideTime = function(chk) {
	var DOMUtil = eXo.core.DOMUtil ;
	if(chk.tagName.toLowerCase() != "input") {
		chk = DOMUtil.findFirstDescendantByClass(chk, "input", "checkbox") ;
	}
	var selectboxes = DOMUtil.findDescendantsByTagName(chk.form, "input") ;
	var fields = new Array() ;
	var len = selectboxes.length ;
	for(var i = 0 ; i < len ; i ++) {
		if((selectboxes[i].getAttribute("name") == "toTime") || (selectboxes[i].getAttribute("name") == "fromTime")) {
			fields.push(selectboxes[i]) ;
		}
	}
	eXo.calendar.UICalendarPortlet.showHideField(chk, fields) ;
} ;
UICalendarPortlet.prototype.showHideField = function(chk,fields) {
	var display = "" ;
	if (typeof(chk) == "string") chk = document.getElementById(chk) ;
	display = (chk.checked) ? "hidden" : "visible" ;
	var len = fields.length ;
	for(var i = 0 ; i < len ; i ++) {
		fields[i].style.visibility = display ;i
	}
} ;

UICalendarPortlet.prototype.initSelection = function() {	
	var UISelection = eXo.calendar.UISelection ;
	var container = eXo.core.DOMUtil.findFirstDescendantByClass(document.getElementById("UIDayViewGrid"), "div", "EventBoard") ;
	UISelection.step = 30 ;
	UISelection.container = container ;
	UISelection.block = document.createElement("div") ;
	UISelection.block.className = "UserSelectionBlock" ;
	UISelection.container.appendChild(UISelection.block) ;
	UISelection.container.onmousedown = UISelection.start ;
	UISelection.relativeObject = eXo.core.DOMUtil.findAncestorByClass(UISelection.container, "EventDayContainer") ;
	UISelection.viewType = "UIDayView" ;
} ;

/* for selection creation */

function UISelection() {
	
} ;

UISelection.prototype.start = function(evt) {
	try{
		var UISelection = eXo.calendar.UISelection ;
		var _e = window.event || evt ;
		var src = _e.srcElement || _e.target ;
		if((src == UISelection.block) || (_e.button == 2) ) {
			return ;
		}
		
		UISelection.startTime = src.getAttribute("startTime") ;
		UISelection.startX = eXo.core.Browser.findPosXInContainer(src, UISelection.container) ;		
		UISelection.block.style.display = "block" ;
		UISelection.startY = eXo.core.Browser.findPosYInContainer(src, UISelection.container) ;
		UISelection.block.style.width = src.offsetWidth  + "px" ;
		UISelection.block.style.left = UISelection.startX  + "px" ;
		UISelection.block.style.top = UISelection.startY  + "px" ;
		UISelection.block.style.height = UISelection.step + "px" ;

		eXo.calendar.UICalendarPortlet.resetZIndex(UISelection.block) ;
		document.onmousemove = UISelection.execute ;
		document.onmouseup = UISelection.clear ;		
	} catch(e) {
		//alert(e.message) ;
	}
} ;

UISelection.prototype.execute = function(evt) {
	var UISelection = eXo.calendar.UISelection ;
	var _e = window.event || evt ;
	var delta = null ;
	var mouseY = eXo.core.Browser.findMouseRelativeY(UISelection.container,_e) + UISelection.relativeObject.scrollTop ;
	var posY = UISelection.block.offsetTop ;
	var height =  UISelection.block.offsetHeight ;
	delta = posY + height - mouseY ;
	if((UISelection.startY - mouseY) < 0) {	
		UISelection.block.style.top = UISelection.startY + "px" ;	
		if (delta >= UISelection.step) {
			UISelection.block.style.height = parseInt(UISelection.block.style.height) - UISelection.step + "px" ;
		}
		if (mouseY >= (posY + height)) {		
			UISelection.block.style.height = parseInt(UISelection.block.style.height) + UISelection.step + "px" ;
		}
	} else {
		delta = mouseY - posY ;
		UISelection.block.style.bottom = UISelection.startY - UISelection.step  + "px" ;
		if (mouseY <= posY) {
			UISelection.block.style.top = parseInt(UISelection.block.style.top) - UISelection.step + "px" ;	
			UISelection.block.style.height = parseInt(UISelection.block.style.height) + UISelection.step + "px" ;		
		}
		if(delta >= UISelection.step) {
			UISelection.block.style.top = parseInt(UISelection.block.style.top) + UISelection.step + "px" ;	
			UISelection.block.style.height = parseInt(UISelection.block.style.height) - UISelection.step + "px" ;					
		}
	}

} ;

UISelection.prototype.clear = function() {
	var UISelection = eXo.calendar.UISelection ;
	var endTime = UISelection.block.offsetHeight*60*1000 + parseInt(UISelection.startTime) ;
	var startTime = UISelection.startTime ;
	if(UISelection.block.offsetTop < UISelection.startY) {
		startTime = parseInt(UISelection.startTime) - UISelection.block.offsetHeight*60*1000 +  UISelection.step*60*1000 ;
		endTime = parseInt(UISelection.startTime) + UISelection.step*60*1000 ;
	}
	eXo.webui.UIForm.submitEvent(UISelection.viewType ,'QuickAdd','&objectId=Event&startTime=' + startTime + '&finishTime=' + endTime) ;		
	eXo.core.DOMUtil.listHideElements(UISelection.block) ;
	document.onmousemove = null ;
	document.onmouseup = null ;
} ;

// check free/busy time
UICalendarPortlet.prototype.initCheck = function(container) {
	var DOMUtil = eXo.core.DOMUtil ;
	if(typeof(container) == "string") container = document.getElementById(container) ;
	var table = DOMUtil.findFirstDescendantByClass(container, "table", "UIGrid") ;
	var tr = DOMUtil.findDescendantsByTagName(table, "tr") ;	
	var firstTr = tr[1] ;
	this.busyCell = DOMUtil.findDescendantsByTagName(firstTr, "td").slice(1) ;
	var len = tr.length ;
	for(var i = 2 ; i < len ; i ++) {
		this.showBusyTime(tr[i]) ;
	}
	eXo.calendar.UICalendarPortlet.initSelectionX(firstTr) ;
} ;

UICalendarPortlet.prototype.parseTime = function(string) {
	var stringTime = string.split(",") ;
	var len = stringTime.length ;
	var time = new Array() ;
	var tmp = null ;
	for(var i = 0 ; i < len ; i += 2) {
		tmp = {"from": this.timeToMin(stringTime[i]),"to":this.timeToMin(stringTime[i+1])} ;
		time.push(tmp) ;
	}
	return time ;
} ;

UICalendarPortlet.prototype.showBusyTime = function(tr) {
	var stringTime = tr.getAttribute("busytime") ;
	if (!stringTime) return ;
	var time = this.parseTime(stringTime) ;
	var len = time.length ;
	var from = null ;
	var to = null ;
	for(var i = 0 ; i < len ; i ++) {
		from = parseInt(time[i].from) ;
		to = parseInt(time[i].to) ;
		this.setBusyTime(from, to, tr)
	}
} ;

UICalendarPortlet.prototype.setBusyTime = function(from, to, tr) {
	var cell = eXo.core.DOMUtil.findDescendantsByTagName(tr, "td").slice(1) ;
	var start = this.round(from,15)/15 ;
	var end = this.round(to,15)/15 ;
	for(var i = start ; i < end ; i ++) {
		cell[i].className = "BusyDotTime" ;
		this.busyCell[i].className = "BusyTime" ;		
	}
} ;

UICalendarPortlet.prototype.round = function(number, dividend) {	
	return	number = number - (number%dividend) ;	
} ;

UICalendarPortlet.prototype.initSelectionX = function(tr) {	
	cell = eXo.core.DOMUtil.findDescendantsByTagName(tr, "td", "UICellBlock").slice(1);
	var len = cell.length ;
	for(var i = 0 ; i < len ; i ++) {
		cell[i].onmousedown = eXo.calendar.Highlighter.start ;
	}
} ;

UICalendarPortlet.prototype.getTimeFormat = function(input) {
	var list = eXo.core.DOMUtil.findPreviousElementByTagName(input, "div") ;
	var a = eXo.core.DOMUtil.findDescendantsByTagName(list, "a") ;
	var am = a[0].getAttribute("value").match(/[A-Z]+/) ;
	if(!am) return null ;
	var pm = a[a.length - 1].getAttribute("value").match(/[A-Z]+/) ;
	return {
		"am": am,
		"pm": pm
	} ;
} ;

UICalendarPortlet.prototype.callbackSelectionX = function() {
	var Highlighter = eXo.calendar.Highlighter ;
	var DOMUtil = eXo.core.DOMUtil ;
	var len = Math.abs(Highlighter.firstCell.cellIndex - Highlighter.lastCell.cellIndex - 1) ;
	var start = (Highlighter.firstCell.cellIndex - 1)*15 ;
	var end = start + len*15 ;
	var uiTabContentContainer = DOMUtil.findAncestorByClass(Highlighter.startCell, "UITabContentContainer") ;
	var UIComboboxInputs = DOMUtil.findDescendantsByClass(uiTabContentContainer, "input","UIComboboxInput") ;
	var len = UIComboboxInputs.length ;
	var name = null ;
	var timeFormat = this.getTimeFormat(UIComboboxInputs[0]) ;
	start = this.minToTime(start, timeFormat) ;
	end = this.minToTime(end, timeFormat) ;
	for(var i = 0 ; i < len ; i ++) {
		name = UIComboboxInputs[i].name.toLowerCase() ;
		if (name.indexOf("from") >= 0) UIComboboxInputs[i].value = start ;
		else  UIComboboxInputs[i].value = end ;
	}
	DOMUtil.addClass(Highlighter.block[1], "BlueBlock") ;
	Highlighter.block[1].style.width = parseFloat(Highlighter.block[1].offsetWidth/Highlighter.container.offsetWidth)*100  + "%" ;
	Highlighter.block[1].style.left = parseFloat(Highlighter.block[1].offsetLeft/Highlighter.container.offsetWidth)*100  + "%" ;
} ;

UICalendarPortlet.prototype.initSettingTab = function(cpid) {
	var cp = document.getElementById(cpid) ;
	var ck = eXo.core.DOMUtil.findFirstDescendantByClass(cp, "input", "checkbox") ;
	var div = eXo.core.DOMUtil.findAncestorByTagName(ck, "div") ;
	eXo.calendar.UICalendarPortlet.workingSetting = eXo.core.DOMUtil.findNextElementByTagName(div, "div") ;
	ck.onclick = eXo.calendar.UICalendarPortlet.showHideWorkingSetting ;
	eXo.calendar.UICalendarPortlet.checkWorkingSetting(ck) ;
}

UICalendarPortlet.prototype.checkWorkingSetting = function(ck) {
	var isCheck = ck.checked ;
	if(isCheck) {
		eXo.calendar.UICalendarPortlet.workingSetting.style.visibility = "visible" ;
	} else {
		eXo.calendar.UICalendarPortlet.workingSetting.style.visibility = "hidden" ;
	}
}

UICalendarPortlet.prototype.showHideWorkingSetting = function() {
	var isCheck = this.checked ;
	if(isCheck) {
		eXo.calendar.UICalendarPortlet.workingSetting.style.visibility = "visible" ;
	} else {
		eXo.calendar.UICalendarPortlet.workingSetting.style.visibility = "hidden" ;
	}
} ;

eXo.calendar.UICalendarPortlet = new UICalendarPortlet() ;
eXo.calendar.UIResizeEvent = new UIResizeEvent() ;
eXo.calendar.UISelection = new UISelection() ;