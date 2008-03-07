if(!eXo.cs){
	eXo.cs = {} ;
}
/********************* Checkbox Manager ******************/
function CheckBox() {
} ;

CheckBox.prototype.init = function(cont) {
	if(typeof(cont) == "string") cont = document.getElementById(cont) ;
	var checkboxes = eXo.core.DOMUtil.findDescendantsByClass(cont, "input", "checkbox") ;
	if(checkboxes.length <=0) return ;
	this.allItems = checkboxes[0] ;
	this.items = checkboxes.slice(1) ;
	this.allItems.onclick = eXo.cs.CheckBox.checkAll ;
	var len = this.items.length ;
	this.checkedItem = 0 ;
	for(var i = 0 ; i < len ; i ++) {
		if(this.items[i].checked) this.checkedItem++ ;
		this.items[i].onclick = eXo.cs.CheckBox.check ;
	}
} ;

CheckBox.prototype.checkAll = function() {
	var CheckBox = eXo.cs.CheckBox ;
	var checked = this.checked ;
	var items = CheckBox.items ;
	var len = items.length ;
	CheckBox.checkedItem = (checked) ? len : 0 ;
	for(var i = 0 ; i < len ; i ++) {
		items[i].checked = checked ;
	}	
} ;

CheckBox.prototype.check = function() {
	var CheckBox = eXo.cs.CheckBox ;
	var checked = this.checked ;
	var len = CheckBox.items.length ;
	if (!checked) {
		if(CheckBox.checkedItem > 0) CheckBox.checkedItem-- ;
		CheckBox.allItems.checked = false ;
	}
	else {
		if(CheckBox.checkedItem < len) CheckBox.checkedItem++ ;
		if(CheckBox.checkedItem == len)	CheckBox.allItems.checked = true ;
	}
} ;

eXo.cs.CheckBox = new CheckBox() ;

/********************* Pane Spliter ******************/

function Spliter() {
} ;

//Spliter.prototype.doResize = function(e , markerobj, beforeAreaObj, afterAreaObj) {
Spliter.prototype.doResize = function(e , markerobj) {
  _e = (window.event) ? window.event : e ;
  var DOMUtil = eXo.core.DOMUtil ;
  this.posY = eXo.core.Browser.findMouseYInPage(_e) ;
  var marker = (typeof(markerobj) == "string")? document.getElementById(markerobj):markerobj ;
  var container = marker.parentNode ;
  var areas = DOMUtil.findDescendantsByClass(container, "div", "SpliterResizableListArea") ;
  if((areas.length < 2) || (areas[0].style.display=="none")) return ;
  this.beforeArea = areas[0] ;
  this.afterArea = areas[1] ;
//  this.beforeArea.style.height = this.beforeArea.offsetHeight + "px" ;
//  this.afterArea.style.height = this.afterArea.offsetHeight + "px" ;
  this.beforeArea.style.overflowY = "auto" ;
  this.afterArea.style.overflowY = "auto" ;
  try{
	  this.beforeArea.style.maxHeight = "none" ;
	  this.afterArea.style.maxHeight = "none" ;
  } catch(e) {} ;
  this.beforeY = this.beforeArea.offsetHeight ;
  this.afterY = this.afterArea.offsetHeight ;
  document.onmousemove = eXo.cs.Spliter.adjustHeight ;
  document.onmouseup = eXo.cs.Spliter.clear ;
} ;

Spliter.prototype.adjustHeight = function(evt) {
  evt = (window.event) ? window.event : evt ;
  var Spliter = eXo.cs.Spliter ;
  var delta = eXo.core.Browser.findMouseYInPage(evt) - Spliter.posY ;
  var afterHeight = Spliter.afterY - delta ;
  var beforeHeight = Spliter.beforeY + delta ;
  if (beforeHeight <= 0  || afterHeight <= 0) return ;
  Spliter.beforeArea.style.height =  beforeHeight + "px" ;
  Spliter.afterArea.style.height =  afterHeight + "px" ;
} ;

Spliter.prototype.clear = function() {
  try {
    var Spliter = eXo.cs.Spliter ;
    document.onmousemove = null ;
    delete Spliter.beforeY ;
    delete Spliter.afterY ;
    delete Spliter.beforeArea ;
    delete Spliter.afterArea ;
    delete Spliter.posY ;
  } catch(e) {window.statuts = "Message : " + e.message ;} ;
} ;

eXo.cs.Spliter = new Spliter() ;

/********************* Utility function for CS ******************/

function Utils() {}

Utils.prototype.showHidePane = function(clickobj, beforeobj, afterobj) {
  var container = eXo.core.DOMUtil.findAncestorByClass(clickobj, "SpliterContainer") ;
  var areas = eXo.core.DOMUtil.findDescendantsByClass(container, "div", "SpliterResizableListArea") ;
  var uiGrid = eXo.core.DOMUtil.findFirstDescendantByClass(areas[1], "table", "UIGrid") ;
  var uiPreview = eXo.core.DOMUtil.findAncestorByClass(areas[1], "UIPreview") ;
  if(areas.length < 2) return ;
	if(areas[0].style.display != "none") {
		clickobj.className = "MinimizeButton"
    //uiGrid.style.height = (uiGrid.offsetHeight + areas[0].offsetHeight - 4) + "px" ;
    areas[1].style.height = (areas[1].offsetHeight  + areas[0].offsetHeight - 4) + "px" ;
		areas[0].style.display = "none" ;
	} else {
		areas[0].style.display = "block" ;
		clickobj.className = "MaximizeButton"
    //uiGrid.style.height = (uiGrid.offsetHeight - areas[0].offsetHeight + 4) + "px" ;
    areas[1].style.height = (areas[1].offsetHeight - areas[0].offsetHeight + 4 ) + "px" ;
	}
} ;

Utils.prototype.getKeynum = function(event) {
  var keynum = false ;
  if(window.event) { /* IE */
    keynum = window.event.keyCode;
    event = window.event ;
  } else if(event.which) { /* Netscape/Firefox/Opera */
    keynum = event.which ;
  }
  if(keynum == 0) {
    keynum = event.keyCode ;
  }
  return keynum ;
} ;

Utils.prototype.captureInput = function(input, action) {
  if(typeof(input) == "string") input = document.getElementById(input) ;
  input.onkeydown = eXo.cs.Utils.onEnter ;
} ;

Utils.prototype.onEnter = function(evt) {
  var _e = evt || window.event ;
  _e.cancelBubble = true ;
  var keynum = eXo.cs.Utils.getKeynum(_e) ;
  if (keynum == 13) {
    this.form.onsubmit = eXo.cs.Utils.cancelSubmit ;
    var action = eXo.core.DOMUtil.findPreviousElementByTagName(this, "a") ;
    action = String(action.href).replace("javascript:","") ;
    eval(action) ;
  }
} ;

Utils.prototype.cancelSubmit = function() {
  return false ;
} ;

eXo.cs.Utils = new Utils() ;

/********************* Scroll Manager ******************/

function UINavigation() {
  
} ;

UINavigation.prototype.loadScroll = function() {
  
} ;

UINavigation.prototype.initScroll = function() {
  
} ;

eXo.cs.UINavigation = new UINavigation() ;