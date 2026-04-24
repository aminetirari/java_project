// Ambient module declarations pour les imports CSS (y compris side-effect
// imports comme `import "./globals.css"`). TypeScript 5.x refuse un module
// ambiant sans corps pour ce type d'import, d'où l'export par défaut bidon.

declare module "*.css" {
  const content: { [className: string]: string };
  export default content;
}

declare module "*.scss" {
  const content: { [className: string]: string };
  export default content;
}

declare module "*.sass" {
  const content: { [className: string]: string };
  export default content;
}
