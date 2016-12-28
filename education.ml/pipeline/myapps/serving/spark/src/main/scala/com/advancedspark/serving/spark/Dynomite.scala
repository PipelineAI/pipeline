package com.advancedspark.serving.spark

import com.netflix.dyno.jedis._
import com.netflix.dyno.connectionpool.Host
import com.netflix.dyno.connectionpool.HostSupplier
import com.netflix.dyno.connectionpool.TokenMapSupplier
import com.netflix.dyno.connectionpool.impl.lb.HostToken
import com.netflix.dyno.connectionpool.exception.DynoException
import com.netflix.dyno.connectionpool.impl.ConnectionPoolConfigurationImpl
import com.netflix.dyno.connectionpool.impl.ConnectionContextImpl
import com.netflix.dyno.connectionpool.impl.OperationResultImpl
import com.netflix.dyno.connectionpool.impl.utils.ZipUtils

import scala.collection.JavaConversions._
import java.util.Collections
import java.util.Collection
import java.util.Set
import java.util.List

object Dynomite {
  val localhostHost = new Host("127.0.0.1", Host.Status.Up)
  val localhostToken = new HostToken(100000L, localhostHost)

  val localhostHostSupplier = new HostSupplier() {
    @Override
    def getHosts(): Collection[Host] = {
      Collections.singletonList(localhostHost)
    }
  }

  val localhostTokenMapSupplier = new TokenMapSupplier() {
    @Override
    def getTokens(activeHosts: Set[Host]): List[HostToken] = {
      Collections.singletonList(localhostToken)
    }

    @Override
    def getTokenForHost(host: Host, activeHosts: Set[Host]): HostToken = {
      return localhostToken
    }
  }

  val redisPort = 6379
  val client = new DynoJedisClient.Builder()
             .withApplicationName("pipeline")
             .withDynomiteClusterName("pipeline-dynomite")
             .withHostSupplier(localhostHostSupplier)
             .withCPConfig(new ConnectionPoolConfigurationImpl("localhostTokenMapSupplier")
                .withTokenSupplier(localhostTokenMapSupplier))
             .withPort(redisPort)
             .build()
}

