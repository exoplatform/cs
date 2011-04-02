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
package org.exoplatform.services.xmpp.connection.impl;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.listener.Event;
import org.exoplatform.services.listener.Listener;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.presence.DefaultPresenceStatus;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.xmpp.history.impl.jcr.HistoryImpl;
import org.exoplatform.services.xmpp.rest.RESTXMPPService;
import org.exoplatform.services.xmpp.userinfo.UserInfoService;
import org.exoplatform.ws.frameworks.cometd.transport.ContinuationServiceDelegate;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          vietnt84@gmail.com
 * Oct 6, 2009  
 */
public class AuthenticationLoginListener extends Listener<ConversationRegistry, ConversationState> {

  protected static final Log log = ExoLogger.getLogger("chat.AuthenticationLoginListener");

  @Override
  public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {
    try {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      XMPPMessenger messenger = (XMPPMessenger) container.getComponentInstanceOfType(XMPPMessenger.class);
      RESTXMPPService restXmppService = (RESTXMPPService) container.getComponentInstanceOfType(RESTXMPPService.class);

      DefaultPresenceStatus dps = (DefaultPresenceStatus) container.getComponentInstance(DefaultPresenceStatus.class);
      if (messenger != null && restXmppService != null) {
        String userId = event.getData().getIdentity().getUserId();
        UserInfoService organization = (UserInfoService) container.getComponentInstanceOfType(UserInfoService.class);
        /*
         * String password = organization.getOrganizationService() .getUserHandler() .findUserByName(userId) .getPassword();
         */
        ConversationState.setCurrent(event.getData());
        String password = organization.providePassword(userId);
        ContinuationServiceDelegate delegate = (ContinuationServiceDelegate) container.getComponentInstanceOfType(ContinuationServiceDelegate.class);
        HistoryImpl history = (HistoryImpl) container.getComponentInstanceOfType(HistoryImpl.class);
        /*
         * if(status != null){ Map<String, String> stmap = status.getPreviousStatus(); }
         */

        messenger.login(userId, password, organization, delegate, history, restXmppService.loadResourceBundle());
      }
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug(e.getMessage());
    }
  }

}
