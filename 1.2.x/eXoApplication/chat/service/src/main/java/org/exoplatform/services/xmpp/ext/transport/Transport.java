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

package org.exoplatform.services.xmpp.ext.transport;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public abstract class Transport {

  /**
   * 
   */
  protected String serviceName;

  /**
   * 
   */
  public Transport() {
  }

  /**
   * @return
   */
  public abstract String getName();

  /**
   * @return the service name
   */
  public String getServiceName() {
    return serviceName;
  }

  /**
   * {@inheritDoc}
   */
  public String toString() {
    return getServiceName();
  }

  /**
   * @author vetal
   *
   */
  public static class Type {
    /**
     * 
     */
    public static final String ICQ   = "icq.";

    /**
     * 
     */
    public static final String YAHOO = "yahoo.";

    /**
     * 
     */
    public static final String MSN   = "msn.";

    /**
     * 
     */
    public static final String XMPP  = "xmpp.";

    /**
     * 
     */
    public static final String AIM   = "aim.";

    /**
     * 
     */
    public static final String GTALK = "gtalk.";
  }

}
