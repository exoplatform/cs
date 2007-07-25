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
