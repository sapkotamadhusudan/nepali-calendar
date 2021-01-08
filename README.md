# Nepali Calendar

A Nepali Calendar used to manage nepali dates and also convert AD dates to nepali.

# Installation

The Nepali Calendar can be installed directly into your application by importing sdk artifacts via Gradle.

Add the following code to your project's **build.gradle** file:

```groovy
allprojects {
    repositories {
        maven {
            url "https://maven.pkg.github.com/sapkotamadhusudan/nepali-calendar"
        }
    }
}
```

And the following code to your **module's** `build.gradle` file:

```groovy
dependencies {
    implementation "com.maddy.calendar.core:0.0.1"
}
```