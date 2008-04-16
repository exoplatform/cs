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

MailServiceHandler.prototype.initService = function(uiId, accountId, checkMailInterval) {
  this.checkMailInterval = checkMailInterval;
  this.checkMailInfobarNode = document.getElementById(uiId);
  this.accountId = accountId;
  if (this.checkMailInterval &&
      !isNaN(this.checkMailInterval)) {
    this.checkMailInterval = parseInt(this.checkMailInterval);
    window.setInterval(eXo.mail.MailServiceHandler.checkMailWrapper, this.checkMailInterval);
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
        status == this.NO_UPDATE_STATUS ||
        status == this.REQUEST_STOP_STATUS) {
      url = this.SERVICE_BASED_URL + '/checkmailjobinfo/' + this.accountId + '/';
    }
    if (url && url != '') {
      this.makeRequest(url, this.HTTP_GET, 'get check mail information');
    }
    if (status != this.NO_UPDATE_STATUS) {
      this.updateUI(status);
    }
  } else if(state == this.ERROR_STATE) {
    this.updateUI(state);
  }
};

MailServiceHandler.prototype.checkMailWrapper = function() {
  eXo.mail.MailServiceHandler.checkMail();
};

MailServiceHandler.prototype.checkMail = function() {
  if (!this.accountId ||
      !this.checkMailInfobarNode ||
      !this.checkMailInfobarNode.parentNode) {
    return;
  }
  var url = this.SERVICE_BASED_URL + '/checkmail/' + this.accountId + '/';
  this.makeRequest(url, this.HTTP_GET, 'check mail');
};

MailServiceHandler.prototype.stopCheckMail = function() {
  if (this.accountId) {
    var url = this.SERVICE_BASED_URL + '/stopcheckmail/' + this.accountId + '/';
    this.makeRequest(url, this.HTTP_GET, 'Stop check mail');
  }
};

/**
 * 
 * @param {Object} status
 */
MailServiceHandler.prototype.updateUI = function(status) {
  var statusTxt = this.serverData.info.checkingmail.statusMsg;
  /*
  switch (status) {
    case this.START_CHECKMAIL_STATUS:
      statusTxt += 'Connecting...';
      break;
    case this.DOWNLOADING_MAIL_STATUS:
      statusTxt += 'Fetching message ' + this.serverData.info.checkingmail.completed + '/' + this.serverData.info.checkingmail.total;
      break;
    case this.FINISHED_CHECKMAIL_STATUS:
      statusTxt += 'Check mail completed';
      break;
    case this.ERROR_STATE:
      statusTxt += 'Server error while checking mail';
  }
  */
  var statusTextNode = eXo.core.DOMUtil.findFirstDescendantByClass(this.checkMailInfobarNode, 'div', 'StatusText');
  if (this.checkMailInfobarNode.style.display == 'none') {
    this.checkMailInfobarNode.style.display = 'block';
  }
  statusTextNode.innerHTML = statusTxt;
};

eXo.mail.MailServiceHandler = new MailServiceHandler() ;