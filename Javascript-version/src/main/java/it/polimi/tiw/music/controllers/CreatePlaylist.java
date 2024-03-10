package it.polimi.tiw.music.controllers;

import java.sql.DriverManager;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.music.beans.User;
import it.polimi.tiw.music.dao.PlaylistDAO;
import it.polimi.tiw.music.dao.SongDAO;

public class CreatePlaylist extends HttpServlet{

	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	public void init() {
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
	
	public void doGet(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		doPost(request , response);
	}
	
	public void doPost(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		HttpSession s = request.getSession();
		User user = (User) s.getAttribute("user");
		String title = request.getParameter("titlePlaylist");
		String[] idSongs = (String[]) request.getParameterValues("selectedSong");
		int[] songs = {-1};
		String error = "";
		
		//checking input
		if(title == null || title.isEmpty() || idSongs == null || idSongs.length == 0)
			error = "Missing Parameters.";
		
		if(title.length() > 45)
			error = "Title is too long.";
		
		if(!error.equals("")){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println(error);
			return;
		}
		
		try {
			if(idSongs != null) {
				songs = Arrays.stream(idSongs).mapToInt(song -> Integer.parseInt(song)).toArray();
				for(int n : songs) {
					if(n <= 0) {
						error = "Selected songs are invalid.";
					}
				}
			}
		} catch(NumberFormatException e) {
			error = "Songs added invalid, please try again.";
		}
		
		if(!error.equals("")){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println(error);
			return;
		}
		
		PlaylistDAO playlistDAO = new PlaylistDAO(connection);
		SongDAO songDAO = new SongDAO(connection);
		try {
			//check if the selected songs exist
			for(int song : songs) {
				if(!songDAO.isSongPresent(user.getUsername(), song)) {
					error = "Some of the selected songs don't exist.";
				}
			}
			//check if the chosen name for the play-list is already taken
			if(playlistDAO.findPlaylistId(user.getUsername(), title) != -1) {
				error = "Playlist title already used.";
			}
		} catch(SQLException e) {
			error = "Somethin went wrong with database connection.";
		}
		
		if(!error.equals("")){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println(error);
			return;
		}
		
		//Valid Input
		PlaylistDAO pDao = new PlaylistDAO(connection);
		try {
			pDao.createPlaylistWithSongs(user.getUsername(), title, songs);
			response.setStatus(HttpServletResponse.SC_OK);//Code 200
			
		}catch(SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
			response.getWriter().println(e.getMessage());
		}
		
	}
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {}
	}
}