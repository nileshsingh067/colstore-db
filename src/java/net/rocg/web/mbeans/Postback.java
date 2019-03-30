
package net.rocg.web.mbeans;


import com.database.jdbc.util.DBHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.StatusMessage;
import org.primefaces.event.RowEditEvent;

/**
 *
 * @author jiffy
 */
@ManagedBean
@SessionScoped
public class Postback {

    DBHandler dbHandler = new DBHandler("java:comp/env/jiffycmsdb", true);
    private int id, cap, percent;
    int loginId, roleId;
    String loginName,selectedOperatorId;
    private String name;
    List<NetworkList> listRecord;
    Map<String,String> operatorList;
    NetworkList newRecord;
    String msg="record found";
    DBConnection dbConn;

    public Postback() {
       // getNetworkList();
        dbConn=new DBConnection();
        operatorList=new HashMap<String,String>();
        fetchLoginDetails();
        fatchdata(true);
    }

    
    public void fatchdata(boolean reloadOperatorList){
        java.sql.Connection conn=dbConn.connect();
        if(conn!=null){
          dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalHomeContentSchedulingBean.class :: fetchData() :: Database Connected.");
            try{
                java.sql.Statement st=conn.createStatement();
                if(reloadOperatorList) {
                  this.reloadOperatorList(st);  
                
                }
                    
                }catch(Exception e){
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalHomeContentSchedulingBean.class :: fetchData() :: Exception "+e);
            }finally{
                try{if(conn!=null) conn.close();}catch(Exception ee){}
                conn=null;
            }
        }
        else{
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalHomeContentSchedulingBean.class :: fetchData() :: Database Connectivity Failed");
            //statusMsg.setMessage("A process failed while reloading Country List. Database Connectivity Issue.", StatusMessage.MSG_CLASS_ERROR);
        }
            
        
    }
    public void getNetworkList() {
        String Sql="";
        Sql = "SELECT ad_network_id,network_name,cap,percent FROM tb_adnetworks";
        if(this.getSelectedOperatorId().equals("10")){
         Sql = "SELECT ad_network_id,network_name,cap,percent FROM db_dialog.tb_adnetworks";
        }
        listRecord = new ArrayList<NetworkList>();
        listRecord.clear();
        ArrayList<String[]> list = dbHandler.getDataQuery(Sql, 100);
        for (String[] data : list) {
            newRecord = new NetworkList();
            newRecord.setId(data[0]);
           // System.out.println(data[0]);
            newRecord.setName(data[1]);
           // System.out.println(data[1]);
            newRecord.setCap(data[2]);
           // System.out.println(data[2]);
            newRecord.setPercent(data[3]);
           // System.out.println(data[3]);
            listRecord.add(newRecord);
            newRecord = null;

        }

    }
public void reloadOperatorList(java.sql.Statement st) {
        try {
            String sql1="";
            //sql1="SELECT DISTINCT a.operator_id,CONCAT(c.`country_name`,\"-\",b.operator_name) AS operator_name FROM tb_rating_operator_configurations a , tb_operators b,tb_country c WHERE a.`operator_id`=b.`operator_id` AND b.`country_id`=c.`country_id`;";
            sql1="select operator_id,operator_name from tb_operators;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalHomeContentSchedulingBean.class :: reloadOperatorList() :: Query : "+sql1);
            java.sql.ResultSet rs = st.executeQuery(sql1);
            operatorList.clear();
            while (rs.next()) {
               
                operatorList.put(rs.getString("operator_name"),rs.getString("operator_id"));
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "PortalHomeContentSchedulingBean.class :: reloadOperatorList() ::  Collection Size "+operatorList.size());
            if (rs != null) {
                rs.close();
            }
            rs = null;
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalHomeContentSchedulingBean.class :: reloadOperatorList() :: Exception "+e.getMessage());
           
        }
    }


    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCap() {
        return cap;
    }

    public void setCap(int cap) {
        this.cap = cap;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<NetworkList> getListRecord() {
        return listRecord;
    }

    public void setListRecord(List<NetworkList> listRecord) {
        this.listRecord = listRecord;
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

    public String getSelectedOperatorId() {
        return selectedOperatorId;
    }

    public void setSelectedOperatorId(String selectedOperatorId) {
        this.selectedOperatorId = selectedOperatorId;
    }

    public Map<String, String> getOperatorList() {
        return operatorList;
    }

    public void setOperatorList(Map<String, String> operatorList) {
        this.operatorList = operatorList;
    }
    
    
    public void onRowEdit(RowEditEvent event) {
        String id=((NetworkList) event.getObject()).getId();
        String cap=((NetworkList) event.getObject()).getCap();
        String per=((NetworkList) event.getObject()).getPercent();
        String name=((NetworkList) event.getObject()).getName();
        String Sql = "update tb_adnetworks set cap="+cap+",percent="+per+",updated_on=now(),updated_by='"+this.getLoginName()+"' where ad_network_id="+id;
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG," onRowEdit() :: selectedAdnetwork id :: "+this.getSelectedOperatorId());   
        if(this.getSelectedOperatorId().equals("10")){
         Sql = "update db_dialog.tb_adnetworks set cap="+cap+",percent="+per+",updated_on=now(),updated_by='"+this.getLoginName()+"' where ad_network_id="+id;
        }
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG," onRowEdit() :: update Query :: "+Sql);   
        int i=dbHandler.getUpdateQuery(Sql);
        if(i==1)
        {
            System.out.println("Adnetwork "+name+" updated successfully with caping="+cap+" and percent="+per);
            msg="SUSCESSFULL UPDATE";
        }
        else
        {
            System.out.println("ERROR WHILE UPDATING");
            msg="ERROR WHILE UPDATING";
        }
    }

    public void onRowCancel(RowEditEvent event) {
        FacesMessage msg = new FacesMessage("Edit Cancelled", ((NetworkList) event.getObject()).getId());
        FacesContext.getCurrentInstance().addMessage(null, msg);
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
           // dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: fetchLoginDetails() :: Exception "+e.getMessage());
        }
        loginBeanObj = null;

    }
    public class NetworkList {

        public String id;
        public String name;
        public String cap;
        public String percent;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCap() {
            return cap;
        }

        public void setCap(String cap) {
            this.cap = cap;
        }

        public String getPercent() {
            return percent;
        }

        public void setPercent(String percent) {
            this.percent = percent;
        }

    }
}
