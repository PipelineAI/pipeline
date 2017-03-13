package com.fluxcapacitor.api.gateway.filters.route

import com.netflix.zuul.ZuulFilter
//
//  http://cloud.spring.io/spring-cloud-netflix/spring-cloud-netflix.html
//
class CanaryRoutingFilter extends ZuulFilter {  
    @Override
    def filterType(): String = {
      "post"
    }

    @Override
    def filterOrder(): Int = {
      1
    }
    
    @Override
    def shouldFilter(): Boolean = {
      true
    }

    @Override
    def run(): Object = {
      System.out.println(s"""${"foo"} request to ${"bar"}""");
        
      null
    }
}
