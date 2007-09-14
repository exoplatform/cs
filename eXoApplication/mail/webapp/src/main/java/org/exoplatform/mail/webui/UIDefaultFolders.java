/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui ;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
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
    template =  "app:/templates/mail/webui/UIDefaultFolders.gtmpl",
    events = {
        @EventConfig(listeners = UIDefaultFolders.ChangeFolderActionListener.class),
        @EventConfig(listeners = UIDefaultFolders.AddFolderActionListener.class),
        @EventConfig(listeners = UIDefaultFolders.RemoveAllMessagesActionListener.class)
    }
)

public class UIDefaultFolders extends UIContainer {
  private String currentFolder_ = null ;
  
  public UIDefaultFolders() throws Exception {}

  public String getSelectedFolder(){ return currentFolder_ ; }
  protected void setSelectedFolder(String folderName) { currentFolder_ = folderName ;}
  
  public List<Folder> getFolders() throws Exception{
    List<Folder> folders = new ArrayList<Folder>() ;
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    String username = getAncestorOfType(UIMailPortlet.class).getCurrentUser() ;
    String accountId = getAncestorOfType(UINavigationContainer.class).
    getChild(UISelectAccount.class).getSelectedValue() ;
    try {
      folders.addAll(mailSvr.getFolders(username, accountId, false)) ;
    } catch (Exception e){
      //e.printStackTrace() ;
    }
    return folders ;
  } 
  public String[] getActions() {
    return new String[] {"AddFolder"} ;
  }
  static public class AddFolderActionListener extends EventListener<UIDefaultFolders> {
    public void execute(Event<UIDefaultFolders> event) throws Exception {
      System.out.println("\n\n AddFolderActionListener");
      UIDefaultFolders uiFolder = event.getSource() ;
      UIPopupAction uiPopup = uiFolder.getAncestorOfType(UIMailPortlet.class).getChild(UIPopupAction.class) ;
      uiPopup.activate(UIFolderForm.class, 450) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolder.getAncestorOfType(UIMailPortlet.class)) ;
    }
  }
  static public class ChangeFolderActionListener extends EventListener<UIDefaultFolders> {
    public void execute(Event<UIDefaultFolders> event) throws Exception {
      System.out.println("\n\n ChangeFolderActionListener");
      String folderId = event.getRequestContext().getRequestParameter(OBJECTID) ;  
      UIDefaultFolders uiDFolder = event.getSource() ;
      UIMailPortlet uiPortlet = uiDFolder.getAncestorOfType(UIMailPortlet.class);
      UIFolderContainer uiFolderContainer = uiDFolder.getAncestorOfType(UIFolderContainer.class) ;
      UICustomizeFolders uiCFolder = uiFolderContainer.getChild(UICustomizeFolders.class) ;
      uiDFolder.setSelectedFolder(folderId) ;
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
      UIMessageArea uiMessageArea = uiMessageList.getParent();
      uiMessageList.setSelectedFolderId(folderId) ;
      uiMessageList.addCheckboxForMessages();
      uiCFolder.setSelectedFolder(null) ;
      uiMessageList.setSelectedTagName(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageArea) ;
    }
  }
  static public class RemoveAllMessagesActionListener extends EventListener<UIDefaultFolders> {
    public void execute(Event<UIDefaultFolders> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
}