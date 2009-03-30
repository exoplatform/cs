/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.xmpp.servlet;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.security.ConversationRegistry;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class LogoutFilter implements Filter {
  /**
   * Class logger.
   */
  private final Log log = ExoLogger.getLogger("liveroom.chat.LogoutFilter");

  /**
   * {@inheritDoc}
   */
  public void destroy() {
  }

  /**
   * {@inheritDoc}
   */
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                                                                                           ServletException {
    chain.doFilter(request, response);
    HttpServletRequest httpRequest = (HttpServletRequest) request;
    javax.servlet.http.HttpSession session = httpRequest.getSession(false);
    if (session != null) {
      String sessionId = session.getId();
      log.info("Remove session : " + sessionId);
      session.invalidate();
      ConversationRegistry conversationRegistry = (ConversationRegistry) ExoContainerContext.getCurrentContainer()
                                                                                            .getComponentInstanceOfType(ConversationRegistry.class);
      if (conversationRegistry.getState(sessionId) != null) {
        log.info("Remove conversation state : " + sessionId);
        conversationRegistry.unregister(sessionId);
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  public void init(FilterConfig arg0) throws ServletException {
  }
}
