Summary

    * Status: Fix missing dependencies in cs 1.3.x
    * CCP Issue: N/A, Product Jira Issue: CS-4185
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Many dependencies are not declared. Maven easily finds them by analyzing imports in the sources.

Fix description

How is the problem fixed?
    * To see which dependencies are missing just launch in the project:
      mvn dependency:analyze -DoutputXML=true
    * Copy/paste the result in each module POM.
    * Replace hard coded versions by a property.
    * Fix "build error" bug by adding missing dependencies needed for exobuild to pom file of pkg module.

Patch file: CS-4185.patch

Tests to perform

Reproduction test
* Yes

Tests performed at DevLevel
* Yes

Tests performed at QA/Support Level
* No

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
* PM review: approved

Support Comment
* Support review: Patch approved

QA Feedbacks
*
