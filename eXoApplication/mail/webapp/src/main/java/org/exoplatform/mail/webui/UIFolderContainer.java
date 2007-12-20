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
package org.exoplatform.mail.webui ;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.SessionsUtils;
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
        @EventConfig(listeners = UIFolderContainer.AddSubFolderActionListener.class),
        @EventConfig(listeners = UIFolderContainer.RenameFolderActionListener.class),
        @EventConfig(listeners = UIFolderContainer.RemoveFolderActionListener.class, confirm="UIFolderContainer.msg.confirm-remove-folder"),
        @EventConfig(listeners = UIFolderContainer.MarkReadActionListener.class),
        @EventConfig(listeners = UIFolderContainer.EmptyFolderActionListener.class)
    }
)
public class UIFolderContainer extends UIContainer {
  private String currentFolder_ = null ;
  public int i = 1;
  
  public UIFolderContainer() throws Exception {
    String accountId = MailUtils.getAccountId();
    currentFolder_ = Utils.createFolderId(accountId, Utils.FD_INBOX, false);
  }

  public String getSelectedFolder(){ return currentFolder_ ; }
  public void setSelectedFolder(String folderId) { currentFolder_ = folderId ;}
  
  public List<Folder> getDefaultFolders() throws Exception{
    return getFolders(false);
  } 
  
  public List<Folder> getCustomizeFolders() throws Exception{
    return getFolders(true);
  }
  
  public List<Folder> getSubFolders(String parentPath) throws Exception {
    MailService mailSvr = MailUtils.getMailService();
    String username = MailUtils.getCurrentUser() ;
    String accountId = MailUtils.getAccountId();
    List<Folder> subFolders = new ArrayList<Folder>();
    for (Folder f : mailSvr.getSubFolders(SessionsUtils.getSessionProvider(), username, accountId, parentPath)) {
      subFolders.add(f);
    }
    return subFolders ;
  }
  
  public String getCustomerFolderPath() throws Exception {
    MailService mailSvr = MailUtils.getMailService();
    String username = MailUtils.getCurrentUser() ;
    String accountId = MailUtils.getAccountId();
    String path = "";
    if (accountId != null) {
      path = mailSvr.getFolderHomePath(SessionsUtils.getSessionProvider(), username, accountId) ;
    } 
    return path;
  }
  
  public Folder getCurrentFolder() throws Exception{
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    String username = getAncestorOfType(UIMailPortlet.class).getCurrentUser() ;
    String accountId = getAncestorOfType(UINavigationContainer.class).
    getChild(UISelectAccount.class).getSelectedValue() ;
    return mailSvr.getFolder(SessionsUtils.getSessionProvider(), username, accountId, getSelectedFolder()) ;
  }
  
  public List<Folder> getFolders(boolean isPersonal) throws Exception{
    List<Folder> folders = new ArrayList<Folder>() ;
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    String username = getAncestorOfType(UIMailPortlet.class).getCurrentUser() ;
    String accountId = getAncestorOfType(UINavigationContainer.class).
    getChild(UISelectAccount.class).getSelectedValue() ;
    try {
      folders.addAll(mailSvr.getFolders(SessionsUtils.getSessionProvider(), username, accountId, isPersonal)) ;
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
      UIFolderContainer uiFolder = event.getSource() ;
      UIPopupAction uiPopup = uiFolder.getAncestorOfType(UIMailPortlet.class).getChild(UIPopupAction.class) ;
      uiPopup.activate(UIFolderForm.class, 450) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder.getAncestorOfType(UIMailPortlet.class)) ;
    }
  }
  
  static public class AddSubFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ; 
      UIFolderContainer uiFolder = event.getSource() ;
      UIPopupAction uiPopup = uiFolder.getAncestorOfType(UIMailPortlet.class).getChild(UIPopupAction.class) ;
      UIFolderForm uiFolderForm = uiPopup.createUIComponent(UIFolderForm.class, null, null);
      uiFolderForm.setParentPath(folderId);
      uiPopup.activate(uiFolderForm, 450, 0, false) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder.getAncestorOfType(UIMailPortlet.class)) ;
    }
  }
  
  static public class ChangeFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      UIFolderContainer uiFolder = event.getSource() ;
      UIMailPortlet uiPortlet = uiFolder.getAncestorOfType(UIMailPortlet.class);
      uiFolder.setSelectedFolder(folderId) ;
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
      UIMessageArea uiMessageArea = uiMessageList.getParent();
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      uiMessageList.setMessagePageList(mailSrv.getMessagePageListByFolder(SessionsUtils.getSessionProvider(), username, accountId, folderId));
      MessageFilter filter = new MessageFilter("Folder"); 
      filter.setAccountId(accountId);
      filter.setFolder(new String[] {folderId});
      uiMessageList.setMessageFilter(filter);
      uiMessageList.setSelectedFolderId(folderId);
      uiMessageList.setSelectedTagId(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea) ;
    }
  }
  
  static public class RenameFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;      
      UIFolderContainer uiFolder = event.getSource() ;
      UIPopupAction uiPopup = uiFolder.getAncestorOfType(UIMailPortlet.class).getChild(UIPopupAction.class) ;
      UIRenameFolderForm uiRenameFolderForm = uiPopup.activate(UIRenameFolderForm.class, 450) ;
      uiRenameFolderForm.setFolderId(folderId);
    }
  }

  static public class RemoveFolderActionListener extends EventListener<UIFolderContainer> {
    public void execute(Event<UIFolderContainer> event) throws Exception {
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;          
      UIFolderContainer uiFolderContainer = event.getSource() ;
      UIMailPortlet uiMailPortlet = uiFolderContainer.getAncestorOfType(UIMailPortlet.class);
      MailService mailService = uiMailPortlet.getApplicationComponent(MailService.class);
      String username = uiMailPortlet.getCurrentUser();
      UINavigationContainer uiNavigationContainer = uiFolderContainer.getAncestorOfType(UINavigationContainer.class);
      String accountId = uiNavigationContainer.getChild(UISelectAccount.class).getSelectedValue();
      
      Account account = mailService.getAccountById(SessionsUtils.getSessionProvider(), username, accountId);
      Folder folder = mailService.getFolder(SessionsUtils.getSessionProvider(), username, accountId, folderId);  
      mailService.removeUserFolder(SessionsUtils.getSessionProvider(), username, account, folder);      
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
      List<Message> messageList = mailSrv.getMessagePageListByFolder(SessionsUtils.getSessionProvider(), username, accountId, folderId).getAll(username);
      for (Message message : messageList) {
        mailSrv.removeMessage(SessionsUtils.getSessionProvider(), username, accountId, message.getId());
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
      List<Message> messageList = mailSrv.getMessagePageListByFolder(SessionsUtils.getSessionProvider(), username, accountId, folderId).getAll(username);
      List<String> msgIds = new ArrayList<String>() ;
      for (Message message : messageList) {
        if (message.isUnread()) {
          msgIds.add(message.getId());
        }
      }
      mailSrv.toggleMessageProperty(SessionsUtils.getSessionProvider(), username, accountId, msgIds, Utils.EXO_ISUNREAD);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
      UIFolderContainer uiFolder = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent()) ;
    }
  }
}