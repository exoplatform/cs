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
  private String   id_;

  private String   name_;

  private String   accountId_;

  private String   to_;

  private int      toCondition_;     // contain (0), doesn't contain (1), is (2) , is not (3) , starts with (4), ends with (5);

  private String   from_;

  private int      fromCondition_;   // contain, doesn't contain, is , is not, starts with, ends with

  private String   subject_;

  private int      subjectCondition_; // contain, doesn't contain, is , is not, starts with, ends with

  private String   body_;

  private int      bodyCondition_;   // contain , doesn't contain

  private String[] excludeFolders_;

  private String[] folders_;

  private String[] tags_;

  private String   viewQuery_;

  private String   accountPath_;

  private String   orderBy_;

  private boolean  isAscending_;

  private Calendar fromDate_;

  private Calendar toDate_;

  private boolean  hasAttach_;

  private boolean  hasStar_;

  private long     priority_;

  private String   text_;

  private String   applyFolder_;

  private String   applyTag_;

  private boolean  keepInbox_;

  private boolean  applyForAll_;

  private boolean  hasStructure_;
  
  private String[] returnedProperties;

  public MessageFilter(String name) {
    this.id_ = Utils.KEY_FILTER + IdGenerator.generate();
    this.name_ = name;
    this.toCondition_ = Utils.CONDITION_CONTAIN;
    this.fromCondition_ = Utils.CONDITION_CONTAIN;
    this.subjectCondition_ = Utils.CONDITION_CONTAIN;
    this.bodyCondition_ = Utils.CONDITION_CONTAIN;
    this.hasAttach_ = false;
    this.hasStar_ = false;
    this.priority_ = 0;
    isAscending_ = false;
    orderBy_ = Utils.EXO_LAST_CHECKED_TIME;
    hasStructure_ = false;
  }

  public String getId() {
    return id_;
  }

  public void setId(String id) {
    this.id_ = id;
  }

  public String getName() {
    return name_;
  }

  public void setName(String name) {
    this.name_ = name;
  }

  public String getAccountId() {
    return accountId_;
  }

  public void setAccountId(String id) {
    accountId_ = id;
  }

  public String[] getExcludeFolders() {
    return excludeFolders_;
  }

  public void setExcludeFolders(String[] folders) {
    this.excludeFolders_ = folders;
  }

  public String[] getFolder() {
    return folders_;
  }

  public void setFolder(String[] folder) {
    this.folders_ = folder;
  }

  public String[] getTag() {
    return tags_;
  }

  public void setTag(String[] tag) {
    this.tags_ = tag;
  }

  public String getFrom() {
    return from_;
  }

  public void setFrom(String from) {
    this.from_ = from;
  }

  public int getFromCondition() {
    return fromCondition_;
  }

  public void setFromCondition(int i) {
    fromCondition_ = i;
  }

  public String getTo() {
    return to_;
  }

  public void setTo(String emailTo) {
    this.to_ = emailTo;
  }

  public int getToCondition() {
    return toCondition_;
  }

  public void setToCondition(int i) {
    toCondition_ = i;
  }

  public String getSubject() {
    return subject_;
  }

  public void setSubject(String subject) {
    this.subject_ = subject;
  }

  public int getSubjectCondition() {
    return subjectCondition_;
  }

  public void setSubjectCondition(int i) {
    subjectCondition_ = i;
  }

  public String getBody() {
    return body_;
  }

  public void setBody(String body) {
    this.body_ = body;
  }

  public int getBodyCondition() {
    return bodyCondition_;
  }

  public void setBodyCondition(int i) {
    bodyCondition_ = i;
  }

  public String getViewQuery() {
    return viewQuery_;
  }

  public void setViewQuery(String query) {
    this.viewQuery_ = query;
  }

  public String getAccountPath() {
    return accountPath_;
  }

  public void setAccountPath(String path) {
    this.accountPath_ = path;
  }

  public String getOrderBy() {
    return orderBy_;
  }

  public void setOrderBy(String orderBy) {
    this.orderBy_ = orderBy;
  }

  public boolean isAscending() {
    return isAscending_;
  }

  public void setAscending(boolean b) {
    this.isAscending_ = b;
  }

  public Calendar getFromDate() {
    return fromDate_;
  }

  public void setFromDate(Calendar date) {
    this.fromDate_ = date;
  }

  public Calendar getToDate() {
    return toDate_;
  }

  public void setToDate(Calendar date) {
    this.toDate_ = date;
  }

  public boolean hasStar() {
    return this.hasStar_;
  }

  public void setHasStar(boolean b) {
    this.hasStar_ = b;
  }

  public boolean hasAttach() {
    return this.hasAttach_;
  }

  public void setHasAttach(boolean b) {
    this.hasAttach_ = b;
  }

  public long getPriority() {
    return this.priority_;
  }

  public void setPriority(long l) {
    this.priority_ = l;
  }

  public String getApplyFolder() {
    return applyFolder_;
  }

  public void setApplyFolder(String folder) {
    this.applyFolder_ = folder;
  }

  public String getApplyTag() {
    return applyTag_;
  }

  public void setApplyTag(String tag) {
    this.applyTag_ = tag;
  }

  public Boolean keepInInbox() {
    return keepInbox_;
  }

  public void setKeepInInbox(boolean keepInbox) {
    this.keepInbox_ = keepInbox;
  }

  public Boolean applyForAll() {
    return applyForAll_;
  }

  public void setApplyForAll(boolean b) {
    this.applyForAll_ = b;
  }

  public String getText() {
    return text_;
  }

  public void setText(String text) {
    this.text_ = text;
  }

  public boolean hasStructure() {
    return hasStructure_;
  }

  public void setHasStructure(boolean hasStructure) {
    this.hasStructure_ = hasStructure;
  }
  
  

  public String[] getReturnedProperties() {
    return returnedProperties;
  }

  public void setReturnedProperties(String[] returnedProperties) {
    this.returnedProperties = returnedProperties;
  }

  public String getStatement() throws Exception {
    StringBuffer queryString = new StringBuffer("/jcr:root" + accountPath_ + "//element(*,exo:message)");
    StringBuilder columnSpecifier = new StringBuilder();
    if (returnedProperties != null && returnedProperties.length > 0) {
      if (returnedProperties.length == 1) {
        columnSpecifier.append("/@" + returnedProperties[0]);
      } else {
        columnSpecifier.append("/(@" + returnedProperties[0]);
      }
      for (int i = 1; i < returnedProperties.length; i++) {
        columnSpecifier.append(" | @" + returnedProperties[i]);
      }
      if (returnedProperties.length > 1) columnSpecifier.append(")");
    }
    queryString.append(columnSpecifier);
    boolean hasConjuntion = false;
    StringBuffer stringBuffer = new StringBuffer("[");
    if (text_ != null && text_.trim().length() > 0) {
      text_ = Utils.encodeJCRTextSearch(text_);
      stringBuffer.append("jcr:contains(., '").append(text_).append("')");
      hasConjuntion = true;
    }

    if (excludeFolders_ != null && excludeFolders_.length > 0) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      for (int i = 0; i < excludeFolders_.length; i++) {
        if (i == 0)
          stringBuffer.append("@exo:folders!='" + excludeFolders_[i] + "'");
        else
          stringBuffer.append(" and @exo:folders!='" + excludeFolders_[i] + "'");
      }
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (folders_ != null && folders_.length > 0) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      for (int i = 0; i < folders_.length; i++) {
        if (i == 0)
          stringBuffer.append("@exo:folders='" + folders_[i] + "'");
        else
          stringBuffer.append(" or @exo:folders='" + folders_[i] + "'");
      }
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (tags_ != null && tags_.length > 0) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      for (int i = 0; i < tags_.length; i++) {
        if (i == 0)
          stringBuffer.append("@exo:tags='" + tags_[i] + "'");
        else
          stringBuffer.append(" or @exo:tags='" + tags_[i] + "'");
      }
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (from_ != null && from_.trim().length() > 0) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      switch (getFromCondition()) {
      case Utils.CONDITION_CONTAIN:
        stringBuffer.append(" jcr:like(@exo:from, '%" + from_ + "%')");
        break;
      case Utils.CONDITION_NOT_CONTAIN:
        stringBuffer.append(" fn:not(jcr:like(@exo:from, '%" + from_ + "%'))");
        break;
      case Utils.CONDITION_IS:
        stringBuffer.append(" @exo:from = '" + from_ + "'");
        break;
      case Utils.CONDITION_NOT_IS:
        stringBuffer.append(" @exo:from != '" + from_ + "'");
        break;
      case Utils.CONDITION_STARTS_WITH:
        stringBuffer.append(" jcr:like(@exo:from, '" + from_ + "%')");
        break;
      case Utils.CONDITION_ENDS_WITH:
        stringBuffer.append(" jcr:like(@exo:from, '%" + from_ + "')");
        break;
      }
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (to_ != null && to_.trim().length() > 0) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      switch (getToCondition()) {
      case Utils.CONDITION_CONTAIN:
        stringBuffer.append(" jcr:like(@exo:to, '%" + to_ + "%')");
        break;
      case Utils.CONDITION_NOT_CONTAIN:
        stringBuffer.append(" fn:not(jcr:like(@exo:to, '%" + to_ + "%'))");
        break;
      case Utils.CONDITION_IS:
        stringBuffer.append(" @exo:to = '" + to_ + "'");
        break;
      case Utils.CONDITION_NOT_IS:
        stringBuffer.append(" @exo:to != '" + to_ + "'");
        break;
      case Utils.CONDITION_STARTS_WITH:
        stringBuffer.append(" jcr:like(@exo:to, '" + to_ + "%')");
        break;
      case Utils.CONDITION_ENDS_WITH:
        stringBuffer.append(" jcr:like(@exo:to, '%" + to_ + "')");
        break;
      }
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    // jcr:contains(., 'JSR 170')
    if (subject_ != null && subject_.trim().length() > 0) {
      subject_ = Utils.encodeJCRTextSearch(subject_);
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      switch (getSubjectCondition()) {
      case Utils.CONDITION_CONTAIN:
        stringBuffer.append(" jcr:contains(@exo:subject, '" + subject_ + "')");
        break;
      case Utils.CONDITION_NOT_CONTAIN:
        stringBuffer.append(" fn:not(jcr:contains(@exo:subject, '" + subject_ + "'))");
        break;
      case Utils.CONDITION_IS:
        stringBuffer.append(" @exo:subject = '" + subject_ + "'");
        break;
      case Utils.CONDITION_NOT_IS:
        stringBuffer.append(" @exo:subject != '" + subject_ + "'");
        break;
      case Utils.CONDITION_STARTS_WITH:
        stringBuffer.append(" jcr:like(@exo:subject, '" + subject_ + "%')");
        break;
      case Utils.CONDITION_ENDS_WITH:
        stringBuffer.append(" jcr:like(@exo:subject, '%" + subject_ + "')");
        break;
      }
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (body_ != null && body_.trim().length() > 0) {
      body_ = Utils.encodeJCRTextSearch(body_);
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      switch (getBodyCondition()) {
      case Utils.CONDITION_CONTAIN:
        stringBuffer.append(" jcr:contains(@exo:body, '" + body_ + "')");
        break;
      case Utils.CONDITION_NOT_CONTAIN:
        stringBuffer.append(" fn:not(jcr:contains(@exo:body, '" + body_ + "'))");
        break;
      }
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (fromDate_ != null) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      stringBuffer.append(" @exo:receivedDate >= xs:dateTime('" + ISO8601.format(fromDate_) + "')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (toDate_ != null) {
      toDate_.set(Calendar.HOUR_OF_DAY, 23);
      toDate_.set(Calendar.MINUTE, 59);
      toDate_.set(Calendar.MILLISECOND, 999);
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      stringBuffer.append(" @exo:receivedDate <= xs:dateTime('" + ISO8601.format(toDate_) + "')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (priority_ > 0) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      stringBuffer.append(" @exo:priority = " + priority_ + "");
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (hasAttach_) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      stringBuffer.append(" @exo:hasAttach = 'true'");
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (hasStar_) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      stringBuffer.append(" @exo:star = 'true'");
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (viewQuery_ != null && viewQuery_.trim().length() > 0) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      stringBuffer.append(viewQuery_);
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (hasStructure_) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      stringBuffer.append(" @exo:isRoot = 'true'");
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    stringBuffer.append("]");

    if (orderBy_ != null && orderBy_.trim().length() > 0) {
      stringBuffer.append(" order by @" + orderBy_ + " ");
      // TODO CS-3734
      // if (isAscending_ && !hasStructure_) stringBuffer.append("ascending") ;
      if (isAscending_)
        stringBuffer.append("ascending");
      else
        stringBuffer.append("descending");

      if (orderBy_.equalsIgnoreCase(Utils.EXO_LAST_UPDATE_TIME)) {
        stringBuffer.append(" , @" + Utils.EXO_RECEIVEDDATE + " ");
        // if (isAscending_ && !hasStructure_) stringBuffer.append("ascending") ;
        if (isAscending_)
          stringBuffer.append("ascending");
        else
          stringBuffer.append("descending");
      }
    }

    if (hasConjuntion)
      queryString.append(stringBuffer.toString());
    return queryString.toString();
  }
}
