package com.hypertino.binders.cassandra

import scala.reflect.runtime.universe._
import com.datastax.driver.core.{BoundStatement, PreparedStatement, Session}
import com.hypertino.inflector.naming.Converter
import org.slf4j.LoggerFactory


class Query[C <: Converter : TypeTag](val session: Session, val preparedStatement: PreparedStatement) {

  //type C = C

  def this(session: Session, queryString: String) = this(session, QueryImpl.prepareStatement(session, queryString))

  def createStatement(): Statement[C] = new Statement[C](session, new BoundStatement(preparedStatement))
}

private [cassandra] object QueryImpl {
  protected val logger = LoggerFactory.getLogger(getClass)

  def prepareStatement(session: Session, queryString: String): PreparedStatement = {
    if (logger.isTraceEnabled) {
      logger.trace(s"Preparing statement: $queryString")
    }
    session.prepare(queryString)
  }
}