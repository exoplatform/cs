if(!eXo.cs) eXo.cs = {};
function UIEmailInput(){
};

UIEmailInput.prototype.remove = function(obj){
	var uiEmailAddressItem = obj.parentNode;
	var uiEmailAddressLabel = eXo.core.DOMUtil.findPreviousElementByTagName(obj,"div");
	var uiEmailInput = eXo.core.DOMUtil.findAncestorByClass(obj,"UIEmailInput");
	uiEmailInput = eXo.core.DOMUtil.getChildrenByTagName(uiEmailInput,"input")[0];
	uiEmailAddressLabel = uiEmailAddressLabel.innerHTML.toString().trim();
	uiEmailInput.value = this.removeItem(uiEmailInput.value,uiEmailAddressLabel);
	eXo.core.DOMUtil.removeElement(uiEmailAddressItem);
};

UIEmailInput.prototype.removeItem = function(str,removeValue){
	if(str.indexOf(",") <= 0) return str;
	var list = str.split(",");
	list.remove(removeValue);
	var tmp = "";
	for(var i = 0 ; i < list.length; i++){
		tmp += ","+list[i];
	}
	return tmp.substr(1,tmp.length);
};


eXo.cs.UIEmailInput = new UIEmailInput();
