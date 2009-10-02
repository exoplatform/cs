/*
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
package org.exoplatform.mail.service;

/**
 * Created by The eXo Platform SARL
 * Author : Nam Phung
 *          phunghainam@gmail.com
 * Sep 22, 2007  
 */
public class MailSetting {
  
  public static long NEVER_CHECK_AUTO = 0;
  public static long FIVE_MINS = 5 ;
  public static long TEN_MINS = 10;
  public static long TWENTY_MINS = 20;
  public static long THIRTY_MINS = 30;
  public static long ONE_HOUR = 60;
  
  public static boolean USE_WYSIWYG = true;
  
  public static boolean FORMAT_AS_ORIGINAL = true ;
  
  public static boolean REPLY_WITH_ATTACH = true ;
  
  public static boolean FORWARD_WITH_ATTACH = true ;
  
  public static String PREFIX_WITH_MINUS = "minus";
  public static String PREFIX_WITH_STAR = "star";
  public static String PREFIX_WITH_EQUAL = "equal";
  public static String PREFIX_WITH_QUOTE = "quote";
  
  private long numberMsgPerPage;  
  private boolean formatAsOriginal;
  private boolean replyWithAtt;
  private boolean forwardWithAtt;
  private String prefixMsgWith;
  private long periodCheckAuto;
  private String defaultAccount;
  private boolean useWysiwyg ; 
  private boolean saveMsgInSent = true;
  
  public MailSetting() {
    numberMsgPerPage = 20;
    periodCheckAuto = FIVE_MINS;
    useWysiwyg = true; 
    formatAsOriginal = true;
    replyWithAtt = false; 
    forwardWithAtt = false;
    prefixMsgWith = PREFIX_WITH_MINUS;
  }
  
  public long getNumberMsgPerPage() { return numberMsgPerPage; }
  public void setNumberMsgPerPage(long number) { numberMsgPerPage = number; }
  
  public long getPeriodCheckAuto() { return periodCheckAuto; }
  public void setPeriodCheckAuto(long period) { periodCheckAuto = period; }
  
  public boolean useWysiwyg() { return useWysiwyg; }
  public void setUseWysiwyg(boolean b) { useWysiwyg = b; }
  
  public String getDefaultAccount(){ return defaultAccount; }
  public void setDefaultAccount(String account) { defaultAccount = account; }
  
  public boolean saveMessageInSent() { return saveMsgInSent; }
  public void setSaveMessageInSent(boolean save) { saveMsgInSent = save; }
  
  public boolean formatAsOriginal(){ return formatAsOriginal ; }
  public void setFormatAsOriginal(boolean b) { formatAsOriginal = b; }
  
  public boolean replyWithAttach() { return replyWithAtt; }
  public void setReplyWithAttach(boolean b) { replyWithAtt = b; }
  
  public boolean forwardWithAtt() { return forwardWithAtt ; }
  public void setForwardWithAtt(boolean b) { forwardWithAtt = b; }
  
  public String getPrefixMessageWith() { return prefixMsgWith; }
  public void setPrefixMessageWith(String prefix) { prefixMsgWith = prefix; }
}
