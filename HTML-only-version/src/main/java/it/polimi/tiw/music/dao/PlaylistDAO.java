package it.polimi.tiw.music.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import it.polimi.tiw.music.beans.*;

public class PlaylistDAO {
	private Connection con;
	
	public PlaylistDAO(Connection con) {
		this.con = con;
	}
	
	public List<Playlist> findPlaylists(String username) throws SQLException {
		List<Playlist> playlists = new ArrayList<>();
		String query = "SELECT * FROM Playlist WHERE idUser = ? ORDER BY creationDate desc";
		ResultSet result = null;
		PreparedStatement pstatement = null;	
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			result = pstatement.executeQuery();
			while (result.next()) {
				Playlist np = new Playlist();
				np.setId(result.getInt("idPlaylist"));
				np.setName(result.getString("name"));
				np.setDate(result.getDate("creationDate"));
				np.setUser(username);
				playlists.add(np);
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
		return playlists;
	}
	
	public void createPlaylistWithSongs(String username, String namePlaylist, int[] songs) throws SQLException {
		int idPlaylist = -1;
		try {
			con.setAutoCommit(false);
			idPlaylist = findPlaylistId(username, namePlaylist);
			if(idPlaylist == -1) {
				createPlaylist(username, namePlaylist);
				idPlaylist = findPlaylistId(username, namePlaylist);
				for(int song: songs) {
					addSongInPlaylist(idPlaylist, song);
				}
			} 
			con.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			con.rollback(); //riporta tutto allo stato precedente
			throw new SQLException(e);
		} finally {
			con.setAutoCommit(true);
		}
	}
	
	public void createPlaylist(String username, String namePlaylist) throws SQLException {
		String query = "INSERT into Playlist (idUser, name) VALUES(?, ?)";
		PreparedStatement pstatement = null;
		
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setString(2, namePlaylist);
			pstatement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				pstatement.close();
			} catch (Exception e2) { 
				e2.printStackTrace();
				throw new SQLException(e2);
			}
		}
	}
	
	public boolean addSongInPlaylist(int idPlaylist, int idSong) throws SQLException {
		String query = "INSERT into InPlaylist (playlist, song) VALUES(?, ?)";
		int result = 0;
		PreparedStatement pstatement = null;	
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idPlaylist);
			pstatement.setInt(2, idSong);
			result = pstatement.executeUpdate();
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException(e);

		} finally {
			try {
				pstatement.close();
			} catch (Exception e2) {
				e2.printStackTrace();
				throw new SQLException(e2);
			}
		}
		return (result > 0);
	}
	
	public int findPlaylistId(String username, String namePlaylist) throws SQLException {
		int playlist = -1;
		String query = "SELECT idPlaylist FROM Playlist WHERE idUser = ? and name = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;	
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setString(2, namePlaylist);
			result = pstatement.executeQuery();
			if (result.next()) {
				playlist = result.getInt("idPlaylist");
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
		return playlist;
	}
	
	public boolean isPlaylistPresent(String username, int idPlaylist) throws SQLException {
		Playlist p = getPlaylistById(username, idPlaylist);
		if (p!=null) {
			return true;
		} else return false;
	}
	
	public Playlist getPlaylistById(String username, int idPlaylist) throws SQLException {
		String query = "SELECT * FROM Playlist WHERE idUser = ? and idPlaylist = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		Playlist np = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setInt(2, idPlaylist);
			result = pstatement.executeQuery();
			if (result.next()) {
				np = new Playlist();
				np.setId(result.getInt("idPlaylist"));
				np.setName(result.getString("name"));
				np.setDate(result.getDate("creationDate"));
				np.setUser(username);
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
		return np;
	}

	public boolean isSongPresentInPlaylist(int idSong, int idPlaylist) throws SQLException {
		boolean r = false;
		String query = "SELECT * FROM InPlaylist WHERE song = ? and playlist = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setInt(1, idSong);
			pstatement.setInt(2, idPlaylist);
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
}
