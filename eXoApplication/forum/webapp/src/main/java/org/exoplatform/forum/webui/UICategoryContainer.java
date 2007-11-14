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
    template =  "app:/templates/forum/webui/UICategoryContainer.gtmpl"
)
public class UICategoryContainer extends UIContainer  {
  public UICategoryContainer() throws Exception {
    addChild(UIForumActionBar.class, null, null).setRendered(true);
    addChild(UICategories.class, null, null).setRendered(true) ;
    addChild(UICategory.class, null, null).setRendered(false) ;
    addChild(UICategoriesSummary.class, null, null);
  } 
  public void updateIsRender(boolean isRender) {
  	getChild(UIForumActionBar.class).setRendered(isRender);
	  getChild(UICategories.class).setRendered(isRender) ;
	  getChild(UICategory.class).setRendered(!isRender) ;
	  this.findFirstComponentOfType(UICategoryInfo.class).setRendered(isRender) ;
  }
}
