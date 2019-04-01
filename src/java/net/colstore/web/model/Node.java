/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.colstore.web.model;

/**
 *
 * @author nilesh
 */
public class Node implements java.io.Serializable{
    private String name;
     
    private String size;
     
    private String type;
    private String url;
    public Node(String name, String size, String type,String url) {
        this.name = name;
        this.size = size;
        this.type = type;
        this.url=url;
    }

    @Override
    public String toString() {
        return "Node{" + "name=" + name + ", size=" + size + ", type=" + type + '}';
    }

    
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    
    
}
