eXo.require('eXo.webui.UIContextMenu') ;
function UIContactPortlet() {
	
}
UIContactPortlet.prototype.showContextMenu = function() {
	var UIContextMenu = eXo.webui.UIContextMenu ;//eXo.contact.ContextMenu ;
	var config = {
		'preventDefault':false, 
		'preventForms':false
	} ;	
	UIContextMenu.init(config) ;
	UIContextMenu.attach('UIContactList', 'UIContactListPopuMenu') ;
	UIContextMenu.attach('ItemList', 'UIAddressBookPopupMenu') ;	
	UIContextMenu.attach('TagList', 'UITagPopupMenu') ;
} ;

UIContactPortlet.prototype.contactCallback = function(evt) {
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var src = _e.srcElement || _e.target ;
	var tr = eXo.core.DOMUtil.findAncestorByTagName(src, "tr") ;
	var checkbox = eXo.core.DOMUtil.findFirstDescendantByClass(tr, "input", "checkbox") ;
	var id = checkbox.name ;
	eXo.webui.UIContextMenu.changeAction(UIContextMenu.menuElement, id) ;
} ;
UIContactPortlet.prototype.addressBookCallback = function(evt) {
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var _e = window.event || evt ;	
	var src = _e.srcElement || _e.target ;
	var a = (src.nodeName.toLowerCase() == "a") ? src : eXo.core.DOMUtil.findFirstChildByClass(src, "a", "IconHolder") ;	
	eXo.webui.UIContextMenu.changeAction(UIContextMenu.menuElement, a.id) ;
	var isPublic = a.getAttribute("isPublic") ;
	var DOMUtil = eXo.core.DOMUtil ;
	var menuItems = DOMUtil.findDescendantsByClass(UIContextMenu.menuElement, "div", "ItemIcon") ;
	var itemLength = menuItems.length ;
	if (isPublic && (isPublic.toLowerCase() == "true")) {
		for(var i = 0 ; i < itemLength ; i ++) {
			if (DOMUtil.hasClass(menuItems[i],"ShareIcon") || DOMUtil.hasClass(menuItems[i],"EditActionIcon") || DOMUtil.hasClass(menuItems[i],"DeleteIcon")) {
				if (menuItems[i].parentNode.getAttribute("oldHref")) break ;
				menuItems[i].parentNode.setAttribute("oldHref", menuItems[i].parentNode.href) ;
				menuItems[i].parentNode.href = "javascript: void(0) ;" ;
				menuItems[i].parentNode.setAttribute("oldColor", DOMUtil.getStyle(menuItems[i].parentNode, "color")) ;
				menuItems[i].parentNode.style.color = "#cccccc" ;
			}
		}
	} else {
		for(var i = 0 ; i < itemLength ; i ++) {
			if (DOMUtil.hasClass(menuItems[i],"ShareIcon") || DOMUtil.hasClass(menuItems[i],"EditActionIcon") || DOMUtil.hasClass(menuItems[i],"DeleteIcon")) {
				if (!menuItems[i].parentNode.getAttribute("oldHref")) break ;
				menuItems[i].parentNode.href = menuItems[i].parentNode.getAttribute("oldHref") ;
				menuItems[i].parentNode.style.color = menuItems[i].parentNode.getAttribute("oldColor") ;
				menuItems[i].parentNode.removeAttribute("oldColor") ;
				menuItems[i].parentNode.removeAttribute("oldHref") ;
			}
		}
	}
} ;
UIContactPortlet.prototype.tagCallback = function(evt) {
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var src = _e.srcElement || _e.target ;
	src = (src.nodeName.toLowerCase() == "div")? src : src.parentNode ;
	var tagName = src.getAttribute("tagName") ;
	eXo.webui.UIContextMenu.changeAction(UIContextMenu.menuElement, tagName) ;
} ;
UIContactPortlet.prototype.printpreview = function (obj){
	var DOMUtil = eXo.core.DOMUtil ;
	var UIPortalApplication = document.getElementById("UIPortalApplication") ;
	var UIContactPreview = DOMUtil.findAncestorByClass(obj, "UIContactPreview") ;
	var div = document.createElement("div") ;
	
	div.className = "UIContactPortlet" ;
	div.appendChild(UIContactPreview.cloneNode(true)) ;
	UIPortalApplication.style.display = "none" ;
	var bg = document.body.style.background ;
	document.body.style.background = "transparent" ;
	document.body.appendChild(div) ;
	var button = DOMUtil.findDescendantsByClass(div, "a", "ActionButton") ;
	button[0].onclick = function(){
		document.body.removeChild(div) ;
		UIPortalApplication.style.display = "block" ;
		document.body.style.background = bg ;
	}
	button[1].onclick = function(){
		window.print() ;
	}
}
eXo.contact.UIContactPortlet = new UIContactPortlet() ;