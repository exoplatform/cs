/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import org.exoplatform.mail.webui.popup.UIPopupComponent;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

/**
 * Created by The eXo Platform SARL Author : pham tuan phamtuanchip@yahoo.de Oct
 * 03, 2006 9:43:23 AM
 */
@ComponentConfig( lifecycle = Lifecycle.class )
public class UIPopupAction extends UIContainer {
  public UIPopupAction() throws Exception {
    addChild(createUIComponent(UIPopupWindow.class, null, null).setRendered(false));
  }

  public void processRender(WebuiRequestContext context) throws Exception {
    context.getWriter().append("<span class=\"").append(getId()).append("\" id=\"").append(getId()).append("\">");
    renderChildren(context) ;
    context.getWriter().append("</span>");
  }

  public <T extends UIComponent> T activate(Class<T> type, int width) throws Exception {
    return activate(type, null, width, 0);
  }

  public <T extends UIComponent> T activate(Class<T> type, String configId, int width, int height)
      throws Exception {
    T comp = createUIComponent(type, configId, null);
    activate(comp, width, height);
    return comp;
  }

  public void activate(UIComponent uiComponent, int width, int height) throws Exception {
    activate(uiComponent, width, height, true);
  }

  public void activate(UIComponent uiComponent, int width, int height, boolean isResizeable)
      throws Exception {
    UIPopupWindow popup = getChild(UIPopupWindow.class);
    popup.setUIComponent(uiComponent);
    ((UIPopupComponent) uiComponent).activate();
    popup.setWindowSize(width, height);
    popup.setRendered(true);
    popup.setShow(true);
    popup.setResizable(isResizeable);
  }

  public void deActivate() throws Exception {
    UIPopupWindow popup = getChild(UIPopupWindow.class);
    if (popup.getUIComponent() != null)
      ((UIPopupComponent) popup.getUIComponent()).deActivate();
    popup.setUIComponent(null);
    popup.setRendered(false);
  }

  public void cancelPopupAction() throws Exception {
    deActivate();
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    context.addUIComponentToUpdateByAjax(this);
  }
}