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

import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormStringInput;

/**
 * Created by The eXo Platform SARL
 * Author : Uoc Nguyen
 *          uocnb.apt.vn@exoplatform.com
 * Dec 3, 2007  
 */
@ComponentConfig(
    template = "app:/templates/contact/webui/popup/UIIMContactInputSet.gtmpl"
)
public class UIIMContactInputSet extends UIFormInputWithActions {
  
  public static String FIELD_EXOCHAT_INPUT = "exoChat";
  public static String FIELD_GOOGLE_INPUT = "google";
  public static String FIELD_MSN_INPUT = "msn";
  public static String FIELD_AOLAIM_INPUT = "aolAim";
  public static String FIELD_YAHOO_INPUT = "yahoo";
  public static String FIELD_IRC_INPUT = "irc";
  public static String FIELD_SKYPE_INPUT = "skype";
  public static String FIELD_ICQ_INPUT = "icq";

  public UIIMContactInputSet(String id) throws Exception {
    super(id) ;  
    setComponentConfig(getClass(), null) ;  
    
    addUIFormInput(new UIFormStringInput(FIELD_EXOCHAT_INPUT, FIELD_EXOCHAT_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_GOOGLE_INPUT, FIELD_GOOGLE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_MSN_INPUT, FIELD_MSN_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_AOLAIM_INPUT, FIELD_AOLAIM_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_YAHOO_INPUT, FIELD_YAHOO_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_IRC_INPUT, FIELD_IRC_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_SKYPE_INPUT, FIELD_SKYPE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_ICQ_INPUT, FIELD_ICQ_INPUT, null));
  }
}


