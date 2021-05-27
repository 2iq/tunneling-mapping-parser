ARG GRAALVM_VERSION
FROM ghcr.io/graalvm/graalvm-ce:ol8-java11-${GRAALVM_VERSION}

WORKDIR /workdir
COPY . git-repo-content

RUN \
    gu install native-image && \
    cd git-repo-content && \
    ./mvnw package -B -P native && \
    cp target/tunneling-mapping-parser / && \
    cd .. && \
    rm -rf git-repo-content && \
    gu remove native-image && \
    rm -rf ~/.m2

RUN \
    uname -s > /os && \
    uname -m > /arch
