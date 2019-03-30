/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rocg.web.mbeans;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.StatusMessage;
import net.rocg.util.StringInputValidator;
import net.rocg.web.beans.CMSPlatform;
import net.rocg.web.beans.Country;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author Rishi Tyagi
 */
@ManagedBean
@RequestScoped
public class PlatformBean implements java.io.Serializable{
   DBConnection dbConn;
    CMSPlatform newPlatform;
    List<CMSPlatform> platformList;
    CMSPlatform selectedPlatform;
    StatusMessage statusMsg;
    /**
     * Creates a new instance of PlatformBean
     */
    public PlatformBean() {
        dbConn=new DBConnection();
        statusMsg=new StatusMessage();
        newPlatform=new CMSPlatform();
        platformList=new ArrayList<>();
        refreshCountryList(null,0);
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PlatformBean.class :: MBean Connected.");
    }
    public void reloadList(){
    }
    
    public void refreshCountryList(CMSPlatform updateObj,int action){
        Connection conn=dbConn.connect();
        StringBuilder logMsg=new StringBuilder();
        
        if(conn!=null){
                logMsg.append("Database Connected;");
                try{
                    java.sql.Statement st=conn.createStatement();
                    String sqlA="";int dbRep=0;
                    if(action==2 && updateObj!=null && updateObj.getPlatformId()>0){
                        //Update Existing Country Name
                        sqlA="update tb_country set platform_name='"+updateObj.getPlatformName()+"',  status="+updateObj.getStatus()+" where platform_id="+updateObj.getPlatformId()+";";
                        dbRep=st.executeUpdate(sqlA);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PlatformBean.class :: refreshCountryList() :: Action Requested=UPDATE-PLATFORM; SQL : " + sqlA + "| DB Result=" + dbRep);
                    }else if(action==1 && updateObj!=null && updateObj.getPlatformName()!=null && updateObj.getPlatformName().length()>0 && updateObj.getPlatformDescription()!=null && updateObj.getPlatformDescription().length()>0){
                        //Create new Country
                        sqlA="insert into tb_cms_platforms(platform_name,platform_description,status) values('"+updateObj.getPlatformName()+"','"+updateObj.getPlatformDescription()+"',1);";
                        dbRep=st.executeUpdate(sqlA);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PlatformBean.class :: refreshCountryList() :: Action Requested=REGISTER-NEW-PLATFORM; SQL : " + sqlA + "| DB Result=" + dbRep);
                    }else{
                        platformList.clear();
                        String sql1="SELECT platform_id,platform_name,platform_description,STATUS FROM tb_cms_platforms ORDER BY status desc, platform_name asc;";
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PlatformBean.class :: refreshCountryList() :: REFRESH PLATFORM SQL : " + sql1);
                        java.sql.ResultSet rs=st.executeQuery(sql1);
                        CMSPlatform newObj=null;
                        while(rs.next()){
                            newObj=new CMSPlatform();
                            newObj.setPlatformId(rs.getInt("platform_id"));
                            newObj.setPlatformName(rs.getString("platform_name"));
                            newObj.setPlatformDescription(rs.getString("platform_description"));
                            newObj.setStatus(rs.getInt("STATUS"));
                            platformList.add(newObj);
                            newObj=null;
                        }
                        rs.close();
                        rs=null;
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PlatformBean.class :: refreshCountryList() :: Platform List (ItemCount=" + platformList.size() + ") Reloaded. ");
                    }
                    st.close();
                    st=null;
                }catch(Exception e){
                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PlatformBean.class :: refreshCountryList() :: Exception :" + e.getMessage());
                    statusMsg.setMessage("A process failed while reloading Platform List. Database Error!",StatusMessage.MSG_CLASS_ERROR);
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
            }else{
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PlatformBean.class :: refreshCountryList() :: Database Connectivity Failed");
                statusMsg.setMessage("A process failed while reloading Platform List. Database Connectivity Issue.",StatusMessage.MSG_CLASS_ERROR);
            }

        
    }
    
     /**
      * Edit Event Handler Method
      */
     public void onEdit(RowEditEvent event) {  
       CMSPlatform newObj=(CMSPlatform)event.getObject();
       if(newObj!=null && newObj.getPlatformId()>0){
           statusMsg.setMessage("Platform Details sent for update into Registry!", StatusMessage.MSG_CLASS_INFO);
           refreshCountryList(newObj,2);
       }else{
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PlatformBean.class :: onEdit() :: Invalid Platform Id (" + newObj.getPlatformId() + ").");
            statusMsg.setMessage("Invalid Platform Id `" + newObj.getPlatformId() + "`. Please select a Platform to update.", StatusMessage.MSG_CLASS_ERROR);
       }
       
    }  

     
     
    /**
    * Method will create new Platform into database based on the details provided in the countryObject, If not created the statusMsg
     */
    
    public void createNew(){
        newPlatform.setPlatformName((newPlatform.getPlatformName()==null)?"":newPlatform.getPlatformName().trim());
        newPlatform.setPlatformDescription((newPlatform.getPlatformDescription()==null)?"":newPlatform.getPlatformDescription().trim());
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PlatformBean.class :: createNew() :: New Platform Name to Register '" + newPlatform.getPlatformName() + "'");
        
        String pV=StringInputValidator.validateString(newPlatform.getPlatformName(), "<>&@#$!`'^*?/=");
        String pDV=StringInputValidator.validateString(newPlatform.getPlatformDescription(), "<>&@#$!`'^*?/=");
        if(!pV.equalsIgnoreCase(newPlatform.getPlatformName()) || !pDV.equalsIgnoreCase(newPlatform.getPlatformDescription())){
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PlatformBean.class :: createNew() :: Invalid Circle Name ");
           statusMsg.setMessage("Sorry! Invalid Input. Special Characters are not allowed.",StatusMessage.MSG_CLASS_ERROR);
       }else if(newPlatform.getPlatformName().length()>=3 && newPlatform.getPlatformName().length()<=50){
            statusMsg.setMessage("New Platform Name sent for registration!", StatusMessage.MSG_CLASS_INFO);
            refreshCountryList(newPlatform, 1);
        }else{
            //Invalid Country Name
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PlatformBean.class :: createNew() :: Invalid Platform Name `" + newPlatform.getPlatformName() + "`. Platform Name must have number of charactesrs more then 3 and less then 50.");
            statusMsg.setMessage("Invalid Platform Name `"+newPlatform.getPlatformName()+"`. Platform Name shall have number of charactesrs more then 3 and less then 50.",StatusMessage.MSG_CLASS_ERROR);
        }
                
    } 

    public CMSPlatform getNewPlatform() {
        return newPlatform;
    }

    public void setNewPlatform(CMSPlatform newPlatform) {
        this.newPlatform = newPlatform;
    }

    public List<CMSPlatform> getPlatformList() {
        return platformList;
    }

    public void setPlatformList(List<CMSPlatform> platformList) {
        this.platformList = platformList;
    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }

    public CMSPlatform getSelectedPlatform() {
        return selectedPlatform;
    }

    public void setSelectedPlatform(CMSPlatform selectedPlatform) {
        this.selectedPlatform = selectedPlatform;
    }
    
    
}
