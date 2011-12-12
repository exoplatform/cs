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
    locale : {},
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
      var UIChatNode = document.getElementById(thys.applicationId);
      var tempVar = eXo.core.DOMUtil.findDescendantById(UIChatNode,thys.applicationId);
      UIChatNode = (tempVar) ? tempVar : UIChatNode;
      var eXoToken = UIChatNode.getAttribute('eXoToken');
      var userName = UIChatNode.getAttribute('userName');
      eXo.communication.chatbar.core.XMPPCommunicator.init(thys.restcontextname);
      eXo.communication.chatbar.core.LocalTemplateEngine.init('templateArea');
      eXo.communication.chatbar.webui.UIMainChatWindow.init(thys.applicationId, eXoToken, userName, thys.cometdcontextname);
      eXo.communication.chatbar.webui.UIChatDragDrop.init(
        eXo.communication.chatbar.webui.UIMainChatWindow.chatWindowsContainerNode,
        [{className:'OverflowContainer', tagName: 'div'}, {className: 'PopupTitle', tagName: 'span'}]);
      eXo.communication.chatbar.webui.UIMainChatWindow.xLogin(userName);
      thys.setWidth();
    } catch (e) {
      throw (new Error('Error while loading chat application.'));
    }
  },
  setWidth : function(){
    var obj = document.getElementById(eXo.communication.chatbar.eXoChatBar.applicationId);
    var tempVar = eXo.core.DOMUtil.findDescendantById(obj,eXo.communication.chatbar.eXoChatBar.applicationId);;
    obj = (tempVar) ? tempVar : obj;  
    var uiWindow = eXo.core.DOMUtil.findAncestorByClass(obj,"UIWindow");
    var CoveringSpaceNode = null;
    if (uiWindow) {
      obj = uiWindow;
      CoveringSpaceNode = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UICharBarPortletCoveringSpace");
    }
    var uiPage = document.getElementById('UIPage') ;
    if(obj)	{
      obj.style.width = uiPage.offsetWidth + "px";
      if (eXo.portal.portalMode != 0) { // if the portal page is not in view mode.
        if (!CoveringSpaceNode) CoveringSpaceNode = eXo.core.DOMUtil.findFirstDescendantByClass(obj.offsetParent, "div", "UICharBarPortletCoveringSpace");
        obj.style.position = 'static';
      } else {
        obj.style.position = "fixed";
        obj.style.bottom = "0px";
        if(eXo.core.Browser.isIE6()){
          obj.style.position = "absolute";
          obj.style.top = (eXo.core.Browser.getBrowserHeight() - obj.offsetHeight + document.documentElement.scrollTop) + "px";
        }
        window.setTimeout('eXo.communication.chatbar.eXoChatBar.setWidth()', 100);
      }
      if (CoveringSpaceNode) CoveringSpaceNode.style.display = 'none';
    }
  }
}
