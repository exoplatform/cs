/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui.popup;

import org.exoplatform.webui.core.UIComponent;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */
public interface UISelectComponent {
  final public static String TYPE_USER = "0".intern() ;
  final public static String TYPE_MEMBERSHIP = "1".intern() ;
  final public static String TYPE_GROUP = "2".intern() ;
  public void setComponent(UIComponent uicomponent, String[] initParams) ;
}
