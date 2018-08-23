package io.github.crr0004.intervalme.database.analytics

import io.github.crr0004.intervalme.database.IntervalData
import io.github.crr0004.intervalme.database.IntervalRunProperties

interface IntervalAnalyticsDataSourceI {
    fun intevalFinish(interval: IntervalData)
    fun intevalFinishWithProperties(interval: IntervalData, groupProperties: IntervalRunProperties)
}