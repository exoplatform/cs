Summary

    * Status: Show a checkbox beside contact
    * CCP Issue: N/A, Product Jira Issue: CS-5633.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
-Go to Mail application
-Click on Contact button in action bar
-Normal i don't see check box beside a contact to checked to delete this contact. It's confusing to the user when deleting a contact.
To avoid confusion for the user, i suggest the following:

    * when the user move their mouse over the contact , a check box that is showed beside its

Fix description

How is the problem fixed?
* Edit logic in CheckAllContactActionListener listener in UIAddressBookForm.java file
* Edit logic in UIAddressBookForm.gtmpl file
* Edit css in MailPortlet

Patch files:CS-5633.patch

Tests to perform

Reproduction test
* Go to Mail application
* Click on Contact button in action bar
* Normal i don't see check box beside a contact to checked to delete this contact. It's confusing to the user when deleting a contact.
* When the user move their mouse over the contact , a check box that is showed beside its

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*
Configuration changes

Configuration changes:
* No

Will previous configuration continue to work?
* No
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

