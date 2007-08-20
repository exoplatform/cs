function UIForumPortlet() {};

UIForumPortlet.prototype.OverButtun = function(oject) {
	if(oject.className.indexOf("Style") > 0){
		var Srt = "";
		for(var i=0; i<oject.className.length - 5; i++) {
			Srt = Srt + oject.className.charAt(i);
		}
		oject.className = Srt;
	}	else oject.className = oject.className + "Style";
};



eXo.forum.UIForumPortlet = new UIForumPortlet() ;