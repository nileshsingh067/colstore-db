/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.colstore.util;

import java.text.SimpleDateFormat;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.colstore.web.mbeans.LoginBean;

/**
 *
 * @author Nilesh Kumar Singh
 */
public class RLogger {
    
    public static final int LOGGING_LEVEL_ERROR=0;
    public static final int LOGGING_LEVEL_INFO=1;
    public static final int LOGGING_LEVEL_DEBUG=2;
    
    public static final int MSG_TYPE_INFO=0;
    public static final int MSG_TYPE_WARNING=1;
    public static final int MSG_TYPE_ERROR=2;
    
    int loggingType;
    SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    public RLogger(){
        loggingType=LOGGING_LEVEL_DEBUG;
    }
    
    public void logUIMsg(int msgType,int msgLevel,String msg){
        String logLevel=(msgLevel==LOGGING_LEVEL_DEBUG)?"DEBUG":(msgLevel==LOGGING_LEVEL_ERROR)?"ERROR":"INFO";
        if(msgLevel<=loggingType){
            String msgHead=(msgType>=MSG_TYPE_ERROR)?"ERROR":(msgType>=MSG_TYPE_WARNING)?"WARNING":"PROCESS";
          writeMsg("["+sdf.format(new java.util.Date())+"] :: "+fetchLoginDetails()+" :: ["+logLevel+"-"+msgHead+"] :: "+msg);
        }
    }
    
    public void logMsg(int msgType,int msgLevel,String msg){
        String logLevel=(msgLevel==LOGGING_LEVEL_DEBUG)?"DEBUG":(msgLevel==LOGGING_LEVEL_ERROR)?"ERROR":"INFO";
        if(msgLevel<=loggingType){
            String msgHead=(msgType>=MSG_TYPE_ERROR)?"ERROR":(msgType>=MSG_TYPE_WARNING)?"WARNING":"INFO";
          writeMsg("["+sdf.format(new java.util.Date())+"] :: PORTAL-LOGS :: [" + logLevel + "-" + msgHead + "] :: "+msg);
        }
    }
    public void writeMsg(String msg){
        System.out.println(msg);
    }
    public int getLoggingType() {
        return loggingType;
    }

    public void setLoggingType(int loggingType) {
        this.loggingType = loggingType;
    }
    
    public String fetchLoginDetails() {
        String loginDetails="";
        LoginBean loginBeanObj = null;
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ExternalContext extCtx = ctx.getExternalContext();
            Map<String, Object> sessionMap = extCtx.getSessionMap();
            loginBeanObj = (LoginBean) sessionMap.get("loginBean");
            loginDetails = "User Details{UserId="+loginBeanObj.getUserId()+";UserName="+loginBeanObj.getUserName()+";RoleId="+loginBeanObj.getRoleName()+"}";
        } catch (Exception e) {
            loginDetails="User Details{NEW-USER}";
        }
        loginBeanObj = null;
        return loginDetails;
    }

}
