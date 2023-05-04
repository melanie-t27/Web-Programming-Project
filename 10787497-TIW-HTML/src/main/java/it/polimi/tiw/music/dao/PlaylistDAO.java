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
	
	public List<Playlist> findPlaylistsByUsername(String username) throws SQLException {
		List<Playlist> playlists = new ArrayList<>();
		String query = "SELECT * FROM Playlist WHERE idUser = ? ORDER BY creationDate desc";
		ResultSet result = null;
		PreparedStatement pstatement = null;	
		try {
			pstatement = con.prepareStatement(query);
			pstatement.setString(1, username);
			result = pstatement.executeQuery();
			while (result.next()) {
				result.getString("idUser");
				Playlist np = new Playlist();
				np.setId(result.getInt("idPlaylist"));
				np.setName(result.getString("name"));
				np.setDate(result.getDate("creationDate"));
				playlists.add(np);
			}
		} catch (SQLException e) {
		    e.printStackTrace();
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
	
	public int createPlaylist(String username, String namePlaylist, Date date, List<Integer> songs) throws SQLException {
		String query1 = "INSERT into Playlist (idUser, name, creationDate) VALUES(?, ?, ?)";
		String query2 = "INSERT into InPlaylist (playlist, song) VALUES(?, ?)";
		String query3 = "SELECT LAST_INSERT_ID() FROM Playlist";
		PreparedStatement pstatement = null;
		Statement statement = null;
		ResultSet result = null;
		
		int code = 0;	
		int idPlaylist = 0;
		
		try {
			pstatement = con.prepareStatement(query1);
			pstatement.setString(1, username);
			pstatement.setString(2, namePlaylist);
			pstatement.setDate(3, (java.sql.Date) date);
			code = pstatement.executeUpdate();
			
			statement = con.createStatement();
			result = statement.executeQuery(query3);
			while(result.next()) {
				idPlaylist = result.getInt("idPlaylist");
			}
			
			for(Integer song: songs) {
				pstatement = con.prepareStatement(query2);
				pstatement.setInt(1, idPlaylist);
				pstatement.setInt(2, song);
				pstatement.executeUpdate();
			}
			
			
		} catch (SQLException e) {
		    e.printStackTrace();
			throw new SQLException(e);
		} finally {
			try {
				pstatement.close();
			} catch (Exception e1) {}
		}
		return code;
	}
	
	
	
}
