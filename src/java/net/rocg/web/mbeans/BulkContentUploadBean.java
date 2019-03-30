/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rocg.web.mbeans;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.rocg.processors.CMSBulkUploadProcessor;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import net.rocg.web.beans.CMSBulkContent;
import net.rocg.web.beans.CMSContentFile;
import net.rocg.web.beans.ContentValidator;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Rishi Tyagi
 */
@ManagedBean
@SessionScoped
public class BulkContentUploadBean  implements java.io.Serializable{

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat sdfdb = new SimpleDateFormat("yyyy-MM-dd");
    ContentValidator contentValidator = new ContentValidator();
     CMSContentFile bulkUploadMetaDataFile,bulkUploadContentFile;
     StatusMessage statusMsg;
    DBConnection dbConn;
    String bulkFilesBaseFolder,loginName;
    int loginId, roleId;
    boolean disableCommit;
    
    /**
     * Variables for Process Bulk Content
     */
    Map<String,String> contentProviderList;
    List<CMSBulkContent> bulkContentList;
    CMSBulkContent selectedBulkContent;
    int selectedBulkContentType;
    String selectedContentProvider;
    String uploadDateStart;
    String uploadDateEnd;
    Integer progress;
    /**
     * Creates a new instance of BulkContentUploadBean
     */
    public BulkContentUploadBean() {
        bulkContentList=new ArrayList<>();
        contentProviderList=new HashMap<>();
        this.setBulkFilesBaseFolder(RUtil.getStringProperty("cms_content_bulkfolder", ""));
        fetchLoginDetails();
        bulkUploadMetaDataFile=new CMSContentFile();
        bulkUploadContentFile=new CMSContentFile();
        statusMsg=new StatusMessage();
        dbConn=new DBConnection();
        reloadBulkContentRecords(true,true);
    }
    
    public void onComplete() {
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Progress Completed"));
    }

/**
 * Methods starts here to handle process bulk content
 */
    
    public void processBulkRecord(){
        java.sql.Connection conn=dbConn.connect();
        if(conn!=null){
            try{
               
                if(this.selectedBulkContent!=null && this.selectedBulkContent.getBulkContentId()>0){
                   CMSBulkUploadProcessor newProcess=new CMSBulkUploadProcessor();
                   newProcess.setBulkContent(selectedBulkContent);
                   dbConn.logUIMsg(RLogger.LOGGING_LEVEL_INFO, RLogger.LOGGING_LEVEL_INFO, "BulkContentUploadBean.class :: processBulkRecord() :: Processing Bulk Content Record Id "+selectedBulkContent.getBulkContentId());
                   this.setProgress(Integer.valueOf(5));
                   newProcess.processRecord(conn, selectedBulkContent,this.getLoginId());
                   this.setProgress(Integer.valueOf(100));
                }else{
                    System.out.println("processBulkRecord () :: No valid record to process.");
                }
               
            }catch(Exception e){
                System.out.println(" processBulkRecord():: Exception "+e);
            }finally{
                try{if(conn!=null) conn.close();}catch(Exception ee){}
                conn=null;
            }
        }
        reloadBulkContentRecords(true,false);
    }
    
    public void reloadBulkContentRecords(boolean reloadContentRecords,boolean reloadCPList){
        java.sql.Connection conn=dbConn.connect();
        if(conn!=null){
            try{
                java.sql.Statement st=conn.createStatement();
                if(reloadContentRecords) reloadContentRecords(st);
                if(reloadCPList) reloadCPList(st);
                if(st!=null) st.close();
                st=null;
            }catch(Exception e){
                System.out.println(" reloadBulkContentRecords():: Exception "+e);
            }finally{
                try{if(conn!=null) conn.close();}catch(Exception ee){}
                conn=null;
            }
        }
        
    }

    public void reloadContentRecords(java.sql.Statement st){
        java.sql.ResultSet rs=null;
        try{
            
            String sql1="";
            if(this.getSelectedBulkContentType()==1)        
                    sql1="SELECT a.id,a.loginid,b.user_name,c.company_name,a.base_folder,a.content_folder,a.meta_data_file,"
                    + "a.meta_data_file_size,a.content_file,a.content_file_size,a.upload_date,a.process_date,a.status,a.status_descp FROM tb_cms_content_bulk a,tb_users b,tb_registered_companies c WHERE a.`loginid`=b.`user_id` AND b.`company_id`=c.`company_id` AND a.status<>0 ";
            else
                sql1="SELECT a.id,a.loginid,b.user_name,c.company_name,a.base_folder,a.content_folder,a.meta_data_file,"
                    + "a.meta_data_file_size,a.content_file,a.content_file_size,a.upload_date,a.process_date,a.status,a.status_descp FROM tb_cms_content_bulk a,tb_users b,tb_registered_companies c WHERE a.`loginid`=b.`user_id` AND b.`company_id`=c.`company_id` AND a.status=0 ";
            int selectedCPId=RUtil.strToInt(this.getSelectedContentProvider(), 0);
            if(selectedCPId>0)
                    sql1 +=" and (a.loginid="+selectedCPId+")";
            else if(this.getRoleId()==1 || this.getRoleId()==2)
                sql1 +="";
            else
                sql1 +=" and b.user_id="+this.getLoginId()+"";
            
            System.out.println("Reload Content Record Query "+sql1);
            
            rs=st.executeQuery(sql1);
            CMSBulkContent newContent=null;
            bulkContentList.clear();
            while(rs.next()){
                newContent=new CMSBulkContent();
                newContent.setBulkContentId(rs.getInt("id"));
                newContent.setCpId(rs.getInt("loginid"));
                newContent.setCpName(rs.getString("company_name")+"::"+rs.getString("user_name"));
                newContent.setBaseFolder(rs.getString("base_folder"));
                newContent.setContentFolder(rs.getString("content_folder"));
                newContent.setMetaDataFileName(rs.getString("meta_data_file")); 
                newContent.setMetaDataFileSize(rs.getInt("meta_data_file_size")); 
                newContent.setContentFileName(rs.getString("content_file"));
                newContent.setContentFileSize(rs.getInt("content_file_size"));
                newContent.setUploadDate(rs.getString("upload_date"));
                newContent.setProcessDate(rs.getString("process_date"));
                newContent.setStatus(rs.getInt("status"));
                newContent.setStatusDescription(rs.getString("status_descp"));
                this.bulkContentList.add(newContent);
                newContent=null;
            }
            if(rs!=null) rs.close();
            rs=null;
            
        }catch(Exception e){
            System.out.println(" reloadContentRecords() :: Exception "+e);
        }finally{
            try{if(rs!=null) rs.close();}catch(Exception ee){}
            rs=null;
        }
    }
    public void reloadCPList(java.sql.Statement st){
        java.sql.ResultSet rs=null;
        try{
            String sql1="";
            if(this.getRoleId()==1 || this.getRoleId()==2)
                sql1="SELECT a.user_id,a.user_name,b.company_name FROM tb_users a,tb_registered_companies b WHERE a.`company_id`=b.`company_id` AND a.`role_id`=3 ORDER BY 3,2";
            else
                sql1="SELECT a.user_id,a.user_name,b.company_name FROM tb_users a,tb_registered_companies b WHERE a.`company_id`=b.`company_id` AND a.`role_id`=3 and a.user_id="+this.getLoginId()+" ORDER BY 3,2";
            System.out.println("Reload CP List Query : "+sql1);
            rs=st.executeQuery(sql1);
            contentProviderList.clear();
            while(rs.next()){
                contentProviderList.put(rs.getString("company_name")+"-"+rs.getString("user_name"), rs.getString("user_id"));
            }
            if(rs!=null) rs.close();
            rs=null;
        }catch(Exception e){
            System.out.println(" reloadCPList() :: Exception "+e);
        }finally{
            try{if(rs!=null) rs.close();}catch(Exception ee){}
            rs=null;
        }
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
    
    
    public String getUploadDateStart() {
        return uploadDateStart;
    }

    public void setUploadDateStart(String uploadDateStart) {
        this.uploadDateStart = uploadDateStart;
    }

    public String getUploadDateEnd() {
        return uploadDateEnd;
    }

    public void setUploadDateEnd(String uploadDateEnd) {
        this.uploadDateEnd = uploadDateEnd;
    }

    public List<CMSBulkContent> getBulkContentList() {
        return bulkContentList;
    }

    public void setBulkContentList(List<CMSBulkContent> bulkContentList) {
        this.bulkContentList = bulkContentList;
    }

    public CMSBulkContent getSelectedBulkContent() {
        return selectedBulkContent;
    }

    public void setSelectedBulkContent(CMSBulkContent selectedBulkContent) {
        this.selectedBulkContent = selectedBulkContent;
    }

    public int getSelectedBulkContentType() {
        return selectedBulkContentType;
    }

    public void setSelectedBulkContentType(int selectedBulkContentType) {
        this.selectedBulkContentType = selectedBulkContentType;
    }

    public Integer getProgress() {
        return progress;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }
    
    
    /**
     * Methods Starts here to handle Bulk Content Upload
     * @param event :
     */
    
      public void handleBulkFileUpload(FileUploadEvent event){
       System.out.println(">>1");
        UploadedFile file = event.getFile();
        StringBuilder logStr = new StringBuilder();
       System.out.println(">>2"); 
        String folderName="/"+this.getLoginId()+"/"+sdf.format(new java.util.Date())+"/"+RUtil.BULK_UPLOAD_COUNTER+"/";
       String folderCompletePath=this.getBulkFilesBaseFolder()+folderName;
       System.out.println(">>3"); 
       try {
            String itemId = event.getComponent().getId();
            String fileName = file.getFileName();
            System.out.println(">>4");
            logStr.append("addContentFile()::UploadedFile=").append(file.getFileName()).append("|fileKey=").append(fileName);
            this.setDisableCommit(false);
            if (itemId.contains("txt")) {
                System.out.println(">>5");
                String fileExtension = RUtil.parseField(fileName, RUtil.FIELD_TYPE_FILE_EXTENSION, "TXT", "META-DATA");
                logStr.append("|FileType=META-DATA|fileExtn=").append(fileExtension);
                System.out.println(">>6");
                this.bulkUploadMetaDataFile=new CMSContentFile();
                this.bulkUploadMetaDataFile.setFileExtension(fileExtension);
                this.bulkUploadMetaDataFile.setFileName(fileName);
                this.bulkUploadMetaDataFile.setFolderPath(folderName);
                this.bulkUploadMetaDataFile.setNumberOfParts(0);
                System.out.println(">>7");
                int rep=RUtil.writeToFile(file.getInputstream(), folderCompletePath, (folderCompletePath + fileName));
                System.out.println(">>8 : "+rep);
                if(rep>0) this.bulkUploadMetaDataFile.setBulkFileSize(rep);
                System.out.println(">>8");
            } else {
                System.out.println(">>11");
                String fileExtension = RUtil.parseField(fileName, RUtil.FIELD_TYPE_FILE_EXTENSION, "ZIP", "BULK-CONTENT");
                logStr.append("|FileType=BULK-CONTENT|fileExtn=").append(fileExtension);
                System.out.println(">>12");
                this.bulkUploadContentFile=new CMSContentFile();
                this.bulkUploadContentFile.setFileExtension(fileExtension);
                this.bulkUploadContentFile.setFileName(fileName);
                this.bulkUploadContentFile.setFolderPath(folderName);
                this.bulkUploadContentFile.setNumberOfParts(0);
                System.out.println(">>13");
                int rep=RUtil.writeToFile(file.getInputstream(), (folderCompletePath), (folderCompletePath + fileName));
                System.out.println(">>14 : "+rep);
                if(rep>0) this.bulkUploadContentFile.setBulkFileSize(rep);
                System.out.println(">>14");
            }
        } catch (Exception e) {
            System.out.println("Exception while uploading file " + file.getFileName() + ", " + e.getMessage());
        } 
        System.out.println(logStr.toString());
        this.statusMsg.setMessage("Meta-Data File: "+bulkUploadMetaDataFile.getFileName()+", File Size: "+(bulkUploadMetaDataFile.getBulkFileSize()/1024)+"KB |Content File : "+bulkUploadContentFile.getFileName()+", File Size: "+(bulkUploadContentFile.getBulkFileSize()/(1024*1024))+"MB", StatusMessage.MSG_CLASS_INFO);

    }
    
    public String bulkuploadAgain(){
        this.bulkUploadMetaDataFile=new CMSContentFile();
        this.bulkUploadContentFile=new CMSContentFile();
        this.statusMsg.setMessage("", StatusMessage.MSG_CLASS_INFO);
        return "bulkupload";
    }
    
    public void commitBulkContent(){
        StringBuilder logStr = new StringBuilder();
        System.out.println("Commit Event Invoked.");
        if(this.bulkUploadMetaDataFile==null || this.bulkUploadMetaDataFile.getFileName()==null || this.bulkUploadMetaDataFile.getFileName().length()<=0 || this.bulkUploadMetaDataFile.getBulkFileSize()<=0){
           this.statusMsg.setMessage("Invalid Meta Data File, Pl go back and upload a valid Meta-Data File again.", StatusMessage.MSG_CLASS_ERROR);
        }else if(this.bulkUploadContentFile==null || this.bulkUploadContentFile.getFileName()==null || this.bulkUploadContentFile.getFileName().length()<=0 || this.bulkUploadContentFile.getBulkFileSize()<=0){
            this.statusMsg.setMessage("Invalid Content File, Pl go back and upload a valid Content File again.", StatusMessage.MSG_CLASS_ERROR);
        }else if(this.getLoginId()<=0){
            this.statusMsg.setMessage("Invalid Login State! Please Login again to upload content.", StatusMessage.MSG_CLASS_ERROR);
        }else{
           
            String sql="INSERT INTO tb_cms_content_bulk(loginid,base_folder,content_folder,meta_data_file,meta_data_file_size,content_file,content_file_size,"
                    + "upload_date,last_update_date,STATUS,status_descp) "
                    + "VALUES('"+this.getLoginId()+"','"+this.getBulkFilesBaseFolder()+"','"+this.bulkUploadMetaDataFile.getFolderPath()+"',"
                    + "'"+this.bulkUploadMetaDataFile.getFileName()+"','"+this.bulkUploadMetaDataFile.getBulkFileSize()+"',"
                    + "'"+this.bulkUploadContentFile.getFileName()+"','"+this.bulkUploadContentFile.getBulkFileSize()+"',now(),now(),0,'New Content Uploaded');";
            System.out.println(sql);
           int rep=dbConn.executeUpdate(sql);
           if(rep>0){ 
                this.statusMsg.setMessage("Content Uploaded.", StatusMessage.MSG_CLASS_INFO);
                this.setDisableCommit(true);
           }else{
               this.statusMsg.setMessage("Failed to upload Content! Please try again later.", StatusMessage.MSG_CLASS_ERROR);
               this.setDisableCommit(false);
           }
        }
        RUtil.BULK_UPLOAD_COUNTER=(RUtil.BULK_UPLOAD_COUNTER>=999)?1:(RUtil.BULK_UPLOAD_COUNTER+1);
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

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
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

    public ContentValidator getContentValidator() {
        return contentValidator;
    }

    public void setContentValidator(ContentValidator contentValidator) {
        this.contentValidator = contentValidator;
    }

    public CMSContentFile getBulkUploadMetaDataFile() {
        return bulkUploadMetaDataFile;
    }

    public void setBulkUploadMetaDataFile(CMSContentFile bulkUploadMetaDataFile) {
        this.bulkUploadMetaDataFile = bulkUploadMetaDataFile;
    }

    public CMSContentFile getBulkUploadContentFile() {
        return bulkUploadContentFile;
    }

    public void setBulkUploadContentFile(CMSContentFile bulkUploadContentFile) {
        this.bulkUploadContentFile = bulkUploadContentFile;
    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }

    public String getBulkFilesBaseFolder() {
        return bulkFilesBaseFolder;
    }

    public void setBulkFilesBaseFolder(String bulkFilesBaseFolder) {
        this.bulkFilesBaseFolder = bulkFilesBaseFolder;
    }

    public boolean isDisableCommit() {
        return disableCommit;
    }

    public void setDisableCommit(boolean disableCommit) {
        this.disableCommit = disableCommit;
    }
    
}
