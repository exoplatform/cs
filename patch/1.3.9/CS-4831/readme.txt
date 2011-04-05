Summary

    * Status: Untranslated tooltips in sharing calendar
    * CCP Issue: CCP-822, Product Jira Issue: CS-4831.
    * Complexity: low

The Proposal
Problem description

What is the problem to fix?
When we open Agenda (language is French), we click on share "Partager" for a calendar, the tooltips for actions "Edit" and "Delete" are not translated to French.
Fix description

How is the problem fixed?

    * Add two keys to resource bundle for French:

         UIAddEditPermission.action.title.Edit=Editer
         UIAddEditPermission.action.title.Delete=Supprimer
        
Patch file: CS-4831.patch

Tests to perform

Reproduction test

   1. Change language to French
   2. Go to Groupes -> Agenda
   3. In the table: Calendriers
   4. Click right mouse and choose Partager to share agenda
   5. Choose the name of user, save
      --> Two icons: Edit and Delete in the column action are not translated in to French
      Expected: Edit -> Editer
      Delete -> Supprimer

Tests performed at DevLevel
*

Tests performed at QA/Support Level
*

Documentation changes

Documentation changes:

    * NO

Configuration changes

Configuration changes:

    * NO

Will previous configuration continue to work?

    * YES

Risks and impacts

Can this bug fix have any side effects on current client projects?

    * Function or ClassName change: NO

Is there a performance risk/cost?

    * NO

Validation (PM/Support/QA)

PM Comment

    * Patch approved.

Support Comment

    * Patch validated by Support

QA Feedbacks
*

