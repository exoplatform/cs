Summary

    * Status: Accept/Refuse buttons invisible in the invitation's email with MS-Exchange email
    * CCP Issue: CCP-1076, Product Jira Issue: CS-5266.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Login as root
    * Go to Mail Portlet
    * Add new MS-Exchange Account (Detail information to add here), use john account in this case.
    * Go to Calendar Portlet
    * Create new event
    * Click to More Detail button
    * In the table Reminder, delete the current email of root(root@localhost.com), and add new john's email (john@testlab.exoplatform.vn) to reminder
    * In the table Participants, delete the current email of root(root@localhost.com), and add new john's email (john@testlab.exoplatform.vn)as participants
    * Click to Save button.
    * Return to Mail Portlet
    * Open the invitation's email
      --> The Accept/Refuse buttons for the event are invisible --> NOK

@Note:

    * The same problem when open the invitation's email with Microsoft Office Outlook and Outlook Express
    * Password for MS-Exchange accounts: exoadmin

Fix description

How is the problem fixed?

    * MS-Exchange server: Must be configure to accept x-header named "x-exo-invitation"
    * Lowercase x-header named "X-Exo-Invitation" at cs source code level

Patch files:CS-5266.patch

Tests to perform

Reproduction test

    * cf. above

Tests performed at DevLevel

    * cf. above

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:

    * NO

Configuration changes

Configuration changes:

    * NO

Will previous configuration continue to work?

    * YES

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: NONE

Is there a performance risk/cost?

    * NO

Validation (PM/Support/QA)

PM Comment

    * PL review: Patch approved

Support Comment

    * Support review: Patch validated

QA Feedbacks
*

