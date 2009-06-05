/**
 * @author Uoc Nguyen
 *  email uoc.nguyen@exoplatform.com
 *
 * This is an SlideAlert manager UI component use to manage notification
 *
 * @param {UIMainChatWindow} UIMainChatWindow
 */
function UISlideAlert(UIMainChatWindow) {
  this.UIMainChatWindow = UIMainChatWindow;
  
  // For slide animation.
  this.TIME_STEP = 0.05*1000;
  this.DISTANCE_STEP = 5;
  this.currentDistance = 0;
  
  this.LIVE_TIME = 2*1000;
  
  this.CHANGE_MESSAGE_TIME_STEP = 2*1000;
  
  this.MAX_DISTANCE = 0;
  
  // Direction.
  this.UP = 'up'; // For show message;
  this.DOWN = 'down'; // For hide message;
  
  /*
  this.rootNode = document.createElement('div');
  with(this.rootNode.style) {
    backgroundColor = '#FFC053';
    fontSize = '13px';
    filter = 'alpha(opacity=85)';
    opacity = '0.85';
    width = '270px';
    padding = '5px';
    height = '50px';
    overflow = 'auto';
    right = '1px';
    top = '-1000px';
    position = 'absolute';
    display = 'block';
  };
  */
  
  this.messageQueue = [];
  this.messageInfoMap = new eXo.core.HashMap();
}

/**
 * Initializing method
 *
 * @param {UIMainChatWindow} UIMainChatWindow
 * @param {HTMLElement} rootNode
 */
UISlideAlert.prototype.init = function(UIMainChatWindow, rootNode) {
  this.UIMainChatWindow = UIMainChatWindow;
  this.rootNode = rootNode;
  
  this.MAX_DISTANCE = this.rootNode.offsetHeight;

  // 
  var DOMUtil = eXo.core.DOMUtil;
  try {
  this.msgCounterNote = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', 'IconBoxNote');
  //this.msgNotificationNode = DOMUtil.findFirstDescendantByClass(this.rootNode.parentNode, 'div', 'UINotification');
  //this.msgContentNode = DOMUtil.findFirstDescendantByClass(this.msgNotificationNode, 'div', 'UINotificationContent');
  //this.closeButton = DOMUtil.findFirstDescendantByClass(this.msgNotificationNode, 'a', 'Close');
  //this.closeButton.onclick = this.hideNotification;
  } catch (e) {
	  alert(e) ;
  }
  //this.rootNode.onmousemove = this.pauseAnim;
  //this.rootNode.onmouseout = this.resumeAnim;
};

/**
 * Use to pause current animation
 *
 * @param {Event} event
 */
UISlideAlert.prototype.pauseAnim = function(event) {
  var thys = eXo.communication.chat.webui.UISlideAlert;
  if (thys.hideId) {
    window.clearTimeout(thys.hideId);
    thys.hideId = null;
  }
};

/**
 * Use to resum paused animation
 *
 * @param {Event} event
 */
UISlideAlert.prototype.resumeAnim = function(event) {
  var thys = eXo.communication.chat.webui.UISlideAlert;
  if (!thys.hideId) {
    thys.hideId = window.setTimeout(thys.hide, thys.LIVE_TIME);
  }
};

/**
 * Use to keep position of notification popup when user is scroll page up or down
 */
UISlideAlert.prototype.positionKeeper = function() {
 /* var thys = eXo.communication.chat.webui.UISlideAlert;
  if (!thys.animateId) {
    thys.msgNotificationNode.style.top = document.documentElement.scrollTop + 'px';
  }*/
};

/**
 * Process message queueing
 */
UISlideAlert.prototype.queueProcess = function() {
  var thys = eXo.communication.chat.webui.UISlideAlert;
  if (thys.messageQueue.length > 0) {
    thys.setVisible(true);
    return;
  }
};

/**
 * Use to add new message to message queue
 *
 * @param {Message} msgObj
 * @param {TabId} tabId
 */
UISlideAlert.prototype.addMessage = function(msgObj, tabId) {
  this.messageQueue.push(msgObj);
  this.messageInfoMap.put(msgObj, tabId);
  this.setMsgCounter();
};

/**
 * Use to set message to notification message container
 *
 * @param {Message} msgObj
 */
UISlideAlert.prototype.setMessage = function(msgObj) {
  if (!msgObj) {
    return;
  }
  //this.msgContentNode.innerHTML = '';
  this.messageInfoMap.remove(msgObj);
 /* var msgNode = document.createElement('div');
  msgNode.tabId = this.messageInfoMap.get(msgObj);
  msgNode.className = 'Item';
  if (msgObj.substring) {
    msgObj = msgObj.replace(/(\w{4})/g, '$1<wbr>');
    msgNode.innerHTML = msgObj;
  } else {    
    msgNode.appendChild(msgObj);
  }*/
  
  /*msgNode.onclick = this.focusTab;
  msgNode.style.cursor = 'pointer';*/
  
  this.setMsgCounter();
  //this.msgContentNode.appendChild(msgNode);
};

/**
 * Use to request focus tab when user click to a notification message which related to tab
 *
 * @param {Event} event
 */
UISlideAlert.prototype.focusTab = function(event) {
  event = event || window.event;
  var srcElement = event.target || event.srcElement;
  if (this.tabId) {
    var thys = eXo.communication.chat.webui.UISlideAlert;
    thys.removeMessageByTabId(this.tabId);
    return eXo.communication.chat.webui.UIChatWindow.focusTab(this.tabId, true);
  }
  return true;
};

/**
 * Find and remove message in queue by tab id
 *
 * @param {TabId} tabId
 */
UISlideAlert.prototype.removeMessageByTabId = function(tabId) {
  var DOMUtil = eXo.core.DOMUtil;
  /*var items = DOMUtil.findDescendantsByClass(this.msgContentNode, 'div', 'Item');
  for (var i=0; i<items.length; i++) {
    if (items[i].tabId &&
        items[i].tabId == tabId) {
      DOMUtil.removeElement(items[i]);
    }
  }*/
  for (var i=0; i<this.messageQueue.length; i++) {
    var tabIdTmp = this.messageInfoMap.get(this.messageQueue[i]);
    if (tabIdTmp == tabId) {
      var msgObj = this.messageQueue[i]
      this.messageQueue.remove(msgObj);
      this.messageInfoMap.remove(msgObj);
    }
  }
  this.hideNotification();
};

/**
 * Set message counter to notification popup
 */
UISlideAlert.prototype.setMsgCounter = function() {
  //this.msgCounterNote.innerHTML = this.messageQueue.length + 1;
};

/**
 * Make component visible or not
 *
 * @param {Boolean} visible
 */
UISlideAlert.prototype.setVisible = function(visible) {
  var thys = eXo.communication.chat.webui.UISlideAlert;
  //thys.msgNotificationNode.style.display = (visible) ? 'block' : 'none';
  if (visible) {
    if (thys.messageQueue.length <= 0 ||
        thys.visible ||
        thys.hideId) {
      return;
    }
    thys.currentDistance = 0;
    thys.direction = thys.DOWN;
    var clearQueue = false;
    if (thys.messageQueue.length == 1) {
      clearQueue = true;
    }
    var msgObj = thys.messageQueue.shift();
    thys.setMessage(msgObj);
    if (clearQueue) {
      thys.messageQueue = [];
    }
  } else {
    thys.direction = thys.UP;
    window.clearInterval(thys.positionKeeperId);
    thys.positionKeeperId = null;
    thys.queueProcess();
  }
  thys.visible = visible;
  //thys.animateId = window.setInterval(thys.animateSlide, thys.TIME_STEP);
};

/**
 * Hide notification popup
 *
 * @param {Event} event
 */
UISlideAlert.prototype.hideNotification = function(event) {
  var thys = eXo.communication.chat.webui.UISlideAlert;
  event = event || window.event;
  if (event) {
    eXo.communication.chat.core.AdvancedDOMEvent.cancelEvent(event);
  }
  thys.setVisible(false);
};

/**
 * Play slide animation for notification popup
 */
UISlideAlert.prototype.animateSlide = function() {
  var thys = eXo.communication.chat.webui.UISlideAlert;
  var stopCondition = false;
  switch(thys.direction) {
    case thys.DOWN:
      thys.currentDistance += thys.DISTANCE_STEP;
      stopCondition = (thys.currentDistance >= thys.MAX_DISTANCE);
      break;
      
    case thys.UP:
      thys.currentDistance -= thys.DISTANCE_STEP;
      stopCondition = (thys.currentDistance <= 0);
      break;
      
    default:
      break;
  }
  if (stopCondition) {
    window.clearInterval(thys.animateId);
    thys.animateId = null;
    if (thys.direction == thys.DOWN) {
      thys.hideId = window.setTimeout(thys.hide, thys.LIVE_TIME);
      if (thys.positionKeeperId) {
        window.clearInterval(thys.positionKeeperId);
      }
      thys.positionKeeperId = window.setInterval(thys.positionKeeper, thys.TIME_STEP);
    }
    if (thys.direction == thys.UP){
      if (thys.positionKeeperId) {
        window.clearInterval(thys.positionKeeperId);
      }
      //thys.msgNotificationNode.style.top = '-1000px';
      thys.visible = false;
      thys.hideId = null;
      thys.queueProcess();
    }
    return;
  }
  //thys.msgNotificationNode.style.top = (thys.currentDistance + document.documentElement.scrollTop - thys.MAX_DISTANCE) + 'px';
};

eXo.communication.chat.webui.UISlideAlert = new UISlideAlert();
