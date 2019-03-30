/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rocg.web.mbeans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import net.rocg.web.beans.CMSContent;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author Mobigosssip
 */
@ManagedBean
@SessionScoped
public class PortalContentSchedulingBean implements java.io.Serializable {
    DBConnection dbConn;
    int loginId, roleId;
    StatusMessage statusMsg;
    String loginName;
    String selectedOperatorId;
    String selectedPortalId;
    String selectedContentType;
    String selectedCategory;
    String selectedTabId;
    String selectedSubCategory;
    String selectedContentProvider;
    String mappingFor;
    Map<String,String> portalList;
    Map<String,String> contentTypeList;
    Map<String,String> contentCategoryList;
    Map<String,String> contentSubCategoryList;
    Map<String,String> contentCPList;
    Map<String,String> tabList;
    Map<String,String> operatorList;
    List<CMSContent> availableCMSContent,portalMappedContent;
    List<CMSContent> selectedCMSContents,selectedPortalContents;
    
   // CMSContent selectedSourceContent,selectedPortalContent;
   String contentBasePath,contentBaseURL;
    /**
     * Creates a new instance of PortalContentSchedulingBean
     */
    public PortalContentSchedulingBean() {
        availableCMSContent=new ArrayList<CMSContent>();
        portalMappedContent=new ArrayList<CMSContent>();
        statusMsg=new StatusMessage();
        dbConn=new DBConnection();
        fetchLoginDetails();
         contentBasePath=RUtil.getStringProperty("cms_content_basefolder", "");
        contentBaseURL=RUtil.getStringProperty("cms_content_baseurl", "");
        portalList=new HashMap<String,String>();
        contentTypeList=new HashMap<String,String>();
        contentCategoryList=new HashMap<String,String>();
        contentSubCategoryList=new HashMap<String,String>();
        contentCPList=new HashMap<String,String>();
        operatorList=new HashMap<String,String>();
        tabList=new HashMap<String,String>();
        fetchData(false,true,false,true,false,false,false,false,false,false);
    }
    
    public void fetchData(boolean reloadTabList,boolean reloadPortalList,boolean reloadOperatorList,boolean reloadContentTypeList,boolean reloadCPList,boolean reloadCategoryList,boolean reloadSubCategory,boolean reloadContent,boolean reloadPortalContent,boolean addContent){
            java.sql.Connection conn=dbConn.connect();
            System.out.println("selectedPortal "+selectedPortalId+", selectedContentType "+selectedContentType+", selectedContentProvider="+selectedContentProvider+", selectedCategory "+selectedCategory+", selectedSubCategory "+selectedSubCategory);
        if(conn!=null){
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: fetchData() :: Database Connected.");
            try{
                java.sql.Statement st=conn.createStatement();
               if(reloadTabList) this.reloadTabList(st);
                if(reloadPortalList) this.reloadPortalList(st);
                if(reloadContentTypeList) this.reloadContentTypeList(st);
                if(reloadCPList) this.reloadCPList(st);
                if(reloadCategoryList) this.reloadContentCategoryList(st);
                if(reloadSubCategory) this.reloadSubCategoryList(st);
                if(reloadContent) this.reloadContentRecords(st);
                if(reloadPortalContent) this.reloadPortalContentRecords(st);
                if(reloadOperatorList) this.reloadOperatorList(st);
                if(addContent){
                    if(st!=null) st.close();
                    st=null;
                    int opId=RUtil.strToInt(this.getSelectedOperatorId(), 0);
                    int tabId=RUtil.strToInt(this.getSelectedTabId(), 0);
                    if(opId<=0){
                        dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: fetchData() ::Invalid Operator Id , Please select an operator to schedule content for.");
                        statusMsg.setMessage("Please select an operator to schedule content for portal.", StatusMessage.MSG_CLASS_ERROR);
                    }
                    else if(tabId<=0){
                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalHomeContentSchedulingBean.class :: fetchData() ::Invalid Tab Id , Please select a tab to schedule content for.");
                        statusMsg.setMessage("Please select a tab to schedule content for portal.", StatusMessage.MSG_CLASS_ERROR);
                    }else if(this.selectedCMSContents.size()>0){
                        String sqlStr="insert into tb_cms_contents_portalmap(content_id,portal_id,tab_id,operator_id,mapping_date,mapped_by,STATUS,show_order) values(?,"+this.getSelectedPortalId()+","+this.getSelectedTabId()+","+this.getSelectedOperatorId()+",now(),"+this.getLoginId()+",1,?);";
                        dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: fetchData() :: Query to transfer content "+sqlStr);
                        PreparedStatement pst=conn.prepareStatement(sqlStr);
                        for(CMSContent newC:this.selectedCMSContents){
                            pst.setLong(1, newC.getContentId());
                            pst.setInt(2, newC.getShowOrder());
                            pst.executeUpdate();
                        }
                        pst.close();pst=null;
                        st=conn.createStatement();
                        this.reloadContentRecords(st);
                        this.reloadPortalContentRecords(st);
                    }
                   
                }
            }catch(Exception e){
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: fetchData() :: Exception "+e);
            }finally{
                try{if(conn!=null) conn.close();}catch(Exception ee){}
                conn=null;
            }
        }else{
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: fetchData() :: Database Connectivity Failed");
            statusMsg.setMessage("A process failed while reloading Country List. Database Connectivity Issue.", StatusMessage.MSG_CLASS_ERROR);
        }
    
    }
   public void removeContentFromPortal() {  
      
       if(this.getSelectedPortalContents().size()>0){ 
           String sql1="delete from tb_cms_contents_portalmap where operator_id="+this.getSelectedOperatorId()+" and tab_id="+this.getSelectedTabId()+" and content_id in (";
           for(CMSContent ct:this.getSelectedPortalContents()){
               sql1 = sql1+ct.getContentId()+",";
           }
           if(sql1.endsWith(",")) sql1=sql1.substring(0,sql1.lastIndexOf(","));
           sql1 +=")";
           int rep=0;
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: removeContentFromPortal() :: sql " + sql1);
           Connection conn=dbConn.connect();
           if(conn!=null){
               try{
                   Statement st=conn.createStatement();
                   rep=st.executeUpdate(sql1);
                   statusMsg.setMessage("Database Updated successfully!", StatusMessage.MSG_CLASS_INFO);
                   this.reloadPortalContentRecords(st);
               }catch(Exception e){
                   dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: removeContentFromPortal() :: Exception " + e);
                    statusMsg.setMessage("Failed to update database.", StatusMessage.MSG_CLASS_ERROR);
               }finally{
                   try{if(conn!=null) conn.close();}catch(Exception ee){}
                   conn=null;
               }
           }
          
           
       }else{
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: removeContentFromPortal() :: No content selected to remove from portal.");
            statusMsg.setMessage(" Please select a Content to remove.", StatusMessage.MSG_CLASS_ERROR);
       }
       
    }  
   
   public void onEdit(RowEditEvent event) {  
      CMSContent actionObj=(CMSContent)event.getObject();
       if(actionObj!=null && actionObj.getContentId()>0){ 
           String sql1="update tb_cms_contents_portalmap set show_order="+actionObj.getShowOrder()+" where portal_id="+this.getSelectedPortalId()+" and content_id="+actionObj.getContentId()+";";
           int rep=0;
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: onEdit() :: sql " + sql1);
           Connection conn=dbConn.connect();
           if(conn!=null){
               try{
                   Statement st=conn.createStatement();
                   rep=st.executeUpdate(sql1);
                   statusMsg.setMessage("Database Updated successfully!", StatusMessage.MSG_CLASS_INFO);
                   this.reloadPortalContentRecords(st);
               }catch(Exception e){
                   dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: onEdit() :: Exception " + e);
                    statusMsg.setMessage("Failed to update database.", StatusMessage.MSG_CLASS_ERROR);
               }finally{
                   try{if(conn!=null) conn.close();}catch(Exception ee){}
                   conn=null;
               }
           }
          
           
       }else{
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: onEdit() :: Invalid Content Id (" + actionObj.getContentId() + ") to update.");
            statusMsg.setMessage("Invalid Content Id `" + actionObj.getContentId() + "`. Please select a Content to update.", StatusMessage.MSG_CLASS_ERROR);
       }
       
    }  

public void reloadPortalContentRecords(java.sql.Statement st){
        java.sql.ResultSet rs=null;
        try{
            System.out.println("reloadPortalContentRecords() :: "+1);
            String previewSize=(RUtil.strToInt(this.getSelectedContentType(), 0))==2?"50x75":"50x50";
            StringBuilder sb=new StringBuilder("SELECT c.`content_id`,c.show_order,a.`cat_nr`,a.`content_type_id`,a.`category_id`,a.`copyright_id`,a.`cp_id`,"
                        + "a.`cp_catnr`,a.`content_name`, a.content_folder,CONCAT(a.`content_url`,b.`file_name`,\"_\",b.`parts_count`,\".\",b.`file_extension`) AS content_url FROM tb_cms_contents_portalmap c, tb_cms_contents a, tb_cms_contents_files b WHERE c.operator_id="+this.getSelectedOperatorId()+" and c.tab_id="+this.getSelectedTabId()+" and c.portal_id="+this.getSelectedPortalId()+" and c.content_id=a.content_id and a.`cat_nr`=b.`cat_nr` AND a.status>=1 AND b.`preview_file`=1 AND b.`file_name` LIKE '%"+previewSize+"' ");
            System.out.println("reloadPortalContentRecords() :: "+2 +" :: "+sb.toString());
            if(RUtil.strToInt(this.selectedSubCategory, 0)>0)
                sb.append(" and a.category_id=").append(this.getSelectedSubCategory());
            System.out.println("reloadPortalContentRecords() :: "+4 +" :: "+sb.toString());
            if(RUtil.strToInt(this.selectedContentType, 0)>0)
                sb.append(" and a.content_type_id=").append(this.getSelectedContentType());
            System.out.println("reloadPortalContentRecords() :: "+5 +" :: "+sb.toString());
            sb.append(" order by c.show_order desc;");
            String sql1=sb.toString();
            
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadPortalContentRecords() :: Query : "+sql1);
            rs=st.executeQuery(sql1);
            CMSContent newContent=null;
            portalMappedContent.clear();
            String contentBaseURL=this.getContentBaseURL();
            while(rs.next()){
                newContent=new CMSContent();
                newContent.setContentId(rs.getLong("content_id"));
                newContent.setShowOrder(rs.getInt("show_order"));
                newContent.setCatalogNumber(rs.getLong("cat_nr"));
                newContent.setContentTypeId(rs.getInt("content_type_id"));
                newContent.setCategoryId(rs.getInt("category_id"));
                newContent.setCopyrightId(rs.getInt("copyright_id"));
                newContent.setContentProviderId(rs.getInt("cp_id"));
                newContent.setCpcatalogNumber(rs.getString("cp_catnr"));
                newContent.setContentName(rs.getString("content_name"));
                newContent.setContentFolder(rs.getString("content_folder"));
                newContent.setContentPath(rs.getString("content_url"));
                this.portalMappedContent.add(newContent);
                newContent=null;
            }
            if(rs!=null) rs.close();
            rs=null;
            
        }catch(Exception e){
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: reloadContentRecords() :: Exception "+e);
        }finally{
            try{if(rs!=null) rs.close();}catch(Exception ee){}
            rs=null;
        }
    }
        
public void reloadContentRecords(java.sql.Statement st){
        java.sql.ResultSet rs=null;
        try{
            System.out.println("reloadContentRecords() :: "+1);
            String previewSize=(RUtil.strToInt(this.getSelectedContentType(), 0))==2?"50x75":"50x50";
            StringBuilder sb=new StringBuilder("SELECT a.`content_id`,a.`cat_nr`,a.`content_type_id`,a.`category_id`,a.`copyright_id`,a.`cp_id`,"
                        + "a.`cp_catnr`,a.`content_name`, a.content_folder,CONCAT(a.`content_url`,b.`file_name`,\"_\",b.`parts_count`,\".\",b.`file_extension`) AS content_url FROM tb_cms_contents a, tb_cms_contents_files b WHERE a.`content_id` not in (SELECT content_id FROM tb_cms_contents_portalmap WHERE portal_id="+this.getSelectedPortalId()+" and tab_id="+this.getSelectedTabId()+" and operator_id="+this.getSelectedOperatorId()+") and a.`cat_nr`=b.`cat_nr` AND a.status>=1 AND b.`preview_file`=1 AND b.`file_name` LIKE '%"+previewSize+"'");
            System.out.println("reloadContentRecords() :: "+2 +" :: "+sb.toString());
            if(RUtil.strToInt(this.selectedSubCategory, 0)>0)
                sb.append(" and a.category_id=").append(this.getSelectedSubCategory());
            System.out.println("reloadContentRecords() :: "+4 +" :: "+sb.toString());
            if(RUtil.strToInt(this.selectedContentType, 0)>0)
                sb.append(" and a.content_type_id=").append(this.getSelectedContentType());
            if(RUtil.strToInt(this.selectedContentProvider, 0)>0)
                sb.append(" and a.cp_id=").append(this.getSelectedContentProvider());
            System.out.println("reloadContentRecords() :: "+5 +" :: "+sb.toString());
            sb.append(" order by a.content_id desc;");
            String sql1=sb.toString();
            
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadContentRecords() :: Query : "+sql1);
            rs=st.executeQuery(sql1);
            CMSContent newContent=null;
            availableCMSContent.clear();
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
                this.availableCMSContent.add(newContent);
                newContent=null;
            }
            if(rs!=null) rs.close();
            rs=null;
            
        }catch(Exception e){
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: reloadContentRecords() :: Exception "+e);
        }finally{
            try{if(rs!=null) rs.close();}catch(Exception ee){}
            rs=null;
        }
    }
    
public void reloadCPList(java.sql.Statement st){
    try {
            String sql1="";
            if(this.getRoleId()==1 || this.getRoleId()==2)
                sql1="SELECT a.cp_user_id,CONCAT(b.user_name,' (',c.company_name,')') AS cp_name FROM tb_portal_cp_mapping a, tb_users b,tb_registered_companies c WHERE a.cp_user_id=b.user_id AND b.role_id=3 AND b.company_id=c.company_id AND  a.portal_id="+this.getSelectedPortalId()+";";
            else
                sql1="SELECT a.cp_user_id,CONCAT(b.user_name,' (',c.company_name,')') AS cp_name FROM tb_portal_cp_mapping a, tb_users b,tb_registered_companies c WHERE a.cp_user_id=b.user_id AND b.role_id=3 AND b.company_id=c.company_id AND  a.portal_id="+this.getSelectedPortalId()+" and a.cp_user_id="+this.getLoginId()+";";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadCPList() :: Query : "+sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            contentCPList.clear();
            while (rs.next()) {
                contentCPList.put(rs.getString("cp_name"),rs.getString("cp_user_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadCPList() ::  Collection Size "+portalList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: reloadCPList() :: Exception "+e.getMessage());
           
        }
}

public void reloadOperatorList(java.sql.Statement st) {
        try {
            String sql1="";
            sql1="SELECT DISTINCT a.operator_id,CONCAT(c.`country_name`,\"-\",b.operator_name) AS operator_name FROM tb_rating_operator_configurations a , tb_operators b,tb_country c WHERE a.`operator_id`=b.`operator_id` AND b.`country_id`=c.`country_id` AND a.`portal_id`="+this.getSelectedPortalId()+";";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadOperatorList() :: Query : "+sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            operatorList.clear();
            while (rs.next()) {
               
                operatorList.put(rs.getString("operator_name"),rs.getString("operator_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadOperatorList() ::  Collection Size "+portalList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: reloadOperatorList() :: Exception "+e.getMessage());
           
        }
    }
public void reloadTabList(java.sql.Statement st){
    try {
        
            String sql1="";

            if(this.getRoleId()==1 || this.getRoleId()==2)
                sql1="select tab_name,id from tb_tab_conf where source='"+this.getMappingFor()+"';";
            else
                sql1="select tab_name,id from tb_tab_conf where portal_id="+this.getSelectedPortalId()+" and source='"+this.getMappingFor()+"';";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadTABist() :: Query : "+sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            tabList.clear();
            while (rs.next()) {
                tabList.put(rs.getString("tab_name"),rs.getString("id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadTABist() ::  Collection Size "+portalList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: reloadTABist() :: Exception "+e.getMessage());
           
        }
}


public void reloadPortalList(java.sql.Statement st) {
        try {
            String sql1="";
            if(this.getRoleId()==1 || this.getRoleId()==2)
                sql1="SELECT DISTINCT portal_name,portal_id FROM vw_portal_services;";
            else
                sql1="SELECT DISTINCT portal_name,portal_id FROM vw_portal_services WHERE user_id="+this.getLoginId()+";";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadPortalList() :: Query : "+sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            portalList.clear();
            while (rs.next()) {
                portalList.put(rs.getString("portal_name"),rs.getString("portal_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadPortalList() ::  Collection Size "+portalList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: reloadPortalList() :: Exception "+e.getMessage());
           
        }
    }

public void reloadContentTypeList(java.sql.Statement st) {
        try {
            contentTypeList.clear();
            String sql1="SELECT content_type_id,content_type_name FROM tb_cms_content_type WHERE STATUS>0 ORDER BY content_type_name;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadContentTypeList() :: Query : "+sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            while (rs.next()) {
                contentTypeList.put(rs.getString("content_type_name"), rs.getString("content_type_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadContentTypeList() ::  Collection Size "+contentTypeList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: reloadContentTypeList() :: Exception "+e.getMessage());
        }
    }

 public void reloadSubCategoryList(java.sql.Statement st) {
        try {
            int catId=RUtil.strToInt(selectedCategory, -1);
            contentSubCategoryList.clear();
            String sql1="SELECT catg_id,catg_name FROM tb_content_categories WHERE STATUS>0 AND parent_catg>0 and parent_catg="+catId+" and catg_id in (SELECT DISTINCT category_id FROM tb_cms_contents WHERE content_type_id='"+this.getSelectedContentType()+"' and cp_id="+selectedContentProvider+") ORDER BY catg_name;";
            //String sql1="SELECT catg_id,catg_name FROM tb_content_categories WHERE STATUS>0 AND parent_catg=0 and catg_id in (SELECT DISTINCT parent_catg FROM tb_content_categories WHERE catg_id IN (SELECT DISTINCT category_id FROM tb_cms_contents WHERE content_type_id='"+this.getSelectedContentType()+"' and cp_id="+selectedContentProvider+")) ORDER BY catg_name;";
            
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadSubCategoryList() :: Query : "+sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            while (rs.next()) {
                contentSubCategoryList.put(rs.getString("catg_name"), rs.getString("catg_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadSubCategoryList() ::  Collection Size "+contentSubCategoryList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: reloadSubCategoryList() :: Exception "+e.getMessage());
           
        }
    }

 public void reloadContentCategoryList(java.sql.Statement st) {
        try {
            contentCategoryList.clear();
            String sql1="SELECT catg_id,catg_name FROM tb_content_categories WHERE STATUS>0 AND parent_catg=0 and catg_id in (SELECT DISTINCT parent_catg FROM tb_content_categories WHERE catg_id IN (SELECT DISTINCT category_id FROM tb_cms_contents WHERE content_type_id='"+this.getSelectedContentType()+"' and cp_id="+selectedContentProvider+")) ORDER BY catg_name;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadContentCategoryList() :: Query : "+sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            while (rs.next()) {
                contentCategoryList.put(rs.getString("catg_name"), rs.getString("catg_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalContentSchedulingBean.class :: reloadContentCategoryList() ::  Collection Size "+contentCategoryList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: reloadContentCategoryList() :: Exception "+e.getMessage());
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
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: fetchLoginDetails() :: Exception "+e.getMessage());
        }
        loginBeanObj = null;

    }

   

    public String getSelectedSubCategory() {
        return selectedSubCategory;
    }

    public void setSelectedSubCategory(String selectedSubCategory) {
        this.selectedSubCategory = selectedSubCategory;
    }

    public String getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(String selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public Map<String, String> getContentSubCategoryList() {
        return contentSubCategoryList;
    }

    public void setContentSubCategoryList(Map<String, String> contentSubCategoryList) {
        this.contentSubCategoryList = contentSubCategoryList;
    }

    public Map<String, String> getContentCategoryList() {
        return contentCategoryList;
    }

    public void setContentCategoryList(Map<String, String> contentCategoryList) {
        this.contentCategoryList = contentCategoryList;
    }

    public Map<String, String> getContentTypeList() {
        return contentTypeList;
    }

    public void setContentTypeList(Map<String, String> contentTypeList) {
        this.contentTypeList = contentTypeList;
    }

    public String getSelectedContentType() {
        return selectedContentType;
    }

    public void setSelectedContentType(String selectedContentType) {
        this.selectedContentType = selectedContentType;
    }

    public String getSelectedPortalId() {
        return selectedPortalId;
    }

    public void setSelectedPortalId(String selectedPortalId) {
        this.selectedPortalId = selectedPortalId;
    }

   
    public Map<String, String> getPortalList() {
        return portalList;
    }

    public void setPortalList(Map<String, String> portalList) {
        this.portalList = portalList;
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

    public String getContentBasePath() {
        return contentBasePath;
    }

    public void setContentBasePath(String contentBasePath) {
        this.contentBasePath = contentBasePath;
    }

    public String getContentBaseURL() {
        return contentBaseURL;
    }

    public void setContentBaseURL(String contentBaseURL) {
        this.contentBaseURL = contentBaseURL;
    }

    public List<CMSContent> getAvailableCMSContent() {
        return availableCMSContent;
    }

    public void setAvailableCMSContent(List<CMSContent> availableCMSContent) {
        this.availableCMSContent = availableCMSContent;
    }

    public List<CMSContent> getPortalMappedContent() {
        return portalMappedContent;
    }

    public void setPortalMappedContent(List<CMSContent> portalMappedContent) {
        this.portalMappedContent = portalMappedContent;
    }

    public List<CMSContent> getSelectedCMSContents() {
        return selectedCMSContents;
    }

    public void setSelectedCMSContents(List<CMSContent> selectedCMSContents) {
        this.selectedCMSContents = selectedCMSContents;
    }

    public List<CMSContent> getSelectedPortalContents() {
        return selectedPortalContents;
    }

    public void setSelectedPortalContents(List<CMSContent> selectedPortalContents) {
        this.selectedPortalContents = selectedPortalContents;
    }

   
    public String getSelectedContentProvider() {
        return selectedContentProvider;
    }

    public void setSelectedContentProvider(String selectedContentProvider) {
        this.selectedContentProvider = selectedContentProvider;
    }

    public Map<String, String> getContentCPList() {
        return contentCPList;
    }

    public void setContentCPList(Map<String, String> contentCPList) {
        this.contentCPList = contentCPList;
    }

    public Map<String, String> getOperatorList() {
        return operatorList;
    }

    public void setOperatorList(Map<String, String> operatorList) {
        this.operatorList = operatorList;
    }

    public String getSelectedOperatorId() {
        return selectedOperatorId;
    }

    public void setSelectedOperatorId(String selectedOperatorId) {
        this.selectedOperatorId = selectedOperatorId;
    }

    public String getSelectedTabId() {
        return selectedTabId;
    }

    public void setSelectedTabId(String selectedTabId) {
        this.selectedTabId = selectedTabId;
    }

    public Map<String, String> getTabList() {
        return tabList;
    }

    public void setTabList(Map<String, String> tabList) {
        this.tabList = tabList;
    }

    public String getMappingFor() {
        return mappingFor;
    }

    public void setMappingFor(String mappingFor) {
        this.mappingFor = mappingFor;
    }

 
    
}
