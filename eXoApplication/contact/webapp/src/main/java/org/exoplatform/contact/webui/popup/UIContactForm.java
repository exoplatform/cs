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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.SessionsUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContactPreview;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UIWorkingContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

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
      @EventConfig(listeners = UIContactForm.ChangeImageActionListener.class),
      @EventConfig(listeners = UIContactForm.DeleteImageActionListener.class),
      @EventConfig(phase = Phase.DECODE, listeners = UIContactForm.SelectPermissionActionListener.class)
    }
)
public class UIContactForm extends UIFormTabPane implements UISelector {
  private Contact contact_ = null ;
  private boolean isNew_ = true;
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
  public static final String INPUT_SHARETAB =  "shareContactTab" ;
  
  final static public String FIELD_EDITPERMISSION = "editPermission" ;
  public static String[] FIELD_SHAREDCONTACT_BOX = null ;
  public static String FIELD_INPUT_INFO = "selectGroups";
  private Map<String, String> permission_ = new HashMap<String, String>() ;

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
    UIFormInputWithActions sharing = new UIFormInputWithActions(INPUT_SHARETAB) ;
    sharing.addUIFormInput(new UIFormInputInfo(FIELD_INPUT_INFO, FIELD_INPUT_INFO, null)) ;
    
    String[] groups = ContactUtils.getUserGroups() ;
    FIELD_SHAREDCONTACT_BOX = new String[groups.length];
    for(int i = 0; i < groups.length; i ++) {
      FIELD_SHAREDCONTACT_BOX[i] = groups[i] ;
      sharing.addUIFormInput(
          new UIFormCheckBoxInput<Boolean>(FIELD_SHAREDCONTACT_BOX[i], FIELD_SHAREDCONTACT_BOX[i], false));
    } 
    List<ActionData> actions = new ArrayList<ActionData>() ;
    actions = new ArrayList<ActionData> () ;
    sharing.addUIFormInput(new UIFormStringInput(FIELD_EDITPERMISSION, null, null)) ;
    ActionData editPermission = new ActionData() ;
    editPermission.setActionListener("SelectPermission") ;
    editPermission.setActionName("SelectUser") ;
    editPermission.setActionParameter(UISelectComponent.TYPE_USER) ;
    editPermission.setActionType(ActionData.TYPE_ICON) ;
    editPermission.setCssIconClass("SelectUserIcon") ;
    
    actions.add(editPermission) ;
    ActionData membershipPerm = new ActionData() ;
    membershipPerm.setActionListener("SelectPermission") ;
    membershipPerm.setActionName("SelectMemberShip") ;
    membershipPerm.setActionParameter(UISelectComponent.TYPE_MEMBERSHIP) ;
    membershipPerm.setActionType(ActionData.TYPE_ICON) ;
    membershipPerm.setCssIconClass("SelectMemberIcon") ;
    actions.add(membershipPerm) ;
    sharing.setActionField(FIELD_EDITPERMISSION, actions) ;
    //sharing.setRendered(false) ;   
    
    addUIFormInput(ProfileTab) ;
    addUIFormInput(WorkTab) ;
    addUIFormInput(IMContactTab) ;    
    addUIFormInput(HomeTab) ;
    addUIFormInput(NoteTab) ;
    addChild(sharing) ;
    //setRenderedChild(UIProfileInputSet.class) ;  
    this.setSelectedTab(ProfileTab.getId());
  }
  
  public List<String> getCheckedSharedGroup() {
    List<String> groups = new ArrayList<String>() ;
    for (int i = 0; i < FIELD_SHAREDCONTACT_BOX.length; i ++) {
      if (getUIFormCheckBoxInput(FIELD_SHAREDCONTACT_BOX[i]).isChecked())
        groups.add(FIELD_SHAREDCONTACT_BOX[i]);
    }
    return groups ;
  }
  
  private Object[] getPublicGroups() throws Exception {
    OrganizationService organization = getApplicationComponent(OrganizationService.class) ;
    String currentUser = Util.getPortalRequestContext().getRemoteUser() ;
    return organization.getGroupHandler().findGroupsOfUser(currentUser).toArray() ;
  }
  
  @SuppressWarnings("unchecked")
  private List getCheckedObjectSharedGroup() throws Exception {
    List groups = new ArrayList() ;
    for(Object o : getPublicGroups()) {
      String groupId = ((Group)o).getId() ;
      UIFormCheckBoxInput<Boolean> input =  getUIFormCheckBoxInput(groupId) ;
      if(input != null && input.isChecked()) {
        groups.add(o) ;
      } 
    }
    return groups  ;
  }
  
  @Override
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }
  
  public void updateSelect(String selectField, String value) throws Exception {
    UIFormInputWithActions shareTab = getChildById(INPUT_SHARETAB) ;
    UIFormStringInput fieldInput = shareTab.getUIStringInput(selectField) ;
    permission_.put(value, value) ;
    StringBuilder sb = new StringBuilder() ;
    for(String s : permission_.values()) {
      if(sb != null && sb.length() > 0) sb.append(", ") ;
      sb.append(s) ;
    }
    fieldInput.setValue(sb.toString()) ;
    setSelectedTab(shareTab.getId()) ;
  }
  
  public void setNew(boolean isNew) { isNew_ = isNew ; }
  
  public void setValues(Contact contact) throws Exception {
    contact_ = contact ;
    if(contact.isShared()) {
      String[] categories = contact.getAddressBook();
      for (String category : categories) {
        UIFormCheckBoxInput checkBoxInput = getUIFormCheckBoxInput(category) ;
        if (checkBoxInput != null) checkBoxInput.setChecked(true) ;
      }
      String[] editPermission = contact.getEditPermission();
      StringBuffer editPermissionBuffer = new StringBuffer("");
      if (editPermission != null && editPermission.length > 0) {
        editPermissionBuffer.append(editPermission[0]);
        for (int i = 1; i < editPermission.length; i ++) 
          editPermissionBuffer.append("," + editPermission[i]);
      }   
      getUIStringInput(FIELD_EDITPERMISSION).setValue(editPermissionBuffer.toString());
    }
    String[] sharedBox = FIELD_SHAREDCONTACT_BOX ;
    for (String group : sharedBox) {
      getUIFormCheckBoxInput(group).setEnable(false) ;
    }
    getUIStringInput(FIELD_EDITPERMISSION).setEditable(false) ;
    UIProfileInputSet profileTab = getChildById(INPUT_PROFILETAB) ;
    profileTab.setFieldFirstName(contact.getFirstName());
    profileTab.setFieldLastName(contact.getLastName());
    profileTab.setFieldNickName(contact.getNickName());
    profileTab.setFieldGender(contact.getGender());    
    
    profileTab.setFieldBirthday(contact.getBirthday());
    profileTab.setFieldJobName(contact.getJobTitle());
    profileTab.setFieldEmail(contact.getEmailAddress());   
    ContactAttachment contactAttachment = contact.getAttachment();
    if (contactAttachment != null) profileTab.setImage(contactAttachment.getInputStream()) ;
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
    @Override
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiContactForm = event.getSource() ;
      UIApplication uiApp = uiContactForm.getAncestorOfType(UIApplication.class) ;
      UIProfileInputSet profileTab = uiContactForm.getChildById(INPUT_PROFILETAB) ;
      
      if (ContactUtils.isEmpty(profileTab.getFieldFirstName())) {  
        uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.firstName-required", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
      
      if (ContactUtils.isEmpty(profileTab.getFieldLastName())) {  
        uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.lastName-required", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }

      ContactService contactService = ContactUtils.getContactService();  
      String username = ContactUtils.getCurrentUser() ;
      Contact contact ;
      boolean isNew = uiContactForm.isNew_ ;
      if (isNew) contact = new Contact() ;
      else contact = uiContactForm.contact_ ;
      
      String firstName = profileTab.getFieldFirstName() ;
      String lastName = profileTab.getFieldLastName() ;
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
      
      contact.setJobTitle(profileTab.getFieldJobName());
      contact.setEmailAddress(profileTab.getFieldEmail());
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
      contact.setWebPage(uiContactForm.getUIStringInput(FIELD_WORKWEBPAGE_INPUT).getValue());
    
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
      contact.setPersonalSite(uiContactForm.getUIStringInput(FIELD_PERSONALSITE_INPUT).getValue());
      contact.setNote(uiContactForm.getUIFormTextAreaInput(FIELD_NOTE_INPUT).getValue());
      contact.setLastUpdated(new Date()) ;

      
      if (isNew) {
        List<String> sharedGroups = uiContactForm.getCheckedSharedGroup() ;
        if (sharedGroups.size() > 0) {
          contact.setAddressBook(sharedGroups.toArray(new String[] {})) ;
          String editPermission = uiContactForm.getUIStringInput(FIELD_EDITPERMISSION).getValue() ;
          if (!ContactUtils.isEmpty(editPermission))
            contact.setEditPermission(editPermission.split(","));
          contact.setShared(true) ;
          contactService.savePublicContact(SessionsUtils.getSystemProvider(), contact, isNew);
        } else {       
          UIPopupContainer popupContainer = uiContactForm.getParent() ;
          UICategorySelect uiCategorySelect = popupContainer.getChild(UICategorySelect.class); 
          String category = uiCategorySelect.getSelectedCategory();
          if (ContactUtils.isEmpty(category)) {
            uiApp.addMessage(new ApplicationMessage("UIContactForm.msg.selectGroup-required", null, 
                ApplicationMessage.WARNING)) ;
            event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
            return ;
          }
          contact.setAddressBook(new String[] { category });
          contactService.saveContact(SessionsUtils.getSessionProvider(), username, contact, isNew);
        }
      } else {
        if (contact.isShared()) contactService.savePublicContact(SessionsUtils.getSystemProvider(), contact, isNew) ;
        else contactService.saveContact(SessionsUtils.getSessionProvider(), username, contact, isNew) ;
      }
      UIContactPortlet uiContactPortlet = uiContactForm.getAncestorOfType(UIContactPortlet.class) ;
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      UIContactPreview uiContactPreview = uiContactPortlet.findFirstComponentOfType(UIContactPreview.class) ;
      
      //if (!ContactUtils.isEmpty(uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class).getSelectedGroup()))
        uiContacts.updateList() ;
      String selectedContact = uiContacts.getSelectedContact() ;
      if (!ContactUtils.isEmpty(selectedContact) && selectedContact.equals(contact.getId())) 
        uiContactPreview.setContact(contact) ;
      uiContactPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPortlet.getChild(UIWorkingContainer.class)) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIContactForm> {
    @Override
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiForm = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      uiContactPortlet.cancelAction() ;
    }
  }
  
  static  public class ChangeImageActionListener extends EventListener<UIContactForm> {
    @Override
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiContactForm = event.getSource() ;
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
      event.getRequestContext().addUIComponentToUpdateByAjax(
        uiContactForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
 
  static  public class SelectPermissionActionListener extends EventListener<UIContactForm> {
    @Override
    public void execute(Event<UIContactForm> event) throws Exception {
      UIContactForm uiForm = event.getSource() ;
      String permType = event.getRequestContext().getRequestParameter(OBJECTID) ;
      
      uiForm.setRenderedChild(INPUT_SHARETAB) ;
      List<String> checkedGroups = uiForm.getCheckedSharedGroup() ;
      if(checkedGroups.size() == 0) {
        UIApplication app = uiForm.getAncestorOfType(UIApplication.class) ;
        app.addMessage(new ApplicationMessage("UIContactForm.msg.checkbox-public-notchecked", null,
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(app.getUIPopupMessages()) ;
        return ;
      }
      UIGroupSelector uiGroupSelector = uiForm.createUIComponent(UIGroupSelector.class, null, null);
      uiGroupSelector.setType(permType) ;
      
      uiGroupSelector.setSelectedGroups(uiForm.getCheckedObjectSharedGroup()) ;
      uiGroupSelector.setComponent(uiForm, new String[] { FIELD_EDITPERMISSION });
      UIPopupContainer uiPopupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
      UIPopupAction uiChildPopup = uiPopupContainer.getChild(UIPopupAction.class) ;
      uiChildPopup.activate(uiGroupSelector, 500, 0, true) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;      
    }
  }
  
  
}
