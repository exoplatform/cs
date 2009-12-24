/**
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
package org.exoplatform.services.xmpp.bean;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.services.xmpp.history.Interlocutor;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class InterlocutorListBean {

  /**
   * 
   */
  private List<Interlocutor> intelocutorList;

  /**
   * 
   */
  public InterlocutorListBean() {
    this.intelocutorList = new ArrayList<Interlocutor>();
  }

  /**
   * @param list the list
   */
  public InterlocutorListBean(List<Interlocutor> list) {
    this.intelocutorList = list;
  }

  /**
   * @return inetrlocutorList
   */
  public List<Interlocutor> getIntelocutorList() {
    return intelocutorList;
  }

  /**
   * @param intelocutorList the intelocutorList to set
   */
  public void setIntelocutorList(List<Interlocutor> intelocutorList) {
    this.intelocutorList = intelocutorList;
  }

}
