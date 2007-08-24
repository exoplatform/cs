/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.Forum;
import org.exoplatform.forum.service.ForumLinkData;
import org.exoplatform.forum.service.ForumPageList;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Post;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.forum.service.TopicView;
import org.exoplatform.registry.JCRRegistryService;
import org.exoplatform.registry.ServiceRegistry;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen Quang
 *          hung.nguyen@exoplatform.com
 * Jul 10, 2007  
 * Edited by Vu Duy Tu
 *          tu.duy@exoplatform.com
 * July 16, 2007 
 */
public class JCRDataStorage implements DataStorage {
  private RepositoryService  repositoryService_ ; 
  private JCRRegistryService jcrRegistryService_ ;
  
  public JCRDataStorage(RepositoryService  repositoryService, 
                        JCRRegistryService jcrRegistryService)throws Exception {
    repositoryService_ = repositoryService ;
    jcrRegistryService_ = jcrRegistryService ;
  }
  
  public List<Category> getCategories() throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
    StringBuffer queryString = new StringBuffer("/jcr:root" + forumHomeNode.getPath() +"//element(*,exo:forumCategory) order by @exo:categoryOrder ascending") ;
    Query query = qm.createQuery(queryString.toString(), Query.XPATH) ;
    QueryResult result = query.execute() ;
    NodeIterator iter = result.getNodes() ;
    List<Category> categories = new ArrayList<Category>() ;
    Category cat ;
    while(iter.hasNext()) {
      Node cateNode = iter.nextNode() ;
      cat = new Category() ;
      cat = getCategory(cateNode) ;
      categories.add(cat) ;
    }
    return categories ;
  }
  
  public Category getCategory(String categoryId) throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    if(!forumHomeNode.hasNode(categoryId)) return null;
    Node cateNode = forumHomeNode.getNode(categoryId) ;
    Category cat = new Category() ;
    cat = getCategory(cateNode) ;
    return cat ;
  }

  private Category getCategory(Node cateNode) throws Exception {
    Category cat = new Category() ;
    if(cateNode.hasProperty("exo:id"))cat.setId(cateNode.getProperty("exo:id").getString()) ;
    if(cateNode.hasProperty("exo:owner"))cat.setOwner(cateNode.getProperty("exo:owner").getString()) ;
    if(cateNode.hasProperty("exo:path"))cat.setPath(cateNode.getProperty("exo:path").getString()) ;
    if(cateNode.hasProperty("exo:name"))cat.setCategoryName(cateNode.getProperty("exo:name").getString()) ;
    if(cateNode.hasProperty("exo:categoryOrder"))cat.setCategoryOrder(cateNode.getProperty("exo:categoryOrder").getLong()) ;
    if(cateNode.hasProperty("exo:createdDate"))cat.setCreatedDate(cateNode.getProperty("exo:createdDate").getDate().getTime()) ;
    if(cateNode.hasProperty("exo:description"))cat.setDescription(cateNode.getProperty("exo:description").getString()) ;
    if(cateNode.hasProperty("exo:modifiedBy"))cat.setModifiedBy(cateNode.getProperty("exo:modifiedBy").getString()) ;
    if(cateNode.hasProperty("exo:modifiedDate"))cat.setModifiedDate(cateNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
    return cat;
  }
  
  public void saveCategory(Category category, boolean isNew) throws Exception {
  	Node forumHomeNode = getForumHomeNode() ;
  	Node catNode;
  	if(isNew) {
  	  catNode = forumHomeNode.addNode(category.getId(), "exo:forumCategory") ;
    	catNode.setProperty("exo:id", category.getId()) ;
    	catNode.setProperty("exo:owner", category.getOwner()) ;
    	catNode.setProperty("exo:path", catNode.getPath()) ;
    	catNode.setProperty("exo:createdDate", GregorianCalendar.getInstance()) ;
  	} else {
  		catNode = forumHomeNode.getNode(category.getId()) ;
  	}
    catNode.setProperty("exo:name", category.getCategoryName()) ;
    catNode.setProperty("exo:categoryOrder", category.getCategoryOrder()) ;
    catNode.setProperty("exo:description", category.getDescription()) ;
    catNode.setProperty("exo:modifiedBy", category.getModifiedBy()) ;
    catNode.setProperty("exo:modifiedDate", GregorianCalendar.getInstance()) ;
    
  	forumHomeNode.save() ;
  	forumHomeNode.getSession().save() ;    
  }
  
  public Category removeCategory(String categoryId) throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    Category category = new Category () ;
    if(forumHomeNode.hasNode(categoryId)){
      category = getCategory(categoryId) ;
      forumHomeNode.getNode(categoryId).remove() ;
      forumHomeNode.save() ;
      forumHomeNode.getSession().save() ;
      return category;
    }
    return null;
  }
  
  
  public List<Forum> getForums(String categoryId) throws Exception {
		Node forumHomeNode = getForumHomeNode() ;
		if(forumHomeNode.hasNode(categoryId)) {
			Node catNode = forumHomeNode.getNode(categoryId) ;
	    QueryManager qm = forumHomeNode.getSession().getWorkspace().getQueryManager() ;
	    String queryString = "/jcr:root" + catNode.getPath() + "//element(*,exo:forum) order by @exo:forumOrder ascending,@exo:createdDate descending";
	    Query query = qm.createQuery(queryString , Query.XPATH) ;
	    QueryResult result = query.execute() ;
	    NodeIterator iter = result.getNodes() ;
		  List<Forum> Forums = new ArrayList<Forum>() ;
		  Forum forum;
		  while (iter.hasNext()) {
				Node forumNode = iter.nextNode() ;
				forum = getForum(forumNode) ;
		    Forums.add(forum) ;
		  }
		  return Forums;
		}
    return null; 
  }

  public Forum getForum(String categoryId, String forumId) throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    if(forumHomeNode.hasNode(categoryId)) {
      Node catNode = forumHomeNode.getNode(categoryId) ;
      Node forumNode = catNode.getNode(forumId) ;
      Forum forum = new Forum() ;
      forum = getForum(forumNode) ;
      return forum;
    }
    return null;
  }

  public void saveForum(String categoryId, Forum forum, boolean isNew) throws Exception {
		Node forumHomeNode = getForumHomeNode() ;
		if(forumHomeNode.hasNode(categoryId)) {
		  Node catNode = forumHomeNode.getNode(categoryId) ;
		  Node forumNode;
		  if(isNew) {
		    forumNode = catNode.addNode(forum.getId(), "exo:forum") ;
		    forumNode.setProperty("exo:id", forum.getId()) ;
		    forumNode.setProperty("exo:owner", forum.getOwner()) ;
		    forumNode.setProperty("exo:path", forumNode.getPath()) ;
		    forumNode.setProperty("exo:createdDate", GregorianCalendar.getInstance()) ;
		    forumNode.setProperty("exo:lastTopicPath", forum.getLastTopicPath()) ;
		    forumNode.setProperty("exo:postCount", 0) ;
		    forumNode.setProperty("exo:topicCount", 0) ;
		  } else {
		  	forumNode = catNode.getNode(forum.getId()) ;
		  }
		  forumNode.setProperty("exo:name", forum.getForumName()) ;
		  forumNode.setProperty("exo:forumOrder", forum.getForumOrder()) ;
		  forumNode.setProperty("exo:modifiedBy", forum.getModifiedBy()) ;
		  forumNode.setProperty("exo:modifiedDate", GregorianCalendar.getInstance()) ;
		  forumNode.setProperty("exo:description", forum.getDescription()) ;
		  
		  forumNode.setProperty("exo:notifyWhenAddPost", forum.getNotifyWhenAddPost()) ;
		  forumNode.setProperty("exo:notifyWhenAddTopic", forum.getNotifyWhenAddTopic()) ;
		  forumNode.setProperty("exo:isModerateTopic", forum.getIsModerateTopic()) ;
		  forumNode.setProperty("exo:isModeratePost", forum.getIsModeratePost()) ;
		  forumNode.setProperty("exo:isClosed", forum.getIsClosed()) ;
		  forumNode.setProperty("exo:isLock", forum.getIsLock()) ;
		  
		  forumNode.setProperty("exo:viewForumRole", forum.getViewForumRole()) ;
		  forumNode.setProperty("exo:createTopicRole", forum.getCreateTopicRole()) ;
		  forumNode.setProperty("exo:replyTopicRole", forum.getReplyTopicRole()) ;
		  forumNode.setProperty("exo:moderators", forum.getModerators()) ;
		  
		  forumHomeNode.save() ;
		  forumHomeNode.getSession().save() ;
		}
  }
  
  private Forum getForum(Node forumNode) throws Exception {
		Forum forum = new Forum() ;
		if(forumNode.hasProperty("exo:id")) forum.setId(forumNode.getProperty("exo:id").getString()) ;
    if(forumNode.hasProperty("exo:owner")) forum.setOwner(forumNode.getProperty("exo:owner").getString()) ;
    if(forumNode.hasProperty("exo:path")) forum.setPath(forumNode.getPath()) ;
    if(forumNode.hasProperty("exo:name")) forum.setForumName(forumNode.getProperty("exo:name").getString()) ;
    if(forumNode.hasProperty("exo:forumOrder")) forum.setForumOrder(Integer.valueOf(forumNode.getProperty("exo:forumOrder").getString())) ;
    if(forumNode.hasProperty("exo:createdDate")) forum.setCreatedDate(forumNode.getProperty("exo:createdDate").getDate().getTime()) ;
    if(forumNode.hasProperty("exo:modifiedBy")) forum.setModifiedBy(forumNode.getProperty("exo:modifiedBy").getString()) ;
    if(forumNode.hasProperty("exo:modifiedDate")) forum.setModifiedDate(forumNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
    if(forumNode.hasProperty("exo:lastTopicPath")) forum.setLastTopicPath(forumNode.getProperty("exo:lastTopicPath").getString()) ;
    if(forumNode.hasProperty("exo:description")) forum.setDescription(forumNode.getProperty("exo:description").getString()) ;
    if(forumNode.hasProperty("exo:postCount")) forum.setPostCount(Integer.valueOf(forumNode.getProperty("exo:postCount").getString())) ;
    if(forumNode.hasProperty("exo:topicCount")) forum.setTopicCount(Integer.valueOf(forumNode.getProperty("exo:topicCount").getString())) ;

    if(forumNode.hasProperty("exo:isModerateTopic")) forum.setIsModerateTopic(forumNode.getProperty("exo:isModerateTopic").getBoolean()) ;
    if(forumNode.hasProperty("exo:isModeratePost")) forum.setIsModeratePost(forumNode.getProperty("exo:isModeratePost").getBoolean()) ;
    if(forumNode.hasProperty("exo:isClosed")) forum.setIsClosed(forumNode.getProperty("exo:isClosed").getBoolean()) ;
    if(forumNode.hasProperty("exo:isLock")) forum.setIsLock(forumNode.getProperty("exo:isLock").getBoolean()) ;
    
    if(forumNode.hasProperty("exo:notifyWhenAddPost")) forum.setNotifyWhenAddPost(ValuesToStrings(forumNode.getProperty("exo:notifyWhenAddPost").getValues())) ;
    if(forumNode.hasProperty("exo:notifyWhenAddTopic")) forum.setNotifyWhenAddTopic(ValuesToStrings(forumNode.getProperty("exo:notifyWhenAddTopic").getValues())) ;
    if(forumNode.hasProperty("exo:viewForumRole")) forum.setViewForumRole(ValuesToStrings(forumNode.getProperty("exo:viewForumRole").getValues())) ;
    if(forumNode.hasProperty("exo:createTopicRole")) forum.setCreateTopicRole(ValuesToStrings(forumNode.getProperty("exo:createTopicRole").getValues())) ;
    if(forumNode.hasProperty("exo:replyTopicRole")) forum.setReplyTopicRole(ValuesToStrings(forumNode.getProperty("exo:replyTopicRole").getValues())) ;
    if(forumNode.hasProperty("exo:moderators")) forum.setModerators(ValuesToStrings(forumNode.getProperty("exo:moderators").getValues())) ;
    return forum;
  }

  public Forum removeForum(String categoryId, String forumId) throws Exception {
		Node forumHomeNode = getForumHomeNode() ;
		Forum forum = new Forum() ;
		if(forumHomeNode.hasNode(categoryId)) {
		  Node catNode = forumHomeNode.getNode(categoryId) ;
		  forum = getForum(categoryId, forumId) ;
		  catNode.getNode(forumId).remove() ;
		  forumHomeNode.save() ;
		  forumHomeNode.getSession().save() ;
		  return forum;
		}
		return null ;
  }

	public void moveForum(String forumId, String forumPath, String destCategoryPath)throws Exception {
  	Node forumHomeNode = getForumHomeNode() ;
  	String newForumPath = destCategoryPath + "/" + forumId;
  	forumHomeNode.getSession().getWorkspace().move(forumPath, newForumPath) ;
  	Node forumNode = (Node)getJCRSession().getItem(newForumPath) ;
  	forumNode.setProperty("exo:path", newForumPath) ;
  	forumHomeNode.save() ;
  	forumHomeNode.getSession().save() ;
  }
  
  
  public JCRPageList getTopics(String categoryId, String forumId) throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    if(forumHomeNode.hasNode(categoryId)) {
  	  Node CategoryNode = forumHomeNode.getNode(categoryId) ;
		  Node forumNode = CategoryNode.getNode(forumId) ;
		  NodeIterator iter = forumNode.getNodes() ;
		  JCRPageList pagelist = new ForumPageList(iter, 10, forumNode.getPath(), false) ;
		  return pagelist ;
    }
	  return null ;
  }
  
	public Topic getTopic(String categoryId, String forumId, String topicId) throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    if(forumHomeNode.hasNode(categoryId)) {
  	  Node CategoryNode = forumHomeNode.getNode(categoryId) ;
		  Node forumNode = CategoryNode.getNode(forumId) ;
		  Node topicNode = forumNode.getNode(topicId) ;
		  Topic topicNew = new Topic() ;
		  topicNew = getTopicNode(topicNode) ;
			// setViewCount for Topic
			long newViewCount = topicNode.getProperty("exo:viewCount").getLong() + 1 ;
			topicNode.setProperty("exo:viewCount", newViewCount) ;
      forumHomeNode.save() ;
      forumHomeNode.getSession().save() ;
		  return topicNew ;
    }
    return null ;
  }
	
  public Topic getTopicByPath(String topicPath)throws Exception {
    try {
      return getTopicNode((Node)getJCRSession().getItem(topicPath)) ;
    }catch(Exception e) {
    	return null ;
    }
  }
  
  private Topic getTopicNode(Node topicNode) throws Exception {
    Topic topicNew = new Topic() ;    
    if(topicNode.hasProperty("exo:id")) topicNew.setId(topicNode.getProperty("exo:id").getString()) ;
    if(topicNode.hasProperty("exo:owner")) topicNew.setOwner(topicNode.getProperty("exo:owner").getString()) ;
    if(topicNode.hasProperty("exo:path")) topicNew.setPath(topicNode.getProperty("exo:path").getString()) ;
    if(topicNode.hasProperty("exo:name")) topicNew.setTopicName(topicNode.getProperty("exo:name").getString()) ;
    if(topicNode.hasProperty("exo:createdDate")) topicNew.setCreatedDate(topicNode.getProperty("exo:createdDate").getDate().getTime()) ;
    if(topicNode.hasProperty("exo:modifiedBy")) topicNew.setModifiedBy(topicNode.getProperty("exo:modifiedBy").getString()) ;
    if(topicNode.hasProperty("exo:modifiedDate")) topicNew.setModifiedDate(topicNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
    if(topicNode.hasProperty("exo:lastPostBy")) topicNew.setLastPostBy(topicNode.getProperty("exo:lastPostBy").getString()) ;
    if(topicNode.hasProperty("exo:lastPostDate")) topicNew.setLastPostDate(topicNode.getProperty("exo:lastPostDate").getDate().getTime()) ;
    if(topicNode.hasProperty("exo:description")) topicNew.setDescription(topicNode.getProperty("exo:description").getString()) ;
    if(topicNode.hasProperty("exo:postCount")) topicNew.setPostCount(topicNode.getProperty("exo:postCount").getLong()) ;
    if(topicNode.hasProperty("exo:viewCount")) topicNew.setViewCount(topicNode.getProperty("exo:viewCount").getLong()) ;
    if(topicNode.hasProperty("exo:icon")) topicNew.setIcon(topicNode.getProperty("exo:icon").getString()) ;
    
    if(topicNode.hasProperty("exo:isNotifyWhenAddPost")) topicNew.setIsNotifyWhenAddPost(topicNode.getProperty("exo:isNotifyWhenAddPost").getBoolean()) ;
    if(topicNode.hasProperty("exo:isModeratePost")) topicNew.setIsModeratePost(topicNode.getProperty("exo:isModeratePost").getBoolean()) ;
    if(topicNode.hasProperty("exo:isClosed")) topicNew.setIsClosed(topicNode.getProperty("exo:isClosed").getBoolean()) ;
    if(topicNode.hasProperty("exo:isLock")) topicNew.setIsLock(topicNode.getProperty("exo:isLock").getBoolean()) ;
    if(topicNode.hasProperty("exo:isApproved")) topicNew.setIsApproved(topicNode.getProperty("exo:isApproved").getBoolean()) ;
    if(topicNode.hasProperty("exo:isSticky")) topicNew.setIsSticky(topicNode.getProperty("exo:isSticky").getBoolean()) ;
    if(topicNode.hasProperty("exo:viewPermissions")) topicNew.setViewPermissions(ValuesToStrings(topicNode.getProperty("exo:viewPermissions").getValues())) ;
    if(topicNode.hasProperty("exo:editPermissions")) topicNew.setEditPermissions(ValuesToStrings(topicNode.getProperty("exo:viewPermissions").getValues())) ;
    return topicNew;
  }

  public TopicView getTopicView(String categoryId, String forumId, String topicId) throws Exception {
	  TopicView topicview = new TopicView() ;
	  topicview.setTopicView(getTopic(categoryId, forumId, topicId)) ;
	  topicview.setPageList(getPosts(categoryId, forumId, topicId)) ;
  	return topicview;
  }
  
 
  public void saveTopic(String categoryId, String forumId, Topic topic, boolean isNew) throws Exception {
		Node forumHomeNode = getForumHomeNode() ;
		if(forumHomeNode.hasNode(categoryId)) {
		  Node CategoryNode = forumHomeNode.getNode(categoryId) ;
		  if(CategoryNode.hasNode(forumId)) {
				Node forumNode = CategoryNode.getNode(forumId) ;
				Node topicNode;
				if(isNew) {
					topicNode = forumNode.addNode(topic.getId(), "exo:topic") ;
					topicNode.setProperty("exo:id", topic.getId()) ;
					topicNode.setProperty("exo:path", topicNode.getPath()) ;
					topicNode.setProperty("exo:createdDate", GregorianCalendar.getInstance()) ;
					topicNode.setProperty("exo:lastPostBy", topic.getLastPostBy()) ;
					topicNode.setProperty("exo:lastPostDate", GregorianCalendar.getInstance()) ;
					topicNode.setProperty("exo:postCount", 0) ;
					topicNode.setProperty("exo:viewCount", 1) ;
				} else {
					topicNode = forumNode.getNode(topic.getId()) ;
				}
			  topicNode.setProperty("exo:owner", topic.getOwner()) ;
			  topicNode.setProperty("exo:name", topic.getTopicName()) ;
			  topicNode.setProperty("exo:modifiedBy", topic.getModifiedBy()) ;
			  topicNode.setProperty("exo:modifiedDate", GregorianCalendar.getInstance()) ;
			  topicNode.setProperty("exo:description", topic.getDescription()) ;
			  topicNode.setProperty("exo:icon", topic.getIcon()) ;
			  
			  topicNode.setProperty("exo:isModeratePost", topic.getIsModeratePost()) ;
		    topicNode.setProperty("exo:isNotifyWhenAddPost", topic.getIsNotifyWhenAddPost()) ;
		    topicNode.setProperty("exo:isClosed", topic.getIsClosed()) ;
		    topicNode.setProperty("exo:isLock", topic.getIsLock()) ;
		    topicNode.setProperty("exo:isApproved", topic.getIsApproved()) ;
		    topicNode.setProperty("exo:isSticky", topic.getIsSticky()) ;
		    topicNode.setProperty("exo:viewPermissions", topic.getViewPermissions()) ;
		    topicNode.setProperty("exo:editPermissions", topic.getEditPermissions()) ;

		    if(isNew) {
		    	// setTopicCount for Forum
			    long newTopicCount = forumNode.getProperty("exo:topicCount").getLong() + 1 ;
				  forumNode.setProperty("exo:topicCount", newTopicCount ) ;
				  
				  forumHomeNode.save() ;
				  forumHomeNode.getSession().save() ;
			    // createPost first
			    GregorianCalendar calendar = new GregorianCalendar() ;
          String id = "post" + Long.toString(calendar.getTimeInMillis(), 16);
			    Post post = new Post() ;
			    post.setId(id.toUpperCase()) ;
					post.setOwner(topic.getOwner()) ;
					post.setCreatedDate(new Date()) ;
					post.setModifiedBy(topic.getModifiedBy()) ;
					post.setModifiedDate(new Date()) ;
					post.setSubject(topic.getTopicName()) ;
					post.setMessage(topic.getDescription()) ;
					post.setRemoteAddr("") ;
					post.setIcon(topic.getIcon()) ;
          post.setNumberOfAttachment(topic.getAttachmentFirstPost()) ;
					post.setIsApproved(false) ;
					
					savePost(categoryId, forumId, topic.getId(), post, true) ;
		    } else {
				  forumHomeNode.save() ;
				  forumHomeNode.getSession().save() ;
		    }
		  }
		}
  }
  
  public Topic removeTopic(String categoryId, String forumId, String topicId) throws Exception {
		Node forumHomeNode = getForumHomeNode() ;
		Topic topic = new Topic() ;
	  if(forumHomeNode.hasNode(categoryId)) {
	  	Node CategoryNode = forumHomeNode.getNode(categoryId) ;
		  Node forumNode = CategoryNode.getNode(forumId) ;
		  topic = getTopic(categoryId, forumId, topicId) ;
		  // setTopicCount for Forum
		  long newTopicCount = forumNode.getProperty("exo:topicCount").getLong() - 1 ;
		  forumNode.setProperty("exo:topicCount", newTopicCount ) ;
		  // setPostCount for Forum
		  long newPostCount = forumNode.getProperty("exo:postCount").getLong() - topic.getPostCount() ;
		  forumNode.setProperty("exo:postCount", newPostCount ) ;
		  
		  forumNode.getNode(topicId).remove() ;
		  forumHomeNode.save() ;
		  forumHomeNode.getSession().save() ;
		  return topic ;
	  }
    return null ;
  }
  
  public void moveTopic(String  topicPath, String destForumPath) throws Exception {
  	Node forumHomeNode = getForumHomeNode() ;
  	forumHomeNode.getSession().getWorkspace().move(topicPath, destForumPath) ;
  	forumHomeNode.save() ;
  	forumHomeNode.getSession().save() ;
  }
  

  public JCRPageList getPosts(String categoryId, String forumId, String topicId) throws Exception {
  	Node forumHomeNode = getForumHomeNode() ;
  	if(forumHomeNode.hasNode(categoryId)) {
  		Node CategoryNode = forumHomeNode.getNode(categoryId) ;
  		if(CategoryNode.hasNode(forumId)) {
  			Node forumNode = CategoryNode.getNode(forumId) ;
  			if(forumNode.hasNode(topicId)) {
  				Node topicNode = forumNode.getNode(topicId) ;
  				NodeIterator iter = topicNode.getNodes() ; 
  				JCRPageList pagelist = new ForumPageList(iter, 10, topicNode.getPath(), false) ;
  				return pagelist ;
  			}
  		}
  	}
  	return null ;
  }
  
  public Post getPost(String categoryId, String forumId, String topicId, String postId) throws Exception {
		Node forumHomeNode = getForumHomeNode() ;
		if(forumHomeNode.hasNode(categoryId)) {
		  Node CategoryNode = forumHomeNode.getNode(categoryId) ;
		  if(CategoryNode.hasNode(forumId)) {
				Node forumNode = CategoryNode.getNode(forumId) ;
				Node topicNode = forumNode.getNode(topicId) ;
				if(!topicNode.hasNode(postId)) return null;
				Node postNode = topicNode.getNode(postId) ;
				Post postNew = new Post() ;
				postNew = getPost(postNode) ;
				return postNew ;
		  }
		}
    return null ;
  }

  private Post getPost(Node postNode) throws Exception {
    Post postNew = new Post() ;
    if(postNode.hasProperty("exo:id")) postNew.setId(postNode.getProperty("exo:id").getString()) ;
    if(postNode.hasProperty("exo:owner")) postNew.setOwner(postNode.getProperty("exo:owner").getString()) ;
    if(postNode.hasProperty("exo:path")) postNew.setPath(postNode.getProperty("exo:path").getString()) ;
    if(postNode.hasProperty("exo:createdDate")) postNew.setCreatedDate(postNode.getProperty("exo:createdDate").getDate().getTime()) ;
    if(postNode.hasProperty("exo:modifiedBy")) postNew.setModifiedBy(postNode.getProperty("exo:modifiedBy").getString()) ;
    if(postNode.hasProperty("exo:modifiedDate")) postNew.setModifiedDate(postNode.getProperty("exo:modifiedDate").getDate().getTime()) ;
    if(postNode.hasProperty("exo:subject")) postNew.setSubject(postNode.getProperty("exo:subject").getString()) ;
    if(postNode.hasProperty("exo:message")) postNew.setMessage(postNode.getProperty("exo:message").getString()) ;
    if(postNode.hasProperty("exo:remoteAddr")) postNew.setRemoteAddr(postNode.getProperty("exo:remoteAddr").getString()) ;
    if(postNode.hasProperty("exo:icon")) postNew.setIcon(postNode.getProperty("exo:icon").getString()) ;
    if(postNode.hasProperty("exo:isApproved")) postNew.setIsApproved(postNode.getProperty("exo:isApproved").getBoolean()) ;
    return postNew;
  }
  
  public void savePost(String categoryId, String forumId, String topicId, Post post, boolean isNew) throws Exception {
		Node forumHomeNode = getForumHomeNode() ;
		if(forumHomeNode.hasNode(categoryId)) {
		  Node CategoryNode = forumHomeNode.getNode(categoryId) ;
		  if(CategoryNode.hasNode(forumId)) {
				Node forumNode = CategoryNode.getNode(forumId) ;
				Node topicNode = forumNode.getNode(topicId) ;
				Node postNode;
				if(isNew) {
					postNode = topicNode.addNode(post.getId(), "exo:post") ;
					postNode.setProperty("exo:id", post.getId()) ;
					postNode.setProperty("exo:owner", post.getOwner()) ;
					postNode.setProperty("exo:path", postNode.getPath()) ;
					postNode.setProperty("exo:createdDate", GregorianCalendar.getInstance()) ;
				} else {
					postNode = topicNode.getNode(post.getId()) ;
				}
				postNode.setProperty("exo:modifiedBy", post.getModifiedBy()) ;
				postNode.setProperty("exo:modifiedDate", GregorianCalendar.getInstance()) ;
				postNode.setProperty("exo:subject", post.getSubject()) ;
				postNode.setProperty("exo:message", post.getMessage()) ;
				postNode.setProperty("exo:remoteAddr", post.getRemoteAddr()) ;
				postNode.setProperty("exo:icon", post.getIcon()) ;
				postNode.setProperty("exo:isApproved", post.getIsApproved()) ;
				if(isNew) {
			    // setPostCount for Topic
					long topicPostCount = topicNode.getProperty("exo:postCount").getLong() + 1 ;
					topicNode.setProperty("exo:postCount", topicPostCount ) ;
					// setPostCount for Forum
					long forumPostCount = forumNode.getProperty("exo:postCount").getLong() + 1 ;
					forumNode.setProperty("exo:postCount", forumPostCount ) ;
					forumNode.setProperty("exo:lastTopicPath", topicNode.getPath()) ;
				}
		    forumHomeNode.save() ;
		    forumHomeNode.getSession().save() ;
		  }
		}
  }
  
  public Post removePost(String categoryId, String forumId, String topicId, String postId) throws Exception {
    Node forumHomeNode = getForumHomeNode() ;
    Post post = new Post() ;
		if(forumHomeNode.hasNode(categoryId)) {
		  Node CategoryNode = forumHomeNode.getNode(categoryId) ;
		  if(CategoryNode.hasNode(forumId)) {
			  post = getPost(categoryId, forumId, topicId, postId) ;
				Node forumNode = CategoryNode.getNode(forumId) ;
				Node topicNode = forumNode.getNode(topicId) ;
				topicNode.getNode(postId).remove() ;
				// setPostCount for Topic
				long topicPostCount = topicNode.getProperty("exo:postCount").getLong() - 1 ;
				topicNode.setProperty("exo:postCount", topicPostCount ) ;
				// setPostCount for Forum
				long forumPostCount = forumNode.getProperty("exo:postCount").getLong() - 1 ;
				forumNode.setProperty("exo:postCount", forumPostCount ) ;

				forumHomeNode.save() ;
				forumHomeNode.getSession().save() ;
				return post;
		  }
		}
		return null;
  }
  
  public void movePost(String postPath, String destTopicPaths) throws Exception {
  	Node forumHomeNode = getForumHomeNode() ;
  	forumHomeNode.getSession().getWorkspace().move(postPath, destTopicPaths) ;
  	forumHomeNode.save() ;
  	forumHomeNode.getSession().save() ;
  }
  
  public List getPage(long page, JCRPageList pageList) throws Exception {
    return pageList.getPage(page, getForumHomeNode().getSession()) ;
  }

  private String [] ValuesToStrings(Value[] Val) throws Exception {
  	if(Val.length == 1) return new String[]{Val[0].getString()} ;
		String[] Str = new String[Val.length] ;
		for(int i = 0; i < Val.length; ++i) {
		  Str[i] = Val[i].getString() ;
		}
		return Str;
  }
  
  protected Node getForumHomeNode() throws Exception {
    ServiceRegistry serviceRegistry = new ServiceRegistry("ForumService") ;
    Session session = getJCRSession() ;
    jcrRegistryService_.createServiceRegistry(serviceRegistry, false) ;    
    return jcrRegistryService_.getServiceRegistryNode(session, serviceRegistry.getName()) ;
  }
  
  private Session getJCRSession() throws Exception {
    String defaultWS = 
      repositoryService_.getDefaultRepository().getConfiguration().getDefaultWorkspaceName() ;
    return repositoryService_.getDefaultRepository().getSystemSession(defaultWS) ;
  }
  
//  public Object getObjectByPath(String path) throws Exception {
//    Object object = new Object() ;
//    Node myNode = (Node)getJCRSession().getItem(path) ;
//    if(myNode.getPrimaryNodeType().getName() == "exo:post") {
//      object = (Object)getPost(myNode) ;
//    }else if(myNode.getPrimaryNodeType().getName() == "exo:topic") {
//      object = (Object)getTopicNode(myNode) ;
//    }else if(myNode.getPrimaryNodeType().getName() == "exo:forum") {
//      object = (Object)getForum(myNode) ;
//    } else return null;
//    return object;
//  }
  
  public List<ForumLinkData> getAllLink() throws Exception {
    return null ;
  }
}
