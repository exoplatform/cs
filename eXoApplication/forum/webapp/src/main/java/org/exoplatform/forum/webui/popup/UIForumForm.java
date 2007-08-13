/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;
import org.exoplatform.webui.form.validator.PositiveNumberFormatValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Hung Nguyen
 *          hung.nguyen@exoplatform.com
 * Aus 01, 2007 2:48:18 PM 
 */
@ComponentConfig(
    lifecycle = UIFormLifecycle.class,
//    template = "system:/groovy/webui/form/UIFormWithTitle.gtmpl",
    template = "app:/templates/forum/webui/popup/UIAddNewForum.gtmpl",
    events = {
      @EventConfig(listeners = UIForumForm.SaveActionListener.class), 
      @EventConfig(listeners = UIForumForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UIForumForm extends UIForm implements UIPopupComponent{
	ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
	public static final String FIELD_CATEGORY_SELECTBOX = "Category" ;
	public static final String FIELD_FORUMTITLE_INPUT = "ForumTitle" ;
	public static final String FIELD_FORUMORDER_INPUT = "ForumOrder" ;
	public static final String FIELD_FORUMSTATUS_SELECTBOX = "ForumStatus" ;
	public static final String FIELD_TEXT_AREA = "Description" ;
  
  public UIForumForm() throws Exception {
  	List<Category> category = forumService.getCategories();
  	List<SelectItemOption<String>> cate = new ArrayList<SelectItemOption<String>>() ;
  	for (int i = 0; i < category.size(); i++) {
  		cate.add(new SelectItemOption<String>(category.get(i).getCategoryName(), category.get(i).getId())) ;
		}
  	UIFormSelectBox categoryId = new UIFormSelectBox(FIELD_CATEGORY_SELECTBOX, FIELD_CATEGORY_SELECTBOX, cate) ;
  	categoryId.setDefaultValue(category.get(0).getId());
  	UIFormStringInput forumTitle = new UIFormStringInput(FIELD_FORUMTITLE_INPUT, FIELD_FORUMTITLE_INPUT, null);
  	UIFormStringInput forumOrder = new UIFormStringInput(FIELD_FORUMORDER_INPUT, FIELD_FORUMORDER_INPUT, null);
  	forumOrder.addValidator(PositiveNumberFormatValidator.class) ;
  	
  	List<SelectItemOption<String>> ls = new ArrayList<SelectItemOption<String>>() ;
    ls.add(new SelectItemOption<String>("Open", "open")) ;
    ls.add(new SelectItemOption<String>("Locked", "locked")) ;
    ls.add(new SelectItemOption<String>("Closed", "closed")) ;
    UIFormSelectBox forumStatus = new UIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX, FIELD_FORUMSTATUS_SELECTBOX, ls) ;
    forumStatus.setDefaultValue("open");
  	UIFormStringInput description = new UIFormTextAreaInput(FIELD_TEXT_AREA, FIELD_TEXT_AREA, null);
  	 
  	addUIFormInput(categoryId) ;
  	addUIFormInput(forumTitle) ;
  	addUIFormInput(forumOrder) ;
  	addUIFormInput(forumStatus) ;
  	addUIFormInput(description) ;
  	 
//  	UIForumForm uicomponent = this;
//  	uicomponent.getChild(0).getName()
  }
  
  public void activate() throws Exception {
		// TODO Auto-generated method stub
		
	}
	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
	}
  
  static  public class SaveActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      UIForumForm uiForm = event.getSource() ;
      String categoryId = uiForm.getUIFormSelectBox(FIELD_CATEGORY_SELECTBOX).getValue();
      String forumTitle = uiForm.getUIStringInput(FIELD_FORUMTITLE_INPUT).getValue();
      String forumOrder = uiForm.getUIStringInput(FIELD_FORUMORDER_INPUT).getValue();
      String forumStatus = uiForm.getUIFormSelectBox(FIELD_FORUMSTATUS_SELECTBOX).getValue();
      String description = uiForm.getUIFormTextAreaInput(FIELD_TEXT_AREA).getValue();
      
      System.out.println("\n\n" +categoryId +"   :   " +forumTitle + "   :   "+ forumOrder+ "   :   "+ forumStatus+"   :   "+description+"\n\n");
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
  
  static  public class CancelActionListener extends EventListener<UIForumForm> {
    public void execute(Event<UIForumForm> event) throws Exception {
      UIForumForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }
}
