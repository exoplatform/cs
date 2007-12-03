/***************************************************************************
 * Copyright 2001-2007 The eXo Platform SARL         All rights reserved.  *
 * Please look at license.txt in info directory for more license detail.   *
 **************************************************************************/
package org.exoplatform.mail.service;

/**
 * Created by The eXo Platform SARL
 * Author : Nam Phung
 *          phunghainam@gmail.com
 * Sep 22, 2007  
 */
public class MailSetting {
  
  public static long TEN_MINS = 10;
  public static long TWENTY_MINS = 20;
  public static long THIRTY_MINS = 30;
  public static long ONE_HOUR = 60;
  
  public static String WYSIWYG = "wysiwyg";
  public static String PLAIN_TEXT = "plaintext";
  
  public static String FORMAT_AS_ORIGINAL = "original";
  public static String FORMAT_AS_TEXTONLY = "textonly";
  
  public static String REPLY_AS_ORIGINAL = "original";
  public static String REPLY_WITH_ATTACH = "attach";
  
  public static String FORWARD_AS_ORIGINAL = "original";
  public static String FORWARD_WITH_ATTACH = "attach";
  
  public static String PREFIX_WITH_MINUS = "minus";
  public static String PREFIX_WITH_STAR = "star";
  public static String PREFIX_WITH_EQUAL = "equal";
  public static String PREFIX_WITH_QUOTE = "quote";
  
  private long showNumberOfConversation;  
  private String formatWhenReplyForward;
  private String replyMessageWith;
  private String forwardMessageWith;
  private String prefixMessageWith;
  private long periodCheckMailAuto;
  private String defaultAccount;
  private String editor; 
  private boolean saveMessageInSent_ = true;
  
  public MailSetting() {
    showNumberOfConversation = 30;
    formatWhenReplyForward = FORMAT_AS_ORIGINAL;
    replyMessageWith = REPLY_AS_ORIGINAL;
    forwardMessageWith = FORWARD_AS_ORIGINAL;
    periodCheckMailAuto = TEN_MINS;
    editor = WYSIWYG;
    prefixMessageWith = PREFIX_WITH_MINUS;
  }
  
  public long getShowNumberMessage() { return showNumberOfConversation; }
  public void setShowNumberMessage(long number) { showNumberOfConversation = number; }
  
  public long getPeriodCheckMailAuto() { return periodCheckMailAuto; }
  public void setPeriodCheckMailAuto(long period) { periodCheckMailAuto = period; }
  
  public String getTypeOfEditor() { return editor; }
  public void setTypeOfEditor(String edit) { editor = edit; }
  
  public String getDefaultAccount(){ return defaultAccount; }
  public void setDefaultAccount(String account) { defaultAccount = account; }
  
  public boolean saveMessageInSent() { return saveMessageInSent_; }
  public void setSaveMessageInSent(boolean save) { saveMessageInSent_ = save; }
  
  public String getFormatWhenReplyForward(){ return formatWhenReplyForward; }
  public void setFormatWhenReplyForward(String format) { formatWhenReplyForward = format; }
  
  public String getReplyMessageWith() { return replyMessageWith; }
  public void setReplyMessageWith(String reply) { replyMessageWith = reply; }
  
  public String getForwardMessageWith() { return forwardMessageWith; }
  public void setForwardMessageWith(String forward) { forwardMessageWith = forward; }
  
  public String getPrefixMessageWith() { return prefixMessageWith; }
  public void setPrefixMessageWith(String prefix) { prefixMessageWith = prefix; }
}
