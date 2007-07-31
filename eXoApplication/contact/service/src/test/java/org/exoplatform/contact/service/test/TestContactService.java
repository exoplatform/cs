/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reservd.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service.test;

import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;



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
    Contact contact = new Contact();
    contact.setId("id");
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
    contact.setGroups(new String[] {"friend", "work"});
    // test addContact
    contactService_.saveContact("exo", contact, true);
    
    // test getContact
    assertNotNull(contactService_.getContact("exo", contact.getId()));
    //test updateContact
    contact.setHomeAddress("Thanh Xuan Ha Noi");
    contactService_.saveContact("exo", contact, false);
    assertEquals("Thanh Xuan Ha Noi", contactService_.getContact("exo", contact.getId()).getHomeAddress());
    
    //test getListContact
    List<Contact> contacts = contactService_.getAllContact("exo");
    assertEquals(contacts.size(), 1);
    
    // get contact by groupId
    contacts = contactService_.getContactsByGroup("exo","work" );
    assertNotNull(contacts);
    assertEquals(contacts.size(), 1) ;
    
    //test removeContact
    assertNotNull(contactService_.removeContact("exo", contact.getId()));
    assertNull(contactService_.getContact("exo", contact.getId())); 
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
  
  
}