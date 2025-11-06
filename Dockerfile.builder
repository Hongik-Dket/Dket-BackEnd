# Dockerfile.builder
FROM ubuntu:22.04

RUN apt-get update && apt-get install -y \
    build-essential curl pkg-config libssl-dev ca-certificates git \
 && rm -rf /var/lib/apt/lists/*

# Rustup 설치
RUN curl -sSf https://sh.rustup.rs | sh -s -- -y
ENV PATH="/root/.cargo/bin:${PATH}"

# glibc x86_64 타겟
RUN rustup target add x86_64-unknown-linux-gnu

WORKDIR /work