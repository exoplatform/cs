/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/forum/webui/UIForumContainer.gtmpl"
)
public class UIForumContainer extends UIContainer  {
  public UIForumContainer() throws Exception {
    addChild(UIBannerContainer.class, null, null) ;
    addChild(UIBreadcumbs.class, null, null) ;
    addChild(UITopicContainer.class, null, null) ;
    addChild(UIForumInfos.class, null, null) ;
    addChild(UIForumLinks.class, null, null) ;
  }  
}
