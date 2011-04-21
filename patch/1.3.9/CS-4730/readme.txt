Summary

    * Status: RSS Reader Porlet: Missing icon localization
    * CCP Issue: N/A, Product Jira Issue: CS-4730.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

    * Insert an RSS Reader portlet in a page: Icon labels are not localized in French.

Fix description

How is the problem fixed?

    * Create new keys for these icons
    * In the UIContentNavigation.gtmpl: Change the hard-code title by these created keys title.

Patch information:

    * Final files to use should be attached to this page (Jira is for the dicussion)

Patch files:
There are currently no attachments on this page.
Tests to perform

Reproduction test

   1. Create a new page
   2. Add an RSS Reader portlet in a page
   3. Change language to French
   4. Some icon on the left part are not localized
      Up level -> Monter
      Add Category -> Ajouter une catégorie
      Remove Selected Category -> Supprimer catégorie
      Edit Selected Category -> Modifier catégorie
      eXo RSS Reader -> Lecteur de Flux RSS d'eXo

Tests performed at DevLevel

    * cf. above

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

    * Patch approved.

Support Comment

    * Patch validated.

QA Feedbacks
*
