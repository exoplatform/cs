/**
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
 **/
package org.exoplatform.calendar.webui.popup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.calendar.CalendarUtils;
import org.exoplatform.calendar.webui.UIFormComboBox;
import org.exoplatform.calendar.webui.popup.UIAddressForm.ContactData;
import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.contact.service.ContactFilter;
import org.exoplatform.contact.service.ContactService;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPageIterator;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormCheckBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormInputWithActions.ActionData;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Aug 29, 2007  
 */

@ComponentConfig(
                 template = "app:/templates/calendar/webui/UIPopup/UIEventShareTab.gtmpl"
) 
public class UIEventShareTab extends UIFormInputWithActions {

  final public static String FIELD_SHARE = "shareEvent".intern() ;
  final public static String FIELD_STATUS = "status".intern() ;
  final public static String FIELD_SEND = "send".intern();
  final public static String FIELD_INFO =  "info".intern() ;
  final public static String FIELD_ANSWER = "answer".intern() ;
  //final public static String FIELD_
  private Map<String, List<ActionData>> actionField_ = new HashMap<String, List<ActionData>>() ;
  private UIPageIterator uiPageIterator_ ;
  
  public UIEventShareTab(String id) throws Exception {
    super(id);
    setComponentConfig(getClass(), null) ;
    uiPageIterator_ = new UIPageIterator() ;
    uiPageIterator_.setId("UIParticipantList") ;
  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }
  
  public UIFormRadioBoxInput getUIFormRadioBoxInput (String id) {
    return findComponentById(id);
  }
  public Map<String, String> getParticipantStatus() {
    return ((UIEventForm) getParent()).participantStatus_ ;
  }
  public void setActionField(String fieldName, List<ActionData> actions) throws Exception {
    actionField_.put(fieldName, actions) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
  
  public UIPageIterator  getUIPageIterator() {  return uiPageIterator_ ; }
  public long getAvailablePage(){ return uiPageIterator_.getAvailablePage() ;}
  public long getCurrentPage() { return uiPageIterator_.getCurrentPage();}
  public void setContactList(List<ContactData> contactList) throws Exception {
    ObjectPageList objPageList = new ObjectPageList(contactList, 10) ;
    uiPageIterator_.setPageList(objPageList) ;
  }
 
}
