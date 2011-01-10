/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
 */
package org.exoplatform.calendar.service;

import java.io.InputStream;

import org.apache.commons.httpclient.Credentials;

/**
 * Created by The eXo Platform SAS
 * Author : khiem.dohoang
 *          khiem.dohoang@exoplatform.com
 * Jan 10, 2011  
 */
public interface RemoteCalendarService {
  
  /**
   * Check if the remote calendar is public access
   * @param url the url to remote calendar
   * @param type type of remote calendar access, iCalendar or CalDav
   * @return true if the remote calendar is pubilc access, false in otherwise (need authentication)
   * @throws Exception
   */
  boolean isPublicAccessRemoteUrl(String url) throws Exception ;
  
  /**
   * Check if the remote url is valid, in 2 cases of iCalendar url or CalDav url
   * @param url the remote url
   * @param type the type of remote calendar access, iCalendar or CalDav
   * @return true if url is available in case of iCalendar type, in case of CalDav, return true only if the server exists and support CalDav
   * @throws Exception
   */
  boolean isValidRemoteUrl(String url, String type) throws Exception ;
  
  /**
   * Check if the remote url is valid, in 2 cases of iCalendar url or CalDav url, with authentication
   * @param url the remote url
   * @param type the type of remote calendar, iCalendar or CalDav
   * @param username the remote username used to authenticate
   * @param password the remote password used to authenticate
   * @return true if remote url is available in case of iCalendar and CalDav access support in case of CalDav
   * @throws Exception
   */
  boolean isValidRemoteUrl(String url, String type, String remoteUser, String remotePassword) throws Exception ;
  
  /**
   * Connect to online ics file to get calendar data
   * @param icalUrl url of the ics file
   * @param remoteUser username to authenticate
   * @param remotePassword password to authenticate
   * @return iCalendar InputStream
   * @throws Exception
   */
  InputStream connectToRemoteIcs(String icalUrl, String remoteUser, String remotePassword) throws Exception ;
  
  /**
   * Connect to CalDav server to get calendar data
   * @param caldavUrl url to access calendar via CalDav
   * @param remoteUser username to authenticate
   * @param remotePassword password to authenticate
   * @return iCalendar InputStream
   * @throws Exception
   */
  InputStream connectToCalDavCalendar(String caldavUrl, String remoteUser, String remotePassword) throws Exception ;
  
  /**
   * Import iCalendar InputStream to local eXo Calendar
   * @param username owner of this local calendar
   * @param calendarId id of this calendar
   * @param icalInputStream InputStream with iCalendar format
   * @return Calendar object
   * @throws Exception
   */
  Calendar importRemoteCalendar(String username, String calendarId, InputStream icalInputStream) throws Exception ;
  
  /**
   * @param username
   * @param remoteUrl
   * @param calendarName
   * @param syncPeriod
   * @param credentials
   * @return
   * @throws Exception
   */
  Calendar importRemoteIcs(String username, String remoteUrl, String calendarName, String syncPeriod, Credentials credentials) throws Exception ;
  
  /**
   * @param username
   * @param remoteCalendarId
   * @return
   * @throws Exception
   */
  Calendar refreshRemoteCalendar(String username, String remoteCalendarId) throws Exception ;
  
  /**
   * @param username
   * @param calDavUrl
   * @param calendarName
   * @param syncPeriod
   * @param credentials
   * @return
   * @throws Exception
   */
  Calendar importCalDavCalendar(String username, String calDavUrl, String calendarName, String syncPeriod, Credentials credentials) throws Exception ;
}
