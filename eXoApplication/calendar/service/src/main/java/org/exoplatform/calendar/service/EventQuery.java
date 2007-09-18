/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import org.exoplatform.commons.utils.ISO8601;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class EventQuery {
  private String eventType ;
  private String[] categoryIds = null ;
  private String[] calendarIds = null ;
  private java.util.Calendar fromDate  = null ;
  private java.util.Calendar toDate = null ;
  private String calendarPath ;
  private String statement ;
  
  public String getEventType() { return eventType ; }
  public void setEventType(String eventType) { this.eventType = eventType ; }
  
  public String[] getCategoryId() { return categoryIds ; }
  public void setCategoryId(String[] categoryIds) { this.categoryIds = categoryIds ; }
  
  public String[] getCalendarId() { return calendarIds ; }
  public void setCalendarId(String[] calendarIds) { this.calendarIds = calendarIds ; }
  
  public java.util.Calendar getFromDate() { return fromDate ; }
  public void setFromDate(java.util.Calendar fromDate) { this.fromDate = fromDate ; }
  
  public java.util.Calendar getToDate() { return toDate ; }
  public void setToDate(java.util.Calendar toDate) { this.toDate = toDate ; }
  
  public String getCalendarPath() { return calendarPath ; }
  public void setCalendarPath(String calendarPath) { this.calendarPath = calendarPath ; }
  
  public void setStatement(String st){ statement = st ; }
  public String getQueryStatement() throws Exception {
    StringBuffer queryString = new StringBuffer("/jcr:root" + calendarPath + "//element(*,exo:calendarEvent)") ;
    boolean hasConjuntion = false ;
    StringBuffer stringBuffer = new StringBuffer("[") ;
    //desclared category query
    if(categoryIds != null && categoryIds.length > 0) {
      stringBuffer.append("(") ;      
      for(int i = 0; i < categoryIds.length; i ++) {
        if(i ==  0) stringBuffer.append("@exo:eventCategoryId='" + categoryIds[i] +"'") ;
        else stringBuffer.append(" or @exo:eventCategoryId='" + categoryIds[i] +"'") ;
      }
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    // desclared calendar query
    if(calendarIds != null && calendarIds.length > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      for(int i = 0; i < calendarIds.length; i ++) {
        if(i == 0) stringBuffer.append("@exo:calendarId='" + calendarIds[i] +"'") ;
        else stringBuffer.append(" or @exo:calendarId='" + calendarIds[i] +"'") ;
      }
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    // desclared Date time
    if(fromDate != null && toDate != null){
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append("@exo:fromDateTime >= xs:dateTime('"+ISO8601.format(fromDate)+"') and ") ;
      stringBuffer.append("@exo:toDateTime < xs:dateTime('"+ISO8601.format(toDate)+"')") ;
      stringBuffer.append(")") ;      
    }else if(fromDate != null) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append("@exo:fromDateTime >= xs:dateTime('"+ISO8601.format(fromDate)+"')") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }else if(toDate != null) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append("@exo:toDateTime < xs:dateTime('"+ISO8601.format(toDate)+"')") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    stringBuffer.append("]") ;
    if(stringBuffer.length() > 2) queryString.append(stringBuffer.toString()) ;
    return queryString.toString() ;    
  }
}
