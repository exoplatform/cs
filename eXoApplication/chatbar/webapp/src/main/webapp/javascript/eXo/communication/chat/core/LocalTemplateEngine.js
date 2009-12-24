/**
 * @author Uoc Nguyen
 */
function LocalTemplateEngine() {}

LocalTemplateEngine.prototype.init = function(rootNode) {
  this.rootNode = (typeof(rootNode) == 'string') ? document.getElementById(rootNode) : rootNode;
  this.tmplList = this.rootNode.getElementsByTagName('*');
};

/**
 * Return a html template block by class name.
 * 
 * @param {String} klazz
 * 
 * @return {Element}
 */
LocalTemplateEngine.prototype.getTemplateByClassName = function(klazz) {
  for (var i=0; i<this.tmplList.length; i++) {
    if (this.tmplList[i].className &&
        this.tmplList[i].className.indexOf(klazz) != -1) {
      return this.tmplList[i].cloneNode(true);
    }
  }
  return false;
};

eXo.communication.chatbar.core.LocalTemplateEngine = new LocalTemplateEngine();
