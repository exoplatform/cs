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
}
UIWeekView.prototype.init = function(evt) {
	var UIWeekView = eXo.calendar.UIWeekView ;
	UIWeekView.container = document.getElementById("UIWeekViewGrid") ;
	UIWeekView.items = eXo.core.DOMUtil.findDescendantsByClass(UIWeekView.container, "div", "EventContainerBoder") ;
	for(var i = 0 ; i < eXo.calendar.UIWeekView.items.length ; i ++){
		UIWeekView.items[i].onmousedown = UIWeekView.dragStart ;
	}
	UIWeekView.cols = eXo.core.DOMUtil.findDescendantsByClass(UIWeekView.container, "td", "cols") ;	
	var len = UIWeekView.cols.length ;
	for(var i = 0 ; i < len ; i ++) {
		UIWeekView.showInCol(UIWeekView.cols[i]) ;
	}
} ;
UIWeekView.prototype.showInCol = function(obj) {
	var items = eXo.core.DOMUtil.getChildrenByTagName(obj, "div") ;
	var ilen = items.length ;
	var clen = eXo.calendar.UIWeekView.cols.length ;
	var width = parseFloat(90/clen)/ilen ;
	var flag = new Array() ;
  for(var i = 0 ; i < ilen ; i ++ ) {
    if (items[i-1]) {
      if (items[i].offsetTop > (items[i-1].offsetTop) + items[i-1].offsetHeight) {
        flag.push(i) ;
      }
    }
  }
}
UIWeekView.prototype.dragStart = function(evt) {
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	UIWeekView.dragElement = this ;
	UIWeekView.objectOffsetLeft = eXo.core.Browser.findPosX(UIWeekView.dragElement) ;
	UIWeekView.offset = UIWeekView.getOffset(UIWeekView.dragElement, _e) ;
	UIWeekView.containerOffset = {
		"x" : eXo.core.Browser.findPosX(UIWeekView.container.parentNode),
		"y" : eXo.core.Browser.findPosY(UIWeekView.container.parentNode)
	}
	UIWeekView.dragElement.style.position = "absolute" ;
	UIWeekView.dragElement.style.left = UIWeekView.mousePos(_e).x - UIWeekView.offset.x - UIWeekView.containerOffset.x + "px" ;
	UIWeekView.dragElement.style.top = UIWeekView.mousePos(_e).y - UIWeekView.offset.y - UIWeekView.containerOffset.y + "px" ;
	document.onmousemove = UIWeekView.drag ;
	document.onmouseup = UIWeekView.drop ;
} ;
UIWeekView.prototype.drag = function(evt) {
	var _e = window.event || evt ;
	var src = _e.srcElement || _e.target ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	UIWeekView.dragElement.style.left = UIWeekView.mousePos(_e).x - UIWeekView.offset.x - UIWeekView.containerOffset.x + "px" ;
	UIWeekView.dragElement.style.top = UIWeekView.mousePos(_e).y - UIWeekView.offset.y - UIWeekView.containerOffset.y + "px" ;
} ;
UIWeekView.prototype.drop = function(evt) {
	var _e = window.event || evt ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	if (!UIWeekView.isCol(_e)) return ;
	var currentCol = UIWeekView.currentCol ;
	var posX = eXo.core.Browser.findPosXInContainer(UIWeekView.currentCol, UIWeekView.container.parentNode) ;
	UIWeekView.dragElement.style.left = posX + "px" ;
	UIWeekView.currentCol.appendChild(UIWeekView.dragElement) ;
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
	for(var i = 0 ; i < len ; i ++) {
		colX = Browser.findPosX(UIWeekView.cols[i]) ;
		if ((mouseX > colX) && (mouseX < colX + UIWeekView.cols[i].offsetWidth)){
			return UIWeekView.currentCol = UIWeekView.cols[i] ;
		}
	}
	
	return false ;
} ;
//UIWeekView.prototype.isTarget = function(target, object) {
//	var Browser = eXo.core.Browser ;
//	object.x = Browser.findPosX(object) ;
//	object.y = Browser.findPosY(object) ;
//	target.x = Browser.findPosX(target) ;
//	target.y = Browser.findPosY(target) ;
//	if ((object.x > target.x) && (object.x < (target.x + target.offsetWidth)) && (object.y > target.y) && (object.y < (target.y + target.offsetHeight))) {
//		return true ;
//	} else {
//		return false ;
//	}
//} ;

eXo.calendar.UIWeekView = new UIWeekView() ;
