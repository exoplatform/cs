function UIContextMenuChatbar(){
}
UIContextMenuChatbar.prototype = eXo.webui.UIContextMenu;
UIContextMenuChatbar.prototype.portletCssClass = "UIChatBarPortlet" ; 
UIContextMenuChatbar.prototype.chatbarPortletId = "UIChatBarPortlet";
UIContextMenuChatbar.prototype.chatusername = '';
UIContextMenuChatbar.prototype.templateAreaId="templateArea";

UIContextMenuChatbar.prototype.setup = function(evt) {
	var _e = window.event || evt ;
	var type = _e.type ;
	if(type == "mouseover") document.oncontextmenu = eXo.webui.UIContextMenuChatbar.show;
	if(type == "mouseout") document.oncontextmenu = function() { return true ;} ;
} ;

UIContextMenuChatbar.prototype.show = function(evt) {
	var _e = window.event || evt
	var UIContextMenu = eXo.webui.UIContextMenuChatbar;
	UIContextMenu.attachedElement = UIContextMenu.getSource(_e) ;
	var chatusername = UIContextMenu.attachedElement.getAttribute('chatusername');
    if(UIContextMenu.chatusername != chatusername) return false;
	var menuPos = {
		"x": eXo.core.Browser.findMouseXInPage(_e) ,
		"y": eXo.core.Browser.findMouseYInPage(_e)
	} ;
	var menuElementId = UIContextMenu.getMenuElementId() ;
	var currentPortlet = eXo.core.DOMUtil.findAncestorById(UIContextMenu.attachedElement, UIContextMenu.chatbarPortletId);
	if (menuElementId && currentPortlet != null) {
		UIContextMenu.menuElement = eXo.core.DOMUtil.findDescendantById(currentPortlet, menuElementId) ;
		var callback = UIContextMenu.getCallback(UIContextMenu.menuElement) ;
		if(callback) {
			callback = callback + "(_e)" ;
			eval(callback) ;
		}
		eXo.core.DOMUtil.listHideElements(UIContextMenu.menuElement) ;
		var ln = eXo.core.DOMUtil.hideElementList.length ;
		if (ln != null && ln > 0) {
			for (var i = 0; i < ln; i++) {
				if(eXo.core.DOMUtil.hideElementList[i] != null)
					eXo.core.DOMUtil.hideElementList[i].style.display = "none" ;
			}
		}
		var chatmessagepopup = document.getElementById(menuElementId);
		if(!chatmessagepopup) return; 
			UIContextMenu.swapMenu(chatmessagepopup, menuPos,_e) ;
		if(!UIContextMenu.menuElement) return false;
		UIContextMenu.menuElement.onmouseover = UIContextMenu.autoHideMessageChatMenuPopup ;
		UIContextMenu.menuElement.onmouseout = UIContextMenu.autoHideMessageChatMenuPopup ;		
		return false ;
	}
	return UIContextMenu.getReturnValue() ;
} ;

UIContextMenuChatbar.prototype.autoHideMessageChatMenuPopup = function(evt) {
  var _e = window.event || evt ;
  var eventType = _e.type ;
  if (eventType == 'mouseout' && (this.style.display != "none")) {
    eXo.cs.Utils.contextMenuTimeout = window.setTimeout("document.getElementById('" + this.id + "').style.display='none'", 1000) ;
  } else {
    if (eXo.cs.Utils.contextMenuTimeout) {
      window.clearTimeout(eXo.cs.Utils.contextMenuTimeout) ;    
      eXo.cs.Utils.contextMenuTimeout.timeout = null ;    
    }
  }
};

eXo.webui.UIContextMenuChatbar = new UIContextMenuChatbar() ;