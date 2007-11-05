/**
 * @author uocnb
 */

var scKey = 'border' ;
var scValue = 'solid 1px red' ;

function UIMailDnD() {
  this.DOMUtil = eXo.core.DOMUtil ;
  this.DragDrop = eXo.core.DragDrop ;
  this.dropableSets = [] ;
} ;

UIMailDnD.prototype.onLoad = function() {
  eXo.mail.UIMailDnD.init() ;
} ;

UIMailDnD.prototype.init = function() {
  this.uiMailPortletNode = document.getElementById('UIMailPortlet') ;
  this.getAllDropableSets() ;
  this.regMailItem() ;
} ;

UIMailDnD.prototype.getAllDropableSets = function() {
  var uiFolderContainerNode = document.getElementById('UIFolderContainer') ;
  var folderList = this.DOMUtil.findDescendantsByClass(uiFolderContainerNode, 'a', 'Folder') ;
  this.dropableSets[0] = folderList ;
} ;

UIMailDnD.prototype.regMailItem = function() {
  var uiListUsersNode = document.getElementById('UIListUsers') ;
  var mailList = this.DOMUtil.findDescendantsByClass(uiListUsersNode, 'tr', 'MessageItem') ;
  for (var i=0; i<mailList.length; i++) {
    mailList[i].onmousedown = this.mailMDTrigger ;
  }
} ;

UIMailDnD.prototype.getSelectedItems = function(triggerObj) {
  var uiListUsersNode = document.getElementById('UIListUsers') ;
  var mailList = this.DOMUtil.findDescendantsByClass(uiListUsersNode, 'input', 'checkbox') ;
  var selectedItems = [] ;
  var skipAddTriggerObj = false ;
  for (var i=0; i<mailList.length; i++) {
    if (mailList[i].checked) {
      selectedItems[selectedItems.length] = this.DOMUtil.findAncestorByClass(mailList[i], 'NormalItem') ;
      if (triggerObj == selectedItems[selectedItems.length - 1]) {
        skipAddTriggerObj = true ;
      }
    }
  }
  if (!skipAddTriggerObj) {
    selectedItems[selectedItems.length] = triggerObj ;
  }
  return selectedItems ;
} ;

UIMailDnD.prototype.mailMDTrigger = function(e) {
  e = e ? e : window.event ;
  return eXo.mail.UIMailDnD.initDnD(eXo.mail.UIMailDnD.dropableSets[0], this, this, e) ;
} ;

UIMailDnD.prototype.initDnD = function(dropableObjs, clickObj, dragObj, e) {
  var clickBlock = (clickObj && clickObj.tagName) ? clickObj : document.getElementById(clickObj) ;
  var dragBlock = (dragObj && dragObj.tagName) ? dragObj : document.getElementById(dragObj) ;
  
  var tmpNode = document.createElement('div') ;
  tmpNode.className = 'UIGrid' ;
  with(tmpNode.style) {
    padding = '2px' ;
    border = 'solid 1px red' ;
    position = 'absolute' ;
//    width = '200px' ;
//    height = '50px' ;
  }

  var selectedItems = this.getSelectedItems(dragBlock) ;
  
  if (selectedItems.length > 0) {
    for (var i=0; i<selectedItems.length; i++) {
//      var tmp = selectedItems[i].cloneNode(true) ;
//      tmp.innerHTML = selectedItems[i].text ;
      if (selectedItems[i]) {
        tmpNode.appendChild(selectedItems[i].cloneNode(true)) ;
      } else {
        window.alert(selectedItems[i] + ' index: ' + i) ;
      }
    }
  } else {
    tmpNode.appendChild(dragBlock.cloneNode(true)) ;
  }
  
  document.body.appendChild(tmpNode) ;
  
  this.DragDrop.initCallback = this.initCallback ;
  this.DragDrop.dragCallback = this.dragCallback ;
  this.DragDrop.dropCallback = this.dropCallback ;
  
  this.DragDrop.init(dropableObjs, clickBlock, tmpNode, e) ;
  return false ;
} ;

UIMailDnD.prototype.synDragObjectPos = function(dndEvent) {
  if (!dndEvent.backupMouseEvent) {
    return ;
  }
  var dragObject = dndEvent.dragObject ;
  var mouseX = eXo.core.Browser.findMouseXInPage(dndEvent.backupMouseEvent) ;
  var mouseY = eXo.core.Browser.findMouseYInPage(dndEvent.backupMouseEvent)
  dragObject.style.top = mouseY + 'px' ;
  dragObject.style.left = mouseX + 'px' ;
} ;

UIMailDnD.prototype.initCallback = function(dndEvent) {
  eXo.mail.UIMailDnD.synDragObjectPos(dndEvent) ;
} ;

UIMailDnD.prototype.dragCallback = function(dndEvent) {
  var dragObject = dndEvent.dragObject ;

  eXo.mail.UIMailDnD.synDragObjectPos(dndEvent) ;
  
  if (dndEvent.foundTargetObject) {
    if (this.foundTargetObjectCatch != dndEvent.foundTargetObject) {
      if(this.foundTargetObjectCatch) {
        this.foundTargetObjectCatch.style[scKey] = this.foundTargetObjectCatchStyle ;
      }
      this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
      this.foundTargetObjectCatchStyle = this.foundTargetObjectCatch.style[scKey] ;
      this.foundTargetObjectCatch.style[scKey] = scValue ;
    }
  } else {
    if (this.foundTargetObjectCatch) {
      this.foundTargetObjectCatch.style[scKey] = this.foundTargetObjectCatchStyle ;
    }
    this.foundTargetObjectCatch = null ;
  }
} ;

UIMailDnD.prototype.dropCallback = function(dndEvent) {
  var dragObject = dndEvent.dragObject ;
  if (this.foundTargetObjectCatch) {
    this.foundTargetObjectCatch.style[scKey] = this.foundTargetObjectCatchStyle ;
  }
  this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
  if (this.foundTargetObjectCatch) {
    eXo.core.DOMUtil.findDescendantsByClass(dndEvent.clickObject, 'input', 'checkbox')[0].checked = true ;
    // request service
    var folder2MoveNode = document.createElement('input') ;
    folder2MoveNode.type = 'hidden' ;
    folder2MoveNode.name = 'folder' ;
    folder2MoveNode.value = this.foundTargetObjectCatch.getAttribute('folder') ;
    
    var uiMsgList = document.getElementById('UIMessageList') ;
    uiMsgList.appendChild(folder2MoveNode) ;
    
    uiMsgList.action = uiMsgList.action + '&objectId=' + folder2MoveNode.value ;
    
    eXo.webui.UIForm.submitForm('UIMessageList','MoveDirectMessages', true)
  }
  document.body.removeChild(dndEvent.dragObject) ;
} ;

eXo.mail.UIMailDnD = new UIMailDnD();