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

package org.exoplatform.services.xmpp.filter;

import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class SubscriptionFilter extends AndFilter {

  /**
   * 
   */
  public SubscriptionFilter() {
    super();
    addFilter(new PacketTypeFilter(Presence.class));
    addFilter(new PacketFilter() {
      public boolean accept(Packet packet) {
        Presence presence = (Presence) packet;
        if (presence.getFrom().equals(presence.getTo()))
          return false;
        if (presence.getType() == Presence.Type.subscribe
            || presence.getType() == Presence.Type.unsubscribe
            || presence.getType() == Presence.Type.subscribed
            || presence.getType() == Presence.Type.unsubscribed)
          return true;
        return false;
      }
    });
  }

}
