Summary

    * Status: Error when getting email with attachement
    * CCP Issue: N/A, Product Jira Issue: CS-5268.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Go to Mail Portlet
    * Add new account (Gmail)
    * Send some emails with attachments and some email without attachments to the create account in CS Mail
    * Return to Mail Portlet
    * Click to Get Mail button
      --> Among emails are gotten, emails with attachments are displayed but not contains attachments (View attached images)

Fix description

How is the problem fixed?

    * Try to guess that the mail has attachment or not by analyzing mail's header. And then, send that information by cometd to client to show in message list.


Patch files:CS-5268.patch
Tests to perform

Reproduction test
*

Tests performed at DevLevel
*

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
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

