/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.mail.service;

import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Hai Nam 
 *          phunghainam@gmail.com
 * Feb 25, 2009  
 */
public class AuthenticationLogoutListener extends Listener<ConversationRegistry, ConversationState> {
  protected static Log log = ExoLogger.getLogger(AuthenticationLogoutListener.class);
  
  public AuthenticationLogoutListener() throws Exception {
  }

  @Override
  public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {
    try {
      MailService mService = (MailService) PortalContainer.getInstance().getComponentInstanceOfType(MailService.class);
      String username = event.getData().getIdentity().getUserId();
      List<Account> accList = mService.getAccounts(username);
      for (Account acc : accList) {
        mService.stopAllJobs(username, acc.getId());
      }
      mService.closeAllMailConnectionByUser(username);
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception when stop stop all job when user logout", e);
      }
    }
  }
}
