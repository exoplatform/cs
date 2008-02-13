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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactAttachment;
import org.exoplatform.contact.service.ContactGroup;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
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
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.EmailAddressValidator;

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
      @EventConfig(listeners = UIAddContactForm.AddGroupActionListener.class),
      @EventConfig(listeners = UIAddContactForm.ChangeImageActionListener.class),
      @EventConfig(listeners = UIAddContactForm.DeleteImageActionListener.class),
      @EventConfig(listeners = UIAddContactForm.SaveActionListener.class),
      @EventConfig(listeners = UIAddContactForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
    
public class UIAddContactForm extends UIForm implements UIPopupComponent {
  public static final String SELECT_GROUP = "select-group".intern();
  public static final String NAME = "name".intern();
  public static final String FIRST_NAME = "first-name".intern();
  public static final String LAST_NAME = "last-name".intern();
  private static final String NICKNAME = "nickName";
  private static final String GENDER = "gender" ;
  private static final String BIRTHDAY= "birthday" ;
  private static final String DAY = "day" ;
  private static final String MONTH = "month" ;
  private static final String YEAR = "year" ; 
  private static final String JOBTITLE = "jobTitle";
  private static final String EMAIL = "email" ;
  private static final String MALE = "male" ;
  private static final String FEMALE = "female" ;
  private byte[] imageBytes = null;
  private String fileName = null ;
  private String imageMimeType = null ;
  
  public UIAddContactForm() throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    for (ContactGroup group : contactSrv.getGroups(SessionsUtils.getSessionProvider(), MailUtils.getCurrentUser())) {
      options.add(new SelectItemOption<String>(group.getName(), group.getId()));
    }
    addUIFormInput(new UIFormSelectBox(SELECT_GROUP, SELECT_GROUP, options));
    addUIFormInput(new UIFormStringInput(FIRST_NAME, FIRST_NAME, null));
    addUIFormInput(new UIFormStringInput(LAST_NAME, LAST_NAME, null));
    addUIFormInput(new UIFormStringInput(NICKNAME, NICKNAME, null));
    List<SelectItemOption<String>> genderOptions = new ArrayList<SelectItemOption<String>>() ;
    genderOptions.add(new SelectItemOption<String>(MALE, MALE));
    genderOptions.add(new SelectItemOption<String>(FEMALE, FEMALE));
    addUIFormInput(new UIFormRadioBoxInput(GENDER, MALE, genderOptions));
    addUIFormInput(new UIFormInputInfo(BIRTHDAY, BIRTHDAY, null)) ;
    
    List<SelectItemOption<String>> datesOptions = new ArrayList<SelectItemOption<String>>() ;
    datesOptions.add(new SelectItemOption<String>("- "+DAY+" -", DAY)) ;
    for (int i = 1; i < 32; i ++) {
      String date = i + "" ;
      datesOptions.add(new SelectItemOption<String>(date, date)) ;
    }
    addUIFormInput(new UIFormSelectBox(DAY, DAY, datesOptions)) ;
    
    List<SelectItemOption<String>> monthOptions = new ArrayList<SelectItemOption<String>>() ;
    monthOptions.add(new SelectItemOption<String>("-"+MONTH+"-", MONTH)) ;
    for (int i = 1; i < 13; i ++) {
      String month = i + "" ;
      monthOptions.add(new SelectItemOption<String>(month, month)) ;
    }
    addUIFormInput(new UIFormSelectBox(MONTH, MONTH, monthOptions)) ;

    String date = MailUtils.formatDate("dd/MM/yyyy", new Date()) ;
    String strDate = date.substring(date.lastIndexOf("/") + 1, date.length()) ; 
    int thisYear = Integer.parseInt(strDate) ;
    List<SelectItemOption<String>> yearOptions = new ArrayList<SelectItemOption<String>>() ;
    yearOptions.add(new SelectItemOption<String>("- "+YEAR+" -", YEAR)) ;
    for (int i = thisYear; i >= 1900; i --) {
      String year = i + "" ;
      yearOptions.add(new SelectItemOption<String>(year, year)) ;
    }
    addUIFormInput(new UIFormSelectBox(YEAR, YEAR, yearOptions)) ;

    addUIFormInput(new UIFormStringInput(JOBTITLE, JOBTITLE, null));
    addUIFormInput(new UIFormStringInput(EMAIL, EMAIL, null)
    .addValidator(EmailAddressValidator.class));
  }
  
  public void refreshGroupList() throws Exception{
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    ContactService contactSrv = getApplicationComponent(ContactService.class);
    for (ContactGroup group : contactSrv.getGroups(SessionsUtils.getSessionProvider(), MailUtils.getCurrentUser())) {
      options.add(new SelectItemOption<String>(group.getName(), group.getId()));
    }
    getUIFormSelectBox(SELECT_GROUP).setOptions(options);
  } 
  
  public void setFirstNameField(String firstName) throws Exception {
    getUIStringInput(FIRST_NAME).setValue(firstName);
  } 
  
  public void setLastNameField(String lastName) throws Exception {
    getUIStringInput(LAST_NAME).setValue(lastName);
  }
  
  public String getNickName() { 
    return getUIStringInput(NICKNAME).getValue() ; 
  }
  
  public String getJobTitle() {
    return getUIStringInput(JOBTITLE).getValue();
  }
  
  public void setEmailField(String email) throws Exception {
    getUIStringInput(EMAIL).setValue(email);
  }
  
  protected String getFieldGender() { 
    return getChild(UIFormRadioBoxInput.class).getValue() ; 
  }
  
  protected Date getFieldBirthday(){
    int day, month, year ;
    day = month = year = 0 ;
    boolean emptyDay, emptyMonth, emptyYear ;
    emptyDay = emptyMonth = emptyYear = false ;
    try {
      day = Integer.parseInt(getUIFormSelectBox(DAY).getValue()) ;
    } catch (NumberFormatException e) {
      emptyDay = true ;
    }
    try {
      month = Integer.parseInt(getUIFormSelectBox(MONTH).getValue()) ;
    } catch (NumberFormatException e) {
      emptyMonth = true ;
    }
    try {
      year = Integer.parseInt(getUIFormSelectBox(YEAR).getValue()) ;
    } catch (NumberFormatException e) {
      emptyYear = true ;
    }
    if (emptyDay && emptyMonth && emptyYear) return null ;
    else {
      Calendar cal = GregorianCalendar.getInstance() ;
      cal.setLenient(false) ;
      cal.set(Calendar.DATE, day) ;
      cal.set(Calendar.MONTH, month - 1) ;
      cal.set(Calendar.YEAR, year) ;
      return cal.getTime() ;
    }    
  }
  
  public String[] getActions() { return new String[] {"Save", "Cancel"} ; }
  
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
      
      if (MailUtils.isFieldEmpty(groupId)) {  
        uiApp.addMessage(new ApplicationMessage("UIAddContactForm.msg.group-required", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      } else if (MailUtils.isFieldEmpty(firstName) && MailUtils.isFieldEmpty(lastName)) {  
        uiApp.addMessage(new ApplicationMessage("UIAddContactForm.msg.name-required", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ; 
      }
        
      Contact contact = new Contact();
      contact.setAddressBook(new String[] {groupId});
      contact.setFullName(firstName + " " + lastName);
      contact.setFirstName(firstName);
      contact.setLastName(lastName);
      contact.setNickName(uiContact.getNickName());
      contact.setGender(uiContact.getFieldGender());
      contact.setBirthday(uiContact.getFieldBirthday());
      contact.setEmailAddress(email);
      if(uiContact.getImage() != null) {
        ContactAttachment attachment = new ContactAttachment() ;
        attachment.setInputStream(new ByteArrayInputStream(uiContact.getImage())) ;
        attachment.setFileName(uiContact.getFileName()) ;
        attachment.setMimeType(uiContact.getMimeType()) ;
        contact.setAttachment(attachment) ;        
      } else {
        contact.setAttachment(null) ;
      }
      contact.setJobTitle(uiContact.getJobTitle());
      ContactService contactSrv = uiContact.getApplicationComponent(ContactService.class);
      try {
        contactSrv.saveContact(SessionsUtils.getSessionProvider(), uiPortlet.getCurrentUser(), contact, true);
        UIAddressBookForm uiAddress = uiPortlet.findFirstComponentOfType(UIAddressBookForm.class);
        if (uiAddress != null) {
          uiAddress.updateGroup(groupId);
          uiAddress.refrestContactList(groupId);
          uiAddress.setSelectedContact(contact);
          event.getRequestContext().addUIComponentToUpdateByAjax(uiAddress) ;
        }
        uiContact.getAncestorOfType(UIPopupAction.class).deActivate() ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet) ;
      } catch(Exception e) { e.printStackTrace() ; }
    }
  }
  
  public static class CancelActionListener extends EventListener<UIAddContactForm> {
    public void execute(Event<UIAddContactForm> event) throws Exception {
      UIAddContactForm uiContactForm = event.getSource();
      UIPopupAction uiPopupAction = uiContactForm.getAncestorOfType(UIPopupAction.class) ; 
      uiPopupAction.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction) ;
    }
  }
  
  static  public class AddGroupActionListener extends EventListener<UIAddContactForm> {
    public void execute(Event<UIAddContactForm> event) throws Exception {
      UIAddContactForm uiContactForm = event.getSource() ;
      UIPopupActionContainer popupContainer = uiContactForm.getParent() ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.activate(UIAddGroupForm.class, 650) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class ChangeImageActionListener extends EventListener<UIAddContactForm> {
    public void execute(Event<UIAddContactForm> event) throws Exception {
      UIAddContactForm uiContactForm = event.getSource() ;
      UIPopupActionContainer popupContainer = uiContactForm.getAncestorOfType(UIPopupActionContainer.class) ;
      UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
      popupAction.activate(UIImageForm.class, 500) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
  
  static  public class DeleteImageActionListener extends EventListener<UIAddContactForm> {
    @Override
    public void execute(Event<UIAddContactForm> event) throws Exception {
      UIAddContactForm uiContactForm = event.getSource() ;
      uiContactForm.setImage(null) ;
      uiContactForm.setFileName(null) ;
      uiContactForm.setMimeType(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(
        uiContactForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
  
  protected void setImage(InputStream input) throws Exception{
    if (input != null) {
      imageBytes = new byte[input.available()] ; 
      input.read(imageBytes) ;
    }
    else imageBytes = null ;
  }
  protected byte[] getImage() {return imageBytes ;}

  protected String getMimeType() { return imageMimeType ;} ;
  protected void setMimeType(String mimeType) {imageMimeType = mimeType ;} 

  protected void setFileName(String name) { fileName = name ; }
  protected String getFileName() {return fileName ;}

  protected String getImageSource() throws Exception {    
    if(imageBytes == null || imageBytes.length == 0) return null;
    ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes) ;    
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image") ;
    dresource.setDownloadName(fileName) ;
    return  dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }
}