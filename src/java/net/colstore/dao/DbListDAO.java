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

/**
 *
 * @author nilesh
 */
public class DbListDAO extends DBConnection implements Serializable{
    
    
    public DbListDAO(){}
     public List<DbList> getDbList(int userId){
        List<DbList> dbList=new ArrayList<>();
        Connection conn=connect();
        String sql1="select id,user_id,db_name,status,created_on,last_update from tb_dblist where user_id="+userId+";";
        DbList newDb=null;
        if(conn!=null){
            try{
                
                java.sql.ResultSet rs=conn.createStatement().executeQuery(sql1);
                while(rs.next()){
                    newDb=new DbList();
                    newDb.setId(rs.getInt("id"));
                    newDb.setUser_id(rs.getInt("user_id"));
                    newDb.setDb_name(rs.getString("db_name"));
                    newDb.setStatus(rs.getInt("status"));
                    newDb.setCreated_on(rs.getString("created_on"));
                    newDb.setLast_update(rs.getString("last_update"));
                    dbList.add(newDb);
                    newDb=null;
                }
                if(rs!=null)rs=null;
                 logMsg(RLogger.MSG_TYPE_INFO,RLogger.LOGGING_LEVEL_INFO,"DbListDAO.class :: getDbList() :: sql : "+sql1+", Found Count "+dbList.size());
            }catch(Exception e){
                
                logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"DbListDAO.class :: getDbList() :: sql : "+sql1+", Exception "+e);
            }finally{
                try{
                    if(conn!=null) conn.close();
                }catch(Exception ee){}
                conn=null;
            }
        }else{
            
            logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"DbListDAO.class :: getDbList() :: sql : "+sql1+", Failed to connect Database.");
        }
        return dbList;
    }
     public DbList findDBListbyDBName(String dbName){
        List<DbList> dbList=new ArrayList<>();
        Connection conn=connect();
        String sql1="select id,user_id,db_name,status,created_on,last_update from tb_dblist where db_name='"+dbName+"';";
        DbList newDb=null;
        if(conn!=null){
            try{
                
                java.sql.ResultSet rs=conn.createStatement().executeQuery(sql1);
                if(rs.next()){
                    newDb=new DbList();
                    newDb.setId(rs.getInt("id"));
                    newDb.setUser_id(rs.getInt("user_id"));
                    newDb.setDb_name(rs.getString("db_name"));
                    newDb.setStatus(rs.getInt("status"));
                    newDb.setCreated_on(rs.getString("created_on"));
                    newDb.setLast_update(rs.getString("last_update"));
                }
                if(rs!=null)rs=null;
                 logMsg(RLogger.MSG_TYPE_INFO,RLogger.LOGGING_LEVEL_INFO,"DbListDAO.class :: getDbList() :: sql : "+sql1+", Found Count "+dbList.size());
            }catch(Exception e){
                
                logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"DbListDAO.class :: getDbList() :: sql : "+sql1+", Exception "+e);
            }finally{
                try{
                    if(conn!=null) conn.close();
                }catch(Exception ee){}
                conn=null;
            }
        }else{
            
            logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"DbListDAO.class :: getDbList() :: sql : "+sql1+", Failed to connect Database.");
        }
        return newDb;
    }
     public boolean createNewDB(DbList newDb){
         boolean flag=false; 
         int res=0;
         Connection conn=connect();
        String sql1="insert into tb_dblist (user_id,db_name,status,created_on,last_update)  values("+newDb.getUser_id()+",'"+newDb.getDb_name()+"',1,now(),now());";
        if(conn!=null){
            try{
                res=conn.createStatement().executeUpdate(sql1);
                 logMsg(RLogger.MSG_TYPE_INFO,RLogger.LOGGING_LEVEL_INFO,"DbListDAO.class :: createNewDB() :: sql : "+sql1+", result : "+flag);
            }catch(Exception e){
                logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"DbListDAO.class :: createNewDB() :: sql : "+sql1+", Exception "+e);
            }finally{
                try{
                    if(conn!=null) conn.close();
                }catch(Exception ee){}
                conn=null;
            }
        }else{
            logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"DbListDAO.class :: getDbList() :: sql : "+sql1+", Failed to connect Database.");
        }
         return res>0?true:false;
     }
}
