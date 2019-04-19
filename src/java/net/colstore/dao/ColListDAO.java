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
import net.colstore.web.model.ColList;
import net.colstore.web.model.TableList;

/**
 *
 * @author nilesh
 */
public class ColListDAO extends DBConnection implements Serializable{
    
    public ColListDAO(){}
    
    public List<ColList> getColumnList(int tblId){
        List<ColList> colList=new ArrayList<>();
        Connection conn=connect();
        String sql1="select id,tbl_id,col_name,col_dataType,status,created_on,last_updated from tb_collist where tbl_id="+tblId+";";
        ColList newCol=null;
        if(conn!=null){
            try{
                
                java.sql.ResultSet rs=conn.createStatement().executeQuery(sql1);
                while(rs.next()){
                    newCol=new ColList();
                    newCol.setId(rs.getInt("id"));
                    newCol.setTbl_id(rs.getInt("tbl_id"));
                    newCol.setCol_name(rs.getString("col_name"));
                    newCol.setCol_dataType(rs.getString("col_dataType"));
                    newCol.setStatus(rs.getInt("status"));
                    newCol.setCreated_on(rs.getString("created_on"));
                    newCol.setLast_updated(rs.getString("last_updated"));
                    colList.add(newCol);
                    newCol=null;
                }
                if(rs!=null)rs=null;
                 logMsg(RLogger.MSG_TYPE_INFO,RLogger.LOGGING_LEVEL_INFO,"ColListDAO.class :: getColumnList() :: sql : "+sql1+", Found Count "+colList.size());
            }catch(Exception e){
                
                logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"ColListDAO.class :: getColumnList() :: sql : "+sql1+", Exception "+e);
            }finally{
                try{
                    if(conn!=null) conn.close();
                }catch(Exception ee){}
                conn=null;
            }
        }else{
            
            logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"ColListDAO.class :: getColumnList() :: sql : "+sql1+", Failed to connect Database.");
        }
        return colList;
    }
    public boolean createNewColumn(ColList newCol){
         boolean flag=false; 
         int res=0;
         Connection conn=connect();
        String sql1="insert into tb_collist (tbl_id,col_name,col_dataType,status,created_on,last_updated)values("+newCol.getTbl_id()+",'"+newCol.getCol_name()+"','"+newCol.getCol_dataType()+"',1,now(),now());";
        if(conn!=null){
            try{
                res=conn.createStatement().executeUpdate(sql1);
                 logMsg(RLogger.MSG_TYPE_INFO,RLogger.LOGGING_LEVEL_INFO,"ColListDAO.class :: createNewColumn() :: sql : "+sql1+", result : "+res);
            }catch(Exception e){
                logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"ColListDAO.class :: createNewColumn() :: sql : "+sql1+", Exception "+e);
            }finally{
                try{
                    if(conn!=null) conn.close();
                }catch(Exception ee){}
                conn=null;
            }
        }else{
            logMsg(RLogger.MSG_TYPE_ERROR,RLogger.LOGGING_LEVEL_ERROR,"ColListDAO.class :: createNewColumn() :: sql : "+sql1+", Failed to connect Database.");
        }
         return res>0?true:false;
     }
}
