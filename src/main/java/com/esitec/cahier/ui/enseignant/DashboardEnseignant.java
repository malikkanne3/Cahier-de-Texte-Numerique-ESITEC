package com.esitec.cahier.ui.enseignant;

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
 * Dashboard Enseignant — fonctionnalités du cahier des charges :
 * - Voir la liste de ses cours
 * - Ajouter une séance (date, heure, durée, contenu, observations)
 * - Modifier ses séances (avant validation)
 * - Consulter l'historique de ses séances
 * - Générer une fiche de suivi pédagogique (PDF/Excel)
 */
public class DashboardEnseignant extends JFrame {

    private final Utilisateur utilisateur;
    private final CoursDAO    coursDAO  = new CoursDAO();
    private final SeanceDAO   seanceDAO = new SeanceDAO();

    private JPanel     contenu;
    private CardLayout cardLayout;

    public DashboardEnseignant(Utilisateur utilisateur) {
        this.utilisateur = utilisateur;
        initialiserUI();
    }

    private void initialiserUI() {
        setTitle("Espace Enseignant — Cahier de Texte ESITEC");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1050, 650));

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(UIHelper.FOND_DARK);
        Sidebar sidebar = new Sidebar(utilisateur);

        cardLayout = new CardLayout();
        contenu = new JPanel(cardLayout);
        contenu.setBackground(UIHelper.FOND_DARK);

        contenu.add(panelMesCours(),   "cours");
        contenu.add(panelMesSeances(), "seances");
        contenu.add(panelAjouter(),    "ajouter");

        JButton b0 = sidebar.addItem("📖", "Mes cours",        e -> cardLayout.show(contenu, "cours"));
        JButton b1 = sidebar.addItem("📋", "Mes séances",      e -> cardLayout.show(contenu, "seances"));
        sidebar.addSeparateur("Actions");
        JButton b2 = sidebar.addItem("➕", "Nouvelle séance",  e -> cardLayout.show(contenu, "ajouter"));

        sidebar.setActif(b0);
        root.add(sidebar, BorderLayout.WEST);
        root.add(contenu,  BorderLayout.CENTER);
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
        JLabel bclbl = new JLabel("Accueil  /  " + bc);
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

    // ——————————— MES COURS ———————————
    private JPanel panelMesCours() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.FOND_DARK);
        p.add(enTete("Mes cours", "Cours qui vous sont assignés par le chef de département", "Mes cours"), BorderLayout.NORTH);

        JPanel corps = new JPanel(new BorderLayout(0, 14));
        corps.setBackground(UIHelper.FOND_DARK); corps.setBorder(new EmptyBorder(18,22,18,22));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton btnFiche   = UIHelper.creerBouton("📄  Fiche PDF",   new Color(160,40,40));
        JButton btnXls     = UIHelper.creerBouton("📊  Fiche Excel", new Color(25,115,60));
        JButton btnRefresh = UIHelper.creerBouton("↻  Actualiser",   new Color(50,70,110));
        JTextField champRechCours = UIHelper.creerChamp("Rechercher un cours...");
        champRechCours.setPreferredSize(new Dimension(220, 34));
        JButton btnFiltrer = UIHelper.creerBoutonPrimaire("Filtrer");
        toolbar.add(btnFiche); toolbar.add(btnXls); toolbar.add(btnRefresh);
        toolbar.add(champRechCours); toolbar.add(btnFiltrer);
        corps.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Intitulé", "Classe", "Heures prévues", "Effectuées", "Avancement"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(mdl);
        UIHelper.styliserTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(5).setCellRenderer(new TableRenderers.ProgressCellRenderer());

        Runnable charger = () -> {
            mdl.setRowCount(0);
            String filtre = UIHelper.getFiltre(champRechCours);
            for (Cours co : coursDAO.listerParEnseignant(utilisateur.getId())) {
                if (!filtre.isEmpty()) {
                    String concat = (co.getIntitule() + " " + co.getClasseNom()).toLowerCase();
                    if (!concat.contains(filtre)) continue;
                }
                mdl.addRow(new Object[]{ co.getId(), co.getIntitule(), co.getClasseNom(),
                        co.getVolumeHorairePrevu() + "h", co.getVolumeHoraireEffectue() + "h",
                        String.format("%.0f%%", co.getPourcentageAvancement()) });
            }
        };
        charger.run();
        champRechCours.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
        });
        btnFiltrer.addActionListener(e -> charger.run());

        btnFiche.addActionListener(e -> exporterFiche(p, table, mdl, "pdf"));
        btnXls.addActionListener(e   -> exporterFiche(p, table, mdl, "excel"));
        btnRefresh.addActionListener(e -> charger.run());

        corps.add(UIHelper.creerScrollPane(table), BorderLayout.CENTER);

        // Info
        JLabel info = new JLabel("  ℹ️  Sélectionnez un cours pour générer sa fiche de suivi pédagogique.");
        info.setFont(new Font("Dialog",Font.ITALIC,11)); info.setForeground(UIHelper.TEXTE_GRIS2);
        corps.add(info, BorderLayout.SOUTH);
        p.add(corps, BorderLayout.CENTER);
        return p;
    }

    private void exporterFiche(JPanel parent, JTable table, DefaultTableModel mdl, String fmt) {
        int r = table.getSelectedRow();
        if (r < 0) { UIHelper.afficherErreur(parent, "Sélectionnez un cours dans le tableau."); return; }
        int modelRow = table.convertRowIndexToModel(r);
        int id = (int) mdl.getValueAt(modelRow, 0);
        coursDAO.listerParEnseignant(utilisateur.getId()).stream()
                .filter(co -> co.getId() == id).findFirst().ifPresent(cours -> {
            // Séances uniquement pour CE cours
            List<Seance> seances = seanceDAO.listerParCours(cours.getId());
            JFileChooser fc = new JFileChooser();
            String ext = fmt.equals("pdf") ? ".pdf" : ".xlsx";
            fc.setSelectedFile(new File("fiche_" + cours.getIntitule().replaceAll("\\s+","_") + ext));
            fc.setDialogTitle("Générer la fiche de suivi pédagogique");
            if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    String ch = fc.getSelectedFile().getAbsolutePath();
                    if (fmt.equals("pdf")) ExportPDF.genererFicheSuivi(cours, seances, ch);
                    else                   ExportExcel.genererFicheSuivi(cours, seances, ch);
                    UIHelper.afficherSucces(this, "Fiche générée avec succès ✓\n" + ch);
                } catch (Exception ex) {
                    UIHelper.afficherErreur(this, "Erreur : " + ex.getMessage());
                }
            }
        });
    }

    // ——————————— MES SÉANCES ———————————
    private JPanel panelMesSeances() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.FOND_DARK);
        p.add(enTete("Mes séances", "Historique complet de vos séances enregistrées", "Mes séances"), BorderLayout.NORTH);

        JPanel corps = new JPanel(new BorderLayout(0, 14));
        corps.setBackground(UIHelper.FOND_DARK); corps.setBorder(new EmptyBorder(18,22,18,22));

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        JButton btnMod    = UIHelper.creerBouton("✏  Modifier",    new Color(30,90,180));
        JButton btnSuppr  = UIHelper.creerBoutonDanger("🗑  Supprimer");
        JButton btnRefresh = UIHelper.creerBouton("↻  Actualiser", new Color(50,70,110));
        JTextField champRechSeance = UIHelper.creerChamp("Rechercher...");
        champRechSeance.setPreferredSize(new Dimension(220, 34));
        JComboBox<String> comboStatS = new JComboBox<>(new String[]{"— Tous statuts —","VALIDE","EN_ATTENTE","REJETE"});
        UIHelper.styliserCombo(comboStatS);
        comboStatS.setPreferredSize(new Dimension(170, 34));
        JButton btnFiltrerS = UIHelper.creerBoutonPrimaire("Filtrer");
        toolbar.add(btnMod); toolbar.add(btnSuppr); toolbar.add(btnRefresh);
        toolbar.add(champRechSeance); toolbar.add(comboStatS); toolbar.add(btnFiltrerS);
        corps.add(toolbar, BorderLayout.NORTH);

        String[] cols = {"ID", "Cours", "Classe", "Date", "Heure", "Durée", "Contenu", "Statut"};
        DefaultTableModel mdl = new DefaultTableModel(cols, 0) { @Override public boolean isCellEditable(int r, int c) { return false; } };
        JTable table = new JTable(mdl);
        UIHelper.styliserTable(table);
        table.setAutoCreateRowSorter(true);
        table.getColumnModel().getColumn(0).setMaxWidth(45);
        table.getColumnModel().getColumn(7).setCellRenderer(new TableRenderers.StatutCellRenderer());

        Runnable charger = () -> {
            mdl.setRowCount(0);
            String filtreS = UIHelper.getFiltre(champRechSeance);
            String statS = (String) comboStatS.getSelectedItem();
            for (Seance s : seanceDAO.listerParEnseignant(utilisateur.getId())) {
                if (!filtreS.isEmpty()) {
                    String concat = (s.getCoursIntitule() + " " + s.getClasseNom()
                        + " " + s.getDate().toString()).toLowerCase();
                    if (!concat.contains(filtreS)) continue;
                }
                if (statS != null && !statS.startsWith("—") && !s.getStatut().name().equals(statS)) continue;
                String contenu = s.getContenu();
                if (contenu != null && contenu.length() > 55) contenu = contenu.substring(0, 55) + "…";
                mdl.addRow(new Object[]{ s.getId(), s.getCoursIntitule(), s.getClasseNom(),
                        s.getDate(), s.getHeure(), s.getDureeFormatee(), contenu, s.getStatut().name() });
            }
        };
        charger.run();
        champRechSeance.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { charger.run(); }
        });
        comboStatS.addActionListener(e -> charger.run());
        btnFiltrerS.addActionListener(e -> charger.run());

        btnMod.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.afficherErreur(p, "Sélectionnez une séance."); return; }
            if (!"EN_ATTENTE".equals(mdl.getValueAt(r, 7))) {
                UIHelper.afficherErreur(p, "Seules les séances EN_ATTENTE peuvent être modifiées.\nUne séance validée ou rejetée ne peut plus être modifiée."); return;
            }
            int id = (int) mdl.getValueAt(r, 0);
            seanceDAO.listerParEnseignant(utilisateur.getId()).stream()
                    .filter(s -> s.getId() == id).findFirst().ifPresent(s -> {
                new DialogSeance(this, s, utilisateur, coursDAO, seanceDAO).setVisible(true);
                charger.run();
            });
        });
        btnSuppr.addActionListener(e -> {
            int r = table.getSelectedRow();
            if (r < 0) { UIHelper.afficherErreur(p, "Sélectionnez une séance."); return; }
            if (!"EN_ATTENTE".equals(mdl.getValueAt(r, 7))) {
                UIHelper.afficherErreur(p, "Seules les séances EN_ATTENTE peuvent être supprimées."); return;
            }
            if (UIHelper.confirmer(p, "Supprimer cette séance ?") == JOptionPane.YES_OPTION) {
                seanceDAO.supprimer((int) mdl.getValueAt(r, 0)); charger.run();
            }
        });
        btnRefresh.addActionListener(e -> charger.run());

        corps.add(UIHelper.creerScrollPane(table), BorderLayout.CENTER);

        JLabel info = new JLabel("  ℹ️  Vous pouvez modifier ou supprimer uniquement les séances EN_ATTENTE.");
        info.setFont(new Font("Dialog",Font.ITALIC,11)); info.setForeground(UIHelper.TEXTE_GRIS2);
        corps.add(info, BorderLayout.SOUTH);
        p.add(corps, BorderLayout.CENTER);
        return p;
    }

    // ——————————— NOUVELLE SÉANCE ———————————
    private JPanel panelAjouter() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(UIHelper.FOND_DARK);
        p.add(enTete("Nouvelle séance", "Enregistrez une séance dans le cahier de texte numérique", "Nouvelle séance"), BorderLayout.NORTH);

        JPanel centre = new JPanel(new GridBagLayout());
        centre.setBackground(UIHelper.FOND_DARK);

        JPanel carte = new JPanel(new BorderLayout(0, 22)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0,0,UIHelper.FOND_CARD,0,getHeight(),UIHelper.FOND_CARD2);
                g2.setPaint(gp); g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),16,16));
                g2.dispose();
            }
        };
        carte.setOpaque(false); carte.setBorder(new EmptyBorder(40,40,40,40));
        carte.setPreferredSize(new Dimension(500, 260));

        // Icone cahier+crayon dessinée avec Graphics2D — garantie blanche
        JComponent ico = new JComponent() {
            { setPreferredSize(new Dimension(80, 80)); setOpaque(false); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int cx = getWidth()/2, cy = getHeight()/2;
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Corps du cahier
                g2.drawRoundRect(cx-22, cy-28, 38, 46, 6, 6);
                // Spirales à gauche
                for (int i = -10; i <= 10; i += 10) {
                    g2.drawArc(cx-26, cy+i-5, 9, 9, 0, 270);
                }
                // Lignes de texte
                g2.drawLine(cx-12, cy-14, cx+10, cy-14);
                g2.drawLine(cx-12, cy-6,  cx+10, cy-6);
                g2.drawLine(cx-12, cy+2,  cx+4,  cy+2);
                // Crayon (en bas à droite)
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                int[] px = {cx+8, cx+18, cx+22, cx+12};
                int[] py = {cy+14, cy+4,  cy+8,  cy+18};
                g2.drawPolygon(px, py, 4);
                // Pointe du crayon
                int[] tx = {cx+18, cx+22, cx+26};
                int[] ty = {cy+4,  cy+8,  cy+0};
                g2.drawPolyline(tx, ty, 3);
                // Gomme
                g2.fillRoundRect(cx+20, cy-5, 6, 10, 3, 3);
                g2.dispose();
            }
        };

        JPanel txtPanel = new JPanel(new GridLayout(3,1,0,6));
        txtPanel.setOpaque(false);

        JLabel titre = new JLabel("Enregistrer une séance", SwingConstants.CENTER);
        titre.setFont(new Font("Dialog",Font.BOLD,18)); titre.setForeground(Color.WHITE);

        JLabel desc = new JLabel("Renseignez la date, l'heure, la durée, le contenu et les observations.", SwingConstants.CENTER);
        desc.setFont(new Font("Dialog",Font.PLAIN,13)); desc.setForeground(UIHelper.TEXTE_GRIS);

        JLabel info = new JLabel("La séance sera soumise à validation par le responsable de classe.", SwingConstants.CENTER);
        info.setFont(new Font("Dialog",Font.ITALIC,11)); info.setForeground(UIHelper.TEXTE_GRIS2);

        txtPanel.add(titre); txtPanel.add(desc); txtPanel.add(info);

        JPanel btnP = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnP.setOpaque(false);
        JButton btn = new JButton("➕  Enregistrer une nouvelle séance") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = getModel().isRollover() ? UIHelper.CYAN_ACCENT : UIHelper.BLEU_ACCENT;
                Color c2 = getModel().isRollover() ? UIHelper.BLEU_ACCENT : new Color(0,80,200);
                g2.setPaint(new GradientPaint(0,0,c1,getWidth(),0,c2));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setFont(new Font("Dialog",Font.BOLD,14)); btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false); btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR)); btn.setPreferredSize(new Dimension(340,50));
        btn.addActionListener(e -> new DialogSeance(this, null, utilisateur, coursDAO, seanceDAO).setVisible(true));
        btnP.add(btn);

        carte.add(ico, BorderLayout.NORTH);
        carte.add(txtPanel, BorderLayout.CENTER);
        carte.add(btnP, BorderLayout.SOUTH);
        centre.add(carte);
        p.add(centre, BorderLayout.CENTER);
        return p;
    }
}
