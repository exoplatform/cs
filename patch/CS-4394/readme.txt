iSummary

    * Status: Chat - Unread message on chat tab is missed when refresh browser
    * CCP Issue: CCPID, Product Jira Issue : CS-4394
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix ?

    * After minimize and maximize the chat windows, the unread message is not updated 

Fix description

How the problem is fixed ?

    * Update java script to clear unread message after it has been read

Patch informations:
Patches files:
CS-4394.patch

Tests to perform

Tests performed at DevLevel ?
* Send some message between 2 users
* Minimize chat window and other user still send messages
* Maximize chat window the unread message on tab have to disappear  

Tests performed at QA/Support Level ?
* Yes


Documentation changes

Documentation Changes:
* No


Configuration changes

Configuration changes:
* No

Previous configuration will continue to work?
* Yes


Risks and impacts

Is this bug fix can have an impact on current client projects ?

    * Function or ClassName change ? No

Is there a performance risk/cost?
* No


Validation (PM/Support/QA)

PM Comment
*

Support Comment
* Reviewed by Support : Validated

QA Feedbacks
*

