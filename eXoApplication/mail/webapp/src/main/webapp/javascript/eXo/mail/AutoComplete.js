/**
 * @author Hoang Manh Dung
 */

function AutoComplete(){
	this.REST_URL = eXo.env.portal.context + "/rest/cs/mail/searchemail/";
};
/**
 * Register handler for input
 * @param {Array} ids is the array of input ids
 */
AutoComplete.prototype.init = function(ids){
	var i = ids.length ;
	while(i--){
		document.getElementById(ids[i]).onkeyup = this.pressHandler ;
	}
};

AutoComplete.prototype.pressHandler = function(evt){
	var me = eXo.mail.AutoComplete;
	var keyNum = me.captureKey(evt);
	switch(keyNum){
		case 13: me.enterHandler(evt, this); break;
		case 27: me.escapeHandler(evt, this); break;
		case 38: me.arrowUpHandler(evt, this); break;
		case 40: me.arrowDownHandler(evt, this); break;
		default: me.typeHandler(evt, this);
	}
	return; 
};

AutoComplete.prototype.makeRequest = function(url,callback){
	var request =  eXo.core.Browser.createHttpRequest() ;
	request.open('GET', url, false) ;
	request.setRequestHeader("Cache-Control", "max-age=86400") ;
	request.send(null) ;
	if(callback) callback(request.responseText) ;
};
/**
 * @return {Array} list contact
 * @param {Object} data
 */
AutoComplete.prototype.processData = function(data){
	var tmpList = [];
	var l = data.info.length;
	for(var i=0; i < l; i++){
		tmpList.push(data.info[i].split("::")[1]);
	}
	return tmpList;
};
/**
 * render drop down menu
 * @param {Object} data
 */
AutoComplete.prototype.renderMenu = function(data){
	var len = data.length;
	var menu = this.createMenuContainer("AutoComplete");
	var html = '';
	for(i=0; i<len; i++){
		html += '<div class="AutoCompleteItem">'+ data[i]+ '</div>';		
	}
	menu.innerHTML = html ;
	if(html != '') menu.style.display = "block";
	else  menu.style.display = "none";
};
/**
 * Complete automatically data to textbox input
 * @param {Object} data
 */
AutoComplete.prototype.typeHandler = function(evt,textbox){
	var me = eXo.mail.AutoComplete ;
	var keyword = eXo.mail.AutoComplete.createKeyword(textbox.value);
	me.activeInput = textbox;
	if(keyword == '') {
		eXo.mail.AutoComplete.hideMenu();
		return;
	}
	var url = me.REST_URL + keyword;
	me.makeRequest(url,me.typeCallback);
	me.setPosition(me.menu,me.activeInput);
};

AutoComplete.prototype.typeCallback = function(data){
	var me = eXo.mail.AutoComplete ;
	eval("var data = " + data.trim());
	if(typeof(data) != "object") return ;
	data = me.processData(data);
	me.renderMenu(data);
};
/**
 * Capture key is pressed by users
 * @param {Object} data
 */
AutoComplete.prototype.captureKey = function(evt){
	evt = window.event || evt ;
	var keynum = false ;
	if(window.event) { /* IE */
		keynum = evt.keyCode;
	} else if(evt.which) { /* Netscape/Firefox/Opera */
		keynum = evt.which ;
	}
	if(keynum == 0) {
		keynum = evt.keyCode ;
	}
	return keynum ;
};
/**
 * Move pointer among items
 */
AutoComplete.prototype.arrowDownHandler = function(){
	var me = eXo.mail.AutoComplete;
	if(!me.currentItem) {
		me.currentItem = me.menu.firstChild;
		eXo.core.DOMUtil.addClass(me.currentItem,"AutoCompleteOver");
		return ;
	}
	eXo.core.DOMUtil.replaceClass(me.currentItem,"AutoCompleteOver","");
	if(me.currentItem.nextSibling) me.currentItem = me.currentItem.nextSibling;
	else me.currentItem = me.menu.firstChild;
	eXo.core.DOMUtil.addClass(me.currentItem,"AutoCompleteOver");
};

AutoComplete.prototype.arrowUpHandler = function(){
	var me = eXo.mail.AutoComplete;
	if(!me.currentItem) {
		return ;
	}
	eXo.core.DOMUtil.replaceClass(me.currentItem,"AutoCompleteOver","");
	if(me.currentItem.previousSibling) me.currentItem = me.currentItem.previousSibling;
	else me.currentItem = me.menu.lastChild;
	eXo.core.DOMUtil.addClass(me.currentItem,"AutoCompleteOver");
};

AutoComplete.prototype.enterHandler = function(){
	var me = eXo.mail.AutoComplete;
	if(me.currentItem) me.activeInput.value = me.currentItem.innerHTML;
	me.hideMenu();
};

AutoComplete.prototype.escapeHandler = function(){
	eXo.mail.AutoComplete.hideMenu();
};

AutoComplete.prototype.createMenuContainer = function(id){
	if (document.getElementById(id)) {
		eXo.core.DOMUtil.listHideElements(document.getElementById(id));
		document.getElementById(id).innerHTML = "<span></span>";
		return (this.menu = document.getElementById(id));
	}
	var div = document.createElement("div");
	div.id = id;
	div.onmouseover = this.overItem;
	div.onmouseout = this.cleanUp;
	div.onclick = this.setValue;
	div.className = "AutoCompleteMenu";
	document.body.appendChild(div);
	this.menu = div ;
	eXo.core.DOMUtil.listHideElements(div);
	return div;
};

/**
 * @author Hoang Manh Dung
 */

function AutoComplete(){
	this.REST_URL = eXo.env.portal.context + "/rest/cs/mail/searchemail/";
};
/**
 * Register handler for input
 * @param {Array} ids is the array of input ids
 */
AutoComplete.prototype.init = function(ids){
	var i = ids.length ;
	while(i--){
		document.getElementById(ids[i]).onkeyup = this.pressHandler ;
	}
};

AutoComplete.prototype.pressHandler = function(evt){
	var me = eXo.mail.AutoComplete;
	var keyNum = me.captureKey(evt);
	switch(keyNum){
		case 13: me.enterHandler(evt, this); break;
		case 27: me.escapeHandler(evt, this); break;
		case 38: me.arrowUpHandler(evt, this); break;
		case 40: me.arrowDownHandler(evt, this); break;
		default: me.typeHandler(evt, this);
	}
	return; 
};

AutoComplete.prototype.makeRequest = function(url,callback){
	var request =  eXo.core.Browser.createHttpRequest() ;
	request.open('GET', url, false) ;
	request.setRequestHeader("Cache-Control", "max-age=86400") ;
	request.send(null) ;
	if(callback) callback(request.responseText) ;
};
/**
 * @return {Array} list contact
 * @param {Object} data
 */
AutoComplete.prototype.processData = function(data){
	var tmpList = [];
	var l = data.info.length;
	for(var i=0; i < l; i++){
		tmpList.push(data.info[i].split("::")[1]);
	}
	return tmpList;
};
/**
 * render drop down menu
 * @param {Object} data
 */
AutoComplete.prototype.renderMenu = function(data){
	var len = data.length;
	var menu = this.createMenuContainer("AutoComplete");
	var html = '';
	for(i=0; i<len; i++){
		html += '<div class="AutoCompleteItem">'+ data[i]+ '</div>';		
	}
	menu.innerHTML = html ;
	if(html != '') menu.style.display = "block";
	else  menu.style.display = "none";
};
/**
 * Complete automatically data to textbox input
 * @param {Object} data
 */
AutoComplete.prototype.typeHandler = function(evt,textbox){
	var me = eXo.mail.AutoComplete ;
	var keyword = eXo.mail.AutoComplete.createKeyword(textbox.value);
	me.activeInput = textbox;
	if(keyword == '') {
		eXo.mail.AutoComplete.hideMenu();
		return;
	}
	var url = me.REST_URL + keyword;
	me.makeRequest(url,me.typeCallback);
	me.setPosition(me.menu,me.activeInput);
};

AutoComplete.prototype.typeCallback = function(data){
	var me = eXo.mail.AutoComplete ;
	eval("var data = " + data.trim());
	if(typeof(data) != "object") return ;
	data = me.processData(data);
	me.renderMenu(data);
};
/**
 * Capture key is pressed by users
 * @param {Object} data
 */
AutoComplete.prototype.captureKey = function(evt){
	evt = window.event || evt ;
	var keynum = false ;
	if(window.event) { /* IE */
		keynum = evt.keyCode;
	} else if(evt.which) { /* Netscape/Firefox/Opera */
		keynum = evt.which ;
	}
	if(keynum == 0) {
		keynum = evt.keyCode ;
	}
	return keynum ;
};
/**
 * Move pointer among items
 */
AutoComplete.prototype.arrowDownHandler = function(){
	var me = eXo.mail.AutoComplete;
	if(!me.currentItem) {
		me.currentItem = me.menu.firstChild;
		eXo.core.DOMUtil.addClass(me.currentItem,"AutoCompleteOver");
		return ;
	}
	eXo.core.DOMUtil.replaceClass(me.currentItem,"AutoCompleteOver","");
	if(me.currentItem.nextSibling) me.currentItem = me.currentItem.nextSibling;
	else me.currentItem = me.menu.firstChild;
	eXo.core.DOMUtil.addClass(me.currentItem,"AutoCompleteOver");
};

AutoComplete.prototype.arrowUpHandler = function(){
	var me = eXo.mail.AutoComplete;
	if(!me.currentItem) {
		return ;
	}
	eXo.core.DOMUtil.replaceClass(me.currentItem,"AutoCompleteOver","");
	if(me.currentItem.previousSibling) me.currentItem = me.currentItem.previousSibling;
	else me.currentItem = me.menu.lastChild;
	eXo.core.DOMUtil.addClass(me.currentItem,"AutoCompleteOver");
};

AutoComplete.prototype.enterHandler = function(){
	var me = eXo.mail.AutoComplete;
	if(me.currentItem) me.addValue(me.currentItem);
	me.hideMenu();
};

AutoComplete.prototype.escapeHandler = function(){
	eXo.mail.AutoComplete.hideMenu();
};

AutoComplete.prototype.createMenuContainer = function(id){
	if (document.getElementById(id)) {
		eXo.core.DOMUtil.listHideElements(document.getElementById(id));
		document.getElementById(id).innerHTML = "<span></span>";
		return (this.menu = document.getElementById(id));
	}
	var div = document.createElement("div");
	div.id = id;
	div.onmouseover = this.overItem;
	div.onmouseout = this.cleanUp;
	div.onclick = this.setValue;
	div.className = "AutoCompleteMenu";
	document.body.appendChild(div);
	this.menu = div ;
	eXo.core.DOMUtil.listHideElements(div);
	return div;
};

AutoComplete.prototype.setValue = function(evt){
	var target = eXo.core.EventManager.getEventTargetByClass(evt,"AutoCompleteItem");
	if(!target) return ;
	eXo.mail.AutoComplete.addValue(target);
};

AutoComplete.prototype.addValue = function(obj){
	var value = "";
	if(String(eXo.mail.AutoComplete.activeInput.value).indexOf(",") != -1){
		value += eXo.mail.AutoComplete.activeInput.value;
	}
	value = value.substr(0,value.lastIndexOf(",")+1) + " ";
	value += obj.innerHTML + ", ";
	eXo.mail.AutoComplete.activeInput.value = value;
	eXo.mail.AutoComplete.hideMenu();
};

AutoComplete.prototype.overItem = function(evt){
	var target = eXo.core.EventManager.getEventTargetByClass(evt,"AutoCompleteItem");
	var me = eXo.mail.AutoComplete;
	if(!target) return ;
	if(me.currentItem) eXo.core.DOMUtil.replaceClass(me.currentItem,"AutoCompleteOver","");
	eXo.core.DOMUtil.addClass(target,"AutoCompleteOver");
	me.currentItem = target;
};

AutoComplete.prototype.setPosition = function(menu,anchor){
	var x = eXo.core.Browser.findPosX(anchor);
	var y = eXo.core.Browser.findPosY(anchor);
	menu.style.left = x + "px";
	menu.style.top = (y + anchor.offsetHeight) + "px";
};

AutoComplete.prototype.cleanUp = function(evt){
	var me = eXo.mail.AutoComplete;
	if(me.currentItem) eXo.core.DOMUtil.replaceClass(me.currentItem,"AutoCompleteOver","");
};

AutoComplete.prototype.hideMenu = function(){
	eXo.mail.AutoComplete.menu.style.display = "none";
};

AutoComplete.prototype.createKeyword = function(str){
	if(str.indexOf(",") != -1) {
		str = str.substr(str.lastIndexOf(",") + 1, str.length);
	}
	return str.trim();	
};

AutoComplete.prototype.overItem = function(evt){
	var target = eXo.core.EventManager.getEventTargetByClass(evt,"AutoCompleteItem");
	var me = eXo.mail.AutoComplete;
	if(!target) return ;
	if(me.currentItem) eXo.core.DOMUtil.replaceClass(me.currentItem,"AutoCompleteOver","");
	eXo.core.DOMUtil.addClass(target,"AutoCompleteOver");
	me.currentItem = target;
};

AutoComplete.prototype.setPosition = function(menu,anchor){
	var x = eXo.core.Browser.findPosX(anchor);
	var y = eXo.core.Browser.findPosY(anchor);
	menu.style.left = x + "px";
	menu.style.top = (y + anchor.offsetHeight) + "px";
};

AutoComplete.prototype.cleanUp = function(evt){
	var me = eXo.mail.AutoComplete;
	if(me.currentItem) eXo.core.DOMUtil.replaceClass(me.currentItem,"AutoCompleteOver","");
};

AutoComplete.prototype.hideMenu = function(){
	eXo.mail.AutoComplete.menu.style.display = "none";
};

AutoComplete.prototype.createKeyword = function(str){
	if(str.indexOf(",") != -1) {
		str = str.substr(str.lastIndexOf(",") + 1, str.length);
	}
	return str.trim();	
};

eXo.mail.AutoComplete = new AutoComplete();