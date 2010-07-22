Summary

    * Status: Chat - display user id instead of user name.
    * CCP Issue: CCP-421, Product Jira Issue : CS-4270
    * Complexity: N/A
    * Impacted Client(s): N/A
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?

    * Up to now, user id is shown in all conversation windows (private, group/chat room) and title windows (where're they?). This issue requests to display the full name instead.
         

Fix description

How is the problem fixed ?

    * The current version of chat server (open fire server and smack) doesn't provide the full name of chat users. We have to therefore inject the full name when the data is ready to put back to the browser. These data are obtained from Portal.

Patch information:

Patches files:
CS-4270.patch

Tests to perform

Which test should have detect the issue ?
* 

Is a test missing in the TestCase file ?
* No

Added UnitTest ?
* No

Recommended Performance test?
* No


Documentation changes

Where is the documentation for this feature ?
* It has only in the issue detail CS-4270

Changes Needed:
* Yes, update screen short for user manual, should be updated in this CS 2.0 User Guide


Configuration changes

Is this bug changing the product configuration ?
* No

Describe configuration changes:
* None

Previous configuration will continue to work?
* Yes


Risks and impacts

Is there a risk applying this bug fix ?
* No

Is this bug fix can have an impact on current client projects ?
* Yes, it is a customer request.

Is there a performance risk/cost?
* No


Validation By PM & Support

PM Comment
*

Support Comment
*


QA Feedbacks

Performed Tests
*

