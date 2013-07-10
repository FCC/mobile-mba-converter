### MMBA Project - JSON To CSV Transformer
 	
  This simple tool is built to convert raw FCC/SamKnows Speed tests to a single file (eg JSON and CSV). The inputs for this
  tool is a source folder containing any number of raw speed test json files.  The results from this tool is a single CSV 
  file ready to be read into any SQL style database AND a single JSON format to be read into a noSQL style database.  This 
  tool runs at a command line using the computer's java installation. Users must compile (or build) the source code on their
  installation prior to running with the instructions below (see Build the project).
 
### **Pre-requisites**
- A source folder containing all the input test files to be converted;
- Java,maven to be set in the classpath prior to running 

###  **Build the project**
*	```mvn clean package -> jar file will be created in ~/target/mmba-jar-with-dependencies.jar```

#### **Command to run the utility program** 

* 	```java -jar mmba-jar-with-dependencies.jar \<sourceFolder\> \<destinationFolder\> \<Delimiter\> \<convertToJsonFlag\> \<formatJsonFlag\>```

####	arguments:
*	sourceFolder -> source location (sub directories allowed)
*	destinationFolder -> destination folder (two files will be created with named samknows.<timestamp>.json/csv)
*	delimiter(optional) -> defaulted to ~
*	convertToJsonFlag(optional) -> true/false
*	formatJsonFlag(optional) -> true/false
*	Example: ```java -jar mmba-jar-with-dependencies.jar C:\Samknows\data\json C:\Samknows ~ true true```
