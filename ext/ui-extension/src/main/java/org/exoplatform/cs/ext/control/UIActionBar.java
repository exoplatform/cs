/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 **/
package org.exoplatform.cs.ext.control;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIContainer;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */

@ComponentConfig(
    template =  "app:/templates/calendar/webui/UIActionBar.gtmpl"
   
)
public class UIActionBar extends UIContainer  {
  
  final public static String DAY_VIEW = "UIDayView".intern() ;
  final public static String WEEK_VIEW = "UIWeekView".intern() ;
  final public static String MONTH_VIEW = "UIMonthView".intern() ;
  final public static String YEAR_VIEW = "UIYearView".intern() ;
  final public static String LIST_VIEW = "UIListContainer".intern() ;
  final public static String SCHEDULE_VIEW = "UIScheduleView".intern() ;
  final public static String WORKING_VIEW = "UIWorkingView".intern() ;
  final public static String[] TYPES = {DAY_VIEW, WEEK_VIEW, MONTH_VIEW, YEAR_VIEW, LIST_VIEW, SCHEDULE_VIEW, WORKING_VIEW} ;
  
  final static String EXTENSION_TYPE =  "org.exoplatform.cs.ext.control.UIActionBar"; 
  final static String CATEGORYID = "categoryId".intern() ;
  private boolean isShowPane_ = true ;
  private String currentView_ = null ;
  public UIActionBar() throws Exception {}
  protected String[] getViewTypes() {return TYPES ;} 
  protected String getCurrentView() {return currentView_ ;}
  public void setCurrentView(String viewName) {currentView_ = viewName ;}

  protected boolean isShowPane() {return isShowPane_ ;}
  protected void setShowPane(boolean isShow) {isShowPane_ = isShow ;}
    
}
