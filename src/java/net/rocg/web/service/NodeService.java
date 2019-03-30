/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rocg.web.service;

import javax.faces.bean.ApplicationScoped;
import javax.faces.bean.ManagedBean;
import net.rocg.web.model.Node;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

/**
 *
 * @author nilesh
 */
@ManagedBean(name = "nodeService")
@ApplicationScoped
public class NodeService {
    
    
    
    public TreeNode createDocuments(int user_id) {
        TreeNode root = new DefaultTreeNode(new Node("Files", "-", "Folder"), null);
        //dynamic db list
        return root;
    }
    
}
