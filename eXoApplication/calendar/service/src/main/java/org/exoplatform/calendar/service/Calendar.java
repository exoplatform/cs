/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class Calendar {
  
  
  public static final String BLACK = "Black".intern() ;
  public static final String GREEN = "Green".intern() ;
  public static final String SILVER = "Silver".intern() ;
  public static final String GRAY = "Gray".intern() ;
  public static final String WHITE = "White".intern() ;
  public static final String RED = "Red".intern() ;
  public static final String PURPLE = "Purple".intern() ;
  public static final String FUCHSIA = "Fuchsia".intern() ;
  public static final String LIME = "Lime".intern() ;
  public static final String OLIVE = "Olive".intern() ;
  public static final String YELLOW = "Yellow".intern() ;
  public static final String NAVY = "Navy".intern() ;
  public static final String BLUE = "Blue".intern() ;
  public static final String TEAL = "Teal".intern() ;
  public static final String AQUA = "Aqua".intern() ;
  
  public static final String[] COLORS = {BLACK,GREEN,SILVER,GRAY,WHITE,RED,PURPLE,FUCHSIA,LIME,OLIVE,YELLOW,NAVY,BLUE,TEAL,AQUA} ;
  
  private String id ;
  private String name ;
  private String calendarPath ;
  private String calendarColor ;
  private String description ;
  private String timeZone ;
  private String locale ;
  private String[] viewPermission ;
  private String[] editPermission ;
  private boolean isPublic = false ;
  private String categoryId ;
  private String[] groups ;
  //private List<EventCategory> eventCategories ;
  
  public Calendar() throws Exception{
    id = "calendar" + IdGenerator.generate() ;
  }
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

  public boolean isPublic() { return isPublic ; }
  public void setPublic(boolean isPublic) { this.isPublic = isPublic ; }
  public void setTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }
  public String getTimeZone() {
    return timeZone;
  }
  public void setLocale(String locale) {
    this.locale = locale;
  }
  public String getLocale() {
    return locale;
  }
  public void setCalendarColor(String calendarColor) {
    this.calendarColor = calendarColor;
  }
  public String getCalendarColor() {
    return calendarColor;
  }
  
  //public List<EventCategory> getEventCategories() { return eventCategories ; }
  //public void setEventCategories(List<EventCategory> evCate) { eventCategories = evCate ; }
  
}
