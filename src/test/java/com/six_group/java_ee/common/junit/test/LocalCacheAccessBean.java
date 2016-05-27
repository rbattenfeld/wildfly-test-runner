package com.six_group.java_ee.common.junit.test;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

@Stateless
public class LocalCacheAccessBean {

   @Resource
   private SessionContext context;

   /**
    * Initialize and store the context for the EJB invocations.
    */
   @PostConstruct
   public void init() {
   }

   public String sayHello() {
       return "hello";
   }
}
