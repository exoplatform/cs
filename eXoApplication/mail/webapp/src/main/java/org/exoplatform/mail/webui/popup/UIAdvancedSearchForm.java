/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MessageFilter;
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
    template =  "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIAdvancedSearchForm.SearchActionListener.class), 
      @EventConfig(listeners = UIAdvancedSearchForm.CancelActionListener.class),
      @EventConfig(listeners = UIAdvancedSearchForm.FromActionListener.class),
      @EventConfig(listeners = UIAdvancedSearchForm.ToActionListener.class)
    }
)
public class UIAdvancedSearchForm extends UIForm implements UIPopupComponent{
  final public static String ALL_FOLDER_SEARCH = "All folder" ;
  final public static String SELECT_FOLDER_SEARCH = "folder" ; 
  final static public String FIELD_FROM_SEARCH = "from-field" ;
  final static public String FIELD_TO_SEARCH = "to-field" ;
  final static public String FIELD_SUBJECT_SEARCH = "subject-field" ;
  final static public String FIELD_CONTENT_SEARCH = "message-content" ;  
  final static public String ACT_TO_SEARCH = "To" ;  
  final static public String ACT_FROM_SEARCH = "From" ;
  
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
  }
  
  public void setFieldEmailFrom(String value) {
    getUIStringInput(FIELD_FROM_SEARCH).setValue(value);
  }
  
  public String getFieldEmailFrom() {
    return getUIStringInput(FIELD_FROM_SEARCH).getValue() ;
  }
  
  public void setFieldEmailTo(List<SelectItemOption<String>> options) {
    getUIFormSelectBox(FIELD_TO_SEARCH).setOptions(options) ;
  }

  public String getFieldEmailTo() {
    return getUIStringInput(FIELD_TO_SEARCH).getValue() ;
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
  
  public String getMessageBody(){
    return getUIStringInput(FIELD_CONTENT_SEARCH).getValue() ;
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
      UIPopupAction uiPopup = uiSearchForm.getAncestorOfType(UIPopupAction.class) ; 
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailService = uiPortlet.getApplicationComponent(MailService.class);
      
      MessageFilter filter = new MessageFilter("Advance Search");
      filter.setAccountId(accountId);
      String selectedFolderId = uiSearchForm.getSelectedFolder();
      if (selectedFolderId != null && selectedFolderId != "") {
        filter.setFolder(new String[] {uiSearchForm.getSelectedFolder()});
      }
      filter.setEmailTo(uiSearchForm.getFieldEmailTo());
      filter.setEmailFrom(uiSearchForm.getFieldEmailFrom());
      filter.setSubject(uiSearchForm.getSubject());
      filter.setBody(uiSearchForm.getMessageBody());     

      uiMessageList.setMessagePageList(mailService.getMessages(username, filter));
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static  public class FromActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {   
      UIAdvancedSearchForm uiAdvancedSearchForm = event.getSource() ;           
      UIPopupActionContainer uiActionContainer = uiAdvancedSearchForm.getAncestorOfType(UIPopupActionContainer.class) ;    
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;  
      UIAddressSearchForm uiAddressSearchForm = uiChildPopup.activate(UIAddressSearchForm.class, 700) ; 
      uiAddressSearchForm.setType("From"); 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  
  static  public class ToActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception { 
      UIAdvancedSearchForm uiAdvancedSearchForm = event.getSource() ;    
      UIPopupActionContainer uiActionContainer = uiAdvancedSearchForm.getAncestorOfType(UIPopupActionContainer.class) ;    
      UIPopupAction uiChildPopup = uiActionContainer.getChild(UIPopupAction.class) ;  
      UIAddressSearchForm uiAddressSearchForm = uiChildPopup.activate(UIAddressSearchForm.class, 700) ; 
      uiAddressSearchForm.setType("To"); 
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}
