package com.esitec.cahier.ui.admin;

import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Dialogue de modification d'un utilisateur existant.
 * Permet de changer : nom, prénom, email, mot de passe, rôle.
 */
public class DialogModifierUtilisateur extends JDialog {

    private final Utilisateur    utilisateur;
    private final UtilisateurDAO dao;
    private final Runnable       onSuccess;

    private JTextField    champNom, champPrenom, champEmail;
    private JPasswordField champMdp;
    private JComboBox<String> comboRole;

    public DialogModifierUtilisateur(Frame parent, Utilisateur u,
                                      UtilisateurDAO dao, Runnable onSuccess) {
        super(parent, "Modifier l'utilisateur", true);
        this.utilisateur = u;
        this.dao         = dao;
        this.onSuccess   = onSuccess;
        initialiserUI();
    }

    private void initialiserUI() {
        setSize(500, 530);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getRootPane().setBorder(UIHelper.bordure("dialog"));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIHelper.FOND_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        root.setOpaque(false);

        // Header
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(0, 0, UIHelper.BLEU_PRIMAIRE,
                        getWidth(), getHeight(), new Color(0, 60, 140));
                g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), getHeight()); g2.dispose();
            }
        };
        header.setOpaque(false); header.setBorder(new EmptyBorder(20, 25, 20, 25));
        JLabel titre = new JLabel("✏  Modifier l'utilisateur");
        titre.setFont(new Font("Dialog", Font.BOLD, 20));
        titre.setForeground(Color.WHITE);
        JLabel sub = new JLabel("Modifiez les informations de " + utilisateur.getNomComplet());
        sub.setFont(new Font("Dialog", Font.PLAIN, 11));
        sub.setForeground(new Color(180, 210, 255));
        JPanel htx = new JPanel(new GridLayout(2, 1, 0, 3)); htx.setOpaque(false);
        htx.add(titre); htx.add(sub);
        header.add(htx, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // Formulaire
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.FOND_CARD);
        form.setBorder(new EmptyBorder(25, 32, 25, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.weightx = 1;

        // Nom / Prénom
        gc.gridy = 0; gc.insets = new Insets(0, 0, 14, 0);
        JPanel rowNP = new JPanel(new GridLayout(1, 2, 12, 0)); rowNP.setOpaque(false);

        JPanel pNom = new JPanel(new BorderLayout(0, 5)); pNom.setOpaque(false);
        pNom.add(lbl("Nom *"), BorderLayout.NORTH);
        champNom = UIHelper.creerChamp(15);
        champNom.setText(utilisateur.getNom());
        pNom.add(champNom, BorderLayout.CENTER);

        JPanel pPre = new JPanel(new BorderLayout(0, 5)); pPre.setOpaque(false);
        pPre.add(lbl("Prénom *"), BorderLayout.NORTH);
        champPrenom = UIHelper.creerChamp(15);
        champPrenom.setText(utilisateur.getPrenom());
        pPre.add(champPrenom, BorderLayout.CENTER);

        rowNP.add(pNom); rowNP.add(pPre);
        form.add(rowNP, gc);

        // Email
        gc.gridy = 1; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Adresse email *"), gc);
        gc.gridy = 2; gc.insets = new Insets(0, 0, 14, 0);
        champEmail = UIHelper.creerChamp(30);
        champEmail.setPreferredSize(new Dimension(420, 42));
        champEmail.setText(utilisateur.getEmail());
        form.add(champEmail, gc);

        // Mot de passe
        gc.gridy = 3; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Nouveau mot de passe (laisser vide = inchangé)"), gc);
        gc.gridy = 4; gc.insets = new Insets(0, 0, 14, 0);
        champMdp = UIHelper.creerChampMotDePasse(30);
        champMdp.setPreferredSize(new Dimension(420, 42));
        form.add(UIHelper.wrapAvecOeil(champMdp), gc);

        // Rôle
        gc.gridy = 5; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Rôle *"), gc);
        gc.gridy = 6; gc.insets = new Insets(0, 0, 25, 0);
        comboRole = new JComboBox<>(new String[]{
            "🎓  Enseignant",
            "📋  Responsable de classe"
        });
        UIHelper.styliserCombo(comboRole);
        comboRole.setFont(new Font("Dialog", Font.PLAIN, 13));
        comboRole.setBackground(UIHelper.FOND_CARD2);
        comboRole.setForeground(UIHelper.TEXTE_BLANC);
        comboRole.setPreferredSize(new Dimension(420, 42));
        // Pré-sélectionner le rôle actuel
        switch (utilisateur.getRole()) {
            case ENSEIGNANT         -> comboRole.setSelectedIndex(0);
            case RESPONSABLE_CLASSE -> comboRole.setSelectedIndex(1);
            case CHEF_DEPARTEMENT   -> comboRole.setSelectedIndex(0); // Chef reste en lecture seule sur Enseignant
        }
        form.add(comboRole, gc);

        // Boutons
        gc.gridy = 7; gc.insets = new Insets(0, 0, 0, 0);
        JPanel btns = new JPanel(new GridLayout(1, 2, 12, 0)); btns.setOpaque(false);
        JButton btnAnn  = UIHelper.creerBouton("Annuler", new Color(50, 60, 90));
        JButton btnSave = UIHelper.creerBoutonSucces("💾  Enregistrer");
        btnAnn.setPreferredSize(new Dimension(180, 44));
        btnSave.setPreferredSize(new Dimension(180, 44));
        btnAnn.addActionListener(e -> dispose());
        btnSave.addActionListener(e -> sauvegarder());
        btns.add(btnAnn); btns.add(btnSave);
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

    private void sauvegarder() {
        String nom    = champNom.getText().trim();
        String prenom = champPrenom.getText().trim();
        String email  = champEmail.getText().trim();
        String mdp    = new String(champMdp.getPassword());

        if (nom.isEmpty() || prenom.isEmpty() || email.isEmpty()) {
            UIHelper.afficherErreur(this, "Nom, prénom et email sont obligatoires."); return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            UIHelper.afficherErreur(this, "Adresse email invalide."); return;
        }
        if (!mdp.isEmpty() && mdp.length() < 6) {
            UIHelper.afficherErreur(this, "Le mot de passe doit contenir au moins 6 caractères."); return;
        }

        Utilisateur.Role role = switch (comboRole.getSelectedIndex()) {
            case 1  -> Utilisateur.Role.RESPONSABLE_CLASSE;
            default -> Utilisateur.Role.ENSEIGNANT;
        };

        utilisateur.setNom(nom);
        utilisateur.setPrenom(prenom);
        utilisateur.setEmail(email);
        utilisateur.setRole(role);
        if (!mdp.isEmpty()) utilisateur.setMotDePasse(mdp);

        if (dao.modifier(utilisateur)) {
            UIHelper.afficherSucces(this, "Utilisateur modifié avec succès ✓");
            if (onSuccess != null) onSuccess.run();
            dispose();
        } else {
            UIHelper.afficherErreur(this, "Erreur lors de la modification.\nCet email est peut-être déjà utilisé.");
        }
    }
}
