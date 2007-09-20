eXo.require('eXo.webui.UIContextMenu') ;

function UICalendarPortlet() {
	
}
/* for general calendar */
UICalendarPortlet.prototype.hide = function() {
	var ln = eXo.core.DOMUtil.hideElementList.length ;
	if (ln > 0) {
		for (var i = 0; i < ln; i++) {
			eXo.core.DOMUtil.hideElementList[i].style.display = "none" ;
		}
	}
} ;

UICalendarPortlet.prototype.showHide = function(obj) {	
	if (obj.style.display != "block") {
		eXo.calendar.UICalendarPortlet.hide() ;
		obj.style.display = "block" ;
		eXo.core.DOMUtil.listHideElements(obj) ;
	} else {
		obj.style.display = "none" ;
	}
} ;
UICalendarPortlet.prototype.show = function(obj, evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;	
	var uiCalendarPortlet =	document.getElementById("UICalendarPortlet") ;
	var contentContainer = eXo.core.DOMUtil.findFirstDescendantByClass(uiCalendarPortlet, "div", "ContentContainer") ;
	var	uiPopupCategory = eXo.core.DOMUtil.findNextElementByTagName(contentContainer,  "div") ;
	
	if (!uiPopupCategory) return ;
	
	var fixIETop = (navigator.userAgent.indexOf("MSIE") >= 0) ? 2.5*obj.offsetHeight : obj.offsetHeight ;
	eXo.webui.UIContextMenu.changeAction(uiPopupCategory, obj.id) ;
	eXo.calendar.UICalendarPortlet.showHide(uiPopupCategory) ;
	uiPopupCategory.style.top = obj.offsetTop + fixIETop - contentContainer.scrollTop + "px" ;
	uiPopupCategory.style.left = obj.offsetLeft - contentContainer.scrollLeft + "px" ;	
	
} ;

UICalendarPortlet.prototype.showAction = function(obj, evt) {
	eXo.webui.UIPopupSelectCategory.show(obj, evt) ;
	if (this.viewer && document.all) {
		//this.viewer.style.visibility = "hidden" ;
		//var uiPopupCategory = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIPopupCategory") ;
		//if (uiPopupCategory.style.display == "none") this.viewer.style.visibility = "visible" ;
		//var board = eXo.core.DOMUtil.findFirstDescendantByClass(this.viewer, "div", "EventBoard") ;
		//board.style.position = "static" ;
	}
//	var uiPopupCategory = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIPopupCategory") ;
} ;

//UICalendarPortlet.prototype.changeAction = function(obj, id) {
//	var actions = eXo.core.DOMUtil.findDescendantsByTagName(obj, "a") ;
//	var len = actions.length ;
//	var href = "" ;
//	var pattern = /objectId\=.*id/ ;
//	for(var i = 0 ; i < len ; i++) {
//		href = String(actions[i].href) ;
//		if (!pattern.test(href)) continue ;
//		actions[i].href = href.replace(pattern,"objectId="+id) ;
//	}
//}

/* for event */

UICalendarPortlet.prototype.init = function() {
	var rowContainerDay = document.getElementById("RowContainerDay") ;
	if (!rowContainerDay) return false ;
	this.viewer = eXo.core.DOMUtil.findFirstDescendantByClass(rowContainerDay, "div", "EventBoardContainer") ;
	this.step = 60 ;
	this.viewer.onmousedown = eXo.calendar.UISelection.init ;//eXo.calendar.UICalendarPortlet.addSelection ;
	return true ;
} ;

UICalendarPortlet.prototype.getElements = function() {
	var elements = eXo.core.DOMUtil.findDescendantsByClass(this.viewer, "div", "EventContainerBorder") ;
	var len = elements.length ;
	var el = {
		
		children: elements,
		count : len
	}
	return el ;
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

UICalendarPortlet.prototype.getInterval = function() {
	var el = eXo.calendar.UICalendarPortlet.getElements() ;
	var bottom = new Array() ;
	var interval = new Array() ;
	for(var i = 0 ; i < el.count ; i ++ ) {
		bottom.push(el.children[i].offsetTop + el.children[i].offsetHeight) ;
		if (bottom[i-1] && (el.children[i].offsetTop > bottom[i-1])) interval.push(i) ;
	}
	interval.unshift(0) ;
	interval.push(el.count) ;
	return interval ;
} ;

UICalendarPortlet.prototype.adjustWidth = function() {
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var inter = UICalendarPortlet.getInterval() ;	
	var el = UICalendarPortlet.getElements() ;
	for(var i = 0 ; i < inter.length ; i ++) {
		var width = "" ;
		var len = (inter[i+1] - inter[i]) ;
		for(var j = inter[i], n = 0 ; j < inter[i+1] ; j++, n ++) {			
			width = Math.floor(100/len) ;
			UICalendarPortlet.setWidth(el.children[j], width) ;
			if (el.children[j-1]&&(len > 1)) el.children[j].style.left = parseInt(el.children[j-1].style.width)*n +  "%" ;
		}
	}
} ;

UICalendarPortlet.prototype.showEvent = function() {
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	if (!UICalendarPortlet.init()) return ;
	var el = UICalendarPortlet.getElements() ;
	var marker = null ;
	for(var i = 0 ; i < el.count ; i ++ ) {
		UICalendarPortlet.setSize(el.children[i]) ;
		el.children[i].onmousedown = UICalendarPortlet.initDND ;
		marker = eXo.core.DOMUtil.findFirstChildByClass(el.children[i], "div", "ResizeEventContainer") ;
		marker.onmousedown = UICalendarPortlet.initResize ;		
	}
	UICalendarPortlet.adjustWidth() ;
} ;

/* for resizing event box */
UICalendarPortlet.prototype.initResize = function(evt) {	
	eXo.calendar.UICalendarPortlet.resize(evt, this) ;
} ;
UICalendarPortlet.prototype.resize = function(evt, markerobj) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	this.posY = _e.clientY ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var marker = (typeof(markerobj) == "string")? document.getElementById(markerobj):markerobj ;
	this.eventBox = eXo.core.DOMUtil.findAncestorByClass(marker,'EventContainerBorder') ;
	this.eventContainer = eXo.core.DOMUtil.findPreviousElementByTagName(marker, "div") ;
	this.posY = _e.clientY ;
	this.beforeHeight = this.eventBox.offsetHeight ;
	this.eventContainerHeight = this.eventContainer.offsetHeight + 2 ;
	document.onmousemove = UICalendarPortlet.adjustHeight ;
	this.eventBox.onmouseup = UICalendarPortlet.resizeCallBack ;
	document.onmouseup = null ;
} ;

UICalendarPortlet.prototype.adjustHeight = function(evt) {
	var _e = window.event || evt ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var delta = _e.clientY - UICalendarPortlet.posY ;
	var height = UICalendarPortlet.beforeHeight + delta ;
	var containerHeight = UICalendarPortlet.eventContainerHeight + delta ;
	if (height <= (eXo.calendar.UICalendarPortlet.step/2)) return ;
		UICalendarPortlet.eventBox.style.height = height + "px" ;
		UICalendarPortlet.eventContainer.style.height = containerHeight + "px" ;
} ;

UICalendarPortlet.prototype.resizeCallBack = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var src = null ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	if (document.all) src = _e.srcElement
	else src = _e.target ;
	var title =  eXo.core.DOMUtil.findPreviousElementByTagName(src, "div") ;
	var delta =	src.offsetHeight - UICalendarPortlet.eventContainerHeight  ;
	var startTime =  parseInt(src.parentNode.getAttribute("startTime")) ;
	var endTime =  parseInt(src.parentNode.getAttribute("endTime")) ;
	var currentEndTime = endTime + delta + 2 ;
	src.parentNode.setAttribute("endTime", currentEndTime) ;
	title.innerHTML = UICalendarPortlet.minutesToHour(startTime) + " - " +  UICalendarPortlet.minutesToHour(currentEndTime) ;
	document.onmousemove = null ;
} ;

/* for drag and drop */

UICalendarPortlet.prototype.initDND = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var dragBlock = this ;
	var clickBlock =  eXo.core.DOMUtil.findFirstChildByClass(dragBlock, "div", "EventContainerBar") ;
	var offsetLeft = dragBlock.offsetLeft ;
	var offsetTop =  dragBlock.offsetTop ;
	var startTime =  parseInt(dragBlock.getAttribute("startTime")) ;
	var endTime =  parseInt(dragBlock.getAttribute("endTime")) ;
	var height = Math.abs(startTime - endTime) ;
	eXo.core.DragDrop.init(null, clickBlock, dragBlock, _e) ;
	eXo.core.DragDrop.initCallback = null ;
	eXo.core.DragDrop.dragCallback = function(dndEvent) {
		dragBlock.style.left = offsetLeft + "px" ;
	}
	eXo.core.DragDrop.dropCallback = function(dndEvent) {
		var delta = offsetTop - dragBlock.offsetTop ;
		var currentStartTime = startTime - delta ;
		var currentEndTime = currentStartTime +  height ;
		dragBlock.setAttribute('startTime', currentStartTime) ;
		dragBlock.setAttribute('endTime', currentEndTime) ;
		clickBlock.innerHTML = UICalendarPortlet.minutesToHour(currentStartTime) + " - " + UICalendarPortlet.minutesToHour(currentEndTime) ;
	}
} ;

/* fo adjusting time */

UICalendarPortlet.prototype.minutesToHour = function(mins) {
	var min = mins%60 ;
	var hour = Math.floor(mins/60) ;
	var str = "" ;
	if (min < 10) min = "0" + min
	if (hour < 12) {
		if (hour == 0) hour = 12 ;
		return str = hour + ":" + min + " AM" ;
	} else {
		hour = (hour - 12) ;
		if (hour == 0) hour = 12 ;
		return str = hour + ":" + min + " PM" ;
	} 
} ;

/* for showing context menu */

UICalendarPortlet.prototype.showContextMenu = function() {	
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var config = {
		'preventDefault':false, 
		'preventForms':false
	} ;	
	UIContextMenu.init(config) ;
	UIContextMenu.attach(["CalendarContentNomal","CalendarContentDisable"],"UIMonthViewRightMenu") ;	
	UIContextMenu.attach("EventOnDayLabel","UIMonthViewEventRightMenu") ;	
} ;

/* for selection creation */

function UISelection() {
	
}

UISelection.prototype.init = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var UISelection = eXo.calendar.UISelection ;
	var Container = this ;
	var selection = document.getElementById("selection") ;
	if (selection) Container.removeChild(selection) ;
	UISelection.selection = document.createElement("div") ;
	UISelection.selection.className = "selection" ;
	UISelection.selection.setAttribute("id", "selection") ;
	UISelection.selectionY = eXo.core.Browser.findMouseRelativeY(Container, _e) ;
	UISelection.selection.innerHTML = "<span></span>" ;
	Container.appendChild(UISelection.selection) ;
	Container.onmousemove = UISelection.resize ;
	Container.onmouseup = UISelection.clear ;
} ;

UISelection.prototype.resize = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;	
	var UISelection = eXo.calendar.UISelection ;
	var delta = UISelection.selectionY - eXo.core.Browser.findMouseRelativeY(this, _e) ;
	if (delta < 0) {
		UISelection.selection.style.top = UISelection.selectionY + "px" ;
		UISelection.selection.style.height = Math.abs(delta) + "px" ;
	} else {
		UISelection.selection.style.bottom = UISelection.selectionY + "px" ;
		UISelection.selection.style.top = eXo.core.Browser.findMouseRelativeY(this, _e) + "px" ;
		UISelection.selection.style.height = Math.abs(delta) + "px" ;
	}
//	window.status = UISelection.selection.offsetParent.className + " | " + eXo.core.Browser.findMouseRelativeY(this, _e);
} ;
UISelection.prototype.clear = function() {	
	this.onmousemove = null ;
} ;

eXo.calendar.UICalendarPortlet = new UICalendarPortlet() ;
eXo.calendar.UISelection = new UISelection() ;