/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui ;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.popup.UIFolderForm;
import org.exoplatform.mail.webui.popup.UIPopupAction;
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
        @EventConfig(listeners = UIFolderContainer.RemoveAllMessagesActionListener.class)
    }
)
public class UIFolderContainer extends UIContainer {
  private String currentFolder_ = Utils.FD_INBOX ;
  
  public UIFolderContainer() throws Exception {}

  public String getSelectedFolder(){ return currentFolder_ ; }
  protected void setSelectedFolder(String folderName) { currentFolder_ = folderName ;}
  
  public List<Folder> getDefaultFolders() throws Exception{
    return getFolders(false);
  } 
  
  public List<Folder> getCustomizeFolders() throws Exception{
    return getFolders(true);
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
  
  public long getNumberOfUnreadMessage(String selectedFolderName) throws Exception {
    long number = 0;
    MailService mailSrv = getApplicationComponent(MailService.class);
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    String username = uiPortlet.getCurrentUser();
    String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
    List<Message> messageList = mailSrv.getMessageByFolder(username, accountId, selectedFolderName);
    for (Message message : messageList) {
      if (message.isUnread()) { number++ ; }
    }
    return number;
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
      String folderName = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      UIFolderContainer uiFolder = event.getSource() ;
      UIMailPortlet uiPortlet = uiFolder.getAncestorOfType(UIMailPortlet.class);
      uiFolder.setSelectedFolder(folderName) ;
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
      UIMessageArea uiMessageArea = uiMessageList.getParent();
      uiMessageList.setSelectedFolderId(folderName) ;
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      uiMessageList.setMessageList(mailSrv.getMessageByFolder(username, accountId, folderName));
      uiMessageList.setSelectedTagName(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea) ;
    }
  }
  static public class RemoveAllMessagesActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
}