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

import org.exoplatform.services.organization.Membership;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class MembershipListXMLEntity implements SerializableEntity {
  
  private final Collection<Membership> memberships_;
  private final String baseURI_; 
  private String username_;
  
  public MembershipListXMLEntity(Collection<Membership> memberships, String baseURI) {
    memberships_ = memberships;
    baseURI_ = baseURI;
  }

  public MembershipListXMLEntity(Collection<Membership> memberships, String username, String baseURI) {
    memberships_ = memberships;
    username_ = username;
    baseURI_ = baseURI;
  }

  public void writeObject(OutputStream out) throws IOException {
    try {
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
      outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
      XMLStreamWriter xsw = outputFactory.createXMLStreamWriter(out);
      xsw.writeStartDocument();
      xsw.writeStartElement("memberships");
      xsw.writeDefaultNamespace(XMLContants.EXO_NAMESPACE_URL);
      xsw.writeNamespace(XMLContants.XLINK_PREFIX, XMLContants.XLINK_NAMESPACE_URL);
      if (username_ != null)
        xsw.writeAttribute("user-name", username_);
      for (Membership m : memberships_) {
        xsw.writeStartElement("membership");
        xsw.writeAttribute(XMLContants.XLINK_NAMESPACE_URL, XMLContants.XLINK_HREF,
            baseURI_ + "/organization/membership/" + m.getId() + "/?output=xml&command=info");
        xsw.writeStartElement("id");
        xsw.writeCharacters(m.getId());
        xsw.writeEndElement();
        xsw.writeStartElement("type");
        xsw.writeCharacters(m.getMembershipType());
        xsw.writeEndElement();
        xsw.writeStartElement("group-id");
        xsw.writeCharacters(m.getGroupId());
        xsw.writeEndElement();
        xsw.writeStartElement("user-name");
        xsw.writeCharacters(m.getUserName());
        xsw.writeEndElement();
        xsw.writeEndElement();
      }
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

