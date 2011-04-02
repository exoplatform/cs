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

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.cs.common.webui.UIPopupComponent;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.UIMailPortlet;
import org.exoplatform.mail.webui.UIMessageList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;


/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam <phunghainam@gmail.com>
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template =  "app:/templates/mail/webui/popup/UIAskmeReturnReceipt.gtmpl",
                 events = {
                   @EventConfig(listeners = UIAskmeReturnReceipt.YesActionListener.class), 
                   @EventConfig(listeners = UIAskmeReturnReceipt.NoActionListener.class)
                 }
)
public class UIAskmeReturnReceipt extends UIForm implements UIPopupComponent {
  public static final String WARNING_ASKME_TEXT = "warning_askme_text".intern();
  private Message selectedMsg;

  public UIAskmeReturnReceipt() throws Exception { }

  public void activate() throws Exception {}
  public void deActivate() throws Exception{}

  public Message getSelectedMsg() { return selectedMsg; }
  public void setSelectedMsg(Message msg) { selectedMsg = msg; }

  static  public class YesActionListener extends EventListener<UIAskmeReturnReceipt> {
    public void execute(Event<UIAskmeReturnReceipt> event) throws Exception {
      UIAskmeReturnReceipt uiForm = event.getSource();
      UIPopupAction uiPopupAction = uiForm.getAncestorOfType(UIPopupAction.class) ; 
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMsgList= uiPortlet.findFirstComponentOfType(UIMessageList.class);
      String selectedMsgId = uiMsgList.getSelectedMessageId();
      String username = uiPortlet.getCurrentUser();
      String accId = uiMsgList.getAccountId();
      MailService mailService = uiForm.getApplicationComponent(MailService.class);
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      ResourceBundle res = event.getRequestContext().getApplicationResourceBundle() ;
      
      MailUtils.sendReturnReceipt(uiApp, event, username, accId, selectedMsgId, res);
      
      List<Message> msgs = new ArrayList<Message>();
      msgs.add(uiForm.getSelectedMsg());
      mailService.toggleMessageProperty(username, accId, msgs, "", Utils.IS_RETURN_RECEIPT, true);
      uiMsgList.updateList();
      uiPopupAction.deActivate();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiForm.getAncestorOfType(UIPopupAction.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgList.getParent()) ;
    }
  }

  static  public class NoActionListener extends EventListener<UIAskmeReturnReceipt> {
    public void execute(Event<UIAskmeReturnReceipt> event) throws Exception {
      UIAskmeReturnReceipt uiForm = event.getSource() ;
      UIMailPortlet uiPortlet = uiForm.getAncestorOfType(UIMailPortlet.class);
      MailService mailService = uiForm.getApplicationComponent(MailService.class);
      UIMessageList uiMsgList= uiPortlet.findFirstComponentOfType(UIMessageList.class);
      String username = uiPortlet.getCurrentUser();
      String accId = uiMsgList.getAccountId();
      List<Message> msgs = new ArrayList<Message>();
      msgs.add(uiForm.getSelectedMsg());
      mailService.toggleMessageProperty(username, accId, msgs,"", Utils.IS_RETURN_RECEIPT, false);
      uiMsgList.updateList();
      uiPortlet.cancelAction();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMsgList.getParent()) ;
    }
  }
}
