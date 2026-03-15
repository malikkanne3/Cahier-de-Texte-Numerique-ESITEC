package com.esitec.cahier;

import com.esitec.cahier.dao.DatabaseManager;
import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.dao.CoursDAO;
import com.esitec.cahier.dao.SeanceDAO;
import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Seance;
import com.esitec.cahier.model.Utilisateur;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests unitaires pour l'application Cahier de Texte.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CahierDeTexteTest {

    static UtilisateurDAO utilisateurDAO;
    static CoursDAO coursDAO;
    static SeanceDAO seanceDAO;

    @BeforeAll
    static void setUp() {
        // Utiliser une base de données en mémoire pour les tests
        utilisateurDAO = new UtilisateurDAO();
        coursDAO = new CoursDAO();
        seanceDAO = new SeanceDAO();
    }

    @Test
    @Order(1)
    @DisplayName("Test authentification admin valide")
    void testAuthentificationAdminValide() {
        Utilisateur u = utilisateurDAO.authentifier("admin@esitec.sn", "admin123");
        assertNotNull(u, "L'authentification admin devrait réussir");
        assertEquals(Utilisateur.Role.CHEF_DEPARTEMENT, u.getRole());
        assertEquals(Utilisateur.Statut.VALIDE, u.getStatut());
    }

    @Test
    @Order(2)
    @DisplayName("Test authentification mot de passe incorrect")
    void testAuthentificationEchoue() {
        Utilisateur u = utilisateurDAO.authentifier("admin@esitec.sn", "mauvaismdp");
        assertNull(u, "L'authentification devrait échouer avec un mauvais mot de passe");
    }

    @Test
    @Order(3)
    @DisplayName("Test création utilisateur")
    void testCreationUtilisateur() {
        Utilisateur u = new Utilisateur("TEST", "Prénom", "test.junit@esitec.sn", "pass123", Utilisateur.Role.ENSEIGNANT);
        u.setStatut(Utilisateur.Statut.VALIDE);
        boolean resultat = utilisateurDAO.creer(u);
        assertTrue(resultat, "La création d'utilisateur devrait réussir");
    }

    @Test
    @Order(4)
    @DisplayName("Test liste des enseignants")
    void testListerEnseignants() {
        List<Utilisateur> enseignants = utilisateurDAO.listerParRole(Utilisateur.Role.ENSEIGNANT);
        assertFalse(enseignants.isEmpty(), "La liste des enseignants ne devrait pas être vide");
    }

    @Test
    @Order(5)
    @DisplayName("Test création et récupération d'un cours")
    void testCreationCours() {
        List<Utilisateur> enseignants = utilisateurDAO.listerParRole(Utilisateur.Role.ENSEIGNANT);
        assertFalse(enseignants.isEmpty());

        int enseignantId = enseignants.get(0).getId();
        Cours cours = new Cours("Test POO", "Cours de test", enseignantId, "L3 Test", 30);
        boolean resultat = coursDAO.creer(cours);
        assertTrue(resultat, "La création de cours devrait réussir");

        List<Cours> coursDeLEnseignant = coursDAO.listerParEnseignant(enseignantId);
        assertFalse(coursDeLEnseignant.isEmpty(), "L'enseignant devrait avoir au moins un cours");
    }

    @Test
    @Order(6)
    @DisplayName("Test ajout d'une séance")
    void testAjoutSeance() {
        List<Cours> cours = coursDAO.listerTous();
        assertFalse(cours.isEmpty());

        int coursId = cours.get(0).getId();
        int enseignantId = cours.get(0).getEnseignantId();

        Seance s = new Seance(coursId, enseignantId, LocalDate.now(), LocalTime.of(9, 0),
            90, "Contenu de test", "Observations de test");
        boolean resultat = seanceDAO.creer(s);
        assertTrue(resultat, "L'ajout de séance devrait réussir");
    }

    @Test
    @Order(7)
    @DisplayName("Test validation d'une séance")
    void testValidationSeance() {
        List<Cours> cours = coursDAO.listerTous();
        if (cours.isEmpty()) return;

        List<Seance> seances = seanceDAO.listerParCours(cours.get(0).getId());
        if (seances.isEmpty()) return;

        int id = seances.get(0).getId();
        boolean resultat = seanceDAO.valider(id);
        assertTrue(resultat, "La validation d'une séance devrait réussir");
    }

    @Test
    @Order(8)
    @DisplayName("Test rejet d'une séance avec commentaire")
    void testRejetSeance() {
        List<Cours> cours = coursDAO.listerTous();
        if (cours.isEmpty()) return;

        // Créer une séance pour tester le rejet
        int coursId = cours.get(0).getId();
        int enseignantId = cours.get(0).getEnseignantId();
        Seance s = new Seance(coursId, enseignantId, LocalDate.now().minusDays(1),
            LocalTime.of(10, 0), 60, "Séance à rejeter", null);
        seanceDAO.creer(s);

        List<Seance> seances = seanceDAO.listerParCours(coursId);
        Seance enAttente = seances.stream()
            .filter(seq -> seq.getStatut() == Seance.Statut.EN_ATTENTE)
            .findFirst().orElse(null);

        if (enAttente != null) {
            boolean resultat = seanceDAO.rejeter(enAttente.getId(), "Contenu incomplet");
            assertTrue(resultat, "Le rejet d'une séance devrait réussir");
        }
    }

    @Test
    @Order(9)
    @DisplayName("Test calcul du pourcentage d'avancement")
    void testPourcentageAvancement() {
        List<Cours> cours = coursDAO.listerTous();
        if (cours.isEmpty()) return;

        Cours c = cours.get(0);
        double pct = c.getPourcentageAvancement();
        assertTrue(pct >= 0 && pct <= 100, "Le pourcentage d'avancement doit être entre 0 et 100");
    }

    @Test
    @Order(10)
    @DisplayName("Test liste des classes")
    void testListeClasses() {
        List<String> classes = coursDAO.listerClasses();
        assertNotNull(classes, "La liste des classes ne devrait pas être null");
    }
}
