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
package org.exoplatform.contact.webui;

import org.exoplatform.contact.webui.popup.UIAdvancedSearchForm;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.DataPageList;
import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/contact/webui/UISearchForm.gtmpl",
    events = {
      @EventConfig(listeners = UISearchForm.SearchActionListener.class),
      @EventConfig(listeners = UISearchForm.AdvancedSearchActionListener.class)    
    }
)
public class UISearchForm extends UIForm {
  final static  private String FIELD_SEARCHVALUE = "inputValue" ;
  public static ContactFilter filter ;
  
  public UISearchForm() {
    addChild(new UIFormStringInput(FIELD_SEARCHVALUE, FIELD_SEARCHVALUE, null)) ;
  }
  
  static  public class SearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiForm = event.getSource() ;
      String text = uiForm.getUIStringInput(UISearchForm.FIELD_SEARCHVALUE).getValue() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(ContactUtils.isEmpty(text)) {
        uiApp.addMessage(new ApplicationMessage("UISearchForm.msg.no-text-to-search", null,
          ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      //String textFiltered = ContactUtils.filterString(text, true) ;
      if(!ContactUtils.isNameValid(text, ContactUtils.specialString2)) {
        uiApp.addMessage(new ApplicationMessage("UISearchForm.msg.text-search-error", null,
          ApplicationMessage.WARNING)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      DataPageList resultPageList =  null ;
      if (!ContactUtils.isEmpty(text)) {
        ContactFilter filter = new ContactFilter() ;
        filter.setText(text) ;
        UISearchForm.filter = new ContactFilter() ;
        UISearchForm.filter.setText(text) ;        
        resultPageList = ContactUtils.getContactService()
          .searchContact(ContactUtils.getCurrentUser(), filter) ;
      }      
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class).setSelectedGroup(null) ;
      uiContactPortlet.findFirstComponentOfType(UITags.class).setSelectedTag(null) ;      
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;    
      uiContacts.setSelectedGroupBeforeSearch(uiContacts.getSelectedGroup()) ;
      uiContacts.setSelectedTagBeforeSearch_(uiContacts.getSelectedTag()) ;
      uiContacts.setSelectSharedContactsBeforeSearch(uiContacts.isSelectSharedContacts()) ;
      uiContacts.setViewListBeforeSearch(uiContacts.viewContactsList) ;
      
      uiContacts.setContacts(resultPageList) ;
      uiContacts.setDisplaySearchResult(true) ;
      uiContacts.setViewContactsList(true) ; 
      uiContacts.setSelectedGroup(null) ;
      uiContacts.setSelectedTag(null) ;
      uiContacts.setSelectSharedContacts(false) ;
      uiContacts.setSortedBy(UIContacts.fullName) ;
      event.getRequestContext()
        .addUIComponentToUpdateByAjax(uiContactPortlet.getChild(UIWorkingContainer.class)) ;
    }
  }  
  
  static  public class AdvancedSearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiForm = event.getSource() ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      UIPopupAction popupAction = uiContactPortlet.getChild(UIPopupAction.class) ;      
      popupAction.activate(UIAdvancedSearchForm.class, 600) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(popupAction) ;
    }
  }
}
