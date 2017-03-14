package com.fluxcapacitor.api.gateway.filters

import com.netflix.zuul.ZuulFilter
import com.netflix.zuul.context.RequestContext

import io.fabric8.kubernetes.client.DefaultKubernetesClient
import io.fabric8.kubernetes.client.KubernetesClient

//import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants.IS_DISPATCHER_SERVLET_REQUEST_KEY

//
//  http://cloud.spring.io/spring-cloud-netflix/spring-cloud-netflix.html
//
class CanaryFilter extends ZuulFilter {  
//  @Autowired
//  val helper: ProxyRequestHelper

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
		val ctx = RequestContext.getCurrentContext();
    
//    System.out.println(s"""Lists: ${kubeClient.lists()}""")
    
//    System.out.println(s"""Services: ${kubeClient.services()}""")
		
//		https://github.com/fabric8io/kubernetes-client
		
		kubeClient.services().withLabel("canary") 
      
		//(!ctx.containsKey("forward.to") // a filter has already forwarded
	  //	&& !ctx.containsKey("service.id")) // a filter has already determined serviceId
  
  //HttpServletRequest request = ctx.getRequest();
	//if (request.getParameter("foo") != null) {
	//    // put the serviceId in `RequestContext`
  // 		ctx.put(SERVICE_ID_KEY, request.getParameter("foo"));
			// or ctx.setRouteHost(url)
  //	}
  //    return null;  		
		
  //  System.out.println(s"""${"foo"} request to ${"bar"}""");
      
    null
  }
}
