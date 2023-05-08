package it.polimi.tiw.music.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
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
			//boolean errorNotNew = false; 
			//boolean errorAudio = false;
			//boolean errorCover = false;
			//boolean errorYear = false;
			
			String error = "";
			boolean success = false;
			
			try {
				year = Integer.parseInt(yearString);
				//Take the current year
				int currentYear = Calendar.getInstance().get(Calendar.YEAR);
				
				//Check if the publicationYear is not bigger than the current year
				if(year > currentYear) {
					//errorYear = true;
					//response.sendError(505, "Year of pubblication error");
					//return;
				}
			} catch (NumberFormatException e) {
				//errorYear = true;
				error="The year of pubblication you have committed is not right, please try again!";
				//String path = getServletContext().getContextPath() + "/goToHomePage";
				//response.sendRedirect(path);
			}
			
			Part coverPart = request.getPart("cover");
			InputStream cover = null;
			String contentTypeCover = null;
			if (coverPart != null) {
				//for debugging
				System.out.println(coverPart.getName());
	            System.out.println(coverPart.getSize());
	            System.out.println(coverPart.getContentType());
	            //obtains input stream of the upload file
				cover = coverPart.getInputStream();
				String filename = coverPart.getSubmittedFileName();
				contentTypeCover = getServletContext().getMimeType(filename);			
			}
			
			Part audioPart = request.getPart("audio");
			InputStream audio = null;
			String contentTypeAudio = null;
			if (coverPart != null) {
				//for debugging
				System.out.println(audioPart.getName());
	            System.out.println(audioPart.getSize());
	            System.out.println(audioPart.getContentType());
	          //obtains input stream of the upload file
				audio = audioPart.getInputStream();
				String filename = audioPart.getSubmittedFileName();
				contentTypeAudio = getServletContext().getMimeType(filename);			
			}

			if (cover == null || cover.available()==0 || !contentTypeCover.startsWith("image/") ) { //Control of input
				//errorCover = true;
				//response.sendError(505, "Parameters incomplete");
				//return;
			}
			
			if(audio == null || audio.available()==0 || !contentTypeAudio.startsWith("audio/")) {
				//errorAudio = true;
			}
			
			SongDAO songDAO = new SongDAO(connection);
			try {
				success = songDAO.createSongAlbum(username, titleSong, genre, audio, titleAlbum, artist, year, cover);
			} catch(SQLException e) {}
			finally {
				//ServletContext servletContext = getServletContext();
				//final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				if(success) {
					error = "A new song has been successfully submitted!";
				} else {
					error = "Something went wrong, please try later!";
				}
				
				ServletContext servletContext = getServletContext();
				final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
				
				ctx.setVariable("resultSong", error);
				
				
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
