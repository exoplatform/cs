/**
 * @author Uoc Nguyen
 *  email: uoc.nguyen@exoplatform.com
 */
function BuddyItem(buddyInfo, actionCallback, maxUserNameLen, isGroupChat) {
  this.buddyInfo = buddyInfo;
  this.actionCallback = actionCallback;
  this.CSS_CLASS = {
    template        : 'TitleIconChat',
    nick            : 'IconChat',
    unavailable     : 'OfflineIcon',
    available       : 'OnlineIcon',
    away            : 'AwayIcon',
    xa              : 'AwayIcon',
    chat            : 'FreeToChat',
    'free to chat'  : 'FreeToChat',
    busy            : 'ExtendsAwayIcon',
    dnd             : 'ExtendsAwayIcon'
  };
  this.MAX_USERNAME_LEN = maxUserNameLen || -1;
  this.isGroupChat = isGroupChat;
  if (this.isGroupChat) {
    with (this.CSS_CLASS) {
      template = 'GroupNick';
      nick     = 'NameId';
    }
  }
  this.init();
  this.updateStatus(buddyInfo.presence.type, true);
}

BuddyItem.prototype.init = function() {
  var DOMUtil = eXo.core.DOMUtil;
  this.rootNode = eXo.communication.chat.core.LocalTemplateEngine.getTemplateByClassName(this.CSS_CLASS.template);

  if (this.isGroupChat) {
    this.iconChatNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', this.CSS_CLASS.nick);
  } else {
    this.iconChatNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'a', this.CSS_CLASS.nick);
  }
  this.updateStatus(this.buddyInfo.presence.type);

  this.iconChatNode.innerHTML = this.getUserName(this.buddyInfo.user, true);
  this.iconChatNode.setAttribute('title' ,this.getUserName(this.buddyInfo.user, false));

  this.rootNode.setAttribute('userName', this.buddyInfo.user);
  this.rootNode.setAttribute('nickname', this.buddyInfo.nickname);
  eXo.communication.chat.core.AdvancedDOMEvent.addEventListener(this.rootNode, 'contextmenu', this.actionCallback, false);
  eXo.communication.chat.core.AdvancedDOMEvent.addEventListener(this.rootNode, 'click', this.actionCallback, false);
};

BuddyItem.prototype.updateStatus = function(newStatus, skipCheck) {
  newStatus = newStatus.toLowerCase();
  window.jsconsole.warn('update status for: ' + this.buddyInfo.user + ' from ' + this.buddyInfo.presence.type + ' to ' + newStatus);
  var cssClass = this.CSS_CLASS[newStatus];
  if (skipCheck ||
      this.buddyInfo.presence.type != newStatus &&
      cssClass) {
    this.buddyInfo.presence.type = newStatus;
    this.iconChatNode.className = 
                  this.iconChatNode.className.replace(this.CSS_CLASS[this.buddyInfo.presence.type], cssClass);
    if (this.iconChatNode.className.indexOf(this.CSS_CLASS[this.buddyInfo.presence.type]) == -1) {
      this.iconChatNode.className = this.CSS_CLASS.nick + ' ' + this.CSS_CLASS[this.buddyInfo.presence.type];
    }
  }
};

BuddyItem.prototype.getUserName = function(userNameFullStr, trimLen) {
  if (userNameFullStr.indexOf('/') != -1) {
    userNameFullStr = userNameFullStr.substring(0, userNameFullStr.indexOf('/'));
  }
  if (trimLen &&
      this.MAX_USERNAME_LEN > 0 &&
      userNameFullStr.length > this.MAX_USERNAME_LEN) {
    userNameFullStr = userNameFullStr.substr(0,this.MAX_USERNAME_LEN - 1) + '...';
  }
  return userNameFullStr;
};

BuddyItem.prototype.remove = function() {
  var buddyItemNode = false;
  if (this.buddyInfo) {
    buddyItemNode = this.rootNode
  } else {
    buddyItemNode = eXo.core.DOMUtil.findAncestorByClass(this, 'BuddyItem');
  }
  if (buddyItemNode) {
    eXo.core.DOMUtil.removeElement(buddyItemNode);
  }
  return false;
}

eXo.communication.chat.webui.component.BuddyItem = BuddyItem;

/**
 * BuddyListControl component
 * 
 * @param {Node} rootNode
 */
function BuddyListControl(rootNode, buddyItemActionCallback, UIMainChatWindow) {
  this.rootNode = rootNode;
  if (!this.rootNode || !(this.rootNode.tagName)) {
    this.rootNode = document.createElement('div');
  }
  this.isGroupChat = false;
  this.UIMainChatWindow = UIMainChatWindow;
  this.MAX_USERNAME_LEN = -1;
  this.buddyItemActionCallback = buddyItemActionCallback;
  this.BuddyItem = eXo.communication.chat.webui.component.BuddyItem;
  this.buddyList = {};
  this.cleanup();
}

/**
 * @return {eXo.communication.chat.webui.component.BuddyItem}
 */
BuddyListControl.prototype.getNewInstanceOfBuddyItem = function(buddyInfo) {
  var buddyItemObj = new this.BuddyItem(buddyInfo, this.buddyItemActionCallback, this.MAX_USERNAME_LEN, this.isGroupChat);
  return buddyItemObj;
};

BuddyListControl.prototype.getBuddyItem = function(buddyId) {
  this.buddyList = this.buddyList || {};
  var buddyObj = this.buddyList[this.getUserName(buddyId)];
  if (buddyObj &&
      buddyObj.buddyInfo) {
    return buddyObj;
  }
  return false;
};

BuddyListControl.prototype.removeBuddy = function(buddyId){
  window.jsconsole.warn('Removing buddy: ' + buddyId);
  var buddyItemObj = this.getBuddyItem(buddyId);
  if (buddyItemObj) {
    buddyItemObj.remove();
    this.buddyList[buddyId] = null;
  }
};

BuddyListControl.prototype.addBuddy = function(buddyInfo){
  window.jsconsole.warn('Adding new buddy: ' + buddyInfo.user);
  var buddyItemObj = this.buddyList[buddyInfo.user];
  if (buddyItemObj) {
    window.jsconsole.warn('User existed, skip add buddy');
  } else {
    buddyItemObj = this.getNewInstanceOfBuddyItem(buddyInfo);
    this.buddyList[buddyInfo.user] = buddyItemObj;
    this.rootNode.appendChild(buddyItemObj.rootNode);
  }
};

// Build new buddy list
BuddyListControl.prototype.build = function(roster, isNotCleanUp) {
  if (!isNotCleanUp) {
    this.cleanup();
  }
  this.buddyList = this.buddyList || {};
  for (var i=0; i<roster.length; i++) {
    var buddyInfo = roster[i];
    buddyItemObj = this.getNewInstanceOfBuddyItem(buddyInfo);
    this.buddyList[buddyInfo.user] = buddyItemObj;
    this.rootNode.appendChild(buddyItemObj.rootNode);
  }
};

BuddyListControl.prototype.room2StandardContactList = function(roomContactList) {
  if (!roomContactList) {
    return [];
  }
  if (!roomContactList.length && roomContactList.length != 0) {
    roomContactList = [roomContactList];
  }
  var contactList = [];
  var mainServiceName = this.UIMainChatWindow.serverInfo.mainServiceName;
  var mucServiceName = this.UIMainChatWindow.serverInfo.mucServicesNames[0];
  for (var i=0; i<roomContactList.length; i++) {
    if (!roomContactList[i].nick) {
      debugger;
      continue;
    }
    var contact = {};
    contact.presence = {mode:null, status:null, type: 'available'};
    contact.presence.from = roomContactList[i].jid;
    contact.nickname = roomContactList[i].nick;
    contact.groups = [];
    contact.subscriptionStatus = null;
    contact.subscriptionType = null;
    contact.user = contact.nickname + '@' +  mainServiceName;
    contactList.push(contact);
  }
  return contactList;
};

BuddyListControl.prototype.cleanup = function() {
  if (this.buddyList) {
    for (var item in this.buddyList) {
      var buddyItemObj = this.buddyList[item];
      if (buddyItemObj &&
          buddyItemObj instanceof Object &&
          buddyItemObj.buddyInfo) {
        try {
          buddyItemObj.remove();
        } catch (e) {}
      }
    }
  }
  this.buddyList = null;
  this.rootNode.innerHTML = '<span/>';
};

BuddyListControl.prototype.getUserName = function(userNameFullStr) {
  if (userNameFullStr.indexOf('/') != -1) {
    return (userNameFullStr.substring(0, userNameFullStr.indexOf('/')));
  } else {
    return userNameFullStr;
  }
};

BuddyListControl.prototype.update = function(presences) {
  for (var i=0; i<presences.length; i++) {
    var presence = presences[i];
    window.jsconsole.debug('update status for: ' + presence.from);
    var buddyItemObj = this.getBuddyItem(presence.from);
    if (buddyItemObj) {
      if (presence.mode) {
        buddyItemObj.updateStatus(presence.mode);
      } else {
        buddyItemObj.updateStatus(presence.type);
      }
    }
  }
};

BuddyListControl.prototype.xUpdate = function(roomContactList) {
  if (!roomContactList) {
    return;
  }
  if (!roomContactList.length) {
    roomContactList = [roomContactList];
  }
  var rosterTmp = this.room2StandardContactList(roomContactList);
  this.buddyList = this.buddyList || {};
  for (var i=0; i<rosterTmp.length; i++) {
    var buddyInfo = rosterTmp[i];
    var presence = roomContactList[i];
    var buddyItemObj = this.buddyList[buddyInfo.user];
    if (buddyItemObj) {
      if (presence.type == 'unavailable') {
        this.removeBuddy(buddyInfo.user);
        continue;
      }
      buddyItemObj.updateStatus('available');
    } else {
      this.addBuddy(buddyInfo);
    }
  }
};

eXo.communication.chat.webui.component.BuddyListControl = BuddyListControl;
