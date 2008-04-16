/**
 * @author Uoc Nguyen
 */
function MailServiceHandler() {
  this.START_CHECKMAIL_STATUS = 101;
  this.DOWNLOADING_MAIL_STATUS = 150;
  this.NO_UPDATE_STATUS = 201;
  this.FINISHED_CHECKMAIL_STATUS = 200;
  this.REQUEST_STOP_STATUS = 202;
  this.SERVICE_BASED_URL = '/portal/rest/cs/mail';
}

MailServiceHandler.prototype = new eXo.cs.webservice.core.WebserviceHandler();

MailServiceHandler.prototype.initService = function(uiId, userName, accountId, checkMailInterval) {
  this.checkMailInterval = checkMailInterval;
  this.uiId = uiId;
  this.accountId = accountId;
  this.userName = userName;
  if (this.checkMailInterval &&
      !isNaN(this.checkMailInterval)) {
    this.checkMailInterval = parseInt(this.checkMailInterval);
    window.setInterval(eXo.mail.MailServiceHandler.checkMailWrapper, this.checkMailInterval);
  }
  if (eXo.core.Browser.getCookie('cs.mail.checkingmail') == 'true') {
    this.checkMail();
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
    if (!this.serverData.info.checkingmail ||
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
      this.makeRequest(url, this.HTTP_GET, 'get check mail information');
    }
    if (status != this.NO_UPDATE_STATUS) {
      this.updateUI(status);
    }
    if (status == this.FINISHED_CHECKMAIL_STATUS ||
        status == this.REQUEST_STOP_STATUS) {
      this.destroy();    
    }
  } else if(state == this.ERROR_STATE) {
    this.updateUI(state);
    this.destroy();    
  }
};

MailServiceHandler.prototype.checkMailWrapper = function() {
  eXo.mail.MailServiceHandler.checkMail();
};

MailServiceHandler.prototype.checkMail = function() {
  if (!this.accountId ||
      !this.userName) {
    return;
  }
  eXo.core.Browser.setCookie('cs.mail.checkingmail', 'true');
  var url = this.SERVICE_BASED_URL + '/checkmail/' + this.userName + '/' + this.accountId + '/';
  this.makeRequest(url, this.HTTP_GET, 'check mail');
};

MailServiceHandler.prototype.stopCheckMail = function() {
  if (this.accountId) {
    var url = this.SERVICE_BASED_URL + '/stopcheckmail/' + this.userName + '/' + this.accountId + '/';
    this.makeRequest(url, this.HTTP_GET, 'Stop check mail');
  }
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
    this.checkMailInfobarNode.style.display = 'block';
  }
  if (statusTxt != '') {
    statusTextNode.innerHTML = statusTxt;
  }
};

MailServiceHandler.prototype.destroy = function() {
  if (this.serverData.info.checkingmail.status == this.FINISHED_CHECKMAIL_STATUS) {
    eXo.core.Browser.setCookie('cs.mail.checkingmail', 'false');
  }
};

eXo.mail.MailServiceHandler = new MailServiceHandler() ;