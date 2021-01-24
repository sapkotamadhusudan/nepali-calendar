# Nepali Calendar
[ ![Download](https://api.bintray.com/packages/sapkotamadhusudan99/nepali-calendar/com.maddy.calendar:core/images/download.svg?version=0.0.5) ](https://bintray.com/sapkotamadhusudan99/nepali-calendar/com.maddy.calendar:core/0.0.5/link)

A Nepali Calendar used to manage nepali dates and also convert AD dates to nepali.

## Setup

#### Step 1

Add the JitPack repository to your project level `build.gradle`:

```groovy
allprojects {
 repositories {
    maven { url  "https://dl.bintray.com/sapkotamadhusudan99/nepali-calendar" }
 }
}
```

Add CalendarView to your app `build.gradle`:

```groovy
dependencies {
	implementation 'com.maddy.calendar:core:<latest-version>'
}
```

You can find the latest version of `nepali-calendar-core` on the Bintray badge above the preview images.

## Usage

#### Step 1

##### Create Instance
Create current Bikaram Sambat(BS) date by

```
val currentBSDate = ILocalDate.nowBS()

val providedBSDate = ILocalDate.ofBS(2077, 10, 11)

val withType = ILocalDate.of(2077, 10, 11, ILocalDate.Type.BS)
```

or 
Anno Domini(AD) date by

```
val currentADDate = ILocalDate.nowAD()

val providedADDate = ILocalDate.ofAD(2021, 1, 24)

val withType = ILocalDate.of(2077, 10, 11, ILocalDate.Type.AD)
```

##### Add or Substract days, month, year

```
val currentBSDate = ILocalDate.nowBS()

// Add a day,month and year
var changed = currentBSDate
				.plusDay(1)
				.plusMonth(1)
				.plusYear(1)

// Substract a day,month and year
var changed = currentBSDate
				.plusDay(-1)
				.plusMonth(-1)
				.plusYear(-1)
```



##### Get Lenght Of Month

```
val currentBSDate = ILocalDate.nowBS()

var maxDaysInMonth = currentBSDate.lengthOfMonth
```


##### Get Lenght Of Year

```
val currentBSDate = ILocalDate.nowBS()

var maxDaysInYear = currentBSDate.lengthOfYear
```


##### Get Days Passed in Selected Date Year

```
val currentBSDate = ILocalDate.nowBS()
// it will provides the total number of days till today of this year
var dayOfYear = currentBSDate.dayOfYear
```



## Contributing

Found a bug? feel free to fix it and send a pull request or [open an issue](https://github.com/sapkotamadhusudan/nepali-calendar/issues).


## License
CalendarView is distributed under the MIT license. See [LICENSE](https://github.com/sapkotamadhusudan/nepali-calendar/blob/master/LICENSE.md) for details.