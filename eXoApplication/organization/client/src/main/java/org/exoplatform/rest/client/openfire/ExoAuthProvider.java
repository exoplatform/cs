/**
 * Copyright (C) 2003-2008 eXo Platform SAS.
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

package org.exoplatform.rest.client.openfire;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.exoplatform.rest.client.openfire.Utils.Response;
import org.jivesoftware.openfire.auth.AuthProvider;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.JiveGlobals;

/**
 * @author <a href="mailto:vitalka_p@ukr.net">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class ExoAuthProvider implements AuthProvider {

  private static final String       AUTHENTICATION_URL    = "eXo.provider.exoAuthProvider.authenticationURL";

  private static final String       AUTHENTICATION_METHOD = "eXo.provider.exoAuthProvider.authenticationMethod";

  private static final String       AUTHENTICATION_PARAMS = "eXo.provider.exoAuthProvider.authenticationParams";

  // URL for authentication users.
  private final String              authURL_;

  // HTTP method for authentication users.
  private final String              authMethod_;

  // Query parameters.
  private final Map<String, String> authParams_;

  public ExoAuthProvider() {
    String t = JiveGlobals.getXMLProperty(AUTHENTICATION_URL);
    authURL_ = Utils.getBaseURL() + (t.endsWith("/") ? t : t + "/");
    authMethod_ = JiveGlobals.getXMLProperty(AUTHENTICATION_METHOD);
    authParams_ = Utils.parseQuery(JiveGlobals.getXMLProperties(AUTHENTICATION_PARAMS));
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.auth.AuthProvider#authenticate(java.lang.String, java.lang.String)
   */
  public void authenticate(String user, String pass) throws UnauthorizedException {
    // System.out.println(">>>>>>>>>>>>>>> plain text authenticate");
    String url = authURL_;
    String method = authMethod_;
    HashMap<String, String> params = new HashMap<String, String>(authParams_);
    params.put("username", user);
    params.put("password", pass);
    Response resp = null;

    try {
      if ("POST".equalsIgnoreCase(method))
        resp = Utils.doPost(new URL(url), params);
      else if ("GET".equalsIgnoreCase(method))
        resp = Utils.doGet(new URL(url), params);
      else
        throw new UnauthorizedException("Authentication filed : " + "Configuration error, only HTTP methods 'POST' or 'GET' allowed, " + "but found '" + authMethod_ + "'.");
    } catch (Exception e) {
      e.printStackTrace();
      throw new UnauthorizedException("Authentication filed : " + e);
    }
    if (resp.getStatus() != HttpStatus.SC_OK) {
      throw new UnauthorizedException("Authentication filed for user " + user + ". Returned status : " + resp.getStatus());
    }
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.auth.AuthProvider#authenticate(java.lang.String, java.lang.String, java.lang.String)
   */
  public void authenticate(String user, String token, String digest) throws UnauthorizedException {
    // System.out.println(">>>>>>>>>>>>>>> digest authenticate");
    // System.out.println(user + "\n" + token + "\n" + digest);
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.auth.AuthProvider#getPassword(java.lang.String)
   */
  public String getPassword(String arg0) throws UserNotFoundException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.auth.AuthProvider#isDigestSupported()
   */
  public boolean isDigestSupported() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.auth.AuthProvider#isPlainSupported()
   */
  public boolean isPlainSupported() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.auth.AuthProvider#setPassword(java.lang.String, java.lang.String)
   */
  public void setPassword(String user, String pass) throws UserNotFoundException, UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.auth.AuthProvider#supportsPasswordRetrieval()
   */
  public boolean supportsPasswordRetrieval() {
    return false;
  }

}
