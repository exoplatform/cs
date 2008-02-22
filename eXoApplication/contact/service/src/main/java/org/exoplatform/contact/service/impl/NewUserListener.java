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
import java.util.List;

import javax.jcr.Node;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.services.jcr.access.AccessControlEntry;
import org.exoplatform.services.jcr.access.PermissionType;
import org.exoplatform.services.jcr.core.ExtendedNode;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserEventListener;

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
  public NewUserListener(ContactService cservice, NodeHierarchyCreator nodeHierarchyCreator) throws Exception {
  	cservice_ = cservice ;
  	nodeHierarchyCreator_ = nodeHierarchyCreator ;  	
  }
  
  public void postSave(User user, boolean isNew) throws Exception {  	
  	if(isNew) {
  		ContactGroup group = new ContactGroup() ;
  		group.setId(DEFAULTGROUP) ;
    	group.setName("My contacts") ;
    	group.setDescription("Default address book") ;
    	SessionProvider sysProvider = SessionProvider.createSystemProvider() ;
    	cservice_.saveGroup(sysProvider, user.getUserName(), group, true) ;
    	Contact contact = new Contact() ;
    	contact.setId(user.getUserName()) ;
    	contact.setFullName(user.getFirstName() + " " + user.getLastName()) ;
    	contact.setFirstName(user.getFirstName()) ;
    	contact.setLastName(user.getLastName()) ;
    	contact.setEmailAddress(user.getEmail()) ;
    	Calendar cal = new GregorianCalendar() ;
    	contact.setLastUpdated(cal.getTime()) ;
    	List<String> ls = new ArrayList<String>()  ;
    	ls.add(group.getId()) ;
    	
    	contact.setAddressBook(ls.toArray(new String[]{})) ;
    	contact.setOwner(true) ;
    	contact.setOwnerId(user.getUserName()) ;
    	cservice_.saveContact(sysProvider, user.getUserName(), contact, true) ;
    	
      Node userApp = nodeHierarchyCreator_.getUserApplicationNode(SessionProvider.createSystemProvider(), user.getUserName()) ;
      //reparePermissions(userApp, user.getUserName()) ;
      //reparePermissions(userApp.getNode("ContactApplication"), user.getUserName()) ;
      //reparePermissions(userApp.getNode("ContactApplication/contactGroup"), user.getUserName()) ;
      //reparePermissions(userApp.getNode("ContactApplication/contactGroup/" + group.getId()), user.getUserName()) ;
      userApp.getSession().save() ;   
      
      sysProvider.close();
  	}
  }
  
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