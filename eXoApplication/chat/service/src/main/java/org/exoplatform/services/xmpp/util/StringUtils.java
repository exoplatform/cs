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

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;


/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class StringUtils {

  public static String encodeToHex(String string) {
    return new String(HexBin.encode(string.getBytes()));
  }

  public static String decodeFromHex(String hexstring) {
    try {
      return new String(HexBin.decode(hexstring));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public static String getUsernameFromJID(String jid) {
    if (jid.contains("@"))
      return jid.split("@")[0];
    return jid;
  }

  public static String getRealJid(String jid) {
    if (jid.contains("/"))
      return jid.split("/")[0];
    return jid;
  }

  public static String parseBareAddress(String XMPPAddress) {
    if (XMPPAddress == null) {
      return null;
    }
    int slashIndex = XMPPAddress.indexOf("/");
    if (slashIndex < 0) {
      return XMPPAddress;
    } else if (slashIndex == 0) {
      return "";
    } else {
      return XMPPAddress.substring(0, slashIndex);
    }
  }

}
