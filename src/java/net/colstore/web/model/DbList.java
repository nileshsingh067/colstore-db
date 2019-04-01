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
//select id,user_id,db_name,status,created_on,last_update from tb_dblist where user_id=2;
public class DbList implements java.io.Serializable{
    private int id;
    private int user_id;
    private String db_name;
    private int status;
    private String created_on;
    private String last_update;

    public DbList() {
    }

    public DbList(int id, int user_id, String db_name, int status, String created_on, String last_update) {
        this.id = id;
        this.user_id = user_id;
        this.db_name = db_name;
        this.status = status;
        this.created_on = created_on;
        this.last_update = last_update;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getDb_name() {
        return db_name;
    }

    public void setDb_name(String db_name) {
        this.db_name = db_name;
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

    public String getLast_update() {
        return last_update;
    }

    public void setLast_update(String last_update) {
        this.last_update = last_update;
    }

    @Override
    public String toString() {
        return "DbList{" + "id=" + id + ", user_id=" + user_id + ", db_name=" + db_name + ", status=" + status + ", created_on=" + created_on + ", last_update=" + last_update + '}';
    }
    
    
}
