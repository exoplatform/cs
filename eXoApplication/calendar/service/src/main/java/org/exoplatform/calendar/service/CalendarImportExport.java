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
package org.exoplatform.calendar.service;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import org.exoplatform.services.jcr.ext.common.SessionProvider;


/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Jul 2, 2007  
 */
public interface CalendarImportExport {  
  
  /**
   * 
   * @param systemSession
   * @param username
   * @param icalInputStream
   * @param calendarName
   * @throws Exception
   */
  public void importCalendar(SessionProvider systemSession, String username, InputStream icalInputStream, String calendarName) throws Exception ;
  
  /**
   * 
   * @param systemSession
   * @param username
   * @param calendarIds
   * @param type
   * @return
   * @throws Exception
   */
  public OutputStream exportCalendar(SessionProvider systemSession, String username, List<String> calendarIds, String type) throws Exception ;
  
  /**
   * 
   * @param systemSession
   * @param username
   * @param calendarId
   * @param type
   * @param eventId
   * @return
   * @throws Exception
   */
  public OutputStream exportEventCalendar(SessionProvider systemSession, String username, String calendarId, String type, String eventId) throws Exception ;
  
  /**
   * 
   * @param icalInputStream
   * @return
   * @throws Exception
   */
  public List<CalendarEvent> getEventObjects(InputStream icalInputStream) throws Exception ;
}
