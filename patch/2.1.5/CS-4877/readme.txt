Summary

    * Status: RSS portlet is not translated in French on 2.1.x
    * CCP Issue: N/A, Product Jira Issue: CS-4877, CS-4838.
    * Complexity: N/A

The Proposal
Problem description

What is the problem to fix?

   1. "Refresh News" on RSS portlet is not translated in French
          * Go to Application Registry & Import applications
          * Create new page with RSS reader portlet (inside Content category)
          * Select to view added page
          * Add a category in RSS Reader page
          * Change language to French --> the label "Refresh News" is still in English
   2. In RSS Portlet Reader in a page: Icon label not localized.

Fix description

How is the problem fixed?

   1. Replace hard-codes in the file: UIContentNavigation.gtmpl by keys:
          * Up level ->UIContentNavigation.label.upLevel
          * Add a Category -> UIContentNavigation.label.addCategory
          * Edit Selected Category -> UIContentNavigation.label.editSelectedCategory
          * Remove Selected Category -> UIContentNavigation.label.removeSelectedCategory
   2. Replace hard-code in the file: UIDetailContent.gtmpl by key:
          * Refresh News -> _ctx.appRes(uicomponent.getName() + ".title.refreshbar
          * Refresh News -> _ctx.appRes(uicomponent.getName() + ".label.refreshbar
          * Go to -> _ctx.appRes(uicomponent.getName() + ".goto.page
   3. Add new keys in the properties file:
          * RSSReaderPortlet.properties and RSSReaderPortlet_en.properties
            ?
            UIContentNavigation.label.upLevel=Up Level
             
            UIContentNavigation.label.addCategory=Add a Category
             
            UIContentNavigation.label.removeSelectedCategory=Remove Selected Category
             
            UIContentNavigation.label.editSelectedCategory=Edit Selected Category
             
            UIDetailContent.label.refreshbar=Refresh Bar
             
            UIDetailContent.title.refreshbar=Refresh Bar
             
            UIDetailContent.goto.page=Go to
             
            URLValidator.msg.Invalid-config=Invalid configuration URL
          * RSSReaderPortlet_en.properties:
            ?
            UIDescription.content.contentPortlet=Lecteur d'eXo RSS
            UIContentNavigation.label.upLevel=Monter
            UIContentNavigation.label.addCategory=Ajouter une catégorie
            UIContentNavigation.label.removeSelectedCategory=Supprimer catégorie
            UIContentNavigation.label.editSelectedCategory=Modifier catégorie
            UIDetailContent.label.refreshbar=Actualiser
            UIDetailContent.title.refreshbar=Actualiser
            UIDetailContent.goto.page=Aller à
            URLValidator.msg.Invalid-config=Configuration de l'URL invalide
          * RSSReaderPortlet_de.properties
            ?
            UIDetailContent.label.refreshbar=Aktualisieren Bar
             
            UIDetailContent.title.refreshbar=Aktualisieren Bar
             
            UIDetailContent.goto.page=Gehen Sie zu
          * RSSReaderPortlet_nl.properties:
            ?
            UIDetailContent.label.refreshbar=Vernieuwen
             
            UIDetailContent.title.refreshbar=Vernieuwen
             
            UIDetailContent.goto.page=Go to

Patch files:CS-4877.patch

Tests to perform

Reproduction test

   1. "Refresh News" on RSS portlet is not translated in French
          * Go to Application Registry & Import applications
          * Create new page with RSS reader portlet (inside Content category)
          * Select to view added page
          * Add a category in RSS Reader page
          * Change language to French --> the label "Refresh News" is still in English
            Expected: Refresh News -> Actualiser
   2. In RSS Portlet Reader in a page: Icon label not localized.
          * Create new page wizard
          * Add new RSS Portlet Reader in a page
          * Change language to French
          * Some icon on the left screen are not localized
            Expected:
            Up level -> Monter
            Add Category -> Ajouter une catégorie
            Remove Selected Category -> Supprimer catégorie
            Edit Selected Category -> Modifier catégorie
            eXo RSS Reader -> Lecteur de Flux RSS d'eXo

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
* PL review: patch validated

Support Comment
* Support reveiw: patch validated

QA Feedbacks
*
Labels parameters

