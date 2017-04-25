package org.openrisknet.example;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

@WebServlet(value="/", name="Hello Servlet")
public class HelloServlet extends HttpServlet {

    private String greeting;

    @Override
    public void init() throws ServletException {
        super.init();
        greeting = System.getenv("GREETING");
        if (greeting == null) {
            greeting = "Hello";
        }
    }


    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        Principal principal = req.getUserPrincipal();
        String username = (principal == null ? "unauthenticated user" : principal.getName());
        res.getWriter().println(greeting + " " + username);
    }

}
