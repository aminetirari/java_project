package com.shopflow.config;

import com.shopflow.entity.*;
import com.shopflow.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;
    private final CouponRepository couponRepository;
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Base de données déjà initialisée.");
            fixBrokenProductImages();
            return;
        }

        log.info("Initialisation de la base de données avec des données de test...");

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

        SellerProfile techStore = SellerProfile.builder()
                .user(sellerUser)
                .nomBoutique("Tech Store Paris")
                .description("Les meilleurs prix sur la high-tech et l'informatique !")
                .logo("https://ui-avatars.com/api/?name=Tech+Store&background=random")
                .note(4.8)
                .build();
        sellerProfileRepository.save(techStore);

        Category electronics = Category.builder().nom("High-Tech").description("Appareils électroniques").build();
        Category gaming = Category.builder().nom("Gaming").description("Consoles et jeux vidéos").build();
        Category fashion = Category.builder().nom("Mode").description("Vêtements et accessoires").build();
        Category maison = Category.builder().nom("Maison").description("Décoration et équipement").build();
        categoryRepository.saveAll(List.of(electronics, gaming, fashion, maison));

        Product laptop = Product.builder()
                .nom("MacBook Pro M3 16\"")
                .description("Le dernier ordinateur portable Apple surpuissant pour les pros.")
                .prix(2499.99)
                .stock(15)
                .seller(techStore)
                .categories(List.of(electronics))
                .images(List.of("https://images.unsplash.com/photo-1517336714731-489689fd1ca8?auto=format&fit=crop&w=1000&q=80"))
                .build();

        Product mouse = Product.builder()
                .nom("Logitech MX Master 3S")
                .description("Souris sans fil ergonomique avec défilement ultra-rapide.")
                .prix(129.99)
                .prixPromo(99.99)
                .stock(50)
                .seller(techStore)
                .categories(List.of(electronics, gaming))
                .images(List.of("https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?auto=format&fit=crop&w=1000&q=80"))
                .build();

        Product headset = Product.builder()
                .nom("Casque Sony WH-1000XM5")
                .description("Casque à réduction de bruit active, qualité audio hi-res exceptionnelle.")
                .prix(349.00)
                .stock(30)
                .seller(techStore)
                .categories(List.of(electronics))
                .images(List.of("https://images.unsplash.com/photo-1618366712010-f4ae9c647dcb?auto=format&fit=crop&w=1000&q=80"))
                .build();

        Product keyboard = Product.builder()
                .nom("Clavier Keychron K2")
                .description("Clavier mécanique sans fil compact avec rétroéclairage RGB.")
                .prix(99.00)
                .prixPromo(79.00)
                .stock(40)
                .seller(techStore)
                .categories(List.of(electronics, gaming))
                .images(List.of("https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=1000&q=80"))
                .build();

        Product monitor = Product.builder()
                .nom("Écran Dell UltraSharp 27\" 4K")
                .description("Moniteur 4K avec calibration couleur professionnelle.")
                .prix(649.00)
                .stock(12)
                .seller(techStore)
                .categories(List.of(electronics))
                .images(List.of("https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=1000&q=80"))
                .build();

        Product console = Product.builder()
                .nom("PlayStation 5")
                .description("Console de nouvelle génération avec manette DualSense.")
                .prix(549.99)
                .stock(8)
                .seller(techStore)
                .categories(List.of(gaming))
                .images(List.of("https://images.unsplash.com/photo-1621259182978-fbf93132d53d?auto=format&fit=crop&w=1000&q=80"))
                .build();

        Product smartphone = Product.builder()
                .nom("iPhone 15 Pro")
                .description("Smartphone haut de gamme avec puce A17 Pro et châssis titane.")
                .prix(1229.00)
                .stock(25)
                .seller(techStore)
                .categories(List.of(electronics))
                .images(List.of("https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=1000&q=80"))
                .build();

        Product tablet = Product.builder()
                .nom("iPad Air M2 11\"")
                .description("Tablette polyvalente pour le travail et le divertissement.")
                .prix(799.00)
                .prixPromo(749.00)
                .stock(20)
                .seller(techStore)
                .categories(List.of(electronics))
                .images(List.of("https://images.unsplash.com/photo-1544244015-0df4b3ffc6b0?auto=format&fit=crop&w=1000&q=80"))
                .build();

        productRepository.saveAll(List.of(laptop, mouse, headset, keyboard, monitor, console, smartphone, tablet));

        Address homeAddress = Address.builder()
                .user(customer)
                .rue("12 Rue de Rivoli")
                .ville("Paris")
                .codePostal("75001")
                .pays("France")
                .principal(true)
                .build();
        addressRepository.save(homeAddress);

        Coupon welcome = Coupon.builder()
                .code("BIENVENUE10")
                .type(CouponType.PERCENT)
                .valeur(new BigDecimal("10"))
                .dateExpiration(LocalDateTime.now().plusMonths(6))
                .usagesMax(1000)
                .usagesActuels(0)
                .actif(true)
                .build();

        Coupon fixe = Coupon.builder()
                .code("SHOPFLOW5")
                .type(CouponType.FIXED)
                .valeur(new BigDecimal("5"))
                .dateExpiration(LocalDateTime.now().plusMonths(3))
                .usagesMax(500)
                .usagesActuels(0)
                .actif(true)
                .build();

        couponRepository.saveAll(List.of(welcome, fixe));

        Review approvedReview = Review.builder()
                .customer(customer)
                .product(headset)
                .note(5)
                .commentaire("Qualité audio incroyable, vraiment le meilleur casque que j'ai essayé.")
                .approuve(true)
                .build();

        Review pendingReview1 = Review.builder()
                .customer(customer)
                .product(laptop)
                .note(4)
                .commentaire("Super machine, un peu chère mais elle vaut le coup. À modérer rapidement svp !")
                .approuve(false)
                .build();

        Review pendingReview2 = Review.builder()
                .customer(customer)
                .product(console)
                .note(2)
                .commentaire("Livraison longue et emballage abîmé. À vérifier.")
                .approuve(false)
                .build();

        reviewRepository.saveAll(List.of(approvedReview, pendingReview1, pendingReview2));

        log.info("Initialisation terminée : {} utilisateurs, {} produits, {} coupons, {} avis.",
                userRepository.count(), productRepository.count(), couponRepository.count(), reviewRepository.count());
    }

    /**
     * Remplace des URLs d'images cassées connues (Unsplash 404 sur certaines photos)
     * par des URLs équivalentes qui répondent 200. Idempotent : ne touche au produit
     * que si l'URL cassée est encore présente. La transaction est ouverte par
     * l'appelant ({@link #run(String...)}) car l'auto-invocation ne passe pas par
     * le proxy Spring.
     */
    void fixBrokenProductImages() {
        Map<String, String> replacements = Map.of(
                "https://images.unsplash.com/photo-1606813909355-008a1c3ebfd4?auto=format&fit=crop&w=1000&q=80",
                "https://images.unsplash.com/photo-1621259182978-fbf93132d53d?auto=format&fit=crop&w=1000&q=80"
        );
        for (Product product : productRepository.findAll()) {
            List<String> images = product.getImages();
            if (images == null || images.isEmpty()) continue;
            boolean changed = false;
            List<String> updated = new ArrayList<>(images.size());
            for (String url : images) {
                String replacement = replacements.get(url);
                if (replacement != null) {
                    updated.add(replacement);
                    changed = true;
                } else {
                    updated.add(url);
                }
            }
            if (changed) {
                product.setImages(updated);
                productRepository.save(product);
                log.info("Image corrigée pour le produit « {} »", product.getNom());
            }
        }
    }
}
