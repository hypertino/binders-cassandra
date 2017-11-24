package com.hypertino.binders.cassandra

import com.datastax.driver.core.{BatchStatement ⇒ DriverBatchStatement, Statement ⇒ DriverStatement, _}
import com.hypertino.inflector.naming.Converter

import scala.reflect.runtime.universe._

class BatchStatement[C <: Converter : TypeTag](session: Session, val batchType: DriverBatchStatement.Type, statements: DriverStatement *)
  extends AbstractStatement[C, DriverBatchStatement](session, new DriverBatchStatement(batchType)) {
  import scala.collection.JavaConverters._

  statement.addAll(statements.asJava)

  override protected def queryString(): String = {
    val allQueryStrings = statement.getStatements.asScala.map {
      case b: BoundStatement ⇒ b.preparedStatement().getQueryString
      case s: SimpleStatement ⇒ s.getQueryString
    }
    s"""
       BEGIN $batchType BATCH
        ${allQueryStrings.mkString(";\n")}
       APPLY BATCH
     """
  }
}
