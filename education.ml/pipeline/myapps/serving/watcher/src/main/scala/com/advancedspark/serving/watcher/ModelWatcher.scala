package com.advancedspark.serving.watcher

import better.files.ThreadBackedFileMonitor
import better.files.File
import java.nio.file.WatchEvent
import org.apache.commons.daemon._

trait ApplicationLifecycle {
  def start(): Unit
  def stop(): Unit
}

abstract class AbstractApplicationDaemon extends Daemon {
  def application: ApplicationLifecycle

  def init(daemonContext: DaemonContext) {}

  def start() = application.start()

  def stop() = application.stop()

  def destroy() = application.stop()
}

class ApplicationDaemon() extends AbstractApplicationDaemon {
  def application = new Application
}

object ServiceApplication extends App {
  val application = createApplication()

  def createApplication() = new ApplicationDaemon

  private[this] var cleanupAlreadyRun: Boolean = false

  def cleanup(){
    val previouslyRun = cleanupAlreadyRun
    cleanupAlreadyRun = true
    if (!previouslyRun) application.stop()
  }

  Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
    def run() {
      cleanup()
    }
  }))

  application.start()
}

class Application() extends ApplicationLifecycle {
  private[this] var started: Boolean = false

  private val applicationName = "Watcher"

  def start() {
    System.out.println(s"Starting $applicationName Service")

    if (!started) {
      val watcher = new ThreadBackedFileMonitor(File("/tmp/live-recommendations/spark-1.6.1/text-debug-only/streaming-mf/"), recursive = true) {
//      val watcher = new ThreadBackedFileMonitor(File("/root/pipeline/datasets/serving/"), recursive = true) {
        override def onCreate(file: File) = println(s"$file got created")
        override def onModify(file: File) = println(s"$file got modified")
        override def onDelete(file: File) = println(s"$file got deleted")
        override def onUnknownEvent(event: WatchEvent[_]) = println(event)
        override def onException(exception: Throwable) = println(exception)
      }

      System.out.println(watcher)

      watcher.start()
      
      started = true

      while (true) {
	Thread.sleep(Long.MaxValue)
      }
    }
  }

  def stop() {
    System.out.println(s"Stopping $applicationName Service")

    if (started) {
      //watcher.stop()
      started = false
    }
  }
}

//object ModelWatcher {
//  def main(args: Array[String]) {
//    val watcher = new ThreadBackedFileMonitor(File("/root/pipeline/datasets/serving/recommendations/"), recursive = true) {
//      override def onCreate(file: File) = println(s"$file got created")
//      override def onModify(file: File) = println(s"$file got modified")
//      override def onDelete(file: File) = println(s"$file got deleted")
//      override def onUnknownEvent(event: WatchEvent[_]) = println(event) 
//      override def onException(exception: Throwable) = println(exception)
//    }
//    watcher.start()
//  }
//}

