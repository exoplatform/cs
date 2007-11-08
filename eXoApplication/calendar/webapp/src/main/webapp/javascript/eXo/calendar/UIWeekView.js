eXo.require('eXo.calendar.UICalendarPortlet', '/javascript/calendar/') ;
function UIWeekView() {
	
}

UIWeekView.prototype.mousePos = function(ev){
	if(ev.pageX || ev.pageY){
		return {x:ev.pageX, y:ev.pageY};
	}
	return {
		x:ev.clientX + document.body.scrollLeft - document.body.clientLeft,
		y:ev.clientY + document.body.scrollTop  - document.body.clientTop
	} ;
} ;

UIWeekView.prototype.init = function() {
	if (arguments.length >= 2){
		var  UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
		UICalendarPortlet.setting(arguments[0], arguments[1]) ;
		var workingStart = (UICalendarPortlet.workingStart) ? UICalendarPortlet.workingStart : 0 ;		
	}
	var DOMUtil = eXo.core.DOMUtil  ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer") ;
	UIWeekView.container = document.getElementById("UIWeekViewGrid") ;
	UIWeekView.items = DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", "EventContainerBorder") ;
	var marker = null ;
	for(var i = 0 ; i < UIWeekView.items.length ; i ++){		
		var height = parseInt(UIWeekView.items[i].getAttribute("endtime")) - parseInt(UIWeekView.items[i].getAttribute("starttime")) ;
		UIWeekView.items[i].onmousedown = UIWeekView.dragStart ;
		eXo.calendar.UICalendarPortlet.setSize(UIWeekView.items[i], workingStart) ;
		marker = DOMUtil.findFirstDescendantByClass(UIWeekView.items[i], "div", "ResizeEventContainer") ;
		marker.onmousedown = UIWeekView.initResize ;
	}
	var uiWeekViewGrid = document.getElementById("UIWeekViewGrid") ;
	var tr = DOMUtil.findDescendantsByTagName(uiWeekViewGrid, "tr") ;
	var firstTr = null ;
	for(var i = 0 ; i < tr.length ; i ++) {
		if (tr[i].style.display != "none") {
			firstTr = tr[i] ;
			break ;
		}
	}
	UIWeekView.cols = DOMUtil.findDescendantsByTagName(firstTr, "td") ;
	var len = UIWeekView.cols.length ;
	for(var i = 1 ; i < len ; i ++) {
		var colIndex = parseInt(UIWeekView.cols[i].getAttribute("eventindex")) ;
		var eventIndex = null ;
		for(var j = 0 ; j < UIWeekView.items.length ; j ++){		
			eventIndex = parseInt(UIWeekView.items[j].getAttribute("eventindex")) ;
			if (colIndex == eventIndex) UIWeekView.cols[i].appendChild(UIWeekView.items[j]) ;
		}	
		if (!DOMUtil.findChildrenByClass(UIWeekView.cols[i], "div", "EventContainerBorder")) return ;
		UIWeekView.showInCol(UIWeekView.cols[i]) ;
	}
	UIWeekView.initAllday() ;
} ;

UIWeekView.prototype.showInCol = function(obj) {
	var items = eXo.core.DOMUtil.findDescendantsByClass(obj, "div", "EventContainerBorder") ;
	var len = items.length ;
	if (len <= 0) return ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	var posX = eXo.core.Browser.findPosXInContainer(obj, UIWeekView.container) ;
	var left = parseFloat(posX/UIWeekView.container.offsetWidth)*100 ;
	var width = parseFloat((obj.offsetWidth - 2)/UIWeekView.container.offsetWidth)*100 ;
	items = eXo.calendar.UICalendarPortlet.sortByAttribute(items, "starttime") ;
	eXo.calendar.UICalendarPortlet.adjustWidth(items, width, left) ;
} ;

UIWeekView.prototype.dragStart = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	if (_e.button == 2) return ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	UIWeekView.dragElement = this ;
	UIWeekView.objectOffsetLeft = eXo.core.Browser.findPosX(UIWeekView.dragElement) ;
	UIWeekView.offset = UIWeekView.getOffset(UIWeekView.dragElement, _e) ;
	UIWeekView.mouseY = _e.clientY ;
	UIWeekView.eventY = UIWeekView.dragElement.offsetTop ;
	UIWeekView.containerOffset = {
		"x" : eXo.core.Browser.findPosX(UIWeekView.container.parentNode),
		"y" : eXo.core.Browser.findPosY(UIWeekView.container.parentNode)
	}
	document.onmousemove = UIWeekView.drag ;
	document.onmouseup = UIWeekView.drop ;
} ;

UIWeekView.prototype.drag = function(evt) {
	var _e = window.event || evt ;
	var src = _e.srcElement || _e.target ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	var deltaY = _e.clientY - UIWeekView.mouseY ;
	if(deltaY%eXo.calendar.UICalendarPortlet.interval == 0) UIWeekView.dragElement.style.top = UIWeekView.mousePos(_e).y - UIWeekView.offset.y - UIWeekView.containerOffset.y + "px" ;
	if (UIWeekView.isCol(_e)) {
		var posX = eXo.core.Browser.findPosXInContainer(UIWeekView.currentCol, UIWeekView.container) ;
		var left = parseFloat(posX/UIWeekView.container.offsetWidth)*100 ;
		UIWeekView.dragElement.style.left = left + "%" ;
	}
} ;

UIWeekView.prototype.dropCallback = function() {
	var dragElement = eXo.calendar.UIWeekView.dragElement ;
	var start = parseInt(dragElement.getAttribute("startTime")) ;
	var end = parseInt(dragElement.getAttribute("endTime")) ;
	var calType = parseInt(dragElement.getAttribute("calType")) ;
	var workingStart = (eXo.calendar.UICalendarPortlet.workingStart) ? eXo.calendar.UICalendarPortlet.workingStart : 0 ;
	var delta = end - start  ;
	var currentStart = dragElement.offsetTop + workingStart ;
	var currentEnd = currentStart + delta ;	
	var actionLink =	eXo.calendar.UICalendarPortlet.adjustTime(currentStart, currentEnd, dragElement) ;
	var currentDate = eXo.calendar.UIWeekView.currentCol.getAttribute("starttime").toString() ;
	actionLink = actionLink.toString().replace(/'\s*\)/,"&currentDate=" + currentDate + "&calType=" + calType + "')") ;
	eval(actionLink) ;	
} ;

UIWeekView.prototype.drop = function(evt) {
	var _e = window.event || evt ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	if (!UIWeekView.isCol(_e)) return ;
	var currentCol = UIWeekView.currentCol ;
	var sourceCol = UIWeekView.dragElement.parentNode ;
	UIWeekView.currentCol.appendChild(UIWeekView.dragElement) ;
	UIWeekView.showInCol(currentCol) ;
	UIWeekView.showInCol(sourceCol) ;	
	if ((currentCol != sourceCol) || (UIWeekView.dragElement.offsetTop != UIWeekView.eventY)) UIWeekView.dropCallback() ;
	UIWeekView.dragElement = null ;
	document.onmousemove = null ;
	return null ;
} ;

UIWeekView.prototype.getOffset = function(object, evt) {	
	return {
		"x": (eXo.calendar.UIWeekView.mousePos(evt).x - eXo.core.Browser.findPosX(object)) ,
		"y": (eXo.calendar.UIWeekView.mousePos(evt).y - eXo.core.Browser.findPosY(object))
	} ;
} ;

UIWeekView.prototype.isCol = function(evt) {
	var UIWeekView = eXo.calendar.UIWeekView ;
	if (!UIWeekView.dragElement) return false;
	var Browser = eXo.core.Browser ;
	var mouseX = Browser.findMouseXInPage(evt) ;
	var len = UIWeekView.cols.length ;
	var colX = 0 ;
	for(var i = 1 ; i < len ; i ++) {
		colX = Browser.findPosX(UIWeekView.cols[i]) ;
		if ((mouseX > colX) && (mouseX < colX + UIWeekView.cols[i].offsetWidth)){
			return UIWeekView.currentCol = UIWeekView.cols[i] ;
		}
	}
	
	return false ;
} ;

// for resize

UIWeekView.prototype.initResize = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;
	var outerElement = eXo.core.DOMUtil.findAncestorByClass(this,'EventContainerBorder') ;
	var innerElement = eXo.core.DOMUtil.findPreviousElementByTagName(this, "div") ;
	var container = document.getElementById("UIWeekViewGrid") ;
	var minHeight = 30 ;
	var interval = eXo.calendar.UICalendarPortlet.interval ;
	UIResizeEvent.start(_e, innerElement, outerElement, container, minHeight, interval) ;
	UIResizeEvent.callback = eXo.calendar.UIWeekView.resizeCallback ;
} ;

UIWeekView.prototype.resizeCallback = function(evt) {
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;
	var eventBox = UIResizeEvent.outerElement ;
	var start =  parseInt(eventBox.getAttribute("startTime")) ;
	var end =  start + eventBox.offsetHeight - 2 ;
	var calType = parseInt(eventBox.getAttribute("calType")) ;
	if (eventBox.offsetHeight != UIResizeEvent.outerElementHeight) {
		var actionLink = eXo.calendar.UICalendarPortlet.adjustTime(start, end, eventBox) ;
		var currentDate = eventBox.parentNode.getAttribute("starttime").toString() ;
		actionLink = actionLink.toString().replace(/'\s*\)/,"&currentDate=" + currentDate + "&calType=" + calType + "')") ;
		eval(actionLink) ;
	}	
} ;

// For all day event

UIWeekView.prototype.initAllday = function() {
	var uiWeekViewGridAllDay = document.getElementById("UIWeekViewGridAllDay") ;
	var eventAlldayContainers = eXo.core.DOMUtil.findDescendantsByClass(uiWeekViewGridAllDay, "div", "EventAlldayContainer") ;
	var th = eXo.core.DOMUtil.findDescendantsByTagName(uiWeekViewGridAllDay, "th") ;
	var len = eventAlldayContainers.length ;
	if(len <= 0) return ;
	var indexBeginDate = 0 ;
	var indexEndDate = 0 ;
	var startTime = 0 ;
	var endTime = 0 ;
	var delta = 0 ;
	for(var i = 0 ; i < len ; i ++) {
		indexBeginDate = parseInt(eventAlldayContainers[i].getAttribute("indexBeginDate")) ;
		indexEndDate = parseInt(eventAlldayContainers[i].getAttribute("indexEndDate")) ;
		startTime = eXo.calendar.UICalendarPortlet.timeToMin(eventAlldayContainers[i].getAttribute("startTime")) ; 	
		endTime = eXo.calendar.UICalendarPortlet.timeToMin(eventAlldayContainers[i].getAttribute("endTime")) ;
		delta = (Math.abs(indexBeginDate - indexEndDate) == 0) ? 1 : Math.abs(indexBeginDate - indexEndDate) ;
		eventAlldayContainers[i].style.width = delta*(100/7) + parseInt((endTime - startTime)*(10/(24*7*6))) + "%" ;
		eventAlldayContainers[i].style.marginLeft = parseFloat((indexBeginDate)*(100/7)) + (startTime)*(10/(24*7*6))  + "%" ;

	}
}
eXo.calendar.UIWeekView = new UIWeekView() ;
