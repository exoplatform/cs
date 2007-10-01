/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.Date;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.download.DownloadService;
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
    template =  "app:/templates/contact/webui/UIContactPreview.gtmpl",
    events = {
        @EventConfig(listeners = UIContactPreview.MaximizeContactPaneActionListener.class)
    }
)
public class UIContactPreview extends UIComponent  {
  private Contact contact_ ; 
  private Date lastUpdated_ ;
  private boolean isMaximized_ = false ;
  
  public UIContactPreview() throws Exception { }
  
  public void setContact(Contact c) { contact_ = c; }
  public Contact getContact() { return contact_; }
  
  public void setLastUpdated(Date s) { lastUpdated_ = s ; }
  public Date getLastUpdated() { return lastUpdated_ ; }
  
  public void setIsMaximized(boolean isMaximize) { isMaximized_ = isMaximize ; }
  public boolean getIsMaximize() { return isMaximized_ ; }
  
  public void updateContact() throws Exception {
    UIContactContainer uiContactContainer = getParent() ; 
    UIContacts uicontacts = uiContactContainer.getChild(UIContacts.class) ;
    if (uicontacts.getContacts().length > 0 ) setContact(uicontacts.getContacts()[0]) ;
    else setContact(null) ;
  }
  
  public String getImageSource() throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    return ContactUtils.getImageSource(contact_, dservice) ; 
  }
  
  static public class MaximizeContactPaneActionListener extends EventListener<UIContactPreview> {
    public void execute(Event<UIContactPreview> event) throws Exception {
      UIContactPreview uiContactPreview = event.getSource() ;
      UIContactContainer uiContactContainer = uiContactPreview.getParent() ;
      UIContacts uiContacts = uiContactContainer.getChild(UIContacts.class) ;
      if (uiContactPreview.getIsMaximize()) {
        uiContacts.setRendered(true) ;
        uiContactPreview.setIsMaximized(false) ;
      } else {
        uiContacts.setRendered(false) ;
        uiContactPreview.setIsMaximized(true) ;
      }
      event.getRequestContext().addUIComponentToUpdateByAjax(uiContactContainer) ;
    }
  }

}
