package com.marketplace.servicos.service;

import com.marketplace.servicos.exception.ResourceNotFoundException;
import com.marketplace.servicos.model.ConfiguracaoPush;
import com.marketplace.servicos.model.PushSubscription;
import com.marketplace.servicos.model.User;
import com.marketplace.servicos.repository.ConfiguracaoPushRepository;
import com.marketplace.servicos.repository.PushSubscriptionRepository;
import com.marketplace.servicos.repository.UserRepository;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Utils;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.interfaces.ECPublicKey;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.Security;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Service
public class WebPushService {

    private static final Logger log = LoggerFactory.getLogger(WebPushService.class);

    private final ConfiguracaoPushRepository pushConfigRepo;
    private final PushSubscriptionRepository subscriptionRepo;
    private final UserRepository userRepository;

    private PushService pushService;
    private String vapidPublicKey;

    public WebPushService(ConfiguracaoPushRepository pushConfigRepo,
                          PushSubscriptionRepository subscriptionRepo,
                          UserRepository userRepository) {
        this.pushConfigRepo = pushConfigRepo;
        this.subscriptionRepo = subscriptionRepo;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void init() {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
        try {
            ConfiguracaoPush config = inicializarChavesVapid();
            this.vapidPublicKey = config.getVapidPublicKey();
            this.pushService = new PushService(
                    config.getVapidPublicKey(),
                    config.getVapidPrivateKey(),
                    "mailto:admin@vemresolver.com"
            );
            log.info("[WebPush] Serviço de push inicializado. Chave pública: {}", this.vapidPublicKey);
        } catch (Exception e) {
            log.error("[WebPush] Falha ao inicializar serviço de push: {}", e.getMessage(), e);
        }
    }

    @Transactional
    protected ConfiguracaoPush inicializarChavesVapid() throws Exception {
        ConfiguracaoPush config = pushConfigRepo.findFirstByOrderByIdAsc()
                .orElseGet(ConfiguracaoPush::new);

        if (config.getVapidPublicKey() == null || config.getVapidPrivateKey() == null) {
            log.info("[WebPush] Gerando novo par de chaves VAPID...");

            ECNamedCurveParameterSpec paramSpec = ECNamedCurveTable.getParameterSpec("prime256v1");
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDH", "BC");
            keyGen.initialize(paramSpec, new SecureRandom());
            KeyPair keyPair = keyGen.generateKeyPair();

            ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
            ECPrivateKey privateKey = (ECPrivateKey) keyPair.getPrivate();

            String pubKeyB64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(Utils.encode(publicKey));
            String privKeyB64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(Utils.encode(privateKey));

            config.setVapidPublicKey(pubKeyB64);
            config.setVapidPrivateKey(privKeyB64);
            config.setAtualizadoEm(LocalDateTime.now());
            config = pushConfigRepo.save(config);
            log.info("[WebPush] Chaves VAPID geradas. Chave pública: {}", config.getVapidPublicKey());
        }
        return config;
    }

    public String getPublicKey() {
        return vapidPublicKey;
    }

    @Transactional
    public void subscribe(Long usuarioId, String endpoint, String p256dh, String auth) {
        User usuario = userRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        subscriptionRepo.findByUsuarioIdAndEndpoint(usuarioId, endpoint).ifPresentOrElse(
            sub -> { /* já existe, não duplicar */ },
            () -> {
                PushSubscription sub = new PushSubscription();
                sub.setUsuario(usuario);
                sub.setEndpoint(endpoint);
                sub.setP256dh(p256dh);
                sub.setAuth(auth);
                subscriptionRepo.save(sub);
                log.info("[WebPush] Subscription registrada para usuário {}", usuarioId);
            }
        );
    }

    @Transactional
    public void unsubscribe(Long usuarioId, String endpoint) {
        subscriptionRepo.findByUsuarioIdAndEndpoint(usuarioId, endpoint)
                .ifPresent(subscriptionRepo::delete);
        log.info("[WebPush] Subscription removida para usuário {}", usuarioId);
    }

    public void notificar(Long usuarioId, String titulo, String corpo) {
        if (pushService == null) return;
        List<PushSubscription> subs = subscriptionRepo.findByUsuarioId(usuarioId);
        if (subs.isEmpty()) return;

        String payload = String.format(
                "{\"notification\":{\"title\":\"%s\",\"body\":\"%s\",\"icon\":\"/assets/icons/icon-192x192.png\",\"data\":{\"url\":\"/\"}}}",
                titulo.replace("\"", "\\\""),
                corpo.replace("\"", "\\\""));

        for (PushSubscription sub : subs) {
            try {
                Notification notification = new Notification(
                        sub.getEndpoint(),
                        sub.getP256dh(),
                        sub.getAuth(),
                        payload.getBytes()
                );
                pushService.send(notification);
                log.info("[WebPush] Push enviado para usuário {}", usuarioId);
            } catch (Exception e) {
                log.warn("[WebPush] Falha ao enviar push para {}: {}", usuarioId, e.getMessage());
            }
        }
    }
}
