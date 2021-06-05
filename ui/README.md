# Nepali CalendarView
![Maven Central](https://img.shields.io/maven-central/v/io.github.sapkotamadhusudan/np-calendar-ui?color=green)

A highly customizable calendar library for Android, powered by RecyclerView. Huge thanks to [CalendarView](https://github.com/kizitonwose/CalendarView) teams, this calendarView module is entirely based on the
[CalendarView](https://github.com/kizitonwose/CalendarView) which is rewritten with Nepali Calendar Core API

**With this library, your calendar will look however you want it to.**

## Features

- [x] [Single or range selection](#date-selection) - The library provides the calendar logic which enables you to implement the view whichever way you like.
- [x] [Week or month mode](#week-view-and-month-view) - show 1 row of weekdays, or any number of rows from 1 to 6.
- [x] [Disable desired dates](#disabling-dates) - Prevent selection of some dates by disabling them.
- [x] Boundary dates - limit the calendar date range.
- [x] Custom date view - make your day cells look however you want, with any functionality you want.
- [x] Custom calendar view - make your calendar look however you want, with whatever functionality you want.
- [x] [Custom first day of the week](#first-day-of-the-week) - Use any day as the first day of the week.
- [x] Horizontal or vertical scrolling mode.
- [x] [Month headers and footers](#adding-month-headers-and-footers) - Add headers/footers of any kind on each month.
- [x] Easily scroll to any date or month view using the date.
- [x] Use all RecyclerView customisations(decorators etc) since CalendarView extends from RecyclerView.

## Sample project

It's very important to check out the sample app. Most techniques that you would want to implement are already implemented in the examples.

View the sample app's source code [here](https://github.com/sapkotamadhusudan/nepali-calendar/tree/master/sample)

## Setup

The library uses `neapli-date` via [nepali-calendar](https://github.com/sapkotamadhusudan/nepali-calendar/tree/master/core) for nepali date compatibility.

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
	implementation 'io.github.sapkotamadhusudan:np-calendar-ui:<latest-version>'
}
```

You can find the latest version of `CalendarView` on the Bintray badge above the preview images.

## Usage

#### Step 1

Add CalendarView to your XML like any other view.

```xml
<com.maddy.calendar.ui.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    app:cv_dayViewResource="@layout/calendar_day_layout" />
```
See all available [attributes](#attributes).

Create your day view resource in `res/layout/calendar_day_layout.xml`.

```xml
<TextView
    android:id="@+id/calendarDayText"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:textSize="16sp"
    tools:text="22" />
```

Create your view container which acts as a view holder for each date cell.
The view passed in here is the inflated day view resource which you provided.

```kotlin
class DayViewContainer(view: View) : ViewContainer(view) {
    val textView = view.findViewById<TextView>(R.id.calendarDayText)

    // With ViewBinding
    // val textView = CalendarDayLayoutBinding.bind(view).calendarDayText
}
```

Provide a `DayBinder` for the CalendarView using your `DayViewContainer` type.

```kotlin
calendarView.dayBinder = object : DayBinder<DayViewContainer> {
    // Called only when a new container is needed.
    override fun create(view: View) = DayViewContainer(view)

    // Called every time we need to reuse a container.
    override fun bind(container: DayViewContainer, day: CalendarDay) {
        container.textView.text = day.date.dayOfMonth.toString()
    }
}
```

#### Step 2

Setup the desired dates in your Fragment or Activity:

```kotlin
val currentMonth = ILocalDate.now()
val firstMonth = currentMonth.plusMonths(-10)
val lastMonth = currentMonth.plusMonths(10)
val firstDayOfWeek = DayOfWeek.SUNDAY
calendarView.setup(firstMonth, lastMonth, firstDayOfWeek)
calendarView.scrollToMonth(currentMonth)
```

**And that's all you need for a simple usage!**

To add a header or footer to each month, the procedure is the same. Just provide your `monthHeaderResource` or `monthFooterResource` attribute, then set the `monthHeaderBinder` or `monthFooterBinder` property of the CalendarView.
For more complex usages, please see the sample project.

In the example above, we get the first day of the week from the current locale, however, we can use a specific day regardless of locale by passing in the value `DayOfWeek.SUNDAY`, `DayOfWeek.MONDAY` etc

### Attributes

#### XML (All prefixed `cv_` for clarity)

- **dayViewResource**: The xml resource that is inflated and used as the day cell view. This must be provided.

- **monthHeaderResource**: The xml resource that is inflated and used as a header for every month.

- **monthFooterResource**: The xml resource that is inflated and used as a footer for every month.

- **orientation**: The calendar orientation, can be `horizontal` or `vertical`. Default is `vertical`.

- **scrollMode**: The scrolling behavior of the calendar. Can be `paged` or `continuous`. If `paged`, the calendar will snap to the nearest month after a scroll or swipe action. Default value is `continuous`.

- **maxRowCount**: The maximum number of rows(1 to 6) to show on each month. If a month has a total of 6 rows and `maxRowCount` is set to 4, there will be two appearances of that month on the calendar, the first one will show 4 rows and the second one will show the remaining 2 rows. To show a week mode calendar, set this value to 1, you may also want to set `hasBoundaries` to false so dates can overflow into the previous/next month for a better experience.

- **hasBoundaries**: Determines if dates of a month should stay in its section or can flow into another month's section.
If `true`, a section can only contain dates belonging to that month, its inDates and outDates. if `false`, the dates are added continuously, irrespective of month sections.

    When this property is `false`, a few things behave slightly differently:
    - If `inDateStyle` is either `allMonths` or `firstMonth`, only the first index will contain inDates.
    - If `outDateStyle` is either `endOfRow` or `endOfGrid`, only the last index will contain outDates.
    - If `outDateStyle` is `endOfGrid`, outDates are generated for the last index until it satisfies the `maxRowCount` requirement.

- **inDateStyle**: This Determines how inDates are generated for each month on the calendar. If set to `allMonths`, the calendar will generate inDates for all months, if set to `firstMonth` inDates will be generated for the first month only and if set to `none`, inDates will not be generated, this means that there will be no offset on any month.

- **outDateStyle**: This determines how outDates are generated for each month on the calendar. If `endOfRow `, the calendar will generate outDates until it reaches the first end of a row. This means that if a month has 6 rows, it will display 6 rows and if a month has 5 rows, it will display 5 rows. However, if this value is set to `endOfGrid`, the calendar will generate outDates until it reaches the end of a 6 x 7 grid. This means that all months will have 6 rows.

If you are wondering what `outDates` and `inDates` mean, let's use the screenshot below as an example.

<img src="https://raw.githubusercontent.com/sapkotamadhusudan/nepali-calendar/master/images/screenshot_in_out_dates.png" alt="inDate and outDates" width="300">

In the image, the dates within the green annotation are `inDates`, the ones within the red annotation are `outDates` while those without annotation are `monthDates`. You can check for this when binding your calendar. To achieve the exact effect on the image, we do this:

```kotlin
calendarView.dayBinder = object : DayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)
    override fun bind(container: DayViewContainer, day: CalendarDay) {
        container.textView.text = day.date.dayOfMonth.toString()
        if (day.owner == DayOwner.THIS_MONTH) {
            container.textView.setTextColor(Color.WHITE)
        } else {
            container.textView.setTextColor(Color.GRAY)
        }
    }
}
```

`inDates` have their `owner` property set to `DayOwner.PREVIOUS_MONTH`

`outDates` have their `owner` property set to `DayOwner.NEXT_MONTH`

`monthDates` have their `owner` property set to `DayOwner.THIS_MONTH` as seen in the code snippet above.

#### Properties

All XML attributes are also available as properties of the CalendarView class via code. So in addition to those, we have:

- **monthScrollListener**: Called when the calendar scrolls to a new month. Mostly beneficial if `scrollMode` is `paged`.

- **dayBinder**: An instance of `DayBinder` for managing day cell views.

- **monthHeaderBinder**: An instance of `MonthHeaderFooterBinder` for managing header views.

- **monthFooterBinder**: An instance of `MonthHeaderFooterBinder` for managing footer views.

- **daySize**: The size, in pixels for each day cell view.

Note that setting the `daySize` property to `CalendarView.SIZE_SQUARE` makes the day cells have equal width and height which is basically the width of the calendar divided by 7. `SIZE_SQUARE` is the default size value.

#### Methods

- **scrollToDate(date: LocalDate)**: Scroll to a specific date on the calendar. Use `smoothScrollToDate()` to get a smooth scrolling animation.

- **scrollToMonth(month: ILocalDate)**: Scroll to a month on the calendar. Use `smoothScrollToMonth()` to get a smooth scrolling animation.

- **notifyDateChanged(date: ILocalDate)**: Reload the view for the specified date.

- **notifyMonthChanged(month: ILocalDate)**: Reload the header, body and footer views for the specified month.

- **notifyCalendarChanged()**: Reload the entire calendar.

- **findFirstVisibleMonth()** and **findLastVisibleMonth()**: Find the first and last visible months on the CalendarView respectively.

- **findFirstVisibleDay()** and **findLastVisibleDay()**: Find the first and last visible days on the CalendarView respectively.

- **setupAsync()**: Setup the CalendarView, *asynchronously*, useful if your `startMonth` and `endMonth` values are *many* years apart.

- **updateMonthRange()**: Update the CalendarView's `startMonth` and/or `endMonth` values after the initial setup. The currently visible month is preserved. Use `updateMonthRangeAsync()` to do this asynchronously.

- **updateMonthConfiguration()**: Update `inDateStyle`, `outDateStyle`, `maxRowCount` and `hasBoundaries` properties without generating the underlying calendar data repeatedly. Prefer this if setting more than one of these properties at the same time. Use `updateMonthConfigurationAsync()` to do this asynchronously.


There's no need to list all available methods or repeating the documentation here. Please see the [CalendarView](https://github.com/sapkotamadhusudan/nepali-calendar/blob/master/ui/src/main/java/com/maddy/calendar/ui/CalendarView.kt) class for all properties and methods available with proper documentation.

### Date clicks

You should set a click listener on the view which is provided to the view container.

XML file for the date cell `calendar_day_layout.xml`:

```xml
<!--We'll use this TextView to show the dates-->
<TextView
    android:id="@+id/calendarDayText"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:gravity="center"
    android:textSize="16sp"
    tools:text="22" />
```

Of course, you need to set the file as `cv_dayViewResource` on the CalendarView:

```xml
<com.maddy.calendar.ui.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:cv_dayViewResource="@layout/calendar_day_layout" />
```

Click listener implementation in your Fragment or Activity:

```kotlin
class DayViewContainer(view: View) : ViewContainer(view) {
    val textView = view.findViewById<TextView>(R.id.calendarDayText)
    // Will be set when this container is bound
    lateinit var day: CalendarDay

    init {
        view.setOnClickListener {
            // Use the CalendarDay associated with this container.
        }
    }
}

calendarView.dayBinder = object : DayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)
    override fun bind(container: DayViewContainer, day: CalendarDay) {
        // Set the calendar day for this container.
        container.day = day
        // Set the date text
        container.textView.text = Formatter.format(day.date, "dd")
        // Other binding logic
    }
}
```

### Date Selection

The library has no inbuilt concept of selected/unselected dates, this gives you the freedom to choose how best you would like to implement this use case.

Implementing date selection is as simple as showing a background on a specific date in the date binder, remember that since CalendarView is a RecyclerView, you need to undo any special effects on dates where it is not needed.

For this example, I want only the last clicked date to be selected on the calendar.

Firstly, let's keep a reference to the selected date:

```kotlin
private var selectedDate: ILocalDate? = null
```

Next, using the click logic already shown in the date click section above, we update this field whenever a date is clicked and show the selection background on the clicked date.

```kotlin
view.setOnClickListener {
    // Check the day owner as we do not want to select in or out dates.
    if (day.owner == DayOwner.THIS_MONTH) {
        // Keep a reference to any previous selection
        // in case we overwrite it and need to reload it.
        val currentSelection = selectedDate
        if (currentSelection == day.date) {
            // If the user clicks the same date, clear selection.
            selectedDate = null
            // Reload this date so the dayBinder is called
            // and we can REMOVE the selection background.
            calendarView.notifyDateChanged(currentSelection)
        } else {
            selectedDate = day.date
            // Reload the newly selected date so the dayBinder is
            // called and we can ADD the selection background.
            calendarView.notifyDateChanged(day.date)
            if (currentSelection != null) {
                // We need to also reload the previously selected
                // date so we can REMOVE the selection background.
                calendarView.notifyDateChanged(currentSelection)
            }
        }
    }
}
```

Lastly, we implement the `dayBinder` to reflect the selection accordingly:

```kotlin
calendarView.dayBinder = object : DayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)
    override fun bind(container: DayViewContainer, day: CalendarDay) {
        container.day = day
        val textView = container.textView
        textView.text = Formatter.format(day.date, "dd")
        if (day.owner == DayOwner.THIS_MONTH) {
            // Show the month dates. Remember that views are recycled!
            textView.visibility = View.VISIBLE
            if (day.date == selectedDate) {
                // If this is the selected date, show a round background and change the text color.
                textView.setTextColor(Color.WHITE)
                textView.setBackgroundResource(R.drawable.selection_background)
            } else {
                // If this is NOT the selected date, remove the background and reset the text color.
                textView.setTextColor(Color.BLACK)
                textView.background = null
            }
        } else {
            // Hide in and out dates
            textView.visibility = View.INVISIBLE
        }
    }
}
```

For more complex selection logic like range selection, please see the sample project. It's quite simple, the magic is all in your binding logic!

### Disabling dates

As expected, the library does not provide this logic internally so you have complete flexibility.

To disable dates, you can simply set the texts on those dates to look "disabled" and ignore clicks on those dates. For example, if we want to show in and out dates but "disable" them so that they cannot be selected, we can just set the alpha property for those dates in the `dayBinder` to give the effect of being disabled.

Continuing with the example in the date selection section, we already ignore clicks for in and out dates using this logic:

```kotlin
view.setOnClickListener {
    // Check the day owner as we do not want to select in or out dates.
    if (day.owner == DayOwner.THIS_MONTH) {
        // Only use month dates
    }
}
```

Then in the `dayBinder`, we check the day owner again and bind accordingly:

```kotlin
calendarView.dayBinder = object : DayBinder<DayViewContainer> {
    override fun create(view: View) = DayViewContainer(view)
    override fun bind(container: DayViewContainer, day: CalendarDay) {
        container.day = day
        val textView = container.textView
        textView.text = Formatter.format(day.date, "dd")
        textView.alpha = if (day.owner == DayOwner.THIS_MONTH) 1f else 0.3f
    }
}
```

And that's all you need to do. Of course you can go wild and do a whole lot more, see the sample project for some complex implementations.

### Adding month headers and footers

This is quite simple, just provide the needed values for `cv_monthHeaderResource` or `cv_monthFooterResource` in XML or programmatically. In the example shown below, we add a header which simply shows the month name above each month:

Create the header view in `res/layout/calendar_month_header_layout.xml`:

```xml
<TextView xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/headerTextView"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:padding="16dp"
    android:textSize="26sp"
    tools:text="October 2019" />
```

Set the view as the month header resource:

```xml
<com.maddy.calendar.ui.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:cv_dayViewResource="@layout/calendar_day_layout"
    app:cv_monthHeaderResource="@layout/calendar_month_header_layout" />
```

Finally, provide a month header binder in code:

```kotlin
class MonthViewContainer(view: View) : ViewContainer(view) {
    val textView = view.findViewById<TextView>(R.id.headerTextView)
}
calendarView.monthHeaderBinder = object : MonthHeaderFooterBinder<MonthViewContainer> {
    override fun create(view: View) = MonthViewContainer(view)
    override fun bind(container: MonthViewContainer, month: CalendarMonth) {
        container.textView.text = "${Formatter.format(month.yearMonth, "MMMM").toLowerCase().capitalize()} ${Formatter.format(month.yearMonth, "yyyy")}"
    }
}
```

The same logic applies if you need to add a footer.

### First day of the week

Here's a method which generates the weekdays from the user's current Locale.

```kotlin
fun daysOfWeekFromLocale(): Array<DayOfWeek> {
    val firstDayOfWeek = DayOfWeek.SUNDAY
    val daysOfWeek = DayOfWeek.weekDays(firstDayOfWeek)
    // Order `daysOfWeek` array so that firstDayOfWeek is at index 0.
    // Only necessary if firstDayOfWeek is not DayOfWeek.MONDAY which has ordinal 0.
    if (firstDayOfWeek != DayOfWeek.MONDAY) {
        val rhs = daysOfWeek.sliceArray(firstDayOfWeek.ordinal..daysOfWeek.indices.last)
        val lhs = daysOfWeek.sliceArray(0 until firstDayOfWeek.ordinal)
        return = rhs + lhs
    }
    return daysOfWeek
}
```

With the method above, you can set up the calendar so the first day of the week is what the user would expect. This could be Sunday, Monday or whatever the Locale returns:

```kotlin
val daysOfWeek = daysOfWeekFromLocale()
calendarView.setup(startMonth, endMonth, daysOfWeek.first())
```

Of course, this could be simplified as:

```kotlin
val firstDayOfWeek = DayOfWeek.SUNDAY
calendarView.setup(startMonth, endMonth, firstDayOfWeek)
```

However, you would typically use the `daysOfWeek` array values to set up the weekday texts in your month header view, this way it matches what is shown on the calendarView.

To use Sunday as the first day of the week, regardless of the user's Locale, use the below logic instead:

```kotlin
val daysOfWeek = arrayOf(
    DayOfWeek.SUNDAY,
    DayOfWeek.MONDAY,
    DayOfWeek.TUESDAY,
    DayOfWeek.WEDNESDAY,
    DayOfWeek.THURSDAY,
    DayOfWeek.FRIDAY,
    DayOfWeek.SATURDAY
)
calendarView.setup(startMonth, endMonth, daysOfWeek.first())
// Use the daysOfWeek to set up your month header texts:
// Sun | Mon | Tue | Wed | Thu | Fri | Sat
```

### Week view and Month view

This library has no concept of week/month view. You'll need to configure the calendar to mimic this behavior by changing its state between a 6 or 1 row calendar, depending on your needs. This feature can be seen in Example 1 in the sample app. In summary, here's what you need:

```xml
<!-- Common configurations for both modes. -->
<com.maddy.calendar.ui.CalendarView
    android:id="@+id/calendarView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:cv_orientation="horizontal"
    app:cv_outDateStyle="endOfRow"
    app:cv_inDateStyle="allMonths"
    app:cv_scrollMode="paged" />
```

```kotlin
val monthToWeek = monthViewCheckBox.isChecked
if (monthToWeek) {
    // One row calendar for week mode
    calendarView.updateMonthConfiguration(
        inDateStyle = InDateStyle.ALL_MONTHS,
        maxRowCount = 1,
        hasBoundaries = false
    )
} else {
    // Six row calendar for month mode
    calendarView.updateMonthConfiguration(
        inDateStyle = InDateStyle.FIRST_MONTH,
        maxRowCount = 6,
        hasBoundaries = true
    )
}
```

With the configuration above, you get the result below:

<img src="https://user-images.githubusercontent.com/15170090/59875600-100bd100-9399-11e9-8329-7c24944bb106.gif" alt="Week and month modes" width="250">

If you wish to animate height changes on the CalendarView when switching between week and month modes, please see Example 1 in the sample app where we use a `ValueAnimator`, of course you can use whichever animation logic you prefer.

You can also set `hasBoundaries` to `true` for a week mode calendar. This helps the library make very few optimizations, however, you should also change `scrollMode` to `ScrollMode.CONTINUOUS` as pagination behavior may not be as expected due to boundary limitations. See Example 7 in the sample app for a week mode calendar with this configuration, a screenshot is shown below:

<img src="https://user-images.githubusercontent.com/15170090/59904118-9f959c00-93fa-11e9-836d-2248f77130ac.png" alt="Week mode" width="260">

Remember that all the screenshots above are just examples of what you can achieve with this library and you can absolutely build your calendar to look however you want.

## FAQ

**Q**: How do I use this library in a Java project?

**A**: It works out of the box, however, the `MonthScrollListener` is not an interface but a Kotlin function. To set the `MonthScrollListener` in a Java project see [this](https://github.com/kizitonwose/CalendarView/issues/74).

**Q**: How do I disable user scrolling on the calendar so I can only scroll programmatically?

**A**: See [this](https://github.com/kizitonwose/CalendarView/issues/38#issuecomment-525786644).

**Q**: Why am I getting the same `YearMonth` value in the `CalendarMonth` passed into the `MonthScrollListener`?

**A**: This is because you have set `app:cv_hasBoundaries` to `false` in XML or have called `calendarView.hasBoundaries = false` in code. When this is set, the underlying `YearMonth` is undefined on all indices as each index could have multiple months depending on your `maxRowCount` value. If you need the month value with the `hasBoundaries = false` setting, you can get it from any of the `CalendarDay` values in the `CalendarMonth` class. You can always check if the first and last dates are from different months and act accordingly.

## Contributing

Found a bug? feel free to fix it and send a pull request or [open an issue](https://github.com/sapkotamadhusudan/nepali-calendar/issues).

## Inspiration

CalendarView was built from the CalendarView library [CalendarView](https://github.com/kizitonwose/CalendarView).

## License
CalendarView is distributed under the MIT license. See [LICENSE](https://github.com/sapkotamadhusudan/nepali-calendar/blob/master/LICENSE.md) for details.