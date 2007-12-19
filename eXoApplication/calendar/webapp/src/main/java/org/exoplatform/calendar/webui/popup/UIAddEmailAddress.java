/**
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
 **/
package org.exoplatform.calendar.webui.popup;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 30, 2007  
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
    template = "app:/templates/calendar/webui/UIPopup/UIAddEmailAddress.gtmpl",
    events = {
        @EventConfig(listeners = UIAddEmailAddress.SaveActionListener.class),
        @EventConfig(listeners = UIAddEmailAddress.CancelActionListener.class)
      }
)
public class UIAddEmailAddress extends UIForm implements UIPopupComponent {

  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }
  static  public class SaveActionListener extends EventListener<UIAddEmailAddress> {
    public void execute(Event<UIAddEmailAddress> event) throws Exception {
      UIAddEmailAddress uiForm = event.getSource() ;
    }
  }
  static  public class CancelActionListener extends EventListener<UIAddEmailAddress> {
    public void execute(Event<UIAddEmailAddress> event) throws Exception {
      UIAddEmailAddress uiForm = event.getSource() ;
      uiForm.getAncestorOfType(UIPopupAction.class).deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax( uiForm.getAncestorOfType(UIPopupAction.class)) ;
    }
  }
}
