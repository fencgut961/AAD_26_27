package org.example.fjencinas.h2.repository;

import org.example.fjencinas.h2.entity.ItemH2Entity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemH2Repository extends JpaRepository<ItemH2Entity, Long> {

}