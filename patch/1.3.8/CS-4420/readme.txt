Summary

    * Status: Labels are not translated in French
    * CCP Issue: CCP-470, Product Jira Issue: CS-4420
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
Some labels are not translated in French

Fix description

How is the problem fixed?
* Correct resource bundle files:
  - Sauver -> Enregistrer
  - Translate labels into French.
* Correct key invocations in
  - UIFormDateTimePicker.java
  - UIFormMultiValueInputSet.java

Patch file: CS-4420.patch

Tests to perform

Reproduction test
Run CS portlets (Chat, Mail, Agenda, Contact) with French as language setting.

Tests performed at DevLevel
* Cf. above

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
* PM review: patch approved

Support Comment
* Support review: patch validated

QA Feedbacks
*
