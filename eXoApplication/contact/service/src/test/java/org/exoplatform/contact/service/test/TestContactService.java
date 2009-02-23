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
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.collections.map.HashedMap;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.impl.JCRDataStorage;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.contact.service.Tag;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2008  
 */


public class TestContactService extends BaseContactServiceTestCase{
  private static ContactService contactService_ ;
  private static SessionProvider sProvider_ ;
  private static String root = "root" ;
  //private String userMarry_ = "marry ";
  private static  String john = "john";
  private static  String demo = "demo";
  
  public void setUp() throws Exception {
    super.setUp() ;
    contactService_ = (ContactService) container.getComponentInstanceOfType(ContactService.class) ;
    SessionProviderService sessionProviderService = (SessionProviderService) container.getComponentInstanceOfType(SessionProviderService.class) ;
    sProvider_ = sessionProviderService.getSystemSessionProvider(null) ;
  }
  
  public void testGetSaveGroup() throws Exception {
    // test the get operation on a non existent address book
    ContactGroup shouldBeNull = contactService_.getGroup(root, "nonexistent");
    assertNull("A non existent address book should be null", shouldBeNull);

    ContactGroup newAB = new ContactGroup();
    newAB.setName("AB1");
    newAB.setDescription("Desc AB1");
    contactService_.saveGroup(root, newAB, true); // test the create operation
    assertNotNull("Saved addressBook has no ID", newAB.getId());
    
    // test get by ID operation
    ContactGroup savedAB = contactService_.getGroup(root, newAB.getId());
    
    assertEquals("Saved adressBook name differs", newAB.getName(), savedAB.getName());
    assertEquals("Saved adressBook description differs", newAB.getDescription(), savedAB.getDescription());
    assertNotNull("Saved addressBook has no ID",savedAB.getId());
    assertEquals("Saved and Loaded addressBook should have same IDs", savedAB.getId(), newAB.getId());
    
    assertNull("Saved addressBook should have null editPermissionsGroups", savedAB.getEditPermissionGroups());
    assertNull("Saved addressBook should have null editPermissionsUsers", savedAB.getEditPermissionUsers());
    assertNull("Saved addressBook should have null viewPermissionsUsers", savedAB.getViewPermissionUsers());
    assertNull("Saved addressBook should have null viewPermissionsGroups", savedAB.getViewPermissionGroups());
       
    
    // test the update operation
    savedAB.setName(newAB.getName() + " updated");
    savedAB.setDescription(newAB.getDescription() + " updated");
    savedAB.setViewPermissionUsers(new String []{"demo", "john"});
    savedAB.setEditPermissionUsers(new String []{"john"});
    savedAB.setViewPermissionGroups(new String []{"/platform/users"});
    savedAB.setEditPermissionGroups(new String []{"/platform/administrators"});
    String beforeId = savedAB.getId();
    contactService_.saveGroup(root, savedAB, false);
    assertEquals("AdressBook should have same ID before and after save", beforeId, newAB.getId());
    
    // test get by ID operation
    ContactGroup loadedAB = contactService_.getGroup(root, savedAB.getId());
    assertEquals("Loaded adressBook name differs", savedAB.getName(), loadedAB.getName());
    assertEquals("Loaded adressBook description differs", savedAB.getDescription(), loadedAB.getDescription());
    assertEquals("Loaded and Saved addressBooks should have same IDs",savedAB.getId(), loadedAB.getId());
    assertEquals("Saved addressBook editPermissionsUsers does not match", savedAB.getEditPermissionUsers(), loadedAB.getEditPermissionUsers());
    assertEquals("Saved addressBook editPermissionsGroups does not match", savedAB.getEditPermissionGroups(), loadedAB.getEditPermissionGroups());  
    assertEquals("Saved addressBook viewPermissionsUsers does not match", savedAB.getViewPermissionUsers(), loadedAB.getViewPermissionUsers());
    assertEquals("Saved addressBook viewPermissionsGroups does not match", savedAB.getViewPermissionGroups(), loadedAB.getViewPermissionGroups());    
  }
  
  /**
   * Assertion method on string arrays
   * @param message
   * @param expected
   * @param actual
   */
  public static void assertEquals(String message, String []expected, String []actual) {
    assertEquals(message, expected.length, actual.length);
    for (int i = 0; i < expected.length; i++) {
      assertEquals(message, expected[i], actual[i]);
    }
  }
  
  public void _testContactService() throws Exception {
  /**
   * Test AddressBook
   */
  // create new address book:
    ContactGroup rootBook1 = createAddressBook("group1", "group1", root);
    ContactGroup rootBook2 = createAddressBook("group2", "group2", root);
    ContactGroup sharedBook = createAddressBook("shareGroup", "shareGroup", root);
    //ContactGroup marryGroup_ = createContactGroup("group3", "group3", userMarry_);
    ContactGroup johnBook = createAddressBook("group3", "group3", john);
    
  // get AddressBook:
    //assertNotNull(contactService_.getGroup(root, rootBook1.getId()));
    //assertNotNull(contactService_.getGroup(root, rootBook2.getId()));
    
  // get Groups:
    //assertEquals(contactService_.getGroups(sProvider_, root).size(), 3);
    
  // update addressBook:
    //rootBook1.setName("newName");
    //contactService_.saveGroup(root, rootBook1, false);
    //assertEquals(rootBook1.getName(), "newName");
    
  //  add Group To Personal Contact
    Contact contact = createContact() ; 
    setContactInAddressBooks(contact, rootBook1, rootBook2);
    contact.setId(root) ;
    contactService_.saveContact(sProvider_, root, contact, true);
    assertEquals(contact.getAddressBook().length, 2);
    contactService_.addGroupToPersonalContact(root, johnBook.getId());
    contact = contactService_.getContact(sProvider_, root, contact.getId());
    assertEquals(contact.getAddressBook().length, 3);
    
  //  remove group:
    assertNotNull(contactService_.removeGroup(sProvider_, root, rootBook2.getId()));
    assertNull(contactService_.getGroup(root, rootBook2.getId()));
    
  // share group:
    sharedBook.setEditPermissionUsers(new String[]{john});
    contactService_.shareAddressBook(sProvider_, root, sharedBook.getId(), Arrays.asList(new String[]{john, demo}));
    
    johnBook.setEditPermissionGroups(new String[]{root});
    contactService_.shareAddressBook(sProvider_, john, johnBook.getId(), Arrays.asList(new String[]{root}));
  
  // get shared addressbooks:
    assertEquals(contactService_.getSharedAddressBooks(sProvider_, john).size(), 1);
    
  // get shared group:
    assertEquals(contactService_.getSharedGroup(john, sharedBook.getId()).getName(), "shareGroup");
    
  // remove User Share Address Book
    assertEquals(contactService_.getSharedAddressBooks(sProvider_, demo).size(), 1);
    contactService_.removeUserShareAddressBook(sProvider_, root, sharedBook.getId(), demo);
    assertEquals(contactService_.getSharedAddressBooks(sProvider_, demo).size(), 0);
    
  /**
   * Test contact:
   */
    Contact contact1 = createContact();
    Contact contact2 = createContact();
    Contact contact3 = createContact();
    Contact contact4 = createContact();
    

    
    setContactInAddressBooks(contact1, rootBook1, rootBook2);
    setContactInAddressBooks(contact2, rootBook1);
    setContactInAddressBooks(contact4, rootBook1);
    //setContactInAddressBooks(contact3, marryGroup_,johnGroup_);
 
    
  //  save contact
    contactService_.saveContact(sProvider_, root, contact1, true);
    contactService_.saveContact(sProvider_, root, contact2, true);
    contactService_.saveContact(sProvider_, root, contact4, true);
    
    contactService_.saveContact(sProvider_, john, contact3, true);
    //contactService_.saveContact(sProvider_, userMarry_, contact3, true);
    
    assertNotNull(contact1);
    assertNotNull(contact2);
    
  // get contact:
    assertNotNull(contactService_.getContact(sProvider_, root, contact1.getId()));
    assertNotNull(contactService_.getContact(sProvider_, root, contact2.getId()));
    
  //  get all contacts:
    assertNotNull(contactService_.getAllContact(sProvider_, root));
    assertEquals(contactService_.getAllContact(sProvider_, root).size(), 3);
    
  // get Contact page list by group
    assertNotNull(contactService_.getContactPageListByGroup(sProvider_, root, rootBook1.getId()));
    assertEquals(contactService_.getContactPageListByGroup(sProvider_, root, rootBook1.getId()).getAll().size(), 3);
    
  // get all email address by group:
    assertNotNull(contactService_.getAllEmailAddressByGroup(sProvider_, root, rootBook1.getId()));
    assertEquals(contactService_.getAllEmailAddressByGroup(sProvider_, root, rootBook1.getId()).size(), 3);
    
  // move contact:
    assertEquals(contactService_.getContactPageListByGroup(sProvider_, root, rootBook2.getId()).getAll().size(), 1);
    contact4.setAddressBook(new String[]{rootBook2.getId()});
    // move contact4 from contactGroup1 to contactGroup4, contactGroup1 and contactGroup4 is userRoot_'s --> type is PRIVATE
    contactService_.moveContacts(sProvider_, root, Arrays.asList(new Contact[]{contact4}), JCRDataStorage.PRIVATE);
    assertEquals(contactService_.getContactPageListByGroup(sProvider_, root, rootBook2.getId()).getAll().size(), 2);
    
  // get public contact:
    assertNotNull(contactService_.getPublicContactsByAddressBook(sProvider_, "/platform/users"));
    assertEquals(contactService_.getPublicContactsByAddressBook(sProvider_, "/platform/users").getAll().size(), 0);
    
  // get public contact:
    assertNull(contactService_.getPublicContact(contact1.getId()));
    
  // get all email by public group:
    assertNotNull(contactService_.getAllEmailByPublicGroup(root, "/platform/users"));
    
  // get property of contact:
    assertEquals(contactService_.getContact(sProvider_, root, contact1.getId()).getFullName(), "fullName");
    
    // update contact:
    contact1.setFirstName("new first name");
    contact1.setOwner(true);
    contactService_.saveContact(sProvider_, root, contact1, false);
    assertEquals("new first name", contact1.getFirstName());
    
  // share contact to user:
    //contactService_.shareContact(sProvider_, userRoot_, new String[]{contact1.getId()}, Arrays.asList(new String[]{userMarry_}));
    contact2.setEditPermissionUsers(new String[]{john});
    contactService_.shareContact(sProvider_, root, new String[]{contact2.getId()}, Arrays.asList(new String[]{john, demo}));
    
  // save shared contact:
    contact2.setFullName("Mai Van Ha");
    contactService_.saveSharedContact(john, contact2);
    assertEquals(contactService_.getContact(sProvider_, root, contact2.getId()).getFullName(), "Mai Van Ha");
    
  // get shared contact:
    assertEquals(contactService_.getSharedContactAddressBook(john, contact2.getId()).getFullName(), "Mai Van Ha");
    
  // get shared contacts:
    //assertNotNull(contactService_.getSharedContacts(userMarry_));
    //assertEquals(contactService_.getSharedContacts(userMarry_).getAll().size(), 1);
    assertNotNull(contactService_.getSharedContacts(john));
    assertNotNull(contactService_.getSharedContacts(demo));
  // get shared contact:
    assertNotNull(contactService_.getSharedContact(sProvider_, john, contact2.getId()));
    
  //  remove shared contact (remove viewed contact):
    assertEquals(contactService_.getSharedContacts(john).getAll().size(), 1);
    contactService_.removeUserShareContact(sProvider_, root, contact2.getId(), john);
    assertEquals(contactService_.getSharedContacts(john).getAll().size(), 0);
    
  // get Shared Contacts By AddressBook
    Contact shareContact = createContact();;
    shareContact.setAddressBook(new String[]{sharedBook.getId()});
    contactService_.saveContact(sProvider_, root, shareContact, true);
    
    assertEquals(contactService_.getContactPageListByGroup(sProvider_, root, sharedBook.getId()).getAll().size(), 1);
    SharedAddressBook sharedAddressBook = createShareAddressbook(sharedBook, root);
    assertEquals(contactService_.getSharedContactsByAddressBook(sProvider_, john, sharedAddressBook).getAll().size(), 1);
    
  // get all email by shared group:
    shareContact.setEmailAddress("maivanha1610@gmail.com");
    shareContact.setFullName("Mai Van Ha");
    contactService_.saveContact(sProvider_, root, shareContact, false);
    assertEquals(contactService_.getAllEmailBySharedGroup(john, sharedBook.getId()).get(0), "maivanha1610@gmail.com");
    
  // remove contact in shared address book 
    contactService_.removeSharedContact(sProvider_, john, sharedBook.getId(), shareContact.getId());
    assertEquals(contactService_.getContactPageListByGroup(sProvider_, root, sharedBook.getId()).getAll().size(), 0);
    
  // save contact to share addressbook:
    contactService_.saveContactToSharedAddressBook(john, sharedBook.getId(), shareContact, true);
    assertEquals(contactService_.getContactPageListByGroup(sProvider_, root, sharedBook.getId()).getAll().size(), 1);
    
  // get contacts by addressBookId:
    assertNotNull(contactService_.getAllEmailAddressByGroup(sProvider_, root, rootBook1.getId()));
    assertNotNull(contactService_.getAllEmailAddressByGroup(sProvider_, root, rootBook2.getId()));
    
  //  remove contact:
    assertNotNull(contactService_.removeContacts(sProvider_, root, Arrays.asList(new String[]{contact2.getId()})));
    assertNull(contactService_.getContact(sProvider_, root, contact2.getId()));
    
  //  paste AddressBook
    contactService_.pasteAddressBook(sProvider_, john, johnBook.getId(), JCRDataStorage.PRIVATE, sharedBook.getId(), JCRDataStorage.SHARED);
    assertEquals(contactService_.getSharedContactsByAddressBook(sProvider_, john, sharedAddressBook).getAll().size(), 2);
    
  // paste contact:
    Map<String, String> contacts = new LinkedHashMap<String, String>() ;
    contacts.put(contact3.getId(), contact3.getContactType()) ;
    contactService_.pasteContacts(sProvider_, john, sharedBook.getId(), JCRDataStorage.SHARED, contacts);
    assertEquals(contactService_.getSharedContactsByAddressBook(sProvider_, john, sharedAddressBook).getAll().size(), 3);
    
	/**
	 * test Search
	 */
	// search contact:
	  ContactFilter contactFilter = createContactFilter("maivanha1610@gmail.com", new String(), new String(), new String(), new String(), 
	  													new String(), new String(), new String(), new String(), new String());
	  assertEquals(contactService_.searchContact(sProvider_, root, contactFilter).getAll().get(0).getEmailAddress(), 
			  		shareContact.getEmailAddress());
	  
	// search email:
	  contactFilter.setAccountPath(" /jcr:root/Users/root/ApplicationData/ContactApplication/contacts");
	  contactFilter.setCategories(new String[]{});
	  assertEquals(contactService_.searchEmails(sProvider_, root, contactFilter).size(), 1);
    
  /**
   * Test Tag:
   */
    Tag tag = createTag("tag1");
  // add new and get tag:
    contactService_.addTag(sProvider_, root, 
    					   Arrays.asList(new String[]{contact1.getId() + "::" + JCRDataStorage.PRIVATE, 
    									 				contact4.getId() + "::" + JCRDataStorage.PRIVATE}), 
    					   Arrays.asList(new Tag[]{tag}));
    assertNotNull(contactService_.getTag(sProvider_, root, tag.getId()));
    
  // update tag:
    tag.setName("Mai Van Ha");
    contactService_.updateTag(sProvider_, root, tag);
    assertEquals(contactService_.getTag(sProvider_, root, tag.getId()).getName(), "Mai Van Ha");
    
  // getTags:
    assertEquals(contactService_.getTags(sProvider_, root).size(), 1);
    
  // getContactPageListByTag
    assertEquals(contactService_.getContactPageListByTag(sProvider_, root, tag.getId()).getAll().size(), 2);
    
  // removeContactTag
    contactService_.removeContactTag(sProvider_, root, 
							    		Arrays.asList(new String[]{contact4.getId() + "::" + JCRDataStorage.PRIVATE}), 
							    		Arrays.asList(new String[]{tag.getId()}));
    assertEquals(contactService_.getContactPageListByTag(sProvider_, root, tag.getId()).getAll().size(), 1);
    
  // add tag:
    contactService_.addTag(sProvider_, root, 
    						Arrays.asList(new String[]{contact1.getId() + "::" + JCRDataStorage.PRIVATE,
    													contact4.getId() + "::" + JCRDataStorage.PRIVATE}), 
    						tag.getId());
    assertEquals(contactService_.getContactPageListByTag(sProvider_, root, tag.getId()).getAll().size(), 2);
    
  // remove tag:
    contactService_.removeTag(sProvider_, root, tag.getId());
    assertNull(contactService_.getTag(sProvider_, root, tag.getId()));
    
  /**
   * test export/ import:
   */
  // get import, export type:
    assertNotNull(contactService_.getImportExportType());
    
  // get contact import/ export:
    assertNotNull(contactService_.getContactImportExports("x-vcard"));
    
  }

  private void setContactInAddressBooks(Contact contact,
                                        ContactGroup... groups) {
    String [] groupIds = new String [groups.length];
    int i = 0;
    for (ContactGroup group : groups) {
      groupIds[i++] = group.getId();
    }
    contact.setAddressBook(groupIds);
  }
  
  private ContactGroup createAddressBook(String name, String description, String owner) throws Exception{
    ContactGroup contactGroup = new ContactGroup();
    contactGroup.setName(name);
    contactGroup.setDescription(description);
    contactService_.saveGroup(owner, contactGroup, true);
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
  
  private Tag createTag(String tabName){
	  Tag tag = new Tag();
	  tag.setName(tabName);
	  tag.setDescription("description");
	  tag.setColor("Red");
	  return tag;
  }
  
  private ContactFilter createContactFilter(String text, String fullName, String firstName, String lastName, String nickName, String gender,
		  									String jobtitle, String emailAddress, String isOwner, String userName){
	  ContactFilter contactFilter = new ContactFilter();
	  contactFilter.setText(text);
	  contactFilter.setFullName(fullName);
	  contactFilter.setFirstName(firstName);
	  contactFilter.setLastName(lastName);
	  contactFilter.setNickName(nickName);
	  contactFilter.setGender(gender);
	  contactFilter.setJobTitle(jobtitle);
	  contactFilter.setEmailAddress(emailAddress);
	  contactFilter.setOwner(isOwner);
	  contactFilter.setUsername(userName);
	  return contactFilter;
  }
  
}