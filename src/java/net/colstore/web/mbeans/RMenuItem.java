/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.colstore.web.mbeans;

/**
 *
 * @author Nilesh Kumar Singh
 */
public class RMenuItem  implements java.io.Serializable{
    int menuId;
    String menuName;
    String targetPage;
    String iconImage;
    String menuDescp;
    String hintText;
    int status;
    int showOrder;
    int parentMenuId;
    int systemMenu;
    int hasSubMenu;
    
    public RMenuItem(){}

    public int getMenuId() {
        return menuId;
    }

    public void setMenuId(int menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public String getTargetPage() {
        return targetPage;
    }

    public void setTargetPage(String targetPage) {
        this.targetPage = targetPage;
    }

    public String getIconImage() {
        return iconImage;
    }

    public void setIconImage(String iconImage) {
        this.iconImage = iconImage;
    }

    public String getMenuDescp() {
        return menuDescp;
    }

    public void setMenuDescp(String menuDescp) {
        this.menuDescp = menuDescp;
    }

    public String getHintText() {
        return hintText;
    }

    public void setHintText(String hintText) {
        this.hintText = hintText;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getShowOrder() {
        return showOrder;
    }

    public void setShowOrder(int showOrder) {
        this.showOrder = showOrder;
    }

    public int getParentMenuId() {
        return parentMenuId;
    }

    public void setParentMenuId(int parentMenuId) {
        this.parentMenuId = parentMenuId;
    }

    public int getSystemMenu() {
        return systemMenu;
    }

    public void setSystemMenu(int systemMenu) {
        this.systemMenu = systemMenu;
    }

    public int getHasSubMenu() {
        return hasSubMenu;
    }

    public void setHasSubMenu(int hasSubMenu) {
        this.hasSubMenu = hasSubMenu;
    }
    
}
