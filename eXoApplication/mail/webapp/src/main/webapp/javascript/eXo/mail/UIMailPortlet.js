function UIMailPortlet(){
};

UIMailPortlet.prototype.showContextMenu = function(compid){
    var UIContextMenuMail = eXo.webui.UIContextMenuMail; //eXo.contact.ContextMenu ;
    UIContextMenuMail.portletName = compid;
    var config = {
        'preventDefault': false,
        'preventForms': false
    };
    UIContextMenuMail.init(config);
    UIContextMenuMail.attach('MessageItem', 'UIMessagePopupMenu');
    UIContextMenuMail.attach('CustomizeFolder', 'UIFolderListPopupMenu');
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
    var src = eXo.core.EventManager.getEventTargetByTagName(evt, "a");
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
    var menuItems = eXo.core.DOMUtil.findDescendantsByClass(csMailLayoutSwitchMenuNode, 'a', 'MenuItem');
    var menuItemTexts = eXo.core.DOMUtil.findDescendantsByClass(csMailLayoutSwitchMenuNode, 'div', 'ItemIcon');
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
};

UIMailPortlet.prototype.showHideAddMoreAddress = function(add){
    var elm = document.getElementById(add);
    if (elm.style.display == "none") {
        elm.style.display = "";
    }
};

UIMailPortlet.prototype.showHidePreviewPane = function(obj){
    var DOMUtil = eXo.core.DOMUtil;
    this.switchLayout(2);
    var actionButton = DOMUtil.findDescendantsByTagName(obj, "span")[0];
    var spliterContainer = DOMUtil.findAncestorByClass(obj, "SpliterContainer");
    var uiMessageArea = DOMUtil.findFirstDescendantByClass(spliterContainer, "div", "UIMessageArea");
    if (uiMessageArea.style.display == "none") 
        actionButton.className = "MinimumReadingPane";
    else 
        actionButton.className = "MaximizeReadingPane";
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
        form = document.getElementById(form);
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
        chk = document.getElementById(chk);
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
        this.uiFolderContainerNode = document.getElementById('UIFolderContainer');
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
    this.uiFolderContainerNode = document.getElementById('UIFolderContainer');
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
    var obj = document.getElementById("uiMessageGrid");
    if (!obj) 
        return;
    var scroll = parseInt(eXo.core.Browser.getCookie("scrollstatus"));
    obj.scrollTop = scroll;
    obj.onclick = eXo.mail.UIMailPortlet.saveScroll;
};

UIMailPortlet.prototype.saveScroll = function(){
    eXo.core.Browser.setCookie("scrollstatus", this.scrollTop, 1);
};

UIMailPortlet.prototype.resizeIframe = function(textAreaId, frameId, styleExpand, contentType){
    var frame = document.getElementById(frameId);
    var textAreas = document.getElementById(textAreaId);
    var expandMessage = eXo.core.DOMUtil.findAncestorByClass(frame, "ExpandMessage");
    var previewArea = document.getElementById("SpliterResizableArea");
    var beforeDisplay = previewArea.style.display;
    if (beforeDisplay == "none") {
        previewArea.style.display = "block";
    }
    var str = textAreas.value;
    if (contentType.indexOf("text/plain") > -1) 
        str = str.replace(/\n/g, "<br>");
    var doc = frame.contentWindow.document;
    var isDesktop = (document.getElementById("UIPageDesktop") != null) ? true : false;
    doc.open();
    doc.write(str);
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
    if ((Browser.browserType == "ie") && !document.getElementById("UIPageDesktop")) {
        if (document.getElementById("UIControlWorkspace") && !eXo.core.I18n.isRT()) 
            menuX -= document.getElementById("UIControlWorkspace").offsetWidth;
    }
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
	var items = eXo.core.DOMUtil.findDescendantsByClass(menu,"a","MenuItem");
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
    init: function(cont){
        if (typeof(cont) == "string") 
            cont = document.getElementById(cont);
        var checkboxes = eXo.core.DOMUtil.findDescendantsByClass(cont, "input", "checkbox");
        if (checkboxes.length <= 0) 
            return;
        checkboxes[0].onclick = this.checkAll;
        var len = checkboxes.length;
        for (var i = 1; i < len; i++) {
            checkboxes[i].onclick = this.check;
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
    var nav = document.getElementById(navId);
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

UIMailPortlet.prototype.isSMTPAuthentication = function(id){
	var chk = document.getElementById(id);
	var checkboxs = eXo.core.DOMUtil.findDescendantsByClass(chk,"input","checkbox");
	chk = checkboxs[1];
	var table = eXo.core.DOMUtil.findAncestorByTagName(chk,"table");
	var div = eXo.core.DOMUtil.findDescendantsByTagName(table,"div")[0];
	var input = eXo.core.DOMUtil.findDescendantsByTagName(div,"input");
	chk.onclick = function(){
		if(!this.checked){
			div.style.display = "none";
		}else div.style.display = "";
	}
	chk = checkboxs[2];
	chk.onclick = function(){
		if (!this.checked) {
			input[1].disabled = false;
			input[2].disabled = false;
		}else{
			input[1].disabled = true;
			input[2].disabled = true;
		}
	}
}
UIMailPortlet.prototype.lazySync = function(obj, fId){
	var actionLink = obj.getAttribute("actionlink") ;
	eval(actionLink);
	if(this.isChecked) {
		window.clearTimeout(this.isChecked);
		//eXo.mail.MailServiceHandler.stopCheckMail();
	}
	this.isChecked = window.setTimeout(function(){		 
		eXo.mail.MailServiceHandler.checkMail(true, fId);
		window.clearTimeout(this.isChecked);
		delete this.isChecked;
	}, 5*1000);
}

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
