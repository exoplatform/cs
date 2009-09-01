function UIChatBar(){
	
};

UIChatBar.prototype.showMenu = function(obj,event){
	event = window.event || event;
	event.cancelBubble = true;
	eXo.core.DOMUtil.cleanUpHiddenElements();
	var menu = eXo.core.DOMUtil.findFirstDescendantByClass(obj,"div","UIRightClickPopupMenu");
	var isRoomMenu = eXo.core.DOMUtil.findFirstDescendantByClass(menu, 'div', 'RoomData');
	if(isRoomMenu)
		eXo.communication.chat.webui.UIMainChatWindow.jabberGetJoinedRoomList();
	this.switchState(menu);
	eXo.core.DOMUtil.listHideElements(menu);
	if(!this.buddyItemActionMenuNode){
		var UIMainChatWindow = eXo.communication.chat.webui.UIMainChatWindow;
  	this.buddyItemActionMenuNode = UIMainChatWindow.buddyItemActionMenuNode;
	}
	this.buddyItemActionMenuNode.style.display = 'none';
};

UIChatBar.prototype.switchState = function(obj){
	if(obj.style.display != "none") obj.style.display = "none";
	else obj.style.display = "block";

};

eXo.communication.chat.webui.UIChatBar = new UIChatBar();