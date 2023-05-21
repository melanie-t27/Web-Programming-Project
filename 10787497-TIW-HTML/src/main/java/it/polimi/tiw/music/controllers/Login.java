package it.polimi.tiw.music.controllers;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.music.dao.UserDAO;
import it.polimi.tiw.music.beans.User;

public class Login extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String pw= context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, pw);
			
			ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
			templateResolver.setTemplateMode(TemplateMode.HTML);
			this.templateEngine = new TemplateEngine();
			this.templateEngine.setTemplateResolver(templateResolver);
			templateResolver.setSuffix(".html");
			
		} catch(ClassNotFoundException e) {
			throw new UnavailableException("Can't load db driver");
		} catch(SQLException e) {
			throw new UnavailableException("Couldn't connect");
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		UserDAO userDao = new UserDAO(connection);
		String username = request.getParameter("username");
	    String password = request.getParameter("password");
	    User user = null;
	    String error = null;
	    
	    if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
			error = "Missing Parameters.";
			String path = "/loginPage.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			ctx.setVariable("error", error);
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
	    
	    try {
			user = userDao.checkUser(username, password);
		} catch (SQLException e) {
			error = "Database access failed.";
		}
	    if (user != null && error == null) {
			HttpSession session = request.getSession();
			session.setAttribute("currentUser", user);
			String path = getServletContext().getContextPath() + "/goToHomePage";
			response.sendRedirect(path);
			return;
		} else {
			String path = "/loginPage.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			if(error==null) {
			 error = "Access denied, please try again!";
			}
			ctx.setVariable("error", error);
			templateEngine.process(path, ctx, response.getWriter());
			return;
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request , response);
	}
	
	public void destroy() {
		try {
			if(connection != null) {
				connection.close();
			}
		} catch(SQLException sqle) {}
	}
	
}
