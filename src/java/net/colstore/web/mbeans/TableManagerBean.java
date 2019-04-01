/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.colstore.web.mbeans;

import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.colstore.util.DBConnection;
import net.colstore.util.RLogger;
import net.colstore.web.model.DbList;
import net.colstore.web.model.Node;
import net.colstore.web.service.NodeService;
import org.primefaces.model.TreeNode;

/**
 *
 * @author nilesh
 */
@ManagedBean
@SessionScoped
public class TableManagerBean {
    
     DBConnection dbConn;
    DbList selectedDb;
    String msg;
    int userId;
    int roleId;
    String roleName;
    String userName;
     private TreeNode root;
      private TreeNode selectedNode;
      private String coldetails;
      private String tableName;
     
    @ManagedProperty("#{nodeService}")
    private NodeService service;
    

    public TableManagerBean(){
      dbConn=new DBConnection();
        selectedDb=new DbList();
        boolean loginStatus=fetchLoginDetails();
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "TableManagerBean.class :: MBean Connected.");
    }
    
    public void createTable(){
        System.out.println("createTable() :: "+this.getColdetails());
       // Node n=(Node)this.getSelectedNode().getData();
        //System.out.println("createTable() :: "+n.toString());;
        this.setMsg("Done");
        this.setTableName("");
        this.setColdetails("");
    }
     public boolean fetchLoginDetails(){
        boolean flag=false;
        LoginBean loginBeanObj=null;
        try{
            loginBeanObj=(LoginBean)getSessionObject("loginBean");
            //dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "RMenuBuilder.class :: fetchLoginDetails() :: loginBean : "+loginBeanObj);
            this.setRoleId(loginBeanObj.getRoleId());
            this.setUserId(loginBeanObj.getUserId());
            this.setRoleName(loginBeanObj.getRoleName());
            this.setUserName(loginBeanObj.getUserName());
            flag=loginBeanObj.isLoginStatus();
            if(flag) loginBeanObj.setSessionExpiry(10);//Session Expiry in Minutes
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "RMenuBuilder.class :: fetchLoginDetails() :: loginBean {User Id="+this.getUserId()+",UserName="+this.getUserName()+",RoleId="+this.getRoleId()+",RoleName="+this.getRoleName()+", LoginStatus="+flag+"}");
            System.out.println(":: fetchLoginDetails :: isLoginStatus :: "+flag);
        }catch(Exception e){
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "TableManagerBean.class :: fetchLoginDetails() :: Exception while accessing Login Info from Session, Exception  : "+e.getMessage());
        }
        loginBeanObj=null;
        return flag;
    }
    public static Object getSessionObject(String objName) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extCtx = ctx.getExternalContext();
       // extCtx.setSessionMaxInactiveInterval(3600);
        Map<String, Object> sessionMap = extCtx.getSessionMap();
        return sessionMap.get(objName);
    }

    public DbList getSelectedDb() {
        return selectedDb;
    }

    public void setSelectedDb(DbList selectedDb) {
        this.selectedDb = selectedDb;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
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

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public TreeNode getRoot() {
        return root;
    }

    public void setRoot(TreeNode root) {
        this.root = root;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public NodeService getService() {
        return service;
    }

    public void setService(NodeService service) {
        this.service = service;
    }

    public String getColdetails() {
        return coldetails;
    }

    public void setColdetails(String coldetails) {
        this.coldetails = coldetails;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

   
    
}

