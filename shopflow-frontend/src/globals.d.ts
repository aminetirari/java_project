// Ambient module declarations so TypeScript ne bronche pas sur les imports CSS
// (Next.js gère l'import via son plugin, mais certains IDE/linters stricts
// râlent sans déclaration).
declare module "*.css";
declare module "*.scss";
