# graph4apk

The tool graph4apk implements analysis for generating graphs from apk files, in details PDGs for each method and a basic version of an SDG (connecting all the PDG toghether trough their call statements).
It is based on the Soot Framework.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes.

### Dependencies

The project needs Java8 and Soot3.3 to be run.
The complete jar of Soot3.3 and its dependencies can be found in the "<project.rootPath>/lib" folder.

### Settings

The <projects.rootPath> property in the config file "<project.rootPath>/pom.xml" needs to be set with the base directory of the project.

In order to run the project, all the path in "<project.rootPath>/scriptBash.config" needs to be set properly.
```
WHICH_PC=<NECESSARY ONLY FOR MULTITHREADING>
PROJECT_PATH=<ABSOLUTE PATH TO GRAPH4APK>
JAVAPATH=<ABSOLUTE PATH TO JAVA FOLDER (OPENJDK OR ORACLEJDK), EXAMPLE:'/usr/lib/jvm/java-8-openjdk-amd64'>
CLASSTORUN=createSDG
APK_FOLDER=<FOLDER WITH APKs TO ANALYZE, THERE ARE SAMPLES IN 'apk_db' FOLDER>
```

### Compile

The project can be compiled using maven
```
mvn compile
```

## Run

The project can be run using the script 'run.sh' in the project folder.
```
./run.sh -h
```

## Authors

* **Giacomo Iadarola** - *main contributor* - [Djack1010](https://github.com/Djack1010)
