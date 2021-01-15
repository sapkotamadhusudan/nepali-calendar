# Nepali Calendar
[ ![Download](https://api.bintray.com/packages/sapkotamadhusudan99/nepali-calendar/com.maddy.calendar:core/images/download.svg?version=0.0.3) ](https://bintray.com/sapkotamadhusudan99/nepali-calendar/com.maddy.calendar:core/0.0.3/link)

A Nepali Calendar used to manage nepali dates and also convert AD dates to nepali.

# Installation

The Nepali Calendar can be installed directly into your application by importing sdk artifacts via Gradle.

Add the following code to your project's **build.gradle** file:

```groovy
repositories {
	maven {
		url  "https://dl.bintray.com/sapkotamadhusudan99/nepali-calendar"
	}
}
```

And the following code to your **module's** `build.gradle` file:

```groovy
dependencies {
    implementation "com.maddy.calendar:core:0.0.3"
}
```