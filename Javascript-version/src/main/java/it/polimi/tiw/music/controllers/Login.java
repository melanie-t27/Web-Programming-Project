package it.polimi.tiw.music.controllers;

import java.io.*;
import java.sql.*;
import javax.servlet.*;
import javax.servlet.http.*;

import it.polimi.tiw.music.dao.UserDAO;
import it.polimi.tiw.music.beans.User;

public class Login extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	
	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String pw= context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, pw);
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
	    
	    if (username == null || password == null || username.isEmpty() || password.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing Parameters.");
			return;
		}
	    
	    try {
			user = userDao.checkUser(username, password);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Database access failed.");
			return;
		}
	    
	    // If the user exists, add info to the session and go to home page, otherwise
	 	// return an error status code and message
	    if (user == null) {
	 		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
	 		response.getWriter().println("Incorrect credentials");
		} else {
	 		request.getSession().setAttribute("user", user);
	 		response.setStatus(HttpServletResponse.SC_OK);
	 		response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
	 		response.getWriter().println(username);
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
