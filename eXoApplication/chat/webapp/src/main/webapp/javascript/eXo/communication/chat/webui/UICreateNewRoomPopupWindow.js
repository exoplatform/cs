/**
 * @author Uoc Nguyen Ba
 */
function UICreateNewRoomPopupWindow() {
}

UICreateNewRoomPopupWindow.prototype.init = function(rootNode, UIMainChatWindow) {
  this.rootNode = rootNode;
  this.UIMainChatWindow = UIMainChatWindow;
  var fieldList = this.rootNode.getElementsByTagName('input');
  for (var i=0; i<fieldList.length; i++) {
    if (fieldList[i].name == 'roomName') {
      this.roomNameField = fieldList[i];
      continue;
    }
  }
};

UICreateNewRoomPopupWindow.prototype.setVisible = function(visible) {
  if (!this.UIMainChatWindow.userStatus ||
      this.UIMainChatWindow.userStatus == this.UIMainChatWindow.OFFLINE_STATUS) {
    return;
  }
  if (visible) {
    if (this.rootNode.style.display != 'block') {
      this.rootNode.style.display = 'block'; 
    }
    this.roomNameField.focus();
    this.UIPopupManager.focusEventFire(this);
  } else {
    if (this.rootNode.style.display != 'none') {
      this.rootNode.style.display = 'none'; 
    }
    this.roomNameField.value = '';
  }
};

UICreateNewRoomPopupWindow.prototype.createNewRoomAction = function() {
  var roomName = this.roomNameField.value;
  if (roomName.indexOf(' ') != -1 ||
      roomName == '') {
    window.alert('Room name is invalid.');
    this.roomNameField.focus();
    return;
  }
  this.setVisible(false);
  
  this.UIMainChatWindow.createRoomChat({id: (new Date()).getTime(), name:roomName});
};

eXo.communication.chat.webui.UICreateNewRoomPopupWindow = new UICreateNewRoomPopupWindow();
