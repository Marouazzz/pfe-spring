#  Gestion des Soutenances de PFE

[![Java](https://img.shields.io/badge/Java-17-orange?logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.5-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-Database-blue?logo=mysql)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Build-Maven-red?logo=apache-maven)](https://maven.apache.org/)


Un système de gestion complet et intelligent pour l'organisation et le suivi des soutenances de Projets de Fin d'Études (PFE). Ce projet automatise la génération des plannings, l'affectation des jurys, et la gestion administrative associée.

##  Table des matières

- [Présentation](#présentation)
- [Fonctionnalités](#fonctionnalités)
- [Architecture](#architecture)
- [Technologies utilisées](#technologies-utilisées)
- [Installation](#installation)
- [Utilisation](#utilisation)
- [Structure du projet](#structure-du-projet)
- [Workflows](#workflows)
- [Perspectives d'évolution](#perspectives-dévolution)
- [Contributeurs](#contributeurs)
- [Licence](#licence)

##  Présentation

Ce projet Spring Boot offre une plateforme centralisée pour gérer l'intégralité du cycle de vie des soutenances de PFE. Les objectifs principaux sont :

 **Gestion complète des soutenances** — Importation, affectation, planification et suivi.

 **Génération automatisée du planning** — Algorithmes d'optimisation avec respect strict des contraintes.

 **Gestion des affectations** — Assignation intelligente des jurys et des encadrants.

 **Génération administrative** — Création automatique des procès-verbaux (PV) en format Word.

 **Tableau de bord analytique** — Visualisation en temps réel des données et des anomalies.

 **Gestion multi-versions** — Support de multiples campagnes d'importation/planification.
##  Démo Vidéo

 **[Regarder la démo complète sur Google Drive](https://drive.google.com/drive/folders/1QUVHjKsvHZ1tSZ4D5amzbYrwgBoPck1Z?usp=drive_link)**
##  Fonctionnalités

###  1. Import et Gestion des Données

- **Import Excel multi-sources** — Importation des étudiants, professeurs, salles et PFEs depuis un fichier Excel structuré.
- **Gestion des versions** — Possibilité de maintenir plusieurs versions de données (idempotence sur les imports).
- **Validation automatique** — Vérification de la conformité des données importées.
- **Gestion des filières** — Support de multiples filières/spécialités d'études.

###  2. Gestion des Entités

- **Étudiants** — Gestion des étudiants avec CNE, nom, prénom, filière et PFE associé.
- **Professeurs** — Gestion des enseignants avec spécialités et disponibilités.
- **Encadrants** — Attribution des encadrants aux PFEs avec gestion des charges.
- **Jurys** — Affectation automatique des jurys (encadrant + 2 professeurs).
- **Salles** — Gestion des salles de soutenance avec capacité et disponibilité.

###  3. Planification Intelligente

- **Génération automatique du planning** — Algorithmes d'optimisation pour placer les soutenances.
- **Respect des contraintes métier** — Évite les conflits de salles, de professeurs et de jurys.
- **Stratégies multiples** — Mode strict (respecte toutes les contraintes) et mode optimisé (maximise les soutenances placées).
- **Configuration flexible** — Paramétrage des jours de soutenance, durée des créneaux, etc.
- **Rapports de comparaison** — Analyse détaillée des différences entre solutions.

###  4. Génération Administrative

- **Génération des procès-verbaux** — Création automatique des PV en format Word (`.docx`) pour chaque soutenance.
- **Archive ZIP** — Regroupement de tous les PV dans une archive téléchargeable.
- **Documents structurés** — PV professionnels avec informations du jury, de l'étudiant et de la soutenance.

###  5. Tableau de Bord Analytique

- **Statistiques globales** — Total PFEs, encadrants, soutenances, salles, jours.
- **PFEs par encadrant** — Visualisation graphique de la charge d'encadrement.
- **Soutenances par filière** — Répartition des soutenances par domaine d'études.
- **Soutenances par professeur** — Charge des enseignants dans les jurys.
- **Anomalies et alertes** — Détection des problèmes d'affectation ou de surcharge.
- **Indicateurs de qualité** — Métriques de l'utilisation des salles et des créneaux.

###  6. Export et Téléchargement

- **Export Excel** — Export du planning en fichier Excel structuré.
- **Export PDF** — Génération du planning en format PDF.
- **Téléchargement des PV** — Archive ZIP contenant tous les procès-verbaux générés.

###  7. Gestion de Session

- **Versions de données en session** — Chaque session utilisateur peut avoir sa version active.
- **Persistance des données** — Toutes les données sont stockées dans la base de données MySQL.

##  Architecture

### Couches applicatives

```
┌─────────────────────────────────────────────────┐
│          Couche Présentation (Vue)              │
│  (Thymeleaf HTML + CSS + Charts.js)             │
├─────────────────────────────────────────────────┤
│          Couche Contrôleur (Web)                │
│  (Spring MVC - @RestController, @GetMapping)    │
├─────────────────────────────────────────────────┤
│          Couche Métier (Services)               │
│  (Logique d'affectation, planification, export) │
├─────────────────────────────────────────────────┤
│          Couche Données (Persistence)           │
│  (JPA/Hibernate + Repositories)                 │
├─────────────────────────────────────────────────┤
│          Base de Données MySQL                  │
└─────────────────────────────────────────────────┘
```

### Services principaux

| Service | Responsabilité |
|---------|----------------|
| **PFEService** | Import Excel et affectation des PFEs |
| **SchedulingService** | Orchestration de la planification |
| **SchedulingExportService** | Export Excel et PDF des plannings |
| **DashboardService** | Calcul des statistiques et anomalies |
| **FileSystemService** | Génération des PV et ZIP |
| **SalleService** | Import et gestion des salles rt soutenances |
| **ProfService** | Import et gestion des professeurs |
| **JuryService** | Affectation des jurys et génération des PV |

### Entités de base de données

```
┌──────────────┐      ┌──────────────┐
│  Etudiant    │      │     Prof     │
└──────────────┘      └──────────────┘
       │                    │
       │ encadrant          │
       ▼                    ▼
┌──────────────┐      ┌──────────────┐
│     PFE      │◄─────┤  Encadrant   │
└──────────────┘      └──────────────┘
       │
       │ jury
       ▼
┌──────────────┐      ┌──────────────┐
│     Jury     │      │   Soutenance │
└──────────────┘      └──────────────┘
       │                    │
       └───────────┬────────┘
                   │ salle
                   ▼
              ┌──────────────┐
              │    Salle     │
              └──────────────┘
```

##  Technologies utilisées

### Backend

| Technologie | Version | Usage |
|-------------|---------|-------|
| **Java** | 17 | Langage de programmation |
| **Spring Boot** | 4.0.5 | Framework web et IoC |
| **Spring Data JPA** | - | ORM et persistance |
| **Thymeleaf** | - | Moteur de templates |
| **MySQL Connector** | - | Driver JDBC MySQL |
| **Lombok** | - | Génération de code (getters, setters, constructeurs) |
| **Validation** | - | Validation JSR-303 |


### Génération de documents

| Librairie | Version | Usage |
|-----------|---------|-------|
| **Apache POI** | 5.2.5 | Lecture/écriture Excel |
| **docx4j** | 11.5.12 | Génération de documents Word |
| **OpenPDF** | 1.3.30 | Génération de fichiers PDF |

### Frontend

| Technologie | Usage |
|-------------|-------|
| **HTML5** | Structure des pages |
| **CSS3** | Styling et responsiveness |
| **Chart.js** | Visualisation graphique (dashboards) |
| **Thymeleaf** | Templating serveur-side |

### Build et déploiement

| Outil | Usage |
|------|-------|
| **Maven** | Gestion des dépendances et build |
| **Spring Boot Maven Plugin** | Packaging de l'application |

### Base de données

| Base | Version | Usage |
|------|---------|-------|
| **MySQL** | - | Stockage persistant des données |

##  Installation

### Prérequis

-  **Java 17** ou supérieur
-  **Maven 3.8+**
-  **MySQL 8.0+**
-  **Git**

### Étapes d'installation

#### 1. Cloner le dépôt

```bash
git clone https://github.com/Marouazzz/pfe-spring.git
cd pfe-spring
```

#### 2. Configurer la base de données

Créer une base de données MySQL :

```sql
CREATE DATABASE pfe_soutenances;
USE pfe_soutenances;
```

Mettre à jour le fichier `application.properties` :

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/pfe_soutenances
spring.datasource.username=root
spring.datasource.password=your_password
spring.jpa.hibernate.ddl-auto=update
```

#### 3. Installer les dépendances

```bash
mvn clean install
```

#### 4. Lancer l'application

```bash
mvn spring-boot:run
```

L'application sera accessible à : **http://localhost:8080/home** ou autre port si configurable

##  Utilisation

### Flux de travail principal

```
┌─────────────────────────────────────────────────────────┐
│ ÉTAPE 1 : Import Excel                                  │
│ └─ Importer le fichier avec étudiants, profs, salles    │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ ÉTAPE 2 : Affectation des encadrants                    │
│ └─ Assignation automatique des PFEs aux encadrants      │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ ÉTAPE 3 : Affectation des jurys                         │
│ └─ Sélection de 2 professeurs supplémentaires           │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ ÉTAPE 4 : Planification des soutenances                 │
│ └─ Configuration et génération du planning optimisé      │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ ÉTAPE 5 : Visualisation et validation                   │
│ └─ Comparaison strict vs optimisé + choix final         │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ ÉTAPE 6 : Génération administrative                     │
│ └─ Création des PV et export des documents              │
└─────────────────────────────────────────────────────────┘
                         ↓
┌─────────────────────────────────────────────────────────┐
│ ÉTAPE 7 : Dashboard et suivi                            │
│ └─ Consultation des statistiques et anomalies           │
└─────────────────────────────────────────────────────────┘
```

### Formatage du fichier Excel

Le fichier Excel doit contenir les feuilles suivantes :

#### **Feuille : `etudiants`**

| Colonne | Format | Description |
|---------|--------|-------------|
| CNE | Texte | Code national de l'étudiant |
| Nom | Texte | Nom de l'étudiant |
| Prénom | Texte | Prénom de l'étudiant |
| Filière | Texte | Filière/spécialité d'étude |

#### **Feuille : `profs`**

| Colonne | Format | Description |
|---------|--------|-------------|
| Nom | Texte | Nom du professeur |
| Prénom | Texte | Prénom du professeur |
| Spécialité | Texte | Spécialité/discipline |

#### **Feuille : `pfe`**

| Colonne | Format | Description |
|---------|--------|-------------|
| CNE | Texte | Code de l'étudiant PFE |
| Sujet | Texte | Sujet du PFE |
| Encadrant | Texte | Nom de l'encadrant |
| Langue | Texte | Langue de la soutenance |
| Filière | Texte | Filière du PFE |

#### **Feuille : `salles`**

| Colonne | Format | Description |
|---------|--------|-------------|
| Nom salle | Texte | Identifiant de la salle |
| Capacité | Nombre | Capacité (nombre de places) |
| Disponible | Booléen | Disponibilité (Oui/Non) |

#### **Feuille : `jours_soutenances`**

| Colonne | Format | Description |
|---------|--------|-------------|
| Date début | Date | Date de début des soutenances |

### Accès aux interfaces

| Interface | URL | Objectif |
|-----------|-----|----------|
| **Orchestration** | `/home` | Workflow complet (import → planification) |
| **Tableau de bord** | `/dashboard` | Visualisation des statistiques |
| **Planification** | `/scheduling/form` | Configuration du planning |
| **Résultats** | `/scheduling/result` | Comparaison et validation des solutions |

##  Structure du projet

```
pfe-spring/
├── src/
│   ├── main/
│   │   ├── java/org/sid/pfespring/
│   │   │   ├── PfeSpringApplication.java
│   │   │   ├── model/                    # Entités JPA
│   │   │   │   ├── PFE.java
│   │   │   │   ├── Etudiant.java
│   │   │   │   ├── Prof.java
│   │   │   │   ├── Encadrant.java
│   │   │   │   ├── Jury.java
│   │   │   │   ├── Soutenance.java
│   │   │   │   ├── Salle.java
│   │   │   │   ├── ImportVersion.java
│   │   │   │   ├── Filiere.java (enum)
│   │   │   │   └── Status.java (enum)
│   │   │   ├── services/                 # Couche métier
│   │   │   │   ├── PFEService.java
│   │   │   │   ├── PFEServiceImpl.java
│   │   │   │   ├── SchedulingService.java
│   │   │   │   ├── SchedulingExportService.java
│   │   │   │   ├── DashboardService.java
│   │   │   │   ├── FileSystemService.java
│   │   │   │   ├── JuryService.java
│   │   │   │   ├── SalleService.java
│   │   │   │   └── scheduling/
│   │   │   │       └── (Services de planification)
│   │   │   ├── repository/               # Couche persistance
│   │   │   │   ├── PFERepository.java
│   │   │   │   ├── EtudiantRepository.java
│   │   │   │   ├── ProfRepository.java
│   │   │   │   └── ...
│   │   │   ├── dto/                      # Objets de transfert
│   │   │   │   ├── RequestPFEDTO.java
│   │   │   │   ├── ResponsePFEDTO.java
│   │   │   │   └── ...
│   │   │   ├── mapper/                   # Conversion Entity ↔ DTO
│   │   │   ├── controller/               # Contrôleurs Web
│   │   │   │   ├── HomeController.java
│   │   │   │   ├── DashboardController.java
│   │   │   │   └── SchedulingController.java
│   │   │   ├── exception/                # Exceptions métier
│   │   │   │   ├── BusinessException.java
│   │   │   │   ├── EtudiantNotFoundException.java
│   │   │   │   └── ...
│   │   │   └── utils/                    # Utilitaires
│   │   ├── resources/
│   │   │   ├── templates/
│   │   │   │   ├── upload.html           # Page d'orchestration
│   │   │   │   ├── dashboard.html        # Dashboard
│   │   │   │   ├── erreur.html
│   │   │   │   ├── scheduling/
│   │   │   │   │   ├── form.html         # Formulaire de planification
│   │   │   │   │   └── result.html       # Résultats
│   │   │   │   └── wp_error.html
│   │   │   ├── static/
│   │   │   │   └── css/
│   │   │   │       ├── upload.css
│   │   │   │       ├── dashboard.css
│   │   │   │       └── result.css
│   │   │   └── application.properties    # Configuration
│   └── test/
│       └── java/org/sid/pfespring/      # Tests
├── pom.xml                              # Configuration Maven
└── README.md                            # Ce fichier
```

##  Workflows

### Workflow 1 : Import et initialisation

```
Fichier Excel → Validation → Stockage en DB → Version créée
```

**Services impliqués** : `PFEService`, `SalleService`, `ProfService`

### Workflow 2 : Affectation des jurys

```
PFEs importés → Sélection encadrants → Sélection 2 profs → Jury créé
```

**Services impliqués** : `JuryService`, `PFEService`

### Workflow 3 : Planification

```
Configuration → Génération (mode strict) → Génération (mode optimisé) → Comparaison
```

**Services impliqués** : `SchedulingService`, `SchedulingExportService`

### Workflow 4 : Génération administrative

```
Soutenances validées → Génération PV Word → Archivage ZIP → Téléchargement
```

**Services impliqués** : `FileSystemService`, `JuryService`

##  Perspectives d'évolution

### Court terme

-  **Notifications** — Alertes email pour les changements de planning.
-  **API REST** — Endpoints publics pour intégration externe.
-  **Authentification** — Implémentation de Spring Security avec rôles (Admin, Professeur, Étudiant).

### Moyen terme

-  **Application mobile** — Interface mobile pour consultation du planning.
-  **Rapports avancés** — Génération de rapports Excel complexes avec formules.
-  **IA pour optimisation** — Machine Learning pour améliorer l'algorithme de planification.
-  **Multilingue** — Support de plusieurs langues (FR, EN, AR).

### Long terme

-  **Déploiement Cloud** — Migration vers AWS/Azure/GCP.
-  **Intégration d'annuaires** — Connexion avec LDAP/Active Directory.
-  **Analytics avancées** — Dashboards interactifs avec Tableau/Power BI.
-  **SSO** — Intégration OAuth 2.0 et SAML.

##  Contributeurs
  **https://github.com/ae-saouiqui**  |  **https://github.com/hajaryaz** 



---

**Dernière mise à jour** : Mai 2026

**Dépôt** : [GitHub - Marouazzz/pfe-spring](https://github.com/Marouazzz/pfe-spring)

**Support** : Pour les questions ou les bugs, veuillez créer une [issue](https://github.com/Marouazzz/pfe-spring/issues).
