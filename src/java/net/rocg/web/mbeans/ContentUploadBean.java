/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rocg.web.mbeans;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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
import net.rocg.web.beans.CMSContentFile;
import net.rocg.web.beans.CMSContentFilePart;
import net.rocg.web.beans.ContentValidationRule;
import net.rocg.web.beans.ContentValidator;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Rishi Tyagi
 */
@ManagedBean
@SessionScoped
public class ContentUploadBean implements java.io.Serializable {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat sdfdb = new SimpleDateFormat("yyyy-MM-dd");
    ContentValidator contentValidator = new ContentValidator();

    Map<String, String> contentTypeList;
    Map<String, String> superCategoryList;
    Map<String, String> contentCategoryList;
    Map<String, String> copyrightOwner;

    List<CMSContentFile> uploadedContentFiles, uploadedPreviewFiles;

    StatusMessage statusMsg;
    DBConnection dbConn;
    CMSContent newContent;

    boolean haveParts, disableMultipartOption;
    int loginId, roleId;
    String loginName, selectedContentType, uploadedFileNames, baseContentPath, baseContentURL, dateString;

    ContentValidationRule validationRule;

    /**
     * Creates a new instance of ContentUploadBean
     */
    public ContentUploadBean() {
        validationRule = new ContentValidationRule();
        uploadedContentFiles = new ArrayList<>();
        uploadedPreviewFiles = new ArrayList<>();
        dbConn = new DBConnection();
        newContent = new CMSContent();
        statusMsg = new StatusMessage();
        newContent.setContentTypeId(1);
        newContent.setShowOrder(1);
        contentTypeList = new HashMap<>();
        superCategoryList = new HashMap<>();
        contentCategoryList = new HashMap<>();
        copyrightOwner = new HashMap<>();
        this.setDisableMultipartOption(true);
        this.setBaseContentPath(RUtil.getStringProperty("temp_content_basefolder", ""));
        this.setBaseContentURL(RUtil.getStringProperty("temp_content_baseurl", ""));

        this.setDateString(sdf.format(new java.util.Date()));
        fetchLoginDetails();
        refreshList(true, true, false, true, false);
        RUtil.BULK_UPLOAD_COUNTER = (RUtil.BULK_UPLOAD_COUNTER >= 999) ? 1 : (RUtil.BULK_UPLOAD_COUNTER + 1);
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: MBean Connected.");
    }

    /**
     * Single Content Upload File Handler starts here
     *
     * @param event
     */
    public void printDetails() {
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: printDetails() :: " + newContent.toString());

    }

    public void commitContentDetails() {
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: commitContentDetails() :: Updating Content Details into Content Registry..");

        Connection conn = dbConn.connect();
        if (conn != null) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: commitContentDetails() :: Database Connected.");

            try {
                if (contentValidator != null && contentValidator.validate(newContent, uploadedContentFiles, uploadedPreviewFiles, validationRule)) {
                    conn.setAutoCommit(false);

                    String sql1 = "INSERT INTO tb_cms_contents(cat_nr,content_name,content_type_id,category_id,super_category_id,copyright_id,"
                            + "cp_id,cp_catnr,content_url,content_folder,hosting_type,show_order,STATUS,upload_date,last_update,uploaded_by,last_update_by,"
                            + "validity_start,validity_end,search_keywords) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,now(),now(),?,?,'" + sdfdb.format(newContent.getValidityStart()) + "','" + sdfdb.format(newContent.getValidityEnd()) + "',?)";
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: commitContentDetails() :: Content (" + newContent.getContentName() + ") Insert SQL ::" + sql1);
                    //Insert into Main Table, Also maintain Transaction while inserting records
                    PreparedStatement pst = conn.prepareCall(sql1);
                    pst.setLong(1, newContent.getCatalogNumber());
                    pst.setString(2, newContent.getContentName());
                    pst.setInt(3, newContent.getContentTypeId());
                    pst.setInt(4, newContent.getCategoryId());
                    pst.setInt(5, newContent.getSuperCategoryId());
                    pst.setInt(6, newContent.getCopyrightId());
                    pst.setInt(7, this.getLoginId());
                    pst.setString(8, newContent.getCpcatalogNumber());
                    pst.setString(9, newContent.getContentPath());
                    pst.setString(10, newContent.getContentFolder());
                    pst.setString(11, newContent.getHostingType());
                    pst.setInt(12, newContent.getShowOrder());
                    pst.setInt(13, -1);//Setting status=-1 i.e new Upload,pending for approval (-2=Disabled,-1=New Upload,0=Validity Expired,1=Active Content)
                    pst.setInt(14, this.getLoginId());
                    pst.setInt(15, this.getLoginId());
                    pst.setString(16, newContent.getSearchKeywords());
                    int rep = pst.executeUpdate();
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: commitContentDetails() :: Content(" + newContent.getContentName() + ") Insert DBResult::" + rep);
                    System.out.println("query result " + rep);
                    if (pst != null) {
                        pst.close();
                    }
                    pst = null;

                    if (rep > 0) {
                        //Insert Content detail data
                        sql1 = this.formatContentDetailQuery(false);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: commitContentDetails() :: Content(" + newContent.getContentName() + ") Details Insert SQL::" + sql1);
                        pst = conn.prepareStatement(sql1);
                        rep = this.insertContentDetailTable(pst, false);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: commitContentDetails() :: Content(" + newContent.getContentName() + ") Details Insert DBResult ::" + rep);
                        if (pst != null) {
                            pst.close();
                        }
                        pst = null;

                        //Insert content files data
                        sql1 = this.formatContentDetailQuery(true);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: commitContentDetails() :: Content(" + newContent.getContentName() + ") File Details Insert SQL::" + sql1);
                        pst = conn.prepareStatement(sql1);
                        rep = this.insertContentDetailTable(pst, true);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: commitContentDetails() :: Content(" + newContent.getContentName() + ") File Details Insert DBResult ::" + rep);
                        if (pst != null) {
                            pst.close();
                        }
                        pst = null;
                    }

                    if (rep > 0) {
                        conn.commit();
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "ContentUploadBean.class :: commitContentDetails() :: Content(" + newContent.getContentName() + ") Uploaded Successfully!");
                        this.statusMsg.setMessage("Content Uploaded", StatusMessage.MSG_CLASS_INFO);
                    } else {
                        conn.rollback();
                        dbConn.logUIMsg(RLogger.LOGGING_LEVEL_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ContentUploadBean.class :: commitContentDetails() :: Content(" + newContent.getContentName() + ") Uploading Failed! All DB Transactions rolled back.");
                        this.statusMsg.setMessage("Transaction Rolledback because of some database error", StatusMessage.MSG_CLASS_ERROR);
                    }
                    conn.setAutoCommit(true);
                } else {
                    //Some of the required Files missing.
                    this.statusMsg.setMessage("Some of the Content Validation Rules Failed! " + contentValidator.getLogMessage(), StatusMessage.MSG_CLASS_ERROR);
                    System.out.println("Content Validator responded with false result, Error Msg : " + contentValidator.getLogMessage());
                }
            } catch (Exception e) {
                dbConn.logUIMsg(RLogger.LOGGING_LEVEL_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ContentUploadBean.class :: commitContentDetails() :: Exception " + e);
                this.statusMsg.setMessage("Content Uploaded Failed, DB Error!", StatusMessage.MSG_CLASS_ERROR);
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
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ContentUploadBean.class :: commitContentDetails() :: Database Connectivity Failed");
            statusMsg.setMessage("A process failed while reloading Country List. Database Connectivity Issue.", StatusMessage.MSG_CLASS_ERROR);
        }

        // this.statusMsg.setMessage("Content Uploaded", StatusMessage.MSG_CLASS_INFO);
    }

    public void clearUploadedFiles() {
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: clearUploadedFiles() Invoked..... Files to remove " + this.getUploadedFileNames() + " from folder " + newContent.getContentFolder());

        RUtil.deleteAllFiles(newContent.getContentFolder());

        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: clearUploadedFiles() :: Files Removed, Clearing object cache.....");

        uploadedPreviewFiles.clear();
        uploadedContentFiles.clear();

        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: clearUploadedFiles() :: Cache Cleared. Uploded File Clean process completed.");
        this.statusMsg.setMessage(this.getUploadedFileNames(), StatusMessage.MSG_CLASS_INFO);
    }

    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        try {
            String itemId = event.getComponent().getId();
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: handleFileUpload() :: New File Received " + file.getFileName() + ", Component Id " + itemId);
            if (itemId.contains("preview")) {
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: handleFileUpload() :: Adding New File to Preview File Stack....");
                addContentFile(file, "preview");
            } else {
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: handleFileUpload() :: Adding New File to Content File Stack....");
                addContentFile(file, "content");
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: handleFileUpload() :: File (" + file.getFileName() + ") upload process complete.");
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: handleFileUpload() :: Exception " + e);
        } finally {
            //clear temporary files
        }

        this.statusMsg.setMessage(this.getUploadedFileNames(), StatusMessage.MSG_CLASS_INFO);

    }

    private synchronized boolean addContentFile(UploadedFile file, String fileCategory) throws IOException {

        String fileName = file.getFileName();
        String fileExtension = RUtil.parseField(fileName, RUtil.FIELD_TYPE_FILE_EXTENSION, "JPEG", fileCategory);
        String filePartName = fileName.substring(0, fileName.lastIndexOf("."));
        fileName = RUtil.parseField(filePartName, RUtil.FIELD_TYPE_FILE_NAME, "", fileCategory, this.isHaveParts());
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "ContentUploadBean.class :: addContentFile() :: File (CompleteFileName=" + file.getFileName() + ", Extn=" + fileExtension + ", FilePartName=" + filePartName + ", FileNameToProcess=" + fileName + ", File Category=" + fileCategory + ") received for processing. Check if file exists already.");

        boolean fileExists = false, addFile = false;
        CMSContentFile newFile = searchtContentFile(fileName, fileExtension, fileCategory.equalsIgnoreCase("preview") ? true : false);
        fileExists = (newFile != null) ? true : false;
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "ContentUploadBean.class :: addContentFile() :: IS File (" + fileName + ") Exists Flag = " + fileExists);

        if (!fileExists) {
            //Create new File as it does not exists
            newFile = new CMSContentFile();
            newFile.setFileName(fileName);
            newFile.setFileExtension(fileExtension);
            newFile.setFileNamePrefix(RUtil.parseField(filePartName, RUtil.FIELD_TYPE_FILE_NAME_PREFIX, "", fileCategory, this.isHaveParts()));
            newFile.setWidth(RUtil.parseField(filePartName, RUtil.FIELD_TYPE_WIDTH, this.isHaveParts()));
            newFile.setHeight(RUtil.parseField(filePartName, RUtil.FIELD_TYPE_HEIGHT, this.isHaveParts()));
            newFile.setFolderPath(newContent.getContentFolder());
            newFile.setContentUrl(newContent.getContentPath());
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: addContentFile() :: File (" + fileName + ") :: New Object Created!");
            addFile = true;
        } else { //Add into existing file if its Content File category and filepart is not duplicate
            addFile = fileCategory.equalsIgnoreCase("preview") ? false : (newFile.getFilePartNames().indexOf(filePartName) >= 0 ? false : true);
        }
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "ContentUploadBean.class :: addContentFile() :: File (" + fileName + ") :: Accept File Flag =" + addFile);

        if (addFile) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: addContentFile() :: File (" + fileName + ") :: File Accepted to Add, Creating File Part Object.... ");
            CMSContentFilePart newFilePart = new CMSContentFilePart();
            newFilePart.setFileExtension(fileExtension);
            newFilePart.setFileName(filePartName);
            newFilePart.setSequenceNumber((this.isHaveParts() && !fileCategory.equalsIgnoreCase("preview")) ? RUtil.parseField(filePartName, RUtil.FIELD_TYPE_PARTNUMBER, this.isHaveParts()) : 1);
            String filePath = this.getBaseContentPath() + ((this.isHaveParts() && !fileCategory.equalsIgnoreCase("preview")) ? newContent.getContentFolder() + filePartName + "." + fileExtension : newContent.getContentFolder() + filePartName + "_1." + fileExtension);
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: addContentFile() :: File (" + fileName + ") :: Writing file to  " + filePath);

            int rep = RUtil.writeToFile(file.getInputstream(), (this.getBaseContentPath() + newFile.getFolderPath()), filePath);
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: addContentFile() :: File (" + fileName + ") :: Bytes Written=" + rep);

            newFilePart.setFileSize(rep);
            newFile.addNewPart(newFilePart);
            if (rep > 0) {
                if (fileCategory.equalsIgnoreCase("preview")) {
                    this.uploadedPreviewFiles.add(newFile);
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "ContentUploadBean.class :: addContentFile() :: File (" + fileName + ") :: File Added to Preview Content Stack");
                } else {
                    this.uploadedContentFiles.add(newFile);
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "ContentUploadBean.class :: addContentFile() :: File (" + fileName + ") :: File Added to Content Stack");
                }
            } else {
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ContentUploadBean.class :: addContentFile() :: File (" + fileName + ") :: File Ignored due to 0 byte length");
            }

        } else {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "ContentUploadBean.class :: addContentFile() :: File (" + fileName + ") :: File Ignored!");
        }
        return true;
    }

    public CMSContentFile searchtContentFile(String fileName, String fileExtension, boolean checkPreviewCollection) {
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: searchtContentFile() :: File (" + fileName + ") ::  Searching File into Stack, CheckPreviewStackFlag=" + checkPreviewCollection);
        CMSContentFile retObj = null;
        if (checkPreviewCollection) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: searchtContentFile() :: File (" + fileName + ") ::  Existing Stack Size=" + uploadedPreviewFiles.size());
            for (CMSContentFile file : this.uploadedPreviewFiles) {
                if (file.getFileName().equalsIgnoreCase(fileName) && file.getFileExtension().equalsIgnoreCase(fileExtension)) {
                    retObj = file;
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: searchtContentFile() :: File (" + fileName + ") ::  File Matched in Preview Stack!");
                    break;
                }
            }

        } else {
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: searchtContentFile() :: File (" + fileName + ") ::  Existing Stack Size=" + uploadedContentFiles.size());
            for (CMSContentFile file : this.uploadedContentFiles) {
                if (file.getFileName().equalsIgnoreCase(fileName) && file.getFileExtension().equalsIgnoreCase(fileExtension)) {
                    retObj = file;
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "ContentUploadBean.class :: searchtContentFile() :: File (" + fileName + ") ::  File Matched in Content Stack!");
                    uploadedContentFiles.remove(file);
                    break;
                }
            }
        }
        return retObj;
    }

    public String uploadSingleContent() {
        //Validate Content Detail
        String actionMsg = "";
        newContent.setCategoryId(RUtil.strToInt(newContent.getCategoryName(), 0));
        newContent.setSuperCategoryId(RUtil.strToInt(newContent.getSuperCategoryName(), 0));
        newContent.setCpcatalogNumber(newContent.getCpcatalogNumber() == null ? "" : newContent.getCpcatalogNumber().trim());
        newContent.setHostingType(newContent.getHostingType() == null ? "" : newContent.getHostingType().trim());
        newContent.setContentName(newContent.getContentName() == null ? "" : newContent.getContentName().trim());
        newContent.setShortPreviewText(newContent.getShortPreviewText() == null ? "" : newContent.getShortPreviewText().trim());
        newContent.setLongPreviewText(newContent.getLongPreviewText() == null ? "" : newContent.getLongPreviewText().trim());
        newContent.setCopyrightId(RUtil.strToInt(newContent.getCopyrightName(), 0));
        newContent.setShowOrder(newContent.getShowOrder() <= 0 ? 1 : newContent.getShowOrder());
        newContent.setSearchKeywords(newContent.getSearchKeywords() == null ? newContent.getContentName() : newContent.getSearchKeywords().trim());
        newContent.setContentNamePrefix(newContent.getContentName().indexOf(" ") > 0 ? newContent.getContentName().substring(0, newContent.getContentName().indexOf(" ")) : newContent.getContentName());

        if (newContent.getContentTypeId() <= 0) {
            //Invalid Content Type Id
            actionMsg = "Invalid Content Type! Pl select a valid content type to upload content.";
        } else if (newContent.getCategoryId() <= 0) {
            //Invalid Category Id
            actionMsg = "Invalid Category Id! Pl select a valid category to upload content.";
        } else if (newContent.getHostingType().length() <= 0) {
            //Invalid Hosting Type
            actionMsg = "Invalid Hostig Type! Hosting category need to be defined correctly.";
        } else if (newContent.getCpcatalogNumber().length() <= 0) {
            //Invalid CP Catalog Number
            actionMsg = "Invalid CP Catalog Number! CP Catalog Number for content can not be blank.";
        } else if (newContent.getContentName().length() <= 0) {
            //Invalid Content Name
            actionMsg = "Invalid Content Name! Content Name can not be blank.";
        } else if (newContent.getShortPreviewText().length() <= 0) {
            //Invalid Short description text
            actionMsg = "Invalid Short Description! Short Description for content can not be blank.";
        } else if (newContent.getLongPreviewText().length() <= 0) {
            //Invalid Long description Text
            actionMsg = "Invalid Long Description! Long Description for content can not be blank.";
        } else if (newContent.getCopyrightId() <= 0) {
            //Invalid Copyright Owner
            actionMsg = "Invalid Copyright Owner! Please select copyright owner and try again.";
        } else if (newContent.getShowOrder() <= 0) {
            //Invalid show order
            actionMsg = "Invalid show order! Show order is required to be non zero integer value.";
        } else if (newContent.getSearchKeywords().length() <= 0) {
            //Invalid Search Keywords
            actionMsg = "Invalid Search Keywords!";
        } else {
            int catNrCounter = getCatalogNumber(newContent.getCategoryId());
            String catNr = RUtil.formatNumber(newContent.getContentTypeId(), 2, false) + RUtil.formatNumber(newContent.getCategoryId(), 3, false) + RUtil.formatNumber(catNrCounter, 5, true);
            newContent.setCatalogNumber(RUtil.strToLong(catNr, 0));
            if (newContent.getCatalogNumber() <= 0) {
                //Invalid Catalog Number
                actionMsg = "Failed to generate catalog number. Pl try again.";
            } else {
                String contentPathRelative = RUtil.getContentPath("", "" + newContent.getContentTypeId(), "" + newContent.getCategoryId(), "" + newContent.getCatalogNumber(), sdf.format(new java.util.Date()));
                newContent.setContentFolder(contentPathRelative);
                newContent.setContentPath(contentPathRelative);
                System.out.println("Relative Path " + contentPathRelative);
                actionMsg = "addcontent";
            }

        }
        System.out.println("Action Msg :" + actionMsg);

        if (!actionMsg.equalsIgnoreCase("addcontent")) {
            statusMsg.setMessage(actionMsg, StatusMessage.MSG_CLASS_ERROR);
            actionMsg = "";
        }

        return actionMsg;
    }

    public synchronized int getCatalogNumber(int contentType) {
        int catalogNo = 0;
        Connection conn = dbConn.connect();
        if (conn != null) {
            try {
                java.sql.Statement st = conn.createStatement(java.sql.ResultSet.TYPE_SCROLL_SENSITIVE, java.sql.ResultSet.CONCUR_UPDATABLE);
                String sqlStr = "SELECT catg_id,folder_name,catnr_ctr FROM tb_content_categories WHERE catg_id=" + contentType;
                System.out.println("sql " + sqlStr);
                java.sql.ResultSet rs = st.executeQuery(sqlStr);
                if (rs.next()) {
                    catalogNo = rs.getInt("catnr_ctr");
                }
                if (catalogNo > 0) {
                    rs.updateInt("catnr_ctr", (catalogNo + 1));
                    rs.updateRow();
                }
                if (rs != null) {
                    rs.close();
                }
                rs = null;
                if (st != null) {
                    st.close();
                }
                st = null;
            } catch (Exception e) {
                catalogNo = 0;
                System.out.println("Exception " + e);
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
        return catalogNo;
    }

    public void refreshList(boolean refreshContentType, boolean refreshSuperCatg, boolean refreshCatg, boolean refreshCopyright, boolean refreshValidationRule) {
        Connection conn = dbConn.connect();
        StringBuilder logMsg = new StringBuilder();

        if (conn != null) {
            logMsg.append("Database Connected;");
            try {
                java.sql.Statement st = conn.createStatement();

                if (refreshContentType) {
                    refreshContentType(st);
                }
                if (refreshSuperCatg) {
                    refreshSuperCategory(st);
                }
                if (refreshCatg) {
                    refreshContentCategory(st);
                }
                if (refreshCopyright) {
                    refreshCopyrightOwner(st);
                }
                if (refreshValidationRule) {
                    searchValidationRule(st, newContent.getContentTypeId());
                }
            } catch (Exception e) {
                logMsg.append("A process failed while reloading Msisdn Series List. Exception ").append(e.getMessage());
                statusMsg.setMessage("A process failed while reloading Msisdn Series List. Database Error!", StatusMessage.MSG_CLASS_ERROR);
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
            logMsg.append("A process failed while reloading Msisdn Series List. Database Connectivity Issue.");
            statusMsg.setMessage("A process failed while reloading Msisdn Series List. Database Connectivity Issue.", StatusMessage.MSG_CLASS_ERROR);
        }
        dbConn.logMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "ContentUploadBean.class :: refreshMsisdnSeriesList() ::" + logMsg.toString());

    }

    public void refreshCopyrightOwner(java.sql.Statement st) {
        try {
            copyrightOwner.clear();
            java.sql.ResultSet rs = st.executeQuery("SELECT copyright_id,NAME FROM tb_cms_copyright WHERE STATUS>0 ORDER BY NAME;");
            while (rs.next()) {
                copyrightOwner.put(rs.getString("NAME"), rs.getString("copyright_id"));
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            System.out.println("Exception " + e);
        }
    }

    public void reloadSubCategories() {
        refreshList(false, false, true, false, false);
        uploadedContentFiles.clear();
        uploadedPreviewFiles.clear();
    }

    public void searchValidationRule(java.sql.Statement st, int contentType) {
        try {

            String sql1 = "SELECT content_type_name,allowed_content_extns,allowed_preview_extns,required_content_sizes,"
                    + "required_preview_sizes,allow_multipart,max_parts,max_file_size FROM tb_cms_content_type WHERE content_type_id=" + contentType;
            java.sql.ResultSet rs = st.executeQuery(sql1);

            if (rs.next()) {
                this.validationRule = new ContentValidationRule();
                this.validationRule.setContentTypeName(rs.getString("content_type_name"));
                this.validationRule.setAllowedContentFileExtensions(RUtil.convertStrToList(rs.getString("allowed_content_extns"), ",", "NA", "NA"));
                this.validationRule.setValidateContentExtensions(this.validationRule.getAllowedContentFileExtensions().size() > 0 ? true : false);
                this.validationRule.setAllowedPreviewFileExtension(RUtil.convertStrToList(rs.getString("allowed_preview_extns"), ",", "NA", "NA"));
                this.validationRule.setValidatePreviewExtensions(this.validationRule.getAllowedPreviewFileExtension().size() > 0 ? true : false);
                this.validationRule.setRequiredContentFileSizes(RUtil.convertStrToList(rs.getString("required_content_sizes"), ",", "NA", "NA"));
                this.validationRule.setValidateContentSize(this.validationRule.getRequiredContentFileSizes().size() > 0 ? true : false);
                this.validationRule.setRequiredPreviewFileSizes(RUtil.convertStrToList(rs.getString("required_preview_sizes"), ",", "NA", "NA"));
                this.validationRule.setValidatePreviewSize(this.validationRule.getRequiredPreviewFileSizes().size() > 0 ? true : false);
                this.validationRule.setAllowMultipartContent(rs.getInt("allow_multipart") > 0 ? true : false);
                this.validationRule.setMaxPartsAllowed(rs.getInt("max_parts"));
                this.validationRule.setMaxFileSizeInMb(rs.getInt("max_file_size"));
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;

        } catch (Exception e) {
            System.out.println("ContentUploadBean.class :: searchValidationRule() :: Exception " + e);
        }
    }

    public void refreshContentCategory(java.sql.Statement st) {
        try {
            contentCategoryList.clear();
            java.sql.ResultSet rs = st.executeQuery("SELECT catg_id,catg_name FROM tb_content_categories WHERE STATUS>0 AND parent_catg>0 and parent_catg=" + newContent.getSuperCategoryName() + " AND subcategorised<=0 ORDER BY catg_name;");
            while (rs.next()) {
                contentCategoryList.put(rs.getString("catg_name"), rs.getString("catg_id"));
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            System.out.println("Exception " + e);
        }
    }

    public void refreshSuperCategory(java.sql.Statement st) {
        try {
            superCategoryList.clear();
            java.sql.ResultSet rs = st.executeQuery("SELECT catg_id,catg_name FROM tb_content_categories WHERE STATUS>0 AND parent_catg<=0 AND subcategorised>0 ORDER BY catg_name;");
            while (rs.next()) {
                superCategoryList.put(rs.getString("catg_name"), rs.getString("catg_id"));
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            System.out.println("Exception " + e);
        }
    }

    public void refreshContentType(java.sql.Statement st) {
        try {
            contentTypeList.clear();
            java.sql.ResultSet rs = st.executeQuery("SELECT CONCAT(CTG.content_group_id,',',CT.content_type_id) AS content_type_id,CONCAT(CTG.content_group_name,'::',CT.content_type_name) AS content_type_name FROM tb_cms_content_type CT,tb_cms_content_type_group CTG WHERE CT.`group_id`= CTG.`content_group_id` AND CT.status > 0 AND CTG.`status`>0 ORDER BY CTG.content_group_name,CT.content_type_name;");
            while (rs.next()) {
                contentTypeList.put(rs.getString("content_type_name"), rs.getString("content_type_id"));
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            System.out.println("Exception " + e);
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

    public String formatContentDetailQuery(boolean fileNameQuery) {
        String sql1 = "";
        switch (newContent.getContentTypeGroupId()) {
            case ContentValidator.CTG_IMAGE:
                if (fileNameQuery) {
                    sql1 = "insert into tb_cms_contents_files(cat_nr,file_name,parts_count,file_extension,preview_file) "
                            + " values(?,?,?,?,?);";
                } else {
                    sql1 = "insert into tb_cms_contents_images(cat_nr,short_preview_text,long_preview_text,album_or_movie,artist,"
                            + "transcode_content) values(?,?,?,?,?,?);";
                }

                break;
            case ContentValidator.CTG_AUDIO:
                if (fileNameQuery) {
                    sql1 = "insert into tb_cms_contents_files(cat_nr,file_name,parts_count,file_extension,preview_file) "
                            + " values(?,?,?,?,?);";
                } else {
                    sql1 = "insert into tb_cms_contents_video(cat_nr,short_preview_text,long_preview_text,album_or_movie,artist,lyricist,"
                            + "musicion,singer) values(?,?,?,?,?,?,?,?);";
                }
                break;
            case ContentValidator.CTG_VIDEO:
                if (fileNameQuery) {
                    sql1 = "insert into tb_cms_contents_files(cat_nr,file_name,parts_count,file_extension,preview_file) "
                            + " values(?,?,?,?,?);";
                } else {
                    sql1 = "insert into tb_cms_contents_video(cat_nr,short_preview_text,long_preview_text,album_or_movie,artist,lyricist,"
                            + "musicion,singer) values(?,?,?,?,?,?,?,?);";
                }
                break;
            case ContentValidator.CTG_APPS:
                // sql1="INSERT INTO tb_cms_contents_apps(cat_nr,short_preview_text,long_preview_text,preview_icon,content_files,download_url) "
                //       + "values(?,?,?,?,?,?);";
                if (fileNameQuery) {
                    sql1 = "insert into tb_cms_contents_files(cat_nr,file_name,parts_count,file_extension,preview_file)  values(?,?,?,?,?);";
                } else {
                    sql1 = "insert into tb_cms_contents_apps(cat_nr,short_preview_text,preview_icon,long_preview_text,content_files,download_url) values(?,?,?,?,?,?);";
                }
                break;

            case ContentValidator.CTG_TEXT:
                //tb_cms_contents_text
                break;
            default:
                break;
        }
        return sql1;
    }

    public int insertContentDetailTable(java.sql.PreparedStatement pst, boolean fileNameQuery) throws IOException, SQLException {

        int rep = 0;
        switch (newContent.getContentTypeGroupId()) {
            case ContentValidator.CTG_IMAGE:
                switch (fileNameQuery ? 1 : 0) {
                    case 1:
                        for (CMSContentFile file : this.uploadedContentFiles) {
                            pst.setLong(1, newContent.getCatalogNumber());
                            pst.setString(2, file.getFileName());
                            pst.setInt(3, file.getNumberOfParts());
                            pst.setString(4, file.getFileExtension());
                            pst.setShort(5, (short) 0);
                            rep = pst.executeUpdate();
                        }

                        for (CMSContentFile file : this.uploadedPreviewFiles) {
                            pst.setLong(1, newContent.getCatalogNumber());
                            pst.setString(2, file.getFileName());
                            pst.setInt(3, file.getNumberOfParts());
                            pst.setString(4, file.getFileExtension());
                            pst.setShort(5, (short) 1);
                            rep = pst.executeUpdate();
                        }
                        break;

                    default:
                        pst.setLong(1, newContent.getCatalogNumber());
                        pst.setString(2, newContent.getShortPreviewText());
                        pst.setString(3, newContent.getLongPreviewText());
                        pst.setString(4, newContent.getAlbumOrMovie());
                        pst.setString(5, newContent.getArtist());
                        pst.setInt(6, newContent.getTranscodeContent());
                        rep = pst.executeUpdate();
                }
                break;
            case ContentValidator.CTG_AUDIO:
                switch (fileNameQuery ? 1 : 0) {
                    case 1:
                        for (CMSContentFile file : this.uploadedContentFiles) {
                            pst.setLong(1, newContent.getCatalogNumber());
                            pst.setString(2, file.getFileName());
                            pst.setInt(3, file.getNumberOfParts());
                            pst.setString(4, file.getFileExtension());
                            pst.setShort(5, (short) 0);
                            rep = pst.executeUpdate();
                        }

                        for (CMSContentFile file : this.uploadedPreviewFiles) {
                            pst.setLong(1, newContent.getCatalogNumber());
                            pst.setString(2, file.getFileName());
                            pst.setInt(3, file.getNumberOfParts());
                            pst.setString(4, file.getFileExtension());
                            pst.setShort(5, (short) 1);
                            rep = pst.executeUpdate();
                        }

                        break;
                    default:
                        pst.setLong(1, newContent.getCatalogNumber());
                        pst.setString(2, newContent.getShortPreviewText());
                        pst.setString(3, newContent.getLongPreviewText());
                        pst.setString(4, newContent.getAlbumOrMovie());
                        pst.setString(5, newContent.getArtist());
                        pst.setString(6, newContent.getLyricist());
                        pst.setString(7, newContent.getMusicion());
                        pst.setString(8, newContent.getSinger());
                        rep = pst.executeUpdate();
                }
                break;
            case ContentValidator.CTG_VIDEO:
                switch (fileNameQuery ? 1 : 0) {
                    case 1:
                        for (CMSContentFile file : this.uploadedContentFiles) {
                            pst.setLong(1, newContent.getCatalogNumber());
                            pst.setString(2, file.getFileName());
                            pst.setInt(3, file.getNumberOfParts());
                            pst.setString(4, file.getFileExtension());
                            pst.setShort(5, (short) 0);
                            rep = pst.executeUpdate();
                        }

                        for (CMSContentFile file : this.uploadedPreviewFiles) {
                            pst.setLong(1, newContent.getCatalogNumber());
                            pst.setString(2, file.getFileName());
                            pst.setInt(3, file.getNumberOfParts());
                            pst.setString(4, file.getFileExtension());
                            pst.setShort(5, (short) 1);
                            rep = pst.executeUpdate();
                        }

                        break;
                    default:
                        pst.setLong(1, newContent.getCatalogNumber());
                        pst.setString(2, newContent.getShortPreviewText());
                        pst.setString(3, newContent.getLongPreviewText());
                        pst.setString(4, newContent.getAlbumOrMovie());
                        pst.setString(5, newContent.getArtist());
                        pst.setString(6, newContent.getLyricist());
                        pst.setString(7, newContent.getMusicion());
                        pst.setString(8, newContent.getSinger());
                        rep = pst.executeUpdate();
                }
                break;
            case ContentValidator.CTG_APPS:
                switch (fileNameQuery ? 1 : 0) {
                    case 1:
                        for (CMSContentFile file : this.uploadedContentFiles) {
                            pst.setLong(1, this.newContent.getCatalogNumber());
                            pst.setString(2, file.getFileName());
                            pst.setInt(3, file.getNumberOfParts());
                            pst.setString(4, file.getFileExtension());
                            pst.setShort(5, (short) 0);
                            rep = pst.executeUpdate();
                        }
                        for (CMSContentFile file : this.uploadedPreviewFiles) {
                            pst.setLong(1, this.newContent.getCatalogNumber());
                            pst.setString(2, file.getFileName());
                            pst.setInt(3, file.getNumberOfParts());
                            pst.setString(4, file.getFileExtension());
                            pst.setShort(5, (short) 1);
                            rep = pst.executeUpdate();
                        }
                        break;
                    default:
                        pst.setLong(1, this.newContent.getCatalogNumber());
                        pst.setString(2, this.newContent.getShortPreviewText());
                        pst.setString(3, "NA");
                        pst.setString(4, this.newContent.getLongPreviewText());
                        pst.setString(5, "NA");
                        pst.setString(6, "1");
                        rep = pst.executeUpdate();
                }
                

                break;

            case ContentValidator.CTG_TEXT:
                break;
            default:
                break;
        }
        return rep;
    }

    public boolean isDisabled(String typeName) {

        boolean retVal = true;
        if (typeName.equalsIgnoreCase("ALBUM") && (newContent.getContentTypeGroupId() == ContentValidator.CTG_IMAGE || newContent.getContentTypeGroupId() == ContentValidator.CTG_AUDIO || newContent.getContentTypeGroupId() == ContentValidator.CTG_VIDEO)) {
            retVal = false;
        } else if (typeName.equalsIgnoreCase("ARTIST") && (newContent.getContentTypeGroupId() == ContentValidator.CTG_IMAGE || newContent.getContentTypeGroupId() == ContentValidator.CTG_AUDIO || newContent.getContentTypeGroupId() == ContentValidator.CTG_VIDEO)) {
            retVal = false;
        } else if (typeName.equalsIgnoreCase("MUSICIAN") && (newContent.getContentTypeGroupId() == ContentValidator.CTG_AUDIO || newContent.getContentTypeGroupId() == ContentValidator.CTG_VIDEO)) {
            retVal = false;
        } else if (typeName.equalsIgnoreCase("LYRICIST") && (newContent.getContentTypeGroupId() == ContentValidator.CTG_AUDIO || newContent.getContentTypeGroupId() == ContentValidator.CTG_VIDEO)) {
            retVal = false;
        } else if (typeName.equalsIgnoreCase("SINGER") && (newContent.getContentTypeGroupId() == ContentValidator.CTG_AUDIO || newContent.getContentTypeGroupId() == ContentValidator.CTG_VIDEO)) {
            retVal = false;
        } else if (typeName.equalsIgnoreCase("TRANSCODE") && newContent.getContentTypeGroupId() == ContentValidator.CTG_IMAGE) {
            retVal = false;
        } else if (typeName.equalsIgnoreCase("MULTIPART") && (newContent.getContentTypeGroupId() == ContentValidator.CTG_AUDIO || newContent.getContentTypeGroupId() == ContentValidator.CTG_VIDEO)) {
            retVal = false;
        }

        System.out.println("Type Name =" + typeName + ", contentTypeGroup=" + newContent.getContentTypeGroupId() + ", retVal=" + retVal);
        return retVal;
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
        if (selectedContentType.indexOf(",") > 0) {
            String[] sp = selectedContentType.split(",");
            newContent.setContentTypeGroupId(RUtil.strToInt(sp[0], 0));
            newContent.setContentTypeId(RUtil.strToInt(sp[1], 0));
            for (String contentTypeVal : contentTypeList.keySet()) {
                String contentTypeKey = contentTypeList.get(contentTypeVal);
                if (contentTypeKey.equalsIgnoreCase(selectedContentType)) {
                    String[] sp1 = contentTypeVal.split("::");
                    System.out.println("Selected Content  Type Group  :" + sp1[0] + ", Selected Content  Type :" + sp1[1]);
                }
            }
        }
        this.selectedContentType = selectedContentType;
    }

    public Map<String, String> getSuperCategoryList() {
        return superCategoryList;
    }

    public void setSuperCategoryList(Map<String, String> superCategoryList) {
        this.superCategoryList = superCategoryList;
    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }

    public Map<String, String> getContentCategoryList() {
        return contentCategoryList;
    }

    public void setContentCategoryList(Map<String, String> contentCategoryList) {
        this.contentCategoryList = contentCategoryList;
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

    public Map<String, String> getCopyrightOwner() {
        return copyrightOwner;
    }

    public void setCopyrightOwner(Map<String, String> copyrightOwner) {
        this.copyrightOwner = copyrightOwner;
    }

    public CMSContent getNewContent() {
        return newContent;
    }

    public void setNewContent(CMSContent newContent) {
        this.newContent = newContent;
    }

    public String getUploadedFileNames() {

        StringBuilder sb = new StringBuilder("Content Files :: FileCount=").append(uploadedContentFiles.size()).append("{");
        for (CMSContentFile file : uploadedContentFiles) {
            sb.append(file.getFileName()).append(";NumberOfParts=").append(file.getFileParts().size()).append("|");
        }

        sb.append("} \n Preview Files :: Count=").append(uploadedPreviewFiles.size()).append("{");
        for (CMSContentFile file : uploadedPreviewFiles) {
            sb.append(file.getFileName()).append("|");

        }
        sb.append("}");
        this.setUploadedFileNames(sb.toString());
        return sb.toString();
    }

    public void setUploadedFileNames(String uploadedFileNames) {
        this.uploadedFileNames = uploadedFileNames;
    }

    public List<CMSContentFile> getUploadedContentFiles() {
        return uploadedContentFiles;
    }

    public void setUploadedContentFiles(List<CMSContentFile> uploadedContentFiles) {
        this.uploadedContentFiles = uploadedContentFiles;
    }

    public List<CMSContentFile> getUploadedPreviewFiles() {
        return uploadedPreviewFiles;
    }

    public void setUploadedPreviewFiles(List<CMSContentFile> uploadedPreviewFiles) {
        this.uploadedPreviewFiles = uploadedPreviewFiles;
    }

    public String getBaseContentPath() {
        return baseContentPath;
    }

    public void setBaseContentPath(String baseContentPath) {
        this.baseContentPath = baseContentPath;
    }

    public String getBaseContentURL() {
        return baseContentURL;
    }

    public void setBaseContentURL(String baseContentURL) {
        this.baseContentURL = baseContentURL;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public boolean isHaveParts() {
        if (!this.isDisableMultipartOption()) {
            return haveParts;
        } else {
            return false;
        }
    }

    public void setHaveParts(boolean haveParts) {
        System.out.println("haveParts=" + haveParts);
        this.haveParts = haveParts;
    }

    public String getDateString() {
        return dateString;
    }

    public void setDateString(String dateString) {
        this.dateString = dateString;
    }

    public boolean isDisableMultipartOption() {

        if (newContent.getContentTypeGroupId() == ContentValidator.CTG_IMAGE) {
            this.setDisableMultipartOption(true);
        } else if (newContent.getContentTypeGroupId() == ContentValidator.CTG_AUDIO) {
            this.setDisableMultipartOption(true);
        } else if (newContent.getContentTypeGroupId() == ContentValidator.CTG_VIDEO) {
            this.setDisableMultipartOption(false);
        } else if (newContent.getContentTypeGroupId() == ContentValidator.CTG_APPS) {
            this.setDisableMultipartOption(true);
        } else if (newContent.getContentTypeGroupId() == ContentValidator.CTG_TEXT) {
            this.setDisableMultipartOption(true);
        } else {
            this.setDisableMultipartOption(true);
        }

        return disableMultipartOption;
    }

    public void setDisableMultipartOption(boolean disableMultipartOption) {
        this.disableMultipartOption = disableMultipartOption;
    }

    public ContentValidationRule getValidationRule() {
        return validationRule;
    }

    public void setValidationRule(ContentValidationRule validationRule) {
        this.validationRule = validationRule;
    }

}
