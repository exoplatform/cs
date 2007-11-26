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
	var  UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var DOMUtil = eXo.core.DOMUtil  ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	var uiCalendarViewContainer = document.getElementById("UICalendarViewContainer") ;
	UIWeekView.container = document.getElementById("UIWeekViewGrid") ;
	UIWeekView.items = DOMUtil.findDescendantsByClass(uiCalendarViewContainer, "div", "EventContainerBorder") ;
	var marker = null ;
	for(var i = 0 ; i < UIWeekView.items.length ; i ++){		
		var height = parseInt(UIWeekView.items[i].getAttribute("endtime")) - parseInt(UIWeekView.items[i].getAttribute("starttime")) ;
		UIWeekView.items[i].onmousedown = UIWeekView.dragStart ;
		eXo.calendar.UICalendarPortlet.setSize(UIWeekView.items[i]) ;
		marker = DOMUtil.findFirstDescendantByClass(UIWeekView.items[i], "div", "ResizeEventContainer") ;
		marker.onmousedown = UIWeekView.initResize ;
	}
	var tr = DOMUtil.findDescendantsByTagName(UIWeekView.container, "tr") ;
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
	var EventWeekContent = DOMUtil.findAncestorByClass(UIWeekView.container,"EventWeekContent") ;
	EventWeekContent.scrollTop = (UICalendarPortlet.workingStart) ? UICalendarPortlet.workingStart : 0 ;
} ;

UIWeekView.prototype.showInCol = function(obj) {
	var items = eXo.calendar.UICalendarPortlet.getElements(obj) ;//eXo.core.DOMUtil.findDescendantsByClass(obj, "div", "EventContainerBorder") ;
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
	eXo.calendar.UICalendarPortlet.resetZIndex(UIWeekView.dragElement) ;
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
	var mouseY = eXo.core.Browser.findMouseRelativeY(UIWeekView.container,_e) - UIWeekView.container.scrollTop ;
	var posY = UIWeekView.dragElement.offsetTop ;
	var height =  UIWeekView.dragElement.offsetHeight ;
	var deltaY = null ;	
	deltaY = _e.clientY - UIWeekView.mouseY ;
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
	var workingStart = 0 ;
	if (end == 0) end = 1440 ;
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
	UIWeekView.showInCol(sourceCol) ;	
	UIWeekView.showInCol(currentCol) ;
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
	var container = eXo.core.DOMUtil.findAncestorByClass(document.getElementById("UIWeekViewGrid"), "EventWeekContent") ;
	var minHeight = 30 ;
	var interval = eXo.calendar.UICalendarPortlet.interval ;
	UIResizeEvent.start(_e, innerElement, outerElement, container, minHeight, interval) ;
	UIResizeEvent.callback = eXo.calendar.UIWeekView.resizeCallback ;
} ;

UIWeekView.prototype.resizeCallback = function(evt) {
	var UIResizeEvent = eXo.calendar.UIResizeEvent ;
	var eventBox = UIResizeEvent.outerElement ;
	var start =  parseInt(eventBox.getAttribute("startTime")) ;
	var end =  start + eventBox.offsetHeight ;
	var calType = parseInt(eventBox.getAttribute("calType")) ;
	if (eventBox.offsetHeight != UIResizeEvent.beforeHeight) {
		var actionLink = eXo.calendar.UICalendarPortlet.adjustTime(start, end, eventBox) ;
		var currentDate = eventBox.parentNode.getAttribute("starttime").toString() ;
		actionLink = actionLink.toString().replace(/'\s*\)/,"&currentDate=" + currentDate + "&calType=" + calType + "')") ;
		eval(actionLink) ;
	}	
} ;

UIWeekView.prototype.initAllDayRightResize = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var UIHorizontalResize = eXo.calendar.UIHorizontalResize ;
	var outerElement = eXo.core.DOMUtil.findAncestorByClass(this,'WeekViewEventBoxes') ;
	var innerElement = eXo.core.DOMUtil.findFirstDescendantByClass(outerElement, "div", "EventAlldayContent") ;
	UIHorizontalResize.start(_e, outerElement, innerElement) ;
	UIHorizontalResize.callback = eXo.calendar.UIWeekView.rightResizeCallback ;
} ;

UIWeekView.prototype.initAllDayLeftResize = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var UIHorizontalResize = eXo.calendar.UIHorizontalResize ;
	var outerElement = eXo.core.DOMUtil.findAncestorByClass(this,'WeekViewEventBoxes') ;
	var innerElement = eXo.core.DOMUtil.findFirstDescendantByClass(outerElement, "div", "EventAlldayContent") ;
	UIHorizontalResize.start(_e, outerElement, innerElement, true) ;
	UIHorizontalResize.callback = eXo.calendar.UIWeekView.leftResizeCallback ;
} ;
// For all day event

UIWeekView.prototype.initAlldayDND = function(evt) {
	var _e = window.event || evt ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	var DragDrop = eXo.core.DragDrop ;
	var EventAllday = eXo.core.DOMUtil.findAncestorByClass(this, "EventAllday") ;
	dragObject = this ;
	UIWeekView.totalWidth = EventAllday.offsetWidth ;
	UIWeekView.elementTop = dragObject.offsetTop ;
	UIWeekView.elementLeft = dragObject.offsetLeft ;
	DragDrop.initCallback = UIWeekView.allDayInitCallback ;
  DragDrop.dragCallback = UIWeekView.allDayDragCallback ;
  DragDrop.dropCallback = UIWeekView.allDayDropCallback ;
	DragDrop.init(null, dragObject, dragObject, _e) ;
	UIWeekView.beforeStart = dragObject.offsetLeft ;
} ;

UIWeekView.prototype.allDayInitCallback = function(evt) {
	var UIWeekView = eXo.calendar.UIWeekView ;
	var dragObject = evt.dragObject ;
	dragObject.style.left = UIWeekView.elementLeft + "px" ;
} ;

UIWeekView.prototype.allDayDragCallback = function(evt) {
	var UIWeekView = eXo.calendar.UIWeekView ;
	var dragObject = evt.dragObject ;
	dragObject.style.top = UIWeekView.elementTop + "px" ;
	var posX = parseInt(dragObject.style.left) ;
	var maxX = posX + dragObject.offsetWidth ;
	if (posX <= 0) dragObject.style.left = "0px" ;
	if (maxX >= UIWeekView.totalWidth) dragObject.style.left = (UIWeekView.totalWidth - dragObject.offsetWidth) + "px" ;
} ;

UIWeekView.prototype.allDayDropCallback = function(evt) {
	var dragObject = evt.dragObject ;	
	var UIWeekView = eXo.calendar.UIWeekView ;
	dragObject.style.left = parseFloat(dragObject.offsetLeft/UIWeekView.totalWidth)*100 + "%" ;
	var delta = dragObject.offsetLeft - UIWeekView.beforeStart ;
	UIWeekView.elementLeft = null ;
	UIWeekView.elementTop = null ;
	UIWeekView.totalWidth = null ;
	UIWeekView.beforeStart = null ;
	if (delta != 0) {
		var UICalendarPortlet = eXo.calendar.UICalendarPortlet
		var delta = Math.round(delta*(24*7*60*60*1000)/UIWeekView.totalWidth) ;
		var start =  parseInt(dragObject.getAttribute("startTime")) + delta ;
		var end = parseInt(dragObject.getAttribute("endTime")) + delta ;
		var calType = parseInt(dragObject.getAttribute("calType")) ;
		var actionLink = UICalendarPortlet.adjustTime(start, end, dragObject) ;
		actionLink = actionLink.toString().replace(/'\s*\)/,"&calType=" + calType + "')") ;
		eval(actionLink) ;
	}	
} ;

UIWeekView.prototype.initAllday = function() {
	var UIWeekView = eXo.calendar.UIWeekView ;
	var uiWeekViewGridAllDay = document.getElementById("UIWeekViewGridAllDay") ;
	var eventAllday = eXo.core.DOMUtil.findDescendantsByClass(uiWeekViewGridAllDay, "div", "EventAlldayContainer") ;
	var len = eventAllday.length ;
	if (len <= 0) return ;
	var resizeMark = null ;
	for(var i = 0 ; i < len ; i ++) {
		resizeMark = eXo.core.DOMUtil.getChildrenByTagName(eventAllday[i], "div") ;
		resizeMark[0].onmousedown = UIWeekView.initAllDayLeftResize ;
		resizeMark[2].onmousedown = UIWeekView.initAllDayRightResize ; 
	}
	var EventAlldayContainer = eXo.core.DOMUtil.findFirstDescendantByClass(uiWeekViewGridAllDay,"td","EventAllday") ;
	EventAlldayContainer.style.height = eventAllday.length * eventAllday[0].offsetHeight + "px" ;
	var th = eXo.core.DOMUtil.findDescendantsByTagName(uiWeekViewGridAllDay, "th") ;
	eventAllday = this.sortEventsInCol(th, eventAllday) ;
	this.setPosition(eventAllday) ;
} ;

UIWeekView.prototype.sortByWidth = function(obj) {
	var len = obj.length ;
	var tmp = null ;
	var attribute1 = null ;
	var attribute2 = null ;
	var start1 = null ;
	var start2 = null ;
	for(var i = 0 ; i < len ; i ++){
		attribute1 = obj[i].offsetWidth ;
		start1 = parseInt(obj[i].getAttribute("startTime")) ;
		for(var j = i + 1 ; j < len ; j ++){
			attribute2 = obj[j].offsetWidth ;
			start2 = parseInt(obj[i].getAttribute("startTime")) ;
			if((attribute2 > attribute1) && (start1 == start2)) {
				tmp = obj[i] ;
				obj[i] = obj[j] ;
				obj[j] = tmp ;
			}
		}
	}
	return obj ;
} ;

UIWeekView.prototype.getMinutes = function(millisecond) {
	return eXo.calendar.UICalendarPortlet.timeToMin(millisecond) ;
} ;

UIWeekView.prototype.getEventsInCol = function(col, events) {
	var colLeft = col.offsetLeft ;
	var colWidth = col.offsetLeft + col.offsetWidth ;
	var eventLeft = null ;
	var len = events.length ;
	var eventsInCol = new Array() ;
	for(var i = 0 ; i < len ; i++) {
		eventLeft = events[i].offsetLeft ;
		if((eventLeft >= colLeft) && (eventLeft <= colWidth)) eventsInCol.push(events[i]) ;
	}
	if (eventsInCol.length <= 0) return false ;
	eventsInCol = this.sortByWidth(eventsInCol) ;
	return eventsInCol ;
} ;

UIWeekView.prototype.sortEventsInCol = function(cols,events) {
	events = eXo.calendar.UICalendarPortlet.sortByAttribute(events, "startTime") ;
	var len = cols.length ;
	if(len <= 0) return ;
	var newevents = new Array() ;
	for(var i =0 ; i < len ; i ++) {		
		if (this.getEventsInCol(cols[i], events) != false) {
			newevents = newevents.concat(this.getEventsInCol(cols[i], events)) ;			
		}
	}
	return newevents ;
} ;

UIWeekView.prototype.setPosition = function(events) {
	events = this.setWidth(events) ;
	events = this.setLeft(events) ;
	//this.setTop(events) ;
} ;


UIWeekView.prototype.setTop = function(events) {
	var beforeLeft = 0 ;
	var afterLeft = 0 ;
	for(var i = 0 ; i < events.length ; i ++) {
		beforeLeft = parseFloat(events[i].style.width) + parseFloat(events[i].style.left) ;
		afterLeft = 0 ;
		for(var j = i + 1, beforeLeft = parseFloat(events[i].style.width) + parseFloat(events[i].style.left) ; j < events.length ; j++) {
			afterLeft = parseFloat(events[j].style.left) ;
			if (afterLeft >= beforeLeft) {
				events[j].style.top = parseInt(events[i].style.top) + "px" ;
				beforeLeft += parseInt(events[i].style.top) ;
				events.splice(j) ;
				break ;
			}
		}
	}
} ;

UIWeekView.prototype.setLeft = function(events) {
	var len = events.length ;
	var uiWeekViewGridAllDay = document.getElementById("UIWeekViewGridAllDay") ;
	var th = eXo.core.DOMUtil.findDescendantsByTagName(uiWeekViewGridAllDay, "th")[1] ;	
	var start = 0 ;
	var left = 0 ;
	var startWeek = parseInt(th.getAttribute("startTime")) ;
	var totalWidth = parseFloat(eXo.core.Browser.findPosXInContainer(events[0].parentNode, events[0].offsetParent)/events[0].offsetParent.offsetWidth)*100 ;
	for(var i = 0 ; i < len ; i ++) {
		start = parseInt(events[i].getAttribute("startTime")) ;
		if (start < startWeek) start = startWeek ;
		diff = start - startWeek ;
		left = parseFloat((diff/(24*7*60*60*1000))*(100*events[0].parentNode.offsetWidth)/(events[0].offsetParent.offsetWidth)) ;
		events[i].style.left = left + totalWidth + "%" ;
		events[i].style.top = eXo.core.Browser.findPosYInContainer(events[i],events[i].offsetParent) +  i*events[i].offsetHeight + "px" ;
	}
	return events ;
} ;

UIWeekView.prototype.setWidth = function(events) {
	var len = events.length ;
	var start = 0 ;
	var end = 0 ;
	var diff = 0 ;
	var uiWeekViewGridAllDay = document.getElementById("UIWeekViewGridAllDay") ;
	var startWeek = eXo.core.DOMUtil.findDescendantsByTagName(uiWeekViewGridAllDay, "th")[1] ;	
	var endWeek = eXo.core.DOMUtil.findDescendantsByTagName(uiWeekViewGridAllDay, "th")[7] ;
	startWeek = parseInt(startWeek.getAttribute("startTime")) ;
	endWeek = parseInt(endWeek.getAttribute("startTime")) ;
	var totalWidth = parseFloat(events[0].parentNode.offsetWidth/events[0].offsetParent.offsetWidth) ;
	for(var i = 0 ; i < len ; i ++) {
		start = parseInt(events[i].getAttribute("startTime")) ;
		end = parseInt(events[i].getAttribute("endTime")) ;
		if (start < startWeek) start = startWeek ;
		if (end > (endWeek + 24*60*60*1000)) end = endWeek + 24*60*60*1000 ;
		diff = end - start ;
		events[i].style.width = parseFloat(diff/(24*7*60*60*1000))*100*totalWidth - 0.2 + "%" ;
		events[i].onmousedown = eXo.calendar.UIWeekView.initAlldayDND ;
	}
	return events ;
} ;
// Resize horizontal

function UIHorizontalResize() {
	
}

UIHorizontalResize.prototype.start = function(evt, outer, inner) {
	var _e = window.event || evt ;
	this.outerElement = outer ;
	this.innerElement = inner ;
	this.outerElement.style.width = this.outerElement.offsetWidth - 2 + "px" ;
	this.innerElement.style.width = this.innerElement.offsetWidth - 2 + "px" ;
	if(arguments.length > 3) {
		this.outerElement.style.left = this.outerElement.offsetLeft + "px" ;
		this.isLeft = true ;
		this.beforeLeft = this.outerElement.offsetLeft ;
	} else {
		this.isLeft = false ;
	}
	this.mouseX = _e.clientX ;
	this.outerBeforeWidth = this.outerElement.offsetWidth ;
	this.innerBeforeWidth = this.innerElement.offsetWidth ;	
	document.onmousemove = eXo.calendar.UIHorizontalResize.execute ;
	document.onmouseup = eXo.calendar.UIHorizontalResize.end ;
} ;

UIHorizontalResize.prototype.execute = function(evt) {
	var _e = window.event || evt ;
	var	UIHorizontalResize = eXo.calendar.UIHorizontalResize ;
	var delta = _e.clientX - UIHorizontalResize.mouseX ;
	if(UIHorizontalResize.isLeft == true) {
		window.status = "Left true : " + UIHorizontalResize.outerElement.style.left ;
		UIHorizontalResize.outerElement.style.left = UIHorizontalResize.beforeLeft + delta + "px" ;
		UIHorizontalResize.outerElement.style.width = UIHorizontalResize.outerBeforeWidth - delta + "px" ;
		UIHorizontalResize.innerElement.style.width = UIHorizontalResize.innerBeforeWidth - delta + "px" ;
	} else {
		UIHorizontalResize.outerElement.style.width = UIHorizontalResize.outerBeforeWidth + delta + "px" ;
		UIHorizontalResize.innerElement.style.width = UIHorizontalResize.innerBeforeWidth + delta + "px" ;		
	}
} ;

UIHorizontalResize.prototype.end = function(evt) {
	var	UIHorizontalResize = eXo.calendar.UIHorizontalResize ;
	UIHorizontalResize.outerElement = null ;
	UIHorizontalResize.innerElement = null ;
	document.onmousemove = null ;
	document.onmouseup = null ;
	if (typeof(UIHorizontalResize.callback) == "function") UIHorizontalResize.callback() ;
} ;

// For user selection 

UIWeekView.prototype.initSelection = function() {	
	var UISelection = eXo.calendar.UISelection ;
	var container = document.getElementById("UIWeekViewGrid") ;
	UISelection.step = 30 ;	
	UISelection.block = document.createElement("div")
	UISelection.block.className = "UserSelectionBlock" ;
	UISelection.container = container ;
	eXo.core.DOMUtil.findPreviousElementByTagName(document.getElementById("UIWeekViewGrid"), "div").appendChild(UISelection.block) ;
	UISelection.container.onmousedown = UISelection.start ;
	UISelection.relativeObject = eXo.core.DOMUtil.findAncestorByClass(UISelection.container, "EventWeekContent") ;
	UISelection.viewType = "UIWeekView" ;
} ;

UIWeekView.prototype.initSelectionX = function() {	
	var UISelectionX = eXo.calendar.UISelectionX ;
	var containers = eXo.core.DOMUtil.findDescendantsByTagName(document.getElementById("UIWeekViewGridAllDay"), "th") ;
	var len = containers.length ;
	for(var i = 1 ; i < len ; i ++) {
		containers[i].onmousedown = UISelectionX.start ;		
	}	
	UISelectionX.extraLeft = 6 ;
	UISelectionX.viewType = "UIWeekView" ;
} ;

eXo.calendar.UIWeekView = new UIWeekView() ;
eXo.calendar.UIHorizontalResize = new UIHorizontalResize() ;
