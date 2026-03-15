package com.esitec.cahier.ui.admin;

import com.esitec.cahier.dao.CoursDAO;
import com.esitec.cahier.dao.SeanceDAO;
import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Seance;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.ui.common.DialogInscription;
import com.esitec.cahier.ui.admin.DialogAjouterUtilisateur;
import com.esitec.cahier.ui.common.Sidebar;
import com.esitec.cahier.util.ExportExcel;
import com.esitec.cahier.util.ExportPDF;
import com.esitec.cahier.util.TableRenderers;
import com.esitec.cahier.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.List;

/**
 * Dashboard Chef de département — toutes les fonctionnalités du cahier des charges.
 * - Ajouter enseignants et responsables de classe
 * - Assigner des cours aux enseignants
 * - Générer une fiche de suivi pédagogique (PDF/Excel)
 * - Consulter les statistiques globales
 * - Valider / rejeter les comptes utilisateurs
 */
public class DashboardAdmin extends JFrame {

    private final Utilisateur utilisateur;
    private final UtilisateurDAO utilisateurDAO = new UtilisateurDAO();
    private final CoursDAO      coursDAO        = new CoursDAO();
    private final SeanceDAO     seanceDAO       = new SeanceDAO();

    private JPanel    contenu;
    private CardLayout cardLayout;

    public DashboardAdmin(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        initialiserUI();
    }


    /** Rafraîchit un panel donné en le reconstruisant */
    private void refreshPanel(String nom) {
        cardLayout.show(contenu, nom);
    }

        private void initialiserUI() {
        setTitle("Chef de Département — Cahier de Texte ESITEC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1050, 650));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIHelper.FOND_DARK);

        Sidebar sidebar = new Sidebar(utilisateur);
        cardLayout = new CardLayout();
        contenu = new JPanel(cardLayout);
        contenu.setBackground(UIHelper.FOND_DARK);

        contenu.add(panelAccueil(),        "accueil");
        contenu.add(panelUtilisateurs(),   "utilisateurs");
        contenu.add(panelCours(),          "cours");
        contenu.add(panelSeances(),        "seances");
        contenu.add(panelStatistiques(),   "statistiques");

        JButton b0 = sidebar.addItem("⌂", "Tableau de bord", e -> cardLayout.show(contenu, "accueil"));
        sidebar.addSeparateur("Gestion");
        JButton b1 = sidebar.addItem("☃", "Utilisateurs",   e -> cardLayout.show(contenu, "utilisateurs"));
        sidebar.addSousMenu(b1,
            new String[][]{{"✚","Ajouter utilisateur"},{"✎","Modifier"},{"✓","Valider comptes"},{"✖","Supprimer"}},
            new ActionListener[]{
                ev -> { cardLayout.show(contenu,"utilisateurs"); new DialogAjouterUtilisateur(DashboardAdmin.this, utilisateurDAO, () -> refreshPanel("utilisateurs")).setVisible(true); },
                ev -> cardLayout.show(contenu,"utilisateurs"),
                ev -> cardLayout.show(contenu,"utilisateurs"),
                ev -> cardLayout.show(contenu,"utilisateurs")
            });
        JButton b2 = sidebar.addItem("▦", "Cours",          e -> cardLayout.show(contenu, "cours"));
        sidebar.addSousMenu(b2,
            new String[][]{{"✚","Nouveau cours"},{"✎","Modifier cours"},{"▤","Fiche PDF"},{"▥","Fiche Excel"}},
            new ActionListener[]{
                ev -> { cardLayout.show(contenu,"cours"); new DialogCours(DashboardAdmin.this, null, utilisateurDAO, coursDAO).setVisible(true); },
                ev -> cardLayout.show(contenu,"cours"),
                ev -> cardLayout.show(contenu,"cours"),
                ev -> cardLayout.show(contenu,"cours")
            });
        JButton b3 = sidebar.addItem("☰", "Seances",        e -> cardLayout.show(contenu, "seances"));
        sidebar.addSeparateur("Analyse");
        JButton b4 = sidebar.addItem("▣", "Statistiques",   e -> cardLayout.show(contenu, "statistiques"));
        sidebar.setActif(b0);
        root.add(sidebar, BorderLayout.WEST);
        root.add(contenu,  BorderLayout.CENTER);
        add(root);
    }

    // ——————————— EN-TÊTE PAGE ———————————
    private JPanel enTete(String titre, String sous, String breadcrumb) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIHelper.FOND_CARD);
                g.fillRect(0, 0, getWidth(), getHeight());
                g.setColor(UIHelper.BORDURE);
                g.fillRect(0, getHeight()-1, getWidth(), 1);
            }
        };
        p.setOpaque(false); p.setBorder(new EmptyBorder(18, 28, 18, 28));
        JLabel bc = new JLabel("Accueil  /  " + breadcrumb);
        bc.setFont(new Font("Dialog", Font.PLAIN, 11)); bc.setForeground(UIHelper.TEXTE_GRIS2);
        JLabel t  = new JLabel(titre);
        t.setFont(new Font("Dialog", Font.BOLD, 22)); t.setForeground(Color.WHITE);
        JLabel s  = new JLabel(sous);
        s.setFont(new Font("Dialog", Font.PLAIN, 13)); s.setForeground(UIHelper.TEXTE_GRIS);
        JPanel tx = new JPanel(); tx.setLayout(new BoxLayout(tx, BoxLayout.Y_AXIS)); tx.setOpaque(false);
        tx.add(bc); tx.add(Box.createVerticalStrut(3)); tx.add(t); tx.add(Box.createVerticalStrut(2)); tx.add(s);
        p.add(tx, BorderLayout.CENTER);
        return p;
    }

    // ——————————— KPI CARD ———————————
    private JPanel kpi(String icone, String label, String valeur, String sous, Color couleur) {
        JPanel c = new JPanel(new BorderLayout(0, 6)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, UIHelper.FOND_CARD, 0, getHeight(), UIHelper.FOND_CARD2);
                g2.setPaint(gp); g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.setColor(couleur);
                g2.fill(new RoundRectangle2D.Float(0, 0, 4, getHeight(), 4, 4));
                g2.dispose();
            }
        };
        c.setOpaque(false); c.setBorder(new EmptyBorder(18, 18, 18, 18));
        // Icone grande + label en dessous
        JLabel icoLbl = new JLabel(icone, SwingConstants.CENTER);
        icoLbl.setFont(new Font("Dialog", Font.BOLD, 30));
        icoLbl.setForeground(Color.WHITE);
        JLabel labelLbl = new JLabel(label, SwingConstants.CENTER);
        labelLbl.setFont(new Font("Dialog", Font.BOLD, 11)); labelLbl.setForeground(Color.WHITE);
        JPanel topKpi = new JPanel(new GridLayout(2,1,0,2)); topKpi.setOpaque(false);
        topKpi.add(icoLbl); topKpi.add(labelLbl);
        JLabel val = new JLabel(valeur, SwingConstants.CENTER);
        val.setFont(new Font("Dialog", Font.BOLD, 36)); val.setForeground(couleur);
        JLabel s = new JLabel(sous, SwingConstants.CENTER);
        s.setFont(new Font("Dialog", Font.PLAIN, 11)); s.setForeground(UIHelper.TEXTE_GRIS2);
        c.add(topKpi, BorderLayout.NORTH); c.add(val, BorderLayout.CENTER); c.add(s, BorderLayout.SOUTH);
        return c;
    }

    private JPanel carte(String titre) {
        JPanel c = new JPanel(new BorderLayout(0, 12)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIHelper.FOND_CARD);
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                g2.dispose();
            }
        };
        c.setOpaque(false); c.setBorder(new EmptyBorder(18, 18, 18, 18));
        if (titre != null) {
            JLabel lbl = new JLabel(titre);
            lbl.setFont(new Font("Dialog", Font.BOLD, 14)); lbl.setForeground(Color.WHITE);
            lbl.setBorder(new EmptyBorder(0, 0, 8, 0));
            c.add(lbl, BorderLayout.NORTH);
        }
        return c;
    }

    // ——————————— ACCUEIL ———————————
    private JPanel panelAccueil() {
        JPanel p = new JPanel(new BorderLayout(0, 0));
        p.setBackground(UIHelper.FOND_DARK);
        p.add(enTete("Tableau de bord", "Vue d'ensemble de l'activité pédagogique — ESITEC", "Tableau de bord"), BorderLayout.NORTH);

        JPanel corps = new JPanel(new BorderLayout(20, 20));
        corps.setBackground(UIHelper.FOND_DARK); corps.setBorder(new EmptyBorder(22, 22, 22, 22));

        List<Utilisateur> ens = utilisateurDAO.listerParRole(Utilisateur.Role.ENSEIGNANT);
        List<Cours>   cours   = coursDAO.listerTous();
        List<Seance>  seances = seanceDAO.listerToutes();
        long valides  = seances.stream().filter(s -> s.getStatut() == Seance.Statut.VALIDE).count();
        long attente  = seances.stream().filter(s -> s.getStatut() == Seance.Statut.EN_ATTENTE).count();
        long rejetes  = seances.stream().filter(s -> s.getStatut() == Seance.Statut.REJETE).count();

        JPanel kpis = new JPanel(new GridLayout(1, 5, 14, 0));
        kpis.setOpaque(false);
        kpis.add(kpi("👨‍🏫", "Enseignants",  String.valueOf(ens.size()),   "validés",     UIHelper.BLEU_ACCENT));
        kpis.add(kpi("📖",  "Cours",        String.valueOf(cours.size()), "assignés",    UIHelper.VERT_BADGE));
        kpis.add(kpi("✅",  "Validées",     String.valueOf(valides),      "séances",     UIHelper.CYAN_ACCENT));
        kpis.add(kpi("⏳",  "En attente",   String.valueOf(attente),      "à valider",   UIHelper.ORANGE_BADGE));
        kpis.add(kpi("❌",  "Rejetées",     String.valueOf(rejetes),      "séances",     UIHelper.ROUGE_BADGE));
        corps.add(kpis, BorderLayout.NORTH);

        JPanel carteSeances = carte("📋  Dernières séances enregistrées");
        String[] cols = {"Cours", "Classe", "Enseignant", "Date", "Durée", "Statut"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(mdl);
        UIHelper.styliserTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(5).setCellRenderer(new TableRenderers.StatutCellRenderer());
        int lim = Math.min(seances.size(), 15);
        for (int i = 0; i < lim; i++) {
            Seance s = seances.get(i);
            mdl.addRow(new Object[]{ s.getCoursIntitule(), s.getClasseNom(), s.getNomEnseignant(),
                    s.getDate(), s.getDureeFormatee(), s.getStatut().name() });
        }
        carteSeances.add(UIHelper.creerScrollPane(table), BorderLayout.CENTER);
        corps.add(carteSeances, BorderLayout.CENTER);
        p.add(corps, BorderLayout.CENTER);
        return p;
    }

    // ——————————— UTILISATEURS ———————————
    private JPanel panelUtilisateurs() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.FOND_DARK);
        p.add(enTete("Gestion des utilisateurs",
                "Valider les comptes, ajouter enseignants et responsables de classe", "Utilisateurs"), BorderLayout.NORTH);

        JPanel corps = new JPanel(new BorderLayout(0, 14));
        corps.setBackground(UIHelper.FOND_DARK); corps.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton btnAjouter  = UIHelper.creerBoutonSucces("+ Ajouter un utilisateur");
        JButton btnModifier = UIHelper.creerBouton("✏  Modifier", new Color(0, 100, 180));
        JButton btnValider  = UIHelper.creerBouton("✓  Valider", new Color(0,140,70));
        JButton btnRejeter  = UIHelper.creerBouton("✗  Rejeter", new Color(180,100,0));
        JButton btnSuppr    = UIHelper.creerBoutonDanger("🗑  Supprimer");
        JButton btnRefresh  = UIHelper.creerBouton("↻  Actualiser", new Color(50,70,110));
        // Champ de recherche
        JTextField champRecherche = UIHelper.creerChamp("Rechercher...");
        champRecherche.setPreferredSize(new Dimension(240, 34));
        JButton btnFiltrer = UIHelper.creerBoutonPrimaire("Filtrer");
        toolbar.add(btnAjouter); toolbar.add(btnModifier); toolbar.add(btnValider);
        toolbar.add(btnRejeter); toolbar.add(btnSuppr); toolbar.add(btnRefresh);
        toolbar.add(champRecherche); toolbar.add(btnFiltrer);
        corps.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Nom", "Prénom", "Email", "Rôle", "Statut"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(mdl);
        UIHelper.styliserTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setCellRenderer(new TableRenderers.StatutCellRenderer());

        Runnable charger = () -> {
            mdl.setRowCount(0);
            String filtre = UIHelper.getFiltre(champRecherche);
            for (Utilisateur u : utilisateurDAO.listerTous()) {
                if (!filtre.isEmpty()) {
                    String concat = (u.getNom() + " " + u.getPrenom() + " " + u.getEmail()
                        + " " + u.getRole().name() + " " + u.getStatut().name()).toLowerCase();
                    if (!concat.contains(filtre)) continue;
                }
                mdl.addRow(new Object[]{ u.getId(), u.getNom(), u.getPrenom(),
                        u.getEmail(), u.getRole().name(), u.getStatut().name() });
            }
        };
        charger.run();
        // Filtrage dynamique (touche par touche)
        champRecherche.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
        });
        btnFiltrer.addActionListener(e -> charger.run());

        btnValider.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.afficherErreur(p, "Sélectionnez un utilisateur."); return; }
            utilisateurDAO.changerStatut((int) mdl.getValueAt(r, 0), Utilisateur.Statut.VALIDE);
            UIHelper.afficherSucces(p, "Compte validé ✓"); charger.run();
        });
        btnRejeter.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.afficherErreur(p, "Sélectionnez un utilisateur."); return; }
            utilisateurDAO.changerStatut((int) mdl.getValueAt(r, 0), Utilisateur.Statut.REJETE);
            UIHelper.afficherSucces(p, "Compte rejeté."); charger.run();
        });
        btnSuppr.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.afficherErreur(p, "Sélectionnez un utilisateur à supprimer."); return; }
            int id = (int) mdl.getValueAt(r, 0);
            String nom = mdl.getValueAt(r, 2) + " " + mdl.getValueAt(r, 1);
            if (UIHelper.confirmer(p, "Supprimer définitivement " + nom + " ?\n"
                + "⚠ Ses cours et séances seront aussi supprimés.") == JOptionPane.YES_OPTION) {
                boolean ok = utilisateurDAO.supprimer(id);
                if (ok) {
                    UIHelper.afficherSucces(p, "Utilisateur supprimé avec succès ✓");
                } else {
                    UIHelper.afficherErreur(p, "Erreur lors de la suppression.\nVeuillez réessayer.");
                }
                charger.run();
            }
        });
        btnAjouter.addActionListener(e -> {
            new DialogAjouterUtilisateur(this, utilisateurDAO, charger).setVisible(true);
        });
        btnModifier.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.afficherErreur(p, "Sélectionnez un utilisateur à modifier."); return; }
            int id = (int) mdl.getValueAt(r, 0);
            Utilisateur u = utilisateurDAO.listerTous().stream()
                .filter(x -> x.getId() == id).findFirst().orElse(null);
            if (u != null) {
                com.esitec.cahier.ui.admin.DialogModifierUtilisateur dlg =
                    new com.esitec.cahier.ui.admin.DialogModifierUtilisateur(
                        (Frame) SwingUtilities.getWindowAncestor(p), u, utilisateurDAO, charger);
                dlg.setVisible(true);
                charger.run(); // rafraîchir après fermeture
            }
        });
        btnRefresh.addActionListener(e -> charger.run());

        corps.add(UIHelper.creerScrollPane(table), BorderLayout.CENTER);
        p.add(corps, BorderLayout.CENTER);
        return p;
    }

    // ——————————— COURS ———————————
    private JPanel panelCours() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.FOND_DARK);
        p.add(enTete("Gestion des cours",
                "Créer, assigner et exporter les fiches de suivi pédagogique", "Cours"), BorderLayout.NORTH);

        JPanel corps = new JPanel(new BorderLayout(0, 14));
        corps.setBackground(UIHelper.FOND_DARK); corps.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton btnAjouter  = UIHelper.creerBoutonSucces("+ Nouveau cours");
        JButton btnModifier = UIHelper.creerBouton("✏  Modifier", new Color(30,90,180));
        JButton btnSuppr    = UIHelper.creerBoutonDanger("🗑  Supprimer");
        JButton btnPDF      = UIHelper.creerBouton("📄  Fiche PDF",   new Color(160,40,40));
        JButton btnXLS      = UIHelper.creerBouton("📊  Fiche Excel", new Color(25,115,60));
        JButton btnRefresh  = UIHelper.creerBouton("↻  Actualiser", new Color(50,70,110));
        JTextField champRecherche = UIHelper.creerChamp("Rechercher...");
        champRecherche.setPreferredSize(new Dimension(270, 34));
        JButton btnFiltrer = UIHelper.creerBoutonPrimaire("Filtrer");
        toolbar.add(btnAjouter); toolbar.add(btnModifier); toolbar.add(btnSuppr);
        toolbar.add(btnPDF); toolbar.add(btnXLS); toolbar.add(btnRefresh);
        toolbar.add(champRecherche); toolbar.add(btnFiltrer);
        corps.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Intitulé", "Classe", "Enseignant", "Prévu", "Effectué", "Avancement"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(mdl);
        UIHelper.styliserTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(6).setCellRenderer(new TableRenderers.ProgressCellRenderer());

        Runnable charger = () -> {
            mdl.setRowCount(0);
            String filtre = UIHelper.getFiltre(champRecherche);
            for (Cours c : coursDAO.listerTous()) {
                if (!filtre.isEmpty()) {
                    String concat = (c.getIntitule() + " " + c.getClasseNom()
                        + " " + c.getNomEnseignant()).toLowerCase();
                    if (!concat.contains(filtre)) continue;
                }
                mdl.addRow(new Object[]{ c.getId(), c.getIntitule(), c.getClasseNom(),
                        c.getNomEnseignant(), c.getVolumeHorairePrevu() + "h",
                        c.getVolumeHoraireEffectue() + "h",
                        String.format("%.0f%%", c.getPourcentageAvancement()) });
            }
        };
        charger.run();
        champRecherche.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
        });
        btnFiltrer.addActionListener(e -> charger.run());

        btnAjouter.addActionListener(e -> {
            new DialogCours(this, null, utilisateurDAO, coursDAO).setVisible(true); charger.run();
        });
        btnModifier.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.afficherErreur(p, "Sélectionnez un cours."); return; }
            int id = (int) mdl.getValueAt(r, 0);
            coursDAO.listerTous().stream().filter(c -> c.getId() == id).findFirst().ifPresent(c -> {
                new DialogCours(this, c, utilisateurDAO, coursDAO).setVisible(true); charger.run();
            });
        });
        btnSuppr.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.afficherErreur(p, "Sélectionnez un cours."); return; }
            if (UIHelper.confirmer(p, "Supprimer ce cours et toutes ses séances ?") == JOptionPane.YES_OPTION) {
                coursDAO.supprimer((int) mdl.getValueAt(r, 0)); charger.run();
            }
        });
        btnPDF.addActionListener(e -> exporterFiche(p, table, mdl, "pdf"));
        btnXLS.addActionListener(e -> exporterFiche(p, table, mdl, "excel"));
        btnRefresh.addActionListener(e -> charger.run());

        corps.add(UIHelper.creerScrollPane(table), BorderLayout.CENTER);
        p.add(corps, BorderLayout.CENTER);
        return p;
    }

    private void exporterFiche(JPanel parent, JTable table, DefaultTableModel mdl, String fmt) {
        int r = table.getSelectedRow();
        if (r < 0) { UIHelper.afficherErreur(parent, "Sélectionnez un cours dans le tableau."); return; }
        // Convertir l'index visuel en index réel du modèle (important si tri/filtre actif)
        int modelRow = table.convertRowIndexToModel(r);
        int id = (int) mdl.getValueAt(modelRow, 0);
        coursDAO.listerTous().stream().filter(c -> c.getId() == id).findFirst().ifPresent(cours -> {
            // Séances uniquement pour CE cours
            List<Seance> seances = seanceDAO.listerParCours(cours.getId());
            JFileChooser fc = new JFileChooser();
            String ext = fmt.equals("pdf") ? ".pdf" : ".xlsx";
            fc.setSelectedFile(new File("fiche_" + cours.getIntitule().replaceAll("\\s+", "_") + ext));
            fc.setDialogTitle("Générer la fiche de suivi pédagogique");
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    String ch = fc.getSelectedFile().getAbsolutePath();
                    if (fmt.equals("pdf")) ExportPDF.genererFicheSuivi(cours, seances, ch);
                    else                   ExportExcel.genererFicheSuivi(cours, seances, ch);
                    UIHelper.afficherSucces(this, "Fiche de suivi générée avec succès ✓\n" + ch);
                } catch (Exception ex) {
                    UIHelper.afficherErreur(this, "Erreur lors de la génération : " + ex.getMessage());
                }
            }
        });
    }

    // ——————————— SÉANCES ———————————
    private JPanel panelSeances() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.FOND_DARK);
        p.add(enTete("Toutes les séances", "Historique complet de toutes les séances", "Séances"), BorderLayout.NORTH);

        JPanel corps = new JPanel(new BorderLayout(0, 14));
        corps.setBackground(UIHelper.FOND_DARK); corps.setBorder(new EmptyBorder(18, 22, 18, 22));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton btnRefresh = UIHelper.creerBouton("↻  Actualiser", new Color(50,70,110));
        JTextField champRecherche = UIHelper.creerChamp("Rechercher...");
        champRecherche.setPreferredSize(new Dimension(270, 34));
        JComboBox<String> comboStatut = new JComboBox<>(new String[]{"— Tous les statuts —","VALIDE","EN_ATTENTE","REJETE"});
        UIHelper.styliserCombo(comboStatut);
        comboStatut.setPreferredSize(new Dimension(180, 34));
        JButton btnFiltrer = UIHelper.creerBoutonPrimaire("Filtrer");
        toolbar.add(btnRefresh); toolbar.add(champRecherche);
        toolbar.add(comboStatut); toolbar.add(btnFiltrer);
        corps.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Cours", "Classe", "Enseignant", "Date", "Heure", "Durée", "Statut"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(mdl);
        UIHelper.styliserTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(7).setCellRenderer(new TableRenderers.StatutCellRenderer());

        Runnable charger = () -> {
            mdl.setRowCount(0);
            String filtre = UIHelper.getFiltre(champRecherche);
            String statFil = (String) comboStatut.getSelectedItem();
            for (Seance s : seanceDAO.listerToutes()) {
                if (!filtre.isEmpty()) {
                    String concat = (s.getCoursIntitule() + " " + s.getClasseNom()
                        + " " + s.getNomEnseignant()).toLowerCase();
                    if (!concat.contains(filtre)) continue;
                }
                if (statFil != null && !statFil.startsWith("—") && !s.getStatut().name().equals(statFil)) continue;
                mdl.addRow(new Object[]{ s.getId(), s.getCoursIntitule(), s.getClasseNom(),
                        s.getNomEnseignant(), s.getDate(), s.getHeure(),
                        s.getDureeFormatee(), s.getStatut().name() });
            }
        };
        charger.run();
        champRecherche.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
        });
        comboStatut.addActionListener(e -> charger.run());
        btnFiltrer.addActionListener(e -> charger.run());
        btnRefresh.addActionListener(e -> { champRecherche.setText(""); comboStatut.setSelectedIndex(0); charger.run(); });
        corps.add(UIHelper.creerScrollPane(table), BorderLayout.CENTER);
        p.add(corps, BorderLayout.CENTER);
        return p;
    }

    // ——————————— STATISTIQUES ———————————
    private JPanel panelStatistiques() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.FOND_DARK);
        p.add(enTete("Statistiques globales", "Vue analytique complète de l'activité pédagogique", "Statistiques"), BorderLayout.NORTH);

        JPanel corps = new JPanel(new BorderLayout(20, 20));
        corps.setBackground(UIHelper.FOND_DARK); corps.setBorder(new EmptyBorder(22, 22, 22, 22));

        List<Cours>  cours   = coursDAO.listerTous();
        List<Seance> seances = seanceDAO.listerToutes();
        List<Utilisateur> tous = utilisateurDAO.listerTous();
        long enseignants   = tous.stream().filter(u -> u.getRole() == Utilisateur.Role.ENSEIGNANT).count();
        long responsables  = tous.stream().filter(u -> u.getRole() == Utilisateur.Role.RESPONSABLE_CLASSE).count();
        long valides  = seances.stream().filter(s -> s.getStatut() == Seance.Statut.VALIDE).count();
        long attente  = seances.stream().filter(s -> s.getStatut() == Seance.Statut.EN_ATTENTE).count();
        long rejetes  = seances.stream().filter(s -> s.getStatut() == Seance.Statut.REJETE).count();
        long classes  = coursDAO.listerClasses().size();

        JPanel grid = new JPanel(new GridLayout(2, 4, 14, 14));
        grid.setOpaque(false);
        grid.add(kpi("👨‍🏫", "Enseignants",    String.valueOf(enseignants),  "comptes",     UIHelper.BLEU_ACCENT));
        grid.add(kpi("📋",  "Responsables",   String.valueOf(responsables), "comptes",     UIHelper.CYAN_ACCENT));
        grid.add(kpi("📖",  "Cours",          String.valueOf(cours.size()), "assignés",    UIHelper.VERT_BADGE));
        grid.add(kpi("🏫",  "Classes",        String.valueOf(classes),      "actives",     UIHelper.JAUNE_BADGE));
        grid.add(kpi("📝",  "Total séances",  String.valueOf(seances.size()), "enregistrées", new Color(130,80,220)));
        grid.add(kpi("✅",  "Validées",       String.valueOf(valides),      "approuvées",  UIHelper.VERT_BADGE));
        grid.add(kpi("⏳",  "En attente",     String.valueOf(attente),      "à valider",   UIHelper.ORANGE_BADGE));
        grid.add(kpi("❌",  "Rejetées",       String.valueOf(rejetes),      "séances",     UIHelper.ROUGE_BADGE));
        corps.add(grid, BorderLayout.NORTH);

        // Tableau d'avancement par cours
        JPanel carteAvanc = carte("📊  Avancement par cours");
        String[] cols = {"Cours", "Classe", "Enseignant", "Heures prévues", "Heures effectuées", "Avancement"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(mdl);
        UIHelper.styliserTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(5).setCellRenderer(new TableRenderers.ProgressCellRenderer());
        for (Cours c : cours)
            mdl.addRow(new Object[]{ c.getIntitule(), c.getClasseNom(), c.getNomEnseignant(),
                    c.getVolumeHorairePrevu() + "h", c.getVolumeHoraireEffectue() + "h",
                    String.format("%.0f%%", c.getPourcentageAvancement()) });
        carteAvanc.add(UIHelper.creerScrollPane(table), BorderLayout.CENTER);
        corps.add(carteAvanc, BorderLayout.CENTER);
        p.add(corps, BorderLayout.CENTER);
        return p;
    }
}
