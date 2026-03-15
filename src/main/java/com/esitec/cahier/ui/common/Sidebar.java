package com.esitec.cahier.ui.common;

import com.esitec.cahier.dao.NotificationDAO;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

public class Sidebar extends JPanel {

    private static final int LARGEUR = 240;

    private final List<JButton> navItems = new ArrayList<>();
    private JButton             itemActif;
    private final JPanel        navPanel;
    private JButton             btnCloche;
    private JLabel              badgeLabel;
    private int                 nonLues = 0;
    private final NotificationDAO notifDAO;
    private PanneauNotifications  panneauNotif;

    public Sidebar(Utilisateur utilisateur) {
        this.notifDAO = new NotificationDAO();
        setLayout(new BorderLayout());
        setBackground(UIHelper.FOND_SIDEBAR);
        setPreferredSize(new Dimension(LARGEUR, 0));
        setMinimumSize(new Dimension(LARGEUR, 0));
        setMaximumSize(new Dimension(LARGEUR, Integer.MAX_VALUE));
        setBorder(UIHelper.bordure("card"));

        // ── TOP ──────────────────────────────────────────────
        JPanel top = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0,0,new Color(12,28,70),0,getHeight(),new Color(5,14,38)));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        top.setLayout(new BoxLayout(top, BoxLayout.Y_AXIS));
        top.setOpaque(false);
        top.setBorder(new EmptyBorder(14,12,12,12));

        // Logo + cloche
        JPanel topRow = new JPanel(new BorderLayout());
        topRow.setOpaque(false);
        topRow.setMaximumSize(new Dimension(LARGEUR, 52));
        topRow.setPreferredSize(new Dimension(LARGEUR, 52));

        JPanel logoWrap = new JPanel(new FlowLayout(FlowLayout.LEFT,0,0));
        logoWrap.setOpaque(false);
        ImageIcon logoE = UIHelper.chargerLogo("logo_esitec.png", 44, 44);
        if (logoE != null) logoWrap.add(new JLabel(logoE));
        else { JLabel e=new JLabel("📚"); e.setFont(new Font("Dialog",Font.PLAIN,26)); logoWrap.add(e); }

        JPanel clocheWrap = new JPanel(null);
        clocheWrap.setOpaque(false);
        clocheWrap.setPreferredSize(new Dimension(42,42));
        clocheWrap.setMinimumSize(new Dimension(42,42));
        clocheWrap.setMaximumSize(new Dimension(42,42));

        // Icone enveloppe dessinée manuellement — garantie visible
        btnCloche = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                // Fond cercle au survol
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,28));
                    g2.fill(new Ellipse2D.Float(1,1,w-2,h-2));
                }
                // Enveloppe
                int ex = 5, ey = 7, ew = w-10, eh = h-14;
                g2.setColor(Color.WHITE);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                // Corps de l'enveloppe
                g2.drawRoundRect(ex, ey, ew, eh, 3, 3);
                // Rabat en V
                g2.drawLine(ex, ey, ex + ew/2, ey + eh/2);
                g2.drawLine(ex + ew, ey, ex + ew/2, ey + eh/2);
                g2.dispose();
            }
        };
        btnCloche.setContentAreaFilled(false); btnCloche.setBorderPainted(false);
        btnCloche.setFocusPainted(false); btnCloche.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCloche.setToolTipText("Notifications");
        btnCloche.setBounds(2,2,36,36);

        badgeLabel = new JLabel() {
            @Override protected void paintComponent(Graphics g) {
                if (nonLues<=0) return;
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIHelper.ROUGE_BADGE); g2.fillOval(0,0,getWidth(),getHeight());
                g2.setColor(Color.WHITE);
                g2.setFont(new Font(UIHelper.FONT_UI,Font.BOLD,9));
                FontMetrics fm=g2.getFontMetrics();
                String txt=nonLues>9?"9+":String.valueOf(nonLues);
                g2.drawString(txt,(getWidth()-fm.stringWidth(txt))/2,(getHeight()+fm.getAscent()-fm.getDescent())/2);
                g2.dispose();
            }
        };
        badgeLabel.setBounds(22,0,18,18); badgeLabel.setOpaque(false);
        clocheWrap.add(btnCloche); clocheWrap.add(badgeLabel);
        topRow.add(logoWrap,BorderLayout.WEST);
        topRow.add(clocheWrap,BorderLayout.EAST);
        top.add(topRow);
        top.add(Box.createVerticalStrut(8));

        // Séparateur dégradé
        JPanel sepL = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create(); int w=getWidth();
                g2.setPaint(new GradientPaint(0,0,new Color(0,0,0,0),w/2,0,UIHelper.BLEU_ACCENT));
                g2.fillRect(0,0,w/2,1);
                g2.setPaint(new GradientPaint(w/2,0,UIHelper.BLEU_ACCENT,w,0,new Color(0,0,0,0)));
                g2.fillRect(w/2,0,w,1); g2.dispose();
            }
        };
        sepL.setOpaque(false); sepL.setPreferredSize(new Dimension(LARGEUR-24,1));
        sepL.setMaximumSize(new Dimension(LARGEUR-24,1));
        top.add(sepL);
        top.add(Box.createVerticalStrut(10));

        // Carte utilisateur
        JPanel userCard = new JPanel(new BorderLayout(10,0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255,255,255,16));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),10,10)); g2.dispose();
            }
        };
        userCard.setOpaque(false);
        userCard.setBorder(new EmptyBorder(10,10,10,10));
        userCard.setMaximumSize(new Dimension(LARGEUR-24,56));
        userCard.setPreferredSize(new Dimension(LARGEUR-24,56));

        JLabel avatar = new JLabel(getAvatar(utilisateur.getRole())) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0,0,UIHelper.BLEU_ACCENT,34,34,UIHelper.CYAN_ACCENT));
                g2.fill(new Ellipse2D.Float(0,0,34,34)); g2.dispose(); super.paintComponent(g);
            }
        };
        avatar.setFont(new Font("Dialog",Font.PLAIN,15));
        avatar.setPreferredSize(new Dimension(34,34));
        avatar.setHorizontalAlignment(SwingConstants.CENTER);
        avatar.setVerticalAlignment(SwingConstants.CENTER);

        JPanel infoUser = new JPanel(new GridLayout(3,1,0,2)); infoUser.setOpaque(false);
        // Nom
        String nomAff = utilisateur.getNomComplet();
        if (nomAff.length()>19) nomAff=nomAff.substring(0,17)+"…";
        JLabel lblNom = new JLabel(nomAff);
        lblNom.setFont(new Font(UIHelper.FONT_UI,Font.BOLD,12)); lblNom.setForeground(Color.WHITE);
        // Email
        String emailAff = utilisateur.getEmail();
        if (emailAff.length()>22) emailAff=emailAff.substring(0,20)+"…";
        JLabel lblEmail = new JLabel(emailAff);
        lblEmail.setFont(new Font(UIHelper.FONT_UI,Font.PLAIN,9));
        lblEmail.setForeground(new Color(120,150,200));
        // Badge rôle
        JLabel lblRole = new JLabel("  "+getRoleLabel(utilisateur.getRole())+"  ") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIHelper.BLEU_ACCENT);
                g2.fill(new RoundRectangle2D.Float(0,1,getWidth(),getHeight()-2,10,10));
                g2.dispose(); super.paintComponent(g);
            }
        };
        lblRole.setFont(new Font(UIHelper.FONT_UI,Font.BOLD,9));
        lblRole.setForeground(Color.WHITE);
        infoUser.add(lblNom); infoUser.add(lblEmail); infoUser.add(lblRole);
        userCard.add(avatar,BorderLayout.WEST);
        userCard.add(infoUser,BorderLayout.CENTER);
        userCard.setMaximumSize(new Dimension(LARGEUR-24,72));
        userCard.setPreferredSize(new Dimension(LARGEUR-24,72));
        top.add(userCard);

        // ── NAV ──────────────────────────────────────────────
        navPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIHelper.FOND_SIDEBAR); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        navPanel.setLayout(new BoxLayout(navPanel,BoxLayout.Y_AXIS));
        navPanel.setOpaque(true);
        navPanel.setBackground(UIHelper.FOND_SIDEBAR);
        navPanel.setBorder(new EmptyBorder(10,8,10,8));

        JScrollPane navScroll = new JScrollPane(navPanel) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIHelper.FOND_SIDEBAR); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        navScroll.setBorder(null); navScroll.setOpaque(true);
        navScroll.getViewport().setOpaque(true);
        navScroll.getViewport().setBackground(UIHelper.FOND_SIDEBAR);
        navScroll.setBackground(UIHelper.FOND_SIDEBAR);
        navScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        navScroll.getVerticalScrollBar().setPreferredSize(new Dimension(3,0));
        UIHelper.rendreFluid(navScroll);

        // ── DÉCONNEXION ───────────────────────────────────────
        JPanel bottom = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIHelper.BORDURE); g.fillRect(0,0,getWidth(),1);
                g.setColor(UIHelper.FOND_SIDEBAR); g.fillRect(0,1,getWidth(),getHeight()-1);
            }
        };
        bottom.setOpaque(false); bottom.setBorder(new EmptyBorder(8,12,16,12));
        JButton btnDeco = new JButton("⬅  Déconnexion") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isRollover()?new Color(220,40,40):new Color(185,30,30));
                g2.fill(new RoundRectangle2D.Float(0,0,getWidth(),getHeight(),8,8));
                g2.dispose(); super.paintComponent(g);
            }
        };
        btnDeco.setFont(new Font("Dialog",Font.BOLD,12));
        btnDeco.setForeground(Color.WHITE);
        btnDeco.setContentAreaFilled(false); btnDeco.setBorderPainted(false);
        btnDeco.setFocusPainted(false); btnDeco.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDeco.setPreferredSize(new Dimension(LARGEUR-24,40));
        btnDeco.addActionListener(e -> {
            Window w=SwingUtilities.getWindowAncestor(this);
            if (w!=null) w.dispose();
            new FenetreConnexion().setVisible(true);
        });
        bottom.add(btnDeco,BorderLayout.CENTER);

        btnCloche.addActionListener(e -> {
            Frame parent=(Frame)SwingUtilities.getWindowAncestor(this);
            if (panneauNotif!=null && panneauNotif.isVisible()) {
                panneauNotif.dispose(); panneauNotif=null;
            } else {
                panneauNotif=new PanneauNotifications(parent,utilisateur,notifDAO,this::mettreAJourBadge);
                panneauNotif.setVisible(true);
            }
        });

        add(top,BorderLayout.NORTH);
        add(navScroll,BorderLayout.CENTER);
        add(bottom,BorderLayout.SOUTH);
        rafraichirBadge(utilisateur.getId());
    }

    // ── Item principal ────────────────────────────────────────────
    public JButton addItem(String icone, String texte, ActionListener action) {
        final int W = LARGEUR - 18;
        JButton btn = new JButton(icone + "  " + texte) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2=(Graphics2D)g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UIHelper.FOND_SIDEBAR); g2.fillRect(0,0,getWidth(),getHeight());
                boolean actif=Boolean.TRUE.equals(getClientProperty("actif"));
                if (actif) {
                    g2.setColor(new Color(0,100,200,100));
                    g2.fill(new RoundRectangle2D.Float(2,1,getWidth()-4,getHeight()-2,8,8));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,14));
                    g2.fill(new RoundRectangle2D.Float(2,1,getWidth()-4,getHeight()-2,8,8));
                }
                g2.dispose(); super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Dialog",Font.PLAIN,13));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setContentAreaFilled(false); btn.setBorderPainted(false); btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(10,14,10,6));
        Dimension d=new Dimension(W,42); btn.setPreferredSize(d); btn.setMinimumSize(d); btn.setMaximumSize(d);
        btn.addActionListener(ev -> { setActif(btn); action.actionPerformed(ev); });
        navPanel.add(btn);
        navPanel.add(Box.createRigidArea(new Dimension(0,2)));
        navItems.add(btn);
        return btn;
    }

    /**
     * Ajoute un sous-menu dépliable sous un item parent.
     * Les sous-items s'affichent/masquent en cliquant sur le parent.
     */
    public void addSousMenu(JButton parent, String[][] sousItems, ActionListener[] actions) {
        final int W = LARGEUR - 18;

        // Panel conteneur des sous-items (visible par défaut)
        JPanel sousPanel = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIHelper.FOND_SIDEBAR); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        sousPanel.setLayout(new BoxLayout(sousPanel,BoxLayout.Y_AXIS));
        sousPanel.setOpaque(true);
        sousPanel.setBackground(UIHelper.FOND_SIDEBAR);
        sousPanel.setVisible(true); // Affiché par défaut

        // Flèche indicateur (bas = déplié par défaut)
        JLabel fleche = new JLabel("▼");
        fleche.setFont(new Font(UIHelper.FONT_UI,Font.PLAIN,9));
        fleche.setForeground(UIHelper.TEXTE_GRIS2);

        // Ajouter la flèche dans le bouton parent
        parent.setLayout(new BorderLayout());
        JLabel parentTxt = new JLabel(parent.getText());
        parentTxt.setFont(parent.getFont());
        parentTxt.setForeground(parent.getForeground());

        // Créer les sous-boutons
        for (int i=0; i<sousItems.length; i++) {
            final int idx=i;
            final String ico=sousItems[i][0];
            final String txt=sousItems[i][1];
            JButton sub = new JButton(ico+"  "+txt) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2=(Graphics2D)g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UIHelper.FOND_SIDEBAR); g2.fillRect(0,0,getWidth(),getHeight());
                    boolean actif=Boolean.TRUE.equals(getClientProperty("actif"));
                    if (actif) {
                        g2.setColor(new Color(0,100,200,80));
                        g2.fill(new RoundRectangle2D.Float(2,1,getWidth()-4,getHeight()-2,6,6));
                    } else if (getModel().isRollover()) {
                        g2.setColor(new Color(255,255,255,10));
                        g2.fill(new RoundRectangle2D.Float(2,1,getWidth()-4,getHeight()-2,6,6));
                    }
                    g2.dispose(); super.paintComponent(g);
                }
            };
            sub.setForeground(Color.WHITE);
            sub.setFont(new Font("Dialog",Font.PLAIN,12));
            sub.setHorizontalAlignment(SwingConstants.LEFT);
            sub.setContentAreaFilled(false); sub.setBorderPainted(false); sub.setFocusPainted(false);
            sub.setCursor(new Cursor(Cursor.HAND_CURSOR));
            // Indentation + barre latérale fine
            sub.setBorder(BorderFactory.createCompoundBorder(
                UIHelper.bordure("accent"),
                new EmptyBorder(8,26,8,6)));
            Dimension ds=new Dimension(W,36); sub.setPreferredSize(ds); sub.setMinimumSize(ds); sub.setMaximumSize(ds);
            final ActionListener act=actions[idx];
            sub.addActionListener(ev -> { setActif(sub); act.actionPerformed(ev); });
            sousPanel.add(sub);
            sousPanel.add(Box.createRigidArea(new Dimension(0,1)));
            navItems.add(sub);
        }
        sousPanel.setMaximumSize(new Dimension(W, sousItems.length*37+2));
        sousPanel.setPreferredSize(new Dimension(W, sousItems.length*37+2));

        // Trouver la position du bouton parent dans navPanel et insérer après
        int idx=-1;
        for (int i=0; i<navPanel.getComponentCount(); i++) {
            if (navPanel.getComponent(i)==parent) { idx=i; break; }
        }
        if (idx>=0) {
            navPanel.add(sousPanel, idx+2);
        } else {
            navPanel.add(sousPanel);
        }

        // Toggle dépliable au clic sur le parent
        parent.addActionListener(e -> {
            boolean nowVisible = !sousPanel.isVisible();
            sousPanel.setVisible(nowVisible);
            fleche.setText(nowVisible ? "▼" : "▶");
            navPanel.revalidate(); navPanel.repaint();
        });

        // Ajouter flèche à droite du bouton parent
        JPanel parentContent = new JPanel(new BorderLayout());
        parentContent.setOpaque(false);
        JLabel parentLabel = new JLabel(parent.getText());
        parentLabel.setFont(parent.getFont()); parentLabel.setForeground(parent.getForeground());
        parentContent.add(parentLabel, BorderLayout.CENTER);
        parentContent.add(fleche, BorderLayout.EAST);
        // Note: on ne peut pas modifier le layout d'un JButton facilement,
        // donc on garde le texte tel quel et on change juste le comportement
    }

    public void addSeparateur(String label) {
        final int W = LARGEUR - 18;
        navPanel.add(Box.createRigidArea(new Dimension(0,8)));
        JLabel lbl = new JLabel("  "+label.toUpperCase());
        lbl.setFont(new Font("Dialog",Font.BOLD,9));
        lbl.setForeground(new Color(70,100,160));
        Dimension d=new Dimension(W,16); lbl.setPreferredSize(d); lbl.setMinimumSize(d); lbl.setMaximumSize(d);
        navPanel.add(lbl);
        navPanel.add(Box.createRigidArea(new Dimension(0,4)));
    }

    public void rafraichirBadge(int id) { mettreAJourBadge(notifDAO.compterNonLues(id)); }
    private void mettreAJourBadge(int count) {
        nonLues=count; badgeLabel.setVisible(count>0); badgeLabel.repaint();
    }
    public void setActif(JButton btn) {
        if (itemActif!=null) UIHelper.marquerInactif(itemActif);
        itemActif=btn; UIHelper.marquerActif(btn);
    }
    public void setActifParIndex(int i) {
        if (i>=0 && i<navItems.size()) setActif(navItems.get(i));
    }
    private String getAvatar(Utilisateur.Role r) {
        return switch(r){case CHEF_DEPARTEMENT->"👔";case ENSEIGNANT->"🎓";case RESPONSABLE_CLASSE->"📋";};
    }
    private String getRoleLabel(Utilisateur.Role r) {
        return switch(r){case CHEF_DEPARTEMENT->"Chef de département";case ENSEIGNANT->"Enseignant";case RESPONSABLE_CLASSE->"Responsable de classe";};
    }
}
