//import cucumber.runtime.{EN, ScalaDsl, PendingException}
import models.ReferenceType._
import db._
import models._

import cucumber.api.scala.{ScalaDsl, EN}
import play.api.test._

import play.api._
import libs.ws.WS
import play.api.mvc._
import play.api.http._

import play.api.libs.iteratee._
import play.api.libs.concurrent._

import org.openqa.selenium._
import org.openqa.selenium.firefox._
import org.openqa.selenium.htmlunit._

import junit.framework.Assert._
import org.fluentlenium.core.filter.FilterConstructor.withText
import Env._
import org.fest.assertions.Assertions._

/*
 * https://github.com/FluentLenium/FluentLenium
 *
 * html-elementtien etsintä:
 * id:n perusteella: $("#myid")
 * tagin perusteella: $("a")
 * luokan perusteella: $(".myclass")
 * tekstin perusteella: $("a", withText("mytext"))
 *
 * Screenshottien otto debuggausta varten:
 * 1) selaimen vaihto (env.scala): browser = TestBrowser.of(Helpers.FIREFOX)
 * 2) browser.takeScreenShot("/tmp/pic.png")
 */

class CucumberSteps extends ScalaDsl with EN {
    Given("""^A new database$"""){ () =>
    }

    When("""^I go to the main page$"""){ () =>
        browser.goTo("http://localhost:3333/")
        assertEquals("MasaDB - Kaikki lähdeviitteet", browser.title())
    }

    And("""^I open the form for adding a new reference$"""){ () =>
        browser.$("#add").click()
        assertEquals("MasaDB - Uusi lähdeviite", browser.title())
    }

    And("""^I choose book as the reference type$"""){ () =>
        browser.$("#type").text("Kirja")
    }

    And("""^I enter reference information$"""){ () =>
        browser.$("#citeKey").text("bt99")
        browser.$("#title").text("The Title")
        browser.$("#author").text("Author Name")
        browser.$("#year").text("2099")
    }

    And("""^I submit the form"""){ () =>
        browser.$("#save").click() //Env.browser.submit("#save")
        assertEquals("MasaDB - Kaikki lähdeviitteet", browser.title())
    }

    And("""^I go to the reference list$"""){ () =>
        browser.goTo("http://localhost:3333/")
        assertEquals("MasaDB - Kaikki lähdeviitteet", browser.title())
    }

    Then("""^The list should contain the added reference$"""){ () =>
        assertFalse(browser.$("td", withText().contains("The Title")).isEmpty())
        assertFalse(browser.$("td", withText().contains("Author Name")).isEmpty())
    }

    Then("""^The list should have no entries$"""){ () =>
        assertFalse(browser.$("p", withText().contains(
                    "Haulla ei löytynyt yhtään viitettä.")).isEmpty())
    }

    And("""^I choose article as the reference type$"""){ () =>
        browser.$("#type").text("Artikkeli")
    }

    Given("""^A tech report reference has been added$"""){ () =>
        Database.save(new Reference(TechReport, "bt99", "The Title",
                                    "Author Name", 2099))
    }

    And("""^I open link to view the reference$"""){ () =>
        browser.$("a", withText().contains("The Title")).click()
        assertEquals("MasaDB - Viitteen tiedot", browser.title())
    }

    Then("""^The reference type should show tech report$"""){ () =>
        assertFalse(browser.$("#type", withText().contains("Tekninen raportti")).isEmpty())
    }

    Given("""^A proceedings reference has been added$"""){ () =>
        Database.save(new Reference(Proceedings, "bt99", "The Title",
                                    "Author Name", 2099))
    }

    Then("""^The page should contain BibTex code for the proceedings reference$"""){ () =>
        assertFalse(browser.$("#bibtex", withText().contains(
                    "@proceedings{bt99,\n"
                    + "    title = {The Title},\n"
                    + "    author = {Author Name},\n"
                    + "    year = {2099},\n"
                    + "}"
                )).isEmpty())
    }

    And("""^I download BibTex file$"""){ () =>
        browser.$("a", withText().contains("Lataa BibTex -tiedosto")).click()
    }

    Then("""^The browser starts downloading a BibTex file$"""){ () =>
        assertEquals(null, browser.title())
    }

    Given("""^Several entries added to database$"""){ () =>
        Database.save(new Reference(Article, "a", "title1", "test", 1))
        Database.save(new Reference(Book, "b", "title2", "FOO test", 1))
        Database.save(new Reference(InProceedings, "c", "title3", "test FOo", 1))
    }

    And("""^I enter author in search box$"""){ () =>
        browser.$("#searchbox").text("Foo")
    }

    And("""^I submit the search"""){ () =>
        browser.$("#searchsubmit").click()
        assertEquals("MasaDB - Kaikki lähdeviitteet", browser.title())
    }

    Then("""^List should contain entries with given author$"""){ () =>
        assertFalse(browser.$("td", withText().contains("title2")).isEmpty())
        assertFalse(browser.$("td", withText().contains("title3")).isEmpty())
    }

    Then("""^List should not contain entries without given author$"""){ () =>
        assertTrue(browser.$("td", withText().contains("title1")).isEmpty())
    }

    And("""^I open first reference to view$"""){ () =>
        browser.$("a", withText().contains("title1")).click()
        assertEquals("MasaDB - Viitteen tiedot", browser.title())
    }

    And("""^I clik modify button$"""){ () =>
        browser.$("a#edit").click()
    }

    And("""^I modify reference data$"""){ () =>
        browser.$("#year").text("2020")
        browser.$("#author").text("Author Name 2")
    }

    And("""^I submit the edit form"""){ () =>
        browser.$("#save").click() //Env.browser.submit("#save")
        assertEquals("MasaDB - Kaikki lähdeviitteet", browser.title())
    }

    Then("""^The page should show modified data in entry$"""){ () =>
        assertEquals("2020", browser.find("#year").getText())
        assertEquals("Author Name 2", browser.find("#author").getText())
    }

    And("""^I click delete button$"""){ () =>
        browser.$("a#delete").click()
    }

    Then("""^The list should not have deleted entry$"""){ () =>
        assertTrue(browser.$("td", withText().contains("title1")).isEmpty())
    }
}
