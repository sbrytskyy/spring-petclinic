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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.visit.Visit;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Ken Krebs
 * @author Arjen Poutsma
 */
@Controller
class VetController {

	private static final String VIEWS_VET_CREATE_OR_UPDATE_FORM = "vets/createOrUpdateVetForm";

	private final VetRepository vets;
	private final VisitRepository visits;

	public VetController(VetRepository clinicService,
		VisitRepository visits) {
		this.vets = clinicService;
		this.visits = visits;
	}

	@ModelAttribute("allSpecialties")
	public Collection<Specialty> populateSpecialties() {
		return this.vets.findSpecialties();
	}

	@GetMapping("/vets/new")
	public String initCreationForm(Map<String, Object> model) {
		Vet vet = new Vet();
		model.put("vet", vet);
		return VIEWS_VET_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/vets/new")
	public String processCreationForm(@Valid Vet vet, BindingResult result) {
		if (result.hasErrors()) {
			return VIEWS_VET_CREATE_OR_UPDATE_FORM;
		}
		else {
			this.vets.save(vet);
			return "redirect:/vets/" + vet.getId();
		}
	}

	@GetMapping("/vets/{vetId}/edit")
	public String initUpdateVetForm(@PathVariable("vetId") int vetId, Model model) {
		Vet vet = this.vets.findById(vetId);
		model.addAttribute(vet);
		return VIEWS_VET_CREATE_OR_UPDATE_FORM;
	}

	@PostMapping("/vets/{vetId}/edit")
	public String processUpdateVetForm(@Valid Vet vet, BindingResult result,
		@PathVariable("vetId") int vetId) {
		if (result.hasErrors()) {
			return VIEWS_VET_CREATE_OR_UPDATE_FORM;
		}
		else {
			vet.setId(vetId);
			this.vets.save(vet);
			return "redirect:/vets/{vetId}";
		}
	}

	/**
	 * Custom handler for displaying an vet.
	 * @param vetId the ID of the vet to display
	 * @return a ModelMap with the model attributes for the view
	 */
	@GetMapping("/vets/{vetId}")
	public ModelAndView showVet(@PathVariable("vetId") int vetId) {
		ModelAndView mav = new ModelAndView("vets/vetDetails");
		Vet vet = this.vets.findById(vetId);
		List<Visit> visits = this.visits.findByVetId(vet.getId());
		vet.setVisitsInternal(visits);
		mav.addObject(vet);
		return mav;
	}

	@GetMapping("/vets")
	public String showVetList(@RequestParam(defaultValue = "1") int page, Model model) {
		// Here we are returning an object of type 'Vets' rather than a collection of Vet
		// objects so it is simpler for Object-Xml mapping
		Page<Vet> paginated = findPaginated(page);
		return addPaginationModel(page, paginated, model);

	}

	private String addPaginationModel(int page, Page<Vet> paginated, Model model) {
	    //model.addAttribute("listVets", paginated);
		List<Vet> listVets = paginated.getContent();
		model.addAttribute("currentPage", page);
		model.addAttribute("totalPages", paginated.getTotalPages());
		model.addAttribute("totalItems", paginated.getTotalElements());
		model.addAttribute("listVets", listVets);
		return "vets/vetList";
	}

	private Page<Vet> findPaginated(int page) {
		int pageSize = 5;
		Pageable pageable = PageRequest.of(page - 1, pageSize);
		return vets.findAll(pageable);
	}
}
