Summary

    * Status: Incorrect label in Calendar portlet
    * CCP Issue: CCP-1092, Product Jira Issue: CS-5300.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Start server.
    * Add new user "test".
    * Apply CS-4914's patch
      --> Incorrect labels appears with test user.

Fix description

How is the problem fixed?

    *  Change the values of default calendar, default category and default event category to the values that custome used in the last version of product.

Patch files:CS-5300.patch
Tests to perform

Reproduction test
*

Tests performed at DevLevel
*

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

