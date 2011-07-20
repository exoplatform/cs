Summary

    * Status: Mail-Wrong number of unread messages
    * CCP Issue: N/A, Product Jira Issue: CS-3923
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Sometimes looking in the Inbox, there are many unread messages but next to the Inbox, there is no information about the unread messages.
Fix description

How is the problem fixed?

    * Set number of unread message again while mark read/unread/detail message actions.
    * Read unread message from db if it existed and update instead of count from zero.

Patch file: CS-3923.patch

Tests to perform

Reproduction test
Steps to reproduce:

    * Add new Gmail account (use imap protocol)
    * Send some new emails to this account
    * Click on the Get Mail button
      --> Get wrong number of unread emails

Tests performed at DevLevel

    * Yes

Tests performed at QA/Support Level

    * No

Documentation changes

Documentation changes:

    * No

Configuration changes

Configuration changes:

    * No

Will previous configuration continue to work?

    * Yes

Risks and impacts

*Can this bug fix have an impact any side effects on current client projects?add follow code in synchImapMessages() method in MailServiceImpl class.

    * Function or ClassName change: no
      Is there a performance risk/cost?
    * No

Validation (PM/Support/QA)

PM Comment
* Patch validated.

Support Comment
* Patch validated.

QA Feedbacks
*

