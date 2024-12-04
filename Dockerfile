FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive
ENV ANDROID_HOME=/opt/android-sdk
ENV BUNDLE_PATH=vendor/bundle
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$ANDROID_HOME/build-tools/34.0.0:$BUNDLE_PATH/bin

RUN apt-get update && apt-get install -y \
    openjdk-17-jdk \
    wget \
    unzip \
    curl \
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
    rm -f /tmp/cmdline-tools.zip && \
    yes | $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager --licenses && \
    $ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager "platform-tools" "platforms;android-31" "platforms;android-34" "build-tools;34.0.0"

COPY docker/x86_64_libs/* /lib/x86_64-linux-gnu/
COPY docker/x86_64_libs/ld-linux-x86-64.so.2 /lib64/ld-linux-x86-64.so.2

#RUN mkdir -p ~/.gradle && echo "org.gradle.jvmargs=-Xmx4G -Dkotlin.daemon.jvm.options=-Xmx2G -XX:+HeapDumpOnOutOfMemoryError -XX:+UseParallelGC -Dfile.encoding=UTF-8" > ~/.gradle/gradle.properties

RUN gem install bundler:2.5.23
RUN gem install fastlane -v 2.211.0 -N -V

COPY Gemfile Gemfile.lock ./
RUN bundle install --jobs=4 --retry=3 --verbose

RUN bundle exec fastlane -v

CMD ["bash"]
