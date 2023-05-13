package it.polimi.tiw.music.controllers;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.*;
import javax.servlet.http.*;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.music.beans.*;
import it.polimi.tiw.music.beans.User;
import it.polimi.tiw.music.dao.PlaylistDAO;
import it.polimi.tiw.music.dao.SongDAO;

public class GoToHomePage extends HttpServlet {
	private static final long serialVersionUID = 2L;
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
	
		List<String> genres = Arrays.asList("Pop", "Indie", "Rock", "Alternative", "R&B");
		PlaylistDAO playlistDAO = new PlaylistDAO(connection);
		SongDAO songDAO = new SongDAO(connection);
		List<Playlist> playlist = new ArrayList<>();
		List<Song> songs = new ArrayList<>();
		String username = ((User) session.getAttribute("currentUser")).getUsername();
		String error = "";
		
		//Handling forward 
		String errorPlaylist = (String) request.getAttribute("errorToPlaylist");
		String errorNewSong = (String) request.getAttribute("errorNewSong");
		String errorNewPlaylist = (String) request.getAttribute("errorNewPlaylist");
		String errorAddSong = (String) request.getAttribute("errorAddSong");
		if(errorAddSong != null && !errorAddSong.equals("")) {
			errorAddSong = "Something went wrong while adding a song to a playlist, please try again.";
		}
		System.out.println("GO TO HOMEPAGE SERVLET");
		System.out.println(errorPlaylist);
		System.out.println(errorNewSong);
		System.out.println(errorNewPlaylist);
		System.out.println(errorAddSong);
		
		try {
			playlist = playlistDAO.findPlaylists(username);
			songs = songDAO.findAllSongsByUsername(username);
		} catch(Exception e) {
			error = "Something went wrong, please logout and try again!";
			System.out.println(error);
		}
		
		System.out.println("processing to homepage.html");
		String path = "/WEB-INF/HomePage.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("playlists", playlist);
		ctx.setVariable("songs", songs);
		ctx.setVariable("genres", genres);
		ctx.setVariable("error", error);
		ctx.setVariable("errorToPlaylist", errorPlaylist);
		ctx.setVariable("errorNewSong", errorNewSong);
		ctx.setVariable("errorNewPlaylist", errorNewPlaylist);
		ctx.setVariable("errorAddSong", errorAddSong);
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
