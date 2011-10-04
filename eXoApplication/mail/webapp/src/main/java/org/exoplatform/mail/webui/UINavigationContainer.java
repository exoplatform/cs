/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.mail.webui;

import java.util.List;

import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig (
  template = "app:/templates/mail/webui/UINavigationContainer.gtmpl"
)
public class UINavigationContainer extends UIContainer  {
  public UINavigationContainer() throws Exception {
    addChild(UISearchForm.class, null, null) ;
    UISelectAccount uiSelectAccount = createUIComponent(UISelectAccount.class, null, null);
    addChild(uiSelectAccount) ;
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    String defaultAcc = mailSvr.getMailSetting(username).getDefaultAccount();
    List<Account> accounts = mailSvr.getAccounts(username);
    if (defaultAcc == null && accounts.size() > 0) defaultAcc = accounts.get(0).getId();
    uiSelectAccount.setSelectedValue(defaultAcc);
    UIFolderContainer uiFolderContainer = createUIComponent(UIFolderContainer.class, null, null);
    String accountId = uiSelectAccount.getSelectedValue();
    uiFolderContainer.init(accountId);
    addChild(uiFolderContainer) ;
    addChild(UITagContainer.class, null, null) ;
  }
}
