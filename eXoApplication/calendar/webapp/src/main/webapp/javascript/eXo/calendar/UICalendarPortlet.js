function UICalendarPortlet() {
	
}
UICalendarPortlet.prototype.show = function(obj, evt) {
	if(!evt) evt = window.event ;
	evt.cancelBubble = true ;	
	var uiCalendarPortlet =	document.getElementById("UICalendarPortlet") ;
	this.contentContainer = eXo.core.DOMUtil.findFirstDescendantByClass(uiCalendarPortlet, "div", "ContentContainer") ;
	this.uiPopupCategory = eXo.core.DOMUtil.findNextElementByTagName(this.contentContainer,  "div") ;
	if (!this.uiPopupCategory) return ;
	if (this.uiPopupCategory.style.display == "none") {
		var fixIETop = (navigator.userAgent.indexOf("MSIE") >= 0) ? 2.5*obj.offsetHeight : obj.offsetHeight ;
		this.uiPopupCategory.style.display = "block" ;
		this.uiPopupCategory.style.top = obj.offsetTop + fixIETop - this.contentContainer.scrollTop + "px" ;
		this.uiPopupCategory.style.left = obj.offsetLeft - this.contentContainer.scrollLeft + "px" ;
	} else {
		this.uiPopupCategory.style.display = "none" ;
	}
	eXo.core.DOMUtil.listHideElements(this.uiPopupCategory) ;
} ;
eXo.calendar.UICalendarPortlet = new UICalendarPortlet() ;