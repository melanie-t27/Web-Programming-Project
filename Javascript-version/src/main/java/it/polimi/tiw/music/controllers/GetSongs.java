package it.polimi.tiw.music.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import it.polimi.tiw.music.beans.Song;
import it.polimi.tiw.music.beans.User;
import it.polimi.tiw.music.dao.PlaylistDAO;
import it.polimi.tiw.music.dao.SongDAO;


public class GetSongs extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public void init(){
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String pw= context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, pw);
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession s = request.getSession();
		User user = (User) s.getAttribute("user");
		List<Song> songs = null;
		
		SongDAO songDao = new SongDAO(connection);
		try {
			songs = songDao.findAllSongsByUsername(user.getUsername());
		}catch(SQLException e) {					
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); //Code 500
			response.getWriter().println("Internal server error, retry later");
			return;
		}
		response.setStatus(HttpServletResponse.SC_OK); //Code 200
		
		//Create the jSon with the answer
		Gson gSon = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
		String jSon = gSon.toJson(songs);
			
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