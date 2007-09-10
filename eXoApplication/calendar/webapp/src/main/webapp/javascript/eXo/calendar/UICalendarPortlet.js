function UICalendarPortlet() {
	
}
/* for general calendar */
UICalendarPortlet.prototype.show = function(obj, evt) {
	if(!evt) evt = window.event ;
	evt.cancelBubble = true ;
	var uiCalendarPortlet =	document.getElementById("UICalendarPortlet") ;
	var contentContainer = eXo.core.DOMUtil.findFirstDescendantByClass(uiCalendarPortlet, "div", "ContentContainer") ;
	var	uiPopupCategory = eXo.core.DOMUtil.findNextElementByTagName(contentContainer,  "div") ;
	
	if (!uiPopupCategory) return ;
	
	var fixIETop = (navigator.userAgent.indexOf("MSIE") >= 0) ? 2.5*obj.offsetHeight : obj.offsetHeight ;
	this.changeAction(uiPopupCategory, obj.id) ;
	uiPopupCategory.style.display = "block" ;
	uiPopupCategory.style.top = obj.offsetTop + fixIETop - contentContainer.scrollTop + "px" ;
	uiPopupCategory.style.left = obj.offsetLeft - contentContainer.scrollLeft + "px" ;
	
	eXo.core.DOMUtil.listHideElements(uiPopupCategory) ;
} ;

UICalendarPortlet.prototype.changeAction = function(obj, id) {
	var actions = eXo.core.DOMUtil.findDescendantsByTagName(obj, "a") ;
	var len = actions.length ;
	var href = "" ;
	var pattern = /\=[a-zA-Z0-9]*\'/ ;
	for(var i = 0 ; i < len ; i++) {
		href = String(actions[i].href) ;
		if (!pattern.test(href)) continue ;
		actions[i].href = href.replace(pattern,"="+id+"'") ;
	}
}

/* for event */

UICalendarPortlet.prototype.init = function() {
	var rowContainerDay = document.getElementById("RowContainerDay") ;
	this.viewer = eXo.core.DOMUtil.findAncestorByClass(rowContainerDay, "EventDayContainer") ;
	//var tdTime = eXo.core.DOMUtil.findFirstDescendantByClass(rowContainerDay, "td", "TdTime") ;
	this.step = 60 ;//(document.all)?(tdTime.offsetHeight+1)*2 : tdTime.offsetHeight*2 ;
	this.extra = 0 ;//(document.all)? 42: 40 ;
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
	//element.style.left = this.extra + "px" ;
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

UICalendarPortlet.prototype.adjustWidth = function(totalWidth) {
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
	eXo.calendar.UICalendarPortlet.init() ;
	var el = UICalendarPortlet.getElements() ;
	var marker = null ;
	for(var i = 0 ; i < el.count ; i ++ ) {
		UICalendarPortlet.setSize(el.children[i]) ;
		el.children[i].onmousedown = UICalendarPortlet.initDND ;
		marker = eXo.core.DOMUtil.findFirstChildByClass(el.children[i], "div", "ResizeEventContainer") ;
		marker.onmousedown = UICalendarPortlet.initResize ;		
	}
	UICalendarPortlet.adjustWidth(890) ;
} ;

/* for resizing event box */
UICalendarPortlet.prototype.initResize = function(evt) {	
	eXo.calendar.UICalendarPortlet.resize(evt, this) ;
} ;
UICalendarPortlet.prototype.resize = function(evt, markerobj) {
	_e = (window.event) ? window.event : evt ;
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
	document.onmouseup = UICalendarPortlet.resizeCallBack ;
} ;

UICalendarPortlet.prototype.adjustHeight = function(evt) {
	_e = (window.event) ? window.event : evt ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var delta = _e.clientY - UICalendarPortlet.posY ;
	var height = UICalendarPortlet.beforeHeight + delta ;
	var containerHeight = UICalendarPortlet.eventContainerHeight + delta ;
	if (height <= (eXo.calendar.UICalendarPortlet.step/2)) return ;
		UICalendarPortlet.eventBox.style.height = height + "px" ;
		UICalendarPortlet.eventContainer.style.height = containerHeight + "px" ;
} ;

UICalendarPortlet.prototype.resizeCallBack = function(evt) {
	_e = window.event || evt ;
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
	//document.onmousedown = null ;
} ;

/* for drag and drop */

UICalendarPortlet.prototype.initDND = function(evt) {
	evt = window.event || evt ;
	evt.cancelBubble = true ;
	var UICalendarPortlet = eXo.calendar.UICalendarPortlet ;
	var dragBlock = this ;
	var clickBlock =  eXo.core.DOMUtil.findFirstChildByClass(dragBlock, "div", "EventContainerBar") ;
	var offsetLeft = dragBlock.offsetLeft ;
	var offsetTop =  dragBlock.offsetTop ;
	var startTime =  parseInt(dragBlock.getAttribute("startTime")) ;
	var endTime =  parseInt(dragBlock.getAttribute("endTime")) ;
	var height = Math.abs(startTime - endTime) ;
	eXo.core.DragDrop.init(null, clickBlock, dragBlock, evt) ;
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
		return str = hour + ":" + min + " AM";
	} else {
		hour = (hour - 12) ;
		if (hour == 0) hour = 12 ;
		return str = hour + ":" + min + " PM";
	} 
} ;

UICalendarPortlet.prototype.adjustTime = function() {
	
} ;

eXo.calendar.UICalendarPortlet = new UICalendarPortlet() ;