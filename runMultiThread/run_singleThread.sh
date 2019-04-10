#!/bin/bash

SCRIPTPATH="$( cd "$(dirname "$0")" ; pwd -P )"
TIMESTAMP=$(date +%s)

JAVAPATH="/usr/lib/jvm/java-8-openjdk-amd64/bin/java" 
ENCODING="-Dfile.encoding=UTF-8" 
CPATH="-classpath /usr/lib/jvm/java-8-openjdk-amd64/jre/lib/charsets.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/cldrdata.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/dnsns.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/icedtea-sound.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/jaccess.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/java-atk-wrapper.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/localedata.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/nashorn.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/sunec.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/sunjce_provider.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/sunpkcs11.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/ext/zipfs.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jce.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jsse.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/management-agent.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/resources.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:/home/giacomo/IdeaProjects/graph4apk/target/classes:/home/giacomo/local_repo/soot/target/sootclasses-trunk-jar-with-dependencies.jar" 
CLASSTORUN="createSDG"
ARGS="-p cg all-reachable:true -no-bodies-for-excluded -full-resolver -w" 
SOOTCP="-cp .:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/rt.jar:/usr/lib/jvm/java-8-openjdk-amd64/jre/lib/jce.jar:/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-16/android.jar"
SOOTCP+=":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-15/android.jar:/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-14/android.jar"
SOOTCP+=":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-13/android.jar:/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-12/android.jar"
SOOTCP+=":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-11/android.jar:/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-10/android.jar"
SOOTCP+=":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-9/android.jar:/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-8/android.jar"
SOOTCP+=":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-7/android.jar:/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-6/android.jar"
SOOTCP+=":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-5/android.jar:/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-4/android.jar"
SOOTCP+=":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-3/android.jar:/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-17/android.jar"
SOOTCP+=":/home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms/android-17/android-17-api.jar"
ANDRJAR="-android-jars /home/giacomo/IdeaProjects/graph4apk/src/main/resources/android-platforms"

RUN="$JAVAPATH $ENCODING $CPATH $CLASSTORUN $ARGS $SOOTCP $ANDRJAR"

start=$(date +%s)
echo "-----  ANALYZING $1  -----" >> $SCRIPTPATH/runResults/res${TIMESTAMP}.txt
eval "$RUN $1 $2 $3 $4 >> $SCRIPTPATH/runResults/res${TIMESTAMP}.txt 2>> $SCRIPTPATH/runResults/err${TIMESTAMP}.txt"
end=$(date +%s)
runtime=$(($end-$start))
echo "---> FINISHING in $runtime sec" >> $SCRIPTPATH/runResults/res${TIMESTAMP}.txt
