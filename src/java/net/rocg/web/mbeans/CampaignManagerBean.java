/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rocg.web.mbeans;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.rocg.util.DBConnection;
import net.rocg.util.RLogger;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import net.rocg.web.beans.WAPCampaign;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;

/**
 * Name : campaignManagerBean
 *
 * @author Rishi Tyagi
 */
public class CampaignManagerBean {

    DBConnection dbConn;
    int loginId, roleId;
    String loginName;
    Map<String, String> adnetworkList;
    String selectedAdnetwork;
    Map<String, String> serviceList;
    String selectedService;
    Map<String, String> portalList;
    String selectedPortal;
    Map<String, String> operatorList;
    String selectedOperator;
    Map<String, String> operatorCircleList;
    String selectedContentType = "0";
    Map<String, String> contentTypeList;
    String selectedPricePoint = "0";
    Map<String, String> pricePointList;
    Map<String, String> pricePointListByCampaign;

    String selectedCircle;
    String selectedChargingCategory;

    Map<String, String> packageList;
    String selectedPackage;

    WAPCampaign newCampaign;

    List<WAPCampaign> campaignList;
    WAPCampaign selectedCampaign;

    StatusMessage statusMsg;

    /**
     * Creates a new instance of CampaignManagerBean
     */
    public CampaignManagerBean() {
        dbConn = new DBConnection();
        fetchLoginDetails();
        adnetworkList = new HashMap<>();
        operatorList = new HashMap<>();
        operatorCircleList = new HashMap<>();
        serviceList = new HashMap<>();
        portalList = new HashMap<>();
        packageList = new HashMap<>();
        pricePointList = new HashMap<>();
        pricePointListByCampaign = new HashMap<>();
        contentTypeList = new HashMap<>();
        statusMsg = new StatusMessage();
        campaignList = new ArrayList<WAPCampaign>();
        newCampaign = new WAPCampaign();
        reloadList(null, 0, true, false, false, true, false, false, true, false, false, false);
    }

    public void reloadList(WAPCampaign updateObj, int action, boolean reloadAdnetwork, boolean reloadOpList, boolean reloadOpcircles,
            boolean reloadServiceList, boolean reloadPortalList, boolean reloadPackageList, boolean reloadCampaignList,
            boolean reloadContentType, boolean reloadPricePoint, boolean createNewCampaign) {
        Connection conn = dbConn.connect();

        if (conn != null) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadList() :: Database Connected.");
            try {
                java.sql.Statement st = conn.createStatement();
                String sqlA = "";
                int dbRep = 0;
                if (action == 1) {
                    //Update Campaign Details Name
                    sqlA = " update tb_campaign_details set campaign_name='" + updateObj.getCampaignName() + "',ad_ref_id='" + updateObj.getAdRefId() + "',price_point="+updateObj.getPricePoint()+" where campaign_id=" + updateObj.getCampaignId() + ";";
                    dbRep = st.executeUpdate(sqlA);
                    dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadList() :: Action Requested=UPDATE-CAMPAIGN; SQL : " + sqlA + "| DB Result=" + dbRep);
                } else if (action == 2) {

                    java.sql.ResultSet rs = null;
                    try {
                        String ratingCategory = (this.getSelectedChargingCategory() == null) ? "SUBSCRIPTION" : this.getSelectedChargingCategory().toUpperCase();
                        String contentType = (this.getSelectedContentType() == null) ? "0" : this.getSelectedContentType();
                        String sql1 = "SELECT a.`op_price_point`, a.`op_currency` FROM tb_operator_subscription_packages a WHERE a.`op_rating_config_id` IN (SELECT rating_config_id FROM tb_rating_operator_configurations WHERE operator_id=" + updateObj.getOperatorId() + " AND portal_id=" + updateObj.getPortalId()+ ");";
                        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadPricepointList() :: Query : " + sql1);
                        rs = st.executeQuery(sql1);
                        this.pricePointListByCampaign.clear();
                        while (rs.next()) {
                            this.pricePointListByCampaign.put(rs.getString("op_currency") + " " + rs.getString("op_price_point"), rs.getString("op_price_point"));
                            System.out.println("op opop opo "+rs.getString("op_currency") + " " + rs.getString("op_price_point"));
                        }

//                        if (pricePointListByCampaign.size() <= 0) {
//                            rs = null;
//                            sql1 = "SELECT a.`op_price_point`, a.`op_currency` FROM tb_operator_subscription_packages a WHERE a.`content_type_id`=" + contentType + "  AND a.`rating_category`='" + this.getSelectedChargingCategory().toUpperCase() + "' AND a.`op_rating_config_id` IN (SELECT rating_config_id FROM tb_rating_operator_configurations WHERE operator_id=" + this.getSelectedOperator() + " AND circle_id=0 AND service_id=" + this.getSelectedService() + " AND portal_id=" + this.getSelectedPortal() + ");";
//                            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadPricepointList() :: Query : " + sql1);
//                            rs = st.executeQuery(sql1);
//                            while (rs.next()) {
//                                this.pricePointList.put(rs.getString("op_currency") + " " + rs.getString("op_price_point"), rs.getString("op_price_point"));
//                            }
//                        }
                        if (rs != null) {
                            rs.close();
                        }
                        rs = null;

                    } catch (Exception e) {
                        dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: reloadPricepointList() :: Exception " + e);
                    } finally {
                        try {
                            if (rs != null) {
                                rs.close();
                            }
                        } catch (Exception ee) {
                        }
                        rs = null;
                    }

                } else {

                    if (reloadAdnetwork) {
                        reloadAdNetworks(st);
                    }
                    if (reloadOpList) {
                        this.reloadOperatorList(st);
                    }
                    if (reloadOpcircles) {
                        this.reloadOperatorCircleList(st);
                    }
                    if (reloadServiceList) {
                        this.reloadServiceList(st);
                    }
                    if (reloadPortalList) {
                        this.reloadPortalList(st);
                    }
                    if (reloadContentType) {
                        this.reloadContentTypeList(st);
                    }
                    if (reloadPricePoint) {
                        this.reloadPricepointList(st);
                    }
                    if (reloadPackageList) {
                        this.reloadPackageList(st);
                    }
                    if (reloadCampaignList) {
                        this.reloadCampaignList(st);
                    }
                    if (createNewCampaign) {
                        this.createNew(st);
                        this.reloadCampaignList(st);
                    }
                    st.close();
                    st = null;
                }
            } catch (Exception e) {
                dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: reloadList() :: Exception :" + e.getMessage());
                statusMsg.setMessage("A process failed while reloading Country List. Database Error!", StatusMessage.MSG_CLASS_ERROR);
            } finally {
                try {
                    if (conn != null) {
                        conn.close();
                    }
                } catch (Exception ee) {
                }
                conn = null;
            }
        } else {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: reloadList() :: Database Connectivity Failed");
            statusMsg.setMessage("A process failed while reloading Country List. Database Connectivity Issue.", StatusMessage.MSG_CLASS_ERROR);
        }
    }

    public void createNew(java.sql.Statement st) {
        try {
            if (this.getNewCampaign().getCampaignName() == null || this.getNewCampaign().getCampaignName().length() <= 0) {
                this.statusMsg.setMessage("Campaign Name can not be blank!", StatusMessage.MSG_CLASS_ERROR);
            } else if (this.getNewCampaign().getAdRefId() == null || this.getNewCampaign().getAdRefId().length() <= 0) {
                this.statusMsg.setMessage("Ad Reference Id can not be blank!", StatusMessage.MSG_CLASS_ERROR);
            } else if (RUtil.strToInt(this.getSelectedAdnetwork(), 0) <= 0) {
                this.statusMsg.setMessage("Please select Ad-Network to created campaign for!", StatusMessage.MSG_CLASS_ERROR);
            } else if (RUtil.strToInt(this.getSelectedOperator(), 0) <= 0) {
                this.statusMsg.setMessage("Please select Operator to created campaign for!", StatusMessage.MSG_CLASS_ERROR);
            } else if (this.getSelectedPackage() == null || this.getSelectedPackage().indexOf(",") <= 0) {
                this.statusMsg.setMessage("Please select Package for promotion in the campaign!", StatusMessage.MSG_CLASS_ERROR);
            } else if (RUtil.strToInt(this.getSelectedService(), 0) <= 0) {
                this.statusMsg.setMessage("Please select Service to created campaign for!", StatusMessage.MSG_CLASS_ERROR);
            } else if (RUtil.strToInt(this.getSelectedPortal(), 0) <= 0) {
                this.statusMsg.setMessage("Please select Portal to created campaign for!", StatusMessage.MSG_CLASS_ERROR);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("INSERT INTO tb_campaign_details(campaign_name,campaign_descp,ad_network_id,op_id,circle_id,package_id,package_detail_id,service_id,portal_id,ad_ref_id,"
                        + "callback_url,reg_date,STATUS,price_point,content_type,rating_category) VALUES('" + this.getNewCampaign().getCampaignName() + "','NA',"
                        + this.getSelectedAdnetwork() + "," + this.getSelectedOperator() + "," + this.getSelectedCircle() + "," + this.getSelectedPackage() + "," + this.getSelectedService() + "," + this.getSelectedPortal() + ","
                        + "'" + this.getNewCampaign().getAdRefId() + "','NA',now(),1," + this.getSelectedPricePoint() + "," + this.getSelectedContentType() + ","
                        + "'" + this.getSelectedChargingCategory() + "');");
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: createNew() :: Query : " + sb.toString());
                int rep = st.executeUpdate(sb.toString());
                if (rep > 0) {
                    this.statusMsg.setMessage("Campaign Created successfully!", StatusMessage.MSG_CLASS_INFO);
                } else {
                    this.statusMsg.setMessage("Failed to register new campaign, DB Query status is " + rep, StatusMessage.MSG_CLASS_ERROR);
                }
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: createNew() :: Query Request : " + rep);
                sb = null;
            }
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: createNew() :: Exception " + e);
        }
    }

    public void reloadCampaignList(java.sql.Statement st) {
        java.sql.ResultSet rs = null;
        try {
            String sql1 = "SELECT a.campaign_id,a.campaign_name,a.ad_network_id,b.`network_name`,a.op_id,c.`operator_name`,a.`portal_id`,d.`portal_name`,"
                    + "a.`ad_ref_id`,a.`rating_category`,a.`price_point` FROM tb_campaign_details a,tb_adnetworks b,tb_operators c,tb_portals d WHERE a.`ad_network_id`=b.`ad_network_id` AND a.`op_id`=c.`operator_id` AND a.`portal_id`=d.`portal_id`";
            if (RUtil.strToInt(this.getSelectedService(), 0) > 0) {
                sql1 += " AND a.`service_id`=" + this.getSelectedService();
            }
            if (RUtil.strToInt(this.getSelectedAdnetwork(), 0) > 0) {
                sql1 += " AND a.`ad_network_id`=" + this.getSelectedAdnetwork();
            }
            if (RUtil.strToInt(this.getSelectedPortal(), 0) > 0) {
                sql1 += " AND a.`portal_id`=" + this.getSelectedPortal();
            }
            if (RUtil.strToInt(this.getSelectedOperator(), 0) > 0) {
                sql1 += " AND a.`op_id`=" + this.getSelectedOperator();
            }
            sql1 += ";";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadCampaignList() :: Query : " + sql1);
            rs = st.executeQuery(sql1);
            this.campaignList.clear();
            WAPCampaign newC = null;
            while (rs.next()) {
                newC = new WAPCampaign();
                newC.setCampaignId(rs.getInt("campaign_id"));
                newC.setCampaignName(rs.getString("campaign_name"));
                newC.setAdnetworkId(rs.getInt("ad_network_id"));
                newC.setAdnetworkName(rs.getString("network_name"));
                newC.setOperatorId(rs.getInt("op_id"));
                newC.setOperatorName(rs.getString("operator_name"));
                newC.setPortalId(rs.getInt("portal_id"));
                newC.setPortalName(rs.getString("portal_name"));
                newC.setAdRefId(rs.getString("ad_ref_id"));
                newC.setRatingCategory(rs.getString("rating_category"));
                newC.setPricePoint(rs.getInt("price_point"));
                campaignList.add(newC);
                newC = null;
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;

        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: reloadCampaignList() :: Exception " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ee) {
            }
            rs = null;
        }
    }

    public void reloadPricepointList(java.sql.Statement st) {
        java.sql.ResultSet rs = null;
        try {
            String ratingCategory = (this.getSelectedChargingCategory() == null) ? "SUBSCRIPTION" : this.getSelectedChargingCategory().toUpperCase();
            String contentType = (this.getSelectedContentType() == null) ? "0" : this.getSelectedContentType();
            String sql1 = "SELECT a.`op_price_point`, a.`op_currency` FROM tb_operator_subscription_packages a WHERE a.`content_type_id`=" + contentType + " AND a.`rating_category`='" + ratingCategory + "' AND a.`op_rating_config_id` IN (SELECT rating_config_id FROM tb_rating_operator_configurations WHERE operator_id=" + this.getSelectedOperator() + " AND circle_id=" + this.getSelectedCircle() + " AND service_id=" + this.getSelectedService() + " AND portal_id=" + this.getSelectedPortal() + ");";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadPricepointList() :: Query : " + sql1);
            rs = st.executeQuery(sql1);
            this.pricePointList.clear();
            while (rs.next()) {
                this.pricePointList.put(rs.getString("op_currency") + " " + rs.getString("op_price_point"), rs.getString("op_price_point"));
            }

            if (pricePointList.size() <= 0) {
                rs = null;
                sql1 = "SELECT a.`op_price_point`, a.`op_currency` FROM tb_operator_subscription_packages a WHERE a.`content_type_id`=" + contentType + "  AND a.`rating_category`='" + this.getSelectedChargingCategory().toUpperCase() + "' AND a.`op_rating_config_id` IN (SELECT rating_config_id FROM tb_rating_operator_configurations WHERE operator_id=" + this.getSelectedOperator() + " AND circle_id=0 AND service_id=" + this.getSelectedService() + " AND portal_id=" + this.getSelectedPortal() + ");";
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadPricepointList() :: Query : " + sql1);
                rs = st.executeQuery(sql1);
                while (rs.next()) {
                    this.pricePointList.put(rs.getString("op_currency") + " " + rs.getString("op_price_point"), rs.getString("op_price_point"));
                }
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;

        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: reloadPricepointList() :: Exception " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ee) {
            }
            rs = null;
        }
    }

    public void editPricePointList(SelectEvent event) {
        WAPCampaign newObj = (WAPCampaign) event.getObject();
        dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_INFO, "PortalContentSchedulingBean :: reloadPricepointListByCampaign() :: Campaign Data For Update (" + newObj.getCampaignName() + ").");
        if (newObj != null && newObj.getCampaignId() > 0) {
            statusMsg.setMessage("Campaign Details sent for update into Registry!", StatusMessage.MSG_CLASS_INFO);
            //refreshCountryList(newObj,2);
            reloadList(newObj, 2, false, false, false, false, false, false, false, false, false, false);
        } else {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "AdnetworkBean :: onEdit() :: Invalid Platform Id (" + newObj.getAdnetworkId() + ").");
            statusMsg.setMessage("Invalid Camapign Id `" + newObj.getCampaignId() + "`. Please select a Campaign to update.", StatusMessage.MSG_CLASS_ERROR);
        }

    }

    public void reloadContentTypeList(java.sql.Statement st) {
        java.sql.ResultSet rs = null;
        try {
            String ratingCategory = (this.getSelectedChargingCategory() == null) ? "SUBSCRIPTION" : this.getSelectedChargingCategory().toUpperCase();
            String sql1 = "SELECT a.`content_type_id`, b.`content_type_name` FROM tb_operator_subscription_packages a,tb_cms_content_type b WHERE a.`content_type_id`=b.`content_type_id` AND a.`rating_category`='" + ratingCategory + "' AND a.`op_rating_config_id` IN (SELECT rating_config_id FROM tb_rating_operator_configurations WHERE operator_id=" + this.getSelectedOperator() + " AND circle_id=" + this.getSelectedCircle() + " AND service_id=" + this.getSelectedService() + " AND portal_id=" + this.getSelectedPortal() + ");";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadContentTypeList() :: Query : " + sql1);
            rs = st.executeQuery(sql1);
            this.contentTypeList.clear();
            while (rs.next()) {
                this.contentTypeList.put(rs.getString("content_type_name"), rs.getString("content_type_id"));
            }

            if (packageList.size() <= 0) {
                rs = null;
                sql1 = "SELECT a.`content_type_id`, b.`content_type_name` FROM tb_operator_subscription_packages a,tb_cms_content_type b WHERE a.`content_type_id`=b.`content_type_id` AND a.`rating_category`='" + this.getSelectedChargingCategory().toUpperCase() + "' AND a.`op_rating_config_id` IN (SELECT rating_config_id FROM tb_rating_operator_configurations WHERE operator_id=" + this.getSelectedOperator() + " AND circle_id=0 AND service_id=" + this.getSelectedService() + " AND portal_id=" + this.getSelectedPortal() + ");";
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadContentTypeList() :: Query : " + sql1);
                rs = st.executeQuery(sql1);
                while (rs.next()) {
                    this.contentTypeList.put(rs.getString("content_type_name"), rs.getString("content_type_id"));
                }
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;

        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: reloadContentTypeList() :: Exception " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ee) {
            }
            rs = null;
        }
    }

    public void reloadPackageList(java.sql.Statement st) {
        java.sql.ResultSet rs = null;
        try {
            String ratingCategory = (this.getSelectedChargingCategory() == null) ? "SUBSCRIPTION" : this.getSelectedChargingCategory().toUpperCase();
            String sql1 = "SELECT a.`package_detail_id`, a.`package_id`,a.`op_price_point`,a.`op_currency`,b.`package_name` FROM tb_operator_subscription_packages a,tb_cms_subscription_packages b WHERE a.`package_id`=b.`package_id` AND a.`rating_category`='" + ratingCategory + "' AND a.`content_type_id`=" + this.getSelectedContentType() + " AND a.op_price_point=" + this.getSelectedPricePoint() + " AND a.`op_rating_config_id` IN (SELECT rating_config_id FROM tb_rating_operator_configurations WHERE operator_id=" + this.getSelectedOperator() + " AND circle_id=" + this.getSelectedCircle() + " AND service_id=" + this.getSelectedService() + " AND portal_id=" + this.getSelectedPortal() + ");";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadPackageList() :: Query : " + sql1);
            rs = st.executeQuery(sql1);
            this.packageList.clear();
            while (rs.next()) {
                this.packageList.put(rs.getString("package_name"), (rs.getString("package_id") + "," + rs.getString("package_detail_id")));
            }

            if (packageList.size() <= 0) {
                rs = null;
                sql1 = "SELECT a.`package_detail_id`, a.`package_id`,a.`op_price_point`,a.`op_currency`,b.`package_name` FROM tb_operator_subscription_packages a,tb_cms_subscription_packages b WHERE a.`package_id`=b.`package_id` AND a.`rating_category`='" + this.getSelectedChargingCategory().toUpperCase() + "' AND a.`op_rating_config_id` IN (SELECT rating_config_id FROM tb_rating_operator_configurations WHERE operator_id=" + this.getSelectedOperator() + " AND circle_id=0 AND service_id=" + this.getSelectedService() + " AND portal_id=" + this.getSelectedPortal() + ");";
                dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadPackageList() :: Query : " + sql1);
                rs = st.executeQuery(sql1);
                while (rs.next()) {
                    this.packageList.put(rs.getString("package_name"), (rs.getString("package_id") + "," + rs.getString("package_detail_id")));
                }
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;

        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: reloadPackageList() :: Exception " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ee) {
            }
            rs = null;
        }
    }

    public void reloadOperatorCircleList(java.sql.Statement st) {
        java.sql.ResultSet rs = null;
        try {
            String sql1 = "SELECT DISTINCT a.`circle_id`,b.`circle_name` FROM tb_rating_operator_configurations a, tb_operator_circles b WHERE a.`circle_id`=b.`circle_id` AND a.`circle_id`>0 AND a.`operator_id`=" + this.getSelectedOperator() + " AND a.`service_id`=" + this.getSelectedService() + " AND a.`portal_id`=" + this.getSelectedPortal() + " ORDER BY b.`circle_name`;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadOperatorCircleList() :: Query : " + sql1);
            rs = st.executeQuery(sql1);
            this.operatorCircleList.clear();
            while (rs.next()) {
                this.operatorCircleList.put(rs.getString("circle_name"), rs.getString("circle_id"));
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;

        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: reloadOperatorCircleList() :: Exception " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ee) {
            }
            rs = null;
        }
    }

    public void reloadOperatorList(java.sql.Statement st) {
        java.sql.ResultSet rs = null;
        try {
            String sql1 = "SELECT DISTINCT a.operator_id,CONCAT(c.`country_name`,\":\",b.`operator_name`) AS operator_name FROM tb_rating_operator_configurations a, tb_operators b,tb_country c WHERE a.`operator_id`=b.`operator_id` AND b.`country_id`=c.`country_id` AND a.`service_id`=" + this.getSelectedService() + " AND a.`portal_id`=" + this.getSelectedPortal() + " ORDER BY c.`country_name`,b.`operator_name`;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadOperatorList() :: Query : " + sql1);
            rs = st.executeQuery(sql1);
            this.operatorList.clear();
            while (rs.next()) {
                this.operatorList.put(rs.getString("operator_name"), rs.getString("operator_id"));
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;

        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: reloadOperatorList() :: Exception " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ee) {
            }
            rs = null;
        }
    }

    public void reloadPortalList(java.sql.Statement st) {
        java.sql.ResultSet rs = null;
        try {
            String sql1 = "SELECT DISTINCT portal_id,portal_name FROM vw_portal_services where service_id=" + this.getSelectedService() + ";";
            if (this.getRoleId() == 1 || this.getRoleId() == 2) {
                sql1 = "SELECT DISTINCT portal_id,portal_name FROM vw_portal_services ;";
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadPortalList() :: Query : " + sql1);
            rs = st.executeQuery(sql1);
            this.portalList.clear();
            while (rs.next()) {
                this.portalList.put(rs.getString("portal_name"), rs.getString("portal_id"));
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;

        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: reloadPortalList() :: Exception " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ee) {
            }
            rs = null;
        }
    }

    public void reloadServiceList(java.sql.Statement st) {
        java.sql.ResultSet rs = null;
        try {

            String sql1 = "SELECT DISTINCT service_id,service_name FROM vw_portal_services;";
            if (this.getRoleId() == 1 || this.getRoleId() == 2) {
                sql1 = "SELECT DISTINCT service_id,service_name FROM vw_portal_services;";
            }
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadAdNetworks() :: Query : " + sql1);
            rs = st.executeQuery(sql1);
            this.serviceList.clear();
            while (rs.next()) {
                this.serviceList.put(rs.getString("service_name"), rs.getString("service_id"));
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;

        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: reloadServiceList() :: Exception " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ee) {
            }
            rs = null;
        }
    }

    public void reloadAdNetworks(java.sql.Statement st) {
        java.sql.ResultSet rs = null;
        try {
            String sql1 = "SELECT network_name,ad_network_id FROM tb_adnetworks WHERE STATUS>0 ORDER BY network_name;";
            dbConn.logUIMsg(RLogger.MSG_TYPE_INFO, RLogger.LOGGING_LEVEL_DEBUG, "CampaignManagerBean.class :: reloadAdNetworks() :: Query : " + sql1);
            rs = st.executeQuery(sql1);
            this.adnetworkList.clear();
            while (rs.next()) {
                this.adnetworkList.put(rs.getString("network_name"), rs.getString("ad_network_id"));
            }
            if (rs != null) {
                rs.close();
            }
            rs = null;

        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "CampaignManagerBean.class :: reloadAdNetworks() :: Exception " + e);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ee) {
            }
            rs = null;
        }
    }

    public void fetchLoginDetails() {

        LoginBean loginBeanObj = null;
        try {
            FacesContext ctx = FacesContext.getCurrentInstance();
            ExternalContext extCtx = ctx.getExternalContext();
            Map<String, Object> sessionMap = extCtx.getSessionMap();
            loginBeanObj = (LoginBean) sessionMap.get("loginBean");
            this.setLoginId(loginBeanObj.getUserId());
            this.setRoleId(loginBeanObj.getRoleId());
            this.setLoginName(loginBeanObj.getUserName());
        } catch (Exception e) {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean.class :: fetchLoginDetails() :: Exception " + e.getMessage());
        }
        loginBeanObj = null;

    }

    /**
     * Edit Event Handler Method
     */
    public void onEdit(RowEditEvent event) {
        WAPCampaign newObj = (WAPCampaign) event.getObject();
        dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "PortalContentSchedulingBean :: onEdit() :: Adnetwork Data For Update (" + newObj.getCampaignName() + ").");
        if (newObj != null && newObj.getCampaignId() > 0) {
            statusMsg.setMessage("Campaign Details sent for update into Registry!", StatusMessage.MSG_CLASS_INFO);
            //refreshCountryList(newObj,2);
            reloadList(newObj, 1, false, false, false, false, false, false, false, false, false, false);
        } else {
            dbConn.logUIMsg(RLogger.MSG_TYPE_ERROR, RLogger.LOGGING_LEVEL_ERROR, "AdnetworkBean :: onEdit() :: Invalid Platform Id (" + newObj.getAdnetworkId() + ").");
            statusMsg.setMessage("Invalid Camapign Id `" + newObj.getCampaignId() + "`. Please select a Campaign to update.", StatusMessage.MSG_CLASS_ERROR);
        }

    }

    public Map<String, String> getAdnetworkList() {
        return adnetworkList;
    }

    public void setAdnetworkList(Map<String, String> adnetworkList) {
        this.adnetworkList = adnetworkList;
    }

    public String getSelectedAdnetwork() {
        return selectedAdnetwork;
    }

    public void setSelectedAdnetwork(String selectedAdnetwork) {
        this.selectedAdnetwork = selectedAdnetwork;
    }

    public Map<String, String> getOperatorList() {
        return operatorList;
    }

    public void setOperatorList(Map<String, String> operatorList) {
        this.operatorList = operatorList;
    }

    public String getSelectedOperator() {
        return selectedOperator;
    }

    public void setSelectedOperator(String selectedOperator) {
        this.selectedOperator = selectedOperator;
    }

    public Map<String, String> getOperatorCircleList() {
        return operatorCircleList;
    }

    public void setOperatorCircleList(Map<String, String> operatorCircleList) {
        this.operatorCircleList = operatorCircleList;
    }

    public String getSelectedCircle() {
        return selectedCircle;
    }

    public void setSelectedCircle(String selectedCircle) {
        this.selectedCircle = selectedCircle;
    }

    public Map<String, String> getServiceList() {
        return serviceList;
    }

    public void setServiceList(Map<String, String> serviceList) {
        this.serviceList = serviceList;
    }

    public String getSelectedService() {
        return selectedService;
    }

    public void setSelectedService(String selectedService) {
        this.selectedService = selectedService;
    }

    public Map<String, String> getPortalList() {
        return portalList;
    }

    public void setPortalList(Map<String, String> portalList) {
        this.portalList = portalList;
    }

    public String getSelectedPortal() {
        return selectedPortal;
    }

    public void setSelectedPortal(String selectedPortal) {
        this.selectedPortal = selectedPortal;
    }

    public Map<String, String> getPackageList() {
        return packageList;
    }

    public void setPackageList(Map<String, String> packageList) {
        this.packageList = packageList;
    }

    public String getSelectedPackage() {
        return selectedPackage;
    }

    public void setSelectedPackage(String selectedPackage) {
        this.selectedPackage = selectedPackage;
    }

    public WAPCampaign getNewCampaign() {
        return newCampaign;
    }

    public void setNewCampaign(WAPCampaign newCampaign) {
        this.newCampaign = newCampaign;
    }

    public List<WAPCampaign> getCampaignList() {
        return campaignList;
    }

    public void setCampaignList(List<WAPCampaign> campaignList) {
        this.campaignList = campaignList;
    }

    public WAPCampaign getSelectedCampaign() {
        return selectedCampaign;
    }

    public void setSelectedCampaign(WAPCampaign selectedCampaign) {
        this.selectedCampaign = selectedCampaign;
    }

    public StatusMessage getStatusMsg() {
        return statusMsg;
    }

    public void setStatusMsg(StatusMessage statusMsg) {
        this.statusMsg = statusMsg;
    }

    public int getLoginId() {
        return loginId;
    }

    public void setLoginId(int loginId) {
        this.loginId = loginId;
    }

    public int getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getLoginName() {
        return loginName;
    }

    public void setLoginName(String loginName) {
        this.loginName = loginName;
    }

    public String getSelectedChargingCategory() {
        return selectedChargingCategory;
    }

    public void setSelectedChargingCategory(String selectedChargingCategory) {
        this.selectedChargingCategory = selectedChargingCategory;
    }

    public String getSelectedContentType() {
        return selectedContentType;
    }

    public void setSelectedContentType(String selectedContentType) {
        this.selectedContentType = selectedContentType;
    }

    public Map<String, String> getContentTypeList() {
        return contentTypeList;
    }

    public void setContentTypeList(Map<String, String> contentTypeList) {
        this.contentTypeList = contentTypeList;
    }

    public String getSelectedPricePoint() {
        return selectedPricePoint;
    }

    public void setSelectedPricePoint(String selectedPricePoint) {
        this.selectedPricePoint = selectedPricePoint;
    }

    public Map<String, String> getPricePointList() {
        return pricePointList;
    }

    public void setPricePointList(Map<String, String> pricePointList) {
        this.pricePointList = pricePointList;
    }

    public Map<String, String> getPricePointListByCampaign() {
        return pricePointListByCampaign;
    }

    public void setPricePointListByCampaign(Map<String, String> pricePointListByCampaign) {
        this.pricePointListByCampaign = pricePointListByCampaign;
    }

}
