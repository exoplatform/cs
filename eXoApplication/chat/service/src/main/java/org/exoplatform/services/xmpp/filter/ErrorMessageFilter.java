/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.services.xmpp.filter;

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;

/**
 * Created by The eXo Platform SAS.
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class ErrorMessageFilter extends AndFilter {
  /**
   * Class logger.
   */
  private final Log log = ExoLogger.getLogger("cs.chat.service");
  
  public ErrorMessageFilter() {
    super();
    addFilter(new PacketTypeFilter(Message.class));
    addFilter(new PacketFilter() {
      public boolean accept(Packet packet) {
        Message message = (Message) packet;
        if (message.getType().equals(Message.Type.error))
          return true;
        return false;
      }
    });
  }
}
