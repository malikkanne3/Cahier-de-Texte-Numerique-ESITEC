package com.esitec.cahier.dao;

import com.esitec.cahier.model.Cours;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO pour la gestion des cours.
 */
public class CoursDAO {

    private Connection conn;

    public CoursDAO() {
        this.conn = DatabaseManager.getInstance().getConnection();
    }

    public boolean creer(Cours c) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO cours (intitule, description, enseignant_id, classe_nom, volume_horaire_prevu) VALUES (?,?,?,?,?)"
            );
            ps.setString(1, c.getIntitule());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getEnseignantId());
            ps.setString(4, c.getClasseNom());
            ps.setInt(5, c.getVolumeHorairePrevu());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean modifier(Cours c) {
        try {
            PreparedStatement ps = conn.prepareStatement(
                "UPDATE cours SET intitule=?, description=?, enseignant_id=?, classe_nom=?, volume_horaire_prevu=? WHERE id=?"
            );
            ps.setString(1, c.getIntitule());
            ps.setString(2, c.getDescription());
            ps.setInt(3, c.getEnseignantId());
            ps.setString(4, c.getClasseNom());
            ps.setInt(5, c.getVolumeHorairePrevu());
            ps.setInt(6, c.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean supprimer(int id) {
        try {
            PreparedStatement ps = conn.prepareStatement("DELETE FROM cours WHERE id=?");
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Cours> listerTous() {
        return listerAvecFiltres(null, null);
    }

    public List<Cours> listerParEnseignant(int enseignantId) {
        return listerAvecFiltres(enseignantId, null);
    }

    public List<Cours> listerParClasse(String classeNom) {
        return listerAvecFiltres(null, classeNom);
    }

    private List<Cours> listerAvecFiltres(Integer enseignantId, String classeNom) {
        List<Cours> liste = new ArrayList<>();
        try {
            StringBuilder sql = new StringBuilder("""
                SELECT c.*, u.nom, u.prenom,
                       COALESCE(SUM(CASE WHEN s.statut='VALIDE' THEN s.duree_minutes ELSE 0 END)/60, 0) as heures_effectuees
                FROM cours c
                LEFT JOIN utilisateurs u ON c.enseignant_id = u.id
                LEFT JOIN seances s ON s.cours_id = c.id
            """);
            if (enseignantId != null) sql.append(" WHERE c.enseignant_id = ").append(enseignantId);
            if (classeNom != null) {
                sql.append(enseignantId != null ? " AND" : " WHERE");
                sql.append(" c.classe_nom = '").append(classeNom.replace("'", "''")).append("'");
            }
            sql.append(" GROUP BY c.id ORDER BY c.intitule");

            ResultSet rs = conn.createStatement().executeQuery(sql.toString());
            while (rs.next()) {
                Cours c = new Cours();
                c.setId(rs.getInt("id"));
                c.setIntitule(rs.getString("intitule"));
                c.setDescription(rs.getString("description"));
                c.setEnseignantId(rs.getInt("enseignant_id"));
                c.setNomEnseignant(rs.getString("prenom") + " " + rs.getString("nom"));
                c.setClasseNom(rs.getString("classe_nom"));
                c.setVolumeHorairePrevu(rs.getInt("volume_horaire_prevu"));
                c.setVolumeHoraireEffectue(rs.getInt("heures_effectuees"));
                liste.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return liste;
    }

    /**
     * Retourne la liste des classes distinctes.
     */
    public List<String> listerClasses() {
        List<String> classes = new ArrayList<>();
        try {
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT DISTINCT classe_nom FROM cours ORDER BY classe_nom"
            );
            while (rs.next()) classes.add(rs.getString("classe_nom"));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return classes;
    }
}

// Optimisation requetes SQL - Nathanael
