Summary

    * Status: Minicalendar isn't translated into French in email
    * CCP Issue: N/A, Product Jira Issue: CS-5090.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
In email application, mini-calendar is still in English when the user's language is French.
To reproduce:

1. Login
2. Change language into French.

    * Case 1: When adding new account using imap protocol, mini-calendar in Limit to messages arrived since isn't translated.
    * Case 2: Modify an available account > Fetch Options > mini-calendar in Do not download all messages isn't translated.
    * Case 3: Create a new event (Nouvel Evenement) in mail application > Minicalendar isn't translated.

Fix description

How is the problem fixed?

    * Get locale language when create event in Mail portal.

Patch information:

    * Final files to use should be attached to this page (Jira is for the discussion)

Patch files:
Aucune pièce jointe sur cette page.
Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel

    * cf. above

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

