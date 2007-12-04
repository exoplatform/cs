/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
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


