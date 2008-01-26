/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.mail.service;

import java.util.Calendar;

import org.exoplatform.commons.utils.ISO8601;
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
  private boolean isAscending ;
  private Calendar fromDate;
  private Calendar toDate;
  private boolean hasAttach;
  private boolean hasStar;
  private long priority;
  
  private String applyFolder ;
  private String applyTag ;
  private Boolean keepInbox ;
  private String[] memberOfConver ;

  public MessageFilter(String name) {
    this.id = Utils.KEY_FILTER + IdGenerator.generate();
    this.name = name ;
    this.toCondition =  Utils.CONDITION_CONTAIN ;
    this.fromCondition = Utils.CONDITION_CONTAIN ;
    this.subjectCondition = Utils.CONDITION_CONTAIN ;
    this.bodyCondition = Utils.CONDITION_CONTAIN ;
    this.hasAttach = false;
    this.hasStar = false;
    this.priority = 0;
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
  
  public Calendar getFromDate() { return fromDate; }
  public void setFromDate(Calendar date) { this.fromDate = date ;}
  
  public Calendar getToDate() { return toDate; }
  public void setToDate(Calendar date) { this.toDate = date ;} 
  
  public boolean hasStar() { return this.hasStar ;}
  public void setHasStar(boolean b) { this.hasStar = b ;}
  
  public boolean hasAttach() { return this.hasAttach; }
  public void setHasAttach(boolean b) { this.hasAttach = b; }
  
  public long getPriority() { return this.priority; }
  public void setPriority(long l) { this.priority = l; }
  
  public String getApplyFolder() { return applyFolder ; }
  public void setApplyFolder(String folder) { this.applyFolder = folder ; }
  
  public String getApplyTag() { return applyTag ; }
  public void setApplyTag(String tag) { this.applyTag = tag ; }
  
  public Boolean keepInInbox() { return keepInbox ; }
  public void setKeepInInbox(boolean keepInbox) { this.keepInbox = keepInbox ; }
  
  public String[] getMemberOfConver() { return memberOfConver ; }
  public void setMemberOfConver(String[] memberOfConver) { this.memberOfConver = memberOfConver ; }
  
  
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
          stringBuffer.append(" @exo:from = '" + from + "'") ;
          break ;
        case Utils.CONDITION_NOT_IS :
          stringBuffer.append(" @exo:from != '" + from + "'") ;
          break;
        case Utils.CONDITION_STARTS_WITH :
          stringBuffer.append(" jcr:like(@exo:from, '" + from + "%')") ;
          break;
        case Utils.CONDITION_ENDS_WITH :
          stringBuffer.append(" jcr:like(@exo:from, '%" + from + "')") ;
          break;
      }
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
          stringBuffer.append(" @exo:to = '" + to + "'") ;
          break ;
        case Utils.CONDITION_NOT_IS :
          stringBuffer.append(" @exo:to != '" + to + "'") ;
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
      subject = subject.replace("&", "&amp;");
      subject = subject.replace("<", "&lt;");
      subject = subject.replace(">", "&gt;");
      subject = subject.replace("'", "&apos;");
      subject = subject.replace("\"", "&quot;");
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
          stringBuffer.append(" @exo:subject = '" + subject + "'") ;
          break ;
        case Utils.CONDITION_NOT_IS :
          stringBuffer.append(" @exo:subject != '" + subject + "'") ;
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
      
    if (body != null && body.trim().length() > 0) {
      body = body.replace("&", "&amp;");
      body = body.replace("<", "&lt;");
      body = body.replace(">", "&gt;");
      body = body.replace("'", "&apos;");
      body = body.replace("\"", "&quot;");
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
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    if (fromDate != null) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append(" @exo:receivedDate >= xs:dateTime('" + ISO8601.format(fromDate)+"')") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    if (toDate != null) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append(" @exo:receivedDate <= xs:dateTime('" + ISO8601.format(toDate)+"')") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    if (priority > 0) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append(" @exo:priority = " + priority + "") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    if (hasAttach) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append(" @exo:hasAttach = 'true'") ;
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    if (hasStar) {
      if(hasConjuntion) stringBuffer.append(" and (") ;
      else stringBuffer.append("(") ;
      stringBuffer.append(" @exo:star = 'true'") ;
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
    
    if(memberOfConver != null && memberOfConver.length > 0) {
      if (hasConjuntion) stringBuffer.append(" and (") ; 
      else stringBuffer.append("(") ;    
      for(int i = 0; i < memberOfConver.length; i ++) {
        if(i == 0) stringBuffer.append(" @exo:addresses='" + memberOfConver[i] +"'") ;
        else stringBuffer.append(" or @exo:addresses='" + memberOfConver[i] +"'") ;
      }
      stringBuffer.append(")") ;
      hasConjuntion = true ;
    }
    
    stringBuffer.append("]") ;
    
    if (orderBy != null && orderBy.trim().length() >0) {
      stringBuffer.append(" order by @" + orderBy + " ") ;
      if (isAscending) stringBuffer.append("ascending") ;
      else stringBuffer.append("descending");
    }
    
    //System.out.println(" ## Query Statement : " + stringBuffer.toString());
    if(hasConjuntion) queryString.append(stringBuffer.toString()) ;
    return queryString.toString() ;
  }
}
