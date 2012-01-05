/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
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
package org.exoplatform.services.xmpp.test;

import junit.framework.TestCase;

import org.exoplatform.services.xmpp.connection.impl.XMPPSessionImpl;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          viet.nguyen@exoplatform.com
 * Jun 21, 2011  
 */
public class UtilsTest extends TestCase {

  public void testEncodeUsername() {
    assertEquals("s220w748s8xn3btuajohn", XMPPSessionImpl.encodeUserName("John"));
    assertEquals("js220w748s8xn3btuaohn", XMPPSessionImpl.encodeUserName("jOhn"));
    assertEquals("jos220w748s8xn3btuahn", XMPPSessionImpl.encodeUserName("joHn"));
    assertEquals("johs220w748s8xn3btuan", XMPPSessionImpl.encodeUserName("johN"));
    assertEquals("s220w748s8xn3btuaexochatautb3nx8s847w022ss220w748s8xn3btuats220w748s8xn3btuaes220w748s8xn3btuass220w748s8xn3btuat", XMPPSessionImpl.encodeUserName("Exochat TEST"));
  }

  public void testDecodeUsername() {
    assertEquals("John", XMPPSessionImpl.decodeUsername("s220w748s8xn3btuajohn"));
    assertEquals("jOhn", XMPPSessionImpl.decodeUsername("js220w748s8xn3btuaohn"));
    assertEquals("joHn", XMPPSessionImpl.decodeUsername("jos220w748s8xn3btuahn"));
    assertEquals("johN", XMPPSessionImpl.decodeUsername("johs220w748s8xn3btuan"));
    assertEquals("Exochat TEST", XMPPSessionImpl.decodeUsername("s220w748s8xn3btuaexochatautb3nx8s847w022ss220w748s8xn3btuats220w748s8xn3btuaes220w748s8xn3btuass220w748s8xn3btuat"));

  }

}
