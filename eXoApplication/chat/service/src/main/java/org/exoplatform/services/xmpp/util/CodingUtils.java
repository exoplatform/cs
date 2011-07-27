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

import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;

import com.sun.org.apache.xerces.internal.impl.dv.util.HexBin;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class CodingUtils {
  private static final Log log = ExoLogger.getExoLogger(CodingUtils.class);

  public static String encodeToHex(String string) {
    if (string != null)
      return new String(HexBin.encode(string.getBytes()));
    else
      return string;
  }

  public static String decodeFromHex(String hexstring) {
    try {
      return new String(HexBin.decode(hexstring));
    } catch (Exception e) {
      if (log.isDebugEnabled()) {
        log.debug("Exception when decode the hexa string", e);
      }
      return null;
    }
  }

}
