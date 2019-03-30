/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.rocg.web.mbeans;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Mobigosssip
 */
@ManagedBean
@SessionScoped
public class TemplateBuilderBean {

    public int createNewTemplate=1;
    boolean disableTemplateSelection;
    boolean disableNewTemplateCreation;
    /**
     * Creates a new instance of TemplateBuilderBean
     */
    public TemplateBuilderBean() {
    }

    public int getCreateNewTemplate() {
        return createNewTemplate;
    }

    public void setCreateNewTemplate(int createNewTemplate) {
        this.setDisableNewTemplateCreation(createNewTemplate>0?false:true);
        this.setDisableTemplateSelection(createNewTemplate>0?true:false);
        this.createNewTemplate = createNewTemplate;
    }

    public boolean isDisableTemplateSelection() {
        return disableTemplateSelection;
    }

    public void setDisableTemplateSelection(boolean disableTemplateSelection) {
        this.disableTemplateSelection = disableTemplateSelection;
    }

    public boolean isDisableNewTemplateCreation() {
        return disableNewTemplateCreation;
    }

    public void setDisableNewTemplateCreation(boolean disableNewTemplateCreation) {
        this.disableNewTemplateCreation = disableNewTemplateCreation;
    }
    
}
