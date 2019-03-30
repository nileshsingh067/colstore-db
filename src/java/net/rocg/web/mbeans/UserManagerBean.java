/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rocg.web.mbeans;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import net.rocg.util.StringInputValidator;
import net.rocg.web.beans.CMSPortal;
import net.rocg.web.beans.CMSUser;
import net.rocg.web.beans.CMSUserPortalAccess;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author Rishi Tyagi
 */
@ManagedBean
@RequestScoped
public class UserManagerBean  implements java.io.Serializable{
     DBConnection dbConn;
     CMSUser selectedUser,newUser;
     ArrayList<CMSUser> userList;
     StatusMessage statusMsg;
     Map<String,String> registeredCompanyList;
     Map<String,String> userRoleList;
     Map<String,String> portalList;
     String selectedUserRole,registeredCompany,allowedPortal;
     
    /**
     * Creates a new instance of UserManagerBean
     */
    public UserManagerBean() {
        registeredCompanyList=new HashMap<>();
        userRoleList=new HashMap<>();
        portalList=new HashMap<>();
        selectedUser=new CMSUser();
        newUser=new CMSUser();
        userList=new ArrayList<>();
        dbConn=new DBConnection();
        statusMsg=new StatusMessage();
        refreshList(null,0,true,true,true,true);
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: MBean Connected.");
    }
    
    public void refreshList(CMSUser actionObj,int action,boolean reloadUserList,boolean refreshCompanyList,boolean refreshUserRoleList,boolean refreshPortalList){
         Connection conn=dbConn.connect();
       
        if(conn!=null){
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: refreshList() :: Database Connected.");
                try{
                    Statement st=conn.createStatement();
                    if(action==1){
                        //Create New
                        String sql1="INSERT INTO tb_users(role_id,user_name,PASSWORD,email,mobile,STATUS,created_on,last_update,company_id) VALUES("
                                + actionObj.getUserRoleId()+",'"+actionObj.getUserName()+"','"+actionObj.getPassword()+"','"+actionObj.getEmail()+"',"
                                + "'"+actionObj.getMobile()+"',1,now(),now(),"+actionObj.getCompanyId()+");";
                        
                        int rep=st.executeUpdate(sql1);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: refreshList() :: Action Requested=CREATE-NEW-USER; SQL : " + sql1 + "| DB Result=" + rep);
                        int userId=0;
                        if(rep>0){
                            
                            sql1 = "Select @@Identity";
                            java.sql.ResultSet rs = st.executeQuery(sql1);
                            if (rs.next())
                            {
                              userId = rs.getInt(1);
                            }
                            if(rs!=null) rs.close();
                            rs=null;
                        }
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: refreshList() :: New User ID  : " + userId);
                        if(userId>0){
                            sql1="INSERT INTO tb_users_portal_access(user_id,role_id,portal_id,access_start_date,access_end_date,use_date_check,STATUS) VALUES("
                                    + userId +","+actionObj.getUserRoleId()+","+actionObj.getPortalAccessDetails().get(0).getPortalId()+",now(),date_add(now(),INTERVAL 1 YEAR),0,1);";
                            rep=st.executeUpdate(sql1);
                            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: refreshList() :: Action Requested=NEW-USER-GRANT-ACCESS; SQL : " + sql1 + "| DB Result=" + rep);
                            
                        }
                        
                        if(rep>0 && userId>0){
                            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: refreshList() :: Action Requested=CREATE-NEW-USER Status : User Created Successfully!");
                            statusMsg.setMessage("New User Created against User Id "+userId,StatusMessage.MSG_CLASS_INFO);
                        }else{
                            if(userId<=0){
                            //User not created
                                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: refreshList() :: Action Requested=CREATE-NEW-USER Status : Failed to create new user "+actionObj.getUserName());
                                statusMsg.setMessage("Failed to create new user "+actionObj.getUserName(),StatusMessage.MSG_CLASS_ERROR);
                            }else{
                                //User Created by Portal Access may not be granted
                                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: refreshList() :: Action Requested=CREATE-NEW-USER Status : User Created against user id ("+userId+") but portal access may not be granted properly");
                                statusMsg.setMessage("User Created against user id ("+userId+") but portal access may not be granted properly",StatusMessage.MSG_CLASS_ERROR);
                            }
                        }
                    }else if(action==2){
                        //Update
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: refreshList() :: Action Requested=UPDATE-USER : Action Not Permitted/Enabled.");
                    }
                    
                    if(refreshCompanyList) reloadCompanyList(st);
                    if(refreshUserRoleList) reloadUserRoleList(st);
                    if(reloadUserList) reloadUserList(st);
                    if(refreshPortalList) reloadPortalList(st);
                    
                    if(st!=null) st.close();
                    st=null;
                }catch(Exception e){
                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: refreshList() :: A process failed while reloading processing request. Exception  :" + e.getMessage());
                    
                    if(action==1)
                        statusMsg.setMessage("A process failed while creating new User Account. Database Error! Exception :"+e.getMessage(),StatusMessage.MSG_CLASS_ERROR);
                    else if(action==2)
                        statusMsg.setMessage("A process failed while updating user Record. Database Error! Exception :"+e.getMessage(),StatusMessage.MSG_CLASS_ERROR);
                    else
                        statusMsg.setMessage("A process failed while reloading CMS User List. Database Error!",StatusMessage.MSG_CLASS_ERROR);
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
            }else{
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: refreshList() :: Database Connectivity Failed");
                statusMsg.setMessage("A process failed while reloading CMS User List. Database Connectivity Issue.",StatusMessage.MSG_CLASS_ERROR);
            }
      
    }
    
    public void createNew(){
       
        int portalId=RUtil.strToInt(getAllowedPortal(), 0);
        newUser.setCompanyId(RUtil.strToInt(getRegisteredCompany(), 0));
        newUser.setUserRoleId(RUtil.strToInt(getSelectedUserRole(), 0));
        CMSUserPortalAccess paccess=new CMSUserPortalAccess();
        paccess.setPortalId(portalId);
        newUser.setUserName((newUser.getUserName()==null)?"":newUser.getUserName().trim());
        newUser.setPassword((newUser.getPassword()==null)?"":newUser.getPassword().trim());
        newUser.setVerifyPassword((newUser.getVerifyPassword()==null)?"":newUser.getVerifyPassword().trim());
        newUser.setEmail((newUser.getEmail()==null)?"":newUser.getEmail().trim());
        newUser.setMobile((newUser.getMobile()==null)?"":newUser.getMobile().trim());
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: createNew() :: "
                + "New User Name to Register '" + newUser.getUserName() + "', Role '"+newUser.getUserId()+"', Company '"+newUser.getCompanyId()+"',"
                + "Portal "+portalId);

        ArrayList<CMSUserPortalAccess> portalAccess=new ArrayList<>();
        portalAccess.add(paccess);
        newUser.setPortalAccessDetails(portalAccess);
        String pV=StringInputValidator.validateString(newUser.getUserName(), "<>&@#$!`'^*?/=");
        String pDV=StringInputValidator.validateString(newUser.getPassword(), "<>&`'^*?/=");
        String pDV1=StringInputValidator.validateString(newUser.getVerifyPassword(), "<>&`'^*?/=");
        if(!pV.equalsIgnoreCase(newUser.getUserName()) || !pDV.equalsIgnoreCase(newUser.getPassword()) || !pDV1.equalsIgnoreCase(newUser.getVerifyPassword())){
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ServiceManagerBean.class :: createNew() :: Invalid Service Name ");
           statusMsg.setMessage("Sorry! Invalid Input. Special Characters are not allowed.",StatusMessage.MSG_CLASS_ERROR);
       }else if(newUser.getUserRoleId()<=0){
            //Invalid User Role
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: createNew() :: Invalid User Role `" + newUser.getUserRoleId() + "`.");
            statusMsg.setMessage("Invalid User Role! Please select a valid User Role and try again.",StatusMessage.MSG_CLASS_ERROR);
        }else if(newUser.getCompanyId()<=0){
            //Invalid Company id
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: createNew() :: Invalid Company Id `" + newUser.getCompanyId() + "`.");
            statusMsg.setMessage("Invalid Company name! User Account must be associated with a registered company on the platform.",StatusMessage.MSG_CLASS_ERROR);
        }else if(portalId<=0){
            //Invalid Portal Id
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: createNew() :: Invalid Portal Id `" + portalId + "`.");
            statusMsg.setMessage("Invalid Portal name! Please select a valid Portal Name you would like this account to be associated with.",StatusMessage.MSG_CLASS_ERROR);
        }else if(newUser.getUserName().length()<3 || newUser.getUserName().length()>50){
            //Invalid User Name
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: createNew() :: Invalid User Name `" + newUser.getUserName() + "`. User Name must be between 3-50 char in length.");
            statusMsg.setMessage("Invalid User Name! Login Id must be between 3-50 char in length.",StatusMessage.MSG_CLASS_ERROR);
        }else if(newUser.getPassword().length()<5 || newUser.getPassword().length()>50){
            //Invalid Password
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: createNew() :: Invalid Password `" + newUser.getPassword() + "`. Password must be between 5-50 char in length.");
            statusMsg.setMessage("Invalid Password! Password must be between 5-50 char in length.",StatusMessage.MSG_CLASS_ERROR);
        }else if(!newUser.getPassword().equals(newUser.getVerifyPassword())){
            //Verify Password does not match with Password
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: createNew() :: Password Verification failed. Password `" + newUser.getPassword() + "` does not match with `"+newUser.getVerifyPassword()+"`.");
            statusMsg.setMessage("Passowrd verification failed! Password must match with the verify passowrd.",StatusMessage.MSG_CLASS_ERROR);
        }else if(newUser.getEmail().length()<3 || newUser.getEmail().length()>50 || newUser.getEmail().indexOf("@")<=0){
            //Invalid Email
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: createNew() :: Invalid Email `" + newUser.getEmail() + "`. Password must be between 5-50 char in length and must contain @.");
            statusMsg.setMessage("Invalid Email id! Please provide a valid email address between 3-50 char in length.",StatusMessage.MSG_CLASS_ERROR);
        }else if(newUser.getMobile().length()<10){
            //Invalid Mobile Number
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: createNew() :: Invalid Mobile `" + newUser.getMobile() + "`. Mobile number must be between 10-12 digits in length without '+' sign.");
            statusMsg.setMessage("Invalid Contact Number. Contact number must contain minimum 10 digits",StatusMessage.MSG_CLASS_ERROR);
        }else{
            //All parameters received, Insert new Record
            statusMsg.setMessage("User Account sent for registration.",StatusMessage.MSG_CLASS_INFO);
            refreshList(newUser,1,true,false,false,false);
        }
    }
    
    /**
     *
     * @param event
     */
    
    public void onEdit(RowEditEvent event) {
           CMSUser newUser=(CMSUser)event.getObject();
       dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: onEdit() :: Update User '" + newUser.getUserName() + "' at ID " + newUser.getUserId());

       if(newUser!=null && newUser.getUserId()>0){
           statusMsg.setMessage("User Details sent for update into Registry!", StatusMessage.MSG_CLASS_INFO);
           refreshList(newUser,2,true,false,false,false);
       }else{
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CountryBean.class :: onEdit() :: Invalid User Id (" + newUser.getUserId() + ").");
           statusMsg.setMessage("Invalid User Id `" + newUser.getUserId() + "`. Please select a User to update.", StatusMessage.MSG_CLASS_ERROR);
       }
    
    }
    
     
    public void reloadUserList(java.sql.Statement st){
        java.sql.ResultSet rs=null;
        userList.clear();
        try{
            String sql1="SELECT U.user_id,U.user_name,U.password,U.email,U.mobile,U.status,U.role_id,UR.role_name,U.company_id,UC.company_name FROM tb_users U,tb_user_roles UR,tb_registered_companies UC WHERE U.`role_id`=UR.`role_id` AND U.`company_id`=UC.`company_id` ORDER BY U.`user_name`;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: reloadUserList() :: SQL : " + sql1);
            rs=st.executeQuery(sql1);
            CMSUser newUser=null;
            while(rs.next()){
                newUser=new CMSUser();
                newUser.setUserId(rs.getInt("user_id"));
                newUser.setUserName(rs.getString("user_name"));
                newUser.setPassword(rs.getString("password"));
                newUser.setEmail(rs.getString("email"));
                newUser.setMobile(rs.getString("mobile"));
                newUser.setStatus(rs.getInt("status"));
                newUser.setUserRoleId(rs.getInt("role_id"));
                newUser.setUserRole(rs.getString("role_name"));
                newUser.setCompanyId(rs.getInt("company_id"));
                newUser.setCompanyName(rs.getString("company_name"));
                userList.add(newUser);
                newUser=null;
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: reloadUserList() :: User List (ItemCount=" + userList.size() + ") Reloaded. ");
        }catch(Exception e){
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: reloadUserList() :: Exception  "+e.getMessage());
        }finally{
            try{ if(rs!=null) rs.close();}catch(Exception ee){}
            rs=null;
        }
    }
    
    public void reloadUserRoleList(java.sql.Statement st){
        java.sql.ResultSet rs=null;
        userRoleList.clear();
        try{
            String sql1="SELECT role_id,role_name FROM tb_user_roles WHERE STATUS>0 AND role_id>1 ORDER BY role_name;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: reloadUserRoleList() :: SQL : " + sql1);
            rs=st.executeQuery(sql1);
            while(rs.next()){
                userRoleList.put(rs.getString("role_name"), rs.getString("role_id"));
            }
            
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: reloadUserRoleList() :: User Role List (ItemCount=" + userRoleList.size() + ") Reloaded. ");
        }catch(Exception e){
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: reloadUserRoleList() :: Exception  "+e.getMessage());
        }finally{
            try{ if(rs!=null) rs.close();}catch(Exception ee){}
            rs=null;
        }
    }
    
            
    public void reloadCompanyList(java.sql.Statement st){
        java.sql.ResultSet rs=null;
        registeredCompanyList.clear();
        try{
            String sql1="SELECT company_id,company_name FROM tb_registered_companies WHERE STATUS>0 ORDER BY company_name;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: reloadCompanyList() :: SQL : " + sql1);
            rs=st.executeQuery(sql1);
            while(rs.next()){
                registeredCompanyList.put(rs.getString("company_name"), rs.getString("company_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: reloadCompanyList() :: Registered Company List (ItemCount=" + registeredCompanyList.size() + ") Reloaded. ");
        }catch(Exception e){
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: reloadCompanyList() :: Exception  "+e.getMessage());
        }finally{
            try{ if(rs!=null) rs.close();}catch(Exception ee){}
            rs=null;
        }
    }
    public void reloadPortalList(java.sql.Statement st){
        java.sql.ResultSet rs=null;
        portalList.clear();
        try{
            String sql1="SELECT portal_id,portal_name FROM tb_portals WHERE STATUS>0 ORDER BY portal_name;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: reloadPortalList() :: SQL : " + sql1);
            rs=st.executeQuery(sql1);
            CMSPortal portalObj=null;
            while(rs.next()){
                portalList.put(rs.getString("portal_name"), rs.getString("portal_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "UserManagerBean.class :: reloadPortalList() :: Portal List (ItemCount=" + portalList.size() + ") Reloaded. ");
        }catch(Exception e){
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "UserManagerBean.class :: reloadPortalList() :: Exception  "+e.getMessage());
        }finally{
            try{ if(rs!=null) rs.close();}catch(Exception ee){}
            rs=null;
        }
    }
    
    public CMSUser getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(CMSUser selectedUser) {
        this.selectedUser = selectedUser;
    }

    public ArrayList<CMSUser> getUserList() {
        return userList;
    }

    public void setUserList(ArrayList<CMSUser> userList) {
        this.userList = userList;
    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }

    public Map<String, String> getRegisteredCompanyList() {
        return registeredCompanyList;
    }

    public void setRegisteredCompanyList(Map<String, String> registeredCompanyList) {
        this.registeredCompanyList = registeredCompanyList;
    }

    public Map<String, String> getUserRoleList() {
        return userRoleList;
    }

    public void setUserRoleList(Map<String, String> userRoleList) {
        this.userRoleList = userRoleList;
    }

    public String getSelectedUserRole() {
        return selectedUserRole;
    }

    public void setSelectedUserRole(String selectedUserRole) {
        this.selectedUserRole = selectedUserRole;
    }

    public String getRegisteredCompany() {
        return registeredCompany;
    }

    public void setRegisteredCompany(String registeredCompany) {
        this.registeredCompany = registeredCompany;
    }

    public CMSUser getNewUser() {
        return newUser;
    }

    public void setNewUser(CMSUser newUser) {
        this.newUser = newUser;
    }

    public Map<String, String> getPortalList() {
        return portalList;
    }

    public void setPortalList(Map<String, String> portalList) {
        this.portalList = portalList;
    }

    public String getAllowedPortal() {
        return allowedPortal;
    }

    public void setAllowedPortal(String allowedPortal) {
        this.allowedPortal = allowedPortal;
    }

    
    
    
}
