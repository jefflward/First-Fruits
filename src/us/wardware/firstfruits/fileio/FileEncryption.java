package us.wardware.firstfruits.fileio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

public class FileEncryption
{
    private static final char[] PASSWORD = "j0L8h2J1l1S9j9S9m".toCharArray();
    private static final byte[] SALT = {
        (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
        (byte) 0xde, (byte) 0x33, (byte) 0x10, (byte) 0x12,
    };

    public static void main(String[] args)
    {
        try {
            final FileInputStream fis = new FileInputStream("Password.ffdb");
            final FileOutputStream fos = new FileOutputStream("Encrypted.ffdb");
            encrypt(fis, fos);

            final FileInputStream fis2 = new FileInputStream("Encrypted.ffdb");
            final FileOutputStream fos2 = new FileOutputStream("Decrypted.ffdb");
            decrypt(fis2, fos2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String input) throws GeneralSecurityException, UnsupportedEncodingException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.ENCRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return Base64.encodeBase64String(pbeCipher.doFinal(input.getBytes("UTF-8")));
    }
    
    public static String decrypt(String input) throws GeneralSecurityException, IOException {
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
        SecretKey key = keyFactory.generateSecret(new PBEKeySpec(PASSWORD));
        Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
        pbeCipher.init(Cipher.DECRYPT_MODE, key, new PBEParameterSpec(SALT, 20));
        return new String(pbeCipher.doFinal(Base64.decodeBase64(input)), "UTF-8");
    }

    public static void encrypt(InputStream is, OutputStream os) throws Throwable
    {
        encryptOrDecrypt(Cipher.ENCRYPT_MODE, is, os);
    }

    public static void decrypt(InputStream is, OutputStream os) throws Throwable
    {
        encryptOrDecrypt(Cipher.DECRYPT_MODE, is, os);
    }
    
    public static void encryptOrDecrypt(int mode, InputStream is, OutputStream os) throws Throwable
    {
        final DESKeySpec dks = new DESKeySpec(new String(PASSWORD).getBytes());
        final SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
        final SecretKey desKey = skf.generateSecret(dks);
        final Cipher cipher = Cipher.getInstance("DES"); // DES/ECB/PKCS5Padding for SunJCE

        if (mode == Cipher.ENCRYPT_MODE) {
            cipher.init(Cipher.ENCRYPT_MODE, desKey);
            final CipherInputStream cis = new CipherInputStream(is, cipher);
            doCopy(cis, os);
        } else if (mode == Cipher.DECRYPT_MODE) {
            cipher.init(Cipher.DECRYPT_MODE, desKey);
            final CipherOutputStream cos = new CipherOutputStream(os, cipher);
            doCopy(is, cos);
        }
    }

    public static void doCopy(InputStream is, OutputStream os) throws IOException
    {
        final byte[] bytes = new byte[64];
        int numBytes;
        while ((numBytes = is.read(bytes)) != -1) {
            os.write(bytes, 0, numBytes);
        }
        os.flush();
        os.close();
        is.close();
    }
}
