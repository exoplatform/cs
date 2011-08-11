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
package org.exoplatform.services.xmpp.history;

import java.util.Date;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public interface HistoricalMessage {

  /**
   * @param id the id to set
   */
  void setId(String id);

  /**
   * @param from the from to set
   */
  void setFrom(String from);

  /**
   * @param to the to to set
   */

  void setTo(String to);

  /**
   * @param type the type to set
   */
  void setType(String type);

  /**
   * @param body the body to set
   */
  void setBody(String body);

  /**
   * @param dateSend the dateSend to set
   */
  void setDateSend(Date dateSend);
  
  /**
   * @param repository the repository to set
   */
  void setRepository(String repository);

  /**
   * @return the id
   */
  String getId();

  /**
   * @return the from
   */
  String getFrom();

  /**
   * @return the to
   */
  String getTo();

  /**
   * @return the type
   */
  String getType();

  /**
   * @return the body
   */
  String getBody();

  /**
   * @return the dateSend
   */
  Date getDateSend();
  
  /**
   * @return the repository's name
   */
  String getRepository();

}
