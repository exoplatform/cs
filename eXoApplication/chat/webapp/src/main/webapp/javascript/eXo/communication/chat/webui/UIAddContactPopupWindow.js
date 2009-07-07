/**
 * @author Uoc Nguyen
 *  email: uoc.nguyen@exoplatform.com
 * This object is using to manage add contact popup window.
 */
function UIAddContactPopupWindow() {
  this.CSS_CLASS = {
    searchField      : 'SearchField',
    uiGrid           : 'UIGrid',
    toggleSelect     : 'ToggleSelect',
    uiPageIterator   : 'UIPageIterator',
    addContactButton : 'AddContactButton'
  };
}

/**
 * Initialize method
 *
 * @param {HTMLElement} rootNode
 * @param {UIMainChatWindow} UIMainChatWindow
 */
UIAddContactPopupWindow.prototype.init = function(rootNode, UIMainChatWindow) {
  this.handler = false;
  var DOMUtil = eXo.core.DOMUtil;
  this.rootNode = rootNode;
  this.UIMainChatWindow = UIMainChatWindow;
  var tmpNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'table', this.CSS_CLASS.uiGrid);
  this.contactListContainerNode = tmpNode.getElementsByTagName('tbody')[0];
  this.toggleSelectAllNode = DOMUtil.findFirstDescendantByClass(tmpNode, 'input', this.CSS_CLASS.toggleSelect);
  this.LocalTemplateEngine = this.UIMainChatWindow.LocalTemplateEngine;
  this.filterFieldNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'input', this.CSS_CLASS.searchField);
  this.pageIteratorNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', this.CSS_CLASS.uiPageIterator);
  this.addContactButtonNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', this.CSS_CLASS.addContactButton);
  this.addContactButtonNode.hrefBk = this.addContactButtonNode.href;
  eXo.communication.chat.core.AdvancedDOMEvent.addEventListener(this.filterFieldNode, 'keyup', this.doSearchContactWrapper, false);
  this.uiPageIterator = new eXo.communication.chat.webui.UIPageIterator(this.pageIteratorNode);
  this.uiPageIterator.setGotoPageCallback(this.doSearchContact);
};

/**
 * Wrapper method will be call doSearchContact method.
 *
 * @param {Event} event
 */
UIAddContactPopupWindow.prototype.doSearchContactWrapper = function(event) {
  event = event || window.event;
  eXo.communication.chat.webui.UIAddContactPopupWindow.doSearchContact();
};

/**
 * Call servicce to filter contact list
 *
 * @param {Integer} from
 * @param {Integer} to
 * @param {String} keyword
 */
UIAddContactPopupWindow.prototype.doSearchContact = function(from, to, keyword) {
  var thys = eXo.communication.chat.webui.UIAddContactPopupWindow;
  keyword = keyword || thys.filterFieldNode.value;
  keyword = keyword || '*';
  if (keyword.indexOf('*') != (keyword.length - 1)) {
    keyword += '*';
  }
  if (keyword != thys.keywordbk) {
    thys.uiPageIterator.currentPageNo = 0;
  }
  thys.keywordbk = keyword;
  from = from || 0;
  to = to || 10;
  eXo.communication.chat.webui.UIMainChatWindow.orgFuzzySearchUser(keyword , from, to);
};

/**
 * Update contact list will be called by UIMainChatWindow after a search contact request responsed.
 *
 * @param {Array[ContactInfo]} serverData
 */
UIAddContactPopupWindow.prototype.updateContactList = function(serverData) {
  if (!this.contactListContainerNode) {
    return;
  }
  this.selectAllContacts(false);
  var tmpNode = this.contactListContainerNode.parentNode;
  tmpNode.removeChild(this.contactListContainerNode);
  this.contactListContainerNode = document.createElement('tbody');
  tmpNode.appendChild(this.contactListContainerNode);
  // Add new contact list
  var contactList = serverData.users;
  var currentActiveUser = eXo.communication.chat.webui.UIMainChatWindow.userNames['xmpp'];
  for (var i=0; i<contactList.length; i++) {
    var contact = contactList[i];
    contact.enabled4Add = true;
    if (currentActiveUser == contact['userName']) {
      continue;
    }
    if (this.handler &&
        this.handler.contactUpdateFilter) {
      this.handler.contactUpdateFilter(contact);
    } else {
      this.filter4MainBuddyList(contact);
    }
    this.contactListContainerNode.appendChild(this.createContactNode(contact, (i%2)));
  }
  this.uiPageIterator.totalItem = serverData.totalUser;
  this.uiPageIterator.renderPageIterator();
  if (this.rootNode.style.display != 'block') {
    this.rootNode.style.display = 'block';
  }
  this.filterFieldNode.focus();
  this.UIPopupManager.focusEventFire(this);
};

/**
 * Use to reload contact result pane and page iterator pane
 */
UIAddContactPopupWindow.prototype.reload = function() {
  this.uiPageIterator.reload();
};

/**
 * This filter will used default for main buddy list.
 *
 * @param {Object} contact
 */
UIAddContactPopupWindow.prototype.filter4MainBuddyList = function(contact) {
  var currentContactList = eXo.communication.chat.webui.UIMainChatWindow.buddyListControlObj.buddyList || [];
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

/**
 * Common method to create a contact DOM node to add to result pane
 * 
 * @param {Object} contactInfo
 * @param {Boolean} isAlternate
 */
UIAddContactPopupWindow.prototype.createContactNode = function(contactInfo, isAlternate) {
  var DOMUtil = eXo.core.DOMUtil;
  var uiContactRowNode = document.createElement('tr');
  if (isAlternate) {
    uiContactRowNode.className = 'UIContactRowC';
  } else {
    uiContactRowNode.className = 'UIContactRow';
  }
  var tdTmpNode = document.createElement('td');
  
  var selectBoxNode = document.createElement('input');
  selectBoxNode.type = 'checkbox';
  selectBoxNode.className = 'CheckBox';
  selectBoxNode.name = contactInfo['userName'];
  if (!contactInfo.enabled4Add) {
    //selectBoxNode.disabled = 'true';
    //selectBoxNode.checked = 'true';
    var selectBoxCode = '<input type="checkbox" checked="true" disabled="true" \
                          class="CheckBox" name="' + contactInfo['userName'] + '">';
    tdTmpNode.innerHTML = selectBoxCode;
  } else {
    tdTmpNode.appendChild(selectBoxNode);
  }
  uiContactRowNode.appendChild(tdTmpNode.cloneNode(true));
  
  tdTmpNode.innerHTML = '<span></span>';
  
  tdTmpNode.innerHTML = contactInfo['firstName'];
  uiContactRowNode.appendChild(tdTmpNode.cloneNode(true));
  tdTmpNode.innerHTML = contactInfo['lastName'];
  uiContactRowNode.appendChild(tdTmpNode.cloneNode(true));
  tdTmpNode.innerHTML = contactInfo['userName'];
  uiContactRowNode.appendChild(tdTmpNode.cloneNode(true));
  
  return uiContactRowNode;
};

/**
 * Call when add contact button is clicked to add all selected contact to user contact list
 */
UIAddContactPopupWindow.prototype.addContactAction = function() {
  var DOMUtil = eXo.core.DOMUtil;
  var uiGridNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'table', 'UIGrid');
  var contactNodeList = DOMUtil.findDescendantsByClass(uiGridNode, 'input', 'CheckBox');
  var contactList = [];
  for (var i=0; i<contactNodeList.length; i++) {
    if (contactNodeList[i].checked &&
        contactNodeList[i].name &&
        !contactNodeList[i].disabled) {
      contactList.push(contactNodeList[i].name);
    }
  }
  if (contactList.length <= 0) {
    return;
  }
  if (this.handler &&
      this.handler.addContactActionCallback) {
    this.handler.addContactActionCallback(contactList);
  } else {
    eXo.communication.chat.webui.UIMainChatWindow.addContacts(contactList);
  }
  this.setVisible(false);
};

/**
 * Toggle select all/none contacts in contact result pane
 */
UIAddContactPopupWindow.prototype.toggleSelectAllContact = function() {
  var DOMUtil = eXo.core.DOMUtil;
  var uiGridNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'table', 'UIGrid');
  var selectMode = false;
  if (!uiGridNode.getAttribute('select') ||
      uiGridNode.getAttribute('select') == 'none') {
    selectMode = true;
  }

  this.selectAllContacts(selectMode);
  
  if (selectMode) {
    uiGridNode.setAttribute('select', 'all');
  } else {
    uiGridNode.setAttribute('select', 'none');
  }
};

/**
 * Select all/none contacts in contact result pane depends on selectMode is.
 */
UIAddContactPopupWindow.prototype.selectAllContacts = function(selectMode) {
  var DOMUtil = eXo.core.DOMUtil;
  var checkboxList = DOMUtil.findDescendantsByClass(this.contactListContainerNode, 'input', 'CheckBox');
  for (var i=0; i<checkboxList.length; i++) {
    var checkBoxTmp = checkboxList[i];
    if (!checkBoxTmp.disabled) {
      checkBoxTmp.checked = selectMode;
    }
  }
  var uiGridNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'table', 'UIGrid');
  var checkboxNode = DOMUtil.findFirstDescendantByClass(uiGridNode, 'input', 'CheckBox');
  checkboxNode.checked = selectMode;
};

/**
 * Use to make this component visible or not.
 *
 * @param {Boolean} visible
 * @param {Function} handler will be call to filter contact result before display it in result pane.
 */
UIAddContactPopupWindow.prototype.setVisible = function(visible, tabId, mustSubmit){
  if (!this.UIMainChatWindow.userStatus ||
      this.UIMainChatWindow.userStatus == this.UIMainChatWindow.OFFLINE_STATUS) {
    return;
  }
  if (visible) {
    //window.alert('handler callback: ', handler);
    //window.alert('handler callback: ', handler.addContactActionCallback);
    //eXo.communication.chat.webui.UIMainChatWindow.orgSearchUser();
    if ((tabId && tabId.targetPerson)) {
	    var chatRoomServiceName = this.UIMainChatWindow.serverInfo.mucServicesNames[0];
	    this.tabId = tabId;
	    var roomName = this.tabId.targetPerson;
	    if (roomName.indexOf('@' + chatRoomServiceName) != -1) {
	      roomName = roomName.substr(0, roomName.indexOf('@' + chatRoomServiceName));
	    }
	    eXo.communication.chat.webui.UIMainChatWindow.orgFuzzySearchUser('*', 0, 10, roomName);
	    this.filterFieldNode.value = '';
	    this.toggleSelectAllNode.checked = false;
	    this.uiPageIterator.destroy();
		} else {
			eXo.communication.chat.webui.UIMainChatWindow.orgFuzzySearchUser('*', 0, 10);
	    this.filterFieldNode.value = '';
	    this.toggleSelectAllNode.checked = false;
	    //this.handler = handler;
	    this.uiPageIterator.destroy();		
		}
  } else {
    if (this.rootNode.style.display != 'none') {
      this.rootNode.style.display = 'none';
    }
    this.mustSubmit = false;
  }
};

eXo.communication.chat.webui.UIAddContactPopupWindow = new UIAddContactPopupWindow();
