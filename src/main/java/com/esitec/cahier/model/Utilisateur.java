package com.esitec.cahier.model;

/**
 * Représente un utilisateur de l'application.
 */
public class Utilisateur {

    public enum Role {
        CHEF_DEPARTEMENT, ENSEIGNANT, RESPONSABLE_CLASSE
    }

    public enum Statut {
        EN_ATTENTE, VALIDE, REJETE
    }

    private int id;
    private String nom;
    private String prenom;
    private String email;
    private String motDePasse;
    private Role role;
    private Statut statut;

    public Utilisateur() {}

    public Utilisateur(String nom, String prenom, String email, String motDePasse, Role role) {
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.motDePasse = motDePasse;
        this.role = role;
        this.statut = Statut.EN_ATTENTE;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public String getNomComplet() { return prenom + " " + nom; }

    @Override
    public String toString() {
        return getNomComplet() + " (" + role + ")";
    }
}
