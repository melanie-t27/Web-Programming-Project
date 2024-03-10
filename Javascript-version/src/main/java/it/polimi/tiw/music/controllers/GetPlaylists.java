package it.polimi.tiw.music.controllers;

import java.io.*;
import java.sql.*;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;
import com.google.gson.*;


import it.polimi.tiw.music.beans.*;
import it.polimi.tiw.music.beans.User;
import it.polimi.tiw.music.dao.PlaylistDAO;

public class GetPlaylists extends HttpServlet {
	private static final long serialVersionUID = 2L;
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
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Take the user from the session
		HttpSession s = request.getSession();
		User user = (User) s.getAttribute("user");
		List<Playlist> playlists = null;
		
		PlaylistDAO pDao = new PlaylistDAO(connection);		
		try {
			playlists = pDao.findPlaylists(user.getUsername());
		}catch(SQLException e) {					
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //Code 500
			response.getWriter().println("Internal server error, retry later");
			return;
		}
			
		response.setStatus(HttpServletResponse.SC_OK); //Code 200
			
		//Create the jSon with the answer
		Gson gSon = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
		String jSon = gSon.toJson(playlists);
			
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(jSon);
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request , response);
	}
	
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {}
	}
	
}
