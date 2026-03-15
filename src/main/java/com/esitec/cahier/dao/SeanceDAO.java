package com.esitec.cahier.dao;

import com.esitec.cahier.model.Seance;
import com.esitec.cahier.service.NotificationService;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO séances — intègre les notifications automatiques.
 */
public class SeanceDAO {

    private final Connection conn;

    public SeanceDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    /** Créer une séance + notifier responsables et chef. */
    public boolean creer(Seance s) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO seances (cours_id, enseignant_id, date, heure, duree_minutes, contenu, observations) VALUES (?,?,?,?,?,?,?)"
            );
            ps.setInt(1, s.getCoursId()); ps.setInt(2, s.getEnseignantId());
            ps.setString(3, s.getDate().toString()); ps.setString(4, s.getHeure().toString());
            ps.setInt(5, s.getDureeMinutes()); ps.setString(6, s.getContenu());
            ps.setString(7, s.getObservations());
            ps.executeUpdate();

            // 🔔 Notifier
            try {
                // Récupérer infos du cours
                PreparedStatement psCours = conn.prepareStatement(
                    "SELECT c.intitule, c.classe_nom, u.nom, u.prenom " +
                    "FROM cours c JOIN utilisateurs u ON c.enseignant_id = u.id WHERE c.id = ?"
                );
                psCours.setInt(1, s.getCoursId());
                ResultSet rs = psCours.executeQuery();
                if (rs.next()) {
                    String cours   = rs.getString("intitule");
                    String classe  = rs.getString("classe_nom");
                    String ens     = rs.getString("prenom") + " " + rs.getString("nom");
                    NotificationService.getInstance().nouvelleSeance(cours, classe, ens, s.getDate().toString());

                    // Vérifier avancement >= 80% et notifier l'enseignant concerné
                    // Protection anti-doublon : on vérifie qu'aucune notif AVANCEMENT_ELEVE
                    // n'a déjà été envoyée pour ce cours (pour ne pas spammer à chaque séance)
                    CoursDAO coursDAO = new CoursDAO();
                    coursDAO.listerTous().stream()
                        .filter(c -> c.getId() == s.getCoursId() && c.getPourcentageAvancement() >= 80)
                        .findFirst()
                        .ifPresent(c -> {
                            NotificationDAO notifDAO = new NotificationDAO();
                            boolean dejaNotifie = notifDAO.existeNotifAvancement(c.getId());
                            if (!dejaNotifie) {
                                NotificationService.getInstance()
                                    .avancementEleve(c.getIntitule(), c.getClasseNom(),
                                        c.getPourcentageAvancement(), c.getEnseignantId());
                                notifDAO.marquerAvancementNotifie(c.getId());
                            }
                        });
                }
            } catch (Exception ignored) {}
            return true;
        } catch (SQLException e) {
            System.err.println("[ERREUR] Création séance : " + e.getMessage()); return false;
        }
    }

    public boolean modifier(Seance s) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE seances SET date=?, heure=?, duree_minutes=?, contenu=?, observations=? WHERE id=? AND statut='EN_ATTENTE'"
            );
            ps.setString(1, s.getDate().toString()); ps.setString(2, s.getHeure().toString());
            ps.setInt(3, s.getDureeMinutes()); ps.setString(4, s.getContenu());
            ps.setString(5, s.getObservations()); ps.setInt(6, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR] Modification séance : " + e.getMessage()); return false;
        }
    }

    public boolean supprimer(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM seances WHERE id=? AND statut='EN_ATTENTE'");
            ps.setInt(1, id); return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR] Suppression séance : " + e.getMessage()); return false;
        }
    }

    /** Valider une séance + notifier l'enseignant. */
    public boolean valider(int id) {
        boolean ok = changerStatut(id, Seance.Statut.VALIDE, null);
        if (ok) {
            try {
                ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT s.enseignant_id, s.date, s.cours_id, c.intitule, c.classe_nom FROM seances s " +
                    "JOIN cours c ON s.cours_id = c.id WHERE s.id = " + id
                );
                if (rs.next()) {
                    int enseignantId = rs.getInt("enseignant_id");
                    int coursId      = rs.getInt("cours_id");
                    String intitule  = rs.getString("intitule");
                    String classe    = rs.getString("classe_nom");
                    String date      = rs.getString("date");

                    // Notification séance validée
                    NotificationService.getInstance().seanceValidee(
                        enseignantId, intitule, classe, date
                    );

                    // Vérifier si avancement >= 80% après cette validation
                    CoursDAO coursDAO = new CoursDAO();
                    coursDAO.listerTous().stream()
                        .filter(c -> c.getId() == coursId && c.getPourcentageAvancement() >= 80)
                        .findFirst()
                        .ifPresent(c -> {
                            NotificationDAO notifDAO = new NotificationDAO();
                            if (!notifDAO.existeNotifAvancement(c.getId())) {
                                NotificationService.getInstance()
                                    .avancementEleve(c.getIntitule(), c.getClasseNom(),
                                        c.getPourcentageAvancement(), c.getEnseignantId());
                                notifDAO.marquerAvancementNotifie(c.getId());
                            }
                        });
                }
            } catch (Exception e) {
                System.err.println("[ERREUR] valider notification : " + e.getMessage());
            }
        }
        return ok;
    }

    /** Rejeter une séance + notifier l'enseignant avec le motif. */
    public boolean rejeter(int id, String commentaire) {
        boolean ok = changerStatut(id, Seance.Statut.REJETE, commentaire);
        if (ok) {
            try {
                ResultSet rs = conn.createStatement().executeQuery(
                    "SELECT s.enseignant_id, s.date, c.intitule, c.classe_nom FROM seances s " +
                    "JOIN cours c ON s.cours_id = c.id WHERE s.id = " + id
                );
                if (rs.next()) {
                    NotificationService.getInstance().seanceRejetee(
                        rs.getInt("enseignant_id"), rs.getString("intitule"),
                        rs.getString("classe_nom"), rs.getString("date"), commentaire
                    );
                }
            } catch (Exception ignored) {}
        }
        return ok;
    }

    private boolean changerStatut(int id, Seance.Statut statut, String commentaire) {
        try {
            PreparedStatement ps = conn.prepareStatement("UPDATE seances SET statut=?, commentaire_rejet=? WHERE id=?");
            ps.setString(1, statut.name()); ps.setString(2, commentaire); ps.setInt(3, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR] Changement statut : " + e.getMessage()); return false;
        }
    }

    public List<Seance> listerParEnseignant(int enseignantId) { return listerAvecFiltres("s.enseignant_id = " + enseignantId); }
    public List<Seance> listerParCours(int coursId)           { return listerAvecFiltres("s.cours_id = " + coursId); }
    public List<Seance> listerToutes()                        { return listerAvecFiltres(null); }
    public List<Seance> listerParClasse(String cl)            { return listerAvecFiltres("c.classe_nom = '" + cl.replace("'","''") + "'"); }
    public List<Seance> listerEnAttente(String cl)            { return listerAvecFiltres("c.classe_nom = '" + cl.replace("'","''") + "' AND s.statut = 'EN_ATTENTE'"); }

    private List<Seance> listerAvecFiltres(String filtre) {
        List<Seance> liste = new ArrayList<>();
        try {
            String sql = "SELECT s.*, c.intitule as cours_intitule, c.classe_nom, " +
                "u.nom as ens_nom, u.prenom as ens_prenom FROM seances s " +
                "JOIN cours c ON s.cours_id = c.id JOIN utilisateurs u ON s.enseignant_id = u.id " +
                (filtre != null ? "WHERE " + filtre : "") + " ORDER BY s.date DESC, s.heure DESC";
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                Seance s = new Seance();
                s.setId(rs.getInt("id")); s.setCoursId(rs.getInt("cours_id"));
                s.setCoursIntitule(rs.getString("cours_intitule")); s.setClasseNom(rs.getString("classe_nom"));
                s.setEnseignantId(rs.getInt("enseignant_id"));
                s.setNomEnseignant(rs.getString("ens_prenom") + " " + rs.getString("ens_nom"));
                s.setDate(LocalDate.parse(rs.getString("date"))); s.setHeure(LocalTime.parse(rs.getString("heure")));
                s.setDureeMinutes(rs.getInt("duree_minutes")); s.setContenu(rs.getString("contenu"));
                s.setObservations(rs.getString("observations"));
                s.setStatut(Seance.Statut.valueOf(rs.getString("statut")));
                s.setCommentaireRejet(rs.getString("commentaire_rejet"));
                liste.add(s);
            }
        } catch (SQLException e) { System.err.println("[ERREUR] Liste séances : " + e.getMessage()); }
        return liste;
    }
}

// Amelioration gestion seances - Nathanael
