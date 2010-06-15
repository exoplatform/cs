/**
 * @author Uoc Nguyen
 *  email uoc.nguyen@exoplatform.com
 *
 *  This is an UI component use to manage UICreateNewRoomPopupWindow popup window
 */
function UICreateNewRoomPopupWindow() {
}

/**
* Extends from JSUIBean
*/
UICreateNewRoomPopupWindow.prototype = new eXo.communication.chat.webui.component.JSUIBean();

/**
 * Initializing method
 *
 * @param {HTMLElement} rootNode
 * @param {UIMainChatWindow} UIMainChatWindow
 */
UICreateNewRoomPopupWindow.prototype.init = function(rootNode, UIMainChatWindow) {
  this.id = 'UICreateNewRoomPopupWindow';
  this.rootNode = rootNode;
  this.UIMainChatWindow = UIMainChatWindow;
  var fieldList = this.rootNode.getElementsByTagName('input');
  for (var i=0; i<fieldList.length; i++) {
    if (fieldList[i].name == 'roomName') {
      this.roomNameField = fieldList[i];
      continue;
    }
  }
  this._callback();
  this._registerEventCallback(this._RELOAD_EVENT, this.onReload);
};

/**
* Use to reload UI states
*/
UICreateNewRoomPopupWindow.prototype.onReload = function(eventData) {
  var uiCreateNewRoomPopupWindow = eXo.communication.chat.webui.UICreateNewRoomPopupWindow;
  uiCreateNewRoomPopupWindow._isOnLoading = true;
  uiCreateNewRoomPopupWindow.setVisible(uiCreateNewRoomPopupWindow._isVisible());
  uiCreateNewRoomPopupWindow._isOnLoading = false;
};

/**
 * Use to make component visible or not
 *
 * @param {Boolean} visible
 */
UICreateNewRoomPopupWindow.prototype.setVisible = function(visible) {
  this._setOption('visible', visible);
  if (!visible || !this.UIMainChatWindow.userStatus ||
      this.UIMainChatWindow.userStatus == this.UIMainChatWindow.OFFLINE_STATUS) {
	  if (this.rootNode.style.display != 'none') {
	      this.rootNode.style.display = 'none'; 
	  }
	  this.roomNameField.value = '';
    return;
  }
  if (visible) {
    if (this.rootNode.style.display != 'block') {
      this.rootNode.style.display = 'block'; 
    }
    this.roomNameField.focus();
    this.UIPopupManager.focusEventFire(this);
  }
};

/**
 * Use to call service to create a new room
 */
UICreateNewRoomPopupWindow.prototype.createNewRoomAction = function() {
  var roomName = this.roomNameField.value;
  if (roomName.indexOf(' ') != -1 ||
      roomName == '') {
	  window.alert(this.UIMainChatWindow.ResourceBundle.chat_message_room_name_is_invalid);
    this.roomNameField.focus();
    return;
  }
  this.setVisible(false);
  this.UIMainChatWindow.newestRoomName = roomName;
  this.UIMainChatWindow.createRoomChat({id: (new Date()).getTime(), name:roomName});
};

eXo.communication.chat.webui.UICreateNewRoomPopupWindow = new UICreateNewRoomPopupWindow();
