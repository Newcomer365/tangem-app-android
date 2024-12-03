FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive
ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    wget \
    unzip \
    curl \
    openssh-client \
    git \
    ruby \
    ruby-dev \
    build-essential \
    && apt-get clean

RUN mkdir -p $ANDROID_HOME/cmdline-tools/latest && \
    wget https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -O /tmp/cmdline-tools.zip && \
    unzip /tmp/cmdline-tools.zip -d $ANDROID_HOME/cmdline-tools/latest && \
    mv $ANDROID_HOME/cmdline-tools/latest/cmdline-tools/* $ANDROID_HOME/cmdline-tools/latest/ && \
    rm -rf $ANDROID_HOME/cmdline-tools/latest/cmdline-tools && \
    rm -f /tmp/cmdline-tools.zip

RUN yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses && \
    $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-31"

# aapt2 requires libs that are not provided in arm64 ubuntu image
RUN  mkdir                             /lib/x86_64-linux-gnu
COPY x86_64_libs/libc.so.6             /lib/x86_64-linux-gnu/libc.so.6
COPY x86_64_libs/libdl.so.2            /lib/x86_64-linux-gnu/libdl.so.2
COPY x86_64_libs/libgcc_s.so.1         /lib/x86_64-linux-gnu/libgcc_s.so.1
COPY x86_64_libs/libm.so.6             /lib/x86_64-linux-gnu/libm.so.6
COPY x86_64_libs/libpthread.so.0       /lib/x86_64-linux-gnu/libpthread.so.0
COPY x86_64_libs/librt.so.1            /lib/x86_64-linux-gnu/librt.so.1
RUN  mkdir                             /lib64
COPY x86_64_libs/ld-linux-x86-64.so.2  /lib64/ld-linux-x86-64.so.2

RUN mkdir -p ~/.gradle && echo "org.gradle.jvmargs=-Xmx4G -Dkotlin.daemon.jvm.options=-Xmx2G -XX:+HeapDumpOnOutOfMemoryError -XX:+UseParallelGC -Dfile.encoding=UTF-8" > ~/.gradle/gradle.properties

RUN gem install bundler

WORKDIR /workspace

COPY Gemfile Gemfile.lock ./
RUN bundle install

CMD ["bash"]



