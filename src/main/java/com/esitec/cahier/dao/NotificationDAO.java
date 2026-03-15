package com.esitec.cahier.dao;

import com.esitec.cahier.model.Notification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des notifications.
 */
public class NotificationDAO {

    private final Connection conn;

    public NotificationDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
        creerTableSiAbsente();
    }

    private void creerTableSiAbsente() {
        try {
            conn.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS notifications (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    destinataire_id INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    titre TEXT NOT NULL,
                    message TEXT NOT NULL,
                    lue INTEGER NOT NULL DEFAULT 0,
                    date_creation TEXT NOT NULL,
                    FOREIGN KEY (destinataire_id) REFERENCES utilisateurs(id)
                )
            """);
        } catch (SQLException e) {
            System.err.println("[ERREUR] Création table notifications : " + e.getMessage());
        }
    }

    /** Créer une nouvelle notification. */
    public boolean creer(Notification n) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO notifications (destinataire_id, type, titre, message, lue, date_creation) VALUES (?,?,?,?,0,?)"
            );
            ps.setInt(1, n.getDestinataireId());
            ps.setString(2, n.getType().name());
            ps.setString(3, n.getTitre());
            ps.setString(4, n.getMessage());
            ps.setString(5, LocalDateTime.now().toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("[ERREUR] Création notification : " + e.getMessage());
            return false;
        }
    }

    /** Lister toutes les notifications d'un utilisateur (non lues en premier). */
    public List<Notification> listerParUtilisateur(int utilisateurId) {
        List<Notification> liste = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM notifications WHERE destinataire_id = ? ORDER BY lue ASC, date_creation DESC"
            );
            ps.setInt(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(map(rs));
        } catch (SQLException e) {
            System.err.println("[ERREUR] Liste notifications : " + e.getMessage());
        }
        return liste;
    }

    /** Compter les notifications non lues. */
    public int compterNonLues(int utilisateurId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM notifications WHERE destinataire_id = ? AND lue = 0"
            );
            ps.setInt(1, utilisateurId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("[ERREUR] Comptage notifications : " + e.getMessage());
        }
        return 0;
    }

    /** Marquer une notification comme lue. */
    public void marquerToutesLues(int utilisateurId) {
        try {
            java.sql.PreparedStatement ps = conn.prepareStatement(
                "UPDATE notifications SET lue = 1 WHERE destinataire_id = ?");
            ps.setInt(1, utilisateurId);
            ps.executeUpdate(); ps.close();
        } catch (java.sql.SQLException e) { e.printStackTrace(); }
    }

    public void marquerLue(int id) {
        try {
            conn.createStatement().execute("UPDATE notifications SET lue = 1 WHERE id = " + id);
        } catch (SQLException e) {
            System.err.println("[ERREUR] Marquer lue : " + e.getMessage());
        }
    }

    /** Marquer toutes les notifications d'un utilisateur comme lues. */
    public void toutMarquerLu(int utilisateurId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE notifications SET lue = 1 WHERE destinataire_id = ?"
            );
            ps.setInt(1, utilisateurId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERREUR] Tout marquer lu : " + e.getMessage());
        }
    }

    /** Supprimer toutes les notifications d'un utilisateur. */
    public void toutSupprimer(int utilisateurId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM notifications WHERE destinataire_id = ?"
            );
            ps.setInt(1, utilisateurId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERREUR] Suppression notifications : " + e.getMessage());
        }
    }

    /**
     * Vérifie si une notification d'avancement a déjà été envoyée pour ce cours.
     * Utilise la table cours_avancement_notifie pour éviter les doublons.
     */
    public boolean existeNotifAvancement(int coursId) {
        try {
            // Créer la table si elle n'existe pas
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS cours_avancement_notifie (" +
                "cours_id INTEGER PRIMARY KEY, notifie_le TEXT)"
            );
            PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM cours_avancement_notifie WHERE cours_id = ?"
            );
            ps.setInt(1, coursId);
            ResultSet rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR] existeNotifAvancement : " + e.getMessage());
            return false;
        }
    }

    /**
     * Enregistre qu'une notification d'avancement a été envoyée pour ce cours.
     */
    public void marquerAvancementNotifie(int coursId) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT OR REPLACE INTO cours_avancement_notifie (cours_id, notifie_le) VALUES (?, datetime('now'))"
            );
            ps.setInt(1, coursId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERREUR] marquerAvancementNotifie : " + e.getMessage());
        }
    }

    private Notification map(ResultSet rs) throws SQLException {
        Notification n = new Notification();
        n.setId(rs.getInt("id"));
        n.setDestinataireId(rs.getInt("destinataire_id"));
        try { n.setType(Notification.Type.valueOf(rs.getString("type"))); }
        catch (Exception e) { n.setType(Notification.Type.INFO); }
        n.setTitre(rs.getString("titre"));
        n.setMessage(rs.getString("message"));
        n.setLue(rs.getInt("lue") == 1);
        try { n.setDateCreation(LocalDateTime.parse(rs.getString("date_creation"))); }
        catch (Exception ignored) {}
        return n;
    }
}
