/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui ;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/mail/webui/UICustomizeFolders.gtmpl",
    events = {
        @EventConfig(listeners = UICustomizeFolders.ChangeFolderActionListener.class),
        @EventConfig(listeners = UICustomizeFolders.AddFolderActionListener.class),
        @EventConfig(listeners = UICustomizeFolders.RemoveFolderActionListener.class),
        @EventConfig(listeners = UICustomizeFolders.RenameFolderActionListener.class), 
        @EventConfig(listeners = UICustomizeFolders.RemoveAllMessagesFolderActionListener.class)
    }
)

public class UICustomizeFolders extends UIComponent {
  private String currentFolder_ = null ;
  
  public UICustomizeFolders() throws Exception {}
  public List<Folder> getFolders() throws Exception{
    List<Folder> folders = new ArrayList<Folder>() ;
    MailService mailSvr = getApplicationComponent(MailService.class) ;
    String username = getAncestorOfType(UIMailPortlet.class).getCurrentUser() ;
    String accountId = getAncestorOfType(UINavigationContainer.class).
    getChild(UISelectAccount.class).getSelectedValue() ;
    try {
      folders.addAll(mailSvr.getFolders(username, accountId, true)) ;
    } catch (Exception e){
      //e.printStackTrace() ;
    }
    return folders ;
  }
  public String[] getActions() {
    return new String[] {"AddFolder"} ;
  }
  public String getSelectedFolder(){return currentFolder_ ;}
  protected void setSelectedFolder(String folderName) { currentFolder_ = folderName ;}
  
  static public class ChangeFolderActionListener extends EventListener<UICustomizeFolders> {
    public void execute(Event<UICustomizeFolders> event) throws Exception {
      String folderName = event.getRequestContext().getRequestParameter(OBJECTID) ; 
      UICustomizeFolders uiCFolder = event.getSource() ;
      UIFolderContainer uiFolderContainer = uiCFolder.getAncestorOfType(UIFolderContainer.class) ;
      UIDefaultFolders uiDFolder = uiFolderContainer.getChild(UIDefaultFolders.class) ;
      uiCFolder.setSelectedFolder(folderName) ;
      uiDFolder.setSelectedFolder(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer) ;
    }
  }
  static public class AddFolderActionListener extends EventListener<UICustomizeFolders> {
    public void execute(Event<UICustomizeFolders> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  static public class RemoveFolderActionListener extends EventListener<UICustomizeFolders> {
    public void execute(Event<UICustomizeFolders> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  static public class RenameFolderActionListener extends EventListener<UICustomizeFolders> {
    public void execute(Event<UICustomizeFolders> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
  static public class RemoveAllMessagesFolderActionListener extends EventListener<UICustomizeFolders> {
    public void execute(Event<UICustomizeFolders> event) throws Exception {
      String path = event.getRequestContext().getRequestParameter(OBJECTID) ;      
    }
  }
}