/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.rocg.web.mbeans;

import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import net.rocg.util.DBConnection;
import net.rocg.web.beans.DashboardItem;

/**
 *
 * @author Rishi Tyagi
 */
@ManagedBean
@RequestScoped
public class CMSDashboard {
    
    DBConnection dbConn;
    DashboardItem selectedItem;
    List<DashboardItem> dashboardItems;
    /**
     * Creates a new instance of CMSDashboard
     */
    public CMSDashboard() {
        dashboardItems=new ArrayList<DashboardItem>();
        dbConn=new DBConnection();
    }

    public void refreshData(){
        java.sql.Connection conn=dbConn.connect();
        if(conn!=null) conn=dbConn.connect();
        if(conn!=null){
            try{
            java.sql.Statement st=conn.createStatement();
            String sql1="";
            
            }catch(Exception e){
            
            }finally{
                try{if(conn==null) conn.close();}catch(Exception e){}
                conn=null;
            }
        }else{
            
        }
    }
    
    public List<DashboardItem> getDashboardItems() {
        return dashboardItems;
    }

    public void setDashboardItems(List<DashboardItem> dashboardItems) {
        this.dashboardItems = dashboardItems;
    }
    
}
