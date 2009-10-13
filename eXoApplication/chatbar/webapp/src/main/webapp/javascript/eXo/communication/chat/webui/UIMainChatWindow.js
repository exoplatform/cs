/**
 * @author Uoc Nguyen
 *  email uoc.nguyen@exoplatform.com
 */

// --- Ajax handle --
function ChatAjaxHandler(action) {
  this.action = action;
}

/**
 * Default ajax loading callback handle
 *
 * @param {XMLHttpRequest} requestObj
 */
ChatAjaxHandler.prototype.onLoading = function(requestObj) {
  var UIMainChatWindow = eXo.communication.chatbar.webui.UIMainChatWindow;
  if (!UIMainChatWindow) return;
  window.jsconsole.info('[' + this.handler.action + '] ' + UIMainChatWindow.LOADING_STATE);
  UIMainChatWindow.update(UIMainChatWindow.LOADING_STATE, requestObj, this.handler.action);
};

/**
 * Default ajax success callback handle
 *
 * @param {XMLHttpRequest} requestObj
 */
ChatAjaxHandler.prototype.onSuccess = function(requestObj) {
  var UIMainChatWindow = eXo.communication.chatbar.webui.UIMainChatWindow;
  if (!UIMainChatWindow) return;
  window.jsconsole.info('[' + this.handler.action + '] ' + UIMainChatWindow.SUCCESS_STATE);
  UIMainChatWindow.update(UIMainChatWindow.SUCCESS_STATE, requestObj, this.handler.action);
};

/**
 * Default ajax error callback handle
 *
 * @param {XMLHttpRequest} requestObj
 */
ChatAjaxHandler.prototype.onError = function(requestObj) {
  var UIMainChatWindow = eXo.communication.chatbar.webui.UIMainChatWindow;
  if (!UIMainChatWindow) return;
  //if(requestObj.status == 401) alert(requestObj.responseText);
  window.jsconsole.info('[' + this.handler.action + '] ' + UIMainChatWindow.ERROR_STATE);
  UIMainChatWindow.update(UIMainChatWindow.ERROR_STATE, requestObj, this.handler.action);
};

/**
 * Default ajax timeout callback handle
 *
 * @param {XMLHttpRequest} requestObj
 */
ChatAjaxHandler.prototype.onTimeout = function(requestObj) {
  var UIMainChatWindow = eXo.communication.chatbar.webui.UIMainChatWindow;
  if (!UIMainChatWindow) return;
  window.jsconsole.info('[' + this.handler.action + '] ' + UIMainChatWindow.TIMEOUT_STATE);
  UIMainChatWindow.update(UIMainChatWindow.TIMEOUT_STATE, requestObj, this.handler.action);
};


/**
 * This object is create to use for manage all action in chat application suchas: call service communicate method,
 * init all sub-component, manage UI, process and manage all thread at once.
 */
function UIMainChatWindow() {
  // XMPP action.
  this.LOGIN_ACTION                       = 'Login';
  this.LOGOUT_ACTION                      = 'Logout';
  this.SEND_STATUS_ACTION                 = 'Send status';
  this.GET_MESSAGE_HISTORY_ACTION         = 'Get message history';
  this.SEND_SUBSCRIPTION_ACTION           = 'Send subscription';
  this.ASK_4_SUBSCRIPTION_ACTION          = 'Ask for subscription';
  this.ADD_USER_ACTION                    = 'Add user';
  this.SEND_MESSAGE_ACTION                = 'Send message';
  this.GET_SUBSCRIPTION_REQUESTS_ACTION   = 'Get subscription requests';
  this.CLEAN_BUDDY_LIST_ACTION            = 'Clean buddy list';
  this.REMOVE_USER_ACTION                 = 'Remove user';
  this.UNSUBSCRIPT_BUDDY_ACTION           = 'Unsubscript buddy';
  this.ORG_GET_ALL_CONTACT_ACTION         = 'Org Service get all contact';
  this.DENIAL_SEND_FILE_ACTION            = 'Denial file exchange';
  this.ACEPT_SEND_FILE_ACTION             = 'Acept file exchange';
  this.ORG_FUZZY_SEARCH_USER_ACTION       = 'Org Service fuzzy contact search';
  this.ORG_COUNT_USER_ACTION              = 'Org Service count user';
  this.CREATE_ROOM_ACTION                 = 'Create room';
  this.CREATE_CONVERSATION_ACTION         = 'Create conversation';
  this.CONFIG_ROOM_ACTION                 = 'Config room';
  this.GET_ROOM_CONFIG_ACTION             = 'Get room\'s config';
  this.GET_ROOM_INFO_ACTION               = 'Get room\'s information';
  this.GET_ROOM_LIST_ACTION               = 'Get room list';
  this.GET_JOINED_ROOM_LIST_ACTION        = 'Get joined room list';
  this.INVITE_JOIN_ROOM_ACTION            = 'Invite join room';
  this.DECLINE_JOIN_ROOM_ACTION           = 'Decline join room';
  this.JOIN_TO_ROOM_ACTION                = 'Join to room';
  this.LEAVE_FROM_ROOM_ACTION             = 'Leave from room';

  // MUC event action defined here.
  this.MUC_ACTION_CREATED_ROOM            = 'created';
  this.MUC_ACTION_INVITE_ROOM             = 'invite';
  this.MUC_ACTION_JOIN_ROOM               = 'joined';
  this.MUC_ACTION_LEFT_ROOM               = 'left';

  // Ajax state.
  this.LOADING_STATE                      = 'Loading';
  this.SUCCESS_STATE                      = 'Success';
  this.ERROR_STATE                        = 'Error';
  this.TIMEOUT_STATE                      = 'Timeout';

  // CSS class.
  this.LOADING_STATE_CLASS                = 'LoadingIcon';
  this.ONLINE_STATE_CLASS                 = 'OnlineIcon';
  this.OFFLINE_STATE_CLASS                = 'OfflineIcon';

  // User mode: loged in or not.
  this.LOGEDIN_MODE                       = 'Loged in';
  this.NOT_LOGEDIN_MODE                   = 'Not loged in';

  // XMPP user status.
  this.ONLINE_STATUS                      = "Available";
  this.OFFLINE_STATUS                     = "Unavailable";
  this.FREE_TO_CHAT_STATUS                = "Free to chat";
  this.DO_NOT_DISTURB_STATUS              = "Do not disturb";
  this.AWAY_STATUS                        = "Away";
  this.EXTEND_AWAY_STATUS                 = "Extend away";

  // Maximum connection to try after request is error in case false to connect to service.
  this.MAX_CONNECTION_TRY = 5;
  this.CHECK_EVENT_TIMEOUT = 1*1000;

  // The default value of time to check is Chat application alive: 
  //    Check is user still in Chat application to avoid missing 
  //    control after page switch by ajax.
  this.DEFAULT_CHECK_ALIVE = 3*1000;

  // Minimum size of chat window in WebOS page.
  this.MIN_WIDTH = 426;
  this.MIN_HEIGHT = 448;

  this.ChatSessionHandler = eXo.communication.chatbar.webui.ChatSessionHandler;
  this.XMPPCommunicator = eXo.communication.chatbar.core.XMPPCommunicator;

  this.checkAliveId = false;
  this.userToken = false;
  this.userStatus = false;
  this.lastStatusSent = false;
  this.activeAction = false;
  this.guiMode = false;
  this.userNames = new Array();
  this.timeoutCount = 0;
  this.errorCount = 0;
  // Using debugLevel <= 0 to disable js logger. Change it from 1->6 to enable debug level.
  this.debugLevel = 0;
  //this.debugLevel = 9;
  this.serverInfo = false;
  this.buddyItemActionStack = false;
  this.joinedRooms = [];
  this.newestRoomName = '';// use only for CS-2999
  this.serverDataStack = false;
  this.actionHandler = {};
  this.initialized = false;
  this.sessionKeeperId = false;
  // The timeout to request resource to avoid portal's session timeout.
  this.PORTAL_SESSION_KEEPER_TIME_STEP = 5*1000*60;
};

/**
 * Initialize function
 *
 * @param {HTMLElement|String} rootNode
 * @param {String} userToken
 * @param {String} userName
 */
UIMainChatWindow.prototype.init = function(rootNode, userToken, userName) {
  window.jsconsole.debugLevel = this.debugLevel;
  if (this.initialized) {
    this.destroy();
  }
  this.rootNode = (typeof(rootNode) == 'string') ? document.getElementById(rootNode) : rootNode;
  var DOMUtil = eXo.core.DOMUtil;
  this.rootNode                   = DOMUtil.findDescendantById(this.rootNode, 'UIChatBarPortlet');

  this.AdvancedDOMEvent           = eXo.communication.chatbar.core.AdvancedDOMEvent;
  this.XMPPCommunicator           = eXo.communication.chatbar.core.XMPPCommunicator;
  this.ChatSessionHandler         = eXo.communication.chatbar.webui.ChatSessionHandler;
  this.UIAddContactPopupWindow    = eXo.communication.chatbar.webui.UIAddContactPopupWindow;
  this.UIChatWindow               = eXo.communication.chatbar.webui.UIChatWindow;
  this.UICreateNewRoomPopupWindow = eXo.communication.chatbar.webui.UICreateNewRoomPopupWindow;
  this.UIRoomConfigPopupWindow    = eXo.communication.chatbar.webui.UIRoomConfigPopupWindow;
  this.UIJoinRoomPopupWindow      = eXo.communication.chatbar.webui.UIJoinRoomPopupWindow;
  this.UIChatResize               = eXo.communication.chatbar.webui.UIChatResize;
  this.UISlideAlert               = eXo.communication.chatbar.webui.UISlideAlert;

  // ----- Specified to used with desktop page----
  this.chatWindowsContainerNode = document.getElementById('UIPageDesktop');
  this.isWebOS = this.chatWindowsContainerNode ? true : false;
  if (this.chatWindowsContainerNode) {
    var portletFragment = DOMUtil.findAncestorById(this.rootNode, 'PORTLET-FRAGMENT') || DOMUtil.findAncestorByClass(this.rootNode, 'PORTLET-FRAGMENT');
    portletFragment.className = portletFragment.className.replace('UIApplication', 'UIApplication1');
    portletFragment.style.height = this.MIN_HEIGHT + 'px';
    portletFragment.style.width = '100%';
    //var nodeList = DOMUtil.findDescendantsByClass(portletFragment, 'div', /(UIWindow|UIPopupWindow)/);
    //for (var i=0; i<nodeList.length; i++) {
      //if (nodeList[i] == this.rootNode) {
        //continue;
      //}
      //this.chatWindowsContainerNode.appendChild(nodeList[i].cloneNode(true));
      //DOMUtil.removeElement(nodeList[i]);
    //}
    this.rootNode = DOMUtil.findAncestorByClass(this.rootNode, 'UIWindow');
    this.rootNode.style.width = this.MIN_WIDTH + 'px';
    this.rootNode.style.height = this.MIN_HEIGHT + 'px';
    this.rootNode.setAttribute('minwidth', this.MIN_WIDTH + '');
    this.rootNode.setAttribute('minheight', this.MIN_HEIGHT + '');
    var bottomDecoratorLeftNode = DOMUtil.findFirstChildByClass(this.rootNode, 'div', 'BottomDecoratorLeft');
    var resizeAreaNode = DOMUtil.findFirstDescendantByClass(bottomDecoratorLeftNode, 'div', 'ResizeArea');
    if (resizeAreaNode) {
      this.UIChatResize.register(resizeAreaNode, null, true);
    }
  } else {
    this.chatWindowsContainerNode = this.rootNode.parentNode;
  }
  // ----- End ---------------------

  this.LocalTemplateEngine = eXo.communication.chatbar.core.LocalTemplateEngine;
  this.addContactIconNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', 'AddContactIcon');
  this.statusIconNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', 'StatusIcon');
  this.statusNode = DOMUtil.findAncestorByClass(this.statusIconNode, 'StatusArea');
  this.roomAreaNode = DOMUtil.findFirstDescendantByClass( this.rootNode, 'div', 'RoomArea');
  //this.statusbarNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', 'Information');
  this.loginFormNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'fieldset', 'LoginForm');
  this.buddyListNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', 'BuddyList');
  this.joinedRoomListNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', 'RoomData');
  this.buddyItemActionMenuNode = DOMUtil.findFirstDescendantByClass(this.chatWindowsContainerNode, 'div', 'BuddyItemActionMenu');
  //this.buddyItemActionMenuNode = DOMUtil.findFirstDescendantByClass(this.rootNode, 'div', 'BuddyItemActionMenu');
  this.ContactAreaNode = DOMUtil.findFirstDescendantByClass(this.chatWindowsContainerNode, 'div', 'ContactArea');
  if(this.ContactAreaNode)
  	this.ContactAreaNode = DOMUtil.findFirstDescendantByClass(this.ContactAreaNode,"div","UIRightClickPopupMenu");
  
  // Notification template
  //this.notificationNode = DOMUtil.findFirstDescendantByClass(this.chatWindowsContainerNode, 'div', 'NotificationArea');
  // Windows and popups.
  this.chatPopupNode = DOMUtil.findFirstDescendantByClass(this.chatWindowsContainerNode, 'div', 'ChatPopup');
  this.addContactPopupNode = DOMUtil.findFirstDescendantByClass(this.chatWindowsContainerNode, 'div', 'AddContactPopup');
  this.createNewRoomPopupNode = DOMUtil.findFirstDescendantByClass(this.chatWindowsContainerNode, 'div', 'CreateNewRoomPopup');
  this.roomConfigPopupNode = DOMUtil.findFirstDescendantByClass(this.chatWindowsContainerNode, 'div', 'RoomConfigPopup');
  this.joinRoomPopupNode = DOMUtil.findFirstDescendantByClass(this.chatWindowsContainerNode, 'div', 'JoinRoomPopup');

  this.UIChatWindow.init(this.chatPopupNode, this);
  this.UIAddContactPopupWindow.init(this.addContactPopupNode, this);
  this.UICreateNewRoomPopupWindow.init(this.createNewRoomPopupNode, this);
  this.UIRoomConfigPopupWindow.init(this.roomConfigPopupNode, this);
  this.UIJoinRoomPopupWindow.init(this.joinRoomPopupNode, this);
  
  //this.UISlideAlert.init(this, this.notificationNode);

  this.UIPopupManager = eXo.communication.chatbar.webui.UIPopupManager;
  this.UIPopupManager.init();
  this.UIPopupManager.addItem(this.UIChatWindow);
  this.UIPopupManager.addItem(this.UIAddContactPopupWindow);
  this.UIPopupManager.addItem(this.UICreateNewRoomPopupWindow);
  this.UIPopupManager.addItem(this.UIRoomConfigPopupWindow);
  this.UIPopupManager.addItem(this.UIJoinRoomPopupWindow);

  this.userStatus = false;
  this.lastStatusSent = false;
  this.activeAction = false;
  this.guiMode = false;
  this.userNames = new Array();
  this.isGetMsgInProcess = false;
  this.serverInfo = false;
  this.userToken = userToken;
  this.userName = userName;
  this.buddyItemActionStack = {};
  this.serverDataStack = {};
  var component = eXo.communication.chatbar.webui.component;
  this.buddyListControlObj =
    new component.BuddyListControl(this.buddyListNode, this.buddyItemActionCallbackWrapper, this);

  // Init cometd service on startup
  this.initCometd();
  this.initialized = true;
};

/**
 * Check the chat application is alive. Using for case portal using ajax to load another application
 * which overwritten the chat application.
 */
UIMainChatWindow.prototype.isChatAlive = function() {
  //TODO CS-3105 logout problem
  /*if (!document.getElementById('UIChatBarPortlet')) {
    eXo.communication.chatbar.webui.UIMainChatWindow.jabberLogout();
  }*/
};

/**
 * Using ajax request to keep active status with portal session to avoid portal session timeout.
 * TODO: remove/maintain this method because it is not really affect to portal session.
UIMainChatWindow.prototype.sessionKeeper = function() {
  eXo.require("eXo.communication.chatbar.core.PortalSessionKeeper", "/chat/javascript/");
  eXo.communication.chatbar.core.PortalSessionKeeper = null;
};

/**
 * Cometd connection initialize method.
 */
UIMainChatWindow.prototype.initCometd = function() {
  var Cometd = eXo.cs.CSCometd;
  if (!Cometd.isConnected()) {
    Cometd.exoId = this.userName;
    Cometd.exoToken = this.userToken;
    Cometd.init();
  }
};

/**
 * Use for login from another object like call from event handle method or in window context.
 */
UIMainChatWindow.prototype.loginWrapper = function() {
  if (!eXo.cs.CSCometd.isConnected()) {
    window.jsconsole.warn('wait for cometd connection ready.');
    return;
  }
  window.jsconsole.warn('Connection is ready, try to login now.');
  var thys = eXo.communication.chatbar.webui.UIMainChatWindow;
  thys.jabberLogin(thys.userNames[thys.XMPPCommunicator.TRANSPORT_XMPP]);
};

/**
 * Use to un subscribe cometd topics for chat application
 */
UIMainChatWindow.prototype.unsubscribeCometdTopics = function() {
  var Cometd = eXo.cs.CSCometd;
  if (Cometd.isConnected()) {
    window.jsconsole.warn('Cometd is not connected');
  }
  Cometd.unsubscribe('/eXo/Application/Chat/message');

  Cometd.unsubscribe('/eXo/Application/Chat/groupchat');

  Cometd.unsubscribe('/eXo/Application/Chat/presence');

  Cometd.unsubscribe('/eXo/Application/Chat/roster');

  Cometd.unsubscribe('/eXo/Application/Chat/subscription');

  Cometd.unsubscribe('/eXo/Application/Chat/FileExchange');
}

/**
 * Use to subscribe cometd topics for chat application
 */
UIMainChatWindow.prototype.subscribeCometdTopics = function() {
  var Cometd = eXo.cs.CSCometd;
  if (!Cometd.isConnected()) {
    window.jsconsole.warn('Cometd is not connected');
    return;
  }
  Cometd.subscribe('/eXo/Application/Chat/message', function(eventObj) {
    eXo.communication.chatbar.webui.UIMainChatWindow.messageListener(eventObj);
  });

  Cometd.subscribe('/eXo/Application/Chat/groupchat', function(eventObj) {
    eXo.communication.chatbar.webui.UIMainChatWindow.groupChatListener(eventObj);
  });

  Cometd.subscribe('/eXo/Application/Chat/presence', function(eventObj) {
    eXo.communication.chatbar.webui.UIMainChatWindow.presenceListener(eventObj);
  });

  Cometd.subscribe('/eXo/Application/Chat/roster', function(eventObj) {
    eXo.communication.chatbar.webui.UIMainChatWindow.rosterListener(eventObj);
  });

  Cometd.subscribe('/eXo/Application/Chat/subscription', function(eventObj) {
    eXo.communication.chatbar.webui.UIMainChatWindow.subscriptionListener(eventObj);
  });

  Cometd.subscribe('/eXo/Application/Chat/FileExchange', function(eventObj) {
    eXo.communication.chatbar.webui.UIMainChatWindow.fileExchangeListener(eventObj);
  });
};

/**
 * Use as finaly task when logout from chat
 */
UIMainChatWindow.prototype.destroy = function() {
  this.unsubscribeCometdTopics(); 
  this.initialized = false;
};

/**
 * Use to make non-ajax request to logout from chat application when user close browser window without
 * logout. This method can avoid 90% problem with chat session keep after user close chat application
 * in no-normal way.
 */
UIMainChatWindow.prototype.destroyAll = function() {
  if (eXo.cs.CSCometd.isConnected()) {
	  eXo.cs.CSCometd.disconnect();
  }
  var thys = eXo.communication.chatbar.webui.UIMainChatWindow;
  var logoutUrl = thys.XMPPCommunicator.SERVICE_URL +
                    '/' + thys.XMPPCommunicator.TRANSPORT_XMPP +
                    '/logout/' + thys.userNames[thys.XMPPCommunicator.TRANSPORT_XMPP];
  var iframeNode = document.createElement('iframe');
  window.document.body.appendChild(iframeNode);
  iframeNode.src = logoutUrl;
  try {
    if (eXo.cs.CSCometd.isConnected()) {
    	eXo.cs.CSCometd.disconnect();
    }
    thys.jabberLogout();
  } catch (e) {}
};

/**
 * Strip out any no-need information from user
 *
 * @param {String} userNameFullStr
 */
UIMainChatWindow.prototype.getUserName = function(userNameFullStr) {
  if (userNameFullStr.indexOf('/') != -1) {
    return (userNameFullStr.substring(0, userNameFullStr.indexOf('/')));
  } else {
    return userNameFullStr;
  }
};

/**
 * Return a common ajax handler use for most ajax request in chat application
 */
UIMainChatWindow.prototype.getAjaxHandler = function() {
  return (new ChatAjaxHandler(this.activeAction));
};

/**
 * Return time of error trying counter using for reset and avoid flood server in case connection error or
 * service error.
 */
UIMainChatWindow.prototype.getTryCount = function() {
  return (this.errorCount + this.timeoutCount);
};

/**
 * Use to reset all trying counter when a new connection has done successfully.
 */
UIMainChatWindow.prototype.resetAllTryCount = function() {
  this.errorCount = 0;
  this.timeoutCount = 0;
};

// --- Ajax callback update --
/**
 *  Call from ajax handle callback function when ajax request is ready.
 *  In this method all data is raw data such as: json is in string type and xml is in string type also.
 *
 * @param {Object} state
 * @param {Object} requestObj
 */
UIMainChatWindow.prototype.update = function(state, requestObj, action) {
  // Update icon and statusbar message
  switch (state) {
    case this.LOADING_STATE :
      //this.statusbarNode.innerHTML = this.LOADING_STATE + '...';
      if (this.activeActionLoading == action) {
        this.errorCount ++;
      }
      this.activeActionLoading = action;
      break;

    case this.SUCCESS_STATE :
      var eventId = 'restEvent_' + (new Date()).getTime();
      this.resetAllTryCount();
      //this.statusbarNode.innerHTML = this.SUCCESS_STATE;
      if (requestObj.responseText) {
        try {
          this.serverDataStack[eventId] = eXo.core.JSON.parse(requestObj.responseText);
          window.jsconsole.dir(this.serverDataStack[eventId]);
        } catch (e) {
          // TODO
        }
      }
      var successAction = action;
      window.setTimeout(function() {
        eXo.communication.chatbar.webui.UIMainChatWindow.processSuccessAction(successAction, eventId);
      }, 1);
      this.activeAction = false;
      break;

    case this.ERROR_STATE :
      this.errorCount ++;
      //this.statusbarNode.innerHTML = this.ERROR_STATE;
      this.processErrorAction(requestObj, action);
      this.activeAction = false;
      break;

    case this.TIMEOUT_STATE :
      this.timeoutCount ++;
      //this.statusbarNode.innerHTML = this.TIMEOUT_STATE;
      this.processTimeoutAction(requestObj, action);
      this.activeAction = false;
      break;

    default :
      break;
  }
  if (state != this.LOADING_STATE) {
    this.activeActionLoading = false;
  }
  if (this.getTryCount() == this.MAX_CONNECTION_TRY) {
    //this.statusbarNode.innerHTML = 'Server down or error! try to reconnect later';
  }
};

/**
 * Process all successfully ajax request action.
 */

UIMainChatWindow.prototype.updateJoinedRoomList = function() {
		var MAX_ROOM_TITLE_LEN = 25;
		var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
		userName = userName+'@';
		this.joinedRoomListNode.innerHTML = '' ;
	  if (this.joinedRooms ) {
		  var roomList = this.joinedRooms ;
		  for (var i=0; i<roomList.length; i++) {
		  		var roomOccupantsList = roomList[i].occupants;
		  		var isThisUserJoined = false;
		  		for(var j=0; j<roomOccupantsList.length; j++){
		  			var occupant = roomOccupantsList[j];
		  			if(occupant.jid.indexOf(userName)==0){
		  				isThisUserJoined = true;
		  				break;
		  			}
		  		}
			    var roomInfo = roomList[i].roomInfo;
			    var roomNode = document.createElement('div');		
			    if(isThisUserJoined)    
			    	roomNode.className = 'TextMemberRoom HightLightTextMemberRoom' ;
			    else
			    	roomNode.className = 'TextMemberRoom' ;
			    var roomLink = document.createElement('div');
			    var roomName = roomInfo.room.substr(0, roomInfo.room.indexOf('@'));
			    if(roomName.length > MAX_ROOM_TITLE_LEN)
			    	roomLink.innerHTML = roomName.substr(0, MAX_ROOM_TITLE_LEN-3)+ '...';
			    else			    	
			    	roomLink.innerHTML = roomName ;
			    roomLink.setAttribute('roomId', roomInfo.room) ;
			    roomLink.onclick = function(event){
			    	eXo.communication.chatbar.webui.UIJoinRoomPopupWindow.joinSelectedRoomByIdAction(event);
			    };
			    roomNode.appendChild(roomLink);
			    this.joinedRoomListNode.appendChild(roomNode);			    
		  }  
	  }
}

UIMainChatWindow.prototype.processSuccessAction = function(action, eventId) {
  var serverData = this.serverDataStack[eventId];
  switch (action) {
    case this.LOGIN_ACTION:
      this.postChangeStatus(this.ONLINE_STATUS, eventId);
      this.checkAliveId = window.setInterval(this.isChatAlive, this.DEFAULT_CHECK_ALIVE);
      break;

    case this.CREATE_ROOM_ACTION:
		if(!serverData){
    		var uiTabControlObj = this.UIChatWindow.createNewTab(this.newestRoomName+'@'+this.serverInfo.mucServicesNames, true);
    		uiTabControlObj.roomConfigured = true;
    	}
      break;

    case this.GET_MESSAGE_HISTORY_ACTION:
      this.UIChatWindow.updateMessageHistory(serverData.messageList);
      break;

    case this.GET_ROOM_CONFIG_ACTION:
      window.jsconsole.warn('==================================================');
      window.jsconsole.debug('Room\' configuration: ', serverData);
      window.jsconsole.warn('==================================================');
      this.UIRoomConfigPopupWindow.updateRoomConfig(serverData);
      break;

    case this.GET_ROOM_INFO_ACTION:
      this.UIChatWindow.roomInfoEventFired(serverData);
      break;

    case this.SEND_STATUS_ACTION:
      this.postChangeStatus(this.lastStatusSent, eventId);
      break;

    case this.ORG_FUZZY_SEARCH_USER_ACTION:
    case this.ORG_GET_ALL_CONTACT_ACTION:
      if (serverData.users) {
        this.UIAddContactPopupWindow.updateContactList(serverData);
      }
      break;

    case this.GET_ROOM_LIST_ACTION:
      if (serverData)
        this.UIJoinRoomPopupWindow.updateRoomList(serverData);
      break;

    case this.GET_JOINED_ROOM_LIST_ACTION:
      this.joinedRooms = serverData && serverData.joinedRooms ? serverData.joinedRooms : [];
      this.updateJoinedRoomList();
      break;

		case this.JOIN_TO_ROOM_ACTION:
			this.jabberGetJoinedRoomList();
			break;

		case this.LEAVE_FROM_ROOM_ACTION:
			eXo.communication.chatbar.webui.UIMainChatWindow.jabberGetJoinedRoomList();
			break;

    case this.GET_SUBSCRIPTION_REQUESTS_ACTION:
      break;

    case this.SEND_SUBSCRIPTION_ACTION:
      break;

    case this.UNSUBSCRIPT_BUDDY_ACTION:
      break;

    case this.REMOVE_USER_ACTION:
      break;

    case this.SEND_MESSAGE_ACTION:
      break;

    case this.LOGOUT_ACTION:
      this.postChangeStatus(this.OFFLINE_STATUS);
      this.UIChatWindow.destroySession();
      break;

    default:
      break;
  }
  if (this.serverDataStack[eventId]) {
    this.serverDataStack[eventId] = null;
  }
};

/**
 * Process all timeout ajax request action.
 */
UIMainChatWindow.prototype.processTimeoutAction = function(requestObj, action) {
};

/**
 * Process all error ajax request action.
 */
UIMainChatWindow.prototype.processErrorAction = function(requestObj, action){
  // Fix: Automatic logout when open multiple window/tab then close once of them.
  if (requestObj.status == 400 &&
      requestObj.responseText == 'XMPPSesion is null!') {
    window.jsconsole.warn('You are not login to chat.');
    window.alert('You are not login to Chat/Chat session has closed by another window/tab.\n Please logout and login again.');
    return;
  }
  switch (action) {
    case this.JOIN_TO_ROOM_ACTION:
    	switch(requestObj.status) {
    		case 401:
    			window.alert('Your secret key to join room is not valid.\nPlease try again later.');
    			break;
    		case 403:
    		case 404:
    			window.alert('You are trying to join a room which is not unlocked yet.\nPlease try again later !');
    			break;
    		case 407:
    		  window.alert('You are trying to join a private room in which you are not a member!\nPlease try again later.');
    			break;
    		case 409:
    		default:
    			break;
    	}
      break;
    //case this.LOGIN_ACTION:
    //case this.LOGOUT_ACTION:
    //case this.SEND_STATUS_ACTION:
    //case this.GET_MESSAGE_HISTORY_ACTION:
    //case this.SEND_SUBSCRIPTION_ACTION:
    //case this.ASK_4_SUBSCRIPTION_ACTION:
    //case this.ADD_USER_ACTION:
    //case this.SEND_MESSAGE_ACTION:
    //case this.GET_SUBSCRIPTION_REQUESTS_ACTION:
    //case this.CLEAN_BUDDY_LIST_ACTION:
    //case this.REMOVE_USER_ACTION:
    //case this.UNSUBSCRIPT_BUDDY_ACTION:
    //case this.ORG_GET_ALL_CONTACT_ACTION:
    //case this.DENIAL_SEND_FILE_ACTION:
    //case this.ORG_FUZZY_SEARCH_USER_ACTION:
    //case this.ORG_COUNT_USER_ACTION:
    case this.CREATE_ROOM_ACTION:
    	switch(requestObj.status) {
    		case 401:
    			this.jabberJoinToRoom(this.newestRoomName,true);
    			break;
    		case 403:
    		case 404:
    			window.alert('You are trying to join a room which is not unlocked yet.\nPlease try again later !');
    			break;
    		case 407:
    			window.alert('You are trying to join a private room in which you are not a member!\nPlease try again later.');
    			break;
    		case 409:
    		default:
    			break;
    	}
    	break;
    case this.CREATE_CONVERSATION_ACTION:
    case this.CONFIG_ROOM_ACTION:
    case this.GET_ROOM_CONFIG_ACTION:
    	window.alert(requestObj.responseText);
    	break;
    //case this.GET_ROOM_INFO_ACTION:
    case this.GET_ROOM_LIST_ACTION:
    case this.GET_JOINED_ROOM_LIST_ACTION:
    	this.joinedRooms = [];
    	this.updateJoinedRoomList();
    	break;
    case this.INVITE_JOIN_ROOM_ACTION:
    case this.DECLINE_JOIN_ROOM_ACTION:
    case this.LEAVE_FROM_ROOM_ACTION:
    case this.ACEPT_SEND_FILE_ACTION:
      if (requestObj.responseText) {
        window.alert('Service message: ' + requestObj.responseText);        
      }
      break;

    default:
      break;
  }
};

// Listeners for cometd connection

/**
 * A Cometd listener for group chat.
 * All cometd notify about group chat will be call this function.
 */
UIMainChatWindow.prototype.groupChatListener = function(eventObj) {
  var eventId = 'groupChatCometdEvent_' + (new Date()).getTime();
  this.serverDataStack[eventId] = eXo.core.JSON.parse(eventObj.data);
  window.setTimeout(function() {
      eXo.communication.chatbar.webui.UIMainChatWindow.processGroupChat(eventId);
  }, 1);
};

/**
 * A Cometd listener for roster.
 * All cometd notify about roster will be call this function.
 */
UIMainChatWindow.prototype.rosterListener = function(eventObj) {
  var eventId = 'rosterCometdEvent_' + (new Date()).getTime();
  this.serverDataStack[eventId] = eXo.core.JSON.parse(eventObj.data);
  window.setTimeout(function() {
      eXo.communication.chatbar.webui.UIMainChatWindow.processRoster(eventId);
  }, 1);
};

/**
 * A Cometd listener for presence.
 * All cometd notify about presence will be call this function.
 */
UIMainChatWindow.prototype.presenceListener = function(eventObj) {
  var eventId = 'presenceCometdEvent_' + (new Date()).getTime();
  this.serverDataStack[eventId] = eXo.core.JSON.parse(eventObj.data);
  window.setTimeout(function() {
      eXo.communication.chatbar.webui.UIMainChatWindow.processPresences(eventId);
  }, 1);
};

/**
 * A Cometd listener for subscription
 * All cometd notify about subscription will be call this function.
 */
UIMainChatWindow.prototype.subscriptionListener = function(eventObj) {
  var eventId = 'subscriptionCometdEvent_' + (new Date()).getTime();
  this.serverDataStack[eventId] = eXo.core.JSON.parse(eventObj.data);
  window.setTimeout(function() {
      eXo.communication.chatbar.webui.UIMainChatWindow.processSubscriptions(eventId);
  }, 1);
};

/**
 * A Cometd listener for file exchange.
 * All cometd notify about file exchange will be call this function.
 */
UIMainChatWindow.prototype.fileExchangeListener = function(eventObj) {
	var eventId = 'fileExchangeCometdEvent_' + (new Date()).getTime();
  this.serverDataStack[eventId] = eXo.core.JSON.parse(eventObj.data);
  window.setTimeout(function() {
      eXo.communication.chatbar.webui.UIMainChatWindow.processFileExchange(eventId);
  }, 1);
};

/**
 * A Cometd listener for message.
 * All cometd notify about message will be call this function.
 */
UIMainChatWindow.prototype.messageListener = function(eventObj) {
  var eventId = 'messageCometdEvent_' + (new Date()).getTime();
  this.serverDataStack[eventId] = eXo.core.JSON.parse(eventObj.data);
  window.setTimeout(function() {
      eXo.communication.chatbar.webui.UIMainChatWindow.processMessages(eventId);
  }, 1);
};

// ---+--
/**
 * Process message event come from Cometd notification
 */
UIMainChatWindow.prototype.processMessages = function(eventId) {
  var serverData = this.serverDataStack[eventId];
  if (serverData &&
      serverData.messages) {
    window.jsconsole.debug('processMessages: id:= ' + eventId, serverData);
    var messages = serverData.messages;
    this.displayMessages(messages);
  }
  this.serverDataStack[eventId] = null;
};

/**
 * Process roster update event come from Cometd notification
 */
UIMainChatWindow.prototype.processRoster = function(eventId) {
  var roster = false;
  var serverData = this.serverDataStack[eventId];
  window.jsconsole.debug('processRoster: id:= ' + eventId, serverData);
  if (!serverData) {
    return;
  }
  roster = serverData.roster;
  this.buddyListControlObj.build(roster);
  this.serverDataStack[eventId] = null;
};

/**
 * Process presences changes come from Cometd notification
 */
UIMainChatWindow.prototype.processPresences = function(eventId) {
  var presences = false;
  var serverData = this.serverDataStack[eventId];
  window.jsconsole.debug('processPresences: id:= ' + eventId, serverData);
  if (!serverData) {
    return;
  }
  presences = serverData.presences;

  // Split to 2 presence list to update.
  var mainPresences = [];
  var roomPresences = [];
  var mucServiceName = this.serverInfo.mucServicesNames[0];
  for (var i=0; i<presences.length; i++) {
    var presence = presences[i];
    presenceUser = presence.from;
    presenceUser = presenceUser.substr(presenceUser.indexOf('/'), presenceUser.length - 1);
    if (presenceUser == this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP]) {
      continue;
    }
    if (presence.from.indexOf('@' + mucServiceName + '/') != -1) {
      roomPresences.push(presence);
    } else {
      mainPresences.push(presence);
    }
  }
  if (roomPresences.length > 0) {
    this.UIChatWindow.updateRoster(roomPresences);
  }
  if (mainPresences.length > 0) {
    this.buddyListControlObj.update(mainPresences);
    this.UIChatWindow.updatePresence(roomPresences);
  }
  this.serverDataStack[eventId] = null;
};

/**
 * Process subscribe event come from Cometd notification
 */
UIMainChatWindow.prototype.processSubscriptions = function(eventId) {
  var serverData = this.serverDataStack[eventId];
  window.jsconsole.debug('Subscription event: id:= ' + eventId, serverData);
  if (!serverData) {
    this.serverDataStack[eventId] = null;
  }
  if (serverData.subscriptions) {
    var subscriptions = serverData.subscriptions;

    for (var i=0; i<subscriptions.length; i++) {
      var subscription = subscriptions[i];
      switch (subscription.type) {
        case 'subscribed':
          break;
        case 'unsubscribed':
        	denyUser = subscription.from;
        	denyUser = denyUser.substring(0, denyUser.indexOf('@'));
        	//window.alert(denyUser + ' has denied your request to add them to your contact list.');
        	this.jabberRemoveUser(denyUser);
          break;
        case 'subscribe':
          var requestUser = subscription.from;
          requestUser = requestUser.substring(0, requestUser.indexOf('@'));
          if (window.confirm('Do you want to allow [' + requestUser + '] to see your status\n and add him/her to your contact list?')) {
            this.jabberSendSubscription(requestUser);
            this.jabberAddUser(requestUser);
          } else {
            this.jabberUnsubscriptUser(requestUser);
          }
          break;
        case 'unsubscribe':
        	denyUser = subscription.from;
        	denyUser = denyUser.substring(0, denyUser.indexOf('@'));
        	//window.alert(denyUser + ' has denied your request to add them to your contact list.');
        	this.jabberRemoveUser(denyUser);
        	break;
      }
    }
  } else if (serverData.presences) {
    this.processRoster(eventId);
  }
  this.serverDataStack[eventId] = null;
};

/**
 * Process file exchange event come from Cometd notification
 */
UIMainChatWindow.prototype.processFileExchange = function(eventId) {
  var serverData = this.serverDataStack[eventId];
  window.jsconsole.debug('FileExchange event: id:= ' + eventId, serverData);
  if (!serverData) {
    this.serverDataStack[eventId] = null;
  }
  if (serverData.fileEvents) {
    this.UIChatWindow.fileExchangeEventFire(serverData.fileEvents);
  }
  this.serverDataStack[eventId] = null;
};

/**
 * Process group chat message event come from Cometd notification
 */
UIMainChatWindow.prototype.processGroupChat = function(eventId) {
  var serverData = this.serverDataStack[eventId];
  window.jsconsole.debug('GroupChat: id:= ' + eventId, serverData);
  if (!serverData) return;
  if (serverData.mucEvents) {
    var mucEvents = serverData.mucEvents;
    var mucServiceName = this.serverInfo.mucServicesNames;
    var currentRoomUserName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP] + '@' + this.serverInfo.mainServiceName;
    for (var i=0; i<mucEvents.length; i++) {
      var mucEvent = mucEvents[i];
      window.jsconsole.info('MUC event action=' + mucEvent.action);
      switch (mucEvent.action) {
        case this.MUC_ACTION_INVITE_ROOM: 
          var inviteInfo = mucEvent.invite;
          var roomName = inviteInfo.room;
          var msgBuf = inviteInfo.inviter + ' invite you join to room: "' + roomName + '"';
          if (window.confirm(msgBuf)) {
            roomName = roomName.substr(0, roomName.indexOf('@'));
            this.jabberJoinToRoom(roomName, inviteInfo.password);
          }
          break;
        case this.MUC_ACTION_CREATED_ROOM: 
          var createdRoom = mucEvent.createdRoom;
          var roomName = createdRoom.roomInfo.room;
          var uiTabControlObj = this.UIChatWindow.createNewTab(roomName, true);
          window.jsconsole.info('Room @ ' + roomName + ' has been created.');
          this.UIRoomConfigPopupWindow.setVisible(true, uiTabControlObj.tabId, true);
          if (createdRoom.occupants &&
              createdRoom.occupants.length > 0) {
            this.UIChatWindow.updateRoster(createdRoom.occupants);
            continue;
          }
          break;
        case this.MUC_ACTION_JOIN_ROOM: 
          var roomName = mucEvent.joined.substr(0, mucEvent.joined.indexOf('/'));
          this.UIChatWindow.createNewTab(roomName, true);
          if (mucEvent.occupants) {
            this.UIChatWindow.updateRoster(mucEvent.occupants);
          } else {
            this.UIChatWindow.userJoinRoomEventFired(mucEvent.joined, mucEvent.room);
          }
          break;
        case this.MUC_ACTION_LEFT_ROOM: 
          this.UIChatWindow.userLeftRoomEventFired(mucEvent.left, mucEvent.room);
          break;
        default:
          // Message arrived context
          if (mucEvent.message) {
            this.displayMessages(mucEvent.message);
          }
          break;
      }
    }
  }
  this.serverDataStack[eventId] = null;
};
// -/-

// ---  GUI handle --
/**
 * Use for login call from eXoChat init method.
 *
 * @param {String} userName
 */
UIMainChatWindow.prototype.xLogin = function(userName) {
  this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP] = userName;
  this.preChangeStatus(this.ONLINE_STATUS);
};

/**
 * Pre-Update user status call after status menu is selected.
 *
 * @param {String} status
 * @param {Boolean} skipCheck
 * @param {Event} event
 */
UIMainChatWindow.prototype.preChangeStatus = function(status, skipCheck, event) {	
  if (!this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP] ||
      !status ||
      ((this.userStatus != this.ONLINE_STATUS) && (this.userStatus == status))) {
    return;
  }
  event = event || window.event;
  if (event) {
    eXo.communication.chatbar.core.AdvancedDOMEvent.cancelEvent(event);
  }
  this.lastStatusSent = status;
  //this.setChangeStatusMenuVisible(this.statusNode, false);
  var DOMUtil = eXo.core.DOMUtil;
  //var userNameNode = DOMUtil.findFirstDescendantByClass(this.statusIconNode, 'div', 'Text');
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  //userNameNode.innerHTML = userName;
  switch (status) {
    case this.ONLINE_STATUS:
      if (!this.userStatus ||
          (this.userStatus == this.OFFLINE_STATUS)) {
        this.jabberLogin(userName);
        break;
      }
      if (skipCheck ||
          (this.userStatus != this.ONLINE_STATUS)) {
        this.jabberSendStatus(this.ONLINE_STATUS);
      }

      break;
    case this.OFFLINE_STATUS:
      if (this.userStatus != this.OFFLINE_STATUS) {
		this.userStatus = this.OFFLINE_STATUS;
		this.buddyListControlObj.cleanup();
		this.addContactIconNode.onclick = null;
		var userStatusIconNode = DOMUtil.findAncestorByTagName(this.statusIconNode, 'div');
		userStatusIconNode.className = 'IconHolder'+' '+'OfflineIcon';
        this.jabberLogout();
      }
      break;
    case this.AWAY_STATUS:
      if (this.userStatus != this.OFFLINE_STATUS) {
        this.jabberSendStatus(this.AWAY_STATUS);
      }
      break;
    case this.EXTEND_AWAY_STATUS:
      if (this.userStatus != this.OFFLINE_STATUS) {
        this.jabberSendStatus(this.EXTEND_AWAY_STATUS);
      }
      break;
    case this.FREE_TO_CHAT_STATUS:
      if (this.userStatus != this.OFFLINE_STATUS) {
        this.jabberSendStatus(this.FREE_TO_CHAT_STATUS);
      }
      break;
    default:
      break;
  }
};

/**
 * Post-Update user status call after status is updated by server.
 *
 * @param {String} status
 * @param {Boolean} skipCheck
 * @param {Event} event
 */
UIMainChatWindow.prototype.postChangeStatus = function(status, eventId) {
  if (!this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP] ||
      !status) {
    return false;
  }
  var serverData = this.serverDataStack[eventId];
  this.lastStatusSent = false;
  var DOMUtil = eXo.core.DOMUtil;
  //var userNameNode = DOMUtil.findFirstDescendantByClass(this.statusIconNode, 'div', 'Text');
  //userNameNode.innerHTML = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  var userStatusIconNode = DOMUtil.findAncestorByTagName(this.statusIconNode, 'div');
  window.jsconsole.warn('User changed status: ' + this.userStatus + ' -> ' + status);
  this.userStatus = status;
  var presenceData = {};
  presenceData.from = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP] + '@' + this.serverInfo.mainServiceName;
  presenceData.mode = null;
  presenceData.type = this.userStatus;
  this.UIChatWindow.updatePresence(presenceData);
  switch (this.userStatus) {
    case this.ONLINE_STATUS:
      userStatusIconNode.className = 'IconHolder'+' '+'OnlineIcon';
      if (!serverData) {
        break;
      }
      //this.sessionKeeperId = window.setInterval(this.sessionKeeper, this.PORTAL_SESSION_KEEPER_TIME_STEP);
      this.serverInfo = serverData;
      this.UIChatWindow.initSession();
      this.timeoutCount = 0;
      this.errorCount = 0;
      this.addContactIconNode.onclick = function() {
    	 eXo.communication.chatbar.webui.UIAddContactPopupWindow.setVisible(true);
      };
      // Create buddy list
      if (this.serverInfo.roster) {
        this.buddyListControlObj.build(this.serverInfo.roster);
      }
      
      this.subscribeCometdTopics();

      // Register onunload event to window for clean logout when user leave this page.
      this.AdvancedDOMEvent.addEventListener(window, 'unload', this.destroyAll, false);
      this.preChangeStatus(this.ONLINE_STATUS, true);
      eXo.communication.chatbar.webui.UIStateManager.init(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP]);
      break;
    case this.OFFLINE_STATUS:
      //this.unsubscribeCometdTopics();
      this.buddyListControlObj.cleanup();
      this.addContactIconNode.onclick = null;
      userStatusIconNode.className = 'IconHolder'+' '+'OfflineIcon';
      break;
    case this.AWAY_STATUS:
      userStatusIconNode.className = 'IconHolder'+' '+'AwayIcon';
      break;
    case this.EXTEND_AWAY_STATUS:
      userStatusIconNode.className = 'IconHolder'+' '+'ExtendAwayIcon';
      break;
    case this.FREE_TO_CHAT_STATUS:
      userStatusIconNode.className = 'IconHolder'+' '+'FreeToChat';
      break;
    default:
      break;
  }
  eXo.core.DOMUtil.cleanUpHiddenElements();
};

/**
 * Use to set visible/invisible for status popup menu.
 *
 * @param {HTMLElement} nodeObj
 * @param {Boolean} visible
 * @param {Event} event
 */
UIMainChatWindow.prototype.setChangeStatusMenuVisible = function(nodeObj, visible, event) {
  event = event || window.event;
  var AdvancedDOMEvent = eXo.communication.chatbar.core.AdvancedDOMEvent;
  if (event) {
    AdvancedDOMEvent.cancelEvent(event);
  }
  if (!nodeObj) {
    nodeObj = this.statusNode;
  }
  var menuNode = eXo.core.DOMUtil.findFirstDescendantByClass(nodeObj, 'div', 'UIRightClickPopupMenu');
  var display = visible ? 'block' : 'none';
  if (visible == null) {
    display = menuNode.style.display;
    if (display == 'block') {
      display = 'none';
    } else {
      display = 'block';
    }
  }
  if (menuNode.style.display != display) {
    menuNode.style.display = display;
  }
  if (display == 'block') {
    if (eXo.core.Browser.browserType == 'ie') {
      menuNode.parentNode.style.position = 'relative';
    }
    menuNode.style.top = nodeObj.offsetTop + nodeObj.offsetHeight + 'px';
    menuNode.style.left = nodeObj.offsetLeft + 'px';
  }
  if (display == 'block') {
    AdvancedDOMEvent.addEventListener(document, 'click', this.setChangeStatusMenuVisibleWrapper, false);
  } else {
    AdvancedDOMEvent.removeEventListener(document, 'click', this.setChangeStatusMenuVisibleWrapper, false);
  }
  return false;
};

/**
 * Wrapper method to use call from window event handle context
 *
 * @param {Event} event
 */
UIMainChatWindow.prototype.setChangeStatusMenuVisibleWrapper = function(event) {
  event = event || window.event;
  var srcElement = event.srcElement || event.target;
  if (srcElement.className.indexOf('TabUser') != -1 &&
      srcElement.className.indexOf('MenuItem') == -1) {
    if (!eXo.core.DOMUtil.findAncestorByClass(srcElement, 'TabUser')) {
      return true;
    }
  }
  return eXo.communication.chatbar.webui.UIMainChatWindow.setChangeStatusMenuVisible(null, false);
};

/**
 * Create a new room with room information provided
 *
 * @param {Object} roomInfo with struct {name:'roomName', ...}
 */
UIMainChatWindow.prototype.createRoomChat = function(roomInfo) {
  this.jabberCreateRoom(roomInfo.name);
};

/**
 * Wrapper method.
 */
UIMainChatWindow.prototype.buddyItemActionCallbackWrapper = function(event) {
  event = event || window.event;
  eXo.communication.chatbar.webui.UIMainChatWindow.buddyItemActionCallback(event);
  return false;
};

/**
 * Call when user left/right click on contact item
 *
 * @param {Event} event
 */
UIMainChatWindow.prototype.buddyItemActionCallback = function(event) {
//  window.jsconsole.warn('button pressed:' + event.button);
  var buddyNode = event.srcElement || event.target;
  this.AdvancedDOMEvent.cancelEvent(event);
  buddyNode = eXo.core.DOMUtil.findAncestorByClass(buddyNode, 'TitleIconChat');
  if (!buddyNode) {
    return;
  }
  var targetPerson = buddyNode.getAttribute('username');
  switch (event.type) {
    // Left click
    case 'click':
      this.createNewConversation(targetPerson);
      break;
    // Right click
    case 'contextmenu':
      this.buddyItemActionStack['username'] = targetPerson;
      var intTop = 0;
      var intLeft = 0;
      eXo.core.Mouse.update(event);
      var Browser = eXo.core.Browser;
      intTop = eXo.core.Mouse.mouseyInPage - 1;
      intLeft = eXo.core.Mouse.mousexInPage - 1;
      var workspaceControlWidth = 0;
      try {
        workspaceControlWidth = eXo.portal.UIControlWorkspace.width;
      } finally {
        if (isNaN(workspaceControlWidth)) {
          workspaceControlWidth = 0;
        }
      }
      intLeft -= workspaceControlWidth;
      if (this.isWebOS) {
        intTop = eXo.core.Mouse.mouseyInPage - Browser.findPosYInContainer(this.rootNode, document.body) - 1;
        intLeft = eXo.core.Mouse.mousexInPage - Browser.findPosXInContainer(this.rootNode, document.body) - 1;
        if (Browser.isIE7()) {
          if (eXo.portal &&
              eXo.portal.UIControlWorkspace) {
            intLeft += workspaceControlWidth;
          }
        }
      }
      with (this.buddyItemActionMenuNode.style) {
        top = intTop + 'px';
        left = intLeft + 'px';
        display = 'block';
      }
  		if(this.ContactAreaNode){
  			this.ContactAreaNode.style.display = 'block';
  			eXo.core.DOMUtil.listHideElements(this.ContactAreaNode);
  		}
      this.AdvancedDOMEvent.addEventListener(document, 'click', this.postProcessBuddyItemAction, false);
      break;
  }
  return false;
};

/**
 * Call when user select remove menu item from contact item popup menu.
 *
 * @param {Event} event
 */
UIMainChatWindow.prototype.removeUserCallback = function(event) {
  event = event || window.event;
  var buddyNode = event.srcElement || event.target;
  buddyNode = eXo.core.DOMUtil.findAncestorByClass(buddyNode, 'TitleIconChat');
  var buddyId = buddyNode.getAttribute('userName');
  eXo.communication.chatbar.webui.UIMainChatWindow.removeContact(buddyId);
};

/**
 * Remove contact callback process
 */
UIMainChatWindow.prototype.removeContact = function(buddyId) {
  if (window.confirm('Are you sure to remove \'' + buddyId + '\'')) {
    buddyId = buddyId.substring(0, buddyId.indexOf('@'));
    eXo.communication.chatbar.webui.UIMainChatWindow.jabberRemoveUser(buddyId);
  }
};

/**
 * Create new conversation with contact when user click to menu item
 * in contact popup menu.
 *
 * @param {Event} event
 */
UIMainChatWindow.prototype.createNewConversation = function(targetPerson) {
  this.UIChatWindow.createNewTab(targetPerson);
  return false;
};

/**
 * Post-Process for buddy item action will be call after some pre-process is finished.
 */
UIMainChatWindow.prototype.postProcessBuddyItemAction = function(event, action) {
  event = event || window.event;
  if (!action) {
    if (event) {
      var srcElement = event.srcElement || event.target;
      if (srcElement.className.indexOf('IconChat') != -1) {
        return true;
      }
    }
    var UIMainChatWindow = eXo.communication.chatbar.webui.UIMainChatWindow;
    UIMainChatWindow.buddyItemActionMenuNode.style.display = 'none';
    eXo.communication.chatbar.core.AdvancedDOMEvent.removeEventListener(document, 'click', UIMainChatWindow.postProcessBuddyItemAction, false);
    return;
  }
  this.buddyItemActionMenuNode.style.display = 'none';
  var targetPerson = this.buddyItemActionStack['username'];
  if (!targetPerson) {
    return;
  }
  switch (action) {
    case this.REMOVE_USER_ACTION:
      this.removeContact(targetPerson);
      break;
    case this.CREATE_CONVERSATION_ACTION:
      this.UIChatWindow.createNewTab(targetPerson);
      break;
    default:
      break;
  }
  this.buddyItemActionStack['username'] = false;
};

/**
 * Using to contact(s) to contact list. This method will be called by UIAddContactPopupWindow window
 * manager object.
 *
 * @param {Array} contactList
 */
UIMainChatWindow.prototype.addContacts = function(contactList){
  for (var i=0; i<contactList.length; i++) {
    var contact = contactList[i];
    this.jabberAddUser(contact);
  }
};

/**
 * Pre-Process then call method to display message in conversation windows
 *
 * @param {Array} messages
 * @param {Boolean} cancelIfNotExist
 */
UIMainChatWindow.prototype.displayMessages = function(messages, cancelIfNotExist) {
  try {
    if (messages.length > 1) {
      var lastSender = messages[0].from;
      var lastSenderPoint = 0;
      for (var i=1; i<messages.length; i++) {
        var msgObj = messages[i];
        if (lastSender != msgObj.from || i == (messages.length -1)) {
          var buffer = '';
          for (var j=lastSenderPoint; j<=i; j++) {
            if (j<i) {
              buffer += messages[j].body + '<br/>';
            } else {
              buffer += messages[j].body;
            }
          }
          var isGroupChat = false;
          if (messages[lastSenderPoint].type == 'groupchat') {
            isGroupChat = true;
          }
          messages[lastSenderPoint].body = buffer;
          this.UIChatWindow.displayMessage(messages[lastSenderPoint].from, messages[lastSenderPoint], isGroupChat);
          lastSenderPoint = i;
          lastSender = msgObj.from;
        }
      }
    } else if (messages.length > 0) {
      var msgObj = messages[0];
      var isGroupChat = false;
      if (msgObj.type == 'groupchat') {
        isGroupChat = true;
      }
      this.UIChatWindow.displayMessage(msgObj.from, msgObj, isGroupChat);
    }
  } catch(e) {
    window.jsconsole.error('Look up error! developer');
    window.jsconsole.dir(e);
  }
};

// -/-

/**
 * Using Organization service to do fuzzy user search which match all user info fields.
 * {from} & {to} parameters used to page iterator.
 *
 * @param {String} question
 * @param {Integer} from
 * @param {Integer} to
 */
UIMainChatWindow.prototype.orgFuzzySearchUser = function(question, from, to) {
  this.activeAction = this.ORG_FUZZY_SEARCH_USER_ACTION;
  question = question || '';
  this.XMPPCommunicator.orgFuzzySearchUser(question, from, to, this.getAjaxHandler());
};

/**
 * Using Organization service to search user by name
 *
 * @param {String} userName
 */
UIMainChatWindow.prototype.orgSearchUser = function(userName) {
  this.activeAction = this.ORG_GET_ALL_CONTACT_ACTION;
  userName = userName || '';
  this.XMPPCommunicator.orgSearchUser(userName, this.getAjaxHandler());
};

/**
 * Get url from file exchange service to download file transfered after user click
 * accept file transfer link.
 *
 * @param {String} uuid user file exchange id to confirm.
 */
UIMainChatWindow.prototype.acceptSendFile = function(uuid) {
  this.activeAction = this.ACEPT_SEND_FILE_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  return this.XMPPCommunicator.acceptSendFile(userName, uuid, this.XMPPCommunicator.TRANSPORT_XMPP);
};

UIMainChatWindow.prototype.denieSendFile = function(uuid) {
  this.activeAction = this.DENIAL_SEND_FILE_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.denieSendFile(userName, uuid, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};
// -/-

/**
 * A common Wrapper method using for event handle wrapper method.
 *
 * @param {String} callbackMethod
 */
UIMainChatWindow.prototype.wrapperMethod = function(callbackMethod) {
  if (callbackMethod) {
    eval(callbackMethod);
  }
};

// --- Jabber protocol handle --

/**
 * Login to jabber server
 *
 * @param {String} userName
 */
UIMainChatWindow.prototype.jabberLogin = function(userName) {
  if (!userName || userName == 'null') {
    return false;
  }
  // Register with on connection ready to wait for cometd connection become ready before try login.
  this.userNames['xmpp'] = userName;
  if (!eXo.cs.CSCometd.isConnected()) {
	  eXo.cs.CSCometd.addOnConnectionReadyCallback(this.loginWrapper);
    return;
  }
  this.activeAction = this.LOGIN_ACTION;
  this.XMPPCommunicator.addTransport(this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
  return false;
};

/**
 * Clean user's contact/buddy list
 */
UIMainChatWindow.prototype.jabberCleanBuddyList = function() {
  this.activeAction = this.CLEAN_BUDDY_LIST_ACTION;
  this.XMPPCommunicator.cleanBuddyList(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP], this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Remove an user from contact list
 *
 * @param {String} buddyId
 */
UIMainChatWindow.prototype.jabberRemoveUser = function(buddyId) {
  this.activeAction = this.REMOVE_USER_ACTION;
  try {
    var tabId = this.UIChatWindow.getTabId(buddyId + '@' + this.serverInfo.mainServiceName);
    this.UIChatWindow.closeTab(tabId.id);
  } catch (e) {
  }
  this.XMPPCommunicator.removeUser(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP], buddyId, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Unsubscribe an user from contact list
 *
 * @param {String} buddyId
 */
UIMainChatWindow.prototype.jabberUnsubscriptUser = function(buddyId) {
  this.activeAction = this.UNSUBSCRIPT_BUDDY_ACTION;
  this.XMPPCommunicator.unSubscribeUser(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP], buddyId, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Use to change user's status
 *
 * @param {String} status
 */
UIMainChatWindow.prototype.jabberSendStatus = function(status) {
  this.activeAction = this.SEND_STATUS_ACTION;
  this.XMPPCommunicator.sendStatus(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP], this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler(), status);
};

/**
 * Use to get history message between time range
 *
 * @param {String} targetPerson
 * @param {String} dateFormat
 * @param {String} dateFrom
 * @param {String} dateTo
 * @param {Boolean} isGroupChat
 */
UIMainChatWindow.prototype.jabberGetMessageHistory = function(targetPerson, dateFormat, dateFrom, dateTo, isGroupChat) {
  this.activeAction = this.GET_MESSAGE_HISTORY_ACTION;
  this.XMPPCommunicator.getMessageHistory(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP], this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler(), targetPerson, dateFormat, dateFrom, dateTo, isGroupChat);
};

/**
 * Use to send private message
 *
 * @param {String} sendTo
 * @param {MessageObject} msg struct: {to:'buddy id', body:'message body'}
 */
UIMainChatWindow.prototype.jabberSendMessage = function(sendTo, msg) {
  this.activeAction = this.SEND_MESSAGE_ACTION;
  msg = {to: sendTo, body: msg};
  var msgPackage = eXo.core.JSON.stringify(msg);//'{"to":"' + sendTo + '", "body":"' + msg + '"}';
  this.XMPPCommunicator.sendMessage(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP], this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler(), msgPackage);
};

/**
 * Use to send a group message
 *
 * @param {String} sendTo
 * @param {MessageObject} msg struct: {to:'buddy id', body:'message body'}
 */
UIMainChatWindow.prototype.jabberSendRoomMessage = function(sendTo, msg) {
  this.activeAction = this.SEND_MESSAGE_ACTION;
  msg = {to: sendTo, body: msg};
  var msgPackage = eXo.core.JSON.stringify(msg);//'{"to":"' + sendTo + '", "body":"' + msg + '"}';
  this.XMPPCommunicator.sendRoomMessage(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP], this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler(), msgPackage);
};

/**
 * Get subscribe request list
 */ 
UIMainChatWindow.prototype.jabberGetSubscriptionRequests = function() {
  this.activeAction = this.GET_SUBSCRIPTION_REQUESTS_ACTION;
  this.isGetMsgInProcess = true;
  this.XMPPCommunicator.getSubscriptionRequests(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP], this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Use to get authorize to see status of another user
 *
 * @param {String} ask4SubUser
 */
UIMainChatWindow.prototype.jabberAsk4Subscription = function(ask4SubUser) {
  this.activeAction = this.ASK_4_SUBSCRIPTION_ACTION;
  this.XMPPCommunicator.askForSubscription(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP], ask4SubUser, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Add a new user to contact list
 *
 * @param {String} addUser
 */
UIMainChatWindow.prototype.jabberAddUser = function(addUser) {
  this.activeAction = this.ADD_USER_ACTION;
  this.XMPPCommunicator.addUser(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP], addUser, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Confirm authorize request
 *
 * @param {String} subUser
 */
UIMainChatWindow.prototype.jabberSendSubscription = function(subUser) {
  this.activeAction = this.SEND_SUBSCRIPTION_ACTION;
  this.XMPPCommunicator.subscribeUser(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP], subUser, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Use to create a new chat room
 *
 * @param {String} roomName
 */
UIMainChatWindow.prototype.jabberCreateRoom = function(roomName) {
  this.activeAction = this.CREATE_ROOM_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.createRoomchat(userName, userName, roomName, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Use to send room's configuration
 *
 * @param {String} roomName
 * @param {String} roomConfigJson
 */
UIMainChatWindow.prototype.jabberSendConfigRoom = function(roomName, roomConfigJson) {
  this.activeAction = this.CONFIG_ROOM_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.sendConfigRoom(userName, roomName, roomConfigJson, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Use to get room's information such as: room name, jid, user joined list....
 *
 * @param {String} roomName
 */
UIMainChatWindow.prototype.jabberGetRoomInfo = function(roomName) {
  this.activeAction = this.GET_ROOM_INFO_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.getRoomInfo(userName, roomName, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Use to get room's configuration
 *
 * @param {String} roomName
 */
UIMainChatWindow.prototype.jabberGetRoomConfig = function(roomName) {
  this.activeAction = this.GET_ROOM_CONFIG_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.getRoomConfig(userName, roomName, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Get current room created on the server list
 *
 * @param {Integer} from
 * @param {Integer} to
 * @param {String} sort have 2 values are: ASC, DASC
 */
UIMainChatWindow.prototype.jabberGetRoomList = function(from, to, sort) {
  //this.jabberGetJoinedRoomList();
  this.activeAction = this.GET_ROOM_LIST_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.getRoomList(userName, from, to, sort, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Use to get user joined room list
 */
UIMainChatWindow.prototype.jabberGetJoinedRoomList = function() {
  this.activeAction = this.GET_JOINED_ROOM_LIST_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.getJoinedRoomList(userName, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Use to manage room chat
 *
 * @param {String} roomName
 * @param {String} nickName
 * @param {String} role
 * @param {String} command
 */
UIMainChatWindow.prototype.jabberSetRoleRoom = function(roomName, nickName, role, command) {
  this.activeAction = this.SET_ROLE_ROOM_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.setRoleForRoom(userName, nickName, roomName, role, command, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler);
};

/**
 * Use to kick an user out from a room chat
 *
 * @param {String} roomName
 * @param {String} nickName
 * @param {String} reason
 */
UIMainChatWindow.prototype.jabberKickUserFromRoom = function(roomName, nickName, reason) {
  this.activeAction = this.KICK_USER_FROM_ROOM_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.kickUserFromRoom(userName, nickName, roomName, reason, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler);
};

/**
 * Use to ban an user from a room chat
 *
 * @param {String} roomName
 * @param {String} nickName
 * @param {String} reason
 */
UIMainChatWindow.prototype.jabberBanUserFromRoom = function(roomName, nickName, reason) {
  this.activeAction = this.BAN_USER_FROM_ROOM_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.banUserFromRoom(userName, nickName, roomName, reason, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler);
};

/**
 * Invite a new user to join room
 *
 * @param {String} inviter
 * @param {String} roomName
 */
UIMainChatWindow.prototype.jabberInviteJoinRoom = function(inviter, roomName) {
  this.activeAction = this.INVITE_JOIN_ROOM_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.inviteJoinRoom(userName, inviter, roomName, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Confirm decline from invite to join a room chat
 *
 * @param {String} inviter
 * @param {String} roomName
 */
UIMainChatWindow.prototype.jabberDeclineJoinRoom = function(inviter, roomName) {
  this.activeAction = this.DECLINE_JOIN_ROOM_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.declineInviteJoinRoom(userName, inviter, roomName, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Use to join to a room chat
 *
 * @param {String} roomName
 * @param {Boolean} askPassword
 */
UIMainChatWindow.prototype.jabberJoinToRoom = function(roomName, askPassword) {
  var password = '';
  if (askPassword) {
    password = window.prompt('Please give secret key to access room:', '');
    if (!password) {
      return;
    }
  }
  this.activeAction = this.JOIN_TO_ROOM_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.joinToRoom(userName, roomName, password, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Use to leave from a chat room
 *
 * @param {String} roomName
 */
UIMainChatWindow.prototype.jabberLeaveFromRoom = function(roomName) {
  this.activeAction = this.LEAVE_FROM_ROOM_ACTION;
  var userName = this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP];
  this.XMPPCommunicator.leaveFromRoom(userName, roomName, this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
};

/**
 * Logout from jabber server
 */
UIMainChatWindow.prototype.jabberLogout = function() {
  if (this.sessionKeeperId) {
    window.clearInterval(this.sessionKeeperId);
    this.sessionKeeperId = false;
  }
  if (this.checkAliveId) {
    window.clearInterval(this.checkAliveId);
    this.checkAliveId = false;
  }
  if (window.parent &&
      window.parent.eXo.communication.chatbar.webui.eXoChatLoader) {
    window.parent.eXo.communication.chatbar.webui.eXoChatLoader.setChatWindowVisible(false);
  }
  if (!this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP]) {
  	return;
  }
  this.destroy();
  this.activeAction = this.LOGOUT_ACTION;
  this.XMPPCommunicator.removeTransport(this.userNames[this.XMPPCommunicator.TRANSPORT_XMPP], this.XMPPCommunicator.TRANSPORT_XMPP, this.getAjaxHandler());
  return false;
};
// -/-

/**
 * Login to yahoo chat protocol
 */
UIMainChatWindow.prototype.yahooLogin = function(nodeObj) {};

/**
 * Login to gtalk chat protocol
 */
UIMainChatWindow.prototype.gtalkLogin = function(nodeObj) {};

/**
 * Login to msg chat protocol
 */
UIMainChatWindow.prototype.msnLogin = function(nodeObj) {};

/**
 * Login to aim chat protocol
 */
UIMainChatWindow.prototype.aimLogin = function(nodeObj) {};

/**
 * Login to icq chat protocol
 */
UIMainChatWindow.prototype.icqLogin = function(nodeObj) {};

eXo.communication.chatbar.webui.UIMainChatWindow = new UIMainChatWindow();
