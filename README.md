# corpus-driven-fact-checking-engine
A corpus-driven fact-checking engine, which returns a confidence value between -1 (fact is false) and +1 (fact is true) given a fact from Wikipedia

## Initialization
A fact is retrieved from the ttl file. Non alphanumeric characters such as commas, fullstops, dashes are removed from the text. As all facts begin with a supposed subject, they can take two basic forms:
Subject_Action_Object
Subject_Object_Action
 


## Builder
The Builder system component sanitizes each fact. It starts with normalizing the text and attaching prepositions that may be part of the subject “and”, “to”, “into” to the subject.
Uppercase characters beginning words are generally used to denote a subject or object depending on the position in the fact. 

## Watson
Watson acts as the knowledge source. It checks the storage folder for possibly cached facts. Stored information in the storage are named using the subject token as identifier. 

## Executing Project
The Eclipse or Intellij IDE is recommended.

# Executing on Eclipse
If you don't already have the Git connector for M2Eclipse install it. M2Eclipse will help you along by prompting you on the Import menu.

Select the "Import..." context menu from the Package Explorer view
Select "Check out Maven projects from SCM" option under the Maven category
On the window that is presented choose the link "Find more SCM connectors in the m2e Marketplace
Find connector for Git...install...restart
Note that in the search box you may have to enter "EGit" instead of "Git" to find the right connector.

With that done, simply go to the EGit repository, bring up the context menu for the Working directory and select "Import Maven projects...".

# Executing on IntelliJ
VCS -> Checkout for Version Control -> Git
Copy and paste “https://github.com/oniyide/SNLP-fact-checking-engine” in the URL textbox field. Click “clone”

“Would you like to create an IntelliJ IDEA project for the sources you ....” Click Yes.

Select “import project from external model” radio button

Select “Maven”, click next, next, next, Finish

Run the “Main” class in the “com.hok” package

## Team Members: HOK
Oniyide Olugbo Olufemi

