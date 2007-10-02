/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.util.Date;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Contact;
import org.exoplatform.download.DownloadService;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/contact/webui/popup/UIContactPreviewForm.gtmpl"
)
public class UIContactPreviewForm extends UIForm implements UIPopupComponent {
  private Contact contact_ ; 
  private Date lastUpdated_ ;
  public UIContactPreviewForm() { }
  
  public void setContact(Contact c) { contact_ = c; }
  public Contact getContact() { return contact_; }
  
  public void setLastUpdated(Date s) { lastUpdated_ = s ; }
  public Date getLastUpdated() { return lastUpdated_ ; }
  
  public String getImageSource() throws Exception {
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    return ContactUtils.getImageSource(contact_, dservice) ; 
  }

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }

}
