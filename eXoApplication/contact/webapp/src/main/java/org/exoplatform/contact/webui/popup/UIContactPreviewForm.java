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
package org.exoplatform.contact.webui.popup;

import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.webui.UIContactPortlet;
import org.exoplatform.contact.webui.UIContacts;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.mail.service.Account;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/contact/webui/popup/UIContactPreviewForm.gtmpl",
    events = {  
      @EventConfig(listeners = UIContactPreviewForm.SendEmailActionListener.class) ,
      @EventConfig(listeners = UIContactPreviewForm.CancelActionListener.class)
    }
)
public class UIContactPreviewForm extends UIForm implements UIPopupComponent {
  private Contact contact_ ; 
  private boolean isPrintForm = false ;
  
  public UIContactPreviewForm() { }  
  public void setContact(Contact c) { contact_ = c; }
  public Contact getContact() { return contact_; }
  
  public void setPrintForm(boolean isPrint) { isPrintForm = isPrint ; }
  public boolean isPrintForm() { return isPrintForm ; }
  
  public String getPortalName() {
    PortalContainer pcontainer =  PortalContainer.getInstance() ;
    return pcontainer.getPortalContainerInfo().getContainerName() ;  
  }
  public String getRepository() throws Exception {
    RepositoryService rService = getApplicationComponent(RepositoryService.class) ;    
    return rService.getCurrentRepository().getConfiguration().getName() ;
  }
  
  public String[] getActions() { return new String[] {"Cancel"} ; }
  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  
  static  public class CancelActionListener extends EventListener<UIContactPreviewForm> {
    public void execute(Event<UIContactPreviewForm> event) throws Exception {
      UIContactPreviewForm uiContactPreviewForm = event.getSource() ;
      uiContactPreviewForm.setPrintForm(false) ;
      uiContactPreviewForm.getAncestorOfType(UIContactPortlet.class)
        .findFirstComponentOfType(UIContacts.class).setPrintDetail(false) ;
      UIPopupAction uiPopupAction = uiContactPreviewForm.getAncestorOfType(UIPopupAction.class) ;
      uiPopupAction.deActivate() ;
    }
  }
  
  static public class SendEmailActionListener extends EventListener<UIContactPreviewForm> {
    public void execute(Event<UIContactPreviewForm> event) throws Exception {
      UIContactPreviewForm uiForm = event.getSource() ;
      String email = event.getRequestContext().getRequestParameter(OBJECTID);
      if (!ContactUtils.isEmpty(email)) {
        UIPopupContainer popupContainer = uiForm.getAncestorOfType(UIPopupContainer.class) ;
        UIPopupAction popupAction = popupContainer.getChild(UIPopupAction.class) ;
        List<Account> acc = ContactUtils.getAccounts() ;
      /*  if (acc == null || acc.size() < 1) {
          UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
          uiApp.addMessage(new ApplicationMessage("UIComposeForm.msg.invalidAcc", null,
              ApplicationMessage.WARNING)) ;
          return ;
        }*/
        UIComposeForm uiComposeForm = popupAction.activate(UIComposeForm.class, 850) ;
        uiComposeForm.init(acc, email) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;       
      }
    }
  }
  
}
