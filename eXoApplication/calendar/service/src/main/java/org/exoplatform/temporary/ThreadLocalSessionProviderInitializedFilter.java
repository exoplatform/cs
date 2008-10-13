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
package org.exoplatform.temporary;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.ext.app.ThreadLocalSessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.log.ExoLogger;
//import org.exoplatform.services.organization.auth.AuthenticationService;
import org.exoplatform.services.security.Identity;

/**
 * Created by The eXo Platform SAS . <br/>
 * Checks out if there are SessionProvider istance in current thread
 * using ThreadLocalSessionProviderService, if no, initializes it getting
 * current credentials from AuthenticationService and initializing 
 * ThreadLocalSessionProviderService with  newly created SessionProvider  
 * @author Gennady Azarenkov
 * @version $Id: $
 */

public class ThreadLocalSessionProviderInitializedFilter implements Filter {

//  private AuthenticationService authenticationService;
  

  private ThreadLocalSessionProviderService providerService;
  
  private static final Log LOGGER =
    ExoLogger.getLogger("jcr.ThreadLocalSessionProviderInitializedFilter"); 

  /* (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  public void init(FilterConfig config) throws ServletException {
  }

  /* (non-Javadoc)
   * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
   * javax.servlet.ServletResponse, javax.servlet.FilterChain)
   */
  public void doFilter(ServletRequest request, ServletResponse response,
      FilterChain chain) throws IOException, ServletException {
      
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    
//    authenticationService = (AuthenticationService) container
//        .getComponentInstanceOfType(AuthenticationService.class);
    providerService = (ThreadLocalSessionProviderService) container
        .getComponentInstanceOfType(ThreadLocalSessionProviderService.class);

    HttpServletRequest httpRequest = (HttpServletRequest) request;
    String user = httpRequest.getRemoteUser();
    LOGGER.debug("Current user : " + user);

    SessionProvider provider = null;

    // initialize thread local SessionProvider
    if (user != null) {
      Identity identity = null;
      try {
//        identity = authenticationService.getIdentityBySessionId(user);
      } catch (Exception e) {
        throw new ServletException(e);
      }

      if (identity != null)
        provider = new SessionProvider(null);
      else
        LOGGER.warn("Identity is null from Authentication Service for " 
            + user + ".");
        
    }
    
    if (provider == null) {
      LOGGER.warn("Create SessionProvider for anonymous.");
      provider = SessionProvider.createAnonimProvider();
    }
    providerService.setSessionProvider(null, provider);
    chain.doFilter(request, response);
    // remove SessionProvider
    providerService.removeSessionProvider(null);

  }
  
  /* (non-Javadoc)
   * @see javax.servlet.Filter#destroy()
   */
  public void destroy() {
  }

}
