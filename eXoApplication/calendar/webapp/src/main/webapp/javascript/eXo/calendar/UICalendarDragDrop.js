function UICalendarDragDrop () {

}
UICalendarDragDrop.prototype.init = function(evt) {
	var _e = window.event || evt ;
	if(_e.button == 2) return ;
  _e.cancelBubble = true;
  if (_e.preventDefault) {
    _e.preventDefault();
  }
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
	var DragDrop = eXo.core.DragDrop ;
	DragDrop.initCallback = UICalendarDragDrop.initCallback ;
  DragDrop.dragCallback = UICalendarDragDrop.dragCallback ;
  DragDrop.dropCallback = UICalendarDragDrop.dropCallback ;
  var dragObject = this.cloneNode(true) ;
  this.parentNode.appendChild(dragObject) ;
	DragDrop.init(null, dragObject, dragObject, _e) ;
} ;

/**
 * 
 * @param {DragDropEvent} dndEvent
 */
UICalendarDragDrop.prototype.initCallback = function(dndEvent) {
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
	var dragObject = dndEvent.dragObject ;
	dragObject.style.left = eXo.core.Mouse.mousexInPage - eXo.core.Browser.findPosX(dragObject.offsetParent) - 10 + "px" ;
	eXo.core.Browser.setOpacity(dragObject, 50) ;
  dragObject.style.width = eXo.calendar.UIMonthView.cells[0].offsetWidth + "px" ;
  var div = document.createElement("div") ; ;
	div.innerHTML = "<span></span>" ;
	var styles = {
		"position" : "absolute",
		"background" : "rgb(237,237,237)"
	} ;
	eXo.calendar.UICalendarPortlet.setStyle(div,styles) ;
	eXo.core.Browser.setOpacity(div, 50) ;
	eXo.calendar.UIMonthView.eventContainer.appendChild(div) ;
} ;

UICalendarDragDrop.prototype.dragCallback = function(evt) {
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
	var dragObject = evt.dragObject ;
	var currentTarget = 
	UICalendarDragDrop.currentTarget = currentTarget ;
	var styles = {
		"top" : currentTarget.offsetTop + 1 + "px",
		"left" : currentTarget.offsetLeft + "px",
		"height" : currentTarget.offsetHeight - 1 + "px",
		"width" : currentTarget.offsetWidth - 1 + "px"
	} ;
	eXo.calendar.UICalendarPortlet.setStyle(UICalendarDragDrop.tmp, styles) ;
} ;

UICalendarDragDrop.prototype.dropCallback = function(evt) {
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
	var dragObject = evt.dragObject ;
	var currentTarget = UICalendarDragDrop.currentTarget ;
	eXo.core.DOMUtil.removeElement(dragObject) ;
	eXo.core.DOMUtil.removeElement(UICalendarDragDrop.tmp) ;
	if (currentTarget) {
		if (currentTarget == UICalendarDragDrop.startCell) return ;
		var actionlink = "" ;
		if (actionlink = currentTarget.getAttribute("actionLink")) {
			var currentDate = currentTarget.getAttribute("startTime") ;
			var eventId = dragObject.getAttribute("eventId") ;
			var calId = dragObject.getAttribute("calId") ;
			var calType = dragObject.getAttribute("calType") ;
			actionlink = actionlink.replace(/objectId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"objectId=" + currentDate) ;
			actionlink = actionlink.replace(/eventId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"eventId=" + eventId) ;
			actionlink = actionlink.replace(/calendarId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"calendarId=" + calId) ;
			actionlink = actionlink.replace(/calType\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"calType=" + calType) ;
			actionlink = actionlink.replace("javascript:","") ;
			currentTarget = null ;
			eval(actionlink) ;
		}
	}
} ;

UICalendarDragDrop.prototype.getStartCell = function(targets, object) {	
	var len = targets.length ;
	var targetStart = null ;
	var objStart = object.getAttribute("startTime") ;
	for(var i = 0 ; i < len ; i ++ ) {
		targetStart = targets[i].getAttribute("startTime") ;
		if(objStart == targetStart) return targets[i] ;
	}
	return false ;
} ;

eXo.calendar.UICalendarDragDrop = new UICalendarDragDrop() ;
//function UICalendarDragDrop () {
//
//}
//UICalendarDragDrop.prototype.init = function(evt) {
//	var _e = window.event || evt ;
//	if(_e.button == 2) return ;
//	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
//	var DragDrop = eXo.core.DragDrop ;
//	DragDrop.initCallback = UICalendarDragDrop.initCallback ;
//  DragDrop.dragCallback = UICalendarDragDrop.dragCallback ;
//  DragDrop.dropCallback = UICalendarDragDrop.dropCallback ;
//  var dragObject = this.cloneNode(true) ;
//  this.parentNode.appendChild(dragObject) ;
//	DragDrop.init(null, dragObject, dragObject, _e) ;
//	UICalendarDragDrop.targets = eXo.calendar.UIMonthView.cells ;
//} ;
//
//UICalendarDragDrop.prototype.initCallback = function(evt) {
//	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
//	var dragObject = evt.dragObject ;
//	dragObject.style.left = eXo.core.Mouse.mousexInPage - eXo.core.Browser.findPosX(dragObject.offsetParent) - 10 + "px" ;
//	dragObject.style.opacity = 0.5 ;
//  dragObject.style.width = eXo.calendar.UIMonthView.cells[0].offsetWidth + "px" ;
//	UICalendarDragDrop.tmp = document.createElement("div") ;
//	UICalendarDragDrop.tmp.innerHTML = "<span></span>" ;
//	UICalendarDragDrop.tmp.style.position = "absolute" ;
//	UICalendarDragDrop.tmp.style.background = "rgb(237,237,237)" ;
//	UICalendarDragDrop.tmp.style.opacity = 0.5 ;
//	eXo.calendar.UIMonthView.eventContainer.appendChild(UICalendarDragDrop.tmp) ;
//} ;
//
//UICalendarDragDrop.prototype.dragCallback = function(evt) {
//	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
//	var dragObject = evt.dragObject ;
//	var len = UICalendarDragDrop.targets.length ;
//	var isTarget = null ;
//	for(var i = 0 ; i < len ; i ++) {
//		isTarget = UICalendarDragDrop.isTarget(UICalendarDragDrop.targets[i], dragObject) ;
//		if(isTarget) {
//			UICalendarDragDrop.tmp.style.top = UICalendarDragDrop.targets[i].offsetTop + 1 + "px" ;
//			UICalendarDragDrop.tmp.style.left = UICalendarDragDrop.targets[i].offsetLeft + "px" ;
//			UICalendarDragDrop.tmp.style.height = UICalendarDragDrop.targets[i].offsetHeight - 1 + "px" ;
//			UICalendarDragDrop.tmp.style.width = UICalendarDragDrop.targets[i].offsetWidth - 1 + "px" ;
//			UICalendarDragDrop.currentTarget = UICalendarDragDrop.targets[i] ;
//		}
//	}
//	
//} ;
//
//UICalendarDragDrop.prototype.dropCallback = function(evt) {
//	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
//	var dragObject = evt.dragObject ;
//	eXo.core.DOMUtil.removeElement(dragObject) ;
//	eXo.core.DOMUtil.removeElement(UICalendarDragDrop.tmp) ;
//	if (UICalendarDragDrop.currentTarget) {
//		if (UICalendarDragDrop.currentTarget.className == "CalendarContentDisable") return ;
//	var actionlink = "" ;
//	if (actionlink = UICalendarDragDrop.currentTarget.getAttribute("actionLink")) {
//		var currentDate = UICalendarDragDrop.currentTarget.getAttribute("startTime") ;
//		var eventId = dragObject.getAttribute("eventId") ;
//		var calId = dragObject.getAttribute("calId") ;
//		var calType = dragObject.getAttribute("calType") ;
//		actionlink = actionlink.replace(/objectId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"objectId=" + currentDate) ;
//		actionlink = actionlink.replace(/eventId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"eventId=" + eventId) ;
//		actionlink = actionlink.replace(/calendarId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"calendarId=" + calId) ;
//		actionlink = actionlink.replace(/calType\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"calType=" + calType) ;
//		actionlink = actionlink.replace("javascript:","") ;
//		UICalendarDragDrop.currentTarget = null ;
//		eval(actionlink) ;
//	}
//	}
//} ;
//
//UICalendarDragDrop.prototype.isTarget = function(target, object) {	
//	object.x = eXo.core.Browser.findPosX(object) ;
//	object.y = eXo.core.Browser.findPosY(object) ;
//	target.x = eXo.core.Browser.findPosX(target) ;
//	target.y = eXo.core.Browser.findPosY(target) ;
//	if ((object.x > target.x) && (object.x < (target.x + target.offsetWidth)) && (object.y > target.y) && (object.y < (target.y + target.offsetHeight))) {
//		return true ;
//	} else {
//		return false ;
//	}
//} ;
//
//eXo.calendar.UICalendarDragDrop = new UICalendarDragDrop() ;