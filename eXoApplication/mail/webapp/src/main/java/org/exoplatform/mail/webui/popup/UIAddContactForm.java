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
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.SessionsUtils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 8, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class, 
    template = "app:/templates/mail/webui/UIAddContactForm.gtmpl", 
    events = {
      @EventConfig(listeners = UIAddContactForm.SaveActionListener.class),
      @EventConfig(listeners = UIAddContactForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
    
public class UIAddContactForm extends UIForm implements UIPopupComponent {
  public static final String SELECT_GROUP = "select-group".intern();
  public static final String NAME = "name".intern();
  public static final String FIRST_NAME = "first-name".intern();
  public static final String LAST_NAME = "last-name".intern();
  public static final String EMAIL = "email".intern();
  public static final String CHAT_ID = "chat-id".intern();
  public static final String SELECT_CHAT = "select-chat".intern();
  public static final String PHONE = "phone".intern();
  public static final String EXO_ID = "exoId".intern();
  public static final String GOOGLE_ID = "googleId".intern();
  public static final String MSN_ID = "msnId".intern();
  public static final String AOL_ID = "aolId".intern();
  public static final String YAHOO_ID = "yahooId".intern();
  public static final String ICR_ID = "icrId".intern();
  public static final String SKYPE_ID = "skypeId".intern();
  public static final String ICQ_ID = "icqId".intern();
  public static final String CHOOSE_GROUP = "-- Choose group --".intern();
  
  public UIAddContactForm() throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    options.add(new SelectItemOption<String>(CHOOSE_GROUP, ""));
    for (ContactGroup group : contactSrv.getGroups(SessionsUtils.getSessionProvider(), MailUtils.getCurrentUser())) {
      options.add(new SelectItemOption<String>(group.getName(), group.getId()));
    }
    addUIFormInput(new UIFormSelectBox(SELECT_GROUP, SELECT_GROUP, options));
    addUIFormInput(new UIFormStringInput(FIRST_NAME, FIRST_NAME, null));
    addUIFormInput(new UIFormStringInput(LAST_NAME, LAST_NAME, null));
    addUIFormInput(new UIFormStringInput(EMAIL, EMAIL, null));
    addUIFormInput(new UIFormStringInput(CHAT_ID, CHAT_ID, null));
    List<SelectItemOption<String>> optionsChat = new ArrayList<SelectItemOption<String>>();
    optionsChat.add(new SelectItemOption<String>("eXo", EXO_ID));
    optionsChat.add(new SelectItemOption<String>("Yahoo", YAHOO_ID));
    optionsChat.add(new SelectItemOption<String>("Skype", SKYPE_ID));
    optionsChat.add(new SelectItemOption<String>("Google", GOOGLE_ID));
    optionsChat.add(new SelectItemOption<String>("Msn", MSN_ID));
    optionsChat.add(new SelectItemOption<String>("Aol", AOL_ID));
    optionsChat.add(new SelectItemOption<String>("Icr", ICR_ID));
    optionsChat.add(new SelectItemOption<String>("Icq", ICQ_ID));
    addUIFormInput(new UIFormSelectBox(SELECT_CHAT, SELECT_CHAT, optionsChat));
    addUIFormInput(new UIFormStringInput(PHONE, PHONE, null));
  }
  
  public void setFirstNameField(String firstName) throws Exception {
    getUIStringInput(FIRST_NAME).setValue(firstName);
  } 
  
  public void setLastNameField(String lastName) throws Exception {
    getUIStringInput(LAST_NAME).setValue(lastName);
  }
  
  public void setEmailField(String email) throws Exception {
    getUIStringInput(EMAIL).setValue(email);
  }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  public static class SaveActionListener extends EventListener<UIAddContactForm> {
    public void execute(Event<UIAddContactForm> event) throws Exception {
      UIAddContactForm uiContact = event.getSource() ;
      UIMailPortlet uiPortlet = uiContact.getAncestorOfType(UIMailPortlet.class); 
      UIApplication uiApp = uiContact.getAncestorOfType(UIApplication.class) ;
      String groupId = uiContact.getUIFormSelectBox(SELECT_GROUP).getValue();
      String firstName = uiContact.getUIStringInput(FIRST_NAME).getValue();
      String lastName = uiContact.getUIStringInput(LAST_NAME).getValue();
      String email = uiContact.getUIStringInput(EMAIL).getValue();
      String chatId = uiContact.getUIStringInput(CHAT_ID).getValue();
      String selectChatId = uiContact.getUIFormSelectBox(SELECT_CHAT).getValue();
      String phone = uiContact.getUIStringInput(PHONE).getValue();
      
      if (MailUtils.isFieldEmpty(groupId)) {  
        uiApp.addMessage(new ApplicationMessage("UIAddContactForm.msg.group-required", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      } else if (MailUtils.isFieldEmpty(firstName) && MailUtils.isFieldEmpty(lastName)) {  
        uiApp.addMessage(new ApplicationMessage("UIAddContactForm.msg.name-required", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      } else if (MailUtils.isFieldEmpty(email)) {  
        uiApp.addMessage(new ApplicationMessage("UIAddContactForm.msg.email-required", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      } 
        
      Contact contact = new Contact();
      contact.setAddressBook(new String[] {groupId});
      contact.setFullName(firstName + " " + lastName);
      contact.setFirstName(firstName);
      contact.setLastName(lastName);
      contact.setBirthday(new Date());
      contact.setEmailAddress(email);
      if (selectChatId.equals(EXO_ID)) {
        contact.setExoId(chatId) ;
      } else if (chatId.equals(YAHOO_ID)) {
        contact.setYahooId(chatId) ;
      } else if (chatId.equals(SKYPE_ID)) {
        contact.setSkypeId(chatId) ;
      } else if (chatId.equals(GOOGLE_ID)) {
        contact.setGoogleId(chatId) ;
      } else if (chatId.equals(MSN_ID)) {
        contact.setMsnId(chatId) ;
      } else if (chatId.equals(AOL_ID)) {
        contact.setAolId(chatId) ;
      } else if (chatId.equals(ICR_ID)) {
        contact.setIcrId(chatId) ;
      } else if (chatId.equals(ICQ_ID)) {
        contact.setIcqId(chatId) ;
      }
      contact.setMobilePhone(phone);
      ContactService contactSrv = uiContact.getApplicationComponent(ContactService.class);
      try {
        contactSrv.saveContact(SessionsUtils.getSessionProvider(), uiPortlet.getCurrentUser(), contact, true);
        uiContact.getAncestorOfType(UIPopupAction.class).deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
      } catch(Exception e) { e.printStackTrace() ; }
    }
  }
  
  public static class CancelActionListener extends EventListener<UIAddContactForm> {
    public void execute(Event<UIAddContactForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}