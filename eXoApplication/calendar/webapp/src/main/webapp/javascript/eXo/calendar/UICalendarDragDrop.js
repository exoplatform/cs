/**
 * @author uocnb
 */
function UICalendarDragDrop() {
  this.scKey = 'background' ;
  this.scValue = '#c0c0c0' ;
  this.DOMUtil = eXo.core.DOMUtil ;
  this.DragDrop = eXo.core.DragDrop ;
  this.dropableSets = [] ;
  this.listView = false ;
} ;

/**
 * 
 * @param {Array} tableData
 * @param {Array} events
 */
UICalendarDragDrop.prototype.init = function(tableData, events) {
  this.tableData = tableData;
  this.events = events;
  this.getAllDropableSets() ;
  this.regDnDItem() ;
} ;

UICalendarDragDrop.prototype.getAllDropableSets = function() {
  this.dropableSets = new Array();
  for (var i=0; i<this.tableData.length; i++) {
    var row = this.tableData[i];
    for (var j=0; j<row.length; j++) {
      this.dropableSets.push(row[j]);
    }
  }
} ;

UICalendarDragDrop.prototype.regDnDItem = function() {
  for (var i=0; i<this.events.length; i++) {
    this.events[i].rootNode.onmousedown = this.dndTrigger;
  } ; 
} ;

UICalendarDragDrop.prototype.dndTrigger = function(e){
  e = e ? e : window.event;
  if (e.button == 0 || e.which == 1) {
    return eXo.calendar.UICalendarDragDrop.initDnD(eXo.calendar.UICalendarDragDrop.dropableSets, this, this, e);
  }
  return true ;
} ;

/**
 * 
 * @param {Array} dropableObjs
 * @param {Element} clickObj
 * @param {Element} dragObj
 * @param {Event} e
 */
UICalendarDragDrop.prototype.initDnD = function(dropableObjs, clickObj, dragObj, e) {
  var clickBlock = (clickObj && clickObj.tagName) ? clickObj : document.getElementById(clickObj) ;
  var dragBlock = (dragObj && dragObj.tagName) ? dragObj : document.getElementById(dragObj) ;
  
  var blockWidth = clickBlock.offsetWidth ;
  var blockHeight = clickBlock.offsetHeight ;
  var tmpNode = clickBlock.cloneNode(true);
  with (tmpNode.style) {
    background = "rgb(237,237,237)";
    width = dropableObjs[0].offsetWidth + 'px';
  }
  eXo.core.Browser.setOpacity(tmpNode, 50) ;
//  clickBlock.parentNode.appendChild(tmpNode);
  document.body.appendChild(tmpNode);
  
  this.DragDrop.initCallback = this.initCallback ;
  this.DragDrop.dragCallback = this.dragCallback ;
  this.DragDrop.dropCallback = this.dropCallback ;
  
  this.DragDrop.init(dropableObjs, clickBlock, tmpNode, e) ;
  return false ;
} ;

UICalendarDragDrop.prototype.synDragObjectPos = function(dndEvent) {
  if (!dndEvent.backupMouseEvent) {
    dndEvent.backupMouseEvent = window.event ;
    if (!dndEvent.backupMouseEvent) {
      return ;
    }
  }
  var dragObject = dndEvent.dragObject ;
  var mouseX = eXo.core.Browser.findMouseXInPage(dndEvent.backupMouseEvent) ;
  var mouseY = eXo.core.Browser.findMouseYInPage(dndEvent.backupMouseEvent) ;
  dragObject.style.top = mouseY + 'px' ;
  dragObject.style.left = mouseX + 'px' ;
} ;

UICalendarDragDrop.prototype.initCallback = function(dndEvent) {
  eXo.calendar.UICalendarDragDrop.synDragObjectPos(dndEvent) ;
} ;

UICalendarDragDrop.prototype.dragCallback = function(dndEvent) {
  var dragObject = dndEvent.dragObject ;
  if (!dragObject.style.display ||
      dragObject.style.display == 'none') {
    dragObject.style.display = 'block' ;
  }

  eXo.calendar.UICalendarDragDrop.synDragObjectPos(dndEvent) ;
  
  if (dndEvent.foundTargetObject) {
    if (this.foundTargetObjectCatch != dndEvent.foundTargetObject) {
      if(this.foundTargetObjectCatch) {
        this.foundTargetObjectCatch.style[eXo.calendar.UICalendarDragDrop.scKey] = this.foundTargetObjectCatchStyle ;
      }
      this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
      this.foundTargetObjectCatchStyle = this.foundTargetObjectCatch.style[eXo.calendar.UICalendarDragDrop.scKey] ;
      this.foundTargetObjectCatch.style[eXo.calendar.UICalendarDragDrop.scKey] = eXo.calendar.UICalendarDragDrop.scValue ;
    }
  } else {
    if (this.foundTargetObjectCatch) {
      this.foundTargetObjectCatch.style[eXo.calendar.UICalendarDragDrop.scKey] = this.foundTargetObjectCatchStyle ;
    }
    this.foundTargetObjectCatch = null ;
  }
} ;

UICalendarDragDrop.prototype.dropCallback = function(dndEvent) {
  eXo.core.DOMUtil.removeElement(dndEvent.dragObject);
  if (this.foundTargetObjectCatch) {
    this.foundTargetObjectCatch.style[eXo.calendar.UICalendarDragDrop.scKey] = this.foundTargetObjectCatchStyle ;
  }
  this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
  if (this.foundTargetObjectCatch) {
    if (this.foundTargetObjectCatch.getAttribute('starttime') == dndEvent.clickObject.getAttribute('starttime')) {
      return;
    }
    if (actionlink = this.foundTargetObjectCatch.getAttribute("actionLink")) {
      var clickObject = dndEvent.clickObject;
      var currentDate = this.foundTargetObjectCatch.getAttribute("startTime") ;
      var eventId = clickObject.getAttribute("eventId") ;
      var calId = clickObject.getAttribute("calId") ;
      var calType = clickObject.getAttribute("calType") ;
      actionlink = actionlink.replace(/objectId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"objectId=" + currentDate) ;
      actionlink = actionlink.replace(/eventId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"eventId=" + eventId) ;
      actionlink = actionlink.replace(/calendarId\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"calendarId=" + calId) ;
      actionlink = actionlink.replace(/calType\s*=\s*[a-zA-Z0-9_]*(?=&|'|\")/,"calType=" + calType) ;
      actionlink = actionlink.replace("javascript:","") ;
      eval(actionlink) ;
    }
  }
} ;

eXo.calendar.UICalendarDragDrop = new UICalendarDragDrop();
