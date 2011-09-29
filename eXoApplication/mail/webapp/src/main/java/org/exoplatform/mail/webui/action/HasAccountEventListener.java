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

import org.apache.commons.lang.StringUtils;
import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SAS
 * Author : Lai Trung Hieu
 *          hieult@exoplatform.com
 * Sep 28, 2011  
 */
public class HasAccountEventListener<T extends UIComponent> extends EventListener<T> {

  @Override
  public void execute(Event<T> event) throws Exception {
    if (validate(event)) {
      processEvent(event);
    }
  }
  
  public boolean validate(Event<T> event) throws Exception {
    UIMailPortlet uiPortlet = event.getSource().getAncestorOfType(UIMailPortlet.class);
    DataCache dataCache = uiPortlet.getDataCache();
    String username = MailUtils.getCurrentUser();
    if (StringUtils.isEmpty(dataCache.getSelectedAccountId())
        || (dataCache.getAccounts(username).isEmpty() && dataCache.getDelegatedAccounts(username).isEmpty())) {
      event.getRequestContext()
           .getUIApplication()
           .addMessage(new ApplicationMessage("UIMessageList.msg.account-list-empty", null));
      return false;
    }
    return true;
  }

  public void processEvent(Event<T> event) throws Exception {
  }

}
