import com.datastax.driver.core._
import com.hypertino.binders.cassandra
import com.hypertino.binders.cassandra.GuavaSessionQueryCache
import com.hypertino.inflector.naming.{Converter, SnakeCaseToCamelCaseConverter}
import com.hypertino.binders.cassandra._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}

import scala.reflect.runtime.universe._


class AlternativeQuery[C <: Converter : TypeTag](session: Session, preparedStatement: PreparedStatement)
  extends com.hypertino.binders.cassandra.Query[C](session, preparedStatement) {
  def this(session: Session, queryString: String) = this(session, session.prepare(queryString))

  override def createStatement(): cassandra.Statement[C] = new AlternativeStatement[C](session, new BoundStatement(preparedStatement))
}

class AlternativeSessionQueryCache[C <: Converter : TypeTag](session: Session) extends GuavaSessionQueryCache[C](session) {
  override protected def newQuery(query: String) = new AlternativeQuery[C](session, query)
}

class AlternativeStatement[C <: Converter : TypeTag](session: Session, boundStatement: BoundStatement)
  extends cassandra.Statement[C](session, boundStatement) {
}

class TestAlternativeSessionQueryCacheSpec extends FlatSpec with Matchers with ScalaFutures {
  "cql... " should " allow to use alternative SessionQueryCache implementation " in {
    Cassandra.start
    implicit var sessionQueryCache = new AlternativeSessionQueryCache[SnakeCaseToCamelCaseConverter.type](Cassandra.session)
    implicit val scheduler = monix.execution.Scheduler.Implicits.global
    val userCql = cql"select userId,name,created from users where userid=1".task.runAsync.futureValue
  }
}