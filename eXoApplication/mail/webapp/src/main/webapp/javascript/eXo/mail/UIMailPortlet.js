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
	UIContextMenu.attach('IconFolder', 'UIFolderListPopupMenu') ;
	UIContextMenu.attach('IconTagHolder', 'UITagListPopupMenu') ;
	UIContextMenu.attach('InboxIcon', 'UIDefaultFolderPopupMenu') ;
	UIContextMenu.attach('DraftsIcon', 'UIDefaultFolderPopupMenu') ;
	UIContextMenu.attach('SentIcon', 'UIDefaultFolderPopupMenu') ;
	UIContextMenu.attach('SpamIcon', 'UIDefaultFolderPopupMenu') ;
	UIContextMenu.attach('TrashIcon', 'UITrashFolderPopupMenu') ;
} ;

UIMailPortlet.prototype.msgPopupMenuCallback = function(evt) {
	var UIContextMenu = eXo.webui.UIContextMenu ;
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
		if (!tr.getAttribute("tmpClass")) {			
			tr.setAttribute("tmpClass", tr.className) ;
			tr.className = "MessageItem SelectedItem" ;
		}
	} else {
		if (tr.getAttribute("tmpClass")) {			
			tr.className = tr.getAttribute("tmpClass") ;
			tr.removeAttribute("tmpClass") ;
		}
	}
	
	var table = DOMUtil.findAncestorByTagName(tr, "table") ;
  var trs = DOMUtil.findDescendantsByTagName(table, "tr");
	for (var i = 1; i < trs.length; i++) {
		var input = DOMUtil.findFirstDescendantByClass(trs[i], "input", "checkbox");
		if (!input.checked) {
			if (trs[i].className.indexOf("SelectedItem") == -1)
				trs[i].className = trs[i].className.substring(trs[i].className.indexOf("SelectedItem"), trs[i].className.length);
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
	var	layout2 = document.getElementById("UIMessageList") ;
	var	layout3 = document.getElementById("SpliterResizableArea") ;
	var resizePane = document.getElementById("ResizeReadingPane");
	var workingarea = document.getElementById("UIMessageArea");
	//var workingarea = eXo.core.DOMUtil.findNextElementByTagName(layout3, "div") ;
		
	switch(layout) {
		case 0 :
			if (layout1.style.display == "none") {
				layout1.style.display = "block" ;
				//layout3.style.display = "block" ;
				 workingarea.style.marginLeft = "225px"	;			
				//Browser.setCookie("displaymode","block",7) ;
				//Browser.setCookie("displaymode0","block",7) ;
			}
			
			if (layout2.style.display == "none") {
				layout2.style.display = "block" ;
//				layout3.style.display = "block" ;
//				workingarea.style.marginLeft = "243px"	;
//				Browser.setCookie("displaymode","block",7) ;
//				Browser.setCookie("displaymode1","block",7) ;

			}
			
			if (layout3.style.display == "none") {
				layout3.style.display = "block" ;
//				layout3.style.display = "block" ;
//				workingarea.style.marginLeft = "243px"	;
//				Browser.setCookie("displaymode","block",7) ;
//				Browser.setCookie("displaymode1","block",7) ;

			}
			
			if (resizePane.style.display == "none") {
				resizePane.style.display = "block";
//				layout3.style.display = "block" ;
//				workingarea.style.marginLeft = "243px"	;
//				Browser.setCookie("displaymode","block",7) ;
//				Browser.setCookie("displaymode1","block",7) ;

			}
				
			break ;
		case 1 :
			if (layout1.style.display == "none") {
				layout1.style.display = "block" ;
				//layout3.style.display = "block" ;
				 workingarea.style.marginLeft = "225px"	;			
				//Browser.setCookie("displaymode","block",7) ;
				//Browser.setCookie("displaymode0","block",7) ;
			} else {
				layout1.style.display = "none" ;
				if(layout1.style.display == "none") {
					//Browser.setCookie("displaymode","none",7) ;
					workingarea.style.marginLeft = "0px"	;
					//layout3.style.display = "none" ;
				}
				//Browser.setCookie("displaymode0","none",7) ;	
			}
			break ;
		case 2 :
			if (layout2.style.display == "none") {
				layout2.style.display = "block" ;
				if (layout3.style.display != "none" && layout2.style.display != "none") {
					resizePane.style.display = "block";
				}
//				layout3.style.display = "block" ;
//				workingarea.style.marginLeft = "243px"	;
//				Browser.setCookie("displaymode","block",7) ;
//				Browser.setCookie("displaymode1","block",7) ;

			} else {				
				layout2.style.display = "none" ;
				resizePane.style.display = "none";
//				if(layout0.style.display == "none") {
//					Browser.setCookie("displaymode","none",7) ;
//					workingarea.style.marginLeft = "0px"	;
//					layout3.style.display = "none" ;
//				}
//				Browser.setCookie("displaymode1","none",7) ;	
			}
			break ;
		case 3 :
			if (layout3.style.display == "none") {
				layout3.style.display = "block" ;
				if (layout3.style.display != "none" && layout2.style.display != "none") {
					resizePane.style.display = "block";
				}
//				layout3.style.display = "block" ;
//				workingarea.style.marginLeft = "243px"	;
//				Browser.setCookie("displaymode","block",7) ;
//				Browser.setCookie("displaymode1","block",7) ;

			} else {				
				layout3.style.display = "none" ;				
			    resizePane.style.display = "none";
//				if(layout0.style.display == "none") {
//					Browser.setCookie("displaymode","none",7) ;
//					workingarea.style.marginLeft = "0px"	;
//					layout3.style.display = "none" ;
//				}
//				Browser.setCookie("displaymode1","none",7) ;	
			}
			break ;
	}
} ;

UIMailPortlet.prototype.showHide = function(add) {	
	var elm = document.getElementById(add);
	if (elm.style.display == "none") {
		elm.style.display = "" ;
	}
} ;

eXo.mail.UIMailPortlet = new UIMailPortlet();