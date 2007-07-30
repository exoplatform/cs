/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.sql.Date;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 11, 2007  
 */
public class EventQuery {
  private String eventType ;
  private String categoryId = null ;
  private Date fromDate  = null ;
  private Date toDate = null ;
  private String calendarPath ;
  
  public String getEventType() { return eventType ; }
  public void setEventType(String eventType) { this.eventType = eventType ; }
  
  public String getCategoryId() { return categoryId ; }
  public void setCategoryId(String categoryId) { this.categoryId = categoryId ; }
  
  public Date getFromDate() { return fromDate ; }
  public void setFromDate(Date fromDate) { this.fromDate = fromDate ; }
  
  public Date getToDate() { return toDate ; }
  public void setToDate(Date toDate) { this.toDate = toDate ; }
  
  public String getCalendarPath() { return calendarPath ; }
  public void setCalendarPath(String calendarPath) { this.calendarPath = calendarPath ; }
  
  public String getQueryStatement() throws Exception {
    StringBuffer queryString = new StringBuffer("/jcr:root" + calendarPath + "//element(*,exo:calendarEvent)") ;
    if( categoryId != null || fromDate != null || toDate != null) {
      queryString.append("[") ;
      boolean hasConjuntion = false ;
      if(categoryId != null) {
        queryString.append("@exo:eventCategoryId='" + categoryId +"'") ;
        hasConjuntion = true ;
      }
      if(fromDate != null) {
        if(hasConjuntion)  queryString.append(" and ") ;
        queryString.append("@exo:fromDateTime >='" + fromDate +"'") ;
        hasConjuntion = true ;
      }
      if(toDate != null) {
        if(hasConjuntion)  queryString.append(" and ") ;
        queryString.append("@exo:toDateTime <='" + toDate +"'") ;
      }
      queryString.append("]") ;
    }
    return queryString.toString() ;
  }
}
