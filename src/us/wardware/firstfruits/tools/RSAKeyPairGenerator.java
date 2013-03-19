package us.wardware.firstfruits.tools;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class RSAKeyPairGenerator
{
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException
    {
        final KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        return kpg.genKeyPair();
    }
}
