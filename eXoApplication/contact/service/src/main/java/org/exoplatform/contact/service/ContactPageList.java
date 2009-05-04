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

import org.exoplatform.contact.service.impl.JCRDataStorage;
import org.exoplatform.contact.service.impl.NewUserListener;
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

  public ContactPageList(String username,
                         long pageSize,
                         String value,
                         String type) throws Exception {
    
    super(pageSize);
    username_ = username;
    value_ = value;
    contactType_ = type;
  }

  protected void populateCurrentPage(long page, String username) throws Exception {    
    long pageSize = getPageSize() ;
    long totalPage = 0;
    Node currentNode;
    Session session = getJCRSession(username) ;
    
    try {
      QueryImpl queryImpl = createXPathQuery(session, username, value_);
      if( page > 1) {
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
      totalPage = ((QueryResultImpl) result).getTotalSize() ;
    } finally {
      session.logout() ;
    }
    setAvailablePage(totalPage) ;
    
    // cs- 1017
    /*if (iter_ == null) {
      Session session = getJCRSession(username);
      try {
        if (isQuery_) {
          QueryManager qm = session.getWorkspace().getQueryManager();
          Query query = qm.createQuery(value_, Query.XPATH);
          QueryResult result = query.execute();
          iter_ = result.getNodes();
        } else {
          Node node = (Node) session.getItem(value_);
          iter_ = node.getNodes();
        }
      } finally {
        session.logout();
      }
      session.logout();
    }
    setAvailablePage(iter_.getSize());
    Node currentNode;
    long pageSize = getPageSize();
    long position = 0;
    if (page == 1)
      position = 0;
    else {
      position = (page - 1) * pageSize;
      if (pageReturn == page) {
        try {
          iter_.skip(position - 1);
        } catch (Exception e) {
          System.out.println("\n iter exception");
        }
      } else {
        iter_.skip(position);
      }
    }*/
    
    
    
    boolean containDefault = false;
    currentListPage_ = new ArrayList<Contact>();
    for (int i = 0; i < pageSize; i++) {
      if (iter_ != null && iter_.hasNext()) {
        currentNode = iter_.nextNode();
        if (currentNode.isNodeType("exo:contact")) {
          Contact contact = getContact(currentNode, contactType_);
          if (contact.getId().equalsIgnoreCase(username_)
              && (contactType_.equals(JCRDataStorage.PERSONAL))) {
            if (page > 1) {
              i--;
              continue;
            }
            currentListPage_.add(0, contact);
            containDefault = true;
          } else
            currentListPage_.add(contact);
        }
      } else {
        break;
      }
    }
    // add to take default contact to first of list
    if (page == 1 && !containDefault && contactType_.equals(JCRDataStorage.PERSONAL)
        && value_.contains(NewUserListener.DEFAULTGROUP + username_) && iter_ != null) {
      iter_.skip(0);
      while (iter_.hasNext()) {
        Node defaultNode = iter_.nextNode();
        if (defaultNode.getProperty("exo:id").getString().equals(username_)) {
          Contact defaultContact = getContact(defaultNode, contactType_);
          if (iter_.getSize() % pageSize == 0)
            pageReturn = iter_.getSize() / pageSize;
          else
            pageReturn = iter_.getSize() / pageSize + 1;
          currentListPage_.remove(currentListPage_.size() - 1);
          currentListPage_.add(0, defaultContact);
          break;
        }
      }
    }
    iter_ = null;
  }

  private Contact getContact(Node contactNode, String type) throws Exception {
    Contact contact = new Contact();
    if (contactNode.hasProperty("exo:id"))
      contact.setId(contactNode.getProperty("exo:id").getString());
    if (contactNode.hasProperty("exo:fullName"))
      contact.setFullName(contactNode.getProperty("exo:fullName").getString());
    if (contactNode.hasProperty("exo:firstName"))
      contact.setFirstName(contactNode.getProperty("exo:firstName").getString());
    if (contactNode.hasProperty("exo:lastName"))
      contact.setLastName(contactNode.getProperty("exo:lastName").getString());
    if (contactNode.hasProperty("exo:nickName"))
      contact.setNickName(contactNode.getProperty("exo:nickName").getString());
    if (contactNode.hasProperty("exo:gender"))
      contact.setGender(contactNode.getProperty("exo:gender").getString());
    if (contactNode.hasProperty("exo:birthday"))
      contact.setBirthday(contactNode.getProperty("exo:birthday").getDate().getTime());
    if (contactNode.hasProperty("exo:jobTitle"))
      contact.setJobTitle(contactNode.getProperty("exo:jobTitle").getString());
    if (contactNode.hasProperty("exo:emailAddress"))
      contact.setEmailAddress(contactNode.getProperty("exo:emailAddress").getString());

    if (contactNode.hasProperty("exo:exoId"))
      contact.setExoId(contactNode.getProperty("exo:exoId").getString());
    if (contactNode.hasProperty("exo:googleId"))
      contact.setGoogleId(contactNode.getProperty("exo:googleId").getString());
    if (contactNode.hasProperty("exo:msnId"))
      contact.setMsnId(contactNode.getProperty("exo:msnId").getString());
    if (contactNode.hasProperty("exo:aolId"))
      contact.setAolId(contactNode.getProperty("exo:aolId").getString());
    if (contactNode.hasProperty("exo:yahooId"))
      contact.setYahooId(contactNode.getProperty("exo:yahooId").getString());
    if (contactNode.hasProperty("exo:icrId"))
      contact.setIcrId(contactNode.getProperty("exo:icrId").getString());
    if (contactNode.hasProperty("exo:skypeId"))
      contact.setSkypeId(contactNode.getProperty("exo:skypeId").getString());
    if (contactNode.hasProperty("exo:icqId"))
      contact.setIcqId(contactNode.getProperty("exo:icqId").getString());

    if (contactNode.hasProperty("exo:homeAddress"))
      contact.setHomeAddress(contactNode.getProperty("exo:homeAddress").getString());
    if (contactNode.hasProperty("exo:homeCity"))
      contact.setHomeCity(contactNode.getProperty("exo:homeCity").getString());
    if (contactNode.hasProperty("exo:homeState_province"))
      contact.setHomeState_province(contactNode.getProperty("exo:homeState_province").getString());
    if (contactNode.hasProperty("exo:homePostalCode"))
      contact.setHomePostalCode(contactNode.getProperty("exo:homePostalCode").getString());
    if (contactNode.hasProperty("exo:homeCountry"))
      contact.setHomeCountry(contactNode.getProperty("exo:homeCountry").getString());
    if (contactNode.hasProperty("exo:homePhone1"))
      contact.setHomePhone1(contactNode.getProperty("exo:homePhone1").getString());
    if (contactNode.hasProperty("exo:homePhone2"))
      contact.setHomePhone2(contactNode.getProperty("exo:homePhone2").getString());
    if (contactNode.hasProperty("exo:homeFax"))
      contact.setHomeFax(contactNode.getProperty("exo:homeFax").getString());
    if (contactNode.hasProperty("exo:personalSite"))
      contact.setPersonalSite(contactNode.getProperty("exo:personalSite").getString());

    if (contactNode.hasProperty("exo:workAddress"))
      contact.setWorkAddress(contactNode.getProperty("exo:workAddress").getString());
    if (contactNode.hasProperty("exo:workCity"))
      contact.setWorkCity(contactNode.getProperty("exo:workCity").getString());
    if (contactNode.hasProperty("exo:workState_province"))
      contact.setWorkStateProvince(contactNode.getProperty("exo:workState_province").getString());
    if (contactNode.hasProperty("exo:workPostalCode"))
      contact.setWorkPostalCode(contactNode.getProperty("exo:workPostalCode").getString());
    if (contactNode.hasProperty("exo:workCountry"))
      contact.setWorkCountry(contactNode.getProperty("exo:workCountry").getString());
    if (contactNode.hasProperty("exo:workPhone1"))
      contact.setWorkPhone1(contactNode.getProperty("exo:workPhone1").getString());
    if (contactNode.hasProperty("exo:workPhone2"))
      contact.setWorkPhone2(contactNode.getProperty("exo:workPhone2").getString());
    if (contactNode.hasProperty("exo:workFax"))
      contact.setWorkFax(contactNode.getProperty("exo:workFax").getString());
    if (contactNode.hasProperty("exo:mobilePhone"))
      contact.setMobilePhone(contactNode.getProperty("exo:mobilePhone").getString());
    if (contactNode.hasProperty("exo:webPage"))
      contact.setWebPage(contactNode.getProperty("exo:webPage").getString());
    if (contactNode.hasProperty("exo:note"))
      contact.setNote(contactNode.getProperty("exo:note").getString());
    if (contactNode.hasProperty("exo:categories"))
      contact.setAddressBookIds(ValuesToStrings(contactNode.getProperty("exo:categories").getValues()));
    if (contactNode.hasProperty("exo:tags"))
      contact.setTags(ValuesToStrings(contactNode.getProperty("exo:tags").getValues()));
    if (contactNode.hasProperty("exo:viewPermissionUsers"))
      contact.setViewPermissionUsers(ValuesToStrings(contactNode.getProperty("exo:viewPermissionUsers")
                                                                .getValues()));
    if (contactNode.hasProperty("exo:editPermissionUsers"))
      contact.setEditPermissionUsers(ValuesToStrings(contactNode.getProperty("exo:editPermissionUsers")
                                                                .getValues()));

    if (contactNode.hasProperty("exo:viewPermissionGroups"))
      contact.setViewPermissionGroups(ValuesToStrings(contactNode.getProperty("exo:viewPermissionGroups")
                                                                 .getValues()));
    if (contactNode.hasProperty("exo:editPermissionGroups"))
      contact.setEditPermissionGroups(ValuesToStrings(contactNode.getProperty("exo:editPermissionGroups")
                                                                 .getValues()));

    if (contactNode.hasProperty("exo:lastUpdated"))
      contact.setLastUpdated(contactNode.getProperty("exo:lastUpdated").getDate().getTime());
    contact.setPath(contactNode.getPath());
    if (contactNode.hasNode("image")) {
      Node image = contactNode.getNode("image");
      if (image.isNodeType("nt:file")) {
        ContactAttachment file = new ContactAttachment();
        file.setId(image.getPath());
        file.setMimeType(image.getNode("jcr:content").getProperty("jcr:mimeType").getString());
        file.setFileName(image.getName());
        file.setWorkspace(image.getSession().getWorkspace().getName());
        contact.setAttachment(file);
      }
    }
    contact.setContactType(type);
    if (contactNode.hasProperty("exo:isOwner"))
      contact.setOwner(contactNode.getProperty("exo:isOwner").getBoolean());
    if (contactNode.hasProperty("exo:ownerId"))
      contact.setOwnerId(contactNode.getProperty("exo:ownerId").getString());
    return contact;
  }

  private String[] ValuesToStrings(Value[] Val) throws Exception {
    if (Val.length == 1)
      return new String[] { Val[0].getString() };
    String[] Str = new String[Val.length];
    for (int i = 0; i < Val.length; ++i) {
      Str[i] = Val[i].getString();
    }
    return Str;
  }

  @Override
  public List<Contact> getAll() throws Exception {
    /*if (iter_ == null) {
      Session session = getJCRSession(username_);
      if (isQuery_) {
        QueryManager qm = session.getWorkspace().getQueryManager();
        Query query = qm.createQuery(value_, Query.XPATH);
        QueryResult result = query.execute();
        iter_ = result.getNodes();
      } else {
        Node node = (Node) session.getItem(value_);
        iter_ = node.getNodes();
      }
      session.logout();
    }*/
    
    long pageSize = getPageSize() ;
    Session session = getJCRSession(username_) ;    
    try {
      QueryImpl queryImpl = createXPathQuery(session, username_, value_);
      //queryImpl.setLimit(pageSize);
      QueryResult result = queryImpl.execute();
      iter_ = result.getNodes();
    } finally {
      session.logout() ;
    }
    List<Contact> contacts = new ArrayList<Contact>();
    while (iter_.hasNext()) {
      Node contactNode = iter_.nextNode();
      contacts.add(getContact(contactNode, contactType_));
    }
    return contacts;
  }

  public Map<String, String> getEmails() throws Exception {
/*    if (iter_ == null) {
      Session session = getJCRSession(username_);
      if (isQuery_) {
        QueryManager qm = session.getWorkspace().getQueryManager();
        Query query = qm.createQuery(value_, Query.XPATH);
        QueryResult result = query.execute();
        iter_ = result.getNodes();
      } else {
        Node node = (Node) session.getItem(value_);
        iter_ = node.getNodes();
      }
      session.logout();
    }*/    

    long pageSize = getPageSize() ;
    Session session = getJCRSession(username_) ;    
    try {
      QueryImpl queryImpl = createXPathQuery(session, username_, value_);
      //queryImpl.setLimit(pageSize);
      QueryResult result = queryImpl.execute();
      iter_ = result.getNodes();
    } finally {
      session.logout() ;
    }
    NodeIterator inter = iter_;
    Map<String, String> emails = new LinkedHashMap<String, String>();
    while (inter.hasNext()) {
      Node contactNode = inter.nextNode();
      String email = null;
      String fullName = null;
      try {
        email = contactNode.getProperty("exo:emailAddress").getString();
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

  private Session getJCRSession(String username) throws Exception {
    RepositoryService repositoryService = (RepositoryService) PortalContainer.getComponent(RepositoryService.class);
    SessionProvider sessionProvider = SessionProvider.createSystemProvider();
    String defaultWS = repositoryService.getDefaultRepository()
                                        .getConfiguration()
                                        .getDefaultWorkspaceName();
    return sessionProvider.getSession(defaultWS, repositoryService.getCurrentRepository());
  }
  
  private QueryImpl createXPathQuery(Session session, String username, String xpath) throws Exception {
    QueryManager queryManager = session.getWorkspace().getQueryManager();
    return (QueryImpl) queryManager.createQuery(xpath, Query.XPATH);
  }
}
