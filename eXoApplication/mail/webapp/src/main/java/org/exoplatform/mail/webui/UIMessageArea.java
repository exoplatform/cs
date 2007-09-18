/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;

import java.util.List;

import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/mail/webui/UIMessageArea.gtmpl"
)
public class UIMessageArea extends UIContainer  {
  
  public UIMessageArea() throws Exception {
    UIMessageList uiMessageList = addChild(UIMessageList.class, null, null);
    addChild(UIMessagePreview.class, null, null);
    MailService mailSrv = getApplicationComponent(MailService.class);
    String username = Util.getPortalRequestContext().getRemoteUser();
    String accountId = mailSrv.getAccounts(username).get(0).getId();
    String selectedFolderId = uiMessageList.getSelectedFolderId();
    if (accountId != null && accountId != "" && selectedFolderId != null && selectedFolderId != "") {
      List<Message> messageList = mailSrv.getMessageByFolder(username, accountId, selectedFolderId);
      uiMessageList.setMessageList(messageList);
    }
  }
}
