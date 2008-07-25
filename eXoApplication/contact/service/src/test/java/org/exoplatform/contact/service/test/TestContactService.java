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
package org.exoplatform.contact.service.test;

import java.util.Arrays;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.impl.JCRDataStorage;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2008  
 */


public class TestContactService extends BaseContactServiceTestCase{
	private static ContactService contactService_ ;
	private static SessionProvider sProvider_ ;
  private String userRoot_ = "root" ;
  private String userMarry_ = "marry ";
  private String userJohn_ = "john";
  private String userDemo_ = "demo";
	public void setUp() throws Exception {
    super.setUp() ;
    contactService_ = (ContactService) container.getComponentInstanceOfType(ContactService.class) ;
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
    sProvider_ = sessionProviderService.getSystemSessionProvider(null) ;
  }
  
  public void testContactService() throws Exception {
  /**
   * Test AddressBook
   */
  // create new address book:
    ContactGroup rootGroup1_ = createContactGroup("group1", "group1", userRoot_);
    ContactGroup rootGroup2_ = createContactGroup("group2", "group2", userRoot_);
    ContactGroup shareGroup = createContactGroup("shareGroup", "shareGroup", userRoot_);
    ContactGroup marryGroup_ = createContactGroup("group3", "group3", userMarry_);
    ContactGroup johnGroup_ = createContactGroup("group3", "group3", userJohn_);
    
  // get AddressBook:
    assertNotNull(contactService_.getGroup(sProvider_, userRoot_, rootGroup1_.getId()));
    assertNotNull(contactService_.getGroup(sProvider_, userRoot_, rootGroup2_.getId()));
    
  // get Groups:
    assertEquals(contactService_.getGroups(sProvider_, userRoot_).size(), 3);
    
  // update addressBook:
    rootGroup1_.setName("newName");
    contactService_.saveGroup(sProvider_, userRoot_, rootGroup1_, false);
    assertEquals(rootGroup1_.getName(), "newName");
    
  //  add Group To Personal Contact
    /*Contact contact = createContact() ;
    contact.setId(userRoot_) ;
    contactService_.saveContact(sProvider_, userRoot_, contact, true);
    
    assertEquals(contactService_.getGroups(sProvider_, userRoot_).size(), 3);
    contactService_.addGroupToPersonalContact(userRoot_, marryGroup_.getId());
    assertEquals(contactService_.getGroups(sProvider_, userRoot_).size(), 4);*/
    
  //  remove group:
    assertNotNull(contactService_.removeGroup(sProvider_, userRoot_, rootGroup2_.getId()));
    assertNull(contactService_.getGroup(sProvider_, userRoot_, rootGroup2_.getId()));
    
  // share group:
    shareGroup.setEditPermissionUsers(new String[]{userJohn_});
    contactService_.shareAddressBook(sProvider_, userRoot_, shareGroup.getId(), Arrays.asList(new String[]{userJohn_, userDemo_}));
  
  // get shared addressbooks:
    assertNotNull(contactService_.getSharedAddressBooks(sProvider_, userJohn_));
    assertEquals(contactService_.getSharedAddressBooks(sProvider_, userJohn_).size(), 1);
    
  // get shared group:
    assertEquals(contactService_.getSharedGroup(userJohn_, shareGroup.getId()).getName(), "shareGroup");
    
  // remove User Share Address Book
    assertEquals(contactService_.getSharedAddressBooks(sProvider_, userDemo_).size(), 1);
    contactService_.removeUserShareAddressBook(sProvider_, userRoot_, shareGroup.getId(), userDemo_);
    assertEquals(contactService_.getSharedAddressBooks(sProvider_, userDemo_).size(), 0);
    
  /**
   * Test contact:
   */
    Contact contact1 = createContact();
    Contact contact2 = createContact();
    Contact contact3 = createContact();
    Contact contact4 = createContact();
    contact1.setAddressBook(new String[]{rootGroup1_.getId(), rootGroup2_.getId()});
    contact2.setAddressBook(new String[]{rootGroup1_.getId()});
    contact4.setAddressBook(new String[]{rootGroup1_.getId()});
    contact3.setAddressBook(new String[]{marryGroup_.getId(), johnGroup_.getId()});
    
  //  save contact
    contactService_.saveContact(sProvider_, userRoot_, contact1, true);
    contactService_.saveContact(sProvider_, userRoot_, contact2, true);
    contactService_.saveContact(sProvider_, userRoot_, contact4, true);
    
    contactService_.saveContact(sProvider_, userJohn_, contact3, true);
    contactService_.saveContact(sProvider_, userMarry_, contact3, true);
    
    assertNotNull(contact1);
    assertNotNull(contact2);
    
  // get contact:
    assertNotNull(contactService_.getContact(sProvider_, userRoot_, contact1.getId()));
    assertNotNull(contactService_.getContact(sProvider_, userRoot_, contact2.getId()));
    
  //  get all contacts:
    assertNotNull(contactService_.getAllContact(sProvider_, userRoot_));
    assertEquals(contactService_.getAllContact(sProvider_, userRoot_).size(), 3);
    
  // get Contact page list by group
    assertNotNull(contactService_.getContactPageListByGroup(sProvider_, userRoot_, rootGroup1_.getId()));
    assertEquals(contactService_.getContactPageListByGroup(sProvider_, userRoot_, rootGroup1_.getId()).getAll().size(), 3);
    
    // get all email address by group:
    assertNotNull(contactService_.getAllEmailAddressByGroup(sProvider_, userRoot_, rootGroup1_.getId()));
    assertEquals(contactService_.getAllEmailAddressByGroup(sProvider_, userRoot_, rootGroup1_.getId()).size(), 3);
    
  // move contact:
    assertEquals(contactService_.getContactPageListByGroup(sProvider_, userRoot_, rootGroup2_.getId()).getAll().size(), 1);
    contact4.setAddressBook(new String[]{rootGroup2_.getId()});
    // move contact4 from contactGroup1 to contactGroup4, contactGroup1 and contactGroup4 is userRoot_'s --> type is PRIVATE
    contactService_.moveContacts(sProvider_, userRoot_, Arrays.asList(new Contact[]{contact4}), JCRDataStorage.PRIVATE);
    assertEquals(contactService_.getContactPageListByGroup(sProvider_, userRoot_, rootGroup2_.getId()).getAll().size(), 2);
    
  // get public contact:
    assertNotNull(contactService_.getPublicContactsByAddressBook(sProvider_, "/platform/users"));
    assertEquals(contactService_.getPublicContactsByAddressBook(sProvider_, "/platform/users").getAll().size(), 0);
    
  // get property of contact:
    assertEquals(contactService_.getContact(sProvider_, userRoot_, contact1.getId()).getFullName(), "fullName");
    
    // update contact:
    contact1.setFirstName("new first name");
    contact1.setOwner(true);
    contactService_.saveContact(sProvider_, userRoot_, contact1, false);
    assertEquals("new first name", contact1.getFirstName());
    
  // share contact to user:
    contactService_.shareContact(sProvider_, userRoot_, new String[]{contact1.getId()}, Arrays.asList(new String[]{userMarry_}));
    contact2.setEditPermissionUsers(new String[]{userJohn_});
    contactService_.shareContact(sProvider_, userRoot_, new String[]{contact2.getId()}, Arrays.asList(new String[]{userJohn_, userDemo_}));
    
  // save shared contact:
    contact2.setFullName("Mai Van Ha");
    contactService_.saveSharedContact(userJohn_, contact2);
    assertEquals(contactService_.getContact(sProvider_, userRoot_, contact2.getId()).getFullName(), "Mai Van Ha");
    
  // get shared contact:
    assertEquals(contactService_.getSharedContactAddressBook(userJohn_, contact2.getId()).getFullName(), "Mai Van Ha");
    
  // get shared contacts:
    assertNotNull(contactService_.getSharedContacts(userMarry_));
    assertEquals(contactService_.getSharedContacts(userMarry_).getAll().size(), 1);
    assertNotNull(contactService_.getSharedContacts(userJohn_));
    assertNotNull(contactService_.getSharedContacts(userDemo_));
  // get shared contact:
    assertNotNull(contactService_.getSharedContact(sProvider_, userJohn_, contact2.getId()));
    
  //  remove shared contact (remove viewed contact):
    assertEquals(contactService_.getSharedContacts(userJohn_).getAll().size(), 1);
    contactService_.removeUserShareContact(sProvider_, userRoot_, contact2.getId(), userJohn_);
    assertEquals(contactService_.getSharedContacts(userJohn_).getAll().size(), 0);
    
  // get Shared Contacts By AddressBook
    Contact shareContact = createContact();;
    shareContact.setAddressBook(new String[]{shareGroup.getId()});
    contactService_.saveContact(sProvider_, userRoot_, shareContact, true);
    
    assertEquals(contactService_.getContactPageListByGroup(sProvider_, userRoot_, shareGroup.getId()).getAll().size(), 1);
    SharedAddressBook sharedAddressBook = createShareAddressbook(shareGroup, userRoot_);
    assertEquals(contactService_.getSharedContactsByAddressBook(sProvider_, userJohn_, sharedAddressBook).getAll().size(), 1);
    
  // get all email by shared group:
    shareContact.setEmailAddress("maivanha1610@gmail.com");
    contactService_.saveContact(sProvider_, userRoot_, shareContact, false);
    assertEquals(contactService_.getAllEmailBySharedGroup(userJohn_, shareGroup.getId()).get(0), "maivanha1610@gmail.com");
    
  // remove contact in shared address book 
    contactService_.removeSharedContact(sProvider_, userJohn_, shareGroup.getId(), shareContact.getId());
    assertEquals(contactService_.getContactPageListByGroup(sProvider_, userRoot_, shareGroup.getId()).getAll().size(), 0);
    
  // save contact to share addressbook:
    contactService_.saveContactToSharedAddressBook(userJohn_, shareGroup.getId(), shareContact, true);
    assertEquals(contactService_.getContactPageListByGroup(sProvider_, userRoot_, shareGroup.getId()).getAll().size(), 1);
    
  // get contacts by addressBookId:
    assertNotNull(contactService_.getAllEmailAddressByGroup(sProvider_, userRoot_, rootGroup1_.getId()));
    assertNotNull(contactService_.getAllEmailAddressByGroup(sProvider_, userRoot_, rootGroup2_.getId()));
    
  //  remove contact:
    assertNotNull(contactService_.removeContacts(sProvider_, userRoot_, Arrays.asList(new String[]{contact2.getId()})));
    assertNull(contactService_.getContact(sProvider_, userRoot_, contact2.getId()));
    
  }
  
  private ContactGroup createContactGroup(String name, String description, String userName) throws Exception{
    ContactGroup contactGroup = new ContactGroup();
    contactGroup.setName(name);
    contactGroup.setDescription(description);
    contactService_.saveGroup(sProvider_, userName, contactGroup, true);
    return contactGroup;
  }
  
  private SharedAddressBook createShareAddressbook(ContactGroup contactGroup, String userId) throws Exception{
    SharedAddressBook sharedAddressBook = new SharedAddressBook(contactGroup.getName(), contactGroup.getId(), userId);
    sharedAddressBook.setEditPermissionGroups(contactGroup.getEditPermissionGroups());
    sharedAddressBook.setEditPermissionUsers(contactGroup.getEditPermissionUsers());
    return sharedAddressBook;
  }
  
  private Contact createContact() {
    Contact contact = new Contact();
    
    contact.setFullName("fullName");
    contact.setFirstName("firstName");
    contact.setLastName("lastName");
    contact.setNickName("nickName");
    contact.setJobTitle("jobTitle");
    contact.setEmailAddress("emailAddress");
    
    contact.setExoId("exoId");
    contact.setGoogleId("googleId");
    contact.setMsnId("msnId");
    contact.setAolId("aolId");
    contact.setYahooId("yahooId");
    contact.setIcrId("icrId");
    contact.setSkypeId("skypeId");
    contact.setIcqId("icqId");
    
    contact.setHomeAddress("homeAddress");
    contact.setHomeCity("homeCity");
    contact.setHomeState_province("homeState_province");
    contact.setHomePostalCode("homePostalCode");
    contact.setHomeCountry("homeCountry");
    contact.setHomePhone1("homePhone1");
    contact.setHomePhone2("homePhone2");
    contact.setHomeFax("homeFax");
    contact.setPersonalSite("personalSite");
    
    contact.setWorkAddress("workAddress");
    contact.setWorkCity("workCity");
    contact.setWorkStateProvince("workState_province");
    contact.setWorkPostalCode("workPostalCode");
    contact.setWorkCountry("workCountry");
    contact.setWorkPhone1("workPhone1");
    contact.setWorkPhone2("workPhone2");
    contact.setWorkFax("workFax");
    contact.setMobilePhone("mobilePhone");
    contact.setWebPage("webPage");
    
    contact.setNote("note");
    return contact;
  }
  
}