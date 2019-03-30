/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rocg.web.mbeans;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import net.rocg.util.StringInputValidator;
import net.rocg.web.beans.MsisdnSeries;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.UploadedFile;

/**
 *
 * @author Rishi Tyagi
 */
@ManagedBean
@SessionScoped
public class MsisdnSeriesBean  implements java.io.Serializable{

    DBConnection dbConn;
    Map<String,String> operatorList;
    String selectedOperator;
    
    Map<String,String> circleList;
    String selectedCircle;
    
    Map<String,String> ratingEngineList;
    String selectedRatingEngine;
    
    String newMsisdnSeries;
    ArrayList<String> newMsisdnSeriesBulk;
            
    List<MsisdnSeries> msisdnSeriesList;
    MsisdnSeries selectedMsisdnSeries;
    StatusMessage statusMsg;
    UploadedFile msisdnSeriesBulkFile;
    
    /**
     * Creates a new instance of MsisdnSeriesBean
     */
    
    public MsisdnSeriesBean() {
        dbConn=new DBConnection();
        newMsisdnSeriesBulk=new ArrayList<>();
        operatorList=new HashMap<>();
        circleList=new HashMap<>();
        ratingEngineList=new HashMap<>();
        msisdnSeriesList=new ArrayList<>();
        selectedMsisdnSeries=new MsisdnSeries();
        statusMsg=new StatusMessage();
        System.out.println("Constructer Invoked.");
        refreshMsisdnSeriesList(null,0,true,true,true);
         dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: MBean Connected.");
    }

    public void refreshCircles(){
        refreshMsisdnSeriesList(null,0,false,false,true);
    }
    
    public void refreshMsisdnSeriesList(MsisdnSeries updateObj,int action,boolean refreshOperatorList,boolean refreshRatingEngineList,boolean refreshCircleList){
        Connection conn=dbConn.connect();
        StringBuilder logMsg=new StringBuilder();
        
        if(conn!=null){
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Database Connected.");
                try{
                    java.sql.Statement st=null;
                    String sqlA="";int dbRep=0;
                    if(action==2 && updateObj!=null && updateObj.getMsisdnSeriesId()>0){
                        //Update Existing Operator Name
                        sqlA="update tb_msisdn_series set msisdn_prefix='"+updateObj.getMsisdnPrefix()+"', status="+updateObj.getStatus()+",rating_engine_id="+updateObj.getRatingEngineId()+",last_update=now() where msisdn_series_id="+updateObj.getMsisdnSeriesId()+";";
                        st=conn.createStatement();
                        dbRep=st.executeUpdate(sqlA);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Action Requested=UPDATE-MSISDN-SERIES SQL : " + sqlA + "| DB Result=" + dbRep);
                     }else if(action==1 && updateObj!=null && updateObj.getCircleId()>0 && updateObj.getRatingEngineId()>0 && (updateObj.getMsisdnPrefix().length()>4 || newMsisdnSeriesBulk.size()>0)){
                        //Create new Operator
                        String sql11="insert into tb_msisdn_series(msisdn_prefix,circle_id,date_of_creation,last_update,status,rating_engine_id) values(?,"+updateObj.getCircleId()+",now(),now(),1,'"+updateObj.getRatingEngineId()+"');";
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Action Requested=REGISTER-NEW-MSISDN-SERIES SQL : " + sqlA);
                        PreparedStatement pst=null;
                        try{
                            pst=conn.prepareStatement(sql11);
                            if(newMsisdnSeriesBulk.size()>0){
                                for (String newMsisdnSeriesBulk1 : newMsisdnSeriesBulk) {
                                    try{
                                    pst.setString(1, newMsisdnSeriesBulk1);
                                    dbRep=pst.executeUpdate();
                                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Action Requested=REGISTER-NEW-MSISDN-SERIES(Bulk) : " + newMsisdnSeriesBulk1 +"|DB Result=" + dbRep);
                                    }catch(Exception eee){
                                        dbRep=0;
                                        dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Action Requested=REGISTER-NEW-MSISDN-SERIES(Bulk) : Failed to register " + newMsisdnSeriesBulk1 +"|Exception " + eee.getMessage());
                                        statusMsg.setMessage("Failed to register some of the series. Database Error "+eee.getMessage(),StatusMessage.MSG_CLASS_ERROR);
                                    }
                                    
                                }
                            }else{
                                try{
                                    pst.setString(1, updateObj.getMsisdnPrefix());
                                    dbRep=pst.executeUpdate();
                                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Action Requested=REGISTER-NEW-MSISDN-SERIES : " + updateObj.getMsisdnPrefix() +"|DB Result=" + dbRep);
                                }catch(Exception eee){
                                    dbRep=0;
                                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Action Requested=REGISTER-NEW-MSISDN-SERIES(Bulk) : Failed to register " + updateObj.getMsisdnPrefix() +"|Exception " + eee.getMessage());
                                    statusMsg.setMessage("Failed to register some of the series. Database Error "+eee.getMessage(),StatusMessage.MSG_CLASS_ERROR);
                                }
                                
                            }

                        }catch(Exception ee){
                            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Action Requested=REGISTER-NEW-MSISDN-SERIES : Exception " + ee.getMessage());
                        }finally{
                            if(pst!=null) pst.close();
                            pst=null;
                        }
                        
                    }else{
                        st=conn.createStatement();
                        String sql1="SELECT O.operator_id,C.country_name,O.operator_name FROM tb_operators O, tb_country C WHERE O.`country_id`=C.`country_id` ORDER BY C.`country_name`,O.`operator_name`;";

                        java.sql.ResultSet rs=null;
                        //Refresh Operator list if asked for
                        if(refreshOperatorList){
                                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: REFRESH OPERATOR  SQL : " + sql1);
                                operatorList.clear();
                                rs=st.executeQuery(sql1);
                                while(rs.next()){
                                    operatorList.put(rs.getString("country_name")+"::"+rs.getString("operator_name"), rs.getString("operator_id"));
                                }
                                rs.close();
                                rs=null;
                                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Operator List (ItemCount=" + operatorList.size() + ") Reloaded. ");
                        }

                        //Reload Rating Engine List if asked for
                        if(refreshRatingEngineList){
                            sql1="SELECT rating_engine_id,engine_name FROM tb_rating_engine WHERE STATUS>0 ORDER BY engine_name;";
                            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: REFRESH RATING ENGINE  SQL : " + sql1);
                            ratingEngineList.clear();
                            rs=st.executeQuery(sql1);
                            while(rs.next()){
                                ratingEngineList.put(rs.getString("engine_name"), rs.getString("rating_engine_id"));
                            }
                            rs.close();
                            rs=null;
                            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Rating Engine List (ItemCount=" + ratingEngineList.size() + ") Reloaded. ");
                        }

                        //Reload Circle List if asked for
                        if(refreshCircleList && RUtil.strToInt(selectedOperator, 0)>0){

                            sql1="SELECT circle_id,circle_name FROM tb_operator_circles WHERE operator_id="+selectedOperator+" ORDER BY circle_name;";
                            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: REFRESH CIRCLE LIST  SQL : " + sql1);
                            circleList.clear();
                            rs=st.executeQuery(sql1);
                            while(rs.next()){
                                circleList.put(rs.getString("circle_name"), rs.getString("circle_id"));
                            }
                            rs.close();
                            rs=null;
                            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Circle List (ItemCount=" + circleList.size() + ") Reloaded. ");
                        }
                        msisdnSeriesList.clear();
                        sql1="SELECT MS.`msisdn_series_id`,MS.`msisdn_prefix`,MS.`rating_engine_id`,RE.`engine_name`,MS.circle_id,O.country_id,C.country_name,CR.operator_id,O.operator_name,CR.circle_name,CR.status,CR.lrn FROM tb_msisdn_series MS,tb_operators O, tb_country C,tb_operator_circles CR,tb_rating_engine RE WHERE MS.`circle_id`=CR.`circle_id` AND CR.operator_id=O.`operator_id` AND O.country_id=C.`country_id` AND MS.`rating_engine_id`=RE.`rating_engine_id`  ORDER BY CR.`status` DESC, C.`country_name` ASC,O.`operator_name` ASC,CR.`circle_name` ASC;";
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: REFRESH MSISDN SERIES LIST  SQL : " + sql1);
                        rs=st.executeQuery(sql1);
                        MsisdnSeries newSeries=null;
                        while(rs.next()){
                            newSeries=new MsisdnSeries();
                            newSeries.setMsisdnSeriesId(rs.getInt("msisdn_series_id"));
                            newSeries.setMsisdnPrefix(rs.getString("msisdn_prefix"));
                            newSeries.setRatingEngineId(rs.getInt("rating_engine_id"));
                            newSeries.setRatingEngineName(rs.getString("engine_name"));
                            newSeries.setCircleId(rs.getInt("circle_id"));
                            newSeries.setCountryId(rs.getInt("country_id"));
                            newSeries.setCountryName(rs.getString("country_name"));
                            newSeries.setOperatorId(rs.getInt("operator_id"));
                            newSeries.setOperatorName(rs.getString("operator_name"));
                            newSeries.setCircleName(rs.getString("circle_name"));
                            newSeries.setStatus(rs.getInt("status"));
                            newSeries.setLrn(rs.getString("lrn"));
                            msisdnSeriesList.add(newSeries);
                            newSeries=null;
                        }
                        rs.close();
                        rs=null;
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: MSISDN SERIES LIST List (ItemCount=" + msisdnSeriesList.size() + ") Reloaded. ");
                    }
                    if(st!=null) st.close();
                    st=null;
                }catch(Exception e){
                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Exception :" + e.getMessage());
                    statusMsg.setMessage("A process failed while reloading Msisdn Series List. Database Error!",StatusMessage.MSG_CLASS_ERROR);
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
            }else{
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "MsisdnSeriesBean.class :: refreshMsisdnSeriesList() :: Database Connectivity Failed");
                statusMsg.setMessage("A process failed while reloading Msisdn Series List. Database Connectivity Issue.",StatusMessage.MSG_CLASS_ERROR);
            }
        
    }
    
    /**
      * Edit Event Handler Method
      */
     public void onEdit(RowEditEvent event) {  
       MsisdnSeries newSeries=(MsisdnSeries)event.getObject();
       int seriesId=(newSeries==null)?0:newSeries.getMsisdnSeriesId();
       if(seriesId>0){
           statusMsg.setMessage("MSISDN Series Details sent for update into Registry!", StatusMessage.MSG_CLASS_INFO);
           refreshMsisdnSeriesList(newSeries,2,false,false,false);
       }else{
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "MsisdnSeriesBean.class :: onEdit() :: Invalid Msisdn Series Id (" + seriesId + ").");
            statusMsg.setMessage("Invalid Msisdn Series Id `" +seriesId + "`. Please select a Msisdn Series to update.", StatusMessage.MSG_CLASS_ERROR);
       }
       
    }  
     
    public void createNew(){
       int selCircleId=RUtil.strToInt(selectedCircle, 0);
       int ratingEngineId=RUtil.strToInt(selectedRatingEngine, 0);
       dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: createNew() :: Selected Circle Id '" + selCircleId + "', Selected Rating Engine Id  "+ratingEngineId);

       StringBuilder logMsg=new StringBuilder();
       newMsisdnSeriesBulk.clear();
       if(msisdnSeriesBulkFile!=null){
           System.out.println(msisdnSeriesBulkFile.getFileName());
           dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: createNew() :: Extracting Msisdn Series from File  "+msisdnSeriesBulkFile.getFileName());
           try{
           BufferedReader dis=new BufferedReader(new InputStreamReader(msisdnSeriesBulkFile.getInputstream()));
           boolean endFlag=false; String newLine=null;
           while(!endFlag){
               newLine=dis.readLine();
               if(newLine==null){
                   endFlag=true;
               }else{
                   newLine=(newLine==null)?"":newLine.trim();
                   if(newLine.length()>5){
                       newMsisdnSeriesBulk.add(newLine);
                       dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: createNew() :: Series Extracted "+newLine);
                   }else{
                       logMsg.append("Ignored Series "+newLine);
                       dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: createNew() :: Series Ignored "+newLine);
                   }
                   newLine=null;
               }
           }
           dis.close();dis=null;
           
           }catch(Exception e){
               dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "MsisdnSeriesBean.class :: createNew() :: Exception while picking Msisdn Series from File "+e.getMessage());
              
           }
           
       }
       newMsisdnSeries=(newMsisdnSeriesBulk.size()>0)?"":((newMsisdnSeries==null)?"":newMsisdnSeries.trim());
       dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "MsisdnSeriesBean.class :: createNew() :: New Singleton Msisdn Series "+newMsisdnSeries);
       
       if(selCircleId<=0){
           //Invalid Country Id
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "MsisdnSeriesBean.class :: createNew() :: Invalid Operator Circle Id `" + selCircleId + "`. Can't register new Msisdn Series.");
            statusMsg.setMessage("Can't register new Msisdn Series. Invalid Circle/Operator Id!",StatusMessage.MSG_CLASS_ERROR);
       }else if(ratingEngineId<=0){
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "MsisdnSeriesBean.class :: createNew() :: Invalid Rating Engine Id `" + ratingEngineId + "`. Can't register new Msisdn Series.");
            statusMsg.setMessage("Can't register new Msisdn Series. Invalid Rating Engine Id!",StatusMessage.MSG_CLASS_ERROR);
       }else if(newMsisdnSeriesBulk.size()<0 && (newMsisdnSeries.length()<=4 || newMsisdnSeries.length()>12)){
           //Invalid Operator Name
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "MsisdnSeriesBean.class :: createNew() :: Invalid Msisdn Series`" + newMsisdnSeries + "`. Series must contain 5-12 digits!");
           statusMsg.setMessage("Can't register new Msisdn Series. Invalid Msisdn Series. Series must contain 5-12 digits!",StatusMessage.MSG_CLASS_ERROR);
       }else{
          MsisdnSeries newSeries=new MsisdnSeries();
          newSeries.setCircleId(selCircleId);
          newSeries.setRatingEngineId(ratingEngineId);
          newSeries.setMsisdnPrefix(newMsisdnSeries);
          statusMsg.setMessage("New Msisdn Series '"+newMsisdnSeries+"',CircleId '"+selCircleId+"' sent for Registration!",StatusMessage.MSG_CLASS_INFO);
          refreshMsisdnSeriesList(newSeries,1,false,false,false);
          
       }
    
    }

    public Map<String, String> getOperatorList() {
        return operatorList;
    }

    public void setOperatorList(Map<String, String> operatorList) {
        this.operatorList = operatorList;
    }

    public String getSelectedOperator() {
        return selectedOperator;
    }

    public void setSelectedOperator(String selectedOperator) {
        this.selectedOperator = selectedOperator;
    }

    public Map<String, String> getCircleList() {
        return circleList;
    }

    public void setCircleList(Map<String, String> circleList) {
        this.circleList = circleList;
    }

    public String getSelectedCircle() {
        return selectedCircle;
    }

    public void setSelectedCircle(String selectedCircle) {
        this.selectedCircle = selectedCircle;
    }

    public Map<String, String> getRatingEngineList() {
        return ratingEngineList;
    }

    public void setRatingEngineList(Map<String, String> ratingEngineList) {
        this.ratingEngineList = ratingEngineList;
    }

    public String getSelectedRatingEngine() {
        return selectedRatingEngine;
    }

    public void setSelectedRatingEngine(String selectedRatingEngine) {
        this.selectedRatingEngine = selectedRatingEngine;
    }

    public String getNewMsisdnSeries() {
        return newMsisdnSeries;
    }

    public void setNewMsisdnSeries(String newMsisdnSeries) {
        long newSeries=RUtil.strToLong(newMsisdnSeries, 0);
        if(newSeries>0)
            this.newMsisdnSeries = newMsisdnSeries;
    }

  
    public List<MsisdnSeries> getMsisdnSeriesList() {
        return msisdnSeriesList;
    }

    public void setMsisdnSeriesList(List<MsisdnSeries> msisdnSeriesList) {
        this.msisdnSeriesList = msisdnSeriesList;
    }

    public MsisdnSeries getSelectedMsisdnSeries() {
        return selectedMsisdnSeries;
    }

    public void setSelectedMsisdnSeries(MsisdnSeries selectedMsisdnSeries) {
        this.selectedMsisdnSeries = selectedMsisdnSeries;
    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }

    public UploadedFile getMsisdnSeriesBulkFile() {
        return msisdnSeriesBulkFile;
    }

    public void setMsisdnSeriesBulkFile(UploadedFile msisdnSeriesBulkFile) {
        this.msisdnSeriesBulkFile = msisdnSeriesBulkFile;
    }
    
}
