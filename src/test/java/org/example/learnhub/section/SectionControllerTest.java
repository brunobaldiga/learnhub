package org.example.learnhub.section;

import org.example.learnhub.config.SecurityConfiguration;
import org.example.learnhub.section.controller.SectionController;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;

@WebMvcTest(SectionController.class)
@Import(SecurityConfiguration.class)
public class SectionControllerTest {

}