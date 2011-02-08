function UIMailPortlet(){
};

UIMailPortlet.prototype.showContextMenu = function(compid){
    var UIContextMenuMail = eXo.webui.UIContextMenuMail; //eXo.contact.ContextMenu ;
    this.portletNode = document.getElementById(compid);
    UIContextMenuMail.portletName = compid;
    var config = {
        'preventDefault': false,
        'preventForms': false
    };
    UIContextMenuMail.init(config);
    UIContextMenuMail.attach('MessageItem', 'UIMessagePopupMenu');
    UIContextMenuMail.attach('CustomizeFolder', 'UIFolderListPopupMenu');
    UIContextMenuMail.attach('CustomizeFolderPop3', 'UIFolderListPopupMenu');
    UIContextMenuMail.attach('IconTagHolder', 'UITagListPopupMenu');
    UIContextMenuMail.attach('InboxIcon', 'UIDefaultFolderPopupMenu');
    UIContextMenuMail.attach('DraftsIcon', 'UIDefaultFolderPopupMenu');
    UIContextMenuMail.attach('SentIcon', 'UIDefaultFolderPopupMenu');
    UIContextMenuMail.attach('SpamIcon', 'UIDefaultFolderPopupMenu');
    UIContextMenuMail.attach('TrashIcon', 'UITrashFolderPopupMenu');
};

UIMailPortlet.prototype.msgPopupMenuCallback = function(evt){
    var UIContextMenuMail = eXo.webui.UIContextMenuMail;
    var DOMUtil = eXo.core.DOMUtil;
    var src = eXo.core.EventManager.getEventTargetByTagName(evt, "tr");
    if (!DOMUtil.hasClass(src, "SelectedItem")) {
        var tbody = DOMUtil.findAncestorByTagName(src, "tbody");
        eXo.mail.CheckBox.uncheckAll(tbody);
    }
    var check = DOMUtil.findFirstDescendantByClass(src, "input", "checkbox");
    check.checked = true;
    DOMUtil.addClass(src, "SelectedItem");
    var id = src.getAttribute("msgId");
    eXo.mail.UIMailPortlet.changeAction(UIContextMenuMail.menuElement, id);
};

UIMailPortlet.prototype.changeAction = function(menu, id){
    var actions = eXo.core.DOMUtil.findDescendantsByTagName(menu, "a");
    var len = actions.length;
    var href = "";
    var pattern = /objectId\s*=.*(?=&|\>|'|")/;
    for (var i = 0; i < len; i++) {
        href = String(actions[i].href);
        if (!pattern.test(href)) 
            continue;
        actions[i].href = href.replace(pattern, "objectId=" + id);
    }
};

UIMailPortlet.prototype.defaultFolderPopupMenuCallback = function(evt){
    var UIContextMenuMail = eXo.webui.UIContextMenuMail;
    var src = eXo.core.EventManager.getEventTargetByTagName(evt, "div");
    var folder = src.getAttribute("folder");
    eXo.webui.UIContextMenuMail.changeAction(UIContextMenuMail.menuElement, folder);
};

UIMailPortlet.prototype.tagListPopupMenuCallback = function(evt){
    var UIContextMenuMail = eXo.webui.UIContextMenuMail;
    var src = eXo.core.EventManager.getEventTargetByTagName(evt, "a");
    var tagName = src.getAttribute("tagId");
    eXo.webui.UIContextMenuMail.changeAction(UIContextMenuMail.menuElement, tagName);
}

UIMailPortlet.prototype.readMessage = function(){
};

UIMailPortlet.prototype.showPrintPreview = function(obj1){
    var uiPortalApplication = document.getElementById("UIPortalApplication");
    uiPortalApplication.style.visibility = "hidden";
    var uiMailPortletNode = document.createElement('div');
    uiMailPortletNode.className = 'UIMailPortlet';
    var mailWorkingWorkspaceNode = document.createElement('div');
    mailWorkingWorkspaceNode.className = 'MailWorkingWorkspace';
    var uiMessagePreviewNode = document.createElement('div');
    uiMessagePreviewNode.className = 'UIMessagePreview';
    var frame = document.createElement("iframe");
    frame.frameBorder = 0;
    var obj = obj1.cloneNode(true);
    var printContent = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "PrintContent");
    var str = printContent.firstChild.value;
    obj1.style.display = "none";
    document.body.style.background = "white";
    printContent.innerHTML = "";
    printContent.appendChild(frame);
    uiMessagePreviewNode.appendChild(obj);
    mailWorkingWorkspaceNode.appendChild(uiMessagePreviewNode);
    uiMailPortletNode.appendChild(mailWorkingWorkspaceNode);
    uiMailPortletNode.style.width = "99%";
    uiMailPortletNode.style.position = "absolute";
    uiMailPortletNode.style.zIndex = 1;
    if (eXo.core.Browser.browserType == "ie") 
        uiMailPortletNode.style.height = document.documentElement.scrollHeight + "px";
    document.body.insertBefore(uiMailPortletNode, uiPortalApplication);
    frame.style.width = printContent.offsetWidth + "px";
    var doc = frame.contentWindow.document;
    doc.open();
    doc.write(str);
    doc.close();
    if (eXo.core.Browser.isFF()) {
        doc.body.style.visibility = "visible";
        frame.style.height = doc.documentElement.offsetHeight + 20 + "px";
        frame.style.width = doc.body.offsetWidth + "px";
    } else {
        var docHt = 0, sh, oh;
        if (doc.height) {
            docHt = doc.height;
        }
        else 
            if (doc.body) {
                if (doc.body.scrollHeight) 
                    docHt = sh = doc.body.scrollHeight;
                if (doc.body.offsetHeight) 
                    docHt = oh = doc.body.offsetHeight;
                if (sh && oh) 
                    docHt = Math.max(sh, oh);
            }
        frame.style.width = doc.body.scrollWidth + "px";
        frame.style.height = "auto";
        frame.style.height = docHt + 20 + "px";
    }
    uiPortalApplication.style.height = printContent.offsetHeight + "px";
    uiPortalApplication.style.overflow = "hidden";
    if (document.getElementById("UIPageDesktop")) 
        uiPortalApplication.style.display = "none";
    window.scroll(0, 0);
};

UIMailPortlet.prototype.printMessage = function(){
    window.print()
};

UIMailPortlet.prototype.closePrint = function(){
    var DOMUtil = eXo.core.DOMUtil;
    var uiPortalApplication = document.getElementById("UIPortalApplication");
    uiPortalApplication.style.display = "block";
    uiPortalApplication.style.height = "auto";
    uiPortalApplication.style.overflow = "";
    uiPortalApplication.style.visibility = "visible";
    for (var i = 0; i < document.body.childNodes.length; i++) {
        if (document.body.childNodes[i].className == "UIMailPortlet") 
            DOMUtil.removeElement(document.body.childNodes[i]);
    }
    if (document.body.style) 
        document.body.removeAttribute("style");
    if (document.getElementById("UIPageDesktop")) 
        uiPortalApplication.style.display = "block";
    window.scroll(0, 0);
};
/*
UIMailPortlet.prototype.switchLayout = function(layout){
    var layoutMan = eXo.mail.LayoutManager;
    if (layout == 0) {
        layoutMan.reset();
        return;
    }
    layoutMan.switchLayout(layout);
    return;
};

UIMailPortlet.prototype.changeMenuLabel = function(layout, layoutState){
    var csMailLayoutSwitchMenuNode = document.getElementById('_CSMailLayoutSwitchMenu');
    var menuItems = eXo.core.DOMUtil.findDescendantsByClass(csMailLayoutSwitchMenuNode, 'div', 'MenuItem');
    var menuItemTexts = eXo.core.DOMUtil.findDescendantsByClass(csMailLayoutSwitchMenuNode, 'a', 'ItemIcon');
    var fontWeight = false;
    for (var i = 0; i < menuItems.length; i++) {
        if (menuItemTexts[i]) {
            if (layout == "all") {
                if (layoutState) {
                    menuItemTexts[i].innerHTML = menuItemTexts[i].innerHTML.replace('Show', 'Hide');
                }
                else 
                    if (!layoutState) {
                        menuItemTexts[i].innerHTML = menuItemTexts[i].innerHTML.replace('Hide', 'Show');
                    }
                continue;
            }
            if (layout == 0 ||
            (layoutState && i == layout)) {
                menuItemTexts[i].innerHTML = menuItemTexts[i].innerHTML.replace('Show', 'Hide');
            }
            else 
                if (!layoutState && i == layout) {
                    menuItemTexts[i].innerHTML = menuItemTexts[i].innerHTML.replace('Hide', 'Show');
                }
        }
    }
};

UIMailPortlet.prototype.switchLayoutCallback = function(layout, status){
    var layoutMan = eXo.mail.LayoutManager;
    var workingarea = document.getElementById("UIMessageArea");
    var actionReadingPane = eXo.core.DOMUtil.findDescendantById(workingarea, "ActionReadingPane");
    if (!status) {
        if (layout == 1) 
            workingarea.style.marginLeft = "0px";
        if (layout == 2) 
            actionReadingPane.className = "MinimumReadingPane";
        if (layout == 3) {
            document.getElementById("uiMessageGrid").style.overflowY = "visible";
            document.getElementById("uiMessageGrid").style.height = "auto";
        }
        
    }
    else {
        if (layout == 1) 
            workingarea.style.marginLeft = "225px";
        if (layout == 2) 
            actionReadingPane.className = "MaximizeReadingPane";
        
        if (layout == 3) {
            document.getElementById("uiMessageGrid").style.overflowY = "auto";
            document.getElementById("uiMessageGrid").style.height = "200px";
        }
    }
    eXo.mail.UIMailPortlet.changeMenuLabel(layout, status);
};

UIMailPortlet.prototype.checkLayoutCallback = function(layoutcookie){
    var uiMailPortlet = eXo.mail.UIMailPortlet;
    var i = layoutcookie.length;
    while (i--) {
        uiMailPortlet.changeMenuLabel(parseInt(layoutcookie.charAt(i)), false);
        if (parseInt(layoutcookie.charAt(i)) == 1) {
            var workingarea = document.getElementById("UIMessageArea");
            workingarea.style.marginLeft = "0px";
        }
        if (parseInt(layoutcookie.charAt(i)) == 3) {
            document.getElementById("uiMessageGrid").style.overflowY = "visible";
            document.getElementById("uiMessageGrid").style.height = "auto";
        }
    }
};

UIMailPortlet.prototype.resetLayoutCallback = function(){
    var workingarea = document.getElementById("UIMessageArea");
    eXo.mail.UIMailPortlet.changeMenuLabel("all", true);
    document.getElementById("uiMessageGrid").style.overflowY = "auto";
    document.getElementById("uiMessageGrid").style.height = "200px";
    workingarea.style.marginLeft = "225px";
};

UIMailPortlet.prototype.checkLayout = function(){
	try {
	eXo.mail.LayoutManager = new LayoutManager("maillayout");
    var layout1 = document.getElementById("UINavigationContainer");
    var layout2 = document.getElementById("uiMessageListResizableArea");
    var layout3 = document.getElementById("SpliterResizableArea");
    eXo.mail.LayoutManager.layouts = [];
    eXo.mail.LayoutManager.layouts.push(layout1);
    eXo.mail.LayoutManager.layouts.push(layout2);
    eXo.mail.LayoutManager.layouts.push(layout3);
    eXo.mail.LayoutManager.switchCallback = eXo.mail.UIMailPortlet.switchLayoutCallback;
    eXo.mail.LayoutManager.callback = eXo.mail.UIMailPortlet.checkLayoutCallback;
    eXo.mail.LayoutManager.resetCallback = eXo.mail.UIMailPortlet.resetLayoutCallback;
    eXo.mail.LayoutManager.check();
    this.setScroll();
	} catch(e) {}
};
*/
UIMailPortlet.prototype.showHideAddMoreAddress = function(add){
    var elm = document.getElementById(add);
    if (elm.style.display == "none") {
        elm.style.display = "";
    }
};

UIMailPortlet.prototype.showHidePreviewPane = function(obj){
	var DOMUtil = eXo.core.DOMUtil;
    var actionButton = DOMUtil.findDescendantsByTagName(obj, "span")[0];
    var spliterContainer = DOMUtil.findAncestorByClass(obj, "SpliterContainer");
    var uiMessageArea = DOMUtil.findFirstDescendantByClass(spliterContainer, "div", "UIMessageArea");
		var resizePane = DOMUtil.findNextElementByTagName(uiMessageArea,"div"); 
    if (uiMessageArea.style.display == "none"){
        actionButton.className = "MaximizeReadingPane";
				uiMessageArea.style.display = "block";
				resizePane.style.display = "block";
		} 
    else {
        actionButton.className = "MinimumReadingPane";
				uiMessageArea.style.display = "none";
				resizePane.style.display = "none";
		}
};

UIMailPortlet.prototype.showHideMessageHeader = function(obj){
    var DOMUtil = eXo.core.DOMUtil;
    var decorator = DOMUtil.findAncestorByClass(obj, "DecoratorBox");
    var colapse = DOMUtil.findDescendantById(decorator, "CollapseMessageAddressPreview");
    var expand = DOMUtil.findDescendantById(decorator, "MessageAddressPreview");
    var showhide = obj.getAttribute("showhideheader");
    var show = showhide.substring(0, showhide.indexOf(","));
    var hide = showhide.substring(showhide.indexOf(",") + 1, showhide.length);
    if (colapse.style.display == "none") {
        expand.style.display = "none";
        colapse.style.display = "block"
        obj.innerHTML = show;
    }
    else {
        colapse.style.display = "none"
        expand.style.display = "block";
        obj.innerHTML = hide;
    }
    var icons = eXo.core.DOMUtil.findDescendantsByClass(obj.parentNode, 'div', 'DownArrow1Icon');
    if (icons.length > 0) {
        icons[0].className = 'NextArrow1Icon';
    }
    else {
        icons = eXo.core.DOMUtil.findDescendantsByClass(obj.parentNode, 'div', 'NextArrow1Icon');
        if (icons.length > 0) {
            icons[0].className = 'DownArrow1Icon';
        }
    }
};

UIMailPortlet.prototype.showHideMessageDetails = function(obj){
    var DOMUtil = eXo.core.DOMUtil;
    var paneDetails = DOMUtil.findAncestorByClass(obj, "ReadingPaneDetails");
    var expands = DOMUtil.findDescendantsByClass(paneDetails, "div", "ExpandMessage");
    var numberExpand = 0;
    for (var i = 0; i < expands.length; i++) {
        if (expands[i].style.display == "block") 
            numberExpand++;
    }
    var decorator = DOMUtil.findAncestorByClass(obj, "DecoratorBox");
    if ((obj.id == "CollapseMessageAddressPreview") && numberExpand > 1) {
        var expand = DOMUtil.findFirstDescendantByClass(decorator, "div", "ExpandMessage");
        var collapse = DOMUtil.findFirstDescendantByClass(decorator, "div", "CollapseMessage");
        expand.style.display = "none";
        collapse.style.display = "block";
    }
    else 
        if (obj.id == "CollapseMessage") {
            var expand = DOMUtil.findNextElementByTagName(obj, "div");
            obj.style.display = "none";
            expand.style.display = "block";
        }
};

UIMailPortlet.prototype.showMessageAction = function(obj, evt){
    var DOMUtil = eXo.core.DOMUtil;
    eXo.cs.Utils.show(obj, evt);
    var menu = DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu");
		if(eXo.core.I18n.lt) {
			if(!menu.style.left) menu.style.left = "0px";
			menu.style.left = (parseInt(menu.style.left) + obj.offsetWidth - menu.offsetWidth) + "px";
			menu.style.left = (obj.offsetWidth - menu.offsetWidth) + "px";
		}
    var uiResizableBlock = DOMUtil.findAncestorByClass(obj, "UIResizableBlock");
    if (eXo.core.Browser.isIE6() && uiResizableBlock) {
        this.actionMenuTop = menu.offsetTop + uiResizableBlock.scrollTop;
        uiResizableBlock.onscroll = function(){
            menu.style.top = (eXo.mail.UIMailPortlet.actionMenuTop - this.scrollTop) + "px";
        }
    }
};

UIMailPortlet.prototype.isAllday = function(form){
    if (typeof(form) == "string") 
        form = eXo.mail.UIMailPortlet.getElementById(form);
    if (form.tagName.toLowerCase() != "form") {
        form = eXo.core.DOMUtil.findDescendantsByTagName(form, "form");
    }
    var element = eXo.core.DOMUtil.findFirstDescendantByClass(form, "input", "checkbox");
    eXo.mail.UIMailPortlet.showHideTime(element);
};

UIMailPortlet.prototype.showHideTime = function(chk){
    var DOMUtil = eXo.core.DOMUtil;
    if (chk.tagName.toLowerCase() != "input") {
        chk = DOMUtil.findFirstDescendantByClass(chk, "input", "checkbox");
    }
    var selectboxes = DOMUtil.findDescendantsByTagName(chk.form, "input");
    var fields = new Array();
    var len = selectboxes.length;
    for (var i = 0; i < len; i++) {
        if ((selectboxes[i].getAttribute("name") == "toTime") || (selectboxes[i].getAttribute("name") == "fromTime")) {
            fields.push(selectboxes[i]);
        }
    }
    eXo.mail.UIMailPortlet.showHideField(chk, fields);
};

UIMailPortlet.prototype.showHideField = function(chk, fields){
    var display = "";
    if (typeof(chk) == "string") 
        chk = eXo.mail.UIMailPortlet.getElementById(chk);
    display = (chk.checked) ? "hidden" : "visible";
    var len = fields.length;
    for (var i = 0; i < len; i++) {
        fields[i].style.visibility = display;
    }
};

UIMailPortlet.prototype.collapseExpandFolder = function(obj, folderState){
    var DOMUtil = eXo.core.DOMUtil;
    var colExpContainerNode = DOMUtil.findNextElementByTagName(obj, "div");
    var ftitle = obj.getAttribute("titlefolder");
    var collapse = ftitle.substring(ftitle.indexOf(",") + 1, ftitle.length) + " ";
    var expand = ftitle.substring(0, ftitle.indexOf(",")) + " ";
    var objClass = obj.className;
    var folderId = obj.getAttribute('folder');
    if (!folderState) {
        if (objClass.indexOf(" OpenFolder") != -1) {
            obj.className = objClass.replace('OpenFolder', 'CloseFolder');
            obj.title = expand;
            folderState = '0';
        }
        else 
            if (objClass.indexOf(" CloseFolder") != -1) {
                obj.className = objClass.replace('CloseFolder', 'OpenFolder');
                obj.title = collapse;
                folderState = '1';
            }
    }
    else 
        if (folderState == '1') {
            obj.className = objClass.replace('CloseFolder', 'OpenFolder');
            obj.title = collapse;
        }
        else 
            if (folderState == '0') {
                obj.className = objClass.replace('OpenFolder', 'CloseFolder');
                obj.title = expand;
            }
    
    var collapseContainerNode = DOMUtil.findAncestorByClass(obj, 'Collapse');
    while (collapseContainerNode) {
        collapseContainerNode.className = 'Expand';
        var tmpNode = DOMUtil.findPreviousElementByTagName(collapseContainerNode, 'div');
        if (tmpNode) {
            tmpNode.className = tmpNode.className.replace('CloseFolder', 'OpenFolder');
        }
        collapseContainerNode = DOMUtil.findAncestorByClass(collapseContainerNode, 'Collapse');
    }
    if (colExpContainerNode != null &&
    colExpContainerNode.className &&
    (colExpContainerNode.className.indexOf('Collapse') != -1 || colExpContainerNode.className.indexOf('Expand') != -1)) {
        if (folderState == '0') {
            colExpContainerNode.className = "Collapse";
        }
        else 
            if (folderState == '1') {
                colExpContainerNode.className = "Expand";
            }
    }
    eXo.mail.UIMailPortlet.updateFolderState(folderId, folderState);
};

UIMailPortlet.prototype.updateFolderState = function(folderId, folderState){
    if (!this.uiFolderContainerNode) {
        this.uiFolderContainerNode = eXo.mail.UIMailPortlet.getElementById('UIFolderContainer');
    }
    var dateExpire = 365;
    eXo.core.Browser.setCookie('cs.mail.lastfoldershow', folderId, dateExpire);
    eXo.core.Browser.setCookie('cs.mail.folderstate', folderState, dateExpire);
};

UIMailPortlet.prototype.restoreFolderState = function(){
    var folderId = eXo.core.Browser.getCookie('cs.mail.lastfoldershow');
    if (!folderId) {
        return;
    }
    var folderState = eXo.core.Browser.getCookie('cs.mail.folderstate');
    this.uiFolderContainerNode = eXo.mail.UIMailPortlet.getElementById('UIFolderContainer');
    var folderNodes = eXo.core.DOMUtil.findDescendantsByClass(this.uiFolderContainerNode, 'div', 'Folder');
    for (var i = 0; i < folderNodes.length; i++) {
        var folderIdTmp = folderNodes[i].getAttribute('folder');
        if (folderId == folderIdTmp) {
            this.collapseExpandFolder(folderNodes[i], folderState);
            break;
        }
    }
};

UIMailPortlet.prototype.setScroll = function(){
    var obj = eXo.mail.UIMailPortlet.getElementById("uiMessageGrid");
    if (!obj) 
        return;
    var scroll = parseInt(eXo.core.Browser.getCookie("scrollstatus"));
    obj.scrollTop = scroll;
    obj.onclick = eXo.mail.UIMailPortlet.saveScroll;
};

UIMailPortlet.prototype.saveScroll = function(){
    eXo.core.Browser.setCookie("scrollstatus", this.scrollTop, 1);
};

UIMailPortlet.prototype.encodeHTML = function(str){
	return str.replace(/&/g, "&amp;").replace(/"/g, "&quot;").replace(/'/g, "&#39;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
};

UIMailPortlet.prototype.resizeIframe = function(textAreaId, frameId, styleExpand, contentType){
    var frame = eXo.mail.UIMailPortlet.getElementById(frameId);
    var textAreas = eXo.mail.UIMailPortlet.getElementById(textAreaId);
    var expandMessage = eXo.core.DOMUtil.findAncestorByClass(frame, "ExpandMessage");
    var previewArea = eXo.mail.UIMailPortlet.getElementById("SpliterResizableArea");
    var beforeDisplay = previewArea.style.display;
    if (beforeDisplay == "none") {
        previewArea.style.display = "block";
    }
    var mailcontent = textAreas.value;
     
    if (contentType != null && contentType.indexOf("text/plain") > -1) 
        mailcontent = this.encodeHTML(mailcontent).replace(/\n/g, "<br>");
    else  
    	 mailcontent = mailcontent.replace(/<;/g, "&lt;").replace(/;>/g, "&gt;").replace(/\n/g, "<br>");   
    var doc = frame.contentWindow.document;
    var isDesktop = (document.getElementById("UIPageDesktop") != null) ? true : false;
    doc.open();
    doc.write("<div style='font-family:Tahoma,Verdana,Arial,Helvetica,sans-serif; font-size:12px;'><span>");
    doc.write(mailcontent);
    doc.write("</span></div>");
    doc.close();
    
    if (eXo.core.Browser.isFF()) {
        doc.body.style.visibility = true;
        frame.style.height = doc.body.offsetHeight + 20 + "px";
    }
    else {
        var docHt = 0, sh, oh;
        if (doc.height) {
            docHt = doc.height;
        }
        else 
            if (doc.body) {
                if (doc.body.scrollHeight) 
                    docHt = sh = doc.body.scrollHeight;
                if (doc.body.offsetHeight) 
                    docHt = oh = doc.body.offsetHeight;
                if (sh && oh) 
                    docHt = Math.max(sh, oh);
            }
        frame.style.width = "96%";
        frame.style.height = "auto";
        frame.style.height = docHt + 20 + "px";
    }
    
    expandMessage.style.display = styleExpand;
    previewArea.style.display = beforeDisplay;
};

UIMailPortlet.prototype.showMenu = function(obj, evt){
    if (!evt) 
        evt = window.event;
    evt.cancelBubble = true;
    var DOMUtil = eXo.core.DOMUtil;
    var uiPopupCategory = DOMUtil.findFirstDescendantByClass(obj, 'div', 'UIRightClickPopupMenu');
    if (!uiPopupCategory) 
        return;
    if (this.menuElement) {
        if (this.menuElement.style.display == "none") {
            eXo.core.DOMUtil.cleanUpHiddenElements();
            this.menuElement.style.display = "block";
            eXo.core.DOMUtil.listHideElements(this.menuElement);
        }
        else 
            this.menuElement.style.display = "none";
    }
    this.swapMenu(uiPopupCategory, obj);
};

UIMailPortlet.prototype.showView = function(obj, evt){
    if (!evt) 
        evt = window.event;
    evt.cancelBubble = true;
    var DOMUtil = eXo.core.DOMUtil;
    var uiPopupCategory = DOMUtil.findFirstDescendantByClass(obj, 'div', 'UIRightClickPopupMenu');
    if (!uiPopupCategory) 
        return;
    if (this.menuElement) {
        if (this.menuElement.style.display == "none") {
            eXo.core.DOMUtil.cleanUpHiddenElements();
            this.menuElement.style.display = "block";
            eXo.core.DOMUtil.listHideElements(this.menuElement);
        }
        else 
            this.menuElement.style.display = "none";
    }
    this.swapMenu(uiPopupCategory, obj);
};

UIMailPortlet.prototype.swapMenu = function(oldmenu, clickobj){
    var DOMUtil = eXo.core.DOMUtil;
    var Browser = eXo.core.Browser;
    var uiDesktop = document.getElementById("UIPageDesktop");
    var menuX = Browser.findPosX(clickobj) - eXo.cs.Utils.getScrollLeft(clickobj);
    var menuY = Browser.findPosY(clickobj) + clickobj.offsetHeight - eXo.cs.Utils.getScrollTop(clickobj);
    menuY += document.documentElement.scrollTop;
    if (document.getElementById("tmpMenuElement")) 
        DOMUtil.removeElement(document.getElementById("tmpMenuElement"));
    var tmpMenuElement = oldmenu.cloneNode(true);
    tmpMenuElement.setAttribute("id", "tmpMenuElement");
    DOMUtil.addClass(tmpMenuElement, "UIMailPortlet UIEmpty");
    this.menuElement = tmpMenuElement;
    document.body.appendChild(tmpMenuElement);
    this.setMenuWidth(this.menuElement);
    if (eXo.core.I18n.isRT()) {
        menuX -= (eXo.cs.Utils.getElementWidth(this.menuElement) - clickobj.offsetWidth);
    }
    this.menuElement.style.top = menuY + "px";
    this.menuElement.style.left = menuX + "px";
    this.showHide(this.menuElement);
};

UIMailPortlet.prototype.setMenuWidth = function(menu){
  menu.style.width= "1px";
	var items = eXo.core.DOMUtil.findDescendantsByClass(menu,"div","MenuItem");
	var max = 0;
	if(menu.style.display == "none") menu.style.display = "block";
	var i = items.length;
	while(i--){
		if(items[i].offsetWidth > max) max = items[i].offsetWidth;
	}
	if(max < 140) max = 140;
	menu.style.width = max + "px";
	menu.style.display = "none";
};

UIMailPortlet.prototype.showPopupMenu = function(obj, event){
    var popup = eXo.core.DOMUtil.findFirstDescendantByClass(obj, "div", "UIRightClickPopupMenu");
    eXo.cs.Utils.show(obj, event);
    if (eXo.core.Browser.isIE6()) {
        if (eXo.core.DOMUtil.findDescendantsByTagName(popup, "iframe").length > 0) 
            return;
        //var uiRightClickPopupMenu = eXo.core.DOMUtil.findFirstDescendantByClass(popup, "div", "UIRightClickPopupMenu")
        var ifr = document.createElement("iframe");
        ifr.frameBorder = 0;
        ifr.style.width = popup.offsetWidth + "px";
        ifr.style.height = popup.offsetHeight + "px";
        ifr.style.position = "absolute";
        ifr.style.left = "0px";
        ifr.style.zIndex = -1;
        popup.appendChild(ifr);
    }
};

UIMailPortlet.prototype.showHide = function(obj){
    if (obj.style.display != "block") {
        obj.style.display = "block";
        eXo.core.DOMUtil.listHideElements(obj);
    }
    else {
        obj.style.display = "none";
    }
};

//UIMailPortlet.prototype.autoResize = function() {
//	if(!eXo.core.Browser.isIE6()) return ; 
//	var obj = document.getElementById("UIComposeForm");
//	var uiPopupWindow = eXo.core.DOMUtil.findAncestorByClass(obj,"UIPopupWindow");
//	uiPopupWindow.onresize = function(){
//		var width = (this.offsetWidth < 850)? "800px":"99%" ;
//		obj.style.width = width ;		
//	}
//} ;

eXo.mail.CheckBox = {
//    init: function(cont){
//        if (typeof(cont) == "string") 
//            cont = document.getElementById(cont);
//        var checkboxes = eXo.core.DOMUtil.findDescendantsByClass(cont, "input", "checkbox");
//        if (checkboxes.length <= 0) 
//            return;
//        checkboxes[0].onclick = this.checkAll;
//        var len = checkboxes.length;
//        for (var i = 1; i < len; i++) {
//            checkboxes[i].onclick = this.check;
//        }
//    },
    init : function(tablelistId){
	  	var uiMailPortletNode = eXo.core.DOMUtil.findDescendantsByClass(document.body,"div","UIMailPortlet") ;
	  	var i = uiMailPortletNode.length ;
	  	this.tablelistId = tablelistId ;
	  	while(i--){  	
			this.register(uiMailPortletNode[i]) ;
	  	}
	
	},
	register: function(rootNode){
		var tablelist = eXo.core.DOMUtil.findDescendantById(rootNode,this.tablelistId) ;
		if(!tablelist) return ;
		var checkboxes = eXo.core.DOMUtil.findDescendantsByClass(tablelist, "input", "checkbox") ;
		if(checkboxes.length <=0) return ;
		checkboxes[0].onclick = this.checkAll ;
		var len = checkboxes.length ;
		for(var i = 1 ; i < len ; i ++) {
			checkboxes[i].onclick = this.check ;
		}
	
	},
    check: function(){
        eXo.cs.CheckBox.checkItem(this);
        var row = eXo.core.DOMUtil.findAncestorByTagName(this, "tr");
        if (this.checked) 
            eXo.core.DOMUtil.addClass(row, "SelectedItem");
        else 
            eXo.core.DOMUtil.replaceClass(row, "SelectedItem", "");
    },
    
    checkAll: function(){
        eXo.cs.CheckBox.checkAllItem(this);
        var table = eXo.core.DOMUtil.findAncestorByTagName(this, "table");
        var rows = eXo.core.DOMUtil.findDescendantsByClass(table, "tr", "MessageItem");
        var i = rows.length;
        if (this.checked) {
            while (i--) {
                eXo.core.DOMUtil.addClass(rows[i], "SelectedItem");
            }
        }
        else {
            while (i--) {
                eXo.core.DOMUtil.replaceClass(rows[i], "SelectedItem", "");
            }
        }
    },
    uncheckAll: function(cont){
        var rows = eXo.core.DOMUtil.findDescendantsByClass(cont, "tr", "MessageItem");
        var checkboxes = eXo.core.DOMUtil.findDescendantsByClass(cont, "input", "checkbox");
        var i = checkboxes.length;
        while (i--) {
            if (!checkboxes[i].checked) 
                continue;
            checkboxes[i].checked = false;
            eXo.core.DOMUtil.replaceClass(rows[i], "SelectedItem", "");
        }
    }
};

UIMailPortlet.prototype.initNavigationAction = function(navId){
    var nav = eXo.mail.UIMailPortlet.getElementById(navId);
    var titleBars = eXo.core.DOMUtil.findDescendantsByClass(nav, "div", "TitleBar");
    var i = titleBars.length;
    while (i--) {
        eXo.core.EventManager.addEvent(titleBars[i], "click", this.titleBarCallback);
    }
};

UIMailPortlet.prototype.titleBarCallback = function(evt){
    var DOMUtil = eXo.core.DOMUtil;
    var target = eXo.core.EventManager.getEventTarget(evt);
    if (DOMUtil.hasClass(target, "DownArrow3Icon")) 
        eXo.mail.UIMailPortlet.expandCollapse(target, this);
    if (DOMUtil.hasClass(target, "ActionIcon")) 
        eXo.webui.UIPopupSelectCategory.show(target, evt);
};

UIMailPortlet.prototype.expandCollapse = function(clickObj, clickBar){
    var obj = eXo.core.DOMUtil.findNextElementByTagName(clickBar, "div");
    if (obj.style.display != "none") {
        obj.style.display = "none";
        eXo.core.DOMUtil.addClass(clickObj, "FolderCollapseIcon");
    }
    else {
        obj.style.display = "block";
        eXo.core.DOMUtil.replaceClass(clickObj, "FolderCollapseIcon", "");
    }
};

UIMailPortlet.prototype.updateAuthMechsSupported = function(parentId, mechsSupported){//mech1,mech2,mech3,....
	if(parentId=='') return;
	var CLASS = "ChecForSupported";
	var divParent = document.getElementById(parentId);
	var notSupported = "line-through;";
	if(divParent == null) return;
	var cboCheckForSupported = 	eXo.core.DOMUtil.findFirstDescendantByClass(divParent, "span", CLASS);
	if(cboCheckForSupported == null) return;
	//disable all not supported mechanisms here
	var options = eXo.core.DOMUtil.getChildrenByTagName(cboCheckForSupported, "option");
	if(options == null || options.length <=0) return;
	for(var o in options){
	  if(mechsSupported.IndexOf(options[o].innerHTML) > -1)
		options[o].style.textDecoration = notSupported; 	
	}
};

UIMailPortlet.prototype.parentId = '';

UIMailPortlet.prototype.checkForSupportedType = function(element){
	if(element == null || element == undefined) return;
	var mechs = element.getAttribute('mechs');
	var username = element.getAttribute("username");
	var protocol = element.getAttribute("protocol");
	var host = element.getAttribute("host");
	
	if(mechs == null || mechs.length <= 0 || username == null || protocol == null || host == null) return;
	var FIELD_AUTHENTICATIONS_MECHANISM_ID = "authenticationMechanism";
	var FIELD_SECURE_AUTHENTICATION_OUTGOING_ID = "secureAuthenticationOutgoing";
	var PARENT_CLASS = "FieldInput"; 
	var parentElm = eXo.core.DOMUtil.findAncestorByClass(element, PARENT_CLASS);
	var cbos = eXo.core.DOMUtil.findFirstDescendantByClass(parentElm, "select", "selectbox");
	if(cbos == null || parentElm == null) return
	var url = (eXo.cs.restContext)?eXo.env.portal.context+ '/' + eXo.cs.restContext +'/private/cs/mail':'portal/rest/private/cs/mail';
	url = url + '/checkforsupportedtypes/' + mechs.replace('/','-') + '/' + username + '/' +  protocol + '/' + host;
	var request = new eXo.portal.AjaxRequest('GET', url, null);
	request.onSuccess = function(request){
		var uiMailPortlet = eXo.mail.UIMailPortlet;
		var mechsSupported = request.responseText; alert(mechsSupported); 
		uiMailPortlet.updateAuthMechsSupported(uiMailPortlet.parentId, mechsSupported)
	}
	
	request.onError = function(request){
		alert('Sorry, eXoMail cannot check for supported authentication mechanisms');
	}
	//alert("eXoMail has not supported this method.");
};

UIMailPortlet.prototype.validateFieldsIncoming = function(id){
	var fieldInputClass = "FieldInput";
	var fieldComponentAutoClass ="FieldComponentAuto";
	var checForSupportedClass = "CheckForSupported";
	var checForSupportedClassDisable = "CheckForSupportedDisable";
	var lableClass = "Label";
	
	var divIsIncomingSSL;
	var cboSecureAuths;
	var divAuthMechsIncoming;
	var cboAuthMechs;
	var buttCheckForSupported;
	var lblCheckForSupported;
	
	var FIELD_IS_INCOMING_SSL_ID = "isSSL";
	var FIELD_AUTHENTICATIONS_MECHANISM_ID = "authenticationMechanism";
	var FIELD_SERVER_TYPE_ID = "serverType";
	
	var incomingDiv = eXo.mail.UIMailPortlet.getElementById(id);
	var divs = eXo.core.DOMUtil.findDescendantsByClass(incomingDiv,"div",fieldInputClass);
	if(divs.length <= 0) return;
	for(var i in divs){
		if(divs[i].id == FIELD_IS_INCOMING_SSL_ID){
			divIsIncomingSSL = eXo.mail.UIMailPortlet.getElementById(FIELD_IS_INCOMING_SSL_ID);
		}else if(divs[i].id == FIELD_AUTHENTICATIONS_MECHANISM_ID){
			divAuthMechsIncoming = eXo.mail.UIMailPortlet.getElementById(FIELD_AUTHENTICATIONS_MECHANISM_ID);
		}	 
	}
	var chkIsIncomingSSL = eXo.core.DOMUtil.findFirstDescendantByClass(divIsIncomingSSL, "input", "checkbox");
	cboSecureAuths = eXo.core.DOMUtil.findFirstDescendantByClass(divIsIncomingSSL,"select","selectbox");
	
	cboAuthMechs = 	eXo.core.DOMUtil.findFirstDescendantByClass(divAuthMechsIncoming,"select","selectbox");
	lblCheckForSupported = eXo.core.DOMUtil.findFirstDescendantByClass(divAuthMechsIncoming,"span",lableClass);
	buttCheckForSupported = eXo.core.DOMUtil.findFirstDescendantByClass(divAuthMechsIncoming,"span",checForSupportedClass);
	if(buttCheckForSupported == null)
	  buttCheckForSupported = eXo.core.DOMUtil.findFirstDescendantByClass(divAuthMechsIncoming,"span",checForSupportedClassDisable);
		
	if(chkIsIncomingSSL != null ){
		var defaultport = divIsIncomingSSL.getAttribute("defaultport");
		var popports;
		var imapports;
		var txtPort;
		if(defaultport != null && defaultport.split(" ").length > 0){
			var popports = defaultport.split(" ")[0];
			var imapports   = defaultport.split(" ")[1];	
		}
		var FIELD_INCOMING_PORT_NAME = "incomingPort";
		var txtPort = eXo.mail.UIMailPortlet.getElementById(FIELD_INCOMING_PORT_NAME);
		var cboServerType = eXo.core.DOMUtil.findFirstDescendantByClass(incomingDiv, "select", "selectbox");
		//eXo.core.EventManager.addEvent(chkIsIncomingSSL, 'click', this.checkIncomingSSL);
		chkIsIncomingSSL.onclick = function(){
			if(this.checked){
				cboSecureAuths.removeAttribute('disabled');
				lblCheckForSupported.style.color = 'black';
				cboAuthMechs.removeAttribute('disabled');
				buttCheckForSupported.className =  checForSupportedClass;
				buttCheckForSupported.onclick= function(){
					eXo.mail.UIMailPortlet.checkForSupportedType(buttCheckForSupported);
				}	
				if(txtPort != null && cboServerType != null && cboServerType.value.toLowerCase() =="imap" && imapports.split(":").length > 0) txtPort.value = imapports.split(":")[0];
				else if(txtPort != null && cboServerType != null && cboServerType.value.toLowerCase() =="pop3" && popports.split(":").length > 0) txtPort.value = popports.split(":")[0];
			}else{
				cboSecureAuths.disabled = true;
				lblCheckForSupported.style.color = 'gray';
				cboAuthMechs.disabled = true;
				buttCheckForSupported.className =  checForSupportedClassDisable;
				buttCheckForSupported.onclick="";
				if(txtPort != null && cboServerType != null && cboServerType.value.toLowerCase() =="imap" && imapports.split(":").length > 0) txtPort.value = imapports.split(":")[1];
				else if(txtPort != null && cboServerType != null && cboServerType.value.toLowerCase() =="pop3" && popports.split(":").length > 0)txtPort.value = popports.split(":")[1];
			}		
		};
	}	
};

UIMailPortlet.prototype.validateFieldsOutgoing = function(id){
	var fieldInputClass = "FieldInput";
	var fieldComponentAutoClass ="FieldComponentAuto";
	var checForSupportedClass = "CheckForSupported";
	var lableClass = "Label";
	var checForSupportedClassDisable = "CheckForSupportedDisable";
	
	var FIELD_AUTHENTICATIONS_MECHANISM_OUTGOING_ID = "authenticationMechanismOutgoing";
	var FIELD_IS_OUTGOING_SSL_ID = "isOutgoingSsl";
	var IS_OUTGOING_AUTHENTICATION_ID = "isOutgoingAuthentication";
	var USE_INCOMINGSETTING_FOR_OUTGOING_AUTHEN_ID = "useIncomingSettingForOutgoingAuthent";
	var OUTGOING_USERNAME_ID = "outgoingUsername";
	var OUTGOING_PASSWORD_ID = "outgoingPassword";

	var tabElement = eXo.mail.UIMailPortlet.getElementById(id);
	var chkOutgoingAuthElementDiv;
	var chkUseIncomingSettingDiv;
	var txtusernameDiv;
	var txtpasswordDiv;
	var chkUseIncomingSetting;
	
	var divIsOutgoingSSL;
	var cboSecureAuths;
	var divAuthMechsOutgoing;
	var cboAuthMechs;
	var buttCheckForSupported;
	var lblCheckForSupported;
	var outgoingDiv = eXo.mail.UIMailPortlet.getElementById(id);		
	var divs = eXo.core.DOMUtil.findDescendantsByClass(outgoingDiv,"div",fieldInputClass);
	if(divs.length <= 0) return;
	for(var i in divs){
		if(divs[i].id == IS_OUTGOING_AUTHENTICATION_ID){
			 chkOutgoingAuthElementDiv = eXo.mail.UIMailPortlet.getElementById(IS_OUTGOING_AUTHENTICATION_ID);
		}else if(divs[i].id == USE_INCOMINGSETTING_FOR_OUTGOING_AUTHEN_ID){
			 chkUseIncomingSettingDiv = eXo.mail.UIMailPortlet.getElementById(USE_INCOMINGSETTING_FOR_OUTGOING_AUTHEN_ID);	
		}else if(divs[i].id == ('id-' +OUTGOING_USERNAME_ID)){
			 txtusernameDiv = eXo.mail.UIMailPortlet.getElementById('id-'+OUTGOING_USERNAME_ID);
		}else if(divs[i].id == ('id-' + OUTGOING_PASSWORD_ID)){
			 txtpasswordDiv = eXo.mail.UIMailPortlet.getElementById('id-'+OUTGOING_PASSWORD_ID);
		}else if(divs[i].id == FIELD_IS_OUTGOING_SSL_ID){
			divIsOutgoingSSL = eXo.mail.UIMailPortlet.getElementById(FIELD_IS_OUTGOING_SSL_ID);
		}else if(divs[i].id == FIELD_AUTHENTICATIONS_MECHANISM_OUTGOING_ID){
			divAuthMechsOutgoing = eXo.mail.UIMailPortlet.getElementById(FIELD_AUTHENTICATIONS_MECHANISM_OUTGOING_ID);
		}	 
	} 
	
	var chkOutgoingAuthElement = eXo.core.DOMUtil.findFirstDescendantByClass(chkOutgoingAuthElementDiv,"input","checkbox");
	chkUseIncomingSetting = eXo.core.DOMUtil.findFirstDescendantByClass(chkUseIncomingSettingDiv,"input","checkbox");
	
	var lblusername = eXo.core.DOMUtil.findFirstDescendantByClass(txtusernameDiv, "span", "InputFieldLabel");
	var lblpassword = eXo.core.DOMUtil.findFirstDescendantByClass(txtpasswordDiv, "span", "InputFieldLabel");
	var txtusername = eXo.mail.UIMailPortlet.getElementById(OUTGOING_USERNAME_ID);
	var txtpassword = eXo.mail.UIMailPortlet.getElementById(OUTGOING_PASSWORD_ID);
	
	var chkIsOutgoingSSL = eXo.core.DOMUtil.findFirstDescendantByClass(divIsOutgoingSSL, "input", "checkbox");
	cboSecureAuths = eXo.core.DOMUtil.findFirstDescendantByClass(divIsOutgoingSSL,"select","selectbox");
	cboAuthMechs = 	eXo.core.DOMUtil.findFirstDescendantByClass(divAuthMechsOutgoing,"select","selectbox");
	lblCheckForSupported = eXo.core.DOMUtil.findFirstDescendantByClass(divAuthMechsOutgoing,"span",lableClass);
	buttCheckForSupported = eXo.core.DOMUtil.findFirstDescendantByClass(divAuthMechsOutgoing,"span",checForSupportedClass);
	if(buttCheckForSupported == null)
		buttCheckForSupported = eXo.core.DOMUtil.findFirstDescendantByClass(divAuthMechsOutgoing,"span",checForSupportedClassDisable);
	if(chkIsOutgoingSSL != null && chkIsOutgoingSSL != undefined){
		var defaultport = divIsOutgoingSSL.getAttribute("defaultport");
		var FIELD_OUTGOING_PORT_NAME = "outgoingPort";
		var txtPort = eXo.mail.UIMailPortlet.getElementById(FIELD_OUTGOING_PORT_NAME);
		chkIsOutgoingSSL.onclick = function(){
			if(this.checked){
				cboSecureAuths.removeAttribute('disabled');
				lblCheckForSupported.style.color = 'black';
				cboAuthMechs.removeAttribute('disabled');
				buttCheckForSupported.className =  checForSupportedClass;
				buttCheckForSupported.onclick= function(){
					eXo.mail.UIMailPortlet.checkForSupportedType(buttCheckForSupported);
				}	
				if(txtPort != null && defaultport != null && defaultport.split(':').length > 0) txtPort.value = defaultport.split(":")[0];  								
			}else{
				cboSecureAuths.disabled = true;
				lblCheckForSupported.style.color = 'gray';
				cboAuthMechs.disabled = true;
				buttCheckForSupported.className =  checForSupportedClassDisable;
				buttCheckForSupported.onclick="";
				if(txtPort != null && defaultport != null && defaultport.split(':').length > 0) txtPort.value = defaultport.split(":")[1];
			}		
		};
	}
		
	if(chkOutgoingAuthElement !=null && chkOutgoingAuthElement != undefined){
		chkOutgoingAuthElement.onclick = function(){
			var lblUseIncomingSetting = eXo.core.DOMUtil.findFirstDescendantByClass(chkUseIncomingSettingDiv,"span",lableClass);
			if(this.checked){
				if(lblUseIncomingSetting != null && lblUseIncomingSetting != undefined) 
					lblUseIncomingSetting.style.color = "black";
				if(chkUseIncomingSetting != null && chkUseIncomingSetting != undefined){
					chkUseIncomingSetting.removeAttribute('disabled');
					if(chkUseIncomingSetting.checked){
						txtusername.disabled = true;
						txtpassword.disabled = true;
						lblusername.style.color = "gray";
						lblpassword.style.color = "gray";
					}else{
						txtusername.removeAttribute('disabled');
						txtpassword.removeAttribute('disabled');
						lblusername.style.color = "black";
						lblpassword.style.color = "black";
					}	
				}
			}else{
				if(lblUseIncomingSetting != null && lblUseIncomingSetting != undefined) 
					lblUseIncomingSetting.style.color = "gray";
				if(chkUseIncomingSetting != null && chkUseIncomingSetting != undefined){
					chkUseIncomingSetting.disabled = true;
					chkUseIncomingSetting.checked = false;
				}	
				txtusername.disabled = true;
				txtpassword.disabled = true;
				lblusername.style.color = "gray";
				lblpassword.style.color = "gray";
			}
		};
	}
	if(chkUseIncomingSetting != null && chkUseIncomingSetting != undefined){
		chkUseIncomingSetting.onclick = function(){
			if(this.checked){
				txtusername.disabled = true;
				txtpassword.disabled = true;
				lblusername.style.color = "gray";
				lblpassword.style.color = "gray";	
			}else{
				txtusername.removeAttribute('disabled');
				txtpassword.removeAttribute('disabled');
				lblusername.style.color = "black";
				lblpassword.style.color = "black";
			}
		};	
	}
};

UIMailPortlet.prototype.lazySync = function(obj, fId){
	var actionLink = obj.getAttribute("actionlink") ;
	eval(actionLink);
	
	return;
	//remove automatic checking mail.
	if(this.isChecked) {
		window.clearTimeout(this.isChecked);
		//eXo.mail.MailServiceHandler.stopCheckMail();
	}
	this.isChecked = window.setTimeout(function(){		 
		eXo.mail.MailServiceHandler.checkMail(false, fId);
		window.clearTimeout(this.isChecked);
		delete this.isChecked;
	}, 5*1000);
}

UIMailPortlet.prototype.fixFCKforSafari = function(){
	if(eXo.core.Browser.browserType != "safari") return ;
	try{
		var editorFrame = FCKeditorAPI.Instances.messageContent.EditingArea.TargetElement.firstChild	
		editorFrame.style.height = editorFrame.parentNode.offsetHeight + "px";
		clearTimeout(this.fixSafariTimeout);
		return ;
	
	}catch(e){
		this.fixSafariTimeout = setTimeout("eXo.mail.UIMailPortlet.fixFCKforSafari();",100);
	}	
};

UIMailPortlet.prototype.showHideAttach = function(menu){
	if(!menu) return ;
	if(menu.style.display == "none") menu.style.display = "block";
	else menu.style.display = "none";
};

UIMailPortlet.prototype.getElementById = function(id){
	return eXo.core.DOMUtil.findDescendantById(this.portletNode,id);
};

UIMailPortlet.prototype.addMoreDelegatedAccounts = function(obj){
	var tabId = obj.getAttribute("tabId");
	var DOMUtil = eXo.core.DOMUtil;
	var tabNode = document.getElementById(tabId);
	var tableClass = "DelegatedAccountGrid";
	var FIELD_OWNER_ACCOUNTS_ID = "owner-accounts";
	var FIELD_DELEGATED_ACCOUNTS_ID = "delegated-accounts";
	var FIELD_PRIVILEGE_FULL_ID = "full-privilege";
	var FIELD_PRIVILEGE_READONLY_ID = "readonly-pivilege";
	
	var tableNode = DOMUtil.findFirstDescendantByClass(tabNode, "table", tableClass);
	var ownerUserTd = DOMUtil.findDescendantById(tabNode,FIELD_OWNER_ACCOUNTS_ID);
	var fullSpan = DOMUtil.findDescendantById(tabNode, FIELD_PRIVILEGE_FULL_ID);
	var readonlySpan = DOMUtil.findDescendantById(tabNode, FIELD_PRIVILEGE_READONLY_ID);
	
	var ownerUser = DOMUtil.findFirstDescendantByClass(ownerUserTd, "select", "selectbox");
	var delegatedUser = DOMUtil.findDescendantById(tabNode,FIELD_DELEGATED_ACCOUNTS_ID);
	var full = DOMUtil.findFirstDescendantByClass(fullSpan, "input", "checkbox");
	var readonly = DOMUtil.findFirstDescendantByClass(readonlySpan, "input", "checkbox");
		
	var countRows = tableNode.rows.length;
	var tbody = DOMUtil.findDescendantsByTagName(tableNode, 'tbody')[0]; 
	if(tbody == undefined || tbody == null){
		tbody = document.createElement('tbody');
		tableNode.appendChild(tbody); 	 
	}
	var newRow = tableNode.insertRow(countRows);//row(account(email), delegate to, read only, full, acction)	
	newRow.insertCell(0).innerHTML = ownerUser.value;
	newRow.insertCell(1).innerHTML = delegatedUser.value;
	
	var cellFullPri = newRow.insertCell(2);
	var checkboxFull = document.createElement("input");
	checkboxFull.className="checkbox";
	checkboxFull.type="checkbox";
	checkboxFull.name="privilege-full";
	checkboxFull.checked = full.checked;
	cellFullPri.appendChild(checkboxFull);
		
	var readonlyPri = newRow.insertCell(3)
	var checkboxReadonly = document.createElement("input");
	checkboxReadonly.type="checkbox";
	checkboxReadonly.className="checkbox";
	checkboxReadonly.name="privilege-readonly";
	checkboxReadonly.checked = readonly.checked;
	readonlyPri.appendChild(checkboxReadonly);
	
	var cellAction = newRow.insertCell(4);
	var action = document.createElement("a");
	action.href="javascript:void(0);";
	action.innerHTML = "Remove";
	action.className="ActionsDelegateAccount";
	cellAction.appendChild(action);

	action.onclick = function(){
		eXo.mail.UIMailPortlet.removeAccountDelegation(action)
	}
	if(countRows <= 0){	
		tableNode.deleteRow(newRow.indexRow);
		tbody.appendChild(newRow);
	}
	
};

UIMailPortlet.prototype.removeAccountDelegation = function(obj){
	var currentRow = obj.parentNode.parentNode;
	var tableNode = currentRow.parentNode.parentNode;
	var currentIndex = currentRow.rowIndex;
	//call mail webservice here
	//if(scuccess) clean UI
	tableNode.deleteRow(currentRow);
};

UIMailPortlet.prototype.validPrivilege = function(tabId){
	var DOMUtil = eXo.core.DOMUtil;
	var tabNode = document.getElementById(tabId);
	var FIELD_PRIVILEGE_FULL_ID = "full-privilege";
	var FIELD_PRIVILEGE_READONLY_ID = "readonly-pivilege";
	
	var fullSpan = DOMUtil.findDescendantById(tabNode, FIELD_PRIVILEGE_FULL_ID);
	var readonlySpan = DOMUtil.findDescendantById(tabNode, FIELD_PRIVILEGE_READONLY_ID);
	var full = DOMUtil.findFirstDescendantByClass(fullSpan, "input", "checkbox");
	var readonly = DOMUtil.findFirstDescendantByClass(readonlySpan, "input", "checkbox");
	full.onclick = function(){
		if(this.checked) readonly.disabled = true;
		else readonly.removeAttribute("disabled");
	};
};

eXo.mail.UIMailPortlet = new UIMailPortlet();
// Override submit method of UIForm to add a comfirm message
UIForm.prototype.tmpMethod = eXo.webui.UIForm.submitForm;
UIForm.prototype.submitForm = function(formId, action, useAjax, callback){
    var form = this.getFormElemt(formId);
    if ((formId.indexOf("mail#UIComposeForm") >= 0) && (action.indexOf("Send") >= 0)) {
        var to = form["to"].value;
        var subject = form["subject"].value;
        var confirmMessage = eXo.core.DOMUtil.findFirstDescendantByClass(form, "div", "UIConfirmMessage").innerHTML;
        if (this.isEmpty(subject) && this.isEmail(to) && !confirm(confirmMessage)) 
            return;
    }
    this.tmpMethod(formId, action, useAjax, callback)
};
UIForm.prototype.isEmail = function(email){
    if (this.isEmpty(email)) 
        return false;
    var pattern = /^\w+([\.-]?\w+)*@(([\-\w]+)\.?)+\.[a-zA-Z]{2,4}$/;
    return pattern.test(email);
}

UIForm.prototype.isEmpty = function(str){
    str = str.toString().trim();
    if (str == '' || str == null) 
        return true;
    return false;
}

//Scroll manager
function MailScrollManager(){
};

MailScrollManager.prototype.load = function(id){ 
	var uiNav = eXo.mail.MailScrollManager ;
  var container = eXo.mail.UIMailPortlet.getElementById(id) ;
  if(container) {
    var mainContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "CenterBar") ;
	  var randomId = eXo.core.DOMUtil.generateId("MailScrollbar");
  	mainContainer.setAttribute("id",randomId);
    uiNav.scrollMgr = eXo.portal.UIPortalControl.newScrollManager(randomId) ;
    uiNav.scrollMgr.initFunction = uiNav.initScroll ;
    uiNav.scrollMgr.mainContainer = mainContainer ;
    uiNav.scrollMgr.arrowsContainer = eXo.core.DOMUtil.findFirstDescendantByClass(container, "div", "ScrollButtons") ;
    uiNav.scrollMgr.loadButtons("MailListActionsbarButton", true) ;
    var button = eXo.core.DOMUtil.findDescendantsByTagName(uiNav.scrollMgr.arrowsContainer, "div");
    if(button.length >= 2) {    
      uiNav.scrollMgr.initArrowButton(button[0],"left", "ScrollLeftButton", "HighlightScrollLeftButton", "DisableScrollLeftButton") ;
      uiNav.scrollMgr.initArrowButton(button[1],"right", "ScrollRightButton", "HighlightScrollRightButton", "DisableScrollRightButton") ;
    }
		
    uiNav.scrollManagerLoaded = true;
    uiNav.initScroll() ;
  }
} ;

MailScrollManager.prototype.initScroll = function() {
  var uiNav = eXo.mail.MailScrollManager ;
  if(!uiNav.scrollManagerLoaded) uiNav.load() ;
  var elements = uiNav.scrollMgr.elements ;
  uiNav.scrollMgr.init() ;
  uiNav.scrollMgr.csCheckAvailableSpace() ;
  uiNav.scrollMgr.renderElements() ;
} ;

MailScrollManager.prototype.getItemsByClass = function(root,cssClass){
  var elements = root.childNodes ;
  var ln = elements.length ;
		var list = [] ;
  for (var k = 0; k < ln; k++) {
    if (eXo.core.DOMUtil.hasClass(elements[k], cssClass)) {
    	list.push(elements[k]) ;
    }
  }
  return list ;

}

ScrollManager.prototype.loadButtons = function(elementClass, clean) {
	if (clean) this.cleanElements();
	this.elements.clear();
	var container = eXo.core.DOMUtil.findFirstDescendantByClass(this.mainContainer,"div","MailListActionsbar");
	var items = eXo.mail.MailScrollManager.getItemsByClass(container, elementClass);
	for(var i = 0; i < items.length; i++){
		this.elements.push(items[i]);
	}
};

ScrollManager.prototype.csCheckAvailableSpace = function(maxSpace) { // in pixels
	if (!maxSpace) maxSpace = this.getElementSpace(this.mainContainer) - this.getElementSpace(this.arrowsContainer);
	var elementsSpace = 0;
	var margin = 0;
	var length =  this.elements.length;
	for (var i = 0; i < length; i++) {
		elementsSpace += this.getElementSpace(this.elements[i]);
		//dynamic margin;
		if (i+1 < length) margin = this.getElementSpace(this.elements[i+1]) / 3;
		else margin = this.margin;
		if (elementsSpace + margin < maxSpace) { // If the tab fits in the available space
			this.elements[i].isVisible = true;
			this.lastVisibleIndex = i;
		} else { // If the available space is full
			this.elements[i].isVisible = false;
		}
	}
};


eXo.mail.MailScrollManager = new MailScrollManager();
