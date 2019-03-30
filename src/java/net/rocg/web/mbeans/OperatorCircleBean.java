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
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import net.rocg.util.StringInputValidator;
import net.rocg.web.beans.OperatorCircle;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author Rishi Tyagi
 */
@ManagedBean
@RequestScoped
public class OperatorCircleBean  implements java.io.Serializable{
    DBConnection dbConn;
    Map<String,String> operatorList;
    String selectedOperator;
    
    String newCircleName,newLrn;
    
    List<OperatorCircle> operatorCircleList;
    OperatorCircle selectedOperatorCircle;
    StatusMessage statusMsg;
    /**
     * Creates a new instance of OperatorCircleBean
     */
    public OperatorCircleBean() {
        operatorList=new HashMap<>();
        dbConn=new DBConnection();
        operatorCircleList=new ArrayList<>();
        selectedOperatorCircle=new OperatorCircle();
        statusMsg=new StatusMessage();
        refreshOperatorList(null,0,true);
         dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorCircleBean.class :: MBean Connected.");
    }
    
    public void reloadList() {
        //refreshCountryList(null, 0);
    }
    
    public void refreshOperatorList(OperatorCircle updateObj,int action,boolean refreshOperatorList){
        Connection conn=dbConn.connect();
        StringBuilder logMsg=new StringBuilder();
        
        if(conn!=null){
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorCircleBean.class :: refreshOperatorList() :: Database Connected.");
                try{
                    java.sql.Statement st=conn.createStatement();
                    String sqlA="";int dbRep=0;
                    if(action==2 && updateObj!=null && updateObj.getCircleId()>0){
                        //Update Existing Operator Name
                        sqlA="update tb_operator_circles set circle_name='"+updateObj.getCircleName()+"', status="+updateObj.getStatus()+",lrn='"+updateObj.getLrn()+"' where circle_id="+updateObj.getCircleId()+";";
                        dbRep=st.executeUpdate(sqlA);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorCircleBean.class :: refreshOperatorList() :: Action Requested=UPDATE-OPCIRCLE; SQL : " + sqlA + "| DB Result=" + dbRep);
                    }else if(action==1 && updateObj!=null && updateObj.getOperatorId()>0 && updateObj.getCircleName().length()>3){
                        //Create new Operator
                        sqlA="insert into tb_operator_circles(circle_name,operator_id,status,lrn) values('"+updateObj.getCircleName()+"',"+updateObj.getOperatorId()+",1,'"+updateObj.getLrn()+"');";
                        dbRep=st.executeUpdate(sqlA);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorCircleBean.class :: refreshOperatorList() :: Action Requested=REGISTER-NEW-OPCIRCLE; SQL : " + sqlA + "| DB Result=" + dbRep);
                    }else{
                    
                        String sql1="SELECT O.operator_id,C.country_name,O.operator_name FROM tb_operators O, tb_country C WHERE O.`country_id`=C.`country_id` ORDER BY C.`country_name`,O.`operator_name`;";

                        java.sql.ResultSet rs=null;
                        if(refreshOperatorList){
                                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorCircleBean.class :: refreshOperatorList() :: REFRESH OPERATOR SQL : " + sql1);
                                operatorList.clear();
                                rs=st.executeQuery(sql1);
                                while(rs.next()){
                                    operatorList.put(rs.getString("country_name")+"::"+rs.getString("operator_name"), rs.getString("operator_id"));
                                }
                                rs.close();
                                rs=null;
                                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorCircleBean.class :: refreshOperatorList() :: Operator List (ItemCount=" + operatorList.size() + ") Reloaded. ");
                        }

                        operatorCircleList.clear();
                        sql1="SELECT CR.circle_id,O.country_id,C.country_name,CR.operator_id,O.operator_name,CR.circle_name,CR.status,CR.lrn FROM tb_operators O, tb_country C,tb_operator_circles CR WHERE CR.operator_id=O.`operator_id` AND O.country_id=C.`country_id`  ORDER BY CR.`status` DESC, C.`country_name` ASC,O.`operator_name` ASC,CR.`circle_name` ASC;";
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorCircleBean.class :: refreshOperatorList() :: REFRESH OPERATOR CIRCLE SQL : " + sql1);
                        rs=st.executeQuery(sql1);
                        OperatorCircle newCircle=null;
                        while(rs.next()){
                            newCircle=new OperatorCircle();
                            newCircle.setCircleId(rs.getInt("circle_id"));
                            newCircle.setCountryId(rs.getInt("country_id"));
                            newCircle.setCountryName(rs.getString("country_name"));
                            newCircle.setOperatorId(rs.getInt("operator_id"));
                            newCircle.setOperatorName(rs.getString("operator_name"));
                            newCircle.setCircleName(rs.getString("circle_name"));
                            newCircle.setStatus(rs.getInt("status"));
                            newCircle.setLrn(rs.getString("lrn"));
                            operatorCircleList.add(newCircle);
                            newCircle=null;
                        }
                        rs.close();
                        rs=null;
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorCircleBean.class :: refreshOperatorList() :: Operator Circle List (ItemCount=" + operatorCircleList.size() + ") Reloaded. ");
                    }
                    st.close();
                    st=null;
                }catch(Exception e){
                    dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "OperatorCircleBean.class :: refreshOperatorList() :: Exception :" + e.getMessage());
                    statusMsg.setMessage("A process failed while reloading Operator Circle List. Database Error!",StatusMessage.MSG_CLASS_ERROR);
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
            }else{
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "OperatorCircleBean.class :: refreshOperatorList() :: Database Connectivity Failed");
                statusMsg.setMessage("A process failed while reloading Operator Circle List. Database Connectivity Issue.",StatusMessage.MSG_CLASS_ERROR);
            }

    }
    
     /**
      * Edit Event Handler Method
      */
     public void onEdit(RowEditEvent event) {  
       OperatorCircle newCircle=(OperatorCircle)event.getObject();
       int circleId=(newCircle==null)?0:newCircle.getCircleId();
       if(circleId>0) {
           statusMsg.setMessage("Circle Details sent for update into Registry!", StatusMessage.MSG_CLASS_INFO);
           refreshOperatorList(newCircle,2,false);
       }else{
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "OperatorCircleBean.class :: onEdit() :: Invalid Circle Id (" + circleId + ").");
            statusMsg.setMessage("Invalid Circle Id `" +circleId + "`. Please select a Circle to update.", StatusMessage.MSG_CLASS_ERROR);
       }
       
    }  
    public void createNew(){
       int selOperatorId=RUtil.strToInt(selectedOperator, 0);
       newCircleName=(newCircleName==null)?"":newCircleName.trim();
       newLrn=(newLrn==null)?"":newLrn.trim();
       
       dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorCircleBean.class :: createNew() :: New Circle Name to Register '" + newCircleName + "', LRN '"+newLrn+"' against Operator Id "+selOperatorId);

       StringBuilder logMsg=new StringBuilder();
       String newCircleV=StringInputValidator.validateString(newCircleName, "<>&@#$!`'^*?/=");
        if(!newCircleV.equalsIgnoreCase(newCircleName)){
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CountryBean.class :: createNew() :: Invalid Circle Name ");
           statusMsg.setMessage("Sorry! Invalid Input. Special Characters are not allowed.",StatusMessage.MSG_CLASS_ERROR);
       }else if(selOperatorId<=0){
           //Invalid Operator Id
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CountryBean.class :: createNew() :: Invalid Operator Id `" + selOperatorId + "`. Can't register new Circle.");
           statusMsg.setMessage("Can't register new Operator Circle. Invalid Operator Id!");
       }else if(newCircleName.length()<=3 || newCircleName.length()>50){
           //Invalid Circle Name
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CountryBean.class :: createNew() :: Invalid Circle Name `" + newCircleName + "`. Circle Name must be between 3-50 char in length.");
           statusMsg.setMessage("Can't register new Operator Circle. Invalid Circle Name. Must be between 3-50 character length!");
       }else{
          OperatorCircle newCircle=new OperatorCircle();
          newCircle.setOperatorId(selOperatorId);
          newCircle.setCircleName(newCircleName);
          newCircle.setLrn(newLrn);
          statusMsg.setMessage("New Circle Name '"+newCircleName+"',LRN '"+newCircle.getLrn()+"', OperatorId '"+selOperatorId+"' sent for Registration!");
          refreshOperatorList(newCircle,1,false); 
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

    public String getNewCircleName() {
        return newCircleName;
    }

    public void setNewCircleName(String newCircleName) {
        this.newCircleName = newCircleName;
    }

    public String getNewLrn() {
        return newLrn;
    }

    public void setNewLrn(String newLrn) {
        this.newLrn = newLrn;
    }

    public List<OperatorCircle> getOperatorCircleList() {
        return operatorCircleList;
    }

    public void setOperatorCircleList(List<OperatorCircle> operatorCircleList) {
        this.operatorCircleList = operatorCircleList;
    }

    public OperatorCircle getSelectedOperatorCircle() {
        return selectedOperatorCircle;
    }

    public void setSelectedOperatorCircle(OperatorCircle selectedOperatorCircle) {
        this.selectedOperatorCircle = selectedOperatorCircle;
    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }

    
    
}
