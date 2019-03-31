/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.colstore.web.mbeans;

import java.sql.PreparedStatement;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.colstore.util.DBConnection;
import net.colstore.util.RLogger;
import net.colstore.util.StringInputValidator;

/**
 *
 * @author Nilesh Kumar Singh
 */
@ManagedBean
@SessionScoped
public class LoginBean  implements java.io.Serializable{
    DBConnection dbConn;
    String userName;
    String password;
    boolean loginStatus;
    int userId;
    int roleId;
    String roleName;
    String homepage;
    String email;
    String mobile;
    String errMsg="";
    /**
     * Creates a new instance of LoginBean
     */
    public LoginBean() {
        dbConn=new DBConnection();
        loginStatus=false;
    }
    
    public String checkLogin(){
        loginStatus=false;
        this.setErrMsg("");
        String user=StringInputValidator.validateString(this.getUserName(), "<>&");
        String pwd=StringInputValidator.validateString(this.getPassword(), "<>&");
        if(!user.equalsIgnoreCase(this.getUserName()) || !pwd.equalsIgnoreCase(this.getPassword())){
            this.setErrMsg("Login Failed! Either UserName or Password contains illegal characters!");
        }else{
            java.sql.Connection conn=dbConn.connect();
            if(conn==null) conn=dbConn.connect();
            if(conn!=null){
                try{
                    String sql1="SELECT a.user_id,a.email,a.mobile,b.role_id,b.role_name,b.homepage FROM tb_users a, tb_user_roles b WHERE a.role_id=b.role_id AND a.status>0 AND b.status>0 AND a.user_name=? AND a.password=?;";
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "LoginBean.class :: checkLogin() :: sql1 : "+sql1);
                    PreparedStatement pst=conn.prepareStatement(sql1);
                    pst.setString(1, this.getUserName());
                    pst.setString(2, this.getPassword());
                    java.sql.ResultSet rs=pst.executeQuery();
                    if(rs.next()){
                        this.setUserId(rs.getInt("user_id"));
                        this.setEmail(rs.getString("email"));
                        this.setMobile(rs.getString("mobile"));
                        this.setRoleId(rs.getInt("role_id"));
                        this.setRoleName(rs.getString("role_name"));
                        this.setHomepage(rs.getString("homepage"));
                        this.setLoginStatus(true);
                    }
                    if(rs!=null) rs.close();
                    rs=null;
                }catch(Exception e){
                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "LoginBean.class :: checkLogin() :: Exception "+e);
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
            }else{

                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_DEBUG, "LoginBean.class :: checkLogin() :: Failed to connect Database.");
            }
        }
        
        if(this.isLoginStatus()){
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "LoginBean.class :: checkLogin() :: Login Successful.");
            setSessionExpiry(60);
            return "loginsuccess";
        }else{
            if(this.getErrMsg()==null || this.getErrMsg().length()<=0)
                this.setErrMsg("Login Request Failed! Please try again.");
            FacesMessage msg = new FacesMessage(this.getErrMsg(),"");
            FacesContext.getCurrentInstance().addMessage(null, msg);
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_INFO, "LoginBean.class :: checkLogin() :: Login Failed.");
            return "index";
        }
        
    }

    public String logout(){
        invalidatePrevSession();
        return "logout";
    }
   
    public void invalidatePrevSession(){
        try{
             FacesContext ctx = FacesContext.getCurrentInstance();
            ExternalContext extCtx = ctx.getExternalContext();
            extCtx.getSessionMap().remove("loginBean");
            //extCtx.getSessionMap().put("loginBean", null);
            
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();
            System.out.println("LoginBean :: Session Destroyed");
        }catch(Exception e){
            System.out.println("LoginBean ::invalidatePrevSession():: Exception "+e);
        }
        System.gc();
    }
    
    public void setSessionExpiry(int min){
         FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extCtx = ctx.getExternalContext();
        extCtx.getSessionMap().put("loginBean", this);
        extCtx.setSessionMaxInactiveInterval(min*60);
    }
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isLoginStatus() {
        return loginStatus;
    }

    public void setLoginStatus(boolean loginStatus) {
        this.loginStatus = loginStatus;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getRoleId() {
        return roleId;
    }

    public String getErrMsg() {
        return errMsg;
    }

    public void setErrMsg(String errMsg) {
        this.errMsg = errMsg;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getHomepage() {
        return homepage;
    }

    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
}
