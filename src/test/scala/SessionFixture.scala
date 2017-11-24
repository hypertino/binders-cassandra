import java.util.Date

import com.datastax.driver.core.Session
import com.hypertino.binders.cassandra.{GuavaSessionQueryCache, SessionQueryCache}
import com.hypertino.inflector.naming.LowercaseConverter
import com.hypertino.binders.cassandra._
import org.cassandraunit.CassandraCQLUnit
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfter, Suite}

object Cassandra extends CassandraCQLUnit(new ClassPathCQLDataSet("bindersTest.cql","binders_test"), null, "127.0.0.1", 9142, 60000) {
  lazy val start = {
    before()
  }
}

trait SessionFixture extends BeforeAndAfter with ScalaFutures {
  this: Suite =>
  var session: Session = null
  implicit var sessionQueryCache: SessionQueryCache[LowercaseConverter.type] = null
  implicit val scheduler = monix.execution.Scheduler.Implicits.global

  val yesterday = {
    import java.util._
    val cal = Calendar.getInstance()
    cal.setTime(new Date())
    cal.add(Calendar.DATE, -11)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    cal.getTime
  }

  def createUser(id: Int, name: String, created: Date) = {
      cql"insert into users(userId, name, created) values ($id,$name,$created)".task.runAsync.futureValue
  }

  before {
    Cassandra.start
    session = Cassandra.session
    sessionQueryCache = new GuavaSessionQueryCache[LowercaseConverter.type](session)
    createUser(10, "maga", yesterday)
    createUser(11, "alla", yesterday)
  }

  after {
    //EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
    sessionQueryCache = null
  }

  import scala.reflect.runtime.universe._
}
