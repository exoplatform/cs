function UICombobox() {
}

UICombobox.prototype.init = function(container) {
	if(typeof(container) == "string") container = document.getElementById(container) ;
	var UIComboboxContainer = eXo.core.DOMUtil.findDescendantsByClass(container, "div", "UIComboboxList") ;
	var len = UIComboboxContainer.length ;
	var textbox = null ;
	for(var i = 0 ; i < len ; i++) {
		textbox = eXo.core.DOMUtil.findNextElementByTagName(UIComboboxContainer[i], "input");
		textbox.onfocus = eXo.calendar.UICombobox.show ;
		textbox.onclick = eXo.calendar.UICombobox.show ;		
	}
} ;

UICombobox.prototype.show = function(evt) {
	var UICombobox = eXo.calendar.UICombobox ;
	var _e = window.event || evt ;
	_e.cancelBubble = true ;
	var src = _e.target || _e.srcElement ;
	if(UICombobox.list) UICombobox.list.style.display = "none" ;
	UICombobox.list = eXo.core.DOMUtil.findPreviousElementByTagName(src, "div") ;
	UICombobox.items = eXo.core.DOMUtil.findDescendantsByTagName(UICombobox.list, "a") ;
	var len = UICombobox.items.length ;
	for(var i = 0 ; i < len ; i ++ ) {
		UICombobox.items[i].onclick = UICombobox.getValue ; 
	}
	if (len <= 0) return ;
	UICombobox.list.style.width = (this.offsetWidth - 2) + "px" ;	
	UICombobox.list.style.overflowX = "hidden" ;
	UICombobox.list.style.display = "block" ;
	var top = eXo.core.Browser.findPosYInContainer(this, UICombobox.list.offsetParent) + this.offsetHeight ;
	var left = eXo.core.Browser.findPosXInContainer(this, UICombobox.list.offsetParent) ;
	UICombobox.list.style.top = top + "px" ;	
	UICombobox.list.style.left = left + "px" ;
	eXo.core.DOMUtil.listHideElements(UICombobox.list) ;
	//document.onmousedown = eXo.calendar.UICombobox.hide ;
} ;

UICombobox.prototype.hide = function() {
	eXo.calendar.UICombobox.list.style.display = "none" ;
}

UICombobox.prototype.getValue = function() {
	var UICombobox = eXo.calendar.UICombobox ;
	var val = this.getAttribute("value") ;
	var textbox = eXo.core.DOMUtil.findNextElementByTagName(UICombobox.list,"input") ;
	textbox.value = val ;
	var len = UICombobox.items.length ;
	var icon = null ;
	var selectedIcon = null ;
	for(var i = 0 ; i < len ; i ++ ) {
		icon = eXo.core.DOMUtil.findFirstDescendantByClass(UICombobox.items[i],"div", "UIComboboxIcon") ;
		icon.className = "UIComboboxIcon" ;
	}
	selectedIcon = eXo.core.DOMUtil.findFirstDescendantByClass(this,"div", "UIComboboxIcon") ;
	eXo.core.DOMUtil.addClass(selectedIcon, "UIComboboxSelectedIcon") ;
	UICombobox.list.style.display = "none" ;
} ;

eXo.calendar.UICombobox = new UICombobox() ;