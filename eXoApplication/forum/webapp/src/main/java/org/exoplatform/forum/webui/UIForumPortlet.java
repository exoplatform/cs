/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;

/**
 * Author : Nhu Dinh Thuan
 *          nhudinhthuan@yahoo.com
 * May 30, 2006
 */
@ComponentConfig(
   lifecycle = UIApplicationLifecycle.class,
   template = "app:/templates/forum/webui/ThreadsInForum.jstmpl"
)
public class UIForumPortlet extends UIPortletApplication {
  public UIForumPortlet() throws Exception {
    
  }
}