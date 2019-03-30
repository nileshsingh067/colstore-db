/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rocg.web.mbeans;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.rocg.util.DBConnection;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import net.rocg.web.beans.CMSContentFile;
import net.rocg.web.beans.ContentValidator;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;
import net.rocg.util.ZipFileHandler;

/**
 *
 * @author Nilesh Singh
 */
@ManagedBean
@SessionScoped
public class html5gameupload implements java.io.Serializable {

    /**
     * Creates a new instance of html5gameupload
     */
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat sdfdb = new SimpleDateFormat("yyyy-MM-dd");
    ContentValidator contentValidator = new ContentValidator();
    CMSContentFile bulkUploadMetaDataFile, bulkUploadContentFile;
    StatusMessage statusMsg;
    DBConnection dbConn;
    String bulkFilesBaseFolder, loginName, ZipExtractorBaseDolser, html5contentbase;
    int loginId, roleId;
    boolean disableCommit;
    ZipFileHandler zipHandler = null;
    String Zippath;

    public html5gameupload() {
        fetchLoginDetails();
        this.setBulkFilesBaseFolder(RUtil.getStringProperty("cms_content_html5_bulkfolder", "/opt/jiffy-content/html5zip"));
       // this.setBulkFilesBaseFolder(RUtil.getStringProperty("cms_content_html5_bulkfolder", "F:\\bulk"));
        //this.setZipExtractorBaseDolser(RUtil.getStringProperty("cms_content_html5_extrected_folder", "F:\\ext"));
        this.setZipExtractorBaseDolser(RUtil.getStringProperty("cms_content_html5_extrected_folder", "/opt/jiffy-content/html5unzip"));
        this.setHtml5contentbase(RUtil.getStringProperty("cms_content_html5base_folder", "/opt/jiffy-content/content/html5games"));
       // this.setBulkFilesBaseFolder(RUtil.getStringProperty("cms_content_bulkfolder", ""));
        statusMsg = new StatusMessage();
        dbConn = new DBConnection();
        zipHandler = new ZipFileHandler();

    }

    public void handleBulkFileUpload(FileUploadEvent event) {

        UploadedFile file = event.getFile();
        StringBuilder logStr = new StringBuilder();

        String folderName = "/" + this.getLoginId() + "/" + sdf.format(new java.util.Date()) + "/" + RUtil.BULK_UPLOAD_COUNTER + "/";
        String folderCompletePath = this.getBulkFilesBaseFolder() + folderName;

        try {
            String itemId = event.getComponent().getId();
            String fileName = file.getFileName();

            logStr.append("addContentFile()::UploadedFile=").append(file.getFileName()).append("|fileKey=").append(fileName);
            //this.setDisableCommit(false);
            if (itemId.contains("vid")) {

                String fileExtension = RUtil.parseField(fileName, RUtil.FIELD_TYPE_FILE_EXTENSION, "ZIP", "BULK-CONTENT");
                logStr.append("|FileType=BULK-CONTENT|fileExtn=").append(fileExtension);

                this.bulkUploadContentFile = new CMSContentFile();
                this.bulkUploadContentFile.setFileExtension(fileExtension);
                this.bulkUploadContentFile.setFileName(fileName);
                this.bulkUploadContentFile.setFolderPath(folderName);
                this.bulkUploadContentFile.setNumberOfParts(0);
                System.out.println(">>13" + folderCompletePath + " jjj " + fileName);
                int rep = RUtil.writeToFile(file.getInputstream(), (folderCompletePath), (folderCompletePath + fileName));
                System.out.println(">>14 : " + rep);
                if (rep > 0) {
                    this.bulkUploadContentFile.setBulkFileSize(rep);
                    this.setZippath((folderCompletePath + fileName));
                     System.out.println(" unzip source  :: " + this.getZippath());
                        System.out.println(" unzip dest  :: " + this.getHtml5contentbase());
                    int res = processbulkupload(this.getZippath(), this.getHtml5contentbase());
                    if (res >= 1) {
                        //int filecount = RUtil.moveFolder(this.ZipExtractorBaseDolser +"/"+(fileName).substring(0, fileName.length() - 4), this.getHtml5contentbase());
                        System.out.println(" source  :: " + this.ZipExtractorBaseDolser +"/"+(fileName).substring(0, fileName.length() - 4));
                        System.out.println(" dest  :: " + this.html5contentbase);
//                        if (filecount >= 1) {
//                            System.out.println(filecount + " move to " + this.getHtml5contentbase() + " folder");
//                            statusMsg.setMessage("Content Uploaded Successfully..", StatusMessage.MSG_CLASS_INFO);
//                        } else {
//                            System.out.println("unacle to  move content form  " + this.ZipExtractorBaseDolser +"/"+(fileName).substring(0, fileName.length() - 4) + " folder");
//                            statusMsg.setMessage("Unable to upload content.", StatusMessage.MSG_CLASS_ERROR);
//                        }
                        System.out.println(res + " move to " + this.getHtml5contentbase() + " folder");
                        statusMsg.setMessage("Content Successfully uploaded", StatusMessage.MSG_CLASS_ERROR);
                    }

                }

            } else {

            }
        } catch (Exception e) {
            System.out.println("Exception while uploading file " + file.getFileName() + ", " + e.getMessage());
            statusMsg.setMessage("Something Wrong.", StatusMessage.MSG_CLASS_ERROR);
        }
        //System.out.println(logStr.toString());
        //this.statusMsg.setMessage("Meta-Data File: "+bulkUploadMetaDataFile.getFileName()+", File Size: "+(bulkUploadMetaDataFile.getBulkFileSize()/1024)+"KB |Content File : "+bulkUploadContentFile.getFileName()+", File Size: "+(bulkUploadContentFile.getBulkFileSize()/(1024*1024))+"MB", StatusMessage.MSG_CLASS_INFO);

    }

    public int processbulkupload(String Zipfile, String ZipExtractorBaseDolser) {
        int counter = 0;
        try {
            // Open the zip file
            ZipFile zipFile = new ZipFile(Zipfile);
            Enumeration<?> enu = zipFile.entries();
            while (enu.hasMoreElements()) {
               
                ZipEntry zipEntry = (ZipEntry) enu.nextElement();

                String name = zipEntry.getName();
                long size = zipEntry.getSize();
                long compressedSize = zipEntry.getCompressedSize();
                System.out.printf("name: %-20s | size: %6d | compressed size: %6d\n",
                        name, size, compressedSize);

                // Do we need to create a directory ?
                File file = new File(ZipExtractorBaseDolser+"/"+ name);
                if (name.endsWith("/")) {
                    System.out.println(" creating file :: " + name);
//                    boolean createfileflag=false;
//                    String arr[]=file.list();
//                    for(String filename:arr){
//                        if(filename.contains(".txt")){
//                            System.out.println("txt file found in folder...");
//                        createfileflag=true;
//                        break;
//                    }
//                    }
//                    if(createfileflag)file.mkdirs();
//                    else{
//                     System.out.println("txt file not found in folder... unable to create file "+file.getName());   
//                    }
                    file.mkdirs();
                    continue;
                }

                File parent = file.getParentFile();
                if (parent != null) {
                    System.out.println(" creating parent file :: " + name);
                    if(name.contains(".txt")){
                         counter++;
                        System.out.println("valid content true");}
                    parent.mkdirs();
                  
                   // parent.mkdirs();
                }

                // Extract the file
                InputStream is = zipFile.getInputStream(zipEntry);
                FileOutputStream fos = new FileOutputStream(file);
                byte[] bytes = new byte[1024];
                int length;
                while ((length = is.read(bytes)) >= 0) {
                    fos.write(bytes, 0, length);
                }
                is.close();
                fos.close();

            }
            zipFile.close();
        } catch (IOException e) {
            statusMsg.setMessage("Some of the validation rule wrong.", StatusMessage.MSG_CLASS_ERROR);
            counter = -1;
            e.printStackTrace();
        }
        System.out.println(">>zip processer call result :: " + counter);
        return counter;
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

    public boolean isDisableCommit() {
        return disableCommit;
    }

    public void setDisableCommit(boolean disableCommit) {
        this.disableCommit = disableCommit;
    }

    public String getZippath() {
        return Zippath;
    }

    public void setZippath(String Zippath) {
        this.Zippath = Zippath;
    }

    public String getZipExtractorBaseDolser() {
        return ZipExtractorBaseDolser;
    }

    public void setZipExtractorBaseDolser(String ZipExtractorBaseDolser) {
        this.ZipExtractorBaseDolser = ZipExtractorBaseDolser;
    }

    public String getHtml5contentbase() {
        return html5contentbase;
    }

    public void setHtml5contentbase(String html5contentbase) {
        this.html5contentbase = html5contentbase;
    }

}
