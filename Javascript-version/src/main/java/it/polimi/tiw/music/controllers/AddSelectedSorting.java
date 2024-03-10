package it.polimi.tiw.music.controllers;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import it.polimi.tiw.music.beans.User;
import it.polimi.tiw.music.dao.PlaylistDAO;
import it.polimi.tiw.music.dao.SongDAO;

public class AddSelectedSorting extends HttpServlet {

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
	
		BufferedReader reader = request.getReader();
		Gson gson = new GsonBuilder().create();
		TypeToken<ArrayList<Integer>> typeToken = new TypeToken<>() {};
		ArrayList<Integer> ids = gson.fromJson(reader, typeToken);
		Integer pId = ids.get(0);
		List<Integer> songsIds = ids.subList(1, ids.size());
		
		
		//checking the input
		if(pId == null) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println("PlayList not specified");
			return;
		}
		PlaylistDAO playlistDao = new PlaylistDAO(connection);
		try {
			if(!playlistDao.isPlaylistPresent(user.getUsername(), pId)) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
				response.getWriter().println("PlayList not present");
				return;
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500	
			response.getWriter().println("Internal server error, retry later");
			System.out.println("Internal server error, retry later");
			return;
		} 
		
		if (songsIds == null || songsIds.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println("Songs not specified");
			return;
		}
		SongDAO sDao = new SongDAO(connection);
		try {
			for(int id : songsIds) {
				if(!sDao.isSongPresent(user.getUsername(), id) || !playlistDao.isSongPresentInPlaylist(id, pId)) {
					songsIds.remove(id);
				}
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500	
			response.getWriter().println("Internal server error, retry later");
			System.out.println("Internal server error, retry later");
			return;
		} 
		
		// adding the selected sorting 
		try {
			playlistDao.addSorting(pId, ids);
			response.setStatus(HttpServletResponse.SC_OK);//Code 200
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500	
			response.getWriter().println("Internal server error, retry later");
			System.out.println("Internal server error, retry later");
			return;
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