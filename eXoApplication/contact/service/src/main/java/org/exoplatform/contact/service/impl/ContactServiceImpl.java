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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.ContactImportExport;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.service.GroupContactData;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.UserProfile;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class ContactServiceImpl implements ContactService {
  
  final private static String VCARD = "x-vcard".intern() ;
  
  private DataStorage storage_ ;
  private Map<String, ContactImportExport> contactImportExport_ = new HashMap<String, ContactImportExport>() ;
  
  private boolean userCanSeeAllGroupAddressBooks = false;
  
  private List<String> nonPublicGroups = new ArrayList<String>();
  
  private static final String USERCANSEEALLGROUPADDRESSBOOKS = "UserCanSeeAllGroupAddressBooks".intern();
  
  private static final String NONPUBLICGROUPS = "NonPublicGroups".intern();
  
  private static final String TRUE = "true".intern();
  
  public ContactServiceImpl(NodeHierarchyCreator nodeHierarchyCreator, RepositoryService rservice, InitParams initParams) throws Exception {
      storage_ = new JCRDataStorage(nodeHierarchyCreator, rservice) ;
      contactImportExport_.put(VCARD, new VCardImportExport(storage_)) ;
      if(initParams != null && initParams.getValuesParam(USERCANSEEALLGROUPADDRESSBOOKS) != null){
        List values = initParams.getValuesParam(USERCANSEEALLGROUPADDRESSBOOKS).getValues();
        if(TRUE.equalsIgnoreCase(values.get(0).toString()))
          userCanSeeAllGroupAddressBooks = true;
        if(userCanSeeAllGroupAddressBooks && initParams.getValuesParam(NONPUBLICGROUPS) != null){
          values = initParams.getValuesParam(NONPUBLICGROUPS).getValues();
          for (Object object : values) {
            nonPublicGroups.add(object.toString());
          }
        }
      }
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getPublicAddressBookIdsOfUser(String user) throws Exception {
    OrganizationService organizationService = 
      (OrganizationService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class) ;
    Object[] objGroupIds = organizationService.getGroupHandler().findGroupsOfUser(user).toArray() ;
    List<String> groupIds = new ArrayList<String>() ;
    for (Object object : objGroupIds) {
      groupIds.add(((Group)object).getId()) ;
    }
    if(userCanSeeAllGroupAddressBooks)
      groupIds.removeAll(nonPublicGroups);
    return groupIds ;
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getAllsPublicAddressBookIds(String user) throws Exception {
    List<String> publicGroupIds = new ArrayList<String>() ;
    if(userCanSeeAllGroupAddressBooks){
      OrganizationService organizationService = 
        (OrganizationService)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class) ;
      Object[] objPublicGroupIds = organizationService.getGroupHandler().getAllGroups().toArray() ;
      for (Object object : objPublicGroupIds) {
        publicGroupIds.add(((Group)object).getId()) ;
      }
      publicGroupIds.removeAll(nonPublicGroups);
    } else {
      publicGroupIds = getPublicAddressBookIdsOfUser(user);
    }
    return publicGroupIds;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Contact> getPersonalContacts(String username) throws Exception {
    return storage_.findAllContactsByOwner(username);
  }

  /**
   * {@inheritDoc}
   */  
  public Map<String, String> searchEmails(String username, ContactFilter filter) throws Exception {
    return storage_.findEmailsByFilter(username, filter) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public ContactPageList getPersonalContactsByAddressBook(String ownerId, String addressBookId) throws Exception {
    return storage_.findPersonalContactsByAddressBook(ownerId, addressBookId);
  }
 

  /**
   * {@inheritDoc}
   */
  public ContactPageList getPersonalContactsByFilter(String username, ContactFilter filter) throws Exception {
    return storage_.findContactsByFilter(username, filter, DataStorage.PERSONAL) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public ContactPageList getSharedContactsByFilter(String username, ContactFilter filter) throws Exception {
    return storage_.findContactsByFilter(username, filter, DataStorage.SHARED) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public ContactPageList getPublicContactsByFilter(String username, ContactFilter filter) throws Exception {
    return storage_.findContactsByFilter(username, filter, DataStorage.PUBLIC) ;
  }  
  
  /**
   * {@inheritDoc}
   */
  public List<String> getEmailsByAddressBook(String username, String addressBookId) throws Exception {
    return storage_.findEmailsInPersonalAddressBook(username, addressBookId);
  }
  

  
  public Contact getContact(String username, String contactId) throws Exception {
    return storage_.loadPersonalContact(username, contactId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void saveContact(String username, Contact contact, boolean isNew) throws Exception {
    storage_.saveContact(username, contact, isNew);    
  }

  /**
   * {@inheritDoc}
   */
  public List<Contact> removeContacts(String username, List<String> contactIds) throws Exception {
    return storage_.removeContacts(username, contactIds);
  }
  
  public void moveContacts(SessionProvider sysProvider, String username, List<Contact> contacts, String addressType ) throws Exception {
    moveContacts(username, contacts, addressType);
  }

  public void moveContacts(String username, List<Contact> contacts, String addressType ) throws Exception {
    storage_.moveContacts(username, contacts, addressType) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public void addUserContactInAddressBook(String userId, String addressBookId) throws Exception {
  	storage_.addUserContactInAddressBook(userId, addressBookId) ;
  }
  
  public List<AddressBook> getGroups(SessionProvider sProvider, String username) throws Exception {
    return getGroups(username);
  }

  public List<AddressBook> getGroups(String username) throws Exception {
    return storage_.findPersonalAddressBooksByOwner(username);
  }
  
  /**
   * {@inheritDoc}
   */
  public AddressBook getPersonalAddressBook(String username, String addressBookId) throws Exception {
    return storage_.loadPersonalAddressBook(username, addressBookId);
  }
  

  
  /**
   * {@inheritDoc}
   */  
  public void saveAddressBook(String username, AddressBook group, boolean isNew) throws Exception {
    storage_.savePersonalOrSharedAddressBook(username, group, isNew);    
  } 
  
  /**
   * {@inheritDoc}
   */  
  public AddressBook removeAddressBook(String username, String addressBookId) throws Exception {
    // step 1 : remove content
    storage_.clearAddressBook(username, addressBookId);
    
    // step 2 : remove address book
    AddressBook removed =  storage_.removePersonalAddressBook(username, addressBookId);
    
    return removed;
  }
  
  
  public void removeUserShareContact(SessionProvider sProvider, String username, String contactId, String removedUser) throws Exception {
    removeUserShareContact(username, contactId, removedUser);
  }

  public void removeUserShareContact(String username, String contactId, String removedUser) throws Exception {
    storage_.removeUserShareContact(username, contactId, removedUser) ;
  }
  
  public void shareContact(SessionProvider sProvider, String username, String[] contactIds, List<String> receiveUsers) throws Exception {
    shareContact(username, contactIds, receiveUsers);
  }

  public void shareContact(String username, String[] contactIds, List<String> receiveUsers) throws Exception {
  	storage_.shareContact(username, contactIds, receiveUsers) ;
  }
  public DataPageList getSharedContacts(String username) throws Exception {
  	return storage_.getSharedContacts(username) ;
  }
  public void shareAddressBook(String username, String addressBookId, List<String> receiverUsers) throws Exception {
  	storage_.shareAddressBook(username, addressBookId, receiverUsers) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public void unshareAddressBook(String owner,
                                 String addressBookId,
                                 String unsharedUser) throws Exception {
    storage_.unshareAddressBook(owner, addressBookId, unsharedUser) ;
  }

  /**
   * {@inheritDoc}
   */
  public List<SharedAddressBook> getSharedAddressBooks(String username) throws Exception {
    return storage_.findSharedAddressBooksByUser(username) ;
  }
  
  public ContactPageList getSharedContactsByAddressBook(SessionProvider sProvider, String username, SharedAddressBook addressBook) throws Exception {
    return getSharedContactsByAddressBook(username, addressBook);
  }
  public ContactPageList getSharedContactsByAddressBook(String username, SharedAddressBook addressBook) throws Exception {
  	return storage_.getSharedContactsByAddressBook(username, addressBook) ;
  }
  public void removeSharedContact(SessionProvider sProvider, String username, String addressBookId, String contactId) throws Exception {
    removeSharedContact(username, addressBookId, contactId);
  }

  public void removeSharedContact(String username, String addressBookId, String contactId) throws Exception { 
    storage_.removeSharedContact(username, addressBookId, contactId) ;
  }
  
  public void saveContactToSharedAddressBook(String username, String addressBookId, Contact contact, boolean isNew) throws Exception {
  	storage_.saveContactToSharedAddressBook(username, addressBookId, contact, isNew) ;
  }
  
  public Contact getSharedContactAddressBook(String username, String contactId) throws Exception {
    return storage_.getSharedContactAddressBook(username, contactId) ;
  }
  
  public void saveSharedContact(String username, Contact contact) throws Exception  {
    storage_.saveSharedContact(username, contact) ;
  }
  
  public Contact getSharedContact(SessionProvider sProvider, String username, String contactId) throws Exception {
    return getSharedContact(username, contactId);
  }

  public Contact getSharedContact(String username, String contactId) throws Exception {
    return storage_.getSharedContact(username, contactId) ;
  }
  
  public Contact getPublicContact(String contactId) throws Exception {
    return storage_.loadPublicContactByUser(contactId);
  }
  

  public List<GroupContactData> getPublicContacts(SessionProvider sProvider, String[] groupIds) throws Exception {
    return storage_.getPublicContacts(sProvider, groupIds);
  }

  public Tag getTag(SessionProvider sProvider, String username, String tagName) throws Exception {
    return getTag(username, tagName);
  }

  public Tag getTag(String username, String tagName) throws Exception {
    return storage_.getTag(username, tagName) ;
  }
  
  public List<Tag> getTags(SessionProvider sProvider, String username) throws Exception {
    return getTags(username);
  }

  public List<Tag> getTags(String username) throws Exception {
    return storage_.getTags(username);
  }
  public DataPageList getContactPageListByTag(SessionProvider sProvider, String username, String tagName) throws Exception {
    return getContactPageListByTag(username, tagName);
  }

  public DataPageList getContactPageListByTag(String username, String tagName) throws Exception {
    return storage_.getContactPageListByTag(username, tagName);
  }
  
  public void addTag(SessionProvider sProvider, String username, List<String> contactIds, List<Tag> tags) throws Exception {
    addTag(username, contactIds, tags);
  }

  public void addTag(String username, List<String> contactIds, List<Tag> tags) throws Exception {
    storage_.addTag(username, contactIds, tags);
  }
  
  public void addTag(SessionProvider sProvider, String username, List<String> contactIds, String tagId) throws Exception {
    addTag(username, contactIds, tagId);
  }

  public void addTag(String username, List<String> contactIds, String tagId) throws Exception {
	  storage_.addTag(username, contactIds, tagId);
  }
  
  public Tag removeTag(SessionProvider sProvider, String username, String tagName) throws Exception {
    return removeTag(username, tagName);
  }

  public Tag removeTag(String username, String tagName) throws Exception {
    return storage_.removeTag(username, tagName);
  }
  
  public void updateTag(SessionProvider sProvider, String username,Tag tag) throws Exception {
    updateTag(username, tag);
  }

  public void updateTag(String username, Tag tag) throws Exception {
    storage_.updateTag(username, tag) ;
  }
  
  public void removeContactTag(SessionProvider sProvider, String username, List<String> contactIds, List<String> tags) throws Exception {
    removeContactTag(username, contactIds, tags);
  }

  public void removeContactTag(String username, List<String> contactIds, List<String> tags) throws Exception {
    storage_.removeContactTag(username, contactIds, tags) ;
  }
  
  public ContactPageList getPublicContactsByAddressBook(SessionProvider sProvider, String groupId) throws Exception {
    return getPublicContactsByAddressBook(groupId);
  }

  public ContactPageList getPublicContactsByAddressBook(String groupId) throws Exception {
    return storage_.getPublicContactsByAddressBook(groupId) ;
  }
  
  public void pasteAddressBook(SessionProvider sProvider, String username, String srcAddress, String srcType, String destAddress, String destType) throws Exception {
    pasteAddressBook(username, srcAddress, srcType, destAddress, destType);
  }

  public void pasteAddressBook(String username, String srcAddress, String srcType, String destAddress, String destType) throws Exception {
  	storage_.pasteAddressBook(username, srcAddress, srcType, destAddress, destType) ;
  }
  
  public List<Contact> pasteContacts(SessionProvider sProvider, String username, String destAddress, String destType, Map<String, String> contactsMap) throws Exception {
    return pasteContacts(username, destAddress, destType, contactsMap);
  }

  public List<Contact> pasteContacts(String username, String destAddress, String destType, Map<String, String> contactsMap) throws Exception {
    return storage_.pasteContacts(username, destAddress, destType, contactsMap) ;
  }
  
  public ContactImportExport getContactImportExports(String type) {
    return contactImportExport_.get(type) ;
  }
  
  public String[] getImportExportType() throws Exception {
    return contactImportExport_.keySet().toArray(new String[]{}) ;
  }

  public DataPageList searchContact(SessionProvider sProvider, String username, ContactFilter filter) throws Exception {
    return searchContact(username, filter);
  }
  
  public DataPageList searchContact(String username, ContactFilter filter) throws Exception {
    return storage_.searchContact(username, filter) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public AddressBook getSharedAddressBook(String username, String addressBookId) throws Exception {
    return storage_.getSharedAddressBookById(username, addressBookId) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception {
    return storage_.getAllEmailBySharedGroup(username, addressBookId) ;
  }
  
  /**
   * {@inheritDoc}
   */
  public List<String> getAllEmailByPublicGroup(String username, String groupId) throws Exception { 
    return storage_.findEmailsInPublicAddressBook(username, groupId) ;
  }

  
  public void registerNewUser(User user, boolean isNew) throws Exception {
    storage_.registerNewUser(user, isNew) ;
  }
   
  @SuppressWarnings("deprecation")
  public void updateProfile(UserProfile userProfile) throws Exception {
    Contact contact = storage_.loadPublicContactByUser(userProfile.getUserName()) ;
    if (contact == null) return; 
    contact.setNickName(userProfile.getAttribute("user.name.nickName"));
    try {
      Date date = new Date(userProfile.getAttribute("user.bdate"));
      contact.setBirthday(date);
    } catch (Exception e) { }
    contact.setGender(userProfile.getAttribute("user.gender"));
    
    StringBuilder builderNote = new StringBuilder();
    if (!Utils.isEmpty(userProfile.getAttribute("user.employer"))) {
      builderNote.append(userProfile.getAttribute("user.employer"));
    }
    if (!Utils.isEmpty(userProfile.getAttribute("user.department"))) {
      if (builderNote.length() == 0) builderNote.append(userProfile.getAttribute("user.department"));
      else builderNote.append("\n" + userProfile.getAttribute("user.department"));
    }
    if (!Utils.isEmpty(userProfile.getAttribute("user.language"))) {
      if (builderNote.length() == 0) builderNote.append(userProfile.getAttribute("user.language"));
      else builderNote.append("\n" + userProfile.getAttribute("user.language"));
    }
    contact.setNote(builderNote.toString());
    contact.setJobTitle(userProfile.getAttribute("user.jobtitle"));
    
    StringBuilder builderHomeAddress = new StringBuilder();
    if (!Utils.isEmpty(userProfile.getAttribute("user.home-info.postal.name"))) {
      builderHomeAddress.append(userProfile.getAttribute("user.home-info.postal.name"));
    }
    if (!Utils.isEmpty(userProfile.getAttribute("user.home-info.postal.street"))) {
      if (builderHomeAddress.length() == 0) builderHomeAddress.append(userProfile.getAttribute("user.home-info.postal.street"));
      else builderHomeAddress.append(" " + userProfile.getAttribute("user.home-info.postal.street"));
    }
    contact.setHomeAddress(builderHomeAddress.toString());

    contact.setWorkAddress(userProfile.getAttribute("user.business-info.postal.name"));
    contact.setWorkCity(userProfile.getAttribute("user.business-info.postal.city"));
    contact.setWorkStateProvince(userProfile.getAttribute("user.business-info.postal.stateprov"));
    contact.setWorkPostalCode(userProfile.getAttribute("user.business-info.postal.postalcode"));
    contact.setWorkCountry(userProfile.getAttribute("user.business-info.postal.country"));
    contact.setWorkPhone1(userProfile.getAttribute("user.business-info.telecom.telephone.number"));
    contact.setWorkPhone2(userProfile.getAttribute("user.business-info.telecom.mobile.number"));
    contact.setWebPage(userProfile.getAttribute("user.business-info.online.uri"));

    contact.setHomeCity(userProfile.getAttribute("user.home-info.postal.city"));
    contact.setHomeState_province(userProfile.getAttribute("user.home-info.postal.stateprov"));
    contact.setHomePostalCode(userProfile.getAttribute("user.home-info.postal.postalcode"));
    contact.setHomeCountry(userProfile.getAttribute("user.home-info.postal.country"));
    contact.setHomePhone1(userProfile.getAttribute("user.home-info.telecom.telephone.number"));
    contact.setHomePhone2(userProfile.getAttribute("user.home-info.telecom.mobile.number"));
    contact.setPersonalSite(userProfile.getAttribute("user.home-info.online.uri"));
    
    ExoContainer container = ExoContainerContext.getCurrentContainer();
    OrganizationService organizationService = 
      (OrganizationService)container.getComponentInstanceOfType(OrganizationService.class) ;
    String email = organizationService.getUserHandler().findUserByName(userProfile.getUserName()).getEmail();
    StringBuilder builderEmailAddress = new StringBuilder();
    if (!Utils.isEmpty(email)) {
      builderEmailAddress.append(email);
    }
    if (!Utils.isEmpty(userProfile.getAttribute("user.home-info.online.email"))) {
      if (builderEmailAddress.length() == 0) builderEmailAddress.append(userProfile.getAttribute("user.home-info.online.email"));
      else builderEmailAddress.append("," + userProfile.getAttribute("user.home-info.online.email"));
    }
    if (!Utils.isEmpty(userProfile.getAttribute("user.business-info.online.email"))) {
      if (builderEmailAddress.length() == 0) builderEmailAddress.append(userProfile.getAttribute("user.business-info.online.email"));
      else builderEmailAddress.append("," + userProfile.getAttribute("user.business-info.online.email"));
    }
    contact.setEmailAddress(builderEmailAddress.toString());

    Calendar cal = new GregorianCalendar() ;
    contact.setLastUpdated(cal.getTime()) ;
    SessionProvider sysProvider = storage_.createSystemProvider() ;
    try {
      saveContact(userProfile.getUserName(), contact, false);
    } finally {
      storage_.closeSessionProvider(sysProvider);
    }    
  }
  
  ////// LEGACY API //////
  
  /**
   * {@inheritDoc}
   */
  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return getPersonalContactsByAddressBook(username, groupId);
  }
  
  /**
   * {@inheritDoc}
   */
  public void saveGroup(SessionProvider sProvider, String username, AddressBook group, boolean isNew) throws Exception {
    saveAddressBook(username,group, isNew);    
  }
  
  /**
   * {@inheritDoc}
   */
  public AddressBook getGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return getPersonalAddressBook(username, groupId);
  }
  
  /**
   * {@inheritDoc}
   */
  public AddressBook removeGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return removeAddressBook(username, groupId);
  }
    
  /**
   * {@inheritDoc}
   */
  public AddressBook getSharedGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return getSharedAddressBook(username, groupId);
  }  
  
  
  /**
   * {@inheritDoc}
   */
  public List<SharedAddressBook> getSharedAddressBooks(SessionProvider sProvider, String username) throws Exception  {
   return getSharedAddressBooks(username);
  }  
  
  /**
   * {@inheritDoc}
   */
  public void removeUserShareAddressBook(SessionProvider sProvider, String username, String addressBookId, String removedUser) throws Exception {
    unshareAddressBook(username, addressBookId,removedUser);
  }  
    
  /**
   * {@inheritDoc}
   */
  public void saveContact(SessionProvider sProvider, String username, Contact contact, boolean isNew) throws Exception {
    storage_.saveContact(username, contact, isNew);    
  } 
  
  /**
   * {@inheritDoc}
   */
  public List<Contact> getAllContacts(SessionProvider sProvider, String username) throws Exception {
    return getPersonalContacts(username);
  }

  /**
   * {@inheritDoc}
   */
  public List<String> getAllEmailAddressByGroup(SessionProvider sProvider, String username, String groupId) throws Exception {
    return getEmailsByAddressBook(username, groupId);
  } 
  
  /**
   * {@inheritDoc}
   */
  public void addGroupToPersonalContact(String userId, String groupId) throws Exception {
    addUserContactInAddressBook(userId, groupId);
  }
  
  /**
   * {@inheritDoc}
   */
  public Contact getPersonalContact(String userId) throws Exception {
    return getPublicContact(userId) ;
  }  
  
  /**
   * {@inheritDoc}
   */  
  public Map<String, String> searchEmails(SessionProvider sessionProvider,  String username, ContactFilter filter)throws Exception {
    return searchEmails(username, filter) ;
  }
    
  /**
   * {@inheritDoc}
   */
  public ContactPageList getContactPageListByGroup(SessionProvider sProvider, String username, ContactFilter filter, String type) throws Exception {
    return storage_.findContactsByFilter(username, filter, type);
  }
  
  /**
   * {@inheritDoc}
   */
  public List<Contact> removeContacts(SessionProvider sProvider, String username, List<String> contactIds) throws Exception {
    return removeContacts(username, contactIds);
  }
  
  public void saveAddress(String username, String emailAddress) throws Exception {
    storage_.saveAddress(username, emailAddress) ;
  }
  
}
