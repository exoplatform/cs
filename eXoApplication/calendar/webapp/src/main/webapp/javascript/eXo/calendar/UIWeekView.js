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
		var top = parseInt(UIWeekView.items[i].getAttribute("starttime")) + 29 ; // Remove 29 from this line when template correcting is done
		var height = parseInt(UIWeekView.items[i].getAttribute("endtime")) - parseInt(UIWeekView.items[i].getAttribute("starttime")) ;
		UIWeekView.items[i].onmousedown = UIWeekView.dragStart ;
		try{
			UIWeekView.items[i].style.top = top  + "px" ;
			UIWeekView.items[i].style.height = height  + "px" ;
		} catch(e) { window.status = "Error : " + e.message ; }
	}
	UIWeekView.cols = eXo.core.DOMUtil.findDescendantsByClass(UIWeekView.container, "td", "cols") ;	
	var len = UIWeekView.cols.length ;
	for(var i = 0 ; i < len ; i ++) {
		if (!eXo.core.DOMUtil.findChildrenByClass(UIWeekView.cols[i], "div", "EventContainerBoder")) return ;
		UIWeekView.showInCol(UIWeekView.cols[i]) ;
	}
} ;
UIWeekView.prototype.showInCol = function(obj) {
	var items = eXo.core.DOMUtil.getChildrenByTagName(obj, "div") ;
	var len = items.length ;
	if (len <= 0) return ;
	var UIWeekView = eXo.calendar.UIWeekView ;
	var posX = eXo.core.Browser.findPosXInContainer(obj, UIWeekView.container) ;
	var left = parseFloat(posX/UIWeekView.container.offsetWidth)*100 ;
	var width = parseFloat((obj.offsetWidth - 2)/UIWeekView.container.offsetWidth)*100 ;
	items = eXo.calendar.UICalendarPortlet.sortByAttribute(items, "starttime") ;
	eXo.calendar.UICalendarPortlet.adjustWidth(items, width, left) ;
}

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
	var delta = end - start  ;
	var currentStart = dragElement.offsetTop ;
	var currentEnd = currentStart + delta ;	
	var actionLink =	eXo.calendar.UICalendarPortlet.adjustTime(currentStart, currentEnd, dragElement) ;
	var currentDate = eXo.calendar.UIWeekView.currentCol.getAttribute("starttime").toString() ;
	currentDate = currentDate.substring(0,currentDate.indexOf(" ")) ;
	actionLink = actionLink.toString().replace(/'\s*\)/,"&currentDate=" + currentDate + "&calType=" + calType + "')") ;
	//alert(actionLink) ; return ;
	eval(actionLink) ;	
}

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
	for(var i = 0 ; i < len ; i ++) {
		colX = Browser.findPosX(UIWeekView.cols[i]) ;
		if ((mouseX > colX) && (mouseX < colX + UIWeekView.cols[i].offsetWidth)){
			return UIWeekView.currentCol = UIWeekView.cols[i] ;
		}
	}
	
	return false ;
} ;

eXo.calendar.UIWeekView = new UIWeekView() ;
