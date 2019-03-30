/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rocg.web.mbeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import net.rocg.web.beans.CMSContent;

/**
 *
 * @author Rishi Tyagi
 */
public class ViewContentMBean {
    DBConnection dbConn;
    int loginId, roleId;
    StatusMessage statusMsg;
    String loginName;
    String selectedContentProvider;
    Map<String,String> contentProviderList;
    String selectedCategory;
    Map<String,String> contentCategoryList;
    String selectedSubCategory;
    Map<String,String> contentSubCategoryList;
    
    String selectedContentType;
    Map<String,String> contentTypeList;
    
    List<CMSContent> contentList;
    CMSContent selectedContent;
    String contentBaseURL,contentBasePath;
    /**
     * Creates a new instance of ViewContentMBean
     */
    public ViewContentMBean() {
        fetchLoginDetails();
        contentList=new ArrayList<>();
        contentProviderList=new HashMap<>();
        contentCategoryList=new HashMap<>();
        contentTypeList=new HashMap<>();
        contentSubCategoryList=new HashMap<>();
        contentBasePath=RUtil.getStringProperty("cms_content_basefolder", "");
        contentBaseURL=RUtil.getStringProperty("cms_content_baseurl", "");
        statusMsg=new StatusMessage();
        dbConn=new DBConnection();
         fetchData(true,true,true,false,false);
         dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ViewContentMBean.class :: MBean Connected.");
    }

    public void refreshContent(){
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ViewContentMBean.class :: refreshContent() :: Reloading Content List.....");
        fetchData(false,false,false,false,true);
    }
    
   
    public void fetchData(boolean reloadCategoryList,boolean reloadContentTypeList,boolean reloadProviderList,boolean reloadSubCategory,boolean reloadContent){
            java.sql.Connection conn=dbConn.connect();
        if(conn!=null){
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ViewContentMBean.class :: fetchData() :: Database Connected.");
            try{
                java.sql.Statement st=conn.createStatement();
                System.out.println("fetchData() :: "+1);
                if(reloadCategoryList) this.reloadContentCategoryList(st);
                System.out.println("fetchData() :: "+2);
                if(reloadContentTypeList) this.reloadContentTypeList(st);
                System.out.println("fetchData() :: "+3);
                if(reloadProviderList) this.reloadContentProviderList(st);
                System.out.println("fetchData() :: "+4);
                if(reloadSubCategory) this.reloadSubCategoryList(st);
                System.out.println("fetchData() :: "+5);
                if(reloadContent) this.reloadContentRecords(st);
               System.out.println("fetchData() :: "+6);
               
                if(st!=null) st.close();
                st=null;
            }catch(Exception e){
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ViewContentMBean.class :: fetchData() :: Exception "+e);
            }finally{
                try{if(conn!=null) conn.close();}catch(Exception ee){}
                conn=null;
            }
        }else{
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ViewContentMBean.class :: fetchData() :: Database Connectivity Failed");
            statusMsg.setMessage("A process failed while reloading Country List. Database Connectivity Issue.", StatusMessage.MSG_CLASS_ERROR);
        }
    
    }

    
    public void reloadContentRecords(java.sql.Statement st){
        java.sql.ResultSet rs=null;
        try{
            System.out.println("reloadContentRecords() :: "+1);
            StringBuilder sb=new StringBuilder("SELECT a.`content_id`,a.`cat_nr`,a.`content_type_id`,a.`category_id`,a.`copyright_id`,a.`cp_id`,"
                        + "a.`cp_catnr`,a.`content_name`, a.content_folder,CONCAT(a.`content_url`,b.`file_name`,\"_\",b.`parts_count`,\".\",b.`file_extension`) AS content_url FROM tb_cms_contents a, tb_cms_contents_files b WHERE a.`cat_nr`=b.`cat_nr` AND a.status>=1 AND b.`preview_file`=1 AND b.`file_name` LIKE '%50x50' ");
            System.out.println("reloadContentRecords() :: "+2 +" :: "+sb.toString());
            if(RUtil.strToInt(this.selectedContentProvider,0)>0)
                sb.append(" and a.cp_id=").append(this.getSelectedContentProvider());
            System.out.println("reloadContentRecords() :: "+3 +" :: "+sb.toString());
            if(RUtil.strToInt(this.selectedSubCategory, 0)>0)
                sb.append(" and a.category_id=").append(this.getSelectedSubCategory());
            System.out.println("reloadContentRecords() :: "+4 +" :: "+sb.toString());
            if(RUtil.strToInt(this.selectedContentType, 0)>0)
                sb.append(" and a.content_type_id=").append(this.getSelectedContentType());
            System.out.println("reloadContentRecords() :: "+5 +" :: "+sb.toString());
            sb.append(" order by a.content_id desc;");
            String sql1=sb.toString();
            
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ViewContentMBean.class :: reloadContentRecords() :: Query : "+sql1);
            rs=st.executeQuery(sql1);
            CMSContent newContent=null;
            contentList.clear();
            String contentBaseURL=this.getContentBaseURL();
            while(rs.next()){
                newContent=new CMSContent();
                newContent.setContentId(rs.getLong("content_id"));
                newContent.setCatalogNumber(rs.getLong("cat_nr"));
                newContent.setContentTypeId(rs.getInt("content_type_id"));
                newContent.setCategoryId(rs.getInt("category_id"));
                newContent.setCopyrightId(rs.getInt("copyright_id"));
                newContent.setContentProviderId(rs.getInt("cp_id"));
                newContent.setCpcatalogNumber(rs.getString("cp_catnr"));
                newContent.setContentName(rs.getString("content_name"));
                newContent.setContentFolder(rs.getString("content_folder"));
                newContent.setContentPath(rs.getString("content_url"));
                this.contentList.add(newContent);
                newContent=null;
            }
            if(rs!=null) rs.close();
            rs=null;
            
        }catch(Exception e){
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ViewContentMBean.class :: reloadContentRecords() :: Exception "+e);
        }finally{
            try{if(rs!=null) rs.close();}catch(Exception ee){}
            rs=null;
        }
    }
    
    
    public void reloadSubCategoryList(java.sql.Statement st) {
        try {
            int catId=RUtil.strToInt(selectedCategory, -1);
            contentSubCategoryList.clear();
            String sql1="SELECT catg_id,catg_name FROM tb_content_categories WHERE STATUS>0 AND parent_catg>0 and parent_catg="+catId+" ORDER BY catg_name;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ViewContentMBean.class :: reloadSubCategoryList() :: Query : "+sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            while (rs.next()) {
                contentSubCategoryList.put(rs.getString("catg_name"), rs.getString("catg_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ViewContentMBean.class :: reloadSubCategoryList() ::  Collection Size "+contentSubCategoryList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ViewContentMBean.class :: reloadSubCategoryList() :: Exception "+e.getMessage());
           
        }
    }

    public void reloadContentProviderList(java.sql.Statement st) {
        try {
            
            String sql1="SELECT a.user_id,a.user_name,b.company_name FROM tb_users a,tb_registered_companies b WHERE a.`company_id`=b.`company_id` and  b.contract_category='CONTENT-PROVIDER' AND a.`role_id`=3 ORDER BY 3,2";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ViewContentMBean.class :: reloadContentProviderList() :: Query : "+sql1);
            java.sql.ResultSet rs =st.executeQuery(sql1);
            contentProviderList.clear();
            while(rs.next()){
                contentProviderList.put(rs.getString("company_name")+"-"+rs.getString("user_name"), rs.getString("user_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ViewContentMBean.class :: reloadContentProviderList() ::  Collection Size "+contentProviderList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ViewContentMBean.class :: reloadContentProviderList() :: Exception "+e.getMessage());
           
        }
    }

    public void reloadContentTypeList(java.sql.Statement st) {
        try {
            contentTypeList.clear();
            String sql1="SELECT content_type_id,content_type_name FROM tb_cms_content_type WHERE STATUS>0 ORDER BY content_type_name;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ViewContentMBean.class :: reloadContentTypeList() :: Query : "+sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            while (rs.next()) {
                contentTypeList.put(rs.getString("content_type_name"), rs.getString("content_type_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ViewContentMBean.class :: reloadContentTypeList() ::  Collection Size "+contentTypeList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ViewContentMBean.class :: reloadContentTypeList() :: Exception "+e.getMessage());
        }
    }

    public void reloadContentCategoryList(java.sql.Statement st) {
        try {
            contentCategoryList.clear();
            String sql1="SELECT catg_id,catg_name FROM tb_content_categories WHERE STATUS>0 AND parent_catg=0 ORDER BY catg_name;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ViewContentMBean.class :: reloadContentCategoryList() :: Query : "+sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            while (rs.next()) {
                contentCategoryList.put(rs.getString("catg_name"), rs.getString("catg_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ViewContentMBean.class :: reloadContentCategoryList() ::  Collection Size "+contentCategoryList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ViewContentMBean.class :: reloadContentCategoryList() :: Exception "+e.getMessage());
        }
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
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ViewContentMBean.class :: fetchLoginDetails() :: Exception "+e.getMessage());
        }
        loginBeanObj = null;

    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public Map<String, String> getContentCategoryList() {
        return contentCategoryList;
    }

    public void setContentCategoryList(Map<String, String> contentCategoryList) {
        this.contentCategoryList = contentCategoryList;
    }

    public String getSelectedContentType() {
        return selectedContentType;
    }

    public void setSelectedContentType(String selectedContentType) {
        this.selectedContentType = selectedContentType;
    }

    public Map<String, String> getContentTypeList() {
        return contentTypeList;
    }

    public void setContentTypeList(Map<String, String> contentTypeList) {
        this.contentTypeList = contentTypeList;
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

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getSelectedContentProvider() {
        return selectedContentProvider;
    }

    public void setSelectedContentProvider(String selectedContentProvider) {
        this.selectedContentProvider = selectedContentProvider;
    }

    public Map<String, String> getContentProviderList() {
        return contentProviderList;
    }

    public void setContentProviderList(Map<String, String> contentProviderList) {
        this.contentProviderList = contentProviderList;
    }

    public List<CMSContent> getContentList() {
        return contentList;
    }

    public void setContentList(List<CMSContent> contentList) {
        this.contentList = contentList;
    }

    public CMSContent getSelectedContent() {
        return selectedContent;
    }

    public void setSelectedContent(CMSContent selectedContent) {
        this.selectedContent = selectedContent;
    }

    public String getContentBaseURL() {
        return contentBaseURL;
    }

    public void setContentBaseURL(String contentBaseURL) {
        this.contentBaseURL = contentBaseURL;
    }

    public String getContentBasePath() {
        return contentBasePath;
    }

    public void setContentBasePath(String contentBasePath) {
        this.contentBasePath = contentBasePath;
    }

    public String getSelectedSubCategory() {
        return selectedSubCategory;
    }

    public void setSelectedSubCategory(String selectedSubCategory) {
        this.selectedSubCategory = selectedSubCategory;
    }

    public Map<String, String> getContentSubCategoryList() {
        return contentSubCategoryList;
    }

    public void setContentSubCategoryList(Map<String, String> contentSubCategoryList) {
        this.contentSubCategoryList = contentSubCategoryList;
    }

   
    
}
