package com.esitec.cahier.ui.common;

import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialogue d'inscription — Thème sombre SUPDECO.
 * Accessible depuis la page de connexion OU par le chef de département.
 */
public class DialogInscription extends JDialog {

    private JTextField champNom, champPrenom, champEmail;
    private JPasswordField champMdp, champMdpConfirm;
    private JComboBox<String> comboRole;

    public DialogInscription(Frame parent) {
        super(parent, "Créer un compte", true);
        initialiserUI();
    }

    private void initialiserUI() {
        setSize(500, 590);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getRootPane().setBorder(UIHelper.bordure("dialog"));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIHelper.FOND_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // Header
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, UIHelper.BLEU_PRIMAIRE,
                        getWidth(), getHeight(), new Color(0, 60, 140));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(20, 25, 20, 25));
        JLabel titre = new JLabel("👤  Créer un compte");
        titre.setFont(new Font("Dialog", Font.BOLD, 20));
        titre.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Votre compte sera activé après validation par le chef de département");
        sub.setFont(new Font("Dialog", Font.PLAIN, 11));
        sub.setForeground(new Color(180, 210, 255));
        JPanel htx = new JPanel(new GridLayout(2, 1, 0, 3));
        htx.setOpaque(false); htx.add(titre); htx.add(sub);
        header.add(htx, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // Formulaire
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.FOND_CARD);
        form.setBorder(new EmptyBorder(25, 32, 25, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.weightx = 1;

        // Nom / Prénom côte à côte
        gc.gridy = 0; gc.insets = new Insets(0, 0, 14, 0);
        JPanel nomPrenom = new JPanel(new GridLayout(1, 2, 14, 0));
        nomPrenom.setOpaque(false);

        JPanel pNom = new JPanel(new BorderLayout(0, 5)); pNom.setOpaque(false);
        pNom.add(lbl("Nom *"), BorderLayout.NORTH);
        champNom = UIHelper.creerChamp(15); champNom.setPreferredSize(new Dimension(100, 42));
        pNom.add(champNom, BorderLayout.CENTER);

        JPanel pPre = new JPanel(new BorderLayout(0, 5)); pPre.setOpaque(false);
        pPre.add(lbl("Prénom *"), BorderLayout.NORTH);
        champPrenom = UIHelper.creerChamp(15); champPrenom.setPreferredSize(new Dimension(100, 42));
        pPre.add(champPrenom, BorderLayout.CENTER);

        nomPrenom.add(pNom); nomPrenom.add(pPre);
        form.add(nomPrenom, gc);

        // Email
        gc.gridy = 1; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Adresse email *"), gc);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 14, 0);
        champEmail = UIHelper.creerChamp(30);
        champEmail.setPreferredSize(new Dimension(420, 42));
        form.add(champEmail, gc);

        // Mot de passe
        gc.gridy = 3; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Mot de passe * (minimum 6 caractères)"), gc);
        gc.gridy = 4; gc.insets = new Insets(0, 0, 14, 0);
        champMdp = UIHelper.creerChampMotDePasse(30);
        champMdp.setPreferredSize(new Dimension(420, 42));
        form.add(UIHelper.wrapAvecOeil(champMdp), gc);

        // Confirmation
        gc.gridy = 5; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Confirmer le mot de passe *"), gc);
        gc.gridy = 6; gc.insets = new Insets(0, 0, 14, 0);
        champMdpConfirm = UIHelper.creerChampMotDePasse(30);
        champMdpConfirm.setPreferredSize(new Dimension(420, 42));
        form.add(UIHelper.wrapAvecOeil(champMdpConfirm), gc);

        // Rôle
        gc.gridy = 7; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Rôle *"), gc);
        gc.gridy = 8; gc.insets = new Insets(0, 0, 25, 0);
        comboRole = new JComboBox<>(new String[]{"Enseignant", "Responsable de classe"});
        UIHelper.styliserCombo(comboRole);
        comboRole.setPreferredSize(new Dimension(420, 42));
        form.add(comboRole, gc);

        // Boutons
        gc.gridy = 9; gc.insets = new Insets(0, 0, 0, 0);
        JPanel btns = new JPanel(new GridLayout(1, 2, 12, 0));
        btns.setOpaque(false);
        JButton btnAnn  = UIHelper.creerBouton("Annuler", new Color(50, 60, 90));
        JButton btnCreer = UIHelper.creerBoutonPrimaire("Créer le compte");
        btnAnn.setPreferredSize(new Dimension(180, 44));
        btnCreer.setPreferredSize(new Dimension(180, 44));
        btnAnn.addActionListener(e -> dispose());
        btnCreer.addActionListener(e -> creerCompte());
        champMdpConfirm.addActionListener(e -> creerCompte());
        btns.add(btnAnn); btns.add(btnCreer);
        form.add(btns, gc);

        root.add(form, BorderLayout.CENTER);
        add(root);
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Dialog", Font.BOLD, 12));
        l.setForeground(new Color(100, 160, 230));
        return l;
    }

    private void creerCompte() {
        String nom    = champNom.getText().trim();
        String prenom = champPrenom.getText().trim();
        String email  = champEmail.getText().trim();
        String mdp    = new String(champMdp.getPassword());
        String mdpC   = new String(champMdpConfirm.getPassword());

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty() || mdp.isEmpty()) {
            UIHelper.afficherErreur(this, "Veuillez remplir tous les champs obligatoires (*)."); return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            UIHelper.afficherErreur(this, "Adresse email invalide."); return;
        }
        if (mdp.length() < 6) {
            UIHelper.afficherErreur(this, "Le mot de passe doit contenir au moins 6 caractères."); return;
        }
        if (!mdp.equals(mdpC)) {
            UIHelper.afficherErreur(this, "Les mots de passe ne correspondent pas."); return;
        }

        Utilisateur.Role role = comboRole.getSelectedIndex() == 0
                ? Utilisateur.Role.ENSEIGNANT
                : Utilisateur.Role.RESPONSABLE_CLASSE;

        Utilisateur u = new Utilisateur(nom, prenom, email, mdp, role);
        u.setStatut(Utilisateur.Statut.EN_ATTENTE);

        if (new UtilisateurDAO().creer(u)) {
            UIHelper.afficherSucces(this,
                    "Compte créé avec succès ✓\nVotre compte sera activé par le chef de département.");
            dispose();
        } else {
            UIHelper.afficherErreur(this, "Erreur lors de la création.\nCet email est peut-être déjà utilisé.");
        }
    }
}
