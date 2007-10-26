/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.webui.UIMailPortlet;
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
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
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
  final static public String INPUT_SEARCH = "fromInput" ;  
  final static public String FIELD_EMAILFROM_SEARCH = "from" ;
  final static public String FIELD_SUBJECT_SEARCH = "subject" ;
  final static public String FIELD_MESSAGECONTENT_SEARCH = "messageContent" ;  
  final static public String FIELD_EMAILTO_SEARCH = "to" ;
  final static public String FIELD_TO_INPUT_SEARCH = "toInput" ;  
  final static public String ACT_TO_SEARCH = "To" ;  
  final static public String ACT_FROM_SEARCH = "From" ;  
  final public static String SELECTED_FOLDER_SEARCH = "folder" ;
  final static public String FIELD_FROM = "from" ;
  final static public String FIELD_TO = "to" ;  
  final static public String FIELD_RECEIVE_BEFORE_DATE = "beforeDate" ;
  final static public String FIELD_RECEIVE_AFTER_DATE = "afterDate" ;  
  
  public List<Contact> ToContacts = new ArrayList<Contact>();  
  public List<Contact> getToContacts(){ return ToContacts; }  
  public void setToContacts(List<Contact> contactList){ ToContacts = contactList; } 
  
  public UIAdvancedSearchForm() throws Exception {
    UIFormInputWithActions inputSet = new UIFormInputWithActions(INPUT_SEARCH );     
    List<SelectItemOption<String>> optionList = new ArrayList<SelectItemOption<String>>();     
    inputSet.addUIFormInput(new UIFormSelectBox(SELECTED_FOLDER_SEARCH, SELECTED_FOLDER_SEARCH, optionList));
    
    List<ActionData> actions = new ArrayList<ActionData>() ; 
    ActionData fromAction = new ActionData() ;
    fromAction.setActionListener(ACT_FROM_SEARCH) ;
    fromAction.setActionType(ActionData.TYPE_LINK) ;
    fromAction.setActionName(ACT_FROM_SEARCH);
    actions.add(fromAction);
    inputSet.setActionField(FIELD_EMAILFROM_SEARCH, actions) ; 
    
    actions = new ArrayList<ActionData>() ;
    ActionData toAction = new ActionData() ;
    toAction.setActionListener(ACT_TO_SEARCH) ;
    toAction.setActionType(ActionData.TYPE_LINK) ;
    toAction.setActionName(ACT_TO_SEARCH);    
    actions.add(toAction);
    inputSet.setActionField(FIELD_EMAILTO_SEARCH, actions) ;
    
    inputSet.addUIFormInput(new UIFormStringInput(FIELD_EMAILTO_SEARCH, null, null)) ;  
    inputSet.addUIFormInput(new UIFormStringInput(FIELD_EMAILFROM_SEARCH, null, null)) ;  
    inputSet.addUIFormInput(new UIFormStringInput(FIELD_SUBJECT_SEARCH, null, null)) ;
    inputSet.addUIFormInput(new UIFormStringInput(FIELD_MESSAGECONTENT_SEARCH, null, null)) ;
    UIFormDateTimeInput uiFormDateTimeInputBeforeDate = new UIFormDateTimeInput(FIELD_RECEIVE_BEFORE_DATE, FIELD_RECEIVE_BEFORE_DATE, null, false) ;
    UIFormDateTimeInput uiFormDateTimeInputAfterDate = new UIFormDateTimeInput(FIELD_RECEIVE_AFTER_DATE, FIELD_RECEIVE_AFTER_DATE,null, false) ;
    inputSet.addUIFormInput(uiFormDateTimeInputBeforeDate) ;   
    inputSet.addUIFormInput(uiFormDateTimeInputAfterDate) ;  
    addUIFormInput(inputSet);
  }
  
  public void setFieldEmailFrom(List<SelectItemOption<String>> options) {
    UIFormInputWithActions inputSet = getChildById(INPUT_SEARCH) ;
    inputSet.getUIFormSelectBox(FIELD_EMAILFROM_SEARCH).setOptions(options) ;
  }

  public void setFieldEmailFrom(String value) {
    UIFormInputWithActions inputSet = getChildById(INPUT_SEARCH);
    inputSet.getUIStringInput(FIELD_EMAILFROM_SEARCH).setValue(value);
  }
  
  
  public void setFieldEmailTo(List<SelectItemOption<String>> options) {
    UIFormInputWithActions inputSet = getChildById(INPUT_SEARCH) ;
    inputSet.getUIFormSelectBox(FIELD_EMAILTO_SEARCH).setOptions(options) ;
  }

  public void setFieldEmailTo(String value) {
    UIFormInputWithActions inputSet = getChildById(INPUT_SEARCH);
    inputSet.getUIStringInput(FIELD_EMAILTO_SEARCH).setValue(value);
  }
  
  public String getFieldEmailFrom() {
    UIFormInputWithActions inputSet = getChildById(INPUT_SEARCH) ;
    
    return inputSet.getUIStringInput(FIELD_EMAILFROM_SEARCH).getValue() ;
  }
  public String getFieldEmailTo() {
    UIFormInputWithActions inputSet = getChildById(INPUT_SEARCH) ;
    
    return inputSet.getUIStringInput(FIELD_EMAILTO_SEARCH).getValue() ;
  }
  
 
  public String getSelectedFolder(){
    UIFormInputWithActions inputSet = getChildById(INPUT_SEARCH) ;  
    return inputSet.getUIFormSelectBox(SELECTED_FOLDER_SEARCH).getValue() ;
  }
  public String getSubject(){
    UIFormInputWithActions inputSet = getChildById(INPUT_SEARCH) ;
    return inputSet.getUIStringInput(FIELD_SUBJECT_SEARCH).getValue() ;
  }
  public String getMessageContent(){
    UIFormInputWithActions inputSet = getChildById(INPUT_SEARCH) ;
    return inputSet.getUIStringInput(FIELD_MESSAGECONTENT_SEARCH).getValue() ;
  }

 
  
  
  public void setFolderList(List<Folder> folderList) throws Exception {
    //TODO: improve later
    List<SelectItemOption<String>> optionList = new ArrayList<SelectItemOption<String>>();   
    optionList.add(new SelectItemOption<String>("Folders", "Folders"));  
    for (Folder folder : folderList) {   
      if(!folder.isPersonalFolder())
        optionList.add(new SelectItemOption<String>("------" + folder.getName(), folder.getId()));    
    }    
    optionList.add(new SelectItemOption<String>("My Folders", "My Folders"));
    for (Folder folder : folderList) {   
      if( folder.isPersonalFolder())
        optionList.add(new SelectItemOption<String>("------" + folder.getName(), folder.getId()));       
    } 
    UIFormInputWithActions inputSet = getChildById(INPUT_SEARCH);
    inputSet.getUIFormSelectBox(SELECTED_FOLDER_SEARCH).setOptions(optionList);
  }
  public void resetFields() { reset() ; } 
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  static  public class SearchActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {
      System.out.println("============>>>>>SearchActionListener");
      UIAdvancedSearchForm uiAdvancedSearchForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiAdvancedSearchForm.getAncestorOfType(UIMailPortlet.class);    
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);      
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailService = uiPortlet.getApplicationComponent(MailService.class);
      
      MessageFilter messageFilter = new MessageFilter("Advance Search !");
      
      messageFilter.setAccountId(accountId);
      String subject=uiAdvancedSearchForm.getSubject();
      if(subject!=null) messageFilter.setSubject(subject);
      if(uiAdvancedSearchForm.getMessageContent()!=null) messageFilter.setBody(uiAdvancedSearchForm.getMessageContent());     
      String folderId=uiAdvancedSearchForm.getSelectedFolder();
      
      String[] folderIdList=new String[1];
      folderIdList[0]=folderId;   
      messageFilter.setFolder(folderIdList); 
      if(uiAdvancedSearchForm.getFieldEmailFrom()!=null)
      messageFilter.setEmailFrom(uiAdvancedSearchForm.getFieldEmailFrom());

      uiMessageList.setMessagePageList(mailService.getMessages(username, messageFilter));
      uiMessageList.updateList();
      UIPopupAction uiChildPopup = uiAdvancedSearchForm.getAncestorOfType(UIPopupAction.class) ;   
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;

      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList);
      uiChildPopup.deActivate() ;
      
    }
  }
  
  static  public class FromActionListener extends EventListener<UIAdvancedSearchForm> {
    public void execute(Event<UIAdvancedSearchForm> event) throws Exception {   
      System.out.println(" ==========> FromActionListener") ;
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
      System.out.println(" ==========> ToActionListener") ;
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
      UIAdvancedSearchForm uiAddressSearchForm = event.getSource();    
      UIPopupAction uiChildPopup = uiAddressSearchForm.getAncestorOfType(UIPopupAction.class) ;   
      event.getRequestContext().addUIComponentToUpdateByAjax(uiChildPopup) ;      
      uiAddressSearchForm.deActivate();
      UIMailPortlet mailPortlet = event.getSource().getAncestorOfType(UIMailPortlet.class) ;
      mailPortlet.cancelAction() ;
 
    }
  }
  
  public String[] getActions() { return new String[] {"Search", "Cancel"}; }
}
