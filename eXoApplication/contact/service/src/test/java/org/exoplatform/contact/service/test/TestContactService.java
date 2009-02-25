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

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Tag;
import org.exoplatform.contact.service.impl.JCRDataStorage;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2008  
 */


public class TestContactService extends BaseContactServiceTestCase{
  private ContactService contactService ;
  private static String root = "root" ;
  //private String userMarry_ = "marry ";
  private static  String john = "john";
  private static  String demo = "demo";
  
  public TestContactService() throws Exception {
    super();
    contactService = (ContactService) container.getComponentInstanceOfType(ContactService.class) ;  
  }
  

  public void testGetSaveAddressBook() throws Exception {
    // test the get operation on a non existent address book
    AddressBook shouldBeNull = contactService.getPersonalAddressBook(root, "nonexistent");
    assertNull("A non existent address book should be null", shouldBeNull);

    AddressBook newAB = new AddressBook();
    newAB.setName("AB1");
    newAB.setDescription("Desc AB1");
    contactService.saveAddressBook(root, newAB, true); // test the create operation
    assertNotNull("Saved addressBook has no ID", newAB.getId());
    
    // test get by ID operation
    AddressBook savedAB = contactService.getPersonalAddressBook(root, newAB.getId());
    
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
    contactService.saveAddressBook(root, savedAB, false);
    assertEquals("AdressBook should have same ID before and after save", beforeId, newAB.getId());
    
    // test get by ID operation
    AddressBook loadedAB = contactService.getPersonalAddressBook(root, savedAB.getId());
    assertEquals("Loaded adressBook name differs", savedAB.getName(), loadedAB.getName());
    assertEquals("Loaded adressBook description differs", savedAB.getDescription(), loadedAB.getDescription());
    assertEquals("Loaded and Saved addressBooks should have same IDs",savedAB.getId(), loadedAB.getId());
    assertEquals("Saved addressBook editPermissionsUsers does not match", savedAB.getEditPermissionUsers(), loadedAB.getEditPermissionUsers());
    assertEquals("Saved addressBook editPermissionsGroups does not match", savedAB.getEditPermissionGroups(), loadedAB.getEditPermissionGroups());  
    assertEquals("Saved addressBook viewPermissionsUsers does not match", savedAB.getViewPermissionUsers(), loadedAB.getViewPermissionUsers());
    assertEquals("Saved addressBook viewPermissionsGroups does not match", savedAB.getViewPermissionGroups(), loadedAB.getViewPermissionGroups());    
  }
  
  /**
   * Test ContactService.saveAddressBook() on shared address books 
   */
  public void testSaveSharedAddressBook() throws Exception {
    AddressBook ab = new AddressBook();
    ab.setName("shared-with-john");
    ab.setDescription("addressbook shared with john");
    ab.setEditPermissionUsers(new String [] {"john"});
    
    // created by owner
    contactService.saveAddressBook(root, ab, true);
    
    ab = contactService.getPersonalAddressBook(root, ab.getId());
    // updated by shared user
    ab.setName("shared-by-root-to-john"); 
    ab.setDescription("modified by john");
    
    //contactService.saveAddressBook(john, ab, false);
    //AddressBook updated = contactService.getPersonalAddressBook(root, ab.getId()); // I receive a pathNotFoundException!
    //assertEquals("Saved address book name should match", ab.getName(), updated.getName());
    //assertEquals("Saved address book description should match", ab.getDescription(), updated.getDescription());
    
    // updated by non shared user
    // TODO : service should check permissions and not allow modification by non allowed users/groups
    // See  : http://jira.exoplatform.org/browse/CS-2389
    
  }
  
  public void testSaveContact() throws Exception {
    Contact contact = createContact() ; 
    AddressBook ab1 = createAddressBook("save1", "group1", root);
    AddressBook ab2 = createAddressBook("save2", "group2", root);    
    setContactInAddressBooks(contact, ab1, ab2);
    
    contactService.saveContact(sessionProvider, root, contact, true);

    assertNotNull("Saved contact must have an ID", contact.getId());
    Contact saved = contactService.getContact(root, contact.getId());
    
    assertEquals("Contact addressBooks don't match", new String[]{ab1.getId(), ab2.getId()},contact.getAddressBook());
    assertEquals("Saved contact attributes don't match", contact.getAolId(), saved.getAolId());
    assertEquals("Saved contact attributes don't match", contact.getBirthday(), saved.getBirthday());
    assertEquals("Saved contact attributes don't match", contact.getContactType(), saved.getContactType());
    assertEquals("Saved contact attributes don't match", contact.getEmailAddress(), saved.getEmailAddress());
    assertEquals("Saved contact attributes don't match", contact.getExoId(), saved.getExoId());
    assertEquals("Saved contact attributes don't match", contact.getFirstName(), saved.getFirstName());
    assertEquals("Saved contact attributes don't match", contact.getFullName(), saved.getFullName());
    assertEquals("Saved contact attributes don't match", contact.getGender(), saved.getGender());
    assertEquals("Saved contact attributes don't match", contact.getGoogleId(), saved.getGoogleId());
    assertEquals("Saved contact attributes don't match", contact.getHomeAddress(), saved.getHomeAddress());
    assertEquals("Saved contact attributes don't match", contact.getHomeCity(), saved.getHomeCity());
    assertEquals("Saved contact attributes don't match", contact.getHomeCountry(), saved.getHomeCountry());
    assertEquals("Saved contact attributes don't match", contact.getHomeFax(), saved.getHomeFax());
    assertEquals("Saved contact attributes don't match", contact.getHomePhone1(), saved.getHomePhone1());
    assertEquals("Saved contact attributes don't match", contact.getHomePhone2(), saved.getHomePhone2());
    assertEquals("Saved contact attributes don't match", contact.getHomePostalCode(), saved.getHomePostalCode());
    assertEquals("Saved contact attributes don't match", contact.getHomeState_province(), saved.getHomeState_province());
    assertEquals("Saved contact attributes don't match", contact.getIcqId(), saved.getIcqId());
    assertEquals("Saved contact attributes don't match", contact.getIcrId(), saved.getIcrId());
    assertEquals("Saved contact attributes don't match", contact.getJobTitle(), saved.getJobTitle());
    assertEquals("Saved contact attributes don't match", contact.getLastName(), saved.getLastName());
    assertEquals("Saved contact attributes don't match", contact.getMobilePhone(), saved.getMobilePhone());
    assertEquals("Saved contact attributes don't match", contact.getMsnId(), saved.getMsnId());
    assertEquals("Saved contact attributes don't match", contact.getNickName(), saved.getNickName());
    assertEquals("Saved contact attributes don't match", contact.getNote(), saved.getNote());
    assertEquals("Saved contact attributes don't match", contact.getOwnerId(), saved.getOwnerId());
    assertEquals("Saved contact attributes don't match", contact.getPersonalSite(), saved.getPersonalSite());
    assertEquals("Saved contact attributes don't match", contact.getSkypeId(), saved.getSkypeId());
    
  // TODO cover attachments
    
  }
  
  public void testAddGroupToPersonalContact() throws Exception {
//    Contact contact = createContact() ; 
//    ContactGroup johnBook = createAddressBook("group3", "group3", john);
//    contactService_.addGroupToPersonalContact(root, johnBook.getId());
//    contact = contactService_.getContact(john, contact.getId());
//    assertEquals(contact.getAddressBook().length, 3);
  }
  
  public void testRemoveAddressBook() throws Exception {
    AddressBook tobeRemoved = createAddressBook("ToBeRemoved", "will be removed", root);
    contactService.saveAddressBook(root, tobeRemoved, true);
    AddressBook removed = contactService.removeAddressBook(root, tobeRemoved.getId());
    assertNotNull("Removed addressBook should not be null", removed);
    assertNotNull("Removed addressBook should still exist", contactService.getPersonalAddressBook(root, removed.getId()));
  }
  
  public void testShareAddressBook() throws Exception {
    
    // share root address book to john and demo
    AddressBook shared = createAddressBook("ToBeShared", "will be shared with john and demo", root);
    shared.setEditPermissionUsers(new String[]{john});
    contactService.shareAddressBook(root, shared.getId(), Arrays.asList(new String[]{john,demo}));
    
    // verify is was shared to john
    List<SharedAddressBook> sharedList = contactService.getSharedAddressBooks(john);
    assertEquals("Shared address books list size was wrong", 1, sharedList.size());
    String sharedId = sharedList.get(0).getId();
    assertEquals("Shared and initial address books ids differ", shared.getId(), sharedId);
  
    // verify getSharedAddressBook() sends the correct address book
    AddressBook loaded = contactService.getSharedAddressBook(john, shared.getId());
    assertEquals("Loaded and Shared address books don't match", loaded.getName(), shared.getName());

    // root unshare AddressBook with john
    contactService.unshareAddressBook(root, shared.getId(), john);
    AddressBook loaded2 = contactService.getSharedAddressBook(john, shared.getId());
    assertNull("Loaded AddressBook should be null", loaded2);
    List<SharedAddressBook> sharedList3 = contactService.getSharedAddressBooks(john);
    assertEquals("Shared address books list size was wrong", 0, sharedList3.size());
    
    // demo unshare himself
    contactService.unshareAddressBook(root, shared.getId(), demo);
    
    startSessionAs(demo);
  
    endSession();
    
    // TODO : demo should not be able to write
    
    // TODO : john should be able to write
    
    
  }

  public void _testContactService() throws Exception {
  /**
   * Test AddressBook
   */
  // create new address book:
    AddressBook rootBook1 = createAddressBook("group1", "group1", root);
    AddressBook rootBook2 = createAddressBook("group2", "group2", root);
    AddressBook sharedBook = createAddressBook("shareGroup", "shareGroup", root);
    //ContactGroup marryGroup_ = createContactGroup("group3", "group3", userMarry_);
    AddressBook johnBook = createAddressBook("group3", "group3", john);
    
  // get AddressBook:
    //assertNotNull(contactService_.getPersonalAdressBook(root, rootBook1.getId()));
    //assertNotNull(contactService_.getPersonalAdressBook(root, rootBook2.getId()));
    
  // get Groups:
    //assertEquals(contactService_.getPersonalAdressBooks(sProvider_, root).size(), 3);
    
  // update addressBook:
    //rootBook1.setName("newName");
    //contactService_.saveGroup(root, rootBook1, false);
    //assertEquals(rootBook1.getName(), "newName");
    
  //  add Group To Personal Contact
    Contact contact = createContact() ; 
    setContactInAddressBooks(contact, rootBook1, rootBook2);
    contact.setId(root) ;
    //contactService_.saveContact(sProvider_, root, contact, true);
    //assertEquals(contact.getAddressBook().length, 2);
    //contactService_.addGroupToPersonalContact(root, johnBook.getId());
    //contact = contactService_.getContact(root, contact.getId());
    //assertEquals(contact.getAddressBook().length, 3);
    
  //  remove group:
    //assertNotNull(contactService_.removeGroup(sProvider_, root, rootBook2.getId()));
    //assertNull(contactService_.getPersonalAdressBook(root, rootBook2.getId()));
    
  // share group:
    //sharedBook.setEditPermissionUsers(new String[]{john});
    //contactService.shareAddressBook(root, sharedBook.getId(), Arrays.asList(new String[]{john, demo}));
    
    //johnBook.setEditPermissionGroups(new String[]{root});
    //contactService.shareAddressBook(john, johnBook.getId(), Arrays.asList(new String[]{root}));
  
  // get shared addressbooks:
    //assertEquals(contactService.getAddressBooksSharedToUser(john).size(), 1);
    
  // get shared group:
    //assertEquals(contactService.getSharedAddressBook(john, sharedBook.getId()).getName(), "shareGroup");
    
  // remove User Share Address Book
    //assertEquals(contactService.getAddressBooksSharedToUser(demo).size(), 1);
    //contactService.unshareAddressBook(root, sharedBook.getId(), demo);
    //assertEquals(contactService.getAddressBooksSharedToUser(demo).size(), 0);
    
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
    contactService.saveContact(sessionProvider, root, contact1, true);
    contactService.saveContact(sessionProvider, root, contact2, true);
    contactService.saveContact(sessionProvider, root, contact4, true);
    
    contactService.saveContact(sessionProvider, john, contact3, true);
    //contactService_.saveContact(sProvider_, userMarry_, contact3, true);
    
    assertNotNull(contact1);
    assertNotNull(contact2);
    
  // get contact:
    assertNotNull(contactService.getContact(root, contact1.getId()));
    assertNotNull(contactService.getContact(root, contact2.getId()));
    
  //  get all contacts:
    assertNotNull(contactService.getAllContact(sessionProvider, root));
    assertEquals(contactService.getAllContact(sessionProvider, root).size(), 3);
    
  // get Contact page list by group
    assertNotNull(contactService.getContactPageListByGroup(sessionProvider, root, rootBook1.getId()));
    assertEquals(contactService.getContactPageListByGroup(sessionProvider, root, rootBook1.getId()).getAll().size(), 3);
    
  // get all email address by group:
    assertNotNull(contactService.getAllEmailAddressByGroup(sessionProvider, root, rootBook1.getId()));
    assertEquals(contactService.getAllEmailAddressByGroup(sessionProvider, root, rootBook1.getId()).size(), 3);
    
  // move contact:
    assertEquals(contactService.getContactPageListByGroup(sessionProvider, root, rootBook2.getId()).getAll().size(), 1);
    contact4.setAddressBook(new String[]{rootBook2.getId()});
    // move contact4 from contactGroup1 to contactGroup4, contactGroup1 and contactGroup4 is userRoot_'s --> type is PRIVATE
    contactService.moveContacts(sessionProvider, root, Arrays.asList(new Contact[]{contact4}), JCRDataStorage.PRIVATE);
    assertEquals(contactService.getContactPageListByGroup(sessionProvider, root, rootBook2.getId()).getAll().size(), 2);
    
  // get public contact:
    assertNotNull(contactService.getPublicContactsByAddressBook(sessionProvider, "/platform/users"));
    assertEquals(contactService.getPublicContactsByAddressBook(sessionProvider, "/platform/users").getAll().size(), 0);
    
  // get public contact:
    assertNull(contactService.getPublicContact(contact1.getId()));
    
  // get all email by public group:
    assertNotNull(contactService.getAllEmailByPublicGroup(root, "/platform/users"));
    
  // get property of contact:
    assertEquals(contactService.getContact(root, contact1.getId()).getFullName(), "fullName");
    
    // update contact:
    contact1.setFirstName("new first name");
    contact1.setOwner(true);
    contactService.saveContact(sessionProvider, root, contact1, false);
    assertEquals("new first name", contact1.getFirstName());
    
  // share contact to user:
    //contactService_.shareContact(sProvider_, userRoot_, new String[]{contact1.getId()}, Arrays.asList(new String[]{userMarry_}));
    contact2.setEditPermissionUsers(new String[]{john});
    contactService.shareContact(sessionProvider, root, new String[]{contact2.getId()}, Arrays.asList(new String[]{john, demo}));
    
  // save shared contact:
    contact2.setFullName("Mai Van Ha");
    contactService.saveSharedContact(john, contact2);
    assertEquals(contactService.getContact(root, contact2.getId()).getFullName(), "Mai Van Ha");
    
  // get shared contact:
    assertEquals(contactService.getSharedContactAddressBook(john, contact2.getId()).getFullName(), "Mai Van Ha");
    
  // get shared contacts:
    //assertNotNull(contactService_.getSharedContacts(userMarry_));
    //assertEquals(contactService_.getSharedContacts(userMarry_).getAll().size(), 1);
    assertNotNull(contactService.getSharedContacts(john));
    assertNotNull(contactService.getSharedContacts(demo));
  // get shared contact:
    assertNotNull(contactService.getSharedContact(sessionProvider, john, contact2.getId()));
    
  //  remove shared contact (remove viewed contact):
    assertEquals(contactService.getSharedContacts(john).getAll().size(), 1);
    contactService.removeUserShareContact(sessionProvider, root, contact2.getId(), john);
    assertEquals(contactService.getSharedContacts(john).getAll().size(), 0);
    
  // get Shared Contacts By AddressBook
    Contact shareContact = createContact();;
    shareContact.setAddressBook(new String[]{sharedBook.getId()});
    contactService.saveContact(sessionProvider, root, shareContact, true);
    
    assertEquals(contactService.getContactPageListByGroup(sessionProvider, root, sharedBook.getId()).getAll().size(), 1);
    SharedAddressBook sharedAddressBook = createShareAddressbook(sharedBook, root);
    assertEquals(contactService.getSharedContactsByAddressBook(sessionProvider, john, sharedAddressBook).getAll().size(), 1);
    
  // get all email by shared group:
    shareContact.setEmailAddress("maivanha1610@gmail.com");
    shareContact.setFullName("Mai Van Ha");
    contactService.saveContact(sessionProvider, root, shareContact, false);
    assertEquals(contactService.getAllEmailBySharedGroup(john, sharedBook.getId()).get(0), "maivanha1610@gmail.com");
    
  // remove contact in shared address book 
    contactService.removeSharedContact(sessionProvider, john, sharedBook.getId(), shareContact.getId());
    assertEquals(contactService.getContactPageListByGroup(sessionProvider, root, sharedBook.getId()).getAll().size(), 0);
    
  // save contact to share addressbook:
    contactService.saveContactToSharedAddressBook(john, sharedBook.getId(), shareContact, true);
    assertEquals(contactService.getContactPageListByGroup(sessionProvider, root, sharedBook.getId()).getAll().size(), 1);
    
  // get contacts by addressBookId:
    assertNotNull(contactService.getAllEmailAddressByGroup(sessionProvider, root, rootBook1.getId()));
    assertNotNull(contactService.getAllEmailAddressByGroup(sessionProvider, root, rootBook2.getId()));
    
  //  remove contact:
    assertNotNull(contactService.removeContacts(sessionProvider, root, Arrays.asList(new String[]{contact2.getId()})));
    assertNull(contactService.getContact(root, contact2.getId()));
    
  //  paste AddressBook
    contactService.pasteAddressBook(sessionProvider, john, johnBook.getId(), JCRDataStorage.PRIVATE, sharedBook.getId(), JCRDataStorage.SHARED);
    assertEquals(contactService.getSharedContactsByAddressBook(sessionProvider, john, sharedAddressBook).getAll().size(), 2);
    
  // paste contact:
    Map<String, String> contacts = new LinkedHashMap<String, String>() ;
    contacts.put(contact3.getId(), contact3.getContactType()) ;
    contactService.pasteContacts(sessionProvider, john, sharedBook.getId(), JCRDataStorage.SHARED, contacts);
    assertEquals(contactService.getSharedContactsByAddressBook(sessionProvider, john, sharedAddressBook).getAll().size(), 3);
    
	/**
	 * test Search
	 */
	// search contact:
	  ContactFilter contactFilter = createContactFilter("maivanha1610@gmail.com", new String(), new String(), new String(), new String(), 
	  													new String(), new String(), new String(), new String(), new String());
	  assertEquals(contactService.searchContact(sessionProvider, root, contactFilter).getAll().get(0).getEmailAddress(), 
			  		shareContact.getEmailAddress());
	  
	// search email:
	  contactFilter.setAccountPath(" /jcr:root/Users/root/ApplicationData/ContactApplication/contacts");
	  contactFilter.setCategories(new String[]{});
	  assertEquals(contactService.searchEmails(sessionProvider, root, contactFilter).size(), 1);
    
  /**
   * Test Tag:
   */
    Tag tag = createTag("tag1");
  // add new and get tag:
    contactService.addTag(sessionProvider, root, 
    					   Arrays.asList(new String[]{contact1.getId() + "::" + JCRDataStorage.PRIVATE, 
    									 				contact4.getId() + "::" + JCRDataStorage.PRIVATE}), 
    					   Arrays.asList(new Tag[]{tag}));
    assertNotNull(contactService.getTag(sessionProvider, root, tag.getId()));
    
  // update tag:
    tag.setName("Mai Van Ha");
    contactService.updateTag(sessionProvider, root, tag);
    assertEquals(contactService.getTag(sessionProvider, root, tag.getId()).getName(), "Mai Van Ha");
    
  // getTags:
    assertEquals(contactService.getTags(sessionProvider, root).size(), 1);
    
  // getContactPageListByTag
    assertEquals(contactService.getContactPageListByTag(sessionProvider, root, tag.getId()).getAll().size(), 2);
    
  // removeContactTag
    contactService.removeContactTag(sessionProvider, root, 
							    		Arrays.asList(new String[]{contact4.getId() + "::" + JCRDataStorage.PRIVATE}), 
							    		Arrays.asList(new String[]{tag.getId()}));
    assertEquals(contactService.getContactPageListByTag(sessionProvider, root, tag.getId()).getAll().size(), 1);
    
  // add tag:
    contactService.addTag(sessionProvider, root, 
    						Arrays.asList(new String[]{contact1.getId() + "::" + JCRDataStorage.PRIVATE,
    													contact4.getId() + "::" + JCRDataStorage.PRIVATE}), 
    						tag.getId());
    assertEquals(contactService.getContactPageListByTag(sessionProvider, root, tag.getId()).getAll().size(), 2);
    
  // remove tag:
    contactService.removeTag(sessionProvider, root, tag.getId());
    assertNull(contactService.getTag(sessionProvider, root, tag.getId()));
    
  /**
   * test export/ import:
   */
  // get import, export type:
    assertNotNull(contactService.getImportExportType());
    
  // get contact import/ export:
    assertNotNull(contactService.getContactImportExports("x-vcard"));
    
  }

  private void setContactInAddressBooks(Contact contact,
                                        AddressBook... groups) {
    String [] groupIds = new String [groups.length];
    int i = 0;
    for (AddressBook group : groups) {
      groupIds[i++] = group.getId();
    }
    contact.setAddressBook(groupIds);
  }
  
  private AddressBook createAddressBook(String name, String description, String owner) throws Exception{
    AddressBook contactGroup = new AddressBook();
    contactGroup.setName(name);
    contactGroup.setDescription(description);
    contactService.saveAddressBook(owner, contactGroup, true);
    return contactGroup;
  }
  
  private SharedAddressBook createShareAddressbook(AddressBook contactGroup, String userId) throws Exception{
    SharedAddressBook sharedAddressBook = new SharedAddressBook(contactGroup.getName(), contactGroup.getId(), userId);
    sharedAddressBook.setEditPermissionGroups(contactGroup.getEditPermissionGroups());
    sharedAddressBook.setEditPermissionUsers(contactGroup.getEditPermissionUsers());
    return sharedAddressBook;
  }
  
  private Contact createContact() throws Exception {
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
    ContactAttachment attachment = new ContactAttachment();
    attachment.setFileName("file1");
    attachment.setInputStream(new ByteArrayInputStream("should be image data".getBytes()));
    attachment.setMimeType("image/jpg");
    contact.setAttachment(attachment);

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