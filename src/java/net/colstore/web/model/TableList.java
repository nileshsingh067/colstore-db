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

//select id,db_id,tbl_name,status,no_of_col,created_on,last_update from db_tbllist where db_id=1;
public class TableList {
     private int id;
    private int db_id;
    private String tbl_name;
    private int status;
    private int no_of_col;
    private String created_on;
    private String last_update;

    public TableList() {
    }

    public TableList(int id, int db_id, String tbl_name, int status, int no_of_col, String created_on,String last_update) {
        this.id = id;
        this.db_id = db_id;
        this.tbl_name = tbl_name;
        this.status = status;
        this.no_of_col = no_of_col;
        this.created_on = created_on;
        this.last_update=last_update;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDb_id() {
        return db_id;
    }

    public void setDb_id(int db_id) {
        this.db_id = db_id;
    }

    public String getTbl_name() {
        return tbl_name;
    }

    public void setTbl_name(String tbl_name) {
        this.tbl_name = tbl_name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getNo_of_col() {
        return no_of_col;
    }

    public void setNo_of_col(int no_of_col) {
        this.no_of_col = no_of_col;
    }

    public String getCreated_on() {
        return created_on;
    }

    public void setCreated_on(String created_on) {
        this.created_on = created_on;
    }

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    @Override
    public String toString() {
        return "TableList{" + "id=" + id + ", db_id=" + db_id + ", tbl_name=" + tbl_name + ", status=" + status + ", no_of_col=" + no_of_col + ", created_on=" + created_on + ", last_update=" + last_update + '}';
    }

   
    
}
