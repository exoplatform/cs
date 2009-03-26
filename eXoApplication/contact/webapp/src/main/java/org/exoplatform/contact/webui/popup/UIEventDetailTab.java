/*
 * Copyright (C) 2003-2007 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.contact.webui.popup;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.service.Attachment;
import org.exoplatform.calendar.service.CalendarEvent;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormSelectBoxWithGroups;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTextAreaInput;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 *          Nam Phung
 *          phunghainam@gmail.com
 * Aug 29, 2007  
 */

@ComponentConfig(
    template = "app:/templates/contact/webui/popup/UIEventDetailTab.gtmpl"
) 
public class UIEventDetailTab extends UIFormInputWithActions {

  final public static String FIELD_EVENT = "eventName".intern() ;
  final public static String FIELD_CALENDAR = "calendar".intern() ;
  final public static String FIELD_CATEGORY = "category".intern() ;
  final public static String FIELD_FROM = "from".intern() ;
  final public static String FIELD_TO = "to".intern() ;
  final public static String FIELD_FROM_TIME = "fromTime".intern() ;
  final public static String FIELD_TO_TIME = "toTime".intern() ;

  final public static String FIELD_CHECKALL = "allDay".intern() ;
  final public static String FIELD_REPEAT = "repeat".intern() ;
  final public static String FIELD_PLACE = "place".intern() ;
  final public static String FIELD_PRIORITY = "priority".intern() ; 
  final public static String FIELD_DESCRIPTION = "description".intern() ;
  final static public String FIELD_ATTACHMENTS = "attachments".intern() ;

  protected List<Attachment> attachments_ = new ArrayList<Attachment>() ;
  private Map<String, List<ActionData>> actionField_ ;

  public UIEventDetailTab(String id) throws Exception {
    super(id);
    setComponentConfig(getClass(), null) ;
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    actionField_ = new HashMap<String, List<ActionData>>() ;
    addUIFormInput(new UIFormStringInput(FIELD_EVENT, FIELD_EVENT, null)) ;
    addUIFormInput(new UIFormTextAreaInput(FIELD_DESCRIPTION, FIELD_DESCRIPTION, null)) ;
    addUIFormInput(new UIFormSelectBoxWithGroups(FIELD_CALENDAR, FIELD_CALENDAR, null)) ;
    addUIFormInput(new UIFormSelectBox(FIELD_CATEGORY, FIELD_CATEGORY, UIEventForm.getCategory())) ;

    ActionData addCategoryAction = new ActionData() ;
    addCategoryAction.setActionType(ActionData.TYPE_ICON) ;
    addCategoryAction.setActionName(UIEventForm.ACT_ADDCATEGORY) ;
    addCategoryAction.setActionListener(UIEventForm.ACT_ADDCATEGORY) ;
    List<ActionData> addCategoryActions = new ArrayList<ActionData>() ;
    addCategoryActions.add(addCategoryAction) ;
    setActionField(FIELD_CATEGORY, addCategoryActions) ;

    addUIFormInput(new UIFormDateTimeInput(FIELD_FROM, FIELD_FROM, new Date(), false));
    addUIFormInput(new UIFormSelectBox(FIELD_FROM_TIME, FIELD_FROM_TIME, options));
    addUIFormInput(new UIFormDateTimeInput(FIELD_TO, FIELD_TO, new Date(), false));
    addUIFormInput(new UIFormSelectBox(FIELD_TO_TIME, FIELD_TO_TIME,  options));
    addUIFormInput(new UIFormCheckBoxInput<Boolean>(FIELD_CHECKALL, FIELD_CHECKALL, null));
    addUIFormInput(new UIFormStringInput(FIELD_PLACE, FIELD_PLACE, null));
    addUIFormInput(new UIFormSelectBox(FIELD_REPEAT, FIELD_REPEAT, getRepeater())) ;
    addUIFormInput(new UIFormSelectBox(FIELD_PRIORITY, FIELD_PRIORITY, getPriority())) ;
    ActionData addEmailAddress = new ActionData() ;
    addEmailAddress.setActionType(ActionData.TYPE_ICON) ;
    addEmailAddress.setActionName(UIEventForm.ACT_ADDEMAIL) ;
    addEmailAddress.setActionListener(UIEventForm.ACT_ADDEMAIL) ;

    List<ActionData> addMailActions = new ArrayList<ActionData>() ;
    addMailActions.add(addEmailAddress) ;

  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }

  private List<SelectItemOption<String>> getPriority() throws Exception {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    options.add(new SelectItemOption<String>("normal", "2")) ;
    options.add(new SelectItemOption<String>("high", "1")) ;
    options.add(new SelectItemOption<String>("low", "3")) ;
    return options ;
  }
  
  private List<SelectItemOption<String>> getRepeater() {
    List<SelectItemOption<String>> options = new ArrayList<SelectItemOption<String>>() ;
    for(String s : CalendarEvent.REPEATTYPES) {
      options.add(new SelectItemOption<String>(s,s)) ;
    }
    return options ;
  }

  public void setActionField(String fieldName, List<ActionData> actions) throws Exception {
    actionField_.put(fieldName, actions) ;
  }
  
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }

}
