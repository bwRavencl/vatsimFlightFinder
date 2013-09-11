package de.bwravencl.vatsimFlightFinder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.bwravencl.vatsimFlightFinder.model.Flight;

public class SearchEngine {

	public static List<Flight> findFlights(int trueAirspeed,
			Date departureBegin, Date departureEnd, Date arrivalBegin,
			Date arrivalEnd, int countFlags) {
		List<Flight> results = new ArrayList<Flight>();

		long minTimeEnroute = arrivalBegin.getTime() - departureEnd.getTime();
		long maxTimeEnroute = arrivalEnd.getTime() - departureBegin.getTime();

		if (minTimeEnroute <= 0 || maxTimeEnroute <= 0)
			return results;

		List<String> departureAirports = AirportFinder.findAirports(
				departureBegin, departureEnd, countFlags);
		List<String> destinationAirports = AirportFinder.findAirports(
				arrivalBegin, arrivalEnd, countFlags);

		// departureAirports.clear();
		// departureAirports.add("EDDM");
		//
		// destinationAirports.clear();
		// destinationAirports.add("EGKK");

		// Create flights from departure-destination pairs
		for (String departureAirport : departureAirports) {
			for (String destinationAirport : destinationAirports) {
				if (!departureAirport.equals(destinationAirport)) {
					Flight flight = new Flight(trueAirspeed, departureAirport,
							destinationAirport);

					// Check if there exists a route for this flight
					if (flight.getRoutes().size() > 0) {
						// Add flight to results if it meets the given time
						// constraints
						long estimatedTimeEnrote = flight
								.getEstimatedTimeEnrote();
						if (estimatedTimeEnrote >= minTimeEnroute
								&& estimatedTimeEnrote <= maxTimeEnroute)
							results.add(flight);
					}
				}
			}
		}

		return results;
	}
}
