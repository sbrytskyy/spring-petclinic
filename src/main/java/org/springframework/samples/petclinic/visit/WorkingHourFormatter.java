package org.springframework.samples.petclinic.visit;

import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

/**
 * Instructs Spring MVC on how to parse and print elements of type 'WorkingHour'.
 */
@Component
public class WorkingHourFormatter implements Formatter<WorkingHour> {

	private final VisitRepository visits;

	@Autowired
	public WorkingHourFormatter(VisitRepository visits) {
		this.visits = visits;
	}

	@Override
	public String print(WorkingHour wh, Locale locale) {
		return wh.getName();
	}

	@Override
	public WorkingHour parse(String text, Locale locale) throws ParseException {
		int id;
		try {
			id = Integer.parseInt(text);
		} catch (NumberFormatException e) {
			throw new ParseException("Wrong working hour id: " + text, 0);
		}
		Collection<WorkingHour> findWorkingHours = this.visits.findWorkingHours();
		for (WorkingHour wh : findWorkingHours) {
			if (wh.getId().equals(id)) {
				return wh;
			}
		}
		throw new ParseException("working hour not found: " + text, 0);
	}

}
