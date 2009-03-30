/**
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */

package org.exoplatform.services.organization.rest.json;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by The eXo Platform SARL
 * Author : Volodymyr Krasnikov
 *          volodymyr.krasnikov@exoplatform.com.ua
 */

public class MembershipTypesListBean {
  private Collection<?> membershipTypes;
  public MembershipTypesListBean() {
  
  }
  public MembershipTypesListBean(Collection<?> membershipTypes) {
    this.membershipTypes = membershipTypes;
  }
  public Collection<?> getMembershipTypes() {
    return membershipTypes;
  }
  public void setMembershipTypes(Collection<?> membershipTypes) {
    this.membershipTypes = membershipTypes;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj == null)
      return false;
    if(obj == this)
      return true;
    if(this.hashCode() == obj.hashCode())
      return true;
    if(obj instanceof UserListBean){
      MembershipTypesListBean bean = (MembershipTypesListBean)obj;
      if( membershipTypes.size() == bean.membershipTypes.size() ){
        Iterator<?> it_1 = membershipTypes.iterator();
        Iterator<?> it_2 = membershipTypes.iterator();
        while(it_1.hasNext()){
          Object m_obj1 = it_1.next();
          Object m_obj2 = it_2.next();
          if(! m_obj1.equals(m_obj2))
            return false;
        }
        return true;
      }
    }
    return false;
  }
  
  private int h_ = 0;
  @Override
  public int hashCode() {
    if( h_ == 0){
      Iterator<?> i = membershipTypes.iterator();
      while(i.hasNext()){
        h_ = h_ + i.next().hashCode();
      }
    }
    return h_;
  }
  
}
