/**
 * @author Uoc Nguyen
 *  email uoc.nguyen@exoplatform.com
 *
 *  This is an UI component use to manage room configuration popup window
 */
function UIRoomConfigPopupWindow() {
  this.tabId = false;
  this.roomName = false;
  this.visible = false;
  this.ANIMATION_SPEED = 30;
  this.ANIMATION_TIME_STEP = 0.02*1000;
}

/**
 * Initializing method
 *
 * @param {HTMLElement} rootNode
 * @param {UIMainChatWindow} UIMainChatWindow
 */
UIRoomConfigPopupWindow.prototype.init = function(rootNode, UIMainChatWindow) {
  var DOMUtil = eXo.core.DOMUtil;
  this.rootNode = rootNode;
  this.UIMainChatWindow = UIMainChatWindow;
  this.fieldsetList = DOMUtil.findDescendantsByTagName(this.rootNode, 'fieldset');
  // Register toggle event to legend tags
  for (var i=0; i<this.fieldsetList.length; i++) {
    var fieldsetNode = this.fieldsetList[i];
    if (fieldsetNode.getAttribute('maxHeight')) {
      fieldsetNode.maxHeight = parseInt(fieldsetNode.getAttribute('maxHeight'));
    }
    var legendNode = fieldsetNode.getElementsByTagName('legend')[0];
    if (legendNode) {
      legendNode.onclick = this.focusGroupOptions;
      legendNode.style.cursor = 'pointer';
    }
  }
};

/**
 * Use to switch group options
 *
 * @param {Event} event
 */
UIRoomConfigPopupWindow.prototype.focusGroupOptions = function(event) {
  event = event || window.event;
  var srcElement = event.srcElement || event.target;
  var thys  = eXo.communication.chat.webui.UIRoomConfigPopupWindow;
  var DOMUtil = eXo.core.DOMUtil;
  var fieldsetNode = DOMUtil.findAncestorByTagName(srcElement, 'fieldset');
  if (fieldsetNode &&
      fieldsetNode.state != 'open') {
    for (var i=0; i<thys.fieldsetList.length; i++) {
      var tmpFieldsetNode = thys.fieldsetList[i];
      if (tmpFieldsetNode &&
          tmpFieldsetNode != fieldsetNode) {
        var fieldsetContentNode = DOMUtil.findFirstDescendantByClass(tmpFieldsetNode, 'table', 'UIFormGrid');
        if (fieldsetContentNode &&
            fieldsetContentNode.style.display != 'none') {
          thys.closingNode = tmpFieldsetNode;
          if (!thys.closingNode.maxHeight) {
            thys.closingNode.maxHeight = thys.closingNode.offsetHeight;
          }
          break;
        }
      }
    }

    var fieldsetContentNode = DOMUtil.findFirstDescendantByClass(fieldsetNode, 'table', 'UIFormGrid');
    if (fieldsetContentNode &&
        fieldsetContentNode.style.display != 'block') {
      thys.openingNode = fieldsetNode;
      fieldsetContentNode.style.display = 'block';
      if (!thys.openingNode.maxHeight) {
        thys.openingNode.maxHeight = fieldsetContentNode.offsetHeight + 6 + 
                                        DOMUtil.getStyle(fieldsetNode, 'padding-top', true) +
                                        DOMUtil.getStyle(fieldsetNode, 'padding-bottom', true) +
                                        DOMUtil.getStyle(fieldsetNode, 'margin-top', true) +
                                        DOMUtil.getStyle(fieldsetNode, 'margin-bottom', true);
      }
    }
  }
  thys.playAnimation();
  eXo.communication.chat.core.AdvancedDOMEvent.cancelEvent(event);
};

/**
 * Play animation when switch group options
 */
UIRoomConfigPopupWindow.prototype.playAnimation = function() {
  if (this.animationId) {
    window.clearInterval(this.animationId);
  }
  if (this.openingNode &&
      this.closingNode) {
    this.animationId = window.setInterval(this.switchGroupOptionsAnimate, this.ANIMATION_TIME_STEP);
  }
};

/**
 * Do real animation process when switch group options
 */
UIRoomConfigPopupWindow.prototype.switchGroupOptionsAnimate = function() {
  thys = eXo.communication.chat.webui.UIRoomConfigPopupWindow;
  var DOMUtil = eXo.core.DOMUtil;
  var closingContentNode = DOMUtil.findFirstDescendantByClass(thys.closingNode, 'table', 'UIFormGrid');
  var openingContentNode = DOMUtil.findFirstDescendantByClass(thys.openingNode, 'table', 'UIFormGrid');
  if (thys.openingNode.offsetHeight < thys.openingNode.maxHeight &&
      thys.openingNode.maxHeight > 0) {
    var percent = thys.openingNode.percent || 0;
    percent += thys.ANIMATION_SPEED;
    var openingHeight = percent * (thys.openingNode.maxHeight/100);
    thys.openingNode.style.height = openingHeight + 'px';
    openingContentNode.style.opacity = (percent / 100) + '';
    openingContentNode.style.filter = 'alpha(opacity=' + percent + ')';
    
    var closingHeight = (100 - percent) * (thys.closingNode.maxHeight/100);
    closingHeight = (closingHeight < 0) ? 0 : closingHeight;
    thys.closingNode.style.height = closingHeight + 'px';
    closingContentNode.style.opacity = ((100 - percent) / 100) + '';
    openingContentNode.style.filter = 'alpha(opacity=' + (100 - percent) + ')';

    thys.openingNode.percent = percent;
  } else {
    var openLegendNode = thys.openingNode.getElementsByTagName('legend')[0];
    //openLegendNode.innerHTML = openLegendNode.innerHTML.replace(/^Show\s/, '');
    if(thys.willOpeningLegendNode)
      openLegendNode.innerHTML = thys.willOpeningLegendNode;
    openingContentNode.style.opacity = '';
    openingContentNode.style.filter = '';
    openingContentNode.style.display = 'block';
    thys.openingNode.style.height = '';
    thys.openingNode.state = 'open';
    thys.openingNode.percent = false;
    openingContentNode.style.zIndex = '1';
    thys.openingNode = null;

    var closeLegendNode = thys.closingNode.getElementsByTagName('legend')[0];
    //closeLegendNode.innerHTML = 'Show ' + closeLegendNode.innerHTML;
    thys.willOpeningLegendNode = closeLegendNode.innerHTML;
    closeLegendNode.innerHTML = eXo.communication.chat.webui.UIMainChatWindow.ResourceBundle.chat_message_room_show_config.replace('{0}', closeLegendNode.innerHTML);
    closingContentNode.style.opacity = '';
    closingContentNode.style.filter = '';
    closingContentNode.style.display = 'none';
    if (eXo.core.Browser.browserType == 'ie') {
      thys.closingNode.style.height = '20px';
    } else {
      thys.closingNode.style.height = '0px';
    }
    thys.closingNode.state = 'close';
    thys.closingNode.percent = false;
    closingContentNode.style.zIndex = '0';
    thys.closingNode = null;

    window.clearInterval(thys.animationId);
    thys.animationId = false;
  }
};

/**
 * Update group configuration by data got from service
 *
 * @param {JSonData} serverData
 */
UIRoomConfigPopupWindow.prototype.updateRoomConfig = function(serverData) {
  window.jsconsole.info('Update room config data from service');
  if (this.rootNode.style.display != 'block') {
    this.rootNode.style.display = 'block';
    this.rootNode.focus();
  }

  if (serverData &&
      serverData.fields) {
    var data = serverData.fields;
    // Update data from server.
    var DOMUtil = eXo.core.DOMUtil;
    var formNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'form', 'ContainerForm');
    fillData: for (var i=0; i<formNode.elements.length; i++) {
      var element = formNode.elements[i];
      switch (element.nodeName.toLowerCase()) {
        case 'textarea':
          var returnData = this.getValueOf(data, element.name, '');
          if (returnData && returnData.label) {
            element.setAttribute('title', returnData.label);
          }
          if (returnData.type) {
            element.value = returnData.values[0];
          } else {
            element.value = returnData;
          }
          continue fillData;
        case 'select':
          if ((element.name == 'roomadmins' ||
              element.name == 'roomowners') &&
              !element.onchange) {
            element.onchange = this.syncPrivileges;
          }
          var returnData = this.getValueOf(data, element.name, false);
          if (returnData) {
            if (returnData && returnData.label) {
              element.setAttribute('title', returnData.label);
            }
            // Check if selectboxs are in kind of re-fill all values.
            if (element.name == 'roomadmins' ||
                element.name == 'roomowners') {
              element.innerHTML = '';
              // Create new value;
              //debugger;
              for (var j=0; j<serverData.members.length; j++) {
                var optionEl = document.createElement('option');
                var value = serverData.members[j];
                value = value.substr(0, value.indexOf('/'));
                optionEl.text = serverData.fullNames[j];
                optionEl.value = value;
                optionEl.selected = false;
                for (var k=0; k<returnData.values.length; k++) {
                  if (returnData.values[k] == value) {
                    optionEl.selected = true;
                    break;
                  }
                }
                element.options.add(optionEl);
              }
            } else { // Deselect items not in data list.
              for (var j=0; j<returnData.values.length; j++) {
                for (var k=0; k<element.options.length; k++) {
                  if (returnData.values[j] == element.options[k].value) {
                    element.options[k].selected = true;
                  }
                }
              }
            }
          }
          continue fillData;
      }
      if (element.type) {
        switch (element.type.toLowerCase()) {
          case 'checkbox':
            var returnData = this.getValueOf(data, element.name, false);
            if (returnData && returnData.label) {
              element.setAttribute('title', returnData.label);
            }
            if (returnData.type) {
              element.checked = parseInt(returnData.values[0]);
            } else {
              element.checked = returnData;
            }
            if (element.name == 'passwordprotectedroom') {
              element.onclick = this.togglePasswdField;
              this.togglePasswdField(element);
            } else if (element.name == 'allowinvites') {  // CS-2980
            		if (formNode.elements['membersonly'].checked == true) {
            			element.removeAttribute("disabled"); 
            		} else {
            			element.disabled = "true";       			
            		}            	
            } else if (element.name == 'membersonly') {
            	element.onclick = this.toggleMembersOnlyField;
              this.toggleMembersOnlyField(element);
            }
            continue fillData;
          case 'password':
          case 'text':
            var returnData = this.getValueOf(data, element.name, false);
            if (returnData && returnData.label) {
              element.setAttribute('title', returnData.label);
            }
            if (returnData.type) {
              if (returnData.values.length > 0) {
                element.value = returnData.values[0];
               } else {
                element.value = '';
               }
            } else {
              element.value = returnData;
            }
            continue fillData;
        }
      }
    }
  }
  // Sure the UIRoomConfigPopupWindow is focused
  this.UIPopupManager.focusEventFire(this);
};

/**
 * Use to synchronize privileges between 2 group admin and owner permision. To avoid user
 * in 2 group at same time.
 *
 * @param {Event} event
 */
UIRoomConfigPopupWindow.prototype.syncPrivileges = function(event) {
  event = event || window.event;
  var DOMUtil = eXo.core.DOMUtil;
  var targetNode = DOMUtil.findAncestorByTagName(this, 'form');
  var targetName = (this.name == 'roomadmins') ? 'roomowners' : 'roomadmins';
  var roomAdminsListNode = targetNode['roomadmins'];
  var roomOwnersListNode = targetNode['roomowners'];
  
  targetNode = targetNode[targetName];
  if (!targetNode ||
      (targetNode == roomAdminsListNode &&
       roomOwnersListNode.options.length == 1)) {
    roomAdminsListNode.options.selectedIndex = -1;
    return false;
  }
  for (var i=0; i<this.options.length; i++) {
    var option = this.options[i];
    if (!option.selected) {
      continue;
    }
    for (var j=0; j<targetNode.options.length; j++) {
      var targetOption = targetNode.options[j];
      if (targetOption.selected && targetOption.value == option.value ) {
        if (targetNode == roomAdminsListNode) {
          targetNode.options.selectedIndex = -1;
        }
        else this.options.selectedIndex = -1;
      }
    }
  }
};

/**
 * Toggle password field visible or not when protected room check box is checked
 *
 * @param {HTMLElement} checkboxNode
 */
UIRoomConfigPopupWindow.prototype.togglePasswdField = function(checkboxNode) {
  checkboxNode = (checkboxNode && checkboxNode.nodeName) ? checkboxNode : this;
  var DOMUtil = eXo.core.DOMUtil;
  var passwdFieldVisible = checkboxNode.checked ? '' : 'none';
  var passwdFieldContainer = DOMUtil.findAncestorByTagName(DOMUtil.findAncestorByTagName(checkboxNode, 'form')['roomsecret'], 'tr');
  if (passwdFieldContainer) {
    passwdFieldContainer.style.display = passwdFieldVisible;
  }
};

UIRoomConfigPopupWindow.prototype.toggleMembersOnlyField = function(checkboxNode) {
  checkboxNode = (checkboxNode && checkboxNode.nodeName) ? checkboxNode : this;
  var DOMUtil = eXo.core.DOMUtil;
  var allowInviteField = DOMUtil.findAncestorByTagName(checkboxNode, 'form')['allowinvites'] ;
  if (checkboxNode.checked == true) {
    allowInviteField.removeAttribute("disabled");
  } else {
  	allowInviteField.disabled = "true";
  	allowInviteField.checked = false;
  }
};

/**
 * Use to get value from a field by field name then return default value if value 
 * in the field is null
 *
 * @param {Object} data
 * @param {String} fieldName
 * @param {Object} defaultValue
 */
UIRoomConfigPopupWindow.prototype.getValueOf = function(data, fieldName, defaultValue) {
  for (var i=0; i<data.length; i++) {
    var fieldData = data[i];
    if (fieldData.variable &&
        fieldData.variable.replace(/(x-)?muc#roomconfig_/, '') == fieldName) {
      return fieldData;
    }
  }
  return defaultValue;
};

/**
 * Submit configuration data to service when ok button is pressed
 *
 * @param {Boolean} keepWindowState
 */
UIRoomConfigPopupWindow.prototype.okAction = function(keepWindowState) {
  // Collect data and convert them to JSON format
  var roomConfig = {};
  var formNode = eXo.core.DOMUtil.findFirstDescendantByClass(this.rootNode, 'form', 'ContainerForm');
  collectData: for (var i=0; i<formNode.elements.length; i++) {
    var element = formNode.elements[i];
    switch (element.nodeName.toLowerCase()) {
      case 'textarea':
        roomConfig[element.name] = element.value;
        continue collectData;
      case 'select':
        var tmpArr = [];
        for (var j=0; j<element.options.length; j++) {
          if (element.options[j].selected) {
            tmpArr.push(element.options[j].value);
          }
        }
        roomConfig[element.name] = tmpArr;
        continue collectData;
    }
    if (element.type) {
      switch (element.type.toLowerCase()) {
        case 'checkbox':
          roomConfig[element.name] = element.checked;
          continue collectData;
        case 'password':
        case 'text':
          roomConfig[element.name] = element.value;
          continue collectData;
      }
    }
  }

  this.UIMainChatWindow.jabberSendConfigRoom(this.roomName, eXo.core.JSON.stringify(roomConfig));
  this.mustSubmit = false;
  if (!keepWindowState) {
    this.setVisible(false);
  }
};

/**
 * Use to fake configuration submit
 *
 * @param {Boolean} keepWindowState
 */
UIRoomConfigPopupWindow.prototype.fakeConfigCommit = function(keepWindowState) {
  this.okAction(keepWindowState);
  eXo.communication.chat.webui.UIChatWindow.insertCustomMsg(this.UIMainChatWindow.ResourceBundle.chat_message_room_default_config_commit, this.tabId);
};

/**
 * Call when tab is closed while this component is visible to fake commit configuration to service
 * to get permission to close room
 *
 * @param {TabId} tabId
 */
UIRoomConfigPopupWindow.prototype.relateClose = function(tabId) {
  if (this.tabId.id == tabId.id) {
    this.mustSubmit = false;
    this.setVisible(false);
  }
}

/**
 * Make component visible or not
 *
 * @param {Boolean} visible
 * @param {TabId} tabId
 * @param {Boolean} mustSubmit
 */
UIRoomConfigPopupWindow.prototype.setVisible = function(visible, tabId, mustSubmit) {
  if (!this.UIMainChatWindow.userStatus ||
      this.UIMainChatWindow.userStatus == this.UIMainChatWindow.OFFLINE_STATUS) {
    return;
  }
  if (this.mustSubmit) {
    this.fakeConfigCommit(visible);
  }
  this.visible = visible;
  if (visible) {
    if (tabId &&
        tabId.targetPerson) {
      var chatRoomServiceName = this.UIMainChatWindow.serverInfo.mucServicesNames[0];
      this.tabId = tabId;
      this.roomName = this.tabId.targetPerson;
      if (this.roomName.indexOf('@' + chatRoomServiceName) != -1) {
        this.roomName = this.roomName.substr(0, this.roomName.indexOf('@' + chatRoomServiceName));
      }
      this.UIMainChatWindow.jabberGetRoomConfig(this.roomName);
      if (!this.updatedMaxHeight) {
        var DOMUtil = eXo.core.DOMUtil;
        for (var i=0; i<this.fieldsetList.length; i++) {
          var fieldsetContentNode = DOMUtil.findFirstDescendantByClass(this.fieldsetList[i], 'table', 'UIFormGrid');
          this.fieldsetList[i].maxHeight = this.fieldsetList[i].getAttribute('maxHeight');
          fieldsetContentNode.style.visibility = '';
          fieldsetContentNode.style.filter = '';
          fieldsetContentNode.style.opacity = '';
          if (this.fieldsetList[i].getAttribute('state') == 'closed') {
            fieldsetContentNode.style.display = 'none';
          }
        }
        this.updatedMaxHeight = true;
      }
    } else {
      return;
    }
    this.mustSubmit = mustSubmit;
  } else {
    this.roomName = false;
    if (this.rootNode.style.display != 'none') {
      this.rootNode.style.display = 'none';
    }
    this.mustSubmit = false;
  }
};

UIRoomConfigPopupWindow.prototype.isVisible = function() {
  return this.visible;
};

eXo.communication.chat.webui.UIRoomConfigPopupWindow = new UIRoomConfigPopupWindow();
