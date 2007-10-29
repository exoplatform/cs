/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service;

import java.util.List;
/**
 * Created by The eXo Platform SARL  
 */
public interface ForumService {
  /**
   * This method should: 
   * 1. Load all the forum categories from the database
   * 2. Sort the categories by the categories order
   * 3. Cache the list of the categories in the service
   * 4. Return the list of the categories
   * @return
   * @throws Exception
   */
  public List<Category> getCategories() throws Exception;
  /**
   * This method should
   * 1. If the list of the categories is not loaded and cached, call the method getCategories() 
   * 2. Search the category in the cached categories list
   * 3. Return the found category or null.
   * @param categoryId
   * @param accessUser
   * @return
   * @throws Exception
   */
  public Category getCategory(String categoryId) throws Exception;
  /**
   * This method should:
   * 1. Check all the mandatory field for the category
   * 2. Store the category into the database
   * 3. Invalidate the cache
   * 4. Return the created category
   * @param category
   * @return
   * @throws Exception
   */
  public void saveCategory(Category category, boolean isNew)throws Exception;
  /**
   * This method should:
   * 1. Check for the mandatory fields
   * 2. Update the database
   * 3. Invalidate the cache
   * 
   * @param categoryId
   * @param newCategory
   * @return
   * @throws Exception
   */
  public Category removeCategory(String categoryId)throws Exception;  
  /**
   * This method should: 
   * 1. Load all the forums
   * 2. Sort the forum by the forum order
   * 3. cache the forums
   * 4. Return the forum list
   * @param categoryId
   * @return
   * @throws Exception
   */
  public List<Forum> getForums(String categoryId)throws Exception;
  /**
   * This method should:
   * 1. Find the category id from the forum id
   * 2. Check to see if the forums of  the category is cached
   * 3. Load the forums according to the category id if the forums is not cached
   * 4. searh the forum in the cache
   * 5. Return the found forum or  null
   * @param forumId
   * @return
   * @throws Exception
   */
  public Forum getForum(String categoryId, String forumId)throws Exception;  
  /**
   * This method should:
   * 1. Check all the mandatory fields of the forum
   * 2. Save the forum into the database
   * 3. Invalidate the cache
   * 
   * @param categoryId
   * @param forum
   * @param isNew
   * @return
   * @throws Exception
   */
  public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception;
  /**
   * This method should:
   * 1. Check the mandatory fields
   * 2. Update the forum data in the database
   * 3. Invalidate or update the cache
   * @param categoryId
   * @param forumId
   * @return
   * @throws Exception
   */
  public Forum removeForum(String categoryId, String forumId)throws Exception;  
	/**
   * This method should:
   * 1. Check to see if the user has the right to remove the forum. Throw an exception if the user do not
   *    have the right
   * 2. Move the forum data from the database
   * 3. Invalidate the cache
   * @param forumId 
   * @param forumPath
   * @param destCategoryPath
   * @return
   * @throws Exception
   */
  public void moveForum(String forumId, String forumPath, String destCategoryPath)throws Exception;  
  /**
   * This method should: 
   * 1. Implement a JCRPageList in jcrext module
   * 2. Check the user access permission with the forum access permission
   * 3. Create the query and create the JCRPageList or DBPageList  object
   * 
   * @param username
   * @param forumId
   * @return
   * @throws Exception
   */
  public JCRPageList getPageTopic(String categoryId, String forumId) throws Exception;
  /**
   * This method should:
   * 
   * 1. Load the topic from the database
   * 
   * @param username
   * @param topicId
   * @return
   * @throws Exception
   */

  public List<Topic> getTopics(String categoryId, String forumId) throws Exception;
  /**
   * This method should:
   * 
   * 1. Load the topic from the database
   * 
   * @param username
   * @param topicId
   * @return
   * @throws Exception
   */
  public Topic getTopic(String categoryId, String forumId, String topicId, boolean viewTopic) throws Exception;    
  /**
   * This method should:
   * 1. Load the topic from the database
   * @param topicPath
   * @return
   * @throws Exception
   */
  public Topic getTopicByPath(String topicPath) throws Exception;
  /**
   * This method should: 
   * 1. Load the topic and the list of the post belong to the topic. Create the TopicView object and 
   *    cache the topic view
   * 2. Return the TopicView object or null
   * 
   * @param username
   * @param topicId
   * @return
   * @throws Exception
   */
  public TopicView getTopicView(String categoryId, String forumId, String topicId) throws Exception;
  /**
   * This method should:
   * 1. Check the user permission
   * 2. Check all the mandatory field of the topic object
   * 3. Save the topic data into the database
   * 4. Invalidate the TopicView if neccessary
   * @param forumId
   * @param topic
   * @param isNew
   * @param username
   * 
   * @return
   * @throws Exception
   */
  public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew) throws Exception;
  /**
   * This method should:
   * 1. Check the user permission
   * 2. check the Topic mandatory  fields
   * 3. Save the Topic data
   * 4. Invalidate the cache of TopicView
   * @param username
   * @param topicId
   * @param newTopic
   * @return
   * @throws Exception
   */
  public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception;
  /**
   * This method should:
   * 1. Check the user permission
   * 2. Move the topic from the database, throw exception if  the topic is not existed
   * 3. Invalidate the TopicView cache
   * @param topicId 
   * @param topicPath
   * @param destForumPath
   * @return
   * @throws Exception
   */
  public void moveTopic(String topicId, String  topicPath, String destForumPath) throws Exception;
  /**
   * This method should: 
   * 1. Check the user permission
   * 2. Load the posts that belong to the topic
   * 
   * The developer should consider to use the method getTopicView(String username, String topicId)
   * instead of this method
   * @param username
   * @param topicId
   * @return
   * @throws Exception
   */
  public JCRPageList getPosts(String categoryId, String forumId, String topicId)throws Exception;
  /**
   * This method should:
   * 1. Check the user permission
   * 2. Load the Page Post data from the database
   * @param username
   * @param postId
   * @return
   * @throws Exception
   */
  public Post getPost(String categoryId, String forumId, String topicId, String postId)throws Exception;
  /**
   * This method should: 
   * 1. Check the user permission
   * 2. Check the madatory field of the post
   * 3. Save the post data into the database
   * 4. Invalidate the TopicView data cache
   * @param topicId
   * @param post
   * @param isNew
   * @param username
   * @return
   * @throws Exception
   */
  public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew)throws Exception;
  public Post removePost(String categoryId, String forumId, String topicId, String postId)throws Exception;
  public void movePost(String postId, String postPath, String destTopicPath) throws Exception ;
  
  public Poll getPoll(String categoryId, String forumId, String topicId)throws Exception;
  public void savePoll(String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote)throws Exception;
  public Poll removePoll(String categoryId, String forumId, String topicId, String pollId)throws Exception;
  
  //public Object getObjectByPath(String path) throws Exception ;
  public List<ForumLinkData> getAllLink()throws Exception ;
  public List getPage(long page, JCRPageList pageList) throws Exception ;
  public String getForumHomePath() throws Exception ;
}
