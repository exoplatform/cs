Summary

    * Status: Duplicate message when checking mails
    * CCP Issue: N/A, Product Jira Issue: CS-4581.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
    * Login
    * Go to Mail portlet
    * Add a valid mail account
    * Click on Get Mails => Duplicate mail in the right panel

Fix description

Problem analysis
    * After checking mail successfully, we didn't update the mail list by Cometd.

How is the problem fixed?
    * Update list of message by Cometd after loading successful in MailServiceHandler.js.

Patch file: CS-4581.patch

Tests to perform

Reproduction test

    * cf. above

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

    * None

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Validated by PM

Support Comment

    * Validated by Support

QA Feedbacks
*

