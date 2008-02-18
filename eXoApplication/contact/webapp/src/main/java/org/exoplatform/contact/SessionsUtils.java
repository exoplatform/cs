/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.services.jcr.ext.app.SessionProviderService;
import org.exoplatform.services.jcr.ext.common.SessionProvider;

public class SessionsUtils {

	public static boolean isAnonim() {
		String userId = Util.getPortalRequestContext().getRemoteUser();
		if (userId == null)
			return true;
		return false;
	}

	public static SessionProvider getSystemProvider() {
		return SessionProvider.createSystemProvider();
	}

	public static SessionProvider getSessionProvider() {
		SessionProviderService service = (SessionProviderService) PortalContainer
				.getComponent(SessionProviderService.class);
		return service.getSessionProvider(null);
	}

	public static SessionProvider getAnonimProvider() {
		return SessionProvider.createAnonimProvider();
	}	

}
