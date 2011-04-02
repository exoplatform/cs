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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since July 25, 2007
 */
public class DataPageList extends JCRPageList {

  private List<Contact> contactList_ = null;

  private boolean       isQuery_     = false;

  private String        value_;

  public DataPageList(List<Contact> contactList, long pageSize, String value, boolean isQuery) throws Exception {
    super(pageSize);
    contactList_ = contactList;
    Collections.sort(contactList_, new FullNameComparator());
    value_ = value;
    isQuery_ = isQuery;
    setAvailablePage(contactList_.size());
  }

  protected void populateCurrentPage(long page, String username) throws Exception {
    setAvailablePage(contactList_.size());
    // Node currentNode ;
    long pageSize = getPageSize();
    long position = 0;
    if (page == 1)
      position = 0;
    else {
      position = (page - 1) * pageSize;
      // contactList_.skip(position) ;
    }
    currentListPage_ = new ArrayList<Contact>();
    Long objPos = position;
    if (position + pageSize > contactList_.size()) {
      currentListPage_ = contactList_.subList(objPos.intValue(), contactList_.size());
    } else {
      Long objPageSize = pageSize;
      currentListPage_ = contactList_.subList(objPos.intValue(), objPos.intValue() + objPageSize.intValue());
    }
  }

  @Override
  public List<Contact> getAll() throws Exception {
    return contactList_;
  }

  public void setList(List<Contact> contacts) {
    contactList_ = contacts;
  }

  static public class FullNameComparator implements Comparator {
    public int compare(Object o1, Object o2) throws ClassCastException {
      String name1 = ((Contact) o1).getFullName();
      String name2 = ((Contact) o2).getFullName();
      if (Utils.isEmpty(name1) || (Utils.isEmpty(name2)))
        return 0;
      return name1.compareTo(name2);
    }
  }
}
