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

import org.exoplatform.mail.SessionsUtils;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIFolderForm.SaveActionListener.class), 
      @EventConfig(listeners = UIFolderForm.CancelActionListener.class)
    }
)
public class UIFolderForm extends UIForm implements UIPopupComponent {
  final public static String FIELD_NAME = "folderName" ;
  
  private String parentPath_ ;
  
  public UIFolderForm() { 
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null)) ;
  }

  public String getParentPath() { return parentPath_; }
  public void setParentPath(String s) { parentPath_ = s ; }
  
  static  public class SaveActionListener extends EventListener<UIFolderForm> {
    public void execute(Event<UIFolderForm> event) throws Exception {
      UIFolderForm uiForm = event.getSource() ;
      MailService mailSvr = uiForm.getApplicationComponent(MailService.class) ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String folderName = uiForm.getUIStringInput(FIELD_NAME).getValue() ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class) ;
      String username = uiPortlet.getCurrentUser() ;
      String accountId =  uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class) ;
      if(Utils.isEmptyField(folderName)) {
        uiApp.addMessage(new ApplicationMessage("UIFolderForm.msg.name-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      try {
        String folderId = accountId + "UserFolder" + folderName;
        Folder folder = mailSvr.getFolder(SessionsUtils.getSessionProvider(), username, accountId, folderId) ;
        if(folder == null) {
          System.out.println("d====" + folderId);
          folder = new Folder() ;
          folder.setId(folderId);
          folder.setName(folderName) ;
          folder.setLabel(folderName) ;
          if (uiForm.getParentPath() != null && !"".equals(uiForm.getParentPath().trim())) {
            mailSvr.saveFolder(SessionsUtils.getSessionProvider(), username, accountId, uiForm.getParentPath(), folder) ;
          } else {
            mailSvr.saveFolder(SessionsUtils.getSessionProvider(), username, accountId, folder) ;
          }
        } else {
          uiApp.addMessage(new ApplicationMessage("UIFolderForm.msg.folder-exist", new Object[]{folderName})) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      } catch (Exception e){
        uiApp.addMessage(new ApplicationMessage("UIFolderForm.msg.error-create-folder", null)) ;
        e.printStackTrace() ;
      }
      uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIFolderForm> {
    public void execute(Event<UIFolderForm> event) throws Exception {
      UIFolderForm uiForm = event.getSource() ;
      uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }

}
