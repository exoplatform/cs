/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service;

import java.util.List;

import org.exoplatform.commons.utils.PageList;
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
  public List<Category> getCategories(String username) throws Exception;
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
  public Category getCategory(String accessUser, String categoryId) throws Exception;
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
  public Category createCategory(String username, Category category)throws Exception;
  /**
   * This method should: 
   * 1. Remove the category from the database if there is no forum in it. throw an exception if 
   *    there are one or more forum in the category
   * 2. Invalidate the cache
   * @param categoryId
   * @return
   * @throws Exception
   */
  public Category removeCategory(String username, String categoryId)throws Exception;  
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
  public Category updateCategory(String username, Category category)throws Exception;  
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
  public List<Forum> getForums(String username, String categoryId)throws Exception;
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
  public Forum getForum(String username, String forumId)throws Exception;  
  /**
   * This method should:
   * 1. Check all the mandatory fields of the forum
   * 2. Save the forum into the database
   * 3. Invalidate the cache
   * 
   * @param categoryId
   * @param forum
   * @return
   * @throws Exception
   */
  public Forum createForum(String username, String categoryId, Forum forum) throws Exception;
  /**
   * This method should:
   * 1. Check the mandatory fields
   * 2. Update the forum data in the database
   * 3. Invalidate or update the cache
   * @param newForum
   * @return
   * @throws Exception
   */
  public Forum updateForum(String username, Forum newForum)throws Exception;
  /**
   * This method should:
   * 1. Check to see if the user has the right to remove the forum. Throw an exception if the user do not
   *    have the right
   * 2. Remove the forum data from the database
   * 3. Invalidate the cache
   * 
   * @param forumId
   * @return
   * @throws Exception
   */
  public Forum removeForum(String username, String forumId)throws Exception;   
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
  public PageList getTopics(String username, String forumId) throws Exception;
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
  public Topic getTopic(String username, String topicId) throws Exception;    
  
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
  public TopicView getTopicView(String username, String topicId) throws Exception;
  /**
   * This method should:
   * 1. Check the user permission
   * 2. Check all the mandatory field of the topic object
   * 3. Save the topic data into the database
   * 4. Invalidate the TopicView if neccessary
   * 
   * @param username
   * @param forumId
   * @param topic
   * @return
   * @throws Exception
   */
  public Topic createTopic(String username, String forumId, Topic topic) throws Exception;
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
  public Topic updateTopic(String username, Topic newTopic) throws Exception;  
  /**
   * This method should:
   * 1. Check the user permission
   * 2. Remove the topic from the database, throw exception if  the topic is not existed
   * 3. Invalidate the TopicView cache
   * @param username
   * @param topicId
   * @return
   * @throws Exception
   */
  public Topic removeTopic(String username, String topicId) throws Exception;
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
  public List<Post> getPosts(String username, String topicId)throws Exception;
  /**
   * This method should:
   * 1. Check the user permission
   * 2. Load the Post data from the database
   * @param username
   * @param postId
   * @return
   * @throws Exception
   */
  public Post getPost(String username, String postId)throws Exception;
  /**
   * This method should: 
   * 1. Check the user permission
   * 2. Check the madatory field of the post
   * 3. Save the post data into the database
   * 4. Invalidate the TopicView data cache
   * @param username
   * @param topicId
   * @param post
   * @return
   * @throws Exception
   */
  public Post createPost(String username, String topicId, Post post)throws Exception;
  public Post updatePost(String postId, Post newPost)throws Exception;
  public Post removePost(String username, String postId)throws Exception;
}
