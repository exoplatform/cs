/**
 * @author uocnb
 */
function UIContactDragDrop() {
  this.scKey = 'border' ;
  this.scValue = 'solid 1px #000' ;
  this.DOMUtil = eXo.core.DOMUtil ;
  this.DragDrop = eXo.core.DragDrop ;
  this.dropableSets = [] ;
  this.thumbnailView = false ;
  this.listView = false ;
} ;

UIContactDragDrop.prototype.init = function() {
  this.uiContactPortlet = document.getElementById('UIContactPortlet') ;
  this.getAllDropableSets() ;
  this.regDnDItem() ;
} ;

UIContactDragDrop.prototype.getAllDropableSets = function() {
  var uiAddressBooksNode = document.getElementById('UIAddressBooks') ;
  var addressBooks = this.DOMUtil.findDescendantsByClass(uiAddressBooksNode, 'div', 'ItemList') ;
  for (var i=0; i<addressBooks.length; i++) {
    this.dropableSets[this.dropableSets.length] = addressBooks[i] ;
  }
  var uiTagsNode = document.getElementById('UITags') ;
  var tagLists = this.DOMUtil.findDescendantsByClass(uiTagsNode, 'div', 'ItemList') ;
  for (var i=0; i<tagLists.length; i++) {
    this.dropableSets[this.dropableSets.length] = tagLists[i] ;
  }
  var tagContainer = document.getElementById('UITags') ;
//  if (tagContainer &&  tagLists.length <= 0) {
  if (tagContainer) {
    this.dropableSets[this.dropableSets.length] = tagContainer ;
  }
} ;

UIContactDragDrop.prototype.regDnDItem = function() {
  // for thumbnail
  var uiContactsNode = document.getElementById('UIContacts') ;
  var vCards = this.DOMUtil.findDescendantsByClass(uiContactsNode, 'div', 'VCardContent') ;
  for (var i=0; i<vCards.length; i++) {
    vCards[i].onmousedown = this.dndTrigger ;
  }
  // for list
  var contactLists = this.DOMUtil.findDescendantsByClass(uiContactsNode, 'tr', 'UIContactList') ;
  for (var i=0; i<contactLists.length; i++) {
    contactLists[i].onmousedown = this.dndTrigger ;
  }
  if (vCards && vCards.length > 0) {
    this.thumbnailView = true ;
  }
  if (contactLists && contactLists.length > 0) {
    this.listView = true ;
  }
} ;

UIContactDragDrop.prototype.dndTrigger = function(e) {
  e = e ? e : window.event ;
  return eXo.contact.UIContactDragDrop.initDnD(eXo.contact.UIContactDragDrop.dropableSets, this, this, e) ;
} ;

UIContactDragDrop.prototype.initDnD = function(dropableObjs, clickObj, dragObj, e) {
  var clickBlock = (clickObj && clickObj.tagName) ? clickObj : document.getElementById(clickObj) ;
  var dragBlock = (dragObj && dragObj.tagName) ? dragObj : document.getElementById(dragObj) ;
  
  var blockWidth = clickBlock.offsetWidth ;
  var blockHeight = clickBlock.offsetHeight ;
  
  var uiContactPortletNode = document.createElement('div') ;
  uiContactPortletNode.className = 'UIContactPortlet' ;
  
  var uiContactContainerNode = document.createElement('div') ;
  uiContactContainerNode.className = 'UIContactContainer' ;
  
  with (uiContactContainerNode.style) {
    margin = '0px' ;
    padding = '0px' ;
  }
  
  uiContactPortletNode.appendChild(uiContactContainerNode) ;

  var contactListNode = document.createElement('div') ;
  if (this.thumbnailView) {
    contactListNode.className = 'UIVCards' ;
  } else {
    contactListNode = document.createElement('table') ;
    contactListNode.className = 'UIGrid' ;
  }
  with(uiContactPortletNode.style) {
    border = 'solid 1px #A5A5A5' ;
    position = 'absolute' ;
    width = blockWidth + 'px' ;
    display = 'none' ;
  }
  
  if (this.listView) {
    var selectedItems = eXo.cs.FormHelper.getSelectedElementByClass(
                          document.getElementById('UIListUsers') , 'UIContactList', dragBlock) ;
    if (selectedItems.length > 0) {
      for (var i=0; i<selectedItems.length; i++) {
        if (selectedItems[i] && selectedItems[i].cloneNode) {
          contactListNode.appendChild(selectedItems[i].cloneNode(true)) ;
        }
      }
    } else {
      contactListNode.appendChild(dragBlock.cloneNode(true)) ;
    }  
  } else {
    contactListNode.appendChild(dragBlock.cloneNode(true)) ;
  }
  uiContactContainerNode.appendChild(contactListNode) ;
  uiContactPortletNode.appendChild(uiContactContainerNode) ;
  document.body.appendChild(uiContactPortletNode) ;
  this.DragDrop.initCallback = this.initCallback ;
  this.DragDrop.dragCallback = this.dragCallback ;
  this.DragDrop.dropCallback = this.dropCallback ;
  
  this.DragDrop.init(dropableObjs, clickBlock, uiContactPortletNode, e) ;
  return false ;
} ;

UIContactDragDrop.prototype.synDragObjectPos = function(dndEvent) {
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

UIContactDragDrop.prototype.initCallback = function(dndEvent) {
  eXo.contact.UIContactDragDrop.synDragObjectPos(dndEvent) ;
} ;

UIContactDragDrop.prototype.dragCallback = function(dndEvent) {
  var dragObject = dndEvent.dragObject ;
  if (!dragObject.style.display ||
      dragObject.style.display == 'none') {
    dragObject.style.display = 'block' ;
  }

  eXo.contact.UIContactDragDrop.synDragObjectPos(dndEvent) ;
  
  if (dndEvent.foundTargetObject) {
    if (this.foundTargetObjectCatch != dndEvent.foundTargetObject) {
      if(this.foundTargetObjectCatch) {
        this.foundTargetObjectCatch.style[eXo.contact.UIContactDragDrop.scKey] = this.foundTargetObjectCatchStyle ;
      }
      this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
      this.foundTargetObjectCatchStyle = this.foundTargetObjectCatch.style[eXo.contact.UIContactDragDrop.scKey] ;
      this.foundTargetObjectCatch.style[eXo.contact.UIContactDragDrop.scKey] = eXo.contact.UIContactDragDrop.scValue ;
    }
  } else {
    if (this.foundTargetObjectCatch) {
      this.foundTargetObjectCatch.style[eXo.contact.UIContactDragDrop.scKey] = this.foundTargetObjectCatchStyle ;
    }
    this.foundTargetObjectCatch = null ;
  }
} ;

UIContactDragDrop.prototype.dropCallback = function(dndEvent) {
  document.body.removeChild(dndEvent.dragObject) ;
  if (this.foundTargetObjectCatch) {
    this.foundTargetObjectCatch.style[eXo.contact.UIContactDragDrop.scKey] = this.foundTargetObjectCatchStyle ;
  }
  this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
  if (this.foundTargetObjectCatch) {
    if (eXo.contact.UIContactDragDrop.listView) {
      eXo.core.DOMUtil.findDescendantsByClass(dndEvent.clickObject, 'input', 'checkbox')[0].checked = true ;
    }
    if (this.foundTargetObjectCatch.className == 'UITags') {
      eXo.webui.UIForm.submitForm('UIContacts','TagChecked', true) ;
      return ;
    }
    var contactTypeId = this.foundTargetObjectCatch.getAttribute('tagname') ;
    if (!contactTypeId) {
      var contactTypeNode = eXo.core.DOMUtil.findDescendantsByClass(this.foundTargetObjectCatch, 'a', 'IconHolder')[0] ;
      contactTypeId = contactTypeNode.id ;
    }
    
    var contactType = false ;
    
    if (eXo.core.DOMUtil.findAncestorByClass(this.foundTargetObjectCatch, 'PersonalAddress')) {
      contactType = 'private'
    } else if (eXo.core.DOMUtil.findAncestorByClass(this.foundTargetObjectCatch, 'PublicAddress')) {
      contactType = 'public'
    } else if (eXo.core.DOMUtil.findAncestorByClass(this.foundTargetObjectCatch, 'UITags')) {
      contactType = 'tag' ;
    }
    
    // request service
    var uiMsgList = document.getElementById('UIContacts') ;
    uiMsgList.action = uiMsgList.action + '&objectId=' + contactTypeId + '&contactType=' + contactType ;
    eXo.webui.UIForm.submitForm('UIContacts','DNDContacts', true)
  }
} ;

eXo.contact.UIContactDragDrop = new UIContactDragDrop();