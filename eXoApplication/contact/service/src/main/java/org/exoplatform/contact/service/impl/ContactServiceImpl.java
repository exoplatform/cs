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

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactImportExport;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.service.DataStorage;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.ExoContainerContext;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.organization.UserProfile;
import org.exoplatform.services.security.ConversationState;
import org.exoplatform.services.security.Identity;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class ContactServiceImpl implements ContactService {

  final private static String              VCARD                          = "x-vcard".intern();

  private DataStorage                      storage_;

  private Map<String, ContactImportExport> contactImportExport_           = new HashMap<String, ContactImportExport>();

  public boolean                           userCanSeeAllGroupAddressBooks = false;

  public List<String>                      nonPublicGroups                = new ArrayList<String>();

  private static final String              USERCANSEEALLGROUPADDRESSBOOKS = "UserCanSeeAllGroupAddressBooks".intern();

  private static final String              NONPUBLICGROUPS                = "NonPublicGroups".intern();

  private static final String              TRUE                           = "true".intern();

  private List<ContactEventListener>       listeners_                     = new ArrayList<ContactEventListener>(3);

  @SuppressWarnings("unchecked")
  public ContactServiceImpl(NodeHierarchyCreator nodeHierarchyCreator, RepositoryService rservice, InitParams initParams) throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator, rservice);
    contactImportExport_.put(VCARD, new VCardImportExport(storage_));
    if (initParams != null) {
      if (initParams.getValuesParam(USERCANSEEALLGROUPADDRESSBOOKS) != null) {
        List values = initParams.getValuesParam(USERCANSEEALLGROUPADDRESSBOOKS).getValues();
        if (TRUE.equalsIgnoreCase(values.get(0).toString())) {
          userCanSeeAllGroupAddressBooks = true;
        }
      }
      if (initParams.getValuesParam(NONPUBLICGROUPS) != null) {
        List values = initParams.getValuesParam(NONPUBLICGROUPS).getValues();
        for (Object object : values) {
          nonPublicGroups.add(object.toString());
        }
      }
    }
  }

  private List<String> excludeWildCardMatchs(List<String> sourceList, List<String> wildCards) throws Exception {
    List<String> groupIds = new ArrayList<String>();
    if (sourceList != null && !sourceList.isEmpty()) {
      for (String object : sourceList) {
        groupIds.add(object);
      }
    }
    if (wildCards == null || wildCards.isEmpty() || sourceList == null || sourceList.isEmpty()) {
      return groupIds;
    }
    for (String wildCard : wildCards) {
      for (String s : sourceList) {
        if (FilenameUtils.wildcardMatch(s, wildCard, IOCase.INSENSITIVE)) {
          groupIds.remove(s);
        }
      }
    }
    return groupIds;
  }

  public List<String> getPublicAddressBookIdsOfUser(String user) throws Exception {
    List<String> groupIds;
    if (user == null) {
      Identity identity = ConversationState.getCurrent().getIdentity();
      groupIds = new ArrayList<String>(identity.getGroups());
    } else {
      OrganizationService organizationService = (OrganizationService) ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(OrganizationService.class);
      Object[] objGroupIds = organizationService.getGroupHandler().findGroupsOfUser(user).toArray();
      groupIds = new ArrayList<String>();
      for (Object object : objGroupIds) {
        groupIds.add(((Group) object).getId());
      }
    }
    groupIds = excludeWildCardMatchs(groupIds, nonPublicGroups);
    return groupIds;
  }

  public List<String> getAllsPublicAddressBookIds(String user) throws Exception {
    List<String> publicGroupIds = new ArrayList<String>();
    if (userCanSeeAllGroupAddressBooks) {
      publicGroupIds = storage_.getPublicAddresses(user);
    } else {
      publicGroupIds = getPublicAddressBookIdsOfUser(user);
    }
    publicGroupIds = excludeWildCardMatchs(publicGroupIds, nonPublicGroups);
    return publicGroupIds;
  }

  public List<Contact> getPersonalContacts(String username) throws Exception {
    return storage_.findAllContactsByOwner(username);
  }

  public Map<String, String> searchEmails(String username, ContactFilter filter) throws Exception {
    return storage_.findEmailsByFilter(username, filter);
  }

  public List<String> searchEmailsByFilter(String username, ContactFilter filter) throws Exception {
    return storage_.searchEmailsByFilter(username, filter);
  }

  public ContactPageList getPersonalContactsByAddressBook(String ownerId, String addressBookId) throws Exception {
    return storage_.findPersonalContactsByAddressBook(ownerId, addressBookId);
  }

  public ContactPageList getPersonalContactsByFilter(String username, ContactFilter filter) throws Exception {
    return storage_.findContactsByFilter(username, filter, DataStorage.PERSONAL);
  }

  public ContactPageList getSharedContactsByFilter(String username, ContactFilter filter) throws Exception {
    return storage_.findContactsByFilter(username, filter, DataStorage.SHARED);
  }

  public ContactPageList getPublicContactsByFilter(String username, ContactFilter filter) throws Exception {
    return storage_.findContactsByFilter(username, filter, DataStorage.PUBLIC);
  }

  public List<String> getEmailsByAddressBook(String username, String addressBookId) throws Exception {
    return storage_.findEmailsInPersonalAddressBook(username, addressBookId);
  }

  public Contact getContact(String username, String contactId) throws Exception {
    return storage_.loadPersonalContact(username, contactId);
  }

  public void saveContact(String username, Contact contact, boolean isNew) throws Exception {
    storage_.saveContact(username, contact, isNew);
    for (ContactEventListener ce : listeners_) {
      if (isNew)
        ce.saveContact(username, contact);
      else
        ce.updateContact(username, contact);
    }
  }

  public List<Contact> removeContacts(String username, List<String> contactIds) throws Exception {
    return storage_.removeContacts(username, contactIds);
  }

  public void moveContacts(String username, List<Contact> contacts, String addressType) throws Exception {
    storage_.moveContacts(username, contacts, addressType);
  }

  public void addUserContactInAddressBook(String userId, String addressBookId) throws Exception {
    storage_.addUserContactInAddressBook(userId, addressBookId);
  }

  public List<AddressBook> getGroups(String username) throws Exception {
    return storage_.findPersonalAddressBooksByOwner(username);
  }

  public List<String> getPublicAddresses(String username) throws Exception {
    return storage_.getPublicAddresses(username);
  }

  public AddressBook getPersonalAddressBook(String username, String addressBookId) throws Exception {
    return storage_.loadPersonalAddressBook(username, addressBookId);
  }

  public AddressBook getPublicAddressBook(String username, String addressBookId) throws Exception {
    return storage_.loadPublicAddressBook(username, addressBookId);
  }

  public void saveAddressBook(String username, AddressBook group, boolean isNew) throws Exception {
    storage_.savePersonalOrSharedAddressBook(username, group, isNew);
  }

  public AddressBook removeAddressBook(String username, String addressBookId) throws Exception {
    // step 1 : remove content
    storage_.clearAddressBook(username, addressBookId);

    // step 2 : remove address book
    AddressBook removed = storage_.removePersonalAddressBook(username, addressBookId);

    return removed;
  }

  public void removeUserShareContact(String username, String contactId, String removedUser) throws Exception {
    storage_.removeUserShareContact(username, contactId, removedUser);
  }

  public void shareContact(String username, String[] contactIds, List<String> receiveUsers) throws Exception {
    storage_.shareContact(username, contactIds, receiveUsers);
  }

  public DataPageList getSharedContacts(String username) throws Exception {
    return storage_.getSharedContacts(username);
  }

  public void shareAddressBook(String username, String addressBookId, List<String> receiverUsers) throws Exception {
    storage_.shareAddressBook(username, addressBookId, receiverUsers);
  }

  public void unshareAddressBook(String owner, String addressBookId, String unsharedUser) throws Exception {
    storage_.unshareAddressBook(owner, addressBookId, unsharedUser);
  }

  public List<SharedAddressBook> getSharedAddressBooks(String username) throws Exception {
    return storage_.findSharedAddressBooksByUser(username);
  }

  public ContactPageList getSharedContactsByAddressBook(String username, SharedAddressBook addressBook) throws Exception {
    return storage_.getSharedContactsByAddressBook(username, addressBook);
  }

  public void removeSharedContact(String username, String addressBookId, String contactId) throws Exception {
    storage_.removeSharedContact(username, addressBookId, contactId);
  }

  public void saveContactToSharedAddressBook(String username, String addressBookId, Contact contact, boolean isNew) throws Exception {
    storage_.saveContactToSharedAddressBook(username, addressBookId, contact, isNew);
    for (ContactEventListener ce : listeners_) {
      if (isNew)
        ce.saveContact(username, contact);
      else
        ce.updateContact(username, contact);
    }
  }

  public Contact getSharedContactAddressBook(String username, String contactId) throws Exception {
    return storage_.getSharedContactAddressBook(username, contactId);
  }

  public void saveSharedContact(String username, Contact contact) throws Exception {
    storage_.saveSharedContact(username, contact);
  }

  public Contact getSharedContact(String username, String contactId) throws Exception {
    return storage_.getSharedContact(username, contactId);
  }

  public Contact getPublicContact(String contactId) throws Exception {
    return storage_.loadPublicContactByUser(contactId);
  }

  public List<Tag> getTags(String username) throws Exception {
    return storage_.getTags(username);
  }

  public DataPageList getContactPageListByTag(String username, String tagName) throws Exception {
    return storage_.getContactPageListByTag(username, tagName);
  }

  public void addTag(String username, List<String> contactIds, List<Tag> tags) throws Exception {
    storage_.addTag(username, contactIds, tags);
  }

  public void addTag(String username, List<String> contactIds, String tagId) throws Exception {
    storage_.addTag(username, contactIds, tagId);
  }

  public Tag removeTag(String username, String tagName) throws Exception {
    return storage_.removeTag(username, tagName);
  }

  public void updateTag(String username, Tag tag) throws Exception {
    storage_.updateTag(username, tag);
  }

  public void removeContactTag(String username, List<String> contactIds, List<String> tags) throws Exception {
    storage_.removeContactTag(username, contactIds, tags);
  }

  public ContactPageList getPublicContactsByAddressBook(String groupId) throws Exception {
    return storage_.getPublicContactsByAddressBook(groupId);
  }

  public void pasteAddressBook(String username, String srcAddress, String srcType, String destAddress, String destType) throws Exception {
    storage_.pasteAddressBook(username, srcAddress, srcType, destAddress, destType);
  }

  public List<Contact> pasteContacts(String username, String destAddress, String destType, Map<String, String> contactsMap) throws Exception {
    return storage_.pasteContacts(username, destAddress, destType, contactsMap);
  }

  public ContactImportExport getContactImportExports(String type) {
    return contactImportExport_.get(type);
  }

  public String[] getImportExportType() throws Exception {
    return contactImportExport_.keySet().toArray(new String[] {});
  }

  public DataPageList searchContact(String username, ContactFilter filter) throws Exception {
    return storage_.searchContact(username, filter);
  }

  public AddressBook getSharedAddressBook(String username, String addressBookId) throws Exception {
    return storage_.getSharedAddressBookById(username, addressBookId);
  }

  public List<String> getAllEmailBySharedGroup(String username, String addressBookId) throws Exception {
    return storage_.getAllEmailBySharedGroup(username, addressBookId);
  }

  public List<String> getAllEmailByPublicGroup(String username, String groupId) throws Exception {
    return storage_.findEmailsInPublicAddressBook(username, groupId);
  }

  public void registerNewUser(User user, boolean isNew) throws Exception {
    storage_.registerNewUser(user, isNew);
  }

  @SuppressWarnings("deprecation")
  public void updateProfile(UserProfile userProfile) throws Exception {
    Contact contact = storage_.loadPublicContactByUser(userProfile.getUserName());
    if (contact == null)
      return;
    contact.setNickName(userProfile.getAttribute("user.name.nickName"));
    Date date = new Date(userProfile.getAttribute("user.bdate"));
    contact.setBirthday(date);
    contact.setGender(userProfile.getAttribute("user.gender"));

    StringBuilder builderNote = new StringBuilder();
    if (!Utils.isEmpty(userProfile.getAttribute("user.employer"))) {
      builderNote.append(userProfile.getAttribute("user.employer"));
    }
    if (!Utils.isEmpty(userProfile.getAttribute("user.department"))) {
      if (builderNote.length() == 0)
        builderNote.append(userProfile.getAttribute("user.department"));
      else
        builderNote.append("\n" + userProfile.getAttribute("user.department"));
    }
    if (!Utils.isEmpty(userProfile.getAttribute("user.language"))) {
      if (builderNote.length() == 0)
        builderNote.append(userProfile.getAttribute("user.language"));
      else
        builderNote.append("\n" + userProfile.getAttribute("user.language"));
    }
    contact.setNote(builderNote.toString());
    contact.setJobTitle(userProfile.getAttribute("user.jobtitle"));

    StringBuilder builderHomeAddress = new StringBuilder();
    if (!Utils.isEmpty(userProfile.getAttribute("user.home-info.postal.name"))) {
      builderHomeAddress.append(userProfile.getAttribute("user.home-info.postal.name"));
    }
    if (!Utils.isEmpty(userProfile.getAttribute("user.home-info.postal.street"))) {
      if (builderHomeAddress.length() == 0)
        builderHomeAddress.append(userProfile.getAttribute("user.home-info.postal.street"));
      else
        builderHomeAddress.append(" " + userProfile.getAttribute("user.home-info.postal.street"));
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
    OrganizationService organizationService = (OrganizationService) container.getComponentInstanceOfType(OrganizationService.class);
    String email = organizationService.getUserHandler().findUserByName(userProfile.getUserName()).getEmail();
    StringBuilder builderEmailAddress = new StringBuilder();
    if (!Utils.isEmpty(email)) {
      builderEmailAddress.append(email);
    }
    if (!Utils.isEmpty(userProfile.getAttribute("user.home-info.online.email"))) {
      if (builderEmailAddress.length() == 0)
        builderEmailAddress.append(userProfile.getAttribute("user.home-info.online.email"));
      else
        builderEmailAddress.append("," + userProfile.getAttribute("user.home-info.online.email"));
    }
    if (!Utils.isEmpty(userProfile.getAttribute("user.business-info.online.email"))) {
      if (builderEmailAddress.length() == 0)
        builderEmailAddress.append(userProfile.getAttribute("user.business-info.online.email"));
      else
        builderEmailAddress.append("," + userProfile.getAttribute("user.business-info.online.email"));
    }
    contact.setEmailAddress(builderEmailAddress.toString());

    Calendar cal = new GregorianCalendar();
    contact.setLastUpdated(cal.getTime());
    saveContact(userProfile.getUserName(), contact, false);

  }

  public void addGroupToPersonalContact(String userId, String groupId) throws Exception {
    addUserContactInAddressBook(userId, groupId);
  }

  public void saveAddress(String username, String emailAddress) throws Exception {
    storage_.saveAddress(username, emailAddress);
  }

  public Tag getTag(String username, String tagName) throws Exception {
    return storage_.getTag(username, tagName);
  }

  public boolean haveEditPermissionOnContact(String username, Contact contact) throws Exception {
    return storage_.haveEditPermissionOnContact(username, contact);
  }

  @Override
  public void addListenerPlugin(ContactEventListener listener) throws Exception {
    listeners_.add(listener);
  }

  public void savePublicAddressBook(AddressBook addressbook, boolean isNew) throws Exception {
    storage_.savePublicAddressBook(addressbook, isNew);
  }
}
