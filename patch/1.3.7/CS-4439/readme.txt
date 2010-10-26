Summary

    * Status: Chat: Number of unread messages lost after refreshing the page
    * CCP Issue: N/A, Product Jira Issue: CS-4439.
    * Fix also: CCP-500/CS-4411
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * The number of unread messages lost after refreshing the page.

Fix description

How is the problem fixed?

    * Update tab list information after updating unread message

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch file: CS-4439.patch

Tests to perform

Reproduction test
Case 1: 2 users, bug in minimized conversation window.

   1. Login by 2 users in different windows
   2. User A adds user B into his/her personal contact list
   3. B accepts A's invitation
   4. A sends 1 message to B
   5. B views the sent message
   6. B minimizes the private chat window --> no longer unread msg
   7. B refreshes the browser (by clicking F5) --> The number of unread message is reset to 1.

Case 2: 3 users, bug in chat tab.

   1. 3 users A, B, C login on 3 browser windows
   2. A adds C and starts a private chat with C
   3. B adds C and starts a private chat with C
   4. On C's chat window, C is at the chat tab with A.
      And there's 1 unread message in the chat tab with B.
   5. C moves to the chat tab with B. There's no longer unread message in this tab. C doesn't type anything in this chat tab.
   6. C returns to the chat tab with A.
   7. C refreshes the page (F5). In the chat tab with B, there is now 1 unread message.

Tests performed at DevLevel
* No

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
* No

Is there a performance risk/cost?
* No

Validation (PM/Support/QA)

PM Comment
*

Support Comment
*

QA Feedbacks
*
