package it.polimi.tiw.music.controllers;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
 
import java.util.Calendar;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;


import it.polimi.tiw.music.beans.User;
import it.polimi.tiw.music.dao.SongDAO;

public class CreateSong extends HttpServlet{
	private static final long serialVersionUID = 1L;
	private Connection connection;
	
	
	public void init() {
		try {
			ServletContext context = getServletContext();
			String driver = context.getInitParameter("dbDriver");
			String url = context.getInitParameter("dbUrl");
			String user = context.getInitParameter("dbUser");
			String pw= context.getInitParameter("dbPassword");
			Class.forName(driver);
			connection = DriverManager.getConnection(url, user, pw);
		} catch(ClassNotFoundException e) {
			e.printStackTrace();
		} catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	public void doPost(HttpServletRequest request , HttpServletResponse response)throws ServletException,IOException{
		HttpSession s = request.getSession();
		User user = (User) s.getAttribute("user");
		//Take the request parameters
		String songTitle = request.getParameter("title");
		String albumTitle = request.getParameter("albumTitle");
		String singer = request.getParameter("artist");
		String genre = request.getParameter("genre");
		String date = request.getParameter("year");
		Part albumImg = request.getPart("cover");
		Part songFile = request.getPart("audio");
		
		int publicationYear = 0;
		String error = "";
		
		//Check if the user missed some parameters
		if(songTitle == null || songTitle.isEmpty() || genre == null || genre.isEmpty() || albumTitle == null || albumTitle.isEmpty()
				|| singer == null || singer.isEmpty() || date == null || date.isEmpty() 
				|| albumImg == null || albumImg.getSize() <= 0 || songFile == null || songFile.getSize() <= 0) {
			error = "Missin parameters.";
		}	
		
		try {
			publicationYear = Integer.parseInt(date);
			int currentYear = Calendar.getInstance().get(Calendar.YEAR);
			
			//Check if the publicationYear is not bigger than the current year
			if(publicationYear > currentYear)
				error = "Invalid year of publication.";
		}catch(NumberFormatException e) {
			error = "Year of publication not valid.";
		}
		
		//Check if the genre is valid
		if(!(genre.equals("Pop") || genre.equals("Dance") || genre.equals("Rock") || genre.equals("Alternative") || genre.equals("Rap"))) {
			error = "Invalid genre.";
		}
		
		//Check if some input are too long
		if(songTitle.length() > 45)
			error = "Song title too long.";
		if(genre.length() > 45)
			error = "Genre name too long.";
		if(albumTitle.length() > 45)
			error = "Album title too long.";
		if(singer.length() > 45)
			error = "Singer name too long.";
		
		//Take the type of the image file uploaded : image/png
		String contentTypeImg = albumImg.getContentType();

		//Check if the type is an image
		if(!contentTypeImg.startsWith("image"))
			error = "Cover file not valid.";
		else {
			//If it's an image, check id the size is bigger than 1024KB (about 1MB)
			if(albumImg.getSize() > 1024000) {
				error = "Cover size is too big.";
			}	
		}
		
		//Take the type of the music file uploaded : audio/mpeg
		String contentTypeMusic = songFile.getContentType();
		
		//Check the type of the music file uploaded
		if(!contentTypeMusic.startsWith("audio"))
			error = "Audio file not valid.";
		else {
			//If it's a song, check if the size is bigger than 10240KB (about 10MB)
			if(songFile.getSize() > 10240000) {
				error = "Audio size is too big.";
			}	
		}
		
		//If an error occurred, redirect 
		if(!error.equals("")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
			response.getWriter().println(error);
			return;
		}
		
		Part coverPart = request.getPart("cover");
		InputStream cover = null;
		if (coverPart != null) {
			cover = coverPart.getInputStream();	
		}
		
		Part audioPart = request.getPart("audio");
		InputStream audio = null;
		if (coverPart != null) {
			audio = audioPart.getInputStream();	
		}
		
		//Now it's possible update the data base
		
		SongDAO songDAO = new SongDAO(connection);
		try {
			if(!songDAO.songAlreadyExists(user.getUsername(), songTitle, albumTitle, singer)) {
				songDAO.createSongAlbum(user.getUsername(), songTitle, genre, audio, albumTitle, singer, publicationYear, cover);
			}
			else {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);//Code 400	
				response.getWriter().println("Song already exists.");
				return;
			}
			
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500	
			response.getWriter().println("Something went wrong, please try later.");
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
