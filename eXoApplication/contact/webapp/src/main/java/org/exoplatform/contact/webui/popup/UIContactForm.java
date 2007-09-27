/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Date;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.BufferAttachment;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContactPreview;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UIWorkingContainer;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.services.organization.impl.GroupImpl;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
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
      @EventConfig(listeners = UIContactForm.CancelActionListener.class),
      @EventConfig(listeners = UIContactForm.ChangeImageActionListener.class)
    }
)
public class UIContactForm extends UIFormTabPane implements UIPopupComponent {
  public static String contactId_ ;
  public static boolean isNew_ = true;
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
  
  public static final String FIELD_EXOCHAT_INPUT = "exoChat";
  public static final String FIELD_GOOGLE_INPUT = "google";
  public static final String FIELD_MSN_INPUT = "msn";
  public static final String FIELD_AOLAIM_INPUT = "aolAim";
  public static final String FIELD_YAHOO_INPUT = "yahoo";
  public static final String FIELD_ICR_INPUT = "icr";
  public static final String FIELD_SKYPE_INPUT = "skype";
  public static final String FIELD_ICQ_INPUT = "icq";
  
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
  
  public static String[] FIELD_SHAREDCONTACT_BOX = null;
  public static String FIELD_ISPUBLIC_BOX = "isPublic";
  public static String FIELD_EDITPERMISSION_INPUT = "editPermission";  
  
  public static final String INPUT_PROFILETAB = "protileTab" ;
  public static final String INPUT_IMCONTACTTAB = "imContactTab" ;
  public static final String INPUT_HOMETAB = "homeTab" ;
  public static final String INPUT_WORKTAB = "workTab" ;
  public static final String INPUT_NODETAB = "noteTab" ;
  public static final String INPUT_SHARETAB =  "shareContactTab" ;

  public UIContactForm() throws Exception {
    super("UIContactForm", false);
    UIProfileInputSet ProfileTab = new UIProfileInputSet(INPUT_PROFILETAB) ;
    UIFormInputWithActions IMContactTab = new UIFormInputWithActions(INPUT_IMCONTACTTAB) ;
    UIFormInputWithActions HomeTab = new UIFormInputWithActions(INPUT_HOMETAB) ;
    UIFormInputWithActions WorkTab = new UIFormInputWithActions(INPUT_WORKTAB) ;
    UIFormInputWithActions NoteTab = new UIFormInputWithActions(INPUT_NODETAB) ;
    UIFormInputWithActions ShareTab = new UIFormInputWithActions(INPUT_SHARETAB) ;

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

    IMContactTab.addUIFormInput(new UIFormStringInput(FIELD_EXOCHAT_INPUT, FIELD_EXOCHAT_INPUT, null));
    IMContactTab.addUIFormInput(new UIFormStringInput(FIELD_GOOGLE_INPUT, FIELD_GOOGLE_INPUT, null));
    IMContactTab.addUIFormInput(new UIFormStringInput(FIELD_MSN_INPUT, FIELD_MSN_INPUT, null));
    IMContactTab.addUIFormInput(new UIFormStringInput(FIELD_AOLAIM_INPUT, FIELD_AOLAIM_INPUT, null));
    IMContactTab.addUIFormInput(new UIFormStringInput(FIELD_YAHOO_INPUT, FIELD_YAHOO_INPUT, null));
    IMContactTab.addUIFormInput(new UIFormStringInput(FIELD_ICR_INPUT, FIELD_ICR_INPUT, null));
    IMContactTab.addUIFormInput(new UIFormStringInput(FIELD_SKYPE_INPUT, FIELD_SKYPE_INPUT, null));
    IMContactTab.addUIFormInput(new UIFormStringInput(FIELD_ICQ_INPUT, FIELD_ICQ_INPUT, null));

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

    ShareTab.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_ISPUBLIC_BOX, FIELD_ISPUBLIC_BOX, false));
    OrganizationService organizationService = (OrganizationService)PortalContainer.getComponent(OrganizationService.class) ;
    Object[] groups = organizationService.getGroupHandler().getAllGroups().toArray() ;
    //getChildren().clear() ;
    FIELD_SHAREDCONTACT_BOX = new String[groups.length];
    for(int i = 0; i < groups.length; i ++) {
      FIELD_SHAREDCONTACT_BOX[i] = ((GroupImpl)groups[i]).getId() ;
      ShareTab.addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_SHAREDCONTACT_BOX[i], FIELD_SHAREDCONTACT_BOX[i], false));
    }
    ShareTab.addUIFormInput(new UIFormStringInput(FIELD_EDITPERMISSION_INPUT, FIELD_EDITPERMISSION_INPUT, null));
    addUIFormInput(ProfileTab) ;
    addUIFormInput(WorkTab) ;
    addUIFormInput(IMContactTab) ;    
    addUIFormInput(HomeTab) ;
    addUIFormInput(NoteTab) ;
    addUIFormInput(ShareTab) ;
    setRenderedChild(UIProfileInputSet.class) ;  
  } 
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }
  public void activate() throws Exception {}
  public void deActivate() throws Exception {}
  
  public void setValues(String contactId) throws Exception {
    contactId_ = contactId ;
    ContactService contactService = ContactUtils.getContactService();
    String username = ContactUtils.getCurrentUser() ;
    Contact contact = contactService.getContact(username, contactId);
    if(contact == null) {
      contact = contactService.getSharedContact(contactId);
      if (contact == null) return ;
      getUIFormCheckBoxInput(FIELD_ISPUBLIC_BOX).setChecked(true);
      String[] categories = contact.getCategories();
      for (String category : categories) getUIFormCheckBoxInput(category).setChecked(true) ;
      String[] editPermission = contact.getEditPermission();
      StringBuffer editPermissionBuffer = new StringBuffer("");
      if (editPermission != null && editPermission.length > 0) {
        editPermissionBuffer.append(editPermission[0]);
        for (int i = 1; i < editPermission.length; i ++) editPermissionBuffer.append("," + editPermission[i]);
      }   
      getUIStringInput(FIELD_EDITPERMISSION_INPUT).setValue(editPermissionBuffer.toString());
    }    
    getUIFormCheckBoxInput(FIELD_ISPUBLIC_BOX).setEnable(false) ;
    for (String group : FIELD_SHAREDCONTACT_BOX) getUIFormCheckBoxInput(group).setEnable(false) ;
    getUIStringInput(FIELD_EDITPERMISSION_INPUT).setEditable(false) ;
    
    UIProfileInputSet profileTab = getChildById(INPUT_PROFILETAB) ;
    profileTab.setFieldFullName(contact.getFullName());
    profileTab.setFieldFirstName(contact.getFirstName());
    profileTab.setFieldMiddleName(contact.getMiddleName());
    profileTab.setFieldLastName(contact.getLastName());
    profileTab.setFieldNickName(contact.getNickName());
    profileTab.setFieldGender(contact.getGender());
    profileTab.setFieldBirthday(contact.getBirthday());
    profileTab.setFieldJobName(contact.getJobTitle());
    profileTab.setFieldEmail(contact.getEmailAddress());   
    ContactAttachment contactAttachment = contact.getAttachment();  
    if (contactAttachment != null) {
      InputStream is = contactAttachment.getInputStream();
      if (is != null)
        profileTab.setImage(is) ;
    }    
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
    
    getUIStringInput(FIELD_EXOCHAT_INPUT).setValue(contact.getExoId());
    getUIStringInput(FIELD_GOOGLE_INPUT).setValue(contact.getGoogleId());
    getUIStringInput(FIELD_MSN_INPUT).setValue(contact.getMsnId());
    getUIStringInput(FIELD_AOLAIM_INPUT).setValue(contact.getAolId());
    getUIStringInput(FIELD_YAHOO_INPUT).setValue(contact.getYahooId());
    getUIStringInput(FIELD_ICR_INPUT).setValue(contact.getIcrId());
    getUIStringInput(FIELD_SKYPE_INPUT).setValue(contact.getSkypeId());
    getUIStringInput(FIELD_ICQ_INPUT).setValue(contact.getIcqId());
    
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
      if (profileTab.getFieldFullName() == null || profileTab.getFieldFullName().trim().length() == 0) {  
        uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.fullname-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      ContactService contactService = ContactUtils.getContactService();  
      String username = ContactUtils.getCurrentUser() ;
      Contact contact ;
      if (isNew_) contact = new Contact() ;
      else {
        contact = contactService.getContact(username, contactId_);
        if (contact == null) contact = contactService.getSharedContact(contactId_);
      }
       
      contact.setFullName(profileTab.getFieldFullName());
      contact.setFirstName(profileTab.getFieldFirstName());
      contact.setMiddleName(profileTab.getFieldMiddleName());
      contact.setLastName(profileTab.getFieldLastName());
      contact.setNickName(profileTab.getFieldNickName());
      contact.setGender(profileTab.getFieldGender()) ;
      contact.setBirthday(profileTab.getFieldBirthday()) ;
      contact.setJobTitle(profileTab.getFieldJobName());
      contact.setEmailAddress(profileTab.getFieldEmail());
      BufferAttachment bufferAttachment = new BufferAttachment() ;
      bufferAttachment.setId("Attachment" + IdGenerator.generate());
      bufferAttachment.setFileName(profileTab.getFileName()) ;
      bufferAttachment.setMimeType(profileTab.getMimeType()) ;
      if (profileTab.getImage() != null)
        bufferAttachment.setInputStream(new ByteArrayInputStream(profileTab.getImage())) ;
      contact.setAttachment(bufferAttachment) ;
      
      contact.setWorkAddress(uiContactForm.getUIStringInput(FIELD_WORKADDRESS_INPUT).getValue());
      contact.setWorkCity(uiContactForm.getUIStringInput(FIELD_WORKCITY_INPUT).getValue());
      contact.setWorkStateProvince(uiContactForm.getUIStringInput(FIELD_WORKSTATE_INPUT).getValue());
      contact.setWorkPostalCode(uiContactForm.getUIStringInput(FIELD_WORKPOSTALCODE_INPUT).getValue());
      contact.setWorkCountry(uiContactForm.getUIStringInput(FIELD_WORKCOUNTRY_INPUT).getValue());
      contact.setWorkPhone1(uiContactForm.getUIStringInput(FIELD_WORKPHONE1_INPUT).getValue());
      contact.setWorkPhone2(uiContactForm.getUIStringInput(FIELD_WORKPHONE2_INPUT).getValue());
      contact.setWorkFax(uiContactForm.getUIStringInput(FIELD_WORKFAX_INPUT).getValue());
      contact.setMobilePhone(uiContactForm.getUIStringInput(FIELD_WORKMOBILEPHONE_INPUT).getValue());
      contact.setWebPage(uiContactForm.getUIStringInput(FIELD_WORKWEBPAGE_INPUT).getValue());
    
      contact.setExoId(uiContactForm.getUIStringInput(FIELD_EXOCHAT_INPUT).getValue());
      contact.setGoogleId(uiContactForm.getUIStringInput(FIELD_GOOGLE_INPUT).getValue());
      contact.setMsnId(uiContactForm.getUIStringInput(FIELD_MSN_INPUT).getValue());
      contact.setAolId(uiContactForm.getUIStringInput(FIELD_AOLAIM_INPUT).getValue());
      contact.setYahooId(uiContactForm.getUIStringInput(FIELD_YAHOO_INPUT).getValue());
      contact.setIcrId(uiContactForm.getUIStringInput(FIELD_ICR_INPUT).getValue() );
      contact.setSkypeId(uiContactForm.getUIStringInput(FIELD_SKYPE_INPUT).getValue());
      contact.setIcqId(uiContactForm.getUIStringInput(FIELD_ICQ_INPUT).getValue());
      
      contact.setHomeAddress(uiContactForm.getUIStringInput(FIELD_HOMEADDRESS_INPUT).getValue());
      contact.setHomeCity(uiContactForm.getUIStringInput(FIELD_HOMECITY_INPUT).getValue());
      contact.setHomeState_province(uiContactForm.getUIStringInput(FIELD_HOMESTATE_INPUT).getValue());
      contact.setHomePostalCode(uiContactForm.getUIStringInput(FIELD_HOMEPOSTALCODE_INPUT).getValue());
      contact.setHomeCountry(uiContactForm.getUIStringInput(FIELD_HOMECOUNTRY_INPUT).getValue());
      contact.setHomePhone1(uiContactForm.getUIStringInput(FIELD_HOMEPHONE1_INPUT).getValue() );
      contact.setHomePhone2(uiContactForm.getUIStringInput(FIELD_HOMEPHONE2_INPUT).getValue());
      contact.setHomeFax(uiContactForm.getUIStringInput(FIELD_HOMEFAX_INPUT).getValue());
      contact.setPersonalSite(uiContactForm.getUIStringInput(FIELD_PERSONALSITE_INPUT).getValue());
      contact.setNote(uiContactForm.getUIFormTextAreaInput(FIELD_NOTE_INPUT).getValue());

      UIContactPortlet uiContactPortlet = uiContactForm.getAncestorOfType(UIContactPortlet.class) ;
      UIContacts uicontacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      UIAddressBooks uiAddressBook = uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class) ;
      UIContactPreview uiContactPreview = uiContactPortlet.findFirstComponentOfType(UIContactPreview.class) ;
      UIWorkingContainer uiWorkingContainer = uiContactPortlet.findFirstComponentOfType(UIWorkingContainer.class) ; 
      if (uiContactForm.getUIFormCheckBoxInput(FIELD_ISPUBLIC_BOX).isChecked()) {
        StringBuffer sharedGroups = new StringBuffer("");
        for (int i = 0; i < FIELD_SHAREDCONTACT_BOX.length; i ++) {
          if (uiContactForm.getUIFormCheckBoxInput(FIELD_SHAREDCONTACT_BOX[i]).isChecked())
            sharedGroups.append(FIELD_SHAREDCONTACT_BOX[i] + ",");
        }
        if (sharedGroups.toString().equals("")) {
          uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.selectSharedGroups-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ; 
        }   
        if (uiContactForm.getUIStringInput(FIELD_EDITPERMISSION_INPUT).getValue() != null)
          contact.setEditPermission(uiContactForm.getUIStringInput(FIELD_EDITPERMISSION_INPUT).getValue().split(","));
        String[] categories = sharedGroups.toString().split(",") ;
        contact.setCategories(categories);
        contactService.saveSharedContact(contact, isNew_);
        if (isNew_) {
          if (uiWorkingContainer.getSelectedGroup() == null) {
            uiWorkingContainer.setSelectedGroup(categories[0]) ;
          }
          for (String category : categories) {
            if (category.equals(uiAddressBook.getSelectedGroup())) {
              uicontacts.updateContact(contact, isNew_) ;
              uiContactPreview.updateContact() ;
              break ;
            }
          }
        } else {
          uicontacts.updateContact(contact, isNew_) ;
          if (uiContactPreview.getContact() != null && contact.getId().equals(uiContactPreview.getContact().getId())) 
            uiContactPreview.setContact(contact) ;
        }
      } else {
        UIPopupContainer popupContainer = uiContactForm.getParent() ;
        UICategorySelect uiCategorySelect = popupContainer.getChild(UICategorySelect.class); 
        String category = uiCategorySelect.getSelectedCategory();
        if (category == null || category.trim().length() == 0) {  
          uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.selectGroup-required", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ; 
        }        
        contact.setCategories(new String[] { category });
        contactService.saveContact(username, contact, isNew_);
        if (isNew_) {
          if (uiAddressBook.getSelectedGroup() == null) uiWorkingContainer.setSelectedGroup(category) ;
          if (uiAddressBook.getSelectedGroup().equals(category)) uicontacts.updateContact(contact, isNew_) ;
          uiContactPreview.updateContact() ;
        } else {          
          uicontacts.updateContact(contact, isNew_) ;
          if (uiContactPreview.getContact() != null && contact.getId().equals(uiContactPreview.getContact().getId())) 
            uiContactPreview.setContact(contact) ;
        }
      }
      uiContactPreview.setLastUpdated(new Date()) ;
      uiContactPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPortlet) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIContactForm> {
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiForm = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      uiContactPortlet.cancelAction() ; 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPortlet) ;
    }
  }
  
  static  public class ChangeImageActionListener extends EventListener<UIContactForm> {
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiContactForm = event.getSource() ;
      UIPopupContainer popupContainer = uiContactForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.activate(UIImageForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
}
