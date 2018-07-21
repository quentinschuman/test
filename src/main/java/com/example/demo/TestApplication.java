package com.example.demo;

import org.beetl.sql.core.engine.PageQuery;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TestApplication extends SpringBootServletInitializer {

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		return application.sources(TestApplication.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication app = new SpringApplication(TestApplication.class);
		app.setBannerMode(Banner.Mode.OFF);
		PageQuery.DEFAULT_PAGE_SIZE = 20 ;
		app.run(args);
	}
}
