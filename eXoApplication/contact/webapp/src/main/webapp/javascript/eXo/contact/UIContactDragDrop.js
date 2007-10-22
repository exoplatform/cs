
function UIContactDragDrop() {
	
}
UIContactDragDrop.prototype.init = function() {
	eXo.contact.UIContactDragDrop.container = document.getElementById("UIVCards") ;
	eXo.contact.UIContactDragDrop.targets = eXo.core.DOMUtil.findDescendantsByClass(document.getElementById("UIAddressBooks"), "a", "AddressIcon") ;
	if (!eXo.contact.UIContactDragDrop.container) return ;
	var TitleVCards = eXo.core.DOMUtil.findDescendantsByClass(eXo.contact.UIContactDragDrop.container, "div", "TitleVCards") ;
	var len = TitleVCards.length ;
	for(var i = 0 ; i < len ; i ++) {
		TitleVCards[i].onmousedown = eXo.contact.UIContactDragDrop.initDND ;
	}	
}
UIContactDragDrop.prototype.initDND = function(evt) {
	var _e = window.event || evt ;
	var DOMUtil = eXo.core.DOMUtil ;
  var DragDrop = eXo.core.DragDrop ;
  var clickBlock = this ;
  var dragBlock = DOMUtil.findAncestorByClass(clickBlock, "VCardContent") ;
  var dragBlockX = eXo.core.Browser.findPosX(dragBlock) ;
  var dragBlockY = eXo.core.Browser.findPosY(dragBlock) ;
	var tmpElement = dragBlock.cloneNode(true) ;
	eXo.contact.UIContactDragDrop.container.appendChild(tmpElement) ;
	var clickElement = DOMUtil.findFirstDescendantByClass(tmpElement, "div", "TitleVCards") ;
	
  DragDrop.initCallback = function() {
		tmpElement.style.width = dragBlock.offsetWidth - 2 + "px" ;		
		tmpElement.style.position = "absolute" ; 	
		tmpElement.style.top = dragBlockY + "px" ;
		tmpElement.style.left = dragBlockX - 6 + "px" ;
  } ;
  	
  DragDrop.dragCallback = function() {
  	var UIContactDragDrop = eXo.contact.UIContactDragDrop ;
  	var len = UIContactDragDrop.targets.length ;
  	for(var i = 0 ; i < len ; i ++) {
  		var bgColor = UIContactDragDrop.targets[i].style.background ;
  		if (UIContactDragDrop.isTarget(tmpElement, UIContactDragDrop.targets[i])) {  			
  			UIContactDragDrop.targets[i].style.background = "red"
  		}else {
  			UIContactDragDrop.targets[i].style.background = bgColor ;
  		}
  	}
  } ;

  DragDrop.dropCallback = function() {
  	tmpElement.parentNode.removeChild(tmpElement) ;
  } ;
  
  DragDrop.init(null, clickElement, tmpElement, _e) ;
}

UIContactDragDrop.prototype.isTarget = function(object, target) {	
	object.x = eXo.core.Browser.findPosX(object) ;
	object.y = eXo.core.Browser.findPosY(object) ;
	target.x = eXo.core.Browser.findPosX(target) ;
	target.y = eXo.core.Browser.findPosY(target);
	if ((object.x > target.x) && (object.x < (target.x + target.offsetWidth)) && (object.y > target.y) && (object.y < (target.y + target.offsetHeight))) {
		return true ;
	} else {
		return false ;
	}
}

eXo.contact.UIContactDragDrop = new UIContactDragDrop() ;