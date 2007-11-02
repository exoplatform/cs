/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class MessageFilter {
  private String name ;
  private String accountId ;
  private String[] folder ;
  private String[] tag ;
  private String subject ;
  private String body ;
  private String viewQuery ;
  private String accountPath ;
  private String orderBy;
  private boolean isAscending;
  private String emailTo;
  private String emailFrom;

  public MessageFilter(String name) {
    this.name = name ;
    isAscending = false;
  }
  
  public String getName() { return name ; }
  
  public String getAccountId() { return accountId ; }
  public void setAccountId(String id) { accountId =  id ; }
  
  public String[] getFolder() { return folder ; }
  public void setFolder(String[] folder) { this.folder = folder ; }
  
  public String[] getTag() { return tag ; }
  public void setTag(String[] tag) { this.tag = tag ; }
  
  public String getSubject() { return subject ; }
  public void setSubject(String subject) { this.subject = subject ; }
  
  public String getBody() { return body ; }
  public void setBody(String body) { this.body = body ; }
  
  public String getEmailFrom(){return emailFrom;}
  public void setEmailFrom(String emailFrom){this.emailFrom=emailFrom;}
  public String getEmailTo(){return emailTo;}
  public void setEmailTo(String emailTo){this.emailTo=emailTo;}
  
  
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
    //jcr:contains(., 'JSR 170')
    if(subject != null && subject.trim().length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append("jcr:contains(@exo:subject, '" + subject + "')") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    if(emailFrom != null && emailFrom.trim().length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append("jcr:contains(@exo:from, '" + emailFrom + "')") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;     
    }
    
    if(emailTo != null && emailTo.trim().length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append("jcr:contains(@exo:to, '" + emailTo + "')") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
      
    if(body != null && body.trim().length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append("jcr:contains(@exo:body, '" + body + "')") ;
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
