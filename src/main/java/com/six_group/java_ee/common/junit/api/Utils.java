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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sorter;

public class Utils {

    public static void filter(Runner runner, Filter filter) throws NoTestsRemainException {
        try {
            runner.getClass().getMethod("filter", Filter.class).invoke(runner, filter);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof NoTestsRemainException) {
                throw (NoTestsRemainException)e.getCause();
            } else {
                throw new RuntimeException(e.getTargetException());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void sort(Runner runner, Sorter sorter) {
        try {
            runner.getClass().getMethod("sort", Sorter.class).invoke(runner, sorter);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    
    public static String toString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] b = new byte[16384];
        
        int read;
        do {
            read = in.read(b);
            if (read > 0) {
                out.write(b, 0, read);
            }
            
        } while (read != -1);
        
        return out.toString();
    }

    @SuppressWarnings("unchecked")
    public static Runner createRunner(String runnerClassName, Class<?> testClass) {
        try {
            return createRunner((Class<? extends Runner>)Class.forName(runnerClassName), testClass);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to create instance of " + runnerClassName, e);
        }
    }
    
    public static Runner createRunner(Class<? extends Runner> runnerClass, Class<?> testClass) {
        Constructor<? extends Runner> c;
        try {
            c = runnerClass.getDeclaredConstructor(Class.class);
            return c.newInstance(testClass);
        } catch (NoSuchMethodException e) {
                try {
                    return runnerClass.newInstance();
                } catch (Exception e1) {
                    throw new RuntimeException("Unable to create instanceof " + runnerClass, e);
                }
        } catch (Exception e) {
            throw new RuntimeException("Unable to create instanceof " + runnerClass, e);
        }
    }
    
    public static <A extends Annotation> A findAnnotation(Class<?> clazz, Class<A> annotationType) {
        A annotation = clazz.getAnnotation(annotationType);
        if (annotation != null) {
            return annotation;
        }
        for (Class<?> ifc : clazz.getInterfaces()) {
            annotation = findAnnotation(ifc, annotationType);
            if (annotation != null) {
                return annotation;
            }
        }
        if (!Annotation.class.isAssignableFrom(clazz)) {
            for (Annotation ann : clazz.getAnnotations()) {
                annotation = findAnnotation(ann.annotationType(), annotationType);
                if (annotation != null) {
                    return annotation;
                }
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass == null || superClass.equals(Object.class)) {
            return null;
        }
        return findAnnotation(superClass, annotationType);
    }




}
