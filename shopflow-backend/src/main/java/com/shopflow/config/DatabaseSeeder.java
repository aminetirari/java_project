package com.shopflow.config;

import com.shopflow.entity.*;
import com.shopflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Base de données déjà initialisée.");
            return;
        }

        log.info("Initialisation de la base de données avec des données de test...");

        // 1. Création des utilisateurs
        User admin = User.builder()
                .prenom("Admin")
                .nom("Système")
                .email("admin@shopflow.com")
                .motDePasse(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .build();

        User customer = User.builder()
                .prenom("Jean")
                .nom("Dupont")
                .email("jean.dupont@shopflow.com")
                .motDePasse(passwordEncoder.encode("password123"))
                .role(Role.CUSTOMER)
                .build();

        User sellerUser = User.builder()
                .prenom("Marc")
                .nom("Marchand")
                .email("seller@techstore.com")
                .motDePasse(passwordEncoder.encode("password123"))
                .role(Role.SELLER)
                .build();

        userRepository.saveAll(List.of(admin, customer, sellerUser));
        log.info("Utilisateurs créés.");

        // 2. Profil Vendeur
        SellerProfile techStore = SellerProfile.builder()
                .user(sellerUser)
                .nomBoutique("Tech Store Paris")
                .description("Les meilleurs prix sur la high-tech et l'informatique !")
                .logo("https://ui-avatars.com/api/?name=Tech+Store&background=random")
                .note(4.8)
                .build();
        
        sellerProfileRepository.save(techStore);
        log.info("Profil vendeur créé.");

        // 3. Catégories
        Category electronics = Category.builder().nom("High-Tech").description("Appareils électroniques").build();
        Category gaming = Category.builder().nom("Gaming").description("Consoles et jeux vidéos").build();
        Category fashion = Category.builder().nom("Mode").description("Vêtements et accessoires").build();
        
        categoryRepository.saveAll(List.of(electronics, gaming, fashion));
        log.info("Catégories créées.");

        // 4. Produits
        Product laptop = Product.builder()
                .nom("MacBook Pro M3 16\"")
                .description("Le dernier ordinateur portable Apple surpuissant pour les pros.")
                .prix(2499.99)
                .stock(15)
                .seller(techStore)
                .categories(List.of(electronics))
                .images(List.of("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"))
                .build();

        Product mouse = Product.builder()
                .nom("Logitech MX Master 3S")
                .description("Souris sans fil ergonomique avec défilement ultra-rapide.")
                .prix(129.99)
                .prixPromo(99.99) // Article en promo
                .stock(50)
                .seller(techStore)
                .categories(List.of(electronics, gaming))
                .images(List.of("https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"))
                .build();

        Product headset = Product.builder()
                .nom("Casque Sony WH-1000XM5")
                .description("Casque à réduction de bruit active, qualité audio hi-res exceptionnel.")
                .prix(349.00)
                .stock(30)
                .seller(techStore)
                .categories(List.of(electronics))
                .images(List.of("https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?ixlib=rb-4.0.3&auto=format&fit=crop&w=1000&q=80"))
                .build();

        productRepository.saveAll(List.of(laptop, mouse, headset));
        log.info("Produits créés. Initialisation terminée avec succès !");
    }
}
