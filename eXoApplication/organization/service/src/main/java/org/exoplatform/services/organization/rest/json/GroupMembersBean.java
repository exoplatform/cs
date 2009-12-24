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

import org.exoplatform.services.organization.Group;

/**
 * Created by The eXo Platform SARL
 * Author : Volodymyr Krasnikov
 *          volodymyr.krasnikov@exoplatform.com.ua
 */

public class GroupMembersBean {
  private Group group;
  private Collection<?>members;
  
  public GroupMembersBean() {
    super();
  }
  public GroupMembersBean(Group group, Collection<?> collection) {
    super();
    this.group = group;
    this.members = collection;
  }
  public Group getGroup() {
    return group;
  }
  public void setGroup(Group group) {
    this.group = group;
  }
  public Collection<?> getMembers() {
    return members;
  }
  public void setMembers(Collection<?> collection) {
    this.members = collection;
  }
  
  private boolean eqGroup(Group group){
    if( group == this)
      return true;
    if (group == null)
      return false;
    //DummyOrgservice specific: it uses only groupName and groupId
    boolean res_ = group.getGroupName().equals( this.group.getGroupName());
    res_ &= group.getId().equals(this.group.getId());
    
    return res_;
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj == null)
      return false;
    if(obj == this)
      return true;
    if(this.hashCode() == obj.hashCode())
      return true;
    if(obj instanceof GroupMembersBean){
      GroupMembersBean bean = (GroupMembersBean)obj;
      if( ! eqGroup( bean.group )  )
        return false;
      if( members.size() == bean.members.size() ){
        Iterator<?> it_1 = members.iterator();
        Iterator<?> it_2 = members.iterator();
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
      h_ = group.hashCode();
      Iterator<?> i = members.iterator();
      while(i.hasNext()){
        h_ = h_ + i.next().hashCode();
      }
    }
    return h_;
  }
  
}
