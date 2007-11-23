/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
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
    template =  "app:/templates/mail/webui/UIAdvancedSearchForm.gtmpl",
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
  
  public UIAdvancedSearchForm() throws Exception {
    String username = MailUtils.getCurrentUser();
    String accountId = MailUtils.getAccountId();
    MailService mailSrv = getApplicationComponent(MailService.class);
    List<SelectItemOption<String>> optionList = new ArrayList<SelectItemOption<String>>();    
    List<Folder> folderList = mailSrv.getFolders(username, accountId); 
    optionList.add(new SelectItemOption<String>(ALL_FOLDER_SEARCH, ""));
    for (Folder folder : folderList) {   
      optionList.add(new SelectItemOption<String>(folder.getName(), folder.getId()));    
    }    
    addUIFormInput(new UIFormSelectBox(SELECT_FOLDER_SEARCH, SELECT_FOLDER_SEARCH, optionList));
    
    addUIFormInput(new UIFormStringInput(FIELD_TO_SEARCH, null, null)) ;  
    addUIFormInput(new UIFormStringInput(FIELD_FROM_SEARCH, null, null)) ;  
    addUIFormInput(new UIFormStringInput(FIELD_SUBJECT_SEARCH, null, null)) ;
    addUIFormInput(new UIFormStringInput(FIELD_CONTENT_SEARCH, null, null)) ;
    UIFormDateTimeInput uiFormDateTimeInputBeforeDate = new UIFormDateTimeInput(FIELD_FROM_DATE, FIELD_FROM_DATE, null, false) ;
    UIFormDateTimeInput uiFormDateTimeInputAfterDate = new UIFormDateTimeInput(FIELD_TO_DATE, FIELD_TO_DATE, null, false) ;
    addUIFormInput(uiFormDateTimeInputBeforeDate) ;   
    addUIFormInput(uiFormDateTimeInputAfterDate) ;  
    
    List<SelectItemOption<String>>  options1 = new ArrayList<SelectItemOption<String>>() ;
    options1.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options1.add(new SelectItemOption<String>("doesn't contains", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    addUIFormInput(new UIFormSelectBox(SEARCH_BODY_CONDITION, SEARCH_BODY_CONDITION, options1));
    List<SelectItemOption<String>>  options2 = new ArrayList<SelectItemOption<String>>() ;
    options2.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options2.add(new SelectItemOption<String>("doesn't contains", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    options2.add(new SelectItemOption<String>("is", String.valueOf(Utils.CONDITION_IS)));
    options2.add(new SelectItemOption<String>("is not", String.valueOf(Utils.CONDITION_NOT_IS)));
    options2.add(new SelectItemOption<String>("starts with", String.valueOf(Utils.CONDITION_STARTS_WITH)));
    options2.add(new SelectItemOption<String>("ends with", String.valueOf(Utils.CONDITION_ENDS_WITH)));
    addUIFormInput(new UIFormSelectBox(SEARCH_TO_CONDITION, SEARCH_TO_CONDITION, options2));
    List<SelectItemOption<String>>  options3 = new ArrayList<SelectItemOption<String>>() ;
    options3.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options3.add(new SelectItemOption<String>("doesn't contains", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    options3.add(new SelectItemOption<String>("is", String.valueOf(Utils.CONDITION_IS)));
    options3.add(new SelectItemOption<String>("is not", String.valueOf(Utils.CONDITION_NOT_IS)));
    options3.add(new SelectItemOption<String>("starts with", String.valueOf(Utils.CONDITION_STARTS_WITH)));
    options3.add(new SelectItemOption<String>("ends with", String.valueOf(Utils.CONDITION_ENDS_WITH)));
    addUIFormInput(new UIFormSelectBox(SEARCH_FROM_CONDITION, SEARCH_FROM_CONDITION, options3));
    List<SelectItemOption<String>>  options4 = new ArrayList<SelectItemOption<String>>() ;
    options4.add(new SelectItemOption<String>("contains", String.valueOf(Utils.CONDITION_CONTAIN)));
    options4.add(new SelectItemOption<String>("doesn't contains", String.valueOf(Utils.CONDITION_NOT_CONTAIN)));
    options4.add(new SelectItemOption<String>("is", String.valueOf(Utils.CONDITION_IS)));
    options4.add(new SelectItemOption<String>("is not", String.valueOf(Utils.CONDITION_NOT_IS)));
    options4.add(new SelectItemOption<String>("starts with", String.valueOf(Utils.CONDITION_STARTS_WITH)));
    options4.add(new SelectItemOption<String>("ends with", String.valueOf(Utils.CONDITION_ENDS_WITH)));
    addUIFormInput(new UIFormSelectBox(SEARCH_SUBJECT_CONDITION, SEARCH_SUBJECT_CONDITION, options4));
    List<SelectItemOption<String>>  priorities = new ArrayList<SelectItemOption<String>>() ;
    priorities.add(new SelectItemOption<String>(" -- Choose Priority -- ", "0"));
    priorities.add(new SelectItemOption<String>("High", String.valueOf(Utils.PRIORITY_HIGH)));
    priorities.add(new SelectItemOption<String>("Normal", String.valueOf(Utils.PRIORITY_NORMAL)));
    priorities.add(new SelectItemOption<String>("Low", String.valueOf(Utils.PRIORITY_LOW)));
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
    return getUIFormSelectBox(SELECT_FOLDER_SEARCH).getValue() ;
  }
  
  public void setSelectedFolder(String folderId){
    getUIFormSelectBox(SELECT_FOLDER_SEARCH).setValue(folderId) ;
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
    return getUIFormDateTimeInput(FIELD_FROM_DATE).getCalendar();
  } 
  
  public Calendar getToDate() {
    return getUIFormDateTimeInput(FIELD_TO_DATE).getCalendar();
  } 
  
  public boolean hasStar() {
    return getUIFormCheckBoxInput(SEARCH_HAS_STAR).isChecked() ;
  }
  
  public boolean hasAttachment() {
    return getUIFormCheckBoxInput(SEARCH_HAS_ATTACH).isChecked() ;
  }
  
  public long getPriority() {
    return Long.valueOf(getUIFormSelectBox(SEARCH_PRIORITY).getValue()) ;
  }

  public void resetFields() { reset() ; }
  
  public void activate() throws Exception { }
  
  public void deActivate() throws Exception { }
  
  public String[] getActions() { return new String[] {"Search", "Cancel"}; }
  
  static  public class SearchActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      UIAdvancedSearchForm uiSearchForm = event.getSource() ;   
      UIMailPortlet uiPortlet = uiSearchForm.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);      
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailService = uiPortlet.getApplicationComponent(MailService.class);
      
      MessageFilter filter = new MessageFilter("Search");
      filter.setAccountId(accountId);
      String selectedFolderId = uiSearchForm.getSelectedFolder();
      if (selectedFolderId != null && selectedFolderId != "") {
        filter.setFolder(new String[] {uiSearchForm.getSelectedFolder()});
      }
      filter.setTo(uiSearchForm.getFieldEmailTo());
      filter.setToCondition(Integer.valueOf(uiSearchForm.getToCondition()));
      filter.setFrom(uiSearchForm.getFieldEmailFrom());
      filter.setFromCondition(Integer.valueOf(uiSearchForm.getFromCondition()));
      filter.setSubject(uiSearchForm.getSubject());
      filter.setSubjectCondition(Integer.valueOf(uiSearchForm.getSubjectCondition()));
      filter.setBody(uiSearchForm.getMessageBody());
      filter.setBodyCondition(Integer.valueOf(uiSearchForm.getBodyCondition()));
      filter.setFromDate(uiSearchForm.getFromDate());
      filter.setToDate(uiSearchForm.getToDate());
      filter.setHasStar(uiSearchForm.hasStar());
      filter.setHasAttach(uiSearchForm.hasAttachment());
      filter.setPriority(uiSearchForm.getPriority());
      uiMessageList.setSelectedFolderId(null);
      uiMessageList.setSelectedTagId(null);
      uiMessageList.setMessageFilter(filter);

      uiMessageList.setMessagePageList(mailService.getMessages(username, filter));
      uiMessageList.updateList();
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      uiFolderContainer.setSelectedFolder(null);
      uiPortlet.cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}
