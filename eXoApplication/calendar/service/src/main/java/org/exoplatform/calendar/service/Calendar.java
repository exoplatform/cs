/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.util.List;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class Calendar {
  private String id ;
  private String name ;
  private String calendarPath ;
  private String description ;
  private String[] viewPermission ;
  private String[] editPermission ;
  private boolean isPrivate = true ;
  private String categoryId ;
  private String[] groups ;
  private List<EventCategory> eventCategories ;
  
  public String getId() { return id ; }
  public void setId(String id) { this.id = id ; }
  
  public String getName() { return name ; }
  public void setName(String name) { this.name = name ; }
  
  public String getCalendarPath() { return calendarPath ; }
  public void setCalendarPath(String path) { this.calendarPath = path ; }
  
  public String getDescription() { return description ; }
  public void setDescription(String description) { this.description = description ; }

  public String[] getEditPermission() { return editPermission ; }
  public void setEditPermission(String[] editPermission) { this.editPermission = editPermission ; }
  
  public String[] getViewPermission() { return viewPermission ; }
  public void setViewPermission(String[] viewPermission) { this.viewPermission = viewPermission ; }

  public String[] getGroups() { return groups ; }
  public void setGroups(String[] groups) { this.groups = groups ;}

  public String getCategoryId() { return categoryId ; }
  public void setCategoryId(String categoryId) { this.categoryId = categoryId ; }

  public boolean isPrivate() { return isPrivate ; }
  public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate ; }
  
  public List<EventCategory> getEventCategories() { return eventCategories ; }
  public void setEventCategories(List<EventCategory> evCate) { eventCategories = evCate ; }
  
}
