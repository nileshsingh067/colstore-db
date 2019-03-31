/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.colstore.util;

/**
 *
 * @author Nilesh Kumar Singh
 */
public class StatusMessage implements java.io.Serializable{
    public static final String MSG_CLASS_INFO="infomsg";
    public static final String MSG_CLASS_ERROR="errormsg";
    
    String message;
    String msgClass;
    public StatusMessage(){}

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setMessage(String message,String msgClass) {
        this.message = message;
        this.msgClass = msgClass;
    }
    public String getMsgClass() {
        return msgClass;
    }

    public void setMsgClass(String msgClass) {
        this.msgClass = msgClass;
    }
    
}
