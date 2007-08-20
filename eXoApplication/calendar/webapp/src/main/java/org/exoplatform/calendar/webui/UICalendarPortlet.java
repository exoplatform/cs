/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import org.exoplatform.calendar.webui.popup.UIPopupAction;
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
   template = "app:/templates/calendar/webui/UIPopup/UIAddNewEvent.gtmpl"
)
public class UICalendarPortlet extends UIPortletApplication {
  public UICalendarPortlet() throws Exception {
    //addChild(UIBannerContainer.class, null, null) ;
    //addChild(UIActionBar.class, null, null) ;
    //addChild(UIWorkingContainer.class, null, null) ;
    addChild(UIPopupAction.class, null, null) ;
  }
  
  public void cancelAction() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    UIPopupAction popupAction = getChild(UIPopupAction.class) ;
    popupAction.deActivate() ;
    context.addUIComponentToUpdateByAjax(popupAction) ;
  }
}