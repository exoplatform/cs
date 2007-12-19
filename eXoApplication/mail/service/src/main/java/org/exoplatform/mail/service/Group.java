/*
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
package org.exoplatform.mail.service;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * 
 */
public class Group {
  
  private String groupName ;
  private String[] permissions ;
  private String description ;
  
  /**
   * The groupName_ of the contact for ex: friends, ...
   * @return the groupName_ of the group
   */
  public String getGroupName()  { return groupName ; }
  public void   setGroupName(String s) { groupName = s ; }
  
  public String[] getPermissions() {return permissions ; }
  public void setPermissions(String[] per) { permissions = per ; }
  /**
   * The display description_ of the group
   * @return The group description
   */
  public String getDescription() { return description ; }
  public void   setDescription(String s) { description = s ; }
  
}
