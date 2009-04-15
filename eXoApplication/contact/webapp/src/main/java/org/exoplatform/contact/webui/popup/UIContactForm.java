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
package org.exoplatform.contact.webui.popup;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.jcr.PathNotFoundException;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.contact.service.impl.JCRDataStorage;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContactPreview;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UIWorkingContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.SessionProviderFactory;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIFormTabPane.gtmpl",
    events = {
      @EventConfig(listeners = UIContactForm.SaveActionListener.class),      
      @EventConfig(listeners = UIContactForm.CancelActionListener.class, phase=Phase.DECODE),
      @EventConfig(phase = Phase.DECODE,listeners = UIContactForm.ChangeImageActionListener.class),
      @EventConfig(phase = Phase.DECODE,listeners = UIContactForm.DeleteImageActionListener.class),
      @EventConfig(phase = Phase.DECODE,listeners = UIContactForm.SelectTabActionListener.class)
    }
)
public class UIContactForm extends UIFormTabPane {
  public static final String FIELD_WORKADDRESS_INPUT = "workAddress";
  public static final String FIELD_WORKCITY_INPUT = "workCity";
  public static final String FIELD_WORKSTATE_INPUT = "workState_province";
  public static final String FIELD_WORKPOSTALCODE_INPUT = "workPostalCode";
  public static final String FIELD_WORKCOUNTRY_INPUT = "workCountry";
  public static final String FIELD_WORKPHONE1_INPUT = "workPhone1";
  public static final String FIELD_WORKPHONE2_INPUT = "workPhone2";
  public static final String FIELD_WORKFAX_INPUT = "workFax";
  public static final String FIELD_WORKMOBILEPHONE_INPUT = "mobilePhone";
  public static final String FIELD_WORKWEBPAGE_INPUT = "webPage";
  
  public static final String FIELD_HOMEADDRESS_INPUT = "homeAddress";
  public static final String FIELD_HOMECITY_INPUT = "homeCity";
  public static final String FIELD_HOMESTATE_INPUT = "homeState_province";
  public static final String FIELD_HOMEPOSTALCODE_INPUT = "homePostalCode";
  public static final String FIELD_HOMECOUNTRY_INPUT = "homeCountry";
  public static final String FIELD_HOMEPHONE1_INPUT = "homePhone1";
  public static final String FIELD_HOMEPHONE2_INPUT = "homePhone2";
  public static final String FIELD_HOMEFAX_INPUT = "homeFax";
  public static final String FIELD_PERSONALSITE_INPUT = "personalSite";
  public static final String FIELD_NOTE_INPUT = "note";  
  
  public static final String INPUT_PROFILETAB = "protileTab" ;
  public static final String INPUT_IMCONTACTTAB = "imContactTab" ;
  public static final String INPUT_HOMETAB = "homeTab" ;
  public static final String INPUT_WORKTAB = "workTab" ;
  public static final String INPUT_NODETAB = "noteTab" ;
  
  private Contact contact_ = null ;
  private boolean isNew_ = true;
  public Contact getContact() { return contact_ ; }
  public UIContactForm() throws Exception {
    super("UIContactForm");
    UIProfileInputSet ProfileTab = new UIProfileInputSet(INPUT_PROFILETAB) ;
    UIFormInputWithActions IMContactTab = new UIIMContactInputSet(INPUT_IMCONTACTTAB) ;
    UIFormInputWithActions HomeTab = new UIFormInputWithActions(INPUT_HOMETAB) ;
    UIFormInputWithActions WorkTab = new UIFormInputWithActions(INPUT_WORKTAB) ;
    UIFormInputWithActions NoteTab = new UIFormInputWithActions(INPUT_NODETAB) ;

    WorkTab.addUIFormInput(new UIFormStringInput(FIELD_WORKADDRESS_INPUT, FIELD_WORKADDRESS_INPUT, null));
    WorkTab.addUIFormInput(new UIFormStringInput(FIELD_WORKCITY_INPUT, FIELD_WORKCITY_INPUT, null));
    WorkTab.addUIFormInput(new UIFormStringInput(FIELD_WORKSTATE_INPUT, FIELD_WORKSTATE_INPUT, null));
    WorkTab.addUIFormInput(new UIFormStringInput(FIELD_WORKPOSTALCODE_INPUT, FIELD_WORKPOSTALCODE_INPUT, null));
    WorkTab.addUIFormInput(new UIFormStringInput(FIELD_WORKCOUNTRY_INPUT, FIELD_WORKCOUNTRY_INPUT, null));
    WorkTab.addUIFormInput(new UIFormStringInput(FIELD_WORKPHONE1_INPUT, FIELD_WORKPHONE1_INPUT, null));
    WorkTab.addUIFormInput(new UIFormStringInput(FIELD_WORKPHONE2_INPUT, FIELD_WORKPHONE2_INPUT, null));
    WorkTab.addUIFormInput(new UIFormStringInput(FIELD_WORKFAX_INPUT, FIELD_WORKFAX_INPUT, null));
    WorkTab.addUIFormInput(new UIFormStringInput(FIELD_WORKMOBILEPHONE_INPUT, FIELD_WORKMOBILEPHONE_INPUT, null));
    WorkTab.addUIFormInput(new UIFormStringInput(FIELD_WORKWEBPAGE_INPUT, FIELD_WORKWEBPAGE_INPUT, null));

    HomeTab.addUIFormInput(new UIFormStringInput(FIELD_HOMEADDRESS_INPUT, FIELD_HOMEADDRESS_INPUT, null));
    HomeTab.addUIFormInput(new UIFormStringInput(FIELD_HOMECITY_INPUT, FIELD_HOMECITY_INPUT, null));
    HomeTab.addUIFormInput(new UIFormStringInput(FIELD_HOMESTATE_INPUT, FIELD_HOMESTATE_INPUT, null));
    HomeTab.addUIFormInput(new UIFormStringInput(FIELD_HOMEPOSTALCODE_INPUT, FIELD_HOMEPOSTALCODE_INPUT, null));
    HomeTab.addUIFormInput(new UIFormStringInput(FIELD_HOMECOUNTRY_INPUT, FIELD_HOMECOUNTRY_INPUT, null));
    HomeTab.addUIFormInput(new UIFormStringInput(FIELD_HOMEPHONE1_INPUT, FIELD_HOMEPHONE1_INPUT, null));
    HomeTab.addUIFormInput(new UIFormStringInput(FIELD_HOMEPHONE2_INPUT, FIELD_HOMEPHONE2_INPUT, null));
    HomeTab.addUIFormInput(new UIFormStringInput(FIELD_HOMEFAX_INPUT, FIELD_HOMEFAX_INPUT, null));
    HomeTab.addUIFormInput(new UIFormStringInput(FIELD_PERSONALSITE_INPUT, FIELD_PERSONALSITE_INPUT, null));
    NoteTab.addUIFormInput(new UIFormTextAreaInput(FIELD_NOTE_INPUT, FIELD_NOTE_INPUT, null));
    addUIFormInput(ProfileTab) ;
    addUIFormInput(WorkTab) ;
    addUIFormInput(IMContactTab) ;    
    addUIFormInput(HomeTab) ;
    addUIFormInput(NoteTab) ;
    this.setSelectedTab(ProfileTab.getId());
  }
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }  
  public void setNew(boolean isNew) { isNew_ = isNew ; }

  
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  
  public void setValues(Contact contact) throws Exception {
    contact_ = contact ;
    UIProfileInputSet profileTab = getChildById(INPUT_PROFILETAB) ;
    profileTab.setFieldFirstName(contact.getFirstName());
    profileTab.setFieldLastName(contact.getLastName());
    profileTab.setFieldNickName(contact.getNickName());
    profileTab.setFieldGender(contact.getGender());    
    
    profileTab.setFieldBirthday(contact.getBirthday());
    profileTab.setFieldJobName(contact.getJobTitle());
    profileTab.setFieldEmail(contact.getEmailAddress());   
    ContactAttachment contactAttachment = contact.getAttachment();
    if (contactAttachment != null) {
      profileTab.setContact(contact) ;
      profileTab.setImage(contactAttachment.getInputStream()) ;
    }
    else profileTab.setImage(null) ;
    
    getUIStringInput(FIELD_WORKADDRESS_INPUT).setValue(contact.getWorkAddress());
    getUIStringInput(FIELD_WORKCITY_INPUT).setValue(contact.getWorkCity());
    getUIStringInput(FIELD_WORKSTATE_INPUT).setValue(contact.getWorkStateProvince());
    getUIStringInput(FIELD_WORKPOSTALCODE_INPUT).setValue(contact.getWorkPostalCode());
    getUIStringInput(FIELD_WORKCOUNTRY_INPUT).setValue(contact.getWorkCountry());
    getUIStringInput(FIELD_WORKPHONE1_INPUT).setValue(contact.getWorkPhone1());
    getUIStringInput(FIELD_WORKPHONE2_INPUT).setValue(contact.getWorkPhone2());
    getUIStringInput(FIELD_WORKFAX_INPUT).setValue(contact.getWorkFax());
    getUIStringInput(FIELD_WORKMOBILEPHONE_INPUT).setValue(contact.getMobilePhone());
    getUIStringInput(FIELD_WORKWEBPAGE_INPUT).setValue(contact.getWebPage());
    
    getUIStringInput(UIIMContactInputSet.FIELD_EXOCHAT_INPUT).setValue(contact.getExoId());
    getUIStringInput(UIIMContactInputSet.FIELD_GOOGLE_INPUT).setValue(contact.getGoogleId());
    getUIStringInput(UIIMContactInputSet.FIELD_MSN_INPUT).setValue(contact.getMsnId());
    getUIStringInput(UIIMContactInputSet.FIELD_AOLAIM_INPUT).setValue(contact.getAolId());
    getUIStringInput(UIIMContactInputSet.FIELD_YAHOO_INPUT).setValue(contact.getYahooId());
    getUIStringInput(UIIMContactInputSet.FIELD_IRC_INPUT).setValue(contact.getIcrId());
    getUIStringInput(UIIMContactInputSet.FIELD_SKYPE_INPUT).setValue(contact.getSkypeId());
    getUIStringInput(UIIMContactInputSet.FIELD_ICQ_INPUT).setValue(contact.getIcqId());
    
    getUIStringInput(FIELD_HOMEADDRESS_INPUT).setValue(contact.getHomeAddress());
    getUIStringInput(FIELD_HOMECITY_INPUT).setValue(contact.getHomeCity());
    getUIStringInput(FIELD_HOMESTATE_INPUT).setValue(contact.getHomeState_province());
    getUIStringInput(FIELD_HOMEPOSTALCODE_INPUT).setValue(contact.getHomePostalCode());
    getUIStringInput(FIELD_HOMECOUNTRY_INPUT).setValue(contact.getHomeCountry());
    getUIStringInput(FIELD_HOMEPHONE1_INPUT).setValue(contact.getHomePhone1());
    getUIStringInput(FIELD_HOMEPHONE2_INPUT).setValue(contact.getHomePhone2());
    getUIStringInput(FIELD_HOMEFAX_INPUT).setValue(contact.getHomeFax());
    getUIStringInput(FIELD_PERSONALSITE_INPUT).setValue(contact.getPersonalSite());
    getUIStringInput(FIELD_NOTE_INPUT).setValue(contact.getNote());
  }
  
  static  public class SaveActionListener extends EventListener<UIContactForm> {
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiContactForm = event.getSource() ;
      UIApplication uiApp = uiContactForm.getAncestorOfType(UIApplication.class) ;
      UIProfileInputSet profileTab = uiContactForm.getChildById(INPUT_PROFILETAB) ;
      Contact contact ;
      if (uiContactForm.isNew_) contact = new Contact() ;
      else contact = uiContactForm.contact_ ;
      String firstName = profileTab.getFieldFirstName().trim() ;
      String lastName = profileTab.getFieldLastName().trim() ;
      if (ContactUtils.isNameLong(firstName) || ContactUtils.isNameLong(lastName)) {
        uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.nameTooLong", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      contact.setFullName(firstName + " " + lastName);
      contact.setFirstName(firstName);
      contact.setLastName(lastName);
      contact.setNickName(profileTab.getFieldNickName());      
      contact.setGender(profileTab.getFieldGender()) ;
      try {
        contact.setBirthday(profileTab.getFieldBirthday()) ;
      } catch(IllegalArgumentException e) {
        uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.invalid-birthday", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }      
      if (!ContactUtils.isEmpty(profileTab.getFieldJobName())) contact.setJobTitle(profileTab.getFieldJobName().trim());
      if (ContactUtils.isNameLong(profileTab.getFieldJobName())) {
        uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.jobTooLong", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String EMAIL_REGEX = 
        "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[_A-Za-z0-9-.]+";
      String emails = profileTab.getFieldEmail() ;
      for (String email : emails.split(Utils.SEMI_COLON)) {
        if (!ContactUtils.isEmpty(email) && !email.trim().matches(EMAIL_REGEX)) {
          uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.invalid-email", null, 
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }
      contact.setEmailAddress(emails);
      if(profileTab.getImage() != null) {
        ContactAttachment attachment = new ContactAttachment() ;
        attachment.setInputStream(new ByteArrayInputStream(profileTab.getImage())) ;
        attachment.setFileName(profileTab.getFileName()) ;
        attachment.setMimeType(profileTab.getMimeType()) ;
        contact.setAttachment(attachment) ;        
      }else {contact.setAttachment(null) ;}
      contact.setWorkAddress(uiContactForm.getUIStringInput(FIELD_WORKADDRESS_INPUT).getValue());
      contact.setWorkCity(uiContactForm.getUIStringInput(FIELD_WORKCITY_INPUT).getValue());
      contact.setWorkStateProvince(uiContactForm.getUIStringInput(FIELD_WORKSTATE_INPUT).getValue());
      contact.setWorkPostalCode(uiContactForm.getUIStringInput(FIELD_WORKPOSTALCODE_INPUT).getValue());
      contact.setWorkCountry(uiContactForm.getUIStringInput(FIELD_WORKCOUNTRY_INPUT).getValue());
      contact.setWorkPhone1(uiContactForm.getUIStringInput(FIELD_WORKPHONE1_INPUT).getValue());
      contact.setWorkPhone2(uiContactForm.getUIStringInput(FIELD_WORKPHONE2_INPUT).getValue());
      contact.setWorkFax(uiContactForm.getUIStringInput(FIELD_WORKFAX_INPUT).getValue());
      contact.setMobilePhone(uiContactForm.getUIStringInput(FIELD_WORKMOBILEPHONE_INPUT).getValue());
      String webPage = uiContactForm.getUIStringInput(FIELD_WORKWEBPAGE_INPUT).getValue() ;
      if (!ContactUtils.isEmpty(webPage) && !webPage.startsWith(ContactUtils.HTTP)) webPage = ContactUtils.HTTP + webPage ;
      contact.setWebPage(webPage);
    
      contact.setExoId(uiContactForm.getUIStringInput(UIIMContactInputSet.FIELD_EXOCHAT_INPUT).getValue());
      contact.setGoogleId(uiContactForm.getUIStringInput(UIIMContactInputSet.FIELD_GOOGLE_INPUT).getValue());
      contact.setMsnId(uiContactForm.getUIStringInput(UIIMContactInputSet.FIELD_MSN_INPUT).getValue());
      contact.setAolId(uiContactForm.getUIStringInput(UIIMContactInputSet.FIELD_AOLAIM_INPUT).getValue());
      contact.setYahooId(uiContactForm.getUIStringInput(UIIMContactInputSet.FIELD_YAHOO_INPUT).getValue());
      contact.setIcrId(uiContactForm.getUIStringInput(UIIMContactInputSet.FIELD_IRC_INPUT).getValue() );
      contact.setSkypeId(uiContactForm.getUIStringInput(UIIMContactInputSet.FIELD_SKYPE_INPUT).getValue());
      contact.setIcqId(uiContactForm.getUIStringInput(UIIMContactInputSet.FIELD_ICQ_INPUT).getValue());
      
      contact.setHomeAddress(uiContactForm.getUIStringInput(FIELD_HOMEADDRESS_INPUT).getValue());
      contact.setHomeCity(uiContactForm.getUIStringInput(FIELD_HOMECITY_INPUT).getValue());
      contact.setHomeState_province(uiContactForm.getUIStringInput(FIELD_HOMESTATE_INPUT).getValue());
      contact.setHomePostalCode(uiContactForm.getUIStringInput(FIELD_HOMEPOSTALCODE_INPUT).getValue());
      contact.setHomeCountry(uiContactForm.getUIStringInput(FIELD_HOMECOUNTRY_INPUT).getValue());
      contact.setHomePhone1(uiContactForm.getUIStringInput(FIELD_HOMEPHONE1_INPUT).getValue() );
      contact.setHomePhone2(uiContactForm.getUIStringInput(FIELD_HOMEPHONE2_INPUT).getValue());
      contact.setHomeFax(uiContactForm.getUIStringInput(FIELD_HOMEFAX_INPUT).getValue());
      String perSite = uiContactForm.getUIStringInput(FIELD_PERSONALSITE_INPUT).getValue() ;
      if (!ContactUtils.isEmpty(perSite) && !perSite.startsWith(ContactUtils.HTTP)) perSite = ContactUtils.HTTP + perSite ;
      contact.setPersonalSite(perSite);
      contact.setNote(uiContactForm.getUIFormTextAreaInput(FIELD_NOTE_INPUT).getValue());
      contact.setLastUpdated(new Date()) ;

      ContactService contactService = ContactUtils.getContactService();  
      String username = ContactUtils.getCurrentUser() ;
      SessionProvider sessionProvider = SessionProviderFactory.createSessionProvider() ;
      UIContactPortlet uiContactPortlet = uiContactForm.getAncestorOfType(UIContactPortlet.class) ;
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      if (uiContactForm.isNew_) {
        UIPopupContainer popupContainer = uiContactForm.getParent() ;
        UICategorySelect uiCategorySelect = popupContainer.getChild(UICategorySelect.class); 
        String category = uiCategorySelect.getSelectedCategory();        
        contact.setAddressBook(new String[] { category });

        UIAddressBooks uiAddressBooks = uiContactForm
        .getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UIAddressBooks.class) ;
        if (uiAddressBooks.getSharedGroups().containsKey(category)) {
          if (uiAddressBooks.havePermission(category)) {
            contactService.saveContactToSharedAddressBook(username, category, contact, true) ;   
            contact = contactService.getSharedContactAddressBook(username, contact.getId()) ;
            contact.setContactType(JCRDataStorage.SHARED) ;            
          } else {
            uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.removedPer", null, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }          
        } else if (uiAddressBooks.getPrivateGroupMap().containsKey(category)){
          contactService.saveContact(username, contact, true);  
          contact = contactService.getContact(username, contact.getId()) ;
          contact.setContactType(JCRDataStorage.PERSONAL) ;
        } else {
          uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.address-deleted", null,
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      } else {
        try {
          String contactType = contact.getContactType() ;
          if (contactType.equals(JCRDataStorage.PERSONAL)) {
            contactService.saveContact(username, contact, false) ;
            contact = contactService.getContact(username, contact.getId()) ;
            contact.setContactType(JCRDataStorage.PERSONAL) ;
          } else if (contactType.equals(JCRDataStorage.SHARED)) {
            UIAddressBooks uiAddressBooks = uiContactForm
              .getAncestorOfType(UIContactPortlet.class).findFirstComponentOfType(UIAddressBooks.class) ;
            if ( uiAddressBooks.getSharedGroups().containsKey(contact.getAddressBook()[0])) {
              if (uiAddressBooks.havePermission(contact.getAddressBook()[0]) || uiContacts.havePermission(contact)) {
                contactService.saveContactToSharedAddressBook(username, contact.getAddressBook()[0], contact, false) ; 
                contactService.getSharedContactAddressBook(username, contact.getId()) ;
                contact.setContactType(JCRDataStorage.SHARED) ;
              } else {
                uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.removedPer", null, 
                    ApplicationMessage.WARNING)) ;
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                return ;
              }              
            } else {
              Contact sharedContact = contactService
                .getSharedContact(SessionProvider.createSystemProvider(), username, contact.getId()) ;                
              if (uiContacts.havePermission(sharedContact)) {
                contactService.saveSharedContact(username, contact) ;   
                contact = contactService.getSharedContact(SessionProvider.createSystemProvider(), username, contact.getId()) ;
              } else {
                uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.removedPer", null, 
                    ApplicationMessage.WARNING)) ;
                event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
                return ;                
              }
            }
          }
        } catch(PathNotFoundException e) {
          uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.contact-deleted", null,
              ApplicationMessage.WARNING)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }         
      }
      List<String> tempContact = new ArrayList<String>() ;
      tempContact.add(Utils.contactTempId) ;
      try {
        contactService.removeContacts(username, tempContact) ;
      } catch (PathNotFoundException e) {}
      String selectedContact = uiContacts.getSelectedContact() ;
      //if(uiContacts.isDisplaySearchResult()) {
      List<Contact> contacts = new ArrayList<Contact>() ;
      contacts.add(contact) ;
      uiContacts.setContact(contacts, true) ;
      //}
      uiContacts.updateList() ;
      if (!ContactUtils.isEmpty(selectedContact) && selectedContact.equals(contact.getId())) {
        //cs-1835        
        String type = contact.getContactType() ;
        if (type.equals(JCRDataStorage.PERSONAL)) {
          contact = contactService.getContact(username, contact.getId()) ;
        } else {
          if (uiContacts.isSharedAddress(contact)) {
            contact = contactService.getSharedContactAddressBook(username, contact.getId()) ;
          } else {
            contact = contactService.getSharedContact(SessionProviderFactory.createSystemProvider(), username, contact.getId()) ;
          }
        }        
        uiContactPortlet.findFirstComponentOfType(UIContactPreview.class).setContact(contact) ;
        uiContacts.setSelectedContact(selectedContact) ;
      }        
      uiContactPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPortlet.getChild(UIWorkingContainer.class)) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIContactForm> {
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiForm = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      uiContactPortlet.cancelAction() ;
    }
  }
  
  static  public class ChangeImageActionListener extends EventListener<UIContactForm> {
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiContactForm = event.getSource() ;
      UIProfileInputSet profileInputSet = uiContactForm.getChildById(INPUT_PROFILETAB) ;
      profileInputSet.setFieldGender(profileInputSet.getFieldGender()) ;
      UIPopupContainer popupContainer = uiContactForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.activate(UIImageForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class DeleteImageActionListener extends EventListener<UIContactForm> {
    @Override
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiContactForm = event.getSource() ;
      UIProfileInputSet profileTab = uiContactForm.getChildById(INPUT_PROFILETAB) ;
      profileTab.setImage(null) ;
      profileTab.setFileName(null) ;
      profileTab.setMimeType(null) ;
      if (profileTab.getContact() != null) profileTab.getContact().setAttachment(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(
        uiContactForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
  
  static public class SelectTabActionListener extends EventListener<UIContactForm> {
    public void execute(Event<UIContactForm> event) throws Exception {
      event.getRequestContext().addUIComponentToUpdateByAjax(event.getSource()) ;      
    }
  }
}