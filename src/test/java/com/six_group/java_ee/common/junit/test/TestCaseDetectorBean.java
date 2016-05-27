package com.six_group.java_ee.common.junit.test;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.junit.Ignore;
import org.junit.runner.Result;

@Singleton
@Startup
@Ignore
public class TestCaseDetectorBean {

    @Inject
    private Instance<ITestCaseImpl> _testables;

    @Resource
    private TimerService _timerService;

    @PostConstruct
    private void init() {
        final TimerConfig timerConfig = new TimerConfig();
        timerConfig.setPersistent(false);
        timerConfig.setInfo("Test-Execution");
        _timerService.createSingleActionTimer(1000, timerConfig);
    }

    @Timeout
    public void onTimeout(final Timer timer) {
        final List<Class<?>> classList = new ArrayList<>();
        for (final ITestCaseImpl testCaseImpl : _testables) {
            classList.add(testCaseImpl.getClass());
        }    
        final Result result = org.junit.runner.JUnitCore.runClasses(classList.toArray(new Class[0]));
        System.out.println("RunCount: " + result.getRunCount());
    }

}
