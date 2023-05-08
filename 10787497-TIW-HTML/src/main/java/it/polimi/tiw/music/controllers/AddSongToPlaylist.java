package it.polimi.tiw.music.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

import it.polimi.tiw.music.dao.PlaylistDAO;


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
		
		String playlist = (String) request.getParameter("idPlaylist");
		String song = (String) request.getParameter("selectedSong");
		int idPlaylist = -1;
		int idSong = -1;
		
		System.out.print("Adding new song to playlist n.");
		try {
			idPlaylist = Integer.parseInt(playlist);
			System.out.println(idPlaylist);
			idSong = Integer.parseInt(song);
			System.out.println("song no = "+idSong);
		} catch(NumberFormatException e) {
			//error
		}
		
		PlaylistDAO pDAO = new PlaylistDAO(connection);
		try{
			pDAO.addSongInPlaylist(idPlaylist, idSong);
		} catch(Exception e) {
			e.printStackTrace();
		} 
		
		String path = getServletContext().getContextPath() + "/goToPlaylistPage?idPlaylist=" + idPlaylist + "&group=1";
		response.sendRedirect(path);
		
	}
	
	public void destroy() {
		try {
			if (connection != null) {
				connection.close();
			}
		} catch (SQLException sqle) {}
	}
	

}
