/**
 * @author Uoc Nguyen
 *  email uoc.nguyen@exoplatform.com
 *
 *  This object is using to manage state of UI component by store it in JCR using REST service UIStateService
 */

/**
 * AjaxHandler object to handle ajax request and call back to UIStateManager object
 */
function AjaxHandler(callbackObject, action) {
  this.LOADING_STATE = 'LOADING';
  this.SUCCESS_STATE = 'SUCCESS';
  this.ERROR_STATE = 'ERROR';
  this.TIMEOUT_STATE = 'TIMEOUT';
  this.action = action;
  if (callbackObject &&
      callbackObject._ajaxUpdate) {
    this.callbackObject = callbackObject;
  } else {
    this.callbackObject = false;
  }
}

AjaxHandler.prototype.onLoading = function(requestObj) {
  window.jsconsole.info('[' + this.handler.action + '] ' + this.handler.LOADING_STATE);
  if (!this.handler.callbackObject) return;
  this.handler.callbackObject._ajaxUpdate(this.handler, this.handler.LOADING_STATE, requestObj, this.handler.action);
};

AjaxHandler.prototype.onSuccess = function(requestObj) {
  window.jsconsole.info('[' + this.handler.action + '] ' + this.handler.SUCCESS_STATE);
  if (!this.handler.callbackObject) return;
  this.handler.callbackObject._ajaxUpdate(this.handler, this.handler.SUCCESS_STATE, requestObj, this.handler.action);
};

AjaxHandler.prototype.onError = function(requestObj) {
  window.jsconsole.info('[' + this.handler.action + '] ' + this.handler.ERROR_STATE);
  if (!this.handler.callbackObject) return;
  this.handler.callbackObject._ajaxUpdate(this.handler, this.handler.ERROR_STATE, requestObj, this.handler.action);
};

AjaxHandler.prototype.onTimeout = function(requestObj) {
  window.jsconsole.info('[' + this.handler.action + '] ' + this.handler.TIMEOUT_STATE);
  if (!this.handler.callbackObject) return;
  this.handler.callbackObject._ajaxUpdate(this.handler, this.handler.TIMEOUT_STATE, requestObj, this.handler.action);
};

/**
 * UIStateManager
 */
function UIStateManager() {
  this.STORE_DATA_AJAX_ACTION = 'STORE DATA AJAX ACTION';
  this.RELOAD_DATA_AJAX_ACTION = 'RELOAD DATA AJAX ACTION';
  this.DEFAULT_TIMEOUT_CHECK = 1000;
  this.windowList = [];
  this.autoCheckId = false;
  this.isPropertiesChanged = false;
  this.data = {};
}

/**
 * Extends from JSUIBeanListener
 */
UIStateManager.prototype = eXo.communication.chatbar.webui.component.JSUIBeanListener;

/**
 * Initializing method
 *
 * @param {String} userName
 */
UIStateManager.prototype.init = function(userName) {
  this.userName = userName;
  this.reload();
  this.autoCheckId = window.setInterval(this.autoStoreCheck, this.DEFAULT_TIMEOUT_CHECK);
};

/**
 * Stop auto store data by interval
 */
UIStateManager.prototype.stopAutoStore = function() {
  if (this.autoCheckId) {
    window.clearInterval(this.autoCheckId);
    this.autoCheckId = false;
  }
};

/**
 * Overwritten method use to store UI state each time when UI component is changed
 *
 * @param {Object} firedObject
 * @param {String} propertyName
 * @param {Object} oldValue
 * @param {Object} newValue
 */
UIStateManager.prototype._optionChangedEventFire = function(firedObject, propertyName, oldValue, newValue) {
  this.isPropertiesChanged = true;
  window.jsconsole.warn('Window option change event fired: ' + firedObject + ', property name: ' + propertyName + ' old value: ' + oldValue + '  new value: ' + newValue);
  window.jsconsole.debug('before: ', this.data);
  this.data[firedObject.id] = firedObject._options;
  window.jsconsole.debug('after: ', this.data);
  if (!this.autoCheckId) {
    this.store();
  }
};

/**
 * Used by an UI component who want to register to use UIStateService to store UI state
 *
 * @param {WindowManager} windowObject
 */
UIStateManager.prototype.register = function(windowObj) {
  this.windowList.push(windowObj);
  windowObj._addOptionChangeEventListener(this);
};

/**
 * Wrapper method
 */
UIStateManager.prototype.autoStoreCheck = function() {
  eXo.communication.chatbar.webui.UIStateManager.checkStore();
};

/**
 * Check before do an automatic store job
 */
UIStateManager.prototype.checkStore = function() {
  if (this.isPropertiesChanged) {
    this.store();
  }
};

/**
 * Call service to store UI state data
 *
 * @param {JSonData} data
 */
UIStateManager.prototype.store = function(data) {
  if (!this.userName) {
    return;
  }
  // Do upload state data to server using url: /chatbar/messengerservlet/uistateservice/save/{user}/
  var url = '/chatbar/messengerservlet/uistateservice/save/' + this.userName;// + '/';
  var handler = new AjaxHandler(this, this.STORE_DATA_AJAX_ACTION);
  if (data) {
    this.ajaxWrapper(handler, url, 'POST', data);
  } else {
    var data = eXo.core.JSON.stringify(this.data);
    data = {data:data};
    this.ajaxWrapper(handler, url, 'POST', eXo.core.JSON.stringify(data));
  }
  this.isPropertiesChanged = false;
};

/**
 * Reload UI state. Get new UI state data from service.
 */
UIStateManager.prototype.reload = function() {
  // Do get state data to server using url: /chatbar/messengerservlet/uistateservice/get/{user}/
  var url = '/chatbar/messengerservlet/uistateservice/get/' + this.userName;
  var handler = new AjaxHandler(this, this.RELOAD_DATA_AJAX_ACTION);
  this.ajaxWrapper(handler, url, 'GET');
  this.isPropertiesChanged = false;
};

/**
 * Call back handler when ajax request is done
 *
 * @param {AjaxHandler} ajaxHandler
 * @param {String} state
 * @param {HTTPXmlRequest} requestObject
 * @param {String} action
 */
UIStateManager.prototype._ajaxUpdate = function(ajaxHandler, state, requestObject, action) {
  switch (state) {
    case ajaxHandler.LOADING_STATE:
      break;
    case ajaxHandler.SUCCESS_STATE:
      if (action == this.STORE_DATA_AJAX_ACTION) {
        return;
      }
      var _data;
      if (requestObject.responseText) {
        try {
          _data = eXo.core.JSON.parse(requestObject.responseText);
          if (_data &&
              _data.data != '') {
            _data = eXo.core.JSON.parse(_data.data);
          } else {
            _data = null;
          }
          window.jsconsole.dir(_data);
        } catch (e) {
          window.jsconsole.error('JSON parser exception');
        }
      }
      if (_data) {
        this.data = _data;
        if (action == this.RELOAD_DATA_AJAX_ACTION) {
          this.reloadAllWindows();
        }
      }
      break;
    case ajaxHandler.ERROR_STATE:
      break;
    case ajaxHandler.TIMEOUT_STATE:
      break;
    default:
      break;
  }
};

/**
 * Reload all UI component after got UI state data from service
 */
UIStateManager.prototype.reloadAllWindows = function() {
  for ( var i = 0; i < this.windowList.length; i++) {
    var windowData = this.data[this.windowList[i].id];
    if (windowData) {
      this.windowList[i]._options = windowData;
      this.windowList[i]._reloadOptions();
    }
  }
};

/**
 * Ajax process overwritten to set some private parameters
 *
 * @param {Boolean} manualMode
 * @param {AjaxRequest} ajaxRequest
 */
UIStateManager.prototype.ajaxProcessOverwrite = function(manualMode, ajaxRequest) {
  if (ajaxRequest.request == null) return ;
  ajaxRequest.request.open(ajaxRequest.method, ajaxRequest.url, true) ;   
  if (!manualMode) {
    if (ajaxRequest.method == "POST") {
      ajaxRequest.request.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8") ;
    } else {
      ajaxRequest.request.setRequestHeader("Content-Type", "text/plain;charset=UTF-8") ;
    }
  } else {
    ajaxRequest.request.setRequestHeader("Content-Type", "text/xml;charset=UTF-8") ;
  }
  
  if (ajaxRequest.timeout > 0) setTimeout(ajaxRequest.onTimeoutInternal, ajaxRequest.timeout) ;
  
  ajaxRequest.request.send(ajaxRequest.queryString) ;
};

/**
 * Common initialize process for an ajax request
 *
 * @param {AjaxRequest} ajaxRequest
 * @param {Object} handler
 */
UIStateManager.prototype.initAjaxRequest = function(ajaxRequest, handler) {
  ajaxRequest.onSuccess = handler.onSuccess ;
  ajaxRequest.onLoading = handler.onLoading ;
  ajaxRequest.onTimeout = handler.onTimeout ;
  ajaxRequest.onError = handler.onError ;
  ajaxRequest.callBack = handler.callBack ;
  ajaxRequest.handler = handler;
  this.currentRequest = ajaxRequest ;
};

/**
 * Wrapper do a ajax request
 *
 * @param {AjaxHandler} handler
 * @param {String} url
 * @param {Function} method
 * @param {String} data
 */
UIStateManager.prototype.ajaxWrapper = function(handler, url, method, data) {
  var request = new eXo.portal.AjaxRequest(method, url, data);
  this.initAjaxRequest(request, handler);
  this.ajaxProcessOverwrite(true, request);
//  request.process() ;
};

eXo.communication.chatbar.webui.UIStateManager = new UIStateManager();
