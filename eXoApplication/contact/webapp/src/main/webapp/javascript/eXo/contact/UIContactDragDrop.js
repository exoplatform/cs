/**
 * @author uocnb
 */
function UIContactDragDrop() {
  this.scKey = 'border' ;
  this.scValue = 'solid 1px #000' ;
  this.DOMUtil = eXo.core.DOMUtil ;
  this.DragDrop = eXo.core.DragDrop ;
  this.dropableSets = [] ;
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
  var publicAddressBook = this.DOMUtil.findDescendantsByClass(uiAddressBooksNode, 'div', 'PublicAddress') ;
  this.dropableSets[this.dropableSets.length] = publicAddressBook[0] ;
  var uiTagsNode = document.getElementById('UITags') ;
  var tagLists = this.DOMUtil.findDescendantsByClass(uiTagsNode, 'div', 'ItemList') ;
  for (var i=0; i<tagLists.length; i++) {
    this.dropableSets[this.dropableSets.length] = tagLists[i] ;
  }
  var tagContainer = document.getElementById('UITags') ;
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
  if (contactLists && contactLists.length > 0) {
    this.listView = true ;
  }
} ;

UIContactDragDrop.prototype.dndTrigger = function(e){
  e = e ? e : window.event;
  if (e.button == 0 || e.which == 1) {
    return eXo.contact.UIContactDragDrop.initDnD(eXo.contact.UIContactDragDrop.dropableSets, this, this, e);
  }
  return true ;    
} ;

UIContactDragDrop.prototype.initDnD = function(dropableObjs, clickObj, dragObj, e) {
  var clickBlock = (clickObj && clickObj.tagName) ? clickObj : document.getElementById(clickObj) ;
  var dragBlock = (dragObj && dragObj.tagName) ? dragObj : document.getElementById(dragObj) ;
  
  var blockWidth = clickBlock.offsetWidth ;
  var blockHeight = clickBlock.offsetHeight ;
  
  if (clickBlock.className.indexOf('VCardContent') != -1) {
    this.listView = false ;
  }
  
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
  if (!this.listView) {
    contactListNode.className = 'UIVCards' ;
    contactListNode.appendChild(dragBlock.cloneNode(true)) ;
  } else {
    contactListNode = document.createElement('table') ;
    contactListNode.className = 'UIGrid' ;
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
  }
  with(uiContactPortletNode.style) {
    border = 'solid 1px #A5A5A5' ;
    position = 'absolute' ;
    width = blockWidth + 'px' ;
    display = 'none' ;
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
    var uiContacts = document.getElementById('UIContacts') ;
    if (eXo.contact.UIContactDragDrop.listView) {
      eXo.core.DOMUtil.findDescendantsByClass(dndEvent.clickObject, 'input', 'checkbox')[0].checked = true ;
    } else {
      var checkBoxElement = document.createElement('input') ;
      checkBoxElement.style.display = 'none' ;
      checkBoxElement.name = dndEvent.clickObject.id ;
      checkBoxElement.value = 'false' ;
      checkBoxElement.checked = true ;
      dndEvent.clickObject.appendChild(checkBoxElement) ;
    }
    
    if (this.foundTargetObjectCatch.className.indexOf('UITags') != -1) {
      eXo.webui.UIForm.submitForm('UIContacts','TagChecked', true) ;
      return ;
    }
    if (this.foundTargetObjectCatch.className.indexOf('PublicAddress') != -1) {
      uiContacts.action = uiContacts.action + '&isDND=true' ;
      eXo.webui.UIForm.submitForm('UIContacts','MoveContacts', true) ;
      return ;
    }
    var contactTypeId = this.foundTargetObjectCatch.getAttribute('tagname') ;
    if (!contactTypeId) {
      var contactTypeNode = eXo.core.DOMUtil.findDescendantsByClass(this.foundTargetObjectCatch, 'a', 'IconHolder')[0] ;
      contactTypeId = contactTypeNode.id ;
    }

    if (eXo.core.DOMUtil.findAncestorByClass(this.foundTargetObjectCatch, 'UITags')) {
	    uiContacts.action = uiContacts.action + '&objectId=' + contactTypeId ;
	    eXo.webui.UIForm.submitForm('UIContacts','DNDContactsToTag', true) ;
	    return ;
    }
    var addressBookType = this.foundTargetObjectCatch.getAttribute('addresstype') ;
    
    // request service
    uiContacts.action = uiContacts.action + '&objectId=' + contactTypeId + '&addressType=' + addressBookType ;
    eXo.webui.UIForm.submitForm('UIContacts','DNDContacts', true)
  }
} ;

eXo.contact.UIContactDragDrop = new UIContactDragDrop();
