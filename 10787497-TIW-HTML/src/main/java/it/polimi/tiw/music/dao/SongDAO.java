package it.polimi.tiw.music.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import it.polimi.tiw.music.beans.*;

public class SongDAO {
	Connection connection;
	
	public SongDAO(Connection connection) {
		this.connection = connection;
	}
	
	
	@Deprecated
	public void createSongOld(String user, String titleSong, String genre, InputStream file, String titleAlbum, String artist, int year, InputStream cover) throws SQLException {
		String query1 = "SELECT idAlbum FROM Album WHERE title = ? and artist = ? and userId = ? ";
		String query2 = "INSERT into Album (titleAlbum, artist, year, cover, userId) VALUES(?, ?, ?, ?, ?)";
		String query3 = "INSERT into Song (user, album, title, file, genre) VALUES(?, ?, ?, ?, ?)";
		String query4 = "SELECT LAST_INSERT_ID() FROM Album";
		ResultSet result1 = null, result2 = null;
		PreparedStatement pstatement1 = null, pstatement2 = null, pstatement3 = null;
		Statement statement = null;
		int idAlbum = -1;
		try {
			connection.setAutoCommit(false);
			pstatement1 = connection.prepareStatement(query1);
			pstatement1.setString(1, titleAlbum);
			pstatement1.setString(2, artist);
			pstatement1.setString(3, user);
			result1 = pstatement1.executeQuery();
			if(result1.next()) {
				idAlbum = result1.getInt(idAlbum);	
			} else {
				pstatement2 = connection.prepareStatement(query2);
				pstatement2.setString(1, titleAlbum);
				pstatement2.setString(2, artist);
				pstatement2.setInt(3, year);
				pstatement2.setBlob(4,cover);
				pstatement2.setString(5, user);
				pstatement2.executeUpdate();
				statement = connection.createStatement();
				result2 = statement.executeQuery(query4);
				if(result2.next()) {
					idAlbum = result2.getInt(idAlbum);
				}
			}
			pstatement3 = connection.prepareStatement(query3);
			pstatement3.setString(1, user);
			pstatement3.setInt(2, idAlbum);
			pstatement3.setString(3, titleSong);
			pstatement3.setBlob(4, file);
			pstatement3.setString(5, genre);
			pstatement3.executeUpdate();
			connection.commit();
			
		} catch (SQLException e){
			connection.rollback();
			throw new SQLException(e);
		} finally {
			connection.setAutoCommit(true);
			try {
				result1.close();
				if(idAlbum == -1) {
					result2.close();
				} 
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement1.close();
				pstatement3.close();
				if(idAlbum != -1) {
					statement.close();
					pstatement2.close();
				}
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
	}
	
	public boolean createSongAlbum(String user, String titleSong, String genre, InputStream file, String titleAlbum, String artist, int year, InputStream cover) throws SQLException {
		int idAlbum = -1;
		try {
			connection.setAutoCommit(false);
			idAlbum = findIdAlbum(user, titleAlbum, artist);
			if(idAlbum == -1) {
				createAlbum(user, titleAlbum, artist, year, cover);
				idAlbum = findIdAlbum(user, titleAlbum, artist);
			}
			System.out.println("idAlbum: "+idAlbum);
			createSong(user, titleSong, genre, file, idAlbum);
			System.out.println("added new song");
			connection.commit();
			
		} catch (SQLException e){
			connection.rollback();
			System.out.println("SQL Exception in createSong");
			return false;
			//throw new SQLException(e);
		} finally {
			connection.setAutoCommit(true);
		}
		return true;
	}
	
	private void createSong(String user, String title, String genre, InputStream file, int idAlbum) throws SQLException {
		String query = "INSERT into Song (user, album, title, file, genre) VALUES(?, ?, ?, ?, ?)";
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, user);
			pstatement.setInt(2, idAlbum);
			pstatement.setString(3, title);
			pstatement.setBlob(4, file);
			pstatement.setString(5, genre);
			pstatement.executeUpdate();
		} catch(SQLException e) {
			
		} finally {
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
	}
	
	private int findIdAlbum(String user, String title, String artist) throws SQLException {
		String query = "SELECT idAlbum FROM Album WHERE titleAlbum = ? and artist = ? and userId = ? ";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		int idAlbum = -1;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, title);
			pstatement.setString(2, artist);
			pstatement.setString(3, user);
			result = pstatement.executeQuery();
			if(result.next()) {
				System.out.println("Album already existed");
				idAlbum = result.getInt("idAlbum");	
			}
		} catch(SQLException e) {}
		finally {
			try {
				result.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		
		return idAlbum;
	}
	
	private void createAlbum(String user, String title, String artist, int year, InputStream cover) throws SQLException {
		String query = "INSERT into Album (titleAlbum, artist, year, cover, userId) VALUES(?, ?, ?, ?, ?)";
		PreparedStatement pstatement = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, title);
			pstatement.setString(2, artist);
			pstatement.setInt(3, year);
			pstatement.setBlob(4,cover);
			pstatement.setString(5, user);
			pstatement.executeUpdate();
		} catch(SQLException e) {
			
		} finally {
			try {
				pstatement.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
		}
		
	}
	
	public List<Song> findAllSongsByUsername(String username) throws SQLException{
		List<Song> songs = new ArrayList<>();
		String query = "SELECT * FROM Song JOIN Album ON Song.album = Album.idAlbum and Song.user = Album.userId WHERE Song.user = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			result = pstatement.executeQuery();
			while (result.next()) {
				Album al = new Album();
				Song sg = new Song();
				
				al.setId(result.getInt("idAlbum"));
				al.setTitle(result.getString("titleAlbum"));
				al.setArtist(result.getString("artist"));
				al.setYear(result.getInt("year"));
				byte[] imgData = result.getBytes("cover");
				String encodedImg=Base64.getEncoder().encodeToString(imgData);
				al.setCover(encodedImg);
				
				sg.setId(result.getInt("idSong"));
				sg.setTitle(result.getString("title"));
				sg.setGenre(result.getString("genre"));
				sg.setAlbum(al);
				sg.setUser(username);
				byte[] audio = result.getBytes("file");
				String encodedAudio=Base64.getEncoder().encodeToString(audio);
				al.setCover(encodedAudio);
				
				songs.add(sg);
			}
			
		} catch (SQLException e) {
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		return songs;
	}
	
	
	public List<Song> findAllSongsInPlaylist(String username, int idPlaylist) throws SQLException {
		List<Song> songs = new ArrayList<>();
		String query = "SELECT * FROM (Song JOIN InPlaylist ON Song.idSong = InPlaylist.song) JOIN Album ON Song.user = Album.userId and Album.idAlbum = Song.album WHERE Song.user = ? and InPlaylist.playlist = ? ORDER BY Album.year desc";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setInt(2, idPlaylist);
			result = pstatement.executeQuery();
			while(result.next()) {
				Song sg = new Song();
				Album al = new Album();
				
				al.setId(result.getInt("idAlbum"));
				al.setTitle(result.getString("titleAlbum"));
				al.setArtist(result.getString("artist"));
				al.setYear(result.getInt("year"));
				byte[] imgData = result.getBytes("cover");
				String encodedImg=Base64.getEncoder().encodeToString(imgData);
				al.setCover(encodedImg);
				
				sg.setId(result.getInt("idSong"));
				sg.setTitle(result.getString("title"));
				sg.setGenre(result.getString("genre"));
				sg.setAlbum(al);
				sg.setUser(username);
				byte[] audio = result.getBytes("file");
				String encodedAudio=Base64.getEncoder().encodeToString(audio);
				al.setCover(encodedAudio);
				
				songs.add(sg);
			}
			
		} catch (SQLException e) {
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch(Exception e1) {
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				throw new SQLException(e2);
			}
		}
		
		
		return songs;
	}
	
	
	public List<Song> findSongsNotInPlaylist(String username, int idPlaylist) throws SQLException {
		List<Song> songs = findAllSongsByUsername(username);
		songs.removeAll(findAllSongsInPlaylist(username, idPlaylist));
		return songs;
	}
	
	
}
