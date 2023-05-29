package nl.marcenschede.starters.akamaiidentitycloud

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

val fixedClockMay29 = Clock.fixed(Instant.parse("2023-05-29T21:16:30Z"), ZoneId.of("UTC"))