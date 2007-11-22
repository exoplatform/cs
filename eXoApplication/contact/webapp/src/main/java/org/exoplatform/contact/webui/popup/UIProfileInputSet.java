/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.contact.webui.popup;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.exoplatform.contact.ContactUtils;
import org.exoplatform.download.DownloadService;
import org.exoplatform.download.InputStreamDownloadResource;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.form.UIFormDateTimeInput;
import org.exoplatform.webui.form.UIFormInputInfo;
import org.exoplatform.webui.form.UIFormInputWithActions;
import org.exoplatform.webui.form.UIFormRadioBoxInput;
import org.exoplatform.webui.form.UIFormSelectBox;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.validator.DateTimeValidator;
import org.exoplatform.webui.form.validator.EmailAddressValidator;

/**
 * Created by The eXo Platform SARL
 * Author : Tuan Nguyen
 *          tuan.nguyen@exoplatform.com
 * Aug 24, 2007  
 */
@ComponentConfig(
    template = "app:/templates/contact/webui/popup/UIProfileInputSet.gtmpl"
)
public class UIProfileInputSet extends UIFormInputWithActions {
  private static final String FIELD_FULLNAME_INPUT = "fullName";
  private static final String FIELD_FIRSTNAME_INPUT = "firstName";
  private static final String FIELD_MIDDLENAME_INPUT = "middleName";
  private static final String FIELD_LASTNAME_INPUT = "lastName";
  private static final String FIELD_NICKNAME_INPUT = "nickName";
  private static final String FIELD_GENDER_BOX = "gender" ;
  private static final String INFO_BIRTHDAY= "birthday" ;
  private static final String FIELD_MONTH = "month" ;
  private static final String FIELD_DAY = "day" ;
  private static final String FIELD_YEAR = "year" ; 
//  public static final String[] months = { "January", "February", "March", "April", "May", "June",
//    "July", "August", "September", "October", "November", "December" } ;
  private static final String FIELD_JOBTITLE_INPUT = "jobTitle";
  private static final String FIELD_EMAIL_INPUT = "preferredEmail" ;
  private static final String MALE = "male" ;
  private static final String FEMALE = "female" ;
  private String gender = "male" ;
  private byte[] imageBytes = null;
  private String fileName = null ;
  private String imageMimeType = null ;
  
  public UIProfileInputSet(String id) throws Exception {
    super(id) ;
    setComponentConfig(getClass(), null) ;  
    addUIFormInput(new UIFormStringInput(FIELD_FULLNAME_INPUT, FIELD_FULLNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_FIRSTNAME_INPUT, FIELD_FIRSTNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_MIDDLENAME_INPUT, FIELD_MIDDLENAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_LASTNAME_INPUT, FIELD_LASTNAME_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_NICKNAME_INPUT, FIELD_NICKNAME_INPUT, null));
    List<SelectItemOption<String>> genderOptions = new ArrayList<SelectItemOption<String>>() ;
    genderOptions.add(new SelectItemOption<String>(MALE, MALE));
    genderOptions.add(new SelectItemOption<String>(FEMALE, FEMALE));
    addUIFormInput(new UIFormRadioBoxInput(FIELD_GENDER_BOX, FIELD_GENDER_BOX, genderOptions));    
    
    addUIFormInput(new UIFormInputInfo(INFO_BIRTHDAY, INFO_BIRTHDAY, null)) ;
    List<SelectItemOption<String>> monthOptions = new ArrayList<SelectItemOption<String>>() ;
    for (int i = 1; i < 13; i ++) {
      String month = i + "" ;
      monthOptions.add(new SelectItemOption<String>(month, month)) ;
    }
    addUIFormInput(new UIFormSelectBox(FIELD_MONTH, FIELD_MONTH, monthOptions).setValue("7")) ;
    
    List<SelectItemOption<String>> datesOptions = new ArrayList<SelectItemOption<String>>() ;
    for (int i = 1; i < 32; i ++) {
      String date = i + "" ;
      datesOptions.add(new SelectItemOption<String>(date, date)) ;
    }
    addUIFormInput(new UIFormSelectBox(FIELD_DAY, FIELD_DAY, datesOptions).setValue("15")) ;
  
    String date = ContactUtils.formatDate("dd/MM/yyyy", new Date()) ;
    String strDate = date.substring(date.lastIndexOf("/") + 1, date.length()) ; 
    int thisYear = Integer.parseInt(strDate) ;
    
    List<SelectItemOption<String>> yearOptions = new ArrayList<SelectItemOption<String>>() ;
    for (int i = 1900; i <= thisYear; i ++) {
      String year = i + "" ;
      yearOptions.add(new SelectItemOption<String>(year, year)) ;
    }
    addUIFormInput(new UIFormSelectBox(FIELD_YEAR, FIELD_YEAR, yearOptions).setValue("1980")) ;
    
//    addUIFormInput(new UIFormDateTimeInput(FIELD_BIRTHDAY_DATETIME, FIELD_BIRTHDAY_DATETIME, new Date(), false)
//      .addValidator(DateTimeValidator.class));
    
    addUIFormInput(new UIFormStringInput(FIELD_JOBTITLE_INPUT, FIELD_JOBTITLE_INPUT, null));
    addUIFormInput(new UIFormStringInput(FIELD_EMAIL_INPUT, FIELD_EMAIL_INPUT, null)
      .addValidator(EmailAddressValidator.class));
  }  
  protected String getFieldFullName() { return getUIStringInput(FIELD_FULLNAME_INPUT).getValue() ; }
  protected void setFieldFullName(String s) { getUIStringInput(FIELD_FULLNAME_INPUT).setValue(s); }
  
  protected String getFieldFirstName() { return getUIStringInput(FIELD_FIRSTNAME_INPUT).getValue() ; }
  protected void setFieldFirstName(String s) { getUIStringInput(FIELD_FIRSTNAME_INPUT).setValue(s); }
  
  protected String getFieldMiddleName() { return getUIStringInput(FIELD_MIDDLENAME_INPUT).getValue() ; }
  protected void setFieldMiddleName(String s) { getUIStringInput(FIELD_MIDDLENAME_INPUT).setValue(s); }
  
  protected String getFieldLastName() { return getUIStringInput(FIELD_LASTNAME_INPUT).getValue() ; }
  protected void setFieldLastName(String s) { getUIStringInput(FIELD_LASTNAME_INPUT).setValue(s); }
  
  protected String getFieldNickName() { return getUIStringInput(FIELD_NICKNAME_INPUT).getValue() ; }
  protected void setFieldNickName(String s) { getUIStringInput(FIELD_NICKNAME_INPUT).setValue(s); }
  
  protected String getFieldGender() { return getChild(UIFormRadioBoxInput.class).getValue() ; }
  protected void setFieldGender(String s) { gender = s ; }
  
  protected Date getFieldBirthday() throws Exception {
    int day = Integer.parseInt(getUIFormSelectBox(FIELD_DAY).getValue()) ;
    int month = Integer.parseInt(getUIFormSelectBox(FIELD_MONTH).getValue()) ;
    int year = Integer.parseInt(getUIFormSelectBox(FIELD_YEAR).getValue()) ;
    GregorianCalendar cal = new GregorianCalendar(year, month - 1, day) ;
    String strDate = ContactUtils.formatDate("MM/dd/yyyy", cal.getTime());
    SimpleDateFormat date = new SimpleDateFormat("MM/dd/yyyy") ;
    return date.parse(strDate) ; 
  }
  protected void setFieldBirthday(Date d) throws Exception {
    String strDate = ContactUtils.formatDate("MM/dd/yyyy", d);    
    int month = Integer.parseInt(strDate.substring(0, strDate.indexOf("/"))) ;
    System.out.println("\n\n month:" + month + "\n\n");
    
    int day = Integer.parseInt(strDate.substring(strDate.indexOf("/") + 1, strDate.lastIndexOf("/"))) ;
    String year = strDate.substring(strDate.lastIndexOf("/") + 1, strDate.length()) ;
    getUIFormSelectBox(FIELD_MONTH).setValue(month + "") ;
    getUIFormSelectBox(FIELD_DAY).setValue(day + "") ;
    getUIFormSelectBox(FIELD_YEAR).setValue(year) ;
  }
  
  protected String getFieldJobName() { return getUIStringInput(FIELD_JOBTITLE_INPUT).getValue() ; }
  protected void setFieldJobName(String s) { getUIStringInput(FIELD_JOBTITLE_INPUT).setValue(s); }
  
  protected String getFieldEmail() { return getUIStringInput(FIELD_EMAIL_INPUT).getValue(); }
  protected void setFieldEmail(String s) { getUIStringInput(FIELD_EMAIL_INPUT).setValue(s); }
 
  protected void setImage(InputStream input) throws Exception{
    if (input != null) {
      imageBytes = new byte[input.available()] ; 
      input.read(imageBytes) ;
    }
    else imageBytes = null ;
  }
  protected byte[] getImage() {return imageBytes ;}
  
  protected String getMimeType() { return imageMimeType ;} ;
  protected void setMimeType(String mimeType) {imageMimeType = mimeType ;} 
  
  protected void setFileName(String name) { fileName = name ; }
  protected String getFileName() {return fileName ;} ;
  
  protected String getImageSource() throws Exception {    
    if(imageBytes == null || imageBytes.length == 0) return null;
    ByteArrayInputStream byteImage = new ByteArrayInputStream(imageBytes) ;    
    DownloadService dservice = getApplicationComponent(DownloadService.class) ;
    InputStreamDownloadResource dresource = new InputStreamDownloadResource(byteImage, "image") ;
    dresource.setDownloadName(fileName) ;
    return  dservice.getDownloadLink(dservice.addDownloadResource(dresource)) ;
  }
  
}

