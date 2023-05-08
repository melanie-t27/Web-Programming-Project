package it.polimi.tiw.music.controllers;

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

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.music.beans.Playlist;
import it.polimi.tiw.music.beans.Song;
import it.polimi.tiw.music.beans.User;
import it.polimi.tiw.music.dao.PlaylistDAO;
import it.polimi.tiw.music.dao.SongDAO;

public class GoToPlaylistPage extends HttpServlet{
	private static final long serialVersionUID = 5L;
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
			e.printStackTrace();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("currentUser") == null) {
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
		}
		else { 
			PlaylistDAO playlistDAO = new PlaylistDAO(connection);
			SongDAO songDAO = new SongDAO(connection);
			List<Song> songsInPlaylist = new ArrayList<>();
			List<Song> songsNotInPlaylist = new ArrayList<>();
			List<Song> groupSongs = new ArrayList<>();
			Playlist playlist = new Playlist();
			String username = ((User) session.getAttribute("currentUser")).getUsername();
			String chosenPlaylist = request.getParameter("idPlaylist");
			String groupChosen = request.getParameter("group");
			int group = 0;
			boolean next = false;
			
			try { 
				int idPlaylist = Integer.parseInt(chosenPlaylist);
				System.out.println("Accessing playlist with id="+ idPlaylist);
				playlist = playlistDAO.getPlaylistById(username, idPlaylist);
				System.out.println("Playlist found");
				group = Integer.parseInt(groupChosen);
				System.out.println("Playlist found");
			} catch(SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in retrieving playlist from the database");
				return;
			} catch(NumberFormatException e) {
				//error
			}
			
			try {
				songsInPlaylist = songDAO.findAllSongsInPlaylist(username, playlist.getId());
				System.out.println("Songs in playlist found");
				for(Song song:songsInPlaylist) {
					System.out.print(song.getTitle()+", ");
				}
				System.out.println();
				songsNotInPlaylist = songDAO.findSongsNotInPlaylist(username, playlist.getId());
				System.out.println("Songs not in playlist found");
				for(Song song:songsNotInPlaylist) {
					System.out.print(song.getTitle()+", ");
				}
				System.out.println();
				
				if(songsInPlaylist.size() > group*5) {
					next = true;
				} else {
					next = false;
				}
				
				for(int i = group*5-5; i < group*5 && i < songsInPlaylist.size() ; i++) {
					groupSongs.add(songsInPlaylist.get(i));
				}
				
			} catch(SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in retrieving songs in database from the database");
				return;
			}
			
			String path = "/WEB-INF/PlaylistPage.html";
			ServletContext servletContext = getServletContext();
			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
			System.out.println("Processing and setting the variable for process...");
			ctx.setVariable("currentPlaylist", playlist);
			ctx.setVariable("songsInPlaylist", groupSongs); 
			ctx.setVariable("songsNotInPlaylist", songsNotInPlaylist); 
			ctx.setVariable("next", next);
			ctx.setVariable("group", group);
			
			templateEngine.process(path, ctx, response.getWriter());
			
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
