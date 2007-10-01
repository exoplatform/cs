/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import org.exoplatform.mail.service.Folder;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.webui.UIFolderContainer;
import org.exoplatform.mail.webui.UIMailPortlet;
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
import org.exoplatform.webui.form.UIFormInputInfo;
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
      @EventConfig(listeners = UIRenameFolderForm.SaveActionListener.class), 
      @EventConfig(listeners = UIRenameFolderForm.CancelActionListener.class)
    }
)
public class UIRenameFolderForm extends UIForm implements UIPopupComponent {
  final public static String CUR_FOLDER_NAME = "curFolderName" ;
  final public static String NEW_FOLDER_NAME = "newFolderName" ;
  private String folderName;
  public UIRenameFolderForm() {    
    addUIFormInput(new UIFormInputInfo(CUR_FOLDER_NAME, CUR_FOLDER_NAME, null)) ;
    addUIFormInput(new UIFormStringInput(NEW_FOLDER_NAME, NEW_FOLDER_NAME, null)) ;
  }

  public void activate() throws Exception {
    // TODO Auto-generated method stub

  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub

  }

  static  public class SaveActionListener extends EventListener<UIRenameFolderForm> {
    public void execute(Event<UIRenameFolderForm> event) throws Exception {
      UIRenameFolderForm uiForm = event.getSource() ;
      
      String curFolderName = uiForm.getFolderName();
      String newFolderName = uiForm.getUIStringInput(NEW_FOLDER_NAME).getValue() ;
      
      System.out.println(">>> RenameFolder : curFolderName = " + curFolderName);
      System.out.println(">>> RenameFolder : newFolderName = " + newFolderName);

      MailService mailService = uiForm.getApplicationComponent(MailService.class) ;
      UIMailPortlet uiMailPortlet = uiForm.getAncestorOfType(UIMailPortlet.class);
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      String username = uiMailPortlet.getCurrentUser() ;
      String accountId =  uiMailPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
      UIFolderContainer uiFolderContainer = uiMailPortlet.findFirstComponentOfType(UIFolderContainer.class) ;

      if(Utils.isEmptyField(newFolderName)) {
        uiApp.addMessage(new ApplicationMessage("UIFolderForm.msg.name-required", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      
      try {
        if(mailService.getFolder(username, accountId, newFolderName) == null) {
          Folder folder =  mailService.getFolder(username, accountId, curFolderName);
          folder.setLabel(newFolderName) ;
          folder.setName(newFolderName) ;
          mailService.saveUserFolder(username, accountId, folder) ;
        } else {
          uiApp.addMessage(new ApplicationMessage("UIFolderForm.msg.folder-exist", new Object[]{newFolderName})) ;
          event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
          return ;
        }
      } catch (Exception e){
        uiApp.addMessage(new ApplicationMessage("UIRenameFolderForm.msg.error-rename-folder", null)) ;
        e.printStackTrace() ;
      }
      uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
      
    }
  }
  static  public class CancelActionListener extends EventListener<UIRenameFolderForm> {
    public void execute(Event<UIRenameFolderForm> event) throws Exception {
      UIRenameFolderForm uiForm = event.getSource() ;
      uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
  public String getFolderName() {
    return folderName;
  }
  public void setFolderName(String folderName) {
    this.folderName = folderName;
    getUIFormInputInfo(CUR_FOLDER_NAME).setValue(folderName);
    
  }
}
