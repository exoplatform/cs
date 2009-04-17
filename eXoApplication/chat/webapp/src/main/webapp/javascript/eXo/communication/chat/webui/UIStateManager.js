
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

function UIStateManager() {
  this.STORE_DATA_AJAX_ACTION = 'STORE DATA AJAX ACTION';
  this.RELOAD_DATA_AJAX_ACTION = 'RELOAD DATA AJAX ACTION';
  this.DEFAULT_TIMEOUT_CHECK = 5*1000;
  this.windowList = [];
  this.autoCheckId = false;
  this.isPropertiesChanged = false;
  this.data = {};
}

UIStateManager.prototype = eXo.communication.chat.webui.component.JSUIBeanListener;

UIStateManager.prototype.init = function(userName) {
  this.userName = userName;
  this.reload();
  //this.autoCheckId = window.setInterval(this.autoStoreCheck, this.DEFAULT_TIMEOUT_CHECK);
};

UIStateManager.prototype.stopAutoStore = function() {
  if (this.autoCheckId) {
    window.clearInterval(this.autoCheckId);
    this.autoCheckId = false;
  }
};

UIStateManager.prototype._optionChangedEventFire = function(firedObject, propertyName, oldValue, newValue) {
  this.isPropertiesChanged = true;
  window.jsconsole.warn('Window option change event fired: ' + firedObject + ', property name: ' + propertyName + ' old value: ' + oldValue + '  new value: ' + newValue);
  window.jsconsole.debug('before: ', this.data);
  for ( var i = 0; i < this.windowList.length; i++) {
    var windowObj = this.windowList[i];
    if (windowObj == firedObject) {
      this.data[windowObj.id] = windowObj._options;
    }
  }
  window.jsconsole.debug('after: ', this.data);
  if (!this.autoCheckId) {
    this.store();
  }
};

UIStateManager.prototype.register = function(windowObj) {
  this.windowList.push(windowObj);
  windowObj._addOptionChangeEventListener(this);
};

UIStateManager.prototype.autoStoreCheck = function() {
  eXo.communication.chat.webui.UIStateManager.checkStore();
};

UIStateManager.prototype.checkStore = function() {
  if (this.isPropertiesChanged) {
    this.store();
  }
};

UIStateManager.prototype.store = function(data) {
  if (!this.userName) {
    return;
  }
  // Do upload state data to server using url: /portal/rest/chat/uistateservice/save/{user}/{data}
  var url = '/portal/rest/chat/uistateservice/save/' + this.userName;// + '/';
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

UIStateManager.prototype.reload = function() {
  // Do get state data to server using url: /portal/rest/chat/uistateservice/get/{user}/
  var url = '/portal/rest/chat/uistateservice/get/' + this.userName;
  var handler = new AjaxHandler(this, this.RELOAD_DATA_AJAX_ACTION);
  this.ajaxWrapper(handler, url, 'GET');
  this.isPropertiesChanged = false;
};

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

UIStateManager.prototype.reloadAllWindows = function() {
  for ( var i = 0; i < this.windowList.length; i++) {
    var windowData = this.data[this.windowList[i].id];
    if (windowData) {
      this.windowList[i]._options = windowData;
      this.windowList[i]._reloadOptions();
    }
  }
};

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

UIStateManager.prototype.ajaxWrapper =function(handler, url, method, data) {
  var request = new eXo.portal.AjaxRequest(method, url, data);
  this.initAjaxRequest(request, handler);
  this.ajaxProcessOverwrite(true, request);
//  request.process() ;
};

eXo.communication.chat.webui.UIStateManager = new UIStateManager();
