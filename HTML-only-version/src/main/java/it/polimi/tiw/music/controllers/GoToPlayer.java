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
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.music.beans.Song;
import it.polimi.tiw.music.beans.User;
import it.polimi.tiw.music.dao.PlaylistDAO;
import it.polimi.tiw.music.dao.SongDAO;

public class GoToPlayer extends HttpServlet{
	private static final long serialVersionUID = 6L;
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
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("currentUser") == null) {
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
			return;
		}
	
		Song song = new Song();
		SongDAO songDAO = new SongDAO(connection);
		PlaylistDAO playlistDAO = new PlaylistDAO(connection);
		String username = ((User) session.getAttribute("currentUser")).getUsername();
		String chosenSong = request.getParameter("idSong");
		String currentPlaylist = request.getParameter("currentPlaylist");
		String currentGroup = request.getParameter("group");
		int idSong = -1;
		int idPlaylist = -1;
		int group = -1;
		String error = "";
		
		//checking inputs
		if(chosenSong == null || chosenSong.isEmpty() || currentPlaylist == null || currentPlaylist.isEmpty()) {
			error = "Missing parameters.";
		} else {
			try { 
				idSong = Integer.parseInt(chosenSong);
				idPlaylist = Integer.parseInt(currentPlaylist);
				group = Integer.parseInt(currentGroup);
				if (group <= 0) {
					group = 1;
				}
				
				if(idSong <= 0 || idPlaylist <= 0) {
					error = "Parameters invalid.";
				} else {
					//check if the song exists
					if(!songDAO.isSongPresent(username, idSong)) {
						error = "Song doesn't exist.";
					}
					//check if the play-list exists
					if(playlistDAO.getPlaylistById(username, idPlaylist) == null) {
						error = "Playlist doesn't exist;";
					}
					//check if song is in the given play-list
					if(!playlistDAO.isSongPresentInPlaylist(idSong,idPlaylist)) {
						error ="Song is not in the playlist.";
					}
				}
			} catch(NumberFormatException e) {
				error = "Parameters invalid.";
			} catch(SQLException e) {
				error = "Something went wrong, please try again.";
			}
		}
		
		//if inputs invalid, forward to goToPlaylistPage
		if(!error.equals("")) {
			String path = "/goToPlaylistPage";
			request.setAttribute("errorToPlayer", error);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		
		try {
			song = songDAO.findAllSongInfoById(username, idSong);
		} catch(SQLException e) {
			error = "Error in retrieving songs in database from the database";
			String path = "/goToPlaylistPage";
			request.setAttribute("errorToPlayer", error);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		String path = "/WEB-INF/Player.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("currentPlaylist", idPlaylist);
		ctx.setVariable("song", song);
		ctx.setVariable("group", group);
		templateEngine.process(path, ctx, response.getWriter());
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
