/**
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

package org.exoplatform.services.organization.rest;

import org.exoplatform.container.StandaloneContainer;
import org.exoplatform.services.rest.impl.ApplicationContextImpl;
import org.exoplatform.services.rest.impl.RequestHandlerImpl;
import org.exoplatform.services.rest.impl.ResourceBinder;
import org.exoplatform.services.rest.impl.ProviderBinder;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public abstract class BaseTest extends TestCase {

  protected StandaloneContainer container;
  
  protected ProviderBinder providers;

  protected ResourceBinder     binder;

  protected RequestHandlerImpl requestHandler;

  public void setUp() throws Exception {
    StandaloneContainer.setConfigurationPath("src/test/java/conf/standalone/test-configuration.xml");
    container = StandaloneContainer.getInstance();
    binder = (ResourceBinder) container.getComponentInstanceOfType(ResourceBinder.class);
    requestHandler = (RequestHandlerImpl) container.getComponentInstanceOfType(RequestHandlerImpl.class);
    ProviderBinder.setInstance(new ProviderBinder());
    providers = ProviderBinder.getInstance();
//    System.out.println("##########################"+providers);
    ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providers));
    binder.clear();
  }
  
  public void tearDown() throws Exception {
  }

  public boolean registry(Object resource) throws Exception {
//    container.registerComponentInstance(resource);
    return binder.bind(resource);
  }

  public boolean registry(Class<?> resourceClass) throws Exception {
//    container.registerComponentImplementation(resourceClass.getName(), resourceClass);
    return binder.bind(resourceClass);
  }

  public boolean unregistry(Object resource) {
//    container.unregisterComponentByInstance(resource);
    return binder.unbind(resource.getClass());
  }

  public boolean unregistry(Class<?> resourceClass) {
//    container.unregisterComponent(resourceClass.getName());
    return binder.unbind(resourceClass);
  }

}
