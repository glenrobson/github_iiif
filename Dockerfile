# build stage
FROM maven:3-jdk-11 AS buildstage
WORKDIR /usr/src/workbench
COPY . /usr/src/workbench
ARG MVN_ARGS="-DskipTests"
# build SAS using maven
RUN mvn $MVN_ARGS package

# runnable container stage
FROM tomcat:9-jre11 AS runstage
# remove tomcat default webapps 
RUN rm -r /usr/local/tomcat/webapps/*
# copy SAS from build image
COPY --from=buildstage /usr/src/workbench/target/github_iiif /usr/local/tomcat/webapps/ROOT

# use default port and entrypoint
