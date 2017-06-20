package eu.inn.binders.cassandra

import scala.reflect.runtime.universe._
import com.datastax.driver.core.ResultSet
import com.hypertino.binders.core.Deserializer
import com.hypertino.inflector.naming.Converter

class Rows[C <: Converter : TypeTag](val resultSet: ResultSet) extends Deserializer[C] {
  import scala.collection.JavaConversions._

  def fieldName: Option[String] = None

  def isNull = false

  def iterator(): Iterator[Row[C]] = resultSet.iterator().map(r => new Row[C](r))

  def wasApplied: Boolean = resultSet.wasApplied()
}
