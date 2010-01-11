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
 */
package org.exoplatform.services.xmpp.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.exoplatform.services.xmpp.history.impl.jcr.HistoricalMessageImpl;
import org.jivesoftware.smack.packet.Message;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class HistoryUtils {

  private static final String XMPP_DELAY_DATETIME_FORMAT = "yyyyMMdd'T'HH:mm:ss";
  
  public static HistoricalMessageImpl messageToHistoricalMessage(Message message){
    HistoricalMessageImpl historicalMessage = new HistoricalMessageImpl();
    historicalMessage.setId(message.getPacketID());
    historicalMessage.setFrom(message.getFrom());
    historicalMessage.setTo(message.getTo());
    historicalMessage.setType(message.getType().name());
    historicalMessage.setBody(message.getBody());
    historicalMessage.setReceive(false);
    Date delayedDate = getDelayedDate(message);
    if(delayedDate != null)
      historicalMessage.setDateSend(new Date(delayedDate.getTime()));
    else
      historicalMessage.setDateSend(Calendar.getInstance().getTime());
    return historicalMessage;
  }
  
  private static Date getDelayedDate(Message message){
    Document document;
    Date delayedDate = null;
    DateFormat delayedFormatter = new SimpleDateFormat(XMPP_DELAY_DATETIME_FORMAT);
    try {
      String xmlns = message.getExtension("jabber:x:delay").toXML();
      document = DocumentHelper.parseText(xmlns);
      Element delayInformation = document.getRootElement();
      delayedDate = delayedFormatter.parse(delayInformation.attributeValue("stamp"));
    } catch (Exception e) {
      return null;
    }
    return delayedDate;
  }
}
