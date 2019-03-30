/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rocg.web.mbeans;

/**
 *
 * @author Nilesh Singh
 */
public class CMSAdnetwork {
    int adnetworkId;
    String AdnetworkName;
    String AdnetworkDeac;
    int status;
   String statusString;

    @Override
    public String toString() {
        return "CMSAdnetwork{" + "adnetworkId=" + adnetworkId + ", AdnetworkName=" + AdnetworkName + ", AdnetworkDeac=" + AdnetworkDeac + ", status=" + status + ", statusString=" + statusString + '}';
    }
   
   public CMSAdnetwork(){}

    public String getAdnetworkName() {
        return AdnetworkName;
    }

    public void setAdnetworkName(String AdnetworkName) {
        this.AdnetworkName = AdnetworkName;
    }

    public String getAdnetworkDeac() {
        return AdnetworkDeac;
    }

    public void setAdnetworkDeac(String AdnetworkDeac) {
        this.AdnetworkDeac = AdnetworkDeac;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
       this.setStatusString(status>0?"Active":"Disabled");
        this.status = status;
    }

    public String getStatusString() {
        return statusString;
    }

    public void setStatusString(String statusString) {
        this.statusString = statusString;
    }

    public int getAdnetworkId() {
        return adnetworkId;
    }

    public void setAdnetworkId(int adnetworkId) {
        this.adnetworkId = adnetworkId;
    }
   
   
           
   
}
