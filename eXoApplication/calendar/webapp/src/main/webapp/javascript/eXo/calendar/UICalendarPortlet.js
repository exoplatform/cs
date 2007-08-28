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
	uiPopupCategory.style.display = "block" ;
	uiPopupCategory.style.top = obj.offsetTop + fixIETop - contentContainer.scrollTop + "px" ;
	uiPopupCategory.style.left = obj.offsetLeft - contentContainer.scrollLeft + "px" ;

	eXo.core.DOMUtil.listHideElements(uiPopupCategory) ;
} ;
eXo.calendar.UICalendarPortlet = new UICalendarPortlet() ;