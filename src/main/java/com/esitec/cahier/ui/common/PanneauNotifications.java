package com.esitec.cahier.ui.common;

import com.esitec.cahier.dao.NotificationDAO;
import com.esitec.cahier.model.Notification;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.util.List;
import java.util.function.Consumer;

public class PanneauNotifications extends JDialog {

    private final Utilisateur       utilisateur;
    private final NotificationDAO   notifDAO;
    private final Consumer<Integer> onUpdate;
    private JPanel listePanel;

    public PanneauNotifications(Frame parent, Utilisateur utilisateur,
                                 NotificationDAO notifDAO, Consumer<Integer> onUpdate) {
        super(parent, "Notifications", false);
        this.utilisateur = utilisateur;
        this.notifDAO    = notifDAO;
        this.onUpdate    = onUpdate;
        initialiserUI();
        charger();
    }

    private void initialiserUI() {
        setSize(400, 600);
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        setLocation(screen.width - 420, 60);
        setUndecorated(true);
        setAlwaysOnTop(true);
        getRootPane().setBorder(UIHelper.bordure("dialog"));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(UIHelper.FOND_CARD); g2.fillRect(0,0,getWidth(),getHeight());
                g2.dispose();
            }
        };
        root.setOpaque(false);

        // HEADER
        JPanel header = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0,UIHelper.BLEU_PRIMAIRE,getWidth(),0,new Color(0,55,130)));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        header.setOpaque(false); header.setBorder(new EmptyBorder(14,16,14,16));

        // Icone enveloppe + titre
        JPanel titreWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        titreWrap.setOpaque(false);
        JLabel enveloppe = new JLabel("\u2709"); // ✉ en unicode
        enveloppe.setFont(new Font("Dialog", Font.BOLD, 20));
        enveloppe.setForeground(Color.WHITE);
        JLabel lblTitre = new JLabel("Notifications");
        lblTitre.setFont(new Font("Dialog", Font.BOLD, 15));
        lblTitre.setForeground(Color.WHITE);
        titreWrap.add(enveloppe); titreWrap.add(lblTitre);

        JPanel headerBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        headerBtns.setOpaque(false);
        JButton btnToutLire = btnSmall("\u2713 Tout lire", UIHelper.VERT_BADGE);
        JButton btnFermer   = btnSmall("\u2715", new Color(180,50,50));
        headerBtns.add(btnToutLire); headerBtns.add(btnFermer);

        header.add(titreWrap, BorderLayout.WEST);
        header.add(headerBtns, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        // LISTE
        listePanel = new JPanel();
        listePanel.setLayout(new BoxLayout(listePanel, BoxLayout.Y_AXIS));
        listePanel.setBackground(UIHelper.FOND_CARD);

        JScrollPane scroll = new JScrollPane(listePanel);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(UIHelper.FOND_CARD);
        scroll.setBackground(UIHelper.FOND_CARD);
        scroll.getVerticalScrollBar().setPreferredSize(new Dimension(4, 0));
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        UIHelper.rendreFluid(scroll);
        root.add(scroll, BorderLayout.CENTER);

        // FOOTER
        JPanel footer = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(new Color(10,20,48)); g.fillRect(0,0,getWidth(),getHeight());
                g.setColor(UIHelper.BORDURE); g.fillRect(0,0,getWidth(),1);
            }
        };
        footer.setOpaque(false); footer.setBorder(new EmptyBorder(8,14,8,14));
        JLabel footerTxt = new JLabel("ESITEC \u2014 Cahier de Texte \u00a9 2025-2026");
        footerTxt.setFont(new Font("Dialog", Font.PLAIN, 10));
        footerTxt.setForeground(UIHelper.TEXTE_GRIS2);
        footer.add(footerTxt, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);

        // Actions
        btnToutLire.addActionListener(e -> {
            notifDAO.marquerToutesLues(utilisateur.getId());
            if (onUpdate != null) onUpdate.accept(0);
            charger();
        });
        btnFermer.addActionListener(e -> dispose());

        add(root);
    }

    private void charger() {
        listePanel.removeAll();
        List<Notification> notifs = notifDAO.listerParUtilisateur(utilisateur.getId());
        if (notifs.isEmpty()) {
            JPanel vide = new JPanel(new GridBagLayout());
            vide.setBackground(UIHelper.FOND_CARD);
            vide.setPreferredSize(new Dimension(380, 200));
            JPanel inner = new JPanel(); inner.setOpaque(false);
            inner.setLayout(new BoxLayout(inner, BoxLayout.Y_AXIS));
            JLabel ico = new JLabel("\u2709", SwingConstants.CENTER);
            ico.setFont(new Font("Dialog", Font.PLAIN, 48));
            ico.setForeground(new Color(40,60,100));
            ico.setAlignmentX(Component.CENTER_ALIGNMENT);
            JLabel msg = new JLabel("Aucune notification");
            msg.setFont(new Font("Dialog", Font.PLAIN, 13));
            msg.setForeground(UIHelper.TEXTE_GRIS2);
            msg.setAlignmentX(Component.CENTER_ALIGNMENT);
            inner.add(ico); inner.add(Box.createVerticalStrut(8)); inner.add(msg);
            vide.add(inner);
            listePanel.add(vide);
        } else {
            for (Notification n : notifs) listePanel.add(carteNotif(n));
        }
        listePanel.revalidate(); listePanel.repaint();
        if (onUpdate != null) onUpdate.accept((int) notifs.stream().filter(n -> !n.isLue()).count());
    }

    private JPanel carteNotif(Notification n) {
        JPanel card = new JPanel(new BorderLayout(10, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = n.isLue() ? UIHelper.FOND_CARD : new Color(20, 45, 90);
                g2.setColor(bg); g2.fillRect(0, 0, getWidth(), getHeight());
                if (!n.isLue()) {
                    g2.setColor(UIHelper.BLEU_ACCENT);
                    g2.fillRect(0, 0, 3, getHeight());
                }
                // Séparateur bas
                g2.setColor(UIHelper.BORDURE); g2.fillRect(0, getHeight()-1, getWidth(), 1);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(12, 14, 12, 14));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        // Icone type de notif — unicode pour garantir l'affichage
        String iconeChar = switch (n.getType()) {
            case SEANCE_EN_ATTENTE -> "\u23F3"; // ⏳
            case SEANCE_VALIDEE    -> "\u2705"; // ✅
            case SEANCE_REJETEE    -> "\u274C"; // ❌
            case COMPTE_EN_ATTENTE -> "\uD83D\uDC64".equals("") ? "?" : "\u2709"; // ✉
            case AVANCEMENT_ELEVE  -> "\uD83D\uDCC8".equals("") ? "?" : "\u25B2"; // ▲
            default                -> "\uD83D\uDD14".equals("") ? "!" : "\u2709";
        };
        Color couleurIco = switch (n.getType()) {
            case SEANCE_EN_ATTENTE -> UIHelper.ORANGE_BADGE;
            case SEANCE_VALIDEE    -> UIHelper.VERT_BADGE;
            case SEANCE_REJETEE    -> UIHelper.ROUGE_BADGE;
            case COMPTE_EN_ATTENTE -> UIHelper.BLEU_ACCENT;
            case AVANCEMENT_ELEVE  -> UIHelper.CYAN_ACCENT;
            default                -> UIHelper.TEXTE_GRIS;
        };

        JPanel icoWrap = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(couleurIco.getRed(), couleurIco.getGreen(), couleurIco.getBlue(), 30));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                g2.dispose(); super.paintComponent(g);
            }
        };
        icoWrap.setOpaque(false);
        icoWrap.setPreferredSize(new Dimension(36,36));
        icoWrap.setMinimumSize(new Dimension(36,36));
        JLabel icoLbl = new JLabel(iconeChar, SwingConstants.CENTER);
        icoLbl.setFont(new Font("Dialog", Font.BOLD, 18));
        icoLbl.setForeground(couleurIco);
        icoWrap.add(icoLbl);

        // Texte
        JPanel textes = new JPanel(); textes.setOpaque(false);
        textes.setLayout(new BoxLayout(textes, BoxLayout.Y_AXIS));
        JLabel titre = new JLabel(n.getTitre());
        titre.setFont(new Font("Dialog", n.isLue() ? Font.PLAIN : Font.BOLD, 12));
        titre.setForeground(n.isLue() ? UIHelper.TEXTE_GRIS : Color.WHITE);
        JLabel date = new JLabel(n.getDateFormatee());
        date.setFont(new Font("Dialog", Font.PLAIN, 10));
        date.setForeground(UIHelper.TEXTE_GRIS2);
        JLabel msg = new JLabel("<html><div style='width:240px'>" + n.getMessage() + "</div></html>");
        msg.setFont(new Font("Dialog", Font.PLAIN, 11));
        msg.setForeground(UIHelper.TEXTE_GRIS);
        textes.add(titre); textes.add(Box.createVerticalStrut(2));
        textes.add(date);  textes.add(Box.createVerticalStrut(3));
        textes.add(msg);

        // Bouton marquer lu
        JPanel droite = new JPanel(new GridBagLayout()); droite.setOpaque(false);
        if (!n.isLue()) {
            JButton btnLue = new JButton("\u2713") {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(getModel().isRollover()?UIHelper.VERT_BADGE:new Color(0,150,80));
                    g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),6,6));
                    g2.dispose(); super.paintComponent(g);
                }
            };
            btnLue.setFont(new Font("Dialog", Font.BOLD, 11));
            btnLue.setForeground(Color.WHITE);
            btnLue.setContentAreaFilled(false); btnLue.setBorderPainted(false);
            btnLue.setFocusPainted(false); btnLue.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnLue.setPreferredSize(new Dimension(28,24));
            btnLue.setToolTipText("Marquer comme lu");
            btnLue.addActionListener(e -> { notifDAO.marquerLue(n.getId()); charger(); });
            droite.add(btnLue);
        }

        card.add(icoWrap, BorderLayout.WEST);
        card.add(textes,  BorderLayout.CENTER);
        card.add(droite,  BorderLayout.EAST);
        return card;
    }

    private JButton btnSmall(String txt, Color bg) {
        JButton b = new JButton(txt) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?bg.brighter():bg);
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),6,6));
                g2.dispose(); super.paintComponent(g);
            }
        };
        b.setFont(new Font("Dialog", Font.BOLD, 11));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false); b.setBorderPainted(false);
        b.setFocusPainted(false); b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(5,10,5,10));
        return b;
    }
}
