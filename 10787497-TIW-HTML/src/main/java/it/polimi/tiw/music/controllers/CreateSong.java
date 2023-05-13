package it.polimi.tiw.music.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
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
			return;
		}
	
		String username = ((User) session.getAttribute("currentUser")).getUsername();
		String titleSong = (String) request.getParameter("title");
		String genre = (String) request.getParameter("genre");
		String titleAlbum = (String) request.getParameter("titleAlbum");
		String artist = (String) request.getParameter("artist");
		String yearString = (String) request.getParameter("year");
		int year = -1;
		String error = "";
		
		//checking inputs
		try {
			year = Integer.parseInt(yearString);
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			if(year > currentYear) {
				error = "Year invalid, please try again.";
				System.out.println(error);
			}
		} catch (NumberFormatException e) {
			error="Year of pubblication invalid, please try again.";
			System.out.println(error);
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

		if (cover == null || cover.available()==0 || !contentTypeCover.startsWith("image/") ) { //Control of input
			error = "Cover invalid, please try again.";
			System.out.println(error);
		}
		
		if(audio == null || audio.available()==0 || !contentTypeAudio.startsWith("audio/")) {
			error = "Audio invalid, please try again.";
			System.out.println(error);
		}
		
		if(titleSong == null || titleSong.isEmpty() || titleSong.length() > 45) {
			error = "Title song invalid, please try again";
			System.out.println(error);
		}
		
		List<String> genres = Arrays.asList("Pop", "Indie", "Rock", "Alternative", "R&B");
		if(genre == null || genre.isEmpty() || !genres.contains(genre)) {
			error = "Genre not valid, please try again.";
			System.out.println(error);
		}
		
		if(titleAlbum == null || titleAlbum.isEmpty() || titleAlbum.length() > 45) {
			error = "Title of the Album invalid, please try again.";
			System.out.println(error);
		}
		
		if(artist == null || artist.isEmpty() || artist.length() > 45){
			error = "Artist invalid, please try again.";
			System.out.println(error);
		}
		
		//if parameters are invalid, forward to goToHomepage 
		if(!error.equals("") ) {
			System.out.println("forwarding to goToHomepage for error");
			String path = "/goToHomePage";
			request.setAttribute("errorNewSong", error);
			RequestDispatcher dispatcher = getServletContext().getRequestDispatcher(path);
			dispatcher.forward(request,response);
			return;
		}
		
		//if inputs valid
		SongDAO songDAO = new SongDAO(connection);
		try {
			System.out.println("Connection to database.....");
			if(!songDAO.songAlreadyExists(username, titleSong, titleAlbum, artist)) {
				System.out.println("Song didn't exist, adding to database....");
				songDAO.createSongAlbum(username, titleSong, genre, audio, titleAlbum, artist, year, cover);
				System.out.println("Song created.");
			}
			else {
				error = "Song already exists.";
			}
		} catch(SQLException e) {
			error = "Something went wrong, please try later.";
		}
		System.out.println(error);
		System.out.println("forwarding to goToHomepage");
		//forward to goToHomepage
		String path = "/goToHomePage";
		request.setAttribute("errorNewSong", error);
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
