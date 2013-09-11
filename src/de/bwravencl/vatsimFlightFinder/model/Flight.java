package de.bwravencl.vatsimFlightFinder.model;

import java.util.List;

import de.bwravencl.vatsimFlightFinder.RouteFinder;

public class Flight {

	private int trueAirspeed;

	private String departureAirport;

	private String destinationAirport;

	private List<Route> routes;

	private long estimatedTimeEnrote = 0;

	public Flight(int trueAirspeed, String departureAirport,
			String destinationAirport) {
		this.trueAirspeed = trueAirspeed;
		this.departureAirport = departureAirport;
		this.destinationAirport = destinationAirport;
	}

	public int getTrueAirspeed() {
		return trueAirspeed;
	}

	public void setTrueAirspeed(int trueAirspeed) {
		this.trueAirspeed = trueAirspeed;
	}

	public String getDepartureAirport() {
		return departureAirport;
	}

	public void setDepartureAirport(String departureAirport) {
		this.departureAirport = departureAirport;
	}

	public String getDestinationAirport() {
		return destinationAirport;
	}

	public void setDestinationAirport(String destinationAirport) {
		this.destinationAirport = destinationAirport;
	}

	public List<Route> getRoutes() {
		if (routes == null) {
			routes = RouteFinder.findRoutes(departureAirport,
					destinationAirport);
		}

		return routes;
	}

	public void setRoutes(List<Route> routes) {
		this.routes = routes;
	}

	public long getEstimatedTimeEnrote() {
		if (estimatedTimeEnrote == 0) {

			float greatCircleDistance = routes.get(0).getGreatCircleDistance();

			estimatedTimeEnrote = (long) ((greatCircleDistance / trueAirspeed) * 3600000L);
		}

		return estimatedTimeEnrote;
	}

	public void setEstimatedTimeEnrote(long estimatedTimeEnrote) {
		this.estimatedTimeEnrote = estimatedTimeEnrote;
	}
}
