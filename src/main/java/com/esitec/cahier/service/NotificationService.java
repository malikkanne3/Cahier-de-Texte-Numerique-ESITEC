package com.esitec.cahier.service;

import com.esitec.cahier.dao.NotificationDAO;
import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Notification;
import com.esitec.cahier.model.Utilisateur;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service de notifications — gère à la fois :
 * 1. Les notifications in-app (cloche dans la sidebar)
 * 2. Les emails via Brevo (reçus sur Gmail)
 *
 * Les emails sont envoyés en arrière-plan (thread séparé)
 * pour ne pas bloquer l'interface.
 */
public class NotificationService {

    private final NotificationDAO notifDAO  = new NotificationDAO();
    private final UtilisateurDAO  userDAO   = new UtilisateurDAO();
    private final EmailService    emailSvc  = EmailService.getInstance();
    // Thread pool pour envoi email en arrière-plan
    private final ExecutorService executor  = Executors.newCachedThreadPool();

    private static NotificationService instance;

    private NotificationService() {}

    public static NotificationService getInstance() {
        if (instance == null) instance = new NotificationService();
        return instance;
    }

    // ══════════════════════════════════════════
    //  ÉVÉNEMENT : Nouvelle séance ajoutée
    // ══════════════════════════════════════════
    public void nouvelleSeance(String coursIntitule, String classeNom,
                                String nomEnseignant, String dateSeance) {
        // Notifier tous les responsables de classe validés
        for (Utilisateur r : userDAO.listerParRole(Utilisateur.Role.RESPONSABLE_CLASSE)) {
            if (r.getStatut() != Utilisateur.Statut.VALIDE) continue;

            // Notification in-app
            notifDAO.creer(new Notification(r.getId(), Notification.Type.SEANCE_EN_ATTENTE,
                "Nouvelle séance à valider",
                nomEnseignant + " a soumis une séance — " + coursIntitule + " (" + classeNom + ")"));

            // Email en arrière-plan
            final Utilisateur dest = r;
            executor.submit(() -> emailSvc.envoyerEmail(
                dest.getEmail(), dest.getNomComplet(),
                "[ESITEC] ⏳ Nouvelle séance à valider — " + coursIntitule,
                emailSvc.templateNouvelleSeance(dest.getNomComplet(), nomEnseignant,
                    coursIntitule, classeNom, dateSeance)
            ));
        }

        // Notifier le chef de département
        for (Utilisateur c : userDAO.listerParRole(Utilisateur.Role.CHEF_DEPARTEMENT)) {
            notifDAO.creer(new Notification(c.getId(), Notification.Type.SEANCE_EN_ATTENTE,
                "Nouvelle séance enregistrée",
                nomEnseignant + " — " + coursIntitule));

            final Utilisateur dest = c;
            executor.submit(() -> emailSvc.envoyerEmail(
                dest.getEmail(), dest.getNomComplet(),
                "[ESITEC] 📋 Nouvelle séance — " + coursIntitule,
                emailSvc.templateNouvelleSeance(dest.getNomComplet(), nomEnseignant,
                    coursIntitule, classeNom, dateSeance)
            ));
        }
    }

    // ══════════════════════════════════════════
    //  ÉVÉNEMENT : Séance validée
    // ══════════════════════════════════════════
    public void seanceValidee(int enseignantId, String coursIntitule,
                               String classeNom, String dateSeance) {
        Utilisateur ens = userDAO.listerParRole(Utilisateur.Role.ENSEIGNANT).stream()
            .filter(u -> u.getId() == enseignantId).findFirst().orElse(null);
        if (ens == null) return;

        // Notification in-app
        notifDAO.creer(new Notification(enseignantId, Notification.Type.SEANCE_VALIDEE,
            "Séance validée ✓",
            "Votre séance du " + dateSeance + " pour \"" + coursIntitule + "\" a été validée."));

        // Email
        final Utilisateur dest = ens;
        executor.submit(() -> emailSvc.envoyerEmail(
            dest.getEmail(), dest.getNomComplet(),
            "[ESITEC] ✅ Séance validée — " + coursIntitule,
            emailSvc.templateSeanceValidee(dest.getNomComplet(), coursIntitule, classeNom, dateSeance)
        ));
    }

    // ══════════════════════════════════════════
    //  ÉVÉNEMENT : Séance rejetée
    // ══════════════════════════════════════════
    public void seanceRejetee(int enseignantId, String coursIntitule,
                               String classeNom, String dateSeance, String motif) {
        Utilisateur ens = userDAO.listerParRole(Utilisateur.Role.ENSEIGNANT).stream()
            .filter(u -> u.getId() == enseignantId).findFirst().orElse(null);
        if (ens == null) return;

        // Notification in-app
        notifDAO.creer(new Notification(enseignantId, Notification.Type.SEANCE_REJETEE,
            "Séance rejetée ✗",
            "Séance du " + dateSeance + " rejetée.\n📝 Motif : " + motif));

        // Email
        final Utilisateur dest = ens;
        executor.submit(() -> emailSvc.envoyerEmail(
            dest.getEmail(), dest.getNomComplet(),
            "[ESITEC] ❌ Séance rejetée — " + coursIntitule,
            emailSvc.templateSeanceRejetee(dest.getNomComplet(), coursIntitule,
                classeNom, dateSeance, motif)
        ));
    }

    // ══════════════════════════════════════════
    //  ÉVÉNEMENT : Nouveau compte
    // ══════════════════════════════════════════
    public void nouveauCompte(String nomComplet, String email, String role) {
        for (Utilisateur c : userDAO.listerParRole(Utilisateur.Role.CHEF_DEPARTEMENT)) {
            // Notification in-app
            notifDAO.creer(new Notification(c.getId(), Notification.Type.COMPTE_EN_ATTENTE,
                "Nouveau compte à valider",
                nomComplet + " (" + role + ") attend votre validation."));

            // Email
            final Utilisateur dest = c;
            executor.submit(() -> emailSvc.envoyerEmail(
                dest.getEmail(), dest.getNomComplet(),
                "[ESITEC] 👤 Nouveau compte à valider — " + nomComplet,
                emailSvc.templateNouveauCompte(dest.getNomComplet(), nomComplet, email, role)
            ));
        }
    }

    // ══════════════════════════════════════════
    //  ÉVÉNEMENT : Avancement > 80%
    // ══════════════════════════════════════════
    public void avancementEleve(String coursIntitule, String classeNom, double pct, int enseignantId) {
        String pctStr = String.format("%.0f%%", pct);
        String msg    = "\"" + coursIntitule + "\" (" + classeNom + ") atteint " + pctStr + " d'avancement.";

        // Enseignant concerné — notification in-app + email
        Utilisateur ens = userDAO.trouverParId(enseignantId);
        if (ens != null) {
            notifDAO.creer(new Notification(enseignantId, Notification.Type.AVANCEMENT_ELEVE,
                "Avancement de votre cours ≥ 80% 📊",
                "Votre cours \"" + coursIntitule + "\" (" + classeNom + ") a atteint " + pctStr + " d'avancement."));
            final Utilisateur destEns = ens;
            executor.submit(() -> emailSvc.envoyerEmail(
                destEns.getEmail(), destEns.getNomComplet(),
                "[ESITEC] 📊 Votre cours dépasse 80% — " + coursIntitule,
                emailSvc.templateAvancementEleve(destEns.getNomComplet(), coursIntitule, classeNom, pctStr)
            ));
        }

        // Chef de département
        for (Utilisateur c : userDAO.listerParRole(Utilisateur.Role.CHEF_DEPARTEMENT)) {
            notifDAO.creer(new Notification(c.getId(), Notification.Type.AVANCEMENT_ELEVE,
                "Programme presque terminé 📊", msg));
            final Utilisateur dest = c;
            executor.submit(() -> emailSvc.envoyerEmail(
                dest.getEmail(), dest.getNomComplet(),
                "[ESITEC] 📊 Programme presque terminé — " + coursIntitule,
                emailSvc.templateAvancementEleve(dest.getNomComplet(), coursIntitule, classeNom, pctStr)
            ));
        }

        // Responsables validés
        for (Utilisateur r : userDAO.listerParRole(Utilisateur.Role.RESPONSABLE_CLASSE)) {
            if (r.getStatut() != Utilisateur.Statut.VALIDE) continue;
            notifDAO.creer(new Notification(r.getId(), Notification.Type.AVANCEMENT_ELEVE,
                "Programme presque terminé 📊", msg));
            final Utilisateur dest = r;
            executor.submit(() -> emailSvc.envoyerEmail(
                dest.getEmail(), dest.getNomComplet(),
                "[ESITEC] 📊 Programme presque terminé — " + coursIntitule,
                emailSvc.templateAvancementEleve(dest.getNomComplet(), coursIntitule, classeNom, pctStr)
            ));
        }
    }

    // Ancienne signature conservée pour compatibilité (sans enseignantId)
    public void avancementEleve(String coursIntitule, String classeNom, double pct) {
        avancementEleve(coursIntitule, classeNom, pct, -1);
    }

    // Méthodes compatibilité ancienne signature
    public void nouvelleSeance(String coursIntitule, String classeNom, String nomEnseignant) {
        nouvelleSeance(coursIntitule, classeNom, nomEnseignant, "—");
    }
    public void seanceValidee(int enseignantId, String coursIntitule, String dateSeance) {
        seanceValidee(enseignantId, coursIntitule, "—", dateSeance);
    }
    public void seanceRejetee(int enseignantId, String coursIntitule, String dateSeance, String motif) {
        seanceRejetee(enseignantId, coursIntitule, "—", dateSeance, motif);
    }
    public void nouveauCompte(String nomComplet, String role) {
        nouveauCompte(nomComplet, "—", role);
    }

    public NotificationDAO getDAO() { return notifDAO; }
}
