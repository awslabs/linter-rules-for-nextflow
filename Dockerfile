FROM public.ecr.aws/amazonlinux/amazonlinux:2023

RUN dnf install java-17 wget /usr/bin/xargs -y

WORKDIR /work
COPY . ./src

WORKDIR /work/src
RUN ./gradlew clean build
WORKDIR /work

RUN cp src/ast-echo/build/libs/ast-echo-*.jar ./ast-echo-all.jar && \
    cp src/linter-rules/build/libs/linter-rules-*.jar ./linter-rules.jar && \
    cp src/scripts/check.sh ./check.sh && \
    cp src/scripts/echo-tree.sh ./echo-tree.sh && \
    rm -rf src

RUN wget https://repo1.maven.org/maven2/org/codenarc/CodeNarc/3.3.0/CodeNarc-3.3.0-all.jar && \
    wget https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.36/slf4j-api-1.7.36.jar && \
    wget https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.36/slf4j-simple-1.7.36.jar

CMD bash check.sh