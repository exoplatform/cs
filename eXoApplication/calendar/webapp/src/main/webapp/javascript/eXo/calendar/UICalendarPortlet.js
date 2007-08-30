function UICalendarPortlet() {
	
}
//UICalendarPortlet.prototype.showHideElement = function(obj) {
//	if (obj.style.display == "none") {
//		obj.style.display = "block" ;
//	} else {
//		obj.style.display = "none" ;
//	}
//	eXo.core.DOMUtil.listHideElements(obj) ;
//} ;
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
eXo.calendar.UICalendarPortlet = new UICalendarPortlet() ;