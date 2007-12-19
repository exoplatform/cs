/***************************************************************************
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
 ***************************************************************************/
package org.exoplatform.forum.service;

public class UserForum {
	private String userId ;
	private String userName ;
	private String userTitle ;
	private String userGroups ;
	private String signature ;
	private long totalPost ;
	private long totalTopic ;
	private String[] readTopic = new String[] {} ;
	public UserForum() {
		// TODO Auto-generated constructor stub
	}
	
	public void setUserId(String userId) {this.userId = userId;}
	public String getUserId() {return this.userId ;}
	
	public void setUserName(String userName) {this.userName = userName;}
	public String getUserName(){return this.userName ;}
	
	public void setUserTitle(String userTitle) {this.userTitle = userTitle;}
	public String getUserTitle() {return this.userTitle ;}
	
	public void setUserGroups(String userGruops) {this.userGroups = userGruops;}
	public String getUserGoups() {return this.userGroups ;}
	
	public void setSignature(String signature) {this.signature = signature;}
	public String getSignature() {return this.signature ;}
	
	public void setTotalPost(Long totalPost) {this.totalPost = totalPost;}
	public Long getTotalPost() {return this.totalPost ;}
	
	public void setTotalTopic(Long totalTopic) {this.totalTopic = totalTopic;}
	public Long getTotalTopic() {return this.totalTopic ;}
	
	public String[] getReadTopic(){return readTopic;}
	public void setReadTopic(String[] readTopic){this.readTopic = readTopic;}
	
	
	
	
}
