/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.contact.service.bench;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.exoplatform.contact.service.AddressBook;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;
import org.exoplatform.services.bench.DataInjector;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.services.security.ConversationState;

/**
 * Created by The eXo Platform SAS
 * @author <a href="mailto:quanglt@exoplatform.com">Le Thanh Quang</a>
 * Aug 2, 2011  
 */
public class ContactDataInjector extends DataInjector {

  private static Log log = ExoLogger.getExoLogger(ContactDataInjector.class);
  
  private static String markedAddrBookId = "MarkedAddressBook123456789";
  
  private ContactService contactService;
  
  private int maxAddressBooks = 5;
  
  private int maxContacts = 10;
  
  private boolean randomize = false;
  
  private Random rand = new Random();
  
  private Stack<String> catsStack = new Stack<String>();
  
  public ContactDataInjector(ContactService contactService, InitParams initParams) {
    this.contactService = contactService;
    initParams(initParams);
  }
  
  private AddressBook newAddressBook() {
    AddressBook addressBook = new AddressBook();
    addressBook.setName(randomWords(5));
    addressBook.setDescription(randomParagraphs(5));
    return addressBook;
  }
  
  private Contact newContact() {
    Contact contact = new Contact();
    contact.setFirstName(randomWords(1));
    contact.setLastName(randomWords(1));
    contact.setNote(randomParagraphs(3));
    contact.setHomeAddress(randomWords(5));
    contact.setEmailAddress("fake@example.com");
    ContactAttachment attachment = new ContactAttachment();
    attachment.setId("attachment" + IdGenerator.generate());
//    attachment.setInputStream(input);
//    contact.setAttachment(new ContactAttachment());
    return contact;
  }
  
  private int addressBookSize() {
    if (randomize) {
      return rand.nextInt(maxAddressBooks);
    }
    return maxAddressBooks;
  }
  
  private int contactsSize() {
    if (randomize) {
      return rand.nextInt(maxContacts);
    }
    return maxContacts;
  }
  
  private List<AddressBook> generateAddressBooks() {
    List<AddressBook> addressBooks = new ArrayList<AddressBook>();
    for (int i = 1; i < addressBookSize(); i++) {
      addressBooks.add(newAddressBook());
    }
    return addressBooks;
  }
  
  private List<Contact> generateContacts() {
    List<Contact> contacts = new ArrayList<Contact>();
    for (int i = 0; i < contactsSize(); i++) {
      contacts.add(newContact());
    }
    return contacts;
  }
  
  @Override
  public Log getLog() {
    return log;
  }

  @Override
  public boolean isInitialized() {
    // allow multi-injecting.
    return false;
  }

  /* (non-Javadoc)
   * @see org.exoplatform.services.bench.DataInjector#initParams(org.exoplatform.container.xml.InitParams)
   */
  @Override
  public void initParams(InitParams initParams) {
    ValueParam param = initParams.getValueParam("mA");
    if (param != null)
      maxAddressBooks = Integer.parseInt(param.getValue());
    param = initParams.getValueParam("mC");
    if (param != null)
      maxContacts = Integer.parseInt(param.getValue());
    param = initParams.getValueParam("rand");
    if (param != null) 
      randomize = Boolean.parseBoolean(param.getValue());
  }

  @Override
  public void inject() throws Exception {
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    catsStack.clear();
    List<AddressBook> addressBooks = generateAddressBooks();
    for (int i = 0; i < addressBooks.size(); i++) {
      AddressBook ab = addressBooks.get(i);
      log.info(String.format("\tCreate Address Book " + (i + 1)  + "/" + addressBooks.size() + " .......... "));
      contactService.saveAddressBook(userId, ab, true);
      catsStack.push(ab.getId());
      List<Contact> contacts = generateContacts();
      int contactSize = contacts.size();
      for (int j = 0; j < contactSize; j++) {
        Contact c = contacts.get(j);
        c.setAddressBook(new String[] {ab.getId()});
        contactService.saveContact(userId, c, true);
        log.info("\t\t Adding Contact " + (j + 1) + "/" + contactSize + " to the address book");
      }
      
    }
  }

  @Override
  public void reject() throws Exception {
    String userId = ConversationState.getCurrent().getIdentity().getUserId();
    while (!catsStack.isEmpty()) {
      contactService.removeAddressBook(userId, catsStack.pop());
    }
  }
  
}
