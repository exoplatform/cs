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
 **/
package org.exoplatform.calendar.webui;

import java.io.Writer;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.exoplatform.webui.application.WebuiRequestContext;
import org.exoplatform.webui.form.UIFormInputBase;

/**
 * Created by The eXo Platform SAS
 * Author : Pham Tuan
 *          tuan.pham@exoplatform.com
 * Feb 29, 2008  
 */
public class UIFormDateTimePicker extends UIFormInputBase<String>  {
  /**
   * The DateFormat
   */
  private DateFormat dateFormat_ ;
  /**
   * Whether to display the full time (with hours, minutes and seconds), not only the date
   */
  
  private String dateStyle_ = "MM/dd/yyyy" ;
  private String timeStyle_ = "HH:mm:ss" ;
  
  private boolean isDisplayTime_ ;
  
  public UIFormDateTimePicker(String name, String bindField, Date date, boolean isDisplayTime) {
    super(name, bindField, String.class) ;
    setDisplayTime(isDisplayTime) ;
    if(date != null) value_ = dateFormat_.format(date) ;
  }
  
  public UIFormDateTimePicker(String name, String bindField, Date date, boolean isDisplayTime, String dateStyle) {
    super(name, bindField, String.class) ;
    dateStyle_ = dateStyle ;
    setDisplayTime(isDisplayTime) ;
    if(date != null) value_ = dateFormat_.format(date) ;
  }
  public UIFormDateTimePicker(String name, String bindField, Date date, boolean isDisplayTime, String dateStyle, String timeStyle) {
    super(name, bindField, String.class) ;
    dateStyle_ = dateStyle ;
    timeStyle_ = timeStyle ;
    setDisplayTime(isDisplayTime) ;
    if(date != null) value_ = dateFormat_.format(date) ;
  }
  public UIFormDateTimePicker(String name, String bindField, Date date) {
    this(name, bindField, date, true) ;
  }
  /**
   * By default, creates a date of format Month/Day/Year
   * If isDisplayTime is true, adds the time of format Hours:Minutes:Seconds
   * TODO : Display time depending on the locale of the client.
   * @param isDisplayTime
   */
  
  public UIFormDateTimePicker(String name, String bindField, Date date, String dateStyle) {
    this(name, bindField, date, true, dateStyle) ;
  }
  public UIFormDateTimePicker(String name, String bindField, Date date, String dateStyle, String timeStyle) {
    this(name, bindField, date, true, dateStyle, timeStyle) ;
  }
  public void setDisplayTime(boolean isDisplayTime) {
    isDisplayTime_ = isDisplayTime;
    if(isDisplayTime_) dateFormat_ = new SimpleDateFormat(dateStyle_ + " " + timeStyle_);
    else dateFormat_ = new SimpleDateFormat(dateStyle_);
  }
  
  public void setCalendar(Calendar date) { 
    dateFormat_ = new SimpleDateFormat(getFullDateTimeFormat());
    value_ = dateFormat_.format(date.getTime()) ; 
   }
  public Calendar getCalendar() {
    try {
      Calendar calendar = new GregorianCalendar() ;
      dateFormat_ = new SimpleDateFormat(getFullDateTimeFormat());
      calendar.setTime(dateFormat_.parse(value_ + " 0:0:0")) ;
      return calendar ;
    } catch (ParseException e) {
      return null;
    }
  }
  public void setDateFormat(String dateStyle) {
    dateStyle_ = dateStyle ;
  }
  public void setTimeFormat(String timeStyle) {
    timeStyle_ = timeStyle ;
  }
  @SuppressWarnings("unused")
  public void decode(Object input, WebuiRequestContext context) throws Exception {
    if(input != null) value_ = ((String)input).trim();
  }
  public String getFullDateTimeFormat() {
    return dateStyle_ + " " + timeStyle_ ;
  }
  
  public void processRender(WebuiRequestContext context) throws Exception {
    context.getJavascriptManager().importJavascript("eXo.cs.UIDateTimePicker","/csResources/javascript/") ;
    Writer w = context.getWriter();
    w.write("<input format='" + getFullDateTimeFormat() + "' type='text' onfocus='eXo.cs.UIDateTimePicker.init(this,") ;
    w.write(String.valueOf(isDisplayTime_));
    w.write(");' onkeyup='eXo.cs.UIDateTimePicker.show();' name='") ;
    w.write(getName()) ; w.write('\'') ;
    if(value_ != null && value_.length() > 0) {      
      w.write(" value='"); w.write(value_.toString()); w.write('\'');
    }
    w.write(" onmousedown='event.cancelBubble = true' />") ;
  }

}
