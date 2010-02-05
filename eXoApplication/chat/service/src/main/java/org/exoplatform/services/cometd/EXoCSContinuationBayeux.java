/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
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
package org.exoplatform.services.cometd;

import javax.servlet.ServletContext;

import org.mortbay.cometd.continuation.EXoContinuationBayeux;

/**
 * Created by The eXo Platform SAS
 * Author : viet.nguyen
 *          vietnt84@gmail.com
 * Feb 4, 2010  
 */
// Important: only using this override when using platform 3.0.0-Beta05
public class EXoCSContinuationBayeux extends EXoContinuationBayeux {

  /**
   * Cometd webapp context name
   */
  private String cometdContextName = "cometd";

  @Override
  protected void initialize(ServletContext context) {
    super.initialize(context);
    cometdContextName = context.getServletContextName();
  }
  
  /**
   * 
   * @return context name of cometd webapp
   */
  public String getCometdContextName() {
    return cometdContextName;
  }
  
}
