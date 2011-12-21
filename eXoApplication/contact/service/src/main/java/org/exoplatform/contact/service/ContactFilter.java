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
package org.exoplatform.contact.service;

import org.exoplatform.contact.service.impl.JCRDataStorage;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Jun 23, 2007  
 */
public class ContactFilter {
  private String[] categories;

  private String[] tag;

  private String   subject;

  private String   body;

  private String   viewQuery;

  private String   accountPath;

  private String   orderBy;

  private boolean  isAscending;

  private String   text                   = null;

  private String   fullName;

  private String   firstName;

  private String   lastName;

  private String   nickName;

  private String   gender;

  private String   jobTitle;

  private String   emailAddress;

  private String   isOwner                = null;

  private String   username               = null;

  private String   type                   = null;

  private boolean  hasEmails              = false;

  private boolean  isSearchSharedContacts = false;

  private String   relate                 = " and ";

  private int      limit                  = 0;

  public ContactFilter() {
    isAscending = true;
  }

  public boolean isSearchSharedContacts() {
    return isSearchSharedContacts;
  }

  public void setSearchSharedContacts(boolean isSearchSharedContacts) {
    this.isSearchSharedContacts = isSearchSharedContacts;
    type = JCRDataStorage.SHARED;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String s) {
    username = s;
  }

  public void setType(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  public void setLimit(int limit) {
    this.limit = limit;
  }

  public int getLimit() {
    return limit;
  }

  public void searchByAnd(boolean and) {
    if (and)
      relate = " and ";
    else
      relate = " or ";
  }

  public void setText(String fullTextSearch) {
    this.text = fullTextSearch;
  }

  public String getText() {
    return text;
  }

  public String getFullName() {
    return fullName;
  }

  public void setFullName(String s) {
    fullName = s;
  }

  public String isOwner() {
    return isOwner;
  }

  public void setOwner(String s) {
    isOwner = s;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String s) {
    firstName = s;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String s) {
    lastName = s;
  }

  public String getNickName() {
    return nickName;
  }

  public void setNickName(String s) {
    nickName = s;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String s) {
    gender = s;
  }

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String s) {
    jobTitle = s;
  }

  public String getEmailAddress() {
    return emailAddress;
  }

  public void setEmailAddress(String s) {
    emailAddress = s;
  }

  public String[] getCategories() {
    return categories;
  }

  public void setCategories(String[] s) {
    this.categories = s;
  }

  public String[] getTag() {
    return tag;
  }

  public void setTag(String[] tag) {
    this.tag = tag;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }

  public String getViewQuery() {
    return viewQuery;
  }

  public void setViewQuery(String query) {
    this.viewQuery = query;
  }

  public String getAccountPath() {
    return accountPath;
  }

  public void setAccountPath(String path) {
    this.accountPath = path;
  }

  public String getOrderBy() {
    return orderBy;
  }

  public void setOrderBy(String orderBy) {
    this.orderBy = orderBy;
  }

  public boolean isAscending() {
    return isAscending;
  }

  public void setAscending(boolean b) {
    this.isAscending = b;
  }

  public void setHasEmails(boolean hasEmails) {
    this.hasEmails = hasEmails;
  }

  public String getStatement() throws Exception {
    StringBuffer queryString = new StringBuffer("/jcr:root").append((accountPath == null ? "" : accountPath)).append("//element(*,exo:contact)");
    boolean hasConjuntion = false;
    StringBuffer stringBuffer = new StringBuffer("[");

    if (hasEmails)
      stringBuffer.append("( ");
    // Declared full text query
    if (!Utils.isEmpty(text)) {
      String textUpper = text.toUpperCase();
      stringBuffer.append("(fn:upper-case(@exo:id) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:fullName) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:firstName) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:lastName) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:nickName) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:jobTitle) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:workAddress) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:workCity) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:workState_province) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:workPhone1) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:workPhone2) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:workFax) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:mobilePhone) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:webPage) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:exoId) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:googleId) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:msnId) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:aolId) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:yahooId) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:icrId) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:skypeId) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:icqId) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:homeAddress) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:homeCity) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:homeState_province) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:homePostalCode) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:homeCountry) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:homePhone1) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:homePhone2) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:homeFax) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:personalSite) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:note) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:emailAddress) = '").append(textUpper).append("' or")
                  .append(" fn:upper-case(@exo:workCountry) = '").append(textUpper).append("')");
      hasConjuntion = true;
    }

    if (!Utils.isEmpty(categories)) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      for (int i = 0; i < categories.length; i++) {
        if (i == 0)
          stringBuffer.append("@exo:categories='").append(categories[i]).append("'");
        else
          stringBuffer.append(" or @exo:categories='").append(categories[i]).append("'");
      }
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (!Utils.isEmpty(tag)) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      for (int i = 0; i < tag.length; i++) {
        if (i == 0)
          stringBuffer.append("@exo:tags='").append(tag[i]).append("'");
        else
          stringBuffer.append(" or @exo:tags='").append(tag[i]).append("'");
      }
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (!Utils.isEmpty(viewQuery)) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      stringBuffer.append(viewQuery);
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (!Utils.isEmpty(fullName)) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("jcr:like(fn:upper-case(@exo:fullName),'%").append(fullName.toUpperCase()).append("%')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (!Utils.isEmpty(firstName)) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("jcr:like(fn:upper-case(@exo:firstName),'%").append(firstName.toUpperCase()).append("%')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (!Utils.isEmpty(lastName)) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("jcr:like(fn:upper-case(@exo:lastName), '%").append(lastName.toUpperCase()).append("%')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (!Utils.isEmpty(nickName)) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("jcr:like(fn:upper-case(@exo:nickName),'%").append(nickName.toUpperCase()).append("%')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (!Utils.isEmpty(gender)) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("@exo:gender='").append(gender).append("'");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (!Utils.isEmpty(jobTitle)) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("jcr:like(fn:upper-case(@exo:jobTitle), '%").append(jobTitle.toUpperCase()).append("%')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (!Utils.isEmpty(emailAddress)) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("jcr:like(fn:upper-case(@exo:emailAddress), '%").append(emailAddress.toUpperCase()).append("%')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (!Utils.isEmpty(isOwner)) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      // if (isOwner.equals("true"))
      stringBuffer.append("@exo:isOwner='").append(isOwner).append("'");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (hasEmails) {
      if (hasConjuntion)
        stringBuffer.append(") and (");
      else
        stringBuffer.append(" @exo:id) and (");
      stringBuffer.append("@exo:emailAddress");
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    stringBuffer.append("]");

    if (!Utils.isEmpty(orderBy)) {
      stringBuffer.append(" order by @exo:").append(orderBy);
      if (isAscending)
        stringBuffer.append(" ascending");
      else
        stringBuffer.append(" descending");
    }

    if (hasConjuntion)
      queryString.append(stringBuffer);
    return queryString.toString();
  }
}
