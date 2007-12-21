eXo.require('eXo.webui.UIContextMenu') ;
eXo.require('eXo.core.DOMUtil') ;

function UIMailPortlet(){
};

UIMailPortlet.prototype.showContextMenu = function() {
	
	var UIContextMenu = eXo.webui.UIContextMenu ;//eXo.contact.ContextMenu ;
	var config = {
		'preventDefault':false, 
		'preventForms':false
	} ;	
	UIContextMenu.init(config) ;
	UIContextMenu.attach('MessageItem', 'UIMessagePopupMenu') ;
	UIContextMenu.attach('FolderLink', 'UIFolderListPopupMenu') ;
	UIContextMenu.attach('IconTagHolder', 'UITagListPopupMenu') ;
	UIContextMenu.attach('InboxIcon', 'UIDefaultFolderPopupMenu') ;
	UIContextMenu.attach('DraftsIcon', 'UIDefaultFolderPopupMenu') ;
	UIContextMenu.attach('SentIcon', 'UIDefaultFolderPopupMenu') ;
	UIContextMenu.attach('SpamIcon', 'UIDefaultFolderPopupMenu') ;
	UIContextMenu.attach('TrashIcon', 'UITrashFolderPopupMenu') ;
} ;

UIMailPortlet.prototype.msgPopupMenuCallback = function(evt) {
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var DOMUtil = eXo.core.DOMUtil ;
	var _e = window.event || evt ;
	//_e.cancelBubble = true ;
	var src = null ;
	if (UIContextMenu.IE) {
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
	eXo.webui.UIContextMenu.changeAction(UIContextMenu.menuElement, id) ;
} ;

UIMailPortlet.prototype.defaultFolderPopupMenuCallback = function(evt) {
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var _e = window.event || evt ;
	//_e.cancelBubble = true ;
	var src = null ;
	if (UIContextMenu.IE) {
		src = _e.srcElement;
	} else {
		src = _e.target;
	}
	if (src.nodeName != "A")
		src = src.parentNode;
		
	folder = src.getAttribute("folder");
	eXo.webui.UIContextMenu.changeAction(UIContextMenu.menuElement, folder) ;
} ;

UIMailPortlet.prototype.tagListPopupMenuCallback = function(evt) {
	var UIContextMenu = eXo.webui.UIContextMenu ;
	var _e = window.event || evt ;
	//_e.cancelBubble = true ;
	var src = null ;
	if (UIContextMenu.IE) {
		src = _e.srcElement;
	} else {
		src = _e.target;
	}
	if (src.nodeName != "A")
		src = src.parentNode;
		
	tagName = src.getAttribute("tagId");
	eXo.webui.UIContextMenu.changeAction(UIContextMenu.menuElement, tagName) ;
} 

UIMailPortlet.prototype.selectItem = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
    obj = DOMUtil.findFirstDescendantByClass(obj, "input", "checkbox");
	var tr = DOMUtil.findAncestorByTagName(obj, "tr") ;
	if(obj.checked) {
		tr.className = tr.className + " SelectedItem" ;
	} else {
		tr.className = tr.className.replace("SelectedItem", "") ;
	}
	
	var table = DOMUtil.findAncestorByTagName(tr, "table") ;
  var trs = DOMUtil.findDescendantsByTagName(table, "tr");
	for (var i = 1; i < trs.length; i++) {
		var input = DOMUtil.findFirstDescendantByClass(trs[i], "input", "checkbox");
		if (!input.checked) {
			if (trs[i].className.indexOf("SelectedItem") > -1)
				trs[i].className = trs[i].className.replace("SelectedItem", "");
		}
	}
}
UIMailPortlet.prototype.checkAll = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
	var thead = DOMUtil.findAncestorByTagName(obj, "thead") ;
	var tbody = DOMUtil.findNextElementByTagName(thead, "tbody") ;
	var checkboxes = DOMUtil.findDescendantsByClass(tbody, "input", "checkbox") ;
	var len = checkboxes.length ;
	if (obj.checked){
		for(var i = 0 ; i < len ; i++) {
			checkboxes[i].checked = true ;
			var obj = DOMUtil.findAncestorByTagName(checkboxes[i], "td");
			this.selectItem(obj) ;
		}
	} else {
		for(var i = 0 ; i < len ; i++) {
			checkboxes[i].checked = false ;
			var obj = DOMUtil.findAncestorByTagName(checkboxes[i], "td");
			this.selectItem(obj) ;
		}
	}
} ;

UIMailPortlet.prototype.readMessage = function() {} ;

UIMailPortlet.prototype.showPrintPreview = function(obj1) {
	document.getElementById("UIPortalApplication").style.display = "none";
	var root1 = document.createElement('div') ;
	root1.className = 'UIMailPortlet' ;
	var root2 = document.createElement('div') ;
	root2.className = 'MailWorkingWorkspace' ;
	var root3 = document.createElement('div') ;
	root3.className = 'UIMessagePreview' ;
	root3.appendChild(obj1.cloneNode(true)) ;
	root2.appendChild(root3) ;
	root1.appendChild(root2) ;
	document.body.appendChild(root1) ;
} ;

UIMailPortlet.prototype.printMessage = function() {
	window.print()
} ;

UIMailPortlet.prototype.closePrint = function() {
	document.getElementById("UIPortalApplication").style.display = "block";
	document.getElementById("printWrapper").style.display = "none";
} ;

UIMailPortlet.prototype.switchLayout = function(layout) {	
	var Browser = eXo.core.Browser ;
	layout = parseInt(layout) ;
	var	layout1 = document.getElementById("UINavigationContainer") ;
	var	layout2 = document.getElementById("uiMessageListResizableArea") ;
	var	layout3 = document.getElementById("SpliterResizableArea") ;
	var resizePane = document.getElementById("ResizeReadingPane");
	var workingarea = document.getElementById("UIMessageArea");
		
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
			Browser.setCookie("UINavigationContainer", "block", 30);
			Browser.setCookie("SpliterResizableArea", "block", 30);
			layout2.style.height = "220px" ;
			Browser.setCookie("ResizeReadingPane", "block", 30);
			Browser.setCookie("uiMessageListResizableArea", "block", 30)	
			break ;
		case 1 :
			if (layout1.style.display == "none") {
				layout1.style.display = "block" ;
				Browser.setCookie("UINavigationContainer", "block", 30)
				 workingarea.style.marginLeft = "225px"	;			
			} else {
				layout1.style.display = "none" ;
				Browser.setCookie("UINavigationContainer", "none", 30)
				if(layout1.style.display == "none") {
					workingarea.style.marginLeft = "0px"	;
				}
			}
			break ;
		case 2 :
			if (layout2.style.display == "none") {
				layout2.style.display = "block" ;
				Browser.setCookie("uiMessageListResizableArea", "block", 30)
				if (layout3.style.display != "none" && layout2.style.display != "none") {
					resizePane.style.display = "block";
					Browser.setCookie("ResizeReadingPane", "block", 30)
				}
			} else {				
				layout2.style.display = "none" ;
				resizePane.style.display = "none";
				Browser.setCookie("ResizeReadingPane", "none", 30)
				Browser.setCookie("uiMessageListResizableArea", "none", 30)
			}
			break ;
		case 3 :
			if (layout3.style.display == "none") {
				layout3.style.display = "block" ;
				layout2.style.height = "220px" ;
				Browser.setCookie("SpliterResizableArea", "block", 30)
				if (layout3.style.display != "none" && layout2.style.display != "none") {
					resizePane.style.display = "block";
					Browser.setCookie("ResizeReadingPane", "block", 30)
				}

			} else {				
				layout3.style.display = "none" ;	
				layout2.style.height = "100%" ;			
			    resizePane.style.display = "none";
			    Browser.setCookie("ResizeReadingPane", "none", 30)
			    Browser.setCookie("SpliterResizableArea", "none", 30)	
			}
			break ;
	}
} ;

UIMailPortlet.prototype.checkLayout = function() {
	var Browser = eXo.core.Browser ;
	var	layout1 = document.getElementById("UINavigationContainer") ;
	var	layout2 = document.getElementById("uiMessageListResizableArea") ;
	var	layout3 = document.getElementById("SpliterResizableArea") ;
	var resizePane = document.getElementById("ResizeReadingPane");
	var workingarea = document.getElementById("UIMessageArea");
	var	uiMessageList = Browser.getCookie("uiMessageListResizableArea") ;
	var	uiNavigationContainer = Browser.getCookie("UINavigationContainer") ;
	var	SpliterResizableArea = Browser.getCookie("SpliterResizableArea") ;
	var	ResizeReadingPane = Browser.getCookie("ResizeReadingPane") ;
	if (uiMessageList != null) {
		layout1.style.display = uiNavigationContainer;
		layout2.style.display = uiMessageList;
		layout3.style.display = SpliterResizableArea;
		resizePane.style.display = ResizeReadingPane;
	} else {
		layout1.style.display = "block";
		layout2.style.display = "block";
		layout3.style.display = "block";
		resizePane.style.display = "block";
	}
	if(layout1.style.display == "none") {
		workingarea.style.marginLeft = "0px"	;
	}
	
	if(layout3.style.display == "block") {
		layout2.style.height = "220px" ;
	} else {
		layout2.style.height = "100%" ;
	}
} ;

UIMailPortlet.prototype.showHide = function(add) {	
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

UIMailPortlet.prototype.collapseExpandFolder = function(obj) {
	var DOMUtil = eXo.core.DOMUtil;
	var divChild = DOMUtil.findNextElementByTagName(obj, "div");
	
	var objClass = obj.className;
	if (objClass.indexOf(" OpenFolder") > -1) { 
		objClass = objClass.replace(" OpenFolder", "");
		objClass = objClass + " CloseFolder" ;
		obj.className = objClass ;
    } else if (objClass.indexOf(" CloseFolder") > -1) { 
		objClass = objClass.replace(" CloseFolder", "");
		objClass = objClass + " OpenFolder" ;
		obj.className = objClass ;
    }	
    
    if (divChild != null) {
		var childClass = divChild.className ;
		if (childClass.indexOf("Expand") > -1) {
			divChild.className = "Collapse" ;
 		} else if (childClass.indexOf("Collapse") > -1){
			divChild.className = "Expand" ;
		} 
	} 
}
eXo.mail.UIMailPortlet = new UIMailPortlet();
