/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.vet;

import java.text.ParseException;
import java.util.Collection;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.Formatter;
import org.springframework.stereotype.Component;

/**
 * Instructs Spring MVC on how to parse and print elements of type 'Vet'.
 */
@Component
public class VetFormatter implements Formatter<Vet> {

	private final VetRepository vets;

	@Autowired
	public VetFormatter(VetRepository vets) {
		this.vets = vets;
	}

	@Override
	public String print(Vet vet, Locale locale) {
		return vet.getFirstName() + " " + vet.getLastName();
	}

	@Override
	public Vet parse(String text, Locale locale) throws ParseException {
		String[] name = text.split(" ");
		if (name.length == 2) {
			Collection<Vet> findVets = this.vets.findAll();
			for (Vet vet : findVets) {
				if (vet.getFirstName().equals(name[0]) && vet.getLastName().equals(name[1])) {
					return vet;
				}
			}
		}
		throw new ParseException("Vet not found: " + text, 0);
	}

}
