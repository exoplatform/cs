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
package org.exoplatform.mail.webui ;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.SessionsUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.Message;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Tag;
import org.exoplatform.mail.webui.popup.UIEditTagForm;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/mail/webui/UITagContainer.gtmpl",
    events = {
        @EventConfig(listeners = UITagContainer.ChangeTagActionListener.class),
        @EventConfig(listeners = UITagContainer.EditTagActionListener.class),
        @EventConfig(listeners = UITagContainer.RemoveTagActionListener.class,confirm="UITagContainer.msg.confirm-remove-tag"),
        @EventConfig(listeners = UITagContainer.EmptyTagActionListener.class)
    }
)

public class UITagContainer extends UIComponent {
  public UITagContainer() throws Exception {}
  
  public List<Tag> getTags() throws Exception {
    List<Tag> tagList = new ArrayList<Tag>();
    MailService mailService = MailUtils.getMailService() ;
    UIMailPortlet uiPortlet = getAncestorOfType(UIMailPortlet.class);
    String username = uiPortlet.getCurrentUser() ;
    String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue() ;
    if (accountId != null && accountId != "") tagList = mailService.getTags(SessionsUtils.getSessionProvider(), username, accountId);
    return tagList;
  }
  
  static public class ChangeTagActionListener extends EventListener<UITagContainer> {
    public void execute(Event<UITagContainer> event) throws Exception {
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UITagContainer uiTags = event.getSource();
      UIMailPortlet uiPortlet = uiTags.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class) ;
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      uiMessageList.setMessagePageList(mailSrv.getMessagePagelistByTag(SessionsUtils.getSessionProvider(), username, accountId, tagId));
      MessageFilter filter = new MessageFilter("Tag"); 
      filter.setTag(new String[] { tagId });
      filter.setAccountId(accountId);
      uiMessageList.setMessageFilter(filter);
      uiMessageList.setSelectedTagId(tagId);
      uiMessageList.setSelectedFolderId(null);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTags);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
  
  static public class EditTagActionListener extends EventListener<UITagContainer> {
    public void execute(Event<UITagContainer> event) throws Exception {
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;      
      UITagContainer uiTag = event.getSource();
      UIPopupAction uiPopup = uiTag.getAncestorOfType(UIMailPortlet.class).getChild(UIPopupAction.class) ;
      UIEditTagForm uiRenameTagForm = uiPopup.activate(UIEditTagForm.class, 450) ;
      uiRenameTagForm.setTag(tagId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
  
  static public class RemoveTagActionListener extends EventListener<UITagContainer> {
    public void execute(Event<UITagContainer> event) throws Exception {
      System.out.println("============>>>> Remove Tag Action Listener");
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UITagContainer uiTag = event.getSource();     
      UIMailPortlet uiPortlet = uiTag.getAncestorOfType(UIMailPortlet.class);
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      mailSrv.removeTag(SessionsUtils.getSessionProvider(), username, accountId, tagId);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class)) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTag);
    }
  }
  
  static public class EmptyTagActionListener extends EventListener<UITagContainer> {
    public void execute(Event<UITagContainer> event) throws Exception {
      System.out.println("============>>>> Empty Tag Action Listener");
      String tagId = event.getRequestContext().getRequestParameter(OBJECTID) ;
      UITagContainer uiTag = event.getSource();     
      UIMailPortlet uiPortlet = uiTag.getAncestorOfType(UIMailPortlet.class);
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      MailService mailSrv = uiPortlet.getApplicationComponent(MailService.class);
      String username = uiPortlet.getCurrentUser();
      String accountId = uiPortlet.findFirstComponentOfType(UISelectAccount.class).getSelectedValue();
      
      List<Message> listMessage = mailSrv.getMessageByTag(SessionsUtils.getSessionProvider(), username, accountId, tagId);
      List<String> listTag = new ArrayList<String>();
      listTag.add(tagId);
      mailSrv.removeMessageTag(SessionsUtils.getSessionProvider(), username, accountId, listMessage, listTag);
      uiMessageList.updateList();
      event.getRequestContext().addUIComponentToUpdateByAjax(uiTag);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
    }
  }
}