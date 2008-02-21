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
  this.posY = _e.clientY; 
  var marker = (typeof(markerobj) == "string")? document.getElementById(markerobj):markerobj ;
  var container = marker.parentNode ;
  var areas = DOMUtil.findDescendantsByClass(container, "div", "SpliterResizableListArea") ;
  if(areas.length < 2) return ;
  this.beforeArea = areas[0] ;
  this.afterArea = areas[1] ;
//  if (beforeAreaObj) {
//    this.beforeArea = (typeof(beforeAreaObj) == "string")? document.getElementById(beforeAreaObj):beforeAreaObj ;
//  }
//  
//  if (!this.beforeArea) {
//    this.beforeArea = eXo.core.DOMUtil.findPreviousElementByTagName(marker, "div") ;
//  }
//  
//  if (afterAreaObj) {
//    this.afterArea = (typeof(afterAreaObj) == "string")? document.getElementById(afterAreaObj):afterAreaObj ;
//  }
//  
//  if (!this.afterArea) {
//    this.afterArea = eXo.core.DOMUtil.findNextElementByTagName(marker, "div") ;
//  }
  this.beforeArea.style.height = (this.beforeArea.offsetHeight - 1) + "px" ;
  this.afterArea.style.height = (this.afterArea.offsetHeight - 1) + "px" ;
  this.beforeArea.style.overflowY = "auto" ;
  this.afterArea.style.overflowY = "auto" ;
  this.minHeight = 30 ;
  this.beforeY = this.beforeArea.offsetHeight ;
  this.afterY = this.afterArea.offsetHeight ;
  document.onmousemove = eXo.cs.Spliter.adjustHeight ;
  document.onmouseup = eXo.cs.Spliter.clear ;
} ;

Spliter.prototype.adjustHeight = function(evt) {
  evt = (window.event) ? window.event : evt ;
  var Spliter = eXo.cs.Spliter ;
  var delta = evt.clientY - Spliter.posY ;
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
    Spliter.minHeight = null ;
    Spliter.beforeY = null ;
    Spliter.afterY = null ;
    Spliter.beforeArea = null ;
    Spliter.afterArea = null ;
    Spliter.posY = null ;
  } catch(e) {} ;
} ;

eXo.cs.Spliter = new Spliter() ;

/********************* Utility function for CS ******************/

function Utils() {}

Utils.prototype.showHidePane = function(clickobj, beforeobj, afterobj) {
	if(typeof(beforeobj) == "string") beforeobj = document.getElementById(beforeobj) ;
	if(typeof(afterobj) == "string") afterobj = document.getElementById(afterobj) ;
	if(beforeobj.style.display != "none") {
		beforeobj.style.display = "none" ;
		clickobj.className = "MinimizeButton"
	} else {
		beforeobj.style.display = "block" ;
		clickobj.className = "MaximizeButton"
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