/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

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
   template = "app:/templates/forum/webui/UIForumPortlet.gtmpl"
)
public class UIForumPortlet extends UIPortletApplication {
  public UIForumPortlet() throws Exception {
    addChild(UIBannerContainer.class, null, null) ;
    addChild(UIBreadcumbs.class, null, null) ;
    //addChild(UICategoryContainer.class, null, null) ;
    addChild(UIForumContainer.class, null, null) ;
    //addChild(UIPostPreview.class, null, null) ;
  }
}