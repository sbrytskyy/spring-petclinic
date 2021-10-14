package org.springframework.samples.petclinic.visit;

import javax.persistence.Entity;
import javax.persistence.Table;
import org.springframework.samples.petclinic.model.NamedEntity;

@Entity
@Table(name = "working_hour")
public class WorkingHour extends NamedEntity {

}
