Summary

    * Status: CLONE - Calendar Can not send remind email
    * CCP Issue: N/A, Product Jira Issue: CS-5264.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Create new account
    * Login by root
    * Select calendar
    * Open form to create new event
    * Select Reminder tab
    * Check on Remind by email
    * Choose the email of new created account --> show message that email is not valid

Fix description

How is the problem fixed?

    *  Fix code to prevent getting wrong email address of new created account.

Patch files: CS-5264.patch
Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
* No document changes
Configuration changes

Configuration changes:
* No configuration changes

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: None

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

