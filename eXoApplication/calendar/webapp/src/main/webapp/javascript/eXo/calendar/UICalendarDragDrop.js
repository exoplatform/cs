function UICalendarDragDrop () {

}
UICalendarDragDrop.prototype.init = function(evt) {
	var _e = window.event || evt ;
	if(_e.button == 2) return ;
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
	var DragDrop = eXo.core.DragDrop ;
	DragDrop.initCallback = UICalendarDragDrop.initCallback ;
  DragDrop.dragCallback = UICalendarDragDrop.dragCallback ;
  DragDrop.dropCallback = UICalendarDragDrop.dropCallback ;
  var dragObject = this.cloneNode(true) ;
  this.parentNode.appendChild(dragObject) ;
	DragDrop.init(null, dragObject, dragObject, _e) ;
	UICalendarDragDrop.targets = eXo.calendar.UIMonthView.cells ;
} ;

UICalendarDragDrop.prototype.initCallback = function(evt) {
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
	var dragObject = evt.dragObject ;
	dragObject.style.left = eXo.core.Mouse.mousexInPage - eXo.core.Browser.findPosX(dragObject.offsetParent) - 10 + "px" ;
	dragObject.style.opacity = 0.5 ;
  dragObject.style.width = eXo.calendar.UIMonthView.cells[0].offsetWidth + "px" ;
	UICalendarDragDrop.tmp = document.createElement("div") ;
	UICalendarDragDrop.tmp.innerHTML = "<span></span>" ;
	UICalendarDragDrop.tmp.style.position = "absolute" ;
	UICalendarDragDrop.tmp.style.background = "rgb(237,237,237)" ;
	UICalendarDragDrop.tmp.style.opacity = 0.5 ;
	eXo.calendar.UIMonthView.eventContainer.appendChild(UICalendarDragDrop.tmp) ;
} ;

UICalendarDragDrop.prototype.dragCallback = function(evt) {
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
	var dragObject = evt.dragObject ;
	var len = UICalendarDragDrop.targets.length ;
	var isTarget = null ;
	for(var i = 0 ; i < len ; i ++) {
		isTarget = UICalendarDragDrop.isTarget(UICalendarDragDrop.targets[i], dragObject) ;
		if(isTarget) {
			UICalendarDragDrop.tmp.style.top = UICalendarDragDrop.targets[i].offsetTop + 1 + "px" ;
			UICalendarDragDrop.tmp.style.left = UICalendarDragDrop.targets[i].offsetLeft + "px" ;
			UICalendarDragDrop.tmp.style.height = UICalendarDragDrop.targets[i].offsetHeight - 1 + "px" ;
			UICalendarDragDrop.tmp.style.width = UICalendarDragDrop.targets[i].offsetWidth - 1 + "px" ;
			UICalendarDragDrop.currentTarget = UICalendarDragDrop.targets[i] ;
		}
	}
	
} ;

UICalendarDragDrop.prototype.dropCallback = function(evt) {
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
	var dragObject = evt.dragObject ;
	eXo.core.DOMUtil.removeElement(dragObject) ;
	eXo.core.DOMUtil.removeElement(UICalendarDragDrop.tmp) ;
	if (UICalendarDragDrop.currentTarget) {
		if (UICalendarDragDrop.currentTarget.className == "CalendarContentDisable") return ;
	var actionlink = "" ;
	if (actionlink = UICalendarDragDrop.currentTarget.getAttribute("actionLink")) {
		var currentDate = UICalendarDragDrop.currentTarget.getAttribute("currentDate") ;
		//var currentDate = UICalendarDragDrop.currentTarget.getAttribute("startTime") ;
		var eventId = dragObject.getAttribute("eventId") ;
		var calId = dragObject.getAttribute("calId") ;
		actionlink = actionlink.replace(/objectId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"objectId=" + currentDate) ;
		actionlink = actionlink.replace(/eventId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"eventId=" + eventId) ;
		actionlink = actionlink.replace(/calendarId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"calendarId=" + calId) ;
		actionlink = actionlink.replace("javascript:","") ;
		UICalendarDragDrop.currentTarget = null ;
		eval(actionlink) ;
	}
	}
} ;
//UICalendarDragDrop.prototype.mouseCoords = function(ev){
//	if(ev.pageX || ev.pageY){
//		return {x:ev.pageX, y:ev.pageY};
//	}
//	return {
//		x:ev.clientX + document.body.scrollLeft - document.body.clientLeft,
//		y:ev.clientY + document.body.scrollTop  - document.body.clientTop
//	};
//}
//UICalendarDragDrop.prototype.init = function(dragObjects) {
//	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
//	UICalendarDragDrop.container = document.getElementById("UIMonthViewGrid") ;
//	//var dragObjects = eXo.core.DOMUtil.findDescendantsByClass(UICalendarDragDrop.container.parentNode.parentNode, "div", "EventBoxes") ;
//	var len = dragObjects.length ;
//	if (!dragObjects || (len < 1)) return ;
//	var checkbox = null ;
//	for (var i = 0 ; i < len ; i ++) {
//		dragObjects[i].onmousedown = UICalendarDragDrop.dragStart ;
//		checkbox = eXo.core.DOMUtil.findFirstDescendantByClass(dragObjects[i], "input", "checkbox") ;
//		if (!checkbox) continue ;
//		checkbox.onmousedown = function (evt) {
//			var _e = window.event || evt ;
//			_e.cancelBubble = true ;
//			UICalendarDragDrop.container.onmousemove = null ;
//			UICalendarDragDrop.container.onmouseup = null ;
//		}
//	}
//	UICalendarDragDrop.targetObjects = eXo.core.DOMUtil.findDescendantsByTagName(UICalendarDragDrop.container, "td") ;	
//} ;
//
//UICalendarDragDrop.prototype.dragStart = function(evt) {
//	var _e = window.event || evt ;
//	_e.cancelBubble = true ;
//	if (_e.button == 2) return ;
//	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
//	UICalendarDragDrop.dragElement = this ;
//	UICalendarDragDrop.extraY = (UICalendarDragDrop.container.parentNode.scrollTop) ? (UICalendarDragDrop.container.parentNode.scrollTop + UICalendarDragDrop.dragElement.offsetHeight): 0 ;
//	var width = 	UICalendarDragDrop.dragElement.offsetWidth
//	UICalendarDragDrop.dragElement.style.position = "absolute" ;
//	UICalendarDragDrop.dragElement.style.width = width + "px" ;
//	UICalendarDragDrop.parentX = eXo.core.Browser.findPosX(UICalendarDragDrop.dragElement.offsetParent) ;
//	UICalendarDragDrop.parentY =  eXo.core.Browser.findPosY(UICalendarDragDrop.dragElement.offsetParent) ;
//	UICalendarDragDrop.blockElement = eXo.core.DOMUtil.findAncestorByTagName(UICalendarDragDrop.dragElement, "td") ;
//	UICalendarDragDrop.offset = UICalendarDragDrop.getOffset(UICalendarDragDrop.dragElement, _e) ;
//	UICalendarDragDrop.container.onmousemove = UICalendarDragDrop.drag ;
//	UICalendarDragDrop.container.onmouseup = UICalendarDragDrop.drop ;
//} ;
//UICalendarDragDrop.prototype.drag = function(evt) {
//	try{
//		var _e = window.event || evt ;
//		var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
//		var dragElement = UICalendarDragDrop.dragElement ;
//		var RowContainerDay = eXo.core.DOMUtil.findAncestorByClass(UICalendarDragDrop.dragElement,"RowContainerDay") ;
//		var scrollTop = (RowContainerDay)? RowContainerDay.scrollTop : 0 ;
//		dragElement.style.left = (UICalendarDragDrop.mouseCoords(_e).x - UICalendarDragDrop.offset.x) - UICalendarDragDrop.parentX + "px" ;
//		dragElement.style.top = (UICalendarDragDrop.mouseCoords(_e).y - UICalendarDragDrop.offset.y) - UICalendarDragDrop.parentY  + scrollTop + "px" ;
//		window.status = "Parent : " + UICalendarDragDrop.dragElement.offsetParent.className ;
//		UICalendarDragDrop.getTarget(_e, dragElement) ;
//	}catch(e){window.status = "Message: " + e.message ;}
//} ;
//UICalendarDragDrop.prototype.drop = function(evt) {
//	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
//	if (!UICalendarDragDrop.currentTarget) {
//		UICalendarDragDrop.container.onmousemove = null ;
//		UICalendarDragDrop.dragElement.removeAttribute("style") ;
//		return ;
//	}
//	UICalendarDragDrop.currentTarget.style.background = "#ffffff" ;
//	UICalendarDragDrop.dragElement.removeAttribute("style") ;
//	UICalendarDragDrop.dragElement.onmousedown = UICalendarDragDrop.dragStart ;
//	var dayContent = eXo.core.DOMUtil.findFirstDescendantByClass(UICalendarDragDrop.currentTarget,"div", "DayContent") ;
//	var dayContentContainer = eXo.core.DOMUtil.findFirstChildByClass(dayContent, "div", "DayContentContainer") ;
//	if (dayContentContainer) {		
//		dayContentContainer.appendChild(UICalendarDragDrop.dragElement) ;
//	} else{
//		var div = document.createElement("div") ;
//		div.style.height = "75px" ;
//		div.style.overflow = "hidden" ;
//		div.className = "DayContentContainer" ;
//		div.appendChild(UICalendarDragDrop.dragElement) ;
//		dayContent.appendChild(div) ;
//	}
//	if (UICalendarDragDrop.blockElement != UICalendarDragDrop.currentTarget) {		
//		var actionlink = "" ;
//		if (actionlink = UICalendarDragDrop.currentTarget.getAttribute("actionLink")) {
//			var currentDate = UICalendarDragDrop.currentTarget.getAttribute("currentDate") ;
//			var eventId = UICalendarDragDrop.dragElement.getAttribute("eventId") ;
//			var calId = UICalendarDragDrop.dragElement.getAttribute("calId") ;
//			actionlink = actionlink.replace(/objectId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"objectId=" + currentDate) ;
//			actionlink = actionlink.replace(/eventId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"eventId=" + eventId) ;
//			actionlink = actionlink.replace(/calendarId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"calendarId=" + calId) ;
//			actionlink = actionlink.replace("javascript:","") ;
//			UICalendarDragDrop.currentTarget = null ;
//			eval(actionlink) ;
//		}
//	}
//	UICalendarDragDrop.extraY = null ;
//	UICalendarDragDrop.blockElement = null ;
//	UICalendarDragDrop.offset = null ;
//	UICalendarDragDrop.container.onmousemove = null ;
//	UICalendarDragDrop.currentTarget = null ;
//} ;
//
//UICalendarDragDrop.prototype.getOffset = function(object, evt) {
//	var RowContainerDay = eXo.core.DOMUtil.findAncestorByClass(object,"RowContainerDay") ;
//	var scrollTop = (RowContainerDay)? RowContainerDay.scrollTop : 0 ;	
//	return {
//		"x": (eXo.calendar.UICalendarDragDrop.mouseCoords(evt).x - eXo.core.Browser.findPosX(object)) ,
//		"y": (eXo.calendar.UICalendarDragDrop.mouseCoords(evt).y - eXo.core.Browser.findPosY(object) + scrollTop)
//	} ;
//} ;
//
//UICalendarDragDrop.prototype.getTarget = function(evt, object) {
//	var src = evt.srcElement || evt.target ;
//	var target = eXo.core.DOMUtil.findAncestorByTagName(src, "td") ;
//	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
//	var len = UICalendarDragDrop.targetObjects.length ;
//	for(var i = 0 ; i < len ; i ++) {		
//		if (UICalendarDragDrop.isTarget(UICalendarDragDrop.targetObjects[i],object)) {
//			UICalendarDragDrop.targetObjects[i].style.background = "#cccccc" ;
//			UICalendarDragDrop.currentTarget = UICalendarDragDrop.targetObjects[i] ;
//		} else {
//			UICalendarDragDrop.targetObjects[i].style.background = "#ffffff" ;
//			continue ;
//		}
//	}
//} ;
//
UICalendarDragDrop.prototype.isTarget = function(target, object) {	
	object.x = eXo.core.Browser.findPosX(object) ;
	object.y = eXo.core.Browser.findPosY(object) ;
	target.x = eXo.core.Browser.findPosX(target) ;
	target.y = eXo.core.Browser.findPosY(target) ;
	if ((object.x > target.x) && (object.x < (target.x + target.offsetWidth)) && (object.y > target.y) && (object.y < (target.y + target.offsetHeight))) {
		return true ;
	} else {
		return false ;
	}
} ;

eXo.calendar.UICalendarDragDrop = new UICalendarDragDrop() ;