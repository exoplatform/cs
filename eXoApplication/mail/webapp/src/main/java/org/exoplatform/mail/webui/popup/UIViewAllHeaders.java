/*
 * Copyright (C) 2003-2008 eXo Platform SAS.
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
import java.util.Iterator;
import java.util.List;

import org.exoplatform.commons.utils.ObjectPageList;
import org.exoplatform.mail.service.Message;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.UIGrid;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;

/**
 * Created by The eXo Platform SARL
 * Author : Phung Nam
 *          phunghainam@gmail.com
 * Arp 4, 2008  
 */
@ComponentConfig(
    template = "app:/templates/mail/webui/UIGridWithButton.gtmpl",
    events = {
        @EventConfig(listeners = UIViewAllHeaders.CloseActionListener.class)
    }
)
public class UIViewAllHeaders extends UIGrid  implements UIPopupComponent{
  private static String[] BEAN_FIELD = {"Key", "Value"} ;
  private Message msg_ ;
  
  public UIViewAllHeaders() throws Exception { }
  
  public void init(Message msg) throws Exception {
    msg_ = msg ;
    configure("id", BEAN_FIELD, null) ;
    updateGrid() ;
  }

  public void updateGrid() throws Exception {
    List<HeaderData> headers = new ArrayList<HeaderData>() ;
    Iterator<String> iter = msg_.getHeaders().keySet().iterator() ;
    String key ;
    String value ;
    while (iter.hasNext()) {
      key = iter.next() ;
      value = msg_.getHeaders().get(key) ;
      headers.add(new HeaderData(key, value)) ;
    }

    ObjectPageList objPageList = new ObjectPageList(headers, 15) ;
    getUIPageIterator().setPageList(objPageList) ; 
  }

  public void activate() throws Exception { }
  public void deActivate() throws Exception { }
  public String[] getButtons(){ return new String[] {"Close"} ; }
  
  public class HeaderData {
    String key_ ;
    String value_ ;

    public HeaderData(String key, String value){
      key_ = key ;
      value_ = value ;
    }
    
    public String getKey() {return key_ ;}
    public String getValue () {return value_ ;}
  }

  static  public class CloseActionListener extends EventListener<UIViewAllHeaders> {
    public void execute(Event<UIViewAllHeaders> event) throws Exception {
      UIPopupAction uiPopup = event.getSource().getAncestorOfType(UIPopupAction.class);
      uiPopup.deActivate() ;
      event.getRequestContext().addUIComponentToUpdateByAjax(uiPopup) ;
    }
  }
}

