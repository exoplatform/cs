/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.service;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class ContactFilter {
  private String[] categories ;
  private String[] tag ;
  private String subject ;
  private String body ;
  private String viewQuery ;
  private String accountPath ;
  private String orderBy;
  private boolean isAscending;
  private String text = null ;
  private String gender ;
  
  public ContactFilter() { isAscending = true ; }
  
  public void setText(String fullTextSearch) { this.text = fullTextSearch ; }
  public String getText() { return text ; }
  
  public void setGender(String s) { gender = s ; }
  public String getGender() { return gender ; }
  
  public String[] getCategories() { return categories ; }
  public void setCategories(String[] s) { this.categories = s ; }
  
  public String[] getTag() { return tag ; }
  public void setTag(String[] tag) { this.tag = tag ; }
  
  public String getSubject() { return subject ; }
  public void setSubject(String subject) { this.subject = subject ; }
  
  public String getBody() { return body ; }
  public void setBody(String body) { this.body = body ; }
  
  public String getViewQuery() { return viewQuery ; }
  public void setViewQuery(String query) { this.viewQuery = query ; }
  
  public String getAccountPath() { return accountPath ; }
  public void setAccountPath(String path) { this.accountPath = path ; }
  
  public String getOrderBy() { return orderBy; }
  public void setOrderBy(String orderBy) { this.orderBy = orderBy; }
  
  public boolean isAscending() { return isAscending; }
  public void setAscending(boolean b) { this.isAscending = b; } 
  
  public String getStatement() throws Exception {
    StringBuffer queryString = new StringBuffer("/jcr:root" + accountPath + "//element(*,exo:contact)") ;
    boolean hasConjuntion = false ;
    StringBuffer stringBuffer = new StringBuffer("[") ;
    
    //  desclared full text query
    if(text != null && text.length() > 0) {
      stringBuffer.append("jcr:contains(., '").append(text).append("')") ;
      hasConjuntion = true ;
    }
    
    if(categories != null && categories.length > 0) {      
      stringBuffer.append("(") ;    
      for(int i = 0; i < categories.length; i ++) {
        if(i == 0) stringBuffer.append("@exo:categories='" + categories[i] +"'") ;
        else stringBuffer.append(" or @exo:categories='" + categories[i] +"'") ;
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
    
    if(viewQuery != null && viewQuery.trim().length() > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append(viewQuery) ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    if (gender != null && gender.trim().length() >0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      
      stringBuffer.append("@exo:gender='" + gender + "'") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    stringBuffer.append("]") ;
    
    if (orderBy != null && orderBy.trim().length() >0) {
      stringBuffer.append(" order by @exo:" + orderBy + " ") ;
      if (isAscending) stringBuffer.append("ascending") ;
      else stringBuffer.append("descending");
    }
    
    if(hasConjuntion) queryString.append(stringBuffer.toString()) ;
    return queryString.toString() ;
  }
}
