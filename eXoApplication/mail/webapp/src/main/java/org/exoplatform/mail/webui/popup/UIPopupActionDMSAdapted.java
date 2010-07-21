package org.exoplatform.mail.webui.popup;

import java.util.List;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIComponent;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.lifecycle.Lifecycle;

@ComponentConfig( lifecycle = Lifecycle.class )
public class UIPopupActionDMSAdapted extends UIPopupAction {

  public UIPopupActionDMSAdapted() throws Exception {
    super();
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
