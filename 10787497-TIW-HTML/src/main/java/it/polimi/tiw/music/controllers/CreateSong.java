package it.polimi.tiw.music.controllers;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;

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
import org.thymeleaf.context.WebContext;
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
	
	//i need to understand how to handle errors 
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);
		if (session == null || session.getAttribute("currentUser") == null) {
			String path = getServletContext().getContextPath();
			response.sendRedirect(path);
		}
		else {
			String username = ((User) session.getAttribute("currentUser")).getUsername();
			
			String titleSong = (String) request.getParameter("title");
			String genre = (String) request.getParameter("genre");
			String titleAlbum = (String) request.getParameter("titleAlbum");
			String artist = (String) request.getParameter("artist");
			String yearString = (String) request.getParameter("year");
			int year = -1;
			String error = "None";
			boolean success = false;
			
			try {
				year = Integer.parseInt(yearString);
				//Take the current year
				int currentYear = Calendar.getInstance().get(Calendar.YEAR);
				
				//Check if the publicationYear is not bigger than the current year
				if(year > currentYear) {
					response.sendError(505, "Year of pubblication error");
					return;
				}
			} catch (NumberFormatException e) {
				error="The year of pubblication you have committed is not right, please try again!";
				String path = getServletContext().getContextPath() + "/goToHomePage";
				response.sendRedirect(path);
			}
			
			Part coverPart = request.getPart("cover");
			InputStream cover = null;
			String contentTypeCover = null;
			if (coverPart != null) {
				cover = coverPart.getInputStream();
				String filename = coverPart.getSubmittedFileName();
				contentTypeCover = getServletContext().getMimeType(filename);			
			}
			
			Part audioPart = request.getPart("audio");
			InputStream audio = null;
			String contentTypeAudio = null;
			if (coverPart != null) {
				audio = audioPart.getInputStream();
				String filename = audioPart.getSubmittedFileName();
				contentTypeAudio = getServletContext().getMimeType(filename);			
			}

			if (cover == null || cover.available()==0 || !contentTypeCover.startsWith("image/") || audio == null || audio.available()==0 || !contentTypeAudio.startsWith("audio/")) { //Control of input
				response.sendError(505, "Parameters incomplete");
				return;
			}
			
			SongDAO songDAO = new SongDAO(connection);
			try {
				success = songDAO.createSongAlbum(username, titleSong, genre, audio, titleAlbum, artist, year, cover);
			} catch(SQLException e) {}
			finally {
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				if(success) {
					error = "A new song has been successfully submitted!";
				} else {
					error = "Something went wrong, please try later!";
				}
				
				String path = getServletContext().getContextPath() + "/goToHomePage";
				response.sendRedirect(path);
			}
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
