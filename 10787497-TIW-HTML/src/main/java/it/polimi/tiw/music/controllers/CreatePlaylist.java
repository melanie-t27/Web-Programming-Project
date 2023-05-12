package it.polimi.tiw.music.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

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

public class CreatePlaylist extends HttpServlet{
	private static final long serialVersionUID = 4L;
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
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("currentUser") == null) {
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
		}
		
		String username = ((User) session.getAttribute("currentUser")).getUsername();
		String titlePlaylist = (String) request.getParameter("titlePlaylist");
		String[] idSongs = (String[]) request.getParameterValues("selectedSong");
		int[] songs = {-1};
		String error = "";
		
		//checking input
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
		
		if(titlePlaylist == null || idSongs == null || idSongs.length == 0 || titlePlaylist.isEmpty()) {
			error = "Missing Parameters.";
		}
		
		//if input invalid, forward to goToHomePage with error
		if(!error.equals("")) {
			String path = "/goToHomePage";
			request.setAttribute("errorNewPlaylist", error);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		PlaylistDAO playlistDAO = new PlaylistDAO(connection);
		SongDAO songDAO = new SongDAO(connection);
		try {
			//check if the selected songs exist
			for(int s : songs) {
				if(!songDAO.isSongPresent(username, s)) {
					error = "Some of the selected songs don't exist.";
				}
			}
			//check if the chosen name for the play-list is already taken
			if(playlistDAO.findPlaylistId(username, titlePlaylist) != -1) {
				error = "Playlist title already used.";
			}
		} catch(SQLException e) {
			error = "Somethin went wrong with database connection.";
		}
		 
		//if input invalid, forward to goToHomePage with error
		if(!error.equals("")) {
			String path = "/goToHomePage";
			request.setAttribute("errorNewPlaylist", error);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}

		//if inputs valid
		try {
			playlistDAO.createPlaylistWithSongs(username, titlePlaylist, songs);
		} catch(Exception e) {
			error = "Somethin went wrong with database connection, please try later.";
		}
		
		String path = "/goToHomePage";
		request.setAttribute("errorNewPlaylist", error);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
		dispatcher.forward(request,response);
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
