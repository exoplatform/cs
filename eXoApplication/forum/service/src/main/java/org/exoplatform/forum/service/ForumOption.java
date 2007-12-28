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

public class ForumOption {
	private String userName ="";
	private Double timeZone ;
	private String shortDateformat ;
	private String longDateformat ;
	private String timeFormat ;
	private long maxTopic ;
	private long maxPost ;
	private boolean isShowForumJump = false ;
	
	public ForumOption() {
		// TODO Auto-generated constructor stub
	}
	public void setUserName(String userName) { this.userName = userName ;}
	public String getUserName() { return this.userName;}

	public void setTimeZone(Double timeZone) { this.timeZone = timeZone ; }
	public double getTimeZone() {return this.timeZone ;	}
	
	public void setShortDateFormat(String shortDateformat) { this.shortDateformat = shortDateformat ;}
	public String getShortDateFormat() { return this.shortDateformat;}

	public void setLongDateFormat(String longDateformat) { this.longDateformat = longDateformat ;}
	public String getLongDateFormat() { return this.longDateformat;}

	public void setTimeFormat(String timeFormat) {this.timeFormat = timeFormat;}
	public String getTimeFormat() { return this.timeFormat ;}
	
	public void setMaxTopicInPage(long maxTopic) {this.maxTopic = maxTopic ;}
	public Long getMaxTopicInPage() {return this.maxTopic ;}
	
	public void setMaxPostInPage(long maxPost) { this.maxPost = maxPost ;}
	public Long getMaxPostInPage() {return this.maxPost ;}
	
	public void setIsShowForumJump(boolean isShowForumJump) {this.isShowForumJump = isShowForumJump;}
	public boolean getIsShowForumJump() {return this.isShowForumJump ;}
}
