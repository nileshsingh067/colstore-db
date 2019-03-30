package net.rocg.web.mbeans;

import java.io.PrintStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import net.rocg.util.DBConnection;
import net.rocg.util.RUtil;
import net.rocg.util.StatusMessage;
import net.rocg.web.beans.CMSContent;
import org.primefaces.event.RowEditEvent;

@ManagedBean
@SessionScoped
public class PortalAutoDldContentSchedulingBean
{
  DBConnection dbConn;
  int loginId;
  int roleId;
  StatusMessage statusMsg;
  String loginName;
  String selectedOperatorId;
  String selectedPortalId;
  String selectedContentType;
  String selectedCategory;
  String selectedSubCategory;
  String selectedContentProvider;
  Map<String, String> portalList;
  Map<String, String> contentTypeList;
  Map<String, String> contentCategoryList;
  Map<String, String> contentSubCategoryList;
  Map<String, String> contentCPList;
  Map<String, String> operatorList;
  List<CMSContent> availableCMSContent;
  List<CMSContent> portalMappedContent;
  List<CMSContent> selectedCMSContents;
  List<CMSContent> selectedPortalContents;
  String contentBasePath;
  String contentBaseURL;
  
  public PortalAutoDldContentSchedulingBean()
  {
    this.availableCMSContent = new ArrayList();
    this.portalMappedContent = new ArrayList();
    this.statusMsg = new StatusMessage();
    this.dbConn = new DBConnection();
    fetchLoginDetails();
    this.contentBasePath = RUtil.getStringProperty("cms_content_basefolder", "");
    this.contentBaseURL = RUtil.getStringProperty("cms_content_baseurl", "");
    this.portalList = new HashMap();
    this.contentTypeList = new HashMap();
    this.contentCategoryList = new HashMap();
    this.contentSubCategoryList = new HashMap();
    this.contentCPList = new HashMap();
    this.operatorList = new HashMap();
    fetchData(true, false, true, false, false, false, false, false, false);
  }
  
  public void fetchData(boolean reloadPortalList, boolean reloadOperatorList, boolean reloadContentTypeList, boolean reloadCPList, boolean reloadCategoryList, boolean reloadSubCategory, boolean reloadContent, boolean reloadPortalContent, boolean addContent)
  {
    Connection conn = this.dbConn.connect();
    System.out.println("selectedPortal " + this.selectedPortalId + ", selectedContentType " + this.selectedContentType + ", selectedContentProvider=" + this.selectedContentProvider + ", selectedCategory " + this.selectedCategory + ", selectedSubCategory " + this.selectedSubCategory);
    if (conn != null)
    {
      this.dbConn.logUIMsg(0, 2, "PortalHomeContentSchedulingBean.class :: fetchData() :: Database Connected.");
      try
      {
        Statement st = conn.createStatement();
        if (reloadPortalList) {
          reloadPortalList(st);
        }
        if (reloadContentTypeList) {
          reloadContentTypeList(st);
        }
        if (reloadCPList) {
          reloadCPList(st);
        }
        if (reloadCategoryList) {
          reloadContentCategoryList(st);
        }
        if (reloadSubCategory) {
          reloadSubCategoryList(st);
        }
        if (reloadContent) {
          reloadContentRecords(st);
        }
        if (reloadPortalContent) {
          reloadPortalContentRecords(st);
        }
        if (reloadOperatorList) {
          reloadOperatorList(st);
        }
        if (addContent)
        {
          if (st != null) {
            st.close();
          }
          st = null;
          int opId = RUtil.strToInt(getSelectedOperatorId(), 0);
          if (opId <= 0)
          {
            this.dbConn.logUIMsg(2, 0, "PortalHomeContentSchedulingBean.class :: fetchData() ::Invalid Operator Id , Please select an operator to schedule content for.");
            this.statusMsg.setMessage("Please select an operator to schedule content for portal.", "errormsg");
          }
          else if (this.selectedCMSContents.size() > 0)
          {
            PreparedStatement pst = conn.prepareStatement("insert into tb_cms_contents_auto_download(content_id,portal_id,dld_flag,operator_id,mapping_date,mapped_by,STATUS,show_order) values(?,"+this.getSelectedPortalId()+",1,"+this.getSelectedOperatorId()+",now(),"+this.getLoginId()+",1,?);");
            for (CMSContent newC : this.selectedCMSContents)
            {
              pst.setLong(1, newC.getContentId());
              pst.setInt(2, newC.getShowOrder());
              pst.executeUpdate();
            }
            pst.close();pst = null;
            st = conn.createStatement();
            reloadContentRecords(st);
            reloadPortalContentRecords(st);
          }
        }
      }
      catch (Exception e)
      {
        this.dbConn.logUIMsg(2, 0, "PortalHomeContentSchedulingBean.class :: fetchData() :: Exception " + e);
      }
      finally
      {
        try
        {
          if (conn != null) {
            conn.close();
          }
        }
        catch (Exception ee) {}
        conn = null;
      }
    }
    else
    {
      this.dbConn.logUIMsg(2, 0, "PortalAutoDldContentSchedulingBean.class :: fetchData() :: Database Connectivity Failed");
      this.statusMsg.setMessage("A process failed while reloading Country List. Database Connectivity Issue.", "errormsg");
    }
  }
  
  public void removeContentFromPortal()
  {
    if (getSelectedPortalContents().size() > 0)
    {
      String sql1 = "delete from tb_cms_contents_auto_download where operator_id="+this.getSelectedOperatorId()+" and dld_flag=1 and content_id in (";
      for (CMSContent ct : getSelectedPortalContents()) {
        sql1 = sql1 + ct.getContentId() + ",";
      }
      if (sql1.endsWith(",")) {
        sql1 = sql1.substring(0, sql1.lastIndexOf(","));
      }
      sql1 = sql1 + ")";
      int rep = 0;
      this.dbConn.logUIMsg(2, 0, "PortalHomeContentSchedulingBean.class :: removeContentFromPortal() :: sql " + sql1);
      Connection conn = this.dbConn.connect();
      if (conn != null) {
        try
        {
          Statement st = conn.createStatement();
          rep = st.executeUpdate(sql1);
          this.statusMsg.setMessage("Database Updated successfully!", "infomsg");
          reloadPortalContentRecords(st);
        }
        catch (Exception e)
        {
          this.dbConn.logUIMsg(2, 0, "PortalHomeContentSchedulingBean.class :: removeContentFromPortal() :: Exception " + e);
          this.statusMsg.setMessage("Failed to update database.", "errormsg");
        }
        finally
        {
          try
          {
            if (conn != null) {
              conn.close();
            }
          }
          catch (Exception ee) {}
          conn = null;
        }
      }
    }
    else
    {
      this.dbConn.logUIMsg(2, 0, "PortalAutoldContentSchedulingBean.class :: removeContentFromPortal() :: No content selected to remove from portal.");
      this.statusMsg.setMessage(" Please select a Content to remove.", "errormsg");
    }
  }
  
  public void onEdit(RowEditEvent event)
  {
    CMSContent actionObj = (CMSContent)event.getObject();
    if ((actionObj != null) && (actionObj.getContentId() > 0L))
    {
      String sql1 = "update tb_cms_contents_auto_download set show_order="+actionObj.getShowOrder()+" where portal_id="+this.getSelectedPortalId()+" and dld_flag=1 and content_id="+actionObj.getContentId()+";";
      int rep = 0;
      this.dbConn.logUIMsg(2, 0, "PortalAutoldContentSchedulingBean.class :: onEdit() :: sql " + sql1);
      Connection conn = this.dbConn.connect();
      if (conn != null) {
        try
        {
          Statement st = conn.createStatement();
          rep = st.executeUpdate(sql1);
          this.statusMsg.setMessage("Database Updated successfully!", "infomsg");
          reloadPortalContentRecords(st);
        }
        catch (Exception e)
        {
          this.dbConn.logUIMsg(2, 0, "PortalAutoldContentSchedulingBean.class :: onEdit() :: Exception " + e);
          this.statusMsg.setMessage("Failed to update database.", "errormsg");
        }
        finally
        {
          try
          {
            if (conn != null) {
              conn.close();
            }
          }
          catch (Exception ee) {}
          conn = null;
        }
      }
    }
    else
    {
      this.dbConn.logUIMsg(2, 0, "PortalAutoldContentSchedulingBean.class :: onEdit() :: Invalid Content Id (" + actionObj.getContentId() + ") to update.");
      this.statusMsg.setMessage("Invalid Content Id `" + actionObj.getContentId() + "`. Please select a Content to update.", "errormsg");
    }
  }
  
  public void reloadPortalContentRecords(Statement st)
  {
    ResultSet rs = null;
    try
    {
      System.out.println("reloadPortalContentRecords() :: 1");
      StringBuilder sb = new StringBuilder("SELECT c.`content_id`,c.show_order,a.`cat_nr`,a.`content_type_id`,a.`category_id`,a.`copyright_id`,a.`cp_id`,a.`cp_catnr`,a.`content_name`, a.content_folder,CONCAT(a.`content_url`,b.`file_name`,\"_\",b.`parts_count`,\".\",b.`file_extension`) AS content_url FROM tb_cms_contents_auto_download c, tb_cms_contents a, tb_cms_contents_files b WHERE c.operator_id=" + getSelectedOperatorId() + " and c.portal_id=" + getSelectedPortalId() + " and dld_flag=1 and c.content_id=a.content_id and a.`cat_nr`=b.`cat_nr` AND a.status>=1 AND b.`preview_file`=1 AND b.`file_name` LIKE '%50x50' ");
      
      System.out.println("reloadPortalContentRecords() :: 2 :: " + sb.toString());
      if (RUtil.strToInt(this.selectedSubCategory, 0) > 0) {
        sb.append(" and a.category_id=").append(getSelectedSubCategory());
      }
      System.out.println("reloadPortalContentRecords() :: 4 :: " + sb.toString());
      if (RUtil.strToInt(this.selectedContentType, 0) > 0) {
        sb.append(" and a.content_type_id=").append(getSelectedContentType());
      }
      System.out.println("reloadPortalContentRecords() :: 5 :: " + sb.toString());
      sb.append(" order by c.show_order desc;");
      String sql1 = sb.toString();
      
      this.dbConn.logUIMsg(0, 2, "PortalAutoldContentSchedulingBean.class :: reloadPortalContentRecords() :: Query : " + sql1);
      rs = st.executeQuery(sql1);
      CMSContent newContent = null;
      this.portalMappedContent.clear();
      String contentBaseURL = getContentBaseURL();
      while (rs.next())
      {
        newContent = new CMSContent();
        newContent.setContentId(rs.getLong("content_id"));
        newContent.setShowOrder(rs.getInt("show_order"));
        newContent.setCatalogNumber(rs.getLong("cat_nr"));
        newContent.setContentTypeId(rs.getInt("content_type_id"));
        newContent.setCategoryId(rs.getInt("category_id"));
        newContent.setCopyrightId(rs.getInt("copyright_id"));
        newContent.setContentProviderId(rs.getInt("cp_id"));
        newContent.setCpcatalogNumber(rs.getString("cp_catnr"));
        newContent.setContentName(rs.getString("content_name"));
        newContent.setContentFolder(rs.getString("content_folder"));
        newContent.setContentPath(rs.getString("content_url"));
        this.portalMappedContent.add(newContent);
        newContent = null;
      }
      if (rs != null) {
        rs.close();
      }
      rs = null;
    }
    catch (Exception e)
    {
      this.dbConn.logUIMsg(2, 0, "PortalAutoldContentSchedulingBean.class :: reloadContentRecords() :: Exception " + e);
    }
    finally
    {
      try
      {
        if (rs != null) {
          rs.close();
        }
      }
      catch (Exception ee) {}
      rs = null;
    }
  }
  
  public void reloadContentRecords(Statement st)
  {
    ResultSet rs = null;
    try
    {
      System.out.println("reloadContentRecords() :: 1");
      StringBuilder sb = new StringBuilder("SELECT a.`content_id`,a.`cat_nr`,a.`content_type_id`,a.`category_id`,a.`copyright_id`,a.`cp_id`,a.`cp_catnr`,a.`content_name`, a.content_folder,CONCAT(a.`content_url`,b.`file_name`,\"_\",b.`parts_count`,\".\",b.`file_extension`) AS content_url FROM tb_cms_contents a, tb_cms_contents_files b WHERE a.`content_id` not in (SELECT content_id FROM tb_cms_contents_auto_download WHERE portal_id=" + getSelectedPortalId() + " and dld_flag=1 and operator_id=" + getSelectedOperatorId() + ") and a.`cat_nr`=b.`cat_nr` AND a.status>=1 AND b.`preview_file`=1 AND b.`file_name` LIKE '%50x50'");
      
      System.out.println("reloadContentRecords() :: 2 :: " + sb.toString());
      if (RUtil.strToInt(this.selectedSubCategory, 0) > 0) {
        sb.append(" and a.category_id=").append(getSelectedSubCategory());
      }
      System.out.println("reloadContentRecords() :: 4 :: " + sb.toString());
      if (RUtil.strToInt(this.selectedContentType, 0) > 0) {
        sb.append(" and a.content_type_id=").append(getSelectedContentType());
      }
      if (RUtil.strToInt(this.selectedContentProvider, 0) > 0) {
        sb.append(" and a.cp_id=").append(getSelectedContentProvider());
      }
      System.out.println("reloadContentRecords() :: 5 :: " + sb.toString());
      sb.append(" order by a.content_id desc;");
      String sql1 = sb.toString();
      
      this.dbConn.logUIMsg(0, 2, "PortalAutoldContentSchedulingBean.class :: reloadContentRecords() :: Query : " + sql1);
      rs = st.executeQuery(sql1);
      CMSContent newContent = null;
      this.availableCMSContent.clear();
      String contentBaseURL = getContentBaseURL();
      while (rs.next())
      {
        newContent = new CMSContent();
        newContent.setContentId(rs.getLong("content_id"));
        newContent.setCatalogNumber(rs.getLong("cat_nr"));
        newContent.setContentTypeId(rs.getInt("content_type_id"));
        newContent.setCategoryId(rs.getInt("category_id"));
        newContent.setCopyrightId(rs.getInt("copyright_id"));
        newContent.setContentProviderId(rs.getInt("cp_id"));
        newContent.setCpcatalogNumber(rs.getString("cp_catnr"));
        newContent.setContentName(rs.getString("content_name"));
        newContent.setContentFolder(rs.getString("content_folder"));
        newContent.setContentPath(rs.getString("content_url"));
        this.availableCMSContent.add(newContent);
        newContent = null;
      }
      if (rs != null) {
        rs.close();
      }
      rs = null;
    }
    catch (Exception e)
    {
      this.dbConn.logUIMsg(2, 0, "PortalAutoldContentSchedulingBean.class :: reloadContentRecords() :: Exception " + e);
    }
    finally
    {
      try
      {
        if (rs != null) {
          rs.close();
        }
      }
      catch (Exception ee) {}
      rs = null;
    }
  }
  
  public void reloadCPList(Statement st)
  {
    try
    {
      String sql1 = "";
      if ((getRoleId() == 1) || (getRoleId() == 2)) {
        sql1 = "SELECT a.cp_user_id,CONCAT(b.user_name,' (',c.company_name,')') AS cp_name FROM tb_portal_cp_mapping a, tb_users b,tb_registered_companies c WHERE a.cp_user_id=b.user_id AND b.role_id=3 AND b.company_id=c.company_id AND  a.portal_id=" + getSelectedPortalId() + ";";
      } else {
        sql1 = "SELECT a.cp_user_id,CONCAT(b.user_name,' (',c.company_name,')') AS cp_name FROM tb_portal_cp_mapping a, tb_users b,tb_registered_companies c WHERE a.cp_user_id=b.user_id AND b.role_id=3 AND b.company_id=c.company_id AND  a.portal_id=" + getSelectedPortalId() + " and a.cp_user_id=" + getLoginId() + ";";
      }
      this.dbConn.logUIMsg(0, 2, "PortalHomeContentSchedulingBean.class :: reloadCPList() :: Query : " + sql1);
      ResultSet rs = st.executeQuery(sql1);
      this.contentCPList.clear();
      while (rs.next()) {
        this.contentCPList.put(rs.getString("cp_name"), rs.getString("cp_user_id"));
      }
      this.dbConn.logUIMsg(0, 2, "PortalAutoldContentSchedulingBean.class :: reloadCPList() ::  Collection Size " + this.portalList.size());
      if (rs != null) {
        rs.close();
      }
      rs = null;
    }
    catch (Exception e)
    {
      this.dbConn.logUIMsg(2, 0, "PortalAutoldContentSchedulingBean.class :: reloadCPList() :: Exception " + e.getMessage());
    }
  }
  
  public void reloadOperatorList(Statement st)
  {
    try
    {
      String sql1 = "";
      sql1 = "SELECT DISTINCT a.operator_id,CONCAT(c.`country_name`,\"-\",b.operator_name) AS operator_name FROM tb_rating_operator_configurations a , tb_operators b,tb_country c WHERE a.`operator_id`=b.`operator_id` AND b.`country_id`=c.`country_id` AND a.`portal_id`=" + getSelectedPortalId() + ";";
      this.dbConn.logUIMsg(0, 2, "PortalHomeContentSchedulingBean.class :: reloadOperatorList() :: Query : " + sql1);
      ResultSet rs = st.executeQuery(sql1);
      this.operatorList.clear();
      while (rs.next()) {
        this.operatorList.put(rs.getString("operator_name"), rs.getString("operator_id"));
      }
      this.dbConn.logUIMsg(0, 2, "PortalHomeContentSchedulingBean.class :: reloadOperatorList() ::  Collection Size " + this.portalList.size());
      if (rs != null) {
        rs.close();
      }
      rs = null;
    }
    catch (Exception e)
    {
      this.dbConn.logUIMsg(2, 0, "PortalAutoldContentSchedulingBean.class :: reloadOperatorList() :: Exception " + e.getMessage());
    }
  }
  
  public void reloadPortalList(Statement st)
  {
    try
    {
      String sql1 = "";
      if ((getRoleId() == 1) || (getRoleId() == 2)) {
        sql1 = "SELECT DISTINCT portal_name,portal_id FROM vw_portal_services;";
      } else {
        sql1 = "SELECT DISTINCT portal_name,portal_id FROM vw_portal_services WHERE user_id=" + getLoginId() + ";";
      }
      this.dbConn.logUIMsg(0, 2, "PortalHomeContentSchedulingBean.class :: reloadPortalList() :: Query : " + sql1);
      ResultSet rs = st.executeQuery(sql1);
      this.portalList.clear();
      while (rs.next()) {
        this.portalList.put(rs.getString("portal_name"), rs.getString("portal_id"));
      }
      this.dbConn.logUIMsg(0, 2, "PortalAutoldContentSchedulingBean.class :: reloadPortalList() ::  Collection Size " + this.portalList.size());
      if (rs != null) {
        rs.close();
      }
      rs = null;
    }
    catch (Exception e)
    {
      this.dbConn.logUIMsg(2, 0, "PortalAutoldContentSchedulingBean.class :: reloadPortalList() :: Exception " + e.getMessage());
    }
  }
  
  public void reloadContentTypeList(Statement st)
  {
    try
    {
      this.contentTypeList.clear();
      String sql1 = "SELECT content_type_id,content_type_name FROM tb_cms_content_type WHERE STATUS>0 ORDER BY content_type_name;";
      this.dbConn.logUIMsg(0, 2, "PortalHomeContentSchedulingBean.class :: reloadContentTypeList() :: Query : " + sql1);
      ResultSet rs = st.executeQuery(sql1);
      while (rs.next()) {
        this.contentTypeList.put(rs.getString("content_type_name"), rs.getString("content_type_id"));
      }
      this.dbConn.logUIMsg(0, 2, "PortalAutoldContentSchedulingBean.class :: reloadContentTypeList() ::  Collection Size " + this.contentTypeList.size());
      if (rs != null) {
        rs.close();
      }
      rs = null;
    }
    catch (Exception e)
    {
      this.dbConn.logUIMsg(2, 0, "PortalAutoldContentSchedulingBean.class :: reloadContentTypeList() :: Exception " + e.getMessage());
    }
  }
  
  public void reloadSubCategoryList(Statement st)
  {
    try
    {
      int catId = RUtil.strToInt(this.selectedCategory, -1);
      this.contentSubCategoryList.clear();
      String sql1 = "SELECT catg_id,catg_name FROM tb_content_categories WHERE STATUS>0 AND parent_catg>0 and parent_catg=" + catId + " and catg_id in (SELECT DISTINCT category_id FROM tb_cms_contents WHERE content_type_id='" + getSelectedContentType() + "' and cp_id=" + this.selectedContentProvider + ") ORDER BY catg_name;";
      
      this.dbConn.logUIMsg(0, 2, "PortalHomeContentSchedulingBean.class :: reloadSubCategoryList() :: Query : " + sql1);
      ResultSet rs = st.executeQuery(sql1);
      while (rs.next()) {
        this.contentSubCategoryList.put(rs.getString("catg_name"), rs.getString("catg_id"));
      }
      this.dbConn.logUIMsg(0, 2, "PortalHomeContentSchedulingBean.class :: reloadSubCategoryList() ::  Collection Size " + this.contentSubCategoryList.size());
      if (rs != null) {
        rs.close();
      }
      rs = null;
    }
    catch (Exception e)
    {
      this.dbConn.logUIMsg(2, 0, "PortalHomeContentSchedulingBean.class :: reloadSubCategoryList() :: Exception " + e.getMessage());
    }
  }
  
  public void reloadContentCategoryList(Statement st)
  {
    try
    {
      this.contentCategoryList.clear();
      String sql1 = "SELECT catg_id,catg_name FROM tb_content_categories WHERE STATUS>0 AND parent_catg=0 and catg_id in (SELECT DISTINCT parent_catg FROM tb_content_categories WHERE catg_id IN (SELECT DISTINCT category_id FROM tb_cms_contents WHERE content_type_id='" + getSelectedContentType() + "' and cp_id=" + getSelectedContentProvider() + ")) ORDER BY catg_name;";
      this.dbConn.logUIMsg(0, 2, "PortalHomeContentSchedulingBean.class :: reloadContentCategoryList() :: Query : " + sql1);
      ResultSet rs = st.executeQuery(sql1);
      while (rs.next()) {
        this.contentCategoryList.put(rs.getString("catg_name"), rs.getString("catg_id"));
      }
      this.dbConn.logUIMsg(0, 2, "PortalHomeContentSchedulingBean.class :: reloadContentCategoryList() ::  Collection Size " + this.contentCategoryList.size());
      if (rs != null) {
        rs.close();
      }
      rs = null;
    }
    catch (Exception e)
    {
      this.dbConn.logUIMsg(2, 0, "PortalHomeContentSchedulingBean.class :: reloadContentCategoryList() :: Exception " + e.getMessage());
    }
  }
  
  public void fetchLoginDetails()
  {
    LoginBean loginBeanObj = null;
    try
    {
      FacesContext ctx = FacesContext.getCurrentInstance();
      ExternalContext extCtx = ctx.getExternalContext();
      Map<String, Object> sessionMap = extCtx.getSessionMap();
      loginBeanObj = (LoginBean)sessionMap.get("loginBean");
      setLoginId(loginBeanObj.getUserId());
      setRoleId(loginBeanObj.getRoleId());
      setLoginName(loginBeanObj.getUserName());
    }
    catch (Exception e)
    {
      this.dbConn.logUIMsg(2, 0, "PortalHomeContentSchedulingBean.class :: fetchLoginDetails() :: Exception " + e.getMessage());
    }
    loginBeanObj = null;
  }
  
  public String getSelectedSubCategory()
  {
    return this.selectedSubCategory;
  }
  
  public void setSelectedSubCategory(String selectedSubCategory)
  {
    this.selectedSubCategory = selectedSubCategory;
  }
  
  public String getSelectedCategory()
  {
    return this.selectedCategory;
  }
  
  public void setSelectedCategory(String selectedCategory)
  {
    this.selectedCategory = selectedCategory;
  }
  
  public Map<String, String> getContentSubCategoryList()
  {
    return this.contentSubCategoryList;
  }
  
  public void setContentSubCategoryList(Map<String, String> contentSubCategoryList)
  {
    this.contentSubCategoryList = contentSubCategoryList;
  }
  
  public Map<String, String> getContentCategoryList()
  {
    return this.contentCategoryList;
  }
  
  public void setContentCategoryList(Map<String, String> contentCategoryList)
  {
    this.contentCategoryList = contentCategoryList;
  }
  
  public Map<String, String> getContentTypeList()
  {
    return this.contentTypeList;
  }
  
  public void setContentTypeList(Map<String, String> contentTypeList)
  {
    this.contentTypeList = contentTypeList;
  }
  
  public String getSelectedContentType()
  {
    return this.selectedContentType;
  }
  
  public void setSelectedContentType(String selectedContentType)
  {
    this.selectedContentType = selectedContentType;
  }
  
  public String getSelectedPortalId()
  {
    return this.selectedPortalId;
  }
  
  public void setSelectedPortalId(String selectedPortalId)
  {
    this.selectedPortalId = selectedPortalId;
  }
  
  public Map<String, String> getPortalList()
  {
    return this.portalList;
  }
  
  public void setPortalList(Map<String, String> portalList)
  {
    this.portalList = portalList;
  }
  
  public int getLoginId()
  {
    return this.loginId;
  }
  
  public void setLoginId(int loginId)
  {
    this.loginId = loginId;
  }
  
  public int getRoleId()
  {
    return this.roleId;
  }
  
  public void setRoleId(int roleId)
  {
    this.roleId = roleId;
  }
  
  public StatusMessage getStatusMsg()
  {
    return this.statusMsg;
  }
  
  public void setStatusMsg(StatusMessage statusMsg)
  {
    this.statusMsg = statusMsg;
  }
  
  public String getLoginName()
  {
    return this.loginName;
  }
  
  public void setLoginName(String loginName)
  {
    this.loginName = loginName;
  }
  
  public String getContentBasePath()
  {
    return this.contentBasePath;
  }
  
  public void setContentBasePath(String contentBasePath)
  {
    this.contentBasePath = contentBasePath;
  }
  
  public String getContentBaseURL()
  {
    return this.contentBaseURL;
  }
  
  public void setContentBaseURL(String contentBaseURL)
  {
    this.contentBaseURL = contentBaseURL;
  }
  
  public List<CMSContent> getAvailableCMSContent()
  {
    return this.availableCMSContent;
  }
  
  public void setAvailableCMSContent(List<CMSContent> availableCMSContent)
  {
    this.availableCMSContent = availableCMSContent;
  }
  
  public List<CMSContent> getPortalMappedContent()
  {
    return this.portalMappedContent;
  }
  
  public void setPortalMappedContent(List<CMSContent> portalMappedContent)
  {
    this.portalMappedContent = portalMappedContent;
  }
  
  public List<CMSContent> getSelectedCMSContents()
  {
    return this.selectedCMSContents;
  }
  
  public void setSelectedCMSContents(List<CMSContent> selectedCMSContents)
  {
    this.selectedCMSContents = selectedCMSContents;
  }
  
  public List<CMSContent> getSelectedPortalContents()
  {
    return this.selectedPortalContents;
  }
  
  public void setSelectedPortalContents(List<CMSContent> selectedPortalContents)
  {
    this.selectedPortalContents = selectedPortalContents;
  }
  
  public String getSelectedContentProvider()
  {
    return this.selectedContentProvider;
  }
  
  public void setSelectedContentProvider(String selectedContentProvider)
  {
    this.selectedContentProvider = selectedContentProvider;
  }
  
  public Map<String, String> getContentCPList()
  {
    return this.contentCPList;
  }
  
  public void setContentCPList(Map<String, String> contentCPList)
  {
    this.contentCPList = contentCPList;
  }
  
  public Map<String, String> getOperatorList()
  {
    return this.operatorList;
  }
  
  public void setOperatorList(Map<String, String> operatorList)
  {
    this.operatorList = operatorList;
  }
  
  public String getSelectedOperatorId()
  {
    return this.selectedOperatorId;
  }
  
  public void setSelectedOperatorId(String selectedOperatorId)
  {
    this.selectedOperatorId = selectedOperatorId;
  }
}
