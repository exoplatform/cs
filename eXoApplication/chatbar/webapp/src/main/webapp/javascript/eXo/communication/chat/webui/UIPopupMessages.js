function UIPopupMessages() {
}

UIPopupMessages.prototype.init = function(rootNode, UIMainChatWindow) {
  this.rootNode = rootNode;
  this.UIMainChatWindow = UIMainChatWindow;
  var DOMUtil = eXo.core.DOMUtil;
  var warningMessage = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', 'WarningMessage');
  this.warningMessageContentNode = DOMUtil.findFirstDescendantByClass(warningMessage, 'div', 'PopupMessage');
};

UIPopupMessages.prototype.warning = function(message) {
  this.warningMessageContentNode.innerHTML = message;
  this.setVisible(true);
};

UIPopupMessages.prototype.setVisible = function(visible) {
  if (!this.UIMainChatWindow.userStatus ||
    this.UIMainChatWindow.userStatus == this.UIMainChatWindow.OFFLINE_STATUS) {
	return;
  }
  if (visible) {
    if (this.rootNode.style.display != 'block') {
      this.rootNode.style.display = 'block'; 
    }
    this.UIPopupManager.focusEventFire(this);
  } else {
    if (this.rootNode.style.display != 'none') {
      this.rootNode.style.display = 'none'; 
    }
  }
};

eXo.communication.chatbar.webui.UIPopupMessages = new UIPopupMessages();