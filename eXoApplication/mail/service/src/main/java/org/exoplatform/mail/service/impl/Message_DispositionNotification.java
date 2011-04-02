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

import java.awt.datatransfer.DataFlavor;
import java.io.IOException;
import java.io.OutputStream;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataContentHandler;
import javax.activation.DataSource;
import javax.mail.MessagingException;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * Jun 26, 2009  
 */
public class Message_DispositionNotification implements DataContentHandler {
  ActivationDataFlavor ourDataFlavor = new ActivationDataFlavor(DispositionNotification.class, "message/disposition-notification", "Disposition Notification");

  /**
   * return the DataFlavors for this <code>DataContentHandler</code>
   * @return The DataFlavors.
   */
  public DataFlavor[] getTransferDataFlavors() {
    return new DataFlavor[] { ourDataFlavor };
  }

  /**
   * return the Transfer Data of type DataFlavor from InputStream
   * @param df The DataFlavor.
   * @param ins The InputStream corresponding to the data.
   * @return a Message object
   */
  public Object getTransferData(DataFlavor df, DataSource ds) throws IOException {
    // make sure we can handle this DataFlavor
    if (ourDataFlavor.equals(df))
      return getContent(ds);
    else
      return null;
  }

  /**
   * Return the content.
   */
  public Object getContent(DataSource ds) throws IOException {
    // create a new DispositionNotification
    try {
      /*
       * Session session; if (ds instanceof MessageAware) { javax.mail.MessageContext mc = ((MessageAware)ds).getMessageContext(); session = mc.getSession(); } else { // Hopefully a rare case. Also hopefully the application // has created a default Session that can just be returned // here. If not, the one we create here is better than // nothing, but overall not a really good answer. session =
       * Session.getDefaultInstance(new Properties(), null); } return new DispositionNotification(session, ds.getInputStream());
       */
      return new DispositionNotification(ds.getInputStream());
    } catch (MessagingException me) {
      throw new IOException("Exception creating DispositionNotification in " + "message/disposition-notification DataContentHandler: " + me.toString());
    }
  }

  /**
   */
  public void writeTo(Object obj, String mimeType, OutputStream os) throws IOException {
    // if it's a DispositionNotification, we know how to write that out
    if (obj instanceof DispositionNotification) {
      DispositionNotification dn = (DispositionNotification) obj;
      try {
        dn.writeTo(os);
      } catch (MessagingException me) {
        throw new IOException(me.toString());
      }

    } else {
      throw new IOException("unsupported object");
    }
  }

}
