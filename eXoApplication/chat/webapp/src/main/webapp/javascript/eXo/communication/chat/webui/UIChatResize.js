function UIChatResize() {
  this.callbackStack = {};
}

UIChatResize.prototype.register = function(resizeHandleObject, callback, forceReplaceEvent) {
  this.callbackStack[resizeHandleObject] = callback;
  var AdvancedDOMEvent = eXo.communication.chat.core.AdvancedDOMEvent;
  if (forceReplaceEvent) {
    resizeHandleObject.onmousedown = this.init;
  } else {
    AdvancedDOMEvent.addEventListener(resizeHandleObject, 'mousedown', this.init, false);
  }
};

UIChatResize.prototype.init = function(event) {
  if(!event) event = window.event ;
  var srcObj = event.srcElement || event.target;
  var that = eXo.communication.chat.webui.UIChatResize;
  that.resizeHandleObject = srcObj;
  that.portletWindow = eXo.core.DOMUtil.findAncestorByClass(srcObj, 'UIResizeObject');
  if (!that.portletWindow) return;
  var DOMUtil = eXo.core.DOMUtil;

  if (that.portletWindow.getAttribute('minwidth')) {
    that.portletWindow.minWidth = parseInt(that.portletWindow.getAttribute('minwidth'));
  }
  if (that.portletWindow.getAttribute('minheight')) {
    that.portletWindow.minHeight = parseInt(that.portletWindow.getAttribute('minheight'));
  }

  var resizeWindowTmp = that.resizeWindowTmp || document.createElement('div');
  var oWidth = that.portletWindow.offsetWidth;
  var oHeight = that.portletWindow.offsetHeight;
  if (DOMUtil.findFirstDescendantByClass(that.portletWindow, 'div', 'WindowBarLeft')) {
    oHeight = 0;
    var windowCompClass = ['WindowBarLeft', 'MiddleDecoratorLeft', 'BottomDecoratorLeft'];
    for (var i=0; i<windowCompClass.length; i++) {
      var tmpNode = DOMUtil.findFirstChildByClass(that.portletWindow, 'div', windowCompClass[i]);
      if (tmpNode) {
        oHeight += tmpNode.offsetHeight;
      }
    }
  }
  //var popupContentClass = ['UINewRoom', 'UIJoinRoom', 'ExoMesseageDecorator'];
  var popupContentClass = ['ExoMesseageDecorator'];
  for (var i=0; i<popupContentClass.length; i++) {
    var tmpNode = DOMUtil.findFirstChildByClass(that.portletWindow, 'div', 'ExoMesseageDecorator');
    if (tmpNode) {
      oHeight = tmpNode.offsetHeight;
    }
  }
  var oTop = that.portletWindow.offsetTop;
  var oLeft = that.portletWindow.offsetLeft;
  var workspaceControlWidth = 0;

  try {
    workspaceControlWidth = eXo.portal.UIControlWorkspace.width;
  } catch (error) {}

  var UIPageDesktopNode = document.getElementById('UIPageDesktop');
  if (UIPageDesktopNode) {
    oTop += eXo.core.Browser.findPosYInContainer(eXo.communication.chat.webui.UIMainChatWindow.rootNode, document.body);
    oLeft += eXo.core.Browser.findPosXInContainer(eXo.communication.chat.webui.UIMainChatWindow.rootNode, document.body);
    if (eXo.core.Browser.isIE7()) {
      oLeft -= workspaceControlWidth;
    }
  } else {
    if ((!eXo.core.Browser.isIE6() &&
        !eXo.core.Browser.isIE7())) {
      oLeft += workspaceControlWidth;
    }
  }

  window.jsconsole.warn('Old height = ' + oHeight);
  with (resizeWindowTmp.style) {
    position   = 'absolute';
    top        = oTop + 'px';
    left       = oLeft + 'px';
    width      = oWidth + 'px';
    height     = oHeight + 'px';
    display    = 'block';
    background = '#8FBDE5';
    opacity    = '0.7';
    filter     = 'alpha(opacity=70)';
    border     = 'solid 1px #4A67B1';
    zIndex     = '1000';
  }

  that.originalWidth   = oWidth;
  that.originalHeight  = oHeight;
  
  that.resizeWindowTmp = resizeWindowTmp;

  document.body.appendChild(resizeWindowTmp);

  that.initMouseX   = event.clientX ;
  that.initMouseY   = event.clientY ;

  var AdvancedDOMEvent = eXo.communication.chat.core.AdvancedDOMEvent;
  AdvancedDOMEvent.addEventListener(document.body, 'mousemove', that.resizeWindowEvt, false);
  AdvancedDOMEvent.addEventListener(document.body, 'mouseup', that.endResizeWindowEvt, false);

  AdvancedDOMEvent.cancelEvent(event);
  return false;
};

UIChatResize.prototype.resizeWindowEvt = function(event) {
  if(!event) event = window.event ;
  var that = eXo.communication.chat.webui.UIChatResize;
  var deltaX       = event.clientX - that.initMouseX ;
  var deltaY       = event.clientY - that.initMouseY ;
  
  var newWidth = Math.max(10, (that.originalWidth + deltaX));
  var newHeight = Math.max(10, (that.originalHeight + deltaY));

  that.resizeWindowTmp.style.width  = newWidth + "px" ;
  that.resizeWindowTmp.style.height = newHeight + "px" ;
} ;

UIChatResize.prototype.endResizeWindowEvt = function(event) {
  if(!event) event = window.event ;
  var that = eXo.communication.chat.webui.UIChatResize;

  var deltaX = event.clientX - that.initMouseX ;
  var deltaY = event.clientY - that.initMouseY ;

  var newWidth = Math.max(10, (that.originalWidth + deltaX));
  var newHeight = Math.max(10, (that.originalHeight + deltaY));
  if (that.portletWindow.minWidth) {
    if (newWidth < that.portletWindow.minWidth) {
      newWidth = that.portletWindow.minWidth;
    }
  }
  if (that.portletWindow.minHeight) {
    if (newHeight < that.portletWindow.minHeight) {
      newHeight = that.portletWindow.minHeight;
    }
  }
  window.jsconsole.warn('New height = ' + newHeight);

  that.portletWindow.style.width = newWidth + 'px';
  that.portletWindow.style.height = newHeight + 'px';
  //if (document.getElementById('UIPageDesktop')) {
    //var UIApplicationNode = eXo.core.DOMUtil.findFirstDescendantByClass(that.portletWindow, 'div', 'UIApplication1');
    //UIApplicationNode.style.height = newHeight + 'px';
  //}

  if (that.portletWindow.UIWindow) {
    that.portletWindow.UIWindow._setSize(newWidth, newHeight);
  }

  that.resizeWindowTmp.style.display = 'none';
  that.portletWindow   = null ;
  that.originalWidth   = null;
  that.originalHeight  = null;

  var AdvancedDOMEvent = eXo.communication.chat.core.AdvancedDOMEvent;
  AdvancedDOMEvent.removeEventListener(document.body, 'mousemove', that.resizeWindowEvt);
  AdvancedDOMEvent.removeEventListener(document.body, 'mouseup', that.endResizeWindowEvt);

  if (that.callbackStack[that.resizeHandleObject]) {
    (that.callbackStack[that.resizeHandleObject])({deltaX: deltaX, deltaY: deltaY});
  }
} ;  

eXo.communication.chat.webui.UIChatResize = new UIChatResize();
