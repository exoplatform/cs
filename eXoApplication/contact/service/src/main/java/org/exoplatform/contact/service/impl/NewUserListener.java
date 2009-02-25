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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;
import org.exoplatform.services.organization.impl.GroupImpl;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Nov 23, 2007 3:09:21 PM
 */
public class NewUserListener extends UserEventListener {
  private ContactService cservice_ ;
  private NodeHierarchyCreator nodeHierarchyCreator_ ;
  public static String DEFAULTGROUP = "default".intern() ;
  public static String DEFAULTGROUPNAME = "My contacts".intern() ;
  public static String DEFAULTGROUPDES = "Default address book".intern() ;
  public NewUserListener(ContactService cservice, NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
    cservice_ = cservice ;
    nodeHierarchyCreator_ = nodeHierarchyCreator ;  	
  }

  public void postSave(User user, boolean isNew) throws Exception { 
    Contact contact = null ;
    if (isNew) contact = new Contact() ;
    else contact = cservice_.getPublicContact(user.getUserName()) ;
    if (contact != null) {
      contact.setFullName(user.getFirstName() + " " + user.getLastName()) ;
      contact.setFirstName(user.getFirstName()) ;
      contact.setLastName(user.getLastName()) ;
      contact.setEmailAddress(user.getEmail()) ;    
      Calendar cal = new GregorianCalendar() ;
      contact.setLastUpdated(cal.getTime()) ;
    }
    SessionProvider sysProvider = SessionProvider.createSystemProvider() ;
    try {
      if(isNew) {
        AddressBook group = new AddressBook() ;
        group.setId(DEFAULTGROUP+user.getUserName()) ;
        group.setName(DEFAULTGROUPNAME) ;
        group.setDescription(DEFAULTGROUPDES) ;
        cservice_.saveAddressBook(user.getUserName(), group, true) ;

        contact.setId(user.getUserName()) ;
        Map<String, String> groupIds = new LinkedHashMap<String, String>() ;
        groupIds.put(group.getId(), group.getId()) ;      
        OrganizationService organizationService = 
          (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
        Object[] objGroupIds = organizationService.getGroupHandler().findGroupsOfUser(user.getUserName()).toArray() ;
        for (Object object : objGroupIds) {
          String id = ((GroupImpl)object).getId() ;
          groupIds.put(id, id) ;
        }
        contact.setAddressBook(groupIds.keySet().toArray(new String[] {})) ;
        contact.setOwner(true) ;
        contact.setOwnerId(user.getUserName()) ;
        cservice_.saveContact(sysProvider, user.getUserName(), contact, true) ;


        // added 23-4
        JCRDataStorage storage_ = new JCRDataStorage(nodeHierarchyCreator_) ;
        Node publicContactHome = storage_.getPublicContactHome(sysProvider) ;      
        String usersPath = nodeHierarchyCreator_.getJcrPath(JCRDataStorage.USERS_PATH) ;
        QueryManager qm = publicContactHome.getSession().getWorkspace().getQueryManager();
        List<String> recievedUser = new ArrayList<String>() ;
        recievedUser.add(user.getUserName()) ;

        for (Object object : objGroupIds) {  
          String groupId = ((GroupImpl)object).getId() ;
          StringBuffer queryString = new StringBuffer("/jcr:root" + usersPath 
              + "//element(*,exo:contactGroup)[@exo:viewPermissionGroups='").append(groupId + "']") ;        
          Query query = qm.createQuery(queryString.toString(), Query.XPATH);
          QueryResult result = query.execute();
          NodeIterator nodes = result.getNodes() ;
          while (nodes.hasNext()) {
            Node address = nodes.nextNode() ;
            storage_.shareAddressBook(address.getProperty("exo:sharedUserId")
                .getString(), address.getProperty("exo:id").getString(), recievedUser) ;
          }

          // lookup shared contacts
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
            storage_.shareContact(sysProvider, userId,
                new String[] {contactNode.getProperty("exo:id").getString()}, recievedUser) ;
          }
        }
        Node userApp = nodeHierarchyCreator_.getUserApplicationNode(sysProvider, user.getUserName()) ;
        //reparePermissions(userApp, user.getUserName()) ;
        //reparePermissions(userApp.getNode("ContactApplication"), user.getUserName()) ;
        //reparePermissions(userApp.getNode("ContactApplication/contactGroup"), user.getUserName()) ;
        //reparePermissions(userApp.getNode("ContactApplication/contactGroup/" + group.getId()), user.getUserName()) ;
        userApp.getSession().save() ;   
      } else {
        if (contact != null) {
          cservice_.saveContact(sysProvider, user.getUserName(), contact, false) ; 
        }
      }
    } catch (Exception e) {
      e.printStackTrace() ;
    } finally {
      sysProvider.close();
    }
  }


  @SuppressWarnings("unused")
  private void reparePermissions(Node node, String owner) throws Exception {
    ExtendedNode extNode = (ExtendedNode)node ;
    if (extNode.canAddMixin("exo:privilegeable")) extNode.addMixin("exo:privilegeable");
    String[] arrayPers = {PermissionType.READ, PermissionType.ADD_NODE, PermissionType.SET_PROPERTY, PermissionType.REMOVE} ;
    extNode.setPermission(owner, arrayPers) ;
    List<AccessControlEntry> permsList = extNode.getACL().getPermissionEntries() ;    
    for(AccessControlEntry accessControlEntry : permsList) {
      extNode.setPermission(accessControlEntry.getIdentity(), arrayPers) ;      
    } 
    extNode.removePermission("any") ;

  }
  public void preDelete(User user) throws Exception {

  }
}