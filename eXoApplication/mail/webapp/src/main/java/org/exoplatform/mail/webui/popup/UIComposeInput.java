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
package org.exoplatform.mail.webui.popup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.form.UIForm;
import org.exoplatform.webui.form.UIFormInputWithActions;

/**
 * Created by The eXo Platform SARL
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 *          Nam Phung
 *          phunghainam@gmail.com
 * Aug 29, 2007  
 */

@ComponentConfig(
    template = "app:/templates/mail/webui/popup/UIComposeInput.gtmpl"
) 
public class UIComposeInput extends UIFormInputWithActions {
  private Map<String, List<ActionData>> actionField_ ;
  
  private boolean showCc_ = false ;
  private boolean showBcc_ = false ;
  
  public UIComposeInput(String id) throws Exception {
    super(id);
    setComponentConfig(getClass(), null) ;
    actionField_ = new HashMap<String, List<ActionData>>() ;
  }
  protected UIForm getParentFrom() {
    return (UIForm)getParent() ;
  }

  
  @Override
  public void processRender(WebuiRequestContext arg0) throws Exception {
    super.processRender(arg0);
  }
  public void setShowCc(boolean showCc_) {
    this.showCc_ = showCc_;
  }
  public boolean isShowCc() {
    return showCc_;
  }
  public void setShowBcc(boolean showBcc_) {
    this.showBcc_ = showBcc_;
  }
  public boolean isShowBcc() {
    return showBcc_;
  }
  public void setActionField(String fieldName, List<ActionData> actions) throws Exception {
    actionField_.put(fieldName, actions) ;
  }
  public List<ActionData> getActionField(String fieldName) {return actionField_.get(fieldName) ;}
}
