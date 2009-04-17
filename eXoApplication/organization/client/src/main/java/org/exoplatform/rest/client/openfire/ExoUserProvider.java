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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.httpclient.HttpStatus;

import org.exoplatform.rest.client.openfire.Utils.Response;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserAlreadyExistsException;
import org.jivesoftware.openfire.user.UserCollection;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.openfire.user.UserProvider;
import org.jivesoftware.util.JiveGlobals;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:vitalka_p@ukr.net">Vitaly Parfonov</a>
 * @version $Id: $
 */

public class ExoUserProvider implements UserProvider {

  private static final String FIND_USERS_URL = "exoUserProvider.findUsersURL";
  private static final String FIND_USERS_METHHOD = "exoUserProvider.findUsersMethod";
  private static final String FIND_USERS_PARAMS = "exoUserProvider.findUsersParams";

  private static final String GET_USERS_URL = "exoUserProvider.getUsersURL";
  private static final String GET_USERS_METHOD = "exoUserProvider.getUsersMethod";
  private static final String GET_USERS_PARAMS = "exoUserProvider.getUsersParams";
  
  private static final String USERS_COUNT_URL = "exoUserProvider.usersCountURL";
  private static final String USERS_COUNT_METHOD = "exoUserProvider.usersCountMethod";
  private static final String USERS_COUNT_PARAMS = "exoUserProvider.usersCountParams";

  private static final String USER_INFO_URL = "exoUserProvider.userInfoURL";
  private static final String USER_INFO_METHOD = "exoUserProvider.userInfoMethod";
  private static final String USER_INFO_PARAMS = "exoUserProvider.userInfoParams";

  // URL for searching user names.
  private final String findUsersURL_;
  // HTTP method for searching user names.
  private final String findUsersMethod_;
  // Query parameters
  private final Map<String, String> findUsersParams_; 

  // URL for getting all user names.
  private final String getUsersURL_;
  // HTTP method for getting all user names.
  private final String getUsersMethod_;
  // Query parameters
  private final Map<String, String> getUsersParams_; 
  
  // URL for getting total number of user.
  private final String usersCountURL_;
  // HTTP method for getting total number of user.
  private final String usersCountMethod_;
  // Query parameters
  private final Map<String, String> usersCountParams_; 
  
  // URL for getting user information about specified user.
  private final String userInfoURL_;
  // HTTP method for getting user information about specified user.
  private final String userInfoMethod_;
  // Query parameters
  private final Map<String, String> userInfoParams_; 

  public ExoUserProvider() {
    String t = JiveGlobals.getXMLProperty(FIND_USERS_URL);
    
    findUsersURL_ = t.endsWith("/") ? t : t + "/";
    findUsersMethod_ = JiveGlobals.getXMLProperty(FIND_USERS_METHHOD);
    findUsersParams_ = Utils.parseQuery(JiveGlobals.getXMLProperties(FIND_USERS_PARAMS));
    
    t = JiveGlobals.getXMLProperty(GET_USERS_URL);
    getUsersURL_ = t.endsWith("/") ? t : t + "/";
    getUsersMethod_ = JiveGlobals.getXMLProperty(GET_USERS_METHOD);
    getUsersParams_ = Utils.parseQuery(JiveGlobals.getXMLProperties(GET_USERS_PARAMS));
    
    t = JiveGlobals.getXMLProperty(USERS_COUNT_URL);
    usersCountURL_ = t.endsWith("/") ? t : t + "/";
    usersCountMethod_ = JiveGlobals.getXMLProperty(USERS_COUNT_METHOD);
    usersCountParams_ = Utils.parseQuery(JiveGlobals.getXMLProperties(USERS_COUNT_PARAMS));

    t = JiveGlobals.getXMLProperty(USER_INFO_URL);
    userInfoURL_ = t.endsWith("/") ? t : t + "/";
    userInfoMethod_ = JiveGlobals.getXMLProperty(USER_INFO_METHOD);
    userInfoParams_ = Utils.parseQuery(JiveGlobals.getXMLProperties(USER_INFO_PARAMS));
  }
  
  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#findUsers(java.util.Set,java.lang.String)
   */
  public Collection<User> findUsers(Set<String> fields, String query)
      throws UnsupportedOperationException {
    String url = findUsersURL_;
    String method = findUsersMethod_;
    if (query == null || "".equals(query) || fields.isEmpty())
      return Collections.emptyList();
    if (!getSearchFields().containsAll(fields))
      throw new IllegalArgumentException("Search fields "
          + fields + " are not valid.");
    Collection<String> usernames = findUsers(fields, query, url, method);
    return new UserCollection(usernames.toArray(new String[usernames.size()]));
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#findUsers(java.util.Set,
   *      java.lang.String, int, int)
   */
  public Collection<User> findUsers(Set<String> fields, String query,
      int startIndex, int numResults) throws UnsupportedOperationException {
    String url = findUsersURL_ + startIndex + "/" + (startIndex + numResults);
    String method = findUsersMethod_;
    if (query == null || "".equals(query) || fields.isEmpty())
      return Collections.emptyList();
    if (!getSearchFields().containsAll(fields))
      throw new IllegalArgumentException("Search fields "
          + fields + " are not valid.");
    Collection<String> usernames = findUsers(fields, query, url, method);
    return new UserCollection(usernames.toArray(new String[usernames.size()]));
  }

  private Collection<String> findUsers(Set<String> fields, String query,
      String url, String method) {
    Response resp = null;
    HashMap<String, String> params = new HashMap<String, String>(findUsersParams_);
    for (String field : fields) {
      if ("name".equals(field)) {
        params.put("firstname", query);
        params.put("lastname", query);
      } else {
        params.put(field, query);
      }
    }
    try {
      if ("POST".equalsIgnoreCase(method))
        resp = Utils.doPost(new URL(url), params);
      else if ("GET".equalsIgnoreCase(method))
        resp = Utils.doGet(new URL(url), params);
      else
        throw new IllegalStateException(
            "Configuration error, only HTTP methods 'POST' or 'GET' are allowed, "
            + "but found '" + method + "'.");
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    if (resp.getStatus() == HttpStatus.SC_OK) {
      return createUserList(resp.getResponseDoc());
    } else if (resp.getStatus() == HttpStatus.SC_NOT_FOUND) {
      return Collections.emptyList();
    }
    throw new IllegalStateException("Unknown response status : " +
        resp.getStatus());
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#getSearchFields()
   */
  public Set<String> getSearchFields() throws UnsupportedOperationException {
    return new HashSet<String>(Arrays.asList("username", "name", "email"));
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#getUserCount()
   */
  public int getUserCount() {
    Response resp = null;
    String url = usersCountURL_;
    String method = usersCountMethod_;
    HashMap<String, String> params = new HashMap<String, String>(usersCountParams_);
    try {
      if ("POST".equalsIgnoreCase(method))
        resp = Utils.doPost(new URL(url), params);
      else if ("GET".equalsIgnoreCase(method))
        resp = Utils.doGet(new URL(url), params);
      else
        throw new IllegalStateException(
            "Configuration error, only HTTP methods 'POST' or 'GET' are allowed, "
            + "but found '" + method + "'.");
    } catch (Exception e) {
      e.printStackTrace();
      return -1;
    }
    if (resp.getStatus() == HttpStatus.SC_OK) {
      Document d = resp.getResponseDoc();
      return Integer.valueOf(d.getDocumentElement().getElementsByTagName(
          "number").item(0).getTextContent());
    }
    throw new IllegalStateException("Unknown response status : " +
        resp.getStatus());
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#getUsernames()
   */
  public Collection<String> getUsernames() {
    return getUsernames(getUsersURL_, getUsersMethod_);
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#getUsers()
   */
  public Collection<User> getUsers() {
    Collection<String> usernames = getUsernames(getUsersURL_, getUsersMethod_);
    return new UserCollection(usernames.toArray(new String[usernames.size()]));
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#getUsers(int, int)
   */
  public Collection<User> getUsers(int startIndex, int numResults) {
    String url = getUsersURL_ + startIndex + "/" + (startIndex + numResults);
    String method = getUsersMethod_;
    Collection<String> usernames = getUsernames(url, method);
    return new UserCollection(usernames.toArray(new String[usernames.size()]));
  }

  private Collection<String> getUsernames(String url, String method) {
    Response resp = null;
    HashMap<String, String> params = new HashMap<String, String>(getUsersParams_);
    try {
      if ("POST".equalsIgnoreCase(method))
        resp = Utils.doPost(new URL(url), params);
      else if("GET".equalsIgnoreCase(method))
        resp = Utils.doGet(new URL(url), params);
      else
        throw new IllegalStateException(
            "Configuration error, only HTTP methods 'POST' or 'GET' are allowed, "
            + "but found '" + method + "'.");
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    if (resp.getStatus() == HttpStatus.SC_OK) {
      return createUserList(resp.getResponseDoc());
    }
    throw new IllegalStateException("Unknown response status : " +
        resp.getStatus());
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#isReadOnly()
   */
  public boolean isReadOnly() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#loadUser(java.lang.String)
   */
  public User loadUser(String username) throws UserNotFoundException {
    String url = userInfoURL_ + username + "/";
    String method = userInfoMethod_;
    HashMap<String, String> params = new HashMap<String, String>(userInfoParams_);
    Response resp = null;
    try {
      if ("POST".equalsIgnoreCase(method))
        resp = Utils.doPost(new URL(url), params);
      else if ("GET".equalsIgnoreCase(method))
        resp = Utils.doGet(new URL(url), params);
      else
        throw new IllegalStateException(
            "Configuration error, only HTTP methods 'POST' or 'GET' are allowed, "
            + "but found '" + method + "'.");
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
    if (resp.getStatus() == HttpStatus.SC_OK) {
      Document d = resp.getResponseDoc();
      String name = d.getDocumentElement().getElementsByTagName("first-name")
          .item(0).getTextContent();
      String email = d.getDocumentElement().getElementsByTagName("email").item(
          0).getTextContent();
      return new User(username, name, email, new Date(), new Date());
    } else if (resp.getStatus() == HttpStatus.SC_NOT_FOUND) {
      throw new UserNotFoundException("User '" + username + "' not found!");
    }
    throw new IllegalStateException("Unknown response status : " +
        resp.getStatus());
  }

  private List<String> createUserList(Document d) {
    List<String> usernames = new ArrayList<String>();
    NodeList u = d.getDocumentElement().getElementsByTagName("name");
    for (int i = 0; i < u.getLength(); i++)
      usernames.add(u.item(i).getTextContent());
    return usernames;
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#setCreationDate(java.lang.String,
   *      java.util.Date)
   */
  public void setCreationDate(String username, Date creationDate)
      throws UserNotFoundException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#setEmail(java.lang.String,
   *      java.lang.String)
   */
  public void setEmail(String username, String email)
      throws UserNotFoundException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#setModificationDate(java.lang.String,
   *      java.util.Date)
   */
  public void setModificationDate(String username, Date modificationDate)
      throws UserNotFoundException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#setName(java.lang.String,
   *      java.lang.String)
   */
  public void setName(String username, String name)
      throws UserNotFoundException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#createUser(java.lang.String,
   *      java.lang.String, java.lang.String, java.lang.String)
   */
  public User createUser(String username, String password, String name,
      String email) throws UserAlreadyExistsException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.user.UserProvider#deleteUser(java.lang.String)
   */
  public void deleteUser(String username) {
    throw new UnsupportedOperationException();
  }

}
