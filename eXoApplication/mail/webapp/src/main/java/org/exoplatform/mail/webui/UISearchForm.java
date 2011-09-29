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
package org.exoplatform.mail.webui;

import org.exoplatform.cs.common.webui.UIPopupAction;
import org.exoplatform.mail.DataCache;
import org.exoplatform.mail.MailUtils;
import org.exoplatform.mail.service.MailService;
import org.exoplatform.mail.service.MessageFilter;
import org.exoplatform.mail.service.Utils;
import org.exoplatform.mail.webui.action.HasAccountEventListener;
import org.exoplatform.mail.webui.popup.UIAdvancedSearchForm;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Nov 02, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/mail/webui/UISearchForm.gtmpl",
    events = {
      @EventConfig(listeners = UISearchForm.SearchActionListener.class),
      @EventConfig(listeners = UISearchForm.AdvancedActionListener.class)
    }
)
public class UISearchForm extends UIForm {
  final static private String FIELD_SEARCHVALUE = "search" ;
  
  public UISearchForm() {
    addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null)) ;
  }
  
  public void setTextSearch(String txt) throws Exception {
    getUIStringInput(FIELD_SEARCHVALUE).setValue(txt);
  }
  
  public String getTextSearch() throws Exception {
    return getUIStringInput(FIELD_SEARCHVALUE).getValue();
  }
  
  static  public class SearchActionListener extends HasAccountEventListener<UISearchForm> {
    @Override
    public void processEvent(Event<UISearchForm> event) throws Exception {
      UISearchForm uiSearchForm = event.getSource();
      UIMailPortlet uiPortlet = uiSearchForm.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      
      UIMessageList uiMessageList = uiPortlet.findFirstComponentOfType(UIMessageList.class);
      String text = uiSearchForm.getUIStringInput(FIELD_SEARCHVALUE).getValue();
      MessageFilter filter = new MessageFilter("Search");
      filter.setHasStructure(uiMessageList.getMessageFilter().hasStructure());     
      String accId = dataCache.getSelectedAccountId();      
      if (MailUtils.isFieldEmpty(text)) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UISearchForm.msg.no-text-to-search", null));        
        return;
      } else if (!MailUtils.isSearchValid(text, MailUtils.SPECIALCHARACTER)) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UISearchForm.msg.contain-special-characters", null, ApplicationMessage.WARNING));
        
        return;
      } else {
        filter.setText(text);
      }
      filter.setAccountId(accId);
      
      String spamFolderId = Utils.generateFID(accId, Utils.FD_SPAM, false);
      String trashFolderId = Utils.generateFID(accId, Utils.FD_TRASH, false);
      String currentFolder = uiMessageList.getSelectedFolderId();
      if (currentFolder != null) {
        if (currentFolder.equals(spamFolderId)) {
          filter.setFolder(new String[] { spamFolderId });
        } else if (currentFolder.equals(trashFolderId)) {
          filter.setFolder(new String[] { trashFolderId });
        } else {
          filter.setExcludeFolders(new String[] { spamFolderId, trashFolderId });
        }
      } else {
        filter.setExcludeFolders(new String[] { spamFolderId, trashFolderId });
      }

      String username = uiPortlet.getCurrentUser();
      try {
        MailService mailService = uiPortlet.getApplicationComponent(MailService.class);
        uiMessageList.setMessageFilter(filter);
        uiMessageList.setMessagePageList(mailService.getMessagePageList(username, filter));
        uiMessageList.setSelectedFolderId(null);
        uiMessageList.setSelectedTagId(null);
        uiPortlet.findFirstComponentOfType(UIMessagePreview.class).setMessage(null);
        UIFolderContainer uiFolderContainer = uiPortlet.findFirstComponentOfType(UIFolderContainer.class);
        uiFolderContainer.setSelectedFolder(null);
        UITagContainer uiTagContainer = uiPortlet.findFirstComponentOfType(UITagContainer.class);
        uiTagContainer.setSelectedTagId(null);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiTagContainer);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiFolderContainer);
        event.getRequestContext().addUIComponentToUpdateByAjax(uiMessageList.getAncestorOfType(UIMessageArea.class));
      } catch (Exception e) {
        event.getRequestContext()
             .getUIApplication()
             .addMessage(new ApplicationMessage("UISearchForm.msg.contain-special-characters", null));        
        return;
      }
    }
  }
 
  static  public class AdvancedActionListener extends HasAccountEventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiSearchForm = event.getSource();
      UIMailPortlet uiPortlet = uiSearchForm.getAncestorOfType(UIMailPortlet.class);
      DataCache dataCache = uiPortlet.getDataCache();
      String accId = dataCache.getSelectedAccountId();

      UIPopupAction uiPopupAction = uiPortlet.getChild(UIPopupAction.class);
      UIAdvancedSearchForm uiAdvanceSearch = uiPopupAction.createUIComponent(UIAdvancedSearchForm.class, null, null);
      uiPopupAction.activate(uiAdvanceSearch, 600, 0, false);
      uiAdvanceSearch.init(accId);
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopupAction);
    }
  }
}
