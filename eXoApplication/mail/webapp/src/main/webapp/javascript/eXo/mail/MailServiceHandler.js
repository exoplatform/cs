/**
 * @author Uoc Nguyen, Nam Phung
 */
function MailServiceHandler() {
  this.START_CHECKMAIL_STATUS = 101;
  this.CONNECTION_FAILURE = 102 ;
  this.RETRY_PASSWORD = 103
  this.DOWNLOADING_MAIL_STATUS = 150;
  this.NO_UPDATE_STATUS = 201;
  this.FINISHED_CHECKMAIL_STATUS = 200;
  this.REQUEST_STOP_STATUS = 202;
  
  this.START_SYNC_FOLDER = 301;
  this.FINISH_SYNC_FOLDER = 302;
  
  this.SERVICE_BASED_URL = '/portal/rest/cs/mail';
  this.CHECK_MAIL_ACTION = 'check mail action';
  this.GET_CHECK_MAIL_INFO_ACTION = 'get check mail info action';
  this.STOP_CHECK_MAIL_ACTION = 'stop check mail action';
  this.MAX_TRY = 3;
  this.tryCount = 0;
  this.isUpdateUI_ = true ;
}

MailServiceHandler.prototype = new eXo.cs.webservice.core.WebserviceHandler();

MailServiceHandler.prototype.initService = function(uiId, userName, accountId) {
  this.uiId = uiId;
  this.accountId = accountId;
  this.userName = userName;
  if (eXo.core.Browser.getCookie('cs.mail.checkingmail' + this.accountId) == 'true') {
    this.checkMail();
  }
};

MailServiceHandler.prototype.setCheckmailTimeout = function(checkMailInterval) {
  this.checkMailInterval = checkMailInterval;
  if (this.autoCheckMailTimeoutId) {
  	try {
      window.clearInterval(this.autoCheckMailTimeoutId);
    } catch (e) {};
  }
  if (this.checkMailInterval &&
      !isNaN(this.checkMailInterval) && (parseInt(this.checkMailInterval) > 0) ) {
    this.checkMailInterval = parseInt(this.checkMailInterval);
    this.autoCheckMailTimeoutId = window.setInterval(eXo.mail.MailServiceHandler.checkMailWrapper, checkMailInterval);
  }
};

/**
 * 
 * @param {Integer} state
 * @param {eXo.portal.AjaxRequest}} requestObj
 * @param {String} action
 */
MailServiceHandler.prototype.update = function(state, requestObj, action) {
  if (state == this.SUCCESS_STATE && requestObj.responseXML) {
    try {
      eval ("this.serverData = " + eXo.cs.Utils.xml2json(requestObj.responseXML, ''));
    } catch(e) {
      this.updateUI(this.ERROR_STATE);
    }
    if (!this.serverData.info ||
    		!this.serverData.info.checkingmail ||
        !this.serverData.info.checkingmail.status) {
      return;
    }
    var status = parseInt(this.serverData.info.checkingmail.status);
    var url = false;
    if (status == this.START_CHECKMAIL_STATUS ||
        status == this.DOWNLOADING_MAIL_STATUS ||
        status == this.NO_UPDATE_STATUS) {
      url = this.SERVICE_BASED_URL + '/checkmailjobinfo/' + this.userName + '/' + this.accountId + '/';
    }
    if (url && url != '') {
      this.activeAction = this.GET_CHECK_MAIL_INFO_ACTION;
      this.makeRequest(url, this.HTTP_GET, '', this.GET_CHECK_MAIL_INFO_ACTION);
    }
    if (status != this.NO_UPDATE_STATUS) {
      this.updateUI(status);
    }
    
    var statusSync = parseInt(this.serverData.info.checkingmail.syncFolderStatus);
    
    if (statusSync) {
	    if (statusSync == this.START_SYNC_FOLDER) {
	    	document.getElementById('SynchronizeIconRefreshFolder').className = "SyncingIcon"; 
	    } else if (statusSync == this.FINISH_SYNC_FOLDER) {
	    	document.getElementById('SynchronizeIconRefreshFolder').className = "SyncIcon"; 
	    	var updateImapFolder = document.getElementById("UpdateImapFolder");
	  	      if (updateImapFolder != null) {
	  	    	eval(eXo.core.DOMUtil.findDescendantsByTagName(updateImapFolder, 'a')[0].href.replace("%20", ""));
	  	      }
	    }
    }
    	
    if (status == this.FINISHED_CHECKMAIL_STATUS || status == this.CONNECTION_FAILURE || status == this.RETRY_PASSWORD ||
        status == this.REQUEST_STOP_STATUS) {
      this.destroy();    
    }
  } else if(state == this.ERROR_STATE) {
    if (this.activeAction == this.GET_CHECK_MAIL_INFO_ACTION &&
    	  this.tryCount < this.MAX_TRY) {
			this.activeAction = this.GET_CHECK_MAIL_INFO_ACTION;
			this.tryCount ++;
    	url = this.SERVICE_BASED_URL + '/checkmailjobinfo/' + this.userName + '/' + this.accountId + '/';
    	this.makeRequest(url, this.HTTP_GET, '', this.GET_CHECK_MAIL_INFO_ACTION);
    }
    this.updateUI(state);
    this.destroy();    
  }
};

MailServiceHandler.prototype.checkMailWrapper = function() {
  eXo.mail.MailServiceHandler.checkMail();
};

MailServiceHandler.prototype.synchImapFolders = function(isUpdateUI) {
	if (!this.accountId || !this.userName) {
	    return;
	}
		  
	this.isUpdateUI_ = isUpdateUI;
		  
	this.activeAction = this.CHECK_MAIL_ACTION;
	this.tryCount = 0;
	var url = this.SERVICE_BASED_URL + '/synchfolders/' + this.userName + '/' + this.accountId + '/';
	this.makeRequest(url, this.HTTP_GET, '', this.CHECK_MAIL_ACTION);
}

MailServiceHandler.prototype.checkMail = function(isUpdateUI, folderId) {
  if (!this.accountId ||
      !this.userName) {
    return;
  }
  
  this.isUpdateUI_ = isUpdateUI;
  
  this.activeAction = this.CHECK_MAIL_ACTION;
  this.tryCount = 0;
  eXo.core.Browser.setCookie('cs.mail.checkingmail' + this.accountId, 'true');
  var url = this.SERVICE_BASED_URL + '/checkmail/' + this.userName + '/' + this.accountId + '/';
  if (folderId) {
  	url = url + folderId + '/';
  } else {
    url = url + "checkall" + '/';
  }
  this.makeRequest(url, this.HTTP_GET, '', this.CHECK_MAIL_ACTION);
};

MailServiceHandler.prototype.stopCheckMail = function() {
  if (this.accountId) {
  	var stopLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StopCheckMail') ;
    stopLabel.style.display = 'none' ;
  	var stopingLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StopingCheckMail') ;
    stopingLabel.style.display = 'block' ;
  	
  	this.activeAction = this.STOP_CHECK_MAIL_ACTION;
    var url = this.SERVICE_BASED_URL + '/stopcheckmail/' + this.userName + '/' + this.accountId + '/';
    this.makeRequest(url, this.HTTP_GET, '', this.STOP_CHECK_MAIL_ACTION);
  }
};


MailServiceHandler.prototype.showStatusBox = function(status) {
  this.checkMailInfobarNode = document.getElementById(this.uiId);
  var statusTextNode = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StatusText');
  if (this.checkMailInfobarNode.style.display == 'none') {
	  this.checkMailInfobarNode.style.display = 'block';
  }
   if (statusTextNode && status) statusTextNode.innerHTML = status;
};

/**
 * 
 * @param {Object} status
 */
MailServiceHandler.prototype.updateUI = function(status) {
  this.checkMailInfobarNode = document.getElementById(this.uiId);
  var statusTxt = '';
  if (this.serverData.info.checkingmail.statusmsg) {
    statusTxt = this.serverData.info.checkingmail.statusmsg;
  }
  var statusTextNode = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StatusText');
  if (this.checkMailInfobarNode.style.display == 'none') {
    if (this.isUpdateUI_) {
    	this.checkMailInfobarNode.style.display = 'block';
    } 
  }
  var stopingLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StopingCheckMail') ;
  var stopLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StopCheckMail') ;
  if (stopingLabel.style.display == 'none')
	stopLabel.style.display = 'block' ;
  if (statusTxt != '') {
    statusTextNode.innerHTML = statusTxt;
  }
};

MailServiceHandler.prototype.destroy = function() {
	var st = this.serverData.info.checkingmail.status ;
  if (st == this.FINISHED_CHECKMAIL_STATUS || st == this.CONNECTION_FAILURE || st == this.RETRY_PASSWORD) {
    eXo.core.Browser.setCookie('cs.mail.checkingmail' + this.accountId, 'false');
    var stopLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StopCheckMail') ;
    
    stopLabel.style.display = 'none' ;
    if (st == this.RETRY_PASSWORD && this.checkMailInfobarNode.style.display == 'block') {
      eXo.webui.UIForm.submitForm('UIMessageList','ComfirmPassword', true) ;
    } else if (st == this.FINISHED_CHECKMAIL_STATUS){
      var refeshLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'Here');
      this.checkMailInfobarNode.style.display = 'none';
      eval(eXo.core.DOMUtil.findDescendantsByTagName(refeshLabel, 'a')[0].href.replace("%20", ""));
    } else {
      var hideLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'Hide') ;
      hideLabel.style.display = 'block' ;
      return ;
    }
  }
};

eXo.mail.MailServiceHandler = eXo.mail.MailServiceHandler || new MailServiceHandler() ;