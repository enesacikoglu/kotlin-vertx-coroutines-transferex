import io.vertx.core.DeploymentOptions
import io.vertx.core.Vertx
import io.vertx.core.VertxOptions
import io.vertx.kotlin.core.deployVerticleAwait

suspend fun main() {
  val vertx = Vertx.vertx(VertxOptions().apply {
    preferNativeTransport = true
  })
  try {
//    val deploymentOptions = DeploymentOptions().setInstances(Runtime.getRuntime().availableProcessors() * 2)
      //.setWorkerPoolSize(100)
      //.setWorker(true)
      //.setMultiThreaded(true)

    vertx.deployVerticleAwait("com.cengenes.transferex.AppVerticle")
    println("Application started")
  } catch (exception: Throwable) {
    println("Could not start application")
    exception.printStackTrace()
  }
}

