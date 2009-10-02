/*
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
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.exoplatform.services.xmpp.history;

import java.util.Date;
import java.util.List;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public interface History {

  /**
   * @param jidTo the receiver jid 
   * @param jidFrom the sender jid
   * @param isGroupChat true if group chat
   * @return the list of message
   */
  List<HistoricalMessage> getHistoricalMessages(String jidTo,
                                                       String jidFrom,
                                                       Boolean isGroupChat);

  /**
   * messages from conversation after dateFrom.
   * @param jidTo the receiver jid 
   * @param jidFrom the sender jid
   * @param isGroupChat true if group chat 
   * @param dateFrom the date
   * @return the list of message
   */
  List<HistoricalMessage> getHistoricalMessages(String jidTo,
                                                       String jidFrom,
                                                       Boolean isGroupChat,
                                                       Date dateFrom);

  /**
   * messages between dateFrom and dateTo.
   * @param jidTo the receiver jid 
   * @param jidFrom the sender jid
   * @param isGroupChat true if group chat 
   * @param dateFrom 
   * @param dateTo
   * @return
   */
  List<HistoricalMessage> getHistoricalMessages(String jidTo,
                                                       String jidFrom,
                                                       Boolean isGroupChat,
                                                       Date dateFrom,
                                                       Date dateTo);

  /**
   * @param historicalMessage
   */
  void addHistoricalMessage(HistoricalMessage historicalMessage);

  /**
   * @return list of all user that be contacts
   */
  List<Interlocutor> getInterlocutors(String participantJID);

  /**
   * Set true if message receive.
   * 
   * @param messageId
   */
  void messageReceive(String userid, String messageId);

}
