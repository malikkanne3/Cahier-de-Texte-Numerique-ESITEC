package com.esitec.cahier.ui.responsable;

import com.esitec.cahier.dao.CoursDAO;
import com.esitec.cahier.dao.SeanceDAO;
import com.esitec.cahier.model.Cours;
import com.esitec.cahier.model.Seance;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.ui.common.Sidebar;
import com.esitec.cahier.util.ExportExcel;
import com.esitec.cahier.util.ExportPDF;
import com.esitec.cahier.util.TableRenderers;
import com.esitec.cahier.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import java.util.List;

/**
 * Dashboard Responsable de classe — fonctionnalités du cahier des charges :
 * - Consulter le cahier de texte de sa classe
 * - Valider ou rejeter le contenu ajouté par l'enseignant
 * - Ajouter des commentaires lors du rejet
 * - Visualiser l'état d'avancement du programme
 */
public class DashboardResponsable extends JFrame {

    private final Utilisateur utilisateur;
    private final CoursDAO    coursDAO  = new CoursDAO();
    private final SeanceDAO   seanceDAO = new SeanceDAO();

    private JPanel     contenu;
    private CardLayout cardLayout;

    public DashboardResponsable(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        initialiserUI();
    }

    private void initialiserUI() {
        setTitle("Espace Responsable de Classe — Cahier de Texte ESITEC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1050, 650));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIHelper.FOND_DARK);
        Sidebar sidebar = new Sidebar(utilisateur);

        cardLayout = new CardLayout();
        contenu = new JPanel(cardLayout);
        contenu.setBackground(UIHelper.FOND_DARK);

        contenu.add(panelCahier(),     "cahier");
        contenu.add(panelValidation(), "validation");
        contenu.add(panelAvancement(), "avancement");

        JButton b0 = sidebar.addItem("📖", "Cahier de texte",       e -> cardLayout.show(contenu, "cahier"));
        sidebar.addSeparateur("Actions");
        JButton b1 = sidebar.addItem("✅", "Validation séances",    e -> cardLayout.show(contenu, "validation"));
        sidebar.addSeparateur("Analyse");
        JButton b2 = sidebar.addItem("📊", "Avancement programme",  e -> cardLayout.show(contenu, "avancement"));

        sidebar.setActif(b0);
        root.add(sidebar, BorderLayout.WEST);
        root.add(contenu, BorderLayout.CENTER);
        add(root);
    }

    private JPanel enTete(String titre, String sous, String bc) {
        JPanel p = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIHelper.FOND_CARD); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(UIHelper.BORDURE); g.fillRect(0,getHeight()-1,getWidth(),1);
            }
        };
        p.setOpaque(false); p.setBorder(new EmptyBorder(18,28,18,28));
        JLabel bclbl = new JLabel("Accueil  /  "+bc);
        bclbl.setFont(new Font("Dialog",Font.PLAIN,11)); bclbl.setForeground(UIHelper.TEXTE_GRIS2);
        JLabel t = new JLabel(titre);
        t.setFont(new Font("Dialog",Font.BOLD,22)); t.setForeground(Color.WHITE);
        JLabel s = new JLabel(sous);
        s.setFont(new Font("Dialog",Font.PLAIN,13)); s.setForeground(UIHelper.TEXTE_GRIS);
        JPanel tx = new JPanel(); tx.setLayout(new BoxLayout(tx,BoxLayout.Y_AXIS)); tx.setOpaque(false);
        tx.add(bclbl); tx.add(Box.createVerticalStrut(3)); tx.add(t); tx.add(Box.createVerticalStrut(2)); tx.add(s);
        p.add(tx, BorderLayout.CENTER);
        return p;
    }

    // ——————————— CAHIER DE TEXTE ———————————
    private JPanel panelCahier() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.FOND_DARK);
        p.add(enTete("Cahier de texte", "Consultez toutes les séances de votre classe", "Cahier de texte"), BorderLayout.NORTH);

        JPanel corps = new JPanel(new BorderLayout(0, 14));
        corps.setBackground(UIHelper.FOND_DARK); corps.setBorder(new EmptyBorder(18,22,18,22));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JLabel lblClasse = new JLabel("Classe :");
        lblClasse.setForeground(UIHelper.TEXTE_GRIS); lblClasse.setFont(new Font("Dialog",Font.PLAIN,13));
        toolbar.add(lblClasse);

        List<String> classes = coursDAO.listerClasses();
        List<String> avecTous = new java.util.ArrayList<>(); avecTous.add("— Toutes les classes —"); avecTous.addAll(classes);
        JComboBox<String> combo = new JComboBox<>(avecTous.toArray(String[]::new));
        UIHelper.styliserCombo(combo);
        combo.setPreferredSize(new Dimension(190,34));
        toolbar.add(combo);

        JTextField champRech = UIHelper.creerChamp("Rechercher...");
        champRech.setPreferredSize(new Dimension(230, 34));
        JComboBox<String> comboStat = new JComboBox<>(new String[]{"— Tous statuts —","VALIDE","EN_ATTENTE","REJETE"});
        UIHelper.styliserCombo(comboStat);
        comboStat.setPreferredSize(new Dimension(170, 34));
        JButton btnF      = UIHelper.creerBoutonPrimaire("Filtrer");
        JButton btnPDF    = UIHelper.creerBouton("📄  PDF",     new Color(160,40,40));
        JButton btnXLS    = UIHelper.creerBouton("📊  Excel",   new Color(25,115,60));
        JButton btnRefresh = UIHelper.creerBouton("↻  Actualiser", new Color(50,70,110));
        toolbar.add(champRech); toolbar.add(comboStat);
        toolbar.add(btnF); toolbar.add(btnPDF); toolbar.add(btnXLS); toolbar.add(btnRefresh);
        corps.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID","Cours","Classe","Enseignant","Date","Heure","Durée","Contenu","Statut"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        JTable table = new JTable(mdl);
        UIHelper.styliserTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(8).setCellRenderer(new TableRenderers.StatutCellRenderer());

        Runnable charger = () -> {
            mdl.setRowCount(0);
            String cl = (String) combo.getSelectedItem();
            String filtre = UIHelper.getFiltre(champRech);
            String statFil = (String) comboStat.getSelectedItem();
            List<Seance> seances = (cl == null || cl.startsWith("—"))
                    ? seanceDAO.listerToutes() : seanceDAO.listerParClasse(cl);
            for (Seance s : seances) {
                if (!filtre.isEmpty()) {
                    String concat = (s.getCoursIntitule() + " " + s.getNomEnseignant()
                        + " " + s.getClasseNom()).toLowerCase();
                    if (!concat.contains(filtre)) continue;
                }
                if (statFil != null && !statFil.startsWith("—") && !s.getStatut().name().equals(statFil)) continue;
                String cont = s.getContenu();
                if (cont != null && cont.length() > 50) cont = cont.substring(0,50) + "…";
                mdl.addRow(new Object[]{ s.getId(), s.getCoursIntitule(), s.getClasseNom(),
                        s.getNomEnseignant(), s.getDate(), s.getHeure(),
                        s.getDureeFormatee(), cont, s.getStatut().name() });
            }
        };
        charger.run();
        champRech.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
        });
        comboStat.addActionListener(e -> charger.run());
        btnF.addActionListener(e -> charger.run());
        btnRefresh.addActionListener(e -> { champRech.setText(""); comboStat.setSelectedIndex(0); charger.run(); });
        btnPDF.addActionListener(e -> {
            String cl = (String) combo.getSelectedItem();
            if (cl == null || cl.startsWith("—")) { UIHelper.afficherErreur(p, "Sélectionnez une classe."); return; }
            exporterClasse(cl, "pdf");
        });
        btnXLS.addActionListener(e -> {
            String cl = (String) combo.getSelectedItem();
            if (cl == null || cl.startsWith("—")) { UIHelper.afficherErreur(p, "Sélectionnez une classe."); return; }
            exporterClasse(cl, "excel");
        });

        corps.add(UIHelper.creerScrollPane(table), BorderLayout.CENTER);
        p.add(corps, BorderLayout.CENTER);
        return p;
    }

    private void exporterClasse(String cl, String fmt) {
        List<Cours> listeCours = coursDAO.listerParClasse(cl);
        if (listeCours.isEmpty()) { UIHelper.afficherErreur(this, "Aucun cours trouvé pour cette classe."); return; }

        // Demander un dossier de destination (un fichier par cours)
        JFileChooser fc = new JFileChooser();
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Choisir le dossier de destination pour les fiches");
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        String dossier = fc.getSelectedFile().getAbsolutePath();

        String ext = fmt.equals("pdf") ? ".pdf" : ".xlsx";
        int nbGeneres = 0;
        StringBuilder erreurs = new StringBuilder();

        for (Cours cours : listeCours) {
            // Séances UNIQUEMENT pour ce cours précis
            List<Seance> seancesCours = seanceDAO.listerParCours(cours.getId());
            String nomFichier = dossier + java.io.File.separator
                + "fiche_" + cours.getIntitule().replaceAll("[^a-zA-Z0-9_-]","_") + ext;
            try {
                if (fmt.equals("pdf")) ExportPDF.genererFicheSuivi(cours, seancesCours, nomFichier);
                else                   ExportExcel.genererFicheSuivi(cours, seancesCours, nomFichier);
                nbGeneres++;
            } catch (Exception ex) {
                erreurs.append("\n• ").append(cours.getIntitule()).append(" : ").append(ex.getMessage());
            }
        }

        if (erreurs.length() == 0) {
            UIHelper.afficherSucces(this, nbGeneres + " fiche(s) générée(s) dans :\n" + dossier);
        } else {
            UIHelper.afficherErreur(this, "Partiellement généré (" + nbGeneres + "/" + listeCours.size() + ")\n" + erreurs);
        }
    }

    // ——————————— VALIDATION ———————————
    private JPanel panelValidation() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.FOND_DARK);
        p.add(enTete("Validation des séances",
                "Approuvez ou rejetez les séances soumises par les enseignants", "Validation"), BorderLayout.NORTH);

        JPanel corps = new JPanel(new BorderLayout(0, 14));
        corps.setBackground(UIHelper.FOND_DARK); corps.setBorder(new EmptyBorder(18,22,18,22));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JLabel lblClasse = new JLabel("Classe :");
        lblClasse.setForeground(UIHelper.TEXTE_GRIS); lblClasse.setFont(new Font("Dialog",Font.PLAIN,13));
        toolbar.add(lblClasse);

        List<String> classes = coursDAO.listerClasses();
        JComboBox<String> comboV = new JComboBox<>(classes.isEmpty()
                ? new String[]{"(aucune classe)"} : classes.toArray(String[]::new));
        UIHelper.styliserCombo(comboV);
        comboV.setPreferredSize(new Dimension(190,34));
        toolbar.add(comboV);

        JButton btnVal    = UIHelper.creerBouton("✓  Valider",       new Color(0,140,70));
        JButton btnRej    = UIHelper.creerBoutonDanger("✗  Rejeter");
        JButton btnRefresh = UIHelper.creerBouton("↻  Actualiser",   new Color(50,70,110));
        toolbar.add(btnVal); toolbar.add(btnRej); toolbar.add(btnRefresh);
        corps.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID","Cours","Classe","Enseignant","Date","Heure","Durée","Contenu"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        JTable table = new JTable(mdl);
        UIHelper.styliserTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(45);

        Runnable charger = () -> {
            mdl.setRowCount(0);
            String cl = (String) comboV.getSelectedItem();
            if (cl == null || cl.startsWith("(")) return;
            for (Seance s : seanceDAO.listerEnAttente(cl)) {
                String cont = s.getContenu();
                if (cont != null && cont.length() > 60) cont = cont.substring(0,60) + "…";
                mdl.addRow(new Object[]{ s.getId(), s.getCoursIntitule(), s.getClasseNom(),
                        s.getNomEnseignant(), s.getDate(), s.getHeure(), s.getDureeFormatee(), cont });
            }
        };
        if (!classes.isEmpty()) charger.run();
        comboV.addActionListener(e -> charger.run());

        btnVal.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.afficherErreur(p, "Sélectionnez une séance à valider."); return; }
            if (seanceDAO.valider((int) mdl.getValueAt(r, 0))) {
                UIHelper.afficherSucces(p, "Séance validée avec succès ✓"); charger.run();
            }
        });
        btnRej.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.afficherErreur(p, "Sélectionnez une séance à rejeter."); return; }
            // Saisie du commentaire de rejet (obligatoire)
            JPanel motifPanel = new JPanel(new BorderLayout(0,8));
            motifPanel.setBackground(UIHelper.FOND_CARD);
            JLabel lbl = new JLabel("Motif du rejet (obligatoire) :");
            lbl.setFont(new Font("Dialog",Font.BOLD,13)); lbl.setForeground(Color.WHITE);
            JTextArea ta = new JTextArea(4, 30);
            ta.setFont(new Font("Dialog",Font.PLAIN,12));
            ta.setBackground(UIHelper.FOND_CARD2); ta.setForeground(UIHelper.TEXTE_BLANC);
            ta.setLineWrap(true); ta.setWrapStyleWord(true);
            JScrollPane sp = new JScrollPane(ta);
            UIHelper.rendreFluid(sp);
            motifPanel.add(lbl, BorderLayout.NORTH);
            motifPanel.add(sp, BorderLayout.CENTER);

            int res = JOptionPane.showConfirmDialog(this, motifPanel, "Rejeter la séance",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (res == JOptionPane.OK_OPTION) {
                String motif = ta.getText().trim();
                if (motif.isEmpty()) { UIHelper.afficherErreur(p, "Le motif du rejet est obligatoire."); return; }
                if (seanceDAO.rejeter((int) mdl.getValueAt(r, 0), motif)) {
                    UIHelper.afficherSucces(p, "Séance rejetée. Commentaire envoyé à l'enseignant."); charger.run();
                }
            }
        });
        btnRefresh.addActionListener(e -> charger.run());

        corps.add(UIHelper.creerScrollPane(table), BorderLayout.CENTER);

        JLabel info = new JLabel("  ℹ️  Seules les séances EN_ATTENTE sont affichées ici. Le motif du rejet est obligatoire.");
        info.setFont(new Font("Dialog",Font.ITALIC,11)); info.setForeground(UIHelper.TEXTE_GRIS2);
        corps.add(info, BorderLayout.SOUTH);
        p.add(corps, BorderLayout.CENTER);
        return p;
    }

    // ——————————— AVANCEMENT ———————————
    private JPanel panelAvancement() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.FOND_DARK);
        p.add(enTete("Avancement du programme",
                "Visualisez la progression des cours par classe", "Avancement"), BorderLayout.NORTH);

        JPanel corps = new JPanel(new BorderLayout(0, 14));
        corps.setBackground(UIHelper.FOND_DARK); corps.setBorder(new EmptyBorder(18,22,18,22));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);

        JLabel lblClasse = new JLabel("Classe :");
        lblClasse.setForeground(UIHelper.TEXTE_GRIS); lblClasse.setFont(new Font("Dialog",Font.PLAIN,13));
        toolbar.add(lblClasse);

        List<String> classes = coursDAO.listerClasses();
        List<String> avecTous = new java.util.ArrayList<>(); avecTous.add("— Toutes —"); avecTous.addAll(classes);
        JComboBox<String> comboA = new JComboBox<>(avecTous.toArray(String[]::new));
        UIHelper.styliserCombo(comboA);
        comboA.setPreferredSize(new Dimension(190,34));
        toolbar.add(comboA);

        JTextField champRechA = UIHelper.creerChamp("Rechercher...");
        champRechA.setPreferredSize(new Dimension(240, 34));
        JButton btnF = UIHelper.creerBoutonPrimaire("Filtrer");
        toolbar.add(champRechA); toolbar.add(btnF);
        corps.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"Cours","Classe","Enseignant","Volume prévu","Effectué","Avancement"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r,int c){return false;} };
        JTable table = new JTable(mdl);
        UIHelper.styliserTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(5).setCellRenderer(new TableRenderers.ProgressCellRenderer());

        Runnable charger = () -> {
            mdl.setRowCount(0);
            String cl = (String) comboA.getSelectedItem();
            String filtreA = UIHelper.getFiltre(champRechA);
            List<Cours> liste = (cl == null || cl.startsWith("—"))
                    ? coursDAO.listerTous() : coursDAO.listerParClasse(cl);
            for (Cours c : liste)
                mdl.addRow(new Object[]{ c.getIntitule(), c.getClasseNom(), c.getNomEnseignant(),
                        c.getVolumeHorairePrevu() + "h", c.getVolumeHoraireEffectue() + "h",
                        String.format("%.0f%%", c.getPourcentageAvancement()) });
        };
        charger.run();
        champRechA.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
        });
        btnF.addActionListener(e -> charger.run());
        comboA.addActionListener(e -> charger.run());

        corps.add(UIHelper.creerScrollPane(table), BorderLayout.CENTER);

        // KPI résumé en bas
        List<Cours> tousLesCours = coursDAO.listerTous();
        long termines = tousLesCours.stream().filter(c -> c.getPourcentageAvancement() >= 100).count();
        long enCours  = tousLesCours.stream().filter(c -> c.getPourcentageAvancement() > 0 && c.getPourcentageAvancement() < 100).count();
        long nonComm  = tousLesCours.stream().filter(c -> c.getPourcentageAvancement() == 0).count();
        JPanel kpis = new JPanel(new GridLayout(1, 3, 12, 0));
        kpis.setOpaque(false); kpis.setBorder(new EmptyBorder(12,0,0,0));
        kpis.add(kpi("✅","Terminés",   String.valueOf(termines), "cours", UIHelper.VERT_BADGE));
        kpis.add(kpi("🔄","En cours",   String.valueOf(enCours),  "cours", UIHelper.ORANGE_BADGE));
        kpis.add(kpi("⏸","Non commencés",String.valueOf(nonComm),"cours", UIHelper.TEXTE_GRIS));
        corps.add(kpis, BorderLayout.SOUTH);

        p.add(corps, BorderLayout.CENTER);
        return p;
    }

    private JPanel kpi(String icone, String label, String valeur, String sous, Color couleur) {
        JPanel c = new JPanel(new BorderLayout(0,5)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp=new GradientPaint(0,0,UIHelper.FOND_CARD,0,getHeight(),UIHelper.FOND_CARD2);
                g2.setPaint(gp); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),12,12));
                g2.setColor(couleur); g2.fill(new RoundRectangle2D.Float(0,0,4,getHeight(),4,4));
                g2.dispose();
            }
        };
        c.setOpaque(false); c.setBorder(new EmptyBorder(15,15,15,15));
        JLabel ico=new JLabel(icone+" "+label); ico.setFont(new Font("Dialog",Font.PLAIN,11)); ico.setForeground(UIHelper.TEXTE_GRIS);
        JLabel val=new JLabel(valeur,SwingConstants.CENTER); val.setFont(new Font("Dialog",Font.BOLD,34)); val.setForeground(couleur);
        JLabel s=new JLabel(sous,SwingConstants.CENTER); s.setFont(new Font("Dialog",Font.PLAIN,11)); s.setForeground(UIHelper.TEXTE_GRIS2);
        c.add(ico,BorderLayout.NORTH); c.add(val,BorderLayout.CENTER); c.add(s,BorderLayout.SOUTH);
        return c;
    }
}
