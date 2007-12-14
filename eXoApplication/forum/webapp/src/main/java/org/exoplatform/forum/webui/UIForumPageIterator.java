/* Copyright 2001-2007 The eXo Platform SARL				 All rights reserved.	*
 * Please look at license.txt in info directory for more license detail.	 *
 **************************************************************************/
package org.exoplatform.forum.webui;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.forum.service.JCRPageList;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIContainer;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Vu Duy Tu
 *					tu.duy@exoplatform.com
 * Nov 19, 2007 9:18:18 AM 
 */

@ComponentConfig(
	 template = "app:/templates/forum/webui/UIForumPageIterator.gtmpl",
	 events = {
		 @EventConfig(listeners = UIForumPageIterator.GoPageActionListener.class)
	 }
)

public class UIForumPageIterator extends UIContainer {
	private JCRPageList pageList ;
	private long page = 1 ;
	private int endTabPage = 0;
	private int beginTabPage = 0;
	public UIForumPageIterator () throws Exception {
		
	}
	
	public void updatePageList(JCRPageList pageList ) {
		this.pageList = pageList ;
	}
	
	@SuppressWarnings("unused")
	private List<String> getTotalpage() throws	Exception {
		int max_Page = (int)pageList.getAvailablePage() ;
		if(this.page <= 3) {
			beginTabPage = 1 ;
			if(max_Page <= 7)
				endTabPage = max_Page ;
			else endTabPage = 7 ;
		} else {
			if(max_Page > (page + 3)) {
				endTabPage = (int) (page + 3) ;
				beginTabPage = (int) (page - 3) ;
			} else {
				endTabPage = max_Page ;
				if(max_Page > 7) beginTabPage = max_Page - 6 ;
				else beginTabPage = 1 ;
			}
		}
		List<String> temp = new ArrayList<String>() ;
		for (int i = beginTabPage; i <= endTabPage; i++) {
			temp.add("" + i) ;
		}
		return temp ;
	}

	@SuppressWarnings("unused")
	private List<Long> getInfoPage() throws	Exception {
		List<Long> temp = new ArrayList<Long>() ;
		temp.add(pageList.getPageSize()) ;//so item/trang
		temp.add(pageList.getCurrentPage()) ;//so trang hien tai
		temp.add(pageList.getAvailable()) ;//tong so item
		temp.add(pageList.getAvailablePage()) ;// so trang toi da
		return temp ;
	} 
	
	public void setSelectPage(long page) {
		this.page = page;
	}
	
	@SuppressWarnings("unused")
	public long getPageSelected() {
		return this.page ;
	}
		
	static public class GoPageActionListener extends EventListener<UIForumPageIterator> {
		public void execute(Event<UIForumPageIterator> event) throws Exception {
			UIForumPageIterator forumPageIterator = event.getSource() ;
			String stateClick = event.getRequestContext().getRequestParameter(OBJECTID).trim() ;
			long maxPage = forumPageIterator.pageList.getAvailablePage() ;
			long presentPage	= forumPageIterator.page ;
			if(stateClick.equalsIgnoreCase("next")) {
				if(presentPage < maxPage){
					forumPageIterator.page = presentPage + 1 ;
					event.getRequestContext().addUIComponentToUpdateByAjax(forumPageIterator.getParent()) ;
				}
			} else if(stateClick.equalsIgnoreCase("previous")){
				if(presentPage > 1){
					forumPageIterator.page = presentPage - 1 ;
					event.getRequestContext().addUIComponentToUpdateByAjax(forumPageIterator.getParent()) ;
				}
			} else if(stateClick.equalsIgnoreCase("last")) {
				if(presentPage != maxPage) {
					forumPageIterator.page = maxPage ;
					event.getRequestContext().addUIComponentToUpdateByAjax(forumPageIterator.getParent()) ;
				}
			} else if(stateClick.equalsIgnoreCase("first")) {
				if(presentPage != 1) {
					forumPageIterator.page = 1 ;
					event.getRequestContext().addUIComponentToUpdateByAjax(forumPageIterator.getParent()) ;
				}
			} else {
				long temp = Long.parseLong(stateClick) ;
				if(temp > 0 && temp <= maxPage && temp != presentPage) {
					forumPageIterator.page = temp ;
					event.getRequestContext().addUIComponentToUpdateByAjax(forumPageIterator.getParent()) ;
				}
			}
		}
	}
	
	
}
