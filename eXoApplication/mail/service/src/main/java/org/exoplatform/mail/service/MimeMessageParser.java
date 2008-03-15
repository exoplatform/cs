/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.mail.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.mail.MessagingException;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Mar 15, 2008  
 */
public class MimeMessageParser {
  
  public static Calendar getReceivedDate(javax.mail.Message msg) throws Exception {
    Calendar gc = GregorianCalendar.getInstance() ;
    if (msg.getReceivedDate() != null) {
      gc.setTime(msg.getReceivedDate()) ;
    } else {
      gc.setTime(getDateHeader(msg)) ;
    }
    return gc ;
  }
  
  
  private static Date getDateHeader(javax.mail.Message msg) throws MessagingException {
    Date today = new Date();
    String[] received = msg.getHeader("received");
    if(received != null) 
      for (int i = 0; i < received.length; i++) {
        String dateStr = null;
        try {
          dateStr = getDateString(received[i]);
          if(dateStr != null) {
            Date msgDate = parseDate(dateStr);
            if(!msgDate.after(today))
              return msgDate;
          }
        } catch(ParseException ex) { } 
      }

    String[] dateHeader = msg.getHeader("date");
    if(dateHeader != null) {
      String dateStr = dateHeader[0];
      try {
        Date msgDate = parseDate(dateStr);
        if(!msgDate.after(today)) return msgDate;
      } catch(ParseException ex) { }
    }

    return today;
  }
  
  private static String getDateString(String text) {
    String[] daysInDate = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
    int startIndex = -1;
    for (int i = 0; i < daysInDate.length; i++) {
      startIndex = text.lastIndexOf(daysInDate[i]);
      if(startIndex != -1)
        break;
    }
    if(startIndex == -1) {
      return null;
    }

    return text.substring(startIndex);
  }
       
  private static Date parseDate(String dateStr) throws ParseException {
    SimpleDateFormat dateFormat ; 
    try {
      dateFormat = new SimpleDateFormat("EEE, d MMM yy HH:mm:ss Z") ;
      return dateFormat.parse(dateStr);
    } catch(ParseException e) {
      try {
        dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z") ;
        return dateFormat.parse(dateStr);
      } catch(ParseException ex) {
        dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss Z") ;
        return dateFormat.parse(dateStr);
      }
    }
  }

  public static long getPriority(javax.mail.Message msg) throws Exception {
    String[] xPriority = msg.getHeader("X-Priority");
    String[] importance = msg.getHeader("Importance");
    
    // Get priority of message on header if it's available.
    if (xPriority != null && xPriority.length > 0) {
      for (int j = 0 ; j < xPriority.length; j++) {
        return Long.valueOf(msg.getHeader("X-Priority")[j].substring(0,1));
      }          
    }          
    if (importance != null && importance.length > 0) {
      for (int j = 0 ; j < importance.length; j++) {
        if (importance[j].equalsIgnoreCase("Low")) {
          return Utils.PRIORITY_LOW;
        } else if (importance[j].equalsIgnoreCase("high")) {
          return Utils.PRIORITY_HIGH;
        } 
      }
    } 
    return Utils.PRIORITY_NORMAL ;
  }
}
