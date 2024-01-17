# Docker Application By Bit By Bit
1. To compile the code:\
  &ensp; In the root directory use the:\
  &ensp; "mvn clean compile assembly:single" command\
2. To run the code in the root directory run:\
  &ensp; "java -jar Docker_App_bitbybit-1.0-SNAPSHOT-jar-with-dependencies.jar"\
3. Instructions\
 &ensp; -Make sure you have the Docker Desktop running and exposed on localhost:2375\
 &ensp; -To Execute container commands go to the containers tab\
 &emsp;   -Select "All" "Running" "Paused" depending on which containers you want to view\
 &emsp;   -Select a container from the list\
 &emsp;   -Select the action you want to perform\
 &emsp;   -Certain actions will require for milliseconds to be defined. Enter the milliseconds you want the container task to be executed for\
 &emsp;   -If you do not want to specify the milliseconds enter -1\
 &emsp;  -You can view container measurements by pressing the measurements button\
 &emsp;	-You can query the database on a specific date, a specific container, or on both\
 &emsp;	-If you do not wish to specify a field leave it empty\
 &emsp;	-To create a container press the create container button and insert container name and image name\
 &emsp;	-By pressing the ALL button you will view all containers\
 &emps; -To use the command execution select the container press the EXECUTE COMMAND option, enter the command (eg ls) and press execute\
 &ensp; -To Execute image actions go to the images tab\
 &emsp; -Here you can view a list of the pulled images\
 &emsp; -By clicking on an image you can remove it or see information about it\
 &emsp; -Press the pull image button to pull an image and enter the name of the image you want to pull\
 &ensp; -To view Subnets or Volumes click the respective tabs\
4. Directory contents:
   &ensp;-Here are the pom.xml, checkstyle.xml files and the readme file
   &ensp;-In the src/main folder:
   &emsp;-In the java folder are the source and test codes
   &emsp;-In the resource folder is the db file(if created)
   &ensp;-In the target folder:
   &emsp;-Here is the jar file for the app
5. 
     

