#!/bin/bash

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
if [ -f ${SCRIPTPATH}/scriptBash.config ]; then
	source ${SCRIPTPATH}/scriptBash.config
else
	echo "ERROR: config file 'scriptBash.config' not found in project folder, exiting..."
	exit
fi

echo "Starting Setup"

if [ -z "$(which java)" ]; then
	if [ -z "$(${JAVAPATH}/bin/java -version 2>&1 >/dev/null | grep " version ")" ]; then 
		echo "Java not found, installing..."
		sudo apt-get install openjdk-8-jdk
	fi
fi

if [ -z "$(${JAVAPATH}/bin/java -version 2>&1 >/dev/null | grep " version ")" ]; then 
    echo "java not found, check scriptBash.config! Exiting..."
    exit
fi

if [ -z "$(which mvn)" ]; then
        echo "Maven not found, installing..."
        sudo apt-get install maven
fi

if [ -f ${SCRIPTPATH}/pom.xml ]; then 
	PROJECTPATH_POM=$(cat ${SCRIPTPATH}/pom.xml | grep "<project.rootPath>" | cut -d">" -f2 | cut -d"<" -f1)
else
	echo "ERROR! Pom.xml file not found in project folder, exiting..."
	exit
fi

if [ "$SCRIPTPATH" != "$PROJECTPATH_POM" ]; then
	printf "Setting project path in pom.xml... "
	sed -i "s@${PROJECTPATH_POM}@${SCRIPTPATH}@g" ${SCRIPTPATH}/pom.xml
	echo "done!"
fi

if [ "$SCRIPTPATH" != "$PROJECT_PATH" ]; then
	printf "Setting project path in scriptBash.config... "
	sed -i "s@${PROJECT_PATH}@${SCRIPTPATH}@g" ${SCRIPTPATH}/scriptBash.config
	echo "done!"
fi

mvn compile

if [ ! -f ${SCRIPTPATH}/lib/sootclasses-trunk-jar-with-dependencies.jar ]; then
	echo "ERROR! File 'sootclasses-trunk-jar-with-dependencies.jar' not found in the /lib folder, exiting..."
	exit
fi

echo "Setup completed successfully!"
