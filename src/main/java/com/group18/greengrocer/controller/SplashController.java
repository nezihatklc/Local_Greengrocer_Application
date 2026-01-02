package com.group18.greengrocer.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;

public class SplashController {

        @FXML
        private AnchorPane rootPane;

        @FXML
        private Group cartGroup;

        @FXML
        private Group cartBackGroup;

        @FXML
        private Group cartItemsGroup; // 🔑 sepetin içi

        // Items
        @FXML
        private Group appleGroup, bananaGroup, broccoliGroup, carrotGroup, pearGroup,
                        lettuceGroup, strawberryGroup, grapesGroup,
                        orangeGroup, potatoGroup, greenAppleGroup, lemonGroup,
                        tomatoGroup, extraCarrotGroup, extraStrawberryGroup;

        @FXML
        public void initialize() {

                Group[] items = {
                                appleGroup, bananaGroup, broccoliGroup, carrotGroup, pearGroup,
                                lettuceGroup, strawberryGroup, grapesGroup,
                                orangeGroup, potatoGroup, greenAppleGroup, lemonGroup,
                                tomatoGroup, extraCarrotGroup, extraStrawberryGroup
                };

                // 🔹 Ürünleri sepetin içine taşı
                for (Group item : items) {
                        // Eğer daha önce eklenmemişse taşı (FXML loading güvenliği için)
                        if (rootPane.getChildren().contains(item)) {
                                rootPane.getChildren().remove(item);
                                cartItemsGroup.getChildren().add(item);
                        }

                        // Sepet içinde rastgele yatay konum (0-110 arası)
                        item.setLayoutX(45 + Math.random() * 80);

                        // Başlangıç konumu: Sepetin çok üstü
                        // Local Y koordinatı. Sepetin altı +130, üstü +30.
                        // -250'den başlatıyoruz.
                        item.setLayoutY(-250);
                }

                SequentialTransition mainTransition = new SequentialTransition();

                // 1) Sepet giriş
                ParallelTransition cartSlideIn = new ParallelTransition(
                                createSlide(cartGroup, 0, 650),
                                createSlide(cartBackGroup, 0, 650));

                // 2) Ürün düşüşleri
                // HESAPLAMA:
                // Ürün başlangıç Y: -250
                // Hedeflenen sepet dibi (Local Y): ~125 (Sepet dibi 130)
                // Gerekli TranslateY = Hedef - Başlangıç = 125 - (-250) = 375

                // Batch 1: En alta gidecekler (Tabana oturacaklar: Y=115 civarı)
                // Sepet dibi Y=130. Elma yarıçap 18 -> Merkez 112 olmalı.
                // Başlangıç -250. Hedef fark: 112 - (-250) = 362.
                ParallelTransition drop1 = new ParallelTransition(
                                createDrop(appleGroup, 362),
                                createDrop(orangeGroup, 365),
                                createDrop(potatoGroup, 364));

                // Batch 2: Üstüne (Y=100-110)
                ParallelTransition drop2 = new ParallelTransition(
                                createDrop(bananaGroup, 355),
                                createDrop(broccoliGroup, 352),
                                createDrop(greenAppleGroup, 358));

                // Batch 3: Orta katman (Y=90-100)
                ParallelTransition drop3 = new ParallelTransition(
                                createDrop(carrotGroup, 345),
                                createDrop(pearGroup, 342),
                                createDrop(tomatoGroup, 348));

                // Batch 4: Üste doğru (Y=80-90)
                ParallelTransition drop4 = new ParallelTransition(
                                createDrop(lettuceGroup, 335),
                                createDrop(strawberryGroup, 332),
                                createDrop(lemonGroup, 338));

                // Batch 5: En tepe (Taşma efekti Y=70-80)
                ParallelTransition drop5 = new ParallelTransition(
                                createDrop(grapesGroup, 325),
                                createDrop(extraCarrotGroup, 328),
                                createDrop(extraStrawberryGroup, 330)); // 3) Sepet çıkış
                ParallelTransition exit = new ParallelTransition(
                                createSlide(cartGroup, 650, 1200),
                                createSlide(cartBackGroup, 650, 1200));

                for (Group item : items) {
                        TranslateTransition slideOut = new TranslateTransition(Duration.seconds(1.2), item);
                        slideOut.setByX(550);
                        slideOut.setInterpolator(Interpolator.EASE_IN);
                        exit.getChildren().add(slideOut);
                }

                mainTransition.getChildren().addAll(
                                cartSlideIn,
                                new PauseTransition(Duration.millis(120)),
                                drop1, drop2, drop3, drop4, drop5,
                                new PauseTransition(Duration.millis(600)),
                                exit);

                mainTransition.setOnFinished(e -> navigateToLogin());
                mainTransition.play();
        }

        private TranslateTransition createSlide(Group target, double fromX, double toX) {
                TranslateTransition slide = new TranslateTransition(Duration.seconds(1.0), target);
                slide.setToX(toX);
                slide.setInterpolator(toX > 700 ? Interpolator.EASE_IN : Interpolator.EASE_OUT);
                return slide;
        }

        private TranslateTransition createDrop(Group item, double targetY) {
                // Düşüşü biraz daha hızlandırdım (0.4s) ki sert çarpsın
                TranslateTransition fall = new TranslateTransition(Duration.seconds(0.40), item);
                fall.setToY(targetY);
                fall.setInterpolator(Interpolator.EASE_IN);

                fall.setOnFinished(e -> {
                        bounce(cartGroup);
                        bounce(cartBackGroup);
                });

                return fall;
        }

        private void bounce(Group target) {
                ScaleTransition bounce = new ScaleTransition(Duration.millis(70), target);
                bounce.setFromX(1.0);
                bounce.setToX(1.03);
                bounce.setFromY(1.0);
                bounce.setToY(0.97);
                bounce.setAutoReverse(true);
                bounce.setCycleCount(2);
                bounce.play();
        }

        private void navigateToLogin() {
                try {
                        Parent root = FXMLLoader.load(
                                        getClass().getResource("/com/group18/greengrocer/fxml/login.fxml"));
                        // Sadece sahne varsa işlem yap (Unit test vs. durumu için)
                        if (rootPane != null && rootPane.getScene() != null) {
                                Stage stage = (Stage) rootPane.getScene().getWindow();
                                stage.getScene().setRoot(root);
                                stage.setMaximized(true);
                        }
                } catch (IOException e) {
                        e.printStackTrace();
                }
        }
}
