Summary

    * Status:Name of an user is still displayed on list after she leaves a room
    * CCP Issue: N/A, Product Jira Issue: CS-5331.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

Steps
1. Login as root, create 2 users: Test1, Test2
2. Using Test1, create a room chat: R1, invite Test2
3. 2 users chat something...
4. Using Test2, click leave from R1
5. Back to screen of Test1, check room chat

Actual results:

Test2 is still displayed in list of R1
Click Add button, checkbox of Test2 is disable
After click button Refresh --> Test2 is disapeared from list

Fix description

How is the problem fixed?

    * Fix confusion about encoded and decoded user name.

Patch files:CS-5331.patch

Tests to perform
Reproduction test
*

Tests performed at DevLevel
* Reproduction test

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

    * No

Is there a performance risk/cost?
* No
Validation (PM/Support/QA)

PM Comment
* PL review: Patch validated

Support Comment
* Support review: Patch validated

QA Feedbacks
*

