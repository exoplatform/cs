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

import org.exoplatform.mail.webui.UIFormInputWithActions;
import org.exoplatform.webui.config.annotation.ComponentConfig;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Nam 
 *          phunghainam@gmail.com
 * Apr 22, 2009  
 */
@ComponentConfig(
  template = "app:/templates/mail/webui/popup/UIFetchOptionsInputSet.gtmpl"
)
public class UIFetchOptionsInputSet extends UIFormInputWithActions {
  
  public UIFetchOptionsInputSet(String id) throws Exception {
    super(id) ;  
    setComponentConfig(getClass(), null) ;  
  }
}

