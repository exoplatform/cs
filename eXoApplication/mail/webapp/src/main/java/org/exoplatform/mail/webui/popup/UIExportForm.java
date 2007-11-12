/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadResource;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
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
    template = "app:/templates/mail/webui/UIExportForm.gtmpl",
    events = {
      @EventConfig(listeners = UIExportForm.ExportActionListener.class), 
      @EventConfig(listeners = UIExportForm.CancelActionListener.class)
    }
)
public class UIExportForm extends UIForm implements UIPopupComponent {
  public static final String EXPORT_FILE_TYPE = "export-file-type";
  public static final String EXPORT_FILE_NAME = "export-file-name";

  public UIExportForm() throws Exception { 
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>();
    String[] mimeTypes = Utils.MIME_MAIL_TYPES;
    for (int i=0; i < mimeTypes.length; i++) {
      options.add(new SelectItemOption<String>("*." + mimeTypes[i], mimeTypes[i]));
    }   
    addUIFormInput(new UIFormStringInput(EXPORT_FILE_NAME, EXPORT_FILE_NAME, null));
    addUIFormInput(new UIFormSelectBox(EXPORT_FILE_TYPE, EXPORT_FILE_TYPE, options));
  }
  
  public void setExportFileName(String name) throws Exception {
    getUIStringInput(EXPORT_FILE_NAME).setValue(name);
  }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }

  static public class ExportActionListener extends EventListener<UIExportForm> {
    public void execute(Event<UIExportForm> event) throws Exception {
      System.out.println(" === >>> Export Mail");
      UIExportForm uiExportForm = event.getSource();
      UIMailPortlet uiPortlet = uiExportForm.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      String msgExport = uiMessageList.getSelectedMessageId();
      String username = MailUtils.getCurrentUser();
      String accountId = MailUtils.getAccountId();
      MailService mailSrv = MailUtils.getMailService();      
      ByteArrayOutputStream outputStream = (ByteArrayOutputStream)mailSrv.exportMessage(username, accountId, msgExport);
      ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
      String fileName = uiExportForm.getUIStringInput(EXPORT_FILE_NAME).getValue();
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