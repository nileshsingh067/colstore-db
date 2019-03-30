/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rocg.web.mbeans;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import net.rocg.util.StringInputValidator;
import net.rocg.web.beans.CMSCategory;
import net.rocg.web.beans.Country;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author Rishi Tyagi
 */
@ManagedBean
@RequestScoped
public class ContentCategoryBean implements java.io.Serializable{
    
    DBConnection dbConn;
    List<CMSCategory> categoryList;
    Map<String,String> superCategories;
    CMSCategory newCategory,selectedCategory;
    boolean superCategory;
    StatusMessage statusMsg;
    /**
     * Creates a new instance of ContentCategoryBean
     */
    public ContentCategoryBean() {
        dbConn=new DBConnection();
        superCategories=new HashMap<>();
        categoryList=new ArrayList<>();
        newCategory=new CMSCategory();
        selectedCategory=new CMSCategory();
        statusMsg=new StatusMessage();
        refreshList(null,0,true,true);
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentCategoryBean.class :: MBean Connected.");
    }

     public void refreshList(CMSCategory updateObj,int action,boolean reloadSuperCategoryList,boolean reloadcategoryList){
        Connection conn=dbConn.connect();
        StringBuilder logMsg=new StringBuilder();
        
        if(conn!=null){
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentCategoryBean.class :: refreshList() :: Database Connected.");
                try{
                    java.sql.Statement st=conn.createStatement();
                    String sqlA="";int dbRep=0;
                    if(action==2 && updateObj!=null && updateObj.getCategoryId()>0){
                        //Update Existing Country Name
                        sqlA="update tb_content_categories set show_order='"+updateObj.getShowOrder()+"', status="+updateObj.getStatus()+" where catg_id="+updateObj.getCategoryId()+";";
                        dbRep=st.executeUpdate(sqlA);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentCategoryBean.class :: refreshList() :: Action Requested=UPDATE-CATEGORY; SQL : " + sqlA + "| DB Result=" + dbRep);
                    }else if(action==1 && updateObj!=null && updateObj.getCategoryName()!=null && updateObj.getCategoryName().length()>3){
                        //Create new Category
                        String folderName=updateObj.getCategoryName().trim();
                        folderName=folderName.replaceAll(" ", "_");
                        sqlA="INSERT INTO tb_content_categories(catg_name,display_text,folder_name,parent_catg,show_order,subcategorised,STATUS,reg_date,last_update) VALUES('"
                                + updateObj.getCategoryName()+"','"+updateObj.getDisplayName()+"','"+folderName+"',"+updateObj.getParentCategoryId()+","+updateObj.getShowOrder()+","+(updateObj.getParentCategoryId()>0?0:1)+",1,now(),now());";
                        dbRep=st.executeUpdate(sqlA);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentCategoryBean.class :: refreshList() :: Action Requested=REGISTER-NEW-COUNTRY; SQL : " + sqlA + "| DB Result=" + dbRep);
                    }
                    String sql1="";
                    java.sql.ResultSet rs=null;
                    if(reloadSuperCategoryList){
                        superCategories.clear();
                        sql1="SELECT catg_id,catg_name FROM tb_content_categories WHERE parent_catg=0 AND STATUS>0 ORDER BY catg_name;";
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentCategoryBean.class :: refreshList() :: REFRESH SUPER CATG SQL : " + sql1);
                        rs=st.executeQuery(sql1);
                        while(rs.next()){
                            superCategories.put(rs.getString("catg_name"), rs.getString("catg_id"));
                        }
                        if(rs!=null) rs.close();
                        rs=null;
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentCategoryBean.class :: refreshList() :: Super Category List (ItemCount=" + superCategories.size() + ") Reloaded. ");
                    }
                    
                    if(reloadcategoryList){
                        categoryList.clear();
                        sql1="SELECT C.catg_id,C.catg_name,C.display_text,C.folder_name,C.parent_catg AS parent_catg_id,IF(C.parent_catg>0,CS.catg_name,'NONE') AS parent_catg_name,C.`show_order`,C.`status` FROM tb_content_categories C LEFT JOIN tb_content_categories CS ON C.`parent_catg`= CS.`catg_id` ORDER BY C.status desc,5 ASC,2 ASC;";
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentCategoryBean.class :: refreshList() :: REFRESH CATG SQL : " + sql1);
                        rs=st.executeQuery(sql1);
                        CMSCategory tempObj=null;
                        while(rs.next()){
                            tempObj=new CMSCategory();
                            tempObj.setCategoryId(rs.getInt("catg_id"));
                            tempObj.setCategoryName(rs.getString("catg_name"));
                            tempObj.setDisplayName(rs.getString("display_text"));
                            tempObj.setParentCategoryId(rs.getInt("parent_catg_id"));
                            tempObj.setParentCategoryName(rs.getString("parent_catg_name"));
                            tempObj.setShowOrder(rs.getInt("show_order"));
                            tempObj.setStatus(rs.getInt("status"));
                            categoryList.add(tempObj);
                            tempObj=null;
                        }
                         rs.close();
                        rs=null;
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentCategoryBean.class :: refreshList() :: Category List (ItemCount=" + categoryList.size() + ") Reloaded. ");
                    }
                    
                   
                    st.close();
                    st=null;
                }catch(Exception e){
                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ContentCategoryBean.class :: refreshList() :: Exception :" + e.getMessage());
                    statusMsg.setMessage("A process failed while reloading Country List. Database Error!",StatusMessage.MSG_CLASS_ERROR);
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
            }else{
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ContentCategoryBean.class :: refreshList() :: Database Connectivity Failed");
                statusMsg.setMessage("A process failed while reloading Country List. Database Connectivity Issue.",StatusMessage.MSG_CLASS_ERROR);
            }

        
    }
    
    
    public void createNew(){
        newCategory.setCategoryName(newCategory.getCategoryName()==null?"":newCategory.getCategoryName().trim());
        newCategory.setDisplayName(newCategory.getDisplayName()==null?"":newCategory.getDisplayName().trim());
        newCategory.setParentCategoryId(isSuperCategory()?0:RUtil.strToInt(newCategory.getParentCategoryName(), 0));
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentCategoryBean.class :: createNew() :: New Category Name to Register '" + newCategory.getCategoryName() + "'");
        
       String newCategoryV=StringInputValidator.validateString(newCategory.getCategoryName(), "<>&@#$!`'^*?/");
       String newDisplayV=StringInputValidator.validateString(newCategory.getDisplayName(), "<>&@#$!`'^*?/");
       if(!newCategoryV.equalsIgnoreCase(newCategory.getCategoryName()) || !newDisplayV.equalsIgnoreCase(newCategory.getDisplayName())){
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ContentCategoryBean.class :: createNew() :: Invalid Category Name `" + newCategory.getCategoryName() + "`. Category Name must have number of charactesrs more then 3 and less then 50.");
           statusMsg.setMessage("Sorry! Invalid Category Name. Special Characters are not allowed in categpory name or display text.",StatusMessage.MSG_CLASS_ERROR);
       }else if(newCategory.getCategoryName().length()<3 || newCategory.getCategoryName().length()>50){
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ContentCategoryBean.class :: createNew() :: Invalid Category Name `" + newCategory.getCategoryName() + "`. Category Name must have number of charactesrs more then 3 and less then 50.");
           statusMsg.setMessage("Sorry! Invalid Category Name. Category Name must be between 3-50 char in length.",StatusMessage.MSG_CLASS_ERROR);
       }else if(newCategory.getDisplayName().length()<3 || newCategory.getDisplayName().length()>50){
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ContentCategoryBean.class :: createNew() :: Invalid Display Name `" + newCategory.getCategoryName() + "`. Display Name must have number of charactesrs more then 3 and less then 50.");
           statusMsg.setMessage("Sorry! Invalid Display Name. Display Name must be between 3-50 char in length.",StatusMessage.MSG_CLASS_ERROR);
       }else if(!this.isSuperCategory() && newCategory.getParentCategoryId()<=0){
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ContentCategoryBean.class :: createNew() :: Invalid Super Category `" + newCategory.getParentCategoryId() + "`. Please select a super category first.");
           statusMsg.setMessage("Sorry! Invalid Super Category.",StatusMessage.MSG_CLASS_ERROR);
       } else{
           statusMsg.setMessage("New Category Name '"+newCategory.getCategoryName()+"' sent for registration!",StatusMessage.MSG_CLASS_INFO);
           refreshList(newCategory,1,true,true);
       }
    }
    
     /**
      * Edit Event Handler Method
      */
     public void onEdit(RowEditEvent event) {  
      CMSCategory actionObj=(CMSCategory)event.getObject();
       if(actionObj!=null && actionObj.getCategoryId()>0){ 
           statusMsg.setMessage("Category Details sent for update into Registry!", StatusMessage.MSG_CLASS_INFO);
           refreshList(actionObj,2,true,true);
       }else{
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ContentCategoryBean.class :: onEdit() :: Invalid Category Id (" + actionObj.getCategoryId() + ") to update.");
            statusMsg.setMessage("Invalid Category Id `" + actionObj.getCategoryId() + "`. Please select a Category to update.", StatusMessage.MSG_CLASS_ERROR);
       }
       
    }  

    
    public List<CMSCategory> getCategoryList() {
        return categoryList;
    }

    public void setCategoryList(List<CMSCategory> categoryList) {
        this.categoryList = categoryList;
    }

    public CMSCategory getNewCategory() {
        return newCategory;
    }

    public void setNewCategory(CMSCategory newCategory) {
        this.newCategory = newCategory;
    }

    public CMSCategory getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(CMSCategory selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

  

    public boolean isSuperCategory() {
        return superCategory;
    }

    public void setSuperCategory(boolean superCategory) {
        this.superCategory = superCategory;
    }

    public Map<String, String> getSuperCategories() {
        return superCategories;
    }

    public void setSuperCategories(Map<String, String> superCategories) {
        this.superCategories = superCategories;
    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }

   
}
