package it.polimi.tiw.music.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.music.beans.User;
import it.polimi.tiw.music.dao.SongDAO;

@MultipartConfig
public class CreateSong extends HttpServlet{
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
		if (session == null || session.getAttribute("currentUser") == null) {
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
		}
		else {
			String username = ((User) session.getAttribute("currentUser")).getUsername();
			String titleSong = (String) session.getAttribute("title");
			String genre = (String) session.getAttribute("genre");
			String titleAlbum = (String) session.getAttribute("titleAlbum");
			String artist = (String) session.getAttribute("artist");
			String yearString = (String) session.getAttribute("year");
			int year = -1;
			
			try {
				year = Integer.parseInt(yearString);
			} catch (NumberFormatException e) {
				//error
			}
			
			Part coverPart = request.getPart("cover");
			InputStream cover = null;
			String mimeType1 = null;
			if (coverPart != null) {
				cover = coverPart.getInputStream();
				String filename = coverPart.getSubmittedFileName();
				mimeType1 = getServletContext().getMimeType(filename);			
			}
			
			Part audioPart = request.getPart("audio");
			InputStream audio = null;
			String mimeType2 = null;
			if (coverPart != null) {
				audio = audioPart.getInputStream();
				String filename = audioPart.getSubmittedFileName();
				mimeType2 = getServletContext().getMimeType(filename);			
			}

			if (cover == null || cover.available()==0 || !mimeType1.startsWith("image/") || audio == null || audio.available()==0 || !mimeType2.startsWith("audio/")) { //Control of input
				response.sendError(505, "Parameters incomplete");
				return;
			}
			
			SongDAO songDAO = new SongDAO(connection);
			try {
				songDAO.createSongAlbum(username, titleSong, genre, audio, titleAlbum, artist, year, cover);
			} catch(SQLException e) {
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error in creating song in the database");
				return;
			}
			
			String path = getServletContext().getContextPath() + "/goToHomePage";
			response.sendRedirect(path);
			
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
