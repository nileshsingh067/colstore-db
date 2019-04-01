/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.colstore.web.mbeans;

import java.sql.Connection;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.colstore.util.DBConnection;
import net.colstore.util.RLogger;
import net.colstore.web.mbeans.RMenuItem;
import net.colstore.web.service.NodeService;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.TreeNode;
import org.primefaces.model.menu.DefaultMenuItem;
import org.primefaces.model.menu.DefaultMenuModel;
import org.primefaces.model.menu.DefaultSubMenu;
import org.primefaces.model.menu.MenuModel;

/**
 *
 * @author Nilesh Kumar Singh
 */
@ManagedBean
@SessionScoped
public class RMenuBuilder  implements java.io.Serializable{
    DBConnection dbConn;
    int userId;
    int roleId;
    String roleName;
    String userName;
    MenuModel model;
      private TreeNode root;
      private TreeNode selectedNode;
     
    @ManagedProperty("#{nodeService}")
    private NodeService service;
     
    /**
     * Creates a new instance of RMenuBuilder
     */
    public RMenuBuilder() {
        dbConn=new DBConnection();
        boolean loginStatus=fetchLoginDetails();
        
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "RMenuBuilder.class :: constructor :: Login Status= "+loginStatus);
//        if(loginStatus){
//           buildMenu();
//        }else{
//            //show default Menu without any access
//        }
                
    }

    public void buildMenuOld(){
        Connection conn=dbConn.connect();
        if(conn!=null){
            try{
                Statement st=conn.createStatement();
                List<RMenuItem> mainMenu=getMenuList(st,0);
                DefaultSubMenu subMenu=null;RMenuItem newItem=null;DefaultMenuItem menuItem=null;
                for(int i=0;i<mainMenu.size();i++){
                        newItem=mainMenu.get(i);
                        if(newItem!=null && newItem.getMenuId()>0){
                            if(newItem.getHasSubMenu()<=0){
                                //Add as Menu Item
                                menuItem=new DefaultMenuItem(newItem.getMenuName());
                                menuItem.setId("MI"+newItem.getMenuId());
                                menuItem.setUrl(newItem.getTargetPage());
                                model.addElement(menuItem);
                                menuItem=null;
                            }else{
                                //Add as SubMenu
                                subMenu = new DefaultSubMenu(newItem.getMenuName());
                               
                                subMenu.setId("MM"+newItem.getMenuId());
                                //Get List of Menu Iteam
                                List<RMenuItem> subMenuList=getMenuList(st,newItem.getMenuId());
                                newItem=null;
                                for(int j=0;j<subMenuList.size();j++){
                                    newItem=subMenuList.get(j);
                                    menuItem=new DefaultMenuItem(newItem.getMenuName());
                                    menuItem.setId("MI"+newItem.getMenuId());
                                    menuItem.setUrl(newItem.getTargetPage());
                                    subMenu.addElement(menuItem);
                                    menuItem=null;
                                    newItem=null;
                                    
                                }//for loop
                                model.addElement(subMenu);
                                subMenu=null;
                                subMenuList=null;
                            }//end of else
                        }
                        newItem=null;
                    }//end of for loop
            }catch(Exception e){
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "RMenuBuilder.class :: buildMenu() :: Exception while accessing Menu Info from DB, Exception  : "+e.getMessage());
            }finally{
                try{if(conn!=null) conn.close();}catch(Exception ee){}
                conn=null;
            }
        }else{
            //DB Error
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "RMenuBuilder.class :: buildMenu() :: Failed to connect Database.");
        }
        
    }
    
    public void buildMenu(){
        Connection conn=dbConn.connect();
        if(conn!=null){
            try{
                Statement st=conn.createStatement();
                List<RMenuItem> mainMenu=getMenuList(st,0);
                DefaultSubMenu subMenu=null;DefaultSubMenu subMenuChild=null;RMenuItem newItem=null;DefaultMenuItem menuItem=null;
                for(int i=0;i<mainMenu.size();i++){
                        newItem=mainMenu.get(i);
                        if(newItem!=null && newItem.getMenuId()>0){
                            if(newItem.getHasSubMenu()<=0){
                                //Add as Menu Item
                                menuItem=new DefaultMenuItem(newItem.getMenuName());
                                menuItem.setId("MI"+newItem.getMenuId());
                                menuItem.setUrl(newItem.getTargetPage());
                                model.addElement(menuItem);
                                menuItem=null;
                            }else{
                                //Add as SubMenu
                                subMenu = new DefaultSubMenu(newItem.getMenuName());
                               
                                subMenu.setId("MM"+newItem.getMenuId());
                                //Get List of Menu Iteam
                                List<RMenuItem> subMenuList=getMenuList(st,newItem.getMenuId());
                                newItem=null;
                                for(int j=0;j<subMenuList.size();j++){
                                    newItem=subMenuList.get(j);
                                    if(newItem.getHasSubMenu()<=0){
                                        menuItem=new DefaultMenuItem(newItem.getMenuName());
                                        menuItem.setId("MI"+newItem.getMenuId());
                                        menuItem.setUrl(newItem.getTargetPage());
                                        subMenu.addElement(menuItem);
                                        menuItem=null;
                                        newItem=null;
                                    }else{
                                        subMenuChild=new DefaultSubMenu(newItem.getMenuName());
                                        subMenuChild.setId("MM"+newItem.getMenuId());
                                        List<RMenuItem> subMenuChildList=getMenuList(st,newItem.getMenuId());
                                        newItem=null;
                                        for(int k=0;k<subMenuChildList.size();k++){
                                            newItem=subMenuChildList.get(k);
                                            menuItem=new DefaultMenuItem(newItem.getMenuName());
                                            menuItem.setId("MI"+newItem.getMenuId());
                                            menuItem.setUrl(newItem.getTargetPage());
                                            subMenuChild.addElement(menuItem);
                                            menuItem=null;
                                            newItem=null;
                                        }//for loop
                                        subMenu.addElement(subMenuChild);
                                        subMenuChildList=null;
                                        subMenuChild=null;
                                    }
                                }//for loop
                                model.addElement(subMenu);
                                subMenu=null;
                                subMenuList=null;
                            }//end of else
                        }
                        newItem=null;
                    }//end of for loop
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "RMenuBuilder.class :: buildMenu() :: Menu Loaded Successfully");
                root = service.createDocuments();
            }catch(Exception e){
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "RMenuBuilder.class :: buildMenu() :: Exception while accessing Menu Info from DB, Exception  : "+e.getMessage());
            }finally{
                try{if(conn!=null) conn.close();}catch(Exception ee){}
                conn=null;
            }
        }else{
            //DB Error
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "RMenuBuilder.class :: buildMenu() :: Failed to connect Database.");
        }
        
    }
    public void treeNodeListener(){
        System.out.println("tree node :: "+this.selectedNode.toString());
    }
    public List<RMenuItem> getMenuList(Statement st,int parentMenu){
        List<RMenuItem> menuList=new ArrayList<RMenuItem>();
       java.sql.ResultSet rs=null;
       try{
           String sql1="select a.menu_id,a.menu_name,a.target_page,a.icon_image,a.menu_description,a.hint_text,a.status,a.show_order,a.parent_menu,a.system_menu,a.has_submenu from "
                   + "tb_menu_details a, tb_menu_access b where a.menu_id=b.menu_id and a.status>=1 and b.status>=1 and b.role_id="+this.getRoleId()+" and a.parent_menu="+parentMenu+" order by a.show_order;";
          dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "RMenuBuilder.class :: getMenuList() :: Search Menu SQL : "+sql1);
                
        
           rs=st.executeQuery(sql1);
          RMenuItem newItem=null;
          while(rs.next()){
              newItem=new RMenuItem();
              newItem.setMenuId(rs.getInt("menu_id"));
              newItem.setMenuName(rs.getString("menu_name"));
              newItem.setTargetPage(rs.getString("target_page"));
              newItem.setIconImage(rs.getString("icon_image"));
              newItem.setMenuDescp(rs.getString("menu_description"));
              newItem.setHintText(rs.getString("hint_text"));
              newItem.setStatus(rs.getInt("status"));
              newItem.setShowOrder(rs.getInt("show_order"));
              newItem.setParentMenuId(rs.getInt("parent_menu"));
              newItem.setSystemMenu(rs.getInt("system_menu"));
              newItem.setHasSubMenu(rs.getInt("has_submenu"));
              menuList.add(newItem);
              newItem=null;
          }
          
       }catch(Exception e){
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "RMenuBuilder.class :: getMenuList() :: Exception while accessing child menu for menu id ("+parentMenu+") : "+e.getMessage());
       }finally{
           try{if(rs!=null) rs.close();}catch(Exception ee){}
           rs=null;
       }
       
       return menuList;
    }
    
    public void checkSessionState(){
        if(fetchLoginDetails()==false){
            try{
             FacesContext ctx = FacesContext.getCurrentInstance();
             ExternalContext extCtx = ctx.getExternalContext();
             extCtx.redirect("/colstore-db/faces/index.xhtml");
            }catch(Exception e){
                System.out.println("Exception in checkSessionState "+e);
            }
        }
        if(fetchLoginDetails()){
          model=new DefaultMenuModel();
            buildMenu();
            
        }else{
            //show default Menu without any access
            System.out.println("Session Expited Unable to build Meniu");
        }   
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
            //dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "RMenuBuilder.class :: fetchLoginDetails() :: loginBean {User Id="+this.getUserId()+",UserName="+this.getUserName()+",RoleId="+this.getRoleId()+",RoleName="+this.getRoleName()+", LoginStatus="+flag+"}");
            System.out.println(":: fetchLoginDetails :: isLoginStatus :: "+flag);
        }catch(Exception e){
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "RMenuBuilder.class :: fetchLoginDetails() :: Exception while accessing Login Info from Session, Exception  : "+e.getMessage());
        }
        loginBeanObj=null;
        return flag;
    }
    public void onNodeSelect(NodeSelectEvent event) {
      //  FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, "Selected", event.getTreeNode().toString());
        //FacesContext.getCurrentInstance().addMessage(null, message);
        System.out.println("onNodeSelect() :: "+event.getTreeNode().getData().toString());
    }
    public MenuModel getModel() {
        if(model==null) this.buildMenu();
        return model;
    }

    public void setModel(MenuModel model) {
        this.model = model;
    }
    
    public static Object getSessionObject(String objName) {
        FacesContext ctx = FacesContext.getCurrentInstance();
        ExternalContext extCtx = ctx.getExternalContext();
       // extCtx.setSessionMaxInactiveInterval(3600);
        Map<String, Object> sessionMap = extCtx.getSessionMap();
        return sessionMap.get(objName);
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
    
     public void setService(NodeService service) {
        this.service = service;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }
    
}
