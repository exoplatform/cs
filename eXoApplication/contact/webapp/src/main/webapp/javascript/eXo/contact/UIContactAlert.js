function UIContactAlert() {
  
} ;

UIContactAlert.prototype.init = function(eXoUser, eXoToken){
  eXo.core.Cometd.exoId = eXoUser;
  eXo.core.Cometd.exoToken = eXoToken;
  eXo.core.Cometd.subscribe('/eXo/Application/Contact/messages', function(eventObj) {		
		eXo.contact.UIContactAlert.alarm(eventObj) ;
  });
  //eXo.core.Cometd.addOnConnectionReadyCallback(this.initCometd);
	if (!eXo.core.Cometd.isConnected()) {
     eXo.core.Cometd.init();
  }
} ;

UIContactAlert.prototype.initCometd = function() {
	 eXo.core.Cometd.subscribe('/eXo/Application/Contact/messages', function(eventObj) {		
		eXo.contact.UIContactAlert.alarm(eventObj) ;
  });
}

UIContactAlert.prototype.alarm = function(eventObj){
	var a = eXo.core.JSON.parse(eventObj.data);	
	var message = '<a class="Item" href="#">('+ a.fromDateTime.hours + ':' + a.fromDateTime.minutes + ') ' +a.summary+'</a>' ;
	var html = this.generateHTML(message) ;
	var popup = eXo.core.DOMUtil.findFirstDescendantByClass(this.createMessage(html), "div","UIPopupNotification") ;
	eXo.webui.Box.config(popup,popup.offsetHeight, 5, this.openCallback, this.closeBox) ;
	window.focus() ;
	return ;
} ;

UIContactAlert.prototype.openCallback = function(obj){
	obj.onclick = function(){
		this.style.visibility = "hidden" ;
	} ;
}
UIContactAlert.prototype.closeBox = function(obj){
	obj.style.visibility = "hidden" ;
}

UIContactAlert.prototype.createMessage = function(html){
	var msgBox = null ;
	if(document.getElementById("msgBox")) {
		msgBox = document.getElementById("msgBox") ;
		msgBox.innerHTML = html ;
	} else {
		msgBox = document.createElement("div") ;
		msgBox.id = "msgBox" ;
		msgBox.className = "UINotification" ;
		msgBox.innerHTML = html ;
		document.body.appendChild(msgBox) ;
	}
	return msgBox ;
} ;

UIContactAlert.prototype.generateHTML = function(message){
	
	
	var html = '' ;
	html += '<div class="UIPopupNotification">';
	html += '	<div class="TLPopupNotification">';
	html += '		<div class="TRPopupNotification">';
	html += '			<div class="TCPopupNotification"><span></span></div>';
	html += '		</div>';
	html += '	</div>';
	html += '	<div class="MLPopupNotification">';
	html += '		<div class="MRPopupNotification">';
	html += '			<div class="MCPopupNotification">';
	html += '				<div class="TitleNotification">';
	html += '					<a class="ItemTitle" href="#">Notification</a>';
	html += '					<a class="Close" href="#"><span></span></a>';
	html += '				</div>';
	html += 				message;
	html += '			</div>';
	html += '		</div>';
	html += '	</div>';
	html += '	<div class="BLPopupNotification">';
	html += '		<div class="BRPopupNotification">';
	html += '			<div class="BCPopupNotification"><span></span></div>';
	html += '		</div>';
	html += '	</div>';
	html += '</div>';
	return html ;
} ;

// Box effect
function Box(){
	this.speed = 4 ;
	this.tmpHeight = 0 ;
	this.autoClose = true ;
	this.closeInterval = 10 ;
}

Box.prototype.config = function(obj, height, speed, openCallback, closeCallback) {
	this.object = obj;
	this.maxHeight = height ;
	if(speed) this.speed = speed ;
	this.open() ;
	if(openCallback) this.openCallback = openCallback ;
	if(closeCallback) this.closeCallback = closeCallback ;
}

Box.prototype.open = function(){
	var Box = eXo.webui.Box ;
	if(Box.tmpHeight < Box.maxHeight){
		Box.object.style.overflow = "hidden" ;
		Box.object.style.visibility = "visible" ;
		Box.object.style.height = Box.tmpHeight + "px" ;
		Box.tmpHeight += Box.speed ;
		Box.timer = window.setTimeout(Box.open,10) ;
	} else {
		Box.object.style.overflow = "visible" ;
		Box.tmpHeight = Box.maxHeight ;
		if(Box.timer) window.clearTimeout(Box.timer) ;
		if(Box.closeTimer)  window.clearInterval(Box.closeTimer) ;
		if(Box.autoClose) Box.closeTimer = window.setInterval(Box.close,Box.closeInterval*1000) ;
		Box.openCallback(Box.object) ;
		return ;
	}
}

Box.prototype.close = function(){
	var Box = eXo.webui.Box ;	
	if(Box.tmpHeight >= 0){
		Box.object.style.overflow = "hidden" ;
		Box.object.style.height = Box.tmpHeight + "px" ;
		Box.tmpHeight -= Box.speed ;
		Box.timer = window.setTimeout(Box.close,10) ;
	} else {
		Box.object.style.overflow = "visible" ;
		Box.object.style.visibility = "hidden" ;
		Box.tmpHeight = 0 ;
		Box.object.style.height = Box.tmpHeight + "px" ;
		if(Box.timer) window.clearTimeout(Box.timer) ;
		if(Box.closeTimer)  window.clearInterval(Box.closeTimer) ;
		Box.closeCallback(Box.object) ;
		return ;
	}
}

eXo.webui.Box = new Box() ;
eXo.contact.UIContactAlert = new UIContactAlert() ;