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
import org.exoplatform.forum.service.Poll;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;
import org.exoplatform.services.jcr.ext.common.SessionProvider;
import org.exoplatform.services.jcr.ext.hierarchy.NodeHierarchyCreator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 */
public class ForumServiceImpl implements ForumService{
  private JCRDataStorage storage_ ;
  
  public ForumServiceImpl(NodeHierarchyCreator nodeHierarchyCreator)throws Exception {
    storage_ = new JCRDataStorage(nodeHierarchyCreator) ;
  }
  
  public void saveCategory(SessionProvider sProvider, Category category, boolean isNew) throws Exception {
    storage_.saveCategory(sProvider, category, isNew);
  }
  
  public Category getCategory(SessionProvider sProvider, String categoryId) throws Exception {
    return storage_.getCategory(sProvider, categoryId);
  }
  
  public List<Category> getCategories(SessionProvider sProvider) throws Exception {
    return storage_.getCategories(sProvider);
  }
  
  public Category removeCategory(SessionProvider sProvider, String categoryId) throws Exception {
    return storage_.removeCategory(sProvider, categoryId) ;
  }
  
  public void saveForum(SessionProvider sProvider, String categoryId, Forum forum, boolean isNew) throws Exception {
    storage_.saveForum(sProvider, categoryId, forum, isNew);
  }

	public void moveForum(SessionProvider sProvider, String forumId, String forumPath, String destCategoryPath) throws Exception {
    storage_.moveForum(sProvider, forumId, forumPath, destCategoryPath);
  }
  
  public Forum getForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
    return storage_.getForum(sProvider, categoryId, forumId);
  }
  
  public List<Forum> getForums(SessionProvider sProvider, String categoryId) throws Exception {
    return storage_.getForums(sProvider, categoryId);
  }
  
  public Forum removeForum(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
  	return storage_.removeForum(sProvider, categoryId, forumId);
  }
  
  public void saveTopic(SessionProvider sProvider, String categoryId, String forumId, Topic topic, boolean isNew) throws Exception {
	  storage_.saveTopic(sProvider, categoryId, forumId, topic, isNew);
  }
  
  public Topic getTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId, boolean viewTopic) throws Exception {
  	return storage_.getTopic(sProvider, categoryId, forumId, topicId, viewTopic);
  }
  
  public Topic getTopicByPath(SessionProvider sProvider, String topicPath) throws Exception{
    return storage_.getTopicByPath(sProvider, topicPath) ;
  }
  
  public TopicView getTopicView(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
	  return storage_.getTopicView(sProvider, categoryId, forumId, topicId);
  }
  
  public JCRPageList getPageTopic(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
	  return storage_.getPageTopic(sProvider, categoryId, forumId);
  }

  public List<Topic> getTopics(SessionProvider sProvider, String categoryId, String forumId) throws Exception {
    return storage_.getTopics(sProvider, categoryId, forumId);
  }
  
  public void moveTopic(SessionProvider sProvider, String topicId, String  topicPath, String destForumPath) throws Exception {
  	storage_.moveTopic(sProvider, topicId, topicPath, destForumPath);
  }
  
  public Topic removeTopic(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
  	return storage_.removeTopic(sProvider, categoryId, forumId, topicId);
  }

  public void savePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, Post post, boolean isNew) throws Exception {
  	storage_.savePost(sProvider, categoryId, forumId, topicId, post, isNew);
  }
  
  public Post getPost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage_.getPost(sProvider, categoryId, forumId, topicId, postId);
  }

  public JCRPageList getPosts(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
  	return storage_.getPosts(sProvider, categoryId, forumId, topicId);
  }
  
  public void movePost(SessionProvider sProvider, String postId, String postPath, String destTopicPath) throws Exception {
    storage_.movePost(sProvider, postId, postPath, destTopicPath);
  }
  
  public Post removePost(SessionProvider sProvider, String categoryId, String forumId, String topicId, String postId) throws Exception {
    return storage_.removePost(sProvider, categoryId, forumId, topicId, postId);
  }

	public Object getObjectNameByPath(SessionProvider sProvider, String path) throws Exception {
		return storage_.getObjectNameByPath(sProvider, path);
	}
  
  public List getPage(long page, JCRPageList pageList, SessionProvider sProvider) throws Exception {
    return storage_.getPage(page, pageList, sProvider) ;
  }
  
  public List<ForumLinkData> getAllLink(SessionProvider sProvider)throws Exception {
    return storage_.getAllLink(sProvider) ;
  }
  
  public String getForumHomePath(SessionProvider sProvider) throws Exception {
  	return storage_.getForumHomeNode(sProvider).getPath() ;
	}

  public Poll getPoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
    return storage_.getPoll(sProvider, categoryId, forumId, topicId) ;
  }

  public Poll removePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId) throws Exception {
    return storage_.removePoll(sProvider, categoryId, forumId, topicId);
  }

  public void savePoll(SessionProvider sProvider, String categoryId, String forumId, String topicId, Poll poll, boolean isNew, boolean isVote) throws Exception {
    storage_.savePoll(sProvider, categoryId, forumId, topicId, poll, isNew, isVote) ;
  }
}
