package com.esitec.cahier.util;

import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.AbstractBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;

public class UIHelper {

    // ── Palette thème sombre ESITEC ──────────────────────────────
    public static final Color FOND_DARK      = new Color(10, 20, 45);
    public static final Color FOND_SIDEBAR   = new Color(8, 16, 38);
    public static final Color FOND_CARD      = new Color(18, 32, 65);
    public static final Color FOND_CARD2     = new Color(22, 40, 80);
    public static final Color BLEU_PRIMAIRE  = new Color(26, 75, 153);
    public static final Color BLEU_ACCENT    = new Color(0, 140, 255);
    public static final Color BLEU_CLAIR     = new Color(50, 120, 220);
    public static final Color CYAN_ACCENT    = new Color(0, 210, 230);
    public static final Color VERT_BADGE     = new Color(0, 200, 120);
    public static final Color ORANGE_BADGE   = new Color(255, 160, 0);
    public static final Color ROUGE_BADGE    = new Color(255, 70, 70);
    public static final Color JAUNE_BADGE    = new Color(255, 210, 0);
    public static final Color TEXTE_BLANC    = new Color(240, 245, 255);
    public static final Color TEXTE_GRIS     = new Color(140, 160, 200);
    public static final Color TEXTE_GRIS2    = new Color(90, 110, 150);
    public static final Color BORDURE        = new Color(35, 55, 100);

    // Alias compatibilité
    public static final Color COULEUR_PRIMAIRE   = BLEU_PRIMAIRE;
    public static final Color COULEUR_FOND       = FOND_DARK;
    public static final Color COULEUR_TEXTE      = TEXTE_BLANC;
    public static final Color COULEUR_ACCENT     = VERT_BADGE;
    public static final Color COULEUR_DANGER     = ROUGE_BADGE;
    public static final Color COULEUR_WARNING    = ORANGE_BADGE;
    public static final Color COULEUR_SECONDAIRE = FOND_CARD;
    public static final Color TEXTE_SOMBRE       = TEXTE_BLANC;
    public static final Color FOND_PAGE          = FOND_DARK;

    // Police emoji universelle
    public static final String FONT_EMOJI = "Dialog"; // Police logique Java → fallback auto vers Segoe UI Emoji
    public static final String FONT_UI    = "Segoe UI";

    public static void appliquerLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            UIManager.put("Panel.background", FOND_DARK);
            UIManager.put("OptionPane.background", FOND_CARD);
            UIManager.put("OptionPane.messageForeground", TEXTE_BLANC);
        } catch (Exception ignored) {}
    }

    public static ImageIcon chargerLogo(String nom, int w, int h) {
        try {
            URL url = UIHelper.class.getClassLoader().getResource("images/" + nom);
            if (url != null) {
                Image img = new ImageIcon(url).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        } catch (Exception ignored) {}
        return null;
    }
    public static ImageIcon chargerLogo(int w, int h) { return chargerLogo("logo_supdeco.png", w, h); }

    // ── Boutons ────────────────────────────────────────────────────
    public static JButton creerBouton(String texte, Color bg) {
        JButton btn = new JButton(texte) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color c1 = getModel().isRollover() ? bg.brighter() : bg;
                Color c2 = getModel().isRollover() ? bg : bg.darker();
                g2.setPaint(new GradientPaint(0, 0, c1, 0, getHeight(), c2));
                g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Dialog", Font.BOLD, 12));
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 18, 9, 18));
        return btn;
    }

    public static JButton creerBoutonPrimaire(String t) { return creerBouton(t, BLEU_ACCENT);  }
    public static JButton creerBoutonSucces(String t)   { return creerBouton(t, VERT_BADGE);   }
    public static JButton creerBoutonDanger(String t)   { return creerBouton(t, ROUGE_BADGE);  }
    public static JButton creerBoutonWarning(String t)  { return creerBouton(t, ORANGE_BADGE); }

    // ── Labels ─────────────────────────────────────────────────────
    public static JLabel creerTitre(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(new Font(FONT_EMOJI, Font.BOLD, 24));
        l.setForeground(TEXTE_BLANC);
        return l;
    }
    public static JLabel creerSousTitre(String texte) {
        JLabel l = new JLabel(texte);
        l.setFont(new Font(FONT_UI, Font.PLAIN, 13));
        l.setForeground(TEXTE_GRIS);
        return l;
    }
    public static JLabel lblFormulaire(String t) {
        JLabel l = new JLabel(t);
        l.setFont(new Font(FONT_EMOJI, Font.BOLD, 12));
        l.setForeground(new Color(100, 160, 230));
        return l;
    }

    // ── Input texte arrondi avec focus lumineux ────────────────────
    public static JTextField creerChamp(int c) {
        JTextField tf = new JTextField(c) {
            boolean focused = false;
            { addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { focused = true;  repaint(); }
                @Override public void focusLost(java.awt.event.FocusEvent e)   { focused = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FOND_CARD2);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 10, 10));
                g2.setColor(focused ? BLEU_ACCENT : BORDURE);
                g2.setStroke(new BasicStroke(focused ? 2f : 1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 10, 10));
                if (focused) {
                    g2.setColor(new Color(0, 140, 255, 35));
                    g2.setStroke(new BasicStroke(4f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        tf.setFont(new Font(FONT_UI, Font.PLAIN, 13));
        tf.setForeground(TEXTE_BLANC);
        tf.setCaretColor(BLEU_ACCENT);
        tf.setOpaque(false);
        tf.setBorder(new EmptyBorder(10, 14, 10, 14));
        return tf;
    }

    /**
     * Retourne le texte du champ de filtre, ou "" si le champ est vide / contient le placeholder.
     * Utilisé par tous les Runnable charger pour filtrer les tableaux.
     */
    public static String getFiltre(JTextField champ) {
        if (champ == null) return "";
        String txt = champ.getText().trim().toLowerCase();
        if (txt.startsWith("rechercher")) return "";
        return txt;
    }

    // ── Input texte avec placeholder ─────────────────────────────────
    public static JTextField creerChamp(String placeholder) {
        JTextField tf = creerChamp(20);
        tf.setForeground(new Color(120, 140, 180));
        tf.setText(placeholder);
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) {
                if (tf.getText().equals(placeholder)) {
                    tf.setText("");
                    tf.setForeground(TEXTE_BLANC);
                }
            }
            @Override public void focusLost(java.awt.event.FocusEvent e) {
                if (tf.getText().isBlank()) {
                    tf.setText(placeholder);
                    tf.setForeground(new Color(120, 140, 180));
                }
            }
        });
        return tf;
    }

    // ── Input mot de passe arrondi ─────────────────────────────────
    public static JPasswordField creerChampMotDePasse(int c) {
        JPasswordField pf = new JPasswordField(c) {
            boolean focused = false;
            { addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { focused = true;  repaint(); }
                @Override public void focusLost(java.awt.event.FocusEvent e)   { focused = false; repaint(); }
            }); }
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FOND_CARD2);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 10, 10));
                g2.setColor(focused ? BLEU_ACCENT : BORDURE);
                g2.setStroke(new BasicStroke(focused ? 2f : 1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 10, 10));
                if (focused) {
                    g2.setColor(new Color(0, 140, 255, 35));
                    g2.setStroke(new BasicStroke(4f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };
        pf.setFont(new Font(FONT_UI, Font.PLAIN, 13));
        pf.setForeground(TEXTE_BLANC);
        pf.setCaretColor(BLEU_ACCENT);
        pf.setOpaque(false);
        pf.setBorder(new EmptyBorder(10, 14, 10, 48)); // padding droit pour le bouton oeil
        return pf;
    }

    /**
     * Enveloppe un JPasswordField avec un bouton oeil (afficher/masquer).
     * Utilise BorderLayout pour garantir la visibilité du bouton.
     */
    public static JPanel wrapAvecOeil(JPasswordField pf) {
        // Bouton oeil
        // Bouton oeil avec caractères unicode garantis
        final boolean[] mdpVisible = {false};
        JButton oeil = new JButton() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FOND_CARD2);
                g2.fillRect(0, 0, getWidth(), getHeight());
                if (getModel().isRollover()) {
                    g2.setColor(new Color(255,255,255,20));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                }
                // Dessiner oeil manuellement
                g2.setColor(mdpVisible[0] ? BLEU_ACCENT : new Color(100,140,200));
                int cx = getWidth()/2, cy = getHeight()/2;
                // Forme oeil
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.drawArc(cx-10, cy-6, 20, 12, 0, 180);
                g2.drawArc(cx-10, cy-6, 20, 12, 180, 180);
                // Pupille
                g2.fillOval(cx-4, cy-4, 8, 8);
                g2.setColor(FOND_CARD2);
                g2.fillOval(cx-2, cy-2, 4, 4);
                // Barre si masqué
                if (!mdpVisible[0]) {
                    g2.setColor(new Color(100,140,200));
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx-8, cy+6, cx+8, cy-6);
                }
                g2.dispose();
            }
        };
        oeil.setContentAreaFilled(false);
        oeil.setBorderPainted(false);
        oeil.setFocusPainted(false);
        oeil.setCursor(new Cursor(Cursor.HAND_CURSOR));
        oeil.setPreferredSize(new Dimension(44, pf.getPreferredSize().height));
        oeil.setToolTipText("Afficher / masquer le mot de passe");

        oeil.addActionListener(e -> {
            mdpVisible[0] = !mdpVisible[0];
            pf.setEchoChar(mdpVisible[0] ? (char)0 : '\u2022');
            oeil.repaint();
        });

        // Wrapper avec BorderLayout — champ au centre, oeil à droite
        JPanel wrap = new JPanel(new BorderLayout(0, 0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Dessiner le fond arrondi du champ
                g2.setColor(FOND_CARD2);
                g2.fill(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 10, 10));
                // Bordure — bleue si le champ a le focus
                boolean hasFocus = pf.hasFocus();
                g2.setColor(hasFocus ? BLEU_ACCENT : BORDURE);
                g2.setStroke(new BasicStroke(hasFocus ? 2f : 1.2f));
                g2.draw(new RoundRectangle2D.Float(1, 1, getWidth()-2, getHeight()-2, 10, 10));
                if (hasFocus) {
                    g2.setColor(new Color(0, 140, 255, 35));
                    g2.setStroke(new BasicStroke(4f));
                    g2.draw(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 12, 12));
                }
                g2.dispose();
            }
        };
        wrap.setOpaque(false);

        // Le champ ne doit plus dessiner sa propre bordure (c'est le wrap qui le fait)
        pf.setBorder(new EmptyBorder(10, 14, 10, 6));
        pf.setOpaque(false);

        // Rafraîchir le wrap quand le focus change
        pf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override public void focusGained(java.awt.event.FocusEvent e) { wrap.repaint(); }
            @Override public void focusLost(java.awt.event.FocusEvent e)   { wrap.repaint(); }
        });

        wrap.add(pf, BorderLayout.CENTER);
        wrap.add(oeil, BorderLayout.EAST);

        Dimension d = pf.getPreferredSize();
        int totalW = d.width + 44;
        wrap.setPreferredSize(new Dimension(totalW, d.height));
        wrap.setMinimumSize(new Dimension(totalW, d.height));
        wrap.setMaximumSize(new Dimension(Short.MAX_VALUE, d.height));
        return wrap;
    }

    /** Stylise un JComboBox avec le thème sombre — force le rendu même sur Windows. */
    public static void styliserCombo(JComboBox<?> combo) {
        // Forcer UI de base pour éviter que Windows écrase les couleurs
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton btn = new JButton("v") {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g.create();
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(BLEU_ACCENT);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(Color.WHITE);
                        g2.setFont(new Font("Dialog", Font.BOLD, 10));
                        FontMetrics fm = g2.getFontMetrics();
                        String t = "▼";
                        g2.drawString(t, (getWidth()-fm.stringWidth(t))/2,
                                      (getHeight()+fm.getAscent()-fm.getDescent())/2);
                        g2.dispose();
                    }
                };
                btn.setBorder(BorderFactory.createEmptyBorder());
                btn.setPreferredSize(new Dimension(30, 0));
                return btn;
            }
            @Override public void paintCurrentValueBackground(Graphics g, Rectangle bounds, boolean hasFocus) {
                g.setColor(FOND_CARD2); g.fillRect(bounds.x, bounds.y, bounds.width, bounds.height);
            }
        });
        combo.setFont(new Font("Dialog", Font.PLAIN, 13));
        combo.setBackground(FOND_CARD2);
        combo.setForeground(TEXTE_BLANC);
        combo.setBorder(BorderFactory.createCompoundBorder(
            bordure("accent"),
            new EmptyBorder(0, 8, 0, 0)));
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(
                    JList<?> l, Object v, int i, boolean sel, boolean foc) {
                JLabel lbl = new JLabel(v == null ? "" : v.toString());
                lbl.setFont(new Font("Dialog", Font.PLAIN, 13));
                lbl.setForeground(Color.WHITE);
                lbl.setBackground(sel ? BLEU_ACCENT : FOND_CARD2);
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(8, 12, 8, 12));
                return lbl;
            }
        });
        // Popup list background
        Object child = combo.getAccessibleContext().getAccessibleChild(0);
        if (child instanceof javax.swing.plaf.basic.ComboPopup) {
            JList<?> list = ((javax.swing.plaf.basic.ComboPopup) child).getList();
            list.setBackground(FOND_CARD2);
            list.setForeground(TEXTE_BLANC);
            list.setSelectionBackground(BLEU_ACCENT);
            list.setSelectionForeground(Color.WHITE);
        }
    }

    // ── Table ──────────────────────────────────────────────────────
    public static void styliserTable(JTable table) {
        table.setFont(new Font(FONT_EMOJI, Font.PLAIN, 13));
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setShowHorizontalLines(true);
        table.setGridColor(new Color(30, 50, 90));
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(new Color(0, 100, 200, 100));
        table.setSelectionForeground(Color.WHITE);
        table.setBackground(FOND_CARD);
        table.setForeground(TEXTE_BLANC);
        table.setOpaque(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(new Font(FONT_EMOJI, Font.BOLD, 11));
        header.setBackground(new Color(15, 30, 65));
        header.setForeground(TEXTE_GRIS);
        header.setPreferredSize(new Dimension(header.getWidth(), 44));
        header.setBorder(UIHelper.bordure("accent"));
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                JLabel lbl = new JLabel("  " + String.valueOf(val).toUpperCase());
                lbl.setFont(new Font(FONT_EMOJI, Font.BOLD, 11));
                lbl.setForeground(new Color(100, 160, 230));
                lbl.setBackground(new Color(12, 24, 55));
                lbl.setOpaque(true);
                lbl.setBorder(new EmptyBorder(0, 8, 0, 8));
                return lbl;
            }
        });

        DefaultTableCellRenderer rowRenderer = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object val, boolean sel, boolean foc, int r, int c) {
                super.getTableCellRendererComponent(t, val, sel, foc, r, c);
                if (sel) { setBackground(new Color(0,100,200,100)); setForeground(Color.WHITE); }
                else { setBackground(r%2==0 ? FOND_CARD : new Color(20,36,72)); setForeground(TEXTE_BLANC); }
                setBorder(new EmptyBorder(0, 12, 0, 12));
                setOpaque(true);
                return this;
            }
        };
        for (int i=0; i<table.getColumnCount(); i++)
            if (table.getColumnModel().getColumn(i).getCellRenderer() == null ||
                table.getColumnModel().getColumn(i).getCellRenderer() instanceof DefaultTableCellRenderer)
                table.getColumnModel().getColumn(i).setCellRenderer(rowRenderer);
    }

    public static JScrollPane creerScrollPane(JTable table) {
        JScrollPane sp = new JScrollPane(table);
        appliquerBordure(sp, "table");
        sp.getViewport().setBackground(FOND_CARD);
        sp.setBackground(FOND_CARD);
        sp.getVerticalScrollBar().setBackground(FOND_DARK);
        sp.getHorizontalScrollBar().setBackground(FOND_DARK);
        rendreFluid(sp);
        return sp;
    }

    /**
     * Rend le défilement d'un JScrollPane fluide avec interpolation animée.
     */
    public static void rendreFluid(JScrollPane sp) {
        sp.getVerticalScrollBar().setUnitIncrement(16);
        sp.getVerticalScrollBar().setBlockIncrement(64);
        sp.getHorizontalScrollBar().setUnitIncrement(16);
        // Animation smooth scroll
        final double[] cible = {0};
        final double[] actuel = {0};
        final Timer[] timer = {null};
        // Supprimer anciens listeners
        for (java.awt.event.MouseWheelListener ml : sp.getMouseWheelListeners()) sp.removeMouseWheelListener(ml);
        sp.addMouseWheelListener(e -> {
            // Désactiver le scroll par défaut
            e.consume();
            // Calculer la cible selon la rotation molette
            double delta = e.getPreciseWheelRotation() * 40;
            cible[0] = Math.max(0, Math.min(
                cible[0] + delta,
                sp.getVerticalScrollBar().getMaximum() - sp.getVerticalScrollBar().getVisibleAmount()
            ));
            if (timer[0] != null) timer[0].stop();
            timer[0] = new Timer(8, ev -> {
                double diff = cible[0] - actuel[0];
                if (Math.abs(diff) < 0.5) {
                    actuel[0] = cible[0];
                    sp.getVerticalScrollBar().setValue((int) actuel[0]);
                    ((Timer) ev.getSource()).stop();
                } else {
                    // Interpolation exponentielle (easing)
                    actuel[0] += diff * 0.18;
                    sp.getVerticalScrollBar().setValue((int) actuel[0]);
                }
            });
            timer[0].start();
        });
        // Synchroniser actuel avec la valeur réelle lors d'un défilement externe
        sp.getVerticalScrollBar().addAdjustmentListener(e -> {
            if (!e.getValueIsAdjusting()) return;
            cible[0] = e.getValue();
            actuel[0] = e.getValue();
        });
    }



    // ── Sidebar helpers ────────────────────────────────────────────
    public static void marquerActif(JButton btn) {
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Dialog", Font.BOLD, 13));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, BLEU_ACCENT),
            new EmptyBorder(10, 13, 10, 6)));
        btn.putClientProperty("actif", true);
        btn.repaint();
    }
    public static void marquerInactif(JButton btn) {
        btn.setForeground(new Color(140, 170, 220));
        btn.setFont(new Font(FONT_EMOJI, Font.PLAIN, 13));
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setBorder(new EmptyBorder(10, 16, 10, 6));
        btn.putClientProperty("actif", false);
        btn.repaint();
    }

    public static JButton creerItemSidebar(String icone, String texte) {
        JButton btn = new JButton(icone + "  " + texte) {
            @Override protected void paintComponent(Graphics g) {
                if (getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255,255,255,15));
                    g2.fill(new RoundRectangle2D.Float(6,2,getWidth()-12,getHeight()-4,8,8));
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        btn.setForeground(new Color(140,170,220));
        btn.setFont(new Font(FONT_EMOJI, Font.PLAIN, 13));
        btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setBorder(new EmptyBorder(11,20,11,20));
        return btn;
    }

    // ── Dialogs ────────────────────────────────────────────────────
    public static void afficherMessage(Component p, String msg, String titre, int type) {
        JOptionPane.showMessageDialog(p, msg, titre, type);
    }
    public static void afficherErreur(Component p, String msg)  { afficherMessage(p, msg, "Erreur", JOptionPane.ERROR_MESSAGE); }
    public static void afficherSucces(Component p, String msg)  { afficherMessage(p, msg, "Succès", JOptionPane.INFORMATION_MESSAGE); }
    // ══════════════════════════════════════════════════════════════
    //  BORDURE ARRONDIE UNIVERSELLE
    // ══════════════════════════════════════════════════════════════

    public static class RoundedBorder extends AbstractBorder {
        private final Color couleur; private final float ep; private final int r, pad;
        public RoundedBorder(Color couleur, float ep, int r, int pad) {
            this.couleur=couleur; this.ep=ep; this.r=r; this.pad=pad;
        }
        @Override public void paintBorder(Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2=(Graphics2D)g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(couleur); g2.setStroke(new BasicStroke(ep));
            float off=ep/2f;
            g2.draw(new java.awt.geom.RoundRectangle2D.Float(x+off,y+off,w-ep,h-ep,r,r));
            g2.dispose();
        }
        @Override public Insets getBorderInsets(Component c){return new Insets(pad,pad,pad,pad);}
        @Override public Insets getBorderInsets(Component c,Insets i){i.set(pad,pad,pad,pad);return i;}
    }

    public static RoundedBorder bordure(String type) {
        return switch(type) {
            case "accent" -> new RoundedBorder(new Color(0,140,255,130),1.5f,12,0);
            case "card"   -> new RoundedBorder(new Color(30,70,150,90), 1.2f,14,0);
            case "dialog" -> new RoundedBorder(new Color(0,140,255,180),2.0f,16,0);
            case "table"  -> new RoundedBorder(new Color(20,60,130,110),1.2f,10,0);
            default       -> new RoundedBorder(new Color(40,80,160,100),1.2f,10,0);
        };
    }

    public static void appliquerBordure(JScrollPane sp, String type) {
        sp.setBorder(bordure(type)); sp.setViewportBorder(null);
    }

    public static int confirmer(Component p, String msg) {
        return JOptionPane.showConfirmDialog(p, msg, "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
    }

    public static JPanel creerPanneauAvecTitre(String titre) {
        JPanel p = new JPanel(new BorderLayout(0,10));
        p.setBackground(FOND_DARK); p.setBorder(new EmptyBorder(15,20,15,20));
        if (titre != null) p.add(creerTitre(titre), BorderLayout.NORTH);
        return p;
    }
    public static JSeparator creerSeparateur() {
        JSeparator sep = new JSeparator(); sep.setForeground(BORDURE); return sep;
    }
}
