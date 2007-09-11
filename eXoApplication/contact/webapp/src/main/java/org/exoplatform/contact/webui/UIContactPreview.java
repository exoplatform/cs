/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui;

import java.util.Date;

import org.exoplatform.contact.service.Contact;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/contact/webui/UIContactPreview.gtmpl"
)
public class UIContactPreview extends UIComponent  {
  private Contact contact_ ; 
  private Date lastUpdated_ ;
  
  public UIContactPreview() throws Exception { }
  
  public void setContact(Contact c) { contact_ = c; }
  public Contact getContact() { return contact_; }
  
  public void setLastUpdated(Date s) { lastUpdated_ = s ; }
  public Date getLastUpdated() { return lastUpdated_ ; }
  
  public void updateContact() throws Exception {
    UIWorkingContainer uiWorkingContainer = getAncestorOfType(UIWorkingContainer.class) ; 
    UIContacts uicontacts = uiWorkingContainer.findFirstComponentOfType(UIContacts.class) ;
    if (uicontacts.getContacts().length > 0 ) setContact(uicontacts.getContacts()[0]) ;
  }
}
