package xyz.vsl.wsimitator;

import xyz.vsl.wsimitator.imitator.Config;
import xyz.vsl.wsimitator.imitator.ResultType;
import xyz.vsl.wsimitator.imitator.WSIContext;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Map;

/**
 * Created by vsl on 13.05.2016.
 */
public class WSImitatorFilter implements Filter {
    @Override
    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        /*
        req.setCharacterEncoding("UTF-8");

        System.out.println(req.getMethod()+", contextPath="+req.getContextPath()+", servletPath="+req.getServletPath()+", requestURI="+req.getRequestURI()+", pathInfo="+req.getPathInfo()+", queryString="+req.getQueryString()+", pathTranslated="+req.getPathTranslated()+".");
        for (Enumeration<String> headerNames = req.getHeaderNames(); headerNames.hasMoreElements(); ) {
            String header = headerNames.nextElement();
            System.out.println("  h "+header+"="+req.getHeader(header)+";");
        }

        Map<String, String[]> parameterMap = req.getParameterMap();
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            System.out.println("  p "+entry.getKey()+"="+ Arrays.toString(entry.getValue()));
        }

        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                System.out.println("  c name="+cookie.getName()+", domain="+cookie.getDomain()+", path="+cookie.getPath()+", value="+cookie.getValue()+", maxAge="+cookie.getMaxAge()+", httpOnly="+cookie.isHttpOnly()+", secure="+cookie.getSecure()+", version="+cookie.getVersion());
            }
        }
        */

        Config[] configs;
        synchronized (Config.class) {
            configs = Config.getConfigs();
        }
        for (Config config : configs) {
            WSIContext context = new WSIContext(req, (HttpServletResponse)servletResponse, config.getBaseDirectory());
            ResultType result = config.getRequestProcessor().process(context);
            if (result == ResultType.stop)
                return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }
}
