function AutoComplete(){
	this.REST_URL = eXo.env.portal.context + "/rest/cs/calendar/searchCalendar/";
};
/**
 * Register handler for input
 * @param {Array} ids is the array of input ids
 */
AutoComplete.prototype.init = function(id){
	if(eXo.calendar.restContext) this.REST_URL = eXo.env.portal.context + "/" + eXo.calendar.restContext + "/cs/calendar/searchCalendar/";
	document.getElementById(id).onkeyup = function(evt){
			evt = window.event || evt;
			eXo.calendar.AutoComplete.pressHandler(evt,this);
	}
	eXo.core.DOMUtil.findAncestorByTagName(document.getElementById(id),"form").onsubmit = function(){
			return false;
	}
};

AutoComplete.prototype.pressHandler = function(evt, textbox){
	var me = eXo.calendar.AutoComplete;
	var keyNum = me.captureKey(evt);
	switch(keyNum){
		case 13: me.enterHandler(evt, textbox); break;
		case 27: me.escapeHandler(evt, textbox); break;
		case 38: me.arrowUpHandler(evt, textbox); break;
		case 40: me.arrowDownHandler(evt, textbox); break;
		default: me.typeHandler(evt, textbox);
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
	var a = data.info ;
	var l = a.length;
	if(!l || l == 0) return;
	a = this.reArrange(a,this.typingWords);
	for(var i=0; i < l; i++){
		tmpList.push(a[i]);
	}
	return tmpList;
};

AutoComplete.prototype.addBiTag = function(bodyText){
	var newText = "";
  var i = -1;
	var searchTerm = this.typingWords;
  var lcSearchTerm = searchTerm.toLowerCase();
  var lcBodyText = bodyText.toLowerCase();
  var highlightStartTag="<b>", highlightEndTag = "</b>";
  while (bodyText.length > 0) {
    i = lcBodyText.indexOf(lcSearchTerm, i+1);
    if (i < 0) {
      newText += bodyText;
      bodyText = "";
    } else {      
      newText += bodyText.substring(0, i) + highlightStartTag + bodyText.substr(i, searchTerm.length) + highlightEndTag;
      bodyText = bodyText.substr(i + searchTerm.length);
      lcBodyText = bodyText.toLowerCase();
      i = -1;
    }
  }
  
  return newText;
}


AutoComplete.prototype.reArrange = function(data,str){
	if(!data) return;	
	var l = data.length;
	var tmp = [];
	var tmp2 = [];
	for (var i = 0; i < l; i++) {
  	if(data[i].toLowerCase().indexOf(str) == 0) {
  		tmp.push(data[i]);
  		continue;
  	}
  	tmp2.push(data[i]);
  }
	return tmp.concat(tmp2);
};

/**
 * render drop down menu
 * @param {Object} data
 */
AutoComplete.prototype.renderMenu = function(data){
	if(!data) {
		this.hideMenu();
		return;
	}
	var len = data.length;
	var menu = this.createMenuContainer("AutoCompleteMail");
	var html = '';
	for(i=0; i<len; i++){
		showString = data[i].split(":")[2];
		hideString = data[i].split(":")[0] + ":" + data[i].split(":")[1];
		if(i == 0){
			html += '<div class="AutoCompleteItem AutoCompleteOver"><span style="display:none;">' + hideString + '</span><span>' + showString + '</span></div>';
			continue;
		}
		html += '<div class="AutoCompleteItem"><span style="display:none;">' + hideString + '</span><span>' + showString + '</span></div>';		
	}
	menu.innerHTML = html ;
	this.currentItem = menu.firstChild;
	if(html != '') menu.style.display = "block";
	else  menu.style.display = "none";
};

AutoComplete.prototype.typeHandler = function(evt,textbox){
	var me = eXo.calendar.AutoComplete ;
	var keyword = eXo.calendar.AutoComplete.createKeyword(textbox.value);
	me.activeInput = textbox;
	me.typingWords = keyword;
	if(keyword == '') {
		eXo.calendar.AutoComplete.hideMenu();
		return;
	}
	var url = me.REST_URL + keyword;
	me.makeRequest(url,me.typeCallback);
	me.setPosition(me.menu,me.activeInput);
};

AutoComplete.prototype.typeCallback = function(data){
	var me = eXo.calendar.AutoComplete ;
	eval("var data = " + data.trim());
	if(typeof(data) != "object") return ;
	data = me.processData(data);
	me.renderMenu(data);
};
/**
 * Capture key is pressed by users
 * @param {Object} data
 */
AutoComplete.prototype.captureKey = function(e){
	var code;
	if (!e) var e = window.event;
	if (e.keyCode) code = e.keyCode;
	else if (e.which) code = e.which;
	return code;
};
/**
 * Move pointer among items
 */
AutoComplete.prototype.arrowDownHandler = function(){
	var me = eXo.calendar.AutoComplete;
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
	var me = eXo.calendar.AutoComplete;
	if(!me.currentItem) {
		return ;
	}
	eXo.core.DOMUtil.replaceClass(me.currentItem,"AutoCompleteOver","");
	if(me.currentItem.previousSibling) me.currentItem = me.currentItem.previousSibling;
	else me.currentItem = me.menu.lastChild;
	eXo.core.DOMUtil.addClass(me.currentItem,"AutoCompleteOver");
};

AutoComplete.prototype.enterHandler = function(){
	var me = eXo.calendar.AutoComplete;
	if(me.currentItem) me.addValue(me.currentItem);
	me.hideMenu();
};

AutoComplete.prototype.escapeHandler = function(){
	eXo.calendar.AutoComplete.hideMenu();
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
	eXo.calendar.AutoComplete.addValue(target);
};

AutoComplete.prototype.addValue = function(obj){
	var value = "";
	if(String(eXo.calendar.AutoComplete.activeInput.value).indexOf(",") != -1){
		value += eXo.calendar.AutoComplete.activeInput.value;
	}
	value = value.substr(0,value.lastIndexOf(",")+1) + " ";
	calId = eXo.core.DOMUtil.getChildrenByTagName(obj,"span")[0].innerHTML ;	
	value += eXo.core.DOMUtil.getChildrenByTagName(obj,"span")[1].innerHTML + "," ;	
//value += obj.innerHTML + ", ";
	value = value.replace(/\&lt;/gi,"<").replace(/\&gt;/gi,">");
eXo.calendar.AutoComplete.activeInput.value = this.stripHTML(value);
	eXo.calendar.AutoComplete.hideMenu();
	if(eXo.calendar.AutoComplete.currentItem) delete eXo.calendar.AutoComplete.currentItem;
	eXo.calendar.AutoComplete.activeInput.focus();
};

AutoComplete.prototype.overItem = function(evt){
	var target = eXo.core.EventManager.getEventTargetByClass(evt,"AutoCompleteItem");
	var me = eXo.calendar.AutoComplete;
	if(!target) return ;
	if(me.currentItem) eXo.core.DOMUtil.replaceClass(me.currentItem,"AutoCompleteOver","");
	eXo.core.DOMUtil.addClass(target,"AutoCompleteOver");
	me.currentItem = target;
};

AutoComplete.prototype.setPosition = function(menu,anchor){
	var x = eXo.core.Browser.findPosX(anchor);
	var y = eXo.core.Browser.findPosY(anchor);
	if(eXo.core.DOMUtil.findAncestorByClass(anchor,"PopupContent")) y -= eXo.core.DOMUtil.findAncestorByClass(anchor,"PopupContent").scrollTop ;
	menu.style.left = x + "px";
	menu.style.top = (y + anchor.offsetHeight) + "px";
};

AutoComplete.prototype.cleanUp = function(evt){
	var me = eXo.calendar.AutoComplete;
	if (me.currentItem) {
  	eXo.core.DOMUtil.replaceClass(me.currentItem, "AutoCompleteOver", "");
		delete me.currentItem;
  }
};

AutoComplete.prototype.hideMenu = function(){
	if(eXo.calendar.AutoComplete.menu) eXo.calendar.AutoComplete.menu.style.display = "none";
};

AutoComplete.prototype.createKeyword = function(str){
	if(str.indexOf(",") != -1) {
		str = str.substr(str.lastIndexOf(",") + 1, str.length);
	}
	str = str.replace(/^\s*/,"");
	return str.toLowerCase();	
};

AutoComplete.prototype.stripHTML = function(str){
	return str.replace(/<b>/ig,"").replace(/<\/b>/ig,"");
}

eXo.calendar.AutoComplete = new AutoComplete();