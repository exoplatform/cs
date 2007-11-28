/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import org.exoplatform.download.DownloadService;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageArea;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 7, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/UIPrintPreview.gtmpl",
    events = {  
      @EventConfig(listeners = UIPrintPreview.PrintActionListener.class),
      @EventConfig(listeners = UIPrintPreview.CancelActionListener.class)
    }
)
public class UIPrintPreview extends UIForm implements UIPopupComponent {
  private String printMessage_ ;
  
  public UIPrintPreview() { }
  
  public String getPrintMessageId() throws Exception {
    return printMessage_;
  }
  
  public void setPrintMessageId(String msgId) throws Exception {
    printMessage_ = msgId ;
  }
  
  public Message getPrintMessage() throws Exception {
    String msgId = getPrintMessageId();
    String username = MailUtils.getCurrentUser();
    String accountId = MailUtils.getAccountId();
    MailService mailSrv = MailUtils.getMailService();
    try {
      return mailSrv.getMessageById(username, accountId, msgId);
    } catch(Exception e) {
      return null ;
    }
  }
  
  public DownloadService getDownloadService() { 
    return getApplicationComponent(DownloadService.class) ; 
  }
  
  public Account getAccount() throws Exception {
    String username = MailUtils.getCurrentUser();
    String accountId = MailUtils.getAccountId();
    MailService mailSrv = MailUtils.getMailService();
    try {
      return mailSrv.getAccountById(username, accountId);
    } catch(Exception e) {
      return null ;
    }
  }
  
  public String[] getAction() { return new String[] {"print", "cancel"}; }
  
  public void activate() throws Exception { }

  public void deActivate() throws Exception { }
  
  static  public class PrintActionListener extends EventListener<UIPrintPreview> {
    public void execute(Event<UIPrintPreview> event) throws Exception {
      
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIPrintPreview> {
    public void execute(Event<UIPrintPreview> event) throws Exception {
      UIPrintPreview uiPrintPreview = event.getSource();
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.getJavascriptManager().importJavascript("eXo.mail.UIMailPortlet","/mail/javascript/");
      context.getJavascriptManager().addJavascript("eXo.mail.UIMailPortlet.closePrint()");
      uiPrintPreview.getAncestorOfType(UIMailPortlet.class).cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPrintPreview.getAncestorOfType(UIPopupAction.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPrintPreview.getAncestorOfType(UIMailPortlet.class).findFirstComponentOfType(UIMessageArea.class)) ;
    }
  }
}