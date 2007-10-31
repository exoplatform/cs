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
    template =  "app:/templates/forum/webui/UICategoriesSummary.gtmpl"
)
public class UICategoriesSummary extends UIContainer  {
  private boolean isRenderInfo = true ;
  public UICategoriesSummary() throws Exception {
  	addChild(UICategoryInfo.class, null, null).setRendered(isRenderInfo);
  	addChild(UIForumIconState.class, null, null);
    isRenderInfo = true ;
    getChild(UIForumIconState.class).updateInfor(true) ;
  }  
}
