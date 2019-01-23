package com.perkbox.util.token;

import com.perkbox.util.Env;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Date;
import java.util.MissingResourceException;
import java.util.zip.GZIPOutputStream;

public class TokenBuilder {

    private String permissionsJson;
    private String userId;
    private String tenant;
    private String email;
    private String secret;
    private int expiresIn;

    private static final String ISSUER = Env.get("ISSUER") != null ? Env.get("ISSUER") : "perkbox";
    private static final String TOKEN_KEY = Env.get("TOKEN_KEY") != null ? Env.get("TOKEN_KEY") : Env.get("PERKBOX_TOKEN_KEY");

    public TokenBuilder() {
        this.expiresIn = 1800;
    }

    public TokenBuilder withUser(String user) {
        this.userId = user;
        return this;
    }

    public TokenBuilder withTenant(String tenant) {
        this.tenant = tenant;
        return this;
    }

    public TokenBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public TokenBuilder lock(String secret) {
        this.secret = secret;
        return this;
    }

    public TokenBuilder expiresIn(int expiresIn) {
        this.expiresIn = expiresIn;
        return this;
    }

    public TokenBuilder withPermissions(String permissionsJson) {
        this.permissionsJson = permissionsJson;
        return this;
    }

    public String build() throws MissingResourceException, IOException {
        if (this.permissionsJson == null || this.permissionsJson.isEmpty())
            throw new MissingResourceException("Permissions JSON missing", "TokenBuilder", "permissionsJson");
        if (this.userId == null || this.userId.isEmpty())
            throw new MissingResourceException("User ID missing", "TokenBuilder", "userId");
        if (this.tenant == null || this.tenant.isEmpty())
            throw new MissingResourceException("Tenant missing", "TokenBuilder", "tenant");
        if (this.email == null || this.email.isEmpty())
            throw new MissingResourceException("Email missing", "TokenBuilder", "email");
        if (this.secret == null || this.secret.isEmpty())
            throw new MissingResourceException("Secret key missing", "TokenBuilder", "secret");

        return Jwts.builder().setHeaderParam("alg", "HS256").setHeaderParam("typ", "JWT").setIssuer(ISSUER)
                .setIssuedAt(new Date(System.currentTimeMillis())).setSubject(this.userId)
                .setExpiration(new Date(System.currentTimeMillis() + (this.expiresIn * 1000))).claim("usr", this.email)
                .claim("ten", this.tenant).claim("aut", JsonToEncoded(this.permissionsJson))
                .signWith(SignatureAlgorithm.HS256, this.secret.getBytes()).compact();
    }

    public String generateToken(String permissionJson, String UserUuid, String tenant, String userEmail, int expiry) {
        String token = null;

        try {
            token = (new TokenBuilder()).withUser(UserUuid).withTenant(tenant).withEmail(userEmail)
                    .withPermissions(permissionJson).expiresIn(expiry).lock(TOKEN_KEY).build();
        } catch (IOException e) {
            System.out.println("Unable to generate createToken: " + e.getMessage());
        }

        return token;
    }

    private String JsonToEncoded(String json) throws IOException {
        byte[] compressed = zip(json.getBytes());
        return Base64.getEncoder().encodeToString(compressed);
    }

    private byte[] zip(byte[] source) throws IOException {
        ByteArrayOutputStream obj = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(source);
        gzip.close();

        return obj.toByteArray();
    }
}