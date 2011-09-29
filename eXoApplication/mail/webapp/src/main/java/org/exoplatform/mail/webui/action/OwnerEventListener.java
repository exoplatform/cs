/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.exoplatform.mail.webui.action;

import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Sep 28, 2011  
 */
public class OwnerEventListener<T extends UIComponent> extends HasAccountEventListener<T> {

  @Override
  public boolean validate(Event<T> event) throws Exception {
    if (super.validate(event)) {
      UIMailPortlet uiPortlet = event.getSource().getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      String accountId = dataCache.getSelectedAccountId();
      if (MailUtils.isDelegated(accountId, dataCache)) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UISelectAccount.msg.account-list-no-permission", null));
        return false;
      }
      return true;
    }
    return false;
  }

}
