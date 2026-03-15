package com.esitec.cahier.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Représente une notification dans le système.
 */
public class Notification {

    public enum Type {
        SEANCE_EN_ATTENTE,   // Responsable : nouvelle séance à valider
        SEANCE_VALIDEE,      // Enseignant  : sa séance a été validée
        SEANCE_REJETEE,      // Enseignant  : sa séance a été rejetée
        COMPTE_EN_ATTENTE,   // Admin       : nouveau compte à valider
        AVANCEMENT_ELEVE,    // Admin/Resp  : cours > 80% terminé
        INFO                 // Général
    }

    private int id;
    private int destinataireId;       // ID utilisateur cible
    private Type type;
    private String titre;
    private String message;
    private boolean lue;
    private LocalDateTime dateCreation;

    public Notification() {}

    public Notification(int destinataireId, Type type, String titre, String message) {
        this.destinataireId = destinataireId;
        this.type = type;
        this.titre = titre;
        this.message = message;
        this.lue = false;
        this.dateCreation = LocalDateTime.now();
    }

    // Getters / Setters
    public int getId()                        { return id; }
    public void setId(int id)                 { this.id = id; }
    public int getDestinataireId()            { return destinataireId; }
    public void setDestinataireId(int id)     { this.destinataireId = id; }
    public Type getType()                     { return type; }
    public void setType(Type type)            { this.type = type; }
    public String getTitre()                  { return titre; }
    public void setTitre(String titre)        { this.titre = titre; }
    public String getMessage()                { return message; }
    public void setMessage(String message)    { this.message = message; }
    public boolean isLue()                    { return lue; }
    public void setLue(boolean lue)           { this.lue = lue; }
    public LocalDateTime getDateCreation()    { return dateCreation; }
    public void setDateCreation(LocalDateTime d) { this.dateCreation = d; }

    public String getDateFormatee() {
        if (dateCreation == null) return "";
        return dateCreation.format(DateTimeFormatter.ofPattern("dd/MM HH:mm"));
    }

    public String getIcone() {
        return switch (type) {
            case SEANCE_EN_ATTENTE  -> "⏳";
            case SEANCE_VALIDEE     -> "✅";
            case SEANCE_REJETEE     -> "❌";
            case COMPTE_EN_ATTENTE  -> "👤";
            case AVANCEMENT_ELEVE   -> "📊";
            case INFO               -> "ℹ️";
        };
    }

    public String getCouleurHex() {
        return switch (type) {
            case SEANCE_EN_ATTENTE  -> "#FF9800";
            case SEANCE_VALIDEE     -> "#00C878";
            case SEANCE_REJETEE     -> "#FF4646";
            case COMPTE_EN_ATTENTE  -> "#008CFF";
            case AVANCEMENT_ELEVE   -> "#00D2E6";
            case INFO               -> "#8090B0";
        };
    }
}
