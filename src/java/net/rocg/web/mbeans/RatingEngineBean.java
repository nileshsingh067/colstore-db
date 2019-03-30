/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rocg.web.mbeans;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.rocg.rating.beans.RatingEngine;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.ToggleEvent;

/**
 *
 * @author Rishi Tyagi
 */
public class RatingEngineBean {

    RatingEngine newRatingEngine;
    RatingEngine selectedRatingEngine;
    Map<String,String> ratingEngineMap;
    List<RatingEngine> ratingEngineList;
    StatusMessage statusMsg;
    DBConnection dbConn;
    boolean createNewActionFlag;
    String ratingEngineIdToEdit;
    
    int loginId;
    int roleId;
    String loginName;
    /**
     * Creates a new instance of RatingEngineBean
     */
    public RatingEngineBean() {
        dbConn=new DBConnection();
        statusMsg=new StatusMessage();
        if(newRatingEngine==null) newRatingEngine=new RatingEngine();
        selectedRatingEngine=new RatingEngine();
        ratingEngineList=new ArrayList<RatingEngine>();
        ratingEngineMap=new HashMap<String,String>();
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "RatingEngineBean.class :: MBean Connected.");
        reloadRatingEngineList(0,null);
        this.setCreateNewActionFlag(true);
        fetchLoginDetails();
       
    }
    
    
    public void updateRecord(){
        //Create New /Update Record
         int ratingEngineId=RUtil.strToInt(ratingEngineIdToEdit, 0);
         String logHeader="RatingEngineBean.class :: updateRecord() ::";
         dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO,  logHeader+" createNewActionFlag="+createNewActionFlag+", ratingEngineId="+ratingEngineId);
         dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO,  logHeader+" Updated Rating Engine Details ="+newRatingEngine.toShortString());
         if(createNewActionFlag){
         reloadRatingEngineList(1,newRatingEngine);
         }
         else{
         java.sql.Connection conn=dbConn.connect();
        if(conn==null) conn=dbConn.connect();
        if(conn!=null){
            try{
                String sql1="UPDATE tb_rating_engine SET engine_name=?,handler_class=?,descp=?,last_update=NOW()," +
                            "update_user=?,subs_consent_flag=?,event_consent_flag=?,vpack_consent_flag=?,unsub_consent_flag=?," +
                            "subs_smsnotify_flag=?,event_smsnotify_flag=?,vpack_smsnotify_flag=?,unsub_smsnotify_flag=? WHERE rating_engine_id=?";
                java.sql.PreparedStatement pst=conn.prepareCall(sql1);
                pst.setString(1, (newRatingEngine.getEngineName()==null)?"":newRatingEngine.getEngineName());
                pst.setString(2, (newRatingEngine.getHandlerClass()==null)?"":newRatingEngine.getHandlerClass());
                pst.setString(3, (newRatingEngine.getRatingEngineDescription()==null)?"":newRatingEngine.getRatingEngineDescription());
                pst.setInt(4, getLoginId());
                pst.setInt(5, newRatingEngine.isSubscriptionConsentEnabled()?1:0);
                pst.setInt(6, newRatingEngine.isEventConsentEnabled()?1:0);
                pst.setInt(7, newRatingEngine.isTopupConsentEnabled()?1:0);
                pst.setInt(8, newRatingEngine.isUnsubConsentEnabled()?1:0);
                pst.setInt(9, newRatingEngine.isSubscriptionSMSNotifyEnabled()?1:0);
                pst.setInt(10, newRatingEngine.isEventSMSNotifyEnabled()?1:0);
                pst.setInt(11, newRatingEngine.isTopupSMSNotifyEnabled()?1:0);
                pst.setInt(12, newRatingEngine.isUnsubSMSNotifyEnabled()?1:0);
                pst.setInt(13, ratingEngineId);
                int rep=pst.executeUpdate();
                if(pst!=null) pst.close();
                pst=null;
                
                sql1="UPDATE tb_rating_engine_urls SET url=? WHERE rating_engine_id=? AND url_category='CHARGING'";
                pst=conn.prepareCall(sql1);
                pst.setString(1, (newRatingEngine.getSubscriptionURL().getRatingURL()==null)?"":newRatingEngine.getSubscriptionURL().getRatingURL());
                pst.setInt(2, ratingEngineId);
                rep=pst.executeUpdate();
                if(pst!=null) pst.close();
                pst=null;
                
                sql1="UPDATE tb_rating_engine_urls SET url=? WHERE rating_engine_id=? AND url_category='EVENT-CHARGING'";
                pst=conn.prepareCall(sql1);
                pst.setString(1, (newRatingEngine.getEventChargeURL().getRatingURL()==null)?"":newRatingEngine.getEventChargeURL().getRatingURL());
                pst.setInt(2, ratingEngineId);
                rep=pst.executeUpdate();
                if(pst!=null) pst.close();
                pst=null;
                
                sql1="UPDATE tb_rating_engine_urls SET url=? WHERE rating_engine_id=? AND url_category='VPACK-CHARGING'";
                pst=conn.prepareCall(sql1);
                pst.setString(1, (newRatingEngine.getValuePackChargeURL().getRatingURL()==null)?"":newRatingEngine.getValuePackChargeURL().getRatingURL());
                pst.setInt(2, ratingEngineId);
                rep=pst.executeUpdate();
                if(pst!=null) pst.close();
                pst=null;
                
                sql1="UPDATE tb_rating_engine_urls SET url=? WHERE rating_engine_id=? AND url_category='UNSUB'";
                pst=conn.prepareCall(sql1);
                pst.setString(1, (newRatingEngine.getUnsubURL().getRatingURL()==null)?"":newRatingEngine.getUnsubURL().getRatingURL());
                pst.setInt(2, ratingEngineId);
                rep=pst.executeUpdate();
                if(pst!=null) pst.close();
                pst=null;
                this.statusMsg.setMessage("Rating Engine Details Updated Successfully against Rating engine Id ("+ratingEngineId+") !", StatusMessage.MSG_CLASS_INFO);
            }catch(Exception e){
                this.statusMsg.setMessage("Failed to update details against Rating engine Id ("+ratingEngineId+") ! Pl try again later!", StatusMessage.MSG_CLASS_ERROR);
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR,  logHeader+"DB Error : "+e.getMessage());
            }finally{
               try{
                   if(conn!=null) conn.close();
               }catch(Exception ee){}
               conn=null;
            }
        }else{
            this.statusMsg.setMessage("Failed to update details against Rating engine Id ("+ratingEngineId+") ! Pl try again later!", StatusMessage.MSG_CLASS_ERROR);
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR,  logHeader+" DBERROR : Failed to connect Database!");
        }
         }    
    
    
    }
    
    public void edit() {  
         //selectedRatingEngine
        String logHeader="RatingEngineBean.class :: edit() ::";
        int ratingEngineId=RUtil.strToInt(ratingEngineIdToEdit, 0);
        if(ratingEngineId>0){
            
            java.sql.Connection conn=dbConn.connect();
            if(conn==null) conn=dbConn.connect();
            if(conn!=null){
                this.setCreateNewActionFlag(false);
                try{
                    java.sql.Statement st=conn.createStatement();
                    String sql1="SELECT rating_engine_id,engine_name,handler_class,descp,reg_date,last_update,"
                            + "subs_consent_flag,event_consent_flag,vpack_consent_flag,subs_smsnotify_flag,"
                            + "event_smsnotify_flag,event_smsnotify_flag,vpack_smsnotify_flag FROM tb_rating_engine WHERE rating_engine_id="+ratingEngineId+";";
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG,  logHeader+"Running SQL : "+sql1);
                    
                    newRatingEngine=null;
                    java.sql.ResultSet rs=st.executeQuery(sql1);
                    if(rs.next()){
                        newRatingEngine=new RatingEngine();
                        newRatingEngine.setRatingEngineId(rs.getInt("rating_engine_id"));
                        newRatingEngine.setEngineName(rs.getString("engine_name"));
                        newRatingEngine.setHandlerClass(rs.getString("handler_class"));
                        newRatingEngine.setRatingEngineDescription(rs.getString("descp"));
                        newRatingEngine.setRegDate(rs.getString("reg_date"));
                        newRatingEngine.setLastUpdate(rs.getString("last_update"));
                        newRatingEngine.setSubscriptionConsentEnabled(RUtil.intToBool(rs.getInt("subs_consent_flag")));
                        newRatingEngine.setEventConsentEnabled(RUtil.intToBool(rs.getInt("event_consent_flag")));
                        newRatingEngine.setTopupConsentEnabled(RUtil.intToBool(rs.getInt("vpack_consent_flag")));
                        newRatingEngine.setSubscriptionSMSNotifyEnabled(RUtil.intToBool(rs.getInt("subs_smsnotify_flag")));
                        newRatingEngine.setEventSMSNotifyEnabled(RUtil.intToBool(rs.getInt("event_smsnotify_flag")));
                        newRatingEngine.setTopupSMSNotifyEnabled(RUtil.intToBool(rs.getInt("event_smsnotify_flag")));
                    }
                    if(rs!=null) rs.close();
                    rs=null;
                    if(newRatingEngine!=null && newRatingEngine.getRatingEngineId()>0){
                        //Extract Rating URL's
                        sql1="SELECT url_category,rating_url_id,rating_engine_id,rating_url_name,url,status FROM tb_rating_engine_urls WHERE rating_engine_id="+newRatingEngine.getRatingEngineId()+";";
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG,  logHeader+"Running SQL : "+sql1);
                        rs=st.executeQuery(sql1);
                       
                        while(rs.next()){
                           String urlCatg=rs.getString("url_category");
                           if(urlCatg.equalsIgnoreCase("EVENT-CHARGING")){
                               newRatingEngine.getEventChargeURL().setRatingURLId(rs.getInt("rating_url_id"));
                               newRatingEngine.getEventChargeURL().setRatingEngineId(rs.getInt("rating_engine_id"));
                               newRatingEngine.getEventChargeURL().setRatingURLName(rs.getString("rating_url_name"));
                               newRatingEngine.getEventChargeURL().setRatingURL(rs.getString("url"));
                               newRatingEngine.getEventChargeURL().setStatus(rs.getInt("status"));
                               newRatingEngine.getEventChargeURL().setRatingURLCategory(urlCatg);
                           }else if(urlCatg.equalsIgnoreCase("VPACK-CHARGING")){
                               newRatingEngine.getValuePackChargeURL().setRatingURLId(rs.getInt("rating_url_id"));
                               newRatingEngine.getValuePackChargeURL().setRatingEngineId(rs.getInt("rating_engine_id"));
                               newRatingEngine.getValuePackChargeURL().setRatingURLName(rs.getString("rating_url_name"));
                               newRatingEngine.getValuePackChargeURL().setRatingURL(rs.getString("url"));
                               newRatingEngine.getValuePackChargeURL().setStatus(rs.getInt("status"));
                               newRatingEngine.getValuePackChargeURL().setRatingURLCategory(urlCatg); 
                           }else if(urlCatg.equalsIgnoreCase("UNSUB")){
                               newRatingEngine.getUnsubURL().setRatingURLId(rs.getInt("rating_url_id"));
                               newRatingEngine.getUnsubURL().setRatingEngineId(rs.getInt("rating_engine_id"));
                               newRatingEngine.getUnsubURL().setRatingURLName(rs.getString("rating_url_name"));
                               newRatingEngine.getUnsubURL().setRatingURL(rs.getString("url"));
                               newRatingEngine.getUnsubURL().setStatus(rs.getInt("status"));
                               newRatingEngine.getUnsubURL().setRatingURLCategory(urlCatg); 
                           }else{
                               //Subscription CHARGING
                               newRatingEngine.getSubscriptionURL().setRatingURLId(rs.getInt("rating_url_id"));
                               newRatingEngine.getSubscriptionURL().setRatingEngineId(rs.getInt("rating_engine_id"));
                               newRatingEngine.getSubscriptionURL().setRatingURLName(rs.getString("rating_url_name"));
                               newRatingEngine.getSubscriptionURL().setRatingURL(rs.getString("url"));
                               newRatingEngine.getSubscriptionURL().setStatus(rs.getInt("status"));
                               newRatingEngine.getSubscriptionURL().setRatingURLCategory(urlCatg); 
                           }
                        }
                        if(rs!=null) rs.close();
                        rs=null;
                    }
                    if(st!=null) st.close();
                    st=null;
 
                }catch(Exception e){
                     dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR,  logHeader+"DB Error : "+e.getMessage());
                }finally{
                    try{
                        if(conn!=null) conn.close();
                    }catch(Exception ee){}
                    conn=null;
                }
            }else{
                //Failed to connect database
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR,  logHeader+"DB Error : Failed to connect Database.");
            }
        }else{
             this.statusMsg.setMessage("Please select a valid Rating Engine to edit details!", StatusMessage.MSG_CLASS_ERROR);
        }
       
    }
    
    public void pickRatingEngineDetails(int action,RatingEngine reNew){
        Connection conn=dbConn.connect();
        //try to reconnect if not connected
        if(conn==null) conn=dbConn.connect();
        
        if(conn!=null){
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "RatingEngineBean.class :: reloadRatingEngineList() :: Database Connected.");
                try{
                }catch(Exception e){
                   dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "RatingEngineBean.class :: reloadRatingEngineList() :: Exception :" + e.getMessage());
                   statusMsg.setMessage("A process failed while Refreshing List. Database Error! ",StatusMessage.MSG_CLASS_ERROR);
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
        
        }else{
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "RatingEngineBean.class :: reloadRatingEngineList() :: Database Connectivity Failed");
                statusMsg.setMessage("A process failed while reloading service List. Database Connectivity Issue.",StatusMessage.MSG_CLASS_ERROR); 
        }
    }
    
    public void reloadRatingEngineList(int action,RatingEngine reNew){
        Connection conn=dbConn.connect();
        //try to reconnect if not connected
        if(conn==null) conn=dbConn.connect();
        
        if(conn!=null){
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "RatingEngineBean.class :: reloadRatingEngineList() :: Database Connected.");
                try{
                    conn.setAutoCommit(false);
                    if(action>0){
                        if(action==1){
                            //Add New Engine
                            String sqlInsert="insert into tb_rating_engine(engine_name,handler_class,descp,status,reg_date,"
                                    + "last_update,subs_consent_flag,event_consent_flag,vpack_consent_flag,subs_smsnotify_flag,"
                                    + "event_smsnotify_flag,vpack_smsnotify_flag) values(?,?,?,1,now(),now(),?,?,?,?,?,?);";
                            java.sql.PreparedStatement pSt=conn.prepareStatement(sqlInsert);
                            pSt.setString(1, reNew.getEngineName());
                            pSt.setString(2, reNew.getHandlerClass());
                            pSt.setString(3, reNew.getRatingEngineDescription());
                            pSt.setInt(4, reNew.isSubscriptionConsentEnabled()?1:0);
                            pSt.setInt(5, reNew.isEventConsentEnabled()?1:0);
                            pSt.setInt(6, reNew.isTopupConsentEnabled()?1:0);
                            pSt.setInt(7, reNew.isSubscriptionSMSNotifyEnabled()?1:0);
                            pSt.setInt(8, reNew.isEventSMSNotifyEnabled()?1:0);
                            pSt.setInt(9, reNew.isTopupSMSNotifyEnabled()?1:0);
                            int rep=pSt.executeUpdate();
                            if(rep>0){
                                java.sql.ResultSet rs=pSt.executeQuery("select @@IDENTITY");
                                    if(rs.next()){
                                       int ratingEngineId=rs.getInt(1);
                                       reNew.setRatingEngineId(ratingEngineId);
                                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "RatingEngineBean.class :: reloadRatingEngineList() :: Rating Engine Created. ratingEngineId :: "+ratingEngineId);  
                                    }
                                    if(rs!=null) rs.close();
                                    rs=null;
                              dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "RatingEngineBean.class :: reloadRatingEngineList() :: Rating Engine Created.");  
                              
                              reloadRatingEngineList(0,null);
                              statusMsg.setMessage("Rating Engine Created Successfully .ratingEngineId  "+reNew.getRatingEngineId(),StatusMessage.MSG_CLASS_INFO); 
                            }
                        }else if(reNew.getRatingEngineId()>0){
                            //Update Old Engine
                            
                        }
                        
                        
                    }
                    conn.setAutoCommit(true);
                    
                    java.sql.Statement st=conn.createStatement();
                    String sql1="SELECT rating_engine_id,engine_name,handler_class,descp,reg_date,last_update,"
                            + "subs_consent_flag,event_consent_flag,vpack_consent_flag,subs_smsnotify_flag,"
                            + "event_smsnotify_flag,event_smsnotify_flag,vpack_smsnotify_flag FROM tb_rating_engine WHERE tb_rating_engine.status>0 ORDER BY engine_name;";
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "RatingEngineBean.class :: reloadRatingEngineList() :: Running SQL : "+sql1);
                  
                    java.sql.ResultSet rs=st.executeQuery(sql1);
                    RatingEngine re=null;
                    
                    ratingEngineList.clear();
                    while(rs.next()){
                        re=new RatingEngine();
                        re.setRatingEngineId(rs.getInt("rating_engine_id"));
                        re.setEngineName(rs.getString("engine_name"));
                        re.setHandlerClass(rs.getString("handler_class"));
                        re.setRatingEngineDescription(rs.getString("descp"));
                        re.setRegDate(rs.getString("reg_date"));
                        re.setLastUpdate(rs.getString("last_update"));
                        re.setSubscriptionConsentEnabled(RUtil.intToBool(rs.getInt("subs_consent_flag")));
                        re.setEventConsentEnabled(RUtil.intToBool(rs.getInt("event_consent_flag")));
                        re.setTopupConsentEnabled(RUtil.intToBool(rs.getInt("vpack_consent_flag")));
                        re.setSubscriptionSMSNotifyEnabled(RUtil.intToBool(rs.getInt("subs_smsnotify_flag")));
                        re.setEventSMSNotifyEnabled(RUtil.intToBool(rs.getInt("event_smsnotify_flag")));
                        re.setTopupSMSNotifyEnabled(RUtil.intToBool(rs.getInt("event_smsnotify_flag")));
                        ratingEngineMap.put(re.getEngineName(),""+re.getRatingEngineId());
                        ratingEngineList.add(re);
                    }
                    if(rs!=null) rs.close();
                    rs=null;
                    if(st!=null) st.close();
                    st=null;
                }catch(Exception e){
                   dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "RatingEngineBean.class :: reloadRatingEngineList() :: Exception :" + e.getMessage());
                   statusMsg.setMessage("A process failed while Refreshing List. Database Error! ",StatusMessage.MSG_CLASS_ERROR);
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
        
        }else{
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "RatingEngineBean.class :: reloadRatingEngineList() :: Database Connectivity Failed");
                statusMsg.setMessage("A process failed while reloading service List. Database Connectivity Issue.",StatusMessage.MSG_CLASS_ERROR); 
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

    public void handleToggle(ToggleEvent event) {
        System.out.println("Toggle Event Invoked!");
        //System.out.println("Buttong Name , Visibility "+event.getVisibility());
        //FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Toggled", "Visibility:" + event.getVisibility());
        //FacesContext.getCurrentInstance().addMessage(null, msg);
    }

    public String getRatingEngineIdToEdit() {
        return ratingEngineIdToEdit;
    }

    public void setRatingEngineIdToEdit(String ratingEngineIdToEdit) {
        this.ratingEngineIdToEdit = ratingEngineIdToEdit;
    }

    public Map<String, String> getRatingEngineMap() {
        return ratingEngineMap;
    }

    public void setRatingEngineMap(Map<String, String> ratingEngineMap) {
        this.ratingEngineMap = ratingEngineMap;
    }

    public boolean isCreateNewActionFlag() {
        return createNewActionFlag;
    }

    public void setCreateNewActionFlag(boolean createNewActionFlag) {
        this.createNewActionFlag = createNewActionFlag;
    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
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

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    
    public RatingEngine getNewRatingEngine() {
        return newRatingEngine;
    }

    public void setNewRatingEngine(RatingEngine newRatingEngine) {
        this.newRatingEngine = newRatingEngine;
    }

    public RatingEngine getSelectedRatingEngine() {
        return selectedRatingEngine;
    }

    public void setSelectedRatingEngine(RatingEngine selectedRatingEngine) {
        this.selectedRatingEngine = selectedRatingEngine;
    }

    public List<RatingEngine> getRatingEngineList() {
        return ratingEngineList;
    }

    public void setRatingEngineList(List<RatingEngine> ratingEngineList) {
        this.ratingEngineList = ratingEngineList;
    }

}
