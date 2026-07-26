package com.wit.jasonfagerberg.nightsout.database

// Canonical DDL matches the Room entity schema (NOT NULL PKs, REAL for doubles).
// The adoption normalizer, the <40 migration and the pre-populated asset all converge on it.
const val CREATE_DRINKS_TABLE =
    "CREATE TABLE IF NOT EXISTS `drinks` (`id` TEXT NOT NULL, `name` TEXT, `abv` REAL," +
        " `amount` REAL, `measurement` TEXT, `recent` INTEGER, `modifiedTime` INTEGER," +
        " `dontSuggest` INTEGER, PRIMARY KEY(`id`))"
const val CREATE_LOG_TABLE =
    "CREATE TABLE IF NOT EXISTS `log` (`date` INTEGER NOT NULL, `bac` REAL, `duration` REAL," +
        " PRIMARY KEY(`date`))"

// These three tables have no primary key, so Room cannot manage them as entities.
// Their DDL is the original hand-written schema and must never change.
const val CREATE_CURRENT_SESSION_TABLE =
    "CREATE TABLE IF NOT EXISTS `current_session_drinks` (`drink_id` TEXT, `position` INTEGER)"
const val CREATE_FAVORITES_TABLE =
    "CREATE TABLE IF NOT EXISTS `favorites` (`drink_name` TEXT, `origin_id` TEXT)"
const val CREATE_LOG_DRINK_TABLE =
    "CREATE TABLE IF NOT EXISTS `log_drink` (`log_date` NUMERIC, `drink_id` TEXT)"

val CREATE_LEGACY_TABLES = listOf(
    CREATE_CURRENT_SESSION_TABLE,
    CREATE_FAVORITES_TABLE,
    CREATE_LOG_DRINK_TABLE
)
