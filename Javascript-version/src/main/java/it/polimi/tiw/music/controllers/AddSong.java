package it.polimi.tiw.music.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.music.beans.User;
import it.polimi.tiw.music.dao.PlaylistDAO;
import it.polimi.tiw.music.dao.SongDAO;


public class AddSong extends HttpServlet{
	
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
	
	public void doPost(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		HttpSession s = request.getSession();
	    User user = (User) s.getAttribute("user");
		String playlistId = request.getParameter("playlistId");
		String songId = request.getParameter("addSongToPlayList");
		String error = "";
		int pId = -1;
		int sId = -1;
		
		
		
	    //Check id the parameters are present
		if(playlistId == null || playlistId.isEmpty() || songId == null || songId.isEmpty()) {
			error = "Missing parameter.";
		}
		
		if(error.equals("")) {
			try {
				//Create the DAO to check if the playList id belongs to the user 
				PlaylistDAO pDao = new PlaylistDAO(connection);
				SongDAO sDao = new SongDAO(connection);
				
				//Check if the playlistId and songId are numbers
				pId = Integer.parseInt(playlistId);
				sId = Integer.parseInt(songId);
				
				//Check if the user can access in this playList --> Check if the playList exists
				if(!pDao.isPlaylistPresent(user.getUsername(), pId))
					error = "PlayList doesn't exist.";
				//Check if the player has created the song with sId as id -->Check if the song exists
				if(!sDao.isSongPresent(user.getUsername(), sId))
					error = "Song doesn't exist.";
				//Check if the song is already in the playList
				if(pDao.isSongPresentInPlaylist(sId, pId))
					error = "Song already present in this playList.";
			}catch(NumberFormatException e) {
				error = "Playlist not defined.";
			}catch(SQLException e) {
				error = "Impossible comunicate with the data base.";
			}
		}
		
		//if an error occurred
		if(!error.equals("")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400
			response.getWriter().println(error);
			return;
		}
		
		//The user can add the song at the playList
		
		//To add the song in the playList
		PlaylistDAO pDao = new PlaylistDAO(connection);
		
		try {
			boolean result = pDao.addSongInPlaylist(pId, sId);
			
			if(result == true) {
				response.setStatus(HttpServletResponse.SC_OK);//Code 200
			}
			else {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
				response.getWriter().println("An arror occurred with the db, retry later.");
			}
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
			response.getWriter().println("An arror occurred with the db, retry later.");
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