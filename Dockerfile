FROM node:20-bullseye-slim AS zk-build
WORKDIR /opt/zk

RUN apt-get update \
 && apt-get install -y curl bash \
 && rm -rf /var/lib/apt/lists/*

RUN apt-get update \
 && apt-get install -y curl bash git build-essential libssl-dev \
 && rm -rf /var/lib/apt/lists/*

RUN curl -L https://github.com/iden3/circom/releases/download/v2.1.6/circom-linux-amd64 \
      -o /usr/local/bin/circom \
 && chmod +x /usr/local/bin/circom

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