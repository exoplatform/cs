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

import javax.jcr.PathNotFoundException;

import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupComponent;
import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.services.jcr.util.IdGenerator;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.MandatoryValidator;
import org.exoplatform.webui.form.validator.SpecialCharacterValidator;


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
      @EventConfig(listeners = UIFolderForm.CancelActionListener.class, phase = Phase.DECODE)
    }
)
public class UIFolderForm extends UIForm implements UIPopupComponent {
  final public static String FIELD_NAME = "folderName" ;

  private String parentPath_ ;

  public UIFolderForm() throws Exception { 
    addUIFormInput(new UIFormStringInput(FIELD_NAME, FIELD_NAME, null).addValidator(MandatoryValidator.class).addValidator(SpecialCharacterValidator.class)) ;
  }

  public String getParentPath() { return parentPath_; }
  public void setParentPath(String s) { parentPath_ = s ; }

  static  public class SaveActionListener extends EventListener<UIFolderForm> {
    public void execute(Event<UIFolderForm> event) throws Exception {
      UIFolderForm uiForm = event.getSource();
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      MailService mailSvr = uiForm.getApplicationComponent(MailService.class);      
      String folderName = uiForm.getUIStringInput(FIELD_NAME).getValue();
      folderName = MailUtils.reduceSpace(folderName);
      String username = uiPortlet.getCurrentUser();
      String accountId = dataCache.getSelectedAccountId();
      UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
      folderName = folderName.trim();

      String folderId = Utils.KEY_FOLDERS + IdGenerator.generate();
      Folder folder = null;
      username = MailUtils.getDelegateFrom(accountId, dataCache);
      if (mailSvr.isExistFolder(username, accountId, uiForm.getParentPath(), folderName)) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIFolderForm.msg.folder-exist", new Object[] { folderName }));          
        return;
      }
      
      folder = new Folder();
      folder.setId(folderId);
      folder.setName(folderName);
      
      try {
        if (uiForm.getParentPath() != null && !"".equals(uiForm.getParentPath().trim())) {
          mailSvr.saveFolderImapOnline(username, accountId, uiForm.getParentPath(), folder);
        } else {
          mailSvr.saveFolderImapOnline(username, accountId, folder);
        }
      } catch (PathNotFoundException e) { 
        uiPortlet.findFirstComponentOfType(UIMessageList.class).setMessagePageList(null);
        uiPortlet.findFirstComponentOfType(UISelectAccount.class).refreshItems();
        event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet);
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIMessageList.msg.deleted_account", null, ApplicationMessage.WARNING));          
        return;
      } catch (Exception ex) {
        event.getRequestContext().getUIApplication().addMessage(new ApplicationMessage("UIMessageList.msg.cannot-save-folder", null, ApplicationMessage.WARNING));
        return;
      }
      
      uiForm.getAncestorOfType(UIPopupAction.class).cancelPopupAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPortlet.findFirstComponentOfType(UIMessageArea.class));
    }
  }

  static  public class CancelActionListener extends EventListener<UIFolderForm> {
    public void execute(Event<UIFolderForm> event) throws Exception {
      UIFolderForm uiForm = event.getSource() ;
      uiForm.getAncestorOfType(UIPopupAction.class).cancelPopupAction();      
    }
  }

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
}
