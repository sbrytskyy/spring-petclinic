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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import org.assertj.core.util.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.samples.petclinic.visit.VisitRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

/**
 * Test class for the {@link VetController}
 */

@WebMvcTest(VetController.class)
class VetControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private VetRepository vets;

	@MockBean
	private VisitRepository visits;

	private Vet james;

	private Vet helen;

	@BeforeEach
	void setup() {
		james = new Vet();
		james.setFirstName("James");
		james.setLastName("Carter");
		james.setId(1);
		helen = new Vet();
		helen.setFirstName("Helen");
		helen.setLastName("Leary");
		helen.setId(2);
		Specialty radiology = new Specialty();
		radiology.setId(1);
		radiology.setName("radiology");
		helen.addSpecialty(radiology);
		given(this.vets.findAll()).willReturn(Lists.newArrayList(james, helen));
		given(this.vets.findAll(any(Pageable.class))).willReturn(new PageImpl<Vet>(Lists.newArrayList(james, helen)));

	}

	@Test
	void testShowResourcesVetList() throws Exception {
		mockMvc.perform(MockMvcRequestBuilders.get("/vets?page=1")).andExpect(status().isOk())
			.andExpect(model().attributeExists("listVets")).andExpect(view().name("vets/vetList"));
	}

}
