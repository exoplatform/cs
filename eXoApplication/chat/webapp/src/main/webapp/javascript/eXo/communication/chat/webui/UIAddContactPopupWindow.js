/**
 * @author Uoc Nguyen
 *  email: uoc.nguyen@exoplatform.com
 */
function UIAddContactPopupWindow() {
  this.CSS_CLASS = {
    searchField      : 'SearchField',
    uiGrid           : 'UIGrid',
    toggleSelect     : 'ToggleSelect',
    uiPageIterator   : 'UIPageIterator',
    pagesTotal       : 'PagesTotalNumber',
    previousTopPage  : 'LastTopPageIcon',
    previousPage     : 'LastPageIcon',
    nextPage         : 'NextPageIcon',
    nextTopPage      : 'NextTopPageIcon',
    disabledPrefix   : 'Disable',
    pageList         : 'Number',
    selectedPage     : 'PageSelected',
    addContactButton : 'AddContactButton'
  };
  this.currentPageNo = 0;
  this.totalPage = 0;
  this.NUM_PER_PAGE = 10;
  this.MAX_PAGE_NUM = 5;
}

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
  this.pageIteratorNode.UIWindow = this;
  this.pageListNode = DOMUtil.findFirstDescendantByClass(this.pageIteratorNode, 'div', this.CSS_CLASS.pageList);
  this.totalPageNode = DOMUtil.findFirstDescendantByClass(this.pageIteratorNode, 'div', this.CSS_CLASS.pagesTotal);
  this.addContactButtonNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'a', this.CSS_CLASS.addContactButton);
  this.addContactButtonNode.hrefBk = this.addContactButtonNode.href;
  var doSearchContactWrapper = function() {
    eXo.communication.chat.webui.UIAddContactPopupWindow.doSearchContact();
  }
  /*
  if (window.addEventListener) {
    this.filterFieldNode.addEventListener('keyup', doSearchContactWrapper, false);
  } else {
    this.filterFieldNode.attachEvent('onkeyup', doSearchContactWrapper, false);
  }
  */
  eXo.communication.chat.core.AdvancedDOMEvent.addEventListener(this.filterFieldNode, 'keyup', doSearchContactWrapper, false);
};

UIAddContactPopupWindow.prototype.doSearchContact = function(keyword, from, to) {
  keyword = keyword || this.filterFieldNode.value;
  keyword = keyword || '*';
  if (keyword.indexOf('*') != (keyword.length - 1)) {
    keyword += '*';
  }
  if (keyword != this.keywordbk) {
    this.currentPageNo = 0;
  }
  this.keywordbk = keyword;
  from = from || 0;
  to = to || 10;
  eXo.communication.chat.webui.UIMainChatWindow.orgFuzzySearchUser(keyword , from, to);
};

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
  this.renderPageIterator(serverData);
  if (this.rootNode.style.display != 'block') {
    this.rootNode.style.display = 'block';
  }
  this.filterFieldNode.focus();
  this.UIPopupManager.focusEventFire(this);
};

/**
 * This filter will used default for main buddy list.
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
 * 
 * @param {Object} contactInfo
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
  
  tdTmpNode.innerHTML = '<span/>';
  
  tdTmpNode.innerHTML = contactInfo['firstName'];
  uiContactRowNode.appendChild(tdTmpNode.cloneNode(true));
  tdTmpNode.innerHTML = contactInfo['lastName'];
  uiContactRowNode.appendChild(tdTmpNode.cloneNode(true));
  tdTmpNode.innerHTML = contactInfo['userName'];
  uiContactRowNode.appendChild(tdTmpNode.cloneNode(true));
  
  return uiContactRowNode;
};

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

UIAddContactPopupWindow.prototype.renderPageIterator = function(serverData) {
  this.totalPage = Math.ceil(serverData.totalUser/this.NUM_PER_PAGE);
  //debugger;
  if (this.totalPage > 1) {
    this.totalPageNode.innerHTML = this.totalPage + '';
    this.toggleNavButtons();
    
    this.pageListNode.innerHTML = '';
    // Calculate pageStart and pageEnd
    var pageStart = this.currentPageNo;
    var pageEnd = this.currentPageNo + this.MAX_PAGE_NUM;
    if (pageEnd > this.totalPage) {
      var delta = pageEnd - this.totalPage;
      pageEnd = this.totalPage;
      pageStart = pageStart - delta;
      pageStart = (pageStart < 0) ? 0 : pageStart;
    }
    for (var i=pageStart; i<pageEnd; i++) {
      var pageNode = document.createElement('a');
      if (i == this.currentPageNo) {
        pageNode.className = this.CSS_CLASS.selectedPage;
      }
      pageNode.innerHTML = (i + 1);
      pageNode.pageNo = i;
      pageNode.style.cursor = 'pointer';
      pageNode.onclick = this.gotoPageWrapper;

      this.pageListNode.appendChild(pageNode);
    }

    if (this.pageIteratorNode.style.display != 'block') {
      this.pageIteratorNode.style.display = 'block';
    }
  } else {
    if (this.pageIteratorNode.style.display != 'none') {
      this.pageIteratorNode.style.display = 'none';
    }
  }
};

UIAddContactPopupWindow.prototype.enableNavButton = function(buttonNode, enable, disabledClass, enabledClass) {
  if (!buttonNode) {
    window.jsconsole.debug('buttonNode = ' + buttonNode + ' - args=', arguments);
  }
  window.jsconsole.debug('enable: ' + enable, buttonNode);
  var className = buttonNode.className;
  if (enable) {
    buttonNode.className = 'Icon ' + enabledClass;
    buttonNode.onclick = buttonNode.onclickbk || buttonNode.onclick;
  } else {
    buttonNode.className = 'Icon ' + disabledClass;
    buttonNode.onclickbk = buttonNode.onclick;
    buttonNode.onclick = null;
  }
};

UIAddContactPopupWindow.prototype.reload = function() {
  this.gotoPage(this.currentPageNo, true, true);
};

UIAddContactPopupWindow.prototype.gotoPageWrapper = function() {
  var uiPageIterator = eXo.core.DOMUtil.findAncestorsByClass(this, 'UIPageIterator');
  if (uiPageIterator &&
      uiPageIterator[0]) {
    uiPageIterator[0].UIWindow.gotoPage(this.pageNo, true);
  } else {
    window.jsconsole.error('Can not find UIPageIterator');
  }
};

UIAddContactPopupWindow.prototype.gotoPage = function(pageNum, isAbsolutePage, forceReload) {
  var targetPageNo = parseInt(this.currentPageNo) + parseInt(pageNum);
  if (isAbsolutePage) {
    targetPageNo = pageNum;
  }
  if (targetPageNo < 0) {
    targetPageNo = 0;
  }
  if (targetPageNo >= this.totalPage) {
    targetPageNo = this.totalPage - 1;
  }
  if (!forceReload &&
      targetPageNo == this.currentPageNo) {
    return;
  }
  window.jsconsole.warn('Go to page: ' + targetPageNo);
  var from = targetPageNo * this.NUM_PER_PAGE;
  from = (from >= 0) ? from : 0;
  var to = from + this.NUM_PER_PAGE;
  var keyword = this.filterFieldNode.value;
  this.doSearchContact(keyword, from, to);
  this.currentPageNo = targetPageNo;
};

UIAddContactPopupWindow.prototype.toggleNavButtons = function() {
  // Navigator button toggle
  var DOMUtil = eXo.core.DOMUtil;
  var previousPageNode = 
        DOMUtil.findFirstDescendantByClass(this.pageIteratorNode, 'a', this.CSS_CLASS.previousPage) ||
        DOMUtil.findFirstDescendantByClass(this.pageIteratorNode, 'a', this.CSS_CLASS.disabledPrefix + this.CSS_CLASS.previousPage);
  var nextPageNode = 
        DOMUtil.findFirstDescendantByClass(this.pageIteratorNode, 'a', this.CSS_CLASS.nextPage) ||
        DOMUtil.findFirstDescendantByClass(this.pageIteratorNode, 'a', this.CSS_CLASS.disabledPrefix + this.CSS_CLASS.nextPage);
  var nextTopPageNode = 
        DOMUtil.findFirstDescendantByClass(this.pageIteratorNode, 'a', this.CSS_CLASS.nextTopPage) ||
        DOMUtil.findFirstDescendantByClass(this.pageIteratorNode, 'a', this.CSS_CLASS.disabledPrefix + this.CSS_CLASS.nextTopPage);
  var previousTopPageNode = 
        DOMUtil.findFirstDescendantByClass(this.pageIteratorNode, 'a', this.CSS_CLASS.previousTopPage) ||
        DOMUtil.findFirstDescendantByClass(this.pageIteratorNode, 'a', this.CSS_CLASS.disabledPrefix + this.CSS_CLASS.previousTopPage);
  var isPreviousEnable = false;
  var isNextEnable = false;
  var isFirstEnable = false;
  var isLastEnable = false;
  if (this.isPageValid(this.currentPageNo - 1)) {
    isPreviousEnable = true;
  }
  if (this.isPageValid(this.currentPageNo + 1)) {
    isNextEnable = true;
  }
  if (this.isPageValid(this.currentPageNo - 10)) {
    isFirstEnable = true;
  }
  if (this.isPageValid(this.currentPageNo + 10)) {
    isLastEnable = true;
  }
  this.enableNavButton(previousTopPageNode, isFirstEnable, this.CSS_CLASS.disabledPrefix + this.CSS_CLASS.previousTopPage, this.CSS_CLASS.previousTopPage);
  this.enableNavButton(previousPageNode, isPreviousEnable, this.CSS_CLASS.disabledPrefix + this.CSS_CLASS.previousPage, this.CSS_CLASS.previousPage);
  this.enableNavButton(nextPageNode, isNextEnable, this.CSS_CLASS.disabledPrefix + this.CSS_CLASS.nextPage, this.CSS_CLASS.nextPage);
  this.enableNavButton(nextTopPageNode, isLastEnable, this.CSS_CLASS.disabledPrefix + this.CSS_CLASS.nextTopPage, this.CSS_CLASS.nextTopPage);
};

UIAddContactPopupWindow.prototype.isPageValid = function(pageNo) {
  var valid = false;
  if (pageNo >= 0 && pageNo <= (this.totalPage - 1)) {
    valid = true;
  }
  window.jsconsole.warn('check pageno: ' + pageNo + ' valid:' + valid);
  return valid;
};

UIAddContactPopupWindow.prototype.setVisible = function(visible, handler){
  if (!this.UIMainChatWindow.userStatus ||
      this.UIMainChatWindow.userStatus == this.UIMainChatWindow.OFFLINE_STATUS) {
    return;
  }
  if (visible) {
    //window.alert('handler callback: ', handler);
    //window.alert('handler callback: ', handler.addContactActionCallback);
    //eXo.communication.chat.webui.UIMainChatWindow.orgSearchUser();
    eXo.communication.chat.webui.UIMainChatWindow.orgFuzzySearchUser('*', 0, 10);
    this.filterFieldNode.value = '';
    this.toggleSelectAllNode.checked = false;
    //this.filterFieldNode.focus();
    this.handler = handler;
    this.totalPage = 0;
    this.currentPageNo = 0;
  } else {
    if (this.rootNode.style.display != 'none') {
      this.rootNode.style.display = 'none';
    }
    this.handler = null;
  }
};

eXo.communication.chat.webui.UIAddContactPopupWindow = new UIAddContactPopupWindow();
