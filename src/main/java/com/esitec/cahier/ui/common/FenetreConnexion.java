package com.esitec.cahier.ui.common;

import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.ui.admin.DashboardAdmin;
import com.esitec.cahier.ui.enseignant.DashboardEnseignant;
import com.esitec.cahier.ui.responsable.DashboardResponsable;
import com.esitec.cahier.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;

/**
 * Fenêtre de connexion — Thème sombre ENT ESITEC.
 */
public class FenetreConnexion extends JFrame {

    private JTextField     champEmail;
    private JPasswordField champMotDePasse;
    private JLabel         lblErreur;
    private final UtilisateurDAO utilisateurDAO;

    public FenetreConnexion() {
        this.utilisateurDAO = new UtilisateurDAO();
        UIHelper.appliquerLookAndFeel();
        initialiserUI();
    }

    private void initialiserUI() {
        setTitle("ENT ESITEC — Cahier de Texte Numérique");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(true);

        JPanel root = new JPanel(new GridLayout(1, 2)) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIHelper.FOND_DARK);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        // ══════════════════════════════
        // PANNEAU GAUCHE — Branding
        // ══════════════════════════════
        JPanel gauche = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(5, 18, 55),
                        getWidth(), getHeight(), new Color(0, 55, 130));
                g2.setPaint(gp); g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(0, 140, 255, 22));
                g2.fill(new Ellipse2D.Float(-70, -70, 300, 300));
                g2.setColor(new Color(0, 210, 230, 15));
                g2.fill(new Ellipse2D.Float(getWidth()-200, getHeight()-200, 320, 320));
                g2.setColor(new Color(0, 140, 255, 60));
                g2.fillRect(getWidth()-2, 0, 2, getHeight());
                g2.dispose();
            }
        };
        gauche.setOpaque(false);
        gauche.setBorder(new EmptyBorder(30, 30, 30, 30));

        GridBagConstraints gL = new GridBagConstraints();
        gL.gridx = 0; gL.fill = GridBagConstraints.HORIZONTAL; gL.anchor = GridBagConstraints.CENTER;

        // Logo ESITEC seulement
        gL.gridy = 0; gL.insets = new Insets(0, 0, 12, 0);
        JPanel logosP = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        logosP.setOpaque(false);
        ImageIcon logoE = UIHelper.chargerLogo("logo_esitec.png", 80, 80);
        if (logoE != null) logosP.add(new JLabel(logoE));
        else { JLabel e = new JLabel("📚"); e.setFont(new Font("Dialog",Font.PLAIN,52)); logosP.add(e); }
        gauche.add(logosP, gL);

        gL.gridy = 1; gL.insets = new Insets(0, 0, 4, 0);
        JLabel lblESITEC = new JLabel("ESITEC", SwingConstants.CENTER);
        lblESITEC.setFont(new Font("Dialog", Font.BOLD, 36));
        lblESITEC.setForeground(Color.WHITE);
        gauche.add(lblESITEC, gL);

        gL.gridy = 2; gL.insets = new Insets(0, 40, 0, 40);
        JPanel badge = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,UIHelper.BLEU_ACCENT,getWidth(),0,UIHelper.CYAN_ACCENT));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),20,20));
                g2.dispose(); super.paintComponent(g);
            }
        };
        badge.setOpaque(false);
        JLabel lblGroupe = new JLabel("GROUPE SUP DE CO DAKAR");
        lblGroupe.setFont(new Font("Dialog", Font.BOLD, 11));
        lblGroupe.setForeground(Color.WHITE);
        badge.add(lblGroupe);
        gauche.add(badge, gL);

        gL.gridy = 3; gL.insets = new Insets(8, 0, 30, 0);
        JLabel lblGbs = new JLabel("Global Business School", SwingConstants.CENTER);
        lblGbs.setFont(new Font("Georgia", Font.ITALIC, 13));
        lblGbs.setForeground(UIHelper.CYAN_ACCENT);
        gauche.add(lblGbs, gL);

        gL.gridy = 4; gL.insets = new Insets(0, 20, 20, 20);
        JPanel sep = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0,new Color(0,0,0,0),getWidth()/2,0,UIHelper.BLEU_ACCENT));
                g2.fillRect(0,0,getWidth()/2,1);
                g2.setPaint(new GradientPaint(getWidth()/2,0,UIHelper.BLEU_ACCENT,getWidth(),0,new Color(0,0,0,0)));
                g2.fillRect(getWidth()/2,0,getWidth(),1); g2.dispose();
            }
        };
        sep.setOpaque(false); sep.setPreferredSize(new Dimension(200, 1));
        gauche.add(sep, gL);

        gL.gridy = 5; gL.insets = new Insets(0, 0, 6, 0);
        JLabel lblApp = new JLabel("📖  Cahier de Texte Numérique", SwingConstants.CENTER);
        lblApp.setFont(new Font("Dialog", Font.BOLD, 15));
        lblApp.setForeground(new Color(150, 210, 255));
        gauche.add(lblApp, gL);

        gL.gridy = 6; gL.insets = new Insets(0, 0, 30, 0);
        JLabel lblAnnee = new JLabel("Année académique 2025–2026", SwingConstants.CENTER);
        lblAnnee.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblAnnee.setForeground(UIHelper.TEXTE_GRIS);
        gauche.add(lblAnnee, gL);

        // Features supprimées volontairement

        // ══════════════════════════════
        // PANNEAU DROIT — Formulaire
        // ══════════════════════════════
        JPanel droite = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIHelper.FOND_CARD);
                g.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        droite.setOpaque(false);
        droite.setBorder(new EmptyBorder(55, 60, 55, 60));

        GridBagConstraints gR = new GridBagConstraints();
        gR.gridx = 0; gR.fill = GridBagConstraints.HORIZONTAL; gR.weightx = 1;

        gR.gridy = 0; gR.insets = new Insets(0, 0, 4, 0);
        JLabel lblTitre = new JLabel("Connexion");
        lblTitre.setFont(new Font("Dialog", Font.BOLD, 30));
        lblTitre.setForeground(Color.WHITE);
        droite.add(lblTitre, gR);

        gR.gridy = 1; gR.insets = new Insets(0, 0, 36, 0);
        JLabel lblSub = new JLabel("Accédez à votre espace pédagogique ESITEC");
        lblSub.setFont(new Font("Dialog", Font.PLAIN, 13));
        lblSub.setForeground(UIHelper.TEXTE_GRIS);
        droite.add(lblSub, gR);

        // Champ email
        gR.gridy = 2; gR.insets = new Insets(0, 0, 6, 0);
        droite.add(lbl("✉  Adresse email"), gR);
        gR.gridy = 3; gR.insets = new Insets(0, 0, 20, 0);
        champEmail = UIHelper.creerChamp(28);
        champEmail.setPreferredSize(new Dimension(340, 48));
        champEmail.setText("malik.kanne@supdeco.edu.sn");
        droite.add(champEmail, gR);

        // Champ mot de passe
        gR.gridy = 4; gR.insets = new Insets(0, 0, 6, 0);
        droite.add(lbl("🔒  Mot de passe"), gR);
        gR.gridy = 5; gR.insets = new Insets(0, 0, 8, 0);
        champMotDePasse = UIHelper.creerChampMotDePasse(28);
        champMotDePasse.setPreferredSize(new Dimension(340, 48));
        champMotDePasse.setText("malik.com");
        droite.add(UIHelper.wrapAvecOeil(champMotDePasse), gR);

        // Label erreur (caché par défaut)
        gR.gridy = 6; gR.insets = new Insets(0, 0, 16, 0);
        lblErreur = new JLabel(" ");
        lblErreur.setFont(new Font("Dialog", Font.PLAIN, 12));
        lblErreur.setForeground(UIHelper.ROUGE_BADGE);
        droite.add(lblErreur, gR);

        // Bouton connexion
        gR.gridy = 7; gR.insets = new Insets(0, 0, 16, 0);
        JButton btnConnexion = new JButton("Se connecter  →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = getModel().isRollover() ? UIHelper.CYAN_ACCENT : UIHelper.BLEU_ACCENT;
                Color c2 = getModel().isRollover() ? UIHelper.BLEU_ACCENT : new Color(0, 80, 200);
                g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), 0, c2));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnConnexion.setFont(new Font("Dialog", Font.BOLD, 15));
        btnConnexion.setForeground(Color.WHITE);
        btnConnexion.setContentAreaFilled(false); btnConnexion.setBorderPainted(false);
        btnConnexion.setFocusPainted(false); btnConnexion.setOpaque(false);
        btnConnexion.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConnexion.setPreferredSize(new Dimension(340, 50));
        droite.add(btnConnexion, gR);

        // Lien inscription
        gR.gridy = 8; gR.insets = new Insets(0, 0, 0, 0);
        JButton btnInscription = new JButton("Pas encore de compte ? Créer un compte");
        btnInscription.setFont(new Font("Dialog", Font.PLAIN, 12));
        btnInscription.setForeground(UIHelper.CYAN_ACCENT);
        btnInscription.setBorderPainted(false); btnInscription.setContentAreaFilled(false);
        btnInscription.setFocusPainted(false);
        btnInscription.setCursor(new Cursor(Cursor.HAND_CURSOR));
        droite.add(btnInscription, gR);

        // Actions
        btnConnexion.addActionListener(e -> seConnecter());
        champMotDePasse.addActionListener(e -> seConnecter());
        champEmail.addActionListener(e -> champMotDePasse.requestFocus());
        btnInscription.addActionListener(e -> new DialogInscription(this).setVisible(true));

        root.add(gauche);
        root.add(droite);
        add(root);
    }

    private JLabel lbl(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font("Dialog", Font.BOLD, 13));
        l.setForeground(new Color(100, 160, 230));
        return l;
    }

    private void seConnecter() {
        String email = champEmail.getText().trim();
        String mdp   = new String(champMotDePasse.getPassword());

        lblErreur.setText(" ");

        if (email.isEmpty() || mdp.isEmpty()) {
            lblErreur.setText("⚠  Veuillez remplir tous les champs.");
            return;
        }
        Utilisateur u = utilisateurDAO.authentifier(email, mdp);
        if (u == null) {
            lblErreur.setText("⚠  Email ou mot de passe incorrect.");
            champMotDePasse.setText("");
            champMotDePasse.requestFocus();
            return;
        }
        JFrame dashboard = switch (u.getRole()) {
            case CHEF_DEPARTEMENT   -> new DashboardAdmin(u);
            case ENSEIGNANT         -> new DashboardEnseignant(u);
            case RESPONSABLE_CLASSE -> new DashboardResponsable(u);
        };
        dashboard.setVisible(true);
        this.dispose();
    }
}
