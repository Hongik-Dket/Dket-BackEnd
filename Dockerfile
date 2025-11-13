FROM node:20-bullseye-slim AS zk-build
WORKDIR /opt/zk

RUN apt-get update \
 && apt-get install -y curl bash git build-essential libssl-dev pkg-config cmake \
 && rm -rf /var/lib/apt/lists/*

RUN curl https://sh.rustup.rs -sSf | sh -s -- -y
ENV PATH="/root/.cargo/bin:${PATH}"

RUN git clone https://github.com/iden3/circom.git /tmp/circom \
 && cd /tmp/circom \
 && git checkout v2.1.6 \
 && cargo build --release \
 && cp target/release/circom /usr/local/bin/circom \
 && chmod +x /usr/local/bin/circom \
 && rm -rf /tmp/circom

COPY zk/package*.json ./
RUN npm ci

COPY zk/ /opt/zk/

RUN npm run ptau
RUN npm run build:base

FROM eclipse-temurin:17-jre
WORKDIR /app

ARG JAR_FILE=/build/libs/*.jar
COPY ${JAR_FILE} /app/app.jar

COPY --from=zk-build /opt/zk /opt/zk
COPY --from=zk-build /usr/local/bin/node /usr/local/bin/node
COPY --from=zk-build /usr/local/bin/npm  /usr/local/bin/npm
COPY --from=zk-build /usr/local/lib/node_modules /usr/local/lib/node_modules
ENV PATH="/usr/local/bin:${PATH}"

ENV ZK_ROOT=/opt/zk
ENV NODE_BIN=node

EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]