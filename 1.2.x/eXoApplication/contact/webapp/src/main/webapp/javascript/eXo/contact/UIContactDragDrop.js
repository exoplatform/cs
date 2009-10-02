/**
 * @author uocnb
 */
function UIContactDragDrop() {
  this.scKey = 'border' ;
  this.scValue = 'solid 1px #000' ;
  this.DOMUtil = eXo.core.DOMUtil ;
  this.DragDrop = eXo.core.DragDrop ;
  this.listView = false ;
} ;

UIContactDragDrop.prototype.init = function() {
  this.dropableSets = [] ;
  this.uiContactPortlet = document.getElementById('UIContactPortlet') ;
  this.uiGrid = eXo.core.DOMUtil.findFirstDescendantByClass(this.uiContactPortlet,"table", "UIGrid") ;
  this.getAllDropableSets() ;
  this.regDnDItem() ;
} ;

DragDrop.prototype.findDropableTarget = function(dndEvent, dropableTargets, mouseEvent) {
  if(dropableTargets == null) return null ;
  var isDesktop = document.getElementById("UIPageDesktop");
  var extraLeft = 0 ;
  var extraTop = 0 ;
  if(isDesktop && dropableTargets[0]){
	extraLeft = eXo.core.DOMUtil.findAncestorByClass(dropableTargets[0],"UIResizableBlock").scrollLeft ;
	extraTop = eXo.core.DOMUtil.findAncestorByClass(dropableTargets[0],"UIResizableBlock").scrollTop ;
  }
  var mousexInPage = eXo.core.Browser.findMouseXInPage(mouseEvent) + extraLeft;
  var mouseyInPage = eXo.core.Browser.findMouseYInPage(mouseEvent) + extraTop;
  
	var clickObject = dndEvent.clickObject ;
	var dragObject = dndEvent.dragObject ;
  var foundTarget = null ;
  var len = dropableTargets.length ;
  for(var i = 0 ; i < len ; i++) {
    var ele =  dropableTargets[i] ;
    
    if(dragObject != ele && this.isIn(mousexInPage, mouseyInPage, ele)) {
      if(foundTarget == null) {
        foundTarget = ele ;
      } else {
        if(this.isAncestor(foundTarget, ele)) {
          foundTarget = ele ;
        }
      } 
    }
  }
 	
  return foundTarget ;
} ;

UIContactDragDrop.prototype.getAllDropableSets = function() {
  var uiAddressBooksNode = document.getElementById('UIAddressBooks') ;
  var addressBooks = this.DOMUtil.findDescendantsByClass(uiAddressBooksNode, 'div', 'ItemList') ;
  for (var i=0; i<addressBooks.length; i++) {
    if(eXo.core.DOMUtil.hasClass(addressBooks[i],"PublicAddressBook")) continue ;
    this.dropableSets[this.dropableSets.length] = addressBooks[i] ;
  }
  var personalAddress = this.DOMUtil.findDescendantsByClass(uiAddressBooksNode, 'div', 'PersonalAddress') ;
  this.dropableSets[this.dropableSets.length] = personalAddress[1] ;
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
	var menuItem = eXo.core.EventManager.getEventTargetByClass(e,"MenuItem");
  if(menuItem) return ;
  if (e.button == 1 || e.button == 0 ||e.which == 1) {
    return eXo.contact.UIContactDragDrop.initDnD(eXo.contact.UIContactDragDrop.dropableSets, this, this, e);
  }
  return true ;    
} ;

UIContactDragDrop.prototype.initDnD = function(dropableObjs, clickObj, dragObj, e) {
  var clickBlock = (clickObj && clickObj.tagName) ? clickObj : document.getElementById(clickObj) ;
  var dragBlock = (dragObj && dragObj.tagName) ? dragObj : document.getElementById(dragObj) ;
  
  var blockWidth = clickBlock.offsetWidth ;
  var blockHeight = clickBlock.offsetHeight ;
  
  if (eXo.core.DOMUtil.hasClass(clickBlock,'VCardContent')) {
    this.listView = false ;
  }
  
  var uiContactPortletNode = document.createElement('div') ;
  uiContactPortletNode.className = 'UIContactPortlet UIEmpty' ;
  
  var uiContactContainerNode = document.createElement('div') ;
  uiContactContainerNode.className = 'UIContactContainer' ;
  
  with (uiContactContainerNode.style) {
    margin = '0px' ;
    padding = '0px' ;
  }
  
  uiContactPortletNode.appendChild(uiContactContainerNode) ;

  var contactListNode = document.createElement('div') ;
  var cnt = 0;
  if (!this.listView) {
    contactListNode.className = 'UIVCards' ;
    contactListNode.appendChild(dragBlock.cloneNode(true)) ;
		contactListNode.style.border = "none" ;
  } else {
    contactListNode = document.createElement('table') ;
    contactListNode.setAttribute("class", "UIGrid") ;
    contactListNode.setAttribute("cellspacing", "0") ;
    contactListNode.setAttribute("borderspacing", "0") ;
    var tmpNode = document.createElement('tbody');
    var tmpRow = null ;
    var selectedItems = eXo.cs.FormHelper.getSelectedElementByClass(
                          this.uiGrid, 'UIContactList', dragBlock) ; //this.uiContactPortlet
    if (selectedItems.length > 0) {
      for (var i=0; i<selectedItems.length; i++) {
        if (selectedItems[i] && selectedItems[i].cloneNode) {
          tmpRow = selectedItems[i].cloneNode(true) ;
          tmpRow.style.height = blockHeight + "px" ;
          tmpNode.appendChild(tmpRow) ;
          cnt ++;
        }
      }
    } else {
      cnt ++;
      tmpNode.appendChild(dragBlock.cloneNode(true)) ;
    }  
    contactListNode.appendChild(tmpNode);
		uiContactPortletNode.style.border = 'solid 1px #A5A5A5' ;
  }
  with(uiContactPortletNode.style) {    
    position = 'absolute' ;
    width = blockWidth + 'px' ;
    display = 'none' ;
    height = (blockHeight * cnt) + 'px';
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
				if(eXo.core.Browser.browserType == "ie") this.foundTargetObjectCatch.removeAttribute("style") ;
      }
      this.foundTargetObjectCatch = dndEvent.foundTargetObject ;
      this.foundTargetObjectCatchStyle = this.foundTargetObjectCatch.style[eXo.contact.UIContactDragDrop.scKey] ;
      this.foundTargetObjectCatch.style[eXo.contact.UIContactDragDrop.scKey] = eXo.contact.UIContactDragDrop.scValue ;
    }
  } else {
    if (this.foundTargetObjectCatch) {
      this.foundTargetObjectCatch.style[eXo.contact.UIContactDragDrop.scKey] = this.foundTargetObjectCatchStyle ;
	  	if(eXo.core.Browser.browserType == "ie") this.foundTargetObjectCatch.removeAttribute("style") ;
    }
    this.foundTargetObjectCatch = null ;
  }
} ;

UIContactDragDrop.prototype.dropCallback = function(dndEvent) { 
  eXo.core.DOMUtil.removeElement(dndEvent.dragObject) ;
  if (this.foundTargetObjectCatch) {
    this.foundTargetObjectCatch.style[eXo.contact.UIContactDragDrop.scKey] = this.foundTargetObjectCatchStyle ;
		if(eXo.core.Browser.browserType == "ie") this.foundTargetObjectCatch.removeAttribute("style") ;
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
    if (eXo.core.DOMUtil.hasClass(this.foundTargetObjectCatch,'UITags')) {
      /* Commented by DungHM 
        eXo.webui.UIForm.submitForm('UIContacts','TagChecked', true) ;
      */
      eXo.webui.UIForm.submitForm('UIContacts','Tag', true) ;
      return ;
    }

    if (eXo.core.DOMUtil.hasClass(this.foundTargetObjectCatch,"PersonalAddress")) {
      eXo.webui.UIForm.submitForm('contact#UIContacts','SharedContacts', true)
      return ;
    }
    var contactTypeId = this.foundTargetObjectCatch.getAttribute('tagId') ;
    if (!contactTypeId) {      
    	contactTypeId = this.foundTargetObjectCatch.id ;
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