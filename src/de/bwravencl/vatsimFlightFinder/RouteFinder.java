package de.bwravencl.vatsimFlightFinder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import de.bwravencl.vatsimFlightFinder.model.Route;

public class RouteFinder {

	public static final String VATROUTE_BASE_URL = "http://www.vatroute.net/web_showfp.php";
	public static final String GREAT_CIRCLEDISTANCE_STRING = "<td width=\"100%\" align=\"center\"><b>Great circle distance:</b> ";

	public static final int STATE_OMIT = 0;
	public static final int STATE_ALTITUDE = 1;
	public static final int STATE_ROUTE_STRING = 2;
	public static final int STATE_REMARKS = 3;

	private static List<Route> routes;

	private static float greatCircleDistance;
	private static String currentAltitude;
	private static String currentRouteString;
	private static String currentRemarks;

	private static int state;

	private static boolean run;

	private static String departureAirport;
	private static String destinationAirport;

	public static List<Route> findRoutes(String departureAirport,
			String destinationAirport) {
		RouteFinder.departureAirport = departureAirport;
		RouteFinder.destinationAirport = destinationAirport;

		greatCircleDistance = -1f;
		routes = new ArrayList<Route>();
		state = STATE_OMIT;
		run = true;

		// System.out.println("Looking for Routes for: " + departureAirport
		// + " -> " + destinationAirport);

		try {
			InputStream is = new URL(VATROUTE_BASE_URL + "?dep="
					+ departureAirport + "&dest=" + destinationAirport)
					.openStream();

			try {
				XmlPullParserFactory factory = XmlPullParserFactory
						.newInstance();
				XmlPullParser xpp = factory.newPullParser();
				xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
				xpp.setInput(is, null);

				processDocument(xpp);
			} catch (XmlPullParserException e) {
				e.printStackTrace();
			} finally {
				is.close();
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return routes;
	}

	private static void processDocument(XmlPullParser xpp)
			throws XmlPullParserException, IOException {
		int eventType = xpp.getEventType();
		do {
			if (eventType == XmlPullParser.START_TAG)
				processStartElement(xpp);
			else if (eventType == XmlPullParser.END_TAG)
				processEndElement(xpp);
			else if (eventType == XmlPullParser.TEXT)
				processText(xpp);

			try {
				eventType = xpp.next();
			} catch (XmlPullParserException e) {
			}
		} while (eventType != XmlPullParser.END_DOCUMENT && run);
	}

	private static void processStartElement(XmlPullParser xpp) {
		String name = xpp.getName();
		String class_ = xpp.getAttributeValue(null, "class");

		if ("td".equals(name) && "tdcellspacing-left".equals(class_)) {
			state = STATE_ALTITUDE;

			currentRouteString = null;
			currentRemarks = null;
		} else if ("td".equals(name) && "tdcellspacing-right".equals(class_)) {
			state = STATE_ROUTE_STRING;

			currentRemarks = null;
		} else if ("td".equals(name) && "tdcellspacing".equals(class_))
			state = STATE_REMARKS;
	}

	private static void processEndElement(XmlPullParser xpp) {
		String name;
		String class_;

		try {
			name = xpp.getName();
			class_ = xpp.getAttributeValue(null, "class");
		} catch (IndexOutOfBoundsException e) {
			name = null;
			class_ = null;
		}

		if ("td".equals(name) && "tdcellspacing-left".equals(class_))
			state = STATE_OMIT;
		else if ("td".equals(name) && "tdcellspacing-right".equals(class_))
			state = STATE_OMIT;
		else if ("td".equals(name) && "tdcellspacing".equals(class_))
			state = STATE_OMIT;
	}

	private static void processText(XmlPullParser xpp)
			throws XmlPullParserException {
		String text;
		try {
			text = xpp.getText();
		} catch (StringIndexOutOfBoundsException e) {
			text = null;
		}

		switch (state) {
		case STATE_ALTITUDE:
			if ("Altitude".equals(text) || text.startsWith("\n"))
				state = STATE_OMIT;
			else
				currentAltitude = text;
			break;
		case STATE_ROUTE_STRING:
			if (text.startsWith("\n"))
				state = STATE_OMIT;
			else
				currentRouteString = text;
			break;
		case STATE_REMARKS:
			if ("Route".equals(text) || "Remarks".equals(text))
				// || text.startsWith("\n"))
				state = STATE_OMIT;
			else {
				currentRemarks = text;

				if (currentRemarks.startsWith("\n"))
					currentRemarks = "";

				if (currentRouteString != null && currentAltitude != null
						&& currentRemarks != null) {
					routes.add(new Route(currentRouteString, currentAltitude,
							currentRemarks, getGreatCircleDistance()));

					state = STATE_OMIT;
					currentRouteString = null;
					currentAltitude = null;
					currentRemarks = null;
				}
			}
			break;
		default:
			break;
		}
	}

	private static float getGreatCircleDistance() {
		if (greatCircleDistance != -1f)
			return greatCircleDistance;
		else {
			try {
				InputStream is = new URL(VATROUTE_BASE_URL + "?dep="
						+ departureAirport + "&dest=" + destinationAirport)
						.openStream();

				String pageString = convertStreamToString(is);

				if (pageString == null)
					return -1f;
				else {
					int beginIndex = pageString
							.indexOf(GREAT_CIRCLEDISTANCE_STRING);
					int endIndex = pageString.indexOf(" NM", beginIndex);

					String distString = pageString.substring(beginIndex
							+ GREAT_CIRCLEDISTANCE_STRING.length(), endIndex);
					
					distString = distString.replaceAll(",", ".");

					return Float.parseFloat(distString);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
				return -1f;
			} catch (IOException e) {
				e.printStackTrace();
				return -1f;
			}
		}
	}

	static String convertStreamToString(java.io.InputStream is) {
		java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}
}
