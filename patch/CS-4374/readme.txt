Summary

    * Status: Problem in display conversation window when moving between pages
    * CCP Issue: CCP-420, Product Jira Issue : CS-4374
    * Complexity: LOW
    * Impacted Client(s): CG95 and probably all.
    * Client expectations (date/content): N/A

The Proposal
Problem description

What is the problem to fix ?
The minimized conversation window reappears for an instant once the user changes or refreshes pages.

   1. Open a conversation window (private conversation or a chat room)
   2. Minimize the chat window
   3. Change to another page
      Observation: the chat window reappears for an instant. Then it is minimized.

Fix description

How is the problem fixed ?

    * By adding visible property to the conversation windows css

Patch informations:

Patches files:
CS-4374.patch

Tests to perform

Which test should have detect the issue ?

   1. Open a conversation window (private conversation or a chat room)
   2. Minimize the chat window
   3. Change to another page
      Observation: the chat window reappears for an instant. Then it is minimized.

Is a test missing in the TestCase file ?
* No

Added UnitTest ?
* No

Recommended Performance test?
* No


Documentation changes

Where is the documentation for this feature ?
*

Changes Needed:
* No


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
* Yes, customer's feedback 

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

