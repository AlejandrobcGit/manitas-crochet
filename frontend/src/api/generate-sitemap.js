import { mkdirSync, writeFileSync } from "node:fs";
import { resolve } from "node:path";
import { loadEnv } from "vite";

const mode = process.argv[2] || "production";

const env = loadEnv(mode, process.cwd(), "");

const rawSiteUrl = env.VITE_SITE_URL;

if (!rawSiteUrl) {
    console.error(
        `Error: falta VITE_SITE_URL en .env.${mode} o en las variables del entorno.`
    );
    process.exit(1);
}

let siteUrl;

try {
    siteUrl = new URL(rawSiteUrl).origin;
} catch {
    console.error(
        `Error: VITE_SITE_URL no es una URL válida: ${rawSiteUrl}`
    );
    process.exit(1);
}

/*
 * Incluye únicamente rutas públicas e indexables.
 *
 * No incluir:
 * - /login
 * - /registro
 * - /admin
 * - /perfil
 * - /favoritos
 * - rutas privadas
 */
const routes = [
    "/",
    "/sobre-nosotros"
];

const escapeXml = (value) =>
    value
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&apos;");

const urls = routes
    .map((route) => {
        const absoluteUrl = new URL(route, `${siteUrl}/`).href;

        return `  <url>
    <loc>${escapeXml(absoluteUrl)}</loc>
  </url>`;
    })
    .join("\n");

const sitemap = `<?xml version="1.0" encoding="UTF-8"?>
<urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
${urls}
</urlset>
`;

const publicDirectory = resolve(process.cwd(), "public");
const outputFile = resolve(publicDirectory, "sitemap.xml");

mkdirSync(publicDirectory, { recursive: true });
writeFileSync(outputFile, sitemap, "utf8");

console.log(`Sitemap generado: ${outputFile}`);
console.log(`Modo: ${mode}`);
console.log(`URL pública: ${siteUrl}`);
console.log(`URLs incluidas: ${routes.length}`);