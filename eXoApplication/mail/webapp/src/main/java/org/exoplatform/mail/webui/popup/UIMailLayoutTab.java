/*
 * Copyright (C) 2003-2009 eXo Platform SAS.
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
package org.exoplatform.mail.webui.popup;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormRadioBoxInput;

/**
 * Created by The eXo Platform SAS
 * Author : eXoPlatform
 *          exo@exoplatform.com
 * May 4, 2009  
 */

@ComponentConfig(
  template = "app:/templates/mail/webui/popup/UIMailLayoutTab.gtmpl"
) 
              
              
public class UIMailLayoutTab extends UIFormInputSet {

  public static final String VERTICAL_LAYOUT = "UIMailSettings-VerticalLayout";
  public static final String HORIZONTAL_LAYOUT= "UIMailSettings-HorizontalLayout";
  public static final String NOSPLIT_LAYOUT = "UIMailSettings-NosplitLayout";
  public static final String VERTICAL_LAYOUT_VALUE = "vertical-layout";
  public static final String HORIZONTAL_LAYOUT_VALUE = "horizontal-layout";
  public static final String NO_SPLIT_LAYOUT_VALUE = "nosplit-layout";
  public static final String RADIO_ID = "group-id";
  
  
  public UIMailLayoutTab(String id) throws Exception {
    super(id);
    setComponentConfig(getClass(), null) ;
    List<SelectItemOption<String>> layoutOption = new ArrayList<SelectItemOption<String>>();
    layoutOption.add(new SelectItemOption<String>(VERTICAL_LAYOUT_VALUE, VERTICAL_LAYOUT_VALUE));
    UIFormRadioBoxInput uiFormRadioBoxInput=new UIFormRadioBoxInput(RADIO_ID, "", layoutOption);
    uiFormRadioBoxInput.setId(VERTICAL_LAYOUT);
    addUIFormInput(uiFormRadioBoxInput);
    
    layoutOption = new ArrayList<SelectItemOption<String>>();
    layoutOption.add(new SelectItemOption<String>(NO_SPLIT_LAYOUT_VALUE, NO_SPLIT_LAYOUT_VALUE));
    uiFormRadioBoxInput=new UIFormRadioBoxInput(RADIO_ID, "", layoutOption);
    uiFormRadioBoxInput.setId(NOSPLIT_LAYOUT);
    addUIFormInput(uiFormRadioBoxInput);
    
    layoutOption = new ArrayList<SelectItemOption<String>>();
    layoutOption.add(new SelectItemOption<String>(HORIZONTAL_LAYOUT_VALUE, HORIZONTAL_LAYOUT_VALUE));
    uiFormRadioBoxInput=new UIFormRadioBoxInput(RADIO_ID, "", layoutOption);
    uiFormRadioBoxInput.setId(HORIZONTAL_LAYOUT);
    addUIFormInput(uiFormRadioBoxInput);
  }
  
 
  protected UIForm getParentFrom() { return (UIForm)getParent(); }
}
