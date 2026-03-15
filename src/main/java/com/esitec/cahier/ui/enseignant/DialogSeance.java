package com.esitec.cahier.ui.enseignant;

import com.esitec.cahier.dao.CoursDAO;
import com.esitec.cahier.dao.SeanceDAO;
import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Seance;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * Dialogue d'ajout/modification d'une séance — Thème sombre SUPDECO.
 */
public class DialogSeance extends JDialog {

    private Seance seance;
    private Utilisateur utilisateur;
    private CoursDAO coursDAO;
    private SeanceDAO seanceDAO;

    private JComboBox<String> comboCours;
    private JTextField champDate, champHeure, champDuree;
    private JTextArea champContenu, champObservations;
    private List<Cours> cours;

    public DialogSeance(Frame parent, Seance seance, Utilisateur utilisateur,
                        CoursDAO cDAO, SeanceDAO sDAO) {
        super(parent, seance == null ? "Nouvelle séance" : "Modifier la séance", true);
        this.seance = seance;
        this.utilisateur = utilisateur;
        this.coursDAO = cDAO;
        this.seanceDAO = sDAO;
        initialiserUI();
    }

    private void initialiserUI() {
        setSize(580, 640);
        getRootPane().setBorder(UIHelper.bordure("dialog"));
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIHelper.FOND_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // Header gradient
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
        String titreStr = seance == null ? "📝  Nouvelle séance" : "✏  Modifier la séance";
        JLabel titre = new JLabel(titreStr);
        titre.setFont(new Font("Dialog", Font.BOLD, 20));
        titre.setForeground(Color.WHITE);
        JLabel sub = new JLabel(seance == null
                ? "Enregistrez une séance — elle sera soumise à validation"
                : "Modifiez les informations de la séance");
        sub.setFont(new Font("Dialog", Font.PLAIN, 11));
        sub.setForeground(new Color(180, 210, 255));
        JPanel htx = new JPanel(new GridLayout(2, 1, 0, 3));
        htx.setOpaque(false);
        htx.add(titre);
        htx.add(sub);
        header.add(htx, BorderLayout.CENTER);
        root.add(header, BorderLayout.NORTH);

        // Formulaire
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.FOND_CARD);
        form.setBorder(new EmptyBorder(22, 30, 22, 30));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.weightx = 1;

        // Cours
        g.gridy = 0; g.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Cours assigné *"), g);
        g.gridy = 1; g.insets = new Insets(0, 0, 14, 0);
        cours = coursDAO.listerParEnseignant(utilisateur.getId());
        String[] intitules = cours.stream()
                .map(c -> c.getIntitule() + "  —  " + c.getClasseNom())
                .toArray(String[]::new);
        comboCours = new JComboBox<>(intitules);
        styleCombo(comboCours);
        if (seance != null)
            for (int i = 0; i < cours.size(); i++)
                if (cours.get(i).getId() == seance.getCoursId()) { comboCours.setSelectedIndex(i); break; }
        form.add(comboCours, g);

        // Date + Heure sur 2 colonnes
        g.gridy = 2; g.insets = new Insets(0, 0, 4, 0);
        JPanel rowDH = new JPanel(new GridLayout(1, 2, 15, 0));
        rowDH.setOpaque(false);

        JPanel pDate = new JPanel(new BorderLayout(0, 5)); pDate.setOpaque(false);
        pDate.add(lbl("Date * (AAAA-MM-JJ)"), BorderLayout.NORTH);
        champDate = UIHelper.creerChamp(12);
        champDate.setText(seance != null ? seance.getDate().toString() : LocalDate.now().toString());
        pDate.add(champDate, BorderLayout.CENTER);

        JPanel pHeure = new JPanel(new BorderLayout(0, 5)); pHeure.setOpaque(false);
        pHeure.add(lbl("Heure * (HH:MM)"), BorderLayout.NORTH);
        champHeure = UIHelper.creerChamp(8);
        champHeure.setText(seance != null ? seance.getHeure().toString() : "08:00");
        pHeure.add(champHeure, BorderLayout.CENTER);

        rowDH.add(pDate); rowDH.add(pHeure);
        form.add(rowDH, g);

        // Durée
        g.gridy = 3; g.insets = new Insets(14, 0, 4, 0);
        form.add(lbl("Durée (minutes) *"), g);
        g.gridy = 4; g.insets = new Insets(0, 0, 14, 0);
        champDuree = UIHelper.creerChamp(8);
        champDuree.setText(seance != null ? String.valueOf(seance.getDureeMinutes()) : "90");
        champDuree.setPreferredSize(new Dimension(500, 42));
        form.add(champDuree, g);

        // Contenu
        g.gridy = 5; g.insets = new Insets(0, 0, 4, 0);
        form.add(lbl("Contenu de la séance *"), g);
        g.gridy = 6; g.insets = new Insets(0, 0, 14, 0); g.ipady = 70;
        champContenu = creerTextArea(5);
        if (seance != null) champContenu.setText(seance.getContenu());
        form.add(creerScrollArea(champContenu), g);

        // Observations
        g.gridy = 7; g.insets = new Insets(0, 0, 4, 0); g.ipady = 0;
        form.add(lbl("Observations (facultatif)"), g);
        g.gridy = 8; g.insets = new Insets(0, 0, 22, 0); g.ipady = 45;
        champObservations = creerTextArea(3);
        if (seance != null && seance.getObservations() != null)
            champObservations.setText(seance.getObservations());
        form.add(creerScrollArea(champObservations), g);

        // Boutons
        g.gridy = 9; g.insets = new Insets(0, 0, 0, 0); g.ipady = 0;
        JPanel btns = new JPanel(new GridLayout(1, 2, 12, 0));
        btns.setOpaque(false);
        JButton btnAnn = UIHelper.creerBouton("Annuler", new Color(50, 60, 90));
        JButton btnOk = UIHelper.creerBoutonSucces(seance == null ? "Enregistrer" : "Modifier");
        btnAnn.setPreferredSize(new Dimension(180, 44));
        btnOk.setPreferredSize(new Dimension(180, 44));
        btnAnn.addActionListener(e -> dispose());
        btnOk.addActionListener(e -> sauvegarder());
        btns.add(btnAnn); btns.add(btnOk);
        form.add(btns, g);

        JScrollPane sp = new JScrollPane(form);
        sp.setBorder(null);
        sp.getViewport().setBackground(UIHelper.FOND_CARD);
        root.add(sp, BorderLayout.CENTER);
        add(root);
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Dialog", Font.BOLD, 12));
        l.setForeground(new Color(100, 160, 230));
        return l;
    }

    private JTextArea creerTextArea(int rows) {
        JTextArea ta = new JTextArea(rows, 30);
        ta.setFont(new Font("Dialog", Font.PLAIN, 13));
        ta.setBackground(UIHelper.FOND_CARD2);
        ta.setForeground(UIHelper.TEXTE_BLANC);
        ta.setCaretColor(UIHelper.TEXTE_BLANC);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBorder(new EmptyBorder(8, 10, 8, 10));
        return ta;
    }

    private JScrollPane creerScrollArea(JTextArea ta) {
        JScrollPane sp = new JScrollPane(ta);
        UIHelper.appliquerBordure(sp, "accent");
        sp.getViewport().setBackground(UIHelper.FOND_CARD2);
        return sp;
    }

    private void styleCombo(JComboBox<String> cb) {
        UIHelper.styliserCombo(cb);
        cb.setPreferredSize(new Dimension(500, 42));
    }

    private void sauvegarder() {
        if (cours.isEmpty()) {
            UIHelper.afficherErreur(this, "Aucun cours disponible.\nVérifiez votre assignation auprès du chef de département.");
            return;
        }
        String dateStr  = champDate.getText().trim();
        String heureStr = champHeure.getText().trim();
        String dureeStr = champDuree.getText().trim();
        String contenu  = champContenu.getText().trim();

        if (contenu.isEmpty()) {
            UIHelper.afficherErreur(this, "Le contenu de la séance est obligatoire.");
            return;
        }

        LocalDate date;
        LocalTime heure;
        int duree;

        try { date = LocalDate.parse(dateStr); }
        catch (Exception e) { UIHelper.afficherErreur(this, "Format de date invalide. Utilisez AAAA-MM-JJ."); return; }

        try { heure = LocalTime.parse(heureStr); }
        catch (Exception e) { UIHelper.afficherErreur(this, "Format d'heure invalide. Utilisez HH:MM."); return; }

        try {
            duree = Integer.parseInt(dureeStr);
            if (duree <= 0) throw new NumberFormatException();
        } catch (Exception e) { UIHelper.afficherErreur(this, "La durée doit être un entier positif."); return; }

        int coursId = cours.get(comboCours.getSelectedIndex()).getId();

        if (seance == null) {
            Seance s = new Seance(coursId, utilisateur.getId(), date, heure, duree,
                    contenu, champObservations.getText().trim());
            if (seanceDAO.creer(s)) {
                UIHelper.afficherSucces(this, "Séance enregistrée ✓\nElle est en attente de validation par le responsable de classe.");
                dispose();
            } else {
                UIHelper.afficherErreur(this, "Erreur lors de l'enregistrement.");
            }
        } else {
            seance.setCoursId(coursId);
            seance.setDate(date);
            seance.setHeure(heure);
            seance.setDureeMinutes(duree);
            seance.setContenu(contenu);
            seance.setObservations(champObservations.getText().trim());
            if (seanceDAO.modifier(seance)) {
                UIHelper.afficherSucces(this, "Séance modifiée avec succès ✓");
                dispose();
            } else {
                UIHelper.afficherErreur(this, "Erreur lors de la modification.");
            }
        }
    }
}
