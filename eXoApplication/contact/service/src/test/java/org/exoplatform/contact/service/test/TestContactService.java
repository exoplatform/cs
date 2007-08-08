/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service.test;

import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.GroupContactData;



/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 3, 2007  
 */
public class TestContactService extends BaseContactTestCase{

  public void testContactService() throws Exception {
    assertNull(null) ;
  }
  
  public void  testContact() throws Exception {
    Contact contact = createContact("id1", new String[] {"friend", "work"});

    // addContact
    contactService_.saveContact("exo", contact, true);
    
    // getContact
    assertNotNull(contactService_.getContact("exo", contact.getId()));
    
    // updateContact
    contact.setHomeAddress("Thanh Xuan Ha Noi");
    contactService_.saveContact("exo", contact, false);
    assertEquals("Thanh Xuan Ha Noi", contactService_.getContact("exo", contact.getId()).getHomeAddress());
    
    // getListContact
    List<Contact> contacts = contactService_.getAllContact("exo");
    assertEquals(contacts.size(), 1);
    
    // get contact by groupId
    Contact contact2 = createContact("id2", new String[] {"friend", "work"});
    contactService_.saveContact("exo", contact2, true);
    Contact contact3 = createContact("id3", new String[] {"friendd", "workk"});
    contactService_.saveContact("exo", contact3, true);
    contacts = contactService_.getContactsByGroup("exo","work" );
    assertEquals(contacts.size(), 2) ;
    
    //share contact
    Contact cont = contactService_.getContact("exo", "id1") ;
    assertNotNull(contactService_.shareContact(cont, new String[]{"users"})) ;
    //add
    Contact cont2 = contactService_.getContact("exo", "id2");
    assertNotNull(contactService_.shareContact(cont2, new String[]{"users"})) ;
    
    List<GroupContactData> sharedContact = contactService_.getPublicContacts(new String[]{"users"}) ;
    assertEquals(sharedContact.size(), 1) ;
    System.out.println("sharedContact.size() ==== " + sharedContact.size()) ;
    assertEquals(sharedContact.get(0).getContacts().size(), 2) ;
    System.out.println("sharedContact.get(0).getContacts().size() ==== " + sharedContact.get(0).getContacts().size()) ;
    assertEquals(sharedContact.get(0).getName(),"users") ;
    System.out.println("sharedContact.get(0).getName() ==== " + sharedContact.get(0).getName()) ;
    
    // removeContact
    assertNotNull(contactService_.removeContact("exo", contact.getId()));
    assertNull(contactService_.getContact("exo", contact.getId())); 
  }
  
  private Contact createContact(String id, String[] categories) {
  	Contact contact = new Contact();
    contact.setId(id);
    contact.setLastName("Hung");
    contact.setFirstName("HoangQuang");
    contact.setEmailAddress("quanghung@yahoo.com");
    contact.setHomePhone("42749283");
    contact.setWorkPhone("developer");
    contact.setHomeAddress("Ha Noi");
    contact.setCountry("Viet Nam");
    contact.setPostalCode("084");
    contact.setPersonalSite("homepage");
    contact.setOrganization("test");
    contact.setJobTitle("Developer");
    contact.setCompanyAddress("Tran Duy Hung");
    contact.setCompanySite("eXo");
    contact.setCategories(categories);
    return contact;
  }
  
  public void testGroupContact() throws Exception {
    ContactGroup contactGroup = new ContactGroup();
    contactGroup.setId("id");
    contactGroup.setName("groupping");
    
    // test addGroup
    contactService_.saveGroup("exo", contactGroup, true);
    
    // test getGroup
    assertNotNull(contactService_.getGroup("exo", contactGroup.getId()));
    
    // test updateGroup
    contactGroup.setName("groupper");
    contactService_.saveGroup("exo", contactGroup, false);
    assertEquals("groupper", contactService_.getGroup("exo", contactGroup.getId()).getName());
    
    // test removeGroup
    assertNotNull(contactService_.removeGroup("exo", contactGroup.getId()));
    assertNull(contactService_.getGroup("exo", contactGroup.getId()));
  } 
  
  public void  testSharedContact() throws Exception {
    Contact contact = createContact("1", new String[] {"friend", "work"});
    contactService_.saveSharedContact(contact, true);
    
    // test getSharedContact
    assertNotNull(contactService_.getSharedContact(contact.getId()));

    //test updateSharedContact   
    contact.setHomeAddress("Cau Giay");
    contactService_.saveSharedContact(contact, false);
    assertEquals("Cau Giay", contactService_.getSharedContact(contact.getId()).getHomeAddress());     
    
    //  getSharedContacts by groups
    Contact contact2 = createContact("2", new String[] {"friend", "work"});
    contactService_.saveSharedContact(contact2, true); 
    
    List<GroupContactData> sharedContact = contactService_.getSharedContacts(new String[]{"work"}) ;
    assertEquals(sharedContact.size(), 1) ;
    assertEquals(sharedContact.get(0).getContacts().size(), 2) ;
    
    //  test removeSharedContact
    assertNotNull(contactService_.removeSharedContact(contact.getId()));
    assertNull(contactService_.getSharedContact(contact.getId()));
  }
}