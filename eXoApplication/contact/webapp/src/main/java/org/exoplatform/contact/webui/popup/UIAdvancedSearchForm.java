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

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.SessionsUtils;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.contact.webui.UIAddressBooks;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.contact.webui.UITags;
import org.exoplatform.contact.webui.UIWorkingContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIAdvancedSearchForm.SearchActionListener.class),      
      @EventConfig(listeners = UIAdvancedSearchForm.CancelActionListener.class)
    }
)

public class UIAdvancedSearchForm extends UIForm implements UIPopupComponent {
  public final static String FIELD_TEXT_INPUT = "text" ;
  public static final String FIELD_FULLNAME_INPUT = "fullName";
  public static final String FIELD_FIRSTNAME_INPUT = "firstName";
  public static final String FIELD_LASTNAME_INPUT = "lastName";
  public static final String FIELD_NICKNAME_INPUT = "nickName";
  public static final String FIELD_BIRTHDAY_DATETIME = "birthday" ;
  public static final String FIELD_JOBTITLE_INPUT = "jobTitle";
  public static final String FIELD_EMAIL_INPUT = "preferredEmail" ;
  
  public final static String FIELD_GENDER_BOX = "gender" ;
  public static final String MALE = "Male".intern() ;
  public static final String FEMALE = "Female".intern() ;
  
  public UIAdvancedSearchForm() throws Exception {
    addUIFormInput(new UIFormStringInput(FIELD_TEXT_INPUT, FIELD_TEXT_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_FULLNAME_INPUT, FIELD_FULLNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_FIRSTNAME_INPUT, FIELD_FIRSTNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_LASTNAME_INPUT, FIELD_LASTNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_NICKNAME_INPUT, FIELD_NICKNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_JOBTITLE_INPUT, FIELD_JOBTITLE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_EMAIL_INPUT, FIELD_EMAIL_INPUT, null)
      .addValidator(EmailAddressValidator.class));
    
    List<SelectItemOption<String>> genders = new ArrayList<SelectItemOption<String>>() ;
    genders.add(new SelectItemOption<String>("", "")) ;
    genders.add(new SelectItemOption<String>(MALE, MALE)) ;
    genders.add(new SelectItemOption<String>(FEMALE, FEMALE)) ;
    addChild(new UIFormSelectBox(FIELD_GENDER_BOX, FIELD_GENDER_BOX, genders)) ;
  }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  static  public class SearchActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiAdvancedSearchForm = event.getSource() ;
      String text = uiAdvancedSearchForm.getUIStringInput(FIELD_TEXT_INPUT).getValue() ;
      String fullName = uiAdvancedSearchForm.getUIStringInput(FIELD_FULLNAME_INPUT).getValue() ;
      String firstName = uiAdvancedSearchForm.getUIStringInput(FIELD_FIRSTNAME_INPUT).getValue() ;
      String lastName = uiAdvancedSearchForm.getUIStringInput(FIELD_LASTNAME_INPUT).getValue() ;
      String nickName = uiAdvancedSearchForm.getUIStringInput(FIELD_NICKNAME_INPUT).getValue() ;
      String jobTitle = uiAdvancedSearchForm.getUIStringInput(FIELD_JOBTITLE_INPUT).getValue() ;
      String email = uiAdvancedSearchForm.getUIStringInput(FIELD_EMAIL_INPUT).getValue() ;
      String gender = uiAdvancedSearchForm.getUIFormSelectBox(FIELD_GENDER_BOX).getValue() ;
      
      if (ContactUtils.isEmpty(text) && ContactUtils.isEmpty(fullName) && ContactUtils.isEmpty(firstName) &&
          ContactUtils.isEmpty(lastName) && ContactUtils.isEmpty(nickName) &&
          ContactUtils.isEmpty(jobTitle) && ContactUtils.isEmpty(email) && ContactUtils.isEmpty(gender)) {
        UIApplication uiApp = uiAdvancedSearchForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.no-text-to-search", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;        
      }
      ContactFilter filter = new ContactFilter() ;
      if(!ContactUtils.isEmpty(text)) filter.setText(ContactUtils.filterString(text)) ;
      if(!ContactUtils.isEmpty(fullName)) filter.setFullName(ContactUtils.filterString(fullName)) ;   
      if(!ContactUtils.isEmpty(firstName)) filter.setFirstName(ContactUtils.filterString(firstName)) ;       
      if(!ContactUtils.isEmpty(lastName)) filter.setLastName(ContactUtils.filterString(lastName)) ;
      if(!ContactUtils.isEmpty(nickName)) filter.setNickName(ContactUtils.filterString(nickName)) ;      
      if(!ContactUtils.isEmpty(jobTitle)) filter.setJobTitle(ContactUtils.filterString(jobTitle)) ;      
      if(!ContactUtils.isEmpty(email)) filter.setEmailAddress(ContactUtils.filterString(email)) ;      
      if(!ContactUtils.isEmpty(gender)) filter.setGender(ContactUtils.filterString(gender)) ;
      
      DataPageList resultPageList = ContactUtils.getContactService()
        .searchContact(SessionsUtils.getSystemProvider(), ContactUtils.getCurrentUser(), filter) ;
      UIContactPortlet uiContactPortlet = uiAdvancedSearchForm.getAncestorOfType(UIContactPortlet.class) ;
      uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class).setSelectedGroup(null) ;
      uiContactPortlet.findFirstComponentOfType(UITags.class).setSelectedTag(null) ;      
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      uiContacts.setContacts(resultPageList) ;
      uiContacts.setViewContactsList(true) ;
      uiContacts.setDisplaySearchResult(true) ;
      uiContacts.setSelectedGroup(null) ;
      uiContacts.setSelectedTag(null) ;
      event.getRequestContext()
        .addUIComponentToUpdateByAjax(uiContactPortlet.getChild(UIWorkingContainer.class)) ;
      uiContactPortlet.cancelAction() ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiAdvancedSearchForm = event.getSource() ;
      UIPopupAction uiPopupAction = uiAdvancedSearchForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
    } 
  }
  
}