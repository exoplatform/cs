eXoMessageGadget.prototype.onLoadHander = function(){
	eXoMessageGadget.getData();
}
eXoMessageGadget.prototype.getData = function(){					 
	var url = eXoMessageGadget.createRequestUrl();
	this.ajaxAsyncGetRequest(url,eXoMessageGadget.render);
	if(typeof(requestInterval) == "undefined") requestInterval = setInterval(eXoMessageGadget.getData,300000);
}
eXoMessageGadget.prototype.ajaxAsyncGetRequest = function(url, callback) {
	var request =  parent.eXo.core.Browser.createHttpRequest() ;
  request.open('GET', url, true) ;
  request.setRequestHeader("Cache-Control", "max-age=86400") ;
  request.send(null) ;
	request.onreadystatechange = function(){
		if(request.readyState == 4 && (request.status == 200 || request.status == 204)){
			callback(gadgets.json.parse(request.responseText));
		}
	}
}
eXoMessageGadget.prototype.write2Setting = function(data){
	var frmSetting = document.getElementById("SettingMessage");
	var html = '';
	for(var i=0,len = data.accounts.length; i < len;i++){
		html += '<option value="' + data.accounts[i].id + '">' + data.accounts[i].userDisplayName + '</option>';
	}
	frmSetting["account"].innerHTML = html;

	html = '';
	for(var i=0,len = data.folders.length; i < len;i++){
		html += '<option value="' + data.folders[i].id + '">' + data.folders[i].name + '</option>';
	}
  html +=  '<option value=""> </option>';
	frmSetting["folder"].innerHTML = html;

  html = '<option value=""> </option>';
	for(var i=0,len = data.tags.length; i < len;i++){
		html += '<option value="' + data.tags[i].id + '">' + data.tags[i].name + '</option>';
	}
	frmSetting["tag"].innerHTML = html;
}
eXoMessageGadget.prototype.render =  function(data){
	data = data.info;
	if(!data || data.length == 0){
		eXoMessageGadget.notify();
		return;
	}
  var cont = document.getElementById("ItemContainer");	
	var prefs = eXoMessageGadget.getPrefs();
	var gadgetPref = new gadgets.Prefs();
  var html = '';
	var len = (prefs.limit && (parseInt(prefs.limit) > 0) &&  (parseInt(prefs.limit) < data.length))? prefs.limit:data.length;
  for(var i = 0 ; i < len; i++){	
    var item = data[i];
		html +=  '<span class="IconLink">'+ item.subject +'</span>';
  }
  html += '';
  cont.innerHTML = html;
}

eXoMessageGadget.prototype.notify = function(){
	var msg = gadgets.Prefs().getMsg("nomessage");
	document.getElementById("ItemContainer").innerHTML = '<div class="Warning">' + msg + '</div>';
}

eXoMessageGadget.prototype.createRequestUrl = function(){
	var prefs = eXoMessageGadget.getPrefs();
	var limit = (prefs.limit && (parseInt(prefs.limit) > 0))? prefs.limit:0;
  var account =  (prefs.account)? prefs.account:" ";
  var folder=  (prefs.folder)? prefs.folder:" ";
  var tag=  (prefs.tag)? prefs.tag:" ";
	var subscribeurl = "/portal/rest/private/cs/mail/unreadMail" ;
  subscribeurl +=  "/" + account ;
  subscribeurl +=  "/" + folder;
  subscribeurl +=  "/" + tag;
	subscribeurl +=  "/" + limit ;
	subscribeurl += "?rnd=" + (new Date()).getTime();
	return subscribeurl;
}
eXoMessageGadget.prototype.getPrefs = function(){
	var prefs = new gadgets.Prefs();
	var limit = prefs.getString("limit");
	var url   = prefs.getString("url");
	var account = prefs.getString("account");
	var folder = prefs.getString("folder");
	var tag = prefs.getString("tag");
	return {
		"limit": limit,
		"url"  : url,
		"account"  : account ,
		"folder"  : folder ,
		"tag"  : tag,
	}
}

eXoMessageGadget.prototype.saveSetting = function(){
	var prefs = new gadgets.Prefs();
	var frmSetting = document.getElementById("SettingMessage");
  prefs.set("limit",frmSetting["limit"].value);
  prefs.set("account",frmSetting["account"].options[frmSetting["account"].selectedIndex].value);
  prefs.set("folder",frmSetting["folder"].options[frmSetting["folder"].selectedIndex].value);
  prefs.set("tag",frmSetting["tag"].options[frmSetting["tag"].selectedIndex].value);
  //eXoMessageGadget.getData();
  eXoMessageGadget.showHideSetting(false);
	return false;
}

eXoMessageGadget.prototype.showHideSetting = function(isShow){
	var frmSetting = document.getElementById("SettingMessage");
	var display = "";
	if(isShow) {
        var url = eXoMessageGadget.createRequestUrl();
	this.ajaxAsyncGetRequest(url,eXoMessageGadget.write2Setting);
		display = "block";
	}	else display = "none";
	frmSetting.style.display = display;
	gadgets.window.adjustHeight();
}

function eXoMessageGadget(){	
} ;

eXoMessageGadget =  new eXoMessageGadget();

gadgets.util.registerOnLoadHandler(eXoMessageGadget.onLoadHander);