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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.GroupContactData;
import org.exoplatform.contact.service.Tag;



/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class ContactService_old extends BaseContactTestCase{
  private String username = "exo";
  
  public void testContactService() throws Exception {
    //assertNull(null) ;
  }
  
 /* public void  testContact() throws Exception {   
    Contact contact = createContact();
    contact.setCategories(new String[] { "friend", "work" });
    
    // addContact
    contactService_.saveContact(username, contact, true);
    
    // getContact
    assertNotNull(contactService_.getContact(username, contact.getId()));
    
    // updateContact
    contact.setHomeAddress("Thanh Xuan Ha Noi");
    contactService_.saveContact(username, contact, false);
    assertEquals("Thanh Xuan Ha Noi", contactService_.getContact(username, contact.getId()).getHomeAddress());
    
    // getListContact
    List<Contact> contacts = contactService_.getAllContact(username);
    assertEquals(contacts.size(), 1);
    
    // get contact by groupId
    Contact contact2 = createContact();
    contact2.setCategories(new String[] {"friend", "work"});
    contactService_.saveContact(username, contact2, true);
    Contact contact3 = createContact();
    contact3.setCategories(new String[] {"friendd", "workk"});
    contactService_.saveContact(username, contact3, true);
    //contacts = contactService_.getContactsByGroup(username,"work" );
    assertEquals(contacts.size(), 2) ;       
    
    // share contact
//    Contact  = contactService_.getContact(username, "id1") ;
//    assertNotNull(contactService_.shareContact(cont, new String[]{"users"})) ;
//    add
//    Contact cont2 = contactService_.getContact(username, "id2");
//    assertNotNull(contactService_.shareContact(cont2, new String[]{"users"})) ;
    
//    List<GroupContactData> sharedContact = contactService_.getPublicContacts(new String[]{"users"}) ;
//    assertEquals(sharedContact.size(), 1) ;
//    System.out.println("sharedContact.size() ==== " + sharedContact.size()) ;
//    assertEquals(sharedContact.get(0).getContacts().size(), 2) ;
//    System.out.println("sharedContact.get(0).getContacts().size() ==== " + sharedContact.get(0).getContacts().size()) ;
//    assertEquals(sharedContact.get(0).getName(),"users") ;
//    System.out.println("sharedContact.get(0).getName() ==== " + sharedContact.get(0).getName()) ;
    
    // removeContact
    List<String> contactIds = new ArrayList<String>() ;
    contactIds.add(contact.getId()) ;
    assertNotNull(contactService_.removeContacts(username, contactIds));
    assertNull(contactService_.getContact(username, contact.getId())); 
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
  
  public void testGroupContact() throws Exception {
    ContactGroup contactGroup = new ContactGroup();
    contactGroup.setName("name grroup");
    contactGroup.setDescription("dess group") ;
    
    // test addGroup
    contactService_.saveGroup(username, contactGroup, true);
    
    // test getGroup
    assertNotNull(contactService_.getGroup(username, contactGroup.getId()));

    // test updateGroup
    contactGroup.setName("groupper");
    contactService_.saveGroup(username, contactGroup, false);
    assertEquals("groupper", contactService_.getGroup(username, contactGroup.getId()).getName());
    
    Contact contact = createContact();
    contact.setCategories(new String[] { contactGroup.getId() , "exoooo"});
    contactService_.saveContact(username, contact, true);
    
    // test removeGroup
    assertNotNull(contactService_.removeGroup(username, contactGroup.getId()));
    assertNull(contactService_.getGroup(username, contactGroup.getId()));
  } 
  
  public void testTag() throws Exception {  
  
    
    
    // addTag & getContactsByTag
    Contact contact1 = createContact();
    contactService_.saveContact(username, contact1, true);
    Contact contact2 = createContact();
    contact2.setCategories(new String[] {"friend", "work"});
    contactService_.saveSharedContact(contact2, true); 
    List<String> contactIds = new ArrayList<String>() ;
    contactIds.add(contact1.getId()) ;
    contactIds.add(contact2.getId());
    
    Tag tag = new Tag() ;
    tag.setName("Company") ;
    
    List<Tag> tags = new ArrayList<Tag>();
    tags.add(tag);    
    contactService_.addTag(username, contactIds, tags) ;
    assertEquals(contactService_.getContactsByTag(username, tag.getName()).size(), 2);
    
    
    
//    Tag tag2 = new Tag() ;
//    tag2.setName("Customer") ;
//    contactService_.addTag(username, contactIds, tag2) ;
//    Contact c = contactService_.getContact(username, contact1.getId());
//    //String[] tags = c.getTags(); 
//    
//    List<Contact> contacts = contactService_.getContactsByTag(username, tag2.getName());
//    System.out.println("\n\n get contact by tag : " + contacts.size() + "\n\n");
//    
//    // getTags
//    assertEquals(contactService_.getTags(username).size(), 2);
//    
//    // removeTag
//    assertNotNull(contactService_.removeTag(username, tag.getName()));
//    assertEquals(contactService_.getTags(username).size(), 1);
  }
  
  public void  testSharedContact() throws Exception {
    Contact contact = createContact();
    contact.setCategories(new String[] {"friend", "work"});
    contactService_.saveSharedContact(contact, true);
    
    // test getSharedContact
    assertNotNull(contactService_.getSharedContact(contact.getId()));

    //test updateSharedContact   
    contact.setHomeAddress("Cau Giay");
    contactService_.saveSharedContact(contact, false);
    assertEquals("Cau Giay", contactService_.getSharedContact(contact.getId()).getHomeAddress());     
    
    //  getSharedContacts by groups
    Contact contact2 = createContact();
    contact2.setCategories(new String[] {"friend", "work"});
    contactService_.saveSharedContact(contact2, true); 
    
    List<GroupContactData> sharedContact = contactService_.getSharedContacts(new String[]{"work"}) ;
    assertEquals(sharedContact.size(), 1) ;
    assertEquals(sharedContact.get(0).getContacts().size(), 2) ;
    
    //  test removeSharedContact
    assertNotNull(contactService_.removeSharedContact(contact.getId()));
    assertNull(contactService_.getSharedContact(contact.getId()));
  }
  */
}