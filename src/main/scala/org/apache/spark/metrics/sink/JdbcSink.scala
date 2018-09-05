/*
 * Copyright 2018 Cognite AS

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at

 *     http://www.apache.org/licenses/LICENSE-2.0

 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.metrics.sink

import java.util.{Locale, Properties}
import java.util.concurrent.TimeUnit

import com.codahale.metrics.{Metric, MetricFilter, MetricRegistry}
import org.apache.commons.dbcp2.BasicDataSource
import org.apache.spark.SecurityManager
import org.apache.spark.sql.execution.datasources.jdbc.DriverRegistry
import org.wso2.carbon.metrics.jdbc.reporter.JdbcReporter

class JdbcSink(val properties: Properties, val registry: MetricRegistry,
               securityManager: SecurityManager) extends Sink {
  val JDBC_KEY_PERIOD = "period"
  val JDBC_KEY_UNIT = "unit"
  val JDBC_KEY_URL = "url"
  val JDBC_KEY_USER = "user"
  val JDBC_KEY_PASSWORD = "password"
  // this string must be included in the metric name
  val JDBC_KEY_FILTER = "filter"
  val JDBC_KEY_SOURCE_NAME = "name"
  val JDBC_KEY_DRIVER_CLASS = "driver"

  val JDBC_DEFAULT_PERIOD = 10
  val JDBC_DEFAULT_UNIT = "SECONDS"

  def getProperty(property: String): Option[String] = Option(properties.getProperty(property))

  private val url = getProperty(JDBC_KEY_URL).getOrElse {
    throw new Exception(s"${JDBC_KEY_URL} must be set")
  }
  private val driver = getProperty(JDBC_KEY_DRIVER_CLASS).getOrElse {
    throw new Exception(s"${JDBC_KEY_DRIVER_CLASS} must be set")
  }
  private val pollPeriod = getProperty(JDBC_KEY_PERIOD) match {
    case Some(period) => period.toInt
    case None => JDBC_DEFAULT_PERIOD
  }
  private val pollUnit = getProperty(JDBC_KEY_UNIT) match {
    case Some(unit) => TimeUnit.valueOf(unit.toUpperCase(Locale.ROOT))
    case None => TimeUnit.valueOf(JDBC_DEFAULT_UNIT)
  }
  private val user = getProperty(JDBC_KEY_USER)
  private val password = getProperty(JDBC_KEY_PASSWORD)
  private val filter = getProperty(JDBC_KEY_FILTER)
  private val sourceName = getProperty(JDBC_KEY_SOURCE_NAME).getOrElse("JdbcSink")

  DriverRegistry.register(driver)
  val dataSource = new BasicDataSource
  dataSource.setDriverClassName(driver)
  dataSource.setUrl(url)
  user.foreach(dataSource.setUsername)
  password.foreach(dataSource.setPassword)

  val reporter: JdbcReporter = {
    val builder = JdbcReporter.forRegistry(registry)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .convertRatesTo(TimeUnit.SECONDS)
    for (f <- filter) {
      builder.filter(new MetricFilter {
        override def matches(name: String, metric: Metric): Boolean = name.contains(f)
      })
    }
    builder.build(sourceName, dataSource)
  }

  override def start(): Unit = {
    reporter.start(pollPeriod, pollUnit)
  }

  override def stop(): Unit = {
    reporter.stop()
  }

  override def report(): Unit = {
    reporter.report()
  }
}
