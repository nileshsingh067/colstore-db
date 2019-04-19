/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.colstore.web.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import net.colstore.dao.ColListDAO;
import net.colstore.dao.DbListDAO;
import net.colstore.dao.TblListDAO;
import net.colstore.util.DBConnection;
import net.colstore.util.RLogger;
import net.colstore.web.mbeans.LoginBean;
import static net.colstore.web.mbeans.RMenuBuilder.getSessionObject;
import net.colstore.web.model.ColList;
import net.colstore.web.model.DbList;
import net.colstore.web.model.Node;
import net.colstore.web.model.TableList;
import org.primefaces.model.CheckboxTreeNode;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author nilesh
 */
@ManagedBean(name = "nodeService")
@ApplicationScoped
public class NodeService {
     DBConnection dbConn;
    int userId;
    int roleId;
    String roleName;
    String userName;
    private ColListDAO colListDAO;
    private DbListDAO dbListDAO;
    private TblListDAO tblListDAO;
     Map<String, String> dblist;
    
    
    public NodeService(){
        dbConn=new DBConnection();
        colListDAO=new ColListDAO();
        dbListDAO=new DbListDAO();
        tblListDAO=new TblListDAO(); dblist = new HashMap<String, String>();
        
        boolean loginStatus=fetchLoginDetails();
    }
    public TreeNode createDocuments() {
        fetchLoginDetails();
        createDbList();
         System.out.println("NodeService  :: createDocuments");
        TreeNode root = new DefaultTreeNode(new Node("Files", "-", "Folder","#"), null);
        //dynamic db list
        List<DbList> dbList=dbListDAO.getDbList(this.getUserId());
        for(DbList db:dbList){
             TreeNode Nodes = new DefaultTreeNode(new Node(db.getDb_name(), "-", "DB","#"), root);
             List<TableList> tblList=tblListDAO.getTblList(db.getId());
             for(TableList table:tblList){
                 TreeNode nodesTable = new DefaultTreeNode(new Node(table.getTbl_name(), "-", "Table","#"), Nodes);
                 List<ColList> colList=colListDAO.getColumnList(table.getId());
                 for(ColList column:colList){
                     TreeNode nodesColumn = new DefaultTreeNode(new Node(column.getCol_name(), "-", "Column","#"), nodesTable);
                 }
                 nodesTable=null;
             }
             TreeNode CreateNewTable = new CheckboxTreeNode(new Node("Create New Table", "-", "Table","/colstore-db/faces/ui/config/tblmgr.xhtml"), Nodes);
        }
        TreeNode CreateNewDb = new CheckboxTreeNode(new Node("Create New DB", "-", "DB","/colstore-db/faces/ui/config/dbmgr.xhtml"), root);
        return root;
    }

    public TreeNode createCheckboxDocuments() {
        fetchLoginDetails();
         System.out.println("NodeService  :: createDocuments");
        TreeNode root = new CheckboxTreeNode(new Node("Files", "-", "Folder","#"), null);
        //dynamic db list
        List<DbList> dbList=dbListDAO.getDbList(this.getUserId());
        
        
        for(DbList db:dbList){
             TreeNode Nodes = new CheckboxTreeNode(new Node(db.getDb_name(), "-", "Folder","#"), root);
             List<TableList> tblList=tblListDAO.getTblList(db.getId());
             for(TableList table:tblList){
                 TreeNode nodesTable = new CheckboxTreeNode(new Node(table.getTbl_name(), "-", "Folder","#"), Nodes);
                 List<ColList> colList=colListDAO.getColumnList(table.getId());
                 for(ColList column:colList){
                     TreeNode nodesColumn = new CheckboxTreeNode(new Node(column.getCol_name(), "-", "Word Document","#"), nodesTable);
                 }
                 nodesTable=null;
             }
             TreeNode CreateNewTable = new CheckboxTreeNode(new Node("Create New", "-", "Folder","/faces/ui/config/usermgr.xhtml"), Nodes);
        }
        TreeNode CreateNewDb = new CheckboxTreeNode(new Node("Create New", "-", "Folder","/faces/ui/config/usermgr.xhtmls"), root);
        return root;
    }
     public void createDbList() {
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "NodeService.class :: createDbList() :: loginBean {User Id=" + this.getUserId() + ",UserName=" + this.getUserName() + ",RoleId=" + this.getRoleId() + ",RoleName=" + this.getRoleName() + "}");
        DbListDAO dbdao = new DbListDAO();
        List<DbList> db = dbdao.getDbList(this.getUserId());
        dblist.clear();
        for (DbList newDB : db) {
            dblist.put(newDB.getDb_name(), newDB.getDb_name());
        }
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "NodeService.class :: createDbList() :: " + dblist.size());
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
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "NodeService.class :: fetchLoginDetails() :: loginBean {User Id="+this.getUserId()+",UserName="+this.getUserName()+",RoleId="+this.getRoleId()+",RoleName="+this.getRoleName()+", LoginStatus="+flag+"}");
            System.out.println(":: fetchLoginDetails :: isLoginStatus :: "+flag);
        }catch(Exception e){
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "NodeService.class :: fetchLoginDetails() :: Exception while accessing Login Info from Session, Exception  : "+e.getMessage());
        }
        loginBeanObj=null;
        return flag;
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

    public Map<String, String> getDblist() {
        return dblist;
    }

    public void setDblist(Map<String, String> dblist) {
        this.dblist = dblist;
    }
  
    
}
