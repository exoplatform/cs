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

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.sun.mail.util.LineOutputStream;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 18, 2009  
 */
public class DeliveryStatus {
  private Log log = ExoLogger.getLogger(this.getClass());
  private static boolean      debug = false;

  /**
   * The DSN fields for the message.
   */
  protected InternetHeaders   messageDSN;

  /**
   * The DSN fields for each recipient.
   */
  protected InternetHeaders[] recipientDSN;

  /**
   * Construct a delivery status notification with no content.
   */
  public DeliveryStatus() throws MessagingException {
    messageDSN = new InternetHeaders();
    recipientDSN = new InternetHeaders[0];
  }

  /**
   * Construct a delivery status notification by parsing the
   * supplied input stream.
   */
  @SuppressWarnings("unchecked")
  public DeliveryStatus(InputStream is) throws MessagingException, IOException {
    messageDSN = new InternetHeaders(is);
    if (debug)
      log.info("DSN: got messageDSN");
    Vector v = new Vector();
    try {
      while (is.available() > 0) {
        InternetHeaders h = new InternetHeaders(is);
        if (debug)
          log.info("DSN: got recipientDSN");
        v.addElement(h);
      }
    } catch (EOFException ex) {
      if (debug)
        log.info("DSN: got EOFException");
    }
    if (debug)
      log.info("DSN: recipientDSN size " + v.size());
    recipientDSN = new InternetHeaders[v.size()];
    v.copyInto(recipientDSN);
  }

  /**
   * Return all the per-message fields in the delivery status notification.
   * The fields are defined as:
   *
   * <pre>
   *    per-message-fields =
   *          [ original-envelope-id-field CRLF ]
   *          reporting-mta-field CRLF
   *          [ dsn-gateway-field CRLF ]
   *          [ received-from-mta-field CRLF ]
   *          [ arrival-date-field CRLF ]
   *          *( extension-field CRLF )
   * </pre>
   */
  // XXX - could parse each of these fields
  public InternetHeaders getMessageDSN() {
    return messageDSN;
  }

  /**
   * Set the per-message fields in the delivery status notification.
   */
  public void setMessageDSN(InternetHeaders messageDSN) {
    this.messageDSN = messageDSN;
  }

  /**
   * Return the number of recipients for which we have
   * per-recipient delivery status notification information.
   */
  public int getRecipientDSNCount() {
    return recipientDSN.length;
  }

  /**
   * Return the delivery status notification information for
   * the specified recipient.
   */
  public InternetHeaders getRecipientDSN(int n) {
    return recipientDSN[n];
  }

  /**
   * Add deliver status notification information for another
   * recipient.
   */
  public void addRecipientDSN(InternetHeaders h) {
    InternetHeaders[] rh = new InternetHeaders[recipientDSN.length + 1];
    System.arraycopy(recipientDSN, 0, rh, 0, recipientDSN.length);
    recipientDSN = rh;
    recipientDSN[recipientDSN.length - 1] = h;
  }

  public void writeTo(OutputStream os) throws IOException, MessagingException {
    // see if we already have a LOS
    LineOutputStream los = null;
    if (os instanceof LineOutputStream) {
      los = (LineOutputStream) os;
    } else {
      los = new LineOutputStream(os);
    }

    writeInternetHeaders(messageDSN, los);
    los.writeln();
    for (int i = 0; i < recipientDSN.length; i++) {
      writeInternetHeaders(recipientDSN[i], los);
      los.writeln();
    }
  }

  private static void writeInternetHeaders(InternetHeaders h, LineOutputStream los) throws IOException {
    Enumeration e = h.getAllHeaderLines();
    while (e.hasMoreElements())
      los.writeln((String) e.nextElement());
  }

  public String toString() {
    return "DeliveryStatus: Reporting-MTA=" + messageDSN.getHeader("Reporting-MTA", null) + ", #Recipients=" + recipientDSN.length;
  }

}
