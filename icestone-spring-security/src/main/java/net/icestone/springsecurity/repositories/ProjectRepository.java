package net.icestone.springsecurity.repositories;

import org.springframework.data.repository.CrudRepository;

import net.icestone.springsecurity.models.Project;

public interface ProjectRepository extends CrudRepository<Project, Long> { 
	
    @Override
    Iterable<Project> findAllById(Iterable<Long> iterable);

}
