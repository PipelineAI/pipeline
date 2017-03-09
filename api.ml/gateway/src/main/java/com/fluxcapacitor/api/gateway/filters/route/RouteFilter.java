package com.fluxcapacitor.api.gateway.filters.route;

import com.netflix.zuul.ZuulFilter;
//import org.springframework.web.servlet.support.RequestContext;
//import javax.servlet.http.HttpServletRequest;
//
//  http://cloud.spring.io/spring-cloud-netflix/spring-cloud-netflix.html
//
public class RouteFilter extends ZuulFilter {  
//    @Autowired
//    private ProxyRequestHelper helper;

    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        //RequestContext ctx = RequestContext.getServletContext();
        //HttpServletRequest request = ctx.getRequest();

        System.out.println(String.format("%s request to %s", "foo", "bar"));
        
        return null;
    }
}
