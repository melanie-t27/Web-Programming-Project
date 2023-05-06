package it.polimi.tiw.music.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
	
	public int createPlaylist(String username, String namePlaylist, Date date, int[] songs) throws SQLException {
		String query1 = "INSERT into Playlist (idUser, name, creationDate) VALUES(?, ?, ?)";
		String query2 = "INSERT into InPlaylist (playlist, song) VALUES(?, ?)";
		String query3 = "SELECT LAST_INSERT_ID() FROM Playlist";
		PreparedStatement pstatement1 = null, pstatement2 = null;
		Statement statement = null;
		ResultSet result = null;
		
		int code = 0;	
		int idPlaylist = 0;
		
		try {
			con.setAutoCommit(false);
			pstatement1 = con.prepareStatement(query1);
			pstatement1.setString(1, username);
			pstatement1.setString(2, namePlaylist);
			pstatement1.setDate(3, (java.sql.Date) date);
			code = pstatement1.executeUpdate();
			
			statement = con.createStatement();
			result = statement.executeQuery(query3);
			while(result.next()) {
				idPlaylist = result.getInt("idPlaylist");
			}
			
			pstatement2 = con.prepareStatement(query2);
			for(int song: songs) {
				pstatement2.setInt(1, idPlaylist);
				pstatement2.setInt(2, song);
				pstatement2.executeUpdate();
			}
			con.commit();
			
		} catch (SQLException e) {
			con.rollback(); //riporta tutto allo stato precedente
			throw new SQLException(e);
		} finally {
			con.setAutoCommit(true);
			try {
				result.close();
			} catch (Exception e1) {}
			try {
				pstatement1.close();
				pstatement2.close();
				statement.close();
			} catch (Exception e2) { //errore che la playlist non è stata creata 
				}
			}
		return code;
	}
	
	public Playlist getPlaylistById(String username, int idPlaylist) throws SQLException {
		String query = "SELECT * FROM Playlist WHERE idUser = ? and idPlaylist = ?";
		ResultSet result = null;
		PreparedStatement pstatement = null;
		Playlist np = new Playlist();
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			pstatement.setInt(2, idPlaylist);
			result = pstatement.executeQuery();
			if (result.next()) {
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
	
}
