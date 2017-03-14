package com.fluxcapacitor.api.gateway.filters

import com.netflix.zuul.ZuulFilter
import io.fabric8.kubernetes.client.KubernetesClient
import io.fabric8.kubernetes.client.AutoAdaptableKubernetesClient
//import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants

//
//  http://cloud.spring.io/spring-cloud-netflix/spring-cloud-netflix.html
//
class BanditFilter extends ZuulFilter {  
  @Override
  def filterType(): String = {
    "pre"
  }

  @Override
  def filterOrder(): Int = {
    100
  }
  
  @Override
  def shouldFilter(): Boolean = {
    true
  }

  @Override
  def run(): Object = {
    val kubeClient: KubernetesClient = new AutoAdaptableKubernetesClient()
    
    System.out.println(s"""Lists: ${kubeClient.lists()}""")
    
    System.out.println(s"""Services: ${kubeClient.services()}""")
              
    null
  }
}
