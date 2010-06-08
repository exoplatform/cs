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
  this.COMMON_ERROR = 104;
  
  this.START_SYNC_FOLDER = 301;
  this.FINISH_SYNC_FOLDER = 302;
  
  this.SERVICE_BASED_URL = (eXo.cs.restContext)?eXo.env.portal.context+ '/' + eXo.cs.restContext +'/cs/mail':'portal/rest/cs/mail';
  this.CHECK_MAIL_ACTION = 'check mail action';
  this.SYNCH_FOLDER_ACTION = 'synchronize folder action';
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

MailServiceHandler.prototype.updateCheckMailStatus = function(obj) {
	var data = eXo.core.JSON.parse(obj.data);
	var status = data.status;
	var previousStatus = data.previousStatus;
	var statusMsg = data.statusMsg;
	var stopLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StopCheckMail') ;
	var stoppingLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StopingCheckMail') ;
	var warningLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'WarningMessage') ;
	var loadingIcon = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'LoadingIcon');
	var statusText = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StatusText');
	console.log(status);
	if (status != this.FINISHED_CHECKMAIL_STATUS && this.checkMailInfobarNode.style.display == 'none') {
		this.checkMailInfobarNode.style.display = 'block';
	}
	switch (status) {
	case this.START_SYNC_FOLDER:	
		document.getElementById('SynchronizeIconRefreshFolder').className = "SyncingIcon";
		break;
	case this.FINISH_SYNC_FOLDER:
		document.getElementById('SynchronizeIconRefreshFolder').className = "SyncIcon"; 
  	var updateImapFolder = document.getElementById("UpdateImapFolder");
    if (updateImapFolder != null) {
  		eval(eXo.core.DOMUtil.findDescendantsByTagName(updateImapFolder, 'a')[0].href.replace("%20", ""));
    }
	  //this.destroy();
		break;
	case this.START_CHECKMAIL_STATUS:
		//this status when server start checking mail.
		break;
	case this.COMMON_ERROR:
	case this.CONNECTION_FAILURE:	   
	case this.RETRY_PASSWORD:
		// this status indicates that can not connect to mail server due to authentication error. 
		// We will show status message as error notice and show 'retry password' form.
	  warningLabel.style.display = "block";
	  stopLabel.style.display = "none";
	  loadingIcon.style.display = 'none';
	  statusText.style.display = 'none';
	  if(status == this.RETRY_PASSWORD){
			eXo.webui.UIForm.submitForm('UIMessageList','ComfirmPassword', true) ;			
		}
		return;
	case this.FINISHED_CHECKMAIL_STATUS:
		// this status notices that the checking mail task has finished.
		// we will show finish checking mail status and hide the status bar automatically.
		stopLabel.style.display = 'none';
		loadingIcon.style.display = 'none';
		stoppingLabel.style.display = 'none';
		warningLabel.style.display = 'none';
		statusText.style.display = 'block';
		closeFetchingBar();
		break;
	}
	// code for test. Show status message on status bar.
	eXo.mail.MailServiceHandler.showStatusBox(data.statusMsg);
	function closeFetchingBar(){
		setTimeout("eXo.mail.MailServiceHandler.checkMailInfobarNode.style.display='none';",60000);
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
	return;
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
    if (status == this.START_SYNC_FOLDER || status == this.FINISH_SYNC_FOLDER) {
	    if (status == this.START_SYNC_FOLDER) {	    	
	    	document.getElementById('SynchronizeIconRefreshFolder').className = "SyncingIcon"; 
	    } else if (status == this.FINISH_SYNC_FOLDER) {
	    	document.getElementById('SynchronizeIconRefreshFolder').className = "SyncIcon"; 
	    	var updateImapFolder = document.getElementById("UpdateImapFolder");
	  	      if (updateImapFolder != null) {
	  	    	eval(eXo.core.DOMUtil.findDescendantsByTagName(updateImapFolder, 'a')[0].href.replace("%20", ""));
	  	      }
	  	    this.destroy();
	  	    return;
	    }
    }
    
    var url = false;
    if (status == this.START_CHECKMAIL_STATUS ||
        status == this.DOWNLOADING_MAIL_STATUS ||
        status == this.NO_UPDATE_STATUS) {
      url = this.SERVICE_BASED_URL + '/checkmailjobinfo/' + this.userName + '/' + this.accountId + '/';
    }
    if (url && url != '') {
      this.activeAction = this.GET_CHECK_MAIL_INFO_ACTION;
      //this.makeRequest(url, this.HTTP_GET, '', this.GET_CHECK_MAIL_INFO_ACTION);
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
    	if (this.serverData.info.checkingmail.statusmsg) {
    	    var warningLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'WarningMessage') ;
    	    warningLabel.innerHTML = this.serverData.info.checkingmail.statusmsg;
    	  }
      this.destroy();    
    }
  } else if(state == this.ERROR_STATE) {
    if (this.activeAction == this.GET_CHECK_MAIL_INFO_ACTION && this.tryCount < this.MAX_TRY) {
		this.activeAction = this.GET_CHECK_MAIL_INFO_ACTION;
		this.tryCount ++;
    	url = this.SERVICE_BASED_URL + '/checkmailjobinfo/' + this.userName + '/' + this.accountId + '/';
    	//this.makeRequest(url, this.HTTP_GET, '', this.GET_CHECK_MAIL_INFO_ACTION);
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
		  
	this.activeAction = this.SYNCH_FOLDER_ACTION;
	this.tryCount = 0;
	var url = this.SERVICE_BASED_URL + '/synchfolders/' + this.userName + '/' + this.accountId + '/';
	this.makeRequest(url, this.HTTP_GET, '', this.SYNCH_FOLDER_ACTION);
}

/**
 * reset visible status of fetching bar as created by template file.
 */
MailServiceHandler.prototype.resetStatusBar = function() {
	if (this.checkMailInfobarNode != null) {
		eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode,
				'div', 'LoadingIcon').style.display = 'block';
		eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode,
				'div', 'StatusText').style.display = 'block';
		eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode,
				'div', 'StopCheckMail').style.display = 'block';
		eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode,
				'div', 'StopingCheckMail').style.display = 'none';
		eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode,
				'div', 'Here').style.display = 'none';
		eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode,
				'div', 'WarningMessage').style.display = 'none';
		eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode,
				'div', 'UpdateImapFolder').style.display = 'none';
	}
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
	var statusText = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StatusText');
	statusText.style.display = 'none';
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
  if (status == this.START_CHECKMAIL_STATUS) {
	  this.resetStatusBar();
  }
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
    
//    var list = eXo.core.DOMUtil.getChildrenByTagName(this.checkMailInfobarNode, 'div');
//    var length = list.length;
//    for (var i = 0; i < length; i++) {
//    	list[i].style.display = "none";
//    }
    
    var stopLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StopCheckMail') ;
    var warningLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'WarningMessage') ;
    
    stopLabel.style.display = 'none' ;
    if ((st == this.RETRY_PASSWORD || st == this.CONNECTION_FAILURE) && this.checkMailInfobarNode.style.display == 'block') {
       warningLabel.style.display = "block";
       stopLabel.style.display = "none";
       eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'LoadingIcon').style.display = 'none';
       eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StatusText').style.display = 'none';       
       if (st == this.RETRY_PASSWORD) {
    	   // if password is wrong, confirm password form is opened.
    	   eXo.webui.UIForm.submitForm('UIMessageList','ComfirmPassword', true) ;
    	   
       }
      
    } else if (st == this.FINISHED_CHECKMAIL_STATUS){
      var refeshLabel = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'Here');
      eval(eXo.core.DOMUtil.findDescendantsByTagName(refeshLabel, 'a')[0].href.replace("%20", ""));
    } else {
    	if (this.checkMailInfobarNode) this.checkMailInfobarNode.style.display = 'none';
    }
  }
  if (this.checkMailInfobarNode && st == this.FINISHED_CHECKMAIL_STATUS) this.checkMailInfobarNode.style.display = 'none';
};

eXo.mail.MailServiceHandler = eXo.mail.MailServiceHandler || new MailServiceHandler() ;