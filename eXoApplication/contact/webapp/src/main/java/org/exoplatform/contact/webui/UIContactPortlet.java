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
package org.exoplatform.contact.webui;

import javax.portlet.PortletPreferences;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.contact.service.Utils;
import org.exoplatform.contact.webui.popup.UIPopupAction;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.space.SpaceUtils;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.space.spi.SpaceService;
import org.exoplatform.webui.application.WebuiApplication;
import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.application.portlet.PortletRequestContext;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.UIPopupWindow;
import org.exoplatform.webui.core.UIPortletApplication;
import org.exoplatform.webui.core.lifecycle.UIApplicationLifecycle;
import org.exoplatform.ws.frameworks.cometd.ContinuationService;
import org.mortbay.cometd.AbstractBayeux;
import org.mortbay.cometd.continuation.EXoContinuationBayeux;

/**
 * Author : Nguyen Quang Hung 
 *          hung.nguyen@exoplatform.com 
 * Aug 01, 2007
 */
@ComponentConfig(
    lifecycle = UIApplicationLifecycle.class, 
    template = "app:/templates/contact/webui/UIContactPortlet.gtmpl"
)
public class UIContactPortlet extends UIPortletApplication {

  public UIContactPortlet() throws Exception {
    // addChild(UIBannerContainer.class, null, null) ;
    addChild(UIActionBar.class, null, null);
    addChild(UIWorkingContainer.class, null, null);
    UIPopupAction uiPopupAction = addChild(UIPopupAction.class, null, null);
    uiPopupAction.setId("UIContactPopupAction");
    UIPopupWindow uiPopupWindow = uiPopupAction.getChild(UIPopupWindow.class);
    uiPopupWindow.setId("UIContactPopupWindow");
  }

  public void processRender(WebuiApplication app, WebuiRequestContext context) throws Exception {
    if (ContactUtils.isEmpty(context.getRequestParameter(OBJECTID)) && !context.getParentAppRequestContext().useAjax()) {
      //render by group of space
      processByGroupInSpace((PortletRequestContext) context);
    }
    super.processRender(app, context);
  }
  
  private void processByGroupInSpace(PortletRequestContext pcontext) throws Exception {
    try {
      PortletPreferences pref = pcontext.getRequest().getPreferences();
      String url;
      UIAddressBooks addressBooks = findFirstComponentOfType(UIAddressBooks.class);
      if ((url = pref.getValue(SpaceUtils.SPACE_URL, null)) != null) {
        SpaceService sService = (SpaceService) getApplicationComponent(SpaceService.class);
        Space space = sService.getSpaceByUrl(url);
        String groupId = Utils.ADDRESSBOOK_ID_PREFIX + space.getPrettyName();
        addressBooks.processSelectGroup(pcontext, groupId);
      }
    } catch (Exception e) {
      log.debug("Failed to rendering portlet by group in space", e);
    }
  }

  public void cancelAction() throws Exception {
    WebuiRequestContext context = WebuiRequestContext.getCurrentInstance();
    UIPopupAction popupAction = getChild(UIPopupAction.class);
    popupAction.deActivate();
    context.addUIComponentToUpdateByAjax(popupAction);
  }

  protected ContinuationService getContinuationService() {
    ContinuationService continuation = (ContinuationService) PortalContainer.getInstance()
                                                                            .getComponentInstanceOfType(ContinuationService.class);
    return continuation;
  }

  public String getUserToken() throws Exception {
    ContinuationService continuation = getContinuationService();

    try {
      return continuation.getUserToken(ContactUtils.getCurrentUser());
    } catch (Exception e) {
      return "";
    }
  }
  
  protected String getCometdContextName() {
    EXoContinuationBayeux bayeux = (EXoContinuationBayeux) PortalContainer.getInstance()
                                                                          .getComponentInstanceOfType(AbstractBayeux.class);
    return (bayeux == null ? "cometd" : bayeux.getCometdContextName());
  }

  public String getRestContextName() {
    return PortalContainer.getInstance().getRestContextName();
  }
}
