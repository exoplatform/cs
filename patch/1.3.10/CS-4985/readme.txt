Summary

    * Status: CS - Mail - Filter With Attachment does not work
    * CCP Issue: N/A, Product Jira Issue: CS-4985.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Steps to reproduce :
1. Login /portal/private/classic/mail as root
2. Create a new mail account and get mail.
3. At Inbox, click Filter ans select With Attachment

---> All the messages, which has attachment or does not, are filtrated (disappeared) from the view.
Fix description

How is the problem fixed?

    *  Save property exo:hasAttatchment = true when the email has attachments.

Patch file: CS-4985.patch

Tests to perform

Reproduction test
* Cf. above

Tests performed at DevLevel
* 

Tests performed at QA/Support Level
* 

Documentation changes

Documentation changes:
* Not
Configuration changes

Configuration changes:
* Not

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: None

Is there a performance risk/cost?
* Not

Validation (PM/Support/QA)

PM Comment
* Patch validated

Support Comment
* Patch validated.

QA Feedbacks
*
Labels parameters

