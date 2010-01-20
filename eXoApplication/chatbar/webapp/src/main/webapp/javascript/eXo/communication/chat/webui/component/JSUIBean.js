/**
 * @author Uoc Nguyen
 *  A javascript UI Bean like java bean implementation for javascript which can use to 
 *  event broadcast.
 */

function JSUIBean() {
}

JSUIBean.prototype = {
  // Default static variable
  _RELOAD_EVENT : 'reload',
  _RESIZE_EVENT : 'resize',
  _POSITION_EVENT : 'position',
   
  // Default options use in a common UI component
  _options : {
      width : null,
      height: null,
      top:null,
      left:null,
      visible:null,
      extra:{}
  },
  _isOnLoading : false,
  _listerners : [],
  _eventHanlders : {},
  
  /**
   * Register event call back function
   *
   * @param {String} eventName use mozilla & w3c scheme
   * @param {Function} callbackFunc
   */
  _registerEventCallback : function(eventName, callbackFunc) {
    if (callbackFunc) {
      this._eventHanlders[eventName] = this._eventHanlders[eventName] || []; 
      this._eventHanlders[eventName].push(callbackFunc);
    }
  },
  
  /**
   * Use for broadcast event
   *
   * @param {String} eventName
   * @param {Object} eventData
   */
  _eventCallback : function(eventName, eventData) {
    var eventHandler = this._eventHanlders[eventName];
    if (eventHandler &&
        eventHandler.length) {
      for ( var i = 0; i < eventHandler.length; i++) {
        eventHandler[i](eventData);
      }
    }
  },
  
  /**
   * Called when Container component is initialized to initialize itself.
   *
   * @param {HTMLElement} rootNode
   */
  _callback : function(rootNode) {
    if (rootNode) {
      this._rootNode = rootNode;
    } else {
      this._rootNode = this.rootNode;
    }
    if (this._rootNode) {
      this._rootNode.UIWindow = this;
    }
    this._register2StateMan();
    this._initUIOptions();
  },
  
  /**
   * Use to initialize all UI related action using _options data
   */
  _initUIOptions : function() {
    if (!this._rootNode ||
        this._isOnLoading ||
        this._getOption('width')) {
      return;
    }
    if (this._rootNode.style.display != 'none') {
      this._options['width'] = this._rootNode.offsetWidth + 'px';
      this._options['height'] = this._rootNode.offsetHeight + 'px';
      this._options['top'] = this._rootNode.offsetTop + 'px';
      this._options['left'] = this._rootNode.offsetLeft + 'px';
    }
  },
  
  /**
   * Use to add listener which will be called when an option is changed it's value
   *
   * @param {JSUIBeanListener} listener
   */
  _addOptionChangeEventListener : function(listener) {
    this._listerners.push(listener);
  },
  
  /**
   * Use to remove a listener from list of option change listener
   *
   * @param {JSUIBeanListener} listener
   */
  _removeOptionChangeEventListener : function(listener) {
    for ( var i = 0; i < this._listerners.length; i++) {
      if (this._listerners[i] == listener) {
        this._listerners[i] = this._listerners[this._listerners.length - 1];
        this._listerners.pop();
        return true;
      }
    }
    return false;
  },
  
  /**
   * Use to broadcast option change event will be call everytime when component's option is changed.
   *
   * @param {String} name
   * @param {Object} oldValue
   * @param {Object} newValue
   */
  _broadcastOptionChangeEvent : function(name, oldValue, newValue) {
    for ( var i = 0; i < this._listerners.length; i++) {
      var listener = this._listerners[i];
      if (listener._optionChangedEventFire) {
        listener._optionChangedEventFire(this, name, oldValue, newValue);
      }
    }
  },
  
  /**
   * Return true if component is visible
   */
  _isVisible : function() {
    return this._getOption('visible');
  },
  
  /**
   * Use to set component visible or not
   *
   * @param {Boolean} visible
   */
  _setVisible : function(visible) {
    this._setOption('visible', visible);
    visible = visible ? 'block' : 'none';
    if (this._rootNode &&
        this._rootNode.style.display != visible) {
      this._rootNode.style.display = visible;
    }
    if (visible) {
      this._initUIOptions();
    }
  },
  
  /**
   * Use to set component's size
   *
   * @param {Integer} width
   * @param {Integer} height
   */
  _setSize : function(width, height) {
    this._eventCallback(this._RESIZE_EVENT);
    this._setWidth(width);
    this._setHeight(height);
  },
  
  /**
   * Use to set component's position
   * 
   * @param {Integer} top
   * @param {Integer} left
   */
  _setPosition : function(top, left) {
    this._eventCallback(this._POSITION_EVENT);
    this._setTop(top);
    this._setLeft(left);
  },
  
  /**
   * Use to set component's top position
   * 
   * @param {Integer} top
   */
  _setTop : function(top) {
    if (top) {
      top = ((top + '').indexOf('px') != -1) ? top : (top + 'px');
      this._setOption('top', top);
      if (this._rootNode) {
        this._rootNode.style.top = top;
      }
    }
  },
  
  /**
   * Use to set component's left position
   * 
   * @param {Integer} left
   */
  _setLeft : function(left) {
    if (left) {
      left = ((left + '').indexOf('px') != -1) ? left : (left + 'px');
      this._setOption('left', left);
      if (this._rootNode) {
        this._rootNode.style.left = left;
      }
    }
  },
  
  /**
   * Use to set component's width
   * 
   * @param {Integer} width
   */
  _setWidth : function(width) {
    if (width) {
      width = ((width + '').indexOf('px') != -1) ? width : (width + 'px');
      this._setOption('width', width);
      
      if (this._rootNode) {
        var DOMUtil = eXo.core.DOMUtil;
        var resizableObjects = DOMUtil.findDescendantsByClass(this._rootNode, "div", "UIResizableBlock") ;
        var deltaX = width - this._rootNode.offsetWidth ;
        /*
        if (!isNaN(deltaX)) {
          for(var i = 0; i < resizableObjects.length; i++) {
            resizableObjects[i].style.width = resizableObjects[i].offsetWidth + 'px';
            if (resizableObjects[i].className.indexOf('HalfChange') != -1) {
              resizableObjects[i].style.width  = Math.max(10,(resizableObjects[i].offsetWidth + deltaX/2)) + "px" ;
            } else {
              resizableObjects[i].style.width  = Math.max(10,(resizableObjects[i].offsetWidth + deltaX)) + "px" ;
            }
          }
        }
        */
        
        this._rootNode.style.width = width;
      }
    }
  },
  
  /**
   * Use to set component's height
   * 
   * @param {Integer} height
   */
  _setHeight : function(height) {
    if (height) {
      height = ((height + '').indexOf('px') != -1) ? height : (height + 'px');
      this._setOption('height', height);
      
      if (this._rootNode) {
        var DOMUtil = eXo.core.DOMUtil;
        var resizableObjects = DOMUtil.findDescendantsByClass(this._rootNode, "div", "UIResizableBlock") ;
        var deltaY = height - this._rootNode.offsetHeight ;
        /*
        if (!isNaN(deltaY)) {
          for(var i = 0; i < resizableObjects.length; i++) {
            resizableObjects[i].style.height = resizableObjects[i].offsetHeight + 'px';
            if (resizableObjects[i].className.indexOf('HalfChange') != -1) {
              resizableObjects[i].style.height = Math.max(10,(resizableObjects[i].offsetHeight + deltaY/2)) + "px" ;
            } else {
              resizableObjects[i].style.height = Math.max(10,(resizableObjects[i].offsetHeight + deltaY)) + "px" ;
            }
          }
        }
        */
        
        this._rootNode.style.height = height;
      }
    }
  },
  
  /**
   * Use to broadcast event when an option is changed
   *
   * @param {String} name
   * @param {Object} value
   */
  _setOption : function (name, value) {
    if (!this._isOnLoading) {
      this._options[name] = value;
      this._broadcastOptionChangeEvent(name, this._options[name], value);
    }
  },
  
  /**
   * Use to get any component's option by name
   * 
   * @param {String} name
   */
  _getOption : function (name) {
    return this._options[name];
  },
  
  /**
   * Register with state management object to store component's options to server
   * when each option is changed.
   */
  _register2StateMan : function() {
    eXo.communication.chatbar.webui.UIStateManager.register(this);
  },
  
  /**
   * Use to reload all component's option
   */
  _reloadOptions : function() {
    this._eventCallback(this._RELOAD_EVENT);
    /*if (this._rootNode) {
      this._setVisible(this._getOption('visible'));
      this._setSize(this._getOption('width'), this._getOption('height'));
      this._setPosition(this._getOption('top'), this._getOption('left'));
    }*/
  }
};

eXo.communication.chatbar.webui.component.JSUIBean = JSUIBean;
