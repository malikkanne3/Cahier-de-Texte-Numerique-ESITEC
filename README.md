# 📚 Cahier de Texte Numérique — ESITEC

> **Projet POO Java** — Groupe SUP de CO Dakar / ESITEC  
> Date de présentation : **Mardi 17 Mars 2026**

---

## 📋 Description

Application de gestion numérique du cahier de texte avec génération de fiches de suivi pédagogique.  
Développée en **Java avec Swing**, base de données **SQLite**, et export **PDF/Excel**.

---

## 👥 Utilisateurs et rôles

| Rôle | Fonctionnalités |
|------|----------------|
| **Chef de département** | Gestion des utilisateurs, cours, statistiques globales, validation des comptes, export de fiches |
| **Enseignant** | Consultation de ses cours, ajout/modification de séances (avant validation), historique |
| **Responsable de classe** | Consultation du cahier de texte, validation/rejet des séances, suivi d'avancement, export |

---

## 🚀 Lancement de l'application

### Prérequis
- Java 17+
- Maven 3.8+

### Compilation et exécution
```bash
# Cloner le projet
git clone <url-du-repo>
cd cahier-de-texte

# Compiler et packager
mvn clean package -DskipTests

# Lancer l'application
java -jar target/cahier-de-texte-1.0.0-jar-with-dependencies.jar
```

### Comptes de démonstration
| Email | Mot de passe | Rôle |
|-------|-------------|------|
| `admin@esitec.sn` | `admin123` | Chef de département |
| `enseignant@esitec.sn` | `demo123` | Enseignant |
| `responsable@esitec.sn` | `demo123` | Responsable de classe |

---

## 🏗️ Architecture du projet

```
src/
├── main/java/com/esitec/cahier/
│   ├── Main.java                          # Point d'entrée
│   ├── model/                             # Entités métier
│   │   ├── Utilisateur.java
│   │   ├── Cours.java
│   │   └── Seance.java
│   ├── dao/                               # Accès aux données (SQLite)
│   │   ├── DatabaseManager.java
│   │   ├── UtilisateurDAO.java
│   │   ├── CoursDAO.java
│   │   └── SeanceDAO.java
│   ├── service/                           # Logique métier
│   ├── ui/                                # Interface graphique (Swing)
│   │   ├── common/                        # Connexion, inscription
│   │   ├── admin/                         # Dashboard Chef de département
│   │   ├── enseignant/                    # Dashboard Enseignant
│   │   └── responsable/                   # Dashboard Responsable de classe
│   └── util/                              # Utilitaires
│       ├── UIHelper.java                  # Composants graphiques
│       ├── ExportPDF.java                 # Génération PDF (iText)
│       └── ExportExcel.java               # Génération Excel (Apache POI)
└── test/java/com/esitec/cahier/
    └── CahierDeTexteTest.java             # Tests JUnit 5
```

---

## 🔧 Technologies utilisées

| Technologie | Usage |
|-------------|-------|
| **Java 17** | Langage principal |
| **Swing** | Interface graphique |
| **SQLite** | Base de données embarquée |
| **Maven** | Gestion des dépendances |
| **iText 5** | Génération de PDF |
| **Apache POI** | Génération Excel |
| **JUnit 5** | Tests unitaires |
| **BCrypt** | Hachage des mots de passe |

---

## 🗄️ Modèle de données

### Table `utilisateurs`
```sql
id, nom, prenom, email, mot_de_passe, role, statut
```

### Table `cours`
```sql
id, intitule, description, enseignant_id, classe_nom, volume_horaire_prevu
```

### Table `seances`
```sql
id, cours_id, enseignant_id, date, heure, duree_minutes, contenu, observations, statut, commentaire_rejet
```

---

## ✅ Fonctionnalités implémentées

- [x] Authentification par rôle avec hachage BCrypt
- [x] Gestion des utilisateurs (CRUD) et validation des comptes
- [x] Assignation des cours aux enseignants
- [x] Ajout/modification/suppression de séances (avant validation)
- [x] Workflow de validation/rejet des séances par le responsable
- [x] Consultation de l'historique des séances
- [x] Suivi de l'avancement du programme (heures effectuées vs prévues)
- [x] Génération de fiches de suivi au format **PDF** (iText)
- [x] Génération de fiches de suivi au format **Excel** (Apache POI)
- [x] Statistiques globales pour le chef de département
- [x] Tests unitaires (JUnit 5)

---

## 🧪 Lancer les tests

```bash
mvn test
```

---

## 👨‍💻 Équipe

Projet réalisé en équipe de 2 — ESITEC 2026

## Equipe
- Malik KANNE
- Nathanael BALAMOU

- Revision DAO par Nathanael
