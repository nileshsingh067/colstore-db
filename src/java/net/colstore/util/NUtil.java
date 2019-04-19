/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.colstore.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.commons.codec.binary.Base64;

/**
 *
 * @author Nilesh Kumar Singh
 */
public class NUtil {
    public static int FIELD_TYPE_WIDTH=1,FIELD_TYPE_HEIGHT=2,FIELD_TYPE_PARTNUMBER=3,FIELD_TYPE_FILE_EXTENSION=4,FIELD_TYPE_FILE_NAME_PREFIX=5,FIELD_TYPE_FILE_NAME=6;
    public static int BULK_UPLOAD_COUNTER=1;
    public static Properties dbProps;
   static{
        try{
            InputStream is=(InputStream)Class.forName("net.colstore.util.NUtil").getResourceAsStream("/dbprops.properties");
            dbProps=new Properties();
            dbProps.load(is);
        }catch(Exception e){
            System.out.println("RUtil.class :: Exception in static block :: "+e.getMessage());
        }
    }
    
    public static java.sql.Connection getDirectConnection(){
        Connection conn=null;
        try{
         Class.forName("com.mysql.jdbc.Driver");
         conn=java.sql.DriverManager.getConnection("jdbc:mysql://localhost:3306/db_colstore","root","admin");
        }catch(Exception e){
        }
        return conn;
    }
    
    public static void main(String[] args) throws Exception{
        
        String urlStr="https://api.sla-alacrity.com/v2.1/subscription/create?msisdn=<MSISDN>&frequency=<FREQUENCY>&amount=<AMOUNT>&currency=CURRENCY&pin=<PIN>&campaign=<CAMPAIGN>&merchant=<MERCHANT>&notification_url=<NOTIFY_URL>";
        String str1="aHR0cHM6Ly9hcGkuc2xhLWFsYWNyaXR5LmNvbS92Mi4xL3N1YnNjcmlwdGlvbi9jcmVhdGU/bXNpc2RuPTxNU0lTRE4 JmZyZXF1ZW5jeT08RlJFUVVFTkNZPiZhbW91bnQ9PEFNT1VOVD4mY3VycmVuY3k9Q1VSUkVOQ1kmcGluPTxQSU4 JmNhbXBhaWduPTxDQU1QQUlHTj4mbWVyY2hhbnQ9PE1FUkNIQU5UPiZub3RpZmljYXRpb25fdXJsPTxOT1RJRllfVVJMPg==";//RUtil.getBase64EncodedString(urlStr);
        str1=str1.trim();
        int indx=str1.indexOf(" ");
        while(indx>0){
            str1=str1.substring(0,indx)+"+"+str1.substring(indx+1);
            indx=str1.indexOf(" ");
        }
       // System.out.println("indx "+indx+", len "+str1.length());
        //str1.replaceAll(" ", "+");
        String str2=NUtil.getBase64DecodedString(str1);
        System.out.println("urlStr :"+urlStr);
        System.out.println("str1 :"+str1);
        System.out.println("str2 :"+str2);
        
        //System.out.println("Wd:"+parseField("aishwaryax_1x11__640x500_1",RUtil.FIELD_TYPE_WIDTH,true));
        //System.out.println("Ht:"+parseField("aishwarya_1_640x500_1",RUtil.FIELD_TYPE_HEIGHT,true));
        //System.out.println("PartNumber:"+parseField("aishwarya_1_640x500_1",RUtil.FIELD_TYPE_PARTNUMBER,true));
        List<String> retVal=NUtil.convertStrToList("Test1<BR> Test 2<BR>Test3<BR>", "<BR>");
        for(String t:retVal){
            System.out.println(t);
        }
    }
    
    public static int deleteAllFiles(String source){
        int fileCount=0;
        try{
            File f=new File(source);
            File[] filesList=null;
            if(f.exists() && f.isDirectory()){
                filesList=f.listFiles();
            }else if(f.exists()){
                fileCount +=f.delete()?1:0;
            }
            f=null;
            for(File f1:filesList){
                fileCount +=f1.delete()?1:0;
                f1=null;
            }
            filesList=null;
        }catch(Exception e){
            System.out.println("RUtil.class :: moveFolder() :: "+e);
        }
        return fileCount;
    }
    
    public static int moveFolder(String source,String dest){
        int fileCount=0;
        try{
            File f=new File(dest);
            if(!f.exists()) f.mkdirs();
            f=null;
            
            f=new File(source);
            File[] filesList=null;
            if(f.exists() && f.isDirectory()){
                filesList=f.listFiles();
            }
            f=null;
            
            for(File f1:filesList){
                System.out.println("Moving file "+f1.getCanonicalPath()+" to "+dest+f1.getName());
                fileCount +=f1.renameTo(new File(dest+f1.getName()))?1:0;
                f1=null;
            }
            filesList=null;
        }catch(Exception e){
            System.out.println("RUtil.class :: moveFolder() :: "+e);
        }
        return fileCount;
    }
    
    public static boolean moveFile(String sourceFilePath, String destFilePath){
        boolean flag=false;
        try{
            //Delete file if already exists
            File f1=new File(destFilePath);
            if(f1.exists()) f1.delete();
            f1=null;
            //Move File
            File f=new File(sourceFilePath);
            flag=f.renameTo(new File(destFilePath));
            f=null;
        }catch(Exception e){
            flag=false;
            System.out.println("Exception while moving file '" + sourceFilePath + "' to '" + destFilePath + "' Error: "+e);
        }
        return flag;
    }
    
   
   public static List<String> convertStrToList(String str,String separator,String defaultVal,String ignoreVal){
       List<String> retObj=new ArrayList<>();
       str=(str==null)?defaultVal:str.trim();
       if(!str.equalsIgnoreCase(ignoreVal)){
            String[] tempArr=str.split(separator);
            for(String t:tempArr){
                t=(t==null)?ignoreVal:t.trim();
                if(!t.equalsIgnoreCase(ignoreVal)) retObj.add(t);
                t=null;
            }
       }
       return retObj;
   }
   
   public static List<String> convertStrToList(String str,String separator){
       List<String> respData=new ArrayList<>();
        try{
            int indx=str.indexOf(separator);
            if(indx<0 && str.length()>0)
                respData.add(str);
            int cutIndx=0;
            while(indx>=0){
                if(indx==0){
                    respData.add("");
                }else{
                    respData.add(str.substring(0,indx));
                }
                cutIndx=indx+separator.length();
                if(str.length()>cutIndx){
                    str=str.substring(cutIndx);
                }else{
                    str="";
                }
                indx=str.indexOf(separator);
                if(indx<0 && str.length()>0){respData.add(str); str="";}
                    
            }
        }catch(Exception e){
            System.out.println("splitToArray() :: Exception : "+e.getMessage());
        }
        return respData;
    }
    
    
    
    public static String readFile(String folderPath,String fileToRead,String lineFeedChar){
        StringBuilder respData=new StringBuilder();
        try{
            folderPath=(folderPath==null)?"":folderPath.trim();
            String filePath=folderPath.endsWith("/")?folderPath+fileToRead:folderPath+"/"+fileToRead;
            lineFeedChar=(lineFeedChar==null)?"":lineFeedChar.trim();
            BufferedReader br=new BufferedReader(new FileReader(filePath));
            String newLine="";
            while((newLine=br.readLine())!=null){
                if(respData.length()>0)
                    respData.append("<BR>").append(newLine);
                else
                    respData.append(newLine);
            }
           br.close();
           br=null;
        }catch(IOException e){
            System.out.println("readFile() :: Exception while reading file, "+e.getMessage());
        }
        return respData.toString();
    }
    
    public static int writeToFile(InputStream is,String folderPath,String destFile){
        int retVal=0;
        try{
            File f=new File(folderPath);
            if(!f.exists()) f.mkdirs();
            f=null;
            BufferedOutputStream bos=new BufferedOutputStream(new FileOutputStream(new File(destFile)));
            int x=0;
           byte[] b=new byte[2048];
            while((x=is.read(b))!=-1){
                bos.write(b,0,x);
                retVal=retVal+x;
            }
            bos.close();
            bos=null;
            is.close();
            is=null;
        }catch(IOException e){
            System.out.println("Exception while writing to "+destFile+", "+e.getMessage());
        }
        return retVal;
    }
    
    public static String parseField(String fileName,int fieldType,String defaultValue,String fileCategory,boolean haveParts){
        String retVal="";
        try{
            if(!haveParts)
                retVal=parseField(fileName,fieldType,defaultValue,fileCategory);
            else if(fieldType==FIELD_TYPE_FILE_EXTENSION){
               retVal=parseField(fileName,fieldType,defaultValue,fileCategory);
            }else if(fieldType==FIELD_TYPE_FILE_NAME_PREFIX){
                //Remove Part Number
                fileName=haveParts?fileName.substring(0,fileName.lastIndexOf("_")):fileName;
                retVal=parseField(fileName,fieldType,defaultValue,fileCategory);
            }else if(fieldType==FIELD_TYPE_FILE_NAME){
                //Remove Part Number
                fileName=(haveParts && !fileCategory.equalsIgnoreCase("preview"))?fileName.substring(0,fileName.lastIndexOf("_")):fileName;
                retVal=parseField(fileName,fieldType,defaultValue,fileCategory);
            }
        }catch(Exception e){
            retVal=defaultValue;
        }
        return retVal;
    }
    
    public static String getBase64DecodedString(String str){
        byte[] byteArray = Base64.decodeBase64(str.getBytes());
        String decodedString = new String(byteArray);
        return decodedString;
        
    }
    public static String getBase64EncodedString(String str){
        byte[] byteArray = Base64.encodeBase64(str.getBytes());
        String encodedString = new String(byteArray);
        return encodedString;
        
    }
    
    public static String getMD5EncodedString(String str){
        String respStr="";
        try{
        MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(str.getBytes());
		byte[] digest = md.digest();
		StringBuffer sb = new StringBuffer();
		for (byte b : digest) {
			sb.append(String.format("%02x", b & 0xff));
		}
          respStr=sb.toString();
        }catch(Exception e){
        }
        return respStr;
    }
    
    public static String parseField(String fileName,int fieldType,String defaultValue,String fileCategory){
        String retVal="";
        try{
            if(fieldType==FIELD_TYPE_FILE_EXTENSION){
                fileName=fileName.substring(fileName.lastIndexOf(".")+1);
                retVal=(fileName==null)?defaultValue:fileName.toUpperCase().trim();
            }else if(fieldType==FIELD_TYPE_FILE_NAME_PREFIX){
                fileName=fileName.substring(0,fileName.lastIndexOf("_"));
                retVal=(fileName==null)?defaultValue:fileName.trim();
            }else if(fieldType==FIELD_TYPE_FILE_NAME){
                retVal=fileName;
            }
        }catch(Exception e){
            retVal=defaultValue;
        }
        return retVal;
    }
    
    public static int parseField(String fileName,int fieldType,boolean haveParts){
        int retVal=0;
        try{
            if(fieldType==FIELD_TYPE_WIDTH || fieldType==FIELD_TYPE_HEIGHT){
               if(haveParts) fileName=fileName.substring(0, fileName.lastIndexOf("_"));
               fileName=fileName.substring(fileName.lastIndexOf("_")+1);
               String tmpstr=fileName.toLowerCase();
               fileName=(fieldType==FIELD_TYPE_WIDTH)?fileName.substring(0,tmpstr.indexOf("x")):fileName.substring(tmpstr.indexOf("x")+1);

            }else if(fieldType==FIELD_TYPE_PARTNUMBER){
               if(haveParts) fileName=fileName.substring(fileName.lastIndexOf("_")+1);
               else fileName="0";
            }
            retVal=Integer.parseInt(fileName);
        }catch(Exception e){
            retVal=0;
        }
        return retVal;
    }
    
    public static String formatString(String strVal,String defaultVal){
        strVal=(strVal==null)?defaultVal:strVal.trim();
        return strVal;
    }
    
    public static String getStringProperty(String propName,String defaultVal){
        String propVal=defaultVal;
        propVal=dbProps.getProperty(propName);
        propVal=(propVal==null)?defaultVal:propVal.trim();
        return propVal;
    }
    
    public static String formatNumber(int number,int digitCount,boolean fillPrefix){
        String retVal=""+number;
        while(digitCount>retVal.length()) retVal=(fillPrefix)?"0"+retVal:retVal+"0";
        return retVal;
    }
    
    public static int getIntProperty(String propName,int defaultVal){
        String propVal=""+defaultVal;
        propVal=dbProps.getProperty(propName);
        propVal=(propVal==null)?""+defaultVal:propVal.trim();
        return Integer.parseInt(propVal);
    }
    
    public static double getDoubleProperty(String propName,double defaultVal){
        String propVal=""+defaultVal;
        propVal=dbProps.getProperty(propName);
        propVal=(propVal==null)?""+defaultVal:propVal.trim();
        return Double.parseDouble(propVal);
    }
    
    public static int boolToInt(boolean x){
        return (x)?1:0;
    }
    
    public static boolean intToBool(int x){
        return (x>0)?true:false;
    }
    
    public static boolean strToBool(String x,boolean defaultVal){
       return (strToInt(x,(defaultVal==true)?1:0)>0)?true:false;
       
    }
    
    public static int strToInt(String str,int defaultVal){
        int retVal=defaultVal;
        try{
            retVal=Integer.parseInt(str);
        }catch(Exception e){
            retVal=defaultVal;
        }
        return retVal;
    }
     public static long strToLong(String str,long defaultVal){
        long retVal=defaultVal;
        try{
            retVal=Long.parseLong(str);
        }catch(Exception e){
            retVal=defaultVal;
        }
        return retVal;
    }
      public static double strToDouble(String str,double defaultVal){
        double retVal=defaultVal;
        try{
            retVal=Double.parseDouble(str);
        }catch(Exception e){
            retVal=defaultVal;
        }
        return retVal;
    }
      
    public static String doubleToStr(double val){
        String valStr=""+val;
        if(valStr.indexOf(".")>0) valStr=valStr.substring(0,valStr.indexOf("."));
        return valStr;
    }
}
