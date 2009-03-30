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
public class MUCConstants {

  /**
   * @author vetal
   *
   */
  public static class Affiliation {

    /**
     * 
     */
    public static final String OWNER   = "owner";

    /**
     * 
     */
    public static final String ADMIN   = "admin";

    /**
     * 
     */
    public static final String MEMBER  = "member";

    /**
     * 
     */
    public static final String OUTCAST = "outcast";

    /**
     * 
     */
    public static final String NONE    = "none";

  }

  /**
   * @author vetal
   *
   */
  public static class Role {

    /**
     * 
     */
    public static final String MODERATOR   = "moderator";

    /**
     * 
     */
    public static final String PARTICIPANT = "participant";

    /**
     * 
     */
    public static final String VISITOR     = "visitor";

    /**
     * 
     */
    public static final String NONE        = "none";

  }

  /**
   * @author vetal
   *
   */
  public static class Manage {

    /**
     * 
     */
    public static final String GRANT   = "grant";

    /**
     * 
     */
    public static final String REVOKE  = "revoke";

    /**
     * 
     */
    public static final String GRANTED = "granted";

    /**
     * 
     */
    public static final String REVOKED = "revoked";
  }

  /**
   * @author vetal
   *
   */
  public static class Action {

    /**
     * 
     */
    public static final String CREATED              = "created";

    /**
     * 
     */
    public static final String JOINED               = "joined";

    /**
     * 
     */
    public static final String LEFT                 = "left";

    /**
     * 
     */
    public static final String MESSAGE              = "message";

    /**
     * 
     */
    public static final String DESTROY              = "destroy";

    /**
     * 
     */
    public static final String INVITE               = "invite";

    /**
     * 
     */
    public static final String DECLINE              = "decline";

    /**
     * 
     */
    public static final String BANNED               = "banned";

    /**
     * 
     */
    public static final String KICKED               = "kicked";

    /**
     * 
     */
    public static final String ROLE_CHANGE          = "role-change";

    /**
     * 
     */
    public static final String AFFILIATE_CHANGE     = "affiliate-change";

    /**
     * 
     */
    public static final String NICKNAME_CHANGE      = "nickname-change";

    /**
     * 
     */
    public static final String SUBJECT_CHANGE       = "subject-change";

    /**
     * 
     */
    public static final String PRESENCE_CHANGE      = "presence-change";

    /**
     * 
     */
    public static final String YOU_BANNED           = "you-banned";

    /**
     * 
     */
    public static final String YOU_KICKED           = "you-kicked";

    /**
     * 
     */
    public static final String YOU_ROLE_CHANGE      = "you-role-change";

    /**
     * 
     */
    public static final String YOU_AFFILIATE_CHANGE = "you-affiliate-change";

    /**
     * 
     */
    public static final String ITEM_CHANGE          = "item-change";

  }

}
