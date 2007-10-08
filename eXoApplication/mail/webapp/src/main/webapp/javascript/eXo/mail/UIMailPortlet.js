eXo.require('eXo.webui.UIContextMenu') ;

function UIMailPortlet(){
};

UIMailPortlet.prototype.showContextMenu = function() {
	
	var UIContextMenu = eXo.webui.UIContextMenu ;//eXo.contact.ContextMenu ;
	var config = {
		'preventDefault':false, 
		'preventForms':false
	} ;	
	UIContextMenu.init(config) ;
	UIContextMenu.attach('IconFolder', 'UIFolderListPopupMenu') ;
	UIContextMenu.attach('IconHolder', 'UITagListPopupMenu') ;
} ;

UIMailPortlet.prototype.folderListPopupMenuCallback = function(evt) {
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
		
	folderName = src.getAttribute("folderName");
	eXo.webui.UIContextMenu.changeAction(UIContextMenu.menuElement, folderName) ;
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
		
	tagName = src.getAttribute("tagName");
	eXo.webui.UIContextMenu.changeAction(UIContextMenu.menuElement, tagName) ;
} 

UIMailPortlet.prototype.selectItem = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
  obj = DOMUtil.findFirstDescendantByClass(obj, "input", "checkbox");
	var tr = DOMUtil.findAncestorByTagName(obj, "tr") ;
	if(obj.checked) {
		if (!tr.getAttribute("tmpClass")) {			
			tr.setAttribute("tmpClass", tr.className) ;
			tr.className = "SelectedItem" ;
		}
	} else {
		if (tr.getAttribute("tmpClass")) {			
			tr.className = tr.getAttribute("tmpClass") ;
			tr.removeAttribute("tmpClass") ;
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

eXo.mail.UIMailPortlet = new UIMailPortlet();