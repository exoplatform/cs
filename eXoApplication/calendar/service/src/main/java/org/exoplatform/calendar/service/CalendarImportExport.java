/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 2, 2007  
 */
public interface CalendarImportExport {  
  public void importCalendar(String username, InputStream icalInputStream, String calendarName) throws Exception ;
  public OutputStream exportCalendar(String username, List<String> calendarIds, String type) throws Exception ;
}
