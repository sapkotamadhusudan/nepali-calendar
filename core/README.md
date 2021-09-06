# Nepali Calendar
![Maven Central](https://img.shields.io/maven-central/v/io.github.sapkotamadhusudan/np-calendar-core?color=green)

A Nepali Calendar used to manage nepali dates and also convert AD dates to nepali.

## Setup

#### Step 1

Add the JitPack repository to your project level `build.gradle`:

```groovy
allprojects {
 repositories {
    mavenCentral()
 }
}
```

Add CalendarView to your app `build.gradle`:

```groovy
dependencies {
	implementation 'io.github.sapkotamadhusudan:np-calendar-core:<latest-version>'
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

val fromCalendar = ILocalDate.of(Calendar.getInstance(), ILocalDate.Type.BS)

val fromJavaTime = ILocalDate.of(LocalDate.now(), ILocalDate.Type.BS)
```

or 
Anno Domini(AD) date by

```
val currentADDate = ILocalDate.nowAD()

val providedADDate = ILocalDate.ofAD(2021, 1, 24)

val withType = ILocalDate.of(2077, 10, 11, ILocalDate.Type.AD)

val fromCalendar = ILocalDate.of(Calendar.getInstance(), ILocalDate.Type.AD)

val fromJavaTime = ILocalDate.of(LocalDate.now(), ILocalDate.Type.AD)
```


##### Date Conversion

###### AD To BS Date Conversion
```
val bsDateByReverse = ILocalDate.nowAD().reverse()

val bsDateFromAdILocalDate = ILocalDate.convert(ILocalDate.nowAD(), ILocalDate.Type.BS)

val bsDateFromYearMonthDay = ILocalDate.convertToBS(2021, 9, 5)
```

###### BS To AD Date Conversion
```
val adDateByReverse = ILocalDate.nowBS().reverse()

val adDateFromAdILocalDate = ILocalDate.convert(ILocalDate.nowBS(), ILocalDate.Type.AD)
```

##### Add or Subtract days, month, year

```
val currentBSDate = ILocalDate.nowBS()

// Add a day,month and year
var changed = currentBSDate
				.plusDays(1)
				.plusMonths(1)
				.plusYears(1)

// Subtract a day,month and year
var changed = currentBSDate
				.minusDays(1)
				.minusMonths(1)
				.minusYear(1)
```



##### Get Length Of Month

```
val currentBSDate = ILocalDate.nowBS()

var maxDaysInMonth = currentBSDate.lengthOfMonth
```


##### Get Length Of Year

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