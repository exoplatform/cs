Summary

    * Status: Ui problem with some events in week view
    * CCP Issue: CCP-995, Product Jira Issue: CS-5069.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

Steps to reproduce this problem under PLF 3.0.x:

- Go to calendar application.
- Create these events all in friday:
- testA from 09AM to 05PM, default calendar
- testB from 09AM to 12AM, users calendar
- testC from 2PM to 5PM, users calendar
- testD from 5PM to 6PM, default calendar
In day view, all events appear properly, in week view, testC doesn't appear, it's hidden by testA.
When i resize my screen( ctrl+/ctrl-) testC events appears.

Fix description

How is the problem fixed?

    * Set size and position of events before calculate their width.

Patch files:CS-5069.patch

Tests to perform

Reproduction test
* cf. above

Tests performed at DevLevel
* reproduction test and unit test.

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

* No.

Is there a performance risk/cost?
* Not detect yet.
Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*

