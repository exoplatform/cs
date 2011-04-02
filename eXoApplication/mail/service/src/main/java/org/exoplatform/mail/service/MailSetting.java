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
 * Author : Phung Hai Nam
 *          phunghainam@gmail.com
 * Sep 22, 2007  
 */
public class MailSetting {
  public static final long    NEVER_CHECK_AUTO    = 0;

  public static final long    FIVE_MINS           = 5;

  public static final long    TEN_MINS            = 10;

  public static final long    TWENTY_MINS         = 20;

  public static final long    THIRTY_MINS         = 30;

  public static final long    ONE_HOUR            = 60;

  public static final boolean USE_WYSIWYG         = true;

  public static final boolean FORMAT_AS_ORIGINAL  = true;

  public static final boolean REPLY_WITH_ATTACH   = true;

  public static final boolean FORWARD_WITH_ATTACH = true;

  public static final String  PREFIX_WITH_MINUS   = "minus";

  public static final String  PREFIX_WITH_STAR    = "star";

  public static final String  PREFIX_WITH_EQUAL   = "equal";

  public static final String  PREFIX_WITH_QUOTE   = "quote";

  public static final long    VERTICAL_LAYOUT     = 0;

  public static final long    HORIZONTAL_LAYOUT   = 1;

  public static final long    NO_SPLIT_LAYOUT     = 2;

  public static final long    SEND_RECEIPT_ASKSME = 0;

  public static final long    SEND_RECEIPT_NEVER  = 1;

  public static final long    SEND_RECEIPT_ALWAYS = 2;

  private long                numberMsgPerPage_;

  private boolean             formatAsOriginal_;

  private boolean             replyWithAtt_;

  private boolean             forwardWithAtt_;

  private String              prefixMsgWith_;

  private long                periodCheckAuto_;

  private String              defaultAccount_;

  private boolean             useWysiwyg_;

  private boolean             saveMsgInSent_      = true;

  private long                layout_             = HORIZONTAL_LAYOUT;

  private long                sendReceipt_        = SEND_RECEIPT_ASKSME;

  public MailSetting() {
    numberMsgPerPage_ = 20;
    periodCheckAuto_ = FIVE_MINS;
    useWysiwyg_ = true;
    formatAsOriginal_ = true;
    replyWithAtt_ = false;
    forwardWithAtt_ = false;
    prefixMsgWith_ = PREFIX_WITH_MINUS;
  }

  public long getNumberMsgPerPage() {
    return numberMsgPerPage_;
  }

  public void setNumberMsgPerPage(long number) {
    numberMsgPerPage_ = number;
  }

  public long getPeriodCheckAuto() {
    return periodCheckAuto_;
  }

  public void setPeriodCheckAuto(long period) {
    periodCheckAuto_ = period;
  }

  public boolean useWysiwyg() {
    return useWysiwyg_;
  }

  public void setUseWysiwyg(boolean b) {
    useWysiwyg_ = b;
  }

  public String getDefaultAccount() {
    return defaultAccount_;
  }

  public void setDefaultAccount(String account) {
    defaultAccount_ = account;
  }

  public boolean saveMessageInSent() {
    return saveMsgInSent_;
  }

  public void setSaveMessageInSent(boolean save) {
    saveMsgInSent_ = save;
  }

  public boolean formatAsOriginal() {
    return formatAsOriginal_;
  }

  public void setFormatAsOriginal(boolean b) {
    formatAsOriginal_ = b;
  }

  public boolean replyWithAttach() {
    return replyWithAtt_;
  }

  public void setReplyWithAttach(boolean b) {
    replyWithAtt_ = b;
  }

  public boolean forwardWithAtt() {
    return forwardWithAtt_;
  }

  public void setForwardWithAtt(boolean b) {
    forwardWithAtt_ = b;
  }

  public String getPrefixMessageWith() {
    return prefixMsgWith_;
  }

  public void setPrefixMessageWith(String prefix) {
    prefixMsgWith_ = prefix;
  }

  public long getLayout() {
    return layout_;
  }

  public void setLayout(long layout) {
    layout_ = layout;
  }

  public long getSendReturnReceipt() {
    return sendReceipt_;
  }

  public void setSendReturnReceipt(long sendReceipt) {
    sendReceipt_ = sendReceipt;
  }
}
