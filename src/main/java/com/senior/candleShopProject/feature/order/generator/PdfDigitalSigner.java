package com.senior.candleShopProject.feature.order.generator;

import com.senior.candleShopProject.common.utils.Constants;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.ExternalSigningSupport;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureOptions;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.asn1.cms.Time;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

public class PdfDigitalSigner {

    private final PrivateKey privateKey;

    private final Certificate[] certificateChain;

    static {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        }
    }

    public PdfDigitalSigner(KeyStore keyStore, String password) throws Exception {

//        name of keystore
        String alias = keyStore.aliases().nextElement();

//        get private key
        this.privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());

//        get certificate for signature
        this.certificateChain = keyStore.getCertificateChain(alias);
    }

    public byte[] signPdf(byte[] pdfBytes) throws Exception {

        ByteArrayOutputStream signedOutput = new ByteArrayOutputStream();
        Calendar signingTime = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        signingTime.setTime(new Date());

        try (
                PDDocument document = PDDocument.load(pdfBytes);
        ) {
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName(Constants.PDF_SIGN_NAME);
            signature.setLocation(Constants.PDF_SIGN_LOCATION);
            signature.setReason(Constants.PDF_SIGN_REASON);
            signature.setSignDate(signingTime);

            SignatureOptions options = new SignatureOptions();
            options.setPreferredSignatureSize(20000);

            document.addSignature(signature, options);

            ExternalSigningSupport externalSigning =
                    document.saveIncrementalForExternalSigning(signedOutput);

            byte[] cmsSignature = createCMS(externalSigning.getContent(),signingTime);

            externalSigning.setSignature(cmsSignature);

        }

        return signedOutput.toByteArray();

    }

//    Create PCS7/CMS signature for PDF document
    private byte[] createCMS(InputStream pdfStream, Calendar signingTime) throws Exception {

        List<Certificate> certList = Arrays.asList(certificateChain);

//        Store the certificate chain in a list
        JcaCertStore certStore = new JcaCertStore(certList);

//        Create signer By private key and SHA256withRSA algorithm, and certificate for signature
        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256withRSA")
                .setProvider("BC")
                .build(privateKey);

        Date signingDate = signingTime.getTime();

        ASN1EncodableVector signedAttributes = new ASN1EncodableVector();

        signedAttributes.add(
                new Attribute(
                        CMSAttributes.signingTime,
                        new DERSet(new Time(signingDate))
                )
        );

        AttributeTable signedAttrTable = new AttributeTable(signedAttributes);

        SignerInfoGenerator signerInfoGenerator =
                new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder()
                                .setProvider("BC")
                                .build()
                )
                        .setSignedAttributeGenerator(
                                new DefaultSignedAttributeTableGenerator(signedAttrTable)
                        )
                        .build(contentSigner, (X509Certificate) certificateChain[0]);

        CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
        generator.addSignerInfoGenerator(signerInfoGenerator);
        generator.addCertificates(certStore);


        CMSProcessableInputStream cmsData = new CMSProcessableInputStream(pdfStream);

        CMSSignedData signedData = generator.generate(cmsData, false);

        return signedData.toASN1Structure().getEncoded("DER");
    }

}
