[![Maven Central](https://img.shields.io/maven-central/v/org.sputnikdev/bluetooth-manager-tinyb.svg)](https://mvnrepository.com/artifact/org.sputnikdev/bluetooth-manager-tinyb)
[![Build Status](https://travis-ci.org/sputnikdev/bluetooth-manager-tinyb.svg?branch=master)](https://travis-ci.org/sputnikdev/bluetooth-manager-tinyb)
[![Coverage Status](https://coveralls.io/repos/github/sputnikdev/bluetooth-manager-tinyb/badge.svg?branch=master)](https://coveralls.io/github/sputnikdev/bluetooth-manager-tinyb?branch=master)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/478caa4b9498441f89bd1a880b7f8b53)](https://www.codacy.com/app/vkolotov/bluetooth-manager-tinyb?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=sputnikdev/bluetooth-manager-tinyb&amp;utm_campaign=Badge_Grade)
[![Join the chat at https://gitter.im/sputnikdev/bluetooth-manager-tinyb](https://badges.gitter.im/sputnikdev/bluetooth-manager-tinyb.svg)](https://gitter.im/sputnikdev/bluetooth-manager-tinyb?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
# bluetooth-manager-tinyb
A transport implementation for the [Bluetooth Manager](https://github.com/sputnikdev/bluetooth-manager) based on [TinyB](https://github.com/intel-iot-devkit/tinyb) library.

The Bluetooth Manager is a set of java APIs which is designed to streamline all the hard work of dealing with unstable 
by its nature Bluetooth protocol. A specially designed abstraction layer (transport) is used to bring support 
for various bluetooth adapters/dongles, operation systems and hardware architecture types.

The following diagram outlines some details of the Bluetooth Manager Transport abstraction layer:

![Transport diagram](bm-transport-abstraction-layer.png?raw=true "Bluetooth Manager Transport abstraction layer")

The TinyB transport brings support for:
 * Conventional USB bluetooth dongles. 
 * Linux based operation systems.
 * A wide range of hardware architectures (including some ARM based devices, e.g. Raspberry PI etc).

## Prerequisites

You must upgrade your bluez library to 5.43+. This is due to some changes in DBus API in bluez 5.43v.

To check what version you have use bluetoothctrl utility:
```sh
bluetoothctl

[bluetooth]# version
```
 
If you have an older bluez version you can upgrade it by running the following (ubuntu):

```sh
sudo  apt-get install debhelper dh-autoreconf flex bison libdbus-glib-1-dev libglib2.0-dev  libcap-ng-dev libudev-dev libreadline-dev libical-dev check dh-systemd libebook1.2-dev

wget https://launchpad.net/ubuntu/+archive/primary/+files/bluez_5.43.orig.tar.xz
wget https://launchpad.net/ubuntu/+archive/primary/+files/bluez_5.43-0ubuntu1.debian.tar.xz
wget https://launchpad.net/ubuntu/+archive/primary/+files/bluez_5.43-0ubuntu1.dsc

tar xf bluez_5.43.orig.tar.xz
cd bluez-5.43
tar xf ../bluez_5.43-0ubuntu1.debian.tar.xz
debchange --local=~lorenzen 'Backport to Xenial'
debuild -b -j4
cd ..
sudo dpkg -i *.deb
```

---
## Contribution

### Building

You are welcome to contribute to the project, the project environment is designed to make it easy by using:
* Travis CI to release artifacts directly to the Maven Central repository.
* Code style rules to support clarity and supportability. The results can be seen in the Codacy. 
* Code coverage reports in the Coveralls to maintain sustainability. 100% of code coverage with unittests is the target.

The build process is streamlined by using standard maven tools. 

To build the project you will need to install the TinyB library into your maven repository. Run this in the root of the project:
```sh
sh .travis/install-dependencies.sh
```

Then build the project with maven:
```bash
mvn clean install
```

To cut a new release and upload it to the Maven Central Repository:
```bash
mvn release:prepare -B
mvn release:perform
```
Travis CI process will take care of everything, you will find a new artifact in the Maven Central repository when the release process finishes successfully.

### Updating TinyB library

All TinyB dependencies (jar file and native libs) are manged by the project and automatically loaded in runtime, so that end-users do not have to build and install TinyB library locally.

In order to update TinyB library the following steps should be done:
* Build TinyB library as per TinyB [documentation](https://github.com/intel-iot-devkit/tinyb#using-tinyb) for each CPU architecure type:
  * x86_32
  * x86_64
  * arm6
* Copy/replace the following files (arm architecure is used in the examples below, similar paths should be used for the other archs):
  * <tinyb>/build/src/libtinyb.so to <project root>/src/main/resources/native/arm/armv6/libtinyb.so
  * <tinyb>/build/java/jni/libjavatinyb.so to <project root>/src/main/resources/native/arm/armv6/libjavatinyb.so
  * <tinyb>/build/java/tinyb.jar to <project root>/lib/tinyb.jar
* Update the TinyB "Specification-Version" number int the [MANIFEST.MF](META-INF/MANIFEST.MF) file:
  * Unpack tinyb.jar file which was copied earlier
  * Find its "Specification-Version" in the MANIFEST.MF file (<tinyb.jar>/META-INF/MANIFEST.MF)
  * Update "Specification-Version" in the project [MANIFEST.MF](META-INF/MANIFEST.MF) file