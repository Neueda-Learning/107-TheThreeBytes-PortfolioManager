package com.example.hsbcproject.repository;

import com.example.hsbcproject.HsbcprojectApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
		classes = {HsbcprojectApplication.class},
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
abstract class RepositoryTestSupport {
}

