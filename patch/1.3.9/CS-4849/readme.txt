Summary

    * Status: Chat - there is no user notification of the session timeout
    * CCP Issue: CCP-839, Product Jira Issue: CS-4849.
    * Complexity: N/A

The Proposal

Problem description

What is the problem to fix?
During a chat, the user may not be aware of the expiry of the session. In fact, he has the ability to send messages without any notification of the session time out. In this case, messages sent by the user don't reach the other users sharing the chat.
Also, at the session timeout, the history buttons do not work but user is not notified that this is due to the expiry of the session.

Fix description

How is the problem fixed?

    * During a chat, when sesion timeout, a pop-up message notify that the session has timed out and ask user to reload the page or not (if OK, it will redirect to login page)
    * After that, each time user try to send messages or hover mouse over Chat window action menu, chat bar menu, pop-up the notification message again

Patch file: CS-4849.patch

Tests to perform

Reproduction test

    * To test this issue, we have to wait until the session time out (about ~30 minutes), it take a long time so we suggest to reconfigure the total time-out in the /webapp/portal/WEB-INF/web.xml file:

      <session-config> <session-timeout>3</session-timeout>
      </session-config>

Steps to reproduce:

   1. Login as root
   2. Add john in the contact
   3. Start chat with john: send some message to john
   4. Wait until the session time out
      --> The user may not be aware of the expiry of the session. In fact, he has the ability to send messages without any notification of the session time out.
      Also, at the session timeout, the history buttons do not work but user is not notified that this is due to the expiry of the session.

Sent file icon, buttons for change status, join chat room and add contact have same behaviour.

Tests performed at DevLevel

* Reproduction test

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:
* No
Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
* Validated

Support Comment
* Validated

QA Feedbacks
*


