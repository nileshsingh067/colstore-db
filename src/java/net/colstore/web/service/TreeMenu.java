/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.colstore.web.service;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import org.primefaces.model.TreeNode;

/**
 *
 * @author nilesh
 */
//@ManagedBean(name="treeIconView")
public class TreeMenu {
    private TreeNode root;
     
    //@ManagedProperty("#{nodeService}")
    private NodeService service;
     
 //   @PostConstruct
   // public void init() {
     //   System.out.println("TreeMenu  :: PostConstruct");
       // root = service.createDocuments();
    //}
 
    public void setService(NodeService service) {
        this.service = service;
    }
 
    public TreeNode getRoot() {
        return root;
    }
}
