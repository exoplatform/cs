/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since July 25, 2007
 */
public class DataPageList extends JCRPageList {
  
  private List<Contact> contactList_ = null ;
  private boolean isQuery_ = false ;
  private String value_ ;
  
  public DataPageList(List<Contact> contactList, long pageSize, String value, boolean isQuery ) throws Exception {
    super(pageSize) ;
    contactList_ = contactList ;
    value_ = value ;
    isQuery_ = isQuery ;
    setAvailablePage(contactList_.size()) ;
    
  }
  
  protected void populateCurrentPage(long page, String username) throws Exception {
    setAvailablePage(contactList_.size()) ;
    //Node currentNode ;
    long pageSize = getPageSize() ;
    long position = 0 ;
    if(page == 1) position = 0;
    else {
      position = (page-1) * pageSize ;
      //contactList_.skip(position) ;
    }
    currentListPage_ = new ArrayList<Contact>() ;
    Long objPos = position ; 
    if(position + pageSize > contactList_.size()) {
      currentListPage_ = contactList_.subList(objPos.intValue(), contactList_.size()) ;
    }else {
      Long objPageSize = pageSize ; 
      currentListPage_ = contactList_.subList(objPos.intValue(), objPos.intValue() + objPageSize.intValue() ) ;
    }
    /*for(int i = 0; i < pageSize; i ++) {
      if(iter_.hasNext()){
        currentNode = iter_.nextNode() ;
        if(currentNode.isNodeType("exo:contact")) {
          currentListPage_.add(getContact(currentNode)) ;        
        }
      }else {
        break ;
      }
    }
    iter_ = null ;  */  
  }
	@Override
	public List<Contact> getAll() throws Exception { return contactList_; }
	public void setList(List<Contact> contacts) { contactList_ = contacts ; }
}
