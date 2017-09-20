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

---
## Contribution

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