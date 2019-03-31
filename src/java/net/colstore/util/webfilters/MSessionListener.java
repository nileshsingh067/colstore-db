/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.colstore.util.webfilters;

/**
 *
 * @author JSS
 */

import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import net.colstore.web.mbeans.LoginBean;

public class MSessionListener implements HttpSessionListener {
	@Override
	public void sessionCreated(HttpSessionEvent se) {
		System.out.println("MSessionListener :: SESSION CRTEATED");
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent se) {
		System.out.println(" MSessionListener :: SESSION DESTROYED LISTENER");

		HttpSession session = se.getSession();

		finalizeUserSessionLog(session);
	}

	/**
	 * Utility method to finalize user's session log entry. Returns early if the
	 * session log isn't found or more than one is returned.
	 * 
	 * @param session
	 */
	private void finalizeUserSessionLog(HttpSession ses) {
  
		try{
		LoginBean loginBean=null;
            
            
                    if(ses!=null&&ses.getAttribute("loginBean") != null){
                        loginBean=(LoginBean)ses.getAttribute("loginBean");
                    }
                     if(loginBean!=null&&ses!=null){  
                         loginBean.invalidatePrevSession();
                        ses.removeAttribute("loginBean");                      
                    }

                    FacesContext ctx = FacesContext.getCurrentInstance();
                    ExternalContext extCtx = ctx.getExternalContext();

                    Map<String, Object> sessionMap = extCtx.getSessionMap();
                    sessionMap.remove("loginBean");
                    ctx.release();
                    if(ses!=null)ses.invalidate(); 
                    System.out.println("MSessionListener::finalizeUserSessionLog():: Session Invalidated!");
            	
		//session.invalidate();
		}catch(Exception ex){
			
		}
		System.gc();
	}
}
