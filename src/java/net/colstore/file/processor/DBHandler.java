/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.colstore.file.processor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.colstore.util.DBConnection;
import net.colstore.util.NUtil;
import net.colstore.util.RLogger;
import net.colstore.web.model.ColList;

/**
 *
 * @author nilesh
 */
public class DBHandler extends DBConnection{
    
   /*
    Manage db file and folder creation
    */
    private String dbBasePath;
    public DBHandler(){
        this.setDbBasePath(NUtil.getStringProperty("db.base.path", "/home/nilesh/NetBeansProjects/colstore-db/db"));
    }
    public boolean createUser(int userId){
       return createDirectory(this.getDbBasePath()+"/"+userId);
    }
    public boolean createDatabase(int userId,String dbName){
       return createDirectory(this.getDbBasePath()+"/"+userId+"/"+dbName);
    }
    public boolean createTable(int userId,String dbName,String tableName){
       return createDirectory(this.getDbBasePath()+"/"+userId+"/"+dbName+"/"+tableName);
    }
    public boolean createColumn(int userId,String dbName,String tableName,String arr[]){
        boolean flag=true;    
        for(String f:arr){
               flag=flag&createFile(this.getDbBasePath()+"/"+userId+"/"+dbName+"/"+tableName, f);
            }
        return flag;
    }
     public boolean createColumn(int userId,String dbName,String tableName,String columnName){
        boolean flag=true;    
       
               flag=createFile(this.getDbBasePath()+"/"+userId+"/"+dbName+"/"+tableName, columnName);
            
        return flag;
    }
     public boolean buildSchema(int userId,String dbname,String tablename,List<ColList> collist){
         String path=this.getDbBasePath()+"/"+userId+"/"+dbname+"/"+tablename+"/"+tablename+"_schema.xml";
         return createSchema(path,dbname, tablename, collist);
     }
    public String getDbBasePath() {
        return dbBasePath;
    }

    public void setDbBasePath(String dbBasePath) {
        this.dbBasePath = dbBasePath;
    }
    public boolean createDirectory(String path){
        File file=new File(path);
        try{
            if(!file.exists()){
                return file.mkdir();
            }else{
                logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "DBHandler.class :: createDirectory() :: folder \""+path+"\" Already Exist.");    
                return true;
            }
        }catch(Exception e){
            logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "DBHandler.class :: createDirectory() :: Exception while creating folder, Exception  : "+e.getMessage());
        }
        return false;
    }
    public boolean createFile(String path,String fileName){
        File file=new File(path+"/"+fileName);
        try{
            if(file.createNewFile()){
                return true;
            }else{
                logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "DBHandler.class :: createFile() :: File \""+path+"/"+fileName+"\" Already Exist.");    
                return true;
            }
        }catch(Exception e){
            logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "DBHandler.class :: createFile() :: Exception while creating File, Exception  : "+e.getMessage());
        }
        return false;
    }
    public boolean createSchema(String path,String dbname,String tablename,List<ColList> collist){
        File file=new File(path);
        boolean flag=false;
        FileWriter fw=null;
        BufferedWriter bw=null;
        PrintWriter pw=null;
        String header="<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
"<ColstoreDB xmlns=\"http://www.iiitb.ac.in/DS603/MT2018069/Colstore\"\n" +
"		       xmlns:table=\"http://www.iiitb.ac.in/DS603/MT2018069/table\"\n" +
"		       xmlns:column=\"http://www.iiitb.ac.in/DS603/MT2018069/column\">\n" +
"	<DBDetails>\n" +
"		<Name>"+dbname+"</Name>\n" +
"	</DBDetails>\n" +
"	<table:Table_Details>\n" +
"		<table:name>"+tablename+"</table:name>\n" +
"		<column:Column_Details>";
        String footer="</column:Column_Details>\n" +
"	</table:Table_Details>\n" +
"</ColstoreDB>";
        StringBuilder  colDetails=new StringBuilder();
          for(ColList c:collist){
                colDetails.append("<column:attributes>\n" +
"				<column:attribute_name>"+c.getCol_name()+"</column:attribute_name>	\n" +
"				<column:dataType>"+c.getCol_dataType().toUpperCase()+"</column:dataType>		\n" +
"			</column:attributes>");
            }
        
        try{
            fw=new FileWriter(path);
            bw=new BufferedWriter(fw);
            pw=new PrintWriter(bw);
            pw.write(header+colDetails+footer);
            flag=true;
        }catch(Exception e){
            logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "DBHandler.class :: createSchema() :: Exception while creating File, Exception  : "+e.getMessage());
        }finally{
            try {
                pw.close();bw.close();fw.close();
            } catch (IOException ex) {
                Logger.getLogger(DBHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return flag;
    }
}