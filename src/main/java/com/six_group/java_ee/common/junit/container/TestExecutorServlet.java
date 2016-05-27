package com.six_group.java_ee.common.junit.container;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.six_group.java_ee.common.junit.arquillian.JUnitTestRunner;
import com.six_group.java_ee.common.junit.arquillian.TestResult;


@WebServlet(urlPatterns = { "/*" }, asyncSupported = false)
public class TestExecutorServlet extends HttpServlet {
	private static final long serialVersionUID = 3776514432162806144L;
    private static final Log _Logger = LogFactory.getLog(TestExecutorServlet.class);

	@Override
    public void init(final ServletConfig servletConfig) throws ServletException {
		_Logger.info("init");
    }

    @Override
    protected void service(final HttpServletRequest servletRequest, final HttpServletResponse servletResponse) throws ServletException, IOException {
		_Logger.info("service");
        try {
            final String clazz = servletRequest.getParameter("className");
            final String method = servletRequest.getParameter("methodName");
            final String httpMethod = servletRequest.getMethod();
            final String stateless = servletRequest.getParameter("stateless");
            final Object testObject = getTestObject(clazz, method, stateless);
            final Class<?> testClass = testObject.getClass();
            servletResponse.setBufferSize(512);
            final ServletOutputStream pw = servletResponse.getOutputStream();
            final ObjectOutputStream objectOutputStream = new ObjectOutputStream(pw);
            if ("POST".equalsIgnoreCase(httpMethod)) {
                servletResponse.setStatus(200);
                servletResponse.flushBuffer();
                try {
                	final JUnitTestRunner runner = new JUnitTestRunner();
                    final TestResult result = runner.execute(testClass, testObject, method);                    
                    objectOutputStream.writeObject(result);
                } catch (final Exception ex) {
                    objectOutputStream.writeObject(TestResult.failed(ex));
                }
            }
        } catch (final Exception ex) {
        	_Logger.error(ex.getMessage(), ex);
            servletResponse.sendError(500, ex.getMessage());
        }
    }

    //-----------------------------------------------------------------------||
    //-- Private Method -----------------------------------------------------||
    //-----------------------------------------------------------------------||
    
    private Object getTestObject(final String clazz, final String method, final String stateless) {
        if (stateless != null && !stateless.equals("null")) {
            return lookupTestObject(clazz, method);
        } else {
            return createTestObject(clazz);
        }
    }

    private Object lookupTestObject(final String clazz, final String method) {
        try {
	        final Context ctx = new InitialContext();
		    final Object obj = ctx.lookup("java:app/test/LocalCacheITCase");
//		    final Object obj = ctx.lookup("java:module/LocalCacheITCase");
//		    final ClassPool pool = ClassPool.getDefault();
//		    final CtClass cc = pool.getCtClass(clazz);
//		    final CtMethod methodDescriptor = cc.getDeclaredMethod(method);
//		    final ClassFile ccFile = cc.getClassFile();
//		    final ConstPool constpool = ccFile.getConstPool();
//		    final AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
//		    final Annotation annot = new Annotation("org.junit.Test", constpool);
//		    attr.addAnnotation(annot);
		    // add the annotation to the method descriptor
//		    methodDescriptor.getMethodInfo().addAttribute(attr);
		    return obj;
        } catch (final Exception ex) {
        	throw new RuntimeException(ex);
        }
    }
    

    private Object createTestObject(final String clazz) {
        try {
            final Class<?> testClass = Class.forName(clazz);
            final Constructor<?>[] constructors = testClass.getConstructors();
            return constructors[0].newInstance();
        } catch (final ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            throw new RuntimeException(ex);
        }
    }
    
//    private Class<?> createProxyForTestCaseClass(final Class<?> testClass, final String testMethodName) throws CannotCompileException, NotFoundException {
//        final ClassPool pool = ClassPool.getDefault();
//        final CtClass cc = pool.makeClass("com.six_group.java_ee.common.junit.client." + testClass.getSimpleName() + "TestCaseProxyImpl");
//        final ClassFile  classFile = cc.getClassFile();
//        final ConstPool cpool = classFile.getConstPool();
//        final Method[] methods = testClass.getDeclaredMethods();        
//        final CtClass testClassProxy = pool.get(Object.class.getName());
//        final CtField proxyField = new CtField(testClassProxy, "_proxy", cc);
//        final String methodStr = String.format("public void setTestProxy(%s proxy) { _proxy = proxy; }", Object.class.getName());
//        cc.addField(proxyField);
//        final CtMethod proxyMethod = CtNewMethod.make(methodStr, cc);
//        cc.addMethod(proxyMethod);
//        
//        for (Method method : methods) {
//            if (method.getName().equals(testMethodName)) {
//                final CtMethod testMethod = CtNewMethod.make(String.format("public void %s() throws Exception { _proxy.%s(); }", method.getName(), method.getName()), cc);
//                final AnnotationsAttribute attr = new AnnotationsAttribute(cpool, AnnotationsAttribute.visibleTag);
//                final Annotation annot = new Annotation("org.junit.Test", cpool);
//                attr.addAnnotation(annot);
//                testMethod.getMethodInfo().addAttribute(attr);
//                cc.addMethod(testMethod);
//            }
//        }
//        final Class<?> z = cc.toClass(this.getClass().getClassLoader(), this.getClass().getProtectionDomain());
//        try {
//            Class.forName(z.getName());
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        }
//        return z;
//    }
}
