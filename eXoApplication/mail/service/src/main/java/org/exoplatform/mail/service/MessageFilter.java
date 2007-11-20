/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

import org.exoplatform.services.jcr.util.IdGenerator;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 *          Nam Phung
 *          phunghainam@gmail.com
 * Jun 23, 2007  
 */
public class MessageFilter {
  private String id ;
  private String name ;
  private String accountId ;
  private String to ;
  private int toCondition; // contain (0), doesn't contain (1), is (2) , is not (3) , starts with (4), ends with (5);
  private String from ;
  private int fromCondition ; // contain, doesn't contain, is , is not, starts with, ends with
  private String subject ;
  private int subjectCondition ; // contain, doesn't contain, is , is not, starts with, ends with
  private String body ;
  private int bodyCondition ; // contain , doesn't contain
  private String[] folder ;
  private String[] tag ;
  private String viewQuery ;
  private String searchQuery ;
  private String accountPath ;
  private String orderBy;
  private boolean isAscending;

  public MessageFilter(String name) {
    this.id = Utils.KEY_FILTER + IdGenerator.generate();
    this.name = name ;
    this.toCondition =  Utils.CONDITION_CONTAIN ;
    this.fromCondition = Utils.CONDITION_CONTAIN ;
    this.subjectCondition = Utils.CONDITION_CONTAIN ;
    this.bodyCondition = Utils.CONDITION_CONTAIN ;
    isAscending = false;
    orderBy = Utils.EXO_RECEIVEDDATE;
  }
  
  public String getId() { return id ; }
  
  public void setId(String id) { this.id = id; }
  
  public String getName() { return name ; }
  
  public void setName(String name) { this.name = name; }
  
  public String getAccountId() { return accountId ; }
  public void setAccountId(String id) { accountId =  id ; }
  
  public String[] getFolder() { return folder ; }
  public void setFolder(String[] folder) { this.folder = folder ; }
  
  public String[] getTag() { return tag ; }
  public void setTag(String[] tag) { this.tag = tag ; }
  
  public String getFrom() { return from; }
  public void setFrom(String from){ this.from = from; }
  
  public int getFromCondition() { return fromCondition ; }
  public void setFromCondition(int i) { fromCondition = i ; }
  
  public String getTo() {return to;}
  public void setTo(String emailTo){ this.to = emailTo; }
  
  public int getToCondition() { return toCondition ; }
  public void setToCondition(int i) { toCondition = i ; }
  
  public String getSubject() { return subject ; }
  public void setSubject(String subject) { this.subject = subject ; }
  
  public int getSubjectCondition() { return subjectCondition ; }
  public void setSubjectCondition(int i ) { subjectCondition = i ; }
  
  public String getBody() { return body ; }
  public void setBody(String body) { this.body = body ; }
  
  public int getBodyCondition() { return bodyCondition ; }
  public void setBodyCondition(int i) { bodyCondition = i; } 
  
  public String getSearchQuery() { return searchQuery ; }
  public void setSearchQuery(String query) { this.searchQuery = query ; }
  
  public String getViewQuery() { return viewQuery ; }
  public void setViewQuery(String query) { this.viewQuery = query ; }
  
  public String getAccountPath() { return accountPath ; }
  public void setAccountPath(String path) { this.accountPath = path ; }
  
  public String getOrderBy() { return orderBy; }
  public void setOrderBy(String orderBy) { this.orderBy = orderBy; }
  
  public boolean isAscending() { return isAscending; }
  public void setAscending(boolean b) { this.isAscending = b; } 
  
  public String getStatement() throws Exception{
    StringBuffer queryString = new StringBuffer("/jcr:root" + accountPath + "//element(*,exo:message)") ;
    boolean hasConjuntion = false ;
    StringBuffer stringBuffer = new StringBuffer("[") ;
    if(folder != null && folder.length > 0) {
      stringBuffer.append("(") ;    
      for(int i = 0; i < folder.length; i ++) {
        if(i == 0) stringBuffer.append("@exo:folders='" + folder[i] +"'") ;
        else stringBuffer.append(" or @exo:folders='" + folder[i] +"'") ;
      }
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    if(tag != null && tag.length > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      for(int i = 0; i < tag.length; i ++) {
        if(i == 0) stringBuffer.append("@exo:tags='" + tag[i] +"'") ;
        else stringBuffer.append(" or @exo:tags='" + tag[i] +"'") ;
      }
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    if(from != null && from.trim().length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      switch (getFromCondition()) {
        case Utils.CONDITION_CONTAIN :
          stringBuffer.append(" jcr:contains(@exo:from, '" + from + "')") ;
          break;
        case Utils.CONDITION_NOT_CONTAIN :
          stringBuffer.append(" fn:not(jcr:contains(@exo:from, '" + from + "'))") ;
          break;
        case Utils.CONDITION_IS :
          stringBuffer.append(" @exo:from = '" + from + "')") ;
          break ;
        case Utils.CONDITION_NOT_IS :
          stringBuffer.append(" @exo:from != '" + from + "')") ;
          break;
        case Utils.CONDITION_STARTS_WITH :
          stringBuffer.append(" jcr:like(@exo:from, '" + from + "%')") ;
          break;
        case Utils.CONDITION_ENDS_WITH :
          stringBuffer.append(" jcr:like(@exo:from, '%" + from + "')") ;
          break;
      }
      stringBuffer.append("jcr:contains(@exo:from, '" + from + "')") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;     
    }
    
    if(to != null && to.trim().length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      switch (getToCondition()) {
        case Utils.CONDITION_CONTAIN :
          stringBuffer.append(" jcr:contains(@exo:to, '" + to + "')") ;
          break;
        case Utils.CONDITION_NOT_CONTAIN :
          stringBuffer.append(" fn:not(jcr:contains(@exo:to, '" + to + "'))") ;
          break;
        case Utils.CONDITION_IS :
          stringBuffer.append(" @exo:to = '" + to + "')") ;
          break ;
        case Utils.CONDITION_NOT_IS :
          stringBuffer.append(" @exo:to != '" + to + "')") ;
          break;
        case Utils.CONDITION_STARTS_WITH :
          stringBuffer.append(" jcr:like(@exo:to, '" + to + "%')") ;
          break;
        case Utils.CONDITION_ENDS_WITH :
          stringBuffer.append(" jcr:like(@exo:to, '%" + to + "')") ;
          break;
      }
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    //jcr:contains(., 'JSR 170')
    if(subject != null && subject.trim().length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      switch (getSubjectCondition()) {
        case Utils.CONDITION_CONTAIN :
          stringBuffer.append(" jcr:contains(@exo:subject, '" + subject + "')") ;
          break;
        case Utils.CONDITION_NOT_CONTAIN :
          stringBuffer.append(" fn:not(jcr:contains(@exo:subject, '" + subject + "'))") ;
          break;
        case Utils.CONDITION_IS :
          stringBuffer.append(" @exo:subject = '" + subject + "')") ;
          break ;
        case Utils.CONDITION_NOT_IS :
          stringBuffer.append(" @exo:subject != '" + subject + "')") ;
          break;
        case Utils.CONDITION_STARTS_WITH :
          stringBuffer.append(" jcr:like(@exo:subject, '" + subject + "%')") ;
          break;
        case Utils.CONDITION_ENDS_WITH :
          stringBuffer.append(" jcr:like(@exo:subject, '%" + subject + "')") ;
          break;
      }
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
      
    if(body != null && body.trim().length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      switch (getBodyCondition()) {
        case Utils.CONDITION_CONTAIN :
          stringBuffer.append(" jcr:contains(@exo:body, '" + body + "')") ;
          break;
        case Utils.CONDITION_NOT_CONTAIN :
          stringBuffer.append(" fn:not(jcr:contains(@exo:body, '" + body + "'))") ;
          break;
      }
      stringBuffer.append("jcr:contains(@exo:body, '" + body + "')") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    if(searchQuery != null && searchQuery.trim().length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append(searchQuery) ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    if(viewQuery != null && viewQuery.trim().length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append(viewQuery) ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    stringBuffer.append("]") ;
    
    if (orderBy != null && orderBy.trim().length() >0) {
      stringBuffer.append(" order by @" + orderBy + " ") ;
      if (isAscending) stringBuffer.append("ascending") ;
      else stringBuffer.append("descending");
    }
    
    System.out.println("getStatement :"+stringBuffer.toString());
    if(hasConjuntion) queryString.append(stringBuffer.toString()) ;
    return queryString.toString() ;
  }
}
