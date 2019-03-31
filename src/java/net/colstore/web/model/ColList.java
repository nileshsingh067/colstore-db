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
//select id,tbl_id,col_name,col_dataType,status,created_on,last_updated from tb_collist where tbl_id=1;
public class ColList {
       private int id;
    private int tbl_id;
    private String col_name;
    private String col_dataType;
    private int status;
    private String created_on;
    private String last_updated;

    public ColList() {
    }

    public ColList(int id, int tbl_id, String col_name, String col_dataType, int status, String created_on, String last_updated) {
        this.id = id;
        this.tbl_id = tbl_id;
        this.col_name = col_name;
        this.col_dataType = col_dataType;
        this.status = status;
        this.created_on = created_on;
        this.last_updated = last_updated;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getTbl_id() {
        return tbl_id;
    }

    public void setTbl_id(int tbl_id) {
        this.tbl_id = tbl_id;
    }

    public String getCol_name() {
        return col_name;
    }

    public void setCol_name(String col_name) {
        this.col_name = col_name;
    }

    public String getCol_dataType() {
        return col_dataType;
    }

    public void setCol_dataType(String col_dataType) {
        this.col_dataType = col_dataType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCreated_on() {
        return created_on;
    }

    public void setCreated_on(String created_on) {
        this.created_on = created_on;
    }

    public String getLast_updated() {
        return last_updated;
    }

    public void setLast_updated(String last_updated) {
        this.last_updated = last_updated;
    }

    @Override
    public String toString() {
        return "ColList{" + "id=" + id + ", tbl_id=" + tbl_id + ", col_name=" + col_name + ", col_dataType=" + col_dataType + ", status=" + status + ", created_on=" + created_on + ", last_updated=" + last_updated + '}';
    }
    
    
}
