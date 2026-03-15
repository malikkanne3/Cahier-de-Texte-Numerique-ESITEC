package com.esitec.cahier.ui.admin;

import com.esitec.cahier.dao.CoursDAO;
import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

/**
 * Dialogue création/modification d'un cours — Thème sombre SUPDECO.
 * Chef de département : assigner un cours à un enseignant.
 */
public class DialogCours extends JDialog {

    private Cours cours;
    private UtilisateurDAO utilisateurDAO;
    private CoursDAO coursDAO;

    private JTextField champIntitule, champDescription, champClasse, champVolumeH;
    private JComboBox<String> comboEnseignant;
    private List<Utilisateur> enseignantsValides;

    public DialogCours(Frame parent, Cours cours, UtilisateurDAO uDAO, CoursDAO cDAO) {
        super(parent, cours == null ? "Nouveau cours" : "Modifier le cours", true);
        this.cours = cours;
        this.utilisateurDAO = uDAO;
        this.coursDAO = cDAO;
        initialiserUI();
    }

    private void initialiserUI() {
        setSize(520, 530);
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
        JLabel titre = new JLabel(cours == null ? "📖  Assigner un nouveau cours" : "✏  Modifier le cours");
        titre.setFont(new Font("Dialog", Font.BOLD, 20));
        titre.setForeground(Color.WHITE);
        JLabel sub = new JLabel(cours == null
                ? "Créez un cours et assignez-le à un enseignant"
                : "Modifiez les informations du cours");
        sub.setFont(new Font("Dialog", Font.PLAIN, 11));
        sub.setForeground(new Color(180, 210, 255));
        JPanel htx = new JPanel(new GridLayout(2, 1, 0, 3));
        htx.setOpaque(false); htx.add(titre); htx.add(sub);
        header.add(htx, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.FOND_CARD);
        form.setBorder(new EmptyBorder(25, 32, 25, 32));

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.gridx = 0; gc.weightx = 1;

        // Intitulé
        gc.gridy = 0; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Intitulé du cours *"), gc);
        gc.gridy = 1; gc.insets = new Insets(0, 0, 14, 0);
        champIntitule = UIHelper.creerChamp(30);
        champIntitule.setPreferredSize(new Dimension(440, 42));
        if (cours != null) champIntitule.setText(cours.getIntitule());
        form.add(champIntitule, gc);

        // Description
        gc.gridy = 2; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Description (facultatif)"), gc);
        gc.gridy = 3; gc.insets = new Insets(0, 0, 14, 0);
        champDescription = UIHelper.creerChamp(30);
        champDescription.setPreferredSize(new Dimension(440, 42));
        if (cours != null && cours.getDescription() != null)
            champDescription.setText(cours.getDescription());
        form.add(champDescription, gc);

        // Classe
        gc.gridy = 4; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Classe *"), gc);
        gc.gridy = 5; gc.insets = new Insets(0, 0, 14, 0);
        champClasse = UIHelper.creerChamp(20);
        champClasse.setPreferredSize(new Dimension(440, 42));
        if (cours != null) champClasse.setText(cours.getClasseNom());
        form.add(champClasse, gc);

        // Enseignant assigné
        gc.gridy = 6; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Enseignant assigné *"), gc);
        gc.gridy = 7; gc.insets = new Insets(0, 0, 14, 0);
        enseignantsValides = utilisateurDAO.listerParRole(Utilisateur.Role.ENSEIGNANT)
                .stream().filter(u -> u.getStatut() == Utilisateur.Statut.VALIDE).toList();
        String[] noms = enseignantsValides.stream()
                .map(u -> u.getNomComplet() + " (" + u.getEmail() + ")")
                .toArray(String[]::new);
        comboEnseignant = new JComboBox<>(noms);
        UIHelper.styliserCombo(comboEnseignant);
        comboEnseignant.setPreferredSize(new Dimension(440, 42));
        if (cours != null)
            for (int i = 0; i < enseignantsValides.size(); i++)
                if (enseignantsValides.get(i).getId() == cours.getEnseignantId()) {
                    comboEnseignant.setSelectedIndex(i); break;
                }
        form.add(comboEnseignant, gc);

        // Volume horaire
        gc.gridy = 8; gc.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Volume horaire prévu (heures) *"), gc);
        gc.gridy = 9; gc.insets = new Insets(0, 0, 25, 0);
        champVolumeH = UIHelper.creerChamp(10);
        champVolumeH.setPreferredSize(new Dimension(440, 42));
        if (cours != null) champVolumeH.setText(String.valueOf(cours.getVolumeHorairePrevu()));
        form.add(champVolumeH, gc);

        // Boutons
        gc.gridy = 10; gc.insets = new Insets(0, 0, 0, 0);
        JPanel btns = new JPanel(new GridLayout(1, 2, 12, 0));
        btns.setOpaque(false);
        JButton btnAnn = UIHelper.creerBouton("Annuler", new Color(50, 60, 90));
        JButton btnOk  = UIHelper.creerBoutonSucces(cours == null ? "Créer le cours" : "Enregistrer");
        btnAnn.setPreferredSize(new Dimension(180, 44));
        btnOk.setPreferredSize(new Dimension(180, 44));
        btnAnn.addActionListener(e -> dispose());
        btnOk.addActionListener(e -> sauvegarder());
        btns.add(btnAnn); btns.add(btnOk);
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
        String intitule = champIntitule.getText().trim();
        String classe   = champClasse.getText().trim();
        String volStr   = champVolumeH.getText().trim();

        if (intitule.isEmpty() || classe.isEmpty() || volStr.isEmpty()) {
            UIHelper.afficherErreur(this, "Veuillez remplir tous les champs obligatoires (*)."); return;
        }
        if (enseignantsValides.isEmpty()) {
            UIHelper.afficherErreur(this, "Aucun enseignant validé disponible.\nValidez d'abord un compte enseignant."); return;
        }
        int vol;
        try {
            vol = Integer.parseInt(volStr);
            if (vol <= 0) throw new NumberFormatException();
        } catch (Exception e) { UIHelper.afficherErreur(this, "Volume horaire invalide (entier positif requis)."); return; }

        int ensId = enseignantsValides.get(comboEnseignant.getSelectedIndex()).getId();

        if (cours == null) {
            Cours c = new Cours();
            c.setIntitule(intitule);
            c.setDescription(champDescription.getText().trim());
            c.setClasseNom(classe);
            c.setEnseignantId(ensId);
            c.setVolumeHorairePrevu(vol);
            if (coursDAO.creer(c)) { UIHelper.afficherSucces(this, "Cours créé et assigné avec succès ✓"); dispose(); }
            else UIHelper.afficherErreur(this, "Erreur lors de la création du cours.");
        } else {
            cours.setIntitule(intitule);
            cours.setDescription(champDescription.getText().trim());
            cours.setClasseNom(classe);
            cours.setEnseignantId(ensId);
            cours.setVolumeHorairePrevu(vol);
            if (coursDAO.modifier(cours)) { UIHelper.afficherSucces(this, "Cours modifié avec succès ✓"); dispose(); }
            else UIHelper.afficherErreur(this, "Erreur lors de la modification.");
        }
    }
}
