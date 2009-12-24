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

import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.smack.packet.Presence;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public final class PresenceUtil {

  /**
   * 
   */
  public static final String          ONLINE         = "Available";

  /**
   * 
   */
  public static final String          FREE_TO_CHAT   = "Free to chat";

  /**
   * 
   */
  public static final String          DO_NOT_DISTURB = "Do not disturb";

  /**
   * 
   */
  public static final String          AWAY           = "Away";

  /**
   * 
   */
  public static final String          EXTEND_AWAY    = "Extend away";

  /**
   * 
   */
  private static final List<Presence> PRESENCES      = new ArrayList<Presence>();

  static {
    PRESENCES.add(new Presence(Presence.Type.available, ONLINE, 1, Presence.Mode.available));
    PRESENCES.add(new Presence(Presence.Type.available, FREE_TO_CHAT, 1, Presence.Mode.chat));
    PRESENCES.add(new Presence(Presence.Type.available, DO_NOT_DISTURB, 0, Presence.Mode.dnd));
    PRESENCES.add(new Presence(Presence.Type.available, AWAY, 0, Presence.Mode.away));
    PRESENCES.add(new Presence(Presence.Type.available, EXTEND_AWAY, 0, Presence.Mode.xa));
  }

  /**
   * Get full list of prepared Presence.
   * 
   * @return - list of prepared Presence.
   */
  public static List<Presence> getPresences() {
    return PRESENCES;
  }

  /**
   * Get prepared Presence for status.
   * 
   * @param status - status.
   * @return - Presence or null if no Presence for this status.
   */
  public static Presence getPresence(String status) {
    for (Presence presence : PRESENCES) {
      if (presence.getStatus().equalsIgnoreCase(status)) {
        return presence;
      }
    }
    return null;
  }

  /**
   * Get prepared Presence for status and set address for this Presence.
   * 
   * @param status - status.
   * @return - Presence or null if no Presence for this status.
   */
  public static Presence getPresence(String status, String address) {
    for (Presence presence : PRESENCES) {
      if (presence.getStatus().equalsIgnoreCase(status)) {
        presence.setTo(address);
        return presence;
      }
    }
    return null;
  }

  /**
   * @param mode the mode
   * @return the default status by mode
   */
  public static String getDefaultStatusMode(Presence.Mode mode) {
    for (Presence presence : PRESENCES) {
      if (mode.equals(presence.getMode())) {
        return presence.getStatus();
      }
    }
    return null;
  }

}
