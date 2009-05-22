/**
 * @author uocnguyen
 *
 * @desc this file used to import all javascript library which needed to chat application
 * working well,
 */
eXo.require("eXo.core.JSON");
eXo.require("eXo.core.Cometd");
eXo.require("eXo.core.HTMLUtil");
eXo.require("eXo.core.Resize");
eXo.require("eXo.webui.UIHorizontalTabs");

eXo.communication = {
  chat : {
    eXoChat : {},
    core : {},
    webui : {
      component : {}
    }
  }
}

// Overwrite html entities to ignore white space from html encode
eXo.core.HTMLUtil.entities.nbsp = null;

eXo.require("eXo.communication.chat.core.DateFormat", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.core.Cometd", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.core.AdvancedDOMEvent", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.core.JSLogger", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.core.XMPPCommunicator", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.core.Utils", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.core.LocalTemplateEngine", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UIWindow", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UIPageIterator", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.TabScrollManager", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UISlideAlert", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.component.BuddyListControl", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.component.JSUIBean", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.component.JSUIBeanListener", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UIStateManager", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UIMainChatWindow", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UIPopupManager", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UIChatWindow", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UIAddContactPopupWindow", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UIChatDragDrop", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UIChatResize", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UICreateNewRoomPopupWindow", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UIRoomConfigPopupWindow", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UIJoinRoomPopupWindow", "/chatbar/javascript/");
eXo.require("eXo.communication.chat.webui.UIChatBar", "/chatbar/javascript/");
eXo.communication.chat.eXoChat = {
  /**
   * This method use to initialize all data before call UIMainChatWindow to init all chat application component 
   */
	alert("hello come here") ;
  init : function() {
    try {
      var thys = eXo.communication.chat.eXoChat;
      var UIChatNode = document.getElementById('UIChat');
      var eXoToken = UIChatNode.getAttribute('eXoToken');
      var userName = UIChatNode.getAttribute('userName');
      eXo.communication.chat.core.LocalTemplateEngine.init('templateArea');
      eXo.communication.chat.webui.UIMainChatWindow.init(thys.applicationId, eXoToken, userName);
      eXo.communication.chat.webui.UIChatDragDrop.init(
        eXo.communication.chat.webui.UIMainChatWindow.chatWindowsContainerNode,
        [{className:'WindowBarLeft', tagName: 'div'}, {className: 'PopupTitle', tagName: 'div'}]);
      eXo.communication.chat.webui.UIMainChatWindow.xLogin(userName);
    } catch (e) {
      throw (new Error('Error while loading chat application.'));
    }
  }
}


