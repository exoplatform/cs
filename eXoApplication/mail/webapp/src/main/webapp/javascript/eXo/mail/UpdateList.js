function UpdateList() { }

UpdateList.prototype.init = function(accountId, eXoUser, eXoToken){
  eXo.core.Cometd.exoId = eXoUser;
  eXo.core.Cometd.exoToken = eXoToken;
  eXo.core.Cometd.subscribe('/eXo/Application/mail/messages', function(eventObj) {		
		eXo.mail.UpdateList.update(eventObj) ;
  });
	if (!eXo.core.Cometd.isConnected()) {
     eXo.core.Cometd.init();
  }
  this.accountId_ = accountId;
} ;

UpdateList.prototype.update = function(obj){
	var data = eXo.core.JSON.parse(obj.data);	
	var tbodyMsgList = document.getElementById("TbodyMessageList");
	var updateListLabel = document.getElementById("UpdateList");
	var isUpdate = false ;
	
	if (this.accountId_ == data.accountId) {
		// Update folder unread count
	  var folderIds = data.folders;
  	var folders = folderIds.split(","); 
  	var folderNumberCountNode ;
  	var numberStr;
  	var updateImapFolder;
      for (var i = 0; i < folders.length; i++) {
  	    folderNumberCountNode = document.getElementById(folders[i]);
  	    if (folderNumberCountNode != null) {
  	  	  if (eXo.core.DOMUtil.findAncestorByClass(folderNumberCountNode, "Folder").className.indexOf("SelectedLabel") > -1) isUpdate = true;
  	  	  if (data.isRead != 'true') {
  	  	    numberStr = folderNumberCountNode.innerHTML;
  	  	    numberStr = numberStr.substring(numberStr.indexOf("(") + 1, numberStr.indexOf(")"));
      	    if (numberStr.length == 0) numberStr = "0";
  	  	    folderNumberCountNode.innerHTML = "(" + (parseInt(numberStr) + 1) + ")";
  	      }
  	    } else {
  	      updateImapFolder = document.getElementById("UpdateImapFolder");
  	      if (updateImapFolder != null) {
  	    	eval(eXo.core.DOMUtil.findDescendantsByTagName(updateImapFolder, 'a')[0].href.replace("%20", ""));
  	    }
  	  }
  	}
  	
	  if (tbodyMsgList && updateListLabel && isUpdate) {
		  var tr = document.createElement("tr");
		  tbodyMsgList.appendChild(tr);
		  var preTr = eXo.core.DOMUtil.findPreviousElementByTagName(tr, "tr");
		  if (preTr && !preTr.className) tbodyMsgList.removeChild(preTr);
		  var href = "href=\"javascript:eXo.webui.UIForm.submitEvent('mail#UIMessageList','SelectMessage','&objectId=" + data.msgId + "')\"";
		  var clazz = "UnreadItem";
		  if (data.isRead == 'true') clazz = "ReadItem";
		  eXo.core.EventManager.addEvent(tr, "mousedown", eXo.mail.UIMailDragDrop.mailMDTrigger);
		  tr.className = "MessageItem";
		  tr.msgid = data.msgId;
		  
		  
		  var td = document.createElement("td");
		  td.innerHTML =  "  <input class='checkbox' type='checkbox' name='" + data.msgId + "'/>";
		  tr.appendChild(td);
		  
		  td = document.createElement("td");
		  td.style.padding = "0px auto";
		  td.innerHTML = "<a " + href + "><div class='UnStarredIcon'><span></span></div></a>";
		  tr.appendChild(td);
		  
		  td = document.createElement("td");
		  td.innerHTML = "<a " + href + "> &nbsp 1 &nbsp </a>";
		  tr.appendChild(td);
		  
		  td = document.createElement("td");
		  td.className = clazz;
		  td.innerHTML = "<a " + href + "> " + data.subject + "</a>";
		  tr.appendChild(td);
		  
		  
		  td = document.createElement("td");
		  td.className = clazz;
		  td.innerHTML = "<a " + href + "> " + data.from + "</a>";
		  tr.appendChild(td);
		  
		  td = document.createElement("td");
		  td.className = clazz;
		  td.innerHTML = "<a " + href + ">" + data.date + " </a>";
		  tr.appendChild(td);
		  
		  td = document.createElement("td");
		  td.className = clazz;
		  td.innerHTML = "<a " + href + "> </a>";
		  tr.appendChild(td);
		  
		  td = document.createElement("td");
		  td.className = clazz;
		  td.innerHTML = "<a " + href + "> " + data.size + " </a>";
		  tr.appendChild(td);
		  
		  td = document.createElement("td");
		  td.innerHTML = "<a " + href + ">  </a>";
		  tr.appendChild(td);
		 
		  if (preTr) {
		  	if (tr.className) {
				if (preTr.className.indexOf("OddItem") != -1) tr.className = tr.className.replace("OddItem", "") + " EvenItem";
				 else if (preTr.className.indexOf("EvenItem") != -1) tr.className = tr.className.replace("EvenItem", "") + " OddItem";
				  else tr.className += " OddItem";
		  	} else {
		  		tr.className = "OddItem";
		  	}
		  }
		  var form = eXo.core.DOMUtil.findAncestorByTagName(tbodyMsgList,"form");
		  eXo.mail.UpdateList.sendRequest(form.action,data.msgId);
		}
  }
} ;

UpdateList.prototype.sendRequest = function(url, msgId){
	url += "&formOp=UpdateList&objectId=" + msgId + "&ajaxRequest=true";
	ajaxAsyncGetRequest(url,false);
} ;

eXo.mail.UpdateList = new UpdateList();
