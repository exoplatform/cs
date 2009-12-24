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

import org.apache.commons.codec.binary.Hex;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class CodingUtils {

  public static String encodeToHex(String string) {
    Hex hex = new Hex();
    if (string != null)
     return new String(hex.encode(string.getBytes()));
    else return string;
  }

  public static String decodeFromHex(String hexstring) {
    try {
      Hex hex = new Hex();
      return new String(hex.decode(hexstring.getBytes()));
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

}
