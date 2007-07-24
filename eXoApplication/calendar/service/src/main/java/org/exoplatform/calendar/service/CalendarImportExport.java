/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 2, 2007  
 */
public interface CalendarImportExport {  
  public void importCalendar(String username, InputStream icalInputStream) throws Exception ;
  public OutputStream exportCalendar(String username, String calendarId) throws Exception ;
}
