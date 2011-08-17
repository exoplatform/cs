Summary

    * Status: Can not add a contact with a long mail address in the address book
    * CCP Issue: CCP-1046, Product Jira Issue: CS-4919.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
In the contacts portlet , when we click on "add contact" and we want to add a contact with a long mail address as
"jean-christophe.lastname@adressetroplongue.fr", we get a message "invalid mail address".Please see the attachment "longmail.png"
Contacts with long mail address should be accepted.
Fix description

How is the problem fixed?

- before we have limitation length of email address, now we let user can put very long and valid email address

Patch files:CS-4919.patch

Tests to perform

Reproduction test
  Open Contact portlet, add contact and input a long mail address (ex: jean-christophe.lastname@adressetroplongue.fr ). If not show a message "invalid mail address" --> The problem fixed.

Tests performed at DevLevel
* Yes

Tests performed at QA/Support Level
* No
Documentation changes

Documentation changes:

    * NO

Configuration changes

Configuration changes:

    * NO

Will previous configuration continue to work?

    * Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: None

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    *   PL review: Patch validated

Support Comment

    *   Support review: Patch validated

QA Feedbacks
*

