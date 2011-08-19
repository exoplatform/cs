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
eXo.communication.chat = {
    eXoChat : {},
    core : {},
    locale : {},
    webui : {
      component : {}
    }
}

eXo.chatRequire = function(module, jsLocation) {  
  window.status = "Loading Javascript Module " + module ;
  if(jsLocation == null) jsLocation = '/eXoResources/javascript/' ;
  var path = jsLocation  + module.replace(/\./g, '/')  + '.js' ;
  eXo.loadJS(path);
} ;

// Overwrite html entities to ignore white space from html encode
eXo.core.HTMLUtil.entities.nbsp = null;

eXo.require("eXo.communication.chat.core.DateFormat", "/chat/javascript/");
eXo.require("eXo.cs.CSUtils", "/csResources/javascript/");
//eXo.require("eXo.cs.CSCometd", "/csResources/javascript/");
eXo.require("eXo.communication.chat.core.AdvancedDOMEvent", "/chat/javascript/");
eXo.require("eXo.communication.chat.core.JSLogger", "/chat/javascript/");
eXo.require("eXo.communication.chat.core.XMPPCommunicator", "/chat/javascript/");
eXo.require("eXo.communication.chat.core.Utils", "/chat/javascript/");
eXo.require("eXo.communication.chat.core.LocalTemplateEngine", "/chat/javascript/");
eXo.chatRequire("eXo.communication.chat.webui.UIWindow", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UIPageIterator", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.TabScrollManager", "/chat/javascript/");
eXo.require("eXo.communication.chat.webui.UISlideAlert", "/chat/javascript/");
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
  /**
   * This method use to initialize all data before call UIMainChatWindow to init all chat application component 
   */
  init : function() {
    try {
      var thys = eXo.communication.chat.eXoChat;
      var UIChatNode = document.getElementById('UIChat');
      var eXoToken = UIChatNode.getAttribute('eXoToken');
      var userName = UIChatNode.getAttribute('userName');
      eXo.communication.chat.core.XMPPCommunicator.init(thys.restcontextname);
      eXo.communication.chat.core.LocalTemplateEngine.init('templateArea');
      eXo.communication.chat.webui.UIMainChatWindow.init(thys.applicationId, eXoToken, userName, thys.cometdcontextname);
      eXo.communication.chat.webui.UIChatDragDrop.init(
        eXo.communication.chat.webui.UIMainChatWindow.chatWindowsContainerNode,
        [{className:'OverflowContainer', tagName: 'div'}, {className: 'PopupTitle', tagName: 'span'}]);
      eXo.communication.chat.webui.UIMainChatWindow.xLogin(userName);
    } catch (e) {
      throw (new Error('Error while loading chat application.'));
    }
  }
}


