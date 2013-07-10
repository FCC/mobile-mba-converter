### MMBA Project - JSON To CSV Transformer
  
  This simple tool is built to convert raw FCC/SamKnows Speed tests to a single file (eg JSON and CSV). The inputs for this tool is a source folder containing any number of raw speed test json files.  The results from this tool is a single CSV file ready to be read into any SQL style database AND a single JSON format to be read into a noSQL style database.  This tool runs at a command line using the computer's java installation. 
  
  Users must compile (or build) the source code on their installation prior to running with the instructions below (see Build the project).

### **Pre-requisites**
- A source folder containing all the input Measuring Mobile Broadband America files to be converted;
- Java .  most computers have this installed, because of web browsing.  To test if you do, you can use the directions at the [java site](http://www.java.com/en/download/testjava.jsp).
- Maven must be set in the classpath prior to running.  You only need this if you are attempting to compile the code yourself.

### **Getting started**
 Download the program from [gitHub](https://github.com/fcc/mobile-mba-androidapp/tree/master/JsonTransformer).  There are two options for download.
 
 1) Easiest - click the `Target` folder then click the `mmba-jar-with-dependencies.jar` file.  This will save the pre-compiled version to your location machine.  From your computer's command line (e.g. cmd window for windows or terminal window from a mac) navigate to the directory where you stored this file and follow the instructions below.
 
 2) Harder - Download the entire project from [github](https://github.com/fcc/mobile-mba-androidapp/archive/master.zip).  Use the directions below to compile and build the project.  If you choose this option you are a developer and want to understand the source code.  You also have familiarity with compiling java code, maven and compiling source projects in general.  If this sounds like greek go to option 1.
 

###  **Build the project**
If you choose Option 1 in Getting Started above, skip this section.  If you choose Option 2, follow these directions.
*	```mvn clean package -> jar file will be created in ~/target/mmba-jar-with-dependencies.jar```


#### **Running the Software** 
This software runs from your command line.  If you are not familiar with the command line, don't worry we will walk you through it.  It is very easy.  
- Windows - If you are on a windows style computer, simply hold the `windows key` (usually in the bottom left corner of the keyboard next to the crtl key) and the `r` key at the same time.  A window called Run will appear asking you to type the name of a command you want to run into the dialog box called `Open`.  Simply type the word `cmd` in the box and press the `OK` button.  You are now at the command line!  For tips on navigation, see [this link](http://www.makeuseof.com/tag/a-beginners-guide-to-the-windows-command-line/).

- Mac - If you are on a Mac style computer, simply hold the `command` key and press the `space bar`.  In the resulting Spotlight window, type the word `Terminal` and press return.  You are now at the command line!  This [link](http://www.youtube.com/watch?v=ftJoIN_OADc) is a good video introduction to command line usage.

1) Step 1 - Navigate to where the compiled version of the JasonTransformer software is.  If you selected the Easiest Option above, this is where you downloaded or saved the .jar file.  If you built the software with the harder option, you likely know where it is.  Use the commands `cd` and `ls` or `dir` to help you navigate and find our software.

2) Step 2 - Run the software.  Running the software is very easy to do.  Simply type;
- `java -jar mmba-jar-with-dependencies.jar <sourceFolder> <destinationFolder> <Delimieter> <convertToJsonFlag> <formatJsonFlag>`
- anything in the <> is what we call an argument.  These are things you need to tell the software to do.  Below are the examples;
- <sourceFolder> is the location of where all of the json files from Measuring Mobile Broadband America tests are located.  These are the files you are trying to import.  It could be anywhere you like on your computer.  An example is `c:/users/mmba/`
- <destinationFolder> is the location of where you want the output of the program to be.  This program creates (potentially) two files.  This argument tells the software where to write these two files.   The output names of these files are always called  samknows.<timestamp>.json/csv, where <timestamp> is a number meaning today.
- <delimiter> this is an optional argument for what you would like the delimiter of the output csv file to be.  The default delimiter (if you do not specify one) is a `~`.
<convertToJsonFlag> is an optional argument to tell the software to prepare (true) or don't prepare (false) a Json output file.  Enter `true` or nothing to prepare an output Json file and enter `false` to not prepare one.  Entering false will run faster.
<formatJsonFlag> is an optional argument.  You can either enter true or false to format the Json file.  The default is `true`.

An example of what you would type on the command line is;
```java -jar mmba-jar-with-dependencies.jar C:\Samknows\data\json C:\Samknows ~ true true```


#### **Problems** 
Some problems you might encounter in running the software are described below;
- I get an error when running saying Java command not found or something like that.  This means you do not have Java installed.  Go to [this site](http://www.java.com) and follow the directions there to download and install java.
- I get an error when running the software that reads `You need to provide at least the first two arguments delimited by space` - this means you didn't tell the software what the <sourceFolder> AND <destinationFolder> (or either one of them) where, or perhaps you mispelled the folder names.  Be sure you type the folder names correctly and that you have Measuring Mobile Broadband America Json files in that directory.
- 
