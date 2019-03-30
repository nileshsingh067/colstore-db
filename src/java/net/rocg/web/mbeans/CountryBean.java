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
public class CountryBean implements java.io.Serializable {

    DBConnection dbConn;
    String newCountryName;
    Country countryObject;
    StatusMessage statusMsg;
    List<Country> countryList;

    /**
     * Creates a new instance of CountryBean
     */
    public CountryBean() {
        statusMsg = new StatusMessage();
        countryObject = new Country();
        dbConn = new DBConnection();
        countryList = new ArrayList<>();
        refreshCountryList(null, 0);
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CountryBean.class :: MBean Connected.");
    }

    public void reloadList() {
        //refreshCountryList(null, 0);
    }

    public void refreshCountryList(Country updateObj, int action) {
        Connection conn = dbConn.connect();
        if (conn != null) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CountryBean.class :: refreshCountryList() :: Database Connected.");
            try {
                java.sql.Statement st = conn.createStatement();
                String sqlA = "";
                int dbRep = 0;
                if (action == 2 && updateObj != null && updateObj.getId() > 0) {
                    //Update Existing Country Name
                    sqlA = "update tb_country set country_name='" + updateObj.getCountryName() + "', status=" + updateObj.getStatus() + " where country_id=" + updateObj.getId() + ";";
                    dbRep = st.executeUpdate(sqlA);
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CountryBean.class :: refreshCountryList() :: Action Requested=UPDATE-COUNTRY; SQL : " + sqlA + "| DB Result=" + dbRep);
                } else if (action == 1 && updateObj != null && updateObj.getCountryName() != null && updateObj.getCountryName().length() >= 3) {
                    //Create new Country
                    sqlA = "insert into tb_country(country_name) values('" + updateObj.getCountryName() + "');";
                    dbRep = st.executeUpdate(sqlA);
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CountryBean.class :: refreshCountryList() :: Action Requested=REGISTER-NEW-COUNTRY; SQL : " + sqlA + "| DB Result=" + dbRep);
                } else {

                    countryList.clear();

                    String sql1 = "SELECT country_id,country_name,status FROM tb_country order by status desc,country_name asc;";
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CountryBean.class :: refreshCountryList() :: REFRESH COUNTRY SQL : " + sql1);
                    java.sql.ResultSet rs = st.executeQuery(sql1);
                    Country newCountry = null;
                    while (rs.next()) {
                        newCountry = new Country();
                        newCountry.setId(rs.getInt("country_id"));
                        newCountry.setCountryName(rs.getString("country_name"));
                        newCountry.setStatus(rs.getInt("status"));
                        countryList.add(newCountry);
                        newCountry = null;
                    }
                    rs.close();
                    rs = null;
                    
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CountryBean.class :: refreshCountryList() :: Country List (ItemCount=" + countryList.size() + ") Reloaded. ");
                }
                st.close();
                st = null;
            } catch (Exception e) {
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CountryBean.class :: refreshCountryList() :: Exception :" + e.getMessage());
                statusMsg.setMessage("A process failed while reloading Country List!", StatusMessage.MSG_CLASS_ERROR);
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
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CountryBean.class :: refreshCountryList() :: Database Connectivity Failed");
            statusMsg.setMessage("A process failed while reloading Country List. Database Connectivity Issue.", StatusMessage.MSG_CLASS_ERROR);
        }

    }

    /**
     * Edit Event Handler Method
     */
    public void onEdit(RowEditEvent event) {
        Country newCountry = (Country) event.getObject();
        String newCountryName = newCountry.getCountryName();
        newCountryName = (newCountryName == null) ? "" : newCountryName.trim();
        newCountry.setCountryName(newCountryName);
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CountryBean.class :: onEdit() :: Update Country '" + newCountryName + "' at ID " + newCountryName);
         String newCountry1=StringInputValidator.validateString(newCountryName, "<>&@#$!`'^*?/=");
        if (newCountry != null && newCountry.getId() > 0 && newCountry1.equalsIgnoreCase(newCountryName)) {
            statusMsg.setMessage("Country Details sent for update into Registry!", StatusMessage.MSG_CLASS_INFO);
            refreshCountryList(newCountry, 2);
        } else {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CountryBean.class :: onEdit() :: Invalid Country Id (" + countryObject.getId() + ").");
            statusMsg.setMessage("Invalid Country Id `" + countryObject.getId() + "` or Country Name. Please select a country to update.", StatusMessage.MSG_CLASS_ERROR);
        }

    }

    /**
     * Method will create new Country into database based on the details
     * provided in the countryObject, If not created the statusMsg
     */
    public void createNew() {
        newCountryName = (newCountryName == null) ? "" : newCountryName.trim();
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CountryBean.class :: createNew() :: New Country Name to Register '" + newCountryName + "'");
        String newCountry=StringInputValidator.validateString(newCountryName, "<>&@#$!`'^*?/=");
        if (newCountry.equalsIgnoreCase(newCountryName) && newCountryName.length() >= 3 && newCountryName.length() <= 50) {
            Country newCountryObj = new Country();
            newCountryObj.setCountryName(newCountryName);
            statusMsg.setMessage("New Country Name sent for registration!", StatusMessage.MSG_CLASS_INFO);
            refreshCountryList(newCountryObj, 1);
        } else {
            //Invalid Country Name
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CountryBean.class :: createNew() :: Invalid Country Name `" + countryObject.getCountryName() + "`. Country Name must have number of charactesrs more then 3 and less then 50.");
                statusMsg.setMessage("Invalid Country Name `" + countryObject.getCountryName() + "`. Country Name shall have number of charactesrs more then 3 and less then 50.", StatusMessage.MSG_CLASS_ERROR);
            
        }

    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }

    public Country getCountryObject() {
        return countryObject;
    }

    public void setCountryObject(Country countryObject) {
        this.countryObject = countryObject;
    }

    public List<Country> getCountryList() {
        return countryList;
    }

    public void setCountryList(List<Country> countryList) {
        this.countryList = countryList;
    }

    public String getNewCountryName() {
        return newCountryName;
    }

    public void setNewCountryName(String newCountryName) {
        this.newCountryName = newCountryName;
    }

}
