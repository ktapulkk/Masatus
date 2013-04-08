
import db.Database;
import java.util.List;
import models.Reference;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import play.test.FakeApplication;
import static play.test.Helpers.*;

public class DatabaseTest {

    FakeApplication fa;
    Reference ref1, ref2;

    @Test
    public void emptyDatabaseHasNoEntries() {
        assertEquals(0, Database.findAll().size());
    }

    @Before
    public void setUp() {
        fa = fakeApplication(inMemoryDatabase());
        start(fa);

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

    @After
    public void tearDown() {
        stop(fa);
    }

    @Test
    public void idIsAutogenerated() {
        Database.save(ref1);
        assertNotNull(ref1.getId());
        Database.save(ref2);
        assertNotNull(ref2.getId());
        assertTrue(!ref1.getId().equals(ref2.getId()));
    }

    @Test
    public void savedEntryHasCorrectAttributes() {
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

    @Test
    public void addingTwoEntriesWorks() {
        Database.save(ref1);
        Database.save(ref2);
        List<Reference> all = Database.findAll();
        assertEquals(2, all.size());
    }

    @Test
    public void deleteRemovesAnEntry() {
        Database.save(ref1);
        Database.save(ref2);
        assertTrue(Database.delete(ref1.getId()));
        List<Reference> all = Database.findAll();
        assertEquals(1, all.size());
        assertEquals(ref2.getId(), all.get(0).getId());
    }
}
