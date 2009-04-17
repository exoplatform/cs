/**
 * @author Uoc Nguyen
 *         email: uoc.nguyen@exoplatform.com
 * 
 * This object will provide some advanced DOM Event function
 */
function AdvancedDOMEvent() {
}

/**
 * Using this function to simulate a mouse event fire on any DOM Element
 *
 * @param {Element} targetObj
 * @param {String} eventName mouse event name: click, mousedown, mouseup, mousemove, mouseover, mouseout.
 */
AdvancedDOMEvent.prototype.fireMouseEventTo = function(targetObj, eventName, isBubble) {
  if (document.createEvent) {
    var eventObj = document.createEvent("MouseEvents");
    eventObj.initEvent(eventName, true, isBubble);
    targetObj.dispatchEvent(eventObj);
  } else if (document.createEventObject) {
    targetObj.fireEvent("on" + eventName);
  }
};

AdvancedDOMEvent.prototype.addEventListener = function(node, eventType, handler, allowBubble) {
  if ((!node ||
      !node.nodeName) &&
      node != window) {
    throw (new Error('Can not add event listener for null or not DOM Element object'));
  }
  if (node.addEventListener) {
    node.addEventListener(eventType, handler, allowBubble);
  } else {
    node.attachEvent('on' + eventType, handler, allowBubble);
  }
};

AdvancedDOMEvent.prototype.removeEventListener = function(node, eventType, handler, allowBubble) {
  if ((!node||
      !node.nodeName) &&
      node != window){
    throw (new Error('Can not remove event listener for null or not DOM Element object'));
  }
  if (node.addEventListener) {
    node.removeEventListener(eventType, handler, allowBubble);
  } else {
    node.detachEvent('on' + eventType, handler, allowBubble);
  }
};

AdvancedDOMEvent.prototype.cancelEvent = function(event) {
  if (event) {
    if (event.preventDefault) {
      event.preventDefault();
      event.stopPropagation();
    } else {
      event.cancelBubble = true;
    }
  }
};

eXo.communication.chat.core.AdvancedDOMEvent = new AdvancedDOMEvent();
