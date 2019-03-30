/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rocg.web.mbeans;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.StatusMessage;
import net.rocg.web.beans.OperatorCircle;

/**
 *
 * @author Rishi Tyagi
 */
@ManagedBean
@SessionScoped
public class OperatorReportsBean {
    DBConnection dbConn;
    String campaignId="0";
    String operatorId="4";
    String circleId="25";
    String serviceId="2";
     int loginId, roleId;String loginName;
     StatusMessage statusMsg;
     Map<String,String> campaignList;
    /**
     * Creates a new instance of OperatorReportsBean
     */
    public OperatorReportsBean() {
         statusMsg=new StatusMessage();
        dbConn=new DBConnection();
        campaignList=new HashMap<String,String>();
        refreshData(true,false);
    }
    
    
    public void refreshData(boolean refreshCampaign,boolean refreshReport){
      Connection conn=dbConn.connect();
        StringBuilder logMsg=new StringBuilder();
        
        if(conn!=null){
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorReportsBean.class :: refreshData() :: Database Connected.");
                try{
                    java.sql.Statement st=conn.createStatement();
                    if(refreshCampaign){
                        String sql1="SELECT campaign_id,campaign_name FROM tb_campaign_details WHERE op_id=4 AND service_id=2 ORDER BY campaign_name;";
                        java.sql.ResultSet rs=st.executeQuery(sql1);
                        campaignList.clear();
                        campaignList.put("0", "Portal/No Campaign");
                        while(rs.next()){
                            campaignList.put(rs.getString("campaign_id"), rs.getString("campaign_name"));
                        }
                        if(rs!=null) rs.close();
                        rs=null;
                    }
                        
                    if(refreshReport){
                        String sql1="SELECT report_date,package_id,price_point,subs_hits,subs_success,renewal_hits,renewal_success,unsub_hits,unsub_success from tb_reports  where report_date>= date(date_add(now(),INTERVAL -30 DAY)) order by report_date desc,package_id;";
                        java.sql.ResultSet rs=st.executeQuery(sql1);
                    }
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorReportsBean.class :: refreshData() :: Operator Circle List (ItemCount=) Reloaded. ");
                    st.close();
                    st=null;
                }catch(Exception e){
                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "OperatorReportsBean.class :: refreshData() :: Exception :" + e.getMessage());
                    statusMsg.setMessage("A process failed while reloading Operator Circle List. Database Error!",StatusMessage.MSG_CLASS_ERROR);
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
            }else{
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "OperatorReportsBean.class :: refreshData() :: Database Connectivity Failed");
                statusMsg.setMessage("A process failed while reloading Operator Circle List. Database Connectivity Issue.",StatusMessage.MSG_CLASS_ERROR);
            }  
    }

    public String getCampaignId() {
        return campaignId;
    }

    public void setCampaignId(String campaignId) {
        this.campaignId = campaignId;
    }

    public String getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(String operatorId) {
        this.operatorId = operatorId;
    }

    public String getCircleId() {
        return circleId;
    }

    public void setCircleId(String circleId) {
        this.circleId = circleId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public int getLoginId() {
        return loginId;
    }

    public void setLoginId(int loginId) {
        this.loginId = loginId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }
    
    public void fetchLoginDetails() {

        LoginBean loginBeanObj = null;
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ExternalContext extCtx = ctx.getExternalContext();
            Map<String, Object> sessionMap = extCtx.getSessionMap();
            loginBeanObj = (LoginBean) sessionMap.get("loginBean");
            this.setLoginId(loginBeanObj.getUserId());
            this.setRoleId(loginBeanObj.getRoleId());
            this.setLoginName(loginBeanObj.getUserName());
        } catch (Exception e) {

        }
        loginBeanObj = null;

    }
    
}
