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
  private Contact contact_ = null ; 
  private boolean isMaximized_ = false ;
  
  public UIContactPreview() throws Exception { }
  
  public void setContact(Contact c) { contact_ = c; }
  public Contact getContact() { return contact_; }

  public void setIsMaximized(boolean isMaximize) { isMaximized_ = isMaximize ; }
  public boolean getIsMaximize() { return isMaximized_ ; }
  
  public String getImageSource() throws Exception {
    return ContactUtils.getImageSource(contact_, getApplicationComponent(DownloadService.class)) ; 
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
