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

import org.exoplatform.container.PortalContainer;
import org.exoplatform.download.DownloadService;
import org.exoplatform.mail.service.Account;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.services.jcr.RepositoryService;
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
  private Message printMessage_ ;
  Account acc_ = null ;
  
  public UIPrintPreview() { }
  
  public Message getPrintMessage() throws Exception {
    return printMessage_ ;
  }
  
  public void setPrintMessage(Message msg) throws Exception {
    printMessage_ = msg ;
  }
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  public DownloadService getDownloadService() { 
    return getApplicationComponent(DownloadService.class) ; 
  }
  
  public void setAcc(Account a) { acc_ = a ; }  
  public Account getAccount() throws Exception {  return acc_ ; }
  
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
      context.getJavascriptManager().addJavascript("eXo.mail.UIMailPortlet.closePrint() ;");
      uiPrintPreview.getAncestorOfType(UIMailPortlet.class).cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPrintPreview.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
}