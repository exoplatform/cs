function UICalendarPortlet() {
}
/*
 * 
 * @param {Object} object : DOM Element
 * @param {Object} styles : Object contains style name and style value
 * This function to set stylesheet for a DOM element
 */
UICalendarPortlet.prototype.setStyle = function(object,styles) {
	for(var value in styles) {
		object.style[value] = styles[value] ;
	}
} ;
/*
 * 
 * @param {Int} milliseconds : Milliseconds
 * This function to convert time from milliseconds to minutes
 */
UICalendarPortlet.prototype.timeToMin = function(milliseconds) {
	if (typeof(milliseconds) == "string") milliseconds = parseInt(milliseconds) ;
	var d = new Date(milliseconds) ;
	var hour = d.getHours() ;
	var min = d.getMinutes() ;
  var min = hour*60 + min ;
	return min ;
}	;
/*
 * 
 * @param {Int} min : Minutes
 * @param {String} timeFormat : format string of time
 * This function to convert time from minutes to time string
 */
UICalendarPortlet.prototype.minToTime = function(min,timeFormat) {
	var minutes = min%60 ;
	var hour = (min - minutes)/ 60 ;
	if (hour < 10) hour = "0" + hour ;
	if (minutes < 10) minutes = "0" + minutes ;
	if(eXo.calendar.UICalendarPortlet.timeFormat != "hh:mm a") return hour + ":" + minutes ;
	var time = hour + ":" + minutes ;
	if(!timeFormat) return time ;
	if(hour < 12) time += " " + timeFormat.am ;
	else if (hour == 12)  time += " " + timeFormat.pm ;
	else {
		hour -= 12 ;
		if (hour < 10) hour = "0" + hour ;
		time = hour + ":" + minutes ;
		time += " " + timeFormat.pm ;
	}
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

UICalendarPortlet.prototype.getWorkingdays = function(weekdays) {
	this.weekdays = weekdays ;
}

/* common method */
/*
 * This function to apply common setting for portlet
 */

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
/*
 * 
 * @param {Object} obj : DOM element
 * @param {Object} events : DOM element contains calendar events
 * @param {Object} container : DOM element contains all calendar events
 * This function to scroll scrollbar to certain postion
 */
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
/*
 * 
 * @param {Object} obj : DOM Element to click
 * @param {Object} evt : EventObject
 * This function to show/hide Calendar menu
 */
UICalendarPortlet.prototype.showMainMenu = function(obj, evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
  var d = new Date() ;
  var currentTime = d.getTime() ;
  var timezoneOffset = d.getTimezoneOffset() ;
	var oldmenu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu") ;
	var actions = eXo.core.DOMUtil.findDescendantsByTagName(oldmenu, "a") ;
  actions[1].href = String(actions[1].href).replace(/&.*/, "&ct=" + currentTime + "&tz=" + timezoneOffset + "')") ;
  eXo.calendar.UICalendarPortlet.swapMenu(oldmenu, obj) ;  
} ;
/*
 * 
 * @param {Object} obj : DOM Element to click
 * @param {Object} evt : EventObject
 * This function to show/hide Calendar and Group Calendar item
 */
UICalendarPortlet.prototype.show = function(obj, evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var DOMUtil = eXo.core.DOMUtil ;
	var portlet =	document.getElementById(this.portletName).parentNode ;
	var contentContainer = DOMUtil.findFirstDescendantByClass(portlet, "div", "ContentContainer") ;
	var	uiPopupCategory = DOMUtil.findNextElementByTagName(contentContainer,  "div") ;
  var d = new Date() ;
  var currentTime = d.getTime() ;
  var timezoneOffset = d.getTimezoneOffset() ;
  var selectedCategory = (eXo.calendar.UICalendarPortlet.selectedCategory)?eXo.calendar.UICalendarPortlet.selectedCategory :null ;
	if (DOMUtil.findAncestorByClass(obj, "CalendarItem")) {
    uiPopupCategory = DOMUtil.findNextElementByTagName(uiPopupCategory,  "div") ;
  }
	if (!uiPopupCategory) return ;
	var value = "" ;
	var calType = obj.getAttribute("calType") ;
	var calName = obj.getAttribute("calName") ;
	var calColor = obj.getAttribute("calColor") ;
	var canEdit =  String(obj.getAttribute("canedit")).toLowerCase() ;
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
  items[0].href = String(items[0].href).replace("')", "&ct=" + currentTime + "&tz=" + timezoneOffset + "')") ;
	if (DOMUtil.findAncestorByClass(obj, "CalendarItem")) {
    items[1].href = String(items[1].href).replace("')", "&ct=" + currentTime + "&tz=" + timezoneOffset + "')") ;
    items[0].href = String(items[0].href).replace("')", "&categoryId=" + selectedCategory + "')") ;
    items[1].href = String(items[1].href).replace("')", "&categoryId=" + selectedCategory + "')") ;
    
  }
	eXo.calendar.UICalendarPortlet.swapMenu(uiPopupCategory, obj) ;
	
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
	if (canEdit && (canEdit == "true") ) {
		for(var j = 0 ; j < actions.length ; j ++) {
			if (actions[j].href.indexOf("EditCalendar") >= 0) {
				actions[j].style.display = "block" ;
			}
		}
	}	
} ;
/*
 * 
 * @param {String} actions: action link string
 * This function to add current time and timezone offset to action link, then run it
 */
UICalendarPortlet.prototype.runAction = function(obj){
  var actions = obj.getAttribute("actionLink") ;
  var selectedCategroy = (this.selectedCategory)?this.selectedCategory:null ;
  var d = new Date() ;
  var currentTime = d.getTime() ;
  var timeZoneOffset = d.getTimezoneOffset() ;
  actions = actions.replace(/javascript:/,"") ;
  actions = actions.replace(/\'\)/,"&ct="+currentTime + "&tz=" + timeZoneOffset + "&categoryId=" + selectedCategroy + "')") ;
  eval(actions) ;
} ;

/*
 * This function to check layout of portlet when page load
 */

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
/*
 * 
 * @param {Int} layout : layout value
 * This function to switch among types of layout
 */
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
/*
 * Initialize some properties in Day view
 */
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
/*
 * 
 * @param {Object} viewer : DOM element contains all calendar events
 * This function to get all event element
 */
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
/*
 * 
 * @param {Object} obj : DOM element
 * This function to check a DOM element is visible or hidden
 */
UICalendarPortlet.prototype.isShow = function(obj) {
	if(obj.style.display != "none") return true ;
	return false ;
} ;
/*
 * 
 * @param {Object} elements : All calendar event calendar 
 * This function to get all visible event element
 */
UICalendarPortlet.prototype.getBlockElements = function(elements) {
	var el = new Array() ;
	var len = elements.length ;
	for(var i = 0 ; i < len ; i ++) {
		if(this.isShow(elements[i])) el.push(elements[i]);
	}
	return el ;
} ;
/**
 * 
 * @param {Object} obj : DOM element
 * This function to set size for a DOM element
 */
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
	eventContainer.style.height = (height - 19) + "px" ;
} ;

UICalendarPortlet.prototype.setWidth = function(element, width) {
	element.style.width = width + "%" ;
} ;

UICalendarPortlet.prototype.getSize = function(el){
	var start = parseInt(el.getAttribute("starttime")) ;
	var end =  parseInt(el.getAttribute("endtime")) ;
	return [start, end] ;
} ;

UICalendarPortlet.prototype.getInterval = function(el) {
	var bottom = new Array() ;
	var interval = new Array() ;
	var size = null ;
	if (!el || (el.length <= 0)) return ;
	for(var i = 0 ; i < el.length ; i ++ ) {
		size = this.getSize(el[i]) ;
		bottom.push(size[1]) ;
		if (bottom[i-1] && (size[0] > bottom[i-1])) {
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
		var totalWidth = (arguments.length > 1) ? arguments[1] : parseFloat(100) ;
		var offsetLeft = parseFloat(0) ;
		var left = parseFloat(0) ;
		if(arguments.length > 2) {
			offsetLeft = parseFloat(arguments[2]) ;
			left = arguments[2] ;
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
				width = parseFloat((totalWidth + left - parseFloat(el[mark].style.left) - parseFloat(el[mark].style.width))/len) ;
			} else {
				width = parseFloat(totalWidth/len) ;
			}
			UICalendarPortlet.setWidth(el[j], width) ;
			if (el[j-1]&&(len > 1)) el[j].style.left = offsetLeft + parseFloat(el[j-1].style.width)*n +  "%" ;
			else {
				el[j].style.left = offsetLeft +  "%" ;
			}
			n++ ;
		}
	}
} ;

UICalendarPortlet.prototype.showEvent = function() {
	this.init() ;
	var EventDayContainer = eXo.core.DOMUtil.findAncestorByClass(this.viewer,"EventDayContainer") ;
	//EventDayContainer.scrollTop = (this.workingStart) ? this.workingStart : 0 ;
	if (!this.init()) return ;
  this.viewType = "UIDayView" ;
	var el = this.getElements(this.viewer) ;
	el = this.sortByAttribute(el,"starttime") ;
	if (el.length <= 0) return ;
	var marker = null ;
	for(var i = 0 ; i < el.length ; i ++ ) {		
		this.setSize(el[i]) ;
		el[i].onmousedown = eXo.calendar.UICalendarPortlet.initDND ;
    el[i].ondblclick = eXo.calendar.UICalendarPortlet.ondblclickCallback ;
		marker = eXo.core.DOMUtil.findFirstChildByClass(el[i], "div", "ResizeEventContainer") ;
		marker.onmousedown = eXo.calendar.UIResizeEvent.init ;
	}
	this.items = el ;
	this.adjustWidth(this.items) ;
	this.setFocus(this.viewer, el, EventDayContainer) ;
	this.items = null ;
	this.viewer = null ;
} ;

UICalendarPortlet.prototype.onLoad = function(){
	if(eXo.core.Browser.isFF()) {
		window.setTimeout("eXo.calendar.UICalendarPortlet.checkFilter() ;", 1000) ;
		return ;
	}
	eXo.calendar.UICalendarPortlet.checkFilter() ;	
};

UICalendarPortlet.prototype.browserResizeCallback = function() {
	if (!eXo.calendar.UICalendarPortlet.items) return ;
	eXo.calendar.UICalendarPortlet.adjustWidth(eXo.calendar.UICalendarPortlet.items) ;
}

UICalendarPortlet.prototype.ondblclickCallback = function() {
  var eventId = this.getAttribute("eventId") ;
  var calendarId = this.getAttribute("calid") ;
  var calendarType = this.getAttribute("caltype") ;
  eXo.webui.UIForm.submitEvent('calendar#'+eXo.calendar.UICalendarPortlet.viewType,'Edit','&subComponentId='+eXo.calendar.UICalendarPortlet.viewType+'&objectId=' + eventId + '&calendarId=' + calendarId + '&calType='+ calendarType)
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
	var minHeight = 15 ;
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
	this.innerElementHeight =  this.innerElement.offsetHeight ;	
	this.posY = _e.clientY ;
	this.uppermost = outerElement.offsetTop + minHeight - container.scrollTop ;
	if(document.getElementById("UIPageDesktop")) {
		var uiWindow = eXo.core.DOMUtil.findAncestorByClass(container, "UIResizableBlock") ;
		this.uppermost -= uiWindow.scrollTop ;
	}
} ;

UIResizeEvent.prototype.execute = function(evt) {
	var _e = window.event || evt ;
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;	
	//var height = UIResizeEvent.outerElement.offsetHeight ;
	var mouseY = eXo.core.Browser.findMouseRelativeY(UIResizeEvent.container,_e) ;
	var mDelta = _e.clientY - UIResizeEvent.posY ;
//	var delta = posY + height - mouseY ;
	if (mouseY <= UIResizeEvent.uppermost) { return ;
		//if (mouseY >= (posY + height)) {
			UIResizeEvent.outerElement.style.height = (UIResizeEvent.minHeight - 2) + "px" ;
			UIResizeEvent.innerElement.style.height = (UIResizeEvent.minHeight - 22) + "px" ;
			window.document.title = mouseY + " --- " + UIResizeEvent.uppermost;
		//}
	} else {
		if (mDelta%UIResizeEvent.interval == 0) {
			UIResizeEvent.outerElement.style.height = UIResizeEvent.beforeHeight - 2 + mDelta + "px" ;
			UIResizeEvent.innerElement.style.height = UIResizeEvent.innerElementHeight + mDelta + "px" ;
			
		}
//		if (delta >= UIResizeEvent.interval) {
//			UIResizeEvent.outerElement.style.height = parseInt(UIResizeEvent.outerElement.style.height) - UIResizeEvent.interval + "px" ;
//			UIResizeEvent.innerElement.style.height = parseInt(UIResizeEvent.innerElement.style.height) - UIResizeEvent.interval + "px" ;
//			window.document.title = "2 :" + height + "-" + UIResizeEvent.minHeight ;
//		}
//		if (mouseY >= (posY + height)) {
//			UIResizeEvent.outerElement.style.height = parseInt(UIResizeEvent.outerElement.style.height) + UIResizeEvent.interval + "px" ;
//			UIResizeEvent.innerElement.style.height = parseInt(UIResizeEvent.innerElement.style.height) + UIResizeEvent.interval + "px" ;
//			window.document.title = "3 :" + height + "-" + UIResizeEvent.minHeight ;
//		}
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
	UIResizeEvent.innerElementHeight = null ;
	UIResizeEvent.beforeHeight = null ;
	UIResizeEvent.posY = null ;
	UIResizeEvent.uppermost = null ;
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
/*
 * 
 * @param {Object} obj : DOM element
 * This function to reset z-Index of DOM element
 */
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
/*
 * 
 * @param {Object} evt : Event Object
 * This function to initialize drag and drop actions
 */
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
  UICalendarPortlet.title = eXo.core.DOMUtil.findDescendantsByTagName(UICalendarPortlet.dragObject,"p")[0].innerHTML ;
} ;
/*
 * 
 * @param {Object} evt : Event Object
 * This function to process when dragging object 
 */
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
  UICalendarPortlet.updateTitle(UICalendarPortlet.dragObject, posY) ;
} ;
/**
 * 
 * @param {Object} events : DOM elemnt contains a calendar event
 * @param {Object} min : Minutes
 * This function to update title of event when dragging calendar event
 */
UICalendarPortlet.prototype.updateTitle = function(events,min) {
  var timeFormat = events.getAttribute("timeFormat") ;
  var title = eXo.core.DOMUtil.findDescendantsByTagName(events,"p")[0] ;
  timeFormat = (timeFormat)?eval(timeFormat) : {am: "AM", pm: "PM"} ;
  title.innerHTML = eXo.calendar.UICalendarPortlet.minToTime(min, timeFormat) ;
}

UICalendarPortlet.prototype.dragEnd = function() {
	this.onmousemove = null ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var dragObject = UICalendarPortlet.dragObject ;
	var calType = dragObject.getAttribute("calType") ;
	var start = parseInt(dragObject.getAttribute("startTime")) ;
	var end = parseInt(dragObject.getAttribute("endTime")) ;
  var title = eXo.core.DOMUtil.findDescendantsByTagName(dragObject,"p")[0] ;
  var titleName = UICalendarPortlet.title ;
	if (end == 0) end = 1440 ;
	var delta = end - start  ;
	var currentStart = dragObject.offsetTop ;
	var currentEnd = currentStart + delta ;
	var eventDayContainer = eXo.core.DOMUtil.findAncestorByClass(dragObject, "EventDayContainer") ;
	var eventTop = UICalendarPortlet.eventTop ;
  eventDayContainer.onmousemove = null ;
	eventDayContainer.onmouseup = null ;
  UICalendarPortlet.dragObject = null ;
  UICalendarPortlet.eventTop = null ;
  UICalendarPortlet.eventY = null ;
  UICalendarPortlet.dragContainer = null ;  
  UICalendarPortlet.title = null ;
	if(dragObject.offsetTop != eventTop) {
		var actionLink =	UICalendarPortlet.adjustTime(currentStart, currentEnd, dragObject) ;
		if (calType) actionLink = actionLink.replace(/'\s*\)/, "&calType=" + calType + "')") ;
		eval(actionLink) ;
	}
  title.innerHTML = titleName ;
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
	this.fixIE() ;
} ;

UICalendarPortlet.prototype.fixIE = function(){
	var isDesktop = document.getElementById("UIPageDesktop") ;
	if(isDesktop) {
		this.runTimes = true ;
	} else this.runTimes = null ;
	if(!this.runTimes) return
	if ((eXo.core.Browser.browserType == "ie") && isDesktop) {
  	var portlet = document.getElementById(this.portletName);
  	var uiResizeBlock = eXo.core.DOMUtil.findAncestorByClass(portlet, "UIResizableBlock");
		var relative = eXo.core.DOMUtil.findFirstDescendantByClass(uiResizeBlock,"div", "FixIE") ;
		relative.className = "UIResizableBlock" ;
		var style = {
			position:"relative",
			height: uiResizeBlock.offsetHeight + 'px',
			width:"100%",
			overflow:"auto"
		} ;
		this.setStyle(relative,style) ;
  }
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
		src = (eXo.core.DOMUtil.hasClass(src, "EventBoxes"))? src : eXo.core.DOMUtil.findAncestorByClass(src, "EventBoxes") ;
		var eventId = src.getAttribute("eventid") ;
		var calendarId = src.getAttribute("calid") ;
		var calType = src.getAttribute("calType") ;
//		map = {
//			"objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"objectId="+eventId ,
//			"calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calendarId="+calendarId
//		} ;
//		if (calType) {
			map = {
				"objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"objectId=" + eventId ,
				"calendarId\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calendarId=" + calendarId,
				"calType\s*=\s*[A-Za-z0-9_]*(?=&|'|\")":"calType=" + calType
			} ;
//		}
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
		if (document.getElementById("UIMonthView")) eXo.calendar.UICalendarMan.initMonth() ;
  	if (document.getElementById("UIDayViewGrid")) eXo.calendar.UICalendarPortlet.showEvent() ;
  	if (document.getElementById("UIWeekViewGrid")) {
  		eXo.calendar.UICalendarMan.initWeek() ;
  		eXo.calendar.UIWeekView.init() ;
  	}
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
  UICalendarPortlet.runFilterByCategory(UICalendarPortlet.filterSelect) ;
	try {	//TODO: review order javascript file 
		if (document.getElementById("UIMonthView")) eXo.calendar.UICalendarMan.initMonth() ;
  	if (document.getElementById("UIDayViewGrid")) eXo.calendar.UICalendarPortlet.showEvent() ;
  	if (document.getElementById("UIWeekViewGrid")) {
  		eXo.calendar.UICalendarMan.initWeek() ;
  		eXo.calendar.UIWeekView.init() ;
  	}
	}
	catch(e) {} ;

} ;

UICalendarPortlet.prototype.filterByCategory = function() {
	var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer") ;
	if (!uiCalendarViewContainer) return ;
	var category = this.options[this.selectedIndex].value ;
  eXo.calendar.UICalendarPortlet.selectedCategory = category ;
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
	if (document.getElementById("UIMonthView")) eXo.calendar.UICalendarMan.initMonth() ;
	if (document.getElementById("UIDayViewGrid")) eXo.calendar.UICalendarPortlet.showEvent() ;
	if (document.getElementById("UIWeekViewGrid")) {
		eXo.calendar.UICalendarMan.initWeek() ;
		eXo.calendar.UIWeekView.init() ;
	}
} ;

UICalendarPortlet.prototype.runFilterByCategory = function(selectobj) {
	var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer") ;
	if (!uiCalendarViewContainer) return ;
	var category = selectobj.options[selectobj.selectedIndex].value ;
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
	if (document.getElementById("UIMonthView")) eXo.calendar.UICalendarMan.initMonth() ;
	if (document.getElementById("UIDayViewGrid")) eXo.calendar.UICalendarPortlet.showEvent() ;
	if (document.getElementById("UIWeekViewGrid")) {
		eXo.calendar.UICalendarMan.initWeek() ;
		eXo.calendar.UIWeekView.init() ;
	}
} ;

UICalendarPortlet.prototype.getFilterForm = function(form) {
	if(typeof(form) == "string") form = document.getElementById(form) ;
	this.filterForm = form ;
	var CalendarGroup = eXo.core.DOMUtil.findDescendantsByClass(form, "input","CalendarGroup") ;	
	var CalendarItem = eXo.core.DOMUtil.findDescendantsByClass(form, "input","checkbox") ;
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

UICalendarPortlet.prototype.setSelected = function(form) {
  try{
    eXo.calendar.UICalendarPortlet.getFilterSelect(form) ;
  	eXo.calendar.UICalendarPortlet.selectedCategory = eXo.calendar.UICalendarPortlet.filterSelect.options[eXo.calendar.UICalendarPortlet.filterSelect.selectedIndex].value ;
  } catch(e) {}
} ;

UICalendarPortlet.prototype.checkFilter = function() {
  var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
  for(var i = 0 ; i < UICalendarPortlet.filterSelect.options.length ; i ++) {
    if(UICalendarPortlet.filterSelect.options[i].value == UICalendarPortlet.selectedCategory) {
      UICalendarPortlet.filterSelect.options[i].selected = true ;
    }
  }  
	this.checkCalendarFilter() ;
} ;

UICalendarPortlet.prototype.checkCalendarFilter = function() {
	if(!this.filterForm) return ;
	var checkbox = eXo.core.DOMUtil.findDescendantsByClass(this.filterForm, "input", "checkbox") ;
	var len = checkbox.length ;
	for(var i = 0 ; i < len ; i ++) {		
		this.runFilterByCalendar(checkbox[i].name, checkbox[i].checked) ;
	}
  this.runFilterByCategory(this.filterSelect) ;
} ;

UICalendarPortlet.prototype.checkCategoryFilter = function() {
	if(this.filterSelect) eXo.calendar.UICalendarPortlet.runFilterByCategory(this.filterSelect) ;	
} ;

/* EOF filter */
UICalendarPortlet.prototype.switchListView = function(obj, evt){
	var menu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIPopupCategory") ;
	if(eXo.core.Browser.isIE6()) {
		var size = {
			top: obj.offsetHeight ,
			left: "-" + obj.offsetWidth
		} ;
		this.setStyle(menu, size) ;
	} else{
		var size = {
			marginLeft: "-18px"
		} ;
		this.setStyle(menu, size) ;
	}
	eXo.webui.UIPopupSelectCategory.show(obj, evt);
} ;

UICalendarPortlet.prototype.showView = function(obj, evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;	
	var oldmenu = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu") ;
  var actions = eXo.core.DOMUtil.findDescendantsByClass(oldmenu, "a","MenuItem") ;
  if(!this.selectedCategory) this.selectedCategory = null ;
  for(var i=0 ; i < actions.length ; i++) {
    if(actions[i].href.indexOf("categoryId") < 0) continue ; 
    actions[i].href = String(actions[i].href).replace(/categoryId.*&/,"categoryId="+ this.selectedCategory + "&") ;
  }
	eXo.calendar.UICalendarPortlet.swapMenu(oldmenu, obj) ;
} ;

UICalendarPortlet.prototype.getScrollTop = function(obj) {
  var curtop = 0 ;  
  while (obj) {
    if(obj.scrollTop) curtop += obj.scrollTop ;
    obj = obj.parentNode ;    
  }
  return curtop ;
} ;

UICalendarPortlet.prototype.getScrollLeft = function(obj) {
  var curleft = 0 ;  
  while (obj) {
    if(obj.scrollLeft) curleft += obj.scrollLeft ;
    obj = obj.parentNode ;    
  }
  return curleft ;
} ;

UICalendarPortlet.prototype.swapIeMenu = function(menu, clickobj){
	var DOMUtil = eXo.core.DOMUtil ;
	var Browser = eXo.core.Browser ;
	var x = Browser.findPosXInContainer(clickobj,menu.offsetParent) - this.getScrollLeft(clickobj) ;
	var y = Browser.findPosYInContainer(clickobj,menu.offsetParent) - this.getScrollTop(clickobj) + clickobj.offsetHeight ;
	var browserHeight = document.documentElement.clientHeight ;
	var uiRightClickPopupMenu = (!DOMUtil.hasClass(menu,"UIRightClickPopupMenu"))?DOMUtil.findFirstDescendantByClass(menu, "div","UIRightClickPopupMenu") : menu ;
	this.showHide(menu) ;
	if((y + uiRightClickPopupMenu.offsetHeight) > browserHeight) {
		y = browserHeight - uiRightClickPopupMenu.offsetHeight ;
	}
	//TODO: fix on IE7 when there is the ControlWorkspace. This bug occurs only developing parameter assigned false
	if (Browser.isIE7()) {
		var uiControWorkspace = document.getElementById("UIControlWorkspace") ;
		if(uiControWorkspace) x -= uiControWorkspace.offsetWidth ;
	}
	DOMUtil.addClass(menu, "UICalendarPortlet UIEmpty") ;
	menu.style.zIndex = 2000 ;
	menu.style.left = x + "px" ;
	menu.style.top = y + "px" ;
} ;

UICalendarPortlet.prototype.swapMenu = function(oldmenu, clickobj) {
  var DOMUtil = eXo.core.DOMUtil ;
	var Browser = eXo.core.Browser ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
  var uiDesktop = document.getElementById("UIPageDesktop") ;
  var uiWorkSpaceWidth = (document.getElementById("UIControlWorkspace"))? document.getElementById("UIControlWorkspace").offsetWidth : 0 ;
	uiWorkSpaceWidth = (document.all) ? 2*uiWorkSpaceWidth : uiWorkSpaceWidth ;
//  var menuX = Browser.findPosX(clickobj) - uiWorkSpaceWidth ;
//	var menuY = Browser.findPosY(clickobj) + clickobj.offsetHeight ;
//  if(uiDesktop) {
//  	var portlet = DOMUtil.findAncestorByClass(document.getElementById(UICalendarPortlet.portletName), "UIResizableBlock") ;
//    var uiWindow = DOMUtil.findAncestorByClass(portlet, "UIWindow") ;
//    menuX = menuX - uiWindow.offsetLeft  -  portlet.scrollLeft ;
//    menuY = menuY - uiWindow.offsetTop  -  portlet.scrollTop ;
//  }
  if(document.getElementById("tmpMenuElement")) DOMUtil.removeElement(document.getElementById("tmpMenuElement")) ;
	var tmpMenuElement = oldmenu.cloneNode(true) ;
	tmpMenuElement.setAttribute("id","tmpMenuElement") ;
	this.menuElement = tmpMenuElement ;
	if (uiDesktop) {
		document.body.appendChild(this.menuElement) ;
		this.swapIeMenu(this.menuElement,clickobj) ;
		return ;
	}	else {
	  document.getElementById(this.portletName).appendChild(tmpMenuElement) ;		
	}
	
  var menuX = Browser.findPosX(clickobj) - uiWorkSpaceWidth ;
	var menuY = Browser.findPosY(clickobj) + clickobj.offsetHeight ;
	if(arguments.length > 2) {
    menuY -= arguments[2].scrollTop ;
  }
	
	this.menuElement.style.top = menuY + "px" ;
	this.menuElement.style.left = menuX + "px" ;	
	this.showHide(this.menuElement) ;
//  if(uiDesktop) {    
//    var uiRightClick = (DOMUtil.findFirstDescendantByClass(UICalendarPortlet.menuElement, "div", "UIRightClickPopupMenu")) ? DOMUtil.findFirstDescendantByClass(UICalendarPortlet.menuElement, "div", "UIRightClickPopupMenu") : UICalendarPortlet.menuElement ;
//    var mnuBottom = eXo.core.Browser.findPosYInContainer(UICalendarPortlet.menuElement, uiDesktop) + uiRightClick.offsetHeight ;
//    var widBottom = uiWindow.offsetTop + uiWindow.offsetHeight ;
//    if(mnuBottom > widBottom) {
//      menuY -= (mnuBottom - widBottom - clickobj.offsetHeight - uiWindow.scrollTop) ;
//      UICalendarPortlet.menuElement.style.top = menuY + "px" ;
//    }
//  } else {
    var uiRightClick = (DOMUtil.findFirstDescendantByClass(UICalendarPortlet.menuElement, "div", "UIRightClickPopupMenu")) ? DOMUtil.findFirstDescendantByClass(UICalendarPortlet.menuElement, "div", "UIRightClickPopupMenu") : UICalendarPortlet.menuElement ;
    var mnuBottom = UICalendarPortlet.menuElement.offsetTop +  uiRightClick.offsetHeight - window.document.documentElement.scrollTop ;
    if(window.document.documentElement.clientHeight < mnuBottom) {
      menuY += (window.document.documentElement.clientHeight - mnuBottom) ;
      UICalendarPortlet.menuElement.style.top = menuY + "px" ;      
    }
//  }
} ;

UICalendarPortlet.prototype.isAllday = function(form) {
	try{
		if (typeof(form) == "string") form = document.getElementById(form) ;		
		if (form.tagName.toLowerCase() != "form") {
			form = eXo.core.DOMUtil.findDescendantsByTagName(form, "form") ;
		}
		for(var i = 0 ; i < form.elements.length ; i ++) {
			if(form.elements[i].getAttribute("name") == "allDay") {
				eXo.calendar.UICalendarPortlet.allDayStatus = form.elements[i] ;
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
		UISelection.startX = eXo.core.Browser.findPosXInContainer(src, UISelection.container) - document.getElementById(eXo.calendar.UICalendarPortlet.portletName).parentNode.scrollTop;		
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

UICalendarPortlet.prototype.checkAllInBusy = function(chk){
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var isChecked = chk.checked ;
	var timeField = eXo.core.DOMUtil.findFirstDescendantByClass(chk.form, "div", "TimeField") ;
	if(isChecked) {
		timeField.style.display = "none" ;
	}else {
		timeField.style.display = "block" ;
	}
	if (UICalendarPortlet.allDayStatus) {
		UICalendarPortlet.allDayStatus.checked = isChecked ;
		UICalendarPortlet.showHideTime(UICalendarPortlet.allDayStatus) ;
  }
} ;

UICalendarPortlet.prototype.initCheck = function(container) {
	var DOMUtil = eXo.core.DOMUtil ;
	if(typeof(container) == "string") container = document.getElementById(container) ;
	var dateAll = DOMUtil.findDescendantsByClass(container, "input", "checkbox")[1] ;
  var serverTimezone = parseInt(container.getAttribute("serverTimezone")) ;
	var table = DOMUtil.findFirstDescendantByClass(container, "table", "UIGrid") ;
	var tr = DOMUtil.findDescendantsByTagName(table, "tr") ;	
	var firstTr = tr[1] ;
	this.busyCell = DOMUtil.findDescendantsByTagName(firstTr, "td").slice(1) ;
	var len = tr.length ;
	for(var i = 2 ; i < len ; i ++) {
		this.showBusyTime(tr[i],serverTimezone) ;
	}
	if(eXo.calendar.UICalendarPortlet.allDayStatus) dateAll.checked = eXo.calendar.UICalendarPortlet.allDayStatus.checked ;
	eXo.calendar.UICalendarPortlet.checkAllInBusy(dateAll) ;
	dateAll.onclick = function() {		
		eXo.calendar.UICalendarPortlet.checkAllInBusy(this) ;
	}
	eXo.calendar.UICalendarPortlet.initSelectionX(firstTr) ;
} ;

UICalendarPortlet.prototype.localTimeToMin = function(millis, timezoneOffset) {
	if (typeof(millis) == "string") millis = parseInt(millis) ;
	millis -= timezoneOffset*60*1000 ;
	var d = new Date(millis) ;
	var hour = d.getHours() ;
	var min = d.getMinutes() ;
  var min = hour*60 + min ;
	return min ;
} ;

UICalendarPortlet.prototype.parseTime = function(string,timezoneOffset) {
	var stringTime = string.split(",") ;
	var len = stringTime.length ;
	var time = new Array() ;
	var tmp = null ;
	for(var i = 0 ; i < len ; i += 2) {
		tmp = {"from": this.localTimeToMin(stringTime[i],timezoneOffset),"to":this.localTimeToMin(stringTime[i+1],timezoneOffset)} ;
		time.push(tmp) ;
	}
	return time ;
} ;

UICalendarPortlet.prototype.showBusyTime = function(tr,serverTimezone) {
	var stringTime = tr.getAttribute("busytime") ;
	var localize = (tr.getAttribute("usertimezone")) ? parseInt(tr.getAttribute("usertimezone")) : 0 ;
  var extraTime = localize - serverTimezone ;
	if (!stringTime) return ;
	var time = this.parseTime(stringTime,extraTime) ;
	var len = time.length ;
	var from = null ;
	var to = null ;
	for(var i = 0 ; i < len ; i ++) {
		from = parseInt(time[i].from);
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
	var timeTable = DOMUtil.findAncestorByTagName(Highlighter.firstCell, "table") ;
	var dateValue = timeTable.getAttribute("datevalue") ;
	var uiTabContentContainer = DOMUtil.findAncestorByClass(Highlighter.startCell, "UITabContentContainer") ;
	var UIComboboxInputs = DOMUtil.findDescendantsByClass(uiTabContentContainer, "input","UIComboboxInput") ;
	var len = UIComboboxInputs.length ;
	var name = null ;
	var timeFormat = this.getTimeFormat(UIComboboxInputs[0]) ;
	start = this.minToTime(start, timeFormat) ;
	end = this.minToTime(end, timeFormat) ;
	if(dateValue) {
		var DateContainer = DOMUtil.findAncestorByTagName(uiTabContentContainer, "form") ;
		DateContainer.from.value = dateValue ;
		DateContainer.to.value = dateValue ;		
	}
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
