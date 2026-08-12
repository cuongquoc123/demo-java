package com.example.demo.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.Entity.enrollment;
import com.example.demo.Entity.enrollment_id;

@Repository
public interface enrollmentRepository extends JpaRepository<enrollment, enrollment_id> {

}
