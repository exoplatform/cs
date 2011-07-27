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
package org.exoplatform.contact.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.impl.core.query.QueryImpl;
import org.exoplatform.services.jcr.impl.core.query.lucene.QueryResultImpl;

/**
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since July 25, 2007
 */
public class ContactPageList extends JCRPageList {

  private String       username_;

  private NodeIterator iter_        = null;

  private String       value_;

  private String       contactType_ = "0";

  // add to fix bug 1484
  private long         pageReturn   = 0;

  // add to fix bug 2925
  private Session      session_     = null;

  public ContactPageList(String username, long pageSize, String value, String type) throws Exception {

    super(pageSize);
    username_ = username;
    value_ = value;
    contactType_ = type;
    Session session = getJCRSession(username);
    if (session != null) {
      try {
        setAvailablePage(((QueryResultImpl) createXPathQuery(session, username, value_).execute()).getTotalSize());
      } finally {
        session.logout();
      }
    }
  }

  protected void populateCurrentPage(long page, String username) throws Exception {
    long pageSize = getPageSize();
    Node currentNode;
    Session session = getJCRSession(username);
    long totalPage = 0;
    try {
      QueryImpl queryImpl = createXPathQuery(session, username, value_);
      if (page > 1) {
        long position = (page - 1) * pageSize;
        if (pageReturn == page) {
          queryImpl.setOffset(position - 1);
        } else {
          queryImpl.setOffset(position);
        }
      }
      queryImpl.setLimit(pageSize);
      QueryResult result = queryImpl.execute();
      iter_ = result.getNodes();
      totalPage = ((QueryResultImpl) result).getTotalSize();
    } finally {
      session.logout();
    }
    setAvailablePage(totalPage);

    // cs- 1017
    /*
     * if (iter_ == null) { Session session = getJCRSession(username); try { if (isQuery_) { QueryManager qm =
     * session.getWorkspace().getQueryManager(); Query query = qm.createQuery(value_, Query.XPATH); QueryResult result =
     * query.execute(); iter_ = result.getNodes(); } else { Node node = (Node) session.getItem(value_); iter_ = node.getNodes(); }
     * } finally { session.logout(); } session.logout(); } setAvailablePage(iter_.getSize()); Node currentNode; long pageSize =
     * getPageSize(); long position = 0; if (page == 1) position = 0; else { position = (page - 1) * pageSize; if (pageReturn ==
     * page) { try { iter_.skip(position - 1); } catch (Exception e) {  } } else {
     * iter_.skip(position); } }
     */

    // boolean containDefault = false;
    currentListPage_ = new ArrayList<Contact>();
    for (int i = 0; i < pageSize; i++) {
      if (iter_ != null && iter_.hasNext()) {
        currentNode = iter_.nextNode();
        if (currentNode.isNodeType("exo:contact")) {
          Contact contact = Utils.getContact(currentNode, contactType_);
          /*
           * if (contact.getId().equalsIgnoreCase(username_) && (contactType_.equals(JCRDataStorage.PERSONAL))) { if (page > 1) {
           * i--; continue; } currentListPage_.add(0, contact); containDefault = true; } else
           */
          currentListPage_.add(contact);
        }
      } else {
        break;
      }
    }
    // add to take default contact to first of list
    /*
     * if (page == 1 && !containDefault && contactType_.equals(JCRDataStorage.PERSONAL) &&
     * value_.contains(NewUserListener.DEFAULTGROUP + username_) && iter_ != null) { iter_.skip(0); while (iter_.hasNext()) { Node
     * defaultNode = iter_.nextNode(); if (defaultNode.getProperty("exo:id").getString().equals(username_)) { Contact
     * defaultContact = getContact(defaultNode, contactType_); if (iter_.getSize() % pageSize == 0) pageReturn = iter_.getSize() /
     * pageSize; else pageReturn = iter_.getSize() / pageSize + 1; currentListPage_.remove(currentListPage_.size() - 1);
     * currentListPage_.add(0, defaultContact); break; } } }
     */
    iter_ = null;
  }

  @Override
  public List<Contact> getAll() throws Exception {
    /*
     * if (iter_ == null) { Session session = getJCRSession(username_); if (isQuery_) { QueryManager qm =
     * session.getWorkspace().getQueryManager(); Query query = qm.createQuery(value_, Query.XPATH); QueryResult result =
     * query.execute(); iter_ = result.getNodes(); } else { Node node = (Node) session.getItem(value_); iter_ = node.getNodes(); }
     * session.logout(); }
     */
    Session session = getJCRSession(username_);
    try {
      QueryImpl queryImpl = createXPathQuery(session, username_, value_);
      // queryImpl.setLimit(pageSize);
      QueryResult result = queryImpl.execute();
      iter_ = result.getNodes();
    } finally {
      session.logout();
    }
    List<Contact> contacts = new ArrayList<Contact>();
    while (iter_.hasNext()) {
      Node contactNode = iter_.nextNode();
      contacts.add(Utils.getContact(contactNode, contactType_));
    }
    return contacts;
  }

  public Map<String, String> getEmails() throws Exception {
    /*
     * if (iter_ == null) { Session session = getJCRSession(username_); if (isQuery_) { QueryManager qm =
     * session.getWorkspace().getQueryManager(); Query query = qm.createQuery(value_, Query.XPATH); QueryResult result =
     * query.execute(); iter_ = result.getNodes(); } else { Node node = (Node) session.getItem(value_); iter_ = node.getNodes(); }
     * session.logout(); }
     */
    Session session = getJCRSession(username_);
    try {
      QueryImpl queryImpl = createXPathQuery(session, username_, value_);
      // queryImpl.setLimit(pageSize);
      QueryResult result = queryImpl.execute();
      iter_ = result.getNodes();
    } finally {
      session.logout();
    }
    NodeIterator inter = iter_;
    Map<String, String> emails = new LinkedHashMap<String, String>();
    while (inter.hasNext()) {
      Node contactNode = inter.nextNode();
      String email = null;
      String fullName = null;
      try {
        email = valuesToString(contactNode.getProperty("exo:emailAddress").getValues());
      } catch (PathNotFoundException e) {
        email = "";
      }
      try {
        fullName = contactNode.getProperty("exo:fullName").getString();
      } catch (PathNotFoundException e) {
        fullName = "";
      }
      emails.put(contactNode.getProperty("exo:id").getString(), fullName + Utils.SPLIT + email);
    }
    return emails;
  }

  public void setList(List<Contact> contacts) {
  }

  public void setSession(Session s) {
    session_ = s;
  }

  private Session getJCRSession(String username) throws Exception {
    try {
      RepositoryService repositoryService = (RepositoryService) PortalContainer.getComponent(RepositoryService.class);
      SessionProvider sessionProvider = SessionProvider.createSystemProvider();
      String defaultWS = repositoryService.getCurrentRepository().getConfiguration().getDefaultWorkspaceName();
      return sessionProvider.getSession(defaultWS, repositoryService.getCurrentRepository());
    } catch (NullPointerException e) {
      return session_;
    }
  }

  private QueryImpl createXPathQuery(Session session, String username, String xpath) throws Exception {
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    return (QueryImpl) queryManager.createQuery(xpath, Query.XPATH);
  }

  private String valuesToString(Value[] values) {
    if (values == null)
      return null;
    StringBuilder strs = new StringBuilder();
    try {
      for (Value value : values) {
        if (strs.length() == 0)
          strs.append(value.getString());
        else
          strs.append(";" + value.getString());
      }
    } catch (Exception e) {
    }
    return strs.toString();
  }
}
