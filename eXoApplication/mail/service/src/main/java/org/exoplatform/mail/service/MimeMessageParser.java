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

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import javax.mail.Flags;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Mar 15, 2008  
 */
public class MimeMessageParser {
  private static final Log logger = ExoLogger.getLogger("cs.mail.service");

  public static Calendar getReceivedDate(javax.mail.Message msg) throws Exception {
    Calendar gc = GregorianCalendar.getInstance();
    Date date = msg.getReceivedDate();
    if (date != null) {
      gc.setTime(date);
    } else {
      gc.setTime(getDateHeader(msg));
    }
    return gc;
  }

  private static Date getDateHeader(javax.mail.Message msg) throws MessagingException {
    Date today = new Date();
    String[] received = msg.getHeader("received");
    if (received != null) {
      for (int i = 0; i < received.length; i++) {
        String dateStr = null;
        try {
          dateStr = getDateString(received[i]);
          if (dateStr != null) {
            Date msgDate = parseDate(dateStr);
            if (!msgDate.after(today))
              return msgDate;
          }
        } catch (ParseException ex) {
          if (logger.isDebugEnabled()) {
            logger.debug("Failed to parse the received field from message's header", ex);
          }
        }
      }
    }

    String[] dateHeader = msg.getHeader("date");
    if (dateHeader != null) {
      String dateStr = dateHeader[0];
      try {
        Date msgDate = parseDate(dateStr);
        if (!msgDate.after(today))
          return msgDate;
      } catch (ParseException ex) {
        if (logger.isDebugEnabled()) {
          logger.debug("Failed to parse the date field from message's header", ex);
        }
      }
    }

    return today;
  }

  private static String getDateString(String text) {
    String[] daysInDate = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
    int startIndex = -1;
    for (int i = 0; i < daysInDate.length; i++) {
      startIndex = text.lastIndexOf(daysInDate[i]);
      if (startIndex != -1)
        break;
    }
    if (startIndex == -1) {
      return null;
    }

    return text.substring(startIndex);
  }

  private static Date parseDate(String dateStr) throws ParseException {
    dateStr = dateStr.replaceAll("\r\n", "");
    SimpleDateFormat dateFormat;
    try {
      dateFormat = new SimpleDateFormat("EEE, d MMM yy HH:mm:ss Z", Locale.ENGLISH);
      return dateFormat.parse(dateStr);
    } catch (ParseException e) {
      try {
        dateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
        return dateFormat.parse(dateStr);
      } catch (ParseException ex) {
        try {
          dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
          return dateFormat.parse(dateStr);
        } catch (ParseException exx) {
          try {
            dateFormat = new SimpleDateFormat("EEE, d MMM yy HH:mm:ss", Locale.ENGLISH);
            return dateFormat.parse(dateStr.substring(0, dateStr.lastIndexOf(":") + 2));
          } catch (ParseException exxx) {
            logger.warn("Cannot parse date time from message: " + dateStr);
            return null;
          }
        }
      }
    }
  }

  public static long getPriority(javax.mail.Message message) throws MessagingException {
    MimeMessage msg = (MimeMessage) message;
    String xpriority = msg.getHeader("Importance", null);
    if (xpriority != null) {
      xpriority = xpriority.toLowerCase();
      if (xpriority.indexOf("high") == 0) {
        return Utils.PRIORITY_HIGH;
      } else if (xpriority.indexOf("low") == 0) {
        return Utils.PRIORITY_LOW;
      } else {
        return Utils.PRIORITY_NORMAL;
      }
    }
    // X Standard: X-Priority: (highest) 1 | 2 | 3 | 4 | 5 (lowest)
    xpriority = msg.getHeader("X-Priority", null);
    if (xpriority != null) {
      xpriority = xpriority.toLowerCase();
      if (xpriority.indexOf("1") == 0 || xpriority.indexOf("2") == 0) {
        return Utils.PRIORITY_HIGH;
      } else if (xpriority.indexOf("4") == 0 || xpriority.indexOf("5") == 0) {
        return Utils.PRIORITY_LOW;
      } else {
        return Utils.PRIORITY_NORMAL;
      }
    }
    return Utils.PRIORITY_NORMAL;
  }

  public static String getMessageId(javax.mail.Message message) throws Exception {
    String[] msgIdHeaders;
    try {
      msgIdHeaders = message.getHeader("Message-ID");
      if (msgIdHeaders != null && msgIdHeaders[0] != null)
        return msgIdHeaders[0];
    } catch (Exception e) {
      if (logger.isDebugEnabled()) {
        logger.debug("Failed to get header from message", e);
      }
    }
    return "";
  }

  public static String getMsgUID() {
    long t1 = System.currentTimeMillis();
    if (t1 < Long.MAX_VALUE)
      return String.valueOf(t1);
    else
      return String.valueOf(t1 - Long.MAX_VALUE);
  }

  /**
   * separated getMD5 method ... for a general use.
   * @param s
   * @return a MD5 string
   */
  public static String getMD5(String s) {
    try {
      MessageDigest m = MessageDigest.getInstance("MD5");
      m.update(s.getBytes(), 0, s.length());
      return "" + new BigInteger(1, m.digest()).toString(16);
    } catch (NoSuchAlgorithmException e) {
      // almost never ... but the idea is we should control all exceptions
      logger.error("MD5 is not supported !!!");
    }
    return s;
  }

  public static String getInReplyToHeader(javax.mail.Message message) throws Exception {
    String[] inReplyToHeaders = message.getHeader("In-Reply-To");
    if (inReplyToHeaders != null && inReplyToHeaders[0] != null)
      return inReplyToHeaders[0];
    return "";
  }

  public static String[] getReferencesHeader(javax.mail.Message message) throws Exception {
    String[] references = message.getHeader("References");
    return references;
  }

  public static String[] getInvitationHeader(javax.mail.Message message) throws Exception {
    String[] exoInvitationHeaders = message.getHeader("X-Exo-Invitation");
    if (exoInvitationHeaders != null)
      return exoInvitationHeaders;
    return null;
  }

  public static boolean isSeenMessage(javax.mail.Message message) throws Exception {
    Flags.Flag[] sf = message.getFlags().getSystemFlags();
    for (int i = 0; i < sf.length; i++) {
      if (sf[i] == Flags.Flag.SEEN)
        return true;
    }
    return false;
  }

  public static boolean isAnsweredMessage(javax.mail.Message message) throws Exception {
    Flags flags = message.getFlags();
    Flags.Flag[] sf = flags.getSystemFlags();
    for (int i = 0; i < sf.length; i++) {
      if (sf[i] == Flags.Flag.ANSWERED)
        return true;
    }
    return false;
  }

  public static boolean isExistHeader(javax.mail.Message message, String header) throws Exception {
    String[] headers = message.getHeader(header);
    return (headers != null);
  }

  public static boolean requestReturnReceipt(javax.mail.Message message) throws Exception {
    boolean requestReturnReceipt = false;
    // need to emprove
    if (!isSeenMessage(message) && isExistHeader(message, "Disposition-Notification-To"))
      requestReturnReceipt = true;
    return requestReturnReceipt;
  }
}
