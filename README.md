# corpus-driven-fact-checking-engine
A corpus-driven fact-checking engine, which returns a confidence value between -1 (fact is false) and +1 (fact is true) given a fact from Wikipedia

#Initialization
A fact is retrieved from the ttl file. Non alphanumeric characters such as commas, fullstops, dashes are removed from the text. As all facts begin with a supposed subject, they can take two basic forms:
Subject_Action_Object
Subject_Object_Action
The keywords “‘s” and “is” represented as _is_ used as to denote transition from one state to another. 


#Builder
The Builder system component sanitizes each fact. It starts with normalizing the text and attaching prepositions that may be part of the subject “and”, “to”, “into” to the subject.
Uppercase characters beginning words are generally used to denote a subject or object depending on the position in the fact. 

#Watson
Watson acts as the knowledge source. It checks the storage folder for possibly cached facts. Stored information in the storage are named using the subject token as identifier. 

#Executing Project
The Eclipse or Intellij IDE is recommended.
Executing on Eclipse
File -> Import -> Maven -> Check out Maven Projects from SCM
If no Git connector exists, click the “Find more SCM connectors in the m2E Marketplace” link
 Search for “m2e-egit”, select the checkbox and click finish, accept license agreement and install. Restart Eclipse
Select “git” in the SCM URL dropdown
Copy and paste “https://github.com/oniyide/SNLP-fact-checking-engine” in the textbox field
Finish
Run the “Main” class in the “com.hok” package
Executing on IntelliJ
VCS -> Checkout for Version Control -> Git
Copy and paste “https://github.com/oniyide/SNLP-fact-checking-engine” in the URL textbox field. Click “clone”
“Would you like to create an IntelliJ IDEA project for the sources you ....” Click Yes.
Select “import project from external model” radio button
Select “Maven”, click next, next, next, Finish
Run the “Main” class in the “com.hok” package

