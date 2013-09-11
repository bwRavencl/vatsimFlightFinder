package de.bwravencl.vatsimFlightFinder.model;

public class Route {

	private String routeString;

	private String altitude;

	private String remarks;

	private float greatCircleDistance;

	public Route(String routeString, String altitude, String remarks,
			float greatCircleDistance) {
		this.routeString = routeString;
		this.altitude = altitude;
		this.remarks = remarks;
		this.greatCircleDistance = greatCircleDistance;
	}

	public String getRouteString() {
		return routeString;
	}

	public void setRouteString(String routeString) {
		this.routeString = routeString;
	}

	public String getAltitude() {
		return altitude;
	}

	public void setAltitude(String altitude) {
		this.altitude = altitude;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public float getGreatCircleDistance() {
		return greatCircleDistance;
	}

	public void setGreatCircleDistance(float greatCircleDistance) {
		this.greatCircleDistance = greatCircleDistance;
	}
}
