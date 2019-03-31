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
public class StringInputValidator {
    public static String validateString(String str,String prohibitedChars){
        String origString=str;
        str=str.toLowerCase();
        prohibitedChars=prohibitedChars.toLowerCase();
        char[] arr=prohibitedChars.toCharArray();
        boolean errFlag=false;
        for(char x:arr){
            if(str.contains(""+x)){
                errFlag=true;
            }
        }
       return (errFlag==true)?"Invalid Character found in Input!":origString;
           
    }
}
