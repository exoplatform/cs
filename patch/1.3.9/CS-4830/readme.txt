Summary

    * Status: Untranslated label in the search of agenda
    * CCP Issue: CCP-821, Product Jira Issue: CS-4830.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?
When we open Agenda (language is French), then Advanced Search, the options of "Priorité" field are not in French.

Fix description

How is the problem fixed?
    * Fix incorrect resource bundle keys:
      UIAdvancedSearchForm.label.option.normal=Normale
      UIAdvancedSearchForm.label.option.high=Haute
      UIAdvancedSearchForm.label.option.low=Basse

Patch file: CS-4830.patch

Tests to perform

Reproduction test

   1. Set language French
   2. Go to Groupes -> Agenda
   3. Click to the Recherche avancée icon
      --> Drop-down list of the filed 'Priorité' is not translated into French

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

Can this bug fix have an impact any side effects on current client projects?

    * Function or ClassName change: no

Is there a performance risk/cost?

    * No

Validation (PM/Support/QA)

PM Comment

    * Patch approved.

Support Comment

    * Validated by Support

QA Feedbacks
*

