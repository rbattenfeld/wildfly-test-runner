package com.six_group.java_ee.common.junit.api;

import org.jboss.shrinkwrap.api.Archive;

public interface IDeployable {
    Archive<?> getDeplyoment();
}
