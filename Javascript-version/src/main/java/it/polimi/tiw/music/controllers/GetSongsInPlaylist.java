package it.polimi.tiw.music.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.json.JsonException;
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

public class GetSongsInPlaylist extends HttpServlet{
	private static final long serialVersionUID = 5L;
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
			e.printStackTrace();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession s = request.getSession();
		User user = (User) s.getAttribute("user");
		String playlistId = request.getParameter("playlistId");
		String error = "";
		int id = -1;
	    
		//Check if input are valid
		if(playlistId == null || playlistId.isEmpty())
			error = "Playlist not defined.";
		
		
		//Check the follow only if the id is valid
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
		
		//The user created this playList
		SongDAO sDao = new SongDAO(connection);
		PlaylistDAO pDao = new PlaylistDAO(connection);
		
		//Take the titles and the image 
		try {
			List<Song> songsInPlaylist = sDao.findAllSongsInPlaylist(user.getUsername(), id);
			List<Integer> sortedIds = pDao.getSorting(id);
			
			Gson gSon = new GsonBuilder().setDateFormat("dd-MM-yyyy").create();
			String jSon = null;
			
			List<Song> songsWithRightOrder = new ArrayList<>();
			
			if(sortedIds.isEmpty() || sortedIds == null) {
				System.out.println("not sorted before");
				jSon = gSon.toJson(songsInPlaylist);
			} else {
				System.out.println("sorted before");
				for(int i = 0; i < sortedIds.size(); i++) {
					Song song;
					for(int j = 0; j < songsInPlaylist.size(); j++) {
						if(songsInPlaylist.get(j).getId() == sortedIds.get(i)) {
							song = songsInPlaylist.get(j);
							songsInPlaylist.remove(song);
							songsWithRightOrder.add(song);
						}
					}
				}
				// if there are some songs that weren't sorted, i insert them after all the others
				if(!songsInPlaylist.isEmpty()) {
					for(Song s2 : songsInPlaylist) {
						songsWithRightOrder.add(s2);
					}
				}
				jSon = gSon.toJson(songsWithRightOrder);
			}
				
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.getWriter().write(jSon);
			
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
			response.getWriter().println("Internal server error, retry later");
		}catch(JsonException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
			response.getWriter().println("Internal server error, error during the creation of the response");
		}
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
