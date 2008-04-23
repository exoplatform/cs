function UIMailPortlet(){
};

UIMailPortlet.prototype.showContextMenu = function(compid) {	
	var UIContextMenuMail = eXo.webui.UIContextMenuMail ; //eXo.contact.ContextMenu ;
	UIContextMenuMail.portletName = compid ;
	var config = {
		'preventDefault':false, 
		'preventForms':false
	} ;	
	UIContextMenuMail.init(config) ;
	UIContextMenuMail.attach('MessageItem', 'UIMessagePopupMenu') ;
	UIContextMenuMail.attach('FolderLink', 'UIFolderListPopupMenu') ;
	UIContextMenuMail.attach('IconTagHolder', 'UITagListPopupMenu') ;
	UIContextMenuMail.attach('InboxIcon', 'UIDefaultFolderPopupMenu') ;
	UIContextMenuMail.attach('DraftsIcon', 'UIDefaultFolderPopupMenu') ;
	UIContextMenuMail.attach('SentIcon', 'UIDefaultFolderPopupMenu') ;
	UIContextMenuMail.attach('SpamIcon', 'UIDefaultFolderPopupMenu') ;
	UIContextMenuMail.attach('TrashIcon', 'UITrashFolderPopupMenu') ;
} ;

UIMailPortlet.prototype.msgPopupMenuCallback = function(evt) {
	var UIContextMenuMail = eXo.webui.UIContextMenuMail ;
	var DOMUtil = eXo.core.DOMUtil ;
	var _e = window.event || evt ;
	//_e.cancelBubble = true ;
	var src = null ;
	if (UIContextMenuMail.IE) {
		src = _e.srcElement;
	} else {
		src = _e.target;
	}
	if (src.nodeName != "tr")
		src = eXo.core.DOMUtil.findAncestorByTagName(src, "tr");
		
	var check = DOMUtil.findDescendantsByClass(src, "input", "checkbox") ;
	if (check[0].checked == false) {
		var tbody = DOMUtil.findAncestorByTagName(src, "tbody") ;
		var checkboxes = DOMUtil.findDescendantsByClass(tbody, "input", "checkbox") ;
		var len = checkboxes.length ;
		for(var i = 0 ; i < len ; i++) {
			if (checkboxes[i].checked) {
				var tr = DOMUtil.findAncestorByTagName(checkboxes[i] , "tr");
				if (tr.className.indexOf("SelectedItem") > -1) {
					tr.className  = tr.className.replace("SelectedItem", "");
				}
				checkboxes[i].checked = false;
			}
		}
		check[0].checked = true ;
		var str = DOMUtil.findAncestorByTagName(check[0] , "tr");
		str.className += " SelectedItem";
	}
	id = src.getAttribute("msgId");
	eXo.webui.UIContextMenuMail.changeAction(UIContextMenuMail.menuElement, id) ;
} ;

UIMailPortlet.prototype.defaultFolderPopupMenuCallback = function(evt) {
	var UIContextMenuMail = eXo.webui.UIContextMenuMail ;
	var _e = window.event || evt ;
	//_e.cancelBubble = true ;
	var src = null ;
	if (UIContextMenuMail.IE) {
		src = _e.srcElement;
	} else {
		src = _e.target;
	}
	if (src.nodeName != "A")
		src = src.parentNode;
		
	folder = src.getAttribute("folder");
	eXo.webui.UIContextMenuMail.changeAction(UIContextMenuMail.menuElement, folder) ;
} ;

UIMailPortlet.prototype.tagListPopupMenuCallback = function(evt) {
	var UIContextMenuMail = eXo.webui.UIContextMenuMail ;
	var _e = window.event || evt ;
	//_e.cancelBubble = true ;
	var src = null ;
	if (UIContextMenuMail.IE) {
		src = _e.srcElement;
	} else {
		src = _e.target;
	}
	if (src.nodeName != "A")
		src = src.parentNode;
		
	tagName = src.getAttribute("tagId");
	eXo.webui.UIContextMenuMail.changeAction(UIContextMenuMail.menuElement, tagName) ;
} 

UIMailPortlet.prototype.readMessage = function() {} ;

UIMailPortlet.prototype.showPrintPreview = function(obj1) {
	document.getElementById("UIPortalApplication").style.display = "none";
	var uiMailPortletNode = document.createElement('div') ;
	uiMailPortletNode.className = 'UIMailPortlet' ;
	var mailWorkingWorkspaceNode = document.createElement('div') ;
	mailWorkingWorkspaceNode.className = 'MailWorkingWorkspace' ;
	var uiMessagePreviewNode = document.createElement('div') ;
	uiMessagePreviewNode.className = 'UIMessagePreview' ;
	uiMessagePreviewNode.appendChild(obj1.cloneNode(true)) ;
	mailWorkingWorkspaceNode.appendChild(uiMessagePreviewNode) ;
	uiMailPortletNode.appendChild(mailWorkingWorkspaceNode) ;

  // Fit UIMailPortlet to window.
  with(uiMessagePreviewNode.style) {
    width = '100%' ;
    height = '100%' ;
    position = 'absolute' ;
    top = '0px' ;
    left = '0px' ;
  }
	document.body.appendChild(uiMailPortletNode) ;
} ;

UIMailPortlet.prototype.printMessage = function() {
	window.print()
} ;

UIMailPortlet.prototype.closePrint = function() {
	var DOMUtil = eXo.core.DOMUtil ;
    var uiPortalApplication = document.getElementById("UIPortalApplication");
    uiPortalApplication.style.display = "block" ;	
	for(var i = 0 ; i < document.body.childNodes.length ; i++) {
		if(document.body.childNodes[i].className == "UIMailPortlet") DOMUtil.removeElement(document.body.childNodes[i]) ;		
	}
	//document.body.removeChild(uiMailPortlet);
} ;

UIMailPortlet.prototype.switchLayout = function(layout) {	
	var Browser = eXo.core.Browser ;
	layout = parseInt(layout) ;
	var	layout1 = document.getElementById("UINavigationContainer") ;
	var	layout2 = document.getElementById("uiMessageListResizableArea") ;
	var	layout3 = document.getElementById("SpliterResizableArea") ;
	var resizePane = document.getElementById("ResizeReadingPane");
	var workingarea = document.getElementById("UIMessageArea");
	var uiMessageGrid = document.getElementById("uiMessageGrid") ;
	var layoutState = false;
    
	switch(layout) {
		case 0 :
			if (layout1.style.display == "none") {
				layout1.style.display = "block" ;
				workingarea.style.marginLeft = "225px"	;
			}
			
			if (layout2.style.display == "none") {
				layout2.style.display = "block" ;
			}
			
			if (layout3.style.display == "none") {
				layout3.style.display = "block" ;
			}
			
			if (resizePane.style.display == "none") {
				resizePane.style.display = "block";
			}
			layout2.style.height = "220px" ;
			Browser.setCookie("layout1", "1", 30);
			Browser.setCookie("layout2", "1", 30);
			Browser.setCookie("layout3", "1", 30);
			break ;
		case 1 :
			if (layout1.style.display == "none") {
				layout1.style.display = "block" ;
				Browser.setCookie("layout1", "1", 30);
				workingarea.style.marginLeft = "225px"	;		
        layoutState = true;
			} else {
				layout1.style.display = "none" ;
				Browser.setCookie("layout1", "0", 30);
				if(layout1.style.display == "none") {
					workingarea.style.marginLeft = "0px"	;
				}
			}
			break ;
		case 2 :
			if (layout2.style.display == "none") {
				layout2.style.display = "block" ;
				Browser.setCookie("layout2", "1", 30);
				if (layout3.style.display != "none" && layout2.style.display != "none") {
					resizePane.style.display = "block";
				}
        layoutState = true;
			} else {				
				layout2.style.display = "none" ;
				resizePane.style.display = "none";
				Browser.setCookie("layout2", "0", 30);
			}
			break ;
		case 3 :
			if (layout3.style.display == "none") {
				layout3.style.display = "block" ;
				//layout2.style.height = "220px" ;
				Browser.setCookie("layout3", "1", 30);
				if (layout3.style.display != "none" && layout2.style.display != "none") {
				  uiMessageGrid.style.height = "200px" ;
					resizePane.style.display = "block";
				}
        layoutState = true;
			} else {				
				uiMessageGrid.style.height = "100%" ;
				layout3.style.display = "none" ;	
				layout2.style.height = "100%" ;			
		    resizePane.style.display = "none";
        Browser.setCookie("layout3", "0", 30);
			}
			break ;
	}
  var csMailLayoutSwitchMenuNode = document.getElementById('_CSMailLayoutSwitchMenu');
  var menuItems = eXo.core.DOMUtil.findDescendantsByClass(csMailLayoutSwitchMenuNode, 'div', 'MenuItem');
  var menuItemTexts = eXo.core.DOMUtil.findDescendantsByClass(csMailLayoutSwitchMenuNode, 'div', 'ItemIcon');
  var fontWeight = false;
  for (var i=0; i<menuItems.length; i++) {
    if (menuItemTexts[i]) {
      if (layout == 0 ||
          (layoutState && i == layout)) {
        menuItemTexts[i].innerHTML = menuItemTexts[i].innerHTML.replace('Show', 'Hide');        
      } else if (!layoutState && i == layout){        
        menuItemTexts[i].innerHTML = menuItemTexts[i].innerHTML.replace('Hide', 'Show');
      }
    }
  }
} ;

UIMailPortlet.prototype.checkLayout = function() {
  var layout1State = parseInt(eXo.core.Browser.getCookie('layout1'));
  var layout2State = parseInt(eXo.core.Browser.getCookie('layout2'));
  var layout3State = parseInt(eXo.core.Browser.getCookie('layout3'));
  if (layout1State == 0) {    
    eXo.mail.UIMailPortlet.switchLayout(1);
  }
  if (layout2State == 0) {    
    eXo.mail.UIMailPortlet.switchLayout(2);
  }
  if (layout3State == 0) {    
    eXo.mail.UIMailPortlet.switchLayout(3);
  }
} ;

UIMailPortlet.prototype.showHideAddMoreAddress = function(add) {	
	var elm = document.getElementById(add);
	if (elm.style.display == "none") {
		elm.style.display = "" ;
	}
} ; 

UIMailPortlet.prototype.showHidePreviewPane = function(layout) {	
	var Browser = eXo.core.Browser ;
    var	uiMessageList = document.getElementById("uiMessageListResizableArea") ;
	var	previewPane = document.getElementById("SpliterResizableArea") ;
	var resizePane = document.getElementById("ResizeReadingPane");
	var actionReadingPane = document.getElementById("ActionReadingPane");
	if (uiMessageList.style.display == "block") {
		uiMessageList.style.display = "none";
		resizePane.style.display = "none";
		actionReadingPane.className = "MinimumReadingPane";
		Browser.setCookie("uiMessageListResizableArea", "none", 30)
		Browser.setCookie("ResizeReadingPane", "none", 30)
	} else {
		uiMessageList.style.display = "block";
		resizePane.style.display = "block";
		actionReadingPane.className = "MaximizeReadingPane";
		Browser.setCookie("uiMessageListResizableArea", "block", 30)
		Browser.setCookie("ResizeReadingPane", "block", 30)
	}
}

UIMailPortlet.prototype.showHideMessageHeader = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
	var decorator = DOMUtil.findAncestorByClass(obj, "DecoratorBox");
	var colapse = DOMUtil.findDescendantById(decorator, "CollapseMessageAddressPreview");
	var expand = DOMUtil.findDescendantById(decorator, "MessageAddressPreview");
	if (colapse.style.display == "none") {
		expand.style.display = "none";
		colapse.style.display = "block"
		obj.innerHTML = "Show details";
	} else {
		colapse.style.display = "none"
		expand.style.display = "block";
		obj.innerHTML = "Hide details";
	}
  var icons = eXo.core.DOMUtil.findDescendantsByClass(obj.parentNode, 'div', 'DownArrow1Icon') ;
  if (icons.length > 0) {
    icons[0].className = 'NextArrow1Icon' ;
  } else {
    icons = eXo.core.DOMUtil.findDescendantsByClass(obj.parentNode, 'div', 'NextArrow1Icon') ;
    if (icons.length > 0) {
      icons[0].className = 'DownArrow1Icon' ;
    }
  }
} ;

UIMailPortlet.prototype.showHideMessageDetails = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var paneDetails = DOMUtil.findAncestorByClass(obj, "ReadingPaneDetails");
    var expands = DOMUtil.findDescendantsByClass(paneDetails, "div", "ExpandMessage");
    var numberExpand = 0;
    for (var i = 0; i < expands.length; i++) {
    	if (expands[i].style.display == "block") numberExpand++;
    }
	var decorator = DOMUtil.findAncestorByClass(obj, "DecoratorBox");
	if ((obj.id == "CollapseMessageAddressPreview") && numberExpand > 1){
		var expand = DOMUtil.findFirstDescendantByClass(decorator, "div", "ExpandMessage");
		var collapse = DOMUtil.findFirstDescendantByClass(decorator, "div", "CollapseMessage");
	    expand.style.display = "none";
	    collapse.style.display = "block";
	} else if (obj.id == "CollapseMessage") {
	    var expand = DOMUtil.findNextElementByTagName(obj, "div");
	    obj.style.display = "none";
	    expand.style.display= "block";
	}
}

UIMailPortlet.prototype.isAllday = function(form) {
	try{
		if (typeof(form) == "string") form = document.getElementById(form) ;		
		if (form.tagName.toLowerCase() != "form") {
			form = eXo.core.DOMUtil.findDescendantsByTagName(form, "form") ;
		}
		for(var i = 0 ; i < form.elements.length ; i ++) {
			if(form.elements[i].getAttribute("name") == "allDay") {
				eXo.mail.UIMailPortlet.showHideTime(form.elements[i]) ;
				break ;
			}
		}
	}catch(e){
		
	}
} ;
UIMailPortlet.prototype.showHideTime = function(chk) {
	var DOMUtil = eXo.core.DOMUtil ;
	if(chk.tagName.toLowerCase() != "input") {
		chk = DOMUtil.findFirstDescendantByClass(chk, "input", "checkbox") ;
	}
	var selectboxes = DOMUtil.findDescendantsByTagName(chk.form, "select") ;
	var fields = new Array() ;
	var len = selectboxes.length ;
	for(var i = 0 ; i < len ; i ++) {
		if((selectboxes[i].getAttribute("name") == "toTime") || (selectboxes[i].getAttribute("name") == "fromTime")) {
			fields.push(selectboxes[i]) ;
		}
	}
	eXo.mail.UIMailPortlet.showHideField(chk, fields) ;
} ;

UIMailPortlet.prototype.showHideField = function(chk,fields) {
	var display = "" ;
	if (typeof(chk) == "string") chk = document.getElementById(chk) ;
	display = (chk.checked) ? "hidden" : "visible" ;
	var len = fields.length ;
	for(var i = 0 ; i < len ; i ++) {
		fields[i].style.visibility = display ;i
	}
} ;

UIMailPortlet.prototype.collapseExpandFolder = function(obj, folderState) {
	var DOMUtil = eXo.core.DOMUtil;
	var colExpContainerNode = DOMUtil.findNextElementByTagName(obj, "div");
	
	var objClass = obj.className;
  var folderId = obj.getAttribute('folder');
  if (!folderState) {
    if (objClass.indexOf(" OpenFolder") != -1) { 
      obj.className = objClass.replace('OpenFolder', 'CloseFolder');
      folderState = '0';
    } else if (objClass.indexOf(" CloseFolder") != -1) { 
      obj.className = objClass.replace('CloseFolder', 'OpenFolder');
      folderState = '1';
    }
  } else if (folderState == '1') {
    obj.className = objClass.replace('CloseFolder', 'OpenFolder');
  } else if (folderState == '0') {
    obj.className = objClass.replace('OpenFolder', 'CloseFolder');
  }
  
  var collapseContainerNode = DOMUtil.findAncestorByClass(obj, 'Collapse');
  while (collapseContainerNode) {
    collapseContainerNode.className = 'Expand';
    var tmpNode = DOMUtil.findPreviousElementByTagName(collapseContainerNode, 'div');
    if (tmpNode) {
      tmpNode.className = tmpNode.className.replace('CloseFolder', 'OpenFolder');
    }
    collapseContainerNode = DOMUtil.findAncestorByClass(collapseContainerNode, 'Collapse');
  }
    
  if (colExpContainerNode != null && 
  	  colExpContainerNode.className &&
  	  (colExpContainerNode.className.indexOf('Collapse') != -1 || colExpContainerNode.className.indexOf('Expand') != -1)) {
  	if (folderState == '0') {
  		colExpContainerNode.className = "Collapse" ;
  	} else if (folderState == '1'){
  		colExpContainerNode.className = "Expand" ;
  	} 
	}
  eXo.mail.UIMailPortlet.updateFolderState(folderId, folderState);
};

UIMailPortlet.prototype.updateFolderState = function(folderId, folderState) {
  if (!this.uiFolderContainerNode) {
    this.uiFolderContainerNode = document.getElementById('UIFolderContainer');
  }
  // Save state to cookie
  var dateExpire = new Date();
  dateExpire.setYear(dateExpire.getYear() + 49);
  eXo.core.Browser.setCookie('cs.mail.lastfoldershow', folderId, dateExpire);
  eXo.core.Browser.setCookie('cs.mail.folderstate', folderState, dateExpire);
};

UIMailPortlet.prototype.restoreFolderState = function() {
  var folderId = eXo.core.Browser.getCookie('cs.mail.lastfoldershow');
  if (!folderId) {
    return;
  }
  var folderState = eXo.core.Browser.getCookie('cs.mail.folderstate');
  this.uiFolderContainerNode = document.getElementById('UIFolderContainer');
  var folderNodes = eXo.core.DOMUtil.findDescendantsByClass(this.uiFolderContainerNode, 'div', 'Folder');
  for (var i=0; i<folderNodes.length; i++) {
    var folderIdTmp = folderNodes[i].getAttribute('folder');
    if (folderId == folderIdTmp) {
      this.collapseExpandFolder(folderNodes[i], folderState);
      break;
    }
  }
};

UIMailPortlet.prototype.resizeIframe = function(textAreaId, frameId, styleExpand, contentType) {
	var frame = document.getElementById(frameId) ;
	var textAreas = document.getElementById(textAreaId) ;
	var expandMessage = eXo.core.DOMUtil.findAncestorByClass(frame, "ExpandMessage");
	var str = textAreas.value ;
	if (contentType.indexOf("text/plain") > -1) str = str.replace(/</g, "&lt;").replace(/>/g, "&gt;").replace(/\n/g, "<br>") ;
	var doc = frame.contentWindow.document ;
	var isDesktop = (document.getElementById("UIPageDesktop") != null) ? true : false;
	doc.open();
	doc.write(str);
	doc.close();
	if (isDesktop) {
		frame.style.height = "100%";
		if(!eXo.core.Browser.isFF()) frame.style.width = "96%";
	} else {
		if (eXo.core.Browser.isFF()) {
			doc.body.style.visibility = true;
			frame.style.height = doc.body.offsetHeight  + 20 + "px" ;
		} else {
			var docHt = 0, sh, oh;
			if (doc.height) {
				docHt = doc.height;
			} else if (doc.body) {
				if (doc.body.scrollHeight) docHt = sh = doc.body.scrollHeight;
				if (doc.body.offsetHeight) docHt = oh = doc.body.offsetHeight;
				if (sh && oh) docHt = Math.max(sh, oh);
			}
			frame.style.width = "96%";
			frame.style.height = "auto"; 
			frame.style.height = docHt + 20 + "px"; 
		}
	}
	expandMessage.style.display = styleExpand ;
} ;

UIMailPortlet.prototype.showMenu = function(obj, evt){
	if(!evt) evt = window.event ;
   evt.cancelBubble = true ;
   var DOMUtil = eXo.core.DOMUtil ;
	 var UIMailPortlet = eXo.mail.UIMailPortlet ;
   var uiPopupCategory = DOMUtil.findFirstDescendantByClass(obj, 'div', 'UIPopupCategory') ;
   if (!uiPopupCategory) return ;
	 if(UIMailPortlet.menuElement) {
	   if(UIMailPortlet.menuElement.style.display == "none") {
	       eXo.webui.UIPopupSelectCategory.hide() ;
	       UIMailPortlet.menuElement.style.display = "block" ;
	       eXo.core.DOMUtil.listHideElements(UIMailPortlet.menuElement) ; 
	   } else UIMailPortlet.menuElement.style.display = "none" ;
	 }
	 UIMailPortlet.swapMenu(uiPopupCategory,obj) ;
} ;

UIMailPortlet.prototype.showView = function(obj, evt) {
   if(!evt) evt = window.event ;
   evt.cancelBubble = true ;
   var DOMUtil = eXo.core.DOMUtil ;
	 var UIMailPortlet = eXo.mail.UIMailPortlet ;
   var uiPopupCategory = DOMUtil.findFirstDescendantByClass(obj, 'div', 'UIPopupCategory') ;
   if (!uiPopupCategory) return ;
	 if(UIMailPortlet.menuElement) {
	   if(UIMailPortlet.menuElement.style.display == "none") {
	       eXo.webui.UIPopupSelectCategory.hide() ;
	       UIMailPortlet.menuElement.style.display = "block" ;
	       eXo.core.DOMUtil.listHideElements(UIMailPortlet.menuElement) ; 
	   } else UIMailPortlet.menuElement.style.display = "none" ;
	 }
	 UIMailPortlet.swapMenu(uiPopupCategory,obj) ;
   var uiRightPopupMenuContainer = DOMUtil.findFirstDescendantByClass(UIMailPortlet.menuElement, 'div', 'UIRightPopupMenuContainer') ;
   var actions = DOMUtil.findChildrenByClass(uiRightPopupMenuContainer,"a", "MenuItem") ;
   actions[0].onmouseover = UIMailPortlet.showSubmenu ;
   actions[0].onmouseout = UIMailPortlet.hideSubmenu ;
   eXo.mail.UIMailPortlet.subMenu = DOMUtil.findNextElementByTagName(actions[0], "div") ;
   eXo.mail.UIMailPortlet.subMenu.style.left = actions[0].offsetWidth + "px" ;
   eXo.core.DOMUtil.listHideElements(UIMailPortlet.subMenu) ;
} ;

UIMailPortlet.prototype.showSubmenu = function() {
  window.clearTimeout(eXo.mail.UIMailPortlet.timeOutSubmenu) ;
  eXo.mail.UIMailPortlet.subMenu.style.display = "block" ;
} ;

UIMailPortlet.prototype.hideSubmenu = function() {
  eXo.mail.UIMailPortlet.timeOutSubmenu = window.setTimeout('eXo.mail.UIMailPortlet.subMenu.style.display = "none"', 100) ;
  eXo.mail.UIMailPortlet.subMenu.onmouseover = eXo.mail.UIMailPortlet.showSubmenu ;
  eXo.mail.UIMailPortlet.subMenu.onmouseout = eXo.mail.UIMailPortlet.hideSubmenu ;
} ;

UIMailPortlet.prototype.swapMenu = function(oldmenu, clickobj) {
  var DOMUtil = eXo.core.DOMUtil ;
	var Browser = eXo.core.Browser ;
	var UIMailPortlet = eXo.mail.UIMailPortlet ;
  var uiDesktop = document.getElementById("UIPageDesktop") ;
  var uiWorkSpaceWidth = (document.getElementById("UIControlWorkspace"))? document.getElementById("UIControlWorkspace").offsetWidth : 0 ;
	uiWorkSpaceWidth = (document.all) ? 2*uiWorkSpaceWidth : uiWorkSpaceWidth ;
  var menuX = Browser.findPosX(clickobj) - uiWorkSpaceWidth ;
	var menuY = Browser.findPosY(clickobj) + clickobj.offsetHeight ;
  if(uiDesktop) {
  	var portlet = DOMUtil.findAncestorByClass(document.getElementById(eXo.webui.UIContextMenuMail.portletName), "UIResizableBlock") ;
    var uiWindow = DOMUtil.findAncestorByClass(portlet, "UIWindow") ;
    menuX = menuX - uiWindow.offsetLeft  -  portlet.scrollLeft ;
    menuY = menuY - uiWindow.offsetTop  -  portlet.scrollTop ;
  }
  if(document.getElementById("tmpMenuElement")) DOMUtil.removeElement(document.getElementById("tmpMenuElement")) ;
	var tmpMenuElement = oldmenu.cloneNode(true) ;
	tmpMenuElement.setAttribute("id","tmpMenuElement") ;
	UIMailPortlet.menuElement = tmpMenuElement ;
  document.getElementById(eXo.webui.UIContextMenuMail.portletName).appendChild(tmpMenuElement) ;	
  if(arguments.length > 2) {
    menuY -= arguments[2].scrollTop ;
  }
	UIMailPortlet.menuElement.style.top = menuY + "px" ;
	UIMailPortlet.menuElement.style.left = menuX + "px" ;	
	UIMailPortlet.showHide(UIMailPortlet.menuElement) ;
  if(uiDesktop) {    
    var uiRightClick = (DOMUtil.findFirstDescendantByClass(UIMailPortlet.menuElement, "div", "UIRightClickPopupMenu")) ? DOMUtil.findFirstDescendantByClass(UIMailPortlet.menuElement, "div", "UIRightClickPopupMenu") : UIMailPortlet.menuElement ;
    var mnuBottom = eXo.core.Browser.findPosYInContainer(UIMailPortlet.menuElement, uiDesktop) + uiRightClick.offsetHeight ;
    var widBottom = uiWindow.offsetTop + uiWindow.offsetHeight ;
    if(mnuBottom > widBottom) {
      menuY -= (mnuBottom - widBottom - clickobj.offsetHeight - uiWindow.scrollTop) ;
      UIMailPortlet.menuElement.style.top = menuY + "px" ;
    }
  } else {
    var uiRightClick = (DOMUtil.findFirstDescendantByClass(UIMailPortlet.menuElement, "div", "UIRightClickPopupMenu")) ? DOMUtil.findFirstDescendantByClass(UIMailPortlet.menuElement, "div", "UIRightClickPopupMenu") : UIMailPortlet.menuElement ;
    var mnuBottom = UIMailPortlet.menuElement.offsetTop +  uiRightClick.offsetHeight - window.document.documentElement.scrollTop ;
    if(window.document.documentElement.clientHeight < mnuBottom) {
      menuY += (window.document.documentElement.clientHeight - mnuBottom) ;
      UIMailPortlet.menuElement.style.top = menuY + "px" ;      
    }
  }
} ;

UIMailPortlet.prototype.showHide = function(obj) {
	if (obj.style.display != "block") {
		obj.style.display = "block" ;
		eXo.core.DOMUtil.listHideElements(obj) ;
	} else {
		obj.style.display = "none" ;
	}
} ;

// Check all
function CheckBox() {
}

CheckBox.prototype.init = function(cont) {
	if (typeof(cont) == "string") cont =document.getElementById(cont) ;
	this.table = eXo.core.DOMUtil.findDescendantsByTagName(cont, "tbody")[0] ;
	var checkboxes = eXo.core.DOMUtil.findDescendantsByClass(cont, "input", "checkbox") ;
	var len = checkboxes.length ;
	if (len <= 0) return ;
	this.checkall = checkboxes[0] ;
	this.checkboxes = checkboxes.slice(1) ;
	this.rows = new Array() ;
	checkboxes[0].onclick = eXo.mail.CheckBox.checkAll ;
	var isAll = 0 ;
	for(var i = len - 1 ; i >= 1 ; i--) {
		checkboxes[i].onclick = eXo.mail.CheckBox.check ;
		this.rows.push(checkboxes[i].parentNode.parentNode) ;
		if(checkboxes[i].checked == true) isAll++ ;
	}
	if(isAll == (len - 1)) checkboxes[0].checked = true ;
} ;

CheckBox.prototype.checkAll = function() {
	var CheckBox = eXo.mail.CheckBox ;
	var isChecked = CheckBox.checkall.checked ;
	var items = CheckBox.checkboxes ;
	var rows = CheckBox.rows ;
	var len = items.length - 1 ;
	for(var i = len ; i >= 0 ; i--) {
		if(!isChecked) { 		
			if (rows[i].className.indexOf("SelectedItem") > -1) {
				rows[i].className = rows[i].className.replace("SelectedItem", "") ;
			}
		} else {
			if (rows[i].className.indexOf("SelectedItem") < 0) {
				rows[i].className += " SelectedItem" ;
			}
		}
		if(items[i].checked == isChecked) continue ;
		items[i].checked = isChecked ;
	}
} ;

CheckBox.prototype.check = function() {
	var isChecked = this.checked ;
	var tr = this.parentNode.parentNode ;
	if(!isChecked) {
		tr.removeAttribute("style") ;
		tr.className = tr.className.replace("SelectedItem","") ;
		eXo.mail.CheckBox.checkall.checked = false ;
	} else {
	    if (tr.className.indexOf("SelectedItem") < 0) {
			tr.className += " SelectedItem";
		}
		eXo.mail.CheckBox.checkall.checked = eXo.mail.CheckBox.isAll() ;
	}
} ;

CheckBox.prototype.isAll = function() {
	var items = eXo.mail.CheckBox.checkboxes ;
	var len = items.length ;
	for(var i = 0 ; i < len ; i ++) {
		if(!items[i].checked) return false ;
	}
	return true ;
} ;

eXo.mail.CheckBox = new CheckBox() ;
eXo.mail.UIMailPortlet = new UIMailPortlet();
