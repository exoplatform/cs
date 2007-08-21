function UIForumPortlet() {};
UIForumPortlet.prototype.selectItem = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
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
} ;
UIForumPortlet.prototype.checkAll = function(obj) {
		var DOMUtil = eXo.core.DOMUtil ;
		var thead = DOMUtil.findAncestorByTagName(obj, "thead") ;
		var tbody = DOMUtil.findNextElementByTagName(thead, "tbody") ;
		var checkboxes = DOMUtil.findDescendantsByClass(tbody, "input", "checkbox") ;
		var len = checkboxes.length ;
		if (obj.checked){
			for(var i = 0 ; i < len ; i++) {
				checkboxes[i].checked = true ;
				this.selectItem(checkboxes[i]) ;
			}
		} else {
			for(var i = 0 ; i < len ; i++) {
				checkboxes[i].checked = false ;
				this.selectItem(checkboxes[i]) ;				
			}
		}
}
UIForumPortlet.prototype.OverButton = function(oject) {
	if(oject.className.indexOf("Style") > 0){
		var Srt = "";
		for(var i=0; i<oject.className.length - 5; i++) {
			Srt = Srt + oject.className.charAt(i);
		}
		oject.className = Srt;
	}	else oject.className = oject.className + "Style";
};

UIForumPortlet.prototype.checkAction = function(obj, evt) {
	eXo.webui.UIPopupSelectCategory.show(obj, evt) ;
	var uiCategory = document.getElementById("UICategory") ;
	var uiRightClickPopupMenu = eXo.core.DOMUtil.findFirstDescendantByClass(uiCategory, "div", "UIRightClickPopupMenu") ;
	var checkboxes = eXo.core.DOMUtil.findDescendantsByClass(uiCategory, "input", "checkbox") ;
	var clen = checkboxes.length ;
	var menuItems = uiRightClickPopupMenu.getElementsByTagName("a") ;
	var mlen = menuItems.length ;
	var alen = arguments.length ;
	var j = 0 ;
	for(var i = 1 ; i < clen ; i ++) {
		if (checkboxes[i].checked) j++ ;
		if (j > 1) break ;
	}
	if (alen > 2) {	
		if (j < 1) {
			for(var n = arguments[alen-1] ; n < mlen ; n++) {
				if(!menuItems[n].getAttribute("tmpClass")) {
					menuItems[n].setAttribute("tmpClass",menuItems[n].className) ;
					menuItems[n].setAttribute("tmpHref",menuItems[n].href) ;
					menuItems[n].className = "DisableMenuItem" ;
					menuItems[n].href = "javascript:void(0);" ;
				}			
			}
		} else {		
			for(var n = arguments[alen-1] ; n < mlen ; n++) {
				if(menuItems[n].getAttribute("tmpClass")) {
					menuItems[n].className = menuItems[n].getAttribute("tmpClass") ;
					menuItems[n].href = menuItems[n].getAttribute("tmpHref") ;
					menuItems[n].removeAttribute("tmpClass") ;
					menuItems[n].removeAttribute("tmpHref") ;
				}			
			}
			if (j > 1) {
					for(var k = 2; k < alen; k ++) {
						if(!menuItems[arguments[k]].getAttribute("tmpClass")) {
							menuItems[arguments[k]].setAttribute("tmpClass",menuItems[arguments[k]].className) ;
							menuItems[arguments[k]].setAttribute("tmpHref",menuItems[arguments[k]].href) ;
							menuItems[arguments[k]].className = "DisableMenuItem" ;
							menuItems[arguments[k]].href = "javascript:void(0);" ;
					}					
				}	
			}
		}
	}
} ;
UIForumPortlet.prototype.expandCollapse = function(obj) {
	var forumToolbar = eXo.core.DOMUtil.findAncestorByClass(obj,"ForumToolbar") ;
	var contentContainer = eXo.core.DOMUtil.findNextElementByTagName(forumToolbar, "div") ;
	if (contentContainer.style.display != "none") {
		contentContainer.style.display = "none" ;
		obj.className = "ExpandButton" ;
		obj.setAttribute("title","Expand") ;
	} else {
		contentContainer.style.display = "block" ;
		obj.className = "CollapseButton" ;
		obj.setAttribute("title","Collapse") ;
	}
} ;

eXo.forum.UIForumPortlet = new UIForumPortlet() ;