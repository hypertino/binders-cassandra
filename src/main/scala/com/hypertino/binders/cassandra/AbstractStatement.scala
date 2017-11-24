package com.hypertino.binders.cassandra

import java.util.concurrent.CancellationException

import scala.reflect.runtime.universe._
import com.datastax.driver.core.{Statement ⇒ DriverStatement, _}
import com.hypertino.inflector.naming.Converter
import monix.eval.Task
import monix.execution.Cancelable
import org.slf4j.LoggerFactory

import scala.util.{Failure, Success}


/**
  * statement have to be protected in general
  */
abstract class AbstractStatement[C <: Converter : TypeTag, S <: DriverStatement](val session: Session, protected val statement: S) {

  protected val logger = LoggerFactory.getLogger(getClass)

  def task: Task[Rows[C]] = {
    if (logger.isTraceEnabled) {
      logger.trace(queryString().trim)
    }

    Task.create[Rows[C]] { (scheduler, callback) =>
      val f = session.executeAsync(statement)
      val l = new Runnable with Cancelable {
        def run() = {
          scala.util.Try(new Rows[C](f.get())) match {
            case c @ Failure(e : CancellationException) ⇒ // do nothing if canceled
            case s : Success[_] ⇒ callback(s)
            case e : Failure[_] ⇒ callback(e)
          }
        }
        def cancel(): Unit = f.cancel(true)
      }
      f.addListener(l, scheduler)
      l
    }
  }

  def asNonIdempotent(): AbstractStatement[C, S] = {
    statement.setIdempotent(false)
    this
  }

  def asIdempotent(): AbstractStatement[C, S] = {
    statement.setIdempotent(true)
    this
  }

  def withConsistency(consistency: ConsistencyLevel): AbstractStatement[C, S] = {
    if (consistency != ConsistencyLevel.LOCAL_SERIAL && consistency != ConsistencyLevel.SERIAL) {
      statement.setConsistencyLevel(consistency)
    } else {
      statement.setSerialConsistencyLevel(consistency)
    }
    this
  }

  def withTimestamp(timestamp: Long): AbstractStatement[C, S] = {
    statement.setDefaultTimestamp(timestamp)
    this
  }

  protected def queryString(): String
}