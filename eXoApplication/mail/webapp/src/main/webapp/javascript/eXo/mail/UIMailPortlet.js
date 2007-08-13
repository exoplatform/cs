function UIMailPortlet(){
};

UIMailPortlet.prototype.checkAllMessages = function() {
	var tableMsg = document.getElementById("UIListUsers");
	var tableMsgBody = tableMsg.getElementsByTagName("tbody")[0];
	var msgSelectorList = eXo.core.DOMUtil.findDescendantsByClass(tableMsgBody, "input", "checkbox");
	for (var i=0; i<msgSelectorList.length; i++) msgSelectorList[i].checked = !msgSelectorList[i].checked;
};

//UIMailPortlet.prototype.resizeMessageArea = function() {
//	var portlet = document.getElementById("UIMailPortlet");
//	var msgArea = eXo.core.DOMUtil.findFirstDescendantByClass(portlet, "div", "UIMessageArea");
//	//var msgList = document.getElementById("UIListUsers");
//	var resizeZone = eXo.core.DOMUtil.findFirstDescendantByClass(portlet, "div", "ResizeReadingPane");
//	var msgPreview = eXo.core.DOMUtil.findFirstDescendantByClass(portlet, "div", "UIMessagePreview");
//	portlet.onmousemove = function(e) {
//		var posy = eXo.core.Browser.findMouseYInPage(e);
//		var msgAreaTop = msgArea.offsetTop;
//		var msgPrevTop = msgPreview.offsetTop;
//		msgArea.style.height = (posy-2-msgAreaTop) + "px";
//		msgPreview.style.height = (posy+3-msgPrevTop) + "px";
//	};
//	portlet.onmouseup = function(e) {
//		portlet.onmousemove = null;
//	};
//};



eXo.mail.UIMailPortlet = new UIMailPortlet();