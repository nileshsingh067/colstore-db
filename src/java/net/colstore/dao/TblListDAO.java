/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.colstore.dao;

import java.io.Serializable;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import net.colstore.util.DBConnection;
import net.colstore.util.RLogger;
import net.colstore.web.model.DbList;
import net.colstore.web.model.TableList;

/**
 *
 * @author nilesh
 */
public class TblListDAO extends DBConnection implements Serializable{
    
    
    public TblListDAO(){}
    
    public List<TableList> getTblList(int dbId){
        List<TableList> tableList=new ArrayList<>();
        Connection conn=connect();
        String sql1="select id,db_id,tbl_name,status,no_of_col,created_on,last_update from db_tbllist where db_id="+dbId+";";
        TableList newtable=null;
        if(conn!=null){
            try{
                
                java.sql.ResultSet rs=conn.createStatement().executeQuery(sql1);
                while(rs.next()){
                    newtable=new TableList();
                    newtable.setId(rs.getInt("id"));
                    newtable.setDb_id(rs.getInt("db_id"));
                    newtable.setTbl_name(rs.getString("tbl_name"));
                    newtable.setStatus(rs.getInt("status"));
                    newtable.setNo_of_col(rs.getInt("no_of_col"));
                    newtable.setCreated_on(rs.getString("created_on"));
                    newtable.setLast_update(rs.getString("last_update"));
                    tableList.add(newtable);
                    newtable=null;
                }
                if(rs!=null)rs=null;
                 logMsg(RLogger.MSG_TYPE_INFO,RLogger.LOGGING_LEVEL_INFO,"TblListDAO.class :: getTblList() :: sql : "+sql1+", Found Count "+tableList.size());
            }catch(Exception e){
                
                logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"TblListDAO.class :: getTblList() :: sql : "+sql1+", Exception "+e);
            }finally{
                try{
                    if(conn!=null) conn.close();
                }catch(Exception ee){}
                conn=null;
            }
        }else{
            
            logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"TblListDAO.class :: getTblList() :: sql : "+sql1+", Failed to connect Database.");
        }
        return tableList;
    }
    public TableList getTblDetails(String tableName){
        List<TableList> tableList=new ArrayList<>();
        Connection conn=connect();
        String sql1="select id,db_id,tbl_name,status,no_of_col,created_on,last_update from db_tbllist where tbl_name='"+tableName+"';";
        TableList newtable=new TableList();
        if(conn!=null){
            try{
                
                java.sql.ResultSet rs=conn.createStatement().executeQuery(sql1);
                if(rs.next()){
                    newtable.setId(rs.getInt("id"));
                    newtable.setDb_id(rs.getInt("db_id"));
                    newtable.setTbl_name(rs.getString("tbl_name"));
                    newtable.setStatus(rs.getInt("status"));
                    newtable.setNo_of_col(rs.getInt("no_of_col"));
                    newtable.setCreated_on(rs.getString("created_on"));
                    newtable.setLast_update(rs.getString("last_update"));
                }
                if(rs!=null)rs=null;
                 logMsg(RLogger.MSG_TYPE_INFO,RLogger.LOGGING_LEVEL_INFO,"TblListDAO.class :: getTblDetails() :: sql : "+sql1+", Found Count "+newtable);
            }catch(Exception e){
                
                logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"TblListDAO.class :: getTblDetails() :: sql : "+sql1+", Exception "+e);
            }finally{
                try{
                    if(conn!=null) conn.close();
                }catch(Exception ee){}
                conn=null;
            }
        }else{
            
            logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"TblListDAO.class :: getTblDetails() :: sql : "+sql1+", Failed to connect Database.");
        }
        return newtable;
    }
    public boolean createNewTable(TableList newTable){
         boolean flag=false; 
         int res=0;
         Connection conn=connect();
        String sql1="insert into db_tbllist(db_id,tbl_name,status,no_of_col,created_on,last_update) values("+newTable.getDb_id()+",'"+newTable.getTbl_name()+"',1,"+newTable.getNo_of_col()+",now(),now());";
        if(conn!=null){
            try{
                res=conn.createStatement().executeUpdate(sql1);
                 logMsg(RLogger.MSG_TYPE_INFO,RLogger.LOGGING_LEVEL_INFO,"TblListDAO.class :: createNewTable() :: sql : "+sql1+", result : "+flag);
            }catch(Exception e){
                logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"TblListDAO.class :: createNewTable() :: sql : "+sql1+", Exception "+e);
            }finally{
                try{
                    if(conn!=null) conn.close();
                }catch(Exception ee){}
                conn=null;
            }
        }else{
            logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"TblListDAO.class :: createNewTable() :: sql : "+sql1+", Failed to connect Database.");
        }
         return res>0?true:false;
     }
    
}
