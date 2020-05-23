package it.polito.tdp.extflightdelays.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import it.polito.tdp.extflightdelays.model.Adiacente;
import it.polito.tdp.extflightdelays.model.Airline;
import it.polito.tdp.extflightdelays.model.Airport;
import it.polito.tdp.extflightdelays.model.Flight;

public class ExtFlightDelaysDAO {

	public List<Airline> loadAllAirlines() {
		String sql = "SELECT * from airlines";
		List<Airline> result = new ArrayList<Airline>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				result.add(new Airline(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRLINE")));
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public void loadAllAirports(Map<Integer,Airport> mapId) {
		String sql = "SELECT * FROM airports";
		List<Airport> result = new ArrayList<Airport>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Airport airport = new Airport(rs.getInt("ID"), rs.getString("IATA_CODE"), rs.getString("AIRPORT"),
						rs.getString("CITY"), rs.getString("STATE"), rs.getString("COUNTRY"), rs.getDouble("LATITUDE"),
						rs.getDouble("LONGITUDE"), rs.getDouble("TIMEZONE_OFFSET"));
				result.add(airport);
				mapId.put(airport.getId(),airport);				
			}

			conn.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}

	public List<Flight> loadAllFlights() {
		String sql = "SELECT * FROM flights";
		List<Flight> result = new LinkedList<Flight>();

		try {
			Connection conn = ConnectDB.getConnection();
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet rs = st.executeQuery();

			while (rs.next()) {
				Flight flight = new Flight(rs.getInt("ID"), rs.getInt("AIRLINE_ID"), rs.getInt("FLIGHT_NUMBER"),
						rs.getString("TAIL_NUMBER"), rs.getInt("ORIGIN_AIRPORT_ID"),
						rs.getInt("DESTINATION_AIRPORT_ID"),
						rs.getTimestamp("SCHEDULED_DEPARTURE_DATE").toLocalDateTime(), rs.getDouble("DEPARTURE_DELAY"),
						rs.getDouble("ELAPSED_TIME"), rs.getInt("DISTANCE"),
						rs.getTimestamp("ARRIVAL_DATE").toLocalDateTime(), rs.getDouble("ARRIVAL_DELAY"));
				result.add(flight);
			}

			conn.close();
			return result;

		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("Errore connessione al database");
			throw new RuntimeException("Error Connection Database");
		}
	}
	
	/*public List<Adiacente> getAdiacenze(int dmin){
		String sql= "SELECT  f1.ORIGIN_AIRPORT_ID as id1, f1.DESTINATION_AIRPORT_ID as id2, AVG(DISTANCE) as distanzaMedia " + 
				"FROM flights f1 " + 
				"WHERE f1.ORIGIN_AIRPORT_ID< f1.DESTINATION_AIRPORT_ID " + 
				"AND DISTANCE> ?" + 
				"GROUP BY f1.ORIGIN_AIRPORT_ID, f1.DESTINATION_AIRPORT_ID ";
		Connection conn = ConnectDB.getConnection();
		List<Adiacente> adiacenze=new ArrayList<Adiacente>();
		try {
			PreparedStatement st = conn.prepareStatement(sql);
			ResultSet res=st.executeQuery();
			st.setInt(1,dmin);
			while (res.next()) {
				adiacenze.add(new Adiacente(res.getInt("id1"),res.getInt("id2"),res.getFloat("distanzaMedia")));
			}
			conn.close();
			return adiacenze;
		}
		 catch (SQLException e) {
				e.printStackTrace();
			}
		return null;
	
	}*/
	public List<Adiacente> getAdiacenza(int distanza)
	{
		final String sql="SELECT distinct f.ORIGIN_AIRPORT_ID, f.DESTINATION_AIRPORT_ID, SUM(f.DISTANCE) AS distanze, COUNT(*) AS conta\r\n" + 
				"FROM flights AS f\r\n" + 
				"GROUP BY f.ORIGIN_AIRPORT_ID, f.DESTINATION_AIRPORT_ID";
		List <Adiacente> adiacenze=new ArrayList<Adiacente>();
		try
		{
			Connection conn=ConnectDB.getConnection();
			PreparedStatement st=conn.prepareStatement(sql);
			ResultSet rs=st.executeQuery();
			while(rs.next())
			{
				Adiacente a=new Adiacente(rs.getInt("f.ORIGIN_AIRPORT_ID"), rs.getInt("f.DESTINATION_AIRPORT_ID"), rs.getFloat("distanze"), rs.getInt("conta"));
				Adiacente adEs=this.checkAdiacenze(a, adiacenze);
				if(adEs==null)
				{
					a.setDistanza(a.getDistanza()/a.getConta());
					if(a.getDistanza()>=distanza)
						adiacenze.add(a);
				}
				else
				{
					adiacenze.remove(adEs);
					//adEs.setDistanza((a.getDistanza()+adEs.getDistanza())/(a.getConta()+adEs.getConta()));
					adEs.setDistanza((a.getDistanza()+adEs.getDistanza()*adEs.getConta())/(a.getConta()+adEs.getConta()));
					if(adEs.getDistanza()>=distanza)
						adiacenze.add(adEs);
				}
			}
			conn.close();
			return adiacenze;
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
		return null;
	}

	private Adiacente checkAdiacenze(Adiacente a, List<Adiacente> adiacenze) 
	{
		for(Adiacente ad:adiacenze)
		{
			if((ad.getAirP()==a.getAirP() && ad.getAirA()==a.getAirA()) || (ad.getAirP()==a.getAirA() && ad.getAirA()==a.getAirP()))
			{
				return ad;
			}
			
		}
		return null;
	}
}
