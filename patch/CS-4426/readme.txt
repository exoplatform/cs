Summary

    * Status: Authentication listener
    * CCP Issue: CCP-448, Product Jira Issue: CS-4426 (alias ALL-392)
    * Complexity: HIGH

The Proposal
Problem description

What is the problem to fix?

    * By adding a listener on the authentication event (see org.exoplatform.sample.customListener-0.0.1-SNAPSHOT.jar attached in CS-4426), we note that it is launched several times at each login from the classic portal by clicking on the link "Login". But this problem doesn't exist when connecting to the portal from Acme.

Fix description

How is the problem fixed?

    *  We remove messengerservlet and change to user restservlet
    * Update js file to dedicate use rest servlet

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patch files:
There are currently no attachments on this page.
Tests to perform

Tests performed at DevLevel?
* All chat test case passed

Tests performed at QA/Support Level?
* Yes
Documentation changes

Documentation Changes:
* No
Configuration changes

Configuration changes:
* Yes

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have an impact on current client projects?

    * Function or ClassName change ? Node

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
*

Support Comment
* Support Patch review : validated

QA Feedbacks
*

