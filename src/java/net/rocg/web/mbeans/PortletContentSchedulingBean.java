/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rocg.web.mbeans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
 * @author Rishi Tyagi
 */
@ManagedBean
@SessionScoped
public class PortletContentSchedulingBean {

    DBConnection dbConn;
    int loginId, roleId;
    StatusMessage statusMsg;
    String loginName;
    String selectedOperatorId;
    String selectedPortletId;
    String selectedPortalId;
    String selectedContentType;
    String selectedCategory;
    String selectedSubCategory;
    String selectedContentProvider;
    Map<String, String> portalList;
    Map<String, String> contentTypeList;
    Map<String, String> contentCategoryList;
    Map<String, String> contentSubCategoryList;
    Map<String, String> contentCPList;
    Map<String, String> operatorList;
    Map<String, String> portletList;
    List<CMSContent> availableCMSContent, portalMappedContent;
    List<CMSContent> selectedCMSContents, selectedPortalContents;

    // CMSContent selectedSourceContent,selectedPortalContent;
    String contentBasePath, contentBaseURL;

    /**
     * Creates a new instance of PortalHomeContentSchedulingBean
     */
    public PortletContentSchedulingBean() {
        availableCMSContent = new ArrayList<CMSContent>();
        portalMappedContent = new ArrayList<CMSContent>();
        statusMsg = new StatusMessage();
        dbConn = new DBConnection();
        fetchLoginDetails();
        contentBasePath = RUtil.getStringProperty("cms_content_basefolder", "");
        contentBaseURL = RUtil.getStringProperty("cms_content_baseurl", "");
        portalList = new HashMap<String, String>();
        contentTypeList = new HashMap<String, String>();
        contentCategoryList = new HashMap<String, String>();
        contentSubCategoryList = new HashMap<String, String>();
        contentCPList = new HashMap<String, String>();
        operatorList = new HashMap<String, String>();
        portletList = new HashMap<String, String>();
        fetchData(true, false, true, false, false, false, false, false, false);
    }

    public void fetchData(boolean reloadPortalList, boolean reloadPortletList, boolean reloadContentTypeList, boolean reloadCPList, boolean reloadCategoryList, boolean reloadSubCategory, boolean reloadContent, boolean reloadPortalContent, boolean addContent) {
        java.sql.Connection conn = dbConn.connect();
        System.out.println("selectedPortal " + selectedPortalId + ", selectedContentType " + selectedContentType + ", selectedContentProvider=" + selectedContentProvider + ", selectedCategory " + selectedCategory + ", selectedSubCategory " + selectedSubCategory);
        if (conn != null) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: fetchData() :: Database Connected.");
            try {
                java.sql.Statement st = conn.createStatement();

                if (reloadPortalList) {
                    this.reloadPortalList(st);
                }
                if (reloadContentTypeList) {
                    this.reloadContentTypeList(st);
                }
                if (reloadCPList) {
                    this.reloadCPList(st);
                }
                if (reloadCategoryList) {
                    this.reloadContentCategoryList(st);
                }
                if (reloadSubCategory) {
                    this.reloadSubCategoryList(st);
                }
                if (reloadContent) {
                    this.reloadContentRecords(st);
                }
                if (reloadPortalContent) {
                    this.reloadPortalContentRecords(st);
                }
                if (reloadPortletList) {
                    this.reloadPortletList(st);
                }
                if (addContent) {
                    if (st != null) {
                        st.close();
                    }
                    st = null;
                    int count = 0;
//                    ResultSet rs=st.executeQuery("select count(*) as cnt from tb_cms_contents_portlet;");
//                    while(rs.next()){
//                       count=rs.getInt("cnt");
//                    }
                    int ptId = RUtil.strToInt(this.getSelectedPortletId(), 0);
                    if (ptId <= 0) {
                        dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: fetchData() ::Invalid Operator Id , Please select an operator to schedule content for.");
                        statusMsg.setMessage("Please select an portlet to schedule content for portlet.", StatusMessage.MSG_CLASS_ERROR);
                    } //                    else if(count>=5)
                    //                    {
                   
                    //                        dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: fetchData() ::Sileder content count exceed , Please remove old content to map new content.");
                    //                        statusMsg.setMessage("Please remove old content to map new content.", StatusMessage.MSG_CLASS_ERROR);
                    //                    }
                    else if (this.selectedCMSContents.size() > 0) {

                        PreparedStatement pst = conn.prepareStatement("insert into tb_cms_contents_portlet(content_id,portlet_id,portlet_flag,mapping_date,mapped_by,STATUS,show_order) values(?," + this.getSelectedPortletId()+ ",1,now()," + this.getLoginId() + ",1,?);");
                        for (CMSContent newC : this.selectedCMSContents) {
                            pst.setLong(1, newC.getContentId());
                            pst.setInt(2, newC.getShowOrder());
                            pst.executeUpdate();
                        }
                        pst.close();
                        pst = null;
                        st = conn.createStatement();
                        this.reloadContentRecords(st);
                        this.reloadPortalContentRecords(st);
                        
                    }

                }
            } catch (Exception e) {
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: fetchData() :: Exception " + e);
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (Exception ee) {
                }
                conn = null;
            }
        } else {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: fetchData() :: Database Connectivity Failed");
            statusMsg.setMessage("A process failed while reloading Country List. Database Connectivity Issue.", StatusMessage.MSG_CLASS_ERROR);
        }

    }

    public void removeContentFromPortal() {

        if (this.getSelectedPortalContents().size() > 0) {
            String sql1 = "delete from tb_cms_contents_portlet where portlet_id=" + this.getSelectedPortletId()+ " and portlet_flag=1 and content_id in (";
            for (CMSContent ct : this.getSelectedPortalContents()) {
                sql1 = sql1 + ct.getContentId() + ",";
            }
            if (sql1.endsWith(",")) {
                sql1 = sql1.substring(0, sql1.lastIndexOf(","));
            }
            sql1 += ")";
            int rep = 0;
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: removeContentFromPortlet() :: sql " + sql1);
            Connection conn = dbConn.connect();
            if (conn != null) {
                try {
                    Statement st = conn.createStatement();
                    rep = st.executeUpdate(sql1);
                    statusMsg.setMessage("Database Updated successfully!", StatusMessage.MSG_CLASS_INFO);
                    this.reloadPortalContentRecords(st);
                } catch (Exception e) {
                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: removeContentFromPortlet() :: Exception " + e);
                    statusMsg.setMessage("Failed to update database.", StatusMessage.MSG_CLASS_ERROR);
                } finally {
                    try {
                        if (conn != null) {
                            conn.close();
                        }
                    } catch (Exception ee) {
                    }
                    conn = null;
                }
            }

        } else {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: removeContentFromPortlet() :: No content selected to remove from portal.");
            statusMsg.setMessage(" Please select a Content to remove.", StatusMessage.MSG_CLASS_ERROR);
        }

    }

    public void onEdit(RowEditEvent event) {
        CMSContent actionObj = (CMSContent) event.getObject();
        if (actionObj != null && actionObj.getContentId() > 0) {
            String sql1 = "update tb_cms_contents_portlet set show_order=" + actionObj.getShowOrder() + " where portlet_id=" + this.getSelectedPortletId()+ " and portlet_flag=1 and content_id=" + actionObj.getContentId() + ";";
            int rep = 0;
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: onEdit() :: sql " + sql1);
            Connection conn = dbConn.connect();
            if (conn != null) {
                try {
                    Statement st = conn.createStatement();
                    rep = st.executeUpdate(sql1);
                    statusMsg.setMessage("Database Updated successfully!", StatusMessage.MSG_CLASS_INFO);
                    this.reloadPortalContentRecords(st);
                } catch (Exception e) {
                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: onEdit() :: Exception " + e);
                    statusMsg.setMessage("Failed to update database.", StatusMessage.MSG_CLASS_ERROR);
                } finally {
                    try {
                        if (conn != null) {
                            conn.close();
                        }
                    } catch (Exception ee) {
                    }
                    conn = null;
                }
            }

        } else {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: onEdit() :: Invalid Content Id (" + actionObj.getContentId() + ") to update.");
            statusMsg.setMessage("Invalid Content Id `" + actionObj.getContentId() + "`. Please select a Content to update.", StatusMessage.MSG_CLASS_ERROR);
        }

    }

    public void reloadPortalContentRecords(java.sql.Statement st) {
        java.sql.ResultSet rs = null;
        try {
            System.out.println("reloadPortletContentRecords() :: " + 1);
            StringBuilder sb = new StringBuilder("SELECT c.`content_id`,c.show_order,a.`cat_nr`,a.`content_type_id`,a.`category_id`,a.`copyright_id`,a.`cp_id`,"
                    + "a.`cp_catnr`,a.`content_name`, a.content_folder,CONCAT(a.`content_url`,b.`file_name`,\"_\",b.`parts_count`,\".\",b.`file_extension`) AS content_url FROM tb_cms_contents_portlet c, tb_cms_contents a, tb_cms_contents_files b WHERE  c.portlet_id=" + this.getSelectedPortletId()+ " and portlet_flag=1 and c.content_id=a.content_id and a.`cat_nr`=b.`cat_nr` AND a.status>=1 AND b.`preview_file`=1 AND b.`file_name` LIKE '%50x50' ");
            System.out.println("reloadPortletContentRecords() :: " + 2 + " :: " + sb.toString());
            if (RUtil.strToInt(this.selectedSubCategory, 0) > 0) {
                sb.append(" and a.category_id=").append(this.getSelectedSubCategory());
            }
            System.out.println("reloadPortletContentRecords() :: " + 4 + " :: " + sb.toString());
            if (RUtil.strToInt(this.selectedContentType, 0) > 0) {
                sb.append(" and a.content_type_id=").append(this.getSelectedContentType());
            }
            System.out.println("reloadPortletContentRecords() :: " + 5 + " :: " + sb.toString());
           // sb.append(" order by c.show_order desc;");
           sb.append(";");
            String sql1 = sb.toString();

            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadPortletContentRecords() :: Query : " + sql1);
            rs = st.executeQuery(sql1);
            CMSContent newContent = null;
            portalMappedContent.clear();
            String contentBaseURL = this.getContentBaseURL();
            while (rs.next()) {
                newContent = new CMSContent();
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
                newContent = null;
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;

        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: reloadPortletContentRecords() :: Exception " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ee) {
            }
            rs = null;
        }
    }

    public void reloadContentRecords(java.sql.Statement st) {
        java.sql.ResultSet rs = null;
        try {
            System.out.println("reloadContentRecords() :: " + 1);
            StringBuilder sb = new StringBuilder("SELECT a.`content_id`,a.`cat_nr`,a.`content_type_id`,a.`category_id`,a.`copyright_id`,a.`cp_id`,"
                    + "a.`cp_catnr`,a.`content_name`, a.content_folder,CONCAT(a.`content_url`,b.`file_name`,\"_\",b.`parts_count`,\".\",b.`file_extension`) AS content_url FROM tb_cms_contents a, tb_cms_contents_files b WHERE a.`content_id` not in (SELECT content_id FROM tb_cms_contents_portlet WHERE portlet_id=" + this.getSelectedPortletId()+ " and portlet_flag=1 ) and a.`cat_nr`=b.`cat_nr` AND a.status>=1 AND b.`preview_file`=1 AND b.`file_name` LIKE '%50x50'");
            System.out.println("reloadContentRecords() :: " + 2 + " :: " + sb.toString());
            if (RUtil.strToInt(this.selectedSubCategory, 0) > 0) {
                sb.append(" and a.category_id=").append(this.getSelectedSubCategory());
            }
            System.out.println("reloadContentRecords() :: " + 4 + " :: " + sb.toString());
            if (RUtil.strToInt(this.selectedContentType, 0) > 0) {
                sb.append(" and a.content_type_id=").append(this.getSelectedContentType());
            }
            if (RUtil.strToInt(this.selectedContentProvider, 0) > 0) {
                sb.append(" and a.cp_id=").append(this.getSelectedContentProvider());
            }
            System.out.println("reloadContentRecords() :: " + 5 + " :: " + sb.toString());
            sb.append(" order by a.content_id desc;");
            String sql1 = sb.toString();

            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadContentRecords() :: Query : " + sql1);
            rs = st.executeQuery(sql1);
            CMSContent newContent = null;
            availableCMSContent.clear();
            String contentBaseURL = this.getContentBaseURL();
            while (rs.next()) {
                newContent = new CMSContent();
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
                newContent = null;
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;

        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: reloadContentRecords() :: Exception " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ee) {
            }
            rs = null;
        }
    }

    public void reloadCPList(java.sql.Statement st) {
        try {
            String sql1 = "";
            if (this.getRoleId() == 1 || this.getRoleId() == 2) {
                sql1 = "SELECT a.cp_user_id,CONCAT(b.user_name,' (',c.company_name,')') AS cp_name FROM tb_portal_cp_mapping a, tb_users b,tb_registered_companies c WHERE a.cp_user_id=b.user_id AND b.role_id=3 AND b.company_id=c.company_id AND  a.portal_id=" + this.getSelectedPortalId() + ";";
            } else {
                sql1 = "SELECT a.cp_user_id,CONCAT(b.user_name,' (',c.company_name,')') AS cp_name FROM tb_portal_cp_mapping a, tb_users b,tb_registered_companies c WHERE a.cp_user_id=b.user_id AND b.role_id=3 AND b.company_id=c.company_id AND  a.portal_id=" + this.getSelectedPortalId() + " and a.cp_user_id=" + this.getLoginId() + ";";
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadCPList() :: Query : " + sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            contentCPList.clear();
            while (rs.next()) {
                contentCPList.put(rs.getString("cp_name"), rs.getString("cp_user_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadCPList() ::  Collection Size " + portalList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: reloadCPList() :: Exception " + e.getMessage());

        }
    }

    public void reloadOperatorList(java.sql.Statement st) {
        try {
            String sql1 = "";
            sql1 = "SELECT DISTINCT a.operator_id,CONCAT(c.`country_name`,\"-\",b.operator_name) AS operator_name FROM tb_rating_operator_configurations a , tb_operators b,tb_country c WHERE a.`operator_id`=b.`operator_id` AND b.`country_id`=c.`country_id` AND a.`portal_id`=" + this.getSelectedPortalId() + ";";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadOperatorList() :: Query : " + sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            operatorList.clear();
            while (rs.next()) {

                operatorList.put(rs.getString("operator_name"), rs.getString("operator_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadOperatorList() ::  Collection Size " + portalList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: reloadOperatorList() :: Exception " + e.getMessage());

        }
    }
    public void reloadPortletList(java.sql.Statement st) {
        try {
            String sql1 = "";
            sql1 = "select portlets_id,portlets_name from tb_portlets where portal_id=" + this.getSelectedPortalId() + ";";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadPortletList() :: Query : " + sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            portletList.clear();
            while (rs.next()) {

                portletList.put(rs.getString("portlets_name"), rs.getString("portlets_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadPortletList() ::  Collection Size " + portalList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: reloadPortletList() :: Exception " + e.getMessage());

        }
    }
    
    public void reloadPortalList(java.sql.Statement st) {
        try {
            String sql1 = "";
            if (this.getRoleId() == 1 || this.getRoleId() == 2) {
                sql1 = "SELECT DISTINCT portal_name,portal_id FROM vw_portal_services;";
            } else {
                sql1 = "SELECT DISTINCT portal_name,portal_id FROM vw_portal_services WHERE user_id=" + this.getLoginId() + ";";
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadPortalList() :: Query : " + sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            portalList.clear();
            while (rs.next()) {
                portalList.put(rs.getString("portal_name"), rs.getString("portal_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadPortalList() ::  Collection Size " + portalList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: reloadPortalList() :: Exception " + e.getMessage());

        }
    }

    public void reloadContentTypeList(java.sql.Statement st) {
        try {
            contentTypeList.clear();
            String sql1 = "SELECT content_type_id,content_type_name FROM tb_cms_content_type WHERE STATUS>0 ORDER BY content_type_name;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadContentTypeList() :: Query : " + sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            while (rs.next()) {
                contentTypeList.put(rs.getString("content_type_name"), rs.getString("content_type_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadContentTypeList() ::  Collection Size " + contentTypeList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: reloadContentTypeList() :: Exception " + e.getMessage());
        }
    }

    public void reloadSubCategoryList(java.sql.Statement st) {
        try {
            int catId = RUtil.strToInt(selectedCategory, -1);
            contentSubCategoryList.clear();
            String sql1 = "SELECT catg_id,catg_name FROM tb_content_categories WHERE STATUS>0 AND parent_catg>0 and parent_catg=" + catId + " and catg_id in (SELECT DISTINCT category_id FROM tb_cms_contents WHERE content_type_id='" + this.getSelectedContentType() + "' and cp_id=" + selectedContentProvider + ") ORDER BY catg_name;";
            //String sql1="SELECT catg_id,catg_name FROM tb_content_categories WHERE STATUS>0 AND parent_catg=0 and catg_id in (SELECT DISTINCT parent_catg FROM tb_content_categories WHERE catg_id IN (SELECT DISTINCT category_id FROM tb_cms_contents WHERE content_type_id='"+this.getSelectedContentType()+"' and cp_id="+selectedContentProvider+")) ORDER BY catg_name;";

            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadSubCategoryList() :: Query : " + sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            while (rs.next()) {
                contentSubCategoryList.put(rs.getString("catg_name"), rs.getString("catg_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadSubCategoryList() ::  Collection Size " + contentSubCategoryList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: reloadSubCategoryList() :: Exception " + e.getMessage());

        }
    }

    public void reloadContentCategoryList(java.sql.Statement st) {
        try {
            contentCategoryList.clear();
            String sql1 = "SELECT catg_id,catg_name FROM tb_content_categories WHERE STATUS>0 AND parent_catg=0 and catg_id in (SELECT DISTINCT parent_catg FROM tb_content_categories WHERE catg_id IN (SELECT DISTINCT category_id FROM tb_cms_contents WHERE content_type_id='" + this.getSelectedContentType() + "' and cp_id=" + this.getSelectedContentProvider() + ")) ORDER BY catg_name;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadContentCategoryList() :: Query : " + sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            while (rs.next()) {
                contentCategoryList.put(rs.getString("catg_name"), rs.getString("catg_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortletContentSchedulingBean.class :: reloadContentCategoryList() ::  Collection Size " + contentCategoryList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: reloadContentCategoryList() :: Exception " + e.getMessage());
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
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortletContentSchedulingBean.class :: fetchLoginDetails() :: Exception " + e.getMessage());
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

    public String getSelectedPortletId() {
        return selectedPortletId;
    }

    public void setSelectedPortletId(String selectedPortletId) {
        this.selectedPortletId = selectedPortletId;
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

    public Map<String, String> getPortletList() {
        return portletList;
    }

    public void setPortletList(Map<String, String> portletList) {
        this.portletList = portletList;
    }

    public String getSelectedOperatorId() {
        return selectedOperatorId;
    }

    public void setSelectedOperatorId(String selectedOperatorId) {
        this.selectedOperatorId = selectedOperatorId;
    }

}
