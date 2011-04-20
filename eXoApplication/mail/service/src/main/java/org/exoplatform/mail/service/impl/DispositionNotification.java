/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.mail.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.sun.mail.util.LineOutputStream;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 25, 2009  
 */
public class DispositionNotification {
  private Log log = ExoLogger.getLogger(this.getClass());
  private static boolean    debug = false;

  /**
   * The disposition notification content fields.
   */
  protected InternetHeaders notifications;

  /**
   * Construct a disposition notification with no content.
   */
  public DispositionNotification() throws MessagingException {
    notifications = new InternetHeaders();
  }

  /**
   * Construct a disposition notification by parsing the
   * supplied input stream.
   */
  public DispositionNotification(InputStream is) throws MessagingException, IOException {
    notifications = new InternetHeaders(is);
    if (debug)
      log.info("MDN: got notification content");
  }

  /**
   * Return all the disposition notification fields in the
   * disposition notification.
   * The fields are defined as:
   *
   * <pre>
   *    disposition-notification-content =
   *    [ reporting-ua-field CRLF ]
   *    [ mdn-gateway-field CRLF ]
   *    [ original-recipient-field CRLF ]
   *    final-recipient-field CRLF
   *    [ original-message-id-field CRLF ]
   *    disposition-field CRLF
   *    *( failure-field CRLF )
   *    *( error-field CRLF )
   *    *( warning-field CRLF )
   *    *( extension-field CRLF )
   * </pre>
   */
  // XXX - could parse each of these fields
  public InternetHeaders getNotifications() {
    return notifications;
  }

  /**
   * Set the disposition notification fields in the
   * disposition notification.
   */
  public void setNotifications(InternetHeaders notifications) {
    this.notifications = notifications;
  }

  public void writeTo(OutputStream os) throws IOException, MessagingException {
    // see if we already have a LOS
    LineOutputStream los = null;
    if (os instanceof LineOutputStream) {
      los = (LineOutputStream) os;
    } else {
      los = new LineOutputStream(os);
    }

    writeInternetHeaders(notifications, los);
    los.writeln();
  }

  private static void writeInternetHeaders(InternetHeaders h, LineOutputStream los) throws IOException {
    Enumeration e = h.getAllHeaderLines();
    while (e.hasMoreElements())
      los.writeln((String) e.nextElement());
  }

  public String toString() {
    return "DispositionNotification: Reporting-UA=" + notifications.getHeader("Reporting-UA", null);
  }

}
