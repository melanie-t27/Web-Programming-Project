package it.polimi.tiw.music.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.music.beans.User;
import it.polimi.tiw.music.dao.PlaylistDAO;
import it.polimi.tiw.music.dao.SongDAO;

public class AddSongToPlaylist extends HttpServlet{
	private static final long serialVersionUID = 3L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
	
	public void init() throws ServletException {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String pw= context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, pw);
			
			ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(context);
			templateResolver.setTemplateMode(TemplateMode.HTML);
			this.templateEngine = new TemplateEngine();
			this.templateEngine.setTemplateResolver(templateResolver);
			templateResolver.setSuffix(".html");
			
		} catch(ClassNotFoundException e) {
			throw new UnavailableException("Can't load db driver");
		} catch(SQLException e) {
			throw new UnavailableException("Couldn't connect");
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null) {
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
		}
		
		PlaylistDAO playlistDao = new PlaylistDAO(connection);
		SongDAO songDao = new SongDAO(connection);
		String username = ((User) session.getAttribute("currentUser")).getUsername();
		String playlist = (String) request.getParameter("idPlaylist");
		String song = (String) request.getParameter("selectedSong");
		int idPlaylist = -1;
		int idSong = -1;
		String error = "";
		
		//checking inputs
		if(playlist == null || playlist.isEmpty() || song == null || song.isEmpty()) {
			error = "Missing parameter.";
		} else {
			try {
				//check if the inputs are number and if they are positive
				idPlaylist = Integer.parseInt(playlist);
				idSong = Integer.parseInt(song);
				if(idPlaylist <= 0 || idSong <= 0) {
					error = "Parameters submitted don't exist.";
				} else {
					//check if the play-list exists
					if(!playlistDao.isPlaylistPresent(username, idPlaylist))
						error = "PlayList doesn't exist.";
					//check if the song exists
					if(!songDao.isSongPresent(username, idSong))
						error = "Song doesn't exist.";
					//check if the song is already in the playList
					if(playlistDao.isSongPresentInPlaylist(idSong, idPlaylist))
						error = "Song already present in this playlist.";
				}
			} catch(NumberFormatException e) {
				error = "Parameters submitted aren't valid, please try again!";
			} catch (SQLException e) {
				error = "Something went wrong, please try again!";
			}
		}
		
		//if inputs aren't valid, forward to goToPlaylistPage or goToHomePage
		if(!error.equals("")) {
			if(idPlaylist <= 0) {
				String path = getServletContext().getContextPath() + "/goToHomePage?errorAddSong=true";
				response.sendRedirect(path);
				return;
			} else {
				String path = "/goToPlaylistPage?idPlaylist=" + idPlaylist + "&group=1";
				request.setAttribute("errorAddSong", error);
				RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
				dispatcher.forward(request,response);
				return;
			}
		}
		
		
		try{
			playlistDao.addSongInPlaylist(idPlaylist, idSong);
		} catch(Exception e) {
			error="Something went wrong, please try again!";
		} 
		String path = "/goToPlaylistPage?idPlaylist=" + idPlaylist + "&group=1";
		request.setAttribute("errorAddSong", error);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
		dispatcher.forward(request,response); //FUNZIONA
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request , response);
	}
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {}
	}
	

}
