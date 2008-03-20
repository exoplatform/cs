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
	var UIContextMenuCon = eXo.webui.UIContextMenuCon ;//eXo.contact.ContextMenu ;
	UIContextMenuCon.portletName = compid ;
	var config = {
		'preventDefault':false, 
		'preventForms':false
	} ;	
	UIContextMenuCon.init(config) ;
	UIContextMenuCon.attach(['UIContactList','VCardContent'], 'UIContactListPopuMenu') ;
	UIContextMenuCon.attach('PrivateAddressBook', 'UIAddressBookPopupMenu0') ;	
  UIContextMenuCon.attach('ShareAddressBook', 'UIAddressBookPopupMenu1') ;
  UIContextMenuCon.attach('PublicAddressBook', 'UIAddressBookPopupMenu2') ;
	UIContextMenuCon.attach('TagList', 'UITagPopupMenu') ;
} ;

UIContactPortlet.prototype.contactCallback = function(evt) {
	var UIContextMenuCon = eXo.webui.UIContextMenuCon ;
  var DOMUtil = eXo.core.DOMUtil ;
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var src = _e.srcElement || _e.target ;
	var tr = DOMUtil.findAncestorByTagName(src, "tr") ;
	var id = null ;
	if(tr != null) {
		var checkbox = DOMUtil.findFirstDescendantByClass(tr, "input", "checkbox") ;		
		id = checkbox.name ;
		//eXo.webui.UIContextMenuCon.changeAction(UIContextMenuCon.menuElement, id) ;
	} else {
		tr = DOMUtil.findAncestorByClass(src, "VCardContent") ;
		id = tr.getAttribute("id") ;
    //eXo.webui.UIContextMenuCon.changeAction(UIContextMenuCon.menuElement, id) ;
	}
  if(tr.getAttribute("type")) {
    var type = tr.getAttribute("type").toLowerCase() ;
    var actions = DOMUtil.findDescendantsByClass(UIContextMenuCon.menuElement, "div", "ItemIcon") ;
    var isDisable = null ;
    var len = actions.length ;
    if(type == "2") {
      for (var i = 0; i < len; i++) {
        isDisable = DOMUtil.hasClass(actions[i], "EditActionIcon") || DOMUtil.hasClass(actions[i], "ShareIcon") || DOMUtil.hasClass(actions[i], "MoveIcon") || DOMUtil.hasClass(actions[i], "DeleteContactIcon") ;
        if (isDisable == false) continue;
        if (!actions[i].parentNode.getAttribute("oldHref")) {
          actions[i].parentNode.setAttribute("oldHref", actions[i].parentNode.href);
          actions[i].parentNode.style.color = "#cccccc";
          actions[i].parentNode.href = "javascript:void(0);";
        }
      } 
    } else if (type == "1") {
    	for (var i = 0; i < len; i++) {
        isDisable = DOMUtil.hasClass(actions[i], "ShareIcon") ;
        if (isDisable == true) {
	        if (!actions[i].parentNode.getAttribute("oldHref")) {
	          actions[i].parentNode.setAttribute("oldHref", actions[i].parentNode.href);
	          actions[i].parentNode.style.color = "#cccccc";
	          actions[i].parentNode.href = "javascript:void(0);";
	        }		
				}else {
					if (actions[i].parentNode.getAttribute("oldHref")) {
	          actions[i].parentNode.href = actions[i].parentNode.getAttribute("oldHref");
	          actions[i].parentNode.removeAttribute("oldHref");
	          actions[i].parentNode.removeAttribute("style");
	        }
				}
      }    	
    } else {
      for (var i = 0; i < len; i++) {
        isDisable = DOMUtil.hasClass(actions[i], "EditActionIcon") || DOMUtil.hasClass(actions[i], "ShareIcon") || DOMUtil.hasClass(actions[i], "MoveIcon") || DOMUtil.hasClass(actions[i], "DeleteContactIcon")
        if (isDisable == false) continue;
        if (actions[i].parentNode.getAttribute("oldHref")) {
          actions[i].parentNode.href = actions[i].parentNode.getAttribute("oldHref");
          actions[i].parentNode.removeAttribute("oldHref");
          actions[i].parentNode.removeAttribute("style");
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
	
	//var isPublic = addressBook.getAttribute("addressType") ;	
	
	var menuItems = DOMUtil.findDescendantsByClass(UIContextMenuCon.menuElement, "div", "ItemIcon") ;
	var itemLength = menuItems.length ;	

//	if (isPublic && (isPublic.toLowerCase() == "2")) {
//		for(var i = 0 ; i < itemLength ; i ++) {
//			if (DOMUtil.hasClass(menuItems[i],"ShareIcon") || DOMUtil.hasClass(menuItems[i],"EditActionIcon")
//			|| DOMUtil.hasClass(menuItems[i],"DeleteIcon") || DOMUtil.hasClass(menuItems[i],"ImportContactIcon")
//			|| DOMUtil.hasClass(menuItems[i],"ContactIcon")) {
//				if (menuItems[i].parentNode.getAttribute("oldHref")) continue ;
//				menuItems[i].parentNode.setAttribute("oldHref", menuItems[i].parentNode.href) ;
//				menuItems[i].parentNode.href = "javascript: void(0) ;" ;
//				menuItems[i].parentNode.setAttribute("oldColor", DOMUtil.getStyle(menuItems[i].parentNode, "color")) ;
//				menuItems[i].parentNode.style.color = "#cccccc" ;
//			}
//		}
//	} else if(isPublic.toLowerCase() == "1") {
//			for(var i = 0 ; i < itemLength ; i ++) {
//				if (DOMUtil.hasClass(menuItems[i],"ShareIcon") || DOMUtil.hasClass(menuItems[i],"ImportAddressIcon") 
//				|| DOMUtil.hasClass(menuItems[i],"ImportContactIcon")|| DOMUtil.hasClass(menuItems[i],"ContactIcon")) {
//					if (menuItems[i].parentNode.getAttribute("oldHref")) continue ;
//					menuItems[i].parentNode.setAttribute("oldHref", menuItems[i].parentNode.href) ;
//					menuItems[i].parentNode.href = "javascript: void(0) ;" ;
//					menuItems[i].parentNode.setAttribute("oldColor", DOMUtil.getStyle(menuItems[i].parentNode, "color")) ;
//					menuItems[i].parentNode.style.color = "#cccccc" ;
//				} else {
//					if (!menuItems[i].parentNode.getAttribute("oldHref")) continue ;
//					menuItems[i].parentNode.href = menuItems[i].parentNode.getAttribute("oldHref") ;
//					menuItems[i].parentNode.style.color = menuItems[i].parentNode.getAttribute("oldColor") ;
//					menuItems[i].parentNode.removeAttribute("oldColor") ;
//					menuItems[i].parentNode.removeAttribute("oldHref") ;
//				}
//			}
//	}	else {
//		for(var i = 0 ; i < itemLength ; i ++) {
//			if (DOMUtil.hasClass(menuItems[i],"ShareIcon") || DOMUtil.hasClass(menuItems[i],"EditActionIcon") || DOMUtil.hasClass(menuItems[i],"DeleteIcon") || DOMUtil.hasClass(menuItems[i],"ImportContactIcon")) {
//				if (!menuItems[i].parentNode.getAttribute("oldHref")) continue ;
//				menuItems[i].parentNode.href = menuItems[i].parentNode.getAttribute("oldHref") ;
//				menuItems[i].parentNode.style.color = menuItems[i].parentNode.getAttribute("oldColor") ;
//				menuItems[i].parentNode.removeAttribute("oldColor") ;
//				menuItems[i].parentNode.removeAttribute("oldHref") ;
//			}
//		}
//	}
	
	var isDefault = addressBook.getAttribute("isDefault") ;
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
	} else {
    for(var i = 0 ; i < itemLength ; i ++) {
			if (DOMUtil.hasClass(menuItems[i],"EditActionIcon") || DOMUtil.hasClass(menuItems[i],"DeleteIcon")) {
				if (!menuItems[i].parentNode.getAttribute("oldHref")) continue ;
				menuItems[i].parentNode.href = menuItems[i].parentNode.getAttribute("oldHref") ;
				menuItems[i].parentNode.style.color = menuItems[i].parentNode.getAttribute("oldColor") ;
				menuItems[i].parentNode.removeAttribute("oldColor") ;
				menuItems[i].parentNode.removeAttribute("oldHref") ;
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
	var tagName = src.getAttribute("tagId") ;
	
	// hoang quang hung add
	var DOMUtil = eXo.core.DOMUtil ;
	var addressBook = (DOMUtil.hasClass(src, "ItemList")) ? src : DOMUtil.findAncestorByClass(src, "ItemList") ;
	var canPrint = addressBook.getAttribute("canPrint") ;
	var menuItems = DOMUtil.findDescendantsByClass(UIContextMenuCon.menuElement, "div", "ItemIcon") ;
	var itemLength = menuItems.length ;	
	if (canPrint == "true") {
		for(var i = 0 ; i < itemLength ; i ++) {
			if (DOMUtil.hasClass(menuItems[i],"PrintIcon")) {
				if (!menuItems[i].parentNode.getAttribute("oldHref")) continue ;
				menuItems[i].parentNode.href = menuItems[i].parentNode.getAttribute("oldHref") ;
				menuItems[i].parentNode.style.color = menuItems[i].parentNode.getAttribute("oldColor") ;
				menuItems[i].parentNode.removeAttribute("oldColor") ;
				menuItems[i].parentNode.removeAttribute("oldHref") ;
			}
		}
	} else {
		for(var i = 0 ; i < itemLength ; i ++) {
			if (DOMUtil.hasClass(menuItems[i],"PrintIcon")) {
				if (menuItems[i].parentNode.getAttribute("oldHref")) continue ;
				menuItems[i].parentNode.setAttribute("oldHref", menuItems[i].parentNode.href) ;
				menuItems[i].parentNode.href = "javascript: void(0) ;" ;
				menuItems[i].parentNode.setAttribute("oldColor", DOMUtil.getStyle(menuItems[i].parentNode, "color")) ;
				menuItems[i].parentNode.style.color = "#cccccc" ;
			}
		}
	}
	eXo.webui.UIContextMenuCon.changeAction(UIContextMenuCon.menuElement, tagName) ;
} ;

UIContactPortlet.prototype.printpreview = function (obj){
	var DOMUtil = eXo.core.DOMUtil ;
	var UIPortalApplication = document.getElementById("UIPortalApplication") ;
	var UIContactPreview = DOMUtil.findAncestorByClass(obj, "UIContactPreview") ;
	var div = document.createElement("div") ;
	var form = eXo.core.DOMUtil.findAncestorByTagName(obj, "form") ;
	var pirntLabel = DOMUtil.findFirstDescendantByClass(obj, 'div','ButtonMiddle') ;
	obj.href = "javascript:window.print() ;" ;
	if(obj.getAttribute("printLabel")) pirntLabel.innerHTML = obj.getAttribute("printLabel") ;
	if(obj.getAttribute("onclick")) obj.removeAttribute("onclick") ;
	eXo.contact.UIContactPortlet.printList(form.id) ;
//	div.className = "UIContactPortlet" ;
//	div.appendChild(UIContactPreview.cloneNode(true)) ;
//	UIPortalApplication.style.display = "none" ;
//	var bg = document.body.style.background ;
//	document.body.style.background = "transparent" ;
//	document.body.appendChild(div) ;
//	var button = DOMUtil.findDescendantsByClass(div, "a", "ActionButton") ;
//	button[0].href = "#" ;
//	button[0].onclick = function(){
//		document.body.removeChild(div) ;
//		UIPortalApplication.style.display = "block" ;
//		document.body.style.background = bg ;
//	}
//	DOMUtil.findFirstDescendantByClass(button[1], 'div','ButtonMiddle').style.display = "block" ;
//	button[2].style.display = "none" ;
//	
} ;

UIContactPortlet.prototype.adddressPrint = function (){
	var DOMUtil = eXo.core.DOMUtil ;
	var UIPortalApplication = document.getElementById("UIPortalApplication") ;
	var UIContactContainer = document.getElementById("UIContactContainer") ;
	var div = document.createElement("div") ;
	div.className = "UIPrintContainer UIContactPortlet" ;
	var printContainer = UIContactContainer.cloneNode(true) ;
	printContainer.removeAttribute("class") ;
	div.appendChild(printContainer) ;
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
	eXo.core.DOMUtil.removeElement(UIPrintContainer) ;
	UIPortalApplication.style.display = "block" ;
	document.body.style.background = eXo.contact.UIContactPortlet.pageBackground ;
	eXo.contact.UIContactPortlet.pageBackground = null ;
} ;

UIContactPortlet.prototype.cancelPrintList = function (){
	var UIPrintContainer = eXo.core.DOMUtil.findFirstDescendantByClass(document.body,"div", "UIPrintContainer") ;
	var UIPortalApplication = document.getElementById("UIPortalApplication") ;
	if(UIPrintContainer) eXo.core.DOMUtil.removeElement(UIPrintContainer) ;
	UIPortalApplication.style.display = "block" ;
	
//	document.body.style.background = eXo.contact.UIContactPortlet.pageBackground ;
//	eXo.contact.UIContactPortlet.pageBackground = null ;
} ;

UIContactPortlet.prototype.printList = function (obj){
	if(typeof(obj) == "string") obj = document.getElementById(obj) ;
	var printContainer = obj.cloneNode(true) ;
	var UIAction = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIAction") ;
	var div = document.createElement("div") ;
	div.className = "UIContactPortlet UIPrintContainer" ;
	div.appendChild(printContainer) ;
	var UIPortalApplication = document.getElementById("UIPortalApplication") ;
	UIPortalApplication.style.display = "none" ;
	document.body.appendChild(div) ;
	eXo.core.DOMUtil.removeElement(UIAction) ;
} ;

/**
 * 
 * @author Lam Nguyen
 * 
 */
UIContactPortlet.prototype.checkLayout = function() {
  try {
    var Browser = eXo.core.Browser ;
    var layout1State = parseInt(eXo.core.Browser.getCookie('contactLayout1'));
    var layout2State = parseInt(eXo.core.Browser.getCookie('contactLayout2'));
    var layout3State = parseInt(eXo.core.Browser.getCookie('contactLayout3'));    
    var layout4state = parseInt(eXo.core.Browser.getCookie('contactLayout4'));
    var layout5state = parseInt(eXo.core.Browser.getCookie('contactLayout5'));
    if(layout1State == 0) {
      eXo.contact.UIContactPortlet.switchLayout(1);
    }
    
    if(layout2State == 0) {
      eXo.contact.UIContactPortlet.switchLayout(2);
    }
    
    if(layout3State == 0) {   
      eXo.contact.UIContactPortlet.switchLayout(3);
    }
    
    if(layout4state == 0 && layout5state == 0) {
      eXo.contact.UIContactPortlet.switchLayout(4);  
    }
  }
  catch(e) {
    window.alert(e.message);
  }
}

/**
 * 
 *  @author Lam Nguyen
 *  
 *  @param (Object) layout
 */
UIContactPortlet.prototype.switchLayout = function(layout) {
  var Browser = eXo.core.Browser;
  var DOMUtil = eXo.core.DOMUtil;
  layout = parseInt(layout);
  var contactLayout1 = DOMUtil.findFirstDescendantByClass(document.getElementById("UIContactPortlet"), "div", "UINavigationContainer");                                                   
  var contactLayout2 = document.getElementById("UIAddressBooks");
  var contactLayout3 = document.getElementById("UITags");
  var contactLayout4 = DOMUtil.findFirstDescendantByClass(document.getElementById("UIContactPortlet"), "div", "UIContactPreview");
  var contactLayout5 = DOMUtil.findFirstDescendantByClass(document.getElementById("UIContactPortlet"), "div", "ResizeReadingPane");
  var panelWorking = document.getElementById('UIContactContainer');
  var objRoot = document.getElementById("defaultroot");
  var showCheckedMenu = false;
  var isWelcome = eXo.core.DOMUtil.findFirstDescendantByClass(contactLayout4, "div", "UIWelcomeContact");
  switch(layout) {
    case 0 : 
      if(contactLayout1.style.display != "block") {
        contactLayout1.style.display = "block";
      } 
      if(contactLayout2.style.display != "block") {
        contactLayout2.style.display = "block";
      }
      if(contactLayout3.style.display != "block") {
        contactLayout3.style.display = "block";             
      }
      if(contactLayout4) {
        if(contactLayout4.style.display != "block") {
          contactLayout4.style.display = "block";
        }
      }
      if(contactLayout5)  {
        if(contactLayout5.style.display != "block") {
          contactLayout5.style.display = "block";
        }
      }
      panelWorking.style.marginLeft = "225px";   
      Browser.setCookie("contactLayout1", "1", 30);
      Browser.setCookie("contactLayout2", "1", 30);
      Browser.setCookie("contactLayout3", "1", 30);
      Browser.setCookie("contactLayout4", "1", 30); 
      Browser.setCookie("contactLayout5", "1", 30);     
      this.addCheckedIcon(1,true);
      this.addCheckedIcon(2,true);
      this.addCheckedIcon(3,true);
      this.addCheckedIcon(4, true);
      return;
    case 1 :
      if(contactLayout1.style.display == "none") {
        contactLayout1.style.display = "block";
        panelWorking.style.marginLeft = "225px";
        Browser.setCookie("contactLayout1", "1", 30);
        showCheckedMenu = true;
      } else {
        contactLayout1.style.display = "none";  
        panelWorking.style.marginLeft = "0px";
        Browser.setCookie("contactLayout1", "0", 30);
      }
      break;
    case 2 : 
      if(contactLayout2.style.display == "none") {
        contactLayout2.style.display = "block";
        showCheckedMenu = true;        
        if(contactLayout1.style.display == "none") {
           panelWorking.style.marginLeft = "0px";
        } else {
           panelWorking.style.marginLeft ="225px";
        }
        Browser.setCookie("contactLayout2", "1", 30);
      } else {
        contactLayout2.style.display = "none";
        Browser.setCookie("contactLayout2", "0", 30);
        if(contactLayout1.style.display == "none") {
           panelWorking.style.marginLeft = "0px";
        }        
      }
      break;
    case 3 : 
      if(contactLayout3.style.display == "none") {
        contactLayout3.style.display="block";
        showCheckedMenu = true;
        if(contactLayout1.style.display == "none") {
           panelWorking.style.marginLeft = "0px";
        } else {
           panelWorking.style.marginLeft ="225px";
        } 
        Browser.setCookie("contactLayout3", "1", 30);
      } else {
        contactLayout3.style.display = "none";
        Browser.setCookie("contactLayout3", "0", 30);
        if(contactLayout1.style.display == "none") {
           panelWorking.style.marginLeft = "0px";
        }
      }      
      break;
    case 4 : 
      if( objRoot != null) {
        if(contactLayout4 && contactLayout5){
          if(contactLayout4.style.display == "none" && contactLayout5.style.display == "none") {
            contactLayout4.style.display = "block";
            contactLayout5.style.display = "block";
            Browser.setCookie("contactLayout4", "1", 30);
            Browser.setCookie("contactLayout5", "1", 30);
            showCheckedMenu = true;
          } else {
            if(!isWelcome) {
              contactLayout4.style.display = "none";
              contactLayout5.style.display = "none";
              Browser.setCookie("contactLayout4", "0", 30);
              Browser.setCookie("contactLayout5", "0", 30);
            }
          } 
        }
      }
  }
  this.addCheckedIcon(layout, showCheckedMenu);
}
UIContactPortlet.prototype.showImMenu = function(obj, event) {
	var menuItems = eXo.core.DOMUtil.findDescendantsByClass(obj, "span", "MenuItem") ;
	var len = menuItems.length ;
	for(var i = 0 ; i < len ; i++) {
		if (menuItems[i].style.display != "none") break ;
	}
	if (i == len) return ;
	eXo.webui.UIPopupSelectCategory.show(obj, event) ;
}

/**
 * @author Lam Nguyen
 * 
 * @param {Object} layout
 */
UIContactPortlet.prototype.addCheckedIcon = function(layout, visible) {
  layout = parseInt(layout);
  var itemIcon = eXo.core.DOMUtil.findDescendantsByClass(
                document.getElementById("customLayoutViewMenu"), "div", "ItemIcon")[layout];
  if(visible) {
    itemIcon.className = 'ItemIcon CheckedMenu';
  } else {
    itemIcon.className = 'ItemIcon';
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
};

/**
 * 
 *  @author Lam Nguyen
 * 
 */
UIContactPortlet.prototype.checkLayoutView = function() {
  var objRoot = document.getElementById("UIContactPortlet");
  var objPopup = eXo.core.DOMUtil.findFirstDescendantByClass(objRoot, "div", "TypeViewContactPortlet");    
  var objListView = null;
  var objVCards = null;
  if(eXo.core.DOMUtil.findFirstDescendantByClass(objPopup, "div", "ContactListIcon")) {
    objListView =  eXo.core.DOMUtil.findFirstDescendantByClass(objPopup, "div", "ContactListIcon");    
  } else {
    objListView = eXo.core.DOMUtil.findFirstDescendantByClass(objPopup, "div", "ContactListIconSelected");
  }
  
  if(eXo.core.DOMUtil.findFirstDescendantByClass(objPopup, "div", "ContactIcon")) {
    objVCards =  eXo.core.DOMUtil.findFirstDescendantByClass(objPopup, "div", "ContactIcon");    
  } else {
    objVCards = eXo.core.DOMUtil.findFirstDescendantByClass(objPopup, "div", "ContactIconSelected");
  }
  
  var objListMenuItem = eXo.core.DOMUtil.findAncestorByClass(objListView, "MenuItem");
  var objVCardsMenuItem = eXo.core.DOMUtil.findAncestorByClass(objVCards, "MenuItem");  

  if (eXo.core.DOMUtil.findDescendantById(objRoot, "UIListUsers")) {
    objListMenuItem.style.backgroundColor = "#dee4f2";
    objVCardsMenuItem.style.backgroundColor = "#f6f6f6";
  } else {
    objListMenuItem.style.backgroundColor = "#f6f6f6";
    objVCardsMenuItem.style.backgroundColor = "#dee4f2";
  }
};

UIContactPortlet.prototype.showPopupCustomLayoutView = function(obj, evt) {
  var root = document.getElementById("UIContactPortlet");
  var objWelcome = eXo.core.DOMUtil.findFirstDescendantByClass(root, "div", "UIWelcomeContact");
  var objDetails = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "ContactDetailsMenuItem");
  var objVCard = eXo.core.DOMUtil.findFirstDescendantByClass(root, "div", "UIVCards");
  if(objWelcome || objVCard) {
    objDetails.style.display = "none";
  } else {
    objDetails.style.display = "block";
  }
  eXo.webui.UIPopupSelectCategory.show(obj, evt);
};

eXo.contact.UIContactPortlet = new UIContactPortlet() ;