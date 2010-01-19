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
package org.exoplatform.contact.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.ReferentialIntegrityException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.organization.MembershipEventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Nov 23, 2007 3:09:21 PM
 */
public class NewMembershipListener extends MembershipEventListener {
  private ContactService cservice_ ;
  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  private RepositoryService reposervice_ ;
  public NewMembershipListener(ContactService cservice, NodeHierarchyCreator nodeHierarchyCreator, RepositoryService rservice) throws Exception {
    cservice_ = cservice ;
    nodeHierarchyCreator_ = nodeHierarchyCreator ;
    reposervice_ = rservice ;
  }

  public void postSave(Membership m, boolean isNew) throws Exception {
    
    String username = m.getUserName();
    String groupId = m.getGroupId();
    cservice_.addUserContactInAddressBook(username, groupId) ;
    DataStorage storage_ = new JCRDataStorage(nodeHierarchyCreator_, reposervice_) ;
    SessionProvider systemSession = SessionProvider.createSystemProvider() ;
    try {
      String usersPath = nodeHierarchyCreator_.getJcrPath(DataStorage.USERS_PATH) ;
      Contact contact = cservice_.getPublicContact(username) ;
      QueryManager qm = getSession(systemSession).getWorkspace().getQueryManager();
      Map<String, String> groups = new LinkedHashMap<String, String>() ;
      for (String group  : contact.getAddressBookIds()) groups.put(group, group) ;
      groups.put(groupId, groupId) ;
      contact.setAddressBookIds(groups.keySet().toArray(new String[] {})) ;
      cservice_.saveContact(username, contact, false) ;
      StringBuffer queryString = new StringBuffer("/jcr:root" + usersPath 
          + "//element(*,exo:contactGroup)[@exo:viewPermissionGroups='").append( groupId + "']") ;        
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator nodes = result.getNodes() ;
      List<String> to = Arrays.asList(new String []{username});
      while (nodes.hasNext()) {
        Node address = nodes.nextNode() ;
        String from = address.getProperty("exo:sharedUserId").getString();
        String addressBookId = address.getProperty("exo:id").getString();
        storage_.shareAddressBook(from, addressBookId, to) ;
      }
      queryString = new StringBuffer("/jcr:root" + usersPath 
          + "//element(*,exo:contact)[@exo:viewPermissionGroups='").append(groupId + "']") ;        
      query = qm.createQuery(queryString.toString(), Query.XPATH);
      result = query.execute();
      nodes = result.getNodes() ;
      while (nodes.hasNext()) {
        Node contactNode = nodes.nextNode() ;
        String split = "/" ;
        String temp = contactNode.getPath().split(usersPath)[1] ;
        String userId = temp.split(split)[1] ;
        String [] addressBookIds = new String[] {contactNode.getProperty("exo:id").getString()};
        storage_.shareContact(userId, addressBookIds, to) ;
      }
    } catch (Exception e) {
      e.printStackTrace() ;
    }  finally {
      systemSession.close() ;
    }
  }

  public void preDelete(Membership m) throws Exception {
    Contact contact = cservice_.getPublicContact(m.getUserName()) ;
    Map<String, String> groupIds = new LinkedHashMap<String, String>() ;
    for (String group  : contact.getAddressBookIds()) groupIds.put(group, group) ;
    groupIds.remove(m.getGroupId()) ;
    contact.setAddressBookIds(groupIds.keySet().toArray(new String[] {})) ;
    SessionProvider systemSession = SessionProvider.createSystemProvider();
    try {
      cservice_.saveContact(m.getUserName(), contact, false) ;
      DataStorage storage_ = new JCRDataStorage(nodeHierarchyCreator_, reposervice_) ;
      String usersPath = nodeHierarchyCreator_.getJcrPath(DataStorage.USERS_PATH) ;
      QueryManager qm = getSession(systemSession).getWorkspace().getQueryManager();
      StringBuffer queryString = new StringBuffer("/jcr:root" + usersPath 
          + "//element(*,exo:contactGroup)[@exo:viewPermissionGroups='").append( m.getGroupId() + "']") ;        
      Query query = qm.createQuery(queryString.toString(), Query.XPATH);
      QueryResult result = query.execute();
      NodeIterator nodes = result.getNodes() ;
      while (nodes.hasNext()) {
        Node address = nodes.nextNode() ;
        storage_.unshareAddressBook(
            address.getProperty("exo:sharedUserId")
            .getString(), address.getProperty("exo:id").getString(), m.getUserName()) ;
        for (Value groupShared : address.getProperty("exo:viewPermissionGroups").getValues()) {
          if (groupIds.keySet().contains(groupShared.getString())) {
            List<String> reciever = new ArrayList<String>() ;
            reciever.add(m.getUserName()) ;
            storage_.shareAddressBook(address.getProperty("exo:sharedUserId")
                .getString(), address.getProperty("exo:id").getString(), reciever) ;
          }
        }
      }
      queryString = new StringBuffer("/jcr:root" + usersPath 
          + "//element(*,exo:contact)[@exo:viewPermissionGroups='").append(m.getGroupId() + "']") ;        
      query = qm.createQuery(queryString.toString(), Query.XPATH);
      result = query.execute();
      nodes = result.getNodes() ;
      while (nodes.hasNext()) {
        Node contactNode = nodes.nextNode() ;
        String split = "/" ;
        String temp = contactNode.getPath().split(usersPath)[1] ;
        String userId = temp.split(split)[1] ;
        storage_.removeUserShareContact(userId, contactNode.getProperty("exo:id").getString(), m.getUserName()) ;
        for (Value groupShared : contactNode.getProperty("exo:viewPermissionGroups").getValues()) {
          if (groupIds.keySet().contains(groupShared.getString())) {
            List<String> reciever = new ArrayList<String>() ;
            reciever.add(m.getUserName()) ;
            storage_.shareContact(userId, new String [] {contactNode.getProperty("exo:id").getString()}, reciever) ;
          }
        }        
      }      
    } catch (ReferentialIntegrityException e) {
      
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      systemSession.close() ;
    }
  }
  
  private Session getSession(SessionProvider sprovider) throws Exception{
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    RepositoryService repositoryService = (RepositoryService) container.getComponentInstanceOfType(RepositoryService.class);
    ManageableRepository currentRepo = repositoryService.getCurrentRepository() ;
    return sprovider.getSession(currentRepo.getConfiguration().getDefaultWorkspaceName(), currentRepo) ;
  }
  
}