Synchronize Jenkins build jobs with Google Code branches
=========

This is a tool which lets you have your Jenkins build jobs and Google Code branches in sync. That is if someone creates a new branch in your Google Code repository, this utility will create a corresponding build job in Jenkins.

This tool consists of two parts:
1. Java program, which does all the work.
2. Google Gadget which is embeddable in Google Code Wiki pages.

Java program
-

To build clone the repo and use maven assembly plugin `mvn clean assembly:single`

To run it from Jenkins create a free style project with the following command
`java -jar sync-branch-jobs-1.0-SNAPSHOT-jar-with-dependencies.jar credentials.txt`

Where *credentials.txt* is a file containing your Jenkins username and password in two lines.

Google Code Gadget
-

To add this gadget to Google Code Wiki page add this element to your page:
`<wiki:gadgeturl="<url>" up_urlkey="<Simple API Access Key>" width="700" height="0" border="0" />`

Where
`<url>` is a path to hosted gadget xml file.
`<Simple API Access Key>` is a Google Simple Api key from Google Api Console. This key is used to shorten artifact download urls.

Demo
-

To see sync utility in action go to [marsrutai.lt app Jenkins] page.

To see gadget in action go to [marsrutai.lt app downloads] wiki page.

  [marsrutai.lt app jenkins]: https://marsrutai-lt-android.ci.cloudbees.com/job/sync-branch-jobs/
  [marsrutai.lt app downloads]: https://code.google.com/p/marsrutai-lt-mobile/wiki/Downloads
    