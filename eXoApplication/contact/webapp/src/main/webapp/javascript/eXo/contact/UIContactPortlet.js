eXo.require('eXo.cs.CSUtils', '/csResources/javascript/'); 

function UIContactPortlet() {
	
}

UIContactPortlet.prototype.showContextMenu = function(compid) {
	try {
		var UIContextMenuCon = eXo.webui.UIContextMenuCon ;//eXo.contact.ContextMenu ;
		this.portletNode = document.getElementById("UIContactPortlet");
		UIContextMenuCon.portletName = compid ;
		var config = {
			'preventDefault':false, 
			'preventForms':false
		} ;	
		UIContextMenuCon.init(config) ;
		UIContextMenuCon.attach(['UIContactList','VCardContent','ContactPreviewDetails'], 'UIContactListPopuMenu') ;
		UIContextMenuCon.attach('PrivateAddressBook', 'UIAddressBookPopupMenu0') ;	
		UIContextMenuCon.attach('ShareAddressBook', 'UIAddressBookPopupMenu1') ;
		UIContextMenuCon.attach('PublicAddressBook', 'UIAddressBookPopupMenu2') ;
		UIContextMenuCon.attach('TagList', 'UITagPopupMenu') ;
		this.fixForIE(compid);
	} catch (e) {
		return ;
	}


} ;

UIContactPortlet.prototype.fixForIE = function(cpid) {
	var comp = document.getElementById(cpid);
	var uiReiszableBlock = eXo.core.DOMUtil.findAncestorByClass(comp,"UIResizableBlock");
	if(uiReiszableBlock) uiReiszableBlock.onscroll = eXo.webui.UIContextMenu.hide ;
};

UIContactPortlet.prototype.contactCallback = function(evt) {
	var UIContextMenuCon = eXo.webui.UIContextMenuCon ;
  var DOMUtil = eXo.core.DOMUtil ;
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var src = _e.srcElement || _e.target ;
	var id = null ;
	var tr = DOMUtil.findAncestorByClass(src, "VCardContent") ;
	if(tr != null) {
		//tr = DOMUtil.findAncestorByClass(src, "VCardContent") ;
		id = tr.getAttribute("id") ;
	} else {
		tr = DOMUtil.findAncestorByClass(src, "ContactPreviewDetails");
		if (tr != null) {
			id = tr.getAttribute("id");
		} else {
			tr = DOMUtil.findAncestorByTagName(src, "tr") ;
			if (tr != null) {
				var checkbox = DOMUtil.findFirstDescendantByClass(tr, "input", "checkbox") ;
				id = checkbox.name ;
			}
		}
	}
  var type = tr.getAttribute("type").toLowerCase() ;
	var isOwner = tr.getAttribute("isOwner").toLowerCase() ;  

  var actions = DOMUtil.findDescendantsByClass(UIContextMenuCon.menuElement, "a", "ItemIcon") ;
  var isDisable = null ;
  var len = actions.length ;
  if(type == "2") {
    for (var i = 0; i < len; i++) {
      isDisable = DOMUtil.hasClass(actions[i], "EditActionIcon") || DOMUtil.hasClass(actions[i], "ShareIcon") || DOMUtil.hasClass(actions[i], "MoveIcon") || DOMUtil.hasClass(actions[i], "DeleteContactIcon") ;
      if (isDisable == false) continue;
      if (!actions[i].getAttribute("oldHref")) {
        actions[i].setAttribute("oldHref", actions[i].href);
        actions[i].style.color = "#cccccc";
        actions[i].href = "javascript:void(0);";
      }
    } 
  } else if (type == "1") {
  	for (var i = 0; i < len; i++) {
      isDisable = DOMUtil.hasClass(actions[i], "ShareIcon");
      if (isDisable == true) {
        if (!actions[i].getAttribute("oldHref")) {
          actions[i].setAttribute("oldHref", actions[i].href);
          actions[i].style.color = "#cccccc";
          actions[i].href = "javascript:void(0);";
        }		
	  } else {
				if (actions[i].getAttribute("oldHref")) {
          actions[i].href = actions[i].getAttribute("oldHref");
          actions[i].removeAttribute("oldHref");
          actions[i].removeAttribute("style");
        }
	  }
    }
		var havePermission = tr.getAttribute("havePermission").toLowerCase() ;		
		if (havePermission == "false") {
			for (var i = 0; i < len; i++) {    			
  			isDisable = DOMUtil.hasClass(actions[i], "EditActionIcon") ;
      	if (isDisable == false) continue ;
        if (!actions[i].getAttribute("oldHref")) {
          actions[i].setAttribute("oldHref", actions[i].href);
          actions[i].style.color = "#cccccc";
          actions[i].href = "javascript:void(0);";
        }    			
			}
  	} else { // havePermission
  		for (var i = 0; i < len; i++) {
        isDisable = DOMUtil.hasClass(actions[i], "EditActionIcon")
        if (isDisable == false) continue;
        if (actions[i].getAttribute("oldHref")) {
          actions[i].href = actions[i].getAttribute("oldHref");
          actions[i].removeAttribute("oldHref");
          actions[i].removeAttribute("style");
        }
 	 		}      		
  	}	
		var isSharedAddress = tr.getAttribute("isSharedAddress").toLowerCase() ;   
 		
// 		change to fix bug cs -1443
		if (isSharedAddress == "true") {	
		  var havePermissionAdd = tr.getAttribute("havePermissionAdd").toLowerCase() ;	
		  if (havePermissionAdd == "true" && isOwner == "false") {
		    for (var i = 0; i < len; i++) {
	          isDisable = DOMUtil.hasClass(actions[i], "DeleteContactIcon") || DOMUtil.hasClass(actions[i], "MoveIcon");
	          if (isDisable == false) continue;
	          if (actions[i].getAttribute("oldHref")) {
	            actions[i].href = actions[i].getAttribute("oldHref");
	            actions[i].removeAttribute("oldHref");
	            actions[i].removeAttribute("style");
        	  }
		    }
		  } else {
		  	for (var i = 0; i < len; i++) {    			
		  	  isDisable = DOMUtil.hasClass(actions[i], "DeleteContactIcon") || DOMUtil.hasClass(actions[i], "MoveIcon");
		      if (isDisable == false) continue ;
		      if (!actions[i].getAttribute("oldHref")) {
		        actions[i].setAttribute("oldHref", actions[i].href);
		        actions[i].style.color = "#cccccc";
		        actions[i].href = "javascript:void(0);";
		      }
		    }
		  }					
		} else {
		  for (var i = 0; i < len; i++) {
	        isDisable = DOMUtil.hasClass(actions[i], "DeleteContactIcon") || DOMUtil.hasClass(actions[i], "MoveIcon");
	        if (isDisable == false) continue;
	        if (actions[i].getAttribute("oldHref")) {
	          actions[i].href = actions[i].getAttribute("oldHref");
	          actions[i].removeAttribute("oldHref");
	          actions[i].removeAttribute("style");
        	}
		  }
		}

	// -------------------
//		if (isSharedAddress == "true") {
//			for (var i = 0; i < len; i++) {    			
//  			isDisable = DOMUtil.hasClass(actions[i], "DeleteContactIcon") || DOMUtil.hasClass(actions[i], "MoveIcon");
//      	if (isDisable == false) continue ;
//        if (!actions[i].getAttribute("oldHref")) {
//          actions[i].setAttribute("oldHref", actions[i].href);
//          actions[i].style.color = "#cccccc";
//          actions[i].href = "javascript:void(0);";
//        }
//			}
//  	} else { // havePermission
//  		for (var i = 0; i < len; i++) {
//        isDisable = DOMUtil.hasClass(actions[i], "DeleteContactIcon") || DOMUtil.hasClass(actions[i], "MoveIcon");
//        if (isDisable == false) continue;
//        if (actions[i].getAttribute("oldHref")) {
//          actions[i].href = actions[i].getAttribute("oldHref");
//          actions[i].removeAttribute("oldHref");
//          actions[i].removeAttribute("style");
//        }
// 	 		}      		
//  	}
//  
    
  } else { // type = "0"  		
		if (isOwner == "true") { 
			for (var i = 0; i < len; i++) {    			
  			isDisable = DOMUtil.hasClass(actions[i], "DeleteContactIcon") || DOMUtil.hasClass(actions[i], "MoveIcon");
      	if (isDisable == false) continue ;
        if (!actions[i].getAttribute("oldHref")) {
          actions[i].setAttribute("oldHref", actions[i].href);
          actions[i].style.color = "#cccccc";
          actions[i].href = "javascript:void(0);";
        }    			
			}
  	} else {
  		for (var i = 0; i < len; i++) {
        isDisable = DOMUtil.hasClass(actions[i], "DeleteContactIcon") || DOMUtil.hasClass(actions[i], "MoveIcon") ;
        if (isDisable == false) continue;
        if (actions[i].getAttribute("oldHref")) {
          actions[i].href = actions[i].getAttribute("oldHref");
          actions[i].removeAttribute("oldHref");
          actions[i].removeAttribute("style");
        }
 	 		} 
 	 		for (var i = 0; i < len; i++) {
        isDisable = DOMUtil.hasClass(actions[i], "EditActionIcon") || DOMUtil.hasClass(actions[i], "ShareIcon") || DOMUtil.hasClass(actions[i], "MoveIcon")
        if (isDisable == false) continue;
        if (actions[i].getAttribute("oldHref")) {
          actions[i].href = actions[i].getAttribute("oldHref");
          actions[i].removeAttribute("oldHref");
          actions[i].removeAttribute("style");
        }
    	}	     		
  	}
  }
	eXo.webui.UIContextMenuCon.changeAction(UIContextMenuCon.menuElement, id) ;
} ;

UIContactPortlet.prototype.addressBookCallback = function(evt) {
	var UIContextMenuCon = eXo.webui.UIContextMenuCon ;
	var DOMUtil = eXo.core.DOMUtil ;
	var _e = window.event || evt ;
	var src = _e.srcElement || _e.target ;
	var addressBook = (DOMUtil.hasClass(src, "ItemList")) ? src : DOMUtil.findAncestorByClass(src, "ItemList") ;	
	var menuItems = DOMUtil.findDescendantsByClass(UIContextMenuCon.menuElement, "a", "ItemIcon") ;
	var itemLength = menuItems.length ;	

	var isDefault = addressBook.getAttribute("isDefault") ;
	if (addressBook.getAttribute("addressType") == "0") {
		if (isDefault == "true") {
			for(var i = 0 ; i < itemLength ; i ++) {
				if (DOMUtil.hasClass(menuItems[i],"DeleteIcon")) {
					if (menuItems[i].getAttribute("oldHref")) continue ;
					menuItems[i].setAttribute("oldHref", menuItems[i].href) ;
					menuItems[i].href = "javascript: void(0) ;" ;
					menuItems[i].setAttribute("oldColor", DOMUtil.getStyle(menuItems[i], "color")) ;
					menuItems[i].style.color = "#cccccc" ;
				}
			}
		} else { // isDefault = false
	    for(var i = 0 ; i < itemLength ; i ++) {
				if (DOMUtil.hasClass(menuItems[i],"DeleteIcon")) {
					if (!menuItems[i].getAttribute("oldHref")) continue ;
					menuItems[i].href = menuItems[i].getAttribute("oldHref") ;
					menuItems[i].style.color = menuItems[i].getAttribute("oldColor") ;
					menuItems[i].removeAttribute("oldColor") ;
					menuItems[i].removeAttribute("oldHref") ;
				}
			}
  	}
	} else if (addressBook.getAttribute("addressType") == "1") {
  	var havePermission = addressBook.getAttribute("havePermission") ;
		if (havePermission == "false") {
			for(var i = 0 ; i < itemLength ; i ++) {
				if (DOMUtil.hasClass(menuItems[i],"ContactIcon") || DOMUtil.hasClass(menuItems[i],"PasteIcon")
					|| DOMUtil.hasClass(menuItems[i],"EditActionIcon") || DOMUtil.hasClass(menuItems[i],"ImportContactIcon")) {
					if (menuItems[i].getAttribute("oldHref")) continue ;
					menuItems[i].setAttribute("oldHref", menuItems[i].href) ;
					menuItems[i].href = "javascript: void(0) ;" ;
					menuItems[i].setAttribute("oldColor", DOMUtil.getStyle(menuItems[i], "color")) ;
					menuItems[i].style.color = "#cccccc" ;
				}
			}		
		} else {
			for(var i = 0 ; i < itemLength ; i ++) {
				if (DOMUtil.hasClass(menuItems[i],"ContactIcon") || DOMUtil.hasClass(menuItems[i],"PasteIcon")
					|| DOMUtil.hasClass(menuItems[i],"EditActionIcon") || DOMUtil.hasClass(menuItems[i],"ImportContactIcon")) {
					if (!menuItems[i].getAttribute("oldHref")) continue ;
					menuItems[i].href = menuItems[i].getAttribute("oldHref") ;
					menuItems[i].style.color = menuItems[i].getAttribute("oldColor") ;
					menuItems[i].removeAttribute("oldColor") ;
					menuItems[i].removeAttribute("oldHref") ;
				}
			}		
		} 
//		if (isDefault == "true") {
//			for(var i = 0 ; i < itemLength ; i ++) {
//				if (DOMUtil.hasClass(menuItems[i],"EditActionIcon")) {
//					if (menuItems[i].parentNode.getAttribute("oldHref")) continue ;
//					menuItems[i].parentNode.setAttribute("oldHref", menuItems[i].parentNode.href) ;
//					menuItems[i].parentNode.href = "javascript: void(0) ;" ;
//					menuItems[i].parentNode.setAttribute("oldColor", DOMUtil.getStyle(menuItems[i].parentNode, "color")) ;
//					menuItems[i].parentNode.style.color = "#cccccc" ;
//				}
//			}
//		}		
	} else { //
	  
	  
	  var havePermission = addressBook.getAttribute("havePermission") ;
		if (havePermission == "false") {
			for(var i = 0 ; i < itemLength ; i ++) {
				if (DOMUtil.hasClass(menuItems[i],"ShareIcon")) {
					if (menuItems[i].getAttribute("oldHref")) continue ;
					menuItems[i].setAttribute("oldHref", menuItems[i].href) ;
					menuItems[i].href = "javascript: void(0) ;" ;
					menuItems[i].setAttribute("oldColor", DOMUtil.getStyle(menuItems[i], "color")) ;
					menuItems[i].style.color = "#cccccc" ;
				}
			}		
		} else {
			for(var i = 0 ; i < itemLength ; i ++) {
				if (DOMUtil.hasClass(menuItems[i],"ShareIcon")) {
					if (!menuItems[i].getAttribute("oldHref")) continue ;
					menuItems[i].href = menuItems[i].getAttribute("oldHref") ;
					menuItems[i].style.color = menuItems[i].getAttribute("oldColor") ;
					menuItems[i].removeAttribute("oldColor") ;
					menuItems[i].removeAttribute("oldHref") ;
				}
			}		
		}
	  
	  
	}
	
  
//	
//	var isList = addressBook.getAttribute("isList") ;
//	if (isList == "true") {
//		for(var i = 0 ; i < itemLength ; i ++) {
//			if (DOMUtil.hasClass(menuItems[i],"PrintIcon")) {
//				if (menuItems[i].parentNode.getAttribute("oldHref")) continue ;
//				menuItems[i].parentNode.setAttribute("oldHref", menuItems[i].parentNode.href) ;
//				menuItems[i].parentNode.href = "javascript: void(0) ;" ;
//				menuItems[i].parentNode.setAttribute("oldColor", DOMUtil.getStyle(menuItems[i].parentNode, "color")) ;
//				menuItems[i].parentNode.style.color = "#cccccc" ;
//			}
//		}
//	} else {
//		for(var i = 0 ; i < itemLength ; i ++) {
//			if (DOMUtil.hasClass(menuItems[i],"PrintIcon")) {
//				if (!menuItems[i].parentNode.getAttribute("oldHref")) continue ;
//				menuItems[i].parentNode.href = menuItems[i].parentNode.getAttribute("oldHref") ;
//				menuItems[i].parentNode.style.color = menuItems[i].parentNode.getAttribute("oldColor") ;
//				menuItems[i].parentNode.removeAttribute("oldColor") ;
//				menuItems[i].parentNode.removeAttribute("oldHref") ;
//			}
//		}
//	}
	eXo.webui.UIContextMenuCon.changeAction(UIContextMenuCon.menuElement, addressBook.id) ;
} ;

UIContactPortlet.prototype.tagCallback = function(evt) {
	var UIContextMenuCon = eXo.webui.UIContextMenuCon ;
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var src = _e.srcElement || _e.target ;
	src = (src.nodeName.toLowerCase() == "div")? src : src.parentNode ;
	var tagId = src.getAttribute("tagId") ;
	eXo.webui.UIContextMenuCon.changeAction(UIContextMenuCon.menuElement, tagId) ;
} ;

UIContactPortlet.prototype.printpreview = function (obj){
	var DOMUtil = eXo.core.DOMUtil ;
	var UIPortalApplication = document.getElementById("UIPortalApplication") ;
	var UIContactPreview = DOMUtil.findAncestorByClass(obj, "UIContactPreview") ;	
	var a = eXo.core.DOMUtil.findDescendantsByTagName(UIContactPreview, 'a');
	for (var i = 0; i < a.length; i++) {
		if (a[i].title && (a[i].title.length > 0) && (a[i].title.indexOf('@') > -1 )) {
			 a[i].innerHTML = a[i].title;		
		}
	}
	var form = eXo.core.DOMUtil.findAncestorByTagName(obj, "form") ;
	var printLabel = DOMUtil.findFirstDescendantByClass(obj, 'div','ButtonMiddle') ;
	if(obj.getAttribute("printLabel")) printLabel.innerHTML = obj.getAttribute("printLabel") ;
	if(obj.getAttribute("onclick")) obj.removeAttribute("onclick") ;	
	var printButton = obj.cloneNode(true) ;
	printButton.href = "javascript:window.print() ;" ;
	obj.parentNode.insertBefore(printButton,obj) ;
	DOMUtil.removeElement(obj) ;	
	eXo.contact.UIContactPortlet.printList(form.id) ;
	window.scroll(0,0);
} ;

UIContactPortlet.prototype.disableAction = function(cont){
	if(typeof(cont) == "string") cont = eXo.contact.UIContactPortlet.getElementById(cont) ;
	var a = eXo.core.DOMUtil.findDescendantsByTagName(cont, "a") ;
	var len = a.length ;
	for(var i = 0 ; i < len ; i ++) {
		if(eXo.core.DOMUtil.hasClass(a[i],"ActionButton")) continue ;
		var text = document.createTextNode(a[i].innerHTML) ;
		a[i].parentNode.appendChild(text) ;
		eXo.core.DOMUtil.removeElement(a[i]) ;	
	}
	return cont ;
} ;

UIContactPortlet.prototype.adddressPrint = function (){
	var DOMUtil = eXo.core.DOMUtil ;
	var UIPortalApplication = document.getElementById("UIPortalApplication") ;
	var UIContactContainer = eXo.contact.UIContactPortlet.getElementById("UIContactContainer") ;
	var div = document.createElement("div") ;
	div.className = "UIPrintContainer UIContactPortlet" ;
	var printContainer = UIContactContainer.cloneNode(true) ;
	DOMUtil.removeElement(UIContactContainer) ;
	printContainer.removeAttribute("class") ;
	div.appendChild(printContainer) ;
	var uiAction = DOMUtil.findFirstDescendantByClass(div, "div", "UIAction") ;
	DOMUtil.addClass(uiAction, "Printable") ;
	UIPortalApplication.style.visibility = "hidden" ;
	eXo.contact.UIContactPortlet.pageBackground = document.body.style.background ;
	document.body.style.background = "transparent" ;
	document.body.insertBefore(div,UIPortalApplication) ;
	UIPortalApplication.style.height =  div.offsetHeight + "px";
	UIPortalApplication.style.overflow =  "hidden";
	if(document.getElementById("UIPageDesktop")) UIPortalApplication.style.display = "none";
	window.scroll(0,0);
} ;

UIContactPortlet.prototype.cancelPrint = function (obj){
	var UIPrintContainer = eXo.core.DOMUtil.findAncestorByClass(obj, "UIPrintContainer") ;
	var UIPortalApplication = document.getElementById("UIPortalApplication") ;
	eXo.core.DOMUtil.removeElement(UIPrintContainer) ;
	UIPortalApplication.style.display = "block" ;
	UIPortalApplication.style.height =  "auto";
	UIPortalApplication.style.overflow =  "";
	UIPortalApplication.style.visibility = "visible" ;
	if(document.getElementById("UIPageDesktop")) UIPortalApplication.style.display = "block";
	document.body.style.background = eXo.contact.UIContactPortlet.pageBackground ;
	eXo.contact.UIContactPortlet.pageBackground = null ;
	window.scroll(0,0);
} ;

UIContactPortlet.prototype.cancelPrintList = function (){
	var UIPrintContainer = eXo.core.DOMUtil.findFirstDescendantByClass(document.body,"div", "UIPrintContainer") ;
	var UIPortalApplication = document.getElementById("UIPortalApplication") ;
	if(UIPrintContainer) eXo.core.DOMUtil.removeElement(UIPrintContainer) ;
	UIPortalApplication.style.display = "block" ;
	UIPortalApplication.style.height =  "auto";
	UIPortalApplication.style.overflow =  "";
	UIPortalApplication.style.visibility = "visible" ;
	

	//cs-2327 
	var UIWindowContact = document.getElementById("UIWindow-contact") ;
	if (UIWindowContact) {
		UIWindowContact.style.display = "block" ;
	}
	
	if(document.getElementById("UIPageDesktop")) UIPortalApplication.style.display = "block";
	window.scroll(0,0);
} ;

UIContactPortlet.prototype.printList = function (obj){
	if(typeof(obj) == "string") obj = document.getElementById(obj) ;
	obj = eXo.contact.UIContactPortlet.disableAction(obj) ;
	var printContainer = obj.cloneNode(true) ;
	var uiPopupWindow = eXo.core.DOMUtil.findAncestorByClass(obj,"UIPopupWindow") ;
	if(uiPopupWindow) uiPopupWindow.style.display = "none";
	var UIAction = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIAction") ;
	if(eXo.core.DOMUtil.findChildrenByClass(document.body, "div", "UIPrintContainer").length > 0) return ;
	var div = document.createElement("div") ;
	div.className = "UIContactPortlet UIPrintContainer" ;
	div.oncontextmenu = eXo.contact.UIContactPortlet.disableContextMenu ;
	div.appendChild(printContainer) ;
	var UIPortalApplication = document.getElementById("UIPortalApplication") ;
	UIPortalApplication.style.visibility = "hidden" ;
	// cs-2327
	var UIWindowContact = document.getElementById("UIWindow-contact") ;
	if (UIWindowContact) {
		UIWindowContact.style.display = "none" ;
	}
	
	div.style.position = "absolute" ;
	div.style.width = "99%" ;
	div.style.zIndex =  100;
	document.body.insertBefore(div,UIPortalApplication) ;
	eXo.core.DOMUtil.removeElement(UIAction) ;
	UIPortalApplication.style.height =  div.offsetHeight + "px";
	UIPortalApplication.style.overflow = "hidden";
	if(document.getElementById("UIPageDesktop")) {
		UIPortalApplication.style.display = "none";
	}	
} ;

UIContactPortlet.prototype.disableContextMenu = function(evt) {
	eXo.core.EventManager.cancelBubble(evt);
	return false ;
};

UIContactPortlet.prototype.checkLayout = function() {
	eXo.contact.LayoutManager.layouts = [] ;
	var DOMUtil = eXo.core.DOMUtil ;
	var portletNode = eXo.contact.UIContactPortlet.portletNode;
	var contactLayout1 = DOMUtil.findFirstDescendantByClass(portletNode, "div", "UINavigationContainer");                                                   
	var contactLayout2 = eXo.contact.UIContactPortlet.getElementById("UIAddressBooks");
	var contactLayout3 = eXo.contact.UIContactPortlet.getElementById("UITags");
	var contactLayout4 = DOMUtil.findFirstDescendantByClass(portletNode, "div", "UIContactPreview");
	var contactLayout5 = DOMUtil.findFirstDescendantByClass(portletNode, "div", "ResizeReadingPane");
	eXo.contact.LayoutManager.layouts.push(contactLayout1);
	eXo.contact.LayoutManager.layouts.push(contactLayout2);
	eXo.contact.LayoutManager.layouts.push(contactLayout3);
	eXo.contact.LayoutManager.layouts.push(contactLayout4);
	eXo.contact.LayoutManager.layouts.push(contactLayout5);
	eXo.contact.LayoutManager.switchCallback = eXo.contact.UIContactPortlet.swithLayoutCallback ;
	eXo.contact.LayoutManager.callback = eXo.contact.UIContactPortlet.checkLayoutCallback ;
	eXo.contact.LayoutManager.resetCallback = eXo.contact.UIContactPortlet.resetLayoutCallback ;
	eXo.contact.LayoutManager.check();
} ;

UIContactPortlet.prototype.swithLayoutCallback = function(layout, status){
	var layoutMan = eXo.contact.LayoutManager ;
	var uiContactPortlet = eXo.contact.UIContactPortlet ;
	var panelWorking = eXo.contact.UIContactPortlet.getElementById('UIContactContainer');
	var layoutcookie = eXo.core.Browser.getCookie(layoutMan.layoutId);
	
	if((layout == 2) || (layout == 3)){
		if(layoutcookie.indexOf('1') >= 0) { return ; }
	}
	if(!status) {
		layoutMan.layouts[layout-1].style.display = "none" ;
		uiContactPortlet.addCheckedIcon(layout,false) ;
		if(layout == 1){			
			layoutMan.layouts[layout].style.display = "none" ;
			layoutMan.layouts[layout+1].style.display = "none" ;
			if(eXo.core.I18n.isLT()) panelWorking.style.marginLeft = "0px" ;
			else  panelWorking.style.marginRight = "0px" ;			
			uiContactPortlet.addCheckedIcon(2,false) ;
			uiContactPortlet.addCheckedIcon(3,false) ;
			if(layoutcookie.indexOf('2') < 0) layoutcookie = layoutcookie.concat(2) ;
			if(layoutcookie.indexOf('3') < 0) layoutcookie = layoutcookie.concat(3) ;
			eXo.core.Browser.setCookie(layoutMan.layoutId,layoutcookie,1);
		}
		
	} else {
		layoutMan.layouts[layout-1].style.display = "block" ;
		uiContactPortlet.addCheckedIcon(layout,true) ;
		if(layout == 1){
			layoutMan.layouts[layout].style.display = "block" ;
			layoutMan.layouts[layout+1].style.display = "block" ;
			if(eXo.core.I18n.isLT()) panelWorking.style.marginLeft = "225px" ;
			else  panelWorking.style.marginRight = "225px" ;
			uiContactPortlet.addCheckedIcon(2,true) ;
			uiContactPortlet.addCheckedIcon(3,true) ;
			if(layoutcookie.indexOf('2') >= 0) layoutcookie = layoutcookie.replace('2','') ;
			if(layoutcookie.indexOf('3') >= 0) layoutcookie = layoutcookie.replace('3','') ;
			eXo.core.Browser.setCookie(layoutMan.layoutId,layoutcookie,1);
		}
		
	}
	uiContactPortlet.isDefaultLayout() ;
};

UIContactPortlet.prototype.checkLayoutCallback = function(layoutcookie){
	var uiContactPortlet = eXo.contact.UIContactPortlet ;
	var i = layoutcookie.length ;
	while(i--){
		eXo.contact.UIContactPortlet.addCheckedIcon(parseInt(layoutcookie.charAt(i)),false) ;
		if(parseInt(layoutcookie.charAt(i)) == 1) {
			var panelWorking = eXo.contact.UIContactPortlet.getElementById('UIContactContainer');
			if(eXo.core.I18n.isLT()) panelWorking.style.marginLeft = "0px" ;
			else  panelWorking.style.marginRight = "0px" ;
			uiContactPortlet.addCheckedIcon(2,false) ;
			uiContactPortlet.addCheckedIcon(3,false) ;
		}
	}
	uiContactPortlet.isDefaultLayout() ;
};

UIContactPortlet.prototype.resetLayoutCallback = function(){
	var itemIcons = eXo.core.DOMUtil.findDescendantsByClass(eXo.contact.UIContactPortlet.getElementById("customLayoutViewMenu"), "a", "ItemIcon");
	var panelWorking = eXo.contact.UIContactPortlet.getElementById('UIContactContainer');
	var i = itemIcons.length ;
	while(i--){
		eXo.core.DOMUtil.addClass(itemIcons[i],'CheckedMenu');
	}
	if(eXo.core.I18n.isLT()) panelWorking.style.marginLeft = "225px" ;
	else  panelWorking.style.marginRight = "225px" ;
};

UIContactPortlet.prototype.switchLayout = function(layout) {
	if(layout == 0){
			eXo.contact.LayoutManager.reset();
			return ;
	}
	eXo.contact.LayoutManager.switchLayout(layout);
};

UIContactPortlet.prototype.checkView = function() {
	var uiContactPortlet = eXo.contact.UIContactPortlet.portletNode;
	var viewIcon = eXo.core.DOMUtil.findFirstDescendantByClass(uiContactPortlet,"div","ViewIcon");
	var menuItems = eXo.core.DOMUtil.findDescendantsByClass(viewIcon.parentNode,"div","MenuItem");
	var isVcard = eXo.contact.UIContactPortlet.getElementById("UIVCards");
	if (isVcard && eXo.core.DOMUtil.findAncestorByClass(isVcard,"UIContactContainer")) {
		if(menuItems[0].getAttribute("style")) menuItems[0].removeAttribute("style");
  	menuItems[1].style.background = "#eaeaea" ;
	} else{
		if(menuItems[1].getAttribute("style")) menuItems[1].removeAttribute("style");
		menuItems[0].style.background = "#eaeaea" ;
	}	
};

UIContactPortlet.prototype.showImMenu = function(obj, event) {
	var event = window.event || event ;
	event.cancelBubble = true ;
	var uiPopupCategory = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIPopupCategory") ;
	var menuItems = eXo.core.DOMUtil.findDescendantsByClass(uiPopupCategory, "span", "MenuItem") ;
	var len = menuItems.length ;
	for(var i = 0 ; i < len ; i++) {
		if (menuItems[i].style.display != "none") break ;
	}
	if (i == len) {
		uiPopupCategory.style.display = "none" ;
		return ;
  }
	if(uiPopupCategory.style.display != "none") {
		uiPopupCategory.style.display = "none" ;
	} else {
		uiPopupCategory.style.display = "block" ;
		eXo.core.DOMUtil.listHideElements(uiPopupCategory) ;
	}
	var menuX = eXo.core.Browser.findPosXInContainer(obj, uiPopupCategory.offsetParent) ;
	var menuY = eXo.core.Browser.findPosYInContainer(obj, uiPopupCategory.offsetParent) +  obj.offsetHeight ;
	uiPopupCategory.style.top = menuY + "px" ;
	uiPopupCategory.style.left = menuX + "px" ;	
};

UIContactPortlet.prototype.addCheckedIcon = function(layout, visible) {
  layout = parseInt(layout);
  var itemIcon = eXo.core.DOMUtil.findDescendantsByClass(eXo.contact.UIContactPortlet.getElementById("customLayoutViewMenu"), "a", "ItemIcon")[layout];
  if(visible) {
    eXo.core.DOMUtil.addClass(itemIcon,'CheckedMenu');
  } else {
    eXo.core.DOMUtil.replaceClass(itemIcon,'CheckedMenu','');
  }
} ;

UIContactPortlet.prototype.imFormOnload = function(root){
  var domUtil = eXo.core.DOMUtil ;
  root = eXo.contact.UIContactPortlet.getElementById(root) ;
  if (!root) { return false ;}	
  eXo.contact.UIContactPortlet.imFormRoot = root ;
  var menu4Remove = [] ;
  var inputLst = root.getElementsByTagName('input') ;
  for (var i=1; i<inputLst.length; i++) {
    var trTag = eXo.core.DOMUtil.findAncestorByTagName(inputLst[i], 'tr') ;
    if (inputLst[i].value != '') {
      //trTag.style.display = 'table-row' ;
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
  var menuItems = domUtil.findDescendantsByClass(menuRoot, 'a', 'ItemIcon') ;
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

UIContactPortlet.prototype.isDefaultLayout = function() {	
	var itemIcons = eXo.core.DOMUtil.findDescendantsByClass(eXo.contact.UIContactPortlet.getElementById("customLayoutViewMenu"), "a", "ItemIcon");
	var len = itemIcons.length ;
	var j = 0 ;
	for(var i = 1 ; i < len ; i++) {
		if (itemIcons[i].parentNode.style.display == "none") {
			len-- ;
			continue ;
		}
		if(eXo.core.DOMUtil.hasClass(itemIcons[i], "CheckedMenu")) j++ ;
	}
	if(j == (len - 1)) this.addCheckedIcon(0, true);
	else this.addCheckedIcon(0, false);
} ;

UIContactPortlet.prototype.showImField = function() {
  var domUtil = eXo.core.DOMUtil ;
  var menuItem = this ;
  var fieldName = menuItem.getAttribute('fieldname') ;
  var uiIMContact = domUtil.findAncestorByClass(menuItem, 'UIIMContact') ;
  var imFields = domUtil.findDescendantsByTagName(uiIMContact, 'input') ;
  for (var i=0; i<imFields.length; i++) {
    if (imFields[i].name == fieldName) {
	try{
	    var trTag = domUtil.findAncestorByTagName(imFields[i], 'tr') ;
	    if(eXo.core.Browser.browserType == "ie") trTag.style.display = 'block' ;
		else trTag.removeAttribute("style") ;
	}catch(e){}
      var aTag = domUtil.findAncestorByTagName(menuItem, 'span') ;				
      aTag.style.display = 'none' ;
      break ;
    }
  }
  // fix display
	return ;
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
};


UIContactPortlet.prototype.show = function(obj, evt){
	if(!evt) evt = window.event ;
	evt.cancelBubble = true ;
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPopupCategory = DOMUtil.findFirstDescendantByClass(obj, 'div', 'UIRightClickPopupMenu') ;	
	if (!uiPopupCategory) return ;	
	if(uiPopupCategory.style.display == "none") {
		DOMUtil.cleanUpHiddenElements() ;
		uiPopupCategory.style.display = "block" ;
		DOMUtil.listHideElements(uiPopupCategory) ;
		if(eXo.core.I18n.isRT()) uiPopupCategory.style.left = (obj.offsetWidth - uiPopupCategory.offsetWidth) + "px" ;
	}	
	else uiPopupCategory.style.display = "none" ;
};

UIContactPortlet.prototype.showHideInfo = function(obj){
	var table = eXo.core.DOMUtil.findAncestorByTagName(obj,"table");
	var emails = eXo.core.DOMUtil.findDescendantsByClass(table, "tr", "HideEmailAddress");
	var len = emails.length ;
	if (emails[0].style.display == "none") {
		for(var i = 0; i < len ; i++) {
			emails[i].style.display = "";
		}
		var showMore = eXo.core.DOMUtil.findFirstDescendantByClass(table, "tr", "ShowMore").style.display = "none";
		var hide = eXo.core.DOMUtil.findFirstDescendantByClass(table, "tr", "Hide").style.display = "";
	} else {
		for(var i = 0; i < len ; i++) {
			emails[i].style.display = "none";
		}
		var showMore = eXo.core.DOMUtil.findFirstDescendantByClass(table, "tr", "ShowMore").style.display = "";
		var hide = eXo.core.DOMUtil.findFirstDescendantByClass(table, "tr", "Hide").style.display = "none";
	}
};

UIContactPortlet.prototype.showPopupCustomLayoutView = function(obj, evt) {
  var root = eXo.contact.UIContactPortlet.portletNode;
  var objWelcome = eXo.core.DOMUtil.findFirstDescendantByClass(root, "div", "UIWelcomeContact");
  var objDetails = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "ContactDetailsMenuItem");
  var objVCards = eXo.core.DOMUtil.findFirstDescendantByClass(root, "div", "UIVCards");
  if(objWelcome || objVCards) {
    objDetails.style.display = "none";
  } else {
    objDetails.style.display = "block";
  }
  this.show(obj, evt);
};

UIContactPortlet.prototype.refreshData = function() {
	window.onload = function() {
		if(!eXo.contact.UIContactPortlet.getElementById("UIContacts")) return ;
		var uiContacts = eXo.contact.UIContactPortlet.getElementById("UIContacts");
		var portletFragment = eXo.core.DOMUtil.findAncestorByClass(uiContacts,"PORTLET-FRAGMENT");		
		eXo.webui.UIForm.submitForm(portletFragment.parentNode.id+ '#UIContacts','Refresh', true);		
	} ;
} ;

UIContactPortlet.prototype.showTagMenu = function(obj, event) {
	eXo.webui.UIPopupSelectCategory.show(obj, event);
	var uiPopupCategory = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div","UIPopupCategory") ;
	uiPopupCategory.style.left = - eXo.core.DOMUtil.findFirstDescendantByClass(uiPopupCategory,"div","UIRightClickPopupMenu").offsetWidth + "px" ;
} ;

UIContactPortlet.prototype.fitStringToWidth = function (id, str,index) {
	var td = document.getElementById(id);
	var width = td.offsetWidth;

	var span = document.createElement("span");
	span.className=td.className;
	span.style.display='inline';
	span.style.visibility = 'hidden';
	span.style.padding = '0px';
	document.body.appendChild(span);
	span.innerHTML = str;
	if (span.offsetWidth*2 > width) {
		var currentLengthPixel = span.offsetWidth*2;
		var currentLengthChars = str.length;
		while (currentLengthPixel > width) {
			currentLengthChars = currentLengthChars - 1;
			span.innerHTML = str.substring(0,currentLengthChars);
			currentLengthPixel = span.offsetWidth*2;
		}
		str = span.innerHTML;
		var a = eXo.core.DOMUtil.findDescendantsByTagName(td, 'a')[index-1];
		a.innerHTML = str + "...";
  }
  document.body.removeChild(span);
};

UIContactPortlet.prototype.getElementById = function(id){
	return eXo.core.DOMUtil.findDescendantById(this.portletNode,id);
}

/*
UIContactPortlet.prototype.setUpCheckboxCallback = function(obj){
	obj = (typeof(obj) == "string")? document.getElementById(obj) : obj ;
	var tbody = eXo.core.DOMUtil.findDescendantsByTagName(obj, "tbody")[0];
	eXo.core.EventManager.addEvent(tbody,"click",eXo.contact.UIContactPortlet.isChecked);
};

UIContactPortlet.prototype.isChecked = function(evt){
	var target = eXo.core.EventManager.getEventTargetByTagName(evt,"input");
	if(!target || target.checked) return ;
	var checkall = eXo.core.DOMUtil.findDescendantsByTagName(this.parentNode, "input")[0];
	checkall.checked = false ;
};*/

eXo.contact.UIContactPortlet = new UIContactPortlet() ;