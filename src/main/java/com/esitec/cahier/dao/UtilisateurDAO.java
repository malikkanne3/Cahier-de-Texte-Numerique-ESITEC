package com.esitec.cahier.dao;

import com.esitec.cahier.model.Utilisateur;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des utilisateurs.
 */
public class UtilisateurDAO {

    private Connection conn;

    public UtilisateurDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    /**
     * Authentifie un utilisateur par email et mot de passe.
     */
    public Utilisateur authentifier(String email, String motDePasse) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM utilisateurs WHERE email = ? AND statut = 'VALIDE'"
            );
            ps.setString(1, email);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String hashStocke = rs.getString("mot_de_passe");
                boolean motDePasseValide;
                try {
                    motDePasseValide = BCrypt.checkpw(motDePasse, hashStocke);
                } catch (Exception e) {
                    // Fallback comparaison directe (mode sans hash)
                    motDePasseValide = motDePasse.equals(hashStocke);
                }
                if (motDePasseValide) {
                    return mapResultSet(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Crée un nouveau compte utilisateur (statut EN_ATTENTE).
     */
    public boolean creer(Utilisateur u) {
        try {
            String hash;
            try {
                hash = BCrypt.hashpw(u.getMotDePasse(), BCrypt.gensalt());
            } catch (Exception e) {
                hash = u.getMotDePasse();
            }
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, statut) VALUES (?,?,?,?,?,?)"
            );
            ps.setString(1, u.getNom());
            ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail());
            ps.setString(4, hash);
            ps.setString(5, u.getRole().name());
            ps.setString(6, u.getStatut().name());
            ps.executeUpdate();
            // 🔔 Notifier le chef de département d'un nouveau compte
            try {
                com.esitec.cahier.service.NotificationService.getInstance()
                    .nouveauCompte(u.getNomComplet(), u.getEmail(), u.getRole().name());
            } catch (Exception ignored) {}
            return true;
        } catch (SQLException e) {
            System.err.println("[ERREUR] Création utilisateur : " + e.getMessage());
            return false;
        }
    }

    /**
     * Retourne tous les utilisateurs.
     */
    public List<Utilisateur> listerTous() {
        List<Utilisateur> liste = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT * FROM utilisateurs ORDER BY nom, prenom"
            );
            while (rs.next()) liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    /**
     * Retourne les utilisateurs par rôle.
     */
    public List<Utilisateur> listerParRole(Utilisateur.Role role) {
        List<Utilisateur> liste = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(
                "SELECT * FROM utilisateurs WHERE role = ? ORDER BY nom, prenom"
            );
            ps.setString(1, role.name());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) liste.add(mapResultSet(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    /**
     * Valide ou rejette un compte utilisateur.
     */
    public boolean changerStatut(int id, Utilisateur.Statut statut) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE utilisateurs SET statut = ? WHERE id = ?"
            );
            ps.setString(1, statut.name());
            ps.setInt(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Supprime un utilisateur.
     */
    /**
     * Modifier les informations d'un utilisateur existant.
     * Si motDePasse est non vide, il est hashé et mis à jour.
     */
    /**
     * Créer un utilisateur directement avec le statut fourni (utilisé par le chef).
     * Pas de notification envoyée.
     */
    public boolean creerDirectement(Utilisateur u) {
        try {
            String hash;
            try { hash = BCrypt.hashpw(u.getMotDePasse(), BCrypt.gensalt()); }
            catch (Exception e) { hash = u.getMotDePasse(); }
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, statut) VALUES (?,?,?,?,?,?)"
            );
            ps.setString(1, u.getNom()); ps.setString(2, u.getPrenom());
            ps.setString(3, u.getEmail()); ps.setString(4, hash);
            ps.setString(5, u.getRole().name()); ps.setString(6, u.getStatut().name());
            ps.executeUpdate(); ps.close();
            return true;
        } catch (SQLException e) {
            System.err.println("[ERREUR] creerDirectement : " + e.getMessage());
            return false;
        }
    }

        public boolean modifier(Utilisateur u) {
        try {
            if (u.getMotDePasse() != null && !u.getMotDePasse().isEmpty()
                    && !u.getMotDePasse().startsWith("$2a$")) {
                // Nouveau mot de passe à hasher
                String hash;
                try { hash = BCrypt.hashpw(u.getMotDePasse(), BCrypt.gensalt()); }
                catch (Exception e) { hash = u.getMotDePasse(); }
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE utilisateurs SET nom=?, prenom=?, email=?, mot_de_passe=?, role=? WHERE id=?"
                );
                ps.setString(1, u.getNom()); ps.setString(2, u.getPrenom());
                ps.setString(3, u.getEmail()); ps.setString(4, hash);
                ps.setString(5, u.getRole().name()); ps.setInt(6, u.getId());
                return ps.executeUpdate() > 0;
            } else {
                // Pas de changement de mot de passe
                PreparedStatement ps = conn.prepareStatement(
                    "UPDATE utilisateurs SET nom=?, prenom=?, email=?, role=? WHERE id=?"
                );
                ps.setString(1, u.getNom()); ps.setString(2, u.getPrenom());
                ps.setString(3, u.getEmail()); ps.setString(4, u.getRole().name());
                ps.setInt(5, u.getId());
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            System.err.println("[ERREUR] Modification utilisateur : " + e.getMessage());
            return false;
        }
    }

    public boolean supprimer(int id) {
        try {
            // Supprimer d'abord les séances liées à cet utilisateur
            PreparedStatement ps1 = conn.prepareStatement(
                "DELETE FROM seances WHERE enseignant_id = ? OR cours_id IN (SELECT id FROM cours WHERE enseignant_id = ?)"
            );
            ps1.setInt(1, id); ps1.setInt(2, id); ps1.executeUpdate(); ps1.close();

            // Supprimer les cours liés
            PreparedStatement ps2 = conn.prepareStatement("DELETE FROM cours WHERE enseignant_id = ?");
            ps2.setInt(1, id); ps2.executeUpdate(); ps2.close();

            // Supprimer les notifications liées
            PreparedStatement ps3 = conn.prepareStatement("DELETE FROM notifications WHERE destinataire_id = ?");
            ps3.setInt(1, id); ps3.executeUpdate(); ps3.close();

            // Supprimer l'utilisateur
            PreparedStatement ps4 = conn.prepareStatement("DELETE FROM utilisateurs WHERE id = ?");
            ps4.setInt(1, id);
            return ps4.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERREUR] Suppression utilisateur id=" + id + " : " + e.getMessage());
            return false;
        }
    }

    /** Trouver un utilisateur par son ID (pour les notifications email). */
    public Utilisateur trouverParId(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM utilisateurs WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapResultSet(rs);
        } catch (SQLException e) {
            System.err.println("[ERREUR] trouverParId : " + e.getMessage());
        }
        return null;
    }

    private Utilisateur mapResultSet(ResultSet rs) throws SQLException {
        Utilisateur u = new Utilisateur();
        u.setId(rs.getInt("id"));
        u.setNom(rs.getString("nom"));
        u.setPrenom(rs.getString("prenom"));
        u.setEmail(rs.getString("email"));
        u.setMotDePasse(rs.getString("mot_de_passe"));
        u.setRole(Utilisateur.Role.valueOf(rs.getString("role")));
        u.setStatut(Utilisateur.Statut.valueOf(rs.getString("statut")));
        return u;
    }
}
