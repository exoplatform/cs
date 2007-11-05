/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;


/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    template = "system:/groovy/webui/core/UITabPane.gtmpl"
)
public class UIAdvancedSearch extends UIContainer implements UIPopupComponent {
  
  public UIAdvancedSearch() throws Exception {
    addChild(UIAdvancedSearchForm.class, null, null).setRendered(true);
    String username = MailUtils.getCurrentUser();
    String accountId = MailUtils.getAccountId();
    MailService mailSrv = getApplicationComponent(MailService.class);
    UIMessageList uiMessageList = createUIComponent(UIMessageList.class, null, null);
    uiMessageList.setMessagePageList(mailSrv.getMessagePageListByFolder(username, accountId, Utils.createFolderId(accountId, Utils.FD_INBOX, false)));
    uiMessageList.setRendered(false);
    addChild(uiMessageList);
  }
  
  public void showResult() throws Exception {
    getChild(UIAdvancedSearchForm.class).setRendered(false);
    getChild(UIMessageList.class).setRendered(true);
  }
  
  public void setSelectedFolder(String folderId)  throws Exception {
    getChild(UIAdvancedSearchForm.class).setSelectedFolder(folderId);
  }

  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
}
