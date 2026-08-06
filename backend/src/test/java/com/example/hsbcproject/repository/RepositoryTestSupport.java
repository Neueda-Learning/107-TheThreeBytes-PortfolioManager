package com.example.hsbcproject.repository;

import com.example.hsbcproject.HsbcprojectApplication;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest(
		classes = {HsbcprojectApplication.class, RepositoryTestSupport.RepositoryTestConfig.class},
		webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
@Transactional
abstract class RepositoryTestSupport {

	@TestConfiguration
	static class RepositoryTestConfig {
		@Bean
		ObjectMapper objectMapper() {
			return new ObjectMapper();
		}
	}
}

