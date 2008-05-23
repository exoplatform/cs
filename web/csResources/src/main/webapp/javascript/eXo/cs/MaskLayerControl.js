/**
 * @author uocnb
 */
function MaskLayerControl() {
  this.domUtil = eXo.core.DOMUtil ;
}

MaskLayerControl.prototype.init = function(root){
  root = (typeof(root) == 'string') ? document.getElementById(root) : root ;
  var nodeList = this.domUtil.findDescendantsByClass(root, 'span', 'ViewDownloadIcon') ;
  for (var i=0; i<nodeList.length; i++) {
    var linkNode = nodeList[i].getElementsByTagName('a')[0] ;
    linkNode.onclick = this.showPictureWrapper ;
  }
} ;

MaskLayerControl.prototype.showPictureWrapper = function() {
  eXo.cs.MaskLayerControl.showPicture(this) ;
  return false ;
} ;

/**
 * 
 * @param {Element} node
 */
MaskLayerControl.prototype.showPicture = function(node) {
  var attachmentContent = this.domUtil.findAncestorByClass(node, 'AttachmentContent') ;
  var imgSrcNode = this.domUtil.findDescendantsByClass(attachmentContent, 'img', 'AttachmentFile')[0] ;
  var containerNode = document.createElement('div') ;
  with (containerNode.style) {
    margin = 'auto' ;
    top = '20px' ;
    left = '5%' ;
    width = '90%' ;
    height = '95%' ;
  }
  var imageNode = document.createElement('img') ;
  imageNode.src = imgSrcNode.src ;
  imageNode.setAttribute('alt', 'Click to close.') ;
  with (imageNode.style) {
    height = '100%' ;
  }
  containerNode.appendChild(imageNode) ;
  containerNode.setAttribute('title', 'Click to close') ;
  containerNode.onclick = this.hidePicture ;
  maskNode = eXo.core.UIMaskLayer.createMask('UIPortalApplication', containerNode, 100, 'CENTER') ;
  eXo.core.Browser.addOnScrollCallback('MaskLayerControl', this.scrollHandler) ;
} ;

MaskLayerControl.prototype.scrollHandler = function() {
  eXo.core.UIMaskLayer.object.style.top = 20 + document.body.scrollTop + 'px' ;
} ;

MaskLayerControl.prototype.hidePicture = function() {
  eXo.core.Browser.onScrollCallback.remove('MaskLayerControl') ;
  var maskContent = eXo.core.UIMaskLayer.object ;
  var maskNode = document.getElementById("MaskLayer") || document.getElementById("subMaskLayer") ;
  if (maskContent) maskContent.parentNode.removeChild(maskContent) ;
  if (maskNode) maskNode.parentNode.removeChild(maskNode) ;
} ;

if (!eXo.cs) eXo.cs = {} ;
eXo.cs.MaskLayerControl = new MaskLayerControl() ;
