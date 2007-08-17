/***************************************************************************
 * Copyright 2001-2006 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.forum.webui.popup;

import java.util.Date;
import java.util.GregorianCalendar;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.forum.service.Category;
import org.exoplatform.forum.service.ForumService;
import org.exoplatform.forum.webui.UICategories;
import org.exoplatform.forum.webui.UICategoryContainer;
import org.exoplatform.forum.webui.UIForumPortlet;
import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIForm;
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
    template = "app:/templates/forum/webui/popup/UIAddNewCategory.gtmpl",
    events = {
      @EventConfig(listeners = UICategoryForm.SaveActionListener.class), 
      @EventConfig(listeners = UICategoryForm.CancelActionListener.class, phase=Phase.DECODE)
    }
)
public class UICategoryForm extends UIForm implements UIPopupComponent{
	
	public static final String FIELD_CATEGORYTITLE_INPUT = "CategoryTitle" ;
	public static final String FIELD_CATEGORYORDER_INPUT = "CategoryOrder" ;
	public static final String FIELD_TEXT_AREA = "Description" ;
  
  public UICategoryForm() throws Exception {
  	UIFormStringInput categoryTitle = new UIFormStringInput(FIELD_CATEGORYTITLE_INPUT, FIELD_CATEGORYTITLE_INPUT, null);
  	UIFormStringInput categoryOrder = new UIFormStringInput(FIELD_CATEGORYORDER_INPUT, FIELD_CATEGORYORDER_INPUT, "0");
  	categoryOrder.addValidator(PositiveNumberFormatValidator.class);
  	UIFormStringInput description = new UIFormTextAreaInput(FIELD_TEXT_AREA, FIELD_TEXT_AREA, null);
  	 addUIFormInput(categoryTitle);
  	 addUIFormInput(categoryOrder);
  	 addUIFormInput(description);
  }
  
  public void activate() throws Exception {
		// TODO Auto-generated method stub
		
	}
	public void deActivate() throws Exception {
		// TODO Auto-generated method stub
		//System.out.println("\n\n description: sfdsf\n\n");
	}
	
  static  public class SaveActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
      UICategoryForm uiForm = event.getSource() ;
      String categoryTitle = uiForm.getUIStringInput(FIELD_CATEGORYTITLE_INPUT).getValue();
      String categoryOrder = uiForm.getUIStringInput(FIELD_CATEGORYORDER_INPUT).getValue();
      String description = uiForm.getUIFormTextAreaInput(FIELD_TEXT_AREA).getValue();
      
      GregorianCalendar calendar = new GregorianCalendar() ;
      PortalRequestContext pContext = Util.getPortalRequestContext();
      String userName = pContext.getRemoteUser() ;
  		String id = "Cate" + String.valueOf(calendar.getTimeInMillis());
  		
      Category cat = new Category();
      cat.setId(id) ;
      cat.setOwner(userName) ;
      cat.setCategoryName(categoryTitle) ;
      cat.setCategoryOrder(Long.parseLong(categoryOrder)) ;
      cat.setCreatedDate(new Date()) ;
      cat.setDescription(description) ;
      cat.setModifiedBy(userName) ;
      cat.setModifiedDate(new Date()) ;
      
      ForumService forumService =  (ForumService)PortalContainer.getInstance().getComponentInstanceOfType(ForumService.class) ;
      forumService.saveCategory(cat, true);
      
//      Category cate = forumService.getCategory(id);
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
      UICategories uiCategories = forumPortlet.getChild(UICategoryContainer.class).getChild(UICategories.class) ;
      WebuiRequestContext context = WebuiRequestContext.getCurrentInstance() ;
      context.addUIComponentToUpdateByAjax(uiCategories) ;
      
    }
  }
  
  static  public class CancelActionListener extends EventListener<UICategoryForm> {
    public void execute(Event<UICategoryForm> event) throws Exception {
      UICategoryForm uiForm = event.getSource() ;
      UIForumPortlet forumPortlet = event.getSource().getAncestorOfType(UIForumPortlet.class) ;
      forumPortlet.cancelAction() ;
    }
  }	
}
