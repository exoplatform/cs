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
package org.exoplatform.services.xmpp.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.exoplatform.services.xmpp.bean.ContactBean;
import org.exoplatform.services.xmpp.bean.FieldBean;
import org.exoplatform.services.xmpp.bean.FormBean;
import org.exoplatform.services.xmpp.bean.HostedRoomBean;
import org.exoplatform.services.xmpp.bean.MessageBean;
import org.exoplatform.services.xmpp.bean.PresenceBean;
import org.exoplatform.services.xmpp.bean.SearchResultsBean;
import org.exoplatform.services.xmpp.history.HistoricalMessage;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterGroup;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.muc.HostedRoom;

/**
 * Created by The eXo Platform SAS.
 * 
 * @author <a href="mailto:vitaly.parfonov@gmail.com">Vitaly Parfonov</a>
 * @version $Id: $
 */
public class TransformUtils {

  public static PresenceBean presenceToBean(Presence presence) {
    PresenceBean presenceBean = new PresenceBean();
    presenceBean.setFrom(presence.getFrom());
    try {
      presenceBean.setMode(presence.getMode().name());
    } catch (Exception e) {
      presenceBean.setMode(null);
    }
    try {
      presenceBean.setType(presence.getType().name());
    } catch (Exception e) {
      presenceBean.setType(null);
    }
    return presenceBean;
  }

  public static List<ContactBean> rosterToRosterBean(Roster roster) {
    Collection<RosterEntry> collection = roster.getEntries();
    List<ContactBean> list = new ArrayList<ContactBean>();
    for (RosterEntry re : collection) {
      ContactBean contactBean = new ContactBean();
      contactBean.setNickname(re.getName());
      if (re.getStatus() == RosterPacket.ItemStatus.SUBSCRIPTION_PENDING)
        contactBean.setSubscriptionStatus("SUBSCRIPTION_PENDING");
      if (re.getStatus() == RosterPacket.ItemStatus.UNSUBCRIPTION_PENDING)
        contactBean.setSubscriptionStatus("UNSUBSCRIPTION_PENDING");
      if (re.getType() != null)
        contactBean.setSubscriptionType(re.getType().name());
      Collection<RosterGroup> collectionGroup = re.getGroups();
      List<String> groupNames = new ArrayList<String>();
      for (RosterGroup group : collectionGroup) {
        groupNames.add(group.getName());
      }
      contactBean.setGroups(groupNames);
      contactBean.setUser(re.getUser());
      contactBean.setPresence(presenceToBean(roster.getPresence(re.getUser())));
      Iterator<Presence> iterator = roster.getPresences(re.getUser());
      list.add(contactBean);
    }
    return list;
  }

  

  public static MessageBean messageToBean(Message message) {
    return new MessageBean(message.getPacketID(),
                           message.getFrom(),
                           message.getTo(),
                           message.getType().name(),
                           message.getBody());
  }

  public static MessageBean messageToBean(HistoricalMessage message) {
    MessageBean messageBean = new MessageBean(message.getId(),
                                              message.getFrom(),
                                              message.getTo(),
                                              message.getType(),
                                              message.getBody());
    messageBean.setDateSend(message.getDateSend().toString());
    return messageBean;
  }

  public static FormBean formToFormBean(Form form) {
    FormBean formBean = new FormBean();
    Iterator<FormField> iterator = form.getDataFormToSend().getFields();
    List<FieldBean> fieldBeans = new ArrayList<FieldBean>();
    while (iterator.hasNext()) {
      FormField formField = (FormField) iterator.next();
      fieldBeans.add(fieldToFieldBean(formField));
    }
    formBean.setFields(fieldBeans);
    formBean.setInstructions(form.getInstructions());
    formBean.setTitle(form.getTitle());
    formBean.setType(form.getType());
    return formBean;
  }

  public static FieldBean fieldToFieldBean(FormField formField) {
    FieldBean fieldBean = new FieldBean();
    List<String> values = new ArrayList<String>();
    Iterator<String> iterator = formField.getValues();
    while (iterator.hasNext()) {
      String value = (String) iterator.next();
      values.add(value);
    }
    fieldBean.setValues(values);
    fieldBean.setVariable(formField.getVariable());
    fieldBean.setDescription(formField.getDescription());
    fieldBean.setLabel(formField.getLabel());
    fieldBean.setType(formField.getType());
    return fieldBean;
  }

  public static HostedRoomBean hostedRoomToBean(HostedRoom hostedRoom) {
    return new HostedRoomBean(hostedRoom.getJid(), hostedRoom.getName());
  }

  public static List<HostedRoomBean> hostedRoomsToBeansCollection(Collection<HostedRoom> collection) {
    List<HostedRoomBean> rooms = new ArrayList<HostedRoomBean>();
    for (HostedRoom hostedRoom : collection) {
      rooms.add(hostedRoomToBean(hostedRoom));
    }
    return rooms;
  }

  public static SearchResultsBean reportedSateToSearchResultsBean(ReportedData reportedData) {
    List<Map<String, List<String>>> list = new ArrayList<Map<String, List<String>>>();
    Iterator<ReportedData.Row> reportedDataRows = reportedData.getRows();
    while (reportedDataRows.hasNext()) {
      ReportedData.Row reportedDataRow = reportedDataRows.next();
      Map<String, List<String>> map = new HashMap<String, List<String>>();
      Iterator<ReportedData.Column> reportedDataColumns = reportedData.getColumns();
      while (reportedDataColumns.hasNext()) {
        String columnName = reportedDataColumns.next().getVariable();
        Iterator<String> reportedDataFields = reportedDataRow.getValues(columnName);
        List<String> listValue = new ArrayList<String>();
        while (reportedDataFields.hasNext()) {
          listValue.add(reportedDataFields.next());
        }
        map.put(columnName.toLowerCase(), listValue);
      }
      list.add(map);
    }
    return new SearchResultsBean(list);
  }
  
  
  public static FormBean changeFieldForm(FormBean form, String variable, List<String> values){
    List<FieldBean> list = form.getFields();
    List<FieldBean> lfb = new ArrayList<FieldBean>();
    for (FieldBean fb : list) {
      if (fb.getVariable() != null){
        if (fb.getVariable().trim().equals(variable)){
          fb.setValues(values);
        }
      }
      lfb.add(fb);
    }
    form.setFields(lfb);
    return form;
  }


}
