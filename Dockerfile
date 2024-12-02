FROM ubuntu:18.04

LABEL org.opencontainers.image.source="https://github.com/org-arl/jhwbus"
LABEL org.opencontainers.image.authors="jhwbus"

ARG JDK_VERSION=8u422
ARG JDK_VERSION_RELEASE=b05

ENV DEBIAN_FRONTEND=noninteractive
ENV USER=ubuntu

RUN mkdir -p /home/jhwbus

# Install dependencies
RUN apt-get update && apt-get install -y \
  ca-certificates \
  curl \
  gpg \
  gcc \
  locales \
  libi2c-dev

# Install AdoptOpenJDK 8
RUN curl -L https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor -o /etc/apt/trusted.gpg.d/adoptium.gpg
RUN sh -c "echo \"deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main\" | tee 'etc/apt/sources.list.d/adoptium.list'"
RUN apt update && apt -y install temurin-8-jdk

RUN rm -rf /var/lib/apt/lists/*

RUN locale-gen en_US.UTF-8
ENV LANG=en_US.UTF-8
ENV LC_ALL=en_US.UTF-8

WORKDIR /home/jhwbus