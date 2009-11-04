/**
 * @author uocnguyen
 *
 * @desc this file used to import all javascript library which needed to chat application
 * working well,
 */
eXo.require("eXo.core.JSON");
//eXo.require("eXo.cs.CSCometd");
eXo.require("eXo.core.HTMLUtil");
eXo.require("eXo.core.Resize");
eXo.require("eXo.webui.UIHorizontalTabs");
if(!eXo.communication) eXo.communication = {};
eXo.communication.chatbar = {
    eXoChat : {},
    core : {},
    webui : {
      component : {}
    }
}

// Overwrite html entities to ignore white space from html encode
eXo.core.HTMLUtil.entities.nbsp = null;

//eXo.require("eXo.communication.chatbar.core.DateFormat", "/chatbar/javascript/");
//eXo.require("eXo.cs.CSCometd", "/csResources/javascript/");
//eXo.require("eXo.communication.chatbar.core.AdvancedDOMEvent", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.core.JSLogger", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.core.XMPPCommunicator", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.core.Utils", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.core.LocalTemplateEngine", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UIWindow", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UIPageIterator", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.TabScrollManager", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UISlideAlert", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.component.BuddyListControl", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.component.JSUIBean", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.component.JSUIBeanListener", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UIStateManager", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UIMainChatWindow", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UIPopupManager", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UIChatWindow", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UIAddContactPopupWindow", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UIChatDragDrop", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UIChatResize", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UICreateNewRoomPopupWindow", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UIRoomConfigPopupWindow", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UIJoinRoomPopupWindow", "/chatbar/javascript/");
//eXo.require("eXo.communication.chatbar.webui.UIChatBar", "/chatbar/javascript/");
eXo.communication.chatbar.eXoChatBar = {
  /**
   * This method use to initialize all data before call UIMainChatWindow to init all chat application component 
   */
  init : function() {
    try {
      var thys = eXo.communication.chatbar.eXoChatBar;
      var UIChatNode = document.getElementById('UIChatBarPortlet');
      var eXoToken = UIChatNode.getAttribute('eXoToken');
      var userName = UIChatNode.getAttribute('userName');
      eXo.communication.chatbar.core.LocalTemplateEngine.init('templateArea');
      eXo.communication.chatbar.webui.UIMainChatWindow.init(thys.applicationId, eXoToken, userName);
      eXo.communication.chatbar.webui.UIChatDragDrop.init(
        eXo.communication.chatbar.webui.UIMainChatWindow.chatWindowsContainerNode,
        [{className:'WindowBarLeft', tagName: 'div'}, {className: 'PopupTitle', tagName: 'div'}]);
      eXo.communication.chatbar.webui.UIMainChatWindow.xLogin(userName);
	  eXo.communication.chatbar.eXoChatBar.setWidth();
	  var div = document.createElement("div");
	  div.style.height = document.getElementById("UIChatBarPortlet").offsetHeight + "px";
	  document.body.appendChild(div);
    } catch (e) {
      throw (new Error('Error while loading chat application.'));
    }
  },
  setWidth : function(){
		var obj = document.getElementById('UIChatBarPortlet');
		var uiWorkingWorkspace = document.getElementById('UIWorkingWorkspace') ;
		if(obj){
		//var browserWidth = eXo.core.Browser.getBrowserWidth() ;
		//var objWidth = obj.offsetWidth + 16;
		//	if(objWidth != browserWidth){
				//obj.style.width = (browserWidth - 16) + "px";
			obj.style.width = (uiWorkingWorkspace.offsetWidth - 16) + "px";
		}		
		//}
		window.setTimeout('eXo.communication.chatbar.eXoChatBar.setWidth()', 100);
	}
}


