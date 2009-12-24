/*
 * @author Nam Phung
 */

function VerticalSpliter() { };

VerticalSpliter.prototype.doResize = function(e, markerobj, leftAreaObj, rightAreaObj) {
  _e = (window.event) ? window.event : e ;
  this.posX = _e.clientX; 
  var marker = (typeof(markerobj) == "string") ? document.getElementById(markerobj) : markerobj ;
  
  if (leftAreaObj) {
    this.leftArea = (typeof(leftAreaObj) == "string") ? document.getElementById(leftAreaObj) : leftAreaObj;
  }
  
  if (!this.leftArea) {
    this.leftArea = eXo.core.DOMUtil.findPreviousElementByTagName(marker, "div") ;
  }
  
  if (rightAreaObj) {
    this.rightArea = (typeof(rightAreaObj) == "string") ? document.getElementById(rightAreaObj) : rightAreaObj;
  }
  
  if (!this.rightArea) {
    this.rightArea = eXo.core.DOMUtil.findNextElementByTagName(marker, "div") ; 
  }

  this.leftArea.style.width = this.leftArea.offsetWidth + "px" ;
  this.rightArea.style.width = this.rightArea.offsetWidth + "px" ;  
  this.leftX = this.leftArea.offsetWidth ;
  this.rightX = this.rightArea.offsetWidth ;
  document.onmousemove = eXo.mail.VerticalSpliter.adjustWidth ;  
  document.onmouseup = eXo.mail.VerticalSpliter.clear ;
};

VerticalSpliter.prototype.adjustWidth = function(evt) {
  evt = (window.event) ? window.event : evt ;
  var VerticalSpliter = eXo.mail.VerticalSpliter ;
  var delta = evt.clientX - VerticalSpliter.posX ;
  if (delta > 4 || delta < -4) {
	  var rightWidth = (VerticalSpliter.rightX - delta) ;
	  var leftWidth = (VerticalSpliter.leftX + delta) ;
	  if (leftWidth <= 0  || rightWidth <= 0) return ;
	  VerticalSpliter.leftArea.style.width =  leftWidth + "px" ;
	  VerticalSpliter.rightArea.style.width =  rightWidth + "px" ;
  }
} ;

VerticalSpliter.prototype.clear = function() {
  document.onmousemove = null ;
} ;

VerticalSpliter.prototype.initVerticalLayout = function() {
	eXo.mail.VerticalSpliter = new VerticalSpliter();
	try {
		var container = document.getElementById("VeticalLeftLayout");
		var parentWidth = document.getElementById("VeticalLeftLayout").parentNode.offsetWidth;
		var oldLeftContentWidth = document.getElementById("uiMessageListResizableArea").offsetWidth + "px";
		container.style.width = (parentWidth / 2 - 3) + "px";
		document.getElementById("uiMessageListResizableArea").style.width = oldLeftContentWidth;
		document.getElementById("VerticalRightLayout").style.width = (parentWidth / 2 - 5) + "px" ;
		container.style.overflow = "auto";
		var height = container.offsetHeight;
		if (!height || height < 600) {
			height = 600;
			container.style.height = height + "px" ;;
		}
		document.getElementById("ResizeReadingPane").style.height = height + "px" ;
		document.getElementById("VerticalRightLayout").style.height = height + "px" ;
		document.getElementById("SpliterResizableArea").style.height = height + "px" ;
		document.getElementById("UIMessagePreview").style.height = height + "px" ;
		document.getElementById("UIMessagePreview").style.overflow = "auto";
	} catch(e){ };
}

eXo.mail.VerticalSpliter = new VerticalSpliter();

