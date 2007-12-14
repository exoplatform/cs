package org.exoplatform.forum.service;

public class ForumOption {
	private Double timeZone ;
	private String dateFormat ;
	private String timeFormat ;
	private long maxTopic ;
	private long maxPost ;
	private boolean isShowForumJump = false ;
	private String userName ;
	
	public ForumOption() {
		// TODO Auto-generated constructor stub
	}
	public void setTimeZone(Double timeZone) { this.timeZone = timeZone ; }
	public double getTimeZone() {return this.timeZone ;	}
	
	public void setDateFormat(String dateFormat) { this.dateFormat = dateFormat ;}
	public String getDateFormat() { return this.dateFormat;}

	public void setTimeFormat(String timeFormat) {this.timeFormat = timeFormat;}
	public String getTimeFormat() { return this.timeFormat ;}
	
	public void setMaxTopicInPage(long maxTopic) {this.maxTopic = maxTopic ;}
	public Long getMaxTopicInPage() {return this.maxTopic ;}
	
	public void setMaxPostInPage(long maxPost) { this.maxPost = maxPost ;}
	public Long getMaxPostInPage() {return this.maxPost ;}
	
	public void setIsShowForumJump(boolean isShowForumJump) {this.isShowForumJump = isShowForumJump;}
	public boolean getIsShowForumJump() {return this.isShowForumJump ;}
}
