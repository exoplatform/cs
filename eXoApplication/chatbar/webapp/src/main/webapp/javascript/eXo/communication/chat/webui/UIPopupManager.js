/**
 * @author Uoc Nguyen
 *  email uoc.nguyen@exoplatform.com
 *
 *  This is an UI component object use to manage all popup window.
 */
function UIPopupManager() {	
  this.popupList = false;
  this.TOP_INDEX = 3;
  this.NORMAL_INDEX = 2;
  this.initialized = false;
  this.popupCnt = 0;
}

/**
 * Initializing method
 */
UIPopupManager.prototype.init = function() {
  if (this.initialized) {
    this.destroy();
  }
  this.popupList = {};
  this.initialized = true;
};

/**
 * Use when destroy component
 */
UIPopupManager.prototype.destroy = function() {
  this.popupList = null;
  this.initialized = false;
};

/**
 * Use to add component to list
 *
 * @param {UIPopupWindow} popupWindowObj
 */
UIPopupManager.prototype.addItem = function(popupWindowObj) {
  if (!this.initialized) {
    return;
  }
  for (var item in this.popupList) {
    if (this.popupList[item] == popupWindowObj) {
      return;
    }
  }
  popupWindowObj.UIPopupManager = this;
  popupWindowObj.rootNode.UIWindowManager = popupWindowObj;
  popupWindowObj.rootNode.onclick = this.requestFocus;
  this.popupList[this.popupCnt] = popupWindowObj;
  this.popupCnt ++;
};

/**
 * Call weh a popup window want to get focus
 *
 * @param {Event} event
 */
UIPopupManager.prototype.requestFocus = function(event) {
  event = event || window.event;
  var UIWindowManager = this.UIWindowManager;
  if (UIWindowManager) {
    window.setTimeout(function() {
        eXo.communication.chatbar.webui.UIPopupManager.focusEventFire(UIWindowManager);
      }, 50);
  }
  //eXo.communication.chatbar.core.AdvancedDOMEvent.cancelEvent(event);
  return true;
};

/**
 * Call when focus event is fired
 *
 * @param {UIPopupWindow} popupWindowObj
 */
UIPopupManager.prototype.focusEventFire = function(popupWindowObj) {
  thys = eXo.communication.chatbar.webui.UIPopupManager;
  if (!thys.initialized) {
    return;
  }
  window.jsconsole.debug('popup window request focus: ', popupWindowObj);
  // Set z-index for UIWindow on Web OS mode.
  var uiPageDesktop = document.getElementById('UIPageDesktop');
  if (uiPageDesktop) {
    var uiWindowList = eXo.core.DOMUtil.findDescendantsByClass(uiPageDesktop, 'UIWindow', 'div');
    for ( var i = 0; i < uiWindowList.length; i++) {
      var uiWindow = uiWindowList[i];
      if (uiWindow != popupWindowObj.rootNode &&
          (!uiWindow.style.zIndex || uiWindow.style.zIndex >= thys.TOP_INDEX)) {
        uiWindow.style.zIndex = thys.TOP_INDEX - 1;
      }
    }
  }
  for (var item in thys.popupList) {
    if (thys.popupList[item] == popupWindowObj) {
      if (popupWindowObj.rootNode.style.zIndex < thys.TOP_INDEX) {
        popupWindowObj.rootNode.style.zIndex = thys.TOP_INDEX;
      }
    } else {
      if (thys.popupList[item].rootNode.style.zIndex >= thys.TOP_INDEX) {
        thys.popupList[item].rootNode.style.zIndex = thys.TOP_INDEX - 1;
      }
    }
  }
};

eXo.communication.chatbar.webui.UIPopupManager = new UIPopupManager();
