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
import org.exoplatform.services.xmpp.connection.XMPPSession;
import org.exoplatform.services.xmpp.util.CometdChannels;
import org.exoplatform.ws.frameworks.cometd.transport.ContinuationServiceDelegate;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          vietnt84@gmail.com
 * Oct 6, 2009  
 */
public class AuthenticationLogoutListener extends Listener<ConversationRegistry, ConversationState> {

  protected static final Log log = ExoLogger.getLogger("chat.AuthenticationLogoutListener");

  @Override
  public void onEvent(Event<ConversationRegistry, ConversationState> event) throws Exception {
    XMPPMessenger messenger = null;
    String userId = null;
    try {
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      messenger = (XMPPMessenger) container.getComponentInstanceOfType(XMPPMessenger.class);
      // Saving presence status
      DefaultPresenceStatus dps = (DefaultPresenceStatus) container.getComponentInstance(DefaultPresenceStatus.class);
      if (messenger != null) {
        userId = event.getData().getIdentity().getUserId();
        XMPPSession session = messenger.getSession(userId);
        if (session != null) {
          if (dps != null && userId != null && !userId.equals("")) {
            dps.savePresenceStatus(userId, session.getPresenceStatus_());
          } else {
            log.error("Can not save user chat status");
          }
          session.removeAllTransport();
        }
        ContinuationServiceDelegate delegate = (ContinuationServiceDelegate) container.getComponentInstanceOfType(ContinuationServiceDelegate.class);
        if (delegate != null) {
          delegate.sendMessage(userId, CometdChannels.NOTIFICATION, "session-expired", null);
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
    } finally {
      if ((userId != null) && (messenger != null)) {
        messenger.logout(userId);
      }
    }
  }

}
