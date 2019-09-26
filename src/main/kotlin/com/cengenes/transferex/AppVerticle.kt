package com.cengenes.transferex

import io.vertx.ext.jdbc.JDBCClient
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.core.http.listenAwait
import io.vertx.kotlin.core.json.json
import io.vertx.kotlin.core.json.obj
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.coroutines.dispatcher
import io.vertx.kotlin.ext.sql.executeAwait
import io.vertx.kotlin.ext.sql.getConnectionAwait
import io.vertx.kotlin.ext.sql.queryAwait
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class AppVerticle : CoroutineVerticle() {

  private val LOGGER: Logger = LoggerFactory.getLogger(AppVerticle::class.java)

  private lateinit var client: JDBCClient

  override suspend fun start() {

    client = JDBCClient.createShared(vertx, json {
      obj(
        "url" to "jdbc:hsqldb:mem:test?shutdown=true",
        "driver_class" to "org.hsqldb.jdbcDriver",
        "max_pool_size-loop" to 30
      )
    })

    // Populate database
    val statements = listOf(
      "CREATE TABLE ACCOUNT (ID INTEGER IDENTITY PRIMARY KEY, NAME VARCHAR(256) NOT NULL,BALANCE INTEGER NOT NULL,CURRENCY VARCHAR(256) NOT NULL)",
      "INSERT INTO ACCOUNT (ID, NAME,BALANCE,CURRENCY) VALUES 1,'Enes',25000,'EUR'",
      "INSERT INTO ACCOUNT (ID, NAME,BALANCE,CURRENCY) VALUES 2,'Efe',21000,'TR'",
      "INSERT INTO ACCOUNT (ID, NAME,BALANCE,CURRENCY) VALUES 3,'Yunus',45000,'USD'"
    )
    client.getConnectionAwait()
      .use { connection -> statements.forEach { connection.executeAwait(it) } }

    // Build Vert.x Web router
    val router = Router.router(vertx)
    router.get("/api/accounts").coroutineHandler(this::getAllAccounts)

    // Start the server
    vertx.createHttpServer()
      .requestHandler(router)
      .listenAwait(config.getInteger("http.port", 8080))
  }

  suspend fun getAllAccounts(routingContext: RoutingContext) {
    val result = client.queryAwait("SELECT * FROM ACCOUNT")
    routingContext.response()
      .putHeader("content-type", "application/json; charset=utf-8")
      .end(result.results.toString())
    LOGGER.info("Get All Accounts ${result.results}")
  }

  /**
   * An extension method for simplifying coroutines usage with Vert.x Web routers
   */
  fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
    handler { ctx ->
      launch(ctx.vertx().dispatcher()) {
        try {
          fn(ctx)
        } catch (e: Exception) {
          ctx.fail(e)
        }
      }
    }
  }
}
