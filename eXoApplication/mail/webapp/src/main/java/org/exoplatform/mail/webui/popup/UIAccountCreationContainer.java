/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.popup;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.core.lifecycle.UIContainerLifecycle;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 16, 2007  
 */
@ComponentConfig(lifecycle = UIContainerLifecycle.class)
public class UIAccountCreationContainer extends UIContainer implements UIPopupComponent {
  public UIAccountCreationContainer() throws Exception {
    //addChild(UIAccountCreation.class, null, null) ;
    addChild(UIPopupAction.class, null, null) ;
  }
  public void activate() throws Exception {
    // TODO Auto-generated method stub
    
  }

  public void deActivate() throws Exception {
    // TODO Auto-generated method stub
    
  }

}
