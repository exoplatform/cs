/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.chatbar.webui;

import javax.portlet.PortletPreferences;

import org.exoplatform.web.application.ApplicationMessage;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIApplication;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jul 6, 2010  
 */

@ComponentConfig(
                 lifecycle = UIFormLifecycle.class,
                 template = "system:/groovy/webui/form/UIForm.gtmpl",
                 //TODO using this template to customize app:/templates/chatbar/webui/UIConfigForm.gtmpl
                 events = {
                   @EventConfig(listeners = UIConfigForm.SaveActionListener.class)
                  // @EventConfig(listeners = UIConfigForm.CloseActionListener.class, phase = Phase.DECODE)
                 }
)

public class UIConfigForm extends UIForm {

  public static final String CAL_APP = "showCalendarLink".intern();
  public static final String MAIL_APP = "showMailLink".intern();
  public static final String CON_APP = "showContactLink".intern();

  public static final String CAL_URL = "calendarUrl".intern();
  public static final String MAIL_URL = "mailUrl".intern();
  public static final String CON_URL = "contactUrl".intern();

  public static final String INFO = "info".intern();


  public UIConfigForm() throws Exception{
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(CAL_APP, CAL_APP, true)) ;
    addUIFormInput(new UIFormStringInput(CAL_URL, CAL_URL, "")) ;

    addUIFormInput(new UIFormCheckBoxInput<Boolean>(MAIL_APP, MAIL_APP, true)) ;
    addUIFormInput(new UIFormStringInput(MAIL_URL, MAIL_URL, "")) ;

    addUIFormInput(new UIFormCheckBoxInput<Boolean>(CON_APP, CON_APP, true)) ;
    addUIFormInput(new UIFormStringInput(CON_URL, CON_URL, "")) ;

    addUIFormInput(new UIFormInputInfo(INFO, INFO, null));

  }

  protected void init() {
    try {
      PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
      PortletPreferences preferences = pcontext.getRequest().getPreferences() ;
      getUIFormCheckBoxInput(CAL_APP).setChecked(Boolean.getBoolean(preferences.getValue(CAL_APP, null)));
      getUIFormCheckBoxInput(MAIL_APP).setChecked(Boolean.getBoolean(preferences.getValue(MAIL_APP, null)));
      getUIFormCheckBoxInput(CON_APP).setChecked(Boolean.getBoolean(preferences.getValue(CON_APP, null)));

      getUIStringInput(CAL_URL).setValue(preferences.getValue(CAL_URL, null));
      getUIStringInput(MAIL_URL).setValue(preferences.getValue(MAIL_URL, null));
      getUIStringInput(CON_URL).setValue(preferences.getValue(CON_URL, null));

      getUIFormInputInfo(INFO).setValue(getLabel(preferences.getValue(INFO, null))) ; 

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  static  public class SaveActionListener extends EventListener<UIConfigForm>{

    public void execute(Event<UIConfigForm> event) throws Exception {
      try {
        UIConfigForm uiForm = event.getSource() ;
        PortletRequestContext pcontext = (PortletRequestContext)WebuiRequestContext.getCurrentInstance() ;
        PortletPreferences preferences = pcontext.getRequest().getPreferences() ;
        //TODO update value here

        preferences.store();
        UIApplication uiApp = uiForm.getAncestorOfType(UIApplication.class) ;
        uiApp.addMessage(new ApplicationMessage("UIConfigForm.msg.save-successful", null)) ;
        event.getRequestContext().addUIComponentToUpdateByAjax(uiApp.getUIPopupMessages()) ; 
      } catch (Exception e) {
        e.printStackTrace();
      }

    }

  }

//  static  public class CloseActionListener extends EventListener<UIConfigForm>{
//
//    public void execute(Event<UIConfigForm> event) throws Exception {
//      try {
//        UIConfigForm uiForm = event.getSource() ;
//        UIChatBarPortlet uiChatBar =  uiForm.getAncestorOfType(UIChatBarPortlet.class );
//        uiChatBar.setTemplate(UIChatBarPortlet.VIEWMODE_TEMP);
//        uiForm.reset();
//        PortletRequestContext pcontext = (PortletRequestContext)event.getRequestContext() ;
//        pcontext.setApplicationMode(PortletMode.VIEW);
//        event.getRequestContext().addUIComponentToUpdateByAjax(uiChatBar) ;
//      } catch (Exception e) {
//        e.printStackTrace() ;
//      }
//
//    }

//  }


}
