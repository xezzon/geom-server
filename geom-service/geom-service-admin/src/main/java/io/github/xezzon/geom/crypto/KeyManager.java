package io.github.xezzon.geom.crypto;

import cn.hutool.crypto.KeyUtil;
import cn.hutool.crypto.PemUtil;
import io.github.xezzon.geom.auth.event.PublicKeyGeneratedEvent;
import io.github.xezzon.geom.config.GeomConfig.GeomJwtConfig;
import io.github.xezzon.tao.observer.ObserverContext;
import io.micronaut.context.annotation.Context;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.interfaces.ECPrivateKey;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.io.pem.PemObject;

/**
 * TODO 优化扩展性 以便兼容其他加密算法
 */
@Singleton
@Context
@Slf4j
public class KeyManager {

  public static final String algorithm = "EC";
  private final transient GeomJwtConfig geomJwtConfig;
  private transient PrivateKey privateKey;

  public KeyManager(GeomJwtConfig geomJwtConfig) {
    this.geomJwtConfig = geomJwtConfig;
    ObserverContext.register(PublicKeyGeneratedEvent.class, this::printPublicKey);
    ObserverContext.register(PublicKeyGeneratedEvent.class, this::savePublicKeyToClasspath);
  }

  @PostConstruct
  public void loadPrivateKey() {
    /* 获取私钥文件 */
    String privateKeyFilename = geomJwtConfig.getIssuer() + ".pri";
    URL classpath = getClass().getClassLoader().getResource("");
    assert classpath != null;
    Path privateKeyPath = Path.of(privateKeyFilename);
    PublicKey publicKey = null;
    try {
      File privateKeyFile = Path.of(classpath.toURI())
          .resolve(privateKeyPath)
          .toFile();
      try (InputStream pemStream = new FileInputStream(privateKeyFile)) {
        /* 从文件中解析私钥 */
        PemObject pemObject = PemUtil.readPemObject(pemStream);
        this.privateKey = KeyUtil.generatePrivateKey(algorithm, pemObject.getContent());
      }
      /* 从私钥中提取公钥 */
      ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256k1");
      ECPoint Q = ecSpec.getG().multiply(((ECPrivateKey) this.privateKey).getD());
      ECPublicKeySpec pubSpec = new ECPublicKeySpec(Q, ecSpec);
      publicKey = KeyUtil.generatePublicKey(algorithm, pubSpec);
    } catch (Exception ignored) {
    }
    if (this.privateKey == null) {
      /* 获取不到文件或解析不了 则生成一对密钥 */
      KeyPair ecc = KeyUtil.generateKeyPair(algorithm);
      this.privateKey = ecc.getPrivate();
      publicKey = ecc.getPublic();
      /* 保存私钥 */
      try (FileWriter pemWriter = new FileWriter(
          Path.of(classpath.toURI()).resolve(privateKeyPath).toFile()
      )) {
        PemUtil.writePemObject("PRIVATE KEY", this.privateKey.getEncoded(), pemWriter);
      } catch (IOException | URISyntaxException ex) {
        throw new RuntimeException(ex);
      }
    }
    /* 广播公钥 */
    ObserverContext.post(new PublicKeyGeneratedEvent(publicKey));
  }

  public java.security.interfaces.ECPrivateKey getPrivateKey() {
    return (java.security.interfaces.ECPrivateKey) this.privateKey;
  }

  /**
   * 打印公钥到控制台
   * @param event 公钥
   */
  public void printPublicKey(PublicKeyGeneratedEvent event) {
    log.info("Current JWT Public Key is: {}", event.getPublicKey());
  }

  /**
   * 将公钥保存到文件中（PKCS8）
   * @param event 公钥
   */
  public void savePublicKeyToClasspath(PublicKeyGeneratedEvent event) {
    String publicKeyFilename = geomJwtConfig.getIssuer() + ".pub";
    URL classpath = getClass().getClassLoader().getResource("");
    assert classpath != null;
    try {
      File publicKeyFile = Path.of(classpath.toURI())
          .resolve(Path.of(publicKeyFilename))
          .toFile();
      try (FileWriter pemWriter = new FileWriter(publicKeyFile)) {
        PemUtil.writePemObject("PUBLIC KEY", event.publicKey().getEncoded(), pemWriter);
        log.info("JWT Public Key save to file: {}", publicKeyFile.getAbsoluteFile());
      }
    } catch (URISyntaxException | IOException e) {
      throw new RuntimeException(e);
    }
  }
}
