/**
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

package org.exoplatform.services.organization.rest.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.User;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * Build XML document that content information about group (name of group,
 * description and user who are in that group)
 * 
 * @author <a href="mailto:vitalka_p@ukr.net">Vitaly Parfonov</a>
 * @version $Id: $
 */

public class GroupXMLEntity implements SerializableEntity {

  private final Collection<User> userList_;

  private final Group            group_;

  private final String           baseURI_;

  public GroupXMLEntity(Group group, Collection<User> userList, String baseURI) {
    userList_ = userList;
    group_ = group;
    baseURI_ = baseURI;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.exoplatform.services.rest.transformer.SerializableEntity#writeObject(java.io.OutputStream)
   */
  public void writeObject(OutputStream out) throws IOException {
    try {
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
      outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
      XMLStreamWriter xsw = outputFactory.createXMLStreamWriter(out);
      xsw.writeStartDocument();
      xsw.writeStartElement("group");
      xsw.writeDefaultNamespace(XMLContants.EXO_NAMESPACE_URL);
      xsw.writeNamespace(XMLContants.XLINK_PREFIX, XMLContants.XLINK_NAMESPACE_URL);
      xsw.writeAttribute(XMLContants.XLINK_NAMESPACE_URL, XMLContants.XLINK_HREF, baseURI_
          + "/organization/group/" + "?output=xml&command=info&groupId=" + group_.getId().replaceFirst("/", ""));
      xsw.writeStartElement("id");
      xsw.writeCharacters(group_.getId());
      xsw.writeEndElement();
      xsw.writeStartElement("name");
      xsw.writeCharacters(group_.getGroupName());
      xsw.writeEndElement();
      xsw.writeStartElement("label");
      xsw.writeCharacters((group_.getLabel() != null) ? group_.getLabel() : "");
      xsw.writeEndElement();
      xsw.writeStartElement("description");
      xsw.writeCharacters((group_.getDescription() != null) ? group_.getDescription() : "");
      xsw.writeEndElement();
      xsw.writeStartElement("members");
      xsw.writeNamespace(XMLContants.XLINK_PREFIX, XMLContants.XLINK_NAMESPACE_URL);
      for (User u : userList_) {
        xsw.writeStartElement("user");
        xsw.writeAttribute(XMLContants.XLINK_NAMESPACE_URL, XMLContants.XLINK_HREF, baseURI_
            + "/organization/user/" /*
                                                                 * +
                                                                 * u.getUserName()
                                                                 */
            + "?output=xml&command=info");
        xsw.writeCharacters(u.getUserName());
        xsw.writeEndElement();
      }
      xsw.writeEndElement();
      xsw.writeEndElement();
      xsw.writeEndDocument();
      xsw.flush();
      xsw.close();
    } catch (Exception e) {
      e.printStackTrace();
      throw new IOException(e.getMessage());
    }
  }

}
