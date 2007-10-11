function UICalendarDragDrop () {
	this.classes = new Array() ;
	this.orginalElement = null ;
	this.tmpElement = null ;
}
function mouseCoords(ev){
	if(ev.pageX || ev.pageY){
		return {x:ev.pageX, y:ev.pageY};
	}
	return {
		x:ev.clientX + document.body.scrollLeft - document.body.clientLeft,
		y:ev.clientY + document.body.scrollTop  - document.body.clientTop
	};
}
UICalendarDragDrop.prototype.init = function() {
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
	UICalendarDragDrop.container = document.getElementById("UIMonthViewGrid") ;
	var dragObjects = eXo.core.DOMUtil.findDescendantsByClass(UICalendarDragDrop.container, "div", "EventBoxes") ;
	var len = dragObjects.length ;
	if (!dragObjects || (len < 1)) return ;
	for (var i = 0 ; i < len ; i ++) {
		dragObjects[i].onmousedown = UICalendarDragDrop.dragStart ;		
	}
	UICalendarDragDrop.targetObjects = eXo.core.DOMUtil.findDescendantsByTagName(UICalendarDragDrop.container, "td") ;	
} ;

UICalendarDragDrop.prototype.dragStart = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	if (_e.button == 2) return ;
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
	var dragElement = this ;
	UICalendarDragDrop.extraY = (UICalendarDragDrop.container.parentNode.scrollTop) ? (UICalendarDragDrop.container.parentNode.scrollTop + dragElement.offsetHeight): 0 ;
	var newElement = dragElement.cloneNode(true) ;
	var RowContainerDay = eXo.core.DOMUtil.findAncestorByClass(dragElement,"RowContainerDay") ;
	var scrollTop = (RowContainerDay)? RowContainerDay.scrollTop : 0 ;	
	newElement.style.position = "absolute" ;
	newElement.style.border = "solid 1px red" ;
	newElement.style.width = dragElement.offsetWidth + "px" ;
	newElement.style.left = eXo.core.Browser.findPosX(dragElement) + "px" ;
	newElement.style.top = eXo.core.Browser.findPosY(dragElement) - scrollTop + "px" ;
	UICalendarDragDrop.orginalElement = dragElement ;
	UICalendarDragDrop.tmpElement = newElement ;
	UICalendarDragDrop.blockElement = eXo.core.DOMUtil.findAncestorByTagName(dragElement, "td") ;
	document.body.appendChild(newElement) ;
	UICalendarDragDrop.offset = UICalendarDragDrop.getOffset(dragElement, _e) ;
	UICalendarDragDrop.container.onmousemove = UICalendarDragDrop.drag ;
	UICalendarDragDrop.container.onmouseup = UICalendarDragDrop.drop ;
} ;
UICalendarDragDrop.prototype.drag = function(evt) {
	try{		
	var _e = window.event || evt ;
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;	
	var tmpElement = UICalendarDragDrop.tmpElement ;
	tmpElement.style.left = (mouseCoords(_e).x - UICalendarDragDrop.offset.x) + "px" ;
	tmpElement.style.top = (mouseCoords(_e).y - UICalendarDragDrop.offset.y) + "px" ;
	UICalendarDragDrop.getTarget(_e, tmpElement) ;
	}catch(e){window.status = "Message: " + e.message ;}
} ;
UICalendarDragDrop.prototype.drop = function(evt) {
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
	if (!UICalendarDragDrop.tmpElement) return ;
	UICalendarDragDrop.currentTarget.style.background = "#ffffff" ;
	UICalendarDragDrop.tmpElement.removeAttribute("style") ;
	UICalendarDragDrop.tmpElement.onmousedown = UICalendarDragDrop.dragStart ;
	var dayContent = eXo.core.DOMUtil.findFirstDescendantByClass(UICalendarDragDrop.currentTarget,"div", "DayContent") ;
	var dayContentContainer = eXo.core.DOMUtil.findFirstChildByClass(dayContent, "div", "DayContentContainer") ;
	if (dayContentContainer) {		
		dayContentContainer.appendChild(UICalendarDragDrop.tmpElement) ;
	} else{
		var div = document.createElement("div") ;
		div.style.height = "75px" ;
		div.style.overflow = "hidden" ;
		div.className = "DayContentContainer" ;
		div.appendChild(UICalendarDragDrop.tmpElement) ;
		dayContent.appendChild(div) ;
	}
	if (UICalendarDragDrop.orginalElement.parentNode) UICalendarDragDrop.orginalElement.parentNode.removeChild(UICalendarDragDrop.orginalElement) ;	
	UICalendarDragDrop.tmpElement = null ;
	UICalendarDragDrop.orginalElement = null ;
	UICalendarDragDrop.extraY = null ;
	UICalendarDragDrop.blockElement = null ;
	UICalendarDragDrop.offset = null ;
	UICalendarDragDrop.container.onmousemove = null ;
	var actionlink = "" ;
	if (actionlink = UICalendarDragDrop.currentTarget.getAttribute("actionLink")) {
		var currentDate = UICalendarDragDrop.currentTarget.getAttribute("currentDate") ;
		actionlink = actionlink.replace(/objectId\s*=\s*.*(?=&|'|\")/,"objectId="+currentDate).replace("javascript:","") ;
		UICalendarDragDrop.currentTarget = null ;
		eval(actionlink) ;
	}
	UICalendarDragDrop.currentTarget = null ;
} ;

UICalendarDragDrop.prototype.getOffset = function(object, evt) {
	var RowContainerDay = eXo.core.DOMUtil.findAncestorByClass(object,"RowContainerDay") ;
	var scrollTop = (RowContainerDay)? RowContainerDay.scrollTop : 0 ;	
	return {
		"x": (mouseCoords(evt).x - eXo.core.Browser.findPosX(object)) ,
		"y": (mouseCoords(evt).y - eXo.core.Browser.findPosY(object) + scrollTop)
	} ;
} ;

UICalendarDragDrop.prototype.getTarget = function(evt, object) {
	var src = evt.srcElement || evt.target ;
	var target = eXo.core.DOMUtil.findAncestorByTagName(src, "td") ;
	var UICalendarDragDrop = eXo.calendar.UICalendarDragDrop ;
	var len = UICalendarDragDrop.targetObjects.length ;
	for(var i = 0 ; i < len ; i ++) {		
		if (UICalendarDragDrop.isTarget(UICalendarDragDrop.targetObjects[i],object)) {
			UICalendarDragDrop.targetObjects[i].style.background = "#cccccc" ;
			UICalendarDragDrop.currentTarget = UICalendarDragDrop.targetObjects[i] ;
		} else {
			UICalendarDragDrop.targetObjects[i].style.background = "#ffffff" ;
			continue ;
		}
	}
} ;

UICalendarDragDrop.prototype.isTarget = function(target, object) {
	object.x = eXo.core.Browser.findPosX(object) ;
	object.y = eXo.core.Browser.findPosY(object) ;
	target.x = eXo.core.Browser.findPosX(target) ;
	target.y = eXo.core.Browser.findPosY(target) - eXo.calendar.UICalendarDragDrop.extraY ;
	if ((object.x > target.x) && (object.x < (target.x + target.offsetWidth)) && (object.y > target.y) && (object.y < (target.y + target.offsetHeight))) {
		return true ;
	} else {
		return false ;
	}
} ;

eXo.calendar.UICalendarDragDrop = new UICalendarDragDrop() ;