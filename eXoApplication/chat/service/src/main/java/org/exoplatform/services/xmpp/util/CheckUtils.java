/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
package org.exoplatform.services.xmpp.util;


/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class CheckUtils {
 

  /**
   * 
   */
  private static final String[] disableChars = {" ", "\"", "&", "'", "/", ":", "<", ">", "@"};

  // * U+0020 (" ")
  // * U+0022 (")
  // * U+0026 (&)
  // * U+0027 (')
  // * U+002F (/)
  // * U+003A (:)
  // * U+003C (<)
  // * U+003E (>)
  // * U+0040 (@)

  /**
   * @param node the jabber id
   * @return true if jabber id is valid
   */
  public static boolean isNodeValide(String node) {
    for (int i = 0; i < disableChars.length; i++) {
      if (node.contains(disableChars[i]))
        return false;
    }
    return true;
  }

  /**
   * @return not allowed character 
   */
  public static String notAllowedCharacters() {
    return arrayToString(disableChars, ",");
  }

  

  /**
   * Convert an array of strings to one string.
   * Put the 'separator' string between each element.
   * @param a the String[]
   * @param separator the separator
   * @return string
   */
  private static String arrayToString(String[] a, String separator) {
    StringBuffer result = new StringBuffer();
    if (a.length > 0) {
      result.append(a[0]);
      for (int i = 1; i < a.length; i++) {
        result.append(separator);
        result.append(a[i]);
      }
    }
    return result.toString();
  }
}
