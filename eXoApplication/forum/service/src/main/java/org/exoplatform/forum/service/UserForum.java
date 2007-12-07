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
