import org.joda.time.{DateTime, DateTimeZone}
import org.joda.time.format.{DateTimeFormat, DateTimeFormatter}

import java.util.Locale

val locale = new Locale("cy", "GB")
val timeZone: DateTimeZone = DateTimeZone.forID("Europe/London")
val formatter: DateTimeFormatter = DateTimeFormat.forPattern("dd MMMM yyyy").withLocale(locale).withZone(timeZone)

println(DateTime.now().toString(formatter))