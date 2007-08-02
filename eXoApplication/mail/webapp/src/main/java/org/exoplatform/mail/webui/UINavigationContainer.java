/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui;

import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

public class UINavigationContainer extends UIContainer  {
  
  public UINavigationContainer() throws Exception {
    addChild(UISelectAccountForm.class, null, null) ;
    addChild(UIFolderContainer.class, null, null) ;
    addChild(UITags.class, null, null) ;
  }

  
}
