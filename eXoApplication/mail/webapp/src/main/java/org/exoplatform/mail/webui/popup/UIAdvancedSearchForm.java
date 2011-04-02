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
import java.util.Calendar;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.cs.common.webui.UIPopupComponent;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIFormDateTimePicker;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UIMessagePreview;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.mail.webui.UISelectFolder;
import org.exoplatform.mail.webui.UITagContainer;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;


/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 01, 2007 8:48:18 AM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template =  "app:/templates/mail/webui/popup/UIAdvancedSearchForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAdvancedSearchForm.SearchActionListener.class), 
      @EventConfig(listeners = UIAdvancedSearchForm.CancelActionListener.class)
    }
)
public class UIAdvancedSearchForm extends UIForm implements UIPopupComponent{
  final public static String ALL_FOLDER_SEARCH = "All folder" ;
  final public static String SELECT_FOLDER_SEARCH = "folder" ; 
  final static public String FIELD_FROM_SEARCH = "from-field" ;
  final static public String FIELD_TO_SEARCH = "to-field" ;
  final static public String FIELD_SUBJECT_SEARCH = "subject-field" ;
  final static public String FIELD_CONTENT_SEARCH = "message-content" ;  
  public static final String SEARCH_SUBJECT_CONDITION = "filter-subject-condition".intern();
  public static final String SEARCH_TO_CONDITION = "filter-to-condition".intern();
  public static final String SEARCH_FROM_CONDITION = "filter-from-condition".intern();
  public static final String SEARCH_BODY_CONDITION = "filter-body-condition".intern();
  public static final String SEARCH_PRIORITY = "search-priority".intern();
  public static final String SEARCH_HAS_STAR = "search-has-star".intern();
  public static final String SEARCH_HAS_ATTACH = "search-has-attachment".intern();
  
  final static public String FIELD_FROM_DATE = "from-date" ;
  final static public String FIELD_TO_DATE = "to-date" ;  
  
  public List<Contact> ToContacts = new ArrayList<Contact>();  
  public List<Contact> getToContacts(){ return ToContacts; }  
  public void setToContacts(List<Contact> contactList){ ToContacts = contactList; } 
  
  public UIAdvancedSearchForm() throws Exception {}
  
  public void init(String accountId) throws Exception {
    UISelectFolder uiSelectFolder = new UISelectFolder() ;
    addUIFormInput(uiSelectFolder);
    uiSelectFolder.init(accountId) ;
    
    addUIFormInput(new UIFormStringInput(FIELD_TO_SEARCH, null, null)) ;  
    addUIFormInput(new UIFormStringInput(FIELD_FROM_SEARCH, null, null)) ;  
    addUIFormInput(new UIFormStringInput(FIELD_SUBJECT_SEARCH, null, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_CONTENT_SEARCH, null, null)) ;
    UIFormDateTimePicker uiFormDateTimeInputBeforeDate = new UIFormDateTimePicker(FIELD_FROM_DATE, FIELD_FROM_DATE, null, false) ;
    UIFormDateTimePicker uiFormDateTimeInputAfterDate = new UIFormDateTimePicker(FIELD_TO_DATE, FIELD_TO_DATE, null, false) ;
    addUIFormInput(uiFormDateTimeInputBeforeDate) ;   
    addUIFormInput(uiFormDateTimeInputAfterDate) ;  
    
    List<SelectItemOption<String>>  options1 = new ArrayList<SelectItemOption<String>>() ;
    //TODO should replace string values here by resource bundle
    options1.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options1.add(new SelectItemOption<String>("doesn't contain", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    addUIFormInput(new UIFormSelectBox(SEARCH_BODY_CONDITION, SEARCH_BODY_CONDITION, options1));
    List<SelectItemOption<String>>  options2 = new ArrayList<SelectItemOption<String>>() ;
    options2.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options2.add(new SelectItemOption<String>("doesn't contain", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    options2.add(new SelectItemOption<String>("is", String.valueOf(Utils.CONDITION_IS)));
    options2.add(new SelectItemOption<String>("is not", String.valueOf(Utils.CONDITION_NOT_IS)));
    options2.add(new SelectItemOption<String>("starts with", String.valueOf(Utils.CONDITION_STARTS_WITH)));
    options2.add(new SelectItemOption<String>("ends with", String.valueOf(Utils.CONDITION_ENDS_WITH)));
    addUIFormInput(new UIFormSelectBox(SEARCH_TO_CONDITION, SEARCH_TO_CONDITION, options2));
    List<SelectItemOption<String>>  options3 = new ArrayList<SelectItemOption<String>>() ;
    options3.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options3.add(new SelectItemOption<String>("doesn't contain", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    options3.add(new SelectItemOption<String>("is", String.valueOf(Utils.CONDITION_IS)));
    options3.add(new SelectItemOption<String>("is not", String.valueOf(Utils.CONDITION_NOT_IS)));
    options3.add(new SelectItemOption<String>("starts with", String.valueOf(Utils.CONDITION_STARTS_WITH)));
    options3.add(new SelectItemOption<String>("ends with", String.valueOf(Utils.CONDITION_ENDS_WITH)));
    addUIFormInput(new UIFormSelectBox(SEARCH_FROM_CONDITION, SEARCH_FROM_CONDITION, options3));
    List<SelectItemOption<String>>  options4 = new ArrayList<SelectItemOption<String>>() ;
    options4.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options4.add(new SelectItemOption<String>("doesn't contain", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    options4.add(new SelectItemOption<String>("is", String.valueOf(Utils.CONDITION_IS)));
    options4.add(new SelectItemOption<String>("is not", String.valueOf(Utils.CONDITION_NOT_IS)));
    options4.add(new SelectItemOption<String>("starts with", String.valueOf(Utils.CONDITION_STARTS_WITH)));
    options4.add(new SelectItemOption<String>("ends with", String.valueOf(Utils.CONDITION_ENDS_WITH)));
    addUIFormInput(new UIFormSelectBox(SEARCH_SUBJECT_CONDITION, SEARCH_SUBJECT_CONDITION, options4));
    List<SelectItemOption<String>>  priorities = new ArrayList<SelectItemOption<String>>() ;
    priorities.add(new SelectItemOption<String>(" -- Choose Priority -- ", "priority.0"));
    priorities.add(new SelectItemOption<String>("High", "priority." +String.valueOf(Utils.PRIORITY_HIGH)));
    priorities.add(new SelectItemOption<String>("Normal", "priority." + String.valueOf(Utils.PRIORITY_NORMAL)));
    priorities.add(new SelectItemOption<String>("Low", "priority."  + String.valueOf(Utils.PRIORITY_LOW)));
    addUIFormInput(new UIFormSelectBox(SEARCH_PRIORITY, SEARCH_PRIORITY, priorities));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(SEARCH_HAS_STAR, SEARCH_HAS_STAR, false));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(SEARCH_HAS_ATTACH, SEARCH_HAS_ATTACH, false));
  }
  
  public void setFieldEmailFrom(String value) {
    getUIStringInput(FIELD_FROM_SEARCH).setValue(value);
  }
  
  public String getFieldEmailFrom() {
    return getUIStringInput(FIELD_FROM_SEARCH).getValue() ;
  }
  
  public String getFromCondition() throws Exception {
    return getUIFormSelectBox(SEARCH_FROM_CONDITION).getValue();
  }
  
  public void setFieldEmailTo(List<SelectItemOption<String>> options) {
    getUIFormSelectBox(FIELD_TO_SEARCH).setOptions(options) ;
  }

  public String getFieldEmailTo() {
    return getUIStringInput(FIELD_TO_SEARCH).getValue() ;
  }
  
  public String getToCondition() throws Exception {
    return getUIFormSelectBox(SEARCH_TO_CONDITION).getValue();
  }
  
  public String getSelectedFolder(){
    return getChild(UISelectFolder.class).getSelectedValue();
  }
  
  public void setSelectedFolder(String folderId){
    getChild(UISelectFolder.class).setSelectedValue(folderId);
  }
  
  public String getSubject(){
    return getUIStringInput(FIELD_SUBJECT_SEARCH).getValue() ;
  }
  
  public String getSubjectCondition() throws Exception {
    return getUIFormSelectBox(SEARCH_SUBJECT_CONDITION).getValue();
  }
  
  public String getMessageBody(){
    return getUIStringInput(FIELD_CONTENT_SEARCH).getValue() ;
  }
  
  public String getBodyCondition() throws Exception {
    return getUIFormSelectBox(SEARCH_BODY_CONDITION).getValue();
  }
  
  public Calendar getFromDate() {
    return getUIFormDateTimePicker(FIELD_FROM_DATE).getCalendar();
  } 
  
  public String getInputFromDate(){
   return getUIFormDateTimePicker(FIELD_FROM_DATE).getValue(); 
  }
  
  public String getInputToDate(){
    return getUIFormDateTimePicker(FIELD_TO_DATE).getValue(); 
   }
  
  public UIFormDateTimePicker getUIFormDateTimePicker(String id) {
    return findComponentById(id) ;
  }
  public Calendar getToDate() {
    return getUIFormDateTimePicker(FIELD_TO_DATE).getCalendar();
  } 
  
  public boolean hasStar() {
    return getUIFormCheckBoxInput(SEARCH_HAS_STAR).isChecked() ;
  }
  
  public boolean hasAttachment() {
    return getUIFormCheckBoxInput(SEARCH_HAS_ATTACH).isChecked() ;
  }
  
  public long getPriority() {
    String value = getUIFormSelectBox(SEARCH_PRIORITY).getValue() ;
    return Long.valueOf(value.substring(value.indexOf(".")+1), value.length()) ;
  }
  //TODO method never use
  public void resetFields() { reset() ; }
  
  public void activate() throws Exception { }
  
  public void deActivate() throws Exception { }
  
  public String[] getActions() { return new String[] {"Search", "Cancel"}; }

  static  public class SearchActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiSearchForm = event.getSource() ;   
      UIMailPortlet uiPortlet = uiSearchForm.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);  
      UIApplication uiApp = uiSearchForm.getAncestorOfType(UIApplication.class) ;
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailService = uiPortlet.getApplicationComponent(MailService.class);
      String to = uiSearchForm.getFieldEmailTo() ;
      String from = uiSearchForm.getFieldEmailFrom();
      String subject = uiSearchForm.getSubject();
      String body = uiSearchForm.getMessageBody();
      Calendar fromDate = uiSearchForm.getFromDate();
      Calendar toDate = uiSearchForm.getToDate();
      String fromDateText = uiSearchForm.getInputFromDate();
      String toDateText = uiSearchForm.getInputToDate();
      if(!MailUtils.isFieldEmpty(fromDateText)) {
        if(!MailUtils.isDate(fromDateText,"MM/dd/yyyy")){
          uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.invalid-date", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return;
        }
      }else if(!MailUtils.isFieldEmpty(toDateText)) {
        if(!MailUtils.isDate(toDateText,"MM/dd/yyyy")){
          uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.invalid-date", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return;
        }
      }
      if(!MailUtils.isFieldEmpty(fromDateText) && !MailUtils.isFieldEmpty(toDateText)){
        if(!MailUtils.isDate(fromDateText,"MM/dd/yyyy") || !MailUtils.isDate(toDateText,"MM/dd/yyyy")){
          uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.invalid-date", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return;
        }
        if(fromDate.after(toDate)){
          uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.date-time-invalid", null)) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      }  
      
      boolean hasStar = uiSearchForm.hasStar();
      boolean hasAttach = uiSearchForm.hasAttachment();
      long priority = uiSearchForm.getPriority();
      String folder = uiSearchForm.getSelectedFolder();
      if (Utils.isEmptyField(folder) && Utils.isEmptyField(from) && Utils.isEmptyField(to) && Utils.isEmptyField(subject) && (fromDate == null) && 
          (toDate == null) && Utils.isEmptyField(body) && !hasStar && !hasAttach && (priority == 0)) {
        uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.search-query-invalid", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      } else if(!MailUtils.isSearchValid(from, MailUtils.SPECIALCHARACTER) || !MailUtils.isSearchValid(to, MailUtils.SPECIALCHARACTER)
          || !MailUtils.isSearchValid(subject, MailUtils.SPECIALCHARACTER) || !MailUtils.isSearchValid(body, MailUtils.SPECIALCHARACTER)) {
        uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.search-query-invalid", null
                                                , ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      MessageFilter filter = new MessageFilter("Search");
      filter.setAccountId(accountId);
      String selectedFolderId = uiSearchForm.getSelectedFolder();
      if (selectedFolderId != null && selectedFolderId != "") {
        filter.setFolder(new String[] {folder});
      }
      filter.setTo(to);
      filter.setToCondition(Integer.valueOf(uiSearchForm.getToCondition()));
      filter.setFrom(from);
      filter.setFromCondition(Integer.valueOf(uiSearchForm.getFromCondition()));
      filter.setSubject(subject);
      filter.setSubjectCondition(Integer.valueOf(uiSearchForm.getSubjectCondition()));
      filter.setBody(body);
      filter.setBodyCondition(Integer.valueOf(uiSearchForm.getBodyCondition()));
      filter.setFromDate(fromDate);
      filter.setToDate(toDate);
      filter.setHasStar(hasStar);
      filter.setHasAttach(hasAttach);
      filter.setPriority(priority);
      uiMessageList.setSelectedFolderId(null);
      uiMessageList.setSelectedTagId(null);
      filter.setHasStructure(uiMessageList.getMessageFilter().hasStructure());
      uiMessageList.setMessageFilter(filter);
      try {
        uiMessageList.setMessagePageList(mailService.getMessagePageList(username, filter));
        uiPortlet.findFirstComponentOfType(UIMessagePreview.class).setMessage(null);
        UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
        uiFolderContainer.setSelectedFolder(null);
        UITagContainer uiTagContainer = uiPortlet.findFirstComponentOfType(UITagContainer.class); 
        uiTagContainer.setSelectedTagId(null);
        uiPortlet.cancelAction();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTagContainer);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
      } catch(Exception e) {
        uiApp.addMessage(new ApplicationMessage("UIAdvancedSearchForm.msg.search-query-invalid", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}
