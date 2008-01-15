eXo.require('eXo.cs.UIContextMenu','/csResources/javascript/') ;
eXo.require('eXo.cs.CSUtils','/csResources/javascript/') ;
function UIContactPortlet() {
	
}

UIContactPortlet.prototype.toggleSelectAll = function(handler) {
  rootElement = eXo.core.DOMUtil.findAncestorByClass(handler, 'UIGrid') ;
  if (rootElement) {
    var eLst = eXo.core.DOMUtil.findDescendantsByClass(rootElement, 'input', 'checkbox') ;
    var curState = handler.checked ;
    for (var i=0; i<eLst.length; i++) {
       eLst[i].checked = curState ;
    }
  }
} ;

UIContactPortlet.prototype.showContextMenu = function(compid) {
	var UIContextMenu = eXo.webui.UIContextMenu ;//eXo.contact.ContextMenu ;
	UIContextMenu.portletName = compid ;
	var config = {
		'preventDefault':false, 
		'preventForms':false
	} ;	
	UIContextMenu.init(config) ;
	UIContextMenu.attach(['UIContactList','VCardContent'], 'UIContactListPopuMenu') ;
	UIContextMenu.attach('ItemList', 'UIAddressBookPopupMenu') ;	
	UIContextMenu.attach('TagList', 'UITagPopupMenu') ;
} ;

UIContactPortlet.prototype.contactCallback = function(evt) {
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var src = _e.srcElement || _e.target ;
	var tr = eXo.core.DOMUtil.findAncestorByTagName(src, "tr") ;
	var id = null ;
	if(tr != null) {
		var checkbox = eXo.core.DOMUtil.findFirstDescendantByClass(tr, "input", "checkbox") ;		
		id = checkbox.name ;
		eXo.webui.UIContextMenu.changeAction(UIContextMenu.menuElement, id) ;
//		if (tr.getAttribute("selectedTag") && (tr.getAttribute("selectedTag").toLowerCase()!="null")) {		
//			var moveContactIcon =  eXo.core.DOMUtil.findFirstDescendantByClass(UIContextMenu.menuElement, "div", "MoveContactIcon") ;
//			moveContactIcon.parentNode.href = "javascript: void(0) ;" ;
//			moveContactIcon.parentNode.style.color = "#cccccc" ;
//		}
	} else {
		var VCardContent = eXo.core.DOMUtil.findAncestorByClass(src, "VCardContent") ;
		id = VCardContent.getAttribute("id") ;
		eXo.webui.UIContextMenu.changeAction(UIContextMenu.menuElement, id) ;
//		if (VCardContent.getAttribute("selectedTag") && (VCardContent.getAttribute("selectedTag").toLowerCase()!="null")) {		
//			var moveContactIcon =  eXo.core.DOMUtil.findFirstDescendantByClass(UIContextMenu.menuElement, "div", "MoveContactIcon") ;
//			moveContactIcon.parentNode.href = "javascript: void(0) ;" ;
//			moveContactIcon.parentNode.style.color = "#cccccc" ;
//		}
	}
} ;

UIContactPortlet.prototype.addressBookCallback = function(evt) {
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var DOMUtil = eXo.core.DOMUtil ;
	var _e = window.event || evt ;	
	var src = _e.srcElement || _e.target ;
	var a = (DOMUtil.hasClass(src, "ItemList")) ? src : DOMUtil.findAncestorByClass(src, "ItemList") ;	
	eXo.webui.UIContextMenu.changeAction(UIContextMenu.menuElement, a.id) ;
	var isPublic = a.getAttribute("addressType") ;	
	
	var menuItems = DOMUtil.findDescendantsByClass(UIContextMenu.menuElement, "div", "ItemIcon") ;
	var itemLength = menuItems.length ;	

	if (isPublic && (isPublic.toLowerCase() == "2")) {
		for(var i = 0 ; i < itemLength ; i ++) {
			if (DOMUtil.hasClass(menuItems[i],"ShareIcon") || DOMUtil.hasClass(menuItems[i],"EditActionIcon") || DOMUtil.hasClass(menuItems[i],"DeleteIcon") || DOMUtil.hasClass(menuItems[i],"ImportAddressIcon") || DOMUtil.hasClass(menuItems[i],"ImportContactIcon")) {
				if (menuItems[i].parentNode.getAttribute("oldHref")) continue ;
				menuItems[i].parentNode.setAttribute("oldHref", menuItems[i].parentNode.href) ;
				menuItems[i].parentNode.href = "javascript: void(0) ;" ;
				menuItems[i].parentNode.setAttribute("oldColor", DOMUtil.getStyle(menuItems[i].parentNode, "color")) ;
				menuItems[i].parentNode.style.color = "#cccccc" ;
			}
		}
	} else if(isPublic.toLowerCase() == "1") {
			for(var i = 0 ; i < itemLength ; i ++) {
				if (DOMUtil.hasClass(menuItems[i],"ShareIcon") || DOMUtil.hasClass(menuItems[i],"ImportAddressIcon") || DOMUtil.hasClass(menuItems[i],"ImportContactIcon")) {
					if (menuItems[i].parentNode.getAttribute("oldHref")) continue ;
					menuItems[i].parentNode.setAttribute("oldHref", menuItems[i].parentNode.href) ;
					menuItems[i].parentNode.href = "javascript: void(0) ;" ;
					menuItems[i].parentNode.setAttribute("oldColor", DOMUtil.getStyle(menuItems[i].parentNode, "color")) ;
					menuItems[i].parentNode.style.color = "#cccccc" ;
				} else {
					if (!menuItems[i].parentNode.getAttribute("oldHref")) continue ;
					menuItems[i].parentNode.href = menuItems[i].parentNode.getAttribute("oldHref") ;
					menuItems[i].parentNode.style.color = menuItems[i].parentNode.getAttribute("oldColor") ;
					menuItems[i].parentNode.removeAttribute("oldColor") ;
					menuItems[i].parentNode.removeAttribute("oldHref") ;
				}
			}
	}	else {
		for(var i = 0 ; i < itemLength ; i ++) {
			if (DOMUtil.hasClass(menuItems[i],"ShareIcon") || DOMUtil.hasClass(menuItems[i],"EditActionIcon") || DOMUtil.hasClass(menuItems[i],"DeleteIcon") || DOMUtil.hasClass(menuItems[i],"ImportAddressIcon") || DOMUtil.hasClass(menuItems[i],"ImportContactIcon")) {
				if (!menuItems[i].parentNode.getAttribute("oldHref")) continue ;
				menuItems[i].parentNode.href = menuItems[i].parentNode.getAttribute("oldHref") ;
				menuItems[i].parentNode.style.color = menuItems[i].parentNode.getAttribute("oldColor") ;
				menuItems[i].parentNode.removeAttribute("oldColor") ;
				menuItems[i].parentNode.removeAttribute("oldHref") ;
			}
		}
	}
	
	var isList = a.getAttribute("isList") ;
	if (isList == "true") {
		for(var i = 0 ; i < itemLength ; i ++) {
			if (DOMUtil.hasClass(menuItems[i],"PrintIcon")) {
				if (menuItems[i].parentNode.getAttribute("oldHref")) continue ;
				menuItems[i].parentNode.setAttribute("oldHref", menuItems[i].parentNode.href) ;
				menuItems[i].parentNode.href = "javascript: void(0) ;" ;
				menuItems[i].parentNode.setAttribute("oldColor", DOMUtil.getStyle(menuItems[i].parentNode, "color")) ;
				menuItems[i].parentNode.style.color = "#cccccc" ;
			}
		}
	} else {
		for(var i = 0 ; i < itemLength ; i ++) {
			if (DOMUtil.hasClass(menuItems[i],"PrintIcon")) {
				if (!menuItems[i].parentNode.getAttribute("oldHref")) continue ;
				menuItems[i].parentNode.href = menuItems[i].parentNode.getAttribute("oldHref") ;
				menuItems[i].parentNode.style.color = menuItems[i].parentNode.getAttribute("oldColor") ;
				menuItems[i].parentNode.removeAttribute("oldColor") ;
				menuItems[i].parentNode.removeAttribute("oldHref") ;
			}
		}
	}
	
	var isDefault = a.getAttribute("isDefault") ;
	if (isDefault == "true") {
		for(var i = 0 ; i < itemLength ; i ++) {
			if (DOMUtil.hasClass(menuItems[i],"EditActionIcon") || DOMUtil.hasClass(menuItems[i],"DeleteIcon")) {
				if (menuItems[i].parentNode.getAttribute("oldHref")) continue ;
				menuItems[i].parentNode.setAttribute("oldHref", menuItems[i].parentNode.href) ;
				menuItems[i].parentNode.href = "javascript: void(0) ;" ;
				menuItems[i].parentNode.setAttribute("oldColor", DOMUtil.getStyle(menuItems[i].parentNode, "color")) ;
				menuItems[i].parentNode.style.color = "#cccccc" ;
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
	UIContactContainer.removeAttribute("class") ;
	div.className = "UIPrintContainer UIContactPortlet" ;
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
		var	layout2 = document.getElementById("UINavigationContainer") ;
		var workingarea = eXo.core.DOMUtil.findNextElementByTagName(layout2, "div") ;
	}catch(e) {
		alert(e.message) ;
	}
	layout2.style.display = display ;
	if (display == "none") workingarea.style.marginLeft = "0px"	;
	layout0.style.display = display0 ;
	layout1.style.display = display1 ;
  var layoutno = Browser.getCookie('layoutno') ;
  eXo.contact.UIContactPortlet.addCheckedIcon(layoutno) ;
} ;

UIContactPortlet.prototype.switchLayout = function(layout) {
	var Browser = eXo.core.Browser ;
	layout = parseInt(layout) ;
	var	layout0 = document.getElementById("UIAddressBooks") ;
	var	layout1 = document.getElementById("UITags") ;
	var	layout3 = document.getElementById("UINavigationContainer") ;
	var workingarea = eXo.core.DOMUtil.findNextElementByTagName(layout3, "div") ;
	
  this.addCheckedIcon(layout) ;
  
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

UIContactPortlet.prototype.addCheckedIcon = function(layout) {
  var itemIcons = eXo.core.DOMUtil.findDescendantsByClass(
                    document.getElementById('customLayoutViewMenu'), 'div', 'ItemIcon') ;
  for (var i=0; i<itemIcons.length; i++) {
    if (i == layout) {
      itemIcons[i].className = 'ItemIcon CheckedMenu' ;
    } else {
      itemIcons[i].className = 'ItemIcon' ;
    }
  }
  eXo.core.Browser.setCookie("layoutno", layout, 7) ;
} ;

UIContactPortlet.prototype.imFormOnload = function(root){
  var domUtil = eXo.core.DOMUtil ;
  root = document.getElementById(root) ;
  if (!root) { return false ;}
  eXo.contact.UIContactPortlet.imFormRoot = root ;
  var menu4Remove = [] ;
  var inputLst = root.getElementsByTagName('input') ;
  for (var i=1; i<inputLst.length; i++) {
    var trTag = eXo.core.DOMUtil.findAncestorByTagName(inputLst[i], 'tr') ;
    if (inputLst[i].value != '') {
      trTag.style.display = 'table-row' ;
      menu4Remove[menu4Remove.length] = inputLst[i].name ;
    } else {
      trTag.style.display = 'none' ;
    }
  }
  var menuRoot = document.getElementById(root.id + '_PopupMenu') ;
  var menuItems = domUtil.findDescendantsByClass(menuRoot, 'div', 'ItemIcon') ;
  for (var i=0; i<menuItems.length; i++) {
    menuItems[i].onclick = eXo.contact.UIContactPortlet.showImField ;
  }
  if (menu4Remove.length > 0) {
    root.setAttribute('sync', '1') ;
  }
  eXo.contact.UIContactPortlet.synchonizeMenu(menuRoot, menu4Remove) ;
} ;

UIContactPortlet.prototype.synchonizeMenu = function(menuRoot, menu4Remove){
  var domUtil = eXo.core.DOMUtil ;
  var menuItems = domUtil.findDescendantsByClass(menuRoot, 'div', 'ItemIcon') ;
  for (var i=0; i<menuItems.length; i++) {
    var menuItem = menuItems[i] ;
    var fieldName = menuItem.getAttribute('fieldname') ;
    for (var j=0; j<menu4Remove.length; j++) {
      if (fieldName == menu4Remove[j]) {
        domUtil.findAncestorByTagName(menuItem, 'span').style.display = 'none' ;
        break ;
      }
    }
  }
} ;

UIContactPortlet.prototype.showImField = function() {
  var domUtil = eXo.core.DOMUtil ;
  var menuItem = this ;
  var fieldName = menuItem.getAttribute('fieldname') ;
  var uiIMContact = domUtil.findAncestorByClass(menuItem, 'UIIMContact') ;
  var imFields = domUtil.findDescendantsByTagName(uiIMContact, 'input') ;
  for (var i=0; i<imFields.length; i++) {
    if (imFields[i].name == fieldName) {
      var trTag = domUtil.findAncestorByTagName(imFields[i], 'tr') ;
      trTag.style.display = 'table-row' ;
      var aTag = domUtil.findAncestorByTagName(menuItem, 'span') ;
      aTag.style.display = 'none' ;
      break ;
    }
  }
  // fix display
  var root = eXo.contact.UIContactPortlet.imFormRoot ;
  if (!root.getAttribute('sync') || root.getAttribute('sync') != '1') {
    var trTags = root.getElementsByTagName('tr') ;
    for (var i=0; i<trTags.length; i++) {
      if (trTags[i].style.display != 'none') {
        trTags[i].style.display = 'none' ;
        window.setTimeout(eXo.contact.UIContactPortlet.showTrTimer, 10, trTags[i]) ;
      }
    }
    root.setAttribute('sync', '1') ;
  }
  return false ;
} ;

UIContactPortlet.prototype.showTrTimer = function(e) {
  e.style.display = 'table-row' ;  
} ;

UIContactPortlet.prototype.showMap = function(/*String*/ address, /*String*/ message) {
	eXo.core.Topic.publish("UIContactPortlet", "/eXo/portlet/map/displayAddress", {address:address, text:message});
}

eXo.contact.UIContactPortlet = new UIContactPortlet() ;