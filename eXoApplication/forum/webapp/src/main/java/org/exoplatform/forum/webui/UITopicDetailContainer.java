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
    template =  "app:/templates/forum/webui/UITopicDetailContainer.gtmpl" 
)
public class UITopicDetailContainer extends UIContainer  {
  private boolean isRender = false ;
  public UITopicDetailContainer() throws Exception {
    addChild(UITopicPoll.class, null, null).setRendered(isRender) ;
    addChild(UITopicDetail.class, null, null) ;
  }
  
  public void setRederPoll(boolean isRender) throws Exception {
   this.isRender = isRender ;
  }
}
