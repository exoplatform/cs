function UIContextMenuCon(){
}
UIContextMenuCon.prototype = new UIContextMenu() ;
UIContextMenuCon.prototype.portletCssClass = "UIContactPortlet" ;
UIContextMenuCon.prototype.setup = function(evt) {
	var _e = window.event || evt ;
	var type = _e.type ;
	if(type == "mouseover") document.oncontextmenu = eXo.webui.UIContextMenuCon.show;
	if(type == "mouseout") document.oncontextmenu = function() { return true ;} ;
} ;

UIContextMenuCon.prototype.show = function(evt) {
	var _e = window.event || evt
	var UIContextMenu = eXo.webui.UIContextMenuCon ;
	UIContextMenu.attachedElement = UIContextMenu.getSource(_e) ;
	var menuPos = {
		"x": eXo.core.Browser.findMouseXInPage(_e) ,
		"y": eXo.core.Browser.findMouseYInPage(_e)
	} ;
	var menuElementId = UIContextMenu.getMenuElementId() ;
	var currentPortlet = eXo.core.DOMUtil.findAncestorByClass(UIContextMenu.attachedElement, UIContextMenu.portletCssClass) ;
	if (menuElementId) {
		UIContextMenu.menuElement = eXo.core.DOMUtil.findDescendantById(currentPortlet, menuElementId) ; //document.getElementById(menuElementId) ;
		eXo.core.DOMUtil.listHideElements(UIContextMenu.menuElement) ;
		eXo.core.DOMUtil.cleanUpHiddenElements();
		UIContextMenu.swapMenu(document.getElementById(menuElementId), menuPos,_e) ;
		if(!UIContextMenu.menuElement) return false;
		UIContextMenu.menuElement.onmouseover = UIContextMenu.autoHide ;
		UIContextMenu.menuElement.onmouseout = UIContextMenu.autoHide ;		
		return false ;
	}
	return UIContextMenu.getReturnValue() ;
} ;

eXo.webui.UIContextMenuCon = new UIContextMenuCon() ;