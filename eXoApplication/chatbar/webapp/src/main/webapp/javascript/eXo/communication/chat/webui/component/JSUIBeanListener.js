/**
 * @author Uoc Nguyen
 * @email uoc.nguyen@exoplatform.com
 * @desc This object is skeloton object will be extends by another UI component object
 * which want to implement JSUIBean model.
 */

function JSUIBeanListener() {
}

JSUIBeanListener.prototype = {
  /**
   * This method will be overwritten by implement object's method
   *
   * @param {Object} firedObject
   * @param {String} propertyName
   * @param {Object} oldValue
   * @param {Object} newValue
   */
  _optionChangedEventFire : function(firedObject, propertyName, oldValue, newValue) {}
};

eXo.communication.chatbar.webui.component.JSUIBeanListener = JSUIBeanListener.prototype;
