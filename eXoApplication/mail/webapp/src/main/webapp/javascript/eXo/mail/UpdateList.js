function UpdateList() { }

UpdateList.prototype.init = function(accountId, eXoUser, eXoToken, cometdContextName){
  eXo.cs.CSCometd.exoId = eXoUser;
  eXo.cs.CSCometd.exoToken = eXoToken;
  if(cometdContextName)
		eXo.cs.CSCometd.url = '/' + cometdContextName + '/cometd';
  eXo.cs.CSCometd.subscribe('/eXo/Application/mail/messages', function(eventObj) {		
		eXo.mail.UpdateList.update(eventObj) ;
  });
  
	if (!eXo.cs.CSCometd.isConnected()) {
     eXo.cs.CSCometd.init();
  }
  this.accountId_ = accountId;
  this.msgCount = 0;
} ;

UpdateList.prototype.formId = "";

UpdateList.prototype.update = function(obj){
	var data = eXo.core.JSON.parse(obj.data);	
	var tbodyMsgList = document.getElementById("TbodyMessageList");
	var updateListLabel = document.getElementById("UpdateList");
	var viewing = tbodyMsgList.getAttribute("viewing");
	var formFullId = tbodyMsgList.getAttribute("formfullid");
	
	var isUpdate = false ;
	if (this.accountId_ == data.accountId) {
	  var folderIds = data.folders;
  	  var folders = folderIds.split(","); 
  	  var folderNumberCountNode ;
  	  var updateImapFolder;
      for (var i = 0; i < folders.length; i++) {
    	if (folders[i] != "") {  
		    folderNumberCountNode = document.getElementById(folders[i]);
		    if (folderNumberCountNode != null) {
		  	  if (eXo.core.DOMUtil.findAncestorByClass(folderNumberCountNode, "Folder").className.indexOf("SelectedLabel") > -1) isUpdate = true;
		  	  if (data.isRead != 'true') {
		  	    var numberStr = "0";
		  	    if (folderNumberCountNode.innerHTML) {
		  	      numberStr = folderNumberCountNode.innerHTML;
		  	      numberStr = numberStr.substring(numberStr.indexOf("(") + 1, numberStr.indexOf(")"));
		  	      if (numberStr.length == 0) numberStr = "0";
		  	    }
		  	    folderNumberCountNode.innerHTML = "(" + (parseInt(numberStr) + 1) + ")";
		      }
		    } else {
		      updateImapFolder = document.getElementById("UpdateImapFolder");
		      if (updateImapFolder != null) {
		    	eval(eXo.core.DOMUtil.findDescendantsByTagName(updateImapFolder, 'a')[0].href.replace("%20", ""));
		      }
		    }
  	  	}
  	}
  	
	  if (tbodyMsgList && updateListLabel && isUpdate) {		  
	  	if (viewing == "2") {
	  		if (data.hasStar != "true") return ;	
	  	} else if (viewing == "3") {
	  		if (data.hasStar == "true") return ;
	  	} else if (viewing == "4") {
	  		if (data.isUnread != "true") return ;
	  	} else if (viewing == "5") {
	  		if (data.isUnread == "true") return ;
	  	} else if (viewing == "6") {
	  		if (data.hasAttachment != "true") {
	  			return ;	  			
	  		}
	  	}
		  var tr = document.createElement("tr");
		  tbodyMsgList.appendChild(tr);
		  var preTr = eXo.core.DOMUtil.findPreviousElementByTagName(tr, "tr");
		  if (preTr && !preTr.className) tbodyMsgList.removeChild(preTr);
		  var href = "href=\"javascript:eXo.webui.UIForm.submitEvent('" + formFullId + "','SelectMessage','&objectId=" + data.msgId + "')\"";
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
		  if (data.hasAttachment == "true") {
		    td.innerHTML = "<a " + href + "><div class='AttachmentIcon'><span></span></div></a>"
		  } else {
		    td.innerHTML = "<a " + href + "> </a>";
		  }
		  tr.appendChild(td);
		  
		  td = document.createElement("td");
		  td.className = clazz;
		  td.innerHTML = "<a " + href + "> " + data.size + " </a>";
		  tr.appendChild(td);
		  
		  var priority = "NormalPriority";
		  if (data.priority == "5") priority = "LowPriority";
		  else if (data.priority == "1")  priority = "HighPriority";
		  td = document.createElement("td");
		  td.style.padding = "0px";
		  td.innerHTML = "<div class=\"text " + priority + "\"> <span> </span> </div>";
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
		}
  }
} ;

eXo.mail.UpdateList = new UpdateList();
