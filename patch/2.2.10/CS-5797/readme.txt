Summary
    Issue title: Problem while displaying event activities after migration calendar
    CCP Issue:  N/A 
    Product Jira Issue: CS-5797.
    Complexity: N/A

Proposal
 
Problem description

What is the problem to fix?
	* After migration from PLF 3.0.8 to PLF 3.5.3, in activities stream, event activity does not display well. It displays "The data is not available at the moment". 

Fix description

Problem analysis
	* The problem is that format of CalendarID property in soc:activityparam is not the same between two versions of Social. Detais are here

How is the problem fixed?
	* Use SocialChromatticLifeCycle to make sure that CalendarID property value is consistent between two versions.



Tests to perform

Reproduction test
	1) Start PLF-3.0.8 (use social 1.1.8). Login as john, create space "test".
	2) Go to space "test" add event with Event Summary = "test".
	3) Migrate PLF-3.0.8 to PLF-3.5.3 (use social 1.2.9)
	3) After migration, login as john, go to space "test" => click to tab "Home"
	Problem: In activities stream, the event activity does not display well. It displays "The data is not available at the moment".

Tests performed at DevLevel
	* Functional test

Tests performed at Support Level
	* Migration test from PLF 3.0.9 -> PLF 3.5.4 sn

Tests performed at QA
	*

Changes in Test Referential

Changes in SNIFF/FUNC/REG tests
	* No

Changes in Selenium scripts 
	* No

Documentation changes

Documentation (User/Admin/Dev/Ref) changes:


Configuration changes

Configuration changes:
	* No

Will previous configuration continue to work?
	* Yes

Risks and impacts

Can this bug fix have any side effects on current client projects?

    Function or ClassName change: 
    Data (template, node type) migration/upgrade: 

Is there a performance risk/cost?
	*

Validation (PM/Support/QA)

PM Comment
	* Validated

Support Comment
	* Validated

QA Feedbacks
	*
