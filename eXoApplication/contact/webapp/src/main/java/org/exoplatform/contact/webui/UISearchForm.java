/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

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
      @EventConfig(listeners = UISearchForm.SearchActionListener.class)      
    }
)
public class UISearchForm extends UIForm {
  final static  private String FIELD_SEARCH_INPUT = "search" ;
  
  public UISearchForm() {
    addChild(new UIFormStringInput(FIELD_SEARCH_INPUT, FIELD_SEARCH_INPUT, null)) ;
  }
  
  static  public class SearchActionListener extends EventListener<UISearchForm> {
    public void execute(Event<UISearchForm> event) throws Exception {
      UISearchForm uiForm = event.getSource() ;
      String text = uiForm.getUIStringInput(UISearchForm.FIELD_SEARCH_INPUT).getValue() ;
      UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
      if(ContactUtils.isEmpty(text)) {
        uiApp.addMessage(new ApplicationMessage("UISearchForm.msg.no-text-to-search", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ;
        return ;
      }
      ContactFilter filter = new ContactFilter() ;
      filter.setText(text) ;
      DataPageList resultPageList = 
        ContactUtils.getContactService().searchContact(ContactUtils.getCurrentUser(), filter) ;
      UIContactPortlet uiContactPortlet = uiForm.getAncestorOfType(UIContactPortlet.class) ;
      uiContactPortlet.findFirstComponentOfType(UIAddressBooks.class).setSelectedGroup(null) ;
      uiContactPortlet.findFirstComponentOfType(UITags.class).setSelectedTag(null) ;      
      UIContacts uiContacts = uiContactPortlet.findFirstComponentOfType(UIContacts.class) ;
      uiContacts.setContacts(resultPageList) ;    
      uiContacts.setDisplaySearchResult(true) ;
      uiContacts.setSelectedContact(null) ;
      uiContactPortlet.findFirstComponentOfType(UIContactPreview.class).setContact(null) ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactPortlet.getChild(UIWorkingContainer.class)) ;
    }
  }
  
}
