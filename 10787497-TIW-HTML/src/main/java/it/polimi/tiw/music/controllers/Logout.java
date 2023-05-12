package it.polimi.tiw.music.controllers;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class Logout extends HttpServlet{
	private static final long serialVersionUID = 7L;
	
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		//Take the session if it exists
		HttpSession session = request.getSession(false);
		
		//Invalidate session
		if (session != null) {
			session.invalidate();
		}
		
		//Redirect to the login page
		String path = getServletContext().getContextPath() +  "/loginPage.html";
		response.sendRedirect(path);
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request , response);
	}
}
