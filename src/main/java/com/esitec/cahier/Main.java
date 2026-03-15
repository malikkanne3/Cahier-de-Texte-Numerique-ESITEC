package com.esitec.cahier;

import com.esitec.cahier.dao.DatabaseManager;
import com.esitec.cahier.ui.common.FenetreConnexion;
import com.esitec.cahier.util.UIHelper;

import javax.swing.*;

/**
 * Point d'entrée de l'application Cahier de Texte Numérique.
 * ESITEC - Groupe SUP de CO Dakar - Projet POO Java 2026
 */
public class Main {

    public static void main(String[] args) {
        // Appliquer le Look & Feel
        UIHelper.appliquerLookAndFeel();

        // Initialiser la base de données
        SwingUtilities.invokeLater(() -> {
            try {
                // Initialisation DB en arrière-plan
                DatabaseManager.getInstance();

                // Lancer la fenêtre de connexion
                FenetreConnexion fenetre = new FenetreConnexion();
                fenetre.setVisible(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                    "Erreur de démarrage : " + e.getMessage(),
                    "Erreur critique",
                    JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }
        });
    }
}
