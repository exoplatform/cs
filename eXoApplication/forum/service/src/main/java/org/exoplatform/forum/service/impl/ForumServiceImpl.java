/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.impl;

import java.util.List;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class ForumServiceImpl implements ForumService{
  private JCRDataStorage storage_ ;
  
  public ForumServiceImpl(RepositoryService  repositoryService, 
                          JCRRegistryService jcrRegistryService)throws Exception {
    storage_ = new JCRDataStorage(repositoryService, jcrRegistryService) ;
  }
  
  public void saveCategory(Category category, boolean isNew) throws Exception {
    storage_.saveCategory(category, isNew);
  }
  
  public Category getCategory(String categoryId) throws Exception {
    return storage_.getCategory(categoryId);
  }
  
  public List<Category> getCategories() throws Exception {
    return storage_.getCategories();
  }
  
  public Category removeCategory(String categoryId) throws Exception {
    return storage_.removeCategory(categoryId) ;
  }
  
  public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception {
    storage_.saveForum(categoryId, forum, isNew);
  }

	public void moveForum(String forumId, String forumPath, String destCategoryPath) throws Exception {
    storage_.moveForum(forumId, forumPath, destCategoryPath);
  }
  
  public Forum getForum(String categoryId, String forumId) throws Exception {
    return storage_.getForum(categoryId, forumId);
  }
  
  public List<Forum> getForums(String categoryId) throws Exception {
    return storage_.getForums(categoryId);
  }
  
  public Forum removeForum(String categoryId, String forumId) throws Exception {
  	return storage_.removeForum(categoryId, forumId);
  }
  
  public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew) throws Exception {
	  storage_.saveTopic(categoryId, forumId, topic, isNew);
  }
  
  public Topic getTopic(String categoryId, String forumId, String topicId, boolean viewTopic) throws Exception {
  	return storage_.getTopic(categoryId, forumId, topicId, viewTopic);
  }
  
  public Topic getTopicByPath(String topicPath) throws Exception{
    return storage_.getTopicByPath(topicPath) ;
  }
  
  public TopicView getTopicView(String categoryId, String forumId, String topicId) throws Exception {
	  return storage_.getTopicView(categoryId, forumId, topicId);
  }
  
  public JCRPageList getPageTopic(String categoryId, String forumId) throws Exception {
	  return storage_.getPageTopic(categoryId, forumId);
  }

  public List<Topic> getTopics(String categoryId, String forumId) throws Exception {
    return storage_.getTopics(categoryId, forumId);
  }
  
  public void moveTopic(String topicId, String  topicPath, String destForumPath) throws Exception {
  	storage_.moveTopic(topicId, topicPath, destForumPath);
  }
  
  public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception {
  	return storage_.removeTopic(categoryId, forumId, topicId);
  }

  public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew) throws Exception {
  	storage_.savePost(categoryId, forumId, topicId, post, isNew);
  }
  
  public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage_.getPost(categoryId, forumId, topicId, postId);
  }

  public JCRPageList getPosts(String categoryId, String forumId, String topicId) throws Exception {
  	return storage_.getPosts(categoryId, forumId, topicId);
  }
  
  public void movePost(String postId, String postPath, String destTopicPath) throws Exception {
    storage_.movePost(postId, postPath, destTopicPath);
  }
  
  public Post removePost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage_.removePost(categoryId, forumId, topicId, postId);
  }

//	public Object getObjectByPath(String path) throws Exception {
//		return storage_.getObjectByPath(path);
//	}
  
  public List getPage(long page, JCRPageList pageList) throws Exception {
    return storage_.getPage(page, pageList) ;
  }
  
  public List<ForumLinkData> getAllLink()throws Exception {
    return storage_.getAllLink() ;
  }
  
  public String getForumHomePath() throws Exception {
  	return storage_.getForumHomeNode().getPath() ;
	}
  
}
