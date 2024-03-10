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

public class GetSongsNotInPlaylist extends HttpServlet{
	
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
		HttpSession s = request.getSession();
	    User user = (User) s.getAttribute("user");
		String playlistId = request.getParameter("playlistId");
		String error = "";
		int id = -1;
		
		//Check if playlistId is valid
		if(playlistId == null || playlistId.isEmpty())
			error = "Playlist not defined.";
		
		if(error.equals("")) {
			PlaylistDAO pDao = new PlaylistDAO(connection);
			try {
				id = Integer.parseInt(playlistId);
				//Check if the player can access at this playList --> Check if the playList exists
				if(!pDao.isPlaylistPresent(user.getUsername(), id)) {
						error = "PlayList doesn't exist.";
				}
			}catch(NumberFormatException e) {
				error = "Playlist e/o section not defined.";
			}catch(SQLException e) {
				error = "Impossible comunicate with the data base.";
			}
		}	
		
		if(!error.equals("")){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400
			response.getWriter().println(error);
			return;
		}
		
		
		SongDAO sDao = new SongDAO(connection);
		try {
			List<Song> songsNotInPlaylist = sDao.findSongsNotInPlaylist(user.getUsername() , id);
			//Create the jSon with the answer
			Gson gSon = new GsonBuilder().create();
			String jSon = gSon.toJson(songsNotInPlaylist);
			
			response.setStatus(HttpServletResponse.SC_OK);//Code 200
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(jSon);
			
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
			response.getWriter().println("Internal server error, retry later");
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