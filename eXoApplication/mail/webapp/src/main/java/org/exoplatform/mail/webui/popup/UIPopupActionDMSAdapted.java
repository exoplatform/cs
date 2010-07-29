package org.exoplatform.mail.webui.popup;

import java.util.List;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.Lifecycle;
/**
 * this class is used temporary to pass a bug of DMS UI component. the bug is in here: http://jira.exoplatform.org/browse/ECMS-1013
 * @author exo
 *
 */
@ComponentConfig( lifecycle = Lifecycle.class )
public class UIPopupActionDMSAdapted extends UIContainer {

  public UIPopupActionDMSAdapted() throws Exception {
    addChild(createUIComponent(UIPopupWindow.class, null, "UIPopupWindowOneNodeSelector").setRendered(false));
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

  
  
  public void processRender(WebuiRequestContext context) throws Exception {
    context.getWriter().append("<span class=\"").append(getId()).append("\" id=\"").append(getId()).append("\">");
    List<UIComponent> list = getChildren();
    for (UIComponent child : list)
      {
        if(child instanceof UIPopupWindow) {
          if(!child.isRendered()) {
            ((UIPopupWindow)child).setUIComponent(null);
          }
        }
         child.processRender(context);
      }
    context.getWriter().append("</span>");
  }

}
