 package it.polimi.tiw.music.dao;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import it.polimi.tiw.music.beans.*;

public class SongDAO {
	Connection connection;
	
	public SongDAO(Connection connection) {
		this.connection = connection;
	}
	
	public void createSongAlbum(String user, String titleSong, String genre, InputStream file, String titleAlbum, String artist, int year, InputStream cover) throws SQLException {
		int idAlbum = -1;
		try {
			connection.setAutoCommit(false);
			System.out.println("CREATE SONG IN SONG DAO");
			idAlbum = findIdAlbum(user, titleAlbum, artist);
			if(idAlbum == -1) {
				createAlbum(user, titleAlbum, artist, year, cover);
				idAlbum = findIdAlbum(user, titleAlbum, artist);
			}
			createSong(user, titleSong, genre, file, idAlbum);
			connection.commit();
			
		} catch (SQLException e){
			connection.rollback();
			throw new SQLException(e);
		} finally {
			connection.setAutoCommit(true);
		}
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
			throw new SQLException(e);
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
				idAlbum = result.getInt("idAlbum");	
			}
		} catch(SQLException e) {
			throw new SQLException(e);
		}
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
			throw new SQLException(e);
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
				sg.setFile(encodedAudio);
				
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
				
				//al.setId(result.getInt("idAlbum"));
				al.setTitle(result.getString("titleAlbum"));
				al.setArtist(result.getString("artist"));
				//al.setYear(result.getInt("year"));
				byte[] imgData = result.getBytes("cover");
				String encodedImg=Base64.getEncoder().encodeToString(imgData);
				al.setCover(encodedImg);
				
				sg.setId(result.getInt("idSong"));
				sg.setTitle(result.getString("title"));
				//sg.setGenre(result.getString("genre"));
				sg.setAlbum(al);
				//sg.setUser(username);
				//byte[] audio = result.getBytes("file");
				//String encodedAudio=Base64.getEncoder().encodeToString(audio);
				//sg.setFile(encodedAudio);
				
				songs.add(sg);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch(Exception e1) {
				e1.printStackTrace();
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				e2.printStackTrace();
				throw new SQLException(e2);
			}
		}

		return songs;
	}
	
	public List<Song> findSongsNotInPlaylist(String username, int idPlaylist) throws SQLException {
		List<Song> songs = new ArrayList<>();
		String query = "SELECT * FROM Song Join Album on Song.user = Album.userId and Album.idAlbum = Song.album WHERE Song.user = ? and Song.idSong NOT IN (" +
				"SELECT song FROM InPlaylist WHERE playlist = ?)";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setInt(2, idPlaylist);
			result = pstatement.executeQuery();
			while (result.next()) {
				Album al = new Album();
				Song sg = new Song();
				sg.setId(result.getInt("idSong"));
				sg.setTitle(result.getString("title"));
				al.setTitle(result.getString("titleAlbum"));
				al.setArtist(result.getString("artist"));
				sg.setAlbum(al);
				songs.add(sg);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				e2.printStackTrace();
				throw new SQLException(e2);
			}
		}
		
		return songs;
	}
	
	public Song findAllSongInfoById(String username, int idSong) throws SQLException {
		Song sg = new Song();
		String query = "SELECT * FROM Song JOIN Album ON Song.album = Album.idAlbum and Song.user = Album.userId WHERE Song.user = ? and Song.idSong = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setInt(2, idSong);
			result = pstatement.executeQuery();
			while (result.next()) {
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
				sg.setFile(encodedAudio);
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				e2.printStackTrace();
				throw new SQLException(e2);
			}
		}
		
		return sg;
	}
	
	public boolean isSongPresent(String username, int idSong) throws SQLException {
		boolean r = false;
		String query = "SELECT * FROM Song WHERE user = ? and idSong = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setInt(2, idSong);
			result = pstatement.executeQuery();
			if (result.next()) {
				r = true;
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				e2.printStackTrace();
				throw new SQLException(e2);
			}
		}
		return r;
	}

	public boolean songAlreadyExists(String username, String titleSong, String titleAlbum, String artist) throws SQLException {
		boolean r = false;
		
		int idAlbum = findIdAlbum(username, titleAlbum, artist);
		if(idAlbum == -1) {
			return false;
		}
		String query = "SELECT * FROM Song WHERE user = ? and album = ? and title = ?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setInt(2, idAlbum);
			pstatement.setString(3, titleSong);
			result = pstatement.executeQuery();
			if (result.next()) {
				r = true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				e2.printStackTrace();
				throw new SQLException(e2);
			}
		}
		
		return r;
	}

	public boolean songAlreadyExists2(String username, String titleSong, String titleAlbum, String artist) throws SQLException {
		boolean r = false;
		String query = "SELECT * FROM Song JOIN Album ON Song.album = Album.idAlbum and Song.user = Album.userId WHERE Song.user = ? and Album.titleAlbum = ? and Song.title = ? and Album.artist = ?";
		PreparedStatement pstatement = null;
		ResultSet result = null;
		try {
			pstatement = connection.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setString(2, titleAlbum);
			pstatement.setString(3, titleSong);
			pstatement.setString(4, artist);
			result = pstatement.executeQuery();
			if (result.next()) {
				System.out.println("La canzone esiste già");
				r = true;
			} else System.out.println("La canzone non esiste ancora");
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				result.close();
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new SQLException(e1);
			}
			try {
				pstatement.close();
			} catch (Exception e2) {
				e2.printStackTrace();
				throw new SQLException(e2);
			}
			
		}
		return r;
	}

}
