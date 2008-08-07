function Reminder() {
  
} ;

Reminder.prototype.init = function(eXoUser, eXoToken){
  eXo.core.Cometd.exoId = eXoUser;
  eXo.core.Cometd.exoToken = eXoToken;
  eXo.core.Cometd.subscribe('/eXo/Application/Calendar/messages', function(eventObj) {		
		eXo.calendar.Reminder.alarm(eventObj) ;
  });
	if (!eXo.core.Cometd.isConnected()) {
     eXo.core.Cometd.init();
  }
} ;

Reminder.prototype.alarm = function(eventObj){
	var a = eXo.core.JSON.parse(eventObj.data);
	if(!a) return ;
	var n = eXo.webui.Notification ;
	var portlet = document.getElementById(eXo.calendar.UICalendarPortlet.portletName) ;
	var uiNotification = eXo.core.DOMUtil.findFirstDescendantByClass(portlet, "div", "UINotification") ;
	var uiControlInfo = eXo.core.DOMUtil.findFirstDescendantByClass(uiNotification, "div", "UIControlInfo") ;
	n.container = eXo.core.DOMUtil.findFirstDescendantByClass(portlet,"div","UIPopupNotification") ;
	if(n.message && n.message.contains("<div class='ItemLabel'>(" + a.fromDateTime.hours + ":" + a.fromDateTime.minutes + ") " +a.summary+"</div>")) {
		n.show() ;
		return ;
	}
	n.message.push("<div class='ItemLabel'>(" + a.fromDateTime.hours + ":" + a.fromDateTime.minutes + ") " +a.summary+"</div>");
	n.addMessage() ;
	n.show() ;
	n.container.onclick = n.hide ;
	uiControlInfo.onclick = n.show ;
} ;

eXo.calendar.Reminder = new Reminder() ;

function Notification(){
	this.container = null ;
	this.message = [] ;
	this.timer = null ;
	this.timeInterval = null ;
	this.startHeight = 0 ;
}

Notification.prototype.createElement = function(message){
	var a = "<a href='#' class='Item'>" + message + "</a>" ;
	return a ;
}

Notification.prototype.addMessage = function(){
	var	message = this.message ;
	var i = message.length ;
	var messageList = eXo.core.DOMUtil.findFirstDescendantByClass(this.container, "div", "MCPopupNotification") ;
	var html = "" ;
	while (i--) {
		html += this.createElement(message[i]);
	}
	messageList.innerHTML = html ;	
	this.containerHeight = message.length*33 + 30 ;
	var uiControlInfo = eXo.core.DOMUtil.findPreviousElementByTagName(this.container,"div") ;
	var controlInfoLabel = eXo.core.DOMUtil.findFirstDescendantByClass(uiControlInfo, "a", "ControlInfoLabel") ;
	controlInfoLabel.innerHTML = "("+message.length+")" ;
	//this.containerWidth = this.container.offsetWidth ;
} ;

Notification.prototype.show = function(){
	var n =	eXo.webui.Notification;
	if(n.timeInterval) clearInterval(n.timeInterval) ;
	if(n.startHeight < n.containerHeight) {
		n.container.style.visibility = "visible" ;
		n.container.style.overflowY = "hidden" ;
		n.container.style.height = n.startHeight + "px" ;	
		n.startHeight += 4;
		n.timer = setTimeout("eXo.webui.Notification.show() ;",10) ;
	} else {
		clearTimeout(n.timer) ;
		n.timeInterval = setInterval("eXo.webui.Notification.hide()",5000) ;
		n.container.style.overflowY = "visible" ;
	}
} ;

Notification.prototype.hide = function(){
	var n = eXo.webui.Notification ;
	if(n.startHeight > 0) {
		n.container.style.overflowY = "hidden" ;
		n.startHeight -= 4 ;
		n.container.style.height = n.startHeight + "px" ;		
		n.timer = setTimeout("eXo.webui.Notification.hide() ;",10) ;
		if(n.timeInterval) clearInterval(n.timeInterval) ;
	} else{
		n.container.style.visibility = "hidden" ;
		clearTimeout(n.timer) ;
		n.startHeight = 0 ;
		n.container.style.overflowY = "visible" ;
	}
} ;
function printobject(obj) {
	for(i in obj){
		console.log(i + " : " + obj[i]) ;
	}
}
eXo.webui.Notification = new Notification() ;