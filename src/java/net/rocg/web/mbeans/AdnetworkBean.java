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
import net.rocg.web.beans.Country;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author Rishi Tyagi
 */
@ManagedBean
@RequestScoped
public class AdnetworkBean implements java.io.Serializable{
   DBConnection dbConn;
    CMSAdnetwork newAdnetwork;
    
    List<CMSAdnetwork> adnetworkList;
    CMSAdnetwork selectedAdnetwork;
    StatusMessage statusMsg;
    /**
     * Creates a new instance of PlatformBean
     */
    public AdnetworkBean() {
        dbConn=new DBConnection();
        statusMsg=new StatusMessage();
        newAdnetwork=new CMSAdnetwork();
        adnetworkList=new ArrayList<>();
        refreshCountryList(null,0);
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "AdnetworkBean :: MBean Connected.");
    }
    public void reloadList(){
    }
    
    public void refreshCountryList(CMSAdnetwork updateObj,int action){
        Connection conn=dbConn.connect();
        StringBuilder logMsg=new StringBuilder();
        
        if(conn!=null){
                logMsg.append("Database Connected;");
                try{
                    java.sql.Statement st=conn.createStatement();
                    String sqlA="";int dbRep=0;
                    if(action==2 && updateObj!=null && updateObj.getAdnetworkId()>0){
                        //Update Existing Country Name
                        sqlA="update tb_adnetworks set network_name='"+updateObj.getAdnetworkName()+"',  status="+updateObj.getStatus()+" where ad_network_id="+updateObj.getAdnetworkId()+";";
                        dbRep=st.executeUpdate(sqlA);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "AdnetworkBean :: refreshCountryList() :: Action Requested=UPDATE-PLATFORM; SQL : " + sqlA + "| DB Result=" + dbRep);
                    }else if(action==1 && updateObj!=null && updateObj.getAdnetworkName()!=null && updateObj.getAdnetworkName().length()>0 && updateObj.getAdnetworkDeac()!=null && updateObj.getAdnetworkDeac().length()>0){
                        //Create new Country
                        sqlA="insert into tb_adnetworks(network_name,network_descp,reg_date,status) values('"+updateObj.getAdnetworkName()+"','"+updateObj.getAdnetworkDeac()+"',now(),1);";
                        dbRep=st.executeUpdate(sqlA);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "AdnetworkBean :: refreshCountryList() :: Action Requested=REGISTER-NEW-PLATFORM; SQL : " + sqlA + "| DB Result=" + dbRep);
                    }else{
                        adnetworkList.clear();
                        String sql1="select ad_network_id,network_name,network_descp,status from tb_adnetworks ORDER BY status desc, ad_network_id asc;";
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "AdnetworkBean :: refreshCountryList() :: REFRESH Adnetwork SQL : " + sql1);
                        java.sql.ResultSet rs=st.executeQuery(sql1);
                        CMSAdnetwork newObj=null;
                        while(rs.next()){
                            newObj=new CMSAdnetwork();
                            newObj.setAdnetworkId(rs.getInt("ad_network_id"));
                            newObj.setAdnetworkName(rs.getString("network_name"));
                            newObj.setAdnetworkDeac(rs.getString("network_descp"));
                            newObj.setStatus(rs.getInt("status"));
                            adnetworkList.add(newObj);
                            newObj=null;
                        }
                        rs.close();
                        rs=null;
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "AdnetworkBean :: refreshCountryList() :: Platform List (ItemCount=" + adnetworkList.size() + ") Reloaded. ");
                    }
                    st.close();
                    st=null;
                }catch(Exception e){
                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "AdnetworkBean :: refreshCountryList() :: Exception :" + e.getMessage());
                    statusMsg.setMessage("A process failed while reloading Platform List. Database Error!",StatusMessage.MSG_CLASS_ERROR);
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
            }else{
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "AdnetworkBean :: refreshCountryList() :: Database Connectivity Failed");
                statusMsg.setMessage("A process failed while reloading Platform List. Database Connectivity Issue.",StatusMessage.MSG_CLASS_ERROR);
            }

        
    }
    
     /**
      * Edit Event Handler Method
      */
     public void onEdit(RowEditEvent event) {  
       CMSAdnetwork newObj=(CMSAdnetwork)event.getObject();
       dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "AdnetworkBean :: onEdit() :: Adnetwork Data For Update (" + newObj.toString()+ ").");
       if(newObj!=null && newObj.getAdnetworkId()>0){
           statusMsg.setMessage("Platform Details sent for update into Registry!", StatusMessage.MSG_CLASS_INFO);
           refreshCountryList(newObj,2);
       }else{
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "AdnetworkBean :: onEdit() :: Invalid Platform Id (" + newObj.getAdnetworkId() + ").");
            statusMsg.setMessage("Invalid Platform Id `" + newObj.getAdnetworkId() + "`. Please select a Platform to update.", StatusMessage.MSG_CLASS_ERROR);
       }
       
    }  

     
     
    /**
    * Method will create new Platform into database based on the details provided in the countryObject, If not created the statusMsg
     */
    
    public void createNew(){
        newAdnetwork.setAdnetworkName((newAdnetwork.getAdnetworkName()==null)?"":newAdnetwork.getAdnetworkName().trim());
        newAdnetwork.setAdnetworkDeac((newAdnetwork.getAdnetworkDeac()==null)?"":newAdnetwork.getAdnetworkDeac().trim());
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "AdnetworkBean :: createNew() :: New Platform Name to Register '" + newAdnetwork.getAdnetworkName()+ "'");
        
        String pV=StringInputValidator.validateString(newAdnetwork.getAdnetworkName(), "<>&@#$!`'^*?/=");
        String pDV=StringInputValidator.validateString(newAdnetwork.getAdnetworkDeac(), "<>&@#$!`'^*?/=");
        if(!pV.equalsIgnoreCase(newAdnetwork.getAdnetworkName()) || !pDV.equalsIgnoreCase(newAdnetwork.getAdnetworkDeac())){
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "AdnetworkBean :: createNew() :: Invalid Circle Name ");
           statusMsg.setMessage("Sorry! Invalid Input. Special Characters are not allowed.",StatusMessage.MSG_CLASS_ERROR);
       }else if(newAdnetwork.getAdnetworkName().length()>=3 && newAdnetwork.getAdnetworkName().length()<=50){
            statusMsg.setMessage("New Adnetwork Name sent for registration!", StatusMessage.MSG_CLASS_INFO);
            refreshCountryList(newAdnetwork, 1);
        }else{
            //Invalid Country Name
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "AdnetworkBean :: createNew() :: Invalid Platform Name `" + newAdnetwork.getAdnetworkName()+ "`. Platform Name must have number of charactesrs more then 3 and less then 50.");
            statusMsg.setMessage("Invalid Platform Name `"+newAdnetwork.getAdnetworkName()+"`. Adnetwork Name shall have number of charactesrs more then 3 and less then 50.",StatusMessage.MSG_CLASS_ERROR);
        }
                
    } 

    public CMSAdnetwork getNewAdnetwork() {
        return newAdnetwork;
    }

    public void setNewAdnetwork(CMSAdnetwork newAdnetwork) {
        this.newAdnetwork = newAdnetwork;
    }

    public List<CMSAdnetwork> getAdnetworkList() {
        return adnetworkList;
    }

    public void setAdnetworkList(List<CMSAdnetwork> adnetworkList) {
        this.adnetworkList = adnetworkList;
    }

    public CMSAdnetwork getSelectedAdnetwork() {
        return selectedAdnetwork;
    }

    public void setSelectedAdnetwork(CMSAdnetwork selectedAdnetwork) {
        this.selectedAdnetwork = selectedAdnetwork;
    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }

    
    
}
