/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui ;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIFolderForm;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.mail.webui.popup.UIRenameFolderForm;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    template = "app:/templates/mail/webui/UIFolderContainer.gtmpl",
    events = {
        @EventConfig(listeners = UIFolderContainer.ChangeFolderActionListener.class),
        @EventConfig(listeners = UIFolderContainer.AddFolderActionListener.class),
        @EventConfig(listeners = UIFolderContainer.RenameFolderActionListener.class),
        @EventConfig(listeners = UIFolderContainer.RemoveFolderActionListener.class, confirm="UIFolderContainer.msg.confirm-remove-folder"),
        @EventConfig(listeners = UIFolderContainer.MarkReadActionListener.class),
        @EventConfig(listeners = UIFolderContainer.EmptyFolderActionListener.class)
    }
)
public class UIFolderContainer extends UIContainer {
  private String currentFolder_ = null ;
  
  public UIFolderContainer() throws Exception {
    String accountId = MailUtils.getAccountId();
    currentFolder_ = Utils.createFolderId(accountId, Utils.FD_INBOX, false);
  }

  public String getSelectedFolder(){ return currentFolder_ ; }
  protected void setSelectedFolder(String folderId) { currentFolder_ = folderId ;}
  
  public List<Folder> getDefaultFolders() throws Exception{
    return getFolders(false);
  } 
  
  public List<Folder> getCustomizeFolders() throws Exception{
    return getFolders(true);
  }
  
  public Folder getCurrentFolder() throws Exception{
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    String username = getAncestorOfType(UIMailPortlet.class).getCurrentUser() ;
    String accountId = getAncestorOfType(UINavigationContainer.class).
    getChild(UISelectAccount.class).getSelectedValue() ;
    return mailSvr.getFolder(username, accountId, getSelectedFolder()) ;
  }
  
  public List<Folder> getFolders(boolean isPersonal) throws Exception{
    List<Folder> folders = new ArrayList<Folder>() ;
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    String username = getAncestorOfType(UIMailPortlet.class).getCurrentUser() ;
    String accountId = getAncestorOfType(UINavigationContainer.class).
    getChild(UISelectAccount.class).getSelectedValue() ;
    try {
      folders.addAll(mailSvr.getFolders(username, accountId, isPersonal)) ;
    } catch (Exception e){
      //e.printStackTrace() ;
    }
    return folders ;
  }
  
  public String[] getActions() {
    return new String[] {"AddFolder"} ;
  }
  
  static public class AddFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      System.out.println("\n\n AddFolderActionListener");
      UIFolderContainer uiFolder = event.getSource() ;
      UIPopupAction uiPopup = uiFolder.getAncestorOfType(UIMailPortlet.class).getChild(UIPopupAction.class) ;
      uiPopup.activate(UIFolderForm.class, 450) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder.getAncestorOfType(UIMailPortlet.class)) ;
    }
  }
  static public class ChangeFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      System.out.println("\n\n ChangeFolderActionListener");
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      UIFolderContainer uiFolder = event.getSource() ;
      UIMailPortlet uiPortlet = uiFolder.getAncestorOfType(UIMailPortlet.class);
      uiFolder.setSelectedFolder(folderId) ;
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
      UIMessageArea uiMessageArea = uiMessageList.getParent();
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      uiMessageList.setMessagePageList(mailSrv.getMessagePageListByFolder(username, accountId, folderId));
      MessageFilter filter = new MessageFilter("Folder"); 
      filter.setFolder(new String[] {folderId});
      uiMessageList.setMessageFilter(filter);
      uiMessageList.setSelectedFolderId(folderId);
      uiMessageList.setSelectedTagId(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea) ;
    }
  }
  
  static public class RenameFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;      
      System.out.println("=====>>>  RenameFolderActionListener ");
      
      UIFolderContainer uiFolder = event.getSource() ;
      UIPopupAction uiPopup = uiFolder.getAncestorOfType(UIMailPortlet.class).getChild(UIPopupAction.class) ;
      UIRenameFolderForm uiRenameFolderForm = uiPopup.activate(UIRenameFolderForm.class, 450) ;
      uiRenameFolderForm.setFolderId(folderId);
    }
  }

  static public class RemoveFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;          
      System.out.println("====>>>>  RemoveFolderActionListener : " + folderId );
  
      UIFolderContainer uiFolderContainer = event.getSource() ;
      UIMailPortlet uiMailPortlet = uiFolderContainer.getAncestorOfType(UIMailPortlet.class);
      MailService mailService = uiMailPortlet.getApplicationComponent(MailService.class);
      String username = uiMailPortlet.getCurrentUser();
      UINavigationContainer uiNavigationContainer = uiFolderContainer.getAncestorOfType(UINavigationContainer.class);
      String accountId = uiNavigationContainer.getChild(UISelectAccount.class).getSelectedValue();
      
      Account account = mailService.getAccountById(username, accountId);
      Folder folder = mailService.getFolder(username, accountId, folderId);  
      mailService.removeUserFolder(username, account, folder);      
    }
  }
  
  static public class EmptyFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFolderContainer uiFolderContainer = event.getSource() ;
      UIMailPortlet uiPortlet = uiFolderContainer.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      List<Message> messageList = mailSrv.getMessagePageListByFolder(username, accountId, folderId).getAll(username);
      for (Message message : messageList) {
        mailSrv.removeMessage(username, accountId, message.getId());
      }
    }
  }
  
  static public class MarkReadActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UIFolderContainer uiFolderContainer = event.getSource() ;
      UIMailPortlet uiPortlet = uiFolderContainer.getAncestorOfType(UIMailPortlet.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      List<Message> messageList = mailSrv.getMessagePageListByFolder(username, accountId, folderId).getAll(username);
      Folder folder = mailSrv.getFolder(username, accountId, folderId);
      for (Message message : messageList) {
        if (message.isUnread()) {
          message.setUnread(false);
          mailSrv.saveMessage(username, accountId, message, false);
          folder.setNumberOfUnreadMessage(folder.getNumberOfUnreadMessage() - 1);
        }
      }
      mailSrv.saveUserFolder(username, accountId, folder);
    }
  }
  
}