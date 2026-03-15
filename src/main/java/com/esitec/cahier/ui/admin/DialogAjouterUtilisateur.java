package com.esitec.cahier.ui.admin;

import com.esitec.cahier.dao.UtilisateurDAO;
import com.esitec.cahier.model.Utilisateur;
import com.esitec.cahier.util.UIHelper;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;

/**
 * Créer un utilisateur par le chef → statut VALIDE directement, pas de validation requise.
 */
public class DialogAjouterUtilisateur extends JDialog {

    private JTextField     champNom, champPrenom, champEmail;
    private JPasswordField champMdp, champMdpConfirm;
    private JComboBox<String> comboRole;
    private JLabel         lblErreur;
    private final UtilisateurDAO dao;
    private final Runnable onSuccess;

    public DialogAjouterUtilisateur(Frame parent, UtilisateurDAO dao, Runnable onSuccess) {
        super(parent, "Ajouter un utilisateur", true);
        this.dao = dao; this.onSuccess = onSuccess;
        initialiserUI();
    }

    private void initialiserUI() {
        setSize(520, 620);
        setLocationRelativeTo(getParent());
        setResizable(false);
        getRootPane().setBorder(UIHelper.bordure("dialog"));

        JPanel root = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(UIHelper.FOND_DARK); g.fillRect(0,0,getWidth(),getHeight());
            }
        };
        root.setOpaque(false);

        // Header
        JPanel header = new JPanel(new BorderLayout(14,0)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D)g.create();
                g2.setPaint(new GradientPaint(0,0,new Color(0,100,180),getWidth(),getHeight(),new Color(0,55,130)));
                g2.fillRect(0,0,getWidth(),getHeight()); g2.dispose();
            }
        };
        header.setOpaque(false); header.setBorder(new EmptyBorder(20,24,20,24));
        JLabel ico = new JLabel("👤");
        ico.setFont(new Font(UIHelper.FONT_EMOJI,Font.PLAIN,32));
        JPanel htx = new JPanel(new GridLayout(2,1,0,4)); htx.setOpaque(false);
        JLabel t = new JLabel("Ajouter un utilisateur");
        t.setFont(new Font(UIHelper.FONT_UI,Font.BOLD,18)); t.setForeground(Color.WHITE);
        JLabel s = new JLabel("Le compte sera activé immédiatement (sans validation)");
        s.setFont(new Font(UIHelper.FONT_UI,Font.PLAIN,11)); s.setForeground(new Color(160,210,255));
        htx.add(t); htx.add(s);
        header.add(ico,BorderLayout.WEST); header.add(htx,BorderLayout.CENTER);
        root.add(header,BorderLayout.NORTH);

        // Formulaire
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(UIHelper.FOND_CARD);
        form.setBorder(new EmptyBorder(22,28,20,28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill=GridBagConstraints.HORIZONTAL; gc.gridx=0; gc.weightx=1;

        // Nom + Prénom
        gc.gridy=0; gc.insets=new Insets(0,0,12,0);
        JPanel np = new JPanel(new GridLayout(1,2,12,0)); np.setOpaque(false);
        JPanel pN = champPanel("Nom *"); champNom=UIHelper.creerChamp(15);
        champNom.setPreferredSize(new Dimension(100,44)); pN.add(champNom);
        JPanel pP = champPanel("Prénom *"); champPrenom=UIHelper.creerChamp(15);
        champPrenom.setPreferredSize(new Dimension(100,44)); pP.add(champPrenom);
        np.add(pN); np.add(pP); form.add(np,gc);

        // Email
        gc.gridy=1; gc.insets=new Insets(0,0,3,0); form.add(UIHelper.lblFormulaire("✉  Adresse email *"),gc);
        gc.gridy=2; gc.insets=new Insets(0,0,12,0);
        champEmail=UIHelper.creerChamp(30); champEmail.setPreferredSize(new Dimension(450,44));
        form.add(champEmail,gc);

        // MDP
        gc.gridy=3; gc.insets=new Insets(0,0,3,0); form.add(UIHelper.lblFormulaire("🔒  Mot de passe * (min. 6 car.)"),gc);
        gc.gridy=4; gc.insets=new Insets(0,0,12,0);
        champMdp=UIHelper.creerChampMotDePasse(30); champMdp.setPreferredSize(new Dimension(450,44));
        form.add(UIHelper.wrapAvecOeil(champMdp),gc);

        // Confirmation MDP
        gc.gridy=5; gc.insets=new Insets(0,0,3,0); form.add(UIHelper.lblFormulaire("🔒  Confirmer le mot de passe *"),gc);
        gc.gridy=6; gc.insets=new Insets(0,0,12,0);
        champMdpConfirm=UIHelper.creerChampMotDePasse(30); champMdpConfirm.setPreferredSize(new Dimension(450,44));
        form.add(UIHelper.wrapAvecOeil(champMdpConfirm),gc);

        // Rôle
        gc.gridy=7; gc.insets=new Insets(0,0,3,0); form.add(UIHelper.lblFormulaire("👤  Rôle *"),gc);
        gc.gridy=8; gc.insets=new Insets(0,0,14,0);
        comboRole=new JComboBox<>(new String[]{"🎓  Enseignant","📋  Responsable de classe"});
        UIHelper.styliserCombo(comboRole); comboRole.setPreferredSize(new Dimension(450,44));
        form.add(comboRole,gc);

        // Erreur
        gc.gridy=9; gc.insets=new Insets(0,0,10,0);
        lblErreur=new JLabel(" ");
        lblErreur.setFont(new Font(UIHelper.FONT_UI,Font.PLAIN,12));
        lblErreur.setForeground(UIHelper.ROUGE_BADGE);
        form.add(lblErreur,gc);

        // Boutons
        gc.gridy=10; gc.insets=new Insets(0,0,0,0);
        JPanel btns=new JPanel(new GridLayout(1,2,12,0)); btns.setOpaque(false);
        JButton btnAnn=UIHelper.creerBouton("✕  Annuler",new Color(50,60,90));
        JButton btnSave=UIHelper.creerBoutonSucces("✓  Créer le compte");
        btnAnn.setPreferredSize(new Dimension(190,46)); btnSave.setPreferredSize(new Dimension(190,46));
        btnAnn.addActionListener(e->dispose());
        btnSave.addActionListener(e->sauvegarder());
        champMdpConfirm.addActionListener(e->sauvegarder());
        btns.add(btnAnn); btns.add(btnSave);
        form.add(btns,gc);

        root.add(form,BorderLayout.CENTER);
        add(root);
    }

    private JPanel champPanel(String label) {
        JPanel p=new JPanel(new BorderLayout(0,4)); p.setOpaque(false);
        p.add(UIHelper.lblFormulaire(label),BorderLayout.NORTH); return p;
    }

    private void sauvegarder() {
        String nom=champNom.getText().trim(), prenom=champPrenom.getText().trim();
        String email=champEmail.getText().trim();
        String mdp=new String(champMdp.getPassword()), mdpC=new String(champMdpConfirm.getPassword());
        if (nom.isEmpty()||prenom.isEmpty()||email.isEmpty()||mdp.isEmpty()) {
            lblErreur.setText("⚠  Remplissez tous les champs obligatoires."); return; }
        if (!email.contains("@")||!email.contains(".")) {
            lblErreur.setText("⚠  Adresse email invalide."); return; }
        if (mdp.length()<6) {
            lblErreur.setText("⚠  Mot de passe trop court (minimum 6 caractères)."); return; }
        if (!mdp.equals(mdpC)) {
            lblErreur.setText("⚠  Les mots de passe ne correspondent pas."); return; }
        Utilisateur.Role role=switch(comboRole.getSelectedIndex()){
            case 1->Utilisateur.Role.RESPONSABLE_CLASSE;
            default->Utilisateur.Role.ENSEIGNANT;
        };
        Utilisateur u=new Utilisateur(nom,prenom,email,mdp,role);
        u.setStatut(Utilisateur.Statut.VALIDE); // Créé directement actif
        if (dao.creerDirectement(u)) {
            UIHelper.afficherSucces(this,"Utilisateur créé et activé ✓");
            if (onSuccess!=null) onSuccess.run();
            dispose();
        } else {
            lblErreur.setText("⚠  Erreur : cet email est peut-être déjà utilisé.");
        }
    }
}
