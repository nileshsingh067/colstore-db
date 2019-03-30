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
import net.rocg.web.beans.Country;
import net.rocg.web.beans.Operator;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author Rishi Tyagi
 */
@ManagedBean
@RequestScoped
public class OperatorBean  implements java.io.Serializable{
    DBConnection dbConn;
    Map<String,String> countryList;
    String selectedCountry;
    
    String newOperatorName;
    
    List<Operator> operatorList;
    Operator selectedOperator;
    StatusMessage statusMsg;
    /**
     * Creates a new instance of OperatorBean
     */
    public OperatorBean() {
         countryList=new HashMap<>();
         statusMsg=new StatusMessage();
         dbConn=new DBConnection();
         selectedOperator=new Operator();
         operatorList=new ArrayList<>();
         refreshOperatorList(null,0,true);
         dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorBean.class :: MBean Connected.");
    }

     public void reloadList() {
       //Dpo Nothing as reload is done in constructor
    }

    public void refreshOperatorList(Operator updateObj,int action,boolean refreshCountryList){
        Connection conn=dbConn.connect();
              
        if(conn!=null){
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorBean.class :: refreshOperatorList() :: Database Connected.");
                try{
                    java.sql.Statement st=conn.createStatement();
                    String sqlA="";int dbRep=0;
                    if(action==2 && updateObj!=null && updateObj.getOperatorId()>0){
                        //Update Existing Operator Name
                        sqlA="update tb_operators set operator_name='"+updateObj.getOperatorName()+"', status="+updateObj.getStatus()+" where operator_id="+updateObj.getOperatorId()+";";
                        dbRep=st.executeUpdate(sqlA);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorBean.class :: refreshOperatorList() :: Action Requested=UPDATE-OPERATOR; SQL : " + sqlA + "| DB Result=" + dbRep);
                    }else if(action==1 && updateObj!=null && updateObj.getCountryId()>0 && updateObj.getOperatorName().length()>3){
                        //Create new Operator
                        sqlA="insert into tb_operators(operator_name,country_id,status) values('"+updateObj.getOperatorName()+"',"+updateObj.getCountryId()+",1);";
                        dbRep=st.executeUpdate(sqlA);
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorBean.class :: refreshOperatorList() :: Action Requested=REGISTER-NEW-OPERATOR; SQL : " + sqlA + "| DB Result=" + dbRep);
                    }else{

                        String sql1="SELECT country_id,country_name FROM tb_country order by country_name;";
                        java.sql.ResultSet rs=null;
                        if(refreshCountryList){
                            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorBean.class :: refreshOperatorList() :: REFRESH COUNTRY SQL : " + sql1);
                                countryList.clear();
                                rs=st.executeQuery(sql1);
                                while(rs.next()){
                                    countryList.put(rs.getString("country_name"), rs.getString("country_id"));
                                }
                                rs.close();
                                rs=null;
                                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorBean.class :: refreshOperatorList() ::  Country List (ItemCount=" + countryList.size() + ") Reloaded. ");
                        }

                        operatorList.clear();
                        sql1="SELECT O.operator_id,O.country_id,C.country_name,O.operator_name,O.status FROM tb_operators O, tb_country C WHERE O.country_id=C.`country_id` ORDER BY O.`status` DESC, O.`operator_name` ASC;";
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorBean.class :: refreshOperatorList() :: REFRESH OPERATOR SQL : " + sql1);
                        rs=st.executeQuery(sql1);
                        Operator newOperator=null;
                        while(rs.next()){
                            newOperator=new Operator();
                            newOperator.setOperatorId(rs.getInt("operator_id"));
                            newOperator.setCountryId(rs.getInt("country_id"));
                            newOperator.setCountryName(rs.getString("country_name"));
                            newOperator.setOperatorName(rs.getString("operator_name"));
                            newOperator.setStatus(rs.getInt("status"));
                            operatorList.add(newOperator);
                            newOperator=null;
                        }
                        rs.close();
                        rs=null;
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorBean.class :: refreshOperatorList() ::  Operator List (ItemCount=" + operatorList.size() + ") Reloaded. ");
                    }
                    st.close();
                    st=null;
                }catch(Exception e){
                   dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "OperatorBean.class :: refreshOperatorList() :: Exception :" + e.getMessage());
                    statusMsg.setMessage("A process failed while reloading Country List. Database Error!",StatusMessage.MSG_CLASS_ERROR);
                }finally{
                    try{if(conn!=null) conn.close();}catch(Exception ee){}
                    conn=null;
                }
            }else{
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "OperatorBean.class :: refreshOperatorList() :: Database Connectivity Failed");
                statusMsg.setMessage("A process failed while reloading Country List. Database Connectivity Issue.",StatusMessage.MSG_CLASS_ERROR);
            }

    }
     /**
      * Edit Event Handler Method
      */
     public void onEdit(RowEditEvent event) {  
       Operator newOperator=(Operator)event.getObject();
       String newOperatorName=newOperator.getOperatorName();
       newOperatorName=(newOperatorName==null)?"":newOperatorName.trim();
       newOperator.setOperatorName(newOperatorName);
       dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorBean.class :: onEdit() :: Update Operator '" + newOperatorName + "', Status '"+newOperator.getStatus()+"' at ID " + newOperator.getOperatorId());

       if(newOperator!=null && newOperator.getOperatorId()>0 && (newOperatorName.length()>=3 || newOperatorName.length()<=50) ){
           statusMsg.setMessage("Operator Details sent for update into Registry!", StatusMessage.MSG_CLASS_INFO);
           refreshOperatorList(newOperator,2,false);
       }else{
          dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "OperatorBean.class :: onEdit() :: Invalid Operator Id (" +  newOperator.getOperatorId() + ") or Operator Name ("+newOperatorName+") is not valid. Operator Name must be between 3-50 char in length.");
          statusMsg.setMessage("Invalid Operator Id `" + newOperator.getOperatorId() + "` or Operator Name `"+newOperator.getOperatorName()+"`. Please try again.", StatusMessage.MSG_CLASS_ERROR); 
       }
       
    }  
    public void createNew(){
       int selCountryId=RUtil.strToInt(selectedCountry, 0);
       newOperatorName=(newOperatorName==null)?"":newOperatorName.trim();
       dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "OperatorBean.class :: createNew() :: New Operator Name to Register '" + newOperatorName + "' against country id "+selCountryId);

       StringBuilder logMsg=new StringBuilder();
       String newOperatorV=StringInputValidator.validateString(newOperatorName, "<>&@#$!`'^*?/=");
        if(!newOperatorV.equalsIgnoreCase(newOperatorName)){
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "OperatorBean.class :: createNew() :: Invalid Circle Name ");
           statusMsg.setMessage("Sorry! Invalid Input. Special Characters are not allowed.",StatusMessage.MSG_CLASS_ERROR);
       }else if(selCountryId<=0){
           //Invalid Country Id
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "OperatorBean.class :: createNew() :: Invalid Country Id `" + selCountryId + "`. Cant not register new operator witout the valid country id.");
            statusMsg.setMessage("Can't register new Operator. Invalid Country Id!");
       }else if(newOperatorName.length()<=3 || newOperatorName.length()>50){
           //Invalid Operator Name
           dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "OperatorBean.class :: createNew() :: Invalid Operator Name `" + newOperatorName + "`. Operator Name must be between 3-50 char in length.");
           statusMsg.setMessage("Can't register new Operator. Invalid Operator Name. Must be between 3-50 character length!");
       }else{
          Operator newOperator=new Operator();
          newOperator.setCountryId(selCountryId);
          newOperator.setOperatorName(newOperatorName);
          statusMsg.setMessage("New Operator Name sent for registration!", StatusMessage.MSG_CLASS_INFO);
          refreshOperatorList(newOperator,1,false); 
         
          
       }
       
       
    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }
    
    public String getNewOperatorName() {
        return newOperatorName;
    }

    public void setNewOperatorName(String newOperatorName) {
        this.newOperatorName = newOperatorName;
    }

    public List<Operator> getOperatorList() {
        return operatorList;
    }

    public void setOperatorList(List<Operator> operatorList) {
        this.operatorList = operatorList;
    }

    public Operator getSelectedOperator() {
        return selectedOperator;
    }

    public void setSelectedOperator(Operator selectedOperator) {
        this.selectedOperator = selectedOperator;
    }

    public Map<String, String> getCountryList() {
        return countryList;
    }

    public void setCountryList(Map<String, String> countryList) {
        this.countryList = countryList;
    }

    public String getSelectedCountry() {
        return selectedCountry;
    }

    public void setSelectedCountry(String selectedCountry) {
        this.selectedCountry = selectedCountry;
    }

  
}
