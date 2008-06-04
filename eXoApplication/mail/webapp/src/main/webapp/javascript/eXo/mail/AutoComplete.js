/**
 * @author uocnb
 */

// ACMain
function ACMain() {
  this.dataObj = false ;
}

ACMain.prototype.init = function(dataObj, rootElement, klazz, updateOnlyNeed) {
  if (updateOnlyNeed == null) {
    updateOnlyNeed = true ;
  }
  eXo.mail.AutoComplete.ACView.init() ;
  if (this.dataObj == dataObj) {
    if (!updateOnlyNeed) {
      eXo.mail.AutoComplete.ACModel.parseData(dataObj) ;
    }
  } else {
    eXo.mail.AutoComplete.ACModel.parseData(dataObj) ;
  }
  eXo.mail.AutoComplete.ACControl.regEvent(rootElement, klazz) ;
  this.dataObj = dataObj ;
} ;

// ACModelPrototype
function ACModelPrototype() {
}

ACModelPrototype.prototype = {
  contain : function(keyword) { return false ;}
  ,
  compare : function(keyword) { return false ;}
  ,
  highLight : function(keyword) { return 'Not implement' ;}
} ;

// ACModel
function ACModel() {
  this.data = [] ;
  this.separatorChar = ',' ;
}

ACModel.prototype.init = function() {
} ;

/**
 * For initialize some temporaty data for test only
 */
ACModel.prototype.initTmpData = function() {
  for (var i=0; i<1500; i++) {
    var cnt = 0 ;
    var nameAuto = '' ;
    while(cnt<10) {
      nameAuto += String.fromCharCode((Math.random() * 26) + 65) ;
      cnt ++ ;
    }
    this.data[i] = new Contact(nameAuto, nameAuto + '@gmail.com') ;
  }
} ;

/**
 * 
 * @param {Element} dataNode
 */
ACModel.prototype.parseData = function(dataNode) {
  dataNode = (dataNode && dataNode.nodeName) ? dataNode : document.getElementById(dataNode) ;
  objList = dataNode.getElementsByTagName('contact') ;
  var tmpNameNode = false ;
  var tmpEmailNode = false ;
  for (var i=0; i < objList.length; i++) {
    tmpNameNode = objList[i].getElementsByTagName('name')[0] ;
    tmpEmailNode = objList[i].getElementsByTagName('email')[0] ;
    this.data[this.data.length] = new Contact(tmpNameNode.firstChild.nodeValue, tmpEmailNode.firstChild.nodeValue) ;
  }
} ;

/**
 * 
 * @param {String} keyword
 */
ACModel.prototype.query = function(keyword) {
  var result = [] ;
  for (var i=0; i<this.data.length; i++) {
    if (this.data[i].contain(keyword)) {
      result[result.length] = this.data[i] ;
    }
  }
  this.currentResult = result ;
  return result ;
} ;

// ACKeyboardHandler
function ACKeyboardHandler() {}

ACKeyboardHandler.prototype = new eXo.core.KeyboardListenerAPI() ;

ACKeyboardHandler.prototype.init = function() {
  var keyboard = eXo.core.Keyboard ;
  keyboard.register(eXo.mail.AutoComplete.ACKeyboardHandler) ;
  keyboard.init() ;
}

ACKeyboardHandler.prototype.finish = function() {
  eXo.core.Keyboard.finish() ;
}

ACKeyboardHandler.prototype.onDefaultCharset = function(keynum, keychar) {
  eXo.mail.AutoComplete.ACView.show(keychar) ;
} ;

ACKeyboardHandler.prototype.onAlphabet = eXo.mail.AutoComplete.ACKeyboardHandler.onDefaultCharset ;
ACKeyboardHandler.prototype.onDigit = eXo.mail.AutoComplete.ACKeyboardHandler.onDefaultCharset ;
ACKeyboardHandler.prototype.onPunctuation = eXo.mail.AutoComplete.ACKeyboardHandler.onDefaultCharset ;

ACKeyboardHandler.prototype.onUpArrow = function() {
  eXo.mail.AutoComplete.ACView.highLightPrevious() ;
  return false ;
} ;

ACKeyboardHandler.prototype.onDownArrow = function() {
  eXo.mail.AutoComplete.ACView.highLightNext() ;
  return false ;
} ;

ACKeyboardHandler.prototype.onEnter = function() {
  eXo.mail.AutoComplete.ACView.selectCurrent() ;
  return false ;
} ;

// ACView
function ACView() {
  this.displayBoxNode = document.createElement('div') ;
  with(this.displayBoxNode.style) {
    background = '#efefde' ;
    color = '#6c6c6c' ;
    border = 'solid 1px #c7c7c7' ;
    opacity = '0.85' ;
    filter = 'alpha(opacity=85)' ;
    position = 'absolute' ;
    top = '0px' ;
    left = '0px' ;
    padding = '10px' ;
    display = 'none' ;
  }
  document.body.appendChild(this.displayBoxNode) ;
  this.activeNode = false ;
  this.currentIndex = 0 ;
}

ACView.prototype.init = function(event, element) {
  element = element ? element : this ;
  this.activeNode = element ;
  eXo.mail.AutoComplete.ACView.show() ;
} ;

ACView.prototype.finish = function() {
  this.activeNode = false ;
  eXo.mail.AutoComplete.ACView.displayBoxNode.style.display = 'none' ;
} ;

ACView.prototype.show = function() {
  this.currentIndex = 0 ;
  element = this.activeNode ;
  var displayBoxNode = eXo.mail.AutoComplete.ACView.displayBoxNode ;
  displayBoxNode.innerHTML = '' ;
  displayBoxNode.style.display = 'none' ;
  var keyword = element.value ;
  if (!keyword || keyword == '') {
    return ;
  }
  var result = eXo.mail.AutoComplete.ACModel.query(keyword) ;
  if (result.length <= 0) {
    return ;
  }
  
  var contactItem = document.createElement('div') ;
  contactItem.className = '_contact_' ;
  with(contactItem.style) {
    borderBottom = 'solid 1px #6c6c6c' ;
    margin = '5px' ;
  }
  for (var i=0; i<result.length; i++) {
    var tmp = contactItem.cloneNode(false) ;
    tmp.innerHTML = result[i].highLight(keyword) ;
    displayBoxNode.appendChild(tmp) ;
  }
  var topPos = eXo.core.Browser.findPosY(element) + element.offsetHeight ;
  var leftPos = eXo.core.Browser.findPosX(element) ;
  with(displayBoxNode.style) {
    top = topPos + 'px' ;
    left = leftPos + 'px' ;
    display = 'block' ;
  }
} ;

ACView.prototype.highLightPrevious = function()  {
  this.currentIndex -- ;
  this.applyChange() ;
} ;

ACView.prototype.highLightNext = function()  {
  this.currentIndex ++ ;
  this.applyChange() ;
} ;

ACView.prototype.selectCurrent = function()  {
  this.activeNode.value = 
      this.activeNode.value + eXo.mail.AutoComplete.ACModel.result[this.currentIndex] ;
} ;

ACView.prototype.applyChange = function() {
  var contactLst = eXo.core.DOMUtil.findDescendantsByClass(this.displayBoxNode, 'div', '_contact_') ;
  for (var i=0; i<contactLst.length; i++) {
    var contact = contactLst[i] ;
    var bgColor = 'none' ;
    if (i == this.currentIndex) {
      bgColor = '#121212' ;
    }
    contact.style.background = bgColor ;
  }
}

// ACControl
function ACControl() {
  this.data = []
} ;

ACControl.prototype.regEvent = function(rootElement, klazz) {
  if (rootElement && rootElement.length > 0) {
    for (var i=0; i<rootElement.length; i++) {
      var tmpNode = document.getElementById(rootElement[i]) ;
      tmpNode.onfocus = eXo.mail.AutoComplete.ACView.init ;
      tmpNode.onblur = eXo.mail.AutoComplete.ACView.finish ;
      tmpNode.onkeyup =  eXo.mail.AutoComplete.ACView.show ;
    }
  } else {
    rootElement = (rootElement && rootElement.nodeName) ? rootElement : document.getElementById(rootElement) ;
    eList = eXo.core.DOMUtil.findDescendantsByClass(rootElement, '*', klazz) ;
    for (var i=0; i<eList.length; i++) {
      var tmpE = eList[i].getElementsByTagName('input') ;
      if (tmpE) {
        tmpE = tmpE[0] ;
      } else {
        tmpE = eList[i].getElementsByTagName('textarea') ;
        if (tmpE) {
          tmpE = tmpE[0] ;
        }
      }
      if (tmpE) {
        tmpE.onfocus = eXo.mail.AutoComplete.ACView.init ;
        tmpE.onblur = eXo.mail.AutoComplete.ACView.finish ;
        tmpE.onkeyup =  eXo.mail.AutoComplete.ACKeyboardHandler.init ;
      }
    }
  }
} ;

eXo.mail.AutoComplete = {
  ACMain : new ACMain()
  ,
  ACModelPrototype : ACModelPrototype
  ,
  ACModel : new ACModel()
  ,
  ACView : new ACView()
  ,
  ACKeyboardHandler : new ACKeyboardHandler()
  ,
  ACControl : new ACControl()
} ;

// Contact Object
function Contact(name, emailAddress) {
  this.name = name.toLowerCase() ;
  this.emailAddress = emailAddress.toLowerCase() ;
}

Contact.prototype.constructor = new eXo.mail.AutoComplete.ACModelPrototype() ;

/**
 * 
 * @param {Contact} obj
 */
Contact.prototype.compare = function(obj) {
  if (this.name == obj.name &&  this.emailAddress == obj.emailAddress) {
    return true ;
  }
  return false ;
} ;

Contact.prototype.contain = function(keyword) {
  keyword = keyword.toLowerCase() ;
  var containName = false ;
  var containEmailAddress = false ;
  if (this.name.toLowerCase().indexOf(keyword) == 0) {
    containName = true ;
  }
  if (this.emailAddress.toLowerCase().indexOf(keyword) == 0) {
    containEmailAddress = true ;
  }
  return (containName || containEmailAddress) ;
} ;

Contact.prototype.highLight = function(keyword) {
  var keywordHighLighted = '<span style="color: #000; font-weight: bold">' + keyword + '</span>' ;
  var nameTmp = this.name.replace(keyword, keywordHighLighted) ;
  var emailTmp = this.emailAddress.replace(keyword, keywordHighLighted) ;
  return ('<' + nameTmp +  '> ' + emailTmp) ;
} ;

Contact.prototype.toString = function() {
  return ('<b>name:</b> ' + this.name
          + '<br/><b>emailaddress:</b> ' + this.emailAddress) ;
} ;