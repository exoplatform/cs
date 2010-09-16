eXoMessageGadget.prototype.onLoadHander = function(){
  eXoMessageGadget.setLink();
  eXoMessageGadget.showAccounts();
  eXoMessageGadget.getData();
  TitleMessage = document.getElementById("TitleMessage");
  this.hiddenTimeout = null;
  TitleMessage.onmouseover = eXoMessageGadget.moveOver;
  TitleMessage.onmouseout = eXoMessageGadget.moveOut;
}

eXoMessageGadget.prototype.moveOver = function(){
  if(eXoMessageGadget.hiddenTimeout) window.clearTimeout(eXoMessageGadget.hiddenTimeout);
  settingButton = document.getElementById("SettingButtonMessage");
  settingButton.style.display = "block";
}
eXoMessageGadget.prototype.moveOut = function(){
  eXoMessageGadget.hiddenTimeout = window.setTimeout(function(){
    settingButton = document.getElementById("SettingButtonMessage");
    settingButton.style.display = "none";
  },200);
}

eXoMessageGadget.prototype.showAccounts = function(){
  var subscribeurl = "/portal/rest/private/cs/mail/getAccounts" ;
  subscribeurl += "?rnd=" + (new Date()).getTime();
  this.ajaxAsyncGetRequest(subscribeurl,eXoMessageGadget.renderAccounts);
  if(typeof(requestInterval) == "undefined") requestInterval = setInterval(eXoMessageGadget.getData,300000);
}

eXoMessageGadget.prototype.renderAccounts = function(data){
  var frmSetting = document.getElementById("SettingMessage");
  var noAccount = document.getElementById("noAccount");
  var content = document.getElementById("content");
  if (data.info) {
    //frmSetting.style.display="block";
    content.style.display="block";
    noAccount.style.display="none";
  	var html = '';
  	for(var i=0,len = data.info.length; i < len;i++){
  		html += '<option value="' + data.info[i].id + '">' + data.info[i].userDisplayName + '</option>';
  	}
  	frmSetting["account"].innerHTML = html;
  	eXoMessageGadget.getFoldersTags();
  } else {
    noAccount.style.display="block";
    frmSetting.style.display="none";
    content.style.display="none";
  }  
}

eXoMessageGadget.prototype.getFoldersTags = function(){
  var frmSetting = document.getElementById("SettingMessage");
  var accountId = frmSetting["account"].options[frmSetting["account"].selectedIndex].value;
  var subscribeurl = "/portal/rest/private/cs/mail/getFoldersTags/" +  accountId;
  subscribeurl += "?rnd=" + (new Date()).getTime();
  this.ajaxAsyncGetRequest(subscribeurl,eXoMessageGadget.renderFoldersTags);
  if(typeof(requestInterval) == "undefined") requestInterval = setInterval(eXoMessageGadget.getData,300000);
}

eXoMessageGadget.prototype.renderFoldersTags = function(data){
  var frmSetting = document.getElementById("SettingMessage");
  var html = '<option value=""> -- </option>';
  for(var i=0,len = data.folders.length; i < len;i++){
  	html += '<option value="' + data.folders[i].id + '">' + data.folders[i].name + '</option>';
  }
  frmSetting["folder"].innerHTML = html;
  
  html = '<option value=""> -- </option>';
  for(var i=0,len = data.tags.length; i < len;i++){
  	html += '<option value="' + data.tags[i].id + '">' + data.tags[i].name + '</option>';
  }
  frmSetting["tag"].innerHTML = html;
}

eXoMessageGadget.prototype.getData = function(){					 
  var url = eXoMessageGadget.createRequestUrl();
  eXoMessageGadget.ajaxAsyncGetRequest(url,eXoMessageGadget.render);
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

eXoMessageGadget.prototype.render =  function(data){
  data = data.info;
  if(!data || data.length == 0){
  	eXoMessageGadget.notify();
  	eXoMessageGadget.showHideSetting(false);
  	return;
  }
  var cont = document.getElementById("ItemContainer");	
  var prefs = eXoMessageGadget.getPrefs();
  var gadgetPref = new gadgets.Prefs();
  var html = '';
  var len = (prefs.limit && (parseInt(prefs.limit) > 0) &&  (parseInt(prefs.limit) < data.length))? prefs.limit:data.length;
  var url   = eXoMessageGadget.getPrefs().url;
  var baseUrl = "http://" +  top.location.host + parent.eXo.env.portal.context + "/" + parent.eXo.env.portal.accessMode + "/" + parent.eXo.env.portal.portalName;
  
  var account = (prefs.account)? prefs.account:"_";
  var folder = (prefs.folder)? prefs.folder:"_";
  var tag = (prefs.tag)? prefs.tag:"_";
  var msgId = "/" + account + "/" + folder + "/" + tag;
  url = (url)?baseUrl + url + msgId : baseUrl + "/mail" + msgId;
  
  for(var i = 0 ; i < len; i++){
    var item = data[i];newUrl = url + "/" +  item.id;
  	html +=  '<a href ="' + newUrl + '" target="_blank">'+ item.subject +'</a><br>';
  }
  html += '';
  cont.innerHTML = html;
  eXoMessageGadget.showHideSetting(false);
}

eXoMessageGadget.prototype.notify = function(){
  var msg = gadgets.Prefs().getMsg("nomessage");
  document.getElementById("ItemContainer").innerHTML = '<div class="Warning">' + msg + '</div>';
}

eXoMessageGadget.prototype.setLink = function(){
	var url   = eXoMessageGadget.getPrefs().url;
	var frmSetting = document.getElementById("SettingMessage");
	var baseUrlField = frmSetting["urlMessage"];
	baseUrlField.value = (url)? url : "/mail";
	var subscribeurlField = frmSetting["subscribeurlMessage"];
	var subscribeurl   = eXoMessageGadget.getPrefs().subscribeurl;
	subscribeurlField.value = (subscribeurl)? subscribeurl : "/portal/rest/private/cs/mail/unreadMail" ;
	var baseUrl = "http://" +  top.location.host + parent.eXo.env.portal.context + "/" + parent.eXo.env.portal.accessMode + "/" + parent.eXo.env.portal.portalName;
	var a = document.getElementById("ShowAllMessage");
	url = (url)?baseUrl + url: baseUrl + "/mail";
	a.href = url;
}

eXoMessageGadget.prototype.createRequestUrl = function(){
  var prefs = eXoMessageGadget.getPrefs();
  var limit = (prefs.limit && (parseInt(prefs.limit) > 0))? prefs.limit:0;
  var account =  (prefs.account)? prefs.account:"_";
  var folder=  (prefs.folder)? prefs.folder:"_";
  var tag=  (prefs.tag)? prefs.tag:"_";
  var subscribeurl = (prefs.subscribeurl)?prefs.subscribeurl: "/portal/rest/private/cs/mail/unreadMail" ;
  subscribeurl +=  "/" + account ;
  subscribeurl +=  "/" + folder;
  subscribeurl +=  "/" + tag;
  subscribeurl +=  "/" + limit ;
  subscribeurl += "?rnd=" + (new Date()).getTime();
  return subscribeurl;
}
eXoMessageGadget.prototype.getPrefs = function(){
  var prefs = new gadgets.Prefs();
  var setting = prefs.getString("setting");
  if(setting == "") setting = ["/mail","/portal/rest/private/cs/mail/unreadMail","_","_","_", 5];
  else setting = setting.split("::");
  return {
  	"url"  : setting[0],
  	"subscribeurl"  : setting[1],
  	"account"  : setting[2] ,
  	"folder"  : setting[3] ,
  	"tag"  : setting[4],
  	"limit": setting[5]
  }
}

eXoMessageGadget.prototype.saveSetting = function(){
  var prefs = new gadgets.Prefs();
  var frmSetting = document.getElementById("SettingMessage");
  var limit = frmSetting["limit"].value;
  var account = frmSetting["account"].options[frmSetting["account"].selectedIndex].value;
  var folder = frmSetting["folder"].options[frmSetting["folder"].selectedIndex].value;
  var tag = frmSetting["tag"].options[frmSetting["tag"].selectedIndex].value;
  if (isNaN(parseInt(limit)) == true) {
    alert(prefs.getMsg("numberRequired"));
    return false;
  }
  if ((folder.length == 0) && (tag.length == 0)) {
    alert(prefs.getMsg("selectfolderTagRequired"));
    return false;
  }
  var setting = "";
  setting += frmSetting["urlMessage"].value + "::";
  setting += frmSetting["subscribeurlMessage"].value + "::";
  setting += account + "::";
  setting += folder + "::";
  setting += tag + "::";
  setting += limit;
  prefs.set("setting",setting);
  
  frmSetting.style.display = "none";
  //eXoMessageGadget.getData();
  //eXoMessageGadget.showHideSetting(false);
  return false;
}

eXoMessageGadget.prototype.showHideSetting = function(isShow){
  var frmSetting = document.getElementById("SettingMessage");
  var display = "";
  if(isShow) {
  	display = "block";
  }	else display = "none";
  frmSetting.style.display = display;
  gadgets.window.adjustHeight();
}

function eXoMessageGadget(){	
} ;

eXoMessageGadget =  new eXoMessageGadget();

gadgets.util.registerOnLoadHandler(eXoMessageGadget.onLoadHander);
