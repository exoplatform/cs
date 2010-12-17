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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.cs.common.webui.UIPopupComponent;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UISelectAccount;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Oct 25, 2007  
 */

@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    //TODO should not use individual template
    template = "app:/templates/mail/webui/popup/UIExportForm.gtmpl",
    events = {
      @EventConfig(listeners = UIExportForm.ExportActionListener.class), 
      @EventConfig(listeners = UIExportForm.CancelActionListener.class)
    }
)
public class UIExportForm extends UIForm implements UIPopupComponent {
  public static final String EXPORT_FILE_NAME = "export-file-name";
  public static final String EXPORT_FILE_TYPE = "export-file-type";
  private Message exportMessage_ ;

  public UIExportForm() throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    String[] mimeTypes = Utils.MIME_MAIL_TYPES;
    for (int i=0; i < mimeTypes.length; i++) {
      options.add(new SelectItemOption<String>("*." + mimeTypes[i], mimeTypes[i]));
    }   
    addUIFormInput(new UIFormStringInput(EXPORT_FILE_NAME, EXPORT_FILE_NAME, ""));
    addUIFormInput(new UIFormSelectBox(EXPORT_FILE_TYPE, EXPORT_FILE_TYPE, options));
  }
  
  public void setExportMessage(Message msg) throws Exception {
    getUIStringInput(EXPORT_FILE_NAME).setValue(msg.getSubject());
    exportMessage_ = msg ;
  }
  
  public Message getExportMessage() throws Exception { return exportMessage_; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }

  static public class ExportActionListener extends EventListener<UIExportForm> {
    public void execute(Event<UIExportForm> event) throws Exception {
      UIExportForm uiExportForm = event.getSource();
      UIMailPortlet uiPortlet = uiExportForm.getAncestorOfType(UIMailPortlet.class);
      Message msgExport = uiExportForm.getExportMessage();
      String username = MailUtils.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      MailService mailSrv = MailUtils.getMailService();      
      ByteArrayOutputStream outputStream = (ByteArrayOutputStream)mailSrv.exportMessage(username, accountId, msgExport);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
      String fileName = uiExportForm.getUIStringInput(EXPORT_FILE_NAME).getValue();
      //    Verify
      if (fileName == null || "".equals(fileName.trim())) {        
        UIApplication uiApp = uiExportForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIExportForm.msg.export-name-blank", null, ApplicationMessage.INFO)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return;
      }
      String type = uiExportForm.getUIFormSelectBox(EXPORT_FILE_TYPE).getValue();
      DownloadResource dresource = new InputStreamDownloadResource(inputStream, type);
      DownloadService dservice = (DownloadService)PortalContainer.getInstance().getComponentInstanceOfType(DownloadService.class);
      dresource.setDownloadName(fileName + "." + type);
      String downloadLink = dservice.getDownloadLink(dservice.addDownloadResource(dresource));
      event.getRequestContext().getJavascriptManager().addJavascript("ajaxRedirect('" + downloadLink + "');");
      uiPortlet.cancelAction() ;
    }
  }
  
  static public class CancelActionListener extends EventListener<UIExportForm> {
    public void execute(Event<UIExportForm> event) throws Exception {
      event.getSource().getAncestorOfType(UIMailPortlet.class).cancelAction();
    }
  }
}