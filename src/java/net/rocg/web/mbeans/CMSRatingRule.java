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
public class CMSRatingRule implements java.io.Serializable {
  int rule_id;
  String rule_name;
  int status;
  int sub_rating_engine;
  int event_rating_engine;
  int vpack_rating_engine;
  String fallback_method; 
  int enable_subscription;
  int enable_event_charging;
  int enable_vpack_charging;
  String subscription_catg;
  String happy_hr_policy;
  String happy_hours;
  String free_policy;
  int free_policy_val;

    public int getRule_id() {
        return rule_id;
    }

    public void setRule_id(int rule_id) {
        this.rule_id = rule_id;
    }

    public String getRule_name() {
        return rule_name;
    }

    public void setRule_name(String rule_name) {
        this.rule_name = rule_name;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getSub_rating_engine() {
        return sub_rating_engine;
    }

    public void setSub_rating_engine(int sub_rating_engine) {
        this.sub_rating_engine = sub_rating_engine;
    }

    public int getEvent_rating_engine() {
        return event_rating_engine;
    }

    public void setEvent_rating_engine(int event_rating_engine) {
        this.event_rating_engine = event_rating_engine;
    }

    public int getVpack_rating_engine() {
        return vpack_rating_engine;
    }

    public void setVpack_rating_engine(int vpack_rating_engine) {
        this.vpack_rating_engine = vpack_rating_engine;
    }

    public String getFallback_method() {
        return fallback_method;
    }

    public void setFallback_method(String fallback_method) {
        this.fallback_method = fallback_method;
    }

    public int getEnable_subscription() {
        return enable_subscription;
    }

    public void setEnable_subscription(int enable_subscription) {
        this.enable_subscription = enable_subscription;
    }

    public int getEnable_event_charging() {
        return enable_event_charging;
    }

    public void setEnable_event_charging(int enable_event_charging) {
        this.enable_event_charging = enable_event_charging;
    }

    public int getEnable_vpack_charging() {
        return enable_vpack_charging;
    }

    public void setEnable_vpack_charging(int enable_vpack_charging) {
        this.enable_vpack_charging = enable_vpack_charging;
    }

    public String getSubscription_catg() {
        return subscription_catg;
    }

    public void setSubscription_catg(String subscription_catg) {
        this.subscription_catg = subscription_catg;
    }

    public String getHappy_hr_policy() {
        return happy_hr_policy;
    }

    public void setHappy_hr_policy(String happy_hr_policy) {
        this.happy_hr_policy = happy_hr_policy;
    }

    public String getHappy_hours() {
        return happy_hours;
    }

    public void setHappy_hours(String happy_hours) {
        this.happy_hours = happy_hours;
    }

    public String getFree_policy() {
        return free_policy;
    }

    public void setFree_policy(String free_policy) {
        this.free_policy = free_policy;
    }

    public int getFree_policy_val() {
        return free_policy_val;
    }

    public void setFree_policy_val(int free_policy_val) {
        this.free_policy_val = free_policy_val;
    }
  
  
}
