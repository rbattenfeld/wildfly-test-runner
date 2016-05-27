package com.six_group.java_ee.common.junit.test.asEarArchive;

import java.io.IOException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/test")
public class WarTestServlet extends HttpServlet {
    private static final long serialVersionUID = 3317680861199178736L;

    public void doGet(final HttpServletRequest request, final HttpServletResponse response) throws IOException {
        response.getWriter().println("Hello");
    }
}
