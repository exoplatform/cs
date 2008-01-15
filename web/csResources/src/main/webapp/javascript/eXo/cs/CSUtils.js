function CheckBox() {
} ;

CheckBox.prototype.init = function(cont) {
	if(typeof(cont) == "string") cont = document.getElementById(cont) ;
	var checkboxes = eXo.core.DOMUtil.findDescendantsByClass(cont, "input", "checkbox") ;
	this.allItems = checkboxes[0] ;
	this.items = checkboxes.slice(1) ;
	this.allItems.onclick = eXo.webui.CheckBox.checkAll ;
	var len = this.items.length ;
	this.checkedItem = 0 ;
	for(var i = 0 ; i < len ; i ++) {
		if(this.items[i].checked) this.checkedItem++ ;
		this.items[i].onclick = eXo.webui.CheckBox.check ;
	}
} ;

CheckBox.prototype.checkAll = function() {
	var CheckBox = eXo.webui.CheckBox ;
	var checked = this.checked ;
	var items = CheckBox.items ;
	var len = items.length ;
	CheckBox.checkedItem = (checked) ? len : 0 ;
	for(var i = 0 ; i < len ; i ++) {
		items[i].checked = checked ;
	}
	
} ;

CheckBox.prototype.check = function() {
	var CheckBox = eXo.webui.CheckBox ;
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

eXo.webui.CheckBox = new CheckBox() ;

/***************************************************************************************/

function Spliter() {  
} ;

Spliter.prototype.doResize = function(e , markerobj, beforeAreaObj, afterAreaObj) {
  _e = (window.event) ? window.event : e ;
  this.posY = _e.clientY; 
  var marker = (typeof(markerobj) == "string")? document.getElementById(markerobj):markerobj ;
  if (beforeAreaObj) {
    this.beforeArea = (typeof(beforeAreaObj) == "string")? document.getElementById(beforeAreaObj):beforeAreaObj ;
  }
  
  if (!this.beforeArea) {
    this.beforeArea = eXo.core.DOMUtil.findPreviousElementByTagName(marker, "div") ;
  }
  
  if (afterAreaObj) {
    this.afterArea = (typeof(afterAreaObj) == "string")? document.getElementById(afterAreaObj):afterAreaObj ;
  }
  
  if (!this.afterArea) {
    this.afterArea = eXo.core.DOMUtil.findNextElementByTagName(marker, "div") ; 
  }
//  debugger;
  this.beforeArea.style.height = this.beforeArea.offsetHeight + "px" ;
  this.afterArea.style.height = this.afterArea.offsetHeight + "px" ;  
  this.beforeY = this.beforeArea.offsetHeight ;
  this.afterY = this.afterArea.offsetHeight ;
  document.onmousemove = eXo.webui.Spliter.adjustHeight ;  
  document.onmouseup = eXo.webui.Spliter.clear ;
} ;

Spliter.prototype.adjustHeight = function(evt) {
  evt = (window.event) ? window.event : evt ;
  var Spliter = eXo.webui.Spliter ;
  var delta = evt.clientY - Spliter.posY ;
  var afterHeight = (Spliter.afterY - delta) ;
  var beforeHeight = (Spliter.beforeY + delta) ;
  if (beforeHeight <= 0  || afterHeight <= 0) return ;
  Spliter.beforeArea.style.height =  beforeHeight + "px" ;
  Spliter.afterArea.style.height =  afterHeight + "px" ;  
} ;

Spliter.prototype.clear = function() {
  document.onmousemove = null ;
} ;

eXo.webui.Spliter = new Spliter() ;
