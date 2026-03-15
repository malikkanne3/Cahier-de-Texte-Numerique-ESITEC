package com.esitec.cahier.model;

/**
 * Représente un cours assigné à un enseignant pour une classe.
 */
public class Cours {

    private int id;
    private String intitule;
    private String description;
    private int enseignantId;
    private String nomEnseignant;
    private String classeNom;
    private int volumeHorairePrevu; // en heures
    private int volumeHoraireEffectue; // calculé à partir des séances

    public Cours() {}

    public Cours(String intitule, String description, int enseignantId, String classeNom, int volumeHorairePrevu) {
        this.intitule = intitule;
        this.description = description;
        this.enseignantId = enseignantId;
        this.classeNom = classeNom;
        this.volumeHorairePrevu = volumeHorairePrevu;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIntitule() { return intitule; }
    public void setIntitule(String intitule) { this.intitule = intitule; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getEnseignantId() { return enseignantId; }
    public void setEnseignantId(int enseignantId) { this.enseignantId = enseignantId; }

    public String getNomEnseignant() { return nomEnseignant; }
    public void setNomEnseignant(String nomEnseignant) { this.nomEnseignant = nomEnseignant; }

    public String getClasseNom() { return classeNom; }
    public void setClasseNom(String classeNom) { this.classeNom = classeNom; }

    public int getVolumeHorairePrevu() { return volumeHorairePrevu; }
    public void setVolumeHorairePrevu(int volumeHorairePrevu) { this.volumeHorairePrevu = volumeHorairePrevu; }

    public int getVolumeHoraireEffectue() { return volumeHoraireEffectue; }
    public void setVolumeHoraireEffectue(int volumeHoraireEffectue) { this.volumeHoraireEffectue = volumeHoraireEffectue; }

    public double getPourcentageAvancement() {
        if (volumeHorairePrevu == 0) return 0;
        return (double) volumeHoraireEffectue / volumeHorairePrevu * 100;
    }

    @Override
    public String toString() { return intitule + " - " + classeNom; }
}

// Revision modele Cours - Nathanael
