package com.coreflow.invoice.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;

@Service
public class InvoiceStorageService {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    public InvoiceStorageService(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * S'exécute automatiquement au démarrage pour vérifier ou créer le Bucket S3 dans LocalStack
     */
    @PostConstruct
    public void initBucket() {
        try {
            // Vérifie si le bucket existe déjà
            s3Client.headBucket(HeadBucketRequest.builder().bucket(bucketName).build());
        } catch (NoSuchBucketException e) {
            // S'il n'existe pas, on le crée
            s3Client.createBucket(CreateBucketRequest.builder().bucket(bucketName).build());
            System.out.println("Bucket S3 créé avec succès : " + bucketName);
        } catch (Exception e) {
            System.err.println("⚠Impossible de vérifier/créer le bucket S3 : " + e.getMessage());
        }
    }

    /**
     * Envoie une facture sur S3
     * @param key Nom/Chemin du fichier sur S3 (ex: invoices/order-102.pdf)
     * @param content Contenu binaire du fichier
     * @param contentType Type MIME (ex: application/pdf)
     * @return L'URL ou la clé du fichier stocké
     */
    public String uploadInvoice(String key, byte[] content, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(content));
        return key;
    }

    /**
     * Récupère le flux d'un fichier depuis S3
     */
    public InputStream downloadInvoice(String key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Client.getObject(getObjectRequest);
    }
}
