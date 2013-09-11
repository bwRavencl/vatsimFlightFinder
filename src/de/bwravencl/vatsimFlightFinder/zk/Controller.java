package de.bwravencl.vatsimFlightFinder.zk;

import java.io.StringWriter;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Spinner;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import de.bwravencl.vatsimFlightFinder.AirportFinder;
import de.bwravencl.vatsimFlightFinder.SearchEngine;
import de.bwravencl.vatsimFlightFinder.model.Flight;
import de.bwravencl.vatsimFlightFinder.model.Route;

public class Controller extends SelectorComposer<Window> {

	private static final long serialVersionUID = 1L;

	@Wire
	Spinner spinnerTAS;

	@Wire
	Datebox dateboxDepartureBegin;

	@Wire
	Datebox dateboxDepartureEnd;

	@Wire
	Datebox dateboxArrivalBegin;

	@Wire
	Datebox dateboxArrivalEnd;

	@Wire
	Checkbox checkboxCountDelivery;

	@Wire
	Checkbox checkboxCountGround;

	@Wire
	Checkbox checkboxCountTower;

	@Wire
	Checkbox checkboxCountApproach;

	@Wire
	Button buttonSearch;

	@Wire
	Label labelResults;

	@Wire
	Textbox textboxResults;

	@Listen("onClick=#buttonSearch")
	public void submit() {
		if (spinnerTAS.isValid() && dateboxDepartureBegin.isValid()
				&& dateboxDepartureEnd.isValid()
				&& dateboxArrivalBegin.isValid() && dateboxArrivalEnd.isValid()
				&& isFlagsValid()) {
			labelResults.setValue("Results");

			int countFlags = 0;
			if (checkboxCountDelivery.isChecked())
				countFlags |= AirportFinder.FLAG_COUNT_DELIVERY_CONTROLLER;
			if (checkboxCountGround.isChecked())
				countFlags |= AirportFinder.FLAG_COUNT_GROUND_CONTROLLER;
			if (checkboxCountTower.isChecked())
				countFlags |= AirportFinder.FLAG_COUNT_TOWER_CONTROLLER;
			if (checkboxCountApproach.isChecked())
				countFlags |= AirportFinder.FLAG_COUNT_APPROACH_CONTROLLER;

			int trueAirspeed = spinnerTAS.getValue();

			Date departureBegin = dateboxDepartureBegin.getValue();
			Date departureEnd = dateboxDepartureEnd.getValue();

			Date arrivalBegin = dateboxArrivalBegin.getValue();
			Date arrivalEnd = dateboxArrivalEnd.getValue();

			List<Flight> results = SearchEngine.findFlights(trueAirspeed,
					departureBegin, departureEnd, arrivalBegin, arrivalEnd,
					countFlags);

			StringWriter sw = new StringWriter();

			for (Flight f : results) {
				sw.append(f.getDepartureAirport() + " -> "
						+ f.getDestinationAirport() + " ("
						+ convertMillisToString(f.getEstimatedTimeEnrote())
						+ ")\n");
				for (Route r : f.getRoutes())
					sw.append(r.getAltitude() + "    " + r.getRouteString()
							+ "    " + r.getRemarks() + "\n");

				sw.append("\n");
			}

			labelResults.setValue("Results (" + results.size() + ")");
			textboxResults.setValue(sw.toString());
		}
	}

	@Listen("onClick=#checkboxCountDelivery; onClick=#checkboxCountGround; onClick=#checkboxCountTower; onClick=#checkboxCountApproach")
	public void handleFlags() {
		if (isFlagsValid())
			buttonSearch.setDisabled(false);
		else
			buttonSearch.setDisabled(true);
	}

	public boolean isFlagsValid() {
		if (checkboxCountDelivery.isChecked())
			return true;
		else if (checkboxCountGround.isChecked())
			return true;
		else if (checkboxCountTower.isChecked())
			return true;
		else if (checkboxCountApproach.isChecked())
			return true;
		else
			return false;
	}

	public static String convertMillisToString(long millis) {
		return String.format(
				"%02d:%02d",
				TimeUnit.MILLISECONDS.toHours(millis),
				TimeUnit.MILLISECONDS.toMinutes(millis)
						- TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS
								.toHours(millis)));
	}
}
