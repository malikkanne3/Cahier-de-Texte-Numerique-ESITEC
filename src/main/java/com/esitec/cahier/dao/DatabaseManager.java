package com.esitec.cahier.dao;

import java.sql.*;

/**
 * Gestionnaire de connexion à la base de données SQLite.
 * Implémente le pattern Singleton.
 */
public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:cahier_de_texte.db";
    private static DatabaseManager instance;
    private Connection connection;

    private DatabaseManager() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
            initialiserBaseDeDonnees();
            insererDonneesInitiales();
        } catch (Exception e) {
            throw new RuntimeException("Impossible de se connecter à la base de données", e);
        }
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private void initialiserBaseDeDonnees() throws SQLException {
        Statement stmt = connection.createStatement();

        // Table utilisateurs
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS utilisateurs (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT NOT NULL,
                prenom TEXT NOT NULL,
                email TEXT UNIQUE NOT NULL,
                mot_de_passe TEXT NOT NULL,
                role TEXT NOT NULL CHECK(role IN ('CHEF_DEPARTEMENT','ENSEIGNANT','RESPONSABLE_CLASSE')),
                statut TEXT NOT NULL DEFAULT 'EN_ATTENTE' CHECK(statut IN ('EN_ATTENTE','VALIDE','REJETE'))
            )
        """);

        // Table cours
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS cours (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                intitule TEXT NOT NULL,
                description TEXT,
                enseignant_id INTEGER NOT NULL,
                classe_nom TEXT NOT NULL,
                volume_horaire_prevu INTEGER NOT NULL DEFAULT 30,
                FOREIGN KEY (enseignant_id) REFERENCES utilisateurs(id)
            )
        """);

        // Table séances
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS seances (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cours_id INTEGER NOT NULL,
                enseignant_id INTEGER NOT NULL,
                date TEXT NOT NULL,
                heure TEXT NOT NULL,
                duree_minutes INTEGER NOT NULL,
                contenu TEXT NOT NULL,
                observations TEXT,
                statut TEXT NOT NULL DEFAULT 'EN_ATTENTE' CHECK(statut IN ('EN_ATTENTE','VALIDE','REJETE')),
                commentaire_rejet TEXT,
                FOREIGN KEY (cours_id) REFERENCES cours(id),
                FOREIGN KEY (enseignant_id) REFERENCES utilisateurs(id)
            )
        """);

        stmt.close();
    }

    private void insererDonneesInitiales() throws SQLException {
        // Vérifier si des données existent déjà
        ResultSet rs = connection.createStatement().executeQuery("SELECT COUNT(*) FROM utilisateurs");
        if (rs.next() && rs.getInt(1) > 0) return;

        // Seul compte par défaut : Malik KANNE — Chef de département
        // Email : malikkanne3@gmail.com / Mot de passe : malik.com
        String hashAdmin;
        try {
            hashAdmin = org.mindrot.jbcrypt.BCrypt.hashpw("malik.com", org.mindrot.jbcrypt.BCrypt.gensalt());
        } catch (Exception e) {
            hashAdmin = "malik.com";
        }

        PreparedStatement ps = connection.prepareStatement(
            "INSERT INTO utilisateurs (nom, prenom, email, mot_de_passe, role, statut) VALUES (?,?,?,?,?,?)"
        );
        ps.setString(1, "KANNE");
        ps.setString(2, "Malik");
        ps.setString(3, "malikkanne3@gmail.com");
        ps.setString(4, hashAdmin);
        ps.setString(5, "CHEF_DEPARTEMENT");
        ps.setString(6, "VALIDE");
        ps.executeUpdate();
        ps.close();
    }
}