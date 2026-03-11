package com.senior.candleShopProject.feature.order.generator;

import com.senior.candleShopProject.common.utils.Constants;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class PdfDigitalSigner {

    private final PrivateKey privateKey;

    private final Certificate[] certificateChain;

    public PdfDigitalSigner(KeyStore keyStore, String password) throws Exception {

//        name of keystore
        String alias = keyStore.aliases().nextElement();

//        get private key
        this.privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());

//        get certificate for signature
        this.certificateChain = keyStore.getCertificateChain(alias);
    }

    public byte[] signPdf(byte[] pdfBytes) throws Exception {

        try (
                PDDocument document = PDDocument.load(pdfBytes);
                ByteArrayOutputStream signedOutput = new ByteArrayOutputStream()
        ) {

            PDSignature signature = new PDSignature();

            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);

            signature.setName(Constants.PDF_SIGN_NAME);
            signature.setLocation(Constants.PDF_SIGN_LOCATION);
            signature.setReason(Constants.PDF_SIGN_REASON);

            signature.setSignDate(Calendar.getInstance());

            document.addSignature(signature);

            ExternalSigningSupport externalSigning =
                    document.saveIncrementalForExternalSigning(signedOutput);

            byte[] cmsSignature = createCMS(externalSigning.getContent());

            externalSigning.setSignature(cmsSignature);

            document.close();

            return signedOutput.toByteArray();
        }
    }

//    Create PCS7/CMS signature for PDF document
    private byte[] createCMS(InputStream pdfStream) throws Exception {

        List<Certificate> certList = Arrays.asList(certificateChain);

//        Store the certificate chain in a list
        JcaCertStore certStore = new JcaCertStore(certList);

//        Create signer By private key and SHA256withRSA algorithm, and certificate for signature
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA").build(privateKey);

        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder().build()
                ).build(contentSigner, (X509Certificate) certificateChain[0])
        );

        generator.addCertificates(certStore);

        CMSProcessableInputStream cmsData = new CMSProcessableInputStream(pdfStream);

        CMSSignedData signedData = generator.generate(cmsData, false);

        return signedData.getEncoded();
    }

}
