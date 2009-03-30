/**
 * @author Uoc Nguyen
 */

/**
 *
 * @param {Object} tabId
 */
function UITabControl(tabId, isGroupChat, UIMainChatWindow) {
  this.MAX_TAB_TITLE_LEN = 25;
  this.MAX_MSG_TITLE_LEN = 30;
  this.MAX_USERNAME_LEN = 11;
  this.CSS_CLASS = {
    uiTab                 : 'ChatTab',
    tabName               : 'TabName',
    tabContactName        : 'ContactName',
    tabUnreadMessage      : 'UnreadMessage',
    closeTabButton        : 'CloseTabButton',
    uiTabContent          : 'ChatTabContent',
    uiGroupChatTabContent : 'GroupChatTabContent',
    chatSession           : 'ChatSession',
    messagesContainer     : 'OverflowMessagesContainer',
    messagesBox           : 'MessagesBox',
    userMessage           : 'UserMessage',
    guestMessage          : 'GuestMessage',
    sendFile              : 'SendFile',
    sendFileLabel         : 'LableSendFile',
    sendFileName          : 'NameFile',
    actionFile            : 'ActionFile',
    loadingIcon           : 'LoadingIcon',
    chatIconStatus        : 'IconStatus ChatIcon',
    chatTime              : 'ChatTime',
    chatTimeHistory       : 'ChatTimeHistory',
    chatTimeCenter        : 'CenterTime',
    contextChat           : 'ContextChat',
    messageBox            : 'MsgBox',
    sendMessageButton     : 'SendButton',
    nicksGroupChat        : 'OverflowGroupNick'
  };
  this.UIMainChatWindow = UIMainChatWindow;
  this.LocalTemplateEngine = eXo.communication.chat.core.LocalTemplateEngine;
  this.tabId = tabId;
  this.unreadMessageCnt = 0;
  this.isGroupChat = isGroupChat || false;
  this.roomConfigured = false;
  // This variable using for detect first message in room chat to make it active.
  this.activeMe = this.isGroupChat;
  this.initUI();
  if (this.isGroupChat) {
    this.buddyListControlObj = new eXo.communication.chat.webui.component.BuddyListControl(
                                this.buddyListNode, this.buddyItemActionCallback, this.UIMainChatWindow);
    this.buddyListControlObj.isGroupChat = this.isGroupChat;
    this.buddyListControlObj.MAX_USERNAME_LEN = this.MAX_USERNAME_LEN;
  }
  this.isFixedSize = false;
};

UITabControl.prototype.buddyItemActionCallback = function(event) {
  event = event || window.event;
  window.jsconsole.warn('menu action and click action processing');
};

// ===== Functions below available only for when isGroupChat is true
UITabControl.prototype.getRoomName = function() {
  if (this.isGroupChat) {
    return this.tabId.targetPerson;
  }
  return null;
};

UITabControl.prototype.updateRoster = function(roster) {
  if (this.isGroupChat &&
      roster &&
      roster.length &&
      roster.length > 0) {
    this.buddyListControlObj.xUpdate(roster);
  }
};

UITabControl.prototype.userLeftRoomEventFired = function(user) {
  user = user.substr(user.indexOf('/') + 1, user.length-1);
  this.writeMsg(this.UIMainChatWindow.UIChatWindow.SYSTEM_INFO, user + ' just left the room');
  user += '@' + this.UIMainChatWindow.serverInfo.mainServiceName;
  this.buddyListControlObj.removeBuddy(user);
};

UITabControl.prototype.userJoinRoomEventFired = function(user) {
  var userName = user.substr(user.indexOf('/') + 1, user.length-1);
  this.writeMsg(this.UIMainChatWindow.UIChatWindow.SYSTEM_INFO, userName + ' just joined the room');
  userName += '@' + this.UIMainChatWindow.serverInfo.mainServiceName;
  var buddyInfo = {
    presence           : {from: user,mode: null, status: null, type: 'available'},
    nickname           : user,
    groups             : [],
    subscriptionStatus : null,
    subscriptionType   : null,
    user               : userName
  };
  this.buddyListControlObj.addBuddy(buddyInfo);
};

UITabControl.prototype.updatePresence = function(presences) {
  if (!this.isGroupChat ||
      !presences ||
      presences.length ||
      presences.length <= 0) {
    return;
  }
  // Need to rebuild presences array to detect what presences is for this room.
  var newPresences = presences.length ? presences : [presences];
  for (var i=0; i<newPresences.length; i++) {
    if (newPresences[i] &&
        newPresences[i].from &&
        newPresences[i].from.indexOf(this.tabId.targetPerson) != 0) {
      newPresences[i] = newPresences[newPresences.length-1];
      newPresences.pop();
    }
  }
  if (newPresences.length > 0) {
    this.buddyListControlObj.update(newPresences);
  }
};

UITabControl.prototype.inviteToJoinRoom = function() {
  if (this.roomConfigured) {
    eXo.communication.chat.webui.UIAddContactPopupWindow.setVisible(true, this);
  }
};

UITabControl.prototype.contactUpdateFilter = function(contact) {
  var UIChatWindow = eXo.communication.chat.webui.UIChatWindow;
  var uiTabControlObj = UIChatWindow.getUITabControl(this.tabId);
  var currentContactList = uiTabControlObj.buddyListControlObj.buddyList || [];
  for (var contactId in currentContactList) {
    var contactInfo = currentContactList[contactId];
    if (!(contactInfo instanceof Object) ||
        !contactInfo.buddyInfo) {
      continue;
    }
    var userName = contactInfo.buddyInfo.user;
    var shortUserName = userName.substring(0, userName.indexOf('@'));
    if (shortUserName == contact['userName'] ||
        userName == contact['userName']) {
      contact.enabled4Add = false;
      break;
    }
  }
  return contact;
};

UITabControl.prototype.addContactActionCallback = function(contactList) {
  var UIMainChatWindow = eXo.communication.chat.webui.UIMainChatWindow;
  var roomName = this.tabId.targetPerson;
  roomName = roomName.substr(0, roomName.indexOf('@'));
  for (var i=0; i<contactList.length; i++) {
    UIMainChatWindow.jabberInviteJoinRoom(contactList[i], roomName);
  }
};

UITabControl.prototype.roomInfoEventFired = function(roomInfoData) {
  if (roomInfoData &&
      roomInfoData.occupants) {
    this.updateRoster(roomInfoData.occupants);
  }
};
// =/= 

// ===== FileExchange 
UITabControl.prototype.fileTransportRequestEventFire = function(FTReqEvent) {
  var DOMUtil = eXo.core.DOMUtil;
  var fileSize = this.getFuzzyFileSize(FTReqEvent.fileSize);
  // Create file transport node
  var fileTransportNode = this.LocalTemplateEngine.getTemplateByClassName(this.CSS_CLASS.sendFile);
  var labelNode = DOMUtil.findFirstDescendantByClass(fileTransportNode, 'div', this.CSS_CLASS.sendFileLabel);
  labelNode.innerHTML = this.tabId.targetPerson + ' want to send you ' + FTReqEvent.filename;
  var fileNameNode = DOMUtil.findFirstDescendantByClass(fileTransportNode, 'div', this.CSS_CLASS.sendFileName);
  fileNameNode.innerHTML = FTReqEvent.filename + ' (' + fileSize.size + ' ' + fileSize.unit + ')<br>'
                            + (FTReqEvent.description || '') ;
  fileTransportNode.uuid = FTReqEvent.uuid;
  // -/-
  this.messagesBoxNode.appendChild(fileTransportNode);
  this.fileTransportRequestIncoming = true;
  this.scrollMessageBox();

  if (FTReqEvent.responseTimeout) {
    this.fileEventTimeoutId = window.setTimeout(this.fileEventTimeout, FTReqEvent.responseTimeout, fileTransportNode, this);
  }
};

UITabControl.prototype.fileTransportResponseEventFire = function(FTResEvent) {
  if (FTResEvent.status == 'complete') {
    eXo.communication.chat.webui.UIChatWindow.insertSystemMsg('Sent file: [' + FTResEvent.fileName + '] completed.', this.tabId);
  } else {
    eXo.communication.chat.webui.UIChatWindow.insertSystemMsg('Sent file: [' + FTResEvent.fileName + '] denied.', this.tabId);
  }
};

UITabControl.prototype.getFuzzyFileSize = function(size) {
  var units = ['bytes', 'kb', 'mb', 'gb', 'tb', 'pb'];
  var cnt = 0;
  var unit = units[cnt];
  while (size >= 1024) {
    size /= 1024;
    unit = units[++cnt];
  }
  return {size: size, unit: unit};
};

UITabControl.prototype.downloadCompleteCallBack = function(uuid) {
  var DOMUtil = eXo.core.DOMUtil;
  var fileExchangeList = DOMUtil.findDescendantsByClass(this.messageContainerNode, 'div', this.CSS_CLASS.sendFile);
  for (var i=0; i < fileExchangeList.length; i++) {
    if (fileExchangeList[i].uuid == uuid) {
      var loadingIcon = DOMUtil.findFirstDescendantByClass(fileExchangeList[i], 'div', this.CSS_CLASS.loadingIcon);
      loadingIcon.style.display = 'none';
    }
  };
};

UITabControl.prototype.acceptFileExchange = function(acceptNode) {
  var DOMUtil = eXo.core.DOMUtil;
  var fileTransportNode = DOMUtil.findAncestorByClass(acceptNode, this.CSS_CLASS.sendFile);
  var uuid = fileTransportNode.uuid;
  var downloadUrl = this.UIMainChatWindow.acceptSendFile(uuid);
  this.removeActionFileButtons(fileTransportNode);
  var loadingIcon = DOMUtil.findFirstDescendantByClass(fileTransportNode, 'div', this.CSS_CLASS.loadingIcon);
  loadingIcon.style.display = 'block';
  var uploadIframe = this.UIMainChatWindow.UIChatWindow.uploadIframe;
  uploadIframe.onload = function () {
    this.callBackObj.downloadCompleteCallBack(uuid);
  };
  uploadIframe.callBackObj = this;
  uploadIframe.src = downloadUrl;
};

UITabControl.prototype.fileEventTimeout = function(fileTransportNode, uiTabControlObj) {
  uiTabControlObj.removeActionFileButtons(fileTransportNode, uiTabControlObj);
  uiTabControlObj.writeMsg(eXo.communication.chat.webui.UIChatWindow.SYSTEM_INFO, 'The file exchange has been time out and removed by server.')
};

UITabControl.prototype.denieFileExchange = function(denieNode) {
  var DOMUtil = eXo.core.DOMUtil;
  var fileTransportNode = DOMUtil.findAncestorByClass(denieNode, this.CSS_CLASS.sendFile);
  this.UIMainChatWindow.denieSendFile(fileTransportNode.uuid);
  this.removeActionFileButtons(fileTransportNode, this);
};

UITabControl.prototype.removeActionFileButtons = function(fileTransportNode, that) {
  var DOMUtil = eXo.core.DOMUtil;
  var actionFileList = DOMUtil.findDescendantsByClass(fileTransportNode, 'div', this.CSS_CLASS.actionFile);
  for (var i=0; i < actionFileList.length; i++) {
    DOMUtil.removeElement(actionFileList[i]);
  };
  if (that && this.fileEventTimeoutId) {
    try {
      window.clearTimeout(this.fileEventTimeoutId);
      this.fileEventTimeoutId = null;
    } catch (e) {}
  }
};
// =/=

UITabControl.prototype.initUI = function(buddyId) {
  var DOMUtil = eXo.core.DOMUtil;
  this.uiTabNode = this.LocalTemplateEngine.getTemplateByClassName(this.CSS_CLASS.uiTab);
  // Customize new UITab node
  this.tabNameNode = DOMUtil.findFirstDescendantByClass(this.uiTabNode, 'div', this.CSS_CLASS.tabName);
  var tabContactNameNode = DOMUtil.findFirstDescendantByClass(this.tabNameNode, 'span', this.CSS_CLASS.tabContactName);
  if (this.tabId.targetPerson.length > this.MAX_TAB_TITLE_LEN) {
    tabContactNameNode.innerHTML = this.tabId.targetPerson.substr(0, this.MAX_TAB_TITLE_LEN - 3) + '...';
  } else {
    tabContactNameNode.innerHTML = this.tabId.targetPerson;
  }
  this.tabNameNode.setAttribute('title', this.tabId.targetPerson);
  this.tabNameNode.tabcontentid = this.tabId.id;
  this.tabNameNode.className = this.tabNameNode.className;
  this.tabNameNode.onclick = this.focusTabWrapper;
  this.uiTabNode.style.display = 'block';

  var closeTabButtonNode = DOMUtil.findFirstDescendantByClass(this.uiTabNode, 'div', this.CSS_CLASS.closeTabButton);
  closeTabButtonNode.onclick = this.closeTabWrapper;

  var tabContentCSSClass = this.CSS_CLASS.uiTabContent;
  if (this.isGroupChat) {
    tabContentCSSClass = this.CSS_CLASS.uiGroupChatTabContent;
  }
  this.uiTabContentNode = this.LocalTemplateEngine.getTemplateByClassName(tabContentCSSClass);
  // Customize new UITabContent node
  var chatSessionNode = DOMUtil.findFirstDescendantByClass(this.uiTabContentNode, 'div', this.CSS_CLASS.chatSession);
  chatSessionNode.className = chatSessionNode.className + ' ' + this.tabId.id;
  var msgBoxNode = DOMUtil.findFirstDescendantByClass(this.uiTabContentNode, 'textarea', this.CSS_CLASS.messageBox);
  msgBoxNode.onkeypress = this.msgBoxKBHandler;
  this.msgTypingBox = msgBoxNode;
  var sendButtonNode = DOMUtil.findFirstDescendantByClass(this.uiTabContentNode, 'div', this.CSS_CLASS.sendMessageButton);
  sendButtonNode.onclick = this.sendMessageWrapper;
  this.uiTabContentNode.style.display = 'block';
  this.messageContainerNode = DOMUtil.findFirstDescendantByClass(this.uiTabContentNode, 'div', this.CSS_CLASS.messagesContainer);
  this.messagesBoxNode = DOMUtil.findFirstDescendantByClass(this.uiTabContentNode, 'div', this.CSS_CLASS.messagesBox);
  
  this.buddyListNode = DOMUtil.findFirstDescendantByClass(this.uiTabContentNode, 'div', this.CSS_CLASS.nicksGroupChat);

  this.uiTabContentNode.startTime = (new Date()).getTime();

  // TODO: Remove room option if current user is not owner or moderatored of the room.
};

UITabControl.prototype.updateHistoryMessage = function(messageList) {
  this.messagesBoxNode.innerHTML = '';
  // Ignore detect dupplicated message
  this.updatingHistoryMessage = true;
  this.lastBuddyId = false;
  for (var i=0; i<messageList.length; i++) {
    var message = messageList[i];
    this.writeMsg(message.from, message, false);
  }
  this.lastBuddyId = false;
  this.updatingHistoryMessage = false;
};

UITabControl.prototype.updateUnreadMessage = function() {
  var DOMUtil = eXo.core.DOMUtil;
  var myParent = this.UIMainChatWindow.UIChatWindow;
  var tabUnreadMessageNode = DOMUtil.findFirstDescendantByClass(this.tabNameNode, 'span', this.CSS_CLASS.tabUnreadMessage);
  if (this.visible &&
      myParent._isVisible()) {
    tabUnreadMessageNode.innerHTML = '';
    this.unreadMessageCnt = 0;
  } else if (this.unreadMessageCnt > 0) {
    tabUnreadMessageNode.innerHTML = '*[' + this.unreadMessageCnt + ']&nbsp;';
  }
  myParent.updateUnreadMessage();
};

UITabControl.prototype.writeMsg = function(buddyId ,msgObj) {
  var myParent = this.UIMainChatWindow.UIChatWindow;
  if (this.visible &&
      myParent._isVisible()) {
    this.unreadMessageCnt = 0;
  } else {
    this.unreadMessageCnt ++;
  }
  this.updateUnreadMessage();
  var buddyIdTmp = buddyId;
  // Detect dupplicated message
  if (this.isGroupChat &&
      !this.updatingHistoryMessage &&
      buddyId.indexOf('/') != -1 &&
      buddyId.substr(buddyId.lastIndexOf('/') + 1, buddyId.length) == this.tabId.owner) {
    return;
  }
  var DOMUtil = eXo.core.DOMUtil;
  var msgNode = this.lastMsgNode;
  if (buddyId.indexOf('/') != -1) {
    if (this.isGroupChat) {
      buddyIdTmp = buddyId.substr(buddyId.lastIndexOf('/') + 1, buddyId.length);
      buddyId = buddyIdTmp;
    } else {
      buddyIdTmp = buddyId.substr(0, buddyId.indexOf('/'));
    }
  }
  if (buddyIdTmp.indexOf('@') != -1) {
    buddyIdTmp = buddyIdTmp.substr(0, buddyIdTmp.indexOf('@'));
  }
  if (this.lastBuddyId != buddyIdTmp ||
      this.fileTransportRequestIncoming) {
    msgNode = this.createNewMsgNode(buddyIdTmp, msgObj);
    if (this.fileTransportRequestIncoming) {
      this.fileTransportRequestIncoming = false;
    }
  }
  this.messagesBoxNode.appendChild(msgNode);
  var contextChatNode = DOMUtil.findFirstDescendantByClass(msgNode, 'div', this.CSS_CLASS.contextChat);
  var msgTmpNode = document.createElement('div');
  var msgContent = msgObj['body'] || msgObj;

  if (!this.roomConfigured &&
      msgContent.toLowerCase().indexOf('this room is now unlocked.') != -1) {
    this.roomConfigured = true;
  }

  msgTmpNode.innerHTML = this.messageFilter(msgContent);

  contextChatNode.appendChild(msgTmpNode);

  var max = this.messageContainerNode.scrollHeight - this.messageContainerNode.offsetHeight;
  this.scrollMessageBox();
  this.lastBuddyId = buddyIdTmp;
  this.lastMsgNode = msgNode;
  if (this.activeMe) {
    this.activeMe = !this.activeMe;
    this.UIMainChatWindow.UIChatWindow.focusTab(this.tabId.id);
  }
};

UITabControl.prototype.messageFilter = function(msg) {
  // Replace all special charactors by html entities.
  msg = eXo.core.HTMLUtil.entitiesEncode(msg);
  // Treat all url format as link.
  var urlRegex = /(http|https|ftp|mail|news|yahoo|skype|msn|apt):\/\/([\w\d_\.]+@)?((w{3})\.)?[\w\d_\-\.]+\.\w{1,3}(\/[^\s]+)?/gi;
  msg = msg.replace(urlRegex, this.makeLink);
  // Treat all \n as <br> 
  msg = msg.replace(/(\n|\\n)/g, '<br>');
  return msg;
};

UITabControl.prototype.makeLink = function(m) {
  return ('<a href="' + m + '" title="Go to ' + m + '" target="_blank">' + m + '</a>');
};

UITabControl.prototype.scrollMessageBox = function() {
  this.messageContainerNode.scrollTop = this.messageContainerNode.scrollHeight - this.messageContainerNode.offsetHeight;
};

UITabControl.prototype.createNewMsgNode = function(buddyId, msgObj) {
  var DOMUtil = eXo.core.DOMUtil;
  var messageNode = false;
  if (buddyId == this.tabId.owner) {
    messageNode = this.LocalTemplateEngine.getTemplateByClassName(this.CSS_CLASS.userMessage);
  } else {
    messageNode = this.LocalTemplateEngine.getTemplateByClassName(this.CSS_CLASS.guestMessage);
  }
  var msgTitleNode = DOMUtil.findFirstDescendantByClass(messageNode, 'div', this.CSS_CLASS.chatIconStatus);
  if (buddyId.length > this.MAX_MSG_TITLE_LEN) {
    msgTitleNode.innerHTML = buddyId.substr(0, this.MAX_MSG_TITLE_LEN - 3) + '...';
  } else {
    msgTitleNode.innerHTML = buddyId;
  }
  var chatTimeNode = DOMUtil.findFirstDescendantByClass(messageNode, 'div', this.CSS_CLASS.chatTimeCenter);
  if (this.updatingHistoryMessage ||
      msgObj['dateSend']) {
    var chatTimeBoxNode = DOMUtil.findAncestorByClass(chatTimeNode, this.CSS_CLASS.chatTime);
    if (chatTimeBoxNode) {
      chatTimeBoxNode.className += ' ' + this.CSS_CLASS.chatTimeHistory;
      chatTimeNode = DOMUtil.findFirstDescendantByClass(messageNode, 'div', this.CSS_CLASS.chatTimeCenter);
    }
  }
  var timeStamp = msgObj['dateSend'];
  if (!timeStamp) {
    timeStamp = new Date();
    //timeStamp = timeStamp.getHours() + ':' + timeStamp.getMinutes() + ':' + timeStamp.getSeconds();
    timeStamp = timeStamp.format('HH:MM:ss');
  } else {
    timeStamp = timeStamp.replace('ICT', ''); // Remove ICT if exist
    var regexObj = /GMT(\+|\-)?\d{2}:\d{2}/g;
    timeStamp = timeStamp.replace(regexObj, function(m) {return m.replace(/:\d{2}/, '');});
    timeStamp = new Date(timeStamp);
    timeStamp = timeStamp.format('dd/mm/yyyy - HH:MM:ss');
    //timeStamp = timeStamp.getDate() + '/' + (timeStamp.getMonth() + 1) + '/' + timeStamp.getFullYear() +
                //' - ' + 
                //timeStamp.getHours() + ':' + (timeStamp.getMinutes() + 1) + ':' + timeStamp.getSeconds();
  }
  chatTimeNode.innerHTML = timeStamp;
    
  return messageNode;
};

/**
 * This method will do: become keyboard handler for input text box
 * @param {Event} event
 */
UITabControl.prototype.msgBoxKBHandler = function(event) {
  event = event ? event : window.event;
  switch (event.keyCode) {
    case 13:
      if (event.shiftKey || event.ctrlKey) {
        eXo.communication.chat.webui.UIChatWindow.insertToMessageInputBox('\n');
      } else {
        eXo.communication.chat.webui.UIChatWindow.sendMsgFromActiveTab();
      }
      return false;
      break;
  }
  return true;
};

UITabControl.prototype.insertToMessageBox = function(txt) {
  this.msgTypingBox.value = this.msgTypingBox.value + txt;
};

/**
 *
 * @param {Object} event
 */
UITabControl.prototype.sendMessageWrapper = function(event) {
  eXo.communication.chat.webui.UIChatWindow.sendMsgFromActiveTab();
};

UITabControl.prototype.sendMessage = function() {
  var msgStr = this.msgTypingBox.value;
  this.msgTypingBox.value = '';
  this.writeMsg(this.tabId.owner, msgStr);
};

UITabControl.prototype.focusTabWrapper = function() {
  return eXo.communication.chat.webui.UIChatWindow.focusTab(this.tabcontentid, true);
};

UITabControl.prototype.closeTabWrapper = function() {
  var tabNameNode = eXo.core.DOMUtil.findPreviousElementByTagName(this, 'div');
  if (tabNameNode) {
    return eXo.communication.chat.webui.UIChatWindow.closeTab(tabNameNode.tabcontentid);
  }
};

UITabControl.prototype.setVisible = function(visible) {
  if (!this.uiTabContentNode ||
      !this.uiTabNode) {
    return;
  }
  var DOMUtil = eXo.core.DOMUtil;
  this.visible = visible;
  if (visible) {
    this.updateUnreadMessage();
    var normalTabNode = DOMUtil.findFirstDescendantByClass(this.uiTabNode, 'div', 'NormalTab');
    if (normalTabNode) {
      normalTabNode.className = 'SelectedTab';
    }
    if (this.uiTabContentNode.style.display != 'block') {
      this.uiTabContentNode.style.display = 'block';
    }
    /*
    if (!this.isFixedSize) {
      var resizableObjectList = DOMUtil.findDescendantsByClass(this.uiTabContentNode, 'div', 'UIResizableBlock');
      for (var i=0; i<resizableObjectList.length; i++) {
        resizableObjectList[i].style.width = resizableObjectList[i].offsetWidth + 'px';
        resizableObjectList[i].style.height = resizableObjectList[i].offsetHeight + 'px';
      }
      // Fix for room chat tab
      if (this.isGroupChat) {
        var totalWidth = DOMUtil.findFirstDescendantByClass(this.uiTabContentNode, 'div', 'ChatContainer').offsetWidth;
        var leftPaneList = DOMUtil.findFirstDescendantByClass(this.uiTabContentNode, 'div', 'MessagesContainer');
        var leftPaneWidth = DOMUtil.getStyle(leftPaneList, 'width', true);
        leftPaneList = DOMUtil.findDescendantsByClass(this.uiTabContentNode, 'div', 'LeftPane');
        var rightPaneList = DOMUtil.findFirstDescendantByClass(this.uiTabContentNode, 'div', 'RightPane');
        var rightPaneWidth = DOMUtil.getStyle(rightPaneList, 'width', true);
        rightPaneList = DOMUtil.findDescendantsByClass(this.uiTabContentNode, 'div', 'RightPane');

        var delta = ((totalWidth - (leftPaneWidth + rightPaneWidth)) / 2) - 10;

        for (var i=0; i<leftPaneList.length; i++) {
          leftPaneList[i].style.width = Math.abs(DOMUtil.getStyle(leftPaneList[i], 'width', true) + delta) + 'px';
        }
        for (var i=0; i<rightPaneList.length; i++) {
          rightPaneList[i].style.width = Math.abs(DOMUtil.getStyle(rightPaneList[i], 'width', true) + delta) + 'px';
        }
      }

      this.isFixedSize = true;
    }
    */
    this.scrollMessageBox();
  } else {
    var selectedTabNode = DOMUtil.findFirstDescendantByClass(this.uiTabNode, 'div', 'SelectedTab');
    if (selectedTabNode) {
      selectedTabNode.className = 'NormalTab';
    }
    if (this.uiTabContentNode.style.display != 'none') {
      this.uiTabContentNode.style.display = 'none';
    }
  }
};

/**
 * 
 */
function UIChatWindow() {
//  this.id = 'eXo.communication.chat.webui.UIChatWindow';
  this.id = 'UIChatWindow';
  this.CSS_CLASS = {
    tabsContainer       : 'TabsContainer',
    tabContentContainer : 'UITabContentContainer',
    resizeArea          : 'ResizeArea',
    uiTab               : 'ChatTab',
    miniBoxChat         : 'MiniBoxChat'
  };
  this.THIS_WEEK_MESSAGE = 7;
  this.LAST_30_DAY_MESSAGE = 30;
  this.BEGINNING_MESSAGE = -1;
  this.CURRENT_CONVERSATION_MESSAGE = 0;
  this.SYSTEM_INFO = 'System: ';
  this.UIMainChatWindow = eXo.communication.chat.webui.UIMainChatWindow;
  this.LocalTemplateEngine = eXo.communication.chat.core.LocalTemplateEngine;
  this.scrollMgr = false;
  this.isFixedTabContainerHeight = false;
  this.totalTab = 0;
  this.targetUploadIframe = 'chatFileExchangeIframe';
  this.LR_COOKIE_SESSION_START = 'LR_COOKIE_SESSION_START';
  this.MINI_BOX_CHAT_ANIMATION_STEP = 1*1000;
  this.miniBoxChatAnimationId = null;
  eXo.core.Browser.setCookie(this.LR_COOKIE_SESSION_START, (new Date()).getTime());
}

UIChatWindow.prototype = new eXo.communication.chat.webui.component.JSUIBean();

UIChatWindow.prototype.init = function(rootNode, UIMainChatWindow) {
  this.rootNode = rootNode;
  this.UIMainChatWindow = UIMainChatWindow;
  var DOMUtil = eXo.core.DOMUtil;
  this.miniBoxChatNode = DOMUtil.findFirstDescendantByClass(this.UIMainChatWindow.chatWindowsContainerNode, 'div', this.CSS_CLASS.miniBoxChat);
  this.tabsContainerNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', this.CSS_CLASS.tabsContainer);
  this.uiTabContentContainerNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', this.CSS_CLASS.tabContentContainer);
  // Register resizearea to for resizeable window.
  var resizeArea = DOMUtil.findFirstDescendantByClass(this.rootNode, "div", "ResizeArea") ;
  this.UIMainChatWindow.UIChatResize.register(resizeArea, this.resizeCallback, true);
  with (resizeArea.style) {
    display = 'block';
    width   = '18px';
    height  = '18px';
  };
  this.tabsContainerNode.style.overflow = 'hidden';
  this.fileChooserNode = false;
  this.initFileExchange();
  this.initSession();
  this._callback();
  this.registerEventCallback();
  eXo.communication.chat.core.AdvancedDOMEvent.addEventListener(this.rootNode, 'mousemove', this.firstCheck, false);
};

UIChatWindow.prototype.firstCheck = function() {
  var that = eXo.communication.chat.webui.UIChatWindow;
  that.checkScroll(true);
  eXo.communication.chat.core.AdvancedDOMEvent.removeEventListener(that.rootNode, 'mousemove', that.firstCheck);
};

UIChatWindow.prototype.resizeCallback = function(delta) {
  var that = eXo.communication.chat.webui.UIChatWindow;
  that.checkScroll(true);
};

UIChatWindow.prototype.registerEventCallback = function() {
  this._registerEventCallback(this._RELOAD_EVENT, this.onReload);
};

UIChatWindow.prototype.onReload = function(eventData) {
  var that = eXo.communication.chat.webui.UIChatWindow;
  that._isOnLoading = true;
  var visible = that._isVisible();
  that.initSession();
  var tabList = that._getOption('tabs');
  if (tabList) {
    for ( var i = 0; i < tabList.length; i++) {
      var tab = tabList[i];
      if (tab.targetPerson) {
        that.createNewTab(tab.targetPerson, tab.isGroupChat);
      }
    }
  }
  that._setVisible(visible);
  that.checkScroll(true);
  if (tabList && tabList.length > 0) {
    window.jsconsole.warn('Focus tab: ' + that._getOption('activeTabId'));
    var activeTabId = that._getOption('activeTabId');
    if (!activeTabId ||
        !that.tabControlList[that.getTabId(activeTabId)]) {
      activeTabId = that.getTabId(tabList[tabList.length - 1].targetPerson).id;
    }
    if (that.rootNode.offsetHeight > 0) {
      var focusTabIndex = that.focusTab(activeTabId, true);
      window.setTimeout(function() {
        that.autoScroll(focusTabIndex);
        eXo.communication.chat.core.AdvancedDOMEvent.removeEventListener(that.rootNode, 'mousemove', that.firstCheck);
      }, 100);
      that.updateTabList();
    } else {
      that.focusTab(activeTabId, true);
    }
  }
  that._isOnLoading = false;
};

UIChatWindow.prototype.initSession = function() {
  this.destroySession();
  this.tabControlList = this.tabControlList || {};
  this.owner = this.UIMainChatWindow.userNames[eXo.communication.chat.core.XMPPCommunicator.TRANSPORT_XMPP]; 
  this.totalTab = 0;
  this.loadScroll();
};

UIChatWindow.prototype.closeAllWindow = function() {
  for (var item in this.tabControlList) {
    if (this.tabControlList[item] instanceof UITabControl) {
      this.closeTab(item);
    }
  }
  this.scrollMgr = null;
  this.loadScroll();
  this.setVisible(false);
};

UIChatWindow.prototype.destroySession = function() {
  for (var item in this.tabControlList) {
    if (this.tabControlList[item] instanceof UITabControl) {
      this.closeTab(this.tabControlList[item].tabId.id);
    }
  }
  this.scrollMgr = null;
  this.setVisible(false);
  this.miniBoxChatNode.style.display = 'none';
  this.totalTab = 0;
};

UIChatWindow.prototype.destroy = function() {};

UIChatWindow.prototype.createNewTab = function(targetPerson, isGroupChat) {
  var tabId = this.getTabId(targetPerson);
  var uiTabControlObj = this.getUITabControl(tabId, isGroupChat, true);
  this.setVisible(true);
  if (!this._isOnLoading) {
    this.checkScroll();
    this.focusTab(tabId.id, true);
    this.updateTabList();
  }
  return uiTabControlObj;
};

// Using for UIStateService
UIChatWindow.prototype.updateTabList = function() {
  if (this._isOnLoading) {
    return;
  }
  var tabList = [];
  for ( var item in this.tabControlList) {
    var uiTabControlObj = this.tabControlList[item];
    if (uiTabControlObj &&
        uiTabControlObj.tabId &&
        !uiTabControlObj.isGroupChat) {
      var tab = {};
      tab.targetPerson = uiTabControlObj.tabId.targetPerson;
      tab.isGroupChat = uiTabControlObj.isGroupChat;
      tabList.push(tab);
    }
  }
  this._setOption('tabs', tabList);
};

// Chat room
UIChatWindow.prototype.userLeaveRoom = function(userName, roomName) {
  var uiTabControl = this.tabControlList[this.getTabId(roomName).id];
  if (uiTabControl &&
      uiTabControl.isGroupChat &&
      uiTabControl.tabId.targetPerson.indexOf(roomName) != -1) {
    window.jsconsole.warn('Remove buddy: ' + userName + ' @ ' + roomName);
    uiTabControl.buddyListControlObj.removeBuddy(userName);
  }
};

UIChatWindow.prototype.updateRoster = function(roster) {
  window.jsconsole.warn('update roster for room chat');
  for (var item in this.tabControlList) {
    var uiTabControl = this.tabControlList[item];
    if (uiTabControl instanceof UITabControl &&
        uiTabControl.isGroupChat) {
      uiTabControl.updateRoster(roster);
    }
  }
};

UIChatWindow.prototype.userLeftRoomEventFired = function(user, room) {
  var uiTabControlObj = this.getUITabControl(this.getTabId(room), true, false);
  if (uiTabControlObj) {
    uiTabControlObj.userLeftRoomEventFired(user);
  }
};

UIChatWindow.prototype.userJoinRoomEventFired = function(user, room) {
  var uiTabControlObj = this.getUITabControl(this.getTabId(room), true, false);
  if (uiTabControlObj) {
    uiTabControlObj.userJoinRoomEventFired(user);
  }
};

UIChatWindow.prototype.updatePresence = function(presences) {
  window.jsconsole.warn('update presences for room chat');
  for (var item in this.tabControlList) {
    var uiTabControl = this.tabControlList[item];
    if (uiTabControl instanceof UITabControl &&
        uiTabControl.isGroupChat) {
      uiTabControl.updatePresence(presences);
    }
  }
};
//

/**
 *
 * @param {String} targetPerson
 * @param {String} msg
 */
UIChatWindow.prototype.displayMessage = function(targetPerson, msg, isGroupChat) {
  var tabId = this.getTabId(targetPerson);
  var uiTabControlObj = this.getUITabControl(tabId, isGroupChat, true);
  if (uiTabControlObj) {
    if (msg) {
      uiTabControlObj.writeMsg(targetPerson, msg);
    }
    if (!msg || !this.activeTabId) {
      this.focusTab(tabId.id, true);
    }
  }
  this.updateUnreadMessage();
};

UIChatWindow.prototype.updateUnreadMessage = function() {
  var DOMUtil = eXo.core.DOMUtil;
  var unreadMessageNode = null;
  var unreadMessageCnt = 0;
  for (var item in this.tabControlList) {
    if (this.tabControlList[item] instanceof UITabControl) {
      unreadMessageCnt += this.tabControlList[item].unreadMessageCnt;
    }
  }
  if (this._isVisible()) {
    var portletNameNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', 'PortletName');
    unreadMessageNode = DOMUtil.findFirstDescendantByClass(portletNameNode, 'span', 'UnreadMessage');
    if (this.miniBoxChatAnimationId) {
      window.clearInterval(this.miniBoxChatAnimationId);
      this.miniBoxChatAnimationId = null;
      this.blinkMiniBoxChat('NormalMiniBoxChat');
    }
  } else {
    unreadMessageNode = DOMUtil.findFirstDescendantByClass(this.miniBoxChatNode, 'span', 'UnreadMessage');
    if (unreadMessageCnt > 0 &&
        !this.miniBoxChatAnimationId) {
      this.miniBoxChatAnimationId = window.setInterval(this.blinkMiniBoxChat, this.MINI_BOX_CHAT_ANIMATION_STEP);
    }
  }
  if (unreadMessageNode) {
    if (unreadMessageCnt == 0) {
      unreadMessageNode.innerHTML = ''
    } else {
      unreadMessageNode.innerHTML = '*[' + unreadMessageCnt + ']&nbsp;';
    }
  }
  this.checkScroll();
};

UIChatWindow.prototype.blinkMiniBoxChat = function(styleClass) {
  var that = eXo.communication.chat.webui.UIChatWindow;
  var DOMUtil = eXo.core.DOMUtil;
  styleClass = ((styleClass + '').indexOf('MiniBoxChat') == -1) ? '' : styleClass;
  var styleNode = DOMUtil.findFirstDescendantByClass(that.miniBoxChatNode, 'div', 'NormalMiniBoxChat');
  if (styleNode) {
    styleClass = styleClass || 'HightLightMiniBoxChat';
  } else {
    styleNode = DOMUtil.findFirstDescendantByClass(that.miniBoxChatNode, 'div', 'HightLightMiniBoxChat');
    styleClass = styleClass || 'NormalMiniBoxChat';
  }
  styleNode.className = styleClass;
};

UIChatWindow.prototype.insertSystemMsg = function(msg, tabId) {
  var uiTabControlObj = false;
  if (tabId) {
    uiTabControlObj = this.getUITabControl(tabId);
  } else {
    uiTabControlObj = this.getActiveTabControl();
  }
  if (uiTabControlObj) {
    uiTabControlObj.writeMsg(this.SYSTEM_INFO,msg);
  }
};

/**
 *
 * @param {String} targetPerson
 */
UIChatWindow.prototype.getTabId = function(targetPerson) {
  if (targetPerson.indexOf('/') != -1) {
    targetPerson = targetPerson.substring(0, targetPerson.indexOf('/'));
  }
  var owner = this.owner;
  var targetPerson = targetPerson;
  return {owner : owner, targetPerson : targetPerson, id : ((owner + '_' + targetPerson).toLowerCase())};
};

/**
 *
 * @param {Object} tabId
 *
 * @return {UITabControl}
 */
UIChatWindow.prototype.getUITabControl = function(tabId, isGroupChat, forceCreate) {
  if (!tabId.owner) {
    window.jsconsole.error('Get UITabControl object with invalid tabId');
    return null;
  }
  var uiTabControlObj = this.tabControlList[tabId.id];

  if (!(uiTabControlObj instanceof UITabControl) &&
      forceCreate) {
    uiTabControlObj = this.createUITabControl(tabId, isGroupChat);
  }
  return uiTabControlObj;
};

UIChatWindow.prototype.createUITabControl = function(tabId, isGroupChat) {
  window.jsconsole.warn('=========== Create new tab with tabId=' + tabId.id);
  if (isGroupChat) {
    this.UIMainChatWindow.jabberGetRoomInfo(tabId.targetPerson);
  }
  var uiTabControlObj = new UITabControl(tabId, isGroupChat, this.UIMainChatWindow);
  uiTabControlObj.setVisible(false);
  this.tabsContainerNode.appendChild(uiTabControlObj.uiTabNode);
  this.uiTabContentContainerNode.appendChild(uiTabControlObj.uiTabContentNode);
  this.tabControlList[tabId.id] = uiTabControlObj;
  this.totalTab ++;
  return uiTabControlObj;
};

/**
 *
 * @param {String} id
 */
UIChatWindow.prototype.focusTab = function(id, isFocusTextbox) {
  if (!id) {
    window.jsconsole.warn('Wrong tab id, skip focus tab: ' + id);
    return;
  }
  window.jsconsole.warn('Focus to tab with id=' + id);
  this.setVisible(true);
  var targetTab = false;
  var targetTabIndex = 0;
  var cnt = 0;
  for (var item in this.tabControlList) {
    if (this.tabControlList[item] instanceof UITabControl) {
      if (item != id) {
        this.tabControlList[item].setVisible(false);
      } else {
        targetTab = this.tabControlList[item];
        targetTabIndex = cnt;
      }
    }
    cnt ++;
  }
  if (targetTab) {
    targetTab.setVisible(true);
    this.activeTabId = targetTab.tabId.id;
    if (isFocusTextbox) {
      // Exception occur when using ie6, it is not allow to move focus to hidden element.
      try {
        targetTab.msgTypingBox.focus();
      } catch (e) {}
    }
    this._setOption('activeTabId', this.activeTabId);
  }
  this.autoScroll(targetTabIndex);
  return targetTabIndex;
};

UIChatWindow.prototype.getActiveTabControl = function() {
  return (this.tabControlList[this.activeTabId]);
};

/**
 *
 * @param {String} id
 */
UIChatWindow.prototype.closeTab = function(id) {
  var lastTabId = false;
  var tabRemoved = false;
  var cnt = 0;
  for (var item in this.tabControlList) {
    var uiTabObj = this.tabControlList[item];
    if (uiTabObj instanceof UITabControl) {
      if (item == id) {
        eXo.core.DOMUtil.removeElement(uiTabObj.uiTabNode);
        eXo.core.DOMUtil.removeElement(uiTabObj.uiTabContentNode);
        this.tabControlList[id] = null;
        tabRemoved = true;
        this.totalTab --;
        continue;
      }
      if (cnt > 0 &&
          lastTabId &&
          tabRemoved) {
        break;
      }
      lastTabId = item;
      cnt ++;
    }
  }
  this.activeTabId = false;
  this.checkScroll(true);
  // Focus previousTab if exist
  if (this.tabControlList[lastTabId]) {
    this.focusTab(lastTabId);
  } else {
    // Hide Tabed Window when no tab remain
    this.setVisible(false);
  }
  this.updateTabList();
};

UIChatWindow.prototype.setVisible = function(visible, event, requestCancelEvent) {
  if (!this.rootNode ||
      (this.totalTab <= 0 && visible)) {
    return;
  }
  if (requestCancelEvent) {
    eXo.communication.chat.core.AdvancedDOMEvent.cancelEvent(event);
  }
  this._setOption('visible', visible);
  if (visible) {
    var activeTabControl = this.getActiveTabControl();
    if (activeTabControl) {
      activeTabControl.updateUnreadMessage();
    }
    if (this.rootNode.style.display != 'block') {
      this.rootNode.style.display = 'block';
    }
    this.UIPopupManager.focusEventFire(this);
    this._initUIOptions();
  } else {
    this.updateUnreadMessage();
    if (this.rootNode.style.display != 'none') {
      this.rootNode.style.display = 'none';
    }
  }
  if (this.totalTab > 0 &&
      !visible &&
      this.miniBoxChatNode.style.display != 'block') {
    this.miniBoxChatNode.style.display = 'block';
  } else if((this.totalTab <= 0 || visible) &&
            this.miniBoxChatNode.style.display != 'none') {
    this.miniBoxChatNode.style.display = 'none';
  }
};

UIChatWindow.prototype._setVisible = UIChatWindow.prototype.setVisible;

UIChatWindow.prototype.insertToMessageInputBox = function(txt) {
  var activeTabControl = this.getActiveTabControl();
  activeTabControl.insertToMessageBox(txt);
};

UIChatWindow.prototype.sendMsgFromActiveTab = function() {
  var activeTabControl = this.getActiveTabControl();
  var msg = activeTabControl.msgTypingBox.value;
  if (!msg ||
        (msg && (/^\s+$/).test(msg))) {
    return;
  }
  // Re-Format message
  if (activeTabControl.isGroupChat) {
    this.UIMainChatWindow.jabberSendRoomMessage(activeTabControl.tabId.targetPerson, msg);
  } else {
    this.UIMainChatWindow.jabberSendMessage(activeTabControl.tabId.targetPerson, msg);
  }
  activeTabControl.sendMessage();
};

UIChatWindow.prototype.inviteToJoinRoom = function(event) {
  event = event || window.event;
  eXo.communication.chat.core.AdvancedDOMEvent.cancelEvent(event);
  var activeTabControl = this.getActiveTabControl();
  activeTabControl.inviteToJoinRoom();
};

UIChatWindow.prototype.leaveRoomChat = function(event) {
  event = event || window.event;
  eXo.communication.chat.core.AdvancedDOMEvent.cancelEvent(event);
  var activeTabControl = this.getActiveTabControl();
  var roomName = activeTabControl.tabId.targetPerson;
  roomName = roomName.substr(0, roomName.indexOf('@'));
  this.UIMainChatWindow.jabberLeaveFromRoom(roomName);
  this.closeTab(activeTabControl.tabId.id);
};

UIChatWindow.prototype.configRoom = function(event) {
  event = event || window.event;
  eXo.communication.chat.core.AdvancedDOMEvent.cancelEvent(event);
  var activeTabControl = this.getActiveTabControl();
  this.UIMainChatWindow.UIRoomConfigPopupWindow.setVisible(true, activeTabControl.tabId.targetPerson);
};

UIChatWindow.prototype.roomInfoEventFired = function(roomInfoData) {
  for (var item in this.tabControlList) {
    var uiTabControlObj = this.tabControlList[item];
    if (roomInfoData &&
        roomInfoData.roomInfo &&
        uiTabControlObj &&
        uiTabControlObj.tabId.targetPerson == roomInfoData.roomInfo.room) {
      uiTabControlObj.roomInfoEventFired(roomInfoData);
    }
  }
};

UIChatWindow.prototype.hideElement = function(element) {
	element.style.display = 'none';
  element.style.position = 'absolute';
  element.style.left = '-1000px';
};

// -- File exchange --
UIChatWindow.prototype.initFileExchange = function() {
  var tmpDiv = document.createElement('div');
  tmpDiv.innerHTML = '<iframe name="' + this.targetUploadIframe + '" class="ChatUploadIframe" src="#"></iframe>';
  this.uploadIframe = eXo.core.DOMUtil.findFirstDescendantByClass(tmpDiv, 'iframe', 'ChatUploadIframe');
  this.uploadIframe.UIChatWindow = this;
  this.hideElement(tmpDiv);
  document.body.appendChild(tmpDiv);
};

UIChatWindow.prototype.fileExchangeEventFire = function(fileEvents) {
  for (var i=0; i<fileEvents.length; i++) {
    var fileEvent = fileEvents[i];
    // Process file transport request
    if (fileEvent.fileTransportRequests.length) {
      for (var j=0; j < fileEvent.fileTransportRequests.length; j++) {
        var fileTransportRequest = fileEvent.fileTransportRequests[j];
        var tabId = this.getTabId(fileTransportRequest.requestor);
        var uiTabControlObj = this.getUITabControl(tabId, false, true);
        if (uiTabControlObj) {
          this.focusTab(uiTabControlObj.tabId.id);
          uiTabControlObj.fileTransportRequestEventFire(fileTransportRequest);
        }
      }
    } else { // Process file transport response
      for (var j=0; j < fileEvent.fileTransportResponses.length; j++) {
        var fileTransportResponse = fileEvent.fileTransportResponses[j];
        var tabId = this.getTabId(fileTransportResponse.receiver);
        var uiTabControlObj = this.getUITabControl(tabId, false, true);
        if (uiTabControlObj) {
          uiTabControlObj.fileTransportResponseEventFire(fileTransportResponse);
        }
      }
    }
  }
};

UIChatWindow.prototype.acceptFileExchange = function(acceptNode) {
  this.getActiveTabControl().acceptFileExchange(acceptNode);
};

UIChatWindow.prototype.denieFileExchange = function(denieNode) {
  this.getActiveTabControl().denieFileExchange(denieNode);
};

UIChatWindow.prototype.sendFile = function(fileChooserNode, event) {
  event = event || window.event;
  eXo.communication.chat.core.AdvancedDOMEvent.cancelEvent(event);
  if (!fileChooserNode.value) {
    return;
  }
  var uploadForm = eXo.core.DOMUtil.findAncestorByTagName(fileChooserNode, 'form');
  
	var activeTabControl = this.getActiveTabControl();
	var description = '';
	var userName = activeTabControl.tabId.owner;
	var targetUser = activeTabControl.tabId.targetPerson;
	targetUser = targetUser.substr(0, targetUser.indexOf('@'));
	uploadForm.action = '/chat/fileexchange?username=' + userName + '&requestor=' + targetUser + '&description=' + description;
	this.uploadIframe.onload = function() {
  	window.jsconsole.warn('upload completed');
  	this.onload = null;
  };
	uploadForm.submit();
	fileChooserNode.value = '';
};
// --/--

// -- Message history --

UIChatWindow.prototype.updateMessageHistory = function(messageList) {
  var uiTabControlObj = this.getActiveTabControl();
  if (uiTabControlObj) {
    uiTabControlObj.updateHistoryMessage(messageList);
  }
};

UIChatWindow.prototype.getMessageHistory = function(event, timeNo) {
  var activeTabControl = this.getActiveTabControl();
  if (!activeTabControl) return;
  var historyStatus = activeTabControl.uiTabContentNode.historyStatus;
  if (historyStatus == timeNo) {
    return;
  } else {
    historyStatus = -1;
  }
  var endDate = new Date();
  var startDate = new Date(endDate);
  var javaTimeFormat = 'HH:mm:ss:dd:MM:yyyy';
  switch (timeNo) {
    case this.THIS_WEEK_MESSAGE:
      startDate.setDate(endDate.getDate() - endDate.getDay());
      break;
    case this.LAST_30_DAY_MESSAGE:
      startDate.setDate(endDate.getDate() - 30);
      break;
    case this.BEGINNING_MESSAGE:
      startDate = false;
      break;
  }
  if (startDate) {
    startDate = startDate.format('00:00:00:dd:mm:yyyy');
    endDate = endDate.format('HH:MM:ss:dd:mm:yyyy');
  } else {
    javaTimeFormat = null;
  }
  
  var targetPerson = activeTabControl.tabId.targetPerson;
  targetPerson = targetPerson.substr(0, targetPerson.indexOf('@'));
  this.UIMainChatWindow.jabberGetMessageHistory(targetPerson, javaTimeFormat, startDate, endDate, activeTabControl.isGroupChat);
  historyStatus = timeNo;
  activeTabControl.uiTabContentNode.historyStatus = historyStatus;
  /*
  // Remove link who clicked
  event = event || window.event;
  var srcElement = event.srcElement || event.target;
  if (srcElement.className.indexOf('LinkHistory') != -1) {
    var DOMUtil = eXo.core.DOMUtil;
    var linkName = srcElement.textContent || srcElement.innerText;
    var messageHistoryHint = DOMUtil.findAncestorByClass(srcElement, 'HistoryList');
    linkName = linkName.toLowerCase();
    // Remove separator if exist
    var lineSeparator = DOMUtil.findPreviousElementByTagName(srcElement, 'div');
    if (lineSeparator &&
        lineSeparator.className &&
        lineSeparator.className.indexOf('Line') != -1) {
      DOMUtil.removeElement(lineSeparator);
    }

    DOMUtil.removeElement(srcElement);
    // Remove message history hint if nothing remain
    if (!DOMUtil.findFirstDescendantByClass(messageHistoryHint, 'a', 'LinkHistory') ||
        linkName.indexOf('beginning') != -1) {
      DOMUtil.removeElement(messageHistoryHint);
    }
  }
  */
};

UIChatWindow.prototype.exportHistory = function() {
  var historyStatus = false;
  var activeTabControl = this.getActiveTabControl();
  if (activeTabControl.uiTabContentNode.historyStatus) {
    historyStatus = activeTabControl.uiTabContentNode.historyStatus;
  } else {
    historyStatus = this.CURRENT_CONVERSATION_MESSAGE;
  }
  var endDate = new Date();
  var startDate = new Date(endDate);
  var javaTimeFormat = 'dd:MM:yyyy';
    
  switch (historyStatus) {
    case this.CURRENT_CONVERSATION_MESSAGE:
      startDate = new Date(activeTabControl.uiTabContentNode.startTime);
      break;
    case this.THIS_WEEK_MESSAGE:
      startDate.setDate(endDate.getDate() - endDate.getDay());
      break;
    case this.LAST_30_DAY_MESSAGE:
      startDate.setDate(endDate.getDate() - 30);
      break;
    case this.BEGINNING_MESSAGE:
      startDate = false;
      break;
  }
  if (startDate) {
    startDate = startDate.format('dd:mm:yyyy');
    endDate = null;
  } else {
    javaTimeFormat = null;
    startDate = null;
    endDate = null;
  }
  
  var targetPerson = activeTabControl.tabId.targetPerson;
  targetPerson = targetPerson.substr(0, targetPerson.indexOf('@'));
  var currentUser = this.UIMainChatWindow.userNames[this.UIMainChatWindow.XMPPCommunicator.TRANSPORT_XMPP];
  var url = '/chat/messengerservlet/' + this.UIMainChatWindow.XMPPCommunicator.TRANSPORT_XMPP + '/history/file/getmessages/' + currentUser + '/' + targetPerson + '/' + activeTabControl.isGroupChat + '/';
  if (javaTimeFormat) {
    url += javaTimeFormat + '/';
  }
  if (startDate) {
    url += startDate + '/';
  }
  if (startDate &&
      endDate) {
    url += endDate + '/';
  }
  this.uploadIframe.src = url;
};

// --/--

// -- Scroll management --
/***** Scroll Management *****/
/**
 * Function called to load the scroll manager that will manage the tabs in the main nav menu
 *  . Creates the scroll manager with id UIChatWindowTopContainer
 *  . Adds the tabs to the scroll manager
 *  . Configures the arrows
 *  . Calls the initScroll function
 */
UIChatWindow.prototype.loadScroll = function() {
  // Creates new ScrollManager and initializes it
  if (!this.scrollMgr) {
    this.scrollMgr = new eXo.communication.chat.webui.TabScrollManager("UIChatWindow");
    this.scrollMgr.initFunction = this.initScroll;
    // Adds the tab elements to the manager
    this.scrollMgr.mainContainer = this.tabsContainerNode;
    this.scrollMgr.arrowsContainer = eXo.core.DOMUtil.findFirstDescendantByClass(this.tabsContainerNode, "div", "ScrollButtons");
    this.scrollMgr.loadElements("UITab");
    // Configures the arrow buttons
    var arrowButtons = eXo.core.DOMUtil.findDescendantsByTagName(this.scrollMgr.arrowsContainer, "div");
    if (arrowButtons.length == 2) {
      this.scrollMgr.initArrowButton(arrowButtons[0], "left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton");
      this.scrollMgr.initArrowButton(arrowButtons[1], "right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton");
    }
    // Finish initialization
    this.scrollMgr.callback = this.scrollCallback;
    this.scrollManagerLoaded = true;
    this.initScroll();
  }
};

/**
 * Init function for the scroll manager
 *  . Calls the init function of the scroll manager
 *  . Calculates the available space to render the tabs
 *  . Renders the tabs
 */
UIChatWindow.prototype.initScroll = function(e) {
  if (!this.scrollManagerLoaded) this.loadScroll();
  var scrollMgr = this.scrollMgr;
  scrollMgr.init();
  // Gets the maximum width available for the tabs
  this.checkScroll();
};
/**
 * A callback function to call after a scroll event occurs (and the elements are rendered)
 * Is empty so far.
 */
UIChatWindow.prototype.scrollCallback = function() {
  /*var firstIndex = false;
  var lastIndex = false;
  for (var i=0; i<this.elements.length; i++) {
    if (this.elements[i].isVisible &&
        !firstIndex) {
      firstIndex = i;
      continue;
    } else if (!lastIndex){
      lastIndex = i - 1;
      break;
    }
  }
  this.firstVisibleIndex = firstIndex;
  this.lastVisibleIndex = lastIndex;*/
};

UIChatWindow.prototype.checkScroll = function(isReset) {
  if (this.rootNode.offsetHeight <= 0) {
    return;
  }
  // Reset somethings.
  if (isReset) {
    if (this.scrollMgr.arrowsContainer.style.display != 'none') {
      this.scrollMgr.arrowsContainer.style.display = 'none';
    }
    this.scrollMgr.mainContainer.space = null;
  }

  this.scrollMgr.loadElements("UITab", true);
  this.scrollMgr.checkAvailableSpace();
  this.scrollMgr.renderElements();
};

// Function using to auto scroll to visible tab when tab elements is changed
UIChatWindow.prototype.autoScroll = function(focusTabIndex) {
  var scrollMgr = eXo.communication.chat.webui.UIChatWindow.scrollMgr;
  if (!scrollMgr) {
    return;
  }
  scrollMgr.scrollTo(focusTabIndex);
};
// -- / --

eXo.communication.chat.webui.UIChatWindow = new UIChatWindow();
