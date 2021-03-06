package controllers;

import db.Database;
import models.Reference;
import models.ReferenceType;
import play.data.Form;
import static play.data.Form.*;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.add;
import views.html.edit;
import java.util.ArrayList;
import java.util.List;


import java.util.*;
import javax.persistence.*;

import play.db.ebean.*;
import play.data.format.*;
import play.data.validation.*;

import com.avaje.ebean.*;


/**
 * Sovelluslogiikka viittausten lisäyssivua varten.
 */
public class ModifyReference extends Controller {

    /**
     * Generoi sivun sisällön.
     *
     * @return Sivun sisältö.
     */
    public static Result show() {
          return ok(add.render(form(Reference.class)));
    }

    /**
     * Generoi sivun sisällön ja täyttää kentät
     *
     * @return Sivun sisältö.
     */
    public static Result edit(Integer id) {
        //Integer id = Integer.parseInt(request().getQueryString("id"));
        Form<Reference> referenceForm = form(Reference.class).fill(
            Reference.find.byId(id)
        );
        return ok(edit.render(id, referenceForm));
    }

    public static Result update(Integer id) {
        Form<Reference> referenceForm = form(Reference.class).bindFromRequest();

        // Validoidaan kentät.
        if (formHasErrors(referenceForm)) {
            return badRequest(edit.render(id, referenceForm));
        }

        // Haetaan tietue
        Reference reference = Reference.find.byId(id);
        Reference ref = referenceForm .get();

        // Muutetaan arvot
        reference.setType(ref.type);
        reference.setAuthor(ref.author);
        reference.setTitle(ref.title);
        reference.setYear(ref.year);
        reference.setMonth(ref.month);
        reference.setVolume(ref.volume);
        reference.setNumber(ref.number);
        reference.setEdition(ref.edition);
        reference.setPages(ref.pages);
        reference.setBookTitle(ref.bookTitle);
        reference.setPublisher(ref.publisher);
        reference.setAddress(ref.address);
        reference.setOrganization(ref.organization);

        // Tallennetaan
        Database.save(reference);

        return redirect(routes.ReferenceList.show());
    }


    /**
     * Tarkistaa onko muokkaa lomakkeessa virheellisiä kenttiä.
     *
     * @param form lomakkeen tiedot
     * @return true jos lomakkeessa on virheitä
     */
    private static boolean formHasErrors(Form<Reference> form) {
        if (form.hasErrors()) {
            return true;
        }

        if (!form.get().month.isEmpty() && !form.get().month.matches(
                "(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)")) {
            form.reject("month", "Anna kuukausi muodossa \"jan\", \"feb\", jne.");
            return true;
        }

        if (!form.get().pages.isEmpty() && !form.get().pages.matches(
                "[1-9][0-9]*--[1-9][0-9]*")) {
            form.reject("pages", "Anna sivut muodossa 123--321.");
            return true;
        }

        return false;
    }


    /**
     * Tallentaa lomakkeen tiedot. Jos lomakkeessa on virhe, näytetään se
     * uudestaan virheilmoitusten kanssa, muuten tallennetaan tiedot
     * tietokantaan ja siirrytään takaisin aloitussivulle.
     *
     * @return Sivun sisältö.
     */
    public static Result save() {
        Form<Reference> form = form(Reference.class).bindFromRequest();
        if (formHasErrors(form)) {
            return badRequest(add.render(form));
        } else {
            Reference input = form.get();

            Reference ref = new Reference(input.type, generoiViite(input.author, input.year),
                    input.title, input.author, input.year);
            ref.setMonth(input.month);
            ref.setVolume(input.volume);
            ref.setNumber(input.number);
            ref.setEdition(input.edition);
            ref.setPages(input.pages);
            ref.setBookTitle(input.bookTitle);
            ref.setPublisher(input.publisher);
            ref.setAddress(input.address);
            ref.setOrganization(input.organization);

            Database.save(ref);

            return redirect(routes.ReferenceList.show());
        }
    }

    /**
     * Generoi viiteavaimen sukunimien ja vuosiluvun perusteella.
     *
     * @return viiteavain.
     */
    public static String generoiViite(String authorString, int year)
    {
        String viite = "";
        String separator = " and ";
        ArrayList<String> authorList = new ArrayList<String>();

        // Erotellaan nimet listaan, jos enemman kuin yksi tekija.
        while (authorString.contains(separator)) {
            authorList.add(authorString.substring(0, authorString.indexOf(separator)));
            authorString = authorString.substring(authorString.indexOf(separator)+separator.length());
        }

        // Lisataan viimeinen/ainoa tekija listaan.
        authorList.add(authorString);

        // Lisataan viitteeseen jokaisen sukunimen ensimmainen kirjain.
        for (int i = 0; i < authorList.size(); i++) {
            String nimi = authorList.get(i);

            // Sukunimi, Etunimi ("Kekkonen, Urho Kaleva")
            if (nimi.contains(","))	{
                // Useampiosaiset sukunimet ("van Gogh, Vincent")
                if (nimi.substring(0, nimi.indexOf(",")).contains(" ")) {
                    char merkki = Character.toLowerCase(nimi.charAt(0));
                    merkki = fixCharacter(merkki);
                    viite += merkki;

                    merkki = nimi.charAt(nimi.indexOf(" ")+1);
                    merkki = fixCharacter(merkki);
                    viite += merkki;
                }
                // Yksiosaiset sukunimet ("Kekkonen, Urho Kaleva")
                else
                {
                    char merkki = nimi.charAt(0);
                    merkki = fixCharacter(merkki);
                    viite += merkki;
                }
            }
            // Etunimi Sukunimi ("Urho Kekkonen")
            else
            {
                char merkki = nimi.charAt(nimi.lastIndexOf(" ")+1);
                merkki = fixCharacter(merkki);
                viite += merkki;
            }
        }

        // Vain 4 ensimmaista kirjainta mahtuu viitteeseen.
        if (viite.length() > 4)
            viite = viite.substring(0,4);

        // Vuosiluvun kaksi viimeista numeroa viitteen loppuun.
		String yearString = Integer.toString(year);
		if (yearString.length() < 2)
			viite += yearString;
		else
			viite += yearString.substring(yearString.length()-2, yearString.length());

        return makeUniqueCiteKey(viite);
    }

    /**
     * Muuttaa skandin tarvittaessa läheiseen vastineeseensa
     *
     * @param c Tarkistettava merkki
     * @return Yksinkertaistettu merkki tai syötetty merkki
     */
    private static char fixCharacter(char c) {
        switch (c) {
            case 'å': return 'a';
            case 'ä': return 'a';
            case 'ö': return 'o';
            case 'Å': return 'A';
            case 'Ä': return 'A';
            case 'Ö': return 'O';
            default: return c;
        }
    }

    /**
     * Tarkistaa tietokannasta onko samalla tavalla alkavia viiteavaimia jo olemassa.
     * Tarvittaessa generoi erottelukirjaimia.
     *
     * @param citeKey Sitaattiavain
     * @return Uniikki sitaattiavain
     */
    private static String makeUniqueCiteKey(String citeKey) {
        List<Reference> refs = Database.findAll();
        int count = 0;

        for (Reference ref : refs)
            if (ref.getCiteKey().startsWith(citeKey))
                count++;

        return citeKey + createEnding(count);
    }

    /**
     * Luo halutun sitaattiavainerottimen.
     *
     * @param count Monennes kirjainyhdistelmä
     * @return Annettua lukua vastaava kirjainyhdistelmä
     */
    private static String createEnding(int count) {
        if (count == 0)
            return "";

        return createEnding((count - 1) / 26) + (char)('a' + (count - 1) % 26);
    }
}

