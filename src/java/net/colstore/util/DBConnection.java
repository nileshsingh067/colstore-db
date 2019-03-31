/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.colstore.util;

import java.io.Serializable;
import java.sql.Connection;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author Nilesh Kumar Singh
 */
public class DBConnection extends RLogger implements Serializable{
    public DBConnection(){}
    
    public Connection connect(){
        Connection conn=null;
        try{
           conn=getColstoredb().getConnection();
        }catch(Exception e){
            logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"DBConnection.class :: connect() :: DBERROR - Exception "+e.getMessage());
        }
        return conn;
    }

    public int executeUpdate(String sql1){
        int rep=0;
        Connection conn=connect();
        if(conn!=null){
            try{
                rep=conn.createStatement().executeUpdate(sql1);
            }catch(Exception e){
                rep=-1;
                logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"DBConnection.class :: executeUpdate() :: sql1 : "+sql1+", Exception "+e);
            }finally{
                try{
                    if(conn!=null) conn.close();
                }catch(Exception ee){}
                conn=null;
            }
        }else{
            rep=-1;
            logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"DBConnection.class :: executeUpdate() :: sql1 : "+sql1+", Failed to connect Database.");
        }
        return rep;
    }
    
    public boolean execute(String sql1){
        boolean rep=false;
        Connection conn=connect();
        if(conn!=null){
            try{
                rep=conn.createStatement().execute(sql1);
            }catch(Exception e){
                 rep=false;
                logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"DBConnection.class :: execute() :: sql1 : "+sql1+", Exception "+e);
            }finally{
                try{
                    if(conn!=null) conn.close();
                }catch(Exception ee){}
                conn=null;
            }
        }else{
             rep=false;
            logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"DBConnection.class :: execute() :: sql1 : "+sql1+", Failed to connect Database.");
        }
        return rep;
    }

    private DataSource getColstoredb() throws NamingException {
        Context c = new InitialContext();
        return (DataSource) c.lookup("java:comp/env/jiffycmsdb");
    }
    
    
    
    
    
}
