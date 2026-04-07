package com.senior.candleShopProject.feature.order.generator.PDF;

import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cms.CMSTypedData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

// helper class
public record CMSProcessableInputStream(InputStream input) implements CMSTypedData {

    @Override
    public Object getContent() {
        return input;
    }

    @Override
    public void write(OutputStream out) throws IOException {

        byte[] buffer = new byte[8192];
        int n;

        while ((n = input.read(buffer)) != -1) {
            out.write(buffer, 0, n);
        }

    }

    @Override
    public ASN1ObjectIdentifier getContentType() {
        return PKCSObjectIdentifiers.data;
    }
}