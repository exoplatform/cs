/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import java.util.List;

import org.exoplatform.commons.exception.ExoMessageException;
/**
 * @author Tuan Nguyen (tuan08@users.sourceforge.net)
 * @since Oct 21, 2004
 * @version $Id: PageList.java,v 1.2 2004/10/25 03:36:58 tuan08 Exp $
 */
abstract public class JCRPageList {
  //final static public PageList EMPTY_LIST = new ObjectPageList(new ArrayList(), 10) ;
  
  private long pageSize_ ;
  protected long available_ = 0;
  protected long availablePage_  = 1;
  protected long currentPage_ = 1 ;
  protected List<Message> currentListPage_ ;
  
  public JCRPageList(long pageSize) {
    pageSize_ = pageSize ;
  }
  
  public long getPageSize() { return pageSize_  ; }
  public void setPageSize(long pageSize) {
    pageSize_ = pageSize ;
    setAvailablePage(available_) ;
  }
  
  public long getCurrentPage() { return currentPage_ ; }
  public long getAvailable() { return available_ ; }
  
  public long getAvailablePage() { return availablePage_ ; }
  
  public List<Message> currentPage(String username) throws Exception {
    if(currentListPage_ == null) {
      populateCurrentPage(currentPage_, username) ;
    }
    return currentListPage_  ;
  }
  
  abstract protected void populateCurrentPage(long page, String username) throws Exception   ;
  
  public List<Message> getPage(long page, String username) throws Exception   {
    checkAndSetPage(page) ;
    populateCurrentPage(page, username) ;
    return currentListPage_ ;
  }
  
  abstract public List getAll() throws Exception  ;
  
  protected void checkAndSetPage(long page) throws Exception  {
    if(page < 1 || page > availablePage_) {
      Object[] args = { Long.toString(page), Long.toString(availablePage_) } ;
      throw new ExoMessageException("PageList.page-out-of-range", args) ;
    }
    currentPage_ =  page ;
  }
  
  protected void setAvailablePage(long available) {
    available_ = available ;
    if (available == 0)  {
      availablePage_ = 1 ; 
      currentPage_ =  1 ;
    } else {
      long pages = available / pageSize_ ;
      if ( available % pageSize_ > 0) pages++ ;
      availablePage_ = pages ;
      //currentPage_ =  1 ;
    }
  }
  
 
  /*public long getFrom() { 
    return (currentPage_ - 1) * pageSize_ ; 
  }
  
  public long getTo() { 
    long to = currentPage_  * pageSize_ ; 
    if (to > available_ ) to = available_ ;
    return to ;
  }*/
}