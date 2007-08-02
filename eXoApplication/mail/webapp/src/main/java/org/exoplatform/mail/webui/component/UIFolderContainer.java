/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.webui.component ;

import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

public class UIFolderContainer extends UIContainer {
  public UIFolderContainer() throws Exception {
    addChild(UIDefaultFolders.class, null, null) ;
    addChild(UICustomizeFolders.class, null, null) ;
  }
}