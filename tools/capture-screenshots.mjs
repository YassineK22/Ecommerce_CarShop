// Capture d'ecran de toutes les interfaces d'AutoHub.
// Playwright headless, viewport 1366x768, PNG pleine page.
//
// Lancement (depuis le dossier demo) :
//   $env:APP_ADMIN_EMAIL = "..."   # obligatoire pour les 3 pages admin
//   $env:APP_ADMIN_PASSWORD = "..."
//   node tools/capture-screenshots.mjs
//
// Les comptes de demonstration sont ceux du README (mot de passe Passer1234).
// Aucun identifiant n'est ecrit dans ce fichier : l'admin vient uniquement
// des variables d'environnement APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD.

import { chromium } from 'playwright-core';
import { mkdir, writeFile } from 'node:fs/promises';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
const projectRoot = path.resolve(here, '..');
const outDir = path.join(projectRoot, 'docs', 'screenshots');

const BASE = process.env.APP_BASE_URL || 'http://localhost:8080';
const VIEWPORT = { width: 1366, height: 768 };

const DEMO_PASSWORD = 'Passer1234';
const ACCOUNTS = {
  hamza: { email: 'hamza@autohub.tn', password: DEMO_PASSWORD, label: 'Hamza Vendeur' },
  ahmed: { email: 'ahmed@autohub.tn', password: DEMO_PASSWORD, label: 'Ahmed Encherisseur' },
  autoplus: { email: 'autoplus@autohub.tn', password: DEMO_PASSWORD, label: 'AutoPlus Tunisie' },
  admin: {
    email: process.env.APP_ADMIN_EMAIL,
    password: process.env.APP_ADMIN_PASSWORD,
    label: 'Administrateur',
  },
};

const SHOTS = [
  // --- Public, non connecte ---
  { file: '01-accueil.png', url: '/', label: 'Accueil public', role: 'anon' },
  { file: '02-annonces.png', url: '/annonces', label: 'Liste des annonces avec recherche et filtres', role: 'anon' },
  { file: '03-annonces-filtres.png', url: '/annonces?make=Renault&maxPrice=80000', label: 'Liste filtree (Renault, 80 000 DT max)', role: 'anon' },
  { file: '04-annonces-page-2.png', url: '/annonces?page=1', label: 'Liste des annonces, page 2 (pagination)', role: 'anon' },
  { file: '05-detail-prix-fixe.png', url: '/annonces/5', label: 'Detail annonce a prix fixe (Renault Clio 5 Neuve)', role: 'anon' },
  { file: '06-detail-enchere.png', url: '/annonces/2', label: "Detail enchere cloturee avec l'historique des offres", role: 'anon' },
  { file: '07-detail-enchere-ouverte.png', url: '/annonces/6', label: "Detail enchere ouverte avec le formulaire d'offre", role: 'ahmed' },
  { file: '08-login.png', url: '/login', label: 'Connexion', role: 'anon' },
  { file: '09-register.png', url: '/register', label: 'Inscription', role: 'anon' },

  // --- Utilisateur connecte ---
  { file: '10-dashboard.png', url: '/', label: 'Tableau de bord personnel', role: 'ahmed' },
  { file: '11-annonce-new.png', url: '/annonces/new', label: 'Publier une annonce (formulaire)', role: 'ahmed' },
  { file: '12-annonce-edit.png', url: '/annonces/6/edit', label: 'Modifier une annonce (formulaire)', role: 'hamza' },
  { file: '13-mes-favoris.png', url: '/mes-favoris', label: 'Mes favoris', role: 'ahmed' },
  { file: '14-mes-messages.png', url: '/mes-messages', label: 'Mes messages (boite de reception)', role: 'autoplus' },
  { file: '15-profil.png', url: '/profile', label: 'Mon profil', role: 'ahmed' },
  { file: '16-comparer.png', url: '/comparer?ids=5,7,9', label: 'Comparaison de 3 annonces', role: 'ahmed' },

  // --- Administration ---
  { file: '17-admin-dashboard.png', url: '/admin', label: 'Administration : tableau de bord', role: 'admin' },
  { file: '18-admin-annonces.png', url: '/admin/annonces', label: 'Administration : annonces (signalees en premier)', role: 'admin' },
  { file: '19-admin-utilisateurs.png', url: '/admin/utilisateurs', label: 'Administration : utilisateurs', role: 'admin' },

  // --- Swagger ---
  { file: '20-swagger.png', url: '/swagger-ui.html', label: 'Swagger UI (/swagger-ui.html)', role: 'anon' },
];

async function login(page, account) {
  await page.goto(`${BASE}/login`, { waitUntil: 'domcontentloaded' });
  await page.fill('input[id="username"]', account.email);
  await page.fill('input[id="password"]', account.password);
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'networkidle' }),
    page.click('form[action*="/login"] button[type="submit"]'),
  ]);
  if (page.url().includes('/login')) {
    throw new Error(`Connexion echouee pour ${account.email}`);
  }
  // Garde-fou : la session doit bien etre celle du compte demande.
  await page.goto(`${BASE}/`, { waitUntil: 'networkidle' });
  const html = await page.content();
  if (!html.includes(account.email)) {
    throw new Error(`Session inattendue : ${account.email} n'est pas connecte`);
  }
}

async function capture(page, shot) {
  await page.goto(`${BASE}${shot.url}`, { waitUntil: 'networkidle', timeout: 30000 });

  // Swagger UI : attendre le rendu des operations avant la capture.
  if (shot.url.includes('swagger-ui')) {
    await page.waitForSelector('.swagger-ui .opblock, .swagger-ui .info', { timeout: 20000 });
  }
  // Petite stabilisation (polices, images, transitions).
  await page.waitForTimeout(400);

  const target = path.join(outDir, shot.file);
  await page.screenshot({ path: target, fullPage: true });
  return target;
}

async function main() {
  await mkdir(outDir, { recursive: true });

  const browser = await chromium.launch({ channel: 'chrome', headless: true });

  const done = [];
  const skipped = [];
  // Un contexte (donc un jeu de cookies) par role : sinon le login d'un
  // utilisateur remplace la session des autres et les captures sont fausses.
  const pages = {};

  async function pageFor(role) {
    if (!pages[role]) {
      const page = await browser.newPage({
        viewport: VIEWPORT,
        deviceScaleFactor: 1,
        locale: 'fr-FR',
        timezoneId: 'Africa/Tunis',
      });
      if (role !== 'anon') {
        await login(page, ACCOUNTS[role]);
      }
      pages[role] = page;
    }
    return pages[role];
  }

  try {
    for (const shot of SHOTS) {
      const role = shot.role;

      if (role === 'admin' && (!ACCOUNTS.admin.email || !ACCOUNTS.admin.password)) {
        skipped.push(shot.file);
        console.log(`SKIP ${shot.file} (APP_ADMIN_EMAIL / APP_ADMIN_PASSWORD absents)`);
        continue;
      }

      const page = await pageFor(role);
      await capture(page, shot);
      done.push({
        file: shot.file,
        url: shot.url,
        label: shot.label,
        account: role === 'anon' ? 'Visiteur (non connecte)' : ACCOUNTS[role].label,
      });
      console.log(`OK   ${shot.file}  <- ${shot.url}  [${role}]`);
    }
  } finally {
    await browser.close();
  }

  await writeFile(
    path.join(outDir, 'manifest.json'),
    JSON.stringify({ base: BASE, viewport: VIEWPORT, captures: done, skipped }, null, 2),
    'utf8',
  );

  console.log(`\nTotal : ${done.length} capture(s), ${skipped.length} ignoree(s).`);
  if (skipped.length > 0) {
    console.log('Definir APP_ADMIN_EMAIL et APP_ADMIN_PASSWORD puis relancer pour les pages admin.');
    process.exitCode = 2;
  }
}

main().catch((err) => {
  console.error('ERREUR :', err.message);
  process.exitCode = 1;
});
