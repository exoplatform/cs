/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Author : Nguyen Quang Hung
 *          hung.nguyen@exoplatform.com
 * Aug 01, 2007
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class,
    template = "app:/templates/contact/webui/UIContactPortlet.gtmpl"
)
public class UIContactPortlet extends UIPortletApplication {
  public UIContactPortlet() throws Exception {
    addChild(UIBannerContainer.class, null, null) ;
    addChild(UIActionBar.class, null, null) ;
    UIWorkingContainer uiWorkingContainer = addChild(UIWorkingContainer.class, null, null) ;
    addChild(UIPopupAction.class, null, null) ;

    ContactService contactService = getApplicationComponent(ContactService.class);
    String username = Util.getPortalRequestContext().getRemoteUser() ;
    
    UIContacts uiContacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class) ;
    String id = null ;
    if(contactService.getGroups(username).size() > 0) {
      id = contactService.getGroups(username).get(0).getId();
    }
    uiContacts.setGroupId(id) ;
    
    UIContactPreview uiContactPreview = uiWorkingContainer.findFirstComponentOfType(UIContactPreview.class);
    Contact contact = null;
    if (contactService.getContactsByGroup(username, id).size() > 0) {
      contact = contactService.getContactsByGroup(username, id).get(0);
    }
    uiContactPreview.setContact(contact);
  }

  public void cancelAction() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    UIPopupAction popupAction = getChild(UIPopupAction.class) ;
    popupAction.deActivate() ;
    context.addUIComponentToUpdateByAjax(popupAction) ;
  }
}