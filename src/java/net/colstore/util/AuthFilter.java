package net.colstore.util;


import java.io.IOException;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.colstore.web.mbeans.LoginBean;

@WebFilter(filterName = "AuthFilter", urlPatterns = {"*.xhtml"})
public class AuthFilter implements Filter {
     
    public AuthFilter() {
    }
 
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
         
    }
 
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    	HttpServletRequest req =null;
        HttpServletResponse res = null;
    	 try { 
            // check whether session variable is set
             req = (HttpServletRequest) request;
             res = (HttpServletResponse) response;
            HttpSession ses = req.getSession(false);
            LoginBean loginBean=null;
            
            
            if(ses!=null&&ses.getAttribute("loginBean") != null){
            	loginBean=(LoginBean)ses.getAttribute("loginBean");
            }
            
            String reqURI = req.getRequestURI();
          //  logger.info("doFilter:: reqURI:: "+reqURI+",indexOf logout.xhtml: "+reqURI.indexOf("/logout.xhtml")+"ses: "+ses);
          //System.out.println("AuthFilter::doFilter::loginBean:: "+loginBean+",reqURI: "+reqURI+",ses:: "+ses+", index of: /faces/index.xhtml:: "+reqURI.indexOf("/faces/index.xhtml"));
            if( reqURI.indexOf("/faces/logout.xhtml") >= 0){    
                     
                        if(loginBean!=null&&ses!=null){            	       
                            ses.removeAttribute("loginBean");                      
            		}
                        
                        FacesContext ctx = FacesContext.getCurrentInstance();
                        ExternalContext extCtx = ctx.getExternalContext();
                   
                        Map<String, Object> sessionMap = extCtx.getSessionMap();
                        sessionMap.remove("loginBean");
                        ctx.release();
                        if(ses!=null)ses.invalidate(); 
                        System.out.println("AuthFilter::doFilter:: Session Invalidated!");
                        Cookie newCookie=new Cookie("JSESSIONID","SESSION-DESTROYED");
                        res.addCookie(newCookie);
            	 res.sendRedirect(req.getContextPath() + "/faces/index.xhtml"); 
            }else if ( reqURI.indexOf("/faces/index.xhtml") >= 0||loginBean!=null||reqURI.contains("javax.faces.resource")){
                System.out.println("AuthFilter::doFilter:: Valid Session") ;  
                String newsessionId="JCMS-SESSION-"+System.currentTimeMillis()+(Math.random()*999999);
                newsessionId=NUtil.getBase64EncodedString(newsessionId);
                Cookie newCookie=new Cookie("JSESSIONID",newsessionId);
                res.addCookie(newCookie);
                chain.doFilter(request, response);
            }else  {
                   System.out.println("AuthFilter::doFilter:: Anonymous user") ;
                   Cookie newCookie=new Cookie("JSESSIONID","SESSION-NOT-INITIATED");
                   res.addCookie(newCookie);
                   res.sendRedirect(req.getContextPath() + "/faces/index.xhtml");  // Anonymous user. Redirect to login page
            }
      }
     catch(Throwable t) {
    	 System.out.println("doFilter::"+t);
    	 res.sendRedirect(req.getContextPath() + "/faces/index.xhtml");
     }
    } //doFilter
 
    @Override
    public void destroy() {
         
    }
}