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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import net.wimpi.pim.Pim;
import net.wimpi.pim.contact.basicimpl.SimpleExtension;
import net.wimpi.pim.contact.io.ContactMarshaller;
import net.wimpi.pim.contact.io.ContactUnmarshaller;
import net.wimpi.pim.contact.io.vcard.GenericExtensionItemHandler;
import net.wimpi.pim.contact.io.vcard.ItemHandlerManager;
import net.wimpi.pim.contact.model.Address;
import net.wimpi.pim.contact.model.Communications;
import net.wimpi.pim.contact.model.EmailAddress;
import net.wimpi.pim.contact.model.Extensions;
import net.wimpi.pim.contact.model.Image;
import net.wimpi.pim.contact.model.Organization;
import net.wimpi.pim.contact.model.OrganizationalIdentity;
import net.wimpi.pim.contact.model.PersonalIdentity;
import net.wimpi.pim.contact.model.PhoneNumber;
import net.wimpi.pim.factory.ContactIOFactory;
import net.wimpi.pim.factory.ContactModelFactory;
import net.wimpi.pim.util.versitio.versitException;

import org.exoplatform.calendar.service.Reminder;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactImportExport;
import org.exoplatform.contact.service.ContactPageList;
import org.exoplatform.contact.service.SharedAddressBook;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.container.ExoContainer;
import org.exoplatform.container.RootContainer;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
//import org.exoplatform.calendar.service.R;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.exoplatform.ws.frameworks.json.impl.JsonGeneratorImpl;
import org.exoplatform.ws.frameworks.json.value.JsonValue;

/**
 * Author : Huu-Dung Kieu huu-dung.kieu@bull.be 16 oct. 07
 */
public class VCardImportExport implements ContactImportExport {
  private static String          eXoGender   = "EXO-GENDER";
  private static String          eXoExoId    = "EXO-EXOID";
  private static String          eXoAolId    = "EXO-AOLID";
  private static String          eXoGoogleId = "EXO-GOOGLEID";
  private static String          eXoIcqId    = "EXO-ICQID";
  private static String          eXoIcrId    = "EXO-ICRID";
  private static String          eXoSkypeId  = "EXO-SKYPEID";
  private static String          eXoMsnId    = "EXO-MSNID";
  private static String          eXoYahooId  = "EXO-YAHOOID";
  private static int             maxLength = 150 ;
  
  private static String ENCODING = "UTF-8";
  private JCRDataStorage storage_ ;
  
  public VCardImportExport (JCRDataStorage storage) throws Exception{
    storage_ = storage ;
  }
  
  public OutputStream exportContact(SessionProvider sProvider, String username, String[] addressBookIds) throws Exception {
    List<Contact> contactList = new ArrayList<Contact>() ;
    List<String> privateAddress = new ArrayList<String> () ;
    List<String> publicAddress = new ArrayList<String> () ;
    for(String address : addressBookIds){
      Node contactGroupHome = storage_.getPersonalAddressBooksHome(sProvider, username) ;
      Node publicContactGroupHome = storage_.getPublicContactsHome(sProvider);
      try {
      if(contactGroupHome.hasNode(address)) {
        privateAddress.add(address) ;
      } else if (publicContactGroupHome.hasNode(address)){
        publicAddress.add(address) ;
      } else {
        String[] array = address.split(JCRDataStorage.HYPHEN) ;
        if(array.length == 2) {
            ContactPageList pageList = storage_.getSharedContactsByAddressBook(
              sProvider, username, new SharedAddressBook(null, array[0], array[1])) ;
    	    if (pageList.getAvailable() + contactList.size() > Utils.limitExport) throw new ArrayIndexOutOfBoundsException() ;
            contactList.addAll(pageList.getAll()) ;
        }         
      }
      } catch(RepositoryException re) {
      publicAddress.add(address) ;
      }     
    }
    if(privateAddress.size() > 0) {
      ContactFilter filter = new ContactFilter() ;
      filter.setCategories(privateAddress.toArray(new String[]{})) ;
      ContactPageList pageList = storage_.getContactPageListByGroup(sProvider, username, filter, JCRDataStorage.PRIVATE) ;
      if (pageList.getAvailable() + contactList.size() >= Utils.limitExport) throw new ArrayIndexOutOfBoundsException() ;
      contactList.addAll(pageList.getAll()) ;
    }
    if(publicAddress.size() > 0) {
      ContactFilter filter = new ContactFilter() ;
      filter.setCategories(publicAddress.toArray(new String[]{})) ;
      ContactPageList pageList = storage_.getContactPageListByGroup(sProvider, username, filter, JCRDataStorage.PUBLIC) ;
      if (pageList.getAvailable() + contactList.size() >= Utils.limitExport) throw new ArrayIndexOutOfBoundsException() ;
      contactList.addAll(pageList.getAll()) ;
    }
    if(contactList.size() > 0) {
      return exportContact(username, contactList) ;
    }
    return null; 
  }
  
  public OutputStream exportContact(String username, List<Contact> contacts) throws Exception {
    ContactIOFactory ciof = Pim.getContactIOFactory();
    ContactModelFactory cmf = Pim.getContactModelFactory();
    ContactMarshaller marshaller = ciof.createContactMarshaller();
    
    // if needed, we'll change or remove the encoding
    marshaller.setEncoding(ENCODING);

    // converting eXo contacts to Pim contacts
    net.wimpi.pim.contact.model.Contact[] pimContacts = new net.wimpi.pim.contact.model.Contact[contacts.size()];
    int i = 0 ;
    for (Contact contact : contacts) {    
      pimContacts[i] = cmf.createContact();
      // converting now from an eXo contact to PimContact

      // a personal identity
      PersonalIdentity pid = cmf.createPersonalIdentity();
      pid.setFormattedName(nullToEmptyString(contact.getFullName()));
      pid.setFirstname(nullToEmptyString(contact.getFirstName()).replaceAll(";", "_"));
      pid.setLastname(nullToEmptyString(contact.getLastName()).replaceAll(";", "_"));
      
      String firstName = contact.getFirstName();
      if ((firstName != null) && !firstName.equals("")) {
        StringTokenizer tokens = new StringTokenizer(firstName, ",", false);
        while (tokens.hasMoreTokens())
          pid.addAdditionalName(tokens.nextToken().trim().replaceAll(";", "_"));
      } else
        pid.addAdditionalName("");

      String nickName = contact.getNickName();
      if ((nickName != null) && !nickName.equals("")) {
        StringTokenizer tokens = new StringTokenizer(nickName, ",", false);
        while (tokens.hasMoreTokens())
          pid.addNickname(tokens.nextToken().trim());
      } else
        pid.addNickname("");

      pid.setBirthDate(contact.getBirthday());
      
      Image photo = cmf.createImage();
      ContactAttachment attachment = contact.getAttachment();
      if (attachment != null) {
        InputStream is = attachment.getInputStream();
        if (is != null) {
          byte[] data = new byte[is.available()];
          is.read(data);
          photo.setContentType(attachment.getMimeType());
          photo.setData(data);
          pid.setPhoto(photo);
        }
      }

      pimContacts[i].setPersonalIdentity(pid);

      // home address
      Address addr = cmf.createAddress();
      addr.setHome(true);
      addr.setStreet(contact.getHomeAddress());
      addr.setCity(contact.getHomeCity());
      addr.setPostalCode(contact.getHomePostalCode());
      addr.setRegion(contact.getHomeState_province());
      addr.setCountry(contact.getHomeCountry());
      pimContacts[i].addAddress(addr);

      // work address
      addr = cmf.createAddress();
      addr.setWork(true);
      addr.setStreet(contact.getWorkAddress());
      addr.setCity(contact.getWorkCity());
      addr.setPostalCode(contact.getWorkPostalCode());
      addr.setRegion(contact.getWorkStateProvince());
      addr.setCountry(contact.getWorkCountry());
      pimContacts[i].addAddress(addr);

      // communications
      Communications comm = cmf.createCommunications();
      pimContacts[i].setCommunications(comm);

      // email address
      
      String strEmail = contact.getEmailAddress() ;
      if (strEmail != null) {
        EmailAddress email ;
        StringTokenizer tokens = new StringTokenizer(strEmail, ";", false);
        while (tokens.hasMoreTokens()) {
          email = cmf.createEmailAddress();
          email.setAddress(tokens.nextToken().trim());
          comm.addEmailAddress(email);
        }
      }
      

      // phone numbers
      addPhoneNumber(cmf, comm, contact.getMobilePhone(), false, false, true);
      addPhoneNumber(cmf, comm, contact.getHomePhone1(), true, false, false);
      addPhoneNumber(cmf, comm, contact.getHomePhone2(), true, false, false);
      addPhoneNumber(cmf, comm, contact.getWorkPhone1(), false, false, false);
      addPhoneNumber(cmf, comm, contact.getWorkPhone2(), false, false, false);
      addPhoneNumber(cmf, comm, contact.getHomeFax(), true, true, false);
      addPhoneNumber(cmf, comm, contact.getWorkFax(), false, true, false);

      // an organizational identity
      OrganizationalIdentity orgid = cmf.createOrganizationalIdentity();
      orgid.setTitle(nullToEmptyString(contact.getJobTitle()));
      Organization org = cmf.createOrganization();
      org.setURL(contact.getWebPage());

      orgid.setOrganization(org);
      pimContacts[i].setOrganizationalIdentity(orgid);

      // some simple extension
      Extensions extensions = cmf.createExtensions();
      String gender = contact.getGender();
      if ((gender == null) || gender.equalsIgnoreCase("gender"))
        gender = "";
      addExtension(extensions, eXoGender, gender);
      addExtension(extensions, eXoExoId, nullToEmptyString(contact.getExoId()));
      addExtension(extensions, eXoAolId, nullToEmptyString(contact.getAolId()));
      addExtension(extensions, eXoGoogleId, nullToEmptyString(contact.getGoogleId()));
      addExtension(extensions, eXoIcqId, nullToEmptyString(contact.getIcqId()));
      addExtension(extensions, eXoIcrId, nullToEmptyString(contact.getIcrId()));
      addExtension(extensions, eXoSkypeId, nullToEmptyString(contact.getSkypeId()));
      addExtension(extensions, eXoMsnId, nullToEmptyString(contact.getMsnId()));
      addExtension(extensions, eXoYahooId, nullToEmptyString(contact.getYahooId()));

      // add the extension to the contact
      pimContacts[i].setExtensions(extensions);

      pimContacts[i].setURL(contact.getPersonalSite());

      pimContacts[i].setNote(contact.getNote());

      pimContacts[i].setCurrentRevisionDate(contact.getLastUpdated());
      i++ ;
    }

    // The OutputStream that will be returned
    OutputStream out = new ByteArrayOutputStream();
    marshaller.marshallContacts(out, pimContacts);

    return out;
  }
  
  public void importContact(SessionProvider sProvider, String username, InputStream input, String groupId) throws Exception {
    ContactIOFactory ciof = Pim.getContactIOFactory();
    ContactUnmarshaller unmarshaller = ciof.createContactUnmarshaller();

    // unmarshall contact
    addExtensionHandler(eXoGender);
    addExtensionHandler(eXoExoId);
    addExtensionHandler(eXoAolId);
    addExtensionHandler(eXoGoogleId);
    addExtensionHandler(eXoIcqId);
    addExtensionHandler(eXoIcrId);
    addExtensionHandler(eXoSkypeId);
    addExtensionHandler(eXoMsnId);
    addExtensionHandler(eXoYahooId);
    unmarshaller.setStrict(false);
    unmarshaller.setEncoding(ENCODING);

    net.wimpi.pim.contact.model.Contact[] pimContacts = unmarshaller.unmarshallContacts(input);    
    if (pimContacts == null || pimContacts.length == 0) throw new Exception() ;
    if (pimContacts.length > Utils.limitExport) throw new IndexOutOfBoundsException() ;
    
    Reminder re = new Reminder() ;
    JsonGeneratorImpl generatorImpl = new JsonGeneratorImpl();
    re.setSummary("Importing..") ;
    re.setDescription("Imported") ;
    re.setReminderOwner(username) ;
    re.setReminderType(Reminder.TYPE_POPUP) ;
    re.setFromDateTime(new Date()) ;
      
    for (int index = 0; index < pimContacts.length; index++) {
      Contact contact = new Contact();
      PersonalIdentity identity = pimContacts[index].getPersonalIdentity();
      String additionName = null ;
      try {
        additionName = identity.getAdditionalName(0);
      } catch (IndexOutOfBoundsException e) {
        additionName = "" ;
      }
      String fullName = nullToEmptyString(identity.getFormattedName());
      contact.setFullName(fullName);
      String lastName = identity.getLastname();
      String firstName = identity.getFirstname();
      
      // add 26-8
      if (fullName == null || fullName.length() == 0) {
        fullName = firstName + " " + lastName ;
        contact.setFullName(fullName) ;
      }
      int indexComma = fullName.indexOf(";");
      if (indexComma >= 0) {
        int indexSpace = fullName.indexOf(" ") ;
        firstName = fullName.substring(0, indexSpace).trim() ;
        contact.setFirstName(firstName) ;
        lastName = fullName.substring(indexSpace, fullName.length()).trim() ;
      } else if (firstName != null && firstName.length() > 0){
        if (firstName.trim().equals(additionName.trim())) contact.setFirstName(firstName) ;
        else contact.setFirstName(firstName + " " + additionName);
      }
       
      contact.setLastName(lastName); 
      
      int size = identity.getAdditionalNameCount();      
      String nickName = "";
      size = identity.getNicknameCount();
      for (int i = 0; i < size; i++) {
        if (i > 0)
          nickName += ", ";
        nickName = identity.getNickname(i);
      }
      contact.setNickName(nickName);

      contact.setBirthday(identity.getBirthDate());
      ContactAttachment attachment = new ContactAttachment();
      Image photo = identity.getPhoto();
      if (photo != null) {
        InputStream is = photo.getInputStream();
        if (is != null) {
          attachment.setInputStream(is);
          String filename = lastName;
          if ((filename == null) || filename.equals(""))
            filename = firstName;
          attachment.setFileName(filename + ".photo");
          attachment.setMimeType(photo.getContentType());
          contact.setAttachment(attachment);
        }
      }

      OrganizationalIdentity orgid = pimContacts[index].getOrganizationalIdentity();
      if (orgid != null)
        contact.setJobTitle(orgid.getTitle());
        
      // addresses iterator
      for (Iterator iters = pimContacts[index].getAddresses(); iters.hasNext();) {
        Address addr = (Address) iters.next();
        if (addr.isHome()) {
          contact.setHomeAddress(addr.getStreet());
          contact.setHomeCity(addr.getCity());
          contact.setHomeState_province(addr.getRegion());
          contact.setHomePostalCode(addr.getPostalCode());
          contact.setHomeCountry(addr.getCountry());

        } else if (addr.isWork()) {
          contact.setWorkAddress(addr.getStreet());
          contact.setWorkCity(addr.getCity());
          contact.setWorkStateProvince(addr.getRegion());
          contact.setWorkPostalCode(addr.getPostalCode());
          contact.setWorkCountry(addr.getCountry());
        }
      } // end of addresses for

      String mobilePhone = null;

      String homePhone1 = null;
      String homePhone2 = null;
      String homeFax = null;

      String workPhone1 = null;
      String workPhone2 = null;
      String workFax = null;
      Communications communication = pimContacts[index].getCommunications();
      if (communication != null) {
        for (Iterator iters = communication.getPhoneNumbers(); iters.hasNext();) {
          PhoneNumber phone = (PhoneNumber) iters.next();
          if (phone.isHome() && phone.isFax()) {
            homeFax = phone.getNumber();            
          } else if (phone.isHome() && phone.isVoice()) {
            if (homePhone1 == null)
              homePhone1 = phone.getNumber();
            else if (homePhone2 == null)
              homePhone2 = phone.getNumber();
          } else if (phone.isWork() && phone.isFax()) {
            workFax = phone.getNumber();
          } else if (phone.isWork() && phone.isVoice()) {
            if (workPhone1 == null)
              workPhone1 = phone.getNumber();
            else if (workPhone2 == null)
              workPhone2 = phone.getNumber();
          } else if (phone.isCellular()) {
            mobilePhone = phone.getNumber();
          } else if (phone.isHome()) { // add to fix bug cs-1478
            if (homePhone1 == null)
              homePhone1 = phone.getNumber();
            else if (homePhone2 == null)
              homePhone2 = phone.getNumber();
          }
        }
//      add to fix bug cs-1478
        for (Iterator iters = communication.getPhoneNumbers(); iters.hasNext();) {
          PhoneNumber phone = (PhoneNumber) iters.next();
          if (phone.isHome() || phone.isWork() || phone.isCellular()) continue ;
          if (homePhone1 == null)
            homePhone1 = phone.getNumber();
          else if (homePhone2 == null)
            homePhone2 = phone.getNumber();
          else if (workPhone1 == null)
            workPhone1 = phone.getNumber();
          else if (workPhone2 == null)
            workPhone2 = phone.getNumber();
        }
        String emailAddress = "";
        for (Iterator iters = communication.getEmailAddresses() ; iters.hasNext();) {
          EmailAddress email = (EmailAddress) iters.next();
          if (!emailAddress.equals(""))
            emailAddress += "; ";
          emailAddress += email.getAddress();
        }
        contact.setEmailAddress(emailAddress);
      }

      if (mobilePhone != null)
        contact.setMobilePhone(mobilePhone);

      if (homePhone1 != null) {
        contact.setHomePhone1(homePhone1);
        if (homePhone2 != null)
          contact.setHomePhone2(homePhone2);
      }

      if (homeFax != null)
        contact.setHomeFax(homeFax);

      if (workPhone1 != null) {
        contact.setWorkPhone1(workPhone1);
        if (workPhone2 != null)
          contact.setWorkPhone2(workPhone2);
      }

      if (workFax != null)
        contact.setWorkFax(workFax);

      contact.setPersonalSite(pimContacts[index].getURL());
      if ((pimContacts[index].getOrganizationalIdentity() != null)
          && (pimContacts[index].getOrganizationalIdentity().getOrganization() != null))
        contact.setWebPage(pimContacts[index].getOrganizationalIdentity().getOrganization().getURL());

      Extensions extensions = pimContacts[index].getExtensions();
      if (extensions != null) {
        SimpleExtension ext = (SimpleExtension) extensions.get(eXoGender);
        if (ext != null)
          contact.setGender(ext.getValue());

        ext = (SimpleExtension) extensions.get(eXoExoId);
        if (ext != null)
          contact.setExoId(ext.getValue());

        ext = (SimpleExtension) extensions.get(eXoAolId);
        if (ext != null)
          contact.setAolId(ext.getValue());

        ext = (SimpleExtension) extensions.get(eXoGoogleId);
        if (ext != null)
          contact.setGoogleId(ext.getValue());

        ext = (SimpleExtension) extensions.get(eXoIcqId);
        if (ext != null)
          contact.setIcqId(ext.getValue());

        ext = (SimpleExtension) extensions.get(eXoIcrId);
        if (ext != null)
          contact.setIcrId(ext.getValue());

        ext = (SimpleExtension) extensions.get(eXoSkypeId);
        if (ext != null)
          contact.setSkypeId(ext.getValue());

        ext = (SimpleExtension) extensions.get(eXoMsnId);
        if (ext != null)
          contact.setMsnId(ext.getValue());

        ext = (SimpleExtension) extensions.get(eXoYahooId);
        if (ext != null)
          contact.setYahooId(ext.getValue());

      }
      if (pimContacts[index].getNote() != null)
        contact.setNote(pimContacts[index].getNote().replaceAll("\\\\n", "\n"));
      
      Date revisionDate = pimContacts[index].getCurrentRevisionDate();
      
      if (revisionDate != null)
        contact.setLastUpdated(revisionDate);

      // ////////////////////////////////
      // Now we have the contact object
      // Then store it to JCR storage
      // ////////////////////////////////
      
      if (groupId.contains(JCRDataStorage.HYPHEN)) {
        String newGroupId = groupId.replace(JCRDataStorage.HYPHEN, "") ;
        contact.setAddressBook(new String[] { newGroupId }) ;
        storage_.saveContactToSharedAddressBook(username, newGroupId, contact, true) ;
      } else {
        contact.setAddressBook(new String[] { groupId }) ;
        storage_.saveContact(username, contact, true);
      }
      re.setSummary(String.valueOf(index + 1) + " contacts imported ...") ;
      JsonValue json = generatorImpl.createJsonObject(re);
      ContinuationService continuation = getContinuationService() ;
      continuation.sendMessage(username, "/eXo/Application/Contact/messages", json, re.toString());
    }
  }

  protected ContinuationService getContinuationService() {
    ExoContainer container = RootContainer.getInstance();
    container = ((RootContainer)container).getPortalContainer("portal");
    ContinuationService continuation = (ContinuationService) container.getComponentInstanceOfType(ContinuationService.class);
    return continuation;

  }
  
  private void addPhoneNumber(ContactModelFactory cmf, Communications comm, String number,
      boolean isHome, boolean isFax, boolean isCellular) {
    if ((number != null) && !number.equals("")) {
      PhoneNumber phone = cmf.createPhoneNumber();
      phone.setNumber(number);
      if (isCellular)
        phone.setCellular(true);
      else {
        if (isHome)
          phone.setHome(true);
        else
          phone.setWork(true);

        if (isFax)
          phone.setFax(true);
        else
          phone.setVoice(true);
      }
      comm.addPhoneNumber(phone);
    }
  }

  private void addExtension(Extensions extensions, String eXoKey, String eXoValue)
      throws versitException {
    SimpleExtension ext = new SimpleExtension(eXoKey);
    ext.addValue(eXoValue);
    extensions.add(ext);
    // add the handler, so the marshalling will work
    if (!ItemHandlerManager.getReference().hasHandler(ext.getIdentifier()))
      ItemHandlerManager.getReference().addExtensionHandler(ext.getIdentifier(),
          new GenericExtensionItemHandler(ext));
  }

  private void addExtensionHandler(String eXoKey) throws versitException {
    SimpleExtension ext = new SimpleExtension(eXoKey);
    // add the handler, so the marshalling will work
    if (!ItemHandlerManager.getReference().hasHandler(ext.getIdentifier())) {
      ItemHandlerManager.getReference().addExtensionHandler(ext.getIdentifier(),
          new GenericExtensionItemHandler(ext));

    }
  }
  
  private String nullToEmptyString(String s) {
    if (s == null) s = "";
    return s;
  }
}
