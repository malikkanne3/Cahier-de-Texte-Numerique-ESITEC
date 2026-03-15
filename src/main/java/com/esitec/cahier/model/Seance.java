package com.esitec.cahier.model;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Représente une séance de cours enregistrée dans le cahier de texte.
 */
public class Seance {

    public enum Statut {
        EN_ATTENTE, VALIDE, REJETE
    }

    private int id;
    private int coursId;
    private String coursIntitule;
    private String classeNom;
    private int enseignantId;
    private String nomEnseignant;
    private LocalDate date;
    private LocalTime heure;
    private int dureeMinutes;
    private String contenu;
    private String observations;
    private Statut statut;
    private String commentaireRejet;

    public Seance() {
        this.statut = Statut.EN_ATTENTE;
    }

    public Seance(int coursId, int enseignantId, LocalDate date, LocalTime heure,
                  int dureeMinutes, String contenu, String observations) {
        this.coursId = coursId;
        this.enseignantId = enseignantId;
        this.date = date;
        this.heure = heure;
        this.dureeMinutes = dureeMinutes;
        this.contenu = contenu;
        this.observations = observations;
        this.statut = Statut.EN_ATTENTE;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCoursId() { return coursId; }
    public void setCoursId(int coursId) { this.coursId = coursId; }

    public String getCoursIntitule() { return coursIntitule; }
    public void setCoursIntitule(String coursIntitule) { this.coursIntitule = coursIntitule; }

    public String getClasseNom() { return classeNom; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }

    public int getEnseignantId() { return enseignantId; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }

    public String getNomEnseignant() { return nomEnseignant; }
    public void setNomEnseignant(String nomEnseignant) { this.nomEnseignant = nomEnseignant; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getHeure() { return heure; }
    public void setHeure(LocalTime heure) { this.heure = heure; }

    public int getDureeMinutes() { return dureeMinutes; }
    public void setDureeMinutes(int dureeMinutes) { this.dureeMinutes = dureeMinutes; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public String getObservations() { return observations; }
    public void setObservations(String observations) { this.observations = observations; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public String getCommentaireRejet() { return commentaireRejet; }
    public void setCommentaireRejet(String commentaireRejet) { this.commentaireRejet = commentaireRejet; }

    public String getDureeFormatee() {
        int h = dureeMinutes / 60;
        int m = dureeMinutes % 60;
        if (h > 0) return h + "h" + (m > 0 ? String.format("%02d", m) : "");
        return m + " min";
    }
}
