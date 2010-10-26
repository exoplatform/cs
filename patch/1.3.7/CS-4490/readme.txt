Summary

    * Status: [Chat] UUID still appears in chat notifications
    * CCP Issue: CCP-578, Product Jira Issue: CS-4490.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
* UUID still appears in:
    * chat notifications.
    * tooltip of chat tab in a conversation window.
    * tooltip of user name in chat bar.

Fix description

How is the problem fixed?

    * Inject the date returned from the chat server.
    * Fill the full name from portal to data.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: CS-4490.patch

Tests to perform

Reproduction test

    * Steps to reproduce the bug in chat notifications:

   1. Connect to Chat portlet
   2. Add a chat room
   3. When the user joins Chat room, UUID is displayed.
   4. When you add a contact, PopUp notifications are displayed with UUID
   5. It is the same case when you kick, suppress user.

Tests performed at DevLevel

    * No

Tests performed at QA/Support Level

    * No

Documentation changes

Documentation changes:

    * Yes, maybe have to take some new screen-short 

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change : No

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment

    * Validated

Support Comment

    * Proposed patch validated by Support Team

QA Feedbacks
*

