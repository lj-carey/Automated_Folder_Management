# Deployment
This is the first time I am deploying a project, so I wrote handwritten notes.
Here I will discuss a bit of what I did as another source of the experience.

## Research
I researched how maven works alongside what a JAR is.
Maven is a build automation tool that manages dependencies, compiles code, and packages it into a JAR (Java ARchive) file. 
It uses pom.xml which is a Project Object Model. This was used for testing so that when run is pressed the 
Main.java program is run, and the tests were ran through the IDE.
Maven can be used to validate, compile, test, package and verify.
## Deployment
For this project and deployment I used the command terminal and used mvn clean verify to create a JAR file.
In the pom.xml file I changed the version from 1.0-SNAPSHOT to 1.0.0 to show deployment compared to development.
I had to include the maven-jar-plugin in the pom.xml file with the manifest main class set to Main as my project uses Main.java
as the entry point.
Deployment had issues (see Issues.md) with the build failing which was a good way to learn that testing passing in the 
IDE does not mean the build will pass in the terminal or that the project is ready for deployment.
Once these were resolved the build was successful and I was able to run the .JAR file by using 
"java -jar target\automated-folder-management-1.0.0.jar".

To deploy on github I will be creating 
