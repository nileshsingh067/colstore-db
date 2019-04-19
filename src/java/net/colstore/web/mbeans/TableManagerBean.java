/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.colstore.web.mbeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.colstore.dao.ColListDAO;
import net.colstore.dao.DbListDAO;
import net.colstore.dao.TblListDAO;
import net.colstore.file.processor.DBHandler;
import net.colstore.util.DBConnection;
import net.colstore.util.RLogger;
import net.colstore.web.model.ColList;
import net.colstore.web.model.DbList;
import net.colstore.web.model.Node;
import net.colstore.web.model.TableList;
import net.colstore.web.service.NodeService;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author nilesh
 */
@ManagedBean
@SessionScoped
public class TableManagerBean implements java.io.Serializable {

    Map<String, String> dblist;
    List<ColList> columnList;
    List<String> dataTypeList;
    DBConnection dbConn;
    DbList selectedDb;
    TableList newTable;
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

    public TableManagerBean() {
        dbConn = new DBConnection();
        columnList = new ArrayList<>();
        dblist = new HashMap<String, String>();
        selectedDb = new DbList();
        newTable = new TableList();
        selectedNode = new DefaultTreeNode();
        dataTypeList = new ArrayList<>();
        dataTypeList.add("INT");
        dataTypeList.add("STRING");
        boolean loginStatus = fetchLoginDetails();
        this.setMsg("");
        createDbList();
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "TableManagerBean.class :: MBean Connected.");
    }

    public void createNewTable() {
        dbConn.logMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "TableManagerBean.class :: createNew() :: Table-" + this.getSelectedDb().getDb_name() + " | " + this.getNewTable().getTbl_name());
        if (this.getSelectedDb().getDb_name() != null && this.getNewTable().getTbl_name() != null) {
            TableList temp = new TableList();
            
            fetchLoginDetails();
            this.setMsg("Created !! ");
            //this.getColumnList().clear();
            System.out.println("net.colstore.web.mbeans.TableManagerBean.createNewTable() :: " + this.getColumnList().size());
            DbListDAO dbdao = new DbListDAO();
            this.setSelectedDb(dbdao.findDBListbyDBName(this.getSelectedDb().getDb_name()));
            temp.setDb_id(this.getSelectedDb().getId());
            temp.setTbl_name(this.getNewTable().getTbl_name().replaceAll(" ", ""));
            temp.setNo_of_col(0);
            DBHandler dbHandler = new DBHandler();
            if (dbHandler.createTable(this.getUserId(), this.getSelectedDb().getDb_name(), temp.getTbl_name())) {
                TblListDAO tbldao = new TblListDAO();
                boolean flag = tbldao.createNewTable(temp);
                TableList temp2=tbldao.getTblDetails(temp.getTbl_name());
                for (ColList c : this.getColumnList()) {
                    ColListDAO coldao=new ColListDAO();
                    c.setTbl_id(temp2.getId());
                    coldao.createNewColumn(c);
                    dbHandler.createColumn(this.getUserId(), this.getSelectedDb().getDb_name(), temp.getTbl_name(), c.getCol_name());
                    System.out.println("net.colstore.web.mbeans.TableManagerBean.createNewTable() :: " + c.toString());
                }
                boolean s_flag=dbHandler.buildSchema(this.getUserId(), this.getSelectedDb().getDb_name(), temp.getTbl_name(),this.getColumnList());
                 System.out.println("net.colstore.web.mbeans.TableManagerBean.createNewTable() :: Schema Creation Status :: "+s_flag);
                this.getNewTable().setTbl_name("");
                this.setMsg("Created !! " + flag);
            } else {
                this.getNewTable().setTbl_name("");
                this.setMsg("Table Creation failed.");
            }

            this.getNewTable().setTbl_name("");
            this.getColumnList().clear();

        } else {
            this.setMsg("Select Database");
            this.getNewTable().setTbl_name("");
            this.getColumnList().clear();
        }

    }

    public boolean fetchLoginDetails() {
        boolean flag = false;
        LoginBean loginBeanObj = null;
        try {
            loginBeanObj = (LoginBean) getSessionObject("loginBean");
            //dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "RMenuBuilder.class :: fetchLoginDetails() :: loginBean : "+loginBeanObj);
            this.setRoleId(loginBeanObj.getRoleId());
            this.setUserId(loginBeanObj.getUserId());
            this.setRoleName(loginBeanObj.getRoleName());
            this.setUserName(loginBeanObj.getUserName());
            flag = loginBeanObj.isLoginStatus();
            if (flag) {
                loginBeanObj.setSessionExpiry(10);//Session Expiry in Minutes
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "RMenuBuilder.class :: fetchLoginDetails() :: loginBean {User Id=" + this.getUserId() + ",UserName=" + this.getUserName() + ",RoleId=" + this.getRoleId() + ",RoleName=" + this.getRoleName() + ", LoginStatus=" + flag + "}");
            System.out.println(":: fetchLoginDetails :: isLoginStatus :: " + flag);
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "TableManagerBean.class :: fetchLoginDetails() :: Exception while accessing Login Info from Session, Exception  : " + e.getMessage());
        }
        loginBeanObj = null;
        return flag;
    }

    public void createDbList() {
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "TableManagerBean.class :: createDbList() :: loginBean {User Id=" + this.getUserId() + ",UserName=" + this.getUserName() + ",RoleId=" + this.getRoleId() + ",RoleName=" + this.getRoleName() + "}");
        DbListDAO dbdao = new DbListDAO();
        List<DbList> db = dbdao.getDbList(this.getUserId());
        for (DbList newDB : db) {
            dblist.put(newDB.getDb_name(), newDB.getDb_name());
        }
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "TableManagerBean.class :: createDbList() :: " + dblist.size());
    }

    public void onAddNew() {
        // Add one new car to the table:
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "TableManagerBean.class :: onAddNew() :: Adding new Column");
        ColList newCol = new ColList();
        newCol.setCol_name("NewColumn");
        newCol.setCol_dataType("STRING");
        this.getColumnList().add(newCol);
    }

    public void onRowEdit(RowEditEvent event) {
        //   FacesMessage msg = new FacesMessage("Car Edited", ((Car) event.getObject()).getId());
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "TableManagerBean.class :: onRowEdit() :: " + ((ColList) event.getObject()).toString());

        //  FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public void onRowCancel(RowEditEvent event) {
        //FacesMessage msg = new FacesMessage("Edit Cancelled", ((Car) event.getObject()).getId());
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "TableManagerBean.class :: onRowCancel() :: " + ((ColList) event.getObject()).toString());
        //FacesContext.getCurrentInstance().addMessage(null, msg);
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

    public TableList getNewTable() {
        return newTable;
    }

    public void setNewTable(TableList newTable) {
        this.newTable = newTable;
    }

    public Map<String, String> getDblist() {
        return dblist;
    }

    public void setDblist(Map<String, String> dblist) {
        this.dblist = dblist;
    }

    public List<ColList> getColumnList() {
        return columnList;
    }

    public void setColumnList(List<ColList> columnList) {
        this.columnList = columnList;
    }

    public List<String> getDataTypeList() {
        return dataTypeList;
    }

    public void setDataTypeList(List<String> dataTypeList) {
        this.dataTypeList = dataTypeList;
    }

}
