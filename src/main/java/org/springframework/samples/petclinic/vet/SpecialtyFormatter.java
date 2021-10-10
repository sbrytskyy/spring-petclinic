package org.springframework.samples.petclinic.vet;

import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

/**
 * Instructs Spring MVC on how to parse and print elements of type 'Specialty'.
 */
@Component
public class SpecialtyFormatter implements Formatter<Specialty> {

	private final VetRepository vets;

	@Autowired
	public SpecialtyFormatter(VetRepository vets) {
		this.vets = vets;
	}

	@Override
	public String print(Specialty specialty, Locale locale) {
		return specialty.getName();
	}

	@Override
	public Specialty parse(String text, Locale locale) throws ParseException {
		Collection<Specialty> findSpecialties = this.vets.findSpecialties();
		for (Specialty spec : findSpecialties) {
			if (spec.getName().equals(text)) {
				return spec;
			}
		}
		throw new ParseException("Specialty not found: " + text, 0);
	}

}
