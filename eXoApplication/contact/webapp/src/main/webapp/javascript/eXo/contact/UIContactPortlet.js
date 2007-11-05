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
	if (tr.getAttribute("selectedTag") && (tr.getAttribute("selectedTag").toLowerCase()!="null")) {		
		var moveContactIcon =  eXo.core.DOMUtil.findFirstDescendantByClass(UIContextMenu.menuElement, "div", "MoveContactIcon") ;
		moveContactIcon.parentNode.href = "javascript: void(0) ;" ;
		moveContactIcon.parentNode.style.color = "#cccccc" ;
	}
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
	button[0].href = "#" ;
	button[0].onclick = function(){
		document.body.removeChild(div) ;
		UIPortalApplication.style.display = "block" ;
		document.body.style.background = bg ;
	}
	DOMUtil.findFirstDescendantByClass(button[1], 'div','ButtonMiddle').style.display = "block" ;
	button[2].style.display = "none" ;
} ;

UIContactPortlet.prototype.adddressPrint = function (){
	var DOMUtil = eXo.core.DOMUtil ;
	var UIPortalApplication = document.getElementById("UIPortalApplication") ;
	var UIContactContainer = document.getElementById("UIContactContainer") ;
	var div = document.createElement("div") ;
	div.className = "UIPrintContainer" ;
	div.appendChild(UIContactContainer.cloneNode(true)) ;
	var uiAction = DOMUtil.findFirstDescendantByClass(div, "div", "UIAction") ;
	DOMUtil.addClass(uiAction, "Printable") ;
	UIPortalApplication.style.display = "none" ;
	eXo.contact.UIContactPortlet.pageBackground = document.body.style.background ;
	document.body.style.background = "transparent" ;
	document.body.appendChild(div) ;
} ;

UIContactPortlet.prototype.cancelPrint = function (obj){
	var UIPrintContainer = eXo.core.DOMUtil.findAncestorByClass(obj, "UIPrintContainer") ;
	var UIPortalApplication = document.getElementById("UIPortalApplication") ;
	UIPrintContainer.parentNode.removeChild(UIPrintContainer) ;
	UIPortalApplication.style.display = "block" ;
	document.body.style.background = eXo.contact.UIContactPortlet.pageBackground ;
	eXo.contact.UIContactPortlet.pageBackground = null ;
} ;

UIContactPortlet.prototype.checkLayout = function() {
	try{
		var Browser = eXo.core.Browser ;
		var	display = Browser.getCookie("contdisplaymode") ;
		var	display0 = Browser.getCookie("contdisplaymode0") ;
		var	display1 = Browser.getCookie("contdisplaymode1") ;
		var	layout0 = document.getElementById("UIAddressBooks") ;
		var	layout1 = document.getElementById("UITags") ;
		var	layout3 = document.getElementById("UINavigationContainer") ;
		var workingarea = eXo.core.DOMUtil.findNextElementByTagName(layout3, "div") ;
	}catch(e) {
		alert(e.message) ;
	}
	layout3.style.display = display ;
	if (display == "none") workingarea.style.marginLeft = "0px"	;
	layout0.style.display = display0 ;
	layout1.style.display = display1 ;
} ;

UIContactPortlet.prototype.switchLayout = function(layout) {
	var Browser = eXo.core.Browser ;
	layout = parseInt(layout) ;
	var	layout0 = document.getElementById("UIAddressBooks") ;
	var	layout1 = document.getElementById("UITags") ;
	var	layout3 = document.getElementById("UINavigationContainer") ;
	var workingarea = eXo.core.DOMUtil.findNextElementByTagName(layout3, "div") ;
		
	switch(layout) {
		case 0 :
			if (layout3.style.display == "none") {
				layout0.style.display = "block" ;				
				layout1.style.display = "block" ;				
				layout3.style.display = "block" ;												
				workingarea.style.marginLeft = "243px"	;
				Browser.setCookie("contdisplaymode","block",7) ;
				Browser.setCookie("contdisplaymode0","block",7) ;
				Browser.setCookie("contdisplaymode1","block",7) ;
			} else {
				layout0.style.display = "none" ;
				layout1.style.display = "none" ;
				layout3.style.display = "none" ;
				workingarea.style.marginLeft = "0px"	;
				Browser.setCookie("contdisplaymode","none",7) ;
				Browser.setCookie("contdisplaymode0","none",7) ;
				Browser.setCookie("contdisplaymode1","none",7) ;
			}
			break ;
		case 1 :
			if (layout0.style.display == "none") {
				layout0.style.display = "block" ;
				layout3.style.display = "block" ;
				workingarea.style.marginLeft = "243px"	;			
				Browser.setCookie("contdisplaymode","block",7) ;
				Browser.setCookie("contdisplaymode0","block",7) ;
			}
			else {
				layout0.style.display = "none" ;
				if(layout1.style.display == "none") {
					Browser.setCookie("contdisplaymode","none",7) ;
					workingarea.style.marginLeft = "0px"	;
					layout3.style.display = "none" ;
				}
				Browser.setCookie("contdisplaymode0","none",7) ;	
			}
			break ;
		case 2 :
			if (layout1.style.display == "none") {
				layout1.style.display = "block" ;
				layout3.style.display = "block" ;
				workingarea.style.marginLeft = "243px"	;
				Browser.setCookie("contdisplaymode","block",7) ;
				Browser.setCookie("contdisplaymode1","block",7) ;

			}
			else {				
				layout1.style.display = "none" ;
				if(layout0.style.display == "none") {
					Browser.setCookie("contdisplaymode","none",7) ;
					workingarea.style.marginLeft = "0px"	;
					layout3.style.display = "none" ;
				}
				Browser.setCookie("contdisplaymode1","none",7) ;	
			}
			break ;
	}
}	;
eXo.contact.UIContactPortlet = new UIContactPortlet() ;