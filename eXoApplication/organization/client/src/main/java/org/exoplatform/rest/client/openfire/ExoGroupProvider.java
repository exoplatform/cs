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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpStatus;
import org.exoplatform.rest.client.openfire.Utils.Response;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jivesoftware.openfire.group.Group;
import org.jivesoftware.openfire.group.GroupAlreadyExistsException;
import org.jivesoftware.openfire.group.GroupNotFoundException;
import org.jivesoftware.openfire.group.GroupProvider;
import org.jivesoftware.util.JiveGlobals;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xmpp.packet.JID;

/**
 * @author <a href="mailto:vitalka_p@ukr.net">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class ExoGroupProvider implements GroupProvider {
  private static final Log log = ExoLogger.getExoLogger(ExoGroupProvider.class);

  private static final String       GROUPS_COUNT_URL          = "eXo.provider.exoGroupProvider.groupsCountURL";

  private static final String       GROUPS_COUNT_METHOD       = "eXo.provider.exoGroupProvider.groupsCountMethod";

  private static final String       GROUPS_COUNT_PARAMS       = "eXo.provider.exoGroupProvider.groupsCountParams";

  private static final String       GET_GROUPS_ALL_URL        = "eXo.provider.exoGroupProvider.getGroupsAllURL";

  private static final String       GET_GROUPS_ALL_METHOD     = "eXo.provider.exoGroupProvider.getGroupsAllMethod";

  private static final String       GET_GROUPS_ALL_PARAMS     = "eXo.provider.exoGroupProvider.getGroupsAllParams";

  private static final String       GET_GROUPS_RANGE_URL      = "eXo.provider.exoGroupProvider.getGroupsRangeURL";

  private static final String       GET_GROUPS_RANGE_METHOD   = "eXo.provider.exoGroupProvider.getGroupsRangeMethod";

  private static final String       GET_GROUPS_RANGE_PARAMS   = "eXo.provider.exoGroupProvider.getGroupsRangeParams";

  private static final String       GET_GROUPS_FORUSER_URL    = "eXo.provider.exoGroupProvider.getGroupsForUserURL";

  private static final String       GET_GROUPS_FORUSER_METHOD = "eXo.provider.exoGroupProvider.getGroupsForUserMethod";

  private static final String       GET_GROUPS_FORUSER_PARAMS = "eXo.provider.exoGroupProvider.getGroupsForUserParams";

  private static final String       GROUP_INFO_URL            = "eXo.provider.exoGroupProvider.groupInfoURL";

  private static final String       GROUP_INFO_METHOD         = "eXo.provider.exoGroupProvider.groupInfoMethod";

  private static final String       GROUP_INFO_PARAMS         = "eXo.provider.exoGroupProvider.groupInfoParams";

  // URL for getting information about specified group.
  private final String              groupInfoURL_;

  // HTTP method for getting information about specified group.
  private final String              groupInfoMethod_;

  // Query parameters
  private final Map<String, String> groupInfoParams_;

  // URL for getting list of groups.
  private final String              getGroupsAllURL_;

  // HTTP method for getting list of groups.
  private final String              getGroupsAllMethod_;

  // Query parameters
  private final Map<String, String> getGroupsAllParams_;

  // URL for getting list of groups.
  private final String              getGroupsRangeURL_;

  // HTTP method for getting list of groups.
  private final String              getGroupsRangeMethod_;

  // Query parameters
  private final Map<String, String> getGroupsRangeParams_;

  // URL for getting list of groups.
  private final String              getGroupsForUserURL_;

  // HTTP method for getting list of groups.
  private final String              getGroupsForUserMethod_;

  // Query parameters
  private final Map<String, String> getGroupsForUserParams_;

  // URL for getting total number of groups.
  private final String              groupsCountURL_;

  // HTTP method for getting total number of groups.
  private final String              groupsCountMethod_;

  // Query parameters
  private final Map<String, String> groupsCountParams_;

  public ExoGroupProvider() {
    String t = JiveGlobals.getXMLProperty(GROUP_INFO_URL);
    groupInfoURL_ = Utils.getBaseURL() + (t.endsWith("/") ? t : t + "/");
    groupInfoMethod_ = JiveGlobals.getXMLProperty(GROUP_INFO_METHOD);
    groupInfoParams_ = Utils.parseQuery(JiveGlobals.getXMLProperties(GROUP_INFO_PARAMS));

    t = JiveGlobals.getXMLProperty(GET_GROUPS_ALL_URL);
    getGroupsAllURL_ = Utils.getBaseURL() + (t.endsWith("/") ? t : t + "/");
    getGroupsAllMethod_ = JiveGlobals.getXMLProperty(GET_GROUPS_ALL_METHOD);
    getGroupsAllParams_ = Utils.parseQuery(JiveGlobals.getXMLProperties(GET_GROUPS_ALL_PARAMS));

    t = JiveGlobals.getXMLProperty(GET_GROUPS_RANGE_URL);
    getGroupsRangeURL_ = Utils.getBaseURL() + (t.endsWith("/") ? t : t + "/");
    getGroupsRangeMethod_ = JiveGlobals.getXMLProperty(GET_GROUPS_RANGE_METHOD);
    getGroupsRangeParams_ = Utils.parseQuery(JiveGlobals.getXMLProperties(GET_GROUPS_RANGE_PARAMS));

    t = JiveGlobals.getXMLProperty(GROUPS_COUNT_URL);
    groupsCountURL_ = Utils.getBaseURL() + (t.endsWith("/") ? t : t + "/");
    groupsCountMethod_ = JiveGlobals.getXMLProperty(GROUPS_COUNT_METHOD);
    groupsCountParams_ = Utils.parseQuery(JiveGlobals.getXMLProperties(GROUPS_COUNT_PARAMS));

    t = JiveGlobals.getXMLProperty(GET_GROUPS_FORUSER_URL);
    getGroupsForUserURL_ = Utils.getBaseURL() + (t.endsWith("/") ? t : t + "/");
    getGroupsForUserMethod_ = JiveGlobals.getXMLProperty(GET_GROUPS_FORUSER_METHOD);
    getGroupsForUserParams_ = Utils.parseQuery(JiveGlobals.getXMLProperties(GET_GROUPS_FORUSER_PARAMS));
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#getGroup(java.lang.String)
   */
  public Group getGroup(String group) throws GroupNotFoundException {
    String url = groupInfoURL_;
    String method = groupInfoMethod_;
    HashMap<String, String> params = new HashMap<String, String>(groupInfoParams_);
    Response resp = null;
    url += group + "/";
    try {
      if ("POST".equalsIgnoreCase(method)) {
        resp = Utils.doPost(new URL(url), params);
      } else if ("GET".equalsIgnoreCase(method)) {
        resp = Utils.doGet(new URL(url), params);
      } else
        throw new IllegalStateException("Configuration error, only HTTP methods 'POST' or 'GET' are allowed, " + "but found '" + method + "'.");

    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception in method getGroup", e);
      }
      return null;
    }
    if (resp.getStatus() == HttpStatus.SC_OK) {
      Document d = resp.getResponseDoc();
      String description = d.getDocumentElement().getElementsByTagName("description").item(0).getTextContent();
      NodeList t = d.getDocumentElement().getElementsByTagName("member");
      List<JID> members = new ArrayList<JID>();
      for (int i = 0; i < t.getLength(); i++)
        members.add(new JID(t.item(i).getTextContent()));
      // no administrators for each groups
      List<JID> administrators = Collections.emptyList();
      return new Group(group, description, members, administrators);

    } else if (resp.getStatus() == HttpStatus.SC_NOT_FOUND) {
      throw new GroupNotFoundException();
    }
    throw new IllegalStateException("Unknown response status : " + resp.getStatus());
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#getGroupCount()
   */
  public int getGroupCount() {
    String url = groupsCountURL_;
    String method = groupsCountMethod_;
    HashMap<String, String> params = new HashMap<String, String>(groupsCountParams_);
    Response resp = null;
    try {
      if ("POST".equalsIgnoreCase(method))
        resp = Utils.doPost(new URL(url), params);
      else if ("GET".equalsIgnoreCase(method))
        resp = Utils.doGet(new URL(url), params);
      else
        throw new IllegalStateException("Configuration error, only HTTP methods 'POST' or 'GET' are allowed, " + "but found '" + method + "'.");
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception in method getGroupCount", e);
      }
      return -1;
    }
    if (resp.getStatus() == HttpStatus.SC_OK) {
      Document d = resp.getResponseDoc();
      Integer count = Integer.valueOf(d.getDocumentElement().getElementsByTagName("number").item(0).getTextContent());
      return count - 1; // Do not include root node
    }
    throw new IllegalStateException("Unknown response status : " + resp.getStatus());
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#getGroupNames()
   */
  public Collection<String> getGroupNames() {
    String url = getGroupsAllURL_;
    String method = getGroupsAllMethod_;
    HashMap<String, String> params = new HashMap<String, String>(getGroupsAllParams_);
    return getGroupNames(url, method, params);
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#getGroupNames(int, int)
   */
  public Collection<String> getGroupNames(int startIndex, int numResults) {
    String url = getGroupsRangeURL_;
    String method = getGroupsRangeMethod_;
    HashMap<String, String> params = new HashMap<String, String>(getGroupsRangeParams_);
    url += startIndex + "/" + (startIndex + numResults);
    return getGroupNames(url, method, params);
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#getGroupNames(org.xmpp.packet.JID)
   */
  public Collection<String> getGroupNames(JID user) {
    String url = getGroupsForUserURL_;
    String method = getGroupsForUserMethod_;
    HashMap<String, String> params = new HashMap<String, String>(getGroupsForUserParams_);
    params.put("username", user.getNode());
    return getGroupNames(url, method, params);
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#getSharedGroupsNames()
   */
  public Collection<String> getSharedGroupsNames() {
    // search is not supported
    return Collections.emptyList();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#search(java.lang.String)
   */
  public Collection<String> search(String query) {
    // search is not supported
    return Collections.emptyList();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#search(java.lang.String, int, int)
   */
  public Collection<String> search(String query, int startIndex, int numResults) {
    // search is not supported
    return Collections.emptyList();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#isReadOnly()
   */
  public boolean isReadOnly() {
    return true;
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#isSearchSupported()
   */
  public boolean isSearchSupported() {
    return false;
  }

  private Collection<String> getGroupNames(String url, String method, HashMap<String, String> params) {
    Response resp = null;
    try {
      if ("POST".equalsIgnoreCase(method))
        resp = Utils.doPost(new URL(url), params);
      else if ("GET".equalsIgnoreCase(method))
        resp = Utils.doGet(new URL(url), params);
      else
        throw new IllegalStateException("Configuration error, only HTTP methods 'POST' or 'GET' are allowed, " + "but found '" + method + "'.");
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception in method getGroupNames", e);
      }
      return null;
    }
    if (resp.getStatus() == HttpStatus.SC_OK) {
      return createGroupList(resp.getResponseDoc());
    }
    throw new IllegalStateException("Unknown response status : " + resp.getStatus());
  }

  /**
   * Create List of group names from given XML document.
   * @param d Document.
   * @return List of group names.
   */
  private List<String> createGroupList(Document d) {
    List<String> groupnames = new ArrayList<String>();
    NodeList u = d.getDocumentElement().getElementsByTagName("group");
    for (int i = 0; i < u.getLength(); i++) {

      Node descendantGroup = u.item(i);
      NamedNodeMap attribs = descendantGroup.getAttributes();
      String groupName = attribs.getNamedItem("groupId").getNodeValue();

      String getGroups = getGroupNameWithDescendants(groupName);
      for (String _group : getGroups.split(":")) {
        if (_group.startsWith("/"))
          _group = _group.substring(1);
        groupnames.add(_group);
      }

    }
    return groupnames;
  }

  private String getGroupNameWithDescendants(String group) {
    String url = getGroupsAllURL_;
    String method = getGroupsAllMethod_;
    Response resp = null;
    HashMap<String, String> params = new HashMap<String, String>();
    params.put("parentId", group);
    try {
      if ("POST".equalsIgnoreCase(method)) {
        resp = Utils.doPost(new URL(url), params);
      } else if ("GET".equalsIgnoreCase(method)) {
        resp = Utils.doGet(new URL(url), params);
      } else
        throw new IllegalStateException("Configuration error, only HTTP methods 'POST' or 'GET' are allowed, " + "but found '" + method + "'.");

    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception in method getGroupNameWithDescendants", e);
      }
      return null;
    }
    if (resp.getStatus() == HttpStatus.SC_OK) {
      Document d = resp.getResponseDoc();

      NodeList groupList = d.getDocumentElement().getElementsByTagName("group");

      // recursion break
      if (groupList.getLength() == 0)
        return group;

      String compositeGroupName = group;
      for (int i = 0; i < groupList.getLength(); i++) {
        Node descendantGroup = groupList.item(i);
        NamedNodeMap attribs = descendantGroup.getAttributes();
        String groupId = attribs.getNamedItem("groupId").getNodeValue();

        compositeGroupName += ":" + getGroupNameWithDescendants(groupId);
      }

      return compositeGroupName;

    } else if (resp.getStatus() == HttpStatus.SC_NOT_FOUND) {
      throw new IllegalStateException("Group not found");
    }
    throw new IllegalStateException("Unknown response status : " + resp.getStatus());

  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#addMember(java.lang.String, org.xmpp.packet.JID, boolean)
   */
  public void addMember(String groupName, JID user, boolean administrator) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#createGroup(java.lang.String)
   */
  public Group createGroup(String name) throws UnsupportedOperationException, GroupAlreadyExistsException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#deleteGroup(java.lang.String)
   */
  public void deleteGroup(String name) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#deleteMember(java.lang.String, org.xmpp.packet.JID)
   */
  public void deleteMember(String groupName, JID user) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#setDescription(java.lang.String, java.lang.String)
   */
  public void setDescription(String name, String description) throws GroupNotFoundException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#setName(java.lang.String, java.lang.String)
   */
  public void setName(String oldName, String newName) throws UnsupportedOperationException, GroupAlreadyExistsException {
    throw new UnsupportedOperationException();
  }

  /*
   * (non-Javadoc)
   * @see org.jivesoftware.openfire.group.GroupProvider#updateMember(java.lang.String, org.xmpp.packet.JID, boolean)
   */
  public void updateMember(String groupName, JID user, boolean administrator) throws UnsupportedOperationException {
    throw new UnsupportedOperationException();
  }

}
