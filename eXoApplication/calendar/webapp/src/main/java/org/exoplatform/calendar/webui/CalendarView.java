/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.webui;

import java.util.Calendar;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Pham
 *          tuan.pham@exoplatform.com
 * Oct 10, 2007  
 */
public interface CalendarView {
  public void refresh()throws Exception ;
  public void update() throws Exception ;
  public void applySeting() throws Exception ;
  public void setCurrentCalendar(Calendar cal) ;
}
