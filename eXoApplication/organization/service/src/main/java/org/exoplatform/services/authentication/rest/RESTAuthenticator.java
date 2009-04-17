/**
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

package org.exoplatform.services.authentication.rest;

import javax.security.auth.login.LoginContext;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.security.IdentityRegistry;
import org.exoplatform.services.security.jaas.BasicCallbackHandler;

import org.exoplatform.services.rest.HTTPMethod;
import org.exoplatform.services.rest.QueryParam;
import org.exoplatform.services.rest.Response;
import org.exoplatform.services.rest.URITemplate;
import org.exoplatform.services.rest.container.ResourceContainer;

/**
 * @author <a href="mailto:vitalka_p@ukr.net">Vitaly Parfonov</a>
 * @version $Id: $
 */

public class RESTAuthenticator implements ResourceContainer {

  public RESTAuthenticator() {}

  /**
   * Check if the username and the password of an user is valid.
   * @return return Response with status 200 (OK) if the login is successful,
   *         otherwise return Response with status 403 (Forbidden).
   */
  @HTTPMethod("POST")
  @URITemplate("/organization/authenticate/")
  public Response authenticate(
      @QueryParam("username") String username,
      @QueryParam("password") String password) {
    try {
      LoginContext loginContext = new LoginContext("exo-domain",
          new BasicCallbackHandler(username, password.toCharArray()));
      loginContext.login();
      loginContext.logout();
      return Response.Builder.ok().build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.Builder.withStatus(HTTPStatus.FORBIDDEN).errorMessage(e.getMessage())
          .build();
    }
  }
  
  
//  @HTTPMethod("POST")
//  @URITemplate("/organization/authenticate/")
//  public Response isUserLogedIn(@QueryParam("username") String userId ){
//    IdentityRegistry identityRegistry = (IdentityRegistry) getContainer().getComponentInstanceOfType(
//        IdentityRegistry.class);
//    if (identityRegistry.getIdentity(userId) != null)
//      return Response.Builder.ok().build();
//
//    return Response.Builder.withStatus(HTTPStatus.FORBIDDEN).build();
//
//  }
//
//  private static ExoContainer getContainer() {
//    return ExoContainerContext.getCurrentContainer();
//  }


}
