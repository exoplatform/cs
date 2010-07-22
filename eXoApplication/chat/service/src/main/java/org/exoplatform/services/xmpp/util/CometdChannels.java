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
public class CometdChannels {

  /**
   * 
   */
  public final static String MESSAGE       = "/eXo/Application/Chat/message";

  /**
   * 
   */
  public static final String PRESENCE      = "/eXo/Application/Chat/presence";

  /**
   * 
   */
  public static final String GROUP_CHAT    = "/eXo/Application/Chat/groupchat";

  /**
   * 
   */
  public static final String ROSTER        = "/eXo/Application/Chat/roster";

  /**
   * 
   */
  public static final String SUBSCRIPTION  = "/eXo/Application/Chat/subscription";

  /**
   * 
   */
  public static final String FILE_EXCHANGE = "/eXo/Application/Chat/FileExchange";

  /**
   * 
   */
  public static final String FULLNAME_EXCHANGE = "/eXo/Application/Chat/fullnameExchange";
}
