/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.services.xmpp.rest.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Path;

import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.rest.Filter;
import org.exoplatform.services.rest.GenericContainerRequest;
import org.exoplatform.services.rest.RequestFilter;
import org.exoplatform.services.rest.impl.EnvironmentContext;
import org.exoplatform.services.security.ConversationRegistry;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.StateKey;
import org.exoplatform.services.security.web.HttpSessionStateKey;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          vietnt84@gmail.com
 * Feb 3, 2010  
 */

@Path("/xmpp/{x:.*}")
@Filter
public class RESTXMPPServiceFilter implements RequestFilter {

  private final Log log = ExoLogger.getLogger(RESTXMPPServiceFilter.class.getName());

  public void doFilter(GenericContainerRequest request) {
    try {
      EnvironmentContext env = EnvironmentContext.getCurrent();
      HttpServletRequest httpRequest = (HttpServletRequest) env.get(HttpServletRequest.class);
      httpRequest.setCharacterEncoding("UTF-8");
      ExoContainer container = ExoContainerContext.getCurrentContainer();
      ConversationRegistry conversationRegistry = (ConversationRegistry) container.getComponentInstanceOfType(ConversationRegistry.class);
      ConversationState state = null;
      String userId = httpRequest.getRemoteUser();
      if (userId != null) {
        HttpSession httpSession = httpRequest.getSession();
        StateKey stateKey = new HttpSessionStateKey(httpSession);
        state = conversationRegistry.getState(stateKey);
        if (state != null) {
          if (!userId.equals(state.getIdentity().getUserId())) {
            conversationRegistry.unregister(stateKey);
          }
        }
      }
    } catch (Exception e) {
      if (log.isDebugEnabled())
        log.debug("Message: " + e.getMessage() + ". Cause: " + e.getCause());
    }
  }
}
