
import controllers.Database;
import java.util.List;
import models.Reference;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import play.test.Helpers;

public class DatabaseTest {

    Reference ref1, ref2;

    @Test
    public void emptyDatabaseHasNoEntries() {
        Helpers.running(Helpers.fakeApplication(Helpers.inMemoryDatabase()), new Runnable() {
            public void run() {
                assertEquals(0, Database.findAll().size());
            }
        });
    }

    @Before
    public void setUp() {
        ref1 = new Reference("M12", "The Title", "Masa",
                "Masa Publishing", 2012);
        ref1.address = "Masala";
        ref1.edition = "Second";
        ref1.volume = 7;

        ref2 = new Reference("M13", "The Title II", "Masa II",
                "Masa Publishing", 2013);
        ref2.address = "Masala";
        ref2.edition = null;
        ref2.volume = 7;
    }

    @Test
    public void idIsAutogenerated() {
        Helpers.running(Helpers.fakeApplication(Helpers.inMemoryDatabase()), new Runnable() {
            public void run() {
                Database.save(ref1);
                assertEquals(1, ref1.getId());
                Database.save(ref2);
                assertEquals(2, ref2.getId());
            }
        });
    }

    @Test
    public void savedEntryHasCorrectAttributes() {
        Helpers.running(Helpers.fakeApplication(Helpers.inMemoryDatabase()), new Runnable() {
            public void run() {
                Database.save(ref1);

                List<Reference> all = Database.findAll();
                assertEquals(1, all.size());
                assertEquals(ref1.citeKey, all.get(0).citeKey);
                assertEquals(ref1.title, all.get(0).title);
                assertEquals(ref1.author, all.get(0).author);
                assertEquals(ref1.publisher, all.get(0).publisher);
                assertEquals(ref1.year, all.get(0).year);
                assertEquals(ref1.address, all.get(0).address);
                assertEquals(ref1.edition, all.get(0).edition);
                assertEquals(ref1.volume, all.get(0).volume);
            }
        });
    }

    @Test
    public void addingTwoEntriesWorks() {
        Helpers.running(Helpers.fakeApplication(Helpers.inMemoryDatabase()), new Runnable() {
            public void run() {
                Database.save(ref1);
                Database.save(ref2);
                List<Reference> all = Database.findAll();
                assertEquals(2, all.size());
            }
        });
    }

    @Test
    public void deleteRemovesAnEntry() {
        Helpers.running(Helpers.fakeApplication(Helpers.inMemoryDatabase()), new Runnable() {
            public void run() {
                Database.save(ref1);
                Database.save(ref2);
                assertTrue(Database.delete(ref1.getId()));
                List<Reference> all = Database.findAll();
                assertEquals(1, all.size());
                assertEquals(ref2.getId(), all.get(0).getId());
            }
        });
    }
}