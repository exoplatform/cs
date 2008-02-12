function UIContextMenuCon(){
	this.menus = new Array,
	this.attachedElement = null ;
	this.menuElement = null ;
	this.preventDefault = true ;
	this.preventForms = true ;
  this.tmpMenu = null ;
}

UIContextMenuCon.prototype.getCallback = function(menuId) {
	var menus = document.getElementById(menuId) ;
  if(!menus) return ;
	var callback = menus.getAttribute("eXoCallback") ;
	return callback ;
} ;
UIContextMenuCon.prototype.getPortlet = function(portletid) {
	this.portletName = portletid ;
} ;
UIContextMenuCon.prototype.init = function(conf) {
	var UIContextMenuCon = eXo.webui.UIContextMenuCon ;
	if ( document.all && document.getElementById && !window.opera ) {
		UIContextMenuCon.IE = true;
	}

	if ( !document.all && document.getElementById && !window.opera ) {
		UIContextMenuCon.FF = true;
	}

	if ( document.all && document.getElementById && window.opera ) {
		UIContextMenuCon.OP = true;
	}

	if ( UIContextMenuCon.IE || UIContextMenuCon.FF ) {

		if (conf && typeof(conf.preventDefault) != "undefined") {
			UIContextMenuCon.preventDefault = conf.preventDefault;
		}

		if (conf && typeof(conf.preventForms) != "undefined") {
			UIContextMenuCon.preventForms = conf.preventForms;
		}
		document.getElementById(UIContextMenuCon.portletName).oncontextmenu = UIContextMenuCon.show;
	}
} ;

UIContextMenuCon.prototype.attach = function(classNames, menuId) {
	var UIContextMenuCon = eXo.webui.UIContextMenuCon ;
	if (typeof(classNames) == "string") {
		UIContextMenuCon.menus[classNames] = menuId;
	}

	if (typeof(classNames) == "object") {
		for (x = 0; x < classNames.length; x++) {
			UIContextMenuCon.menus[classNames[x]] = menuId ;
		}
	}
} ;

UIContextMenuCon.prototype.getMenuElementId = function(evt) {
	var _e = window.event || evt ;
	var UIContextMenuCon = eXo.webui.UIContextMenuCon ;
	if (UIContextMenuCon.IE) {
		UIContextMenuCon.attachedElement = _e.srcElement;
	} else {
		UIContextMenuCon.attachedElement = _e.target;
	}

	while(UIContextMenuCon.attachedElement != null) {
		var className = UIContextMenuCon.attachedElement.className;

		if (typeof(className) != "undefined") {
			className = className.replace(/^\s+/g, "").replace(/\s+$/g, "")
			var classArray = className.split(/[ ]+/g);

			for (i = 0; i < classArray.length; i++) {
				if (UIContextMenuCon.menus[classArray[i]]) {
					return UIContextMenuCon.menus[classArray[i]];
				}
			}
		}

		if (UIContextMenuCon.IE) {
			UIContextMenuCon.attachedElement = UIContextMenuCon.attachedElement.parentElement;
		} else {
			UIContextMenuCon.attachedElement = UIContextMenuCon.attachedElement.parentNode;
		}
	}

	return null;
} ;

UIContextMenuCon.prototype.getReturnValue = function(evt) {
	var returnValue = true;
	var _e = window.event || evt;

	if (evt.button != 1) {
		if (evt.target) {
			var el = _e.target;
		} else if (_e.srcElement) {
			var el = _e.srcElement;
		}

		var tname = el.tagName.toLowerCase();

		if ((tname == "input" || tname == "textarea")) {
			if (!UIContextMenuCon.preventForms) {
				returnValue = true;
			} else {
				returnValue = false;
			}
		} else {
			if (!UIContextMenuCon.preventDefault) {
				returnValue = true;
			} else {
				returnValue = false;
			}
		}
	}

	return returnValue;
} ;

UIContextMenuCon.prototype.hasChild = function(root, obj) {
	if(typeof(obj) == "string") obj = document.getElementById(obj) ;
	var children = eXo.core.DOMUtil.findChildrenByClass(root, "div", "UIRightClickPopupMenu") ;
	var len = children.length ;
  for(var i = 0 ; i < len ; i ++) {
  	if (children[i].id == obj.id) return children[i] ;    
  }
	return false ;
} ;

UIContextMenuCon.prototype.show = function(evt) {
	var _e = window.event || evt
	var UIContextMenuCon = eXo.webui.UIContextMenuCon ;
	var menuElementId = UIContextMenuCon.getMenuElementId(_e) ;
	if (menuElementId) {
    
		UIContextMenuCon.menuElement = document.getElementById(menuElementId) ;
		var callback = UIContextMenuCon.getCallback(menuElementId) ;
		if(callback) {
			callback = callback + "(_e)" ;
			eval(callback) ;
		}
		var extraX = (document.getElementById("UIControlWorkspace")) ? document.getElementById("UIControlWorkspace").offsetWidth : 0 ;
		var extraY = 0 ;
		var top = eXo.core.Browser.findMouseYInPage(_e) - extraY ;
		var left = eXo.core.Browser.findMouseXInPage(_e) - extraX ;
		eXo.core.DOMUtil.listHideElements(UIContextMenuCon.menuElement) ;
		var ln = eXo.core.DOMUtil.hideElementList.length ;
		if (ln > 0) {
			for (var i = 0; i < ln; i++) {
				eXo.core.DOMUtil.hideElementList[i].style.display = "none" ;
			}
		}
    if (document.getElementById("UIPageDesktop")) {
      var uiWindow = eXo.core.DOMUtil.findAncestorByClass(document.getElementById(UIContextMenuCon.portletName), "UIWindow") ;
      var extra = (document.getElementById("UIControlWorkspace")) ? document.getElementById("UIControlWorkspace").offsetWidth : 0 ;
      top -= uiWindow.offsetTop ;
      left -= (uiWindow.offsetLeft)  ;
		}
		UIContextMenuCon.menuElement.style.left = left + "px" ;
		UIContextMenuCon.menuElement.style.top = top + "px" ;
		UIContextMenuCon.menuElement.style.display = 'block' ;
		UIContextMenuCon.menuElement.onmouseover = UIContextMenuCon.autoHide ;
		UIContextMenuCon.menuElement.onmouseout = UIContextMenuCon.autoHide ;		
    if (!UIContextMenuCon.IE) {			
      var portlet = document.getElementById(UIContextMenuCon.portletName) ;
      if(UIContextMenuCon.hasChild(portlet, menuElementId)) portlet.removeChild(UIContextMenuCon.hasChild(portlet, menuElementId)) ;
      portlet.appendChild(UIContextMenuCon.menuElement) ;
		}
		return false ;
	}
	return UIContextMenuCon.getReturnValue(_e) ;
} ;

UIContextMenuCon.prototype.autoHide = function(evt) {
	var _e = window.event || evt ;
	var eventType = _e.type ;	
	var UIContextMenuCon = eXo.webui.UIContextMenuCon ;
	if (eventType == 'mouseout') {
		UIContextMenuCon.timeout = setTimeout("eXo.webui.UIContextMenuCon.menuElement.style.display='none'", 5000) ;		
	} else {
		if (UIContextMenuCon.timeout) clearTimeout(UIContextMenuCon.timeout) ;		
	}
} ;

UIContextMenuCon.prototype.replaceall = function(string, obj) {			
	var p = new Array() ;
	var i = 0 ;
	for(var reg in obj){
		p.push(new RegExp(reg)) ;
		string = string.replace(p[i], obj[reg]) ;
		i++ ;
	}
	if (!string) alert("Not match") ;
	return string ;
} ;

UIContextMenuCon.prototype.changeAction = function(obj, id) {
	var actions = eXo.core.DOMUtil.findDescendantsByTagName(obj, "a") ;
	var len = actions.length ;
	var href = "" ;
	if (typeof(id) == "string") {		
		var pattern = /objectId\s*=\s*[A-Za-z0-9_]*(?=&|'|")/ ;
		for(var i = 0 ; i < len ; i++) {
			href = String(actions[i].href) ;
			if (!pattern.test(href)) continue ;
			actions[i].href = href.replace(pattern,"objectId="+id) ;
		}
	} else if (typeof(id) == "object") {
		for(var i = 0 ; i < len ; i++) {
			href = String(actions[i].href) ;			
			actions[i].href = eXo.webui.UIContextMenuCon.replaceall(href, id) ;
		}
	} else {
		return  ;
	}
	
} ;

UIContextMenuCon.prototype.hide = function() {
	var ln = eXo.core.DOMUtil.hideElementList.length ;
	if (ln > 0) {
		for (var i = 0; i < ln; i++) {
			eXo.core.DOMUtil.hideElementList[i].style.display = "none" ;
		}
	}
} ;

UIContextMenuCon.prototype.showHide = function(obj) {
	if (obj.style.display != "block") {
		eXo.webui.UIContextMenuCon.hide() ;
		obj.style.display = "block" ;
		eXo.core.DOMUtil.listHideElements(obj) ;
	} else {
		obj.style.display = "none" ;
	}
} ;

UIContextMenuCon.prototype.swapMenu = function(oldmenu, clickobj) {
	var UIContextMenuCon = eXo.webui.UIContextMenuCon ;
	var Browser = eXo.core.Browser ;
	var menuX = Browser.findPosX(clickobj) ;
	var menuY = Browser.findPosY(clickobj) + clickobj.offsetHeight ;
	if (arguments.length > 2) { // Customize position of menu with an object that have 2 properties x, y 
		menuX = arguments[2].x ;
		menuY = arguments[2].y ;
	}	
	if(document.getElementById("tmpMenuElement")) document.getElementById("UIPortalApplication").removeChild(document.getElementById("tmpMenuElement")) ;
	var tmpMenuElement = oldmenu.cloneNode(true) ;
	tmpMenuElement.setAttribute("id","tmpMenuElement") ;
	UIContextMenuCon.menuElement = tmpMenuElement ;
	document.getElementById("UIPortalApplication").appendChild(tmpMenuElement) ;
	UIContextMenuCon.menuElement.style.top = menuY + "px" ;
	UIContextMenuCon.menuElement.style.left = menuX + "px" ;	
	UIContextMenuCon.showHide(UIContextMenuCon.menuElement) ;
} ;

eXo.webui.UIContextMenuCon = new UIContextMenuCon() ;