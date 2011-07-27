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
package org.exoplatform.mail.service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PropertyType;
import javax.jcr.Value;
import javax.jcr.ValueFactory;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.services.jcr.RepositoryService;

/**
 * Created by The eXo Platform SAS
 * Author : Phung Hai Nam
 *          phunghainam@gmail.com
 * Dec 1, 2008  
 */
public class MailUpdateStorage extends MailUpdateStorageEventListener {
  private MailService       service_;

  private RepositoryService repositorySerivce_;

  CsObjectParam             csObj_;

  public MailUpdateStorage(MailService service, InitParams params, RepositoryService repositorySerivce) throws Exception {
    service_ = service;
    repositorySerivce_ = repositorySerivce;
    csObj_ = (CsObjectParam) params.getObjectParam("cs.mail.update.object").getObject();
  }

  public void preUpdate() {
    /*
     * if(csObj_ != null) try { SessionProvider sessionProvider = SessionProvider.createSystemProvider() ; String wsName =
     * repositorySerivce_.getCurrentRepository().getConfiguration().getDefaultWorkspaceName(); Session session =
     * sessionProvider.getSession(wsName, repositorySerivce_.getCurrentRepository()); QueryManager qm =
     * session.getWorkspace().getQueryManager() ; for(CsNodeTypeMapping nt : csObj_.getNodeTypes()) { ExtendedNodeType nodeType =
     * (ExtendedNodeType)repositorySerivce_.getCurrentRepository().getNodeTypeManager().getNodeType(nt.getNodeTypeName()) ; //
     * update added properties if(nt.getAddedProperties() != null && nt.getAddedProperties().size() > 0) { StringBuffer sql = new
     * StringBuffer("/jcr:root //element(*," + nodeType.getName() + ")") ;
     *  Query query = qm.createQuery(sql.toString(),
     * Query.XPATH) ; for(CsPropertyMapping addedProperty : nt.getAddedProperties()) { try{ PropertyDefinition pDef =
     * nodeType.getPropertyDefinitions(addedProperty.getPropertyName()).getAnyDefinition() ; if( pDef != null) { String value =
     * null ; Value[] vls = pDef.getDefaultValues() ; if(pDef.isAutoCreated() || pDef.isMandatory()) { if(vls != null &&
     * vls.length > 0) { QueryResult result = query.execute(); NodeIterator it = result.getNodes();
     * setValue(pDef.getRequiredType(), it, addedProperty.getPropertyName(), vls) ; }else { if(pDef.isMandatory()) { // Must set
     * value if(addedProperty.getDefaultValue() != null && addedProperty.getDefaultValue().trim().length() > 0){ value =
     * addedProperty.getDefaultValue() ; QueryResult result = query.execute(); NodeIterator it = result.getNodes();
     * setValue(pDef.getRequiredType(),pDef.isMultiple(), it, addedProperty.getPropertyName(), value) ; } }else { // set value if
     * configurated value = addedProperty.getDefaultValue() ; QueryResult result = query.execute(); NodeIterator it =
     * result.getNodes(); setValue(pDef.getRequiredType(),pDef.isMultiple(), it, addedProperty.getPropertyName(), value); } } } }
     * }catch(Exception e) {  } } } //update removed properties if(nt.getRemovedProperties() != null &&
     * nt.getRemovedProperties().size() > 0) { StringBuffer sql = new StringBuffer("/jcr:root //element(*," + nodeType.getName() +
     * ")") ;  Query query = qm.createQuery(sql.toString(),
     * Query.XPATH) ; for(CsPropertyMapping removed : nt.getAddedProperties()) { try{ PropertyDefinition pDef =
     * nodeType.getPropertyDefinitions(removed.getPropertyName()).getAnyDefinition() ; if( pDef != null) { QueryResult result =
     * query.execute(); NodeIterator it = result.getNodes(); while (it.hasNext()) { Node node = it.nextNode() ;
     * node.getProperty(removed.getPropertyName()).remove() ; node.save() ; } } }catch(Exception e) {} } }
     * //update update properties if(nt.getUpdatedProperties() != null && nt.getUpdatedProperties().size() > 0) { StringBuffer sql
     * = new StringBuffer("/jcr:root //element(*," + nodeType.getName() + ")") ;
     *  Query query = qm.createQuery(sql.toString(),
     * Query.XPATH) ; QueryResult result = query.execute(); NodeIterator it = result.getNodes(); for(CsPropertyMapping updated :
     * nt.getAddedProperties()) { try{ PropertyDefinition pDef =
     * nodeType.getPropertyDefinitions(updated.getPropertyName()).getAnyDefinition() ; if( pDef != null) {
     * updateValue(pDef.getRequiredType(), pDef.isMultiple(), it, updated.getPropertyName(), updated.getReplaceName()) ; }
     * }catch(Exception e) {} } } } } catch (Exception e) {}
     */
  }

  @Override
  public void postUpdate() {
    super.postUpdate();
    // Run update data base
  }
}
