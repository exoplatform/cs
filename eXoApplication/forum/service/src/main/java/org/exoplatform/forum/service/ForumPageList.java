/***************************************************************************
 * Copyright 2001-2003 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.service;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
/**
 * @author Hung Nguyen (hung.nguyen@exoplatform.com)
 * @since July 25, 2007
 */
public class ForumPageList extends JCRPageList {
  
  private NodeIterator iter_ = null ;
  private boolean isQuery_ = false ;
  private String value_ ;
  
  public ForumPageList(NodeIterator iter, long pageSize, String value, boolean isQuery ) throws Exception{
    super(pageSize) ;
    iter_ = iter ;
    value_ = value ;
    isQuery_ = isQuery ;
    setAvailablePage(iter.getSize()) ;    
  }
  
  protected void populateCurrentPage(long page, Session session) throws Exception  {
    if(iter_ == null) {
      if(isQuery_) {
        QueryManager qm = session.getWorkspace().getQueryManager() ;
        Query query = qm.createQuery(value_, Query.XPATH);
        QueryResult result = query.execute();
        iter_ = result.getNodes();
      } else {
        Node node = (Node)session.getItem(value_) ;
        iter_ = node.getNodes() ;
      }
    }
    setAvailablePage(iter_.getSize()) ;
    Node currentNode ;
    long pageSize = getPageSize() ;
    long position = 0 ;
    if(page == 1) position = 0;
    else {
      position = (page-1) * pageSize ;
      iter_.skip(position) ;
    }
    currentListPage_ = new ArrayList<Object>() ;
    for(int i = 0; i < pageSize; i ++) {
      if(iter_.hasNext()){
        currentNode = iter_.nextNode() ;
        if(currentNode.isNodeType("exo:post")) {
          currentListPage_.add(getPost(currentNode)) ;
        }else if(currentNode.isNodeType("exo:topic")) {
          currentListPage_.add(getTopic(currentNode)) ;
        }
      }else {
        break ;
      }
    }
    iter_ = null ;
    //currentListPage_ = objects_.subList(getFrom(), getTo()) ;
  }
  
  private Post getPost(Node postNode) throws Exception {
    Post postNew = new Post();
    if(postNode.hasProperty("exo:id")) postNew.setId(postNode.getProperty("exo:id").getString());
    if(postNode.hasProperty("exo:owner")) postNew.setOwner(postNode.getProperty("exo:owner").getString());
    if(postNode.hasProperty("exo:path")) postNew.setPath(postNode.getPath());
    if(postNode.hasProperty("exo:createdDate")) postNew.setCreatedDate(postNode.getProperty("exo:createdDate").getDate().getTime());
    if(postNode.hasProperty("exo:modifiedBy")) postNew.setModifiedBy(postNode.getProperty("exo:modifiedBy").getString());
    if(postNode.hasProperty("exo:modifiedDate")) postNew.setModifiedDate(postNode.getProperty("exo:modifiedDate").getDate().getTime());
    if(postNode.hasProperty("exo:subject")) postNew.setSubject(postNode.getProperty("exo:subject").getString());
    if(postNode.hasProperty("exo:message")) postNew.setMessage(postNode.getProperty("exo:message").getString());
    if(postNode.hasProperty("exo:remoteAddr")) postNew.setRemoteAddr(postNode.getProperty("exo:remoteAddr").getString());
    if(postNode.hasProperty("exo:icon")) postNew.setIcon(postNode.getProperty("exo:icon").getString());
    if(postNode.hasProperty("exo:isApproved")) postNew.setIsApproved(postNode.getProperty("exo:isApproved").getBoolean());
    return postNew;
  }
  
  private Topic getTopic(Node topicNode) throws Exception {
    Topic topicNew = new Topic();    
    if(topicNode.hasProperty("exo:id")) topicNew.setId(topicNode.getProperty("exo:id").getString());
    if(topicNode.hasProperty("exo:owner")) topicNew.setOwner(topicNode.getProperty("exo:owner").getString());
    if(topicNode.hasProperty("exo:path")) topicNew.setPath(topicNode.getPath());
    if(topicNode.hasProperty("exo:name")) topicNew.setTopicName(topicNode.getProperty("exo:name").getString());
    if(topicNode.hasProperty("exo:createdDate")) topicNew.setCreatedDate(topicNode.getProperty("exo:createdDate").getDate().getTime());
    if(topicNode.hasProperty("exo:modifiedBy")) topicNew.setModifiedBy(topicNode.getProperty("exo:modifiedBy").getString());
    if(topicNode.hasProperty("exo:modifiedDate")) topicNew.setModifiedDate(topicNode.getProperty("exo:modifiedDate").getDate().getTime());
    if(topicNode.hasProperty("exo:lastPostBy")) topicNew.setLastPostBy(topicNode.getProperty("exo:lastPostBy").getString());
    if(topicNode.hasProperty("exo:lastPostDate")) topicNew.setLastPostDate(topicNode.getProperty("exo:lastPostDate").getDate().getTime());
    if(topicNode.hasProperty("exo:description")) topicNew.setDescription(topicNode.getProperty("exo:description").getString());
    if(topicNode.hasProperty("exo:postCount")) topicNew.setPostCount(topicNode.getProperty("exo:postCount").getLong());
    if(topicNode.hasProperty("exo:viewCount")) topicNew.setViewCount(topicNode.getProperty("exo:viewCount").getLong());
    if(topicNode.hasProperty("exo:icon")) topicNew.setIcon(topicNode.getProperty("exo:icon").getString());
    if(topicNode.hasProperty("exo:isNotifyWhenAddPost")) topicNew.setIsNotifyWhenAddPost(topicNode.getProperty("exo:isNotifyWhenAddPost").getBoolean());
    if(topicNode.hasProperty("exo:isModeratePost")) topicNew.setIsModeratePost(topicNode.getProperty("exo:isModeratePost").getBoolean());
    if(topicNode.hasProperty("exo:isClosed")) topicNew.setIsClosed(topicNode.getProperty("exo:isClosed").getBoolean());
    if(topicNode.hasProperty("exo:isLock")) {
      if(topicNode.getParent().getProperty("exo:isLock").getBoolean()) topicNew.setIsLock(true);
      else topicNew.setIsLock(topicNode.getProperty("exo:isLock").getBoolean()) ;
    }
    if(topicNode.hasProperty("exo:isApproved")) topicNew.setIsApproved(topicNode.getProperty("exo:isApproved").getBoolean());
    if(topicNode.hasProperty("exo:isSticky")) topicNew.setIsSticky(topicNode.getProperty("exo:isSticky").getBoolean()) ;
    if(topicNode.hasProperty("exo:canView")) topicNew.setCanView(ValuesToStrings(topicNode.getProperty("exo:canView").getValues()));
    if(topicNode.hasProperty("exo:canPost")) topicNew.setCanPost(ValuesToStrings(topicNode.getProperty("exo:canPost").getValues()));
    if(topicNode.hasProperty("exo:isPoll")) topicNew.setIsPoll(topicNode.getProperty("exo:isPoll").getBoolean()) ;
    if(topicNode.hasProperty("exo:userVoteRating")) topicNew.setUserVoteRating(ValuesToStrings(topicNode.getProperty("exo:userVoteRating").getValues())) ;
    if(topicNode.hasProperty("exo:voteRating")) topicNew.setVoteRating(topicNode.getProperty("exo:voteRating").getDouble()) ;
    return topicNew;
  }
  
  private String [] ValuesToStrings(Value[] Val) throws Exception {
  	if(Val.length == 1)
  		return new String[]{Val[0].getString()};
		String[] Str = new String[Val.length];
		for(int i = 0; i < Val.length; ++i) {
		  Str[i] = Val[i].getString();
		}
		return Str;
  }
  
	@Override
	public List getAll() throws Exception { return null; }



}
