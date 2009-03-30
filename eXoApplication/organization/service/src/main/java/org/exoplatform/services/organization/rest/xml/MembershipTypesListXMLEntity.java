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

import org.exoplatform.services.organization.MembershipType;
import org.exoplatform.services.rest.transformer.SerializableEntity;

/**
 * @author <a href="mailto:andrew00x@gmail.com">Andrey Parfonov</a>
 * @version $Id: $
 */
public class MembershipTypesListXMLEntity implements SerializableEntity {
  
  private final Collection<MembershipType> membershipTypes_;
  
  public MembershipTypesListXMLEntity(Collection<MembershipType> membershipTypes) {
    membershipTypes_ = membershipTypes;
  }

  public void writeObject(final OutputStream out) throws IOException {
    try {
      XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
      outputFactory.setProperty(XMLOutputFactory.IS_REPAIRING_NAMESPACES, Boolean.TRUE);
      XMLStreamWriter xsw = outputFactory.createXMLStreamWriter(out);
      xsw.writeStartDocument();
      xsw.writeStartElement("membership-types");
      xsw.writeDefaultNamespace(XMLContants.EXO_NAMESPACE_URL);
      for (MembershipType mt : membershipTypes_) {
        xsw.writeStartElement("membership-type");
        xsw.writeAttribute("name", mt.getName());
        xsw.writeStartElement("owner");
        xsw.writeCharacters(mt.getOwner());
        xsw.writeEndElement();
        xsw.writeStartElement("description");
        xsw.writeCharacters(mt.getDescription());
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

