/**
 * @author Uoc Nguyen
 * 
 * A core javascript library using for communicate with services.
 */
function XMPPCommunicator() {
  this.SERVICE_URL       = '/';
  this.TRANSPORT_XMPP    = 'xmpp';
  this.TRANSPORT_YAHOO   = 'yahoo';
  this.TRANSPORT_GTALK   = 'gtalk';
  this.TRANSPORT_ICQ     = 'icq';
  this.TRANSPORT_AIM     = 'aim';
  this.TRANSPORT_MSN     = 'msn';
}

XMPPCommunicator.prototype.init = function(restContextName) {
  this.restContextName = restContextName;
  this.SERVICE_URL += restContextName;
};

/**
 * Encode ajax parameter before send to service
 * 
 * @param {String} param
 */
XMPPCommunicator.prototype.encodeParam = function(param) {
  //return window.escape(param);
  return window.encodeURIComponent(param);
};

/**
 * Encode room name before send it to REST service
 * 
 * @param {String} roomName
 */
XMPPCommunicator.prototype.encodeRoomName = function(roomName) {
  if (roomName.indexOf('@') == -1) {
    return roomName;
  }
  var roomNameParts = roomName.split('@');
  roomNameParts[0] = this.encodeParam(roomNameParts[0]);
  return roomNameParts.join('@');
};

/**
 * Overwritten some ajax parameters and method defined in portal.
 * 
 * @param {Boolean} manualMode
 * @param {AjaxRequest} ajaxRequest
 */
XMPPCommunicator.prototype.ajaxProcessOverwrite = function(manualMode, ajaxRequest) {
  if (ajaxRequest.request == null) return ;
  ajaxRequest.request.open(ajaxRequest.method, ajaxRequest.url, true) ;   
  if (!manualMode) {
    if (ajaxRequest.method == "POST") {
      ajaxRequest.request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8") ;
    } else {
      ajaxRequest.request.setRequestHeader("Content-Type", "text/plain;charset=UTF-8") ;
    }
  } else {
    ajaxRequest.request.setRequestHeader("Content-Type", "application/json;charset=UTF-8") ;
  }
  
  if (ajaxRequest.timeout > 0) setTimeout(ajaxRequest.onTimeoutInternal, ajaxRequest.timeout) ;
  
  ajaxRequest.request.send(ajaxRequest.queryString) ;
};

/**
 * Overwritten some ajax parameters and method defined in portal, using synchronous XHR in window.unload.
 * 
 * @param {AjaxRequest} ajaxRequest
 */
XMPPCommunicator.prototype.synchronousXHR = function(ajaxRequest) {
  if (ajaxRequest.request == null) return ;
  ajaxRequest.request.open(ajaxRequest.method, ajaxRequest.url, false) ; 
  ajaxRequest.request.onreadystatechange = function() {};
  ajaxRequest.request.send(ajaxRequest.queryString) ;
};

/**
 * A common method use for setup an AjaxRequest object
 * 
 * @param {AjaxRequest} ajaxRequest
 * @param {Function} handler
 */
XMPPCommunicator.prototype.initRequest = function(ajaxRequest, handler) {
  ajaxRequest.onSuccess = handler.onSuccess ;
  ajaxRequest.onLoading = handler.onLoading ;
  ajaxRequest.onTimeout = handler.onTimeout ;
  ajaxRequest.onError = handler.onError ;
  ajaxRequest.callBack = handler.callBack ;
  ajaxRequest.handler = handler;
  this.currentRequest = ajaxRequest ;
};

// --- Organization service

/**
 * Use to contact to service which search user by keyword using fuzzy search method and return result in range.
 * Range will be limited in {from} and {to} parameters use for javascript page iterator.
 * 
 * @HTTPMethod(HTTPMethods.GET)
 * @URITemplate("/user/find-user-in-range/?question=question-content&from=x&to=y")
 * @param {String} question
 * @param {Integer} from Begin of range
 * @param {Integer} to End of range
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.orgFuzzySearchUser = function(question, from, to, handler) {
  var url = '/' + this.restContextName + '/organization/json/user/find-user-in-range/?question=' + question + '&from=' + from + '&to=' + to;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Use to contact to service which search user by keyword using fuzzy search method and return all result
 * at once time.
 * 
 * @HTTPMethod(HTTPMethods.GET)
 * @URITemplate("/user/find-all/")
 * @param {String} username
 * @param {String} firstname
 * @param {String} lastname
 * @param {String} email
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.orgSearchUser = function(userName, handler) {
  var url = '/' + this.restContextName + '/organization/json/user/find-all/?username=' + userName;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

// -/-

// --- File Exchange
/**
 * Return the service download url for file exchange.
 * 
 * @param {String} userName
 * @param {String} uuid
 * @param {String} transportName
 * @param {Function} handler
 */
XMPPCommunicator.prototype.acceptSendFile = function(userName, uuid, transportName, handler) {
  return (this.SERVICE_URL + '/' + transportName + '/fileexchange/accept/' + userName + '/' + uuid + '/');
};

/**
 * Send deny file exchange confirmation to file exchange service.
 * 
 * @param {String} userName
 * @param {String} uuid
 * @param {String} transportName
 * @param {Function} handler
 */
XMPPCommunicator.prototype.denieSendFile = function(userName, uuid, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/fileexchange/reject/' + userName + '/' + uuid + '/';
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};
// -/-

/**
 * Using to login to a transport. Data will be automatically take from a HTML Form node. 
 * 
 * @param {Element} formNode
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.addTransportForm = function(formNode, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/login';
  var data = eXo.webui.UIForm.serializeForm(formNode) ;
  var request = new eXo.portal.AjaxRequest('POST', url, data);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Using to login to a transport. This is direct method using method get to pass username and password.
 * 
 * @param {Element} formNode
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.addTransportDirect = function(userName, password, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/login';
  var data = 'username=' + userName + '&password=' + password;
  var request = new eXo.portal.AjaxRequest('POST', url, data);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Using to login to a transport but re-used portal authenticated authorize to login.
 * This method not require username and password. 
 * 
 * @param {Element} formNode
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.addTransport = function(transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/login2/' + (new Date()).getTime() + '/';
  //var url = this.SERVICE_URL + '/' + transportName + '/login2';
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Use for logout.
 * 
 * @param {String} userName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.removeTransport = function(userName, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/logout/' + userName;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Send a request confirmation for subscribe a contact
 * 
 * @param {String} userName
 * @param {String} askUser
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.askForSubscription = function(userName, askUser, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/askforsubscription/' + userName + '/' + askUser;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Use to confirm add an user to contact list
 * 
 * url: /xmpp/roster/add/{username}/{adduser}
 * @param {String} userName
 * @param {String} addUser
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.addUser = function(userName, addUser, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/roster/add/' + userName + '/' + addUser;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Using to confirm authorize and add user to contact list.
 * 
 * @param {String} userName
 * @param {String} subUser
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.subscribeUser = function(userName, subUser, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/subscribeuser/' + userName + '/' + subUser;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Remove user from contact list
 * 
 * @param {String} userName
 * @param {String} subUser
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.removeUser = function(userName, subUser, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/roster/del/' + userName + '/' + subUser;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Remove subscribe authorize for an user.
 * 
 * @param {String} userName
 * @param {String} subUser
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.unSubscribeUser = function(userName, subUser, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/unsubscribeuser/' + userName + '/' + subUser;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Using to get subscription request list. 
 * 
 * @param {String} userName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.getSubscriptionRequests = function(userName, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/getsubscriptionrequests/' + userName;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Use to remove all user in contact list.
 * 
 * @param {String} userName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.cleanBuddyList = function(userName, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/rosterclean/' + userName;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Use to get contact list.
 * 
 * @param {String} userName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.getBuddyList = function(userName, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/roster/' + userName;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Use to change user status to available, away, extends away, free for chat ...
 * 
 * @param {String} userName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 * @param {String} statusMsg
 */
XMPPCommunicator.prototype.sendStatus = function(userName, transportName, handler, statusMsg) {
  var url = this.SERVICE_URL + '/' + transportName + '/sendstatus/' + userName + '/' + statusMsg;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * Use to create a new room chat.
 * 
 * url: /xmpp/muc/createroom/{username}/?room={room}&nickname={nickname}
 * 
 * @param {String} userName
 * @param {String} nickName
 * @param {String} roomName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.createRoomchat = function(userName, nickName, roomName, transportName, handler) {
  roomName = this.encodeParam(roomName);
  var url = this.SERVICE_URL + '/' + transportName + '/muc/createroom/' + userName + '/?room=' + roomName + '&nickname=' + nickName;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * Use to send room's configuration to config room. 
 * 
 * url: /xmpp/muc/configroom/{username}/?room={room}
 * 
 * @param {String} userName
 * @param {String} roomName
 * @param {String} roomConfigJson
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.sendConfigRoom = function(userName, roomName, roomConfigJson, transportName, handler) {
  roomName = this.encodeParam(roomName);
  var url = this.SERVICE_URL + '/' + transportName + '/muc/configroom/' + userName + '/?room=' + roomName;
  var request = new eXo.portal.AjaxRequest('POST', url, roomConfigJson);
  this.initRequest(request, handler);
  this.ajaxProcessOverwrite(true, request);
};

/**
 * Service will be return room's information.
 * 
 * url: /xmpp/muc/getroominfo/{username}/?room={room}
 * 
 * @param {String} userName
 * @param {String} roomName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.getRoomInfo = function(userName, roomName, transportName, handler) {
  roomName = this.encodeRoomName(roomName);
  var url = this.SERVICE_URL + '/' + transportName + '/muc/getroominfo/' + userName + '/?room=' + roomName;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * Use to get current room's configuration data. We are using this data to refresh room configuration form.
 * 
 * url: /xmpp/muc/getroomconfig/{username}/?room={room}
 * 
 * @param {String} userName
 * @param {String} roomName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.getRoomConfig = function(userName, roomName, transportName, handler) {
  roomName = this.encodeParam(roomName);
  var url = this.SERVICE_URL + '/' + transportName + '/muc/getroomconfig/' + userName + '/?room=' + roomName;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * Get current room list created in the server.
 * 
 * url: /xmpp/muc/rooms/{username}
 * 
 * @param {String} userName
 * @param {Integer} from
 * @param {Integer} to
 * @param {String} sort desc for descending, anything else for ascending.
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.getRoomList = function(userName, from, to, sort, transportName, handler) {
  if (isNaN(from)) {
    from = 1;
  }
  if (isNaN(to)) {
    to = -1;
  }
  var url = this.SERVICE_URL + '/' + transportName + '/muc/rooms/' + userName + '/?from=' + from + '&to=' + to + '&sort=' + sort;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * Get current joined room list for current user.
 * 
 * url: /xmpp/muc/rooms/{username}
 * 
 * @param {String} userName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.getJoinedRoomList = function(userName, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/muc/joinedrooms/' + userName + '/';
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * For manage role of participant
 * 
 * url: /xmpp/muc/managerole/{username}/{room}/{nickname}/
 * @param {String} username
 * @param {String} nickName: nickname that role need change
 * @param {String} role: it can be: "moderator","participant"
 * @param {String} command: it can be : "grand" or "revoke"
 * @param {String} roomName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.setRoleForRoom = function(username, nickName, roomName, role, command, transportName, handler) {
  roomName = this.encodeParam(roomName);
  var url = this.SERVICE_URL + '/' + transportName + '/managerole/' + 
              userName + '/?room=' + roomName + '&nickname=' + nickName + '&role=' + role + '&command=' + command;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * For kick user from room
 * url: /xmpp/muc/kick/{username}/{room}/{nickname}/
 *
 * @param {String} username
 * @param {String} nickname
 * @param {String} roomName
 * @param {String} roomName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.kickUserFromRoom = function(username, nickname, roomName, reason, transportName, handler) {
  roomName = this.encodeParam(roomName);
  var url = this.SERVICE_URL + '/' + transportName + '/muc/kick/' + userName + '/?room=' + roomName + '&nickname=' + nickName + '&reason=' + reason;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * For ban user
 * url: /xmpp/muc/ban/{username}/{room}/{name}/
 *
 * @param {String} userName
 * @param {String} name: it must be not nickname in room but real exo username
 * @param {String} roomName
 * @param {String} reason
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.banUserFromRoom = function(userName, name, roomName, reason, transportName, handler) {
  roomName = this.encodeParam(roomName);
  var url = this.SERVICE_URL + '/' + transportName + '/muc/ban/' + userName + '/?room=' + roomName + '&name=' + name + '&reason=' + reason;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * Invite an user to join to room.
 * 
 * @param {String} userName
 * @param {String} inviter
 * @param {String} roomName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.inviteJoinRoom = function(userName, inviter, roomName, transportName, handler) {
  roomName = this.encodeParam(roomName);
  var url = this.SERVICE_URL + '/' + transportName + '/muc/invite/' + userName + '/' + inviter + '/?room=' + roomName; 
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * Decline from join room invitation. 
 * 
 * @param {String} userName
 * @param {String} nickName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.declineInviteJoinRoom = function(userName, inviter, roomName, transportName, handler) {
  roomName = this.encodeParam(roomName);
  var url = this.SERVICE_URL + '/' + transportName + '/muc/decline/' + userName + '/' + inviter + '/?room=' + roomName;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * Join to room.
 * 
 * url: /xmpp/muc/join/{username}/{room}/
 * @param {String} userName
 * @param {String} roomName
 * @param {String} password
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.joinToRoom = function(userName, roomName, password, transportName, handler) {
  roomName = this.encodeParam(roomName);
  var url = this.SERVICE_URL + '/' + transportName + '/muc/join/' + userName + '/?room=' + roomName + '&password=' + password;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * Leave from current joined room.
 * 
 * url: /xmpp/muc/leaveroom/{username}/{room}/
 * @param {String} userName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.leaveFromRoom = function(userName, roomName, transportName, handler) {
  roomName = this.encodeParam(roomName);
  var url = this.SERVICE_URL + '/' + transportName + '/muc/leaveroom/' + userName + '/?room=' + roomName;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process();
};

/**
 * Send message to user.
 * 
 * @param {String} userName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 * @param {Object} msgObj
 */
XMPPCommunicator.prototype.sendMessage = function(userName, transportName, handler, msgObj) {
  var url = this.SERVICE_URL + '/' + transportName + '/sendmessage/' + userName + '/';
  var data = msgObj;
  var request = new eXo.portal.AjaxRequest('POST', url, data);
  this.initRequest(request, handler);
  this.ajaxProcessOverwrite(true, request);
};

/**
 * Get message history by time.
 * 
 * @param {String} userName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 * @param {String} targetPerson
 * @param {String} dateFormat
 * @param {String} dateFrom
 * @param {String} dateTo
 * @param {Boolean} isGroupChat
 *
 */
XMPPCommunicator.prototype.getMessageHistory = function(userName, transportName, handler, targetPerson, dateFrom, dateTo, isGroupChat) {
  //targetPerson = this.encodeParam(targetPerson);
  var url = this.SERVICE_URL + '/' + transportName + '/history/getmessages/' + userName + '/' + isGroupChat + '/';
  if (dateFrom) {
    url += dateFrom + '/';
  }
  if (dateFrom &&
      dateTo) {
    url += dateTo + '/';
  }
  url += '?usernamefrom=' + targetPerson;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Send a room's message.
 * 
 * @param {String} userName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 * @param {Object} msgObj
 */
XMPPCommunicator.prototype.sendRoomMessage = function(userName, transportName, handler, msgObj) {
  var url = this.SERVICE_URL + '/' + transportName + '/muc/sendmessage/' + userName + '/';
  var data = msgObj;
  var request = new eXo.portal.AjaxRequest('POST', url, data);
  this.initRequest(request, handler);
  this.ajaxProcessOverwrite(true, request);
};

/**
 * Get a list of subscription requesting.
 * 
 * @param {String} userName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.getSubscriptionrequests = function(userName, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/getsubscriptionrequests/' + userName;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

/**
 * Use for logout from a transport.
 * 
 * @param {String} userName
 * @param {String} transportName
 * @param {AjaxHandler} handler
 */
XMPPCommunicator.prototype.removeTransport = function(userName, transportName, handler, status) {
  var url = this.SERVICE_URL + '/' + transportName + '/logout/' + userName + '/' + status;
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  this.synchronousXHR(request);
};

XMPPCommunicator.prototype.loadJsResourceBundle = function(locale, transportName, handler) {
  var url = this.SERVICE_URL + '/' + transportName + '/loadJsResourceBundle/' + locale + '/';
  var request = new eXo.portal.AjaxRequest('GET', url, null);
  this.initRequest(request, handler);
  request.process() ;
};

eXo.communication.chatbar.core.XMPPCommunicator = new XMPPCommunicator();
