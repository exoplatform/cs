function UIChatBar(){
};

UIChatBar.prototype.autoHidePopupMenuChatbar = function(evt) {
  var _e = window.event || evt ;
  var eventType = _e.type ;
  if (eventType == 'mouseout' && (this.style.display != "none")) {
    eXo.cs.Utils.contextMenuTimeout = window.setTimeout("document.getElementById('" + this.id + "').style.display='none'", 1500) ;
  } else {
    if (eXo.cs.Utils.contextMenuTimeout) {
      window.clearTimeout(eXo.cs.Utils.contextMenuTimeout) ;    
      eXo.cs.Utils.contextMenuTimeout.timeout = null ;    
    }
  }
};

UIChatBar.prototype.showMenu = function(obj,event){
	event = window.event || event;
	event.cancelBubble = true;
	eXo.core.DOMUtil.cleanUpHiddenElements();
	var menu = eXo.core.DOMUtil.findFirstDescendantByClass(obj,"div","UIRightClickPopupMenu");
	var isRoomMenu = eXo.core.DOMUtil.findFirstDescendantByClass(menu, 'div', 'RoomData');
	if(isRoomMenu)
		eXo.communication.chatbar.webui.UIMainChatWindow.jabberGetJoinedRoomList();
	this.switchState(menu);
	eXo.core.DOMUtil.listHideElements(menu);
	if(!this.buddyItemActionMenuNode){
		var UIMainChatWindow = eXo.communication.chatbar.webui.UIMainChatWindow;
  	this.buddyItemActionMenuNode = UIMainChatWindow.buddyItemActionMenuNode;
	}
	this.buddyItemActionMenuNode.style.display = 'none';
	menu.onmouseover = eXo.communication.chatbar.webui.UIChatBar.autoHidePopupMenuChatbar;
	menu.onmouseout = eXo.communication.chatbar.webui.UIChatBar.autoHidePopupMenuChatbar;
};

UIChatBar.prototype.switchState = function(obj){
	if(obj.style.display != "none") obj.style.display = "none";
	else obj.style.display = "block";

};

//begin chat message popup menu
UIChatBar.prototype.showContextMenu = function(compid, username){
    var UIContextMenuChatbar = eXo.webui.UIContextMenuChatbar;
    UIContextMenuChatbar.chatbarPortletId = compid;
    UIContextMenuChatbar.portletName = compid;
    UIContextMenuChatbar.chatusername = username;
    var config = {
        'preventDefault': true,
        'preventForms': true
    };
    UIContextMenuChatbar.init(config);
    UIContextMenuChatbar.attach('ChatMsgItem', 'UIChatMsgPopupMenu'); 
};

UIChatBar.prototype.chatMsgPopupMenuCallback = function(evt){
  var UIContextMenuChatbar = eXo.webui.UIContextMenuChatbar;
  var DOMUtil = eXo.core.DOMUtil;
  var src = eXo.core.EventManager.getEventTargetByTagName(evt, "div");
  var chatMsgId = src.id;
  eXo.communication.chatbar.webui.UIChatBar.changeAction(UIContextMenuChatbar.menuElement, chatMsgId);
};

UIChatBar.prototype.changeAction = function(menu, id){
  var subDivs = eXo.core.DOMUtil.findDescendantsByTagName(menu, "div");
  var UIChatWindow = eXo.communication.chatbar.webui.UIChatWindow;
  var divsLen = subDivs.length;
  	
  for (var i = 0; i < divsLen; i++) {
      if (subDivs[i].id == UIChatWindow.EDIT_CHAT_MESSAGE_ID || subDivs[i].id == UIChatWindow.DEL_CHAT_MESSAGE_ID){
      	subDivs[i].setAttribute('chatmessageId', id);
      //	if(id == null) subDivs[i].className = "MenuItemDisabled"; 
      }	
  }
};
//end chat message popup menu
eXo.communication.chatbar.webui.UIChatBar = new UIChatBar();