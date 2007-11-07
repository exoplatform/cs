/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

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

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactImportExport;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.container.PortalContainer;

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
  
  private static String ENCODING = "UTF-8";

  public OutputStream exportContact(String username, List<String> contactIds) throws Exception {

    ContactIOFactory ciof = Pim.getContactIOFactory();
    ContactModelFactory cmf = Pim.getContactModelFactory();
    ContactMarshaller marshaller = ciof.createContactMarshaller();
    
    // if needed, we'll change or remove the encoding
    marshaller.setEncoding(ENCODING);

    // converting eXo contacts to Pim contacts
    ContactService contactService = (ContactService) PortalContainer
        .getComponent(ContactService.class);
    int numberOfContacts = contactIds.size();
    net.wimpi.pim.contact.model.Contact[] pimContacts = new net.wimpi.pim.contact.model.Contact[numberOfContacts];

    Contact contact;
    for (int i = 0; i < numberOfContacts; i++) {    
      String contactId = contactIds.get(i) ;
      contact = contactService.getContact(username, contactId);
      if (contact == null) contact = contactService.getSharedContact(contactId) ;
      pimContacts[i] = cmf.createContact();

      // converting now from an eXo contact to PimContact

      // a personal identity
      PersonalIdentity pid = cmf.createPersonalIdentity();
      pid.setFormattedName(nullToEmptyString(contact.getFullName()));
      pid.setFirstname(nullToEmptyString(contact.getFirstName()));
      pid.setLastname(nullToEmptyString(contact.getLastName()));
      
      String middleName = contact.getMiddleName();
      if ((middleName != null) && !middleName.equals("")) {
        StringTokenizer tokens = new StringTokenizer(middleName, ",", false);
        while (tokens.hasMoreTokens())
          pid.addAdditionalName(tokens.nextToken().trim());
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
      EmailAddress email;
      
      StringTokenizer tokens = new StringTokenizer(contact.getEmailAddress(), ";", false);
      while (tokens.hasMoreTokens()) {
        email = cmf.createEmailAddress();
        email.setAddress(tokens.nextToken().trim());
        comm.addEmailAddress(email);
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

    }

    // The OutputStream that will be returned
    OutputStream out = new ByteArrayOutputStream();
    marshaller.marshallContacts(out, pimContacts);

    return out;
  }

  public void importContact(String username, InputStream input, String groupId) throws Exception {

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
    for (int index = 0; index < pimContacts.length; index++) {

      Contact contact = new Contact();

      PersonalIdentity identity = pimContacts[index].getPersonalIdentity();

      String fullName = identity.getFormattedName();
      contact.setFullName(fullName);
      String lastName = identity.getLastname();
      contact.setLastName(lastName);
      String firstName = identity.getFirstname();
      contact.setFirstName(identity.getFirstname());

      String middleName = "";
      int size = identity.getAdditionalNameCount();
      for (int i = 0; i < size; i++) {
        if (i > 0)
          middleName += ", ";
        middleName = identity.getAdditionalName(i);
      }
      contact.setMiddleName(middleName);

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

      Communications communication = pimContacts[index].getCommunications();
      String emailAddress = "";
      for (Iterator iters = communication.getEmailAddresses(); iters.hasNext();) {
        EmailAddress email = (EmailAddress) iters.next();
        if (!emailAddress.equals(""))
          emailAddress += "; ";
        emailAddress += email.getAddress();
      }

      contact.setEmailAddress(emailAddress);

      String mobilePhone = null;

      String homePhone1 = null;
      String homePhone2 = null;
      String homeFax = null;

      String workPhone1 = null;
      String workPhone2 = null;
      String workFax = null;

      for (Iterator iters = communication.getPhoneNumbers(); iters.hasNext();) {
        PhoneNumber phone = (PhoneNumber) iters.next();

        if (phone.isHome() && phone.isFax())
          homeFax = phone.getNumber();
        else if (phone.isHome() && phone.isVoice()) {
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
        }

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

      contact.setNote(pimContacts[index].getNote());
      
      Date revisionDate = pimContacts[index].getCurrentRevisionDate();
      
      if (revisionDate != null)
        contact.setLastUpdated(revisionDate);

      // ////////////////////////////////
      // Now we have the contact object
      // Then store it to JCR storage
      // ////////////////////////////////

      contact.setCategories(new String[] { groupId });
      ContactService contactService = (ContactService) PortalContainer
          .getComponent(ContactService.class);
      contactService.saveContact(username, contact, true);
    }
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
