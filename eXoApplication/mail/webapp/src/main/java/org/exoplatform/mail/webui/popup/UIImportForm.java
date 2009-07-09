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

import java.io.InputStream;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.mail.webui.UISelectFolder;
import org.exoplatform.upload.UploadResource;
import org.exoplatform.upload.UploadService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormUploadInput;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Oct 25, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "system:/groovy/webui/form/UIForm.gtmpl",
    events = {
      @EventConfig(listeners = UIImportForm.ImportActionListener.class), 
      @EventConfig(listeners = UIImportForm.CancelActionListener.class)
    }
)
public class UIImportForm extends UIForm implements UIPopupComponent {
  private static final String CHOOSE_MIME_MESSAGE = "choose-mime-message".intern();

  public UIImportForm() throws Exception { }

  public void init(String accId) throws Exception {
    addUIFormInput(new UIFormUploadInput(CHOOSE_MIME_MESSAGE, CHOOSE_MIME_MESSAGE));
    UISelectFolder uiSelectFolder = new UISelectFolder() ;
    addUIFormInput(uiSelectFolder);
    uiSelectFolder.init(accId) ;
  }

  public void setSelectedFolder(String value) throws Exception {
    getChild(UISelectFolder.class).setSelectedValue(value);
  } 

  public void activate() throws Exception { }

  public void deActivate() throws Exception { }

  static public class ImportActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiImport = event.getSource();
      MailService mailSrv = MailUtils.getMailService();
      UIMailPortlet uiPortlet = uiImport.getAncestorOfType(UIMailPortlet.class);
      UIFormUploadInput uiUploadInput = (UIFormUploadInput) uiImport.getUIInput(CHOOSE_MIME_MESSAGE);
      UploadResource uploadResource = uiUploadInput.getUploadResource();
      UIApplication  uiApp = uiImport.getAncestorOfType(UIApplication.class);
      if(uploadResource == null) {
        uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.upload-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      String name = uploadResource.getFileName() ;
      String fileType = name.substring(name.lastIndexOf(".") + 1, name.length()) ;
      boolean validType = false ;
      for (String type : Utils.MIME_MAIL_TYPES) {
        if(fileType.trim().toLowerCase().equals(type.toLowerCase())) {
          validType = true ;
          break ;
        }
      }
      if(!validType)  {
        uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.file-upload-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      InputStream inputStream = uiUploadInput.getUploadDataAsStream();
      String type = uploadResource.getMimeType();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
      String username = uiPortlet.getCurrentUser() ;
      String folderId = uiImport.getChild(UISelectFolder.class).getSelectedValue();
      if (!mailSrv.importMessage(username, accountId, folderId, inputStream, type)) {
        uiApp.addMessage(new ApplicationMessage("UIImportForm.msg.import-messages-error", null, 
            ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      } 
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class) ;
      uploadService.removeUpload(uiUploadInput.getUploadId()) ;
      uiPortlet.cancelAction() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIFolderContainer.class)) ;
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getParent());
    }
  }

  static public class CancelActionListener extends EventListener<UIImportForm> {
    public void execute(Event<UIImportForm> event) throws Exception {
      UIImportForm uiForm = event.getSource() ;
      UploadService uploadService = (UploadService)PortalContainer.getComponent(UploadService.class) ;
      UIFormUploadInput input = uiForm.getUIInput(CHOOSE_MIME_MESSAGE) ;
      uploadService.removeUpload(input.getUploadId()) ;
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}