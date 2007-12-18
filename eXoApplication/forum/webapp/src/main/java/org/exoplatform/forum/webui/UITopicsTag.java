/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL				 All rights reserved.	*
 * Please look at license.txt in info directory for more license detail.	 *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.ForumUtils;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.forum.service.Tag;
import org.exoplatform.forum.service.Topic;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.form.UIForm;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *					hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
		lifecycle = UIFormLifecycle.class,
		template =	"app:/templates/forum/webui/UITopicsTag.gtmpl",
		events = {
				@EventConfig(listeners = UITopicsTag.OpenTopicActionListener.class )
		}
)

public class UITopicsTag extends UIForm {
	private ForumService forumService = (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	private String tagId = "" ;
	private JCRPageList listTopic ;
	private List<Topic> topics ;
	private long page = 1 ;
	private List <JCRPageList> listPageListPost = new ArrayList<JCRPageList>() ;
	public UITopicsTag() throws Exception {
		//addChild(UIForumPageIterator.class, null, null) ;
	}
	
	public void setIdTag(String tagId) {
		this.tagId = tagId ;
  }
	
	@SuppressWarnings("unused")
  private JCRPageList getListTopicTag() throws Exception {
		System.out.println("\n\n Xem chay cai nay chua da UI" + this.tagId);
		this.listTopic = forumService.getTopicsByTag(ForumUtils.getSystemProvider(), this.tagId) ;
		return this.listTopic ;
	}

	@SuppressWarnings({ "unchecked", "unused" })
  private List<Topic> getTopicsTag() throws Exception {
		this.listTopic = forumService.getTopicsByTag(ForumUtils.getSystemProvider(), this.tagId) ;
		this.topics = forumService.getPage(page, this.listTopic, ForumUtils.getSystemProvider()) ;
		return this.topics ;
	}
	
	@SuppressWarnings("unused")
  private Tag getTagById() throws Exception {
		return forumService.getTag(ForumUtils.getSystemProvider(), this.tagId) ;
	}

	@SuppressWarnings("unused")
	private String[] getStarNumber(Topic topic) throws Exception {
		double voteRating = topic.getVoteRating() ;
		int star = (int)voteRating ;
		String[] className = new String[6] ;
		float k = 0;
		for (int i = 0; i < 5; i++) {
			if(i < star) className[i] = "star" ;
			else if(i == star) {
				k = (float) (voteRating - i) ; 
				if(k < 0.25) className[i] = "notStar" ;
				if(k >= 0.25 && k < 0.75) className[i] = "halfStar" ;
				if(k >= 0.75) className[i] = "star" ;
			} else {
				className[i] = "notStar" ;
			}
			
			className[5] = ("" + voteRating) ;
			if(className[5].length() >= 3) className[5] = className[5].substring(0, 3) ;
			if(k == 0) className[5] = "" + star ; 
		}
		return className ;
	}
	
	
	
	
	@SuppressWarnings("unused")
	private String getStringCleanHtmlCode(String sms) {
		StringBuffer string = new StringBuffer();
		char c; boolean get = true ;
		for (int i = 0; i < sms.length(); i++) {
			c = sms.charAt(i);
			if(c == '<') get = false ;
			if(get) string.append(c);
			if(c == '>') get = true ;
		}
		return string.toString();
	}
	@SuppressWarnings("unused")
	private List<Tag> getTagsByTopic(String[] tagIds) throws Exception {
		return this.forumService.getTagsByTopic(ForumUtils.getSystemProvider(), tagIds);	
	}
	
	private Topic getTopic(String topicId) throws Exception {
		List<Topic> listTopic = this.topics ;
		for (Topic topic : listTopic) {
			if(topic.getId().equals(topicId)) return topic ;
		}
		return null ;
	}
	
	static public class OpenTopicActionListener extends EventListener<UITopicsTag> {
		public void execute(Event<UITopicsTag> event) throws Exception {
			UITopicsTag uiTopicsTag = event.getSource();
			String topicId = event.getRequestContext().getRequestParameter(OBJECTID) ;
			Topic topic = uiTopicsTag.getTopic(topicId);
			String []temp = topic.getPath().split("/") ;
			UIForumPortlet forumPortlet = uiTopicsTag.getAncestorOfType(UIForumPortlet.class) ;
			forumPortlet.updateIsRendered(2);
			UIForumContainer uiForumContainer = forumPortlet.getChild(UIForumContainer.class) ;
			UITopicDetailContainer uiTopicDetailContainer = uiForumContainer.getChild(UITopicDetailContainer.class) ;
			uiForumContainer.setIsRenderChild(false) ;
			UITopicDetail uiTopicDetail = uiTopicDetailContainer.getChild(UITopicDetail.class) ;
			uiTopicDetail.setUpdateContainer(temp[temp.length-3], temp[temp.length-2], topic, 1) ;
			uiTopicDetailContainer.getChild(UITopicPoll.class).updatePoll(temp[temp.length-3], temp[temp.length-2], topic) ;
			forumPortlet.getChild(UIForumLinks.class).setValueOption(temp[temp.length-3] + "/" + temp[temp.length-2] + "");
			event.getRequestContext().addUIComponentToUpdateByAjax(forumPortlet) ;
		}
	}
	


}
