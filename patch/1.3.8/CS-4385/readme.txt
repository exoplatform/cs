Summary

    * Status: Error while deleting an e-mail
    * CCP Issue: CCP-472, Product Jira Issue: CS-4385.
    * Fixes also: CS-4615.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

   1. Problem 1: Can't delete (or move to Trash folder) an existing mail in Sent, Draft, Spam folder => get NullPointerException
   2. Problem 2: Can't move a mail between Sent, Draft, Spam folder => get NullPointerException
   3. Problem 3: Can't delete a mail that existing in both Inbox and Sent folder:
          * Send an email to the sender himself/herself -> this email exists in both Inbox and Sent folder.
          * Trying to delete this email in Inbox folder => get NumberFormatException

    * Environment:
      Microsoft Exchange Server 2008.
      Active Directory.

Fix description

How is the problem fixed?

    * Add response message to user when move/delete message(s) is not absolute successful.
    * Moving/deleting between folders (local and server folder) has a little difficulty about synchronizing between them. If a folder does not exist on server, it will be created before message(s) moved/deleted.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:
There are currently no attachments on this page.
Tests to perform

Reproduction test

    * Cf. above
      Tests performed at DevLevel
    * Cf. above

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

    * Function or ClassName change:
      In MailService class: change the signature of public methods (saveMessage, moveMessages).

Is there a performance risk/cost?
*No
Validation (PM/Support/QA)

PM Comment

    * PL review: patch approved.

Support Comment

    * Support review: Patch validated

QA Feedbacks
*

