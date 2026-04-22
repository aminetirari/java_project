#!/bin/bash
echo "🚀 Démarrage de ShopFlow..."

# Tuer les anciens processus si nécessaire
pkill -f spring-boot 2>/dev/null
pkill -f "next dev" 2>/dev/null

# Lancer le Backend en arrière-plan
echo "📦 Lancement du Backend (Spring Boot)..."
cd /workspaces/Java-Project/shopflow-backend
mvn spring-boot:run &
BACKEND_PID=$!

# Lancer le Frontend
echo "💻 Lancement du Frontend (Next.js)..."
cd /workspaces/Java-Project/shopflow-frontend
npm run dev

# Quand on arrête le script avec Ctrl+C, on arrête aussi le backend
trap "kill $BACKEND_PID" EXIT
