/**
 * @author uocnguyen
 */
eXo.require("eXo.core.JSON");
eXo.require("eXo.core.Cometd");
eXo.require("eXo.core.HTMLUtil");
eXo.require("eXo.core.Resize");
eXo.require("eXo.webui.UIHorizontalTabs");
//eXo.require("eXo.desktop.UIWindow");

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

eXo.require("eXo.communication.chat.core.DateFormat", "/chat/javascript/");
eXo.require("eXo.communication.chat.core.Cometd", "/chat/javascript/");
eXo.require("eXo.communication.chat.core.AdvancedDOMEvent", "/chat/javascript/");
eXo.require("eXo.communication.chat.core.JSLogger", "/chat/javascript/");
eXo.require("eXo.communication.chat.core.XMPPCommunicator", "/chat/javascript/");
eXo.require("eXo.communication.chat.core.Utils", "/chat/javascript/");
eXo.require("eXo.communication.chat.core.LocalTemplateEngine", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UIWindow", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.TabScrollManager", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.component.BuddyListControl", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.component.JSUIBean", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.component.JSUIBeanListener", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UIStateManager", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UIMainChatWindow", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UIPopupManager", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UIChatWindow", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UIAddContactPopupWindow", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UIChatDragDrop", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UIChatResize", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UICreateNewRoomPopupWindow", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UIRoomConfigPopupWindow", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UIJoinRoomPopupWindow", "/chat/javascript/");

eXo.communication.chat.eXoChat = {
  init : function() {
    try {
      var that = eXo.communication.chat.eXoChat;
      var UIChatNode = document.getElementById('UIChat');
      var eXoToken = UIChatNode.getAttribute('eXoToken');
      var userName = UIChatNode.getAttribute('userName');
      eXo.communication.chat.core.LocalTemplateEngine.init('templateArea');
      eXo.communication.chat.webui.UIMainChatWindow.init(that.applicationId, eXoToken, userName);
      eXo.communication.chat.webui.UIChatDragDrop.init(
        eXo.communication.chat.webui.UIMainChatWindow.chatWindowsContainerNode,
        [{className:'WindowBarLeft', tagName: 'div'}, {className: 'PopupTitle', tagName: 'div'}]);
      eXo.communication.chat.webui.UIMainChatWindow.xLogin(userName);
    } catch (e) {
      throw (new Error('Error while loading chat application.'));
    }
  }
}


