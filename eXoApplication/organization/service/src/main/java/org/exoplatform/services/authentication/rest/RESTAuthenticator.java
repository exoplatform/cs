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
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.exoplatform.common.http.HTTPStatus;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.rest.resource.ResourceContainer;
import org.exoplatform.services.security.jaas.BasicCallbackHandler;

/**
 * @author <a href="mailto:vitalka_p@ukr.net">Vitaly Parfonov</a>
 * @version $Id: $
 */
@Path("/organization/authenticate")
public class RESTAuthenticator implements ResourceContainer {

  public RESTAuthenticator() {}

  /**
   * Check if the username and the password of an user is valid.
   * @return return Response with status 200 (OK) if the login is successful,
   *         otherwise return Response with status 403 (Forbidden).
   */
  @POST
  @Path("/authenticate/")
  public Response authenticate(
	@FormParam("username") String username,
	@FormParam("password") String password) {
    try {
      LoginContext loginContext = new LoginContext(ExoContainerContext.getCurrentContainer().getContext().getRealmName(),
          new BasicCallbackHandler(username, password.toCharArray()));
      loginContext.login();
      loginContext.logout();
      return Response.ok().build();
    } catch (Exception e) {
      e.printStackTrace();
      return Response.status(HTTPStatus.FORBIDDEN).entity(e.getMessage())
          .build();
    }
  }
  
  
//  @POST
//  @Path("/organization/authenticate/")
//  public Response isUserLogedIn(@QueryParam("username") String userId ){
//    IdentityRegistry identityRegistry = (IdentityRegistry) getContainer().getComponentInstanceOfType(
//        IdentityRegistry.class);
//    if (identityRegistry.getIdentity(userId) != null)
//      return Response.ok().build();
//
//    return Response.withStatus(HTTPStatus.FORBIDDEN).build();
//
//  }
//
//  private static ExoContainer getContainer() {
//    return ExoContainerContext.getCurrentContainer();
//  }


}
