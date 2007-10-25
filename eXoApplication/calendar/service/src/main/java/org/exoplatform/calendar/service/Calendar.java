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
  
  
  public static final String OLIVE = "Olive".intern() ;
  public static final String OLIVEDRAB = "OliveDrab".intern() ;
  public static final String ORANGERED = "OrangeRed".intern() ;
  public static final String ORCHID = "Orchid".intern() ;
  public static final String PALEGOLDENROD = "PaleGoldenRod".intern() ;
  public static final String PALEGREEN = "PaleGreen".intern() ;
  public static final String PALETURQUOISE = "PaleTurquoise".intern() ;
  public static final String PALEVIOLETRED = "PaleVioletRed".intern() ;
  public static final String PAPAYAWHIP = "PapayaWhip".intern() ;
  public static final String PEACHPUFF = "PeachPuff".intern() ;
  public static final String PERU = "Peru".intern() ;
  public static final String PINK = "Pink".intern() ;
  public static final String PLUM = "Plum".intern() ;
  public static final String POWDERBLUE = "PowderBlue".intern() ;
  public static final String PURPLE = "Purple".intern() ;
  public static final String RED = "Red".intern() ;
  public static final String ROSYBROWN = "RosyBrown".intern() ;
  public static final String ROYALBLUE = "RoyalBlue".intern() ;
  public static final String SADDLEBROWN = "SaddleBrown".intern() ;
  public static final String SALMON = "Salmon".intern() ;
  public static final String SANDYBROWN = "SandyBrown".intern() ;
  public static final String SEAGREEN = "SeaGreen".intern() ;
  public static final String SEASHELL = "SeaShell".intern() ;
  public static final String SIANNA = "Sienna".intern() ;
  public static final String SILVER = "Silver".intern() ;
  public static final String SKYBLUE = "SkyBlue".intern() ;
  public static final String THISTLE = "Thistle".intern() ;
  public static final String TOMATO = "Tomato".intern() ;
  public static final String TURQUOISE = "Turquoise".intern() ;
  public static final String VIOLET = "Violet".intern() ;
  public static final String WHEAT = "Wheat".intern() ;
  public static final String YELLOW = "Yellow".intern() ;
  public static final String YELLOWGREEN = "YellowGreen".intern() ;
  
  /*Olive   #808000    
  OliveDrab   #6B8E23    
  Orange    #FFA500    
  OrangeRed   #FF4500    
  Orchid    #DA70D6    
  PaleGoldenRod   #EEE8AA    
  PaleGreen   #98FB98    
  PaleTurquoise   #AFEEEE    
  PaleVioletRed   #D87093    
  PapayaWhip    #FFEFD5    
  PeachPuff   #FFDAB9    
  Peru    #CD853F    
  Pink    #FFC0CB    
  Plum    #DDA0DD    
  PowderBlue    #B0E0E6    
  Purple    #800080    
  Red   #FF0000    
  RosyBrown   #BC8F8F    
  RoyalBlue   #4169E1    
  SaddleBrown   #8B4513    
  Salmon    #FA8072    
  SandyBrown    #F4A460    
  SeaGreen    #2E8B57    
  SeaShell    #FFF5EE    
  Sienna    #A0522D    
  Silver    #C0C0C0    
  SkyBlue   #87CEEB
  Thistle   #D8BFD8    
  Tomato    #FF6347    
  Turquoise   #40E0D0    
  Violet    #EE82EE    
  Wheat   #F5DEB3    
  Yellow    #FFFF00    
  YellowGreen   #9ACD32*/
  
  public static final String[] COLORS = {OLIVE,OLIVEDRAB,ORANGERED,ORCHID,
    PALEGOLDENROD,PALEGREEN,PALETURQUOISE,PALEVIOLETRED,PAPAYAWHIP,PEACHPUFF,
    PERU,PINK,PLUM,POWDERBLUE,PURPLE,RED,ROSYBROWN,ROYALBLUE,SADDLEBROWN,SALMON,
    SANDYBROWN,SEAGREEN,SEASHELL,SIANNA,SILVER,SKYBLUE,THISTLE,TOMATO,TURQUOISE,
    VIOLET,WHEAT,YELLOW,YELLOWGREEN  } ;
  
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
