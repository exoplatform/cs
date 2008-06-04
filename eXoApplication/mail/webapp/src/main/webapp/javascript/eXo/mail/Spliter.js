/**
 * @author uocnb
 */

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
  document.onmousemove = eXo.mail.Spliter.adjustHeight ;  
  document.onmouseup = eXo.mail.Spliter.clear ;
} ;

Spliter.prototype.adjustHeight = function(evt) {
  evt = (window.event) ? window.event : evt ;
  var Spliter = eXo.mail.Spliter ;
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

eXo.mail.Spliter = new Spliter() ;