package br.com.controlei;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * A arquitetura declarada (domain / application / infrastructure, portas e adaptadores) como teste: se alguem
 * importar a camada errada, o build quebra. Antes, a regra so existia nos documentos, e foi violada sem ninguem notar
 * (dois services importavam o publicador do Kafka, que e infraestrutura).
 */
@AnalyzeClasses(packages = "br.com.controlei", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainDoesNotKnowTheOtherLayers = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..application..", "..infrastructure..")
            .because("o dominio e o centro: nao depende de quem o usa");

    @ArchTest
    static final ArchRule domainIsFreeOfFrameworks = noClasses().that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..", "jakarta.persistence..", "org.hibernate..")
            .because("regra de negocio nao pode depender de Spring nem de JPA (a validacao Jakarta nos DTOs e a unica excecao conhecida)");

    @ArchTest
    static final ArchRule applicationDoesNotDependOnInfrastructure = noClasses().that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .because("a aplicacao fala com a infraestrutura por portas (interfaces), nunca por classes concretas");

    @ArchTest
    static final ArchRule controllersTalkToServicesOnly = noClasses().that().resideInAPackage("..application.controllers..")
            .should().dependOnClassesThat().resideInAnyPackage("..infrastructure..", "..domain.contracts.repositories..")
            .because("controller recebe e responde HTTP; quem decide e o service");

    @ArchTest
    static final ArchRule persistenceEntitiesStayInInfrastructure = noClasses()
            .that().resideOutsideOfPackage("..infrastructure..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure.persistence..")
            .because("entidade JPA e detalhe de persistencia; o resto do sistema usa o modelo de dominio");

    @ArchTest
    static final ArchRule noFieldInjection = fields().should().notBeAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
            .because("injecao por construtor deixa as dependencias explicitas e os objetos testaveis sem Spring");

    @ArchTest
    static final ArchRule portsAreInterfaces = classes()
            .that().resideInAPackage("..domain.contracts..").and().areTopLevelClasses()
            .should().beInterfaces()
            .because("uma porta e um contrato; a implementacao concreta mora na infraestrutura");
}
