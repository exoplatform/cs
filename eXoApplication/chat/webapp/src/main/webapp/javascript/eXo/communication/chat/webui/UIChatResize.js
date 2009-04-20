/**
 * @auther Uoc Nguyen
 * Using to support resize UI component in chat application
 */
function UIChatResize() {
  this.callbackStack = {};
}

/**
 * Register UI component resizeable
 *
 * @param {HTMLElement} resizeHandleObject
 * @param {Function} callback
 * @param {Boolean} forceReplaceEvent
 */
UIChatResize.prototype.register = function(resizeHandleObject, callback, forceReplaceEvent) {
  this.callbackStack[resizeHandleObject] = callback;
  var AdvancedDOMEvent = eXo.communication.chat.core.AdvancedDOMEvent;
  if (forceReplaceEvent) {
    resizeHandleObject.onmousedown = this.init;
  } else {
    AdvancedDOMEvent.addEventListener(resizeHandleObject, 'mousedown', this.init, false);
  }
};

/**
 * Initialize method
 *
 * @param {Event} event
 */
UIChatResize.prototype.init = function(event) {
  if(!event) event = window.event ;
  var srcObj = event.srcElement || event.target;
  var thys = eXo.communication.chat.webui.UIChatResize;
  thys.resizeHandleObject = srcObj;
  thys.portletWindow = eXo.core.DOMUtil.findAncestorByClass(srcObj, 'UIResizeObject');
  if (!thys.portletWindow) return;
  var DOMUtil = eXo.core.DOMUtil;

  if (thys.portletWindow.getAttribute('minwidth')) {
    thys.portletWindow.minWidth = parseInt(thys.portletWindow.getAttribute('minwidth'));
  }
  if (thys.portletWindow.getAttribute('minheight')) {
    thys.portletWindow.minHeight = parseInt(thys.portletWindow.getAttribute('minheight'));
  }

  var resizeWindowTmp = thys.resizeWindowTmp || document.createElement('div');
  var oWidth = thys.portletWindow.offsetWidth;
  var oHeight = thys.portletWindow.offsetHeight;
  if (DOMUtil.findFirstDescendantByClass(thys.portletWindow, 'div', 'WindowBarLeft')) {
    oHeight = 0;
    var windowCompClass = ['WindowBarLeft', 'MiddleDecoratorLeft', 'BottomDecoratorLeft'];
    for (var i=0; i<windowCompClass.length; i++) {
      var tmpNode = DOMUtil.findFirstChildByClass(thys.portletWindow, 'div', windowCompClass[i]);
      if (tmpNode) {
        oHeight += tmpNode.offsetHeight;
      }
    }
  }
  //var popupContentClass = ['UINewRoom', 'UIJoinRoom', 'ExoMesseageDecorator'];
  var popupContentClass = ['ExoMesseageDecorator'];
  for (var i=0; i<popupContentClass.length; i++) {
    var tmpNode = DOMUtil.findFirstChildByClass(thys.portletWindow, 'div', 'ExoMesseageDecorator');
    if (tmpNode) {
      oHeight = tmpNode.offsetHeight;
    }
  }
  var oTop = thys.portletWindow.offsetTop;
  var oLeft = thys.portletWindow.offsetLeft;
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

  thys.originalWidth   = oWidth;
  thys.originalHeight  = oHeight;
  
  thys.resizeWindowTmp = resizeWindowTmp;

  document.body.appendChild(resizeWindowTmp);

  thys.initMouseX   = event.clientX ;
  thys.initMouseY   = event.clientY ;

  var AdvancedDOMEvent = eXo.communication.chat.core.AdvancedDOMEvent;
  AdvancedDOMEvent.addEventListener(document.body, 'mousemove', thys.resizeWindowEvt, false);
  AdvancedDOMEvent.addEventListener(document.body, 'mouseup', thys.endResizeWindowEvt, false);

  AdvancedDOMEvent.cancelEvent(event);
  return false;
};

UIChatResize.prototype.resizeWindowEvt = function(event) {
  if(!event) event = window.event ;
  var thys = eXo.communication.chat.webui.UIChatResize;
  var deltaX       = event.clientX - thys.initMouseX ;
  var deltaY       = event.clientY - thys.initMouseY ;
  
  var newWidth = Math.max(10, (thys.originalWidth + deltaX));
  var newHeight = Math.max(10, (thys.originalHeight + deltaY));

  thys.resizeWindowTmp.style.width  = newWidth + "px" ;
  thys.resizeWindowTmp.style.height = newHeight + "px" ;
} ;

UIChatResize.prototype.endResizeWindowEvt = function(event) {
  if(!event) event = window.event ;
  var thys = eXo.communication.chat.webui.UIChatResize;

  var deltaX = event.clientX - thys.initMouseX ;
  var deltaY = event.clientY - thys.initMouseY ;

  var newWidth = Math.max(10, (thys.originalWidth + deltaX));
  var newHeight = Math.max(10, (thys.originalHeight + deltaY));
  if (thys.portletWindow.minWidth) {
    if (newWidth < thys.portletWindow.minWidth) {
      newWidth = thys.portletWindow.minWidth;
    }
  }
  if (thys.portletWindow.minHeight) {
    if (newHeight < thys.portletWindow.minHeight) {
      newHeight = thys.portletWindow.minHeight;
    }
  }
  window.jsconsole.warn('New height = ' + newHeight);

  thys.portletWindow.style.width = newWidth + 'px';
  thys.portletWindow.style.height = newHeight + 'px';
  //if (document.getElementById('UIPageDesktop')) {
    //var UIApplicationNode = eXo.core.DOMUtil.findFirstDescendantByClass(thys.portletWindow, 'div', 'UIApplication1');
    //UIApplicationNode.style.height = newHeight + 'px';
  //}

  if (thys.portletWindow.UIWindow) {
    thys.portletWindow.UIWindow._setSize(newWidth, newHeight);
  }

  thys.resizeWindowTmp.style.display = 'none';
  thys.portletWindow   = null ;
  thys.originalWidth   = null;
  thys.originalHeight  = null;

  var AdvancedDOMEvent = eXo.communication.chat.core.AdvancedDOMEvent;
  AdvancedDOMEvent.removeEventListener(document.body, 'mousemove', thys.resizeWindowEvt);
  AdvancedDOMEvent.removeEventListener(document.body, 'mouseup', thys.endResizeWindowEvt);

  if (thys.callbackStack[thys.resizeHandleObject]) {
    (thys.callbackStack[thys.resizeHandleObject])({deltaX: deltaX, deltaY: deltaY});
  }
} ;  

eXo.communication.chat.webui.UIChatResize = new UIChatResize();
