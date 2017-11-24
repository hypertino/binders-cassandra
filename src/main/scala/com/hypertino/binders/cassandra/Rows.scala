package com.hypertino.binders.cassandra

import com.datastax.driver.core.ResultSet
import com.hypertino.binders.core.Deserializer
import com.hypertino.inflector.naming.Converter

import scala.reflect.runtime.universe._

class Rows[C <: Converter : TypeTag](val resultSet: ResultSet) extends Deserializer[C] {
  import scala.collection.JavaConverters._

  def fieldName: Option[String] = None

  def isNull = false

  def iterator(): Iterator[Row[C]] = resultSet.iterator().asScala.map(r => new Row[C](r))

  def wasApplied: Boolean = resultSet.wasApplied()
}
