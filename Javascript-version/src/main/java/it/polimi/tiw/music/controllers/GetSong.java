package it.polimi.tiw.music.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONException;
import org.json.JSONObject;

import it.polimi.tiw.music.beans.Song;
import it.polimi.tiw.music.beans.User;
import it.polimi.tiw.music.dao.PlaylistDAO;
import it.polimi.tiw.music.dao.SongDAO;


public class GetSong extends HttpServlet{

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
	
	public void doGet(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		HttpSession s = request.getSession();
	    User user = (User) s.getAttribute("user");
		//Take the song id
		String songId = request.getParameter("songId");
		String playlistId = request.getParameter("playlistId");
		String error = "";
		int sId = -1;
		int pId = -1;

		//Check if songId is valid
		if(songId.isEmpty() || songId == null)
			error = "Song not defined.";
		//Check if playlistId is valid
		if(playlistId.isBlank() || playlistId == null)
			error = "Playlist not defined.";
		
		if(error.equals("")) { 
			try {
				SongDAO sDao = new SongDAO(connection);
				PlaylistDAO pDao = new PlaylistDAO(connection);
				sId = Integer.parseInt(songId);
				pId = Integer.parseInt(playlistId);
				
				//Check if the player has this song --> Check if the song exists
				if(!sDao.isSongPresent(user.getUsername(), sId)) {
					error = "Song doesn't exist.";
				}
				//Check if the player has this playList
				if(!pDao.isPlaylistPresent(user.getUsername(), pId)) {
					error = "Playlist doesn't exist.";
				}
			}catch(NumberFormatException e) {
				error = "Request with bad format;";
			}catch(SQLException e) {
				error = "Impossible comunicate with the data base.";
			}
		}
		
		//if an error occurred
		if(!error.equals("")){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400
			response.getWriter().println(error);
			return;
		}
		
		//User can be here 
		
		//To take song details 
		SongDAO sDao = new SongDAO(connection);//I can use the same sDao used before
		
		try {
			Song song = sDao.findAllSongInfoById(user.getUsername(), sId);
			
			JsonObject value =  Json.createObjectBuilder()
					.add("songTitle" , song.getTitle())
					.add("singer" , song.getAlbum().getArtist())
					.add("albumTitle" , song.getAlbum().getTitle())
					.add("publicationYear" , song.getAlbum().getYear())
					.add("genre" , song.getGenre())
					.add("cover" , song.getAlbum().getCover())
					.add("audio", song.getFile())
					.build();
			
			response.setStatus(HttpServletResponse.SC_OK);//Code 200
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().println(value);
			
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
			response.getWriter().println("An arror occurred with the db, retry later");
			return;
		}catch(JSONException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
			response.getWriter().println("Internal server error, error during the creation of the response");
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