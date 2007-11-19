/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;


/**
 * Created by The eXo Platform SARL
 * Author : HAI NGUYEN
 *          haiexo1002@gmail.com
 * September 14, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/UIMoveMessageForm.gtmpl",
    events = {
      @EventConfig(listeners = UIMoveMessageForm.SaveActionListener.class), 
      @EventConfig(listeners = UIMoveMessageForm.CancelActionListener.class)
    }
)
public class UIMoveMessageForm extends UIForm implements UIPopupComponent {
  final public static String FIELD_NAME = "folderName" ;
  final public static String SELECT_FOLDER = "folder" ;
  public static String folderId="";
  public List<Message> messageList=new ArrayList<Message>();  
  
  public UIMoveMessageForm() throws Exception { 
    MailService mailSrv = MailUtils.getMailService();
    String username = MailUtils.getCurrentUser();
    String accountId = MailUtils.getAccountId();
    
    List<SelectItemOption<String>> optionList = new ArrayList<SelectItemOption<String>>();   

    for (Folder folder : mailSrv.getFolders(username, accountId)) {   
      if(!folder.getName().equals("Sent"))
        optionList.add(new SelectItemOption<String>(folder.getName(), folder.getId()));       
    }    

    addUIFormInput(new UIFormSelectBox(SELECT_FOLDER, SELECT_FOLDER, optionList));
  }
  
  public void setMessageList(List<Message> messageList){
    this.messageList= messageList; 
  }
  
  public List<Message> getMessageList(){ return messageList; }
 
  public String getFolderId() throws Exception { return folderId; }
  
  public String getSelectedFolderId(){ return folderId; }
  
  static public class SaveActionListener extends EventListener<UIMoveMessageForm> {
    public void execute(Event<UIMoveMessageForm> event) throws Exception {
      System.out.println("=====>>>> Move Folder Action Listener");
      UIMoveMessageForm uiMoveMessageForm = event.getSource() ;
      MailService mailSrv = uiMoveMessageForm.getApplicationComponent(MailService.class) ;
      UIMailPortlet uiPortlet = uiMoveMessageForm.getAncestorOfType(UIMailPortlet.class) ;
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType((UIMessageList.class));
      String username = uiPortlet.getCurrentUser() ;
      String accountId =  MailUtils.getAccountId();
      String destFolder = uiMoveMessageForm.getUIFormSelectBox(SELECT_FOLDER).getValue();     
          
      String[] destFolders = { destFolder };  

      for(Message message: uiMessageList.getCheckedMessage()) {
         Folder oldFolder = mailSrv.getFolder(username, accountId, message.getFolders()[0]);
         message.setFolders(destFolders);         
         mailSrv.saveMessage(username, accountId, message, false);
         Folder folder = mailSrv.getFolder(username, accountId, message.getFolders()[0]);
         oldFolder.setTotalMessage(oldFolder.getTotalMessage() - 1);
         folder.setTotalMessage(folder.getTotalMessage() + 1);
         if (message.isUnread()) {           
           oldFolder.setNumberOfUnreadMessage(oldFolder.getNumberOfUnreadMessage() - 1);         
           folder.setNumberOfUnreadMessage(folder.getNumberOfUnreadMessage() + 1);                    
         }
         mailSrv.saveFolder(username, accountId, oldFolder);
         mailSrv.saveFolder(username, accountId, folder);
      }       
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));   
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMoveMessageForm.getAncestorOfType(UIPopupAction.class)) ;     
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class)) ;
      uiPortlet.cancelAction();
    }
  }
   
  static  public class CancelActionListener extends EventListener<UIMoveMessageForm> {
    public void execute(Event<UIMoveMessageForm> event) throws Exception {
      UIMoveMessageForm uiForm = event.getSource() ;
      uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
}
