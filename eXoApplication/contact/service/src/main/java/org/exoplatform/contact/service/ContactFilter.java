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
    StringBuffer queryString = new StringBuffer("/jcr:root" + (accountPath == null ? "" : accountPath) + "//element(*,exo:contact)");
    boolean hasConjuntion = false;
    StringBuffer stringBuffer = new StringBuffer("[");

    if (hasEmails)
      stringBuffer.append("( ");
    // desclared full text query
    if (text != null && text.length() > 0) {
      text = text.toUpperCase();
      // if (username != null && text.equalsIgnoreCase(username)) {
      stringBuffer.append("(fn:upper-case(@exo:id) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:fullName) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:firstName) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:lastName) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:nickName) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:jobTitle) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:workAddress) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:workCity) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:workState_province) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:workPhone1) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:workPhone2) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:workFax) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:mobilePhone) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:webPage) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:exoId) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:googleId) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:msnId) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:aolId) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:yahooId) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:icrId) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:skypeId) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:icqId) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:homeAddress) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:homeCity) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:homeState_province) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:homePostalCode) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:homeCountry) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:homePhone1) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:homePhone2) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:homeFax) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:personalSite) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:note) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:emailAddress) = '" + text + "' or")
                  .append(" fn:upper-case(@exo:workCountry) = '" + text + "')");
      hasConjuntion = true;
    }

    if (categories != null && categories.length > 0) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      for (int i = 0; i < categories.length; i++) {
        if (i == 0)
          stringBuffer.append("@exo:categories='" + categories[i] + "'");
        else
          stringBuffer.append(" or @exo:categories='" + categories[i] + "'");
      }
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (tag != null && tag.length > 0) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      for (int i = 0; i < tag.length; i++) {
        if (i == 0)
          stringBuffer.append("@exo:tags='" + tag[i] + "'");
        else
          stringBuffer.append(" or @exo:tags='" + tag[i] + "'");
      }
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (viewQuery != null && viewQuery.trim().length() > 0) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      stringBuffer.append(viewQuery);
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (fullName != null && fullName.trim().length() > 0) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("jcr:like(fn:upper-case(@exo:fullName),'%" + fullName.toUpperCase() + "%')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (firstName != null && firstName.trim().length() > 0) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("jcr:like(fn:upper-case(@exo:firstName),'%" + firstName.toUpperCase() + "%')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (lastName != null && lastName.trim().length() > 0) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("jcr:like(fn:upper-case(@exo:lastName), '%" + lastName.toUpperCase() + "%')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (nickName != null && nickName.trim().length() > 0) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("jcr:like(fn:upper-case(@exo:nickName),'%" + nickName.toUpperCase() + "%')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (gender != null && gender.trim().length() > 0) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("@exo:gender='" + gender + "'");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (jobTitle != null && jobTitle.trim().length() > 0) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("jcr:like(fn:upper-case(@exo:jobTitle), '%" + jobTitle.toUpperCase() + "%')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }
    if (emailAddress != null && emailAddress.trim().length() > 0) {
      if (hasConjuntion)
        stringBuffer.append(relate + "(");
      else
        stringBuffer.append("(");
      stringBuffer.append("jcr:like(fn:upper-case(@exo:emailAddress), '%" + emailAddress.toUpperCase() + "%')");
      stringBuffer.append(")");
      hasConjuntion = true;
    }

    if (isOwner != null && isOwner.trim().length() > 0) {
      if (hasConjuntion)
        stringBuffer.append(" and (");
      else
        stringBuffer.append("(");
      // if (isOwner.equals("true"))
      stringBuffer.append("@exo:isOwner='" + isOwner + "'");
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

    if (orderBy != null && orderBy.trim().length() > 0) {
      stringBuffer.append(" order by @exo:" + orderBy + " ");
      if (isAscending)
        stringBuffer.append("ascending");
      else
        stringBuffer.append("descending");
    }

    if (hasConjuntion)
      queryString.append(stringBuffer.toString());
    return queryString.toString();
  }
}
