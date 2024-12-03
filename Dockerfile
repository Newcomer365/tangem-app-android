FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive
ENV ANDROID_HOME=/opt/android-sdk
ENV GRADLE_HOME=/opt/gradle
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/34.0.0:$GRADLE_HOME/bin

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
    $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-31" "platforms;android-34" "build-tools;34.0.0"

RUN mkdir -p $GRADLE_HOME && \
    wget https://services.gradle.org/distributions/gradle-8.2-bin.zip -O /tmp/gradle.zip && \
    unzip /tmp/gradle.zip -d $GRADLE_HOME && \
    rm -f /tmp/gradle.zip

RUN mkdir /lib/x86_64-linux-gnu
COPY docker/x86_64_libs/* /lib/x86_64-linux-gnu/
RUN mkdir /lib64
COPY docker/x86_64_libs/ld-linux-x86-64.so.2 /lib64/ld-linux-x86-64.so.2

RUN mkdir -p ~/.gradle && echo "org.gradle.jvmargs=-Xmx4G -Dkotlin.daemon.jvm.options=-Xmx2G -XX:+HeapDumpOnOutOfMemoryError -XX:+UseParallelGC -Dfile.encoding=UTF-8" > ~/.gradle/gradle.properties

RUN gem install bundler:2.5.23

COPY Gemfile Gemfile.lock ./
RUN bundle install --verbose

CMD ["bash"]
