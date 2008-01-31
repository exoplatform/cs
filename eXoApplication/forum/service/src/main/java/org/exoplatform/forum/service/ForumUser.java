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

import java.util.Date;

public class ForumUser {
	
	public static final String ADMIN = "0" ;
	public static final String MODERATOR = "1" ;
	public static final String USER = "2" ;
	
	private String userId ;
	private String userName ;
	private String userTitle ; //Rank of user
	private String userRole = USER ; // values: 0: Admin ; 1: Moderator ; 2: User 
	private String signature ;
	private long totalPost ;
	private long totalTopic ;
	
	private boolean isBanned = false ;
	private Date banUntil ;
	private String banReason ;
	
	private int banCounter = 0 ;
	private String[] banReasonSummary ; // value: Ban reason + fromDate - toDate
	
	private Date createdDate ;
	private Date lastLoginDate ;
	private Date lastPostDate ;
	
	private boolean isDisplaySignature = true ;
	private boolean isDisplayAvatar = true ;
	
	
	private String[] moderateForums ; //store Ids of forum this user is moderator
	private String[] moderateTopics ; //store Ids of topic this user is moderator
	private String[] readTopic = new String[] {} ;
	public ForumUser() {
		// TODO Auto-generated constructor stub
	}
	
	public void setUserId(String userId) {this.userId = userId;}
	public String getUserId() {return this.userId ;}
	
	public void setUserName(String userName) {this.userName = userName;}
	public String getUserName(){return this.userName ;}
	
	public void setUserTitle(String userTitle) {this.userTitle = userTitle;}
	public String getUserTitle() {return this.userTitle ;}
	
	public void setUserRole(String userGruops) {this.userRole = userGruops;}
	public String getUserGoups() {return this.userRole ;}
	
	public void setSignature(String signature) {this.signature = signature;}
	public String getSignature() {return this.signature ;}
	
	public void setTotalPost(Long totalPost) {this.totalPost = totalPost;}
	public Long getTotalPost() {return this.totalPost ;}
	
	public void setTotalTopic(Long totalTopic) {this.totalTopic = totalTopic;}
	public Long getTotalTopic() {return this.totalTopic ;}
	
	public String[] getReadTopic(){return readTopic;}
	public void setReadTopic(String[] readTopic){this.readTopic = readTopic;}

	public void setModerateForums(String[] moderateForums) { this.moderateForums = moderateForums ;	}
	public String[] getModerateForums() { return moderateForums ;	}

	public void setModerateTopics(String[] moderateTopics) { this.moderateTopics = moderateTopics ; }
	public String[] getModerateTopics() { return moderateTopics ;	}
	
}
