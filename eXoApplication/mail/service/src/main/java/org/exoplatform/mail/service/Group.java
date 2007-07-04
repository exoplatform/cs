/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * July 2, 2007  
 * 
 */
public class Group {
  
  private String groupName_ ;
  private String[] permissions_ ;
  private String description_ ;
  
  /**
   * The groupName_ of the contact for ex: friends, ...
   * @return the groupName_ of the group
   */
  public String getGroupName()  { return groupName_ ; }
  public void   setGroupName(String s) { groupName_ = s ; }
  
  public String[] getPermissions() {return permissions_ ; }
  public void setPermissions(String[] per) { permissions_ = per ; }
  /**
   * The display description_ of the group
   * @return The group description
   */
  public String getDescription() { return description_ ; }
  public void   setDescription(String s) { description_ = s ; }
  
}
