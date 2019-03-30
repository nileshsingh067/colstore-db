/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rocg.web.mbeans;

import java.sql.*;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import org.primefaces.event.RowEditEvent;
/**
 *
 * @author Nilesh Singh
 */
@ManagedBean
@SessionScoped
public class PortletManagerBean implements java.io.Serializable{

    /**
     * Creates a new instance of PortletManagerBean
     */String portletName;
     String portletsDescp;
      DBConnection dbConn;
      StatusMessage statusMsg;
      String selectedPortal;
        int loginId;
        int roleId;
        String loginName;
        portletData selectedPortlet;
        Map<String, String> portalList;
        ArrayList<portletData> viewPortletList;
    public PortletManagerBean() {
          fetchLoginDetails();
        dbConn=new DBConnection();
         statusMsg=new StatusMessage();
         this.portalList = new HashMap();
         viewPortletList=new ArrayList<>();
         selectedPortlet=new portletData();
             reloadData(true, true);
    }

    
    public void submit(){
        
        System.out.println("portal name ::: "+this.getPortletName());
         Connection conn=dbConn.connect();
         if(conn!=null){
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletManagerBean.class :: submit() :: Database Connected.");
                try{
                    if(this.getSelectedPortal().equals("null")||this.getSelectedPortal().equals(" ")){
                          statusMsg.setMessage("Please Select Portal First",StatusMessage.MSG_CLASS_ERROR); 
                    }
                    else if(this.getPortletName().isEmpty())
                    {
                          statusMsg.setMessage("Please Give a Appropriate Portlet Name",StatusMessage.MSG_CLASS_ERROR); 
                    }
                    else  if(this.getPortletsDescp().isEmpty())
                    {
                          statusMsg.setMessage("Please Give a Appropriate Portlet Desc",StatusMessage.MSG_CLASS_ERROR); 
                    }
                    else{
                    java.sql.Statement st=conn.createStatement();
                    String sql=" insert into tb_portlets (portlets_name,portlets_descp,portal_id,status,reg_date,last_update)values('"+this.getPortletName()+"','"+this.getPortletsDescp()+"',"+this.getSelectedPortal()+",1,now(),now());";
                      dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: createNew() :: Query : "+sql);
                    int status=st.executeUpdate(sql);
                    System.out.println("status :"+status);
                    if(status>0){
                         this.statusMsg.setMessage("Portlet Created successfully!", StatusMessage.MSG_CLASS_INFO);
                         reloadData(false, true);
                    }
                    st.close();
                    st=null;
                    }
         }catch(Exception e){
                   dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletManagerBean.class :: submit() :: Exception :" + e.getMessage());
                    statusMsg.setMessage("A process failed while creating portlet. Database Error!",StatusMessage.MSG_CLASS_ERROR);
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
            }else{
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletManagerBean.class :: submit() :: Database Connectivity Failed");
                statusMsg.setMessage("A process failed while creating portlet. Database Connectivity Issue.",StatusMessage.MSG_CLASS_ERROR);
            }
        
    }
    
    
       public void reloadPortletList(java.sql.Statement st){
        java.sql.ResultSet rs=null;
        viewPortletList.clear();
        try{
            String sql1="select portlets_id,portlets_name,portal_id,status from tb_portlets;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletManagerBean.class :: reloadPortletList() :: SQL : " + sql1);
            rs=st.executeQuery(sql1);
            portletData newUser=null;
            while(rs.next()){
                newUser=new portletData();
                newUser.setPortlet_id(rs.getInt("portlets_id"));
                newUser.setPortlet_name(rs.getString("portlets_name"));
                newUser.setPortal_id(rs.getInt("portal_id"));
                newUser.setStatus(rs.getInt("status"));
                viewPortletList.add(newUser);
                newUser=null;
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletManagerBean.class :: reloadPortletList() :: Portlet List (ItemCount=" + viewPortletList.size() + ") Reloaded. ");
        }catch(Exception e){
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletManagerBean.class :: reloadPortletList() :: Exception  "+e.getMessage());
        }finally{
            try{ if(rs!=null) rs.close();}catch(Exception ee){}
            rs=null;
        }
    }
    
    
    
    public String getPortletName() {
        return portletName;
    }

    public void setPortletName(String portletName) {
        this.portletName = portletName;
    }

    public String getPortletsDescp() {
        return portletsDescp;
    }

    public void setPortletsDescp(String portletsDescp) {
        this.portletsDescp = portletsDescp;
    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getSelectedPortal() {
        return selectedPortal;
    }

    public void setSelectedPortal(String selectedPortal) {
        this.selectedPortal = selectedPortal;
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

    public Map<String, String> getPortalList() {
        return portalList;
    }

    public void setPortalList(Map<String, String> portalList) {
        this.portalList = portalList;
    }

    public ArrayList<portletData> getViewPortletList() {
        return viewPortletList;
    }

    public void setViewPortletList(ArrayList<portletData> viewPortletList) {
        this.viewPortletList = viewPortletList;
    }

    public portletData getSelectedPortlet() {
        return selectedPortlet;
    }

    public void setSelectedPortlet(portletData selectedPortlet) {
        this.selectedPortlet = selectedPortlet;
    }
    
    
    
    
    public void fetchLoginDetails()
  {
    LoginBean loginBeanObj = null;
    try
    {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext extCtx = ctx.getExternalContext();
      Map<String, Object> sessionMap = extCtx.getSessionMap();
      loginBeanObj = (LoginBean)sessionMap.get("loginBean");
      setLoginId(loginBeanObj.getUserId());
      setRoleId(loginBeanObj.getRoleId());
      setLoginName(loginBeanObj.getUserName());
    }
    catch (Exception e)
    {
      this.dbConn.logUIMsg(2, 0, "PortletManagerBean.class :: fetchLoginDetails() :: Exception " + e.getMessage());
    }
    loginBeanObj = null;
  }  
    
    public void reloadPortalList(java.sql.Statement st)
  {
       Connection conn=dbConn.connect();
         if(conn!=null){
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletManagerBean.class :: submit() :: Database Connected.");
    try
    {
       
      String sql1 = "";
      if ((getRoleId() == 1) || (getRoleId() == 2)) {
        sql1 = "SELECT DISTINCT portal_name,portal_id FROM vw_portal_services;";
      } else {
        sql1 = "SELECT DISTINCT portal_name,portal_id FROM vw_portal_services WHERE user_id=" + getLoginId() + ";";
      }
      this.dbConn.logUIMsg(0, 2, "PortletManagerBean.class :: reloadPortalList() :: Query : " + sql1);
      ResultSet rs = st.executeQuery(sql1);
      this.portalList.clear();
      while (rs.next()) {
        this.portalList.put(rs.getString("portal_name"), rs.getString("portal_id"));
      }
      this.dbConn.logUIMsg(0, 2, "PortletManagerBean.class :: reloadPortalList() ::  Collection Size " + this.portalList.size());
      if (rs != null) {
        rs.close();
      }
      rs = null;
    }
    catch (Exception e)
    {
      this.dbConn.logUIMsg(2, 0, "PortletManagerBean.class :: reloadPortalList() :: Exception " + e.getMessage());
    }
  }
  }
    
       public void onEdit(RowEditEvent event) {
           portletData newPortlet=(portletData)event.getObject();
       dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletManagerBean.class :: onEdit() :: Update Portlet '" + newPortlet.getPortlet_name()+ "' at ID " + newPortlet.getPortlet_id());

       if(newPortlet!=null && newPortlet.getPortlet_id()>0){
           statusMsg.setMessage("Portlet Details sent for update into Registry!", StatusMessage.MSG_CLASS_INFO);
          // String sql1="update tb_cms_contents_portalmap set show_order="+actionObj.getShowOrder()+" where portal_id="+this.getSelectedPortalId()+" and hp_flag=1 and content_id="+actionObj.getContentId()+";";
           int rep=0;
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalHomeContentSchedulingBean.class :: onEdit() :: sql ");
           Connection conn=dbConn.connect();
           if(conn!=null){
               try{
                   Statement st=conn.createStatement();
                   rep=st.executeUpdate("");
                   statusMsg.setMessage("Database Updated successfully!", StatusMessage.MSG_CLASS_INFO);
                //   this.reloadPortalContentRecords(st);
               }catch(Exception e){
                   dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalHomeContentSchedulingBean.class :: onEdit() :: Exception " + e);
                    statusMsg.setMessage("Failed to update database.", StatusMessage.MSG_CLASS_ERROR);
               }finally{
                   try{if(conn!=null) conn.close();}catch(Exception ee){}
                   conn=null;
               }
           }
//           refreshList(newUser,2,true,false,false,false);
       }else{
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CountryBean.class :: onEdit() :: Invalid Portlet Id (" + newPortlet.getPortlet_id()+ ").");
           statusMsg.setMessage("Invalid Portlet Id `" + newPortlet.getPortlet_id()+ "`. Please select a Portlet to update.", StatusMessage.MSG_CLASS_ERROR);
       }
    
    }
    public void reloadData(boolean reloadPortal,boolean reloadPortlet){
    
     Connection conn=dbConn.connect();
       
        if(conn!=null){
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletManagerBean.class :: reloadData() :: Database Connected.");
                try{
                    Statement st=conn.createStatement();
                     if(reloadPortal) reloadPortalList(st);
                    if(reloadPortlet) reloadPortletList(st);
                  
                }catch(Exception e){
                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletManagerBean.class :: reloadData() :: A process failed while reloading processing request. Exception  :" + e.getMessage());
                    
                   
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
            }else{
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletManagerBean.class :: reloadData() :: Database Connectivity Failed");
                statusMsg.setMessage("A process failed while reloading Portlet  List. Database Connectivity Issue.",StatusMessage.MSG_CLASS_ERROR);
            }
    
    
    
    }


public class portletData{
    int portal_id;
    int portlet_id;
    String portlet_name;
    int status;

        public int getPortal_id() {
            return portal_id;
        }

        public void setPortal_id(int portal_id) {
            this.portal_id = portal_id;
        }

        public int getPortlet_id() {
            return portlet_id;
        }

        public void setPortlet_id(int portlet_id) {
            this.portlet_id = portlet_id;
        }

        public String getPortlet_name() {
            return portlet_name;
        }

        public void setPortlet_name(String portlet_name) {
            this.portlet_name = portlet_name;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    
}


}
