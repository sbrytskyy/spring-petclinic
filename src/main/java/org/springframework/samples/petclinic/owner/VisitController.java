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
package org.springframework.samples.petclinic.owner;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.samples.petclinic.vet.Vet;
import org.springframework.samples.petclinic.vet.VetRepository;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.samples.petclinic.visit.WorkingHour;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * @author Juergen Hoeller
 * @author Ken Krebs
 * @author Arjen Poutsma
 * @author Michael Isvy
 * @author Dave Syer
 */
@Controller
class VisitController {

	private final VisitRepository visits;

	private final PetRepository pets;
	private final VetRepository vets;

	public VisitController(VisitRepository visits, PetRepository pets,
		VetRepository vets) {
		this.visits = visits;
		this.pets = pets;
		this.vets = vets;
	}

	@InitBinder
	public void setAllowedFields(WebDataBinder dataBinder) {
		dataBinder.setDisallowedFields("id");
	}

	@ModelAttribute("vets")
	public Collection<Vet> populateVets() {
		return this.vets.findAll();
	}

	@ModelAttribute("workingHours")
	public Collection<WorkingHour> populateWorkingHours() {
		return this.visits.findWorkingHours();
	}

	/**
	 * Called before each and every @RequestMapping annotated method. 2 goals: - Make sure
	 * we always have fresh data - Since we do not use the session scope, make sure that
	 * Pet object always has an id (Even though id is not part of the form fields)
	 * @param petId
	 * @return Pet
	 */
	@ModelAttribute("visit")
	public Visit loadPetWithVisit(@PathVariable("petId") int petId, Map<String, Object> model) {
		Pet pet = this.pets.findById(petId);
		pet.setVisitsInternal(this.visits.findByPetId(petId));
		model.put("pet", pet);
		Visit visit = new Visit();
		pet.addVisit(visit);
		return visit;
	}

	// Spring MVC calls method loadPetWithVisit(...) before initNewVisitForm is called
	@GetMapping("/owners/*/pets/{petId}/visits/new")
	public String initNewVisitForm(@PathVariable("petId") int petId, Map<String, Object> model) {
		return "pets/createOrUpdateVisitForm";
	}

	// Spring MVC calls method loadPetWithVisit(...) before processNewVisitForm is called
	@PostMapping("/owners/{ownerId}/pets/{petId}/visits/new")
	public String processNewVisitForm(@Valid Visit visit, BindingResult result) {
		validateVisit(visit, result);

		if (result.hasErrors()) {
			return "pets/createOrUpdateVisitForm";
		}
		else {
			try {
				this.visits.save(visit);
			} catch (DataIntegrityViolationException e) {
				String err = "Appointment conflict. Looks like this time has been booked by some other pet. Please select different date and time.";
				FieldError error = new FieldError("visit", "time", err);
				result.addError(error);
				return "pets/createOrUpdateVisitForm";
			}
			return "redirect:/owners/{ownerId}";
		}
	}

	@GetMapping("/owners/{ownerId}/pets/{petId}/visits/{visitId}/cancel")
	public String processCancelVisitForm(@PathVariable("ownerId") int ownerId,
		@PathVariable("petId") int petId,
		@PathVariable("visitId") int visitId,
		Map<String, Object> model) {

		visits.deleteById(visitId);

		return "redirect:/owners/{ownerId}";
	}

	private void validateVisit(Visit visit, BindingResult result) {
		LocalDate date = visit.getDate();
		LocalDate today = LocalDate.now();
		if (date.isBefore(today)) {
			String err = "Appointment can not be scheduled in the past";
			FieldError error = new FieldError("visit", "date", err);
			result.addError(error);
		}

		DayOfWeek d = date.getDayOfWeek();
		boolean weekend = d == DayOfWeek.SATURDAY || d == DayOfWeek.SUNDAY;
		if (weekend) {
			String err = "Appointment can not be scheduled on weekend";
			FieldError error = new FieldError("visit", "date", err);
			result.addError(error);
		}

		WorkingHour wh = visit.getTime();
		LocalTime localTime = LocalTime.parse(wh.getName().toUpperCase(), DateTimeFormatter.ofPattern("[h:mm a][hh:mm a]"));
		LocalTime now = LocalTime.now().plusHours(1);
		if (date.isEqual(today) && localTime.isBefore(now)) {
			String err = "Appointment can not be scheduled in the past";
			FieldError error = new FieldError("visit", "time", err);
			result.addError(error);
		}
	}
}
