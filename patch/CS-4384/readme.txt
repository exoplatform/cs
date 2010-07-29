Summary

    * Status: Mail: message reply: Field "To" is empty
    * CCP Issue: CCP-469, Product Jira Issue: CS-4384
    * Complexity: N/A
   
The Proposal
Problem description

What is the problem to fix?

    *  the to field in Mail message is empty when trying to reply to a received message

Fix description

How is the problem fixed?

    *  this is fixed by putting the value of from property from the received mail on which we will respond.

Patch information:
Patches files:
CS-4384.patch

Tests to perform

Which test should have detect the issue?
*

Is a test missing in the TestCase file?
*

Added UnitTest?
*

Recommended Performance test?
*


Documentation changes

Where is the documentation for this feature?
*

Changes Needed:
*


Configuration changes

Is this bug changing the product configuration?
*

Describe configuration changes:
*

Previous configuration will continue to work?
*


Risks and impacts

Is there a risk applying this bug fix?
*

Can this bug fix  have an impact on current client projects?
*

Is there a performance risk/cost?
*


Validation By PM & Support

PM Comment
*

Support Comment
* Proposed patch validated by Support


QA Feedbacks

Performed Tests
*

