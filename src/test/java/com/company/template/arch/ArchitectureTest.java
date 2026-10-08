package com.company.template.arch;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

/**
 * Architecture validation (Hexagonal/layered):
 * <ul>
 * <li>domain is pure Java (no Spring, no Jakarta).</li>
 * <li>app depends only on domain/app/java (+ Spring stereotype).</li>
 * <li>web adapters never touch persistence/messaging adapters directly.</li>
 * </ul>
 */
class ArchitectureTest {

	private final JavaClasses classes = new ClassFileImporter().importPackages("com.company.template");

	@Test
	void domainIsFrameworkFree() {
		ArchRule rule = noClasses().that()
			.resideInAPackage("..domain..")
			.should()
			.dependOnClassesThat()
			.resideInAnyPackage("org.springframework..", "jakarta..");
		rule.check(classes);
	}

	@Test
	void appDoesNotDependOnAdapters() {
		ArchRule rule = noClasses().that()
			.resideInAPackage("..app..")
			.should()
			.dependOnClassesThat()
			.resideInAPackage("..adapter..");
		rule.check(classes);
	}

	@Test
	void webAdapterDoesNotDependOnOutboundAdapters() {
		ArchRule rule = noClasses().that()
			.resideInAPackage("..adapter.in..")
			.should()
			.dependOnClassesThat()
			.resideInAPackage("..adapter.out..");
		rule.check(classes);
	}

}
