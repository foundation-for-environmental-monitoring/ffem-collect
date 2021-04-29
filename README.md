# ffem Collect
![Platform](https://img.shields.io/badge/platform-Android-blue.svg)
[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build status](https://circleci.com/gh/getodk/collect.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/getodk/collect)
[![codecov.io](https://codecov.io/github/getodk/collect/branch/master/graph/badge.svg)](https://codecov.io/github/getodk/collect)
[![Slack](https://img.shields.io/badge/chat-on%20slack-brightgreen)](https://slack.getodk.org)

ffem Collect is an Android app for filling out forms. It is integrated with ffem Water and ffem Soil.

ffem Collect is a fork of the Open Data Kit (ODK) Collect project, a free and open-source set of tools which help organizations author, field, and manage mobile data collection solutions. Learn more about the Open Data Kit project and its history [here](https://opendatakit.org/about/) and read about example ODK deployments [here](https://opendatakit.org/about/deployments/).

ffem Collect renders forms that are compliant with the [ODK XForms standard](https://getodk.github.io/xforms-spec/), a subset of the [XForms 1.1 standard](https://www.w3.org/TR/xforms/) with some extensions. The form parsing is done by the [JavaRosa library](https://github.com/getodk/javarosa) which Collect includes as a dependency.

Please note that the `master` branch reflects ongoing development and is not production-ready.

## Table of Contents
* [Learn more about ODK Collect](#learn-more-about-odk-collect)
* [Setting up your development environment](#setting-up-your-development-environment)

## Learn more about ODK Collect
* ODK website: [https://getodk.org](https://getodk.org)
* ODK Collect usage documentation: [https://docs.getodk.org/collect-intro/](https://docs.getodk.org/collect-intro/)
* ODK forum: [https://forum.getodk.org](https://forum.getodk.org)
* ODK developer Slack chat: [https://slack.getodk.org](https://slack.getodk.org)

## Setting up your development environment

1. Download and install [Git](https://git-scm.com/downloads) and add it to your PATH

1. Download and install [Android Studio](https://developer.android.com/studio/index.html) 

1. Fork the collect project ([why and how to fork](https://help.github.com/articles/fork-a-repo/))

1. Clone your fork of the project locally. At the command line:

        git clone https://github.com/YOUR-GITHUB-USERNAME/collect

    If you prefer not to use the command line, you can use Android Studio to create a new project from version control using `https://github.com/YOUR-GITHUB-USERNAME/collect`.

1. Use Android Studio to import the project from its Gradle settings. To run the project, click on the green arrow at the top of the screen.

1. Windows developers: continue configuring Android Studio with the steps in this document: [Developing ODK Collect on Windows](docs/WindowsDevSetup.md).

1. Make sure you can run unit tests by running everything under `collect_app/src/test/java` in Android Studio or on the command line:

    ```
    ./gradlew testDebug
    ```

1. Make sure you can run instrumented tests by running everything under `collect_app/src/androidTest/java` in Android Studio or on the command line:

    ```
    ./gradlew connectedAndroidTest
    ```
    **Note:** You can see the emulator setup used on CI in  `.circleci/config.yml`.
