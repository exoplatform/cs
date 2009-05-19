function UIChatBar(){
	
};

UIChatBar.prototype.showMenu = function(obj,event){
	event = window.event || event;
	event.cancelBubble = true;
	eXo.core.DOMUtil.cleanUpHiddenElements();
	var menu = eXo.core.DOMUtil.findFirstDescendantByClass(obj,"div","UIRightClickPopupMenu");
	this.switchState(menu);
	eXo.core.DOMUtil.listHideElements(menu);
};

UIChatBar.prototype.switchState = function(obj){
	if(obj.style.display != "none") obj.style.display = "none";
	else obj.style.display = "block";

};

eXo.communication.chat.webui.UIChatBar = new UIChatBar();