package it.polito.tdp.extflightdelays.model;

import java.util.*;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
private Map<Integer, Airport> idMap;
private Graph<Airport, DefaultWeightedEdge> grafo;

public Model() {
	idMap=new TreeMap<Integer, Airport>();
}

public String creaGrafo(int dmin) {
	this.grafo= new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
	ExtFlightDelaysDAO dao=new ExtFlightDelaysDAO();
	dao.loadAllAirports(idMap);
	String s="";
	//aggiungo i vertici
	Graphs.addAllVertices(this.grafo,idMap.values());
	List<Adiacente> ad=dao.getAdiacenza(dmin);
	for (Adiacente a:ad) {
			Graphs.addEdge(this.grafo,idMap.get(a.getAirP()), idMap.get(a.getAirA()),a.getDistanza());
		}
	//System.out.println(String.format("Grafo ceato! #vertici %d, # Archi %d", this.grafo.vertexSet().size(), this.grafo.edgeSet().size()));
	s+=String.format("Grafo ceato! #vertici %d, # Archi %d ", this.grafo.vertexSet().size(), this.grafo.edgeSet().size());
	s+="\n";
	for (Adiacente a:ad) {
		s+=a.getAirP()+ " verso "+a.getAirA()+ " distanza: "+a.getDistanza()+"\n";
	}
	return s.substring(0,s.length()-1);
}

}


