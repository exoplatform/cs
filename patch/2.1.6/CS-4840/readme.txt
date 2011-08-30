Summary

    * Status: PLF:Calendar mini calendar and month view is wrong title day of week name
    * CCP Issue: PLF:CCP-890, Product Jira Issue: PLF:CS-4840.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
PLF:Calendar mini calendar and month view is wrong title day of week name :

    * login
    * go to calendar application
    * check the mini calendar and compare with weekview
    * switch to month view, we see the same problem as mini calendar
    * see more detail in attachment

Fix description

How is the problem fixed?

    *  get day names of week to render title days due to start day of week.


Patch files:CS-4840.p

Tests to perform

Reproduction test
* Steps to reproduce:
1. Go to Calendar Application
2. By default, Calendar setting: Country (Language): Belgium (French)/ Time zone: (GMT +1:00) Europe/Brussels
3. Change these values: Country (Language): Canada (French) /Time zone: (GMT -8:00) Canada/Pacific
A week begins on Tuesday. Same problem with Month view

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*
Documentation changes

Documentation changes:
*
Configuration changes

Configuration changes:
* None

Will previous configuration continue to work?
* Yes
Risks and impacts

Can this bug fix have any side effects on current client projects?

    * None

Is there a performance risk/cost?
*
Validation (PM/Support/QA)

PM Comment
* PL review: patch validated

Support Comment
* Support review: patch validated

QA Feedbacks
*

