/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.calendar.service;

import java.util.ArrayList;
import java.util.List;
/**
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since July 25, 2007
 */
public class EventPageList extends JCRPageList {
  
  private List<CalendarEvent> eventList_ = null ;
  
  public EventPageList(List<CalendarEvent> eventList, long pageSize) throws Exception{
    super(pageSize) ;
    eventList_ = eventList ;
    setAvailablePage(eventList_.size()) ;    
  }  
  protected void populateCurrentPage(long page, String username) throws Exception  {
    setAvailablePage(eventList_.size()) ;
    long pageSize = getPageSize() ;
    long position = 0 ;
    if(page == 1) position = 0;
    else {
      position = (page-1) * pageSize ;      
    }
    currentListPage_ = new ArrayList<CalendarEvent>() ;
    Long objPos = position ; 
    if(position + pageSize > eventList_.size()) {
      currentListPage_ = eventList_.subList(objPos.intValue(), eventList_.size()) ;
    }else {
      Long objPageSize = pageSize ; 
      currentListPage_ = eventList_.subList(objPos.intValue(), objPos.intValue() + objPageSize.intValue() ) ;
    }    
  }
	@Override
	public List<CalendarEvent> getAll() throws Exception { return eventList_; }

}
