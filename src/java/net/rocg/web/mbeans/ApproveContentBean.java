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
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.rocg.util.DBConnection;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import net.rocg.web.beans.CMSContent;

/**
 *
 * @author Rishi Tyagi
 */
@ManagedBean
@RequestScoped
public class ApproveContentBean {

    DBConnection dbConn;
    int loginId, roleId;
    StatusMessage statusMsg;
    String loginName;
    String selectedContentProvider;
    Map<String, String> contentProviderList;
    List<CMSContent> contentList;
    CMSContent selectedContent;
    String tempContentBaseURL, tempContentBasePath, approvedContentBasePath, approvedContentBaseURL;

    /**
     * Creates a new instance of ApproveContentBean
     */
    public ApproveContentBean() {
        fetchLoginDetails();
        contentList = new ArrayList<>();
        contentProviderList = new HashMap<>();
        tempContentBaseURL = RUtil.getStringProperty("temp_content_baseurl", "");
        tempContentBasePath = RUtil.getStringProperty("temp_content_basefolder", "");
        approvedContentBasePath = RUtil.getStringProperty("cms_content_basefolder", "");
        approvedContentBaseURL = RUtil.getStringProperty("cms_content_baseurl", "");
        statusMsg = new StatusMessage();
        dbConn = new DBConnection();
        reloadBulkContentRecords(true, true);
    }

    public void approveContent(String action) {
        if (action.equalsIgnoreCase("APPROVE")) {
            System.out.println("Approved content " + this.getSelectedContent().getContentId());
            reloadBulkContentRecords(1, true, false);
        } else {
            System.out.println("Reject content " + this.getSelectedContent().getContentId());
            reloadBulkContentRecords(-1, true, false);
        }
    }

    public void reloadBulkContentRecords(boolean reloadContentRecords, boolean reloadCPList) {
        reloadBulkContentRecords(0, true, true);
    }

    public void reloadBulkContentRecords(int contentAction, boolean reloadContentRecords, boolean reloadCPList) {
        java.sql.Connection conn = dbConn.connect();
        if (conn != null) {
            try {
                java.sql.Statement st = conn.createStatement();
                if (contentAction == 1) {
                    //Move Content Folder
                    String sourceFolder = this.getTempContentBasePath() + getSelectedContent().getContentFolder();
                    String destFolder = this.getApprovedContentBasePath() + getSelectedContent().getContentFolder();
                    int filesMoved = RUtil.moveFolder(sourceFolder, destFolder);
                    System.out.println(filesMoved + " files moved from source (" + sourceFolder + ") to dest (" + destFolder + ")");
                    //Approve Content
                    if (filesMoved > 0) {
                        st.executeUpdate("update tb_cms_contents set status=1, approve_date=now(), last_update=now(), approved_by=" + this.getLoginId() + ", last_update_by=" + this.getLoginId() + " where content_id=" + this.getSelectedContent().getContentId());
                        this.statusMsg.setMessage("Content (" + getSelectedContent().getContentId() + ") approved! " + filesMoved + " files transfered to content folder.", StatusMessage.MSG_CLASS_INFO);
                    } else {
                        this.statusMsg.setMessage("Failed to move files!", StatusMessage.MSG_CLASS_ERROR);
                    }
                } else if (contentAction == -1) {
                    //Delete content Files
                    String sourceFolder = this.getTempContentBasePath() + getSelectedContent().getContentFolder();
                    int filesDeleted = RUtil.deleteAllFiles(sourceFolder);
                    System.out.println(filesDeleted + " files deleted from source folder (" + sourceFolder + ")");
                    //reject Content
                    if (filesDeleted > 0) {
                        st.executeUpdate("update tb_cms_contents set status=-3,last_update=now(),last_update_by=" + this.getLoginId() + " where content_id=" + this.getSelectedContent().getContentId());
                        this.statusMsg.setMessage("Content (" + getSelectedContent().getContentId() + ") Rejected! " + filesDeleted + " files removed from system completely.", StatusMessage.MSG_CLASS_INFO);
                    } else {
                        this.statusMsg.setMessage("Failed to delete files from (" + sourceFolder + ")!", StatusMessage.MSG_CLASS_ERROR);
                    }
                }

                if (reloadContentRecords) {
                    reloadContentRecords(st);
                }
                if (reloadCPList) {
                    reloadCPList(st);
                }
                if (st != null) {
                    st.close();
                }
                st = null;
            } catch (Exception e) {
                System.out.println(" reloadBulkContentRecords():: Exception " + e);
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

    }

    public void reloadContentRecords(java.sql.Statement st) {
        java.sql.ResultSet rs = null;
        try {

            String sql1 = "";
            if (RUtil.strToInt(this.selectedContentProvider, 0) == 0)//Select all CP content        
            {
                sql1 = "SELECT a.`content_id`,a.`cat_nr`,a.`content_type_id`,a.`category_id`,a.`copyright_id`,a.`cp_id`,"
                        + "a.`cp_catnr`,a.`content_name`, a.content_folder,CONCAT(a.`content_url`,b.`file_name`,\"_\",b.`parts_count`,\".\",b.`file_extension`) AS content_url FROM tb_cms_contents a, tb_cms_contents_files b WHERE a.`cat_nr`=b.`cat_nr` AND a.status=-1 AND b.`preview_file`=1 AND b.`file_name` LIKE '%50x75'";
            } else//select content for selected CP
            {
                sql1 = "SELECT a.`content_id`,a.`cat_nr`,a.`content_type_id`,a.`category_id`,a.`copyright_id`,a.`cp_id`,"
                        + "a.`cp_catnr`,a.`content_name`, a.content_folder,CONCAT(a.`content_url`,b.`file_name`,\"_\",b.`parts_count`,\".\",b.`file_extension`) AS content_url FROM tb_cms_contents a, tb_cms_contents_files b WHERE a.`cat_nr`=b.`cat_nr` AND a.status=-1 AND b.`preview_file`=1 AND b.`file_name` LIKE '%50x75' and a.`cp_id`=" + this.selectedContentProvider;
            }

            System.out.println("Reload Content Record Query " + sql1);

            rs = st.executeQuery(sql1);
            CMSContent newContent = null;
            contentList.clear();

            String contentBaseURL = RUtil.getStringProperty("temp_content_baseurl", "");
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
                this.contentList.add(newContent);
                newContent = null;
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;
            if (contentList.isEmpty()) {
                System.out.println("Reload Content Record Query " + sql1.replace("50x75", "50x50"));
                java.sql.ResultSet rst = null;

                try {
                    rst = st.executeQuery(sql1.replace("50x75", "50x50"));
                    while (rst.next()) {
                        newContent = new CMSContent();
                        newContent.setContentId(rst.getLong("content_id"));
                        newContent.setCatalogNumber(rst.getLong("cat_nr"));
                        newContent.setContentTypeId(rst.getInt("content_type_id"));
                        newContent.setCategoryId(rst.getInt("category_id"));
                        newContent.setCopyrightId(rst.getInt("copyright_id"));
                        newContent.setContentProviderId(rst.getInt("cp_id"));
                        newContent.setCpcatalogNumber(rst.getString("cp_catnr"));
                        newContent.setContentName(rst.getString("content_name"));
                        newContent.setContentFolder(rst.getString("content_folder"));
                        newContent.setContentPath(rst.getString("content_url"));
                        this.contentList.add(newContent);
                        newContent = null;
                    }
                    if (rst != null) {
                        rst.close();
                    }
                    rst = null;

                } catch (Exception e) {
                    System.out.println(" reloadContentRecords()2 :: Exception " + e);
                } finally {
                    try {
                        if (rst != null) {
                            rst.close();
                        }
                    } catch (Exception ee) {
                    }
                    rst = null;
                }
            }
        } catch (Exception e) {
            System.out.println(" reloadContentRecords() :: Exception " + e);
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
        java.sql.ResultSet rs = null;
        try {
            String sql1 = "SELECT a.user_id,a.user_name,b.company_name FROM tb_users a,tb_registered_companies b WHERE a.`company_id`=b.`company_id` AND a.`role_id`=3 ORDER BY 3,2";
            System.out.println("Reload CP List Query : " + sql1);
            rs = st.executeQuery(sql1);
            contentProviderList.clear();
            while (rs.next()) {
                contentProviderList.put(rs.getString("company_name") + "-" + rs.getString("user_name"), rs.getString("user_id"));
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            System.out.println(" reloadCPList() :: Exception " + e);
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

    public CMSContent getSelectedContent() {
        return selectedContent;
    }

    public void setSelectedContent(CMSContent selectedContent) {
        this.selectedContent = selectedContent;
    }

    public List<CMSContent> getContentList() {
        return contentList;
    }

    public void setContentList(List<CMSContent> contentList) {
        this.contentList = contentList;
    }

    public String getTempContentBaseURL() {
        return tempContentBaseURL;
    }

    public void setTempContentBaseURL(String tempContentBaseURL) {
        this.tempContentBaseURL = tempContentBaseURL;
    }

    public String getTempContentBasePath() {
        return tempContentBasePath;
    }

    public void setTempContentBasePath(String tempContentBasePath) {
        this.tempContentBasePath = tempContentBasePath;
    }

    public String getApprovedContentBasePath() {
        return approvedContentBasePath;
    }

    public void setApprovedContentBasePath(String approvedContentBasePath) {
        this.approvedContentBasePath = approvedContentBasePath;
    }

    public String getApprovedContentBaseURL() {
        return approvedContentBaseURL;
    }

    public void setApprovedContentBaseURL(String approvedContentBaseURL) {
        this.approvedContentBaseURL = approvedContentBaseURL;
    }

}
