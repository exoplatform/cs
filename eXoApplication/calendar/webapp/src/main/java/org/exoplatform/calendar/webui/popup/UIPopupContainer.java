/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIPopupContainer extends UIContainer implements UIPopupComponent {

  public UIPopupContainer() throws Exception {
    addChild(UIPopupAction.class, null, "UIChildPopup") ;
  }
  public void activate() throws Exception {
    // TODO Auto-generated method stub
  }

  public void deActivate() throws Exception {
  }

}
