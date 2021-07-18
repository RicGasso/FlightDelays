package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {
	
	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer, Airport> idMap;
	Map<Airport, Airport> visita;
	
	public Model() {
		dao = new ExtFlightDelaysDAO();
		idMap = new HashMap<Integer, Airport>();
		dao.loadAllAirports(idMap);
	}
	
	public void creaGrafo(int x) {
		this.grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		//aggiungo vertici filtrati
		Graphs.addAllVertices(grafo, dao.getVertici(x, idMap));
		
		//aggiungo gli archi
		for(Rotta r : dao.getRotte(idMap)) {
			if(this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())) {
				DefaultWeightedEdge e= this.grafo.getEdge(r.getA1(), r.getA2());
				if(e==null) {
					Graphs.addEdgeWithVertices(grafo, r.getA1(), r.getA2(), r.getN());
				}else {
					double pesoVecchio = this.grafo.getEdgeWeight(e);
					double pesoNuovo = pesoVecchio + r.getN();
					this.grafo.setEdgeWeight(e, pesoNuovo);
				}
			}
		}
		System.out.println("Grafo creato con "+ this.grafo.vertexSet().size() +" vertici e "+ this.grafo.edgeSet().size() +" lati");
	}

	public Set<Airport> getVertici() {
		return this.grafo.vertexSet();
	}
	
	public List<Airport> trovaPercorso(Airport a1, Airport a2){
		List<Airport> percorso = new LinkedList<Airport>();
		
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(this.grafo, a1);
		
		visita = new HashMap<>();
		visita.put(a1, null);
		
		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>(){

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				
			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				
			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {
				Airport airport1 = grafo.getEdgeSource(e.getEdge());
				Airport airport2 = grafo.getEdgeTarget(e.getEdge());
				
				if(visita.containsKey(airport1) && !visita.containsKey(airport2)) {
					visita.put(airport2, airport1);
				} else if(visita.containsKey(airport2) && !visita.containsKey(airport1)) {
					visita.put(airport1, airport2);
				}
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				
			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				
			}
			
		});
		while(it.hasNext()) {
			it.next();
		}
		
		percorso.add(a2);
		Airport step = a2;
		while(visita.get(step)!=null) {
			step = visita.get(step);
			percorso.add(step);
		}
		
		return percorso;
	}
}
