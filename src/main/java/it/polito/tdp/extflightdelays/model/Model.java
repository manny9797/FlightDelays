package it.polito.tdp.extflightdelays.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jgrapht.Graphs;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.jgrapht.traverse.BreadthFirstIterator;

import it.polito.tdp.extflightdelays.db.ExtFlightDelaysDAO;

public class Model {

	private SimpleWeightedGraph<Airport, DefaultWeightedEdge> grafo;
	private ExtFlightDelaysDAO dao;
	private Map<Integer, Airport> idMap;
	private Map<Airport, Airport> visita;

	public Model() {
		dao = new ExtFlightDelaysDAO();
		idMap = new HashMap<Integer, Airport>();
		dao.loadAllAirports(idMap); // abbiamo modificato il metodo nel DAO, ora riempie la idMap
	}

	public void creaGrafo(int x) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

		// AGGIUNGO I VERTICI FILTRATI

		Graphs.addAllVertices(grafo, dao.getVertici(x, idMap));

		// AGGIUNGO GLI ARCHI

		for (Rotta r : this.dao.getRotte(idMap)) {

			// SE E' UNA ROTTA DI INTERESSE

			if (this.grafo.containsVertex(r.getA1()) && this.grafo.containsVertex(r.getA2())) {

				DefaultWeightedEdge e = this.grafo.getEdge(r.getA1(), r.getA2());

				if (e == null) {
					Graphs.addEdgeWithVertices(grafo, r.getA1(), r.getA2());
				} else {
					double pesoVecchio = this.grafo.getEdgeWeight(e);
					double pesoNuovo = pesoVecchio + r.getN();
					this.grafo.setEdgeWeight(e, pesoNuovo);
				}
			}
		}
		System.out.println("GRAFO CREATO ");
		System.out.println("# Vertici: " + grafo.vertexSet().size());
		System.out.println("# Archi: " + grafo.edgeSet().size());

	}

	public Set<Airport> getVertici() {
		return this.grafo.vertexSet();
	}

	public List<Airport> trovaPercorso(final Airport a1, Airport a2) {
		List<Airport> percorso = new LinkedList<>();
		BreadthFirstIterator<Airport, DefaultWeightedEdge> it = new BreadthFirstIterator<>(grafo, a1); // USO
																										// L'ITERATORE
																										// PER VISITARE
																										// // IL GRAFO
																										// PASSO PER
		visita = new HashMap<>(); // PASSO

		visita.put(a1, null);
		// FINCHE' L'ITERATORE HA UN PROSSIMO NODO DA VISITARE, IO LO VISITO

		it.addTraversalListener(new TraversalListener<Airport, DefaultWeightedEdge>() {

			@Override
			public void connectedComponentFinished(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void connectedComponentStarted(ConnectedComponentTraversalEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void edgeTraversed(EdgeTraversalEvent<DefaultWeightedEdge> e) {

				Airport a1 = grafo.getEdgeSource(e.getEdge());
				Airport a2 = grafo.getEdgeTarget(e.getEdge());

				if (visita.containsKey(a1) && !visita.containsKey(a2)) {
					visita.put(a2, a1);
				} else if (visita.containsKey(a2) && !visita.containsKey(a1)) {
					visita.put(a1, a2);
				}
			}

			@Override
			public void vertexTraversed(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void vertexFinished(VertexTraversalEvent<Airport> e) {
				// TODO Auto-generated method stub

			}
		}

		); // ASSOCIO ALL'ITERATORE UN REGISTRATORE DI EVENTI // QUANDO SUCCEDE QUALCOSA
			// (ATTRAVERSO UN ARCO) SALVO IL MIO ALBERO DI VISITA
		while (it.hasNext()) {
			it.next();
		}

		percorso.add(a2);
		Airport step = a2;

		while (visita.get(step) != null) {

			step = visita.get(step);
			percorso.add(step);

		}
		return percorso;
	}
}
