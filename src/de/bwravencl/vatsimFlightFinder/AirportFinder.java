package de.bwravencl.vatsimFlightFinder;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class AirportFinder {

	public static final int FLAG_COUNT_DELIVERY_CONTROLLER = 0;
	public static final int FLAG_COUNT_GROUND_CONTROLLER = 1;
	public static final int FLAG_COUNT_TOWER_CONTROLLER = 2;
	public static final int FLAG_COUNT_APPROACH_CONTROLLER = 3;

	public static final String BOOKINGS_URL = "http://vatbook.euroutepro.com/xml2.php";

	public static final int STATE_OMIT = 0;
	public static final int STATE_CALLSIGN = 1;
	public static final int STATE_TIME_START = 2;
	public static final int STATE_TIME_END = 3;

	public static final String DELIVERY_CONTROLLER_SUFFIX = "_DEL";
	public static final String GROUND_CONTROLLER_SUFFIX = "_GND";
	public static final String TOWER_CONTROLLER_SUFFIX = "_TWR";
	public static final String APPROACH_CONTROLLER_SUFFIX = "_APP";

	private static Date dateStart;
	private static Date dateEnd;

	private static int countFlags;

	private static int state;

	private static List<String> results;

	private static String currentCallsign;
	private static String currentTimeStart;
	private static String currentTimeEnd;

	public static List<String> findAirports(Date dateStart, Date dateEnd,
			int countFlags) {
		AirportFinder.countFlags = countFlags;
		List<String> results = new ArrayList<String>();

		try {
			InputStream is = new URL(BOOKINGS_URL).openStream();
			results = AirportFinder.findBookings(is, dateStart, dateEnd);

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (XmlPullParserException e) {
			e.printStackTrace();
		}

		return results;
	}

	public static List<String> findBookings(InputStream is, Date dateStart,
			Date dateEnd) throws XmlPullParserException, IOException {
		AirportFinder.dateStart = dateStart;
		AirportFinder.dateEnd = dateEnd;

		state = STATE_OMIT;
		results = new ArrayList<String>();

		try {
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser xpp = factory.newPullParser();
			xpp.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
			xpp.setInput(is, null);

			processDocument(xpp);

			return results;
		} finally {
			is.close();
		}
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

			eventType = xpp.next();
		} while (eventType != XmlPullParser.END_DOCUMENT);
	}

	private static void processStartElement(XmlPullParser xpp) {
		String name = xpp.getName();

		if ("booking".equals(name)) {
			currentCallsign = null;
			currentTimeStart = null;
			currentTimeEnd = null;
		} else if ("callsign".equals(name))
			state = STATE_CALLSIGN;
		else if ("time_start".equals(name))
			state = STATE_TIME_START;
		else if ("time_end".equals(name))
			state = STATE_TIME_END;
	}

	private static void processEndElement(XmlPullParser xpp) {
		String name = xpp.getName();

		if ("booking".equals(name)) {
			if (currentCallsign != null
					&& currentTimeStart != null
					&& currentTimeEnd != null
					&& (((currentCallsign.endsWith(DELIVERY_CONTROLLER_SUFFIX)) && ((countFlags & FLAG_COUNT_DELIVERY_CONTROLLER) == FLAG_COUNT_DELIVERY_CONTROLLER))
							|| ((currentCallsign
									.endsWith(GROUND_CONTROLLER_SUFFIX)) && ((countFlags & FLAG_COUNT_GROUND_CONTROLLER) == FLAG_COUNT_GROUND_CONTROLLER))
							|| ((currentCallsign
									.endsWith(TOWER_CONTROLLER_SUFFIX)) && ((countFlags & FLAG_COUNT_TOWER_CONTROLLER) == FLAG_COUNT_TOWER_CONTROLLER)) || ((currentCallsign
							.endsWith(APPROACH_CONTROLLER_SUFFIX)) && ((countFlags & FLAG_COUNT_APPROACH_CONTROLLER) == FLAG_COUNT_APPROACH_CONTROLLER)))) {

				// Filter for correct four digit ICAO codes
				String icaoCode = currentCallsign.substring(0,
						currentCallsign.indexOf("_"));
				if (icaoCode.length() == 4) {

					DateFormat dateFormat = new SimpleDateFormat(
							"yyy-MM-dd kk:mm:ss", Locale.ENGLISH);
					try {
						Date currentDateStart = dateFormat
								.parse(currentTimeStart);
						Date currentDateEnd = dateFormat.parse(currentTimeEnd);

						if ((currentDateStart.compareTo(dateStart) <= 0)
								&& (currentDateEnd.compareTo(dateEnd) >= 0)) {

							if (!results.contains(icaoCode))
								results.add(icaoCode);
							// System.out.println("ATC: " + currentCallsign
							// + "\nFrom: " + currentTimeStart + "\nTo: "
							// + currentTimeEnd + "\n");
						}
					} catch (ParseException e) {
						e.printStackTrace();
					}
				}
			}
		} else if ("callsign".equals(name))
			state = STATE_OMIT;
		else if ("time_start".equals(name))
			state = STATE_OMIT;
		else if ("time_end".equals(name))
			state = STATE_OMIT;
	}

	private static void processText(XmlPullParser xpp)
			throws XmlPullParserException {
		String text = xpp.getText();

		switch (state) {
		case STATE_CALLSIGN:
			currentCallsign = text;
			break;
		case STATE_TIME_START:
			currentTimeStart = text;
			break;
		case STATE_TIME_END:
			currentTimeEnd = text;
			break;
		default:
			break;
		}
	}
}
