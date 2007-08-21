function UIMailPortlet(){
};
UIMailPortlet.prototype.selectItem = function(obj) {
	var DOMUtil = eXo.core.DOMUtil ;
	var tr = DOMUtil.findAncestorByTagName(obj, "tr") ;
	var text = DOMUtil.getChildrenByTagName(tr,"td") ;
	var tlen = text.length ;
	if(obj.checked) {
		for(var i = 0 ; i < tlen ; i++) {
			text[i].className =  "textBold" ;
		}
	} else {
		for(var i = 0 ; i < tlen ; i++) {
			text[i].className =  "text" ;
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
			this.selectItem(checkboxes[i]) ;
		}
	} else {
		for(var i = 0 ; i < len ; i++) {
			checkboxes[i].checked = false ;
			this.selectItem(checkboxes[i]) ;
		}
	}
} ;

eXo.mail.UIMailPortlet = new UIMailPortlet();