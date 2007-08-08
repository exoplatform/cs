/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;


import org.exoplatform.mail.webui.popup.UIComposeForm;
import org.exoplatform.mail.webui.popup.UIPopupAction;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * May 30, 2006
 */
@ComponentConfig(
   lifecycle = UIApplicationLifecycle.class,
   template = "app:/templates/mail/webui/UIMailPortlet.gtmpl"
)
public class UIMailPortlet extends UIPortletApplication {
  public UIMailPortlet() throws Exception {
    addChild(UIBannerContainer.class, null, null) ;
    addChild(UIActionBar.class, null, null) ;
    addChild(UINavigationContainer.class, null, null) ;
    addChild(UIMessageArea.class, null, null) ;
    
    UIPopupWindow popupCompose = addChild(UIPopupWindow.class, null, null) ;
    popupCompose.setRendered(false);
    popupCompose.setWindowSize(400, 400);
    UIComposeForm formCompose = createUIComponent(UIComposeForm.class, null, null);
    popupCompose.setUIComponent(formCompose);

    //addChild(UIMailContainer.class, null, null) ;
    
    //addChild(UIPopupAction.class, null, null) ;
  }
  
  public void cancelAction() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
    UIPopupAction popupAction = getChild(UIPopupAction.class) ;
    popupAction.deActivate() ;
    context.addUIComponentToUpdateByAjax(popupAction) ;
  }
} 
