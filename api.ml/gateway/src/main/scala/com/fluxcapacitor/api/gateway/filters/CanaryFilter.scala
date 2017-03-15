package com.fluxcapacitor.api.gateway.filters

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient
import scala.collection.JavaConverters._
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper
import org.springframework.beans.factory.annotation.Autowired
import java.net.URI
import okhttp3.Headers
import okhttp3.internal.http.HttpMethod
import okhttp3.MediaType
import org.springframework.util.StreamUtils
import okhttp3.RequestBody
import okhttp3.Request
import okhttp3.OkHttpClient

//import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.IS_DISPATCHER_SERVLET_REQUEST_KEY

//
//  http://cloud.spring.io/spring-cloud-netflix/spring-cloud-netflix.html
//
class CanaryFilter extends ZuulFilter {  
  @Autowired
  val helper: ProxyRequestHelper = null

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

  val kubeClient: KubernetesClient = new DefaultKubernetesClient()

  @Override
  def run(): Object = {
    try{
  		val ctx = RequestContext.getCurrentContext()
      
  //    System.out.println(s"""Lists: ${kubeClient.lists()}""")
      
  //    System.out.println(s"""Services: ${kubeClient.services()}""")
      val beforeRouteHost = ctx.getRouteHost
  		println(s"""Before route host: ${beforeRouteHost}""")		
  		
      val predictionKeyValueService = kubeClient.services().withName("prediction-keyvalue").get()
  		println(s"""After route host: ${"blah1"}""")
  		println(s"""predictionKeyValueService:  ${predictionKeyValueService}""")
  		
      //println(predictionKeyValueService.getSpec)			
  		println(s"""After route host: ${"blah2"}""")
  		
  //		https://github.com/fabric8io/kubernetes-client		
  
  		val canaryServices = kubeClient.services().withLabel("canary").list() //.getItems()
      println(s"""canaryServices: ${canaryServices}""")
      
      val httpClient = new OkHttpClient.Builder().build()
  
  		val context = RequestContext.getCurrentContext()
  		val request = context.getRequest()
  
      println(s"""request: ${request}""")
  
  		var requestBody = null 
          
      // http://cloud.spring.io/spring-cloud-netflix/spring-cloud-netflix.html
  
  		val canaryUri = this.helper.buildZuulRequestURI(request)
      println(s"""canaryUri ${canaryUri}""")
      
  	  if (canaryServices.getItems != null && canaryServices.getItems.size > 0) {
    	  val canaryService = canaryServices.getItems().get(0)
    	  val canaryHost = canaryService.getSpec.getClusterIP
    	  val canaryPort = canaryService.getSpec.getPorts.get(0).getTargetPort.getIntVal
    		val canaryUrl = s"""http://${canaryHost}:${canaryPort}${canaryUri}"""
    
        println(s"""canaryUrl ${canaryUrl}""")
  
     		val method = request.getMethod()
        println(s"""method ${method}""")
     		
  	  	val headers = new Headers.Builder()
        println(s"""headers ${headers}""")
              
  		  val headerNames = request.getHeaderNames()
        println(s"""headerNames ${headerNames}""")
  		  if (headerNames != null) {
    		  while (headerNames.hasMoreElements()) {
    			  val name = headerNames.nextElement()
    			  val values = request.getHeaders(name)
    
    			  while (values.hasMoreElements()) {
    			  	val value = values.nextElement()
    			  	headers.add(name, value)
    			  }
    		  }
  		  }
  
    		val builder = new Request.Builder()
    				.headers(headers.build())
    				.url(canaryUrl)
    				.method(method, requestBody)
    				
    		val canaryRequest = builder.build()  		
    	  println(s"""canaryRequest ${canaryRequest}""")
    	  
    	  //val response = httpClient.newCall(canaryRequest).execute()
    	  //println(s"""Response: ${response}""")
  //	  
  //		  println(s"""Response code: ${response.code()}""")
  //		  println(s"""Response body: ${response.body()}""")
  	  }
    } catch {
       case e: Throwable => {
         e.printStackTrace()
       }
    }

    null
  }
}
