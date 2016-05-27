/**
 * junit-remote
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * @author Tradeshift - http://www.tradeshift.com
 */

package com.six_group.java_ee.common.junit.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.six_group.java_ee.common.junit.client.WildflyTestRunner;

/**
 * Mark a test class as remote.
 * 
 * Use this on either a class itself or any of it's superclasses.
 * 
 * The annotation will only be picked up if the class is also {@link RunWith}({@link WildflyTestRunner}.class)
 * 
 * @author recht
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface DeploymentClass {

	String deploymentClass();
    
}
