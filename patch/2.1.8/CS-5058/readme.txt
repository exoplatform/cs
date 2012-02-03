Summary

Status: Get message "Please select messages" when right-click on a message and select a menu item
CCP Issue: N/A, Product Jira Issue: PLF:CS-5058.
Complexity: N/A
The Proposal

Problem description
What is the problem to fix?

Get message "Please select messages" when right-click on a message and select a menu item
Action on the menu item could not be performed
Fix description
How is the problem fixed?

Because mail messages are fetched and rendered in client side(Browser) but they are not stored in server side(UI component) at the same time. Therefore, when an action request is call from the client, the server has no data to serve and response. In a nutshell, there is an asynchronous between client's data and server's data. To fix this issue, all data in the request must be read, stored and rendered in UI components before action is invoked actually
Previously, we use JS to make a request and sent each time client receives 10 message as:
var form = eXo.core.DOMUtil.findAncestorByTagName(tbodyMsgList, "form");
if (this.msgCount < 10) {
  this.msgCount++;
} else {
  this.msgCount = 0;
  eXo.mail.UpdateList.sendRequest(form.action,data.msgId, form);
}
Create an UI component lifecyle(override UIFormLifeCycle) to achieve all data in the action request:
public class UIMessageListLifecycle extends UIFormLifecycle {
...
 
  private void processNormalRequest(UIForm uiForm, WebuiRequestContext context) throws Exception {
...
      PortletRequest request = context.getRequest();
      Iterator<Entry<String, String[]>> paramsIter = request.getParameterMap().entrySet().iterator();
      while (paramsIter.hasNext()) {
// Render one UIFormCheckBoxInput per param(message's id) to display a mail message
}
...
}
From there, the above Javascript code isn't used anymore, so it would be remove
Create a function which aims to encode and decode message's id to advoid special character(for eg: +,=,&) :
public static String encodeMailId(String id) {
    if (id == null)
      return "";
    return id.replaceAll("\\+", PLUS_ENCODE)
             .replaceAll("=", EQUAL_ENCODE)
             .replaceAll("&", AND_ENCODE);
  }
 
  public static String decodeMailId(String id) {
    if (id == null)
      return "";
    return id.replaceAll(PLUS_ENCODE, "+")
             .replaceAll(EQUAL_ENCODE, "=")
             .replaceAll(AND_ENCODE, "&");

Patch files: CS-5058.patch

Tests to perform
Reproduction test
Steps to reproduce:

Login /portal/private/classic/mail as root
Create a mail account and get mail
Check Inbox, right-click on a message and select Reply.---> Receive message "Please select messages" and the Reply action is not performed and cannot do anything with this message
Tests performed at DevLevel

Same as above
Tests performed at QA/Support Level

Same as above
Documentation changes
Documentation changes:

No
Configuration changes
Configuration changes:

No
Will previous configuration continue to work?

Yes
Risks and impacts
Can this bug fix have any side effects on current client projects?

Function endcodeMailId and decodeMailId are moved from MailUtils(in mail app project) to Utils(in mail service project)
Create new class UIMessageListLifecycle to replace UIFormLifecycle
Is there a performance risk/cost?

No
Validation (PM/Support/QA)

PM Comment

PL review: Patch validated
Support Comment

Support review: Patch validated
QA Feedbacks

 
